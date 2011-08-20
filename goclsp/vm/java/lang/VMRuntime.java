/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMRuntime.java --
 * VM specific methods for Java "Runtime" class.
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

import java.io.File;
import java.io.IOException;
import java.io.VMAccessorJavaIo;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.List;
import java.util.Map;

final class VMRuntime /* hard-coded class name */
{

 static final class TermHandler /* hard-coded class name */
 { /* used by VM classes only */

  static
  {
   handleSigTerm0X(-1); /* hack */
  }

  TermHandler() {}
 }

 private static final class FinalizerThread extends Thread
 {

  private static final int MAX_NOTIFY_COUNT_DELTA = 10;

  private static final Object[] lock = {};

  private static int stalledThreadCnt;

  private static int secondaryThreadCnt;

  private static int notifyCount;

  private boolean noWait;

  private int runFinalizeCount;

  private FinalizerThread(boolean noWait, String name, int priority)
  {
   super((VMThread) null, name, priority, true);
   VMThread.rootGroupAdd(this);
   this.noWait = noWait;
  }

  public void run()
  {
   synchronized (lock)
   {
    runFinalizeCount = notifyCount;
   }
   do
   {
    try
    {
     do
     {
      runFinalization0();
      synchronized (lock)
      {
       if (noWait)
        return;
       if (runFinalizeCount == notifyCount)
        VMThread.wait(lock, 0L, 0);
       runFinalizeCount = notifyCount;
      }
     } while (true);
    }
    catch (InterruptedException e) {}
    catch (ThreadDeath e) {}
   } while (true);
  }

  final boolean runFinalization()
  {
   int threadNum;
   synchronized (lock)
   {
    threadNum = ++secondaryThreadCnt;
   }
   Thread thread = new FinalizerThread(true, "Secondary finalizer-".concat(
                    String.valueOf(threadNum)),
                    currentThread().getPriority());
   try
   {
    thread.start();
   }
   catch (OutOfMemoryError e)
   {
    return false;
   }
   try
   {
    thread.join();
   }
   catch (InterruptedException e) {}
   return true;
  }

  final boolean runFinalizationForExitInner()
  {
   Thread thread;
   try
   {
    thread =
     new Thread((VMThread) null, "On-exit finalizer", NORM_PRIORITY, true)
     {

      public void run()
      {
       runFinalization0();
       runFinalizationForExit0();
      }
     };
    VMThread.rootGroupAdd(thread);
    thread.start();
   }
   catch (OutOfMemoryError e)
   {
    return false;
   }
   try
   {
    thread.join();
   }
   catch (InterruptedException e) {}
   return true;
  }

  static final boolean createFinalizer()
  {
   FinalizerThread thread = new FinalizerThread(false, "Finalizer",
                             MAX_PRIORITY - 1);
   synchronized (lock)
   {
    try
    {
     thread.start();
    }
    catch (OutOfMemoryError e)
    {
     return false;
    }
    FinalizerThread fnlz = finalizerThread;
    finalizerThread = thread;
    if (fnlz != null)
     fnlz.noWait = true;
   }
   return true;
  }

  final void notifyFinalizer()
  {
   FinalizerThread fnlz;
   synchronized (lock)
   {
    int curNotifyCount = ++notifyCount;
    VMThread.notify(lock, true);
    if ((fnlz = finalizerThread) == null ||
        curNotifyCount - fnlz.runFinalizeCount <= MAX_NOTIFY_COUNT_DELTA ||
        currentThread() == fnlz)
     return;
    fnlz.runFinalizeCount = curNotifyCount - 1;
   }
   try
   {
    fnlz.changeStalledFinalizer();
   }
   catch (OutOfMemoryError e) {}
  }

  private void changeStalledFinalizer()
  {
   int threadNum;
   synchronized (lock)
   {
    if (noWait)
     return;
    threadNum = ++stalledThreadCnt;
   }
   if (createFinalizer())
   {
    VMThread vt;
    if ((vt = vmThread) != null)
    {
     vt.setName("Stalled Finalizer-".concat(String.valueOf(threadNum)));
     vt.setPriority(MIN_PRIORITY);
    }
   }
  }
 }

 private static final class FinalizeOnExit
 {

  FinalizeOnExit() {}

  final void runFinalizationForExit()
  {
   gcOnNoResources();
   FinalizerThread fnlz;
   if ((fnlz = finalizerThread) == null ||
       !fnlz.runFinalizationForExitInner())
   {
    runFinalization0();
    runFinalizationForExit0();
   }
  }
 }

 static FinalizerThread finalizerThread; /* used by VM classes only */

 private static FinalizeOnExit finalizeOnExit;

 private static volatile boolean sigTermFired;

 private VMRuntime() {}

 static int availableProcessors()
 {
  int count = availableProcessors0();
  if (count <= 0)
  {
   String str = VMAccessorJavaIo.getenvPlatformVMFile("NUMBER_OF_PROCESSORS");
   if (str != null)
   {
    try
    {
     count = Integer.parseInt(str);
    }
    catch (NumberFormatException e) {}
   }
   if (count <= 0)
    count = 1;
  }
  return count;
 }

 static void exit(int status)
 {
  VMThrowable.exit(status);
 }

 static native long freeMemory(); /* JVM-core */

 static native long totalMemory(); /* JVM-core */

 static native long maxMemory(); /* JVM-core */

 static void gc()
 {
  gc0(0);
 }

 static void runFinalization()
 {
  FinalizerThread fnlz;
  if ((fnlz = finalizerThread) == null || !fnlz.runFinalization())
   runFinalization0();
 }

 static void runFinalizationForExit()
 {
  if (finalizeOnExit != null)
   finalizeOnExit.runFinalizationForExit();
 }

 static void runFinalizersOnExit(boolean enable)
 {
  if (enable && finalizeOnExit == null)
   finalizeOnExit = new FinalizeOnExit();
 }

 static void traceInstructions(boolean enable)
 {
  traceCode0(1, enable ? 1 : 0);
 }

 static void traceMethodCalls(boolean enable)
 {
  traceCode0(0, enable ? 1 : 0);
 }

 static int nativeLoad(String filename, ClassLoader loader)
 {
  return nativeLoad0(filename, loader);
 }

 static String mapLibraryName(String libname)
 {
  return VMAccessorJavaIo.mapLibraryNameVMFile(libname);
 }

 static Process exec(String[] cmd, String[] env, File dir)
  throws IOException
 {
  return VMProcess.exec(cmd, env, dir);
 }

 static Process exec(List cmdList, Map envMap, File dir, boolean redirect)
  throws IOException
 {
  return VMProcess.exec(cmdList, envMap, dir, redirect);
 }

 static void enableShutdownHooks()
 {
  Object obj = new TermHandler(); /* hack */
  if (obj != null) /* hack */
   enableShutdownHooks0();
 }

 static final int handleSigTerm0X(int signum)
 { /* called from native code */
  try
  {
   if (signum != -1 && VMThread.currentThread() != null && /* hack */
       Runtime.getRuntime() != null && /* hack */
       VMThread.ExitMain.initialized && !sigTermFired)
   {
    sigTermFired = true;
    Thread thread =
     new Thread((VMThread) null, "SigTerm handler", Thread.MAX_PRIORITY - 2,
      false)
     {

      public void run()
      {
       AccessController.doPrivileged(
        new PrivilegedAction()
        {

         public Object run()
         {
          try
          {
           Runtime.getRuntime().exit(130);
          }
          catch (SecurityException e) {}
          return null;
         }
        });
      }
     };
    VMThread.rootGroupAdd(thread);
    try
    {
     thread.start();
    }
    catch (OutOfMemoryError e)
    {
     thread.run();
    }
   }
  }
  catch (ThreadDeath e)
  {
   throw e;
  }
  catch (Throwable throwable)
  {
   VMThread.printUncaughtException(null, throwable);
  }
  return 0;
 }

 static final int finalizeObject0X(Object obj)
  throws Throwable /* hard-coded method signature */
 { /* called from native code */
  FinalizerThread fnlz;
  if (obj != null)
   obj.finalize();
   else if ((fnlz = finalizerThread) != null)
    fnlz.notifyFinalizer();
  return 0;
 }

 static final void createMainFinalizer()
 { /* used by VM classes only */
  if (runFinalization0() != -1 && FinalizerThread.createFinalizer())
   enableNotifyOnFinalization0();
 }

 static final void gcOnNoResources()
 { /* used by VM classes only */
  gc0(1);
 }

 static final boolean preventIOBlocking()
 { /* used by VM classes only */
  return preventIOBlocking0() != 0;
 }

 static final String getJavaExePathname()
 { /* used by VM classes only */
  String path = getJavaExePathname0();
  return path != null && path.length() > 0 ? path : ".";
 }

 static final String getCustomJavaProps()
 { /* used by VM classes only */
  String propsLine = VMAccessorJavaIo.getenvPlatformVMFile("JAVA_PROPS");
  return propsLine != null || (propsLine = getCustomJavaProps0()) != null ?
          propsLine : "";
 }

 static final String getJavaVmVersion()
 { /* used by VM classes only */
  int version = getJavaVmVersion0();
  int minor = version % 100;
  if (minor < 0)
   minor = -minor;
  return String.valueOf(version / 100) + "." + (minor < 10 ? "0" : "") +
          String.valueOf(minor);
 }

 private static native int enableNotifyOnFinalization0(); /* JVM-core */

 private static native int gc0(int clearRefs); /* JVM-core */

 private static native int runFinalization0(); /* JVM-core */

 private static native int runFinalizationForExit0(); /* JVM-core */

 private static native int availableProcessors0(); /* JVM-core */

 private static native int traceCode0(int instr, int on); /* JVM-core */

 private static native int nativeLoad0(String filename,
   Object loader); /* JVM-core */

 private static native int enableShutdownHooks0(); /* JVM-core */

 private static native int preventIOBlocking0(); /* JVM-core */

 private static native String getJavaExePathname0(); /* JVM-core */

 private static native String getCustomJavaProps0(); /* JVM-core */

 private static native int getJavaVmVersion0(); /* JVM-core */
}
