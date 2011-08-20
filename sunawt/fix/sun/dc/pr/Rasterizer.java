/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)Rasterizer.java  1.15 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)Rasterizer.java 2.1 97/08/18
 *
 * ---------------------------------------------------------------------
 *      Copyright (c) 1996-1997 by Ductus, Inc. All Rights Reserved.
 * ---------------------------------------------------------------------
 *
 */

package sun.dc.pr;

import  sun.dc.path.*;

/**
 *  A Rasterizer is an object capable of turning a <b>path</b>
 *  description into rectangular arrays of alpha pixels as indicated
 *  by <b>path to alpha conversion</b> conventions (PACs).
 *  <p>
 *  A <b>rasterization cycle</b> - the sequence of method invocations
 *  used by a client to decribe path and PAC, and to retrieve the
 *  resulting alphas - must comform to the following protocol:
 *  <p>
 *  <b>Initialization and PAC Description:</b> The client declares
 *  what it intends to do with the path - e.g., fill it, stroke it -
 *  and any aditional information which affects the relation between
 *  the path and the corresponding alpha pixel - e.g., the size of the
 *  pen used to stroke the path. The Rasterizer stores this
 *  information.
 *  <p>
 *  <b>Path Description:</b> The client describes the path - in raster
 *  space - to the Rasterizer, which stores it as part of its
 *  state.
 *  <p>
 *  <b>Alpha Bounding Box Retrieval:</b> At this stage, the client may
 *  request the rasterizer to return the alpha's bounding box, a
 *  raster space rectangle outside of which alpha is uniformly
 *  zero.
 *  <p>
 *  <b>Definition of Output Area:</b> The client defines an
 *  arbitrary rectangle in raster space, the "output area" or
 *  OA.  Only the alpha pixels inside OA will be computed.
 *  <p>
 *  <b>Tile Retrieval:</b> OA divided in square "tiles." The tiles
 *  are retrieved one by one, in a conventional order. After the last
 *  tile is retrieved, the path rasterizer object resets itself to an
 *  initial state and can be used for a different path/PAC
 *  combination.
 *  <p>
 *  A rasterization cycle can be interrupted at any time by invoking
 *  <b>reset</b>.
 *
 * @version 3.0, 11 June 1997
 */
public class Rasterizer
{
    static public final int
        // usage
        EOFILL          =  1,
        NZFILL          =  2,
        STROKE          =  3,

        // caps, corners
        ROUND           = PathStroker.ROUND,
        SQUARE          = PathStroker.SQUARE,
        BUTT            = PathStroker.BUTT,
        BEVEL           = PathStroker.BEVEL,
        MITER           = PathStroker.MITER,

        // tile size
        TILE_SIZE       = 1 << PathFiller.tileSizeL2S,
        TILE_SIZE_L2S   = PathFiller.tileSizeL2S,

        // implementation limits
        MAX_ALPHA       = PathFiller.MAX_PATH,

        MAX_MITER       = 10,
        MAX_WN          = 63,

        // tile states
        TILE_IS_ALL_0   = PathFiller.TILE_IS_ALL_0,
        TILE_IS_ALL_1   = PathFiller.TILE_IS_ALL_1,
        TILE_IS_GENERAL = PathFiller.TILE_IS_GENERAL;

    private static final int    BEG             = 1,
                                PAC_FILL        = 2,
                                PAC_STROKE      = 3,
                                PATH            = 4,
                                SUBPATH         = 5,
                                RAS             = 6;
    private int                 state;

    private PathFiller          filler;
    private PathStroker         stroker;
    private PathDasher          dasher;
    private PathConsumer        curPC;

    public Rasterizer() {
        state   = BEG;
        filler  = new PathFiller();
        stroker = new PathStroker(filler);
        dasher  = new PathDasher(stroker);
    }

    /**
     *  Determines whether the path will be EOFILLed, NZFILLed or STROKEd.
     *  Mandatory; must be the first method used in a rasterization cycle.
     *  <p>
     *  After <tt>setUsage(STROKE)</tt>, the following state variables
     *  are implicitly set as per:
     *  <pre><tt>
     *      setPenDiameter(1.0);
     *      setPenT4(null);             // identity
     *      setPenDisplacement(null);   // (0,0)
     *      setCaps(ROUND);
     *      setCorners(ROUND, 0);
     *      setDash(null, 0.0);         // solid line
     *  </tt></pre>
     *
     * @param <tt>usage</tt>
     *  the usage selector
     * @exception PRError
     *  when invoked <ol>
     *  <li>    in a state other than between rasterization cycles (unexpected),
     *  <li>    with an invalid selector (unknow usage type).
     *  </ol>
     */
    public void setUsage(int usage) throws PRError {
        if (state != BEG) {
            throw new PRError(PRError.UNEX_setUsage);
        }
        if (usage == EOFILL) {
            filler.setFillMode(PathFiller.EOFILL);
            curPC = filler;
            state = PAC_FILL;
        } else if (usage == NZFILL) {
            filler.setFillMode(PathFiller.NZFILL);
            curPC = filler;
            state = PAC_FILL;
        } else if (usage == STROKE) {
            curPC = stroker;
            filler.setFillMode(PathFiller.NZFILL);
            stroker.setPenDiameter((float)1.0);
            stroker.setPenT4(null);
            stroker.setCaps(ROUND);
            stroker.setCorners(ROUND, 0);
            state = PAC_STROKE;
        } else {
            throw new PRError(PRError.UNK_usage);
        }

    }

    /**
     *  Sets the diameter of the pen used in stroking (the actual size and
     *  shape of the raster pen depends also on the transformation set by
     *  <tt>setPen(T4)</tt>. Optional after <tt>setUsage(STROKE)</tt>;
     *  forbidden otherwise.
     *
     * @param <tt>d</tt>
     *  the diameter of the pen
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>setUsage</tt>, after <tt>beginPath</tt>, or
     *       after <tt>setUsage(EOFILL)</tt> or <tt>setUsage(NZFILL)</tt>
     *       (unexpected);
     *  <li> with <tt>d &lt; 0</tt> (invalid pen diameter).
     *  </ol>
     */
    public void setPenDiameter(float d) throws PRError {
        if (state != PAC_STROKE) {
            throw new PRError(PRError.UNEX_setPenDiameter);
        }
        stroker.setPenDiameter(d);
        /* state   = PAC_STROKE;        // in same state */
    }

    /**
     *  Sets the transformation which transforms the circle defined by
     *  <tt>setPenDiameter</tt> into the (elliptical) raster pen.
     *  Optional after <tt>setUsage(STROKE)</tt> (default = identity);
     *  forbidden otherwise.
     *
     * @param <tt>t4</tt>
     *  a 4 coefficient transformation; null stands for the identity transformation
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>setUsage</tt>, after <tt>beginPath</tt>, or
     *       after <tt>setUsage(EOFILL)</tt> or <tt>setUsage(NZFILL)</tt>
     *       (unexpected),
     *  <li> with <tt>t4.length &lt; 4</tt> (invalid pen transformation),
     *  <li> with a non-inversible <tt>t4</tt> (invalid pen transformation (singular)).
     *  </ol>
     */
    public void setPenT4(float[] t4) throws PRError {
        if (state != PAC_STROKE) {
            throw new PRError(PRError.UNEX_setPenT4);
        }
        stroker.setPenT4(t4);
        /* state   = PAC_STROKE;        // in same state */
    }

    /**
     *  Changes the pen transformation to ensure that the ellipse obtained
     *  by applying the output transformation to the raster pen has vertical
     *  and horizontal projection which are (1) multiples of <tt>unit</tt>
     *  and (2) no smaller than <tt>unit*mindiameter</tt>.
     *  Optional after <tt>setUsage(STROKE)</tt> (default = no changes);
     *  forbidden otherwise.
     *
     * @param <tt>unit</tt>
     * @param <tt>mindiameter</tt>
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>setUsage</tt>, after <tt>beginPath</tt>, or
     *       after <tt>setUsage(EOFILL)</tt> or <tt>setUsage(NZFILL)</tt>
     *       (unexpected),
     *  <li> with <tt>unit &lt; 0</tt> or <tt>mindiameter &lt; 0</tt>
     *       (invalid pen fitting specification).
     *  </ol>
     */
    public void setPenFitting(float unit, int mindiameter) throws PRError {
        if (state != PAC_STROKE) {
            throw new PRError(PRError.UNEX_setPenFitting);
        }
        stroker.setPenFitting(unit, mindiameter);
        /* state   = PAC_STROKE;        // in same state */
    }

    /**
     *  Sets the displacement from the path trajectory to the center of
     *  the pen. Optional after <tt>setUsage(STROKE)</tt> (default (0,0));
     *  forbidden otherwise.
     *
     * @param <tt>dx</tt>
     *  the X displacement
     * @param <tt>dy</tt>
     *  the Y displacement
     * @exception PRError
     *  when invoked before <tt>setUsage</tt>, after <tt>beginPath</tt>, or
     *  after <tt>setUsage(EOFILL)</tt> or <tt>setUsage(NZFILL)</tt>
     *  (unexpected).
     */
    public void setPenDisplacement(float dx, float dy) throws PRError {
        if (state != PAC_STROKE) {
            throw new PRError(PRError.UNEX_setPenDisplacement);
        }
        float[] t6 = {(float)1.0, (float)0.0, (float)0.0, (float)1.0, dx, dy};
        stroker.setOutputT6(t6);
        /* state   = PAC_STROKE;        // in same state */
    }

    /**
     *  Sets the shape of the caps to either BUTT, ROUND or SQUARE.
     *  Optional after <tt>setUsage(STROKE)</tt> (default: ROUND);
     *  forbidden otherwise.
     *
     * @param <tt>caps</tt>
     *  the caps shape selector
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>setUsage</tt>, after <tt>beginPath</tt>, or
     *       after <tt>setUsage(EOFILL)</tt> or <tt>setUsage(NZFILL)</tt>
     *       (unexpected),
     *  <li> with an invalid selector (unknown cap type).
     *  </ol>
     */
    public void setCaps(int caps) throws PRError {
        if (state != PAC_STROKE) {
            throw new PRError(PRError.UNEX_setCaps);
        }
        stroker.setCaps(caps);
        /* state   = PAC_STROKE;        // in same state */
    }

    /**
     *  Sets the shape of the corners to either ROUND, BEVEL or MITER.
     *  Optional after <tt>setUsage(STROKE)</tt> (default: ROUND);
     *  forbidden otherwise.
     *
     * @param <tt>corners</tt>
     *  the corners shape selector
     * @param <tt>miter</tt>
     *  determines how sharp a mitered corner can be; otherwise irrelevant;
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>setUsage</tt>, after <tt>beginPath</tt>, or
     *       after <tt>setUsage(EOFILL)</tt> or <tt>setUsage(NZFILL)</tt>
     *       (unexpected),
     *  <li> with an invalid selector (unknown corner type).
     *  <li> with <tt>corners==MITER & miter &lt; 0</tt> (invalid miter limit).
     *  </ol>
     */
    public void setCorners(int corners, float miter) throws PRError {
        if (state != PAC_STROKE) {
            throw new PRError(PRError.UNEX_setCorners);
        }
        stroker.setCorners(corners, miter);
        /* state   = PAC_STROKE;        // in same state */
    }

    /**
     *  Sets the dash pattern and its initial offset. Optional after
     *  <tt>setUsage(STROKE)</tt> (default: solid line); forbidden
     *  otherwise.
     *
     * @param <tt>dash</tt>
     *  the dash pattern; its values are lengths, alternatively
     *  interpreted as dash and interdash lengths (the actual length
     *  depends also on their direction and on the transformation set by
     *  <tt>setDashT4</tt>); null stands for a solid line.
     * @param <tt>offset</tt>
     *  the initial offset in the dash pattern
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>setUsage</tt>, after <tt>beginPath</tt>, or
     *       after <tt>setUsage(EOFILL)</tt> or <tt>setUsage(NZFILL)</tt>
     *       (unexpected),
     *  <li> with a negative offset, with negative dash or interdash lengths, or
     *       with a dash pattern consisting only of zeroes
     *       (invalid dash pattern).
     *  </ol>
     */
    public void setDash(float[] dash, float offset) throws PRError {
        if (state != PAC_STROKE) {
            throw new PRError(PRError.UNEX_setDash);
        }
        dasher.setDash(dash, offset);
        curPC = dasher;                 // if the previous call is successful
        /* state = PAC_STROKE;          // in same state */
    }

    /**
     *  Sets the transformation which transforms the dash pattern defined
     *  by <tt>setDash</tt>. Optional after <tt>setUsage(STROKE)</tt>
     *  (default = identity); forbidden otherwise.
     *
     * @param <tt>t4</tt>
     *  a 4 coefficient transformation; null stands for the identity transformation;
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>setUsage</tt>, after <tt>beginPath</tt>, or
     *       after <tt>setUsage(EOFILL)</tt> or <tt>setUsage(NZFILL)</tt>
     *       (unexpected),
     *  <li> with <tt>t4.length &lt; 4</tt> (invalid dash transformation),
     *  <li> with a non-inversible <tt>t4</tt> (invalid dash transformation (singular)).
     *  </ol>
     */
    public void setDashT4(float[] dasht4) throws PRError {
        if (state != PAC_STROKE) {
            throw new PRError(PRError.UNEX_setDashT4);
        }
        dasher.setDashT4(dasht4);
        /* state   = PAC_STROKE;        // in same state */
    }

    /**
     *  Ends the PAC description and begins the path description. It is at
     *  this point that the PAC is validated. Mandatory.
     *
     * @param <tt>box</tt>
     *  an optional box (box may be null) containing all the points in the path;
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>setUsage</tt> or
     *       more than once in a rasterization cycle (unexpected),
     *  </ol>
     */
    public void beginPath(float[] box) throws PRError {
        beginPath();
    }
    /**
     *  Equivalent to beginPath(null).
     */
    public void beginPath() throws PRError {
        if (state != PAC_FILL && state != PAC_STROKE) {
            throw new PRError(PRError.UNEX_beginPath);
        }
        try {
            curPC.beginPath();
            state = PATH;
        } catch (PathError e) {
            throw new PRError(e.getMessage());
        }
    }

    /**
     *  Begins a new subpath. Mandatory after <tt>beginPath</tt>; and everytime
     *  when a new subpath begins.
     *
     * @param <tt>x0</tt>
     *  the X coordinate of the initial point
     * @param <tt>y0</tt>
     *  the Y coordinate of the initial point
     * @exception PRError
     *  when invoked  before <tt>beginpath</tt> or after <tt>endPath</tt>
     *  (unexpected).
     */
    public void beginSubpath(float x0, float y0) throws PRError {
        if (state != PATH && state != SUBPATH) {
            throw new PRError(PRError.UNEX_beginSubpath);
        }
        try {
            curPC.beginSubpath(x0, y0);
            state = SUBPATH;
        } catch (PathError e) {
            throw new PRError(e.getMessage());
        }
    }

    /**
     *  Appends a line arc to the current subpath. Optional.
     *
     * @param <tt>x1</tt>
     *  the X coordinate of the end point
     * @param <tt>y1</tt>
     *  the Y coordinate of the end point
     * @exception PRError
     *  when invoked before <tt>beginSubpath</tt> or after <tt>endPath</tt>
     *  (unexpected).
     */
    public void appendLine(float x1, float y1) throws PRError {
        if (state != SUBPATH) {
            throw new PRError(PRError.UNEX_appendLine);
        }
        try {
            curPC.appendLine(x1, y1);
        } catch (PathError e) {
            throw new PRError(e.getMessage());
        }
    }

    /**
     *  Appends a quadratic arc to the current subpath. Optional.
     *
     * @param <tt>xm</tt>
     *  the X coordinate of the control point;
     * @param <tt>ym</tt>
     *  the Y coordinate of the control point;
     * @param <tt>x1</tt>
     *  the X coordinate of the end point;
     * @param <tt>y1</tt>
     *  the Y coordinate of the end point;
     * @exception PRError
     *  when invoked before <tt>beginSubpath</tt> or after <tt>endPath</tt>
     *  (unexpected).
     */
    public void appendQuadratic(float xm, float ym, float x1, float y1)
        throws PRError {
        if (state != SUBPATH) {
            throw new PRError(PRError.UNEX_appendQuadratic);
        }
        try {
            curPC.appendQuadratic(xm, ym, x1, y1);
        } catch (PathError e) {
            throw new PRError(e.getMessage());
        }
    }

    /**
     *  Appends a cubic arc to the current subpath. Optional.
     *
     * @param <tt>xm</tt>
     *  the X coordinate of the 1st control point;
     * @param <tt>ym</tt>
     *  the Y coordinate of the 1st control point;
     * @param <tt>xn</tt>
     *  the X coordinate of the 2nd control point;
     * @param <tt>yn</tt>
     *  the Y coordinate of the 2nd control point;
     * @param <tt>x1</tt>
     *  the X coordinate of the end point;
     * @param <tt>y1</tt>
     *  the Y coordinate of the end point;
     * @exception PRError
     *  when invoked before <tt>beginSubpath</tt> or after <tt>endPath</tt>
     *  (unexpected).
     */
    public void appendCubic(float xm, float ym, float xn, float yn, float x1, float y1)
        throws PRError {
        if (state != SUBPATH) {
            throw new PRError(PRError.UNEX_appendCubic);
        }
        try {
            curPC.appendCubic(xm, ym, xn, yn, x1, y1);
        } catch (PathError e) {
            throw new PRError(e.getMessage());
        }
    }

    /**
     *  Declares that the current subpath will be closed. This may require
     *  to append a line arc connecting the last point to the first.
     *  Optional.
     *
     * @exception PRError
     *  when invoked before <tt>beginSubpath</tt> or after <tt>endPath</tt>
     *  (unexpected).
     */
    public void closedSubpath() throws PRError {
        if (state != SUBPATH)
            throw new PRError(PRError.UNEX_closedSubpath);
        try {
            curPC.closedSubpath();
        } catch (PathError e) {
            throw new PRError(e.getMessage());
        }
    }

    /**
     *
     *  Ends the path description. Mandatory.
     *
     * @exception PRError
     *  when invoked before <tt>beginPath</tt> or more than once in a rasterization cycle
     *  (unexpected).
     * @exception PRException
     *  when the rasterization would result in non-zero alpha values for pixel
     *  coordinates outside of [<tt>-MAX_ALPHA,MAX_ALPHA</tt>]
     *  (alpha coordinate out of bounds).
     */
    public void endPath() throws PRError, PRException {
        if (state != PATH && state != SUBPATH)
            throw new PRError(PRError.UNEX_endPath);
        try {
            curPC.endPath();
            state = RAS;
        } catch (PathError e) {
            throw new PRError(e.getMessage());
        } catch (PathException e) {
            String msg = e.getMessage();
            if (msg == null || !msg.startsWith("endPath:"))
                throw new PRException(msg);
        }
    }

    /**
     * Describes a path <i>by proxy</i>
     * @exception PRError
     *  when invoked at an inappropriate time (unexpected).
     * @see dc.path.FastPathProducer
     */
    public void useProxy(FastPathProducer proxy) throws PRError, PRException {
        if (state != PAC_FILL && state != PAC_STROKE) {
            throw new PRError(PRError.UNEX_useProxy);
        }
        try {
            curPC.useProxy(proxy);
            state = RAS;
        } catch (PathError e) {
            throw new PRError(e.getMessage());
        } catch (PathException e) {
            throw new PRException(e.getMessage());
        }
    }

    /**
     *  Returns in its argument array a box guaranteed to contain every
     *  non-zero alpha pixel. Optional. May be invoked <ol>
     *  <li> anytime after using <tt>beginPath</tt> with a non-null path box,
     *  <li> after <tt>endPath</tt>.
     *  </ol>
     *
     * @param <tt>box</tt>
     *  an array of no less than 4 entries where the extreme coordinates
     *  of the alpha box are placed;
     *
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>beginPath</tt> or
     *       after <tt>beginPath(null)</tt> but before <tt>endPath</tt>
     *       (unexpected),
     *  <li> with a statically invalid parameter
     *       (<tt>box == null || box.length &lt; 4</tt>)
     *       (invalid box destination array).
     *  </ol>
     */
    public void getAlphaBox(int[] box) throws PRError {
        filler.getAlphaBox(box);
    }

    /**
     *  Declares the output area (OA), a region of raster space
     *  containing all the pixels deemed interesting by the client.
     *  The pixels will be written by succeeding invocations to
     *  <tt>writeAlpha</tt>.
     *  <p>
     *  Pixels will be retrieved in "tiles", rectangular groups of maximum
     *  dimension less than or equal to <tt>TILE_SIZE</tt>.
     *  The region is tiled with maximal square tiles organized in "rows"
     *  extending in the direction X+. If we visualize the coordinate axii
     *  X/Y respectively pointing right/up, then the first tile is placed
     *  with its lower/left corner coincident with the lower/left corner
     *  of the output area. The first row consists of the first tile and
     *  possibly others placed to its right, as needed to completely cover
     *  the width of the output area. Rows are stacked above the first row
     *  as needed to completely cover the height of the output area.
     *  Tiles do not exceed OA; because the width and height of OA are not,
     *  in general, multiples of <tt>TILE_SIZE</tt>, the rightmost tiles may
     *  have a width &lt; <tt>TILE_SIZE</tt> and the uppermost tiles a height
     *  less than <tt>TILE_SIZE</tt>.
     *  <p>
     *  The method is mandatory. It must be used at least once, after the
     *  description of the path and before the retrival of tiles can
     *  begin.
     *
     * @param <tt>x0</tt>
     *  the low X boundary of the output area;
     * @param <tt>y0</tt>
     *  the low Y boundary of the output area;
     * @param <tt>w</tt>
     *  the (>0) X dimension of the output area;
     * @param <tt>h</tt>
     *  the (>0) Y dimension of the output area;
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>endPath</tt> (unexpected),
     *  <li> with a statically invalid output area
     *       (<tt>w &lt;= 0 | h &lt;= 0</tt>)
     *       (invalid output area),
     *  </ol>
     * @exception PRException
     *  when <tt>x0</tt>, <tt>y0</tt>, <tt>x0+w</tt> or <tt>y0+h</tt> fall outside
     *  [<tt>-MAX_ALPHA,MAX_ALPHA</tt>] (alpha coordinate out of bounds).
     */
    public void setOutputArea(float x0, float y0, int w, int h)
                        throws PRError, PRException {
        filler.setOutputArea(x0, y0, w, h);
    }

    /**
     *  Returns the state of the current tile. Optional.
     * @return either TILE_IS_ALL_0, TILE_IS_ALL_1 or TILE_IS_GENERAL
     *
     * @exception PRError
     *  when invoked before <tt>setOutputArea</tt> (unexpected),
     */
    public int getTileState() throws PRError {
        return filler.getTileState();   /* return current tile state */
    }

    /**
     *  Writes the alpha pixels of the current tile to pixel destination
     *  declared - the rectangular array alpha; this array is organized
     *  so the index difference between two pixels is <tt>xstride</tt>
     *  for X-adjacent pixels and <tt>ystride</tt> for Y-adjacent pixels.
     *  The pixel of lowest coordinates in the tile is written to the alpha
     *  array position <tt>pix0offset</tt>, depending on the alpha values -
     *  reals between 0 and 1 - are scaled by 255 (<tt>byte[] alpha</tt>)
     *  or 65535 (<tt>char[] alpha</tt>).
     *
     * @param <tt>alpha</tt>
     *  the array where pixels is to be placed;
     * @param <tt>xstride</tt>
     *  the index difference in the array of alphas (&gt;0) between two
     *  pixels adjacent in the X direction;
     * @param <tt>ystride</tt>
     *  the index difference in the array of alphas (&gt;0) between two
     *  pixels adjacent in the Y direction;
     * @param <tt>pix0ffset</tt>
     *  the offset (&gt;=0) of the lowest coordinates pixel in the array
     *  alpha.
     * @exception PRError
     *  when invoked <ol>
     *  <li> before <tt>setOutputArea</tt> (unexpected),
     *  <li> with <tt>alpha==null</tt>,
     *       <tt>xstride &lt;= 0 or ystride &lt;= 0</tt>
     *       (invalid alpha destination).
     *  </ol>
     * @exception PRException
     *  when the combination of the pixel destination parameters
     *  (<tt>alpha.length</tt>, <tt>xstride</tt> and <tt>ystride</tt>),
     *   <tt>pix0offset</tt> and dimensions of the current tile would
     *  otherwise result in an <tt>IndexOutOfBoundsException</tt>
     *  (alpha destination array too short).
     */
    public void writeAlpha(byte[] alpha, int xstride, int ystride, int pix0offset)
                        throws PRError, PRException, InterruptedException {
        filler.writeAlpha(alpha, xstride, ystride, pix0offset);
    }

    /**
     *  See writeAlpha(byte[] ...)
     */
    public void writeAlpha(char[] alpha, int xstride, int ystride, int pix0offset)
                        throws PRError, PRException, InterruptedException {
        filler.writeAlpha(alpha, xstride, ystride, pix0offset);
    }

    /**
     *  Advances to the next tile. Optional (but either <tt>writeAlpha</tt> or
     *  <tt>nextTile</tt> must be used in order to move to the next tile).
     * @exception PRError
     *  when invoked before <tt>setOutputArea</tt> (unexpected).
     */
    public void nextTile() throws PRError {
        filler.nextTile();
    }

    /**
     *  Mandatory; must be invoked to end a rasterization cycle.
     *  Can be invoked anytime.
     */
    public void reset() {
        state = BEG;
        filler.reset();
        stroker.reset();
        dasher.reset();
    }
}
