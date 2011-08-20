/*
 * @(#) $(JCGO)/goclsp/vm/gnu/java/lang/management/VMMemoryMXBeanImpl.java --
 * VM specific memory bean implementation.
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

package gnu.java.lang.management;

import java.lang.management.MemoryUsage;

final class VMMemoryMXBeanImpl
{

 private static final Object[] lock = {};

 private VMMemoryMXBeanImpl() {}

 static MemoryUsage getHeapMemoryUsage()
 {
  Runtime runtime = Runtime.getRuntime();
  long totalMem = runtime.totalMemory();
  return new MemoryUsage(-1L, totalMem - runtime.freeMemory(), totalMem,
          runtime.maxMemory());
 }

 static MemoryUsage getNonHeapMemoryUsage()
 {
  long[] usageVals = new long[4];
  getNonHeapMemoryUsage0(usageVals);
  return new MemoryUsage(usageVals[0], usageVals[1], usageVals[2],
          usageVals[3]);
 }

 static boolean isVerbose()
 {
  return setVerbose0(0) != 0;
 }

 static void setVerbose(boolean verbose)
 {
  synchronized (lock)
  {
   setVerbose0(verbose ? 1 : -1);
  }
 }

 static native int getObjectPendingFinalizationCount(); /* JVM-core */

 private static native int getNonHeapMemoryUsage0(
   long[] usageVals); /* JVM-core */

 private static native int setVerbose0(int set); /* JVM-core */
}
