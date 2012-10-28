/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMThread.java --
 * VM specific methods for Java "Thread" class.
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

import java.io.PrintStream;

final class VMThread /* hard-coded class name */
{

 static final class ExitMain /* hard-coded class name */
 { /* used by VM classes only */

  static boolean initialized; /* used by VM classes only */

  static
  {
   if (mainVMThread == null) /* hack */
   {
    createAttachedThread0X(null, "", null, 0); /* hack */
    run0X(null); /* hack */
    Throwable throwable = new Throwable(); /* hack */
    jniExceptionDescribe0X(throwable); /* hack */
    detachThread0X(throwable); /* hack */
    destroyJavaVM0X(null, 0); /* hack */
   }
   initialized = true; /* hack */
   VMRuntime.createMainFinalizer();
  }

  static long vmStartTime = getStartTimeMillis(); /* hack */

  private ExitMain() {}
 }

 static final class UncaughtHandler /* hack */
 { /* used by VM classes only */

  private boolean insideJniExc;

  UncaughtHandler() {}

  final synchronized boolean printJniException(Throwable throwable)
  {
   boolean done = false;
   if (throwable != null && !insideJniExc)
   {
    insideJniExc = true;
    try
    {
     Thread thread;
     PrintStream err = System.err;
     if (err != null && (thread = currentThread()) != null && /* hack */
         thread.group != null) /* hack */
     {
      err.println("[JNI] Exception in thread \"" + thread.getName() + "\": " +
       throwable.toString());
      done = true;
     }
    }
    finally
    {
     insideJniExc = false;
    }
   }
   return done;
  }

  final boolean printException(Thread thread, Throwable throwable)
  {
   PrintStream err = System.err;
   if (err != null) /* hack */
   {
    if (thread != null &&
        thread.group != null) /* hack */
    {
     try
     {
      thread.getUncaughtExceptionHandler().uncaughtException(thread,
       throwable);
      return true;
     }
     catch (Error e) {}
     catch (RuntimeException e) {}
    }
    try
    {
     err.println();
     synchronized (err)
     {
      err.print("Exception ");
      err.println(throwable.getClass().getName());
     }
     return true;
    }
    catch (Error e) {}
    catch (RuntimeException e) {}
   }
   return false;
  }
 }

 private static final int STATE_NEW = 0;

 private static final int STATE_RUNNABLE = 1;

 private static final int STATE_BLOCKED = 2;

 private static final int STATE_WAITING = 3;

 private static final int STATE_TIMED_WAITING = 4;

 private static final int STATE_TERMINATED = 5;

 private static final int PARKFLAGS_PARKED = 0x1;

 private static final int PARKFLAGS_UNPARKPERMIT = 0x2;

 private static boolean hasThreads;

 static int nonDaemonCnt; /* used by VM classes only */

 private static final Object[] threadStartLock = {};

 static final Object nonDaemonLock =
  new Object(); /* used by VM classes only */

 static VMThread mainVMThread; /* used by VM classes only */

 private static volatile PrintStream sysOut;

 private static volatile UncaughtHandler uncaughtHandler;

 private static int liveThreadCnt;

 private static int maxLiveThreadCnt;

 private static long totalStartedCnt;

 final Thread thread;

 private volatile int threadStatus /* = STATE_NEW */;

 private transient volatile Object vmdata;

 private int parkFlags;

 private int suspendCount;

 static
 {
  setupMainThread();
  if (mainVMThread == null) /* hack */
   throwIllegalMonitorStateException0X(); /* hack */
 }

 private VMThread(Thread thread)
 {
  this.thread = thread;
 }

 static void create(Thread thread, long stacksize)
 {
  VMThread vt = new VMThread(thread);
  vt.start(stacksize);
  vt.attachInner();
  /* if (vt == null) thread.run(); */ /* hack */
 }

 String getName()
 {
  return thread.name;
 }

 void setName(String name)
 {
  thread.name = name;
 }

 void setPriority(int priority)
 {
  thread.priority = priority;
  nativeSetPriority(priority);
 }

 int getPriority()
 {
  return thread.priority;
 }

 boolean isDaemon()
 {
  return thread.daemon;
 }

 int countStackFrames()
 {
  Object vmdata = this.vmdata;
  int count;
  if (vmdata == null || (count = countStackFrames0(vmdata)) < 0)
   throw new IllegalThreadStateException();
  return count;
 }

 void join(long ms, int ns)
  throws InterruptedException
 {
  synchronized (this)
  {
   while (thread.vmThread != null)
   {
    wait(this, ms, ns);
    if (ms != 0L || ns != 0)
     break;
   }
  }
 }

 void stop(Throwable throwable)
 {
  synchronized (thread)
  {
   if (threadStatus == STATE_NEW)
   {
    thread.stillborn = throwable;
    return;
   }
   if (threadStatus == STATE_TERMINATED)
    return;
  }
  nativeStop(throwable);
 }

 private void start(long stacksize)
 {
  boolean retrying = false;
  while ((vmdata = start0(thread, stacksize)) == null)
  {
   if (retrying || !isStartRetryNeededOnce())
    throw new OutOfMemoryError("cannot start thread: ".concat(getName()));
   retrying = true;
  }
  synchronized (threadStartLock)
  {
   thread.vmThread = this; /* hack */
   totalStartedCnt++;
   notify(threadStartLock, true);
  }
  yield();
 }

 void interrupt()
 {
  Object vmdata = this.vmdata;
  if (vmdata != null)
   synchronized (this)
   {
    if (parkFlags != (PARKFLAGS_PARKED | PARKFLAGS_UNPARKPERMIT))
     interrupt0(vmdata, 1);
     else parkFlags = PARKFLAGS_PARKED;
   }
 }

 boolean isInterrupted()
 {
  Object vmdata = this.vmdata;
  int res = 0;
  if (vmdata != null)
   synchronized (this)
   {
    if (parkFlags != (PARKFLAGS_PARKED | PARKFLAGS_UNPARKPERMIT))
     res = interrupt0(vmdata, 0);
   }
  return res > 0;
 }

 void suspend()
 {
  Object vmdata = this.vmdata;
  if (vmdata != null)
   suspend0(vmdata, 1);
 }

 void resume()
 {
  Object vmdata = this.vmdata;
  if (vmdata != null)
   suspend0(vmdata, -1);
 }

 void nativeSetPriority(int priority)
 {
  Object vmdata = this.vmdata;
  if (vmdata != null)
   nativeSetPriority0(vmdata, priority);
 }

 void nativeStop(Throwable throwable)
 {
  if (currentThread().vmThread == this)
   VMClass.throwException(throwable);
  Object vmdata = this.vmdata;
  if (vmdata != null)
   nativeStop0(vmdata, throwable);
 }

 String getState()
 {
  int state = threadStatus;
  Object vmdata;
  if (state == STATE_RUNNABLE && (vmdata = this.vmdata) != null)
   state = getState0(vmdata);
  switch (state)
  {
  case STATE_NEW:
   return "NEW";
  case STATE_RUNNABLE:
   return "RUNNABLE";
  case STATE_BLOCKED:
   return "BLOCKED";
  case STATE_WAITING:
   return "WAITING";
  case STATE_TIMED_WAITING:
   return "TIMED_WAITING";
  }
  return "TERMINATED";
 }

 static Thread currentThread()
 {
  Thread thread = (Thread) currentThread0();
  VMThread vt;
  if (thread == null && ((vt = mainVMThread) == null || /* hack */
      (thread = vt.thread) == null)) /* hack */
   throw new InternalError();
  return thread;
 }

 static native void yield(); /* JVM-core */

 static boolean interrupted()
 {
  VMThread vt = currentThread().vmThread;
  Object vmdata;
  int res = 0;
  if (vt != null && (vmdata = vt.vmdata) != null)
   synchronized (vt)
   {
    res = interrupt0(vmdata, -1);
   }
  return res > 0;
 }

 static void sleep(long ms, int ns)
  throws InterruptedException
 {
  if (ms != 0L || ns != 0)
  {
   VMThread vt = currentThread().vmThread;
   if (vt != null)
    synchronized (vt)
    {
     wait(vt, ms, ns);
    }
  }
   else
   {
    yield();
    if (interrupted())
     throw new InterruptedException();
   }
 }

 static boolean holdsLock(Object obj)
 {
  if (obj == null)
   throw new NullPointerException();
  return notify0(obj, -1) >= 0;
 }

 static final void notify(Object obj, boolean all)
 { /* used by VM classes only */
  if (notify0(obj, all ? 1 : 0) < 0)
   throw new IllegalMonitorStateException();
 }

 static final void wait(Object obj, long ms, int ns)
  throws InterruptedException
 { /* used by VM classes only */
  int res = wait0(obj, ms, ns);
  if (res < 0)
   throw new IllegalMonitorStateException();
  if (res != 0)
   throw new InterruptedException();
 }

 static final void initSystemErr() /* hard-coded method signature */
 { /* used by VM classes only */
  uncaughtHandler = new UncaughtHandler();
  if (mainVMThread == null) /* hack */
  {
   jniExceptionDescribe0X(new Throwable()); /* hack */
   destroyJavaVM0X(null, 0); /* hack */
  }
 }

 static final void setSystemOut(
   PrintStream out) /* hard-coded method signature */
 { /* used by VM classes only */
  /* if (out == null) out = System.err; */ /* hack */
  sysOut = out;
 }

 static final void flushSystemOut()
 { /* used by VM classes only */
  PrintStream out = sysOut;
  if (out != null)
  {
   try
   {
    out.flush();
   }
   catch (Error e) {}
   catch (RuntimeException e) {}
  }
 }

 static final void printUncaughtException(Thread thread, Throwable throwable)
 { /* used by VM classes only */
  if (!(throwable instanceof ThreadDeath))
  {
   if (thread != null)
    flushSystemOut();
   UncaughtHandler handler;
   if ((handler = uncaughtHandler) == null ||
       !handler.printException(thread, throwable))
   {
    if (throwable instanceof RuntimeException)
     throw (RuntimeException) throwable;
    throw (Error) (throwable instanceof Error ? throwable :
           (new InternalError("VMThread")).initCause(throwable));
   }
  }
 }

 static final void rootGroupAdd(Thread thread)
 { /* used by VM classes only */
  if (thread.group == null)
   (thread.group = ThreadGroup.root).addThread(thread);
 }

 static final int countStackFrames(Thread thread)
 { /* used by VM classes only */
  VMThread vt = thread.vmThread;
  if (vt == null)
   throw new IllegalThreadStateException();
  return vt.countStackFrames();
 }

 static final void suspendNested(Thread thread)
 { /* used by VM classes only */
  VMThread vt = thread.vmThread;
  if (vt != null)
   synchronized (vt)
   {
    if (++vt.suspendCount == 1)
     vt.suspend();
   }
 }

 static final void resumeNested(Thread thread)
 { /* used by VM classes only */
  VMThread vt = thread.vmThread;
  if (vt != null)
   synchronized (vt)
   {
    if (--vt.suspendCount == 0)
     vt.resume();
   }
 }

 static final int getSuspendCount(Thread thread)
 { /* used by VM classes only */
  int count = 0;
  VMThread vt = thread.vmThread;
  if (vt != null)
   synchronized (vt)
   {
    count = vt.suspendCount;
   }
  return count;
 }

 static final long getStartTimeMillis()
 { /* used by VM classes only */
  long startTime;
  if ((startTime = ExitMain.vmStartTime) == 0L) /* hack */
  {
   startTime = VMSystem.currentTimeMillis();
   ExitMain.vmStartTime = startTime;
  }
  return startTime;
 }

 static final int getPeakThreadCount(boolean reset)
 { /* used by VM classes only */
  int count = maxLiveThreadCnt;
  if (reset)
   synchronized (nonDaemonLock)
   {
    maxLiveThreadCnt = liveThreadCnt;
   }
  return count + 1;
 }

 static final long getTotalStartedCount()
 { /* used by VM classes only */
  synchronized (threadStartLock)
  {
   return totalStartedCnt;
  }
 }

 static final void park(boolean isAbsolute, long time)
 { /* used by VM classes only */
  VMThread vt = currentThread().vmThread;
  if (vt != null)
  {
   long ms;
   int ns = 0;
   if (isAbsolute ? (ms = time - VMSystem.currentTimeMillis()) > 0L :
       (ms = time / (1000L * 1000L)) >= 0L &&
       (ns = (int) (time % (1000L * 1000L))) >= 0)
    vt.parkInner(ms, ns);
    else
    {
     synchronized (vt)
     {
      vt.parkFlags = 0;
     }
    }
  }
 }

 static final void unpark(Thread thread)
 { /* used by VM classes only */
  VMThread vt = thread.vmThread;
  if (vt != null)
   synchronized (vt)
   {
    if (vt.parkFlags == PARKFLAGS_PARKED)
    {
     Object vmdata = vt.vmdata;
     if (vmdata != null && interrupt0(vmdata, 1) <= 0)
      vt.parkFlags = PARKFLAGS_PARKED | PARKFLAGS_UNPARKPERMIT;
    }
     else if (vt.parkFlags == 0)
      vt.parkFlags = PARKFLAGS_UNPARKPERMIT;
   }
 }

 static final void throwIllegalMonitorStateException0X()
 { /* called from native code */
  throw new IllegalMonitorStateException();
 }

 static final int jniExceptionDescribe0X(Object throwableObj)
 { /* called from native code */
  if (throwableObj instanceof ThreadDeath)
   return 1;
  UncaughtHandler handler;
  return (handler = uncaughtHandler) != null &&
          handler.printJniException((Throwable) throwableObj) ? 1 : 0;
 }

 static final int run0X(Object vmdata)
 { /* called from native code */
  try
  {
   Thread thread;
   if (vmdata != null && (thread = currentThread()) != null) /* hack */
   {
    VMThread vt;
    synchronized (threadStartLock)
    {
     while ((vt = thread.vmThread) == null)
     {
      try
      {
       wait(threadStartLock, 0L, 0);
      }
      catch (InterruptedException e) {}
     }
    }
    if (vt.vmdata != vmdata)
     throw new InternalError("VMThread.start() fault");
    nativeSetPriority0(vmdata, vt.getPriority());
    vt.run();
   }
  }
  catch (Throwable throwable)
  {
   printUncaughtException(null, throwable);
  }
  return 0;
 }

 static final Object createAttachedThread0X(Object groupObj, String name,
   Object vmdata, int daemon)
  throws ClassCastException
 { /* called from native code */
  if (mainVMThread == null || ThreadGroup.root == null) /* hack */
   throw new InternalError("VMThread class not initialized");
  Thread thread = new Thread((VMThread) null, name, Thread.NORM_PRIORITY,
                   daemon != 0);
  (thread.group = groupObj != null ? (ThreadGroup) groupObj :
                   ThreadGroup.root).addThread(thread);
  VMThread vt = new VMThread(thread);
  vt.vmdata = vmdata;
  vt.threadStatus = STATE_RUNNABLE;
  vt.attachInner();
  return thread;
 }

 static final int detachThread0X(Object throwableObj)
 { /* called from native code */
  VMThread vt = null;
  try
  {
   Thread thread = currentThread();
   if (thread != null)
   {
    vt = thread.vmThread;
    if (throwableObj != null && !(throwableObj instanceof ThreadDeath) &&
        vt != null && vt.threadStatus != STATE_TERMINATED)
    {
     printUncaughtException(thread, (Throwable) throwableObj);
    }
   }
  }
  finally
  {
   if (vt != null)
    vt.detachInner();
  }
  return 0;
 }

 static final int destroyJavaVM0X(Object throwableObj,
   int isInInitializer) /* hard-coded method signature */
 { /* called from native code */
  try
  {
   Thread thread = currentThread();
   VMThread vt;
   if (thread == null || (vt = thread.vmThread) == null || /* hack */
       nonDaemonLock == null || mainVMThread == null ||
       Runtime.getRuntime() == null || ThreadGroup.root == null) /* hack */
    throw new InternalError("VMThread class not initialized");
   if (throwableObj != null)
   {
    if (throwableObj instanceof ThreadDeath)
     throwableObj = null;
     else
     {
      if (!ExitMain.initialized)
       throw new InternalError("VMThread class not initialized");
      Throwable throwable = (Throwable) throwableObj;
      if (isInInitializer != 0 && !(throwable instanceof LinkageError) &&
          !(throwable instanceof VirtualMachineError))
      {
       try
       {
        throwable = new ExceptionInInitializerError(throwable);
       }
       catch (Error e)
       {
        throwable = e;
       }
      }
      printUncaughtException(thread, throwable);
     }
   }
   Thread cleanupThread = null;
   if (hasThreads && !(throwableObj instanceof Error))
   {
    cleanupThread =
     new Thread((VMThread) null, "VM cleanup", Thread.NORM_PRIORITY, true)
     {

      public void run()
      {
       synchronized (nonDaemonLock)
       {
        while (nonDaemonCnt != 0)
        {
         try
         {
          VMThread.wait(nonDaemonLock, 0L, 0);
         }
         catch (InterruptedException e) {}
        }
       }
       flushSystemOut();
       Runtime.getRuntime().runShutdownHooks();
       flushSystemOut();
      }
     };
    rootGroupAdd(cleanupThread);
    try
    {
     cleanupThread.start();
    }
    catch (OutOfMemoryError e)
    {
     cleanupThread = null;
    }
   }
   if (cleanupThread == null)
   {
    flushSystemOut();
    Runtime.getRuntime().runShutdownHooks();
    flushSystemOut();
   }
   vt.threadStatus = STATE_TERMINATED;
   thread.die();
   if (cleanupThread != null)
   {
    synchronized (vt)
    {
     notify(vt, true);
    }
    try
    {
     cleanupThread.join();
    }
    catch (InterruptedException e) {}
   }
   if (throwableObj != null)
    VMThrowable.exit(254);
  }
  catch (OutOfMemoryError e)
  {
   throw e;
  }
  catch (Error e)
  {
   if (throwableObj instanceof OutOfMemoryError)
    throw (OutOfMemoryError) throwableObj;
   throw e;
  }
  catch (RuntimeException e)
  {
   if (throwableObj instanceof OutOfMemoryError)
    throw (OutOfMemoryError) throwableObj;
   throw (Error) (new InternalError("VMThread")).initCause(e);
  }
  return 0;
 }

 private static void setupMainThread()
 {
  Thread thread =
   new Thread((VMThread) null, "main", Thread.NORM_PRIORITY, false);
  rootGroupAdd(thread);
  VMThread vt = new VMThread(thread);
  thread.vmThread = vt;
  vt.vmdata = setupMainThread0(thread);
  vt.threadStatus = STATE_RUNNABLE;
  mainVMThread = vt; /* hack */
  Throwable throwable;
  if ((throwable = thread.stillborn) != null)
  {
   thread.stillborn = null;
   if (throwable instanceof RuntimeException)
    throw (RuntimeException) throwable;
   throw (Error) (throwable instanceof Error ? throwable :
          (new InternalError("VMThread")).initCause(throwable));
  }
  vt.nativeSetPriority(vt.getPriority());
 }

 private static boolean isStartRetryNeededOnce()
 {
  if (!hasThreads)
   return false;
  VMRuntime.gcOnNoResources();
  yield();
  return true;
 }

 private void attachInner()
 {
  thread.vmThread = this;
  synchronized (nonDaemonLock)
  {
   hasThreads = true;
   int count;
   if ((count = ++liveThreadCnt) > maxLiveThreadCnt)
    maxLiveThreadCnt = count;
   if (!thread.daemon && ++nonDaemonCnt == 0)
    notify(nonDaemonLock, false);
  }
 }

 private void detachInner()
 {
  vmdata = null;
  if (threadStatus != STATE_TERMINATED)
  {
   threadStatus = STATE_TERMINATED;
   boolean died = false;
   synchronized (nonDaemonLock)
   {
    liveThreadCnt--;
    if (!thread.daemon && --nonDaemonCnt == 0)
    {
     thread.die();
     notify(nonDaemonLock, false);
     died = true;
    }
   }
   if (!died)
    thread.die();
   synchronized (this)
   {
    notify(this, true);
   }
  }
 }

 private void run()
 {
  try
  {
   try
   {
    synchronized (thread)
    {
     threadStatus = STATE_RUNNABLE;
     Throwable throwable = thread.stillborn;
     if (throwable != null)
     {
      thread.stillborn = null;
      throw throwable;
     }
    }
    thread.run();
   }
   catch (Throwable throwable)
   {
    printUncaughtException(thread, throwable);
   }
  }
  finally
  {
   detachInner();
  }
 }

 private synchronized void parkInner(long ms, int ns)
 {
  try
  {
   if (parkFlags != PARKFLAGS_UNPARKPERMIT)
   {
    parkFlags = PARKFLAGS_PARKED;
    wait(this, ms, ns);
   }
  }
  catch (InterruptedException e)
  {
   if (parkFlags == PARKFLAGS_PARKED)
    interrupt();
  }
  finally
  {
   parkFlags = 0;
  }
 }

 private static native int notify0(Object obj, int all); /* JVM-core */

 private static native int wait0(Object obj, long ms,
   int ns); /* JVM-core */ /* blocking syscall */

 private static native Object setupMainThread0(Object thread); /* JVM-core */

 private static native Object currentThread0(); /* JVM-core */

 private static native Object start0(Object thread,
   long stacksize); /* JVM-core */

 private static native int nativeSetPriority0(Object vmdata,
   int priority); /* JVM-core */

 private static native int nativeStop0(Object vmdata,
   Object throwable); /* JVM-core */

 private static native int interrupt0(Object vmdata, int set); /* JVM-core */

 private static native int suspend0(Object vmdata, int set); /* JVM-core */

 private static native int countStackFrames0(Object vmdata); /* JVM-core */

 private static native int getState0(Object vmdata); /* JVM-core */
}
