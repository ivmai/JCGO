/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMProcess.java --
 * VM specific implementation of Java "Process".
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

import gnu.classpath.SystemProperties;

import gnu.java.nio.FileChannelImpl;
import gnu.java.nio.VMAccessorGnuJavaNio;
import gnu.java.nio.VMChannel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.VMAccessorJavaIo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class VMProcess extends Process /* hard-coded class name */
{

 private static final int STATE_INITIAL = 0;

 private static final int STATE_RUNNING = 1;

 private static final int STATE_TERMINATED = 2;

 private static int threadNum;

 private static final String[] PROPAGATE_ENVS_LIST =
 {
  "LD_LIBRARY_PATH", "TMPDIR", "HOME", "LOGNAME", "LC_CTYPE", "TZ",
  "COMSPEC", "TMP", "TEMP"
 };

 private static final boolean IS_PROG_SEARCH_NEEDED =
  isProgSearchNeeded0() != 0;

 private static final int IS_CMD_SPACE_DELIMITED = isCmdSpaceDelimited0();

 private static final String VALID_PROG_EXTS_LIST = getValidProgExtsList();

 private static final boolean preventBlocking = VMRuntime.preventIOBlocking();

 volatile int state;

 long pid;

 OutputStream stdinStream;

 InputStream stdoutStream;

 InputStream stderrStream;

 int exitValue;

 private VMProcess() {}

 private VMProcess(final String[] cmd, final String[] env, final File dir,
   final boolean redirect)
  throws IOException
 {
  final Throwable[] throwableArr = new Throwable[1];
  ThreadGroup tg = Thread.currentThread().getThreadGroup();
  try
  {
   for (ThreadGroup tgn = tg; tgn != null; tgn = tg.getParent())
    tg = tgn;
  }
  catch (SecurityException e) {}
  Thread processThread =
   new Thread(tg, "Process reaper-".concat(String.valueOf(nextThreadNum())))
   {

    public void run()
    {
     synchronized (VMProcess.this)
     {
      try
      {
       nativeSpawn(cmd, env, dir, redirect);
       state = STATE_RUNNING;
      }
      catch (ThreadDeath e)
      {
       state = STATE_TERMINATED;
       throw e;
      }
      catch (Throwable e)
      {
       state = STATE_TERMINATED;
       throwableArr[0] = e;
      }
      VMProcess.this.notifyAll();
     }
     waitForProcess();
    }
   };
  processThread.setDaemon(true);
  try
  {
   processThread.start();
  }
  catch (OutOfMemoryError e)
  {
   processThread.run();
  }
  synchronized (this)
  {
   while (state == STATE_INITIAL)
   {
    try
    {
     wait();
    }
    catch (InterruptedException e) {}
   }
  }
  Throwable throwable = throwableArr[0];
  if (throwable != null)
  {
   throwable.fillInStackTrace();
   if (throwable instanceof IOException)
    throw (IOException) throwable;
   if (throwable instanceof RuntimeException)
    throw (RuntimeException) throwable;
   if (throwable instanceof Error)
    throw (Error) throwable;
   throw (Error) (new InternalError("VMProcess")).initCause(throwable);
  }
 }

 void setProcessInfo(OutputStream stdin, InputStream stdout,
   InputStream stderr, long pid)
 {
  stdinStream = stdin != null ? stdin :
   new OutputStream()
   {

    public void write(int b)
     throws IOException
    {
     throw new IOException("process input stream is not redirected");
    }
   };
  InputStream nulIn = stdout != null && stderr != null ? null :
   new InputStream()
   {

    public int read()
     throws IOException
    {
     return -1;
    }
   };
  stdoutStream = stdout != null ? stdout : nulIn;
  stderrStream = stderr != null ? stderr : nulIn;
  this.pid = pid;
 }

 static Process exec(String[] cmd, String[] env, File dir)
  throws IOException
 {
  return new VMProcess(cmd, env, dir, false);
 }

 static Process exec(List cmdList, Map envMap, File dir, boolean redirect)
  throws IOException
 {
  String[] cmd = new String[cmdList.size()];
  Iterator iter = cmdList.iterator();
  for (int i = 0; iter.hasNext(); i++)
   cmd[i] = (String) iter.next();
  String[] env = new String[envMap.size()];
  iter = envMap.entrySet().iterator();
  for (int i = 0; iter.hasNext(); i++)
  {
   Map.Entry entry = (Map.Entry) iter.next();
   StringBuilder sb = new StringBuilder();
   sb.append((String) entry.getKey());
   sb.append('=');
   sb.append((String) entry.getValue());
   env[i] = sb.toString();
  }
  return new VMProcess(cmd, env, dir, redirect);
 }

 public OutputStream getOutputStream()
 {
  return stdinStream;
 }

 public InputStream getInputStream()
 {
  return stdoutStream;
 }

 public InputStream getErrorStream()
 {
  return stderrStream;
 }

 public synchronized int waitFor()
  throws InterruptedException
 {
  while (state != STATE_TERMINATED)
   wait();
  return exitValue;
 }

 public synchronized int exitValue()
 {
  if (state != STATE_TERMINATED)
   throw new IllegalThreadStateException();
  return exitValue;
 }

 public synchronized void destroy()
 {
  if (state != STATE_TERMINATED)
  {
   int res = nativeKill(pid);
   if (res > 0)
   {
    if (exitValue == 0)
     exitValue = res;
    state = STATE_TERMINATED;
    notifyAll();
   }
    else
    {
     do
     {
      try
      {
       wait();
      }
      catch (InterruptedException e) {}
     } while (state != STATE_TERMINATED);
    }
  }
 }

 final void nativeSpawn(String[] cmd, String[] env, File dir,
   boolean redirect)
  throws IOException
 { /* used by VM classes only */
  if (cmd == null)
   throw new NullPointerException();
  int res = cmd.length;
  if (res == 0)
   throw new IndexOutOfBoundsException();
  do
  {
   if (cmd[--res] == null)
    throw new NullPointerException();
  } while (res > 0);
  String envZBlock = "";
  if (env != null)
  {
   env = addMissingEnvVar(env, "PATH", ".");
   for (int i = 0; i < PROPAGATE_ENVS_LIST.length; i++)
    env = addMissingEnvVar(env, PROPAGATE_ENVS_LIST[i], null);
   envZBlock = convertEnv(env);
  }
  String dirpath = ".";
  boolean changeDir = false;
  if (dir != null)
  {
   dirpath = VMAccessorJavaIo.normVolumeColonVMFile(dir.getPath());
   if (!dirpath.equals("."))
   {
    VMAccessorGnuJavaNio.checkIOResCodeVMChannel(
     checkPermissions0(dirpath, 1));
    changeDir = true;
   }
  }
  String progName;
  String cmdZBlock;
  if (IS_CMD_SPACE_DELIMITED < 0)
  {
   progName = convertProgName(cmd[0], true);
   cmdZBlock = convertCmd(null, cmd, true);
  }
   else
   {
    cmdZBlock = convertCmd(convertProgName(cmd[0], changeDir), cmd,
                 IS_CMD_SPACE_DELIMITED != 0);
    progName = "";
   }
  int bufLen = getSpawnWorkBufSize0(cmdZBlock, envZBlock, cmd.length,
                env != null ? env.length : 0);
  byte[] workBuf = new byte[bufLen >= 0 ? bufLen : -1 >>> 1];
  VMChannel inCh = new VMChannel();
  VMChannel outCh = new VMChannel();
  VMChannel errCh = redirect ? null : new VMChannel();
  int[] fdsArr = { -1, -1, -1 };
  long[] pidArr = new long[1];
  boolean retrying = false;
  do
  {
   res = nativeSpawn0(progName, cmdZBlock, envZBlock, dirpath, fdsArr, pidArr,
          workBuf, bufLen, redirect ? 1 : 0);
   if (retrying || !VMAccessorGnuJavaNio.isIORetryNeededOnceVMChannel(res))
    break;
   retrying = true;
  } while (true);
  VMAccessorGnuJavaNio.checkIOResCodeVMChannel(res);
  if (fdsArr[2] == -1)
   errCh = null;
  try
  {
   if (fdsArr[0] != -1)
    VMAccessorGnuJavaNio.setNativeFileFDVMChannel(inCh, fdsArr[0],
     FileChannelImpl.WRITE);
  }
  finally
  {
   try
   {
    if (fdsArr[1] != -1)
     VMAccessorGnuJavaNio.setNativeFileFDVMChannel(outCh, fdsArr[1],
      FileChannelImpl.READ);
   }
   finally
   {
    if (errCh != null)
     VMAccessorGnuJavaNio.setNativeFileFDVMChannel(errCh, fdsArr[2],
      FileChannelImpl.READ);
   }
  }
  setProcessInfo(fdsArr[0] != -1 ? createOutputStream(inCh) : null,
   fdsArr[1] != -1 ? createInputStream(outCh) : null,
   errCh != null ? createInputStream(errCh) : null, pidArr[0]);
 }

 final void waitForProcess()
 { /* used by VM classes only */
  int nohang = 0;
  if (preventBlocking)
  {
   Thread.yield();
   nohang = 1;
  }
  int res;
  do
  {
   res = nativeWaitFor0(pid, nohang);
   if (state == STATE_TERMINATED)
    return;
   if (res != -1)
    break;
   try
   {
    Thread.sleep(1L);
   }
   catch (InterruptedException e) {}
  } while (true);
  exitValue = res;
  synchronized (this)
  {
   if (state != STATE_TERMINATED)
   {
    state = STATE_TERMINATED;
    notifyAll();
   }
  }
 }

 static final void pipe(VMChannel chIn, VMChannel chOut)
  throws IOException
 { /* used by VM classes only */
  int[] fdsArr = { -1, -1 };
  int res;
  boolean retrying = false;
  do
  {
   res = pipe0(fdsArr);
   if (retrying || !VMAccessorGnuJavaNio.isIORetryNeededOnceVMChannel(res))
    break;
   retrying = true;
  } while (true);
  VMAccessorGnuJavaNio.checkIOResCodeVMChannel(res);
  try
  {
   VMAccessorGnuJavaNio.setNativeFileFDVMChannel(chIn, fdsArr[0],
    FileChannelImpl.READ);
  }
  finally
  {
   VMAccessorGnuJavaNio.setNativeFileFDVMChannel(chOut, fdsArr[1],
    FileChannelImpl.WRITE);
  }
 }

 private static synchronized int nextThreadNum()
 {
  return ++threadNum;
 }

 private static String[] addMissingEnvVar(String[] env, String name,
   String defValue)
 {
  String value = VMAccessorJavaIo.getenvPlatformVMFile(name);
  if (value == null && (value = defValue) == null)
   return env;
  int len = env.length;
  String nameAssign = name.concat("=");
  String str;
  for (int i = 0; i < len; i++)
   if ((str = env[i]) != null && str.startsWith(nameAssign))
    return env;
  String[] newEnv = new String[len + 1];
  VMSystem.arraycopy(env, 0, newEnv, 0, len);
  newEnv[len] = nameAssign.concat(value);
  return newEnv;
 }

 private static String convertEnv(String[] env)
 {
  int len = env.length;
  StringBuilder sb = new StringBuilder();
  for (int i = 0; i < len; i++)
  {
   String envStr = env[i];
   if (envStr == null)
    throw new NullPointerException();
   if (envStr.length() > 0)
   {
    int pos = envStr.indexOf('\0', 0);
    sb.append(pos >= 0 ? envStr.substring(0, pos) : envStr).append('\0');
   }
  }
  return sb.toString();
 }

 private static String getValidProgExtsList()
 {
  String validDotExts = VMSystem.getenv("PATHEXT");
  return validDotExts != null || (validDotExts =
          getValidProgExtsList0()) != null ? validDotExts : "";
 }

 private static String convertProgName(String progName, boolean changeDir)
  throws IOException
 {
  int pos = progName.indexOf('\0', 0);
  if (pos >= 0)
   progName = progName.substring(0, pos);
  String validDotExts = VALID_PROG_EXTS_LIST;
  boolean isSearchNeeded = IS_PROG_SEARCH_NEEDED;
  if (progName.length() == 0 ||
      (validDotExts.length() == 0 && !isSearchNeeded))
   return (new File(progName)).getPath();
  String name = (new File(progName)).getName();
  String searchList = isSearchNeeded && progName.equals(name) ?
                       VMSystem.getenv("PATH") : null;
  char pathSep =
   SystemProperties.getProperty("path.separator", ":").charAt(0);
  if (searchList == null)
   searchList = "";
  if (name.lastIndexOf('.') >= 0)
   validDotExts = "";
  pos = searchList.length();
  if (pos > 0 && searchList.charAt(pos - 1) != pathSep &&
      (searchList.charAt(pos - 1) != '.' ||
      (pos != 1 && searchList.charAt(pos - 2) != pathSep)))
   searchList = searchList.concat(String.valueOf(pathSep)).concat(".");
  if ((pos = validDotExts.length()) > 0 && validDotExts.charAt(pos - 1) !=
      pathSep && validDotExts.charAt(0) != pathSep)
   validDotExts = validDotExts.concat(String.valueOf(pathSep));
  pos = 0;
  String prefix;
  File file;
  String path = null;
  do
  {
   int next = searchList.indexOf(pathSep, pos);
   if (next < 0)
    next = searchList.length();
   prefix = searchList.substring(pos, next);
   if (pos == next && searchList.length() > 0)
    prefix = ".";
   int pos2 = 0;
   do
   {
    int next2 = validDotExts.indexOf(pathSep, pos2);
    if (next2 < 0)
     next2 = validDotExts.length();
    name = progName;
    if (pos2 < next2)
    {
     String ext = validDotExts.substring(pos2, next2);
     if (ext.charAt(0) != '.')
      ext = ".".concat(ext);
     name = progName.concat(ext);
    }
    file = prefix.length() > 0 ? new File(prefix, name) : new File(name);
    if (checkPermissions0(file.getPath(), 0) >= 0)
    {
     next = searchList.length();
     path = file.getPath();
     break;
    }
    pos2 = next2 + 1;
   } while (validDotExts.length() > pos2);
   pos = next + 1;
  } while (searchList.length() > pos);
  if (path == null)
   throw new IOException("executable file not found: ".concat(progName));
  if (changeDir && isSearchNeeded && !file.isAbsolute())
  {
   if (prefix.equals("."))
    file = new File(name);
   path = file.getAbsoluteFile().getPath();
  }
  return path;
 }

 private static String convertCmd(String progName, String[] cmd,
   boolean isSpaceDelimited)
 {
  StringBuilder sb = new StringBuilder();
  String cmdStr = progName;
  int i = 0;
  if (isSpaceDelimited)
  {
   do
   {
    if (cmdStr != null)
    {
     if (cmdStr.length() > 0 && cmdStr.indexOf(' ') < 0 &&
         cmdStr.indexOf('\t') < 0 && cmdStr.indexOf('\n') < 0 &&
         cmdStr.indexOf('\r') < 0 && cmdStr.indexOf('"') < 0)
      sb.append(cmdStr);
      else
      {
       sb.append('"');
       do
       {
        int j = cmdStr.indexOf('"');
        int k = cmdStr.indexOf('\\');
        if (k >= 0 && (j < 0 || k < j))
         j = k;
        if (j < 0)
         break;
        sb.append(cmdStr.substring(0, j));
        if (j == k)
        {
         int len = cmdStr.length();
         do
         {
          j++;
         } while (j < len && cmdStr.charAt(j) == '\\');
         sb.append(cmdStr.substring(k, j));
         if (j >= len || cmdStr.charAt(j) == '"')
          sb.append(cmdStr.substring(k, j));
         j--;
        }
         else sb.append('\\').append('"');
        cmdStr = cmdStr.substring(j + 1);
       } while (true);
       sb.append(cmdStr).append('"');
      }
    }
    if (++i >= cmd.length)
     break;
    if (cmdStr != null)
     sb.append(' ');
    cmdStr = cmd[i];
    int pos = cmdStr.indexOf('\0', 0);
    if (pos >= 0)
     cmdStr = cmdStr.substring(0, pos);
   } while (true);
   sb.append('\0');
  }
   else
   {
    do
    {
     if (cmdStr != null)
     {
      if (cmdStr.length() == 0 || cmdStr.charAt(0) == ' ')
       sb.append(' ');
      sb.append(cmdStr).append('\0');
     }
     if (++i >= cmd.length)
      break;
     cmdStr = cmd[i];
     int pos = cmdStr.indexOf('\0', 0);
     if (pos >= 0)
      cmdStr = cmdStr.substring(0, pos);
    } while (true);
   }
  return sb.toString();
 }

 private static InputStream createInputStream(VMChannel ch)
 {
  return VMAccessorJavaIo.newFileInputStream(
          VMAccessorGnuJavaNio.newFileChannelImpl(ch, FileChannelImpl.READ));
 }

 private static OutputStream createOutputStream(VMChannel ch)
 {
  return VMAccessorJavaIo.newFileOutputStream(
          VMAccessorGnuJavaNio.newFileChannelImpl(ch, FileChannelImpl.WRITE));
 }

 private static native String getValidProgExtsList0();

 private static native int isProgSearchNeeded0();

 private static native int isCmdSpaceDelimited0();

 private static native int checkPermissions0(String path, int isDirCheck);

 private static native int getSpawnWorkBufSize0(String cmdZBlock,
   String envZBlock, int cmdArrLen, int envArrLen);

 private static native int nativeSpawn0(String progName, String cmdZBlock,
   String envZBlock, String dirpath, int[] fdsArr, long[] pidArr,
   byte[] workBuf, int bufLen, int redirect);

 private static native int nativeWaitFor0(long pid,
   int nohang); /* blocking syscall */

 private static native int nativeKill(long pid);

 private static native int pipe0(int[] fdsArr);
}
