/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMSystem.java --
 * VM specific methods for Java "System" class.
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
import gnu.classpath.VMAccessorGnuClasspath;

import java.lang.reflect.Field;
import java.lang.reflect.VMAccessorJavaLangReflect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.VMAccessorJavaIo;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.LinkedList;
import java.util.List;

final class VMSystem /* hard-coded class name */
{

 private VMSystem() {}

 static native void arraycopy(Object src, int srcStart, Object dest,
   int destStart, int len); /* JVM-core */ /* hard-coded method signature */

 static native int identityHashCode(Object obj); /* JVM-core */

 static void setIn(InputStream in)
 {
  if (System.in != in && in != null)
  {
   try
   {
    setFinalStaticField(System.class.getDeclaredField("in"), in); /* hack */
   }
   catch (Exception e)
   {
    throw (Error) (new InternalError("VMSystem")).initCause(e);
   }
  }
 }

 static void setOut(PrintStream out)
 {
  if (System.out != out && out != null)
  {
   try
   {
    setFinalStaticField(System.class.getDeclaredField("out"), out); /* hack */
   }
   catch (Exception e)
   {
    throw (Error) (new InternalError("VMSystem")).initCause(e);
   }
   VMThread.setSystemOut(out);
  }
 }

 static void setErr(PrintStream err)
 {
  if (System.err != err && err != null)
  {
   try
   {
    setFinalStaticField(System.class.getDeclaredField("err"), err); /* hack */
   }
   catch (Exception e)
   {
    throw (Error) (new InternalError("VMSystem")).initCause(e);
   }
  }
 }

 static long currentTimeMillis()
 {
  return VMAccessorJavaIo.currentTimeVMFile(false);
 }

 static long nanoTime()
 {
  return VMAccessorJavaIo.currentTimeVMFile(true);
 }

 static InputStream makeStandardInputStream()
 {
  return new BufferedInputStream(new FileInputStream(FileDescriptor.in));
 }

 static PrintStream makeStandardOutputStream()
 {
  PrintStream out = makePrintStream(new BufferedOutputStream(
                     new FileOutputStream(FileDescriptor.out), 512));
  VMThread.setSystemOut(out);
  return out;
 }

 static PrintStream makeStandardErrorStream()
 {
  PrintStream err = makePrintStream(new FileOutputStream(FileDescriptor.err));
  VMThread.initSystemErr();
  return err;
 }

 static String getenv(String name)
 {
  if (name == null)
   throw new NullPointerException();
  name = toUpperCaseLatin(name);
  String value = VMAccessorJavaIo.getenvPlatformVMFile(name);
  if (value != null)
  {
   if (name.equals("HOME") || name.equals("HOMEPATH") || name.equals("PWD") ||
       name.equals("TEMP") || name.equals("TMP") || name.endsWith("_HOME") ||
       name.endsWith("DIR") || name.startsWith("DIR_"))
    value = VMAccessorJavaIo.normPlatformPathVMFile(value);
    else if (name.equals("JAVA_FONTS") || name.endsWith("PATH") ||
             name.startsWith("PATH"))
     value = VMAccessorJavaIo.normPlatformListOfPathsVMFile(value);
  }
   else if (name.equals("PWD"))
    value = SystemProperties.getProperty("user.dir", ".");
  return value;
 }

 static List environ()
 {
  /* not implemented */
  return new LinkedList();
 }

 static final String concat0X(String str,
   String str2) /* hard-coded method signature */
 { /* called from native code */
  if (str == null)
   str = "null";
  if (str2 == null)
   str2 = "null";
  return str.concat(str2);
 }

 static final String toUpperCaseLatin(String str)
 { /* used by VM classes only */
  char[] chars = str.toCharArray();
  int i = chars.length;
  boolean replaced = false;
  char ch;
  while (i-- > 0)
   if ((ch = chars[i]) >= 'a' && ch <= 'z')
   {
    chars[i] = (char) (ch - ('a' - 'A'));
    replaced = true;
   }
  return replaced ? new String(chars, 0, chars.length, true) : str;
 }

 private static PrintStream makePrintStream(OutputStream out)
 {
  try
  {
   return new PrintStream(out, true,
           VMAccessorGnuClasspath.getConsoleEncodingVMSystemProperties(out));
  }
  catch (UnsupportedEncodingException e) {}
  try
  {
   return new PrintStream(out, true, "ISO8859_1");
  }
  catch (UnsupportedEncodingException e)
  {
   throw (Error) (new InternalError("VMSystem")).initCause(e);
  }
 }

 private static void setFinalStaticField(final Field field, Object value)
  throws IllegalAccessException
 {
  Field nonFinalField = (Field) AccessController.doPrivileged(
   new PrivilegedAction()
   {

    public Object run()
    {
     return VMAccessorJavaLangReflect.createNonFinalAccessibleVMField(field);
    }
   });
  nonFinalField.set(null, value);
 }
}
