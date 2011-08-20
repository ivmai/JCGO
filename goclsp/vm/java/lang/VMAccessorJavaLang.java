/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMAccessorJavaLang.java --
 * VM cross-package access helper for "java.lang".
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
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

import gnu.java.nio.VMChannel;

import java.io.IOException;

public final class VMAccessorJavaLang
{ /* used by VM classes only */

 private VMAccessorJavaLang() {}

 public static Class arrayClassOfVMClass(Class klass, int dims)
 {
  return VMClass.arrayClassOf0X(klass, dims);
 }

 public static Class classForSigVMClass(String sig, Class klass)
 {
  return VMClass.classForSig(sig, klass);
 }

 public static int countStackFramesVMThread(Thread thread)
 {
  return VMThread.countStackFrames(thread);
 }

 public static void gcOnNoResourcesVMRuntime()
 {
  VMRuntime.gcOnNoResources();
 }

 public static ClassLoader getClassLoaderVMClass(Class klass)
 {
  return VMClass.getClassLoader(klass);
 }

 public static String getClassStatusVMClass(Class klass)
 {
  return VMClass.getClassStatus(klass);
 }

 public static String getCustomJavaPropsVMRuntime()
 {
  return VMRuntime.getCustomJavaProps();
 }

 public static Class[] getInterfacesInnerVMClass(Class klass)
 {
  return VMClass.getInterfacesInner(klass);
 }

 public static String getJavaExePathnameVMRuntime()
 {
  return VMRuntime.getJavaExePathname();
 }

 public static String getJavaVmVersionVMRuntime()
 {
  return VMRuntime.getJavaVmVersion();
 }

 public static int getModifiersVMClass(Class klass,
   boolean ignoreInnerClassesAttrib)
 {
  return VMClass.getModifiers(klass, ignoreInnerClassesAttrib);
 }

 public static int getPeakThreadCountVMThread(boolean reset)
 {
  return VMThread.getPeakThreadCount(reset);
 }

 public static String getSourceFilenameVMClass(Class klass)
 {
  return VMClass.getSourceFilename(klass);
 }

 public static long getStartTimeMillisVMThread()
 {
  return VMThread.getStartTimeMillis();
 }

 public static int getSuspendCountVMThread(Thread thread)
 {
  return VMThread.getSuspendCount(thread);
 }

 public static long getTotalStartedCountVMThread()
 {
  return VMThread.getTotalStartedCount();
 }

 public static boolean hasClassInitializerVMClass(Class klass)
 {
  return VMClass.hasClassInitializer(klass);
 }

 public static void initializeVMClass(Class klass)
 {
  VMClass.initialize(klass);
 }

 public static void parkVMThread(boolean isAbsolute, long time)
 {
  VMThread.park(isAbsolute, time);
 }

 public static void pipeVMProcess(VMChannel chIn, VMChannel chOut)
  throws IOException
 {
  VMProcess.pipe(chIn, chOut);
 }

 public static boolean preventIOBlockingVMRuntime()
 {
  return VMRuntime.preventIOBlocking();
 }

 public static void resumeNestedVMThread(Thread thread)
 {
  VMThread.resumeNested(thread);
 }

 public static void suspendNestedVMThread(Thread thread)
 {
  VMThread.suspendNested(thread);
 }

 public static void throwExceptionVMClass(Throwable throwable)
 {
  VMClass.throwException(throwable);
 }

 public static String toUpperCaseLatinVMSystem(String str)
 {
  return VMSystem.toUpperCaseLatin(str);
 }

 public static void unparkVMThread(Thread thread)
 {
  VMThread.unpark(thread);
 }
}
