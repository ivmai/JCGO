/*
 * @(#) $(JCGO)/goclsp/vm/gnu/java/lang/management/MemoryPoolMXBeanImpl.java --
 * VM specific memory pool bean implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2007 Ivan Maidanski <ivmai@ivmaisoft.com>
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

package gnu.java.lang.management;

import java.lang.management.MemoryUsage;

final class VMMemoryPoolMXBeanImpl
{

 private VMMemoryPoolMXBeanImpl() {}

 static MemoryUsage getCollectionUsage(String name)
 {
  long[] usageVals = new long[4];
  getCollectionUsage0(name, usageVals);
  return new MemoryUsage(usageVals[0], usageVals[1], usageVals[2],
          usageVals[3]);
 }

 static native long getCollectionUsageThreshold(String name); /* JVM-core */

 static native long getCollectionUsageThresholdCount(
   String name); /* JVM-core */

 static native String[] getMemoryManagerNames(String name); /* JVM-core */

 static MemoryUsage getPeakUsage(String name)
 {
  long[] usageVals = new long[4];
  getPeakUsage0(name, usageVals);
  return new MemoryUsage(usageVals[0], usageVals[1], usageVals[2],
          usageVals[3]);
 }

 static String getType(String name)
 {
  return getType0(name) != 0 ? "NON_HEAP" : "HEAP";
 }

 static MemoryUsage getUsage(String name)
 {
  long[] usageVals = new long[4];
  getUsage0(name, usageVals);
  return new MemoryUsage(usageVals[0], usageVals[1], usageVals[2],
          usageVals[3]);
 }

 static native long getUsageThreshold(String name); /* JVM-core */

 static native long getUsageThresholdCount(String name); /* JVM-core */

 static boolean isValid(String name)
 {
  return isValid0(name) != 0;
 }

 static native void resetPeakUsage(String name); /* JVM-core */

 static native void setCollectionUsageThreshold(String name,
   long threshold); /* JVM-core */

 static native void setUsageThreshold(String name,
   long threshold); /* JVM-core */

 private static native int getCollectionUsage0(String name,
  long[] usageVals); /* JVM-core */

 private static native int getPeakUsage0(String name,
   long[] usageVals); /* JVM-core */

 private static native int getType0(String name); /* JVM-core */

 private static native int getUsage0(String name,
   long[] usageVals); /* JVM-core */

 private static native int isValid0(String name); /* JVM-core */
}
