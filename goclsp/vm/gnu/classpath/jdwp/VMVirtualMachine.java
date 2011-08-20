/*
 * @(#) $(JCGO)/goclsp/vm/gnu/classpath/jdwp/VMVirtualMachine.java --
 * VM specific implementation for JDWP virtual machine.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2008 Ivan Maidanski <ivmai@ivmaisoft.com>
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

package gnu.classpath.jdwp;

import gnu.classpath.jdwp.event.EventRequest;

import gnu.classpath.jdwp.exception.InvalidMethodException;
import gnu.classpath.jdwp.exception.JdwpException;
import gnu.classpath.jdwp.exception.NotImplementedException;

import gnu.classpath.jdwp.util.MethodResult;

import gnu.java.lang.VMAccessorGnuJavaLang;

import java.io.File;

import java.lang.reflect.Method;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public final class VMVirtualMachine
{

 private VMVirtualMachine() {}

 public static void suspendAllThreads()
  throws JdwpException
 {
  ThreadGroup jdwpGroup = Jdwp.getDefault().getJdwpThreadGroup();
  ThreadGroup tg = jdwpGroup;
  for (ThreadGroup tgn = tg; tgn != null; tgn = tg.getParent())
   tg = tgn;
  int num = tg.activeCount();
  Thread[] threads = new Thread[num];
  tg.enumerate(threads);
  Thread current = Thread.currentThread();
  Thread t;
  for (int i = 0; i < num; i++)
   if ((t = threads[i]) != null && t != current &&
       t.getThreadGroup() != jdwpGroup)
    suspendThread(t);
  if (current.getThreadGroup() != jdwpGroup)
   suspendThread(current);
 }

 public static void resumeAllThreads()
  throws JdwpException
 {
  Thread current = Thread.currentThread();
  ThreadGroup jdwpGroup = current.getThreadGroup();
  ThreadGroup tg = jdwpGroup;
  for (ThreadGroup tgn = tg; tgn != null; tgn = tg.getParent())
   tg = tgn;
  int num = tg.activeCount();
  Thread[] threads = new Thread[num];
  tg.enumerate(threads);
  Thread t;
  for (int i = 0; i < num; i++)
   if ((t = threads[i]) != null && t != current &&
       t.getThreadGroup() != jdwpGroup)
    resumeThread(t);
 }

 public static void suspendThread(Thread thread)
  throws JdwpException
 {
  VMAccessorJavaLang.suspendNestedVMThread(thread);
 }

 public static void resumeThread(Thread thread)
  throws JdwpException
 {
  VMAccessorJavaLang.resumeNestedVMThread(thread);
 }

 public static int getSuspendCount(Thread thread)
  throws JdwpException
 {
  return VMAccessorJavaLang.getSuspendCountVMThread(thread);
 }

 public static int getAllLoadedClassesCount()
  throws JdwpException
 {
  return VMAccessorGnuJavaLang.getAllLoadedClassCountVMInstrumentationImpl();
 }

 public static Iterator /* Collection */ getAllLoadedClasses()
  throws JdwpException
 {
  return Arrays.asList(
          VMAccessorGnuJavaLang.getAllLoadedClassesVMInstrumentationImpl()).
          iterator();
 }

 public static int getClassStatus(Class klass)
  throws JdwpException
 {
  String status = VMAccessorJavaLang.getClassStatusVMClass(klass);
  return "INITIALIZED".equals(status) ?
          JdwpConstants.ClassStatus.INITIALIZED :
          "PREPARED".equals(status) ? JdwpConstants.ClassStatus.PREPARED :
          "VERIFIED".equals(status) ? JdwpConstants.ClassStatus.VERIFIED :
          JdwpConstants.ClassStatus.ERROR;
 }

 public static VMMethod[] getAllClassMethods(Class klass)
  throws JdwpException
 {
  /* not implemented */
  throw new NotImplementedException("getAllClassMethods");
 }

 public static VMMethod getClassMethod(Class klass, long id)
  throws JdwpException
 {
  VMMethod[] methods = getAllClassMethods(klass);
  int len = methods.length;
  for (int i = 0; i < len; i++)
   if (methods[i].getId() == id)
    return methods[i];
  throw new InvalidMethodException(id);
 }

 public static ArrayList getFrames(Thread thread, int start, int len)
  throws JdwpException
 {
  VMFrame[] frames = getFrames(thread);
  if (len == -1)
   len = frames.length - start;
  if ((start | len) < 0 || frames.length - start < len)
   throw new ArrayIndexOutOfBoundsException();
  ArrayList list = new ArrayList(len);
  while (len-- > 0)
   list.add(frames[start++]);
  return list;
 }

 public static VMFrame getFrame(Thread thread, ByteBuffer bb)
  throws JdwpException
 {
  VMFrame[] frames = getFrames(thread);
  long id = bb.getLong();
  int len = frames.length;
  for (int i = 0; i < len; i++)
   if (frames[i].getId() == id)
    return frames[i];
  throw new JdwpException(JdwpConstants.Error.INVALID_FRAMEID,
         "invalid frame id (" + id + ")");
 }

 public static int getFrameCount(Thread thread)
  throws JdwpException
 {
  try
  {
   return VMAccessorJavaLang.countStackFramesVMThread(thread);
  }
  catch (IllegalThreadStateException e) {}
  return 0;
 }

 public static int getThreadStatus(Thread thread)
  throws JdwpException
 {
  String state = thread.getState();
  return "TIMED_WAITING".equals(state) ? JdwpConstants.ThreadStatus.SLEEPING :
          "WAITING".equals(state) ? JdwpConstants.ThreadStatus.WAIT :
          "BLOCKED".equals(state) ? JdwpConstants.ThreadStatus.MONITOR :
          "RUNNABLE".equals(state) ? JdwpConstants.ThreadStatus.RUNNING :
          JdwpConstants.ThreadStatus.ZOMBIE;
 }

 public static ArrayList getLoadRequests(ClassLoader cl)
  throws JdwpException
 {
  /* not implemented */
  throw new NotImplementedException("getLoadRequests");
 }

 public static MethodResult executeMethod(Object obj, Thread thread,
   Class klass, Method method, Object[] values, boolean nonVirtual)
  throws JdwpException
 {
  /* not implemented */
  throw new NotImplementedException("executeMethod");
 }

 public static String getSourceFile(Class klass)
  throws JdwpException
 {
  String filename = VMAccessorJavaLang.getSourceFilenameVMClass(klass);
  return filename != null ? filename.replace('/', File.separatorChar) :
          "no path information for the file is included";
 }

 public static void registerEvent(EventRequest request)
  throws JdwpException
 {
  /* not implemented */
  throw new NotImplementedException("registerEvent");
 }

 public static void unregisterEvent(EventRequest request)
  throws JdwpException
 {
  /* not implemented */
  throw new NotImplementedException("unregisterEvent");
 }

 public static void clearEvents(byte kind)
  throws JdwpException
 {
  /* not implemented */
  throw new NotImplementedException("clearEvents");
 }

 private static VMFrame[] getFrames(Thread thread)
  throws JdwpException
 {
  /* not implemented */
  throw new NotImplementedException("getFrames");
 }
}
