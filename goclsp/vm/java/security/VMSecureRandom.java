/*
 * @(#) $(JCGO)/goclsp/vm/java/security/VMSecureRandom.java --
 * VM specific native SecureRandom "generateSeed" implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2008 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 **
 * Class specification origin: GNU Classpath v0.93 vm/reference
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

package java.security;

import gnu.java.security.hash.Sha160;

final class VMSecureRandom
{

 private VMSecureRandom() {}

 static int generateSeed(byte[] buf, int offset, int len)
 {
  long[] vals =
   new long[]
   {
    System.nanoTime(),
    Runtime.getRuntime().totalMemory(),
    Runtime.getRuntime().freeMemory()
   };
  Sha160 md = new Sha160();
  byte[] bytes = new byte[8];
  for (int i = 0; i < vals.length; i++)
  {
   long v = vals[i];
   int intval = (int) (v >>> 32);
   bytes[0] = (byte) (intval >>> 24);
   bytes[1] = (byte) (intval >>> 16);
   bytes[2] = (byte) (intval >>> 8);
   bytes[3] = (byte) intval;
   intval = (int) v;
   bytes[4] = (byte) (intval >>> 24);
   bytes[5] = (byte) (intval >>> 16);
   bytes[6] = (byte) (intval >>> 8);
   bytes[7] = (byte) intval;
   md.update(bytes, 0, 8);
  }
  int ofs = offset;
  while (len-- > 0)
  {
   bytes = md.digest();
   buf[ofs++] = bytes[0];
   md.update(bytes, 1, md.hashSize() - 1);
  }
  return ofs - offset;
 }
}
