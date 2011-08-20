/*
 * @(#) $(JCGO)/goclsp/fpvm/java/lang/VMMath.java --
 * VM specific methods for Java "Math" class (non-native implementation).
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

package java.lang;

final class VMMath
{

 private VMMath() {}

 static double sin(double a)
 {
  return StrictMath.sin(a);
 }

 static double cos(double a)
 {
  return StrictMath.cos(a);
 }

 static double tan(double a)
 {
  return StrictMath.tan(a);
 }

 static double asin(double a)
 {
  return StrictMath.asin(a);
 }

 static double acos(double a)
 {
  return StrictMath.acos(a);
 }

 static double atan(double a)
 {
  return StrictMath.atan(a);
 }

 static double atan2(double y, double x)
 {
  return StrictMath.atan2(y, x);
 }

 static double exp(double a)
 {
  return StrictMath.exp(a);
 }

 static double log(double a)
 {
  return StrictMath.log(a);
 }

 static double sqrt(double a)
 {
  return StrictMath.sqrt(a);
 }

 static double pow(double a, double b)
 {
  return StrictMath.pow(a, b);
 }

 static double IEEEremainder(double x, double y)
 {
  return StrictMath.IEEEremainder(x, y);
 }

 static double ceil(double a)
 {
  return StrictMath.ceil(a);
 }

 static double floor(double a)
 {
  return StrictMath.floor(a);
 }

 static double rint(double a)
 {
  return StrictMath.rint(a);
 }

 static double cbrt(double a)
 {
  /* if (a == 0.0)
   return a;
  double v = pow(a < 0.0 ? -a : a, 1.0 / 3.0);
  return a < 0.0 ? -v : v; */
  return StrictMath.cbrt(a);
 }

 static double hypot(double a, double b)
 {
  if (a == 0.0 && b == 0.0)
   return 0.0;
  if (a < 0.0)
   a = -a;
  if (b < 0.0)
   b = -b;
  if (a < b)
  {
   double v = a;
   a = b;
   b = v;
  }
  b = a != b ? b / a : 1.0;
  return sqrt(b * b + 1.0) * a;
 }

 static double expm1(double a)
 {
  /* double v = exp(a) - 1.0;
  return v != 0.0 ? v : a; */
  return StrictMath.expm1(a);
 }

 static double log10(double a)
 {
  return log(a) * 0.434294481903251827651;
 }

 static double log1p(double a)
 {
  double v = a + 1.0;
  return v != 1.0 && a < Double.POSITIVE_INFINITY ?
          a / (v - 1.0) * log(v) : a;
 }

 static double sinh(double a)
 {
  /* boolean isneg = false;
  if (a < 0.0)
  {
   isneg = true;
   a = -a;
  }
  double v = expm1(a);
  if (v < Double.POSITIVE_INFINITY)
   v = (v / (v + 1.0) + v) * 0.5;
   else
   {
    v = exp(a * 0.5);
    v = (v * 0.5) * v;
   }
  return isneg ? -v : v; */
  return StrictMath.sinh(a);
 }

 static double cosh(double a)
 {
  /* if (a < 0.0)
   a = -a;
  double v = expm1(a);
  if (v < Double.POSITIVE_INFINITY)
  {
   double w = v + 1.0;
   v = v < 0.347 ? v * v / (w * 2.0) + 1.0 : w * 0.5 + 0.5 / w;
  }
   else
   {
    v = exp(a * 0.5);
    v = (v * 0.5) * v;
   }
  return v; */
  return StrictMath.cosh(a);
 }

 static double tanh(double a)
 {
  /* boolean isneg = false;
  if (a < 0.0)
  {
   isneg = true;
   a = -a;
  }
  double v;
  if (a < 1.0)
  {
   v = expm1(-2.0 * a);
   v = -v / (v + 2.0);
  }
   else
   {
    v = exp(a * 2.0);
    v = -2.0 / (v + 1.0) + 1.0;
   }
  return isneg ? -v : v; */
  return StrictMath.tanh(a);
 }
}
