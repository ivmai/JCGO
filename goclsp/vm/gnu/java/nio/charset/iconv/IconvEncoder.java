/*
 * @(#) $(JCGO)/goclsp/vm/gnu/java/nio/charset/iconv/IconvEncoder.java --
 * VM specific encoder wrapper for GNU libiconv.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2007 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 **
 * Class specification origin: GNU Classpath v0.93
 */

/*
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 **
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License (GPL) for more details.
 **
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 **
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module. An independent module is a module which is not derived from
 * or based on this library. If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

/* IconvEncoder.java --
   Copyright (C) 2005 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package gnu.java.nio.charset.iconv;

import gnu.classpath.Pointer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

final class IconvEncoder extends CharsetEncoder
{ /* VM class */

 private Pointer data;

 private int inremaining;

 private int outremaining;

 IconvEncoder(Charset cs, IconvMetaData info)
 {
  super(cs, info.averageBytesPerChar(), info.maxBytesPerChar());
  openIconv(info.iconvName());
 }

 private void openIconv(String name)
 {
  /* not implemented */
  throw new IllegalArgumentException(
         "IconvEncoder.openIconv() not implemented");
 }

 private int encode(char[] in, byte[] out, int posIn, int remIn, int posOut,
   int remOut)
 {
  /* not implemented */
  return 0;
 }

 private void closeIconv()
 {
  /* not implemented */
 }

 protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out)
 {
  int inPos = in.position();
  int outPos = out.position();
  int remIn = in.remaining();
  int remOut = out.remaining();
  char[] inArr;
  int ret;
  if (in.hasArray())
   inArr = in.array();
   else
   {
    inArr = new char[remIn];
    in.get(inArr);
   }
  if (out.hasArray())
  {
   ret = encode(inArr, out.array(), inPos, remIn, outPos, remOut);
   out.position(outPos + (remOut - outremaining));
  }
   else
    {
     byte[] outArr = new byte[remOut];
     ret = encode(inArr, outArr, inPos, remIn, outPos, remOut);
     out.put(outArr, 0, remOut - outremaining);
    }
  in.position(inPos + (remIn - inremaining));
  if (ret == 1)
   return CoderResult.malformedForLength(1);
  if (in.remaining() == 0)
   return CoderResult.UNDERFLOW;
  return CoderResult.OVERFLOW;
 }

 protected void finalize()
  throws Throwable
 {
  closeIconv();
  super.finalize();
 }
}
