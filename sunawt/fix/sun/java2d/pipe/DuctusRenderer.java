/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)DuctusRenderer.java      1.16 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.java2d.pipe;

import java.awt.geom.AffineTransform;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import sun.dc.path.PathConsumer;
import sun.dc.path.PathException;
import sun.dc.pr.Rasterizer;
import sun.dc.pr.PathStroker;
import sun.dc.pr.PathDasher;
import sun.dc.pr.PRException;

/**
 * This is the superclass of any class that needs to use the Ductus
 * Rasterizer.
 * This class provides utility routines to create, cache, initialize
 * and release Rasterizer objects.
 */
public class DuctusRenderer {
    public static final float PenUnits = 0.01f;
    public static final int MinPenUnits = 100;
    public static final int MinPenUnitsAA = 20;
    public static final float MinPenSizeAA = PenUnits * MinPenUnitsAA;

    static final float UPPER_BND = 1.701412E+038F;
    static final float LOWER_BND = -UPPER_BND;

    static final int RasterizerCaps[] = {
        Rasterizer.BUTT, Rasterizer.ROUND, Rasterizer.SQUARE
    };

    static final int RasterizerCorners[] = {
        Rasterizer.MITER, Rasterizer.ROUND, Rasterizer.BEVEL
    };

    private static Rasterizer theRasterizer;

    public synchronized static Rasterizer getRasterizer() {
        Rasterizer r = theRasterizer;
        if (r == null) {
            r = new Rasterizer();
        } else {
            theRasterizer = null;
        }
        return r;
    }

    public synchronized static void dropRasterizer(Rasterizer r) {
        r.reset();
        theRasterizer = r;
    }

    private static byte[] theTile;

    public synchronized static byte[] getAlphaTile() {
        byte[] t = theTile;
        if (t == null) {
            int dim = Rasterizer.TILE_SIZE;
            t = new byte[dim * dim];
        } else {
            theTile = null;
        }
        return t;
    }

    public synchronized static void dropAlphaTile(byte[] t) {
        theTile = t;
    }

    /*
     * writeAlpha is not threadsafe (it uses a global table without
     * locking), so we must use this static synchronized accessor
     * method to serialize accesses to it.
     */
    public synchronized static void getAlpha(Rasterizer r, byte[] alpha,
                                             int xstride, int ystride,
                                             int offset)
        throws PRException
    {
        try {
            r.writeAlpha(alpha, xstride, ystride, offset);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /*
     * Returns a new PathConsumer that will take a path trajectory and
     * feed the stroked outline for that trajectory to the supplied
     * PathConsumer.
     */
    public static PathConsumer createStroker(PathConsumer consumer,
                                             BasicStroke bs, boolean thin,
                                             AffineTransform transform) {
        PathStroker stroker = new PathStroker(consumer);
        consumer = stroker;

        float matrix[] = null;
        if (!thin) {
            stroker.setPenDiameter(bs.getLineWidth());
            if (transform != null) {
                matrix = new float[4];
                double dmatrix[] = new double[6];
                transform.getMatrix(dmatrix);
                for (int i = 0; i < 4; i++) {
                    matrix[i] = (float) dmatrix[i];
                }
            }
            stroker.setPenT4(matrix);
            stroker.setPenFitting(PenUnits, MinPenUnits);
        }
        stroker.setCaps(RasterizerCaps[bs.getEndCap()]);
        stroker.setCorners(RasterizerCorners[bs.getLineJoin()],
                           bs.getMiterLimit());
        float[] dashes = bs.getDashArray();
        if (dashes != null) {
            PathDasher dasher = new PathDasher(stroker);
            dasher.setDash(dashes, bs.getDashPhase());
            dasher.setDashT4(matrix);
            consumer = dasher;
        }

        return consumer;
    }

    /*
     * Feed a path from a PathIterator to a Ductus PathConsumer.
     */
    public static void feedConsumer(PathIterator pi, PathConsumer consumer,
                                    boolean normalize, float norm)
        throws PathException
    {
        consumer.beginPath();
        boolean pathClosed = false;
        boolean subpathBegin = false;
        boolean subpathOpened = false;
        float mx = 0.0f;
        float my = 0.0f;
        float point[]  = new float[6];
        float rnd = (0.5f - norm);
        float ax = 0.0f;
        float ay = 0.0f;

        while (!pi.isDone()) {
            int type = pi.currentSegment(point);
            if (pathClosed == true) {
                pathClosed = false;
                if (type != PathIterator.SEG_MOVETO) {
                    // Force current point back to last moveto point
                    consumer.beginSubpath(mx, my);
                    subpathOpened = true;
                }
            }
            if (normalize) {
                int index;
                switch (type) {
                case PathIterator.SEG_CUBICTO:
                    index = 4;
                    break;
                case PathIterator.SEG_QUADTO:
                    index = 2;
                    break;
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    index = 0;
                    break;
                case PathIterator.SEG_CLOSE:
                default:
                    index = -1;
                    break;
                }
                if (index >= 0) {
                    float ox = point[index];
                    float oy = point[index+1];
                    float newax = (float) Math.floor(ox + rnd) + norm;
                    float neway = (float) Math.floor(oy + rnd) + norm;
                    point[index] = newax;
                    point[index+1] = neway;
                    newax -= ox;
                    neway -= oy;
                    switch (type) {
                    case PathIterator.SEG_CUBICTO:
                        point[0] += ax;
                        point[1] += ay;
                        point[2] += newax;
                        point[3] += neway;
                        break;
                    case PathIterator.SEG_QUADTO:
                        point[0] += (newax + ax) / 2;
                        point[1] += (neway + ay) / 2;
                        break;
                    case PathIterator.SEG_MOVETO:
                    case PathIterator.SEG_LINETO:
                    case PathIterator.SEG_CLOSE:
                        break;
                    }
                    ax = newax;
                    ay = neway;
                }
            }
            switch (type) {
            default:
                break;
            case PathIterator.SEG_MOVETO:
                if (point[0] < UPPER_BND && point[0] > LOWER_BND &&
                    point[1] < UPPER_BND && point[1] > LOWER_BND) {
                    mx = point[0];
                    my = point[1];
                    consumer.beginSubpath(mx, my);
                    subpathOpened = true;
                    subpathBegin = false;
                } else {
                    subpathBegin = true;
                }
                break;
            case PathIterator.SEG_LINETO:
                if (point[0] >= UPPER_BND || point[0] <= LOWER_BND ||
                    point[1] >= UPPER_BND || point[1] <= LOWER_BND) {
                    break;
                }
                if (subpathBegin) {
                    consumer.beginSubpath(point[0], point[1]);
                    subpathOpened = true;
                    subpathBegin = false;
                } else {
                    consumer.appendLine(point[0], point[1]);
                }
                break;
            case PathIterator.SEG_QUADTO:
                // Quadratic curves take two points
                if (point[2] >= UPPER_BND || point[2] <= LOWER_BND ||
                    point[3] >= UPPER_BND || point[3] <= LOWER_BND) {
                    break;
                }
                if (subpathBegin) {
                    consumer.beginSubpath(point[2], point[3]);
                    subpathOpened = true;
                    subpathBegin = false;
                    break;
                }
                if (point[0] < UPPER_BND && point[0] > LOWER_BND &&
                    point[1] < UPPER_BND && point[1] > LOWER_BND) {
                    consumer.appendQuadratic(point[0], point[1],
                                             point[2], point[3]);
                } else {
                    consumer.appendLine(point[2], point[3]);
                }
                break;
            case PathIterator.SEG_CUBICTO:
                // Cubic curves take three points
                if (point[4] >= UPPER_BND || point[4] <= LOWER_BND ||
                    point[5] >= UPPER_BND || point[5] <= LOWER_BND) {
                    break;
                }
                if (subpathBegin) {
                    consumer.beginSubpath(point[4], point[5]);
                    subpathOpened = true;
                    subpathBegin = false;
                    break;
                }
                if (point[0] < UPPER_BND && point[0] > LOWER_BND &&
                    point[1] < UPPER_BND && point[1] > LOWER_BND &&
                    point[2] < UPPER_BND && point[2] > LOWER_BND &&
                    point[3] < UPPER_BND && point[3] > LOWER_BND) {
                    consumer.appendCubic(point[0], point[1],
                                         point[2], point[3],
                                         point[4], point[5]);
                } else {
                    consumer.appendLine(point[4], point[5]);
                }
                break;
            case PathIterator.SEG_CLOSE:
                if (subpathOpened) {
                    consumer.closedSubpath();
                    subpathOpened = false;
                    pathClosed = true;
                }
                break;
            }
            pi.next();
        }

        consumer.endPath();
    }

    /*
     * Returns a new Rasterizer that is ready to rasterize the given Shape.
     */
    public static Rasterizer createShapeRasterizer(PathIterator pi,
                                                   AffineTransform transform,
                                                   BasicStroke stroke,
                                                   boolean thin,
                                                   boolean normalize,
                                                   float norm) {
        Rasterizer r = getRasterizer();

        if (stroke != null) {
            float matrix[] = null;
            r.setUsage(Rasterizer.STROKE);
            if (thin) {
                r.setPenDiameter(MinPenSizeAA);
            } else {
                r.setPenDiameter(stroke.getLineWidth());
                if (transform != null) {
                    matrix = new float[4];
                    double dmatrix[] = new double[6];
                    transform.getMatrix(dmatrix);
                    for (int i = 0; i < 4; i++) {
                        matrix[i] = (float) dmatrix[i];
                    }
                    r.setPenT4(matrix);
                }
                r.setPenFitting(PenUnits, MinPenUnitsAA);
            }
            r.setCaps(RasterizerCaps[stroke.getEndCap()]);
            r.setCorners(RasterizerCorners[stroke.getLineJoin()],
                         stroke.getMiterLimit());
            float[] dashes = stroke.getDashArray();
            if (dashes != null) {
                r.setDash(dashes, stroke.getDashPhase());
                r.setDashT4(matrix);
            }
        } else {
            r.setUsage(pi.getWindingRule() == PathIterator.WIND_EVEN_ODD
                       ? Rasterizer.EOFILL
                       : Rasterizer.NZFILL);
        }

        r.beginPath();
        {
            boolean pathClosed = false;
            boolean subpathBegin = false;
            boolean subpathOpened = false;
            float mx = 0.0f;
            float my = 0.0f;
            float point[]  = new float[6];
            float rnd = (0.5f - norm);
            float ax = 0.0f;
            float ay = 0.0f;

            while (!pi.isDone()) {
                int type = pi.currentSegment(point);
                if (pathClosed == true) {
                    pathClosed = false;
                    if (type != PathIterator.SEG_MOVETO) {
                        // Force current point back to last moveto point
                        r.beginSubpath(mx, my);
                        subpathOpened = true;
                    }
                }
                if (normalize) {
                    int index;
                    switch (type) {
                    case PathIterator.SEG_CUBICTO:
                        index = 4;
                        break;
                    case PathIterator.SEG_QUADTO:
                        index = 2;
                        break;
                    case PathIterator.SEG_MOVETO:
                    case PathIterator.SEG_LINETO:
                        index = 0;
                        break;
                    case PathIterator.SEG_CLOSE:
                    default:
                        index = -1;
                        break;
                    }
                    if (index >= 0) {
                        float ox = point[index];
                        float oy = point[index+1];
                        float newax = (float) Math.floor(ox + rnd) + norm;
                        float neway = (float) Math.floor(oy + rnd) + norm;
                        point[index] = newax;
                        point[index+1] = neway;
                        newax -= ox;
                        neway -= oy;
                        switch (type) {
                        case PathIterator.SEG_CUBICTO:
                            point[0] += ax;
                            point[1] += ay;
                            point[2] += newax;
                            point[3] += neway;
                            break;
                        case PathIterator.SEG_QUADTO:
                            point[0] += (newax + ax) / 2;
                            point[1] += (neway + ay) / 2;
                            break;
                        case PathIterator.SEG_MOVETO:
                        case PathIterator.SEG_LINETO:
                        case PathIterator.SEG_CLOSE:
                            break;
                        }
                        ax = newax;
                        ay = neway;
                    }
                }
                switch (type) {
                default:
                    break;
                case PathIterator.SEG_MOVETO:
                    if (point[0] < UPPER_BND && point[0] > LOWER_BND &&
                        point[1] < UPPER_BND && point[1] > LOWER_BND) {
                        mx = point[0];
                        my = point[1];
                        r.beginSubpath(mx, my);
                        subpathOpened = true;
                        subpathBegin = false;
                    } else {
                        subpathBegin = true;
                    }
                    break;
                case PathIterator.SEG_LINETO:
                    if (point[0] >= UPPER_BND || point[0] <= LOWER_BND ||
                        point[1] >= UPPER_BND || point[1] <= LOWER_BND) {
                        break;
                    }
                    if (subpathBegin) {
                        r.beginSubpath(point[0], point[1]);
                        subpathOpened = true;
                        subpathBegin = false;
                    } else {
                        r.appendLine(point[0], point[1]);
                    }
                    break;
                case PathIterator.SEG_QUADTO:
                    // Quadratic curves take two points
                    if (point[2] >= UPPER_BND || point[2] <= LOWER_BND ||
                        point[3] >= UPPER_BND || point[3] <= LOWER_BND) {
                        break;
                    }
                    if (subpathBegin) {
                        r.beginSubpath(point[2], point[3]);
                        subpathOpened = true;
                        subpathBegin = false;
                        break;
                    }
                    if (point[0] < UPPER_BND && point[0] > LOWER_BND &&
                        point[1] < UPPER_BND && point[1] > LOWER_BND) {
                        r.appendQuadratic(point[0], point[1],
                                          point[2], point[3]);
                    } else {
                        r.appendLine(point[2], point[3]);
                    }
                    break;
                case PathIterator.SEG_CUBICTO:
                    // Cubic curves take three points
                    if (point[4] >= UPPER_BND || point[4] <= LOWER_BND ||
                        point[5] >= UPPER_BND || point[5] <= LOWER_BND) {
                        break;
                    }
                    if (subpathBegin) {
                        r.beginSubpath(point[4], point[5]);
                        subpathOpened = true;
                        subpathBegin = false;
                        break;
                    }
                    if (point[0] < UPPER_BND && point[0] > LOWER_BND &&
                        point[1] < UPPER_BND && point[1] > LOWER_BND &&
                        point[2] < UPPER_BND && point[2] > LOWER_BND &&
                        point[3] < UPPER_BND && point[3] > LOWER_BND) {
                        r.appendCubic(point[0], point[1],
                                      point[2], point[3],
                                      point[4], point[5]);
                    } else {
                        r.appendLine(point[4], point[5]);
                    }
                    break;
                case PathIterator.SEG_CLOSE:
                    if (subpathOpened) {
                        r.closedSubpath();
                        subpathOpened = false;
                        pathClosed = true;
                    }
                    break;
                }
                pi.next();
            }
        }

        try {
            r.endPath();
        } catch (PRException e) {
            e.printStackTrace();
        }

        return r;
    }
}
