/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMFloat.java --
 * VM specific methods for Java "Float" class.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
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

package java.lang;

final class VMFloat
{

 private VMFloat() {}

 static int floatToIntBits(float value)
 {
  int bits = floatToRawIntBits(value);
  if ((bits & (-1 >>> 1)) > (-1 >>> 1) - (-1 >>> 9))
   bits = (-1 >>> 1) - (-1 >>> 10);
  return bits;
 }

 static native int floatToRawIntBits(float value); /* JVM-core */

 static native float intBitsToFloat(int bits); /* JVM-core */

 static String toString(float value)
 {
  return VMDouble.toString(value, true);
 }

 static float parseFloat(String str)
 {
  return VMDouble.parseFloat(str);
 }
}
