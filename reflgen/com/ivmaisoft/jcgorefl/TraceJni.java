/*
 * @(#) $(JCGO)/reflgen/com/ivmaisoft/jcgorefl/TraceJni.java --
 * "TraceJni" utility source (part of JCGO).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * Use is subject to license terms. No warranties. All rights reserved.
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
 */

/**
 * The utility launches "main" method of the specified class, collects
 * Class/JNI constructor/method/field reflection dependency info (i.e.
 * which reflected constructors, methods, fields are used and by which
 * Java and/or native methods) and stores it into the data file.
 **
 * To collect reflection dependency info for native callers, the native
 * library ("trjnic") is loaded and initialized (unless "-n" command-line
 * option is specified).
 **
 * To collect reflection dependency info for the callers written in Java,
 * the standard "java.lang.Class" class should be manually patched (the
 * interception hooks should be added as described bellow) and forced to
 * be loaded by VM (e.g., by specifying java "-Xbootclasspath/p" option).
 */

package com.ivmaisoft.jcgorefl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.util.Hashtable;
import java.util.Vector;

public final class TraceJni extends Thread
{

 private static PrintStream systemOut;

 private static PrintStream systemErr;

 private static final Hashtable insideHandler = new Hashtable();

 private static final Vector curEntries = new Vector();

 private static String datFileName;

 private static Hashtable oldSet;

 private static Hashtable currSet;

 private TraceJni() {}

 /**
  * Usage in java/lang/Class.java:
  *
  * public Object newInstance()
  *  throws InstantiationException, IllegalAccessException
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_newInstance(this);
  *  ...
  * }
  */
 public static final void intercept_newInstance(Class This)
 {
  if (This != null)
   processInterceptInner(true, This, "<init>", "()", null, true);
 }

 /**
  * Usage in java/lang/Class.java:
  *
  * public Constructor getConstructor(Class[] parameterTypes)
  *  throws NoSuchMethodException
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getConstructor(false, this,
  *   parameterTypes);
  *  ...
  * }
  *
  * ...
  *
  * public Constructor getDeclaredConstructor(Class[] parameterTypes)
  *  throws NoSuchMethodException
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getConstructor(true, this,
  *   parameterTypes);
  *  ...
  * }
  */
 public static final void intercept_getConstructor(boolean isDeclared,
   Class This, Class[] parameterTypes)
 { /* ignore isDeclared */
  if (This != null)
   processInterceptInner(true, This, "<init>", "()", parameterTypes, true);
 }

 /**
  * Usage in java/lang/Class.java:
  *
  * public Constructor[] getConstructors()
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getConstructors(false, this);
  *  ...
  * }
  *
  * ...
  *
  * public Constructor[] getDeclaredConstructors()
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getConstructors(true, this);
  *  ...
  * }
  */
 public static final void intercept_getConstructors(boolean isDeclared,
   Class This)
 {
  if (This != null)
   processInterceptInner(isDeclared, This, "<init>", "(*)", null, true);
 }

 /**
  * Usage in java/lang/Class.java:
  *
  * public Method getMethod(String name, Class[] parameterTypes)
  *  throws NoSuchMethodException
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getMethod(false, this, name,
  *   parameterTypes);
  *  ...
  * }
  *
  * ...
  *
  * public Method getDeclaredMethod(String name, Class[] parameterTypes)
  *  throws NoSuchMethodException
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getMethod(true, this, name,
  *   parameterTypes);
  *  ...
  * }
  */
 public static final void intercept_getMethod(boolean isDeclared, Class This,
   String name, Class[] parameterTypes)
 {
  if (currSet != null && This != null && name != null &&
      !isSpecialSynthMethod(name))
   processInterceptInner(isDeclared, This, name, "()", parameterTypes, true);
 }

 /**
  * Usage in java/lang/Class.java:
  *
  * public Method[] getMethods()
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getMethods(false, this);
  *  ...
  * }
  *
  * ...
  *
  * public Method[] getDeclaredMethods()
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getMethods(true, this);
  *  ...
  * }
  */
 public static final void intercept_getMethods(boolean isDeclared, Class This)
 {
  if (This != null)
   processInterceptInner(isDeclared, This, "*", "(*)", null, true);
 }

 /**
  * Usage in java/lang/Class.java:
  *
  * public Field getField(String name)
  *  throws NoSuchFieldException
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getField(false, this, name);
  *  ...
  * }
  *
  * ...
  *
  * public Field getDeclaredField(String name)
  *  throws NoSuchFieldException
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getField(true, this, name);
  *  ...
  * }
  */
 public static final void intercept_getField(boolean isDeclared, Class This,
   String name)
 {
  if (This != null && name != null)
   processInterceptInner(isDeclared, This, name, "", null, true);
 }

 /**
  * Usage in java/lang/Class.java:
  *
  * public Field[] getFields()
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getFields(false, this);
  *  ...
  * }
  *
  * ...
  *
  * public Field[] getDeclaredFields()
  * {
  *  com.ivmaisoft.jcgorefl.TraceJni.intercept_getFields(true, this);
  *  ...
  * }
  */
 public static final void intercept_getFields(boolean isDeclared, Class This)
 {
  if (This != null)
   processInterceptInner(isDeclared, This, "*", "", null, true);
 }

 public static final void main(String[] args)
 {
  if (args == null)
  {
   try
   {
    TraceJni.class.getDeclaredMethod("processIntercept",
     new Class[] { Class.class, String.class, String.class });
   }
   catch (NoSuchMethodException e) {}
   processIntercept(null, null, null);
  }
  if (args.length < 2)
  {
   System.out.println("JCGO TraceJni utility v1.2");
   System.out.println(
    "Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>");
   System.out.println(
    "This is free software. All rights reserved. See README file.");
   System.out.println("The utility collects Class/JNI ctor/method/field" +
    " reflection dependency info.");
   System.out.println("");
   System.out.println(
    "Arguments: [-n] file.dat [+file2.dat ...] <classname> [args ...]");
   System.out.println(
    "Option \"-n\" disables loading of the native library (\"trjnic\").");
   if (args.length > 0)
    System.exit(1);
   return;
  }
  boolean loadJniLib = true;
  int pos = 0;
  if (args[0].equals("-n") && args.length > 2)
  {
   loadJniLib = false;
   pos = 1;
  }
  datFileName = args[pos];
  systemOut = System.out;
  systemErr = System.err;
  System.out.println("TraceJni: Reading data files...");
  pos = loadOldInfo(args, pos);
  if (pos == 0)
   System.exit(2);
  if (loadJniLib)
  {
   try
   {
    System.loadLibrary("trjnic");
   }
   catch (UnsatisfiedLinkError e)
   {
    System.err.println("Error: cannot load native library: " +
     e.getMessage());
    System.exit(3);
   }
  }
  Class aclass = null;
  try
  {
   aclass = Class.forName(args[pos]);
  }
  catch (ClassNotFoundException e)
  {
   System.err.println("Error: class not found: " + args[pos]);
   System.exit(4);
  }
  if ((aclass.getModifiers() & Modifier.PUBLIC) == 0)
  {
   System.err.println("Error: class is not public: " + aclass.getName());
   System.exit(4);
  }
  Method method = null;
  try
  {
   System.out.println("TraceJni: Starting application...");
   method = aclass.getDeclaredMethod("main", new Class[] { String[].class });
  }
  catch (NoSuchMethodException e)
  {
   System.err.println("Error: no main(String[]) method");
   System.exit(5);
  }
  if ((method.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) !=
      (Modifier.PUBLIC | Modifier.STATIC))
  {
   System.err.println("Error: method 'main' is not public static");
   System.exit(5);
  }
  String[] args2 = new String[args.length - pos - 1];
  System.arraycopy(args, pos + 1, args2, 0, args2.length);
  Runtime.getRuntime().addShutdownHook(new TraceJni());
  if (loadJniLib)
   initIntercept();
  try
  {
   currSet = new Hashtable();
   method.invoke(null, new Object[] { args2 });
  }
  catch (Exception e)
  {
   currSet = null;
   systemOut.println("");
   systemErr.println("Error: cannot invoke main method!");
   System.exit(5);
  }
 }

 /**
  * This method is called from "trjnic" native library (JNI).
  */
 static final void processIntercept(Class clazz, String name, String sig)
 {
  if (currSet != null && clazz != null && name != null && sig != null &&
      !name.equals("<clinit>"))
   processInterceptInner(name.equals("<init>"), clazz, name,
    sig.substring(0, sig.indexOf(')') + 1), null, false);
 }

 public final void run()
 {
  storeInfo();
 }

 private static int loadOldInfo(String[] args, int pos)
 {
  oldSet = new Hashtable();
  try
  {
   if (loadOneFile(datFileName, true))
   {
    while (++pos < args.length - 1)
    {
     String filename = args[pos];
     if (filename.length() == 0 || filename.charAt(0) != '+')
      break;
     if (filename.length() == 1)
     {
      if (args.length - 2 == pos)
       break;
      filename = args[++pos];
     }
      else filename = filename.substring(1);
     if (!loadOneFile(filename, false))
     {
      pos = 0;
      break;
     }
    }
   }
    else pos = 0;
  }
  catch (SecurityException e)
  {
   pos = 0;
  }
  return pos;
 }

 private static boolean loadOneFile(String filename, boolean isFirst)
 {
  BufferedReader reader;
  try
  {
   reader = new BufferedReader(new FileReader(filename));
  }
  catch (IOException e)
  {
   if (isFirst)
    return true;
   System.err.println("Error: cannot open data file: " + filename);
   return false;
  }
  try
  {
   String str;
   while ((str = reader.readLine()) != null)
    if ((str = str.trim()).length() > 0)
     oldSet.put(str, "");
   reader.close();
  }
  catch (IOException e)
  {
   System.err.println("Error: cannot read data file: " + filename);
   return false;
  }
  return true;
 }

 static final void storeInfo()
 {
  if (currSet == null)
   return;
  currSet = null;
  int count = curEntries.size();
  systemOut.println("");
  systemOut.println(" TraceJni: Dependencies found: " + count);
  if (count != 0)
  {
   try
   {
    PrintWriter writer = new PrintWriter(new FileWriter(datFileName, true));
    writer.println("");
    for (int i = 0; i < count; i++)
     writer.println((String) curEntries.elementAt(i));
    writer.close();
    systemOut.println(" TraceJni: Data saved!");
    return;
   }
   catch (IOException e) {}
   catch (SecurityException e) {}
   systemErr.println(" TraceJni: Error: cannot write data file!");
  }
 }

 private static boolean isSpecialSynthMethod(String name)
 {
  return name.equals("class$") || name.startsWith("access$");
 }

 private static boolean isSpecialFieldExcl(String name)
 {
  return name.equals("$VALUES") || name.equals("$assertionsDisabled") ||
          name.equals("ENUM$VALUES") || name.equals("cl$") ||
          name.startsWith("$SWITCH_TABLE$") ||
          name.startsWith("$SwitchMap$") || name.startsWith("array$") ||
          name.startsWith("class$");
 }

 private static void processInterceptInner(boolean isDeclared, Class clazz,
   String name, String sig, Class[] types, boolean isNotJniCall)
 {
  if (currSet != null)
  {
   try
   {
    Thread currThread = Thread.currentThread();
    if (insideHandler.put(currThread, "") == null)
    {
     String str = getCallerString(new Throwable(), isNotJniCall);
     String callerClassName = "?";
     String callerMethodName = "?";
     int pos = -1;
     if (str != null && (pos = str.lastIndexOf('.')) > 0)
     {
      callerClassName = str.substring(0, pos);
      callerMethodName = str.substring(pos + 1);
     }
     Hashtable oldSetL = oldSet;
     if (oldSetL != null && (pos <= 0 ||
         ((isNotJniCall || !callerClassName.startsWith("java.lang.")) &&
         oldSetL.get(callerClassName.substring(0,
         pos = callerClassName.lastIndexOf('.') + 1) + "*") == null &&
         (!callerMethodName.equals("<clinit>") ||
         !callerClassName.substring(pos).startsWith("$Proxy")))))
     {
      if (name.equals("*") || sig.equals("(*)"))
      {
       String className = clazz.getName();
       try
       {
        if (name.equals("<init>"))
        {
         Constructor[] ctors = isDeclared ? clazz.getDeclaredConstructors() :
                                clazz.getConstructors();
         int len = ctors.length;
         for (int i = 0; i < len; i++)
          addNewEntry(callerClassName, callerMethodName, className, name,
           toMethodSig(ctors[i].getParameterTypes()));
        }
         else
         {
          if (sig.length() > 0)
          {
           Method[] methods = isDeclared ? clazz.getDeclaredMethods() :
                               clazz.getMethods();
           int len = methods.length;
           for (int i = 0; i < len; i++)
           {
            Method method = methods[i];
            String methodName = method.getName();
            if (!isSpecialSynthMethod(methodName))
             addNewEntry(callerClassName, callerMethodName,
              method.getDeclaringClass().getName(), methodName,
              toMethodSig(method.getParameterTypes()));
           }
          }
           else
           {
            Field[] fields = isDeclared ? clazz.getDeclaredFields() :
                              clazz.getFields();
            int len = fields.length;
            for (int i = 0; i < len; i++)
            {
             Field field = fields[i];
             String fieldName = field.getName();
             if (!isDeclared || !isSpecialFieldExcl(fieldName))
              addNewEntry(callerClassName, callerMethodName,
               field.getDeclaringClass().getName(), fieldName, sig);
            }
           }
         }
       }
       catch (SecurityException e)
       {
        addNewEntry(callerClassName, callerMethodName, className, name, sig);
       }
      }
       else
       {
        if ((types == null || (sig = toMethodSig(types)) != null) &&
            (isDeclared || (clazz = declaringClassOf(clazz, name, sig, types,
            isNotJniCall)) != null))
        {
         String className = clazz.getName();
         if (name.equals("<init>") && className.substring(
             className.lastIndexOf('.') + 1).startsWith("$Proxy"))
          className = toProxyClassName(clazz.getInterfaces());
         addNewEntry(callerClassName, callerMethodName, className, name, sig);
        }
       }
     }
     insideHandler.remove(currThread);
    }
   }
   catch (Throwable e)
   {
    insideHandler.remove(Thread.currentThread());
    if (e instanceof ThreadDeath)
     throw (ThreadDeath) e;
    if (e instanceof OutOfMemoryError)
     throw (OutOfMemoryError) e;
    try
    {
     PrintStream out = systemOut;
     if (out != null)
      out.println("");
     out = systemErr;
     if (out != null)
      out.println(" TraceJni: Interception processing error: " +
       e.toString());
    }
    catch (Throwable e2) {}
    System.exit(6);
   }
  }
 }

 private static String getCallerString(Throwable exc, boolean ignoreJavaLang)
 {
  CharArrayWriter writer = new CharArrayWriter();
  PrintWriter printwriter = new PrintWriter(writer);
  exc.printStackTrace(printwriter);
  printwriter.flush();
  String trace = writer.toString();
  int pos = strNextLinePos(trace, 0);
  String str = null;
  if (pos > 0 && (pos = strNextLinePos(trace, pos)) > 0)
   do
   {
    pos = strNextLinePos(trace, pos);
    if (pos <= 0)
     break;
    int endPos = trace.indexOf('(', pos);
    int next = strNextLinePos(trace, pos);
    if (next <= 0)
     next = trace.length();
    if (endPos <= pos || endPos >= next)
     break;
    int beginPos = trace.lastIndexOf(' ', endPos - 1);
    if (beginPos < pos)
     break;
    str = trace.substring(beginPos + 1, endPos);
   } while (ignoreJavaLang && str.startsWith("java.lang."));
  return str;
 }

 private static int strNextLinePos(String str, int pos)
 {
  int next = str.indexOf('\n', pos);
  int next2 = str.indexOf('\r', pos);
  return (next > next2 ? next : next2) + 1;
 }

 private static Class declaringClassOf(Class clazz, String name, String sig,
   Class[] types, boolean isPublicOnly)
 {
  try
  {
   if (sig.length() > 0)
   {
    if (types != null || (types = decodeMethodSig(sig, clazz)) != null)
    {
     Method m = null;
     try
     {
      m = clazz.getMethod(name, types);
     }
     catch (NoSuchMethodException e)
     {
      if (isPublicOnly)
       return null;
      m = getMethodOf(clazz, name, types);
     }
     if (m != null)
      clazz = m.getDeclaringClass();
    }
   }
    else
    {
     Field f = null;
     try
     {
      f = clazz.getField(name);
     }
     catch (NoSuchFieldException e)
     {
      if (isPublicOnly)
       return null;
      f = getFieldOf(clazz, name);
     }
     if (f != null)
      clazz = f.getDeclaringClass();
    }
  }
  catch (SecurityException e) {}
  return clazz;
 }

 private static Field getFieldOf(Class clazz, String name)
 {
  Field f = null;
  do
  {
   try
   {
    f = clazz.getDeclaredField(name);
   }
   catch (NoSuchFieldException e) {}
   if (f != null)
    break;
   Class[] interfaces = clazz.getInterfaces();
   for (int i = 0; i < interfaces.length; i++)
    if ((f = getFieldOf(interfaces[i], name)) != null)
     return f;
   clazz = clazz.getSuperclass();
  } while (clazz != null);
  return f;
 }

 private static Method getMethodOf(Class clazz, String name, Class[] types)
 {
  Method m = null;
  do
  {
   try
   {
    m = clazz.getDeclaredMethod(name, types);
   }
   catch (NoSuchMethodException e) {}
   if (m != null)
    break;
   Class[] interfaces = clazz.getInterfaces();
   for (int i = 0; i < interfaces.length; i++)
    if ((m = getMethodOf(interfaces[i], name, types)) != null)
     return m;
   clazz = clazz.getSuperclass();
  } while (clazz != null);
  return m;
 }

 private static String toMethodSig(Class[] types)
 {
  StringBuffer sb = new StringBuffer();
  int len = types.length;
  sb.append('(');
  for (int i = 0; i < len; i++)
  {
   Class clazz = types[i];
   if (clazz != null)
   {
    Class aclass;
    while ((aclass = clazz.getComponentType()) != null)
    {
     sb.append('[');
     clazz = aclass;
    }
   }
    else clazz = Object.class;
   char ch = getPrimTypeChar(clazz);
   sb.append(ch);
   if (ch == 'L')
   {
    String className = clazz.getName();
    if (className.substring(
        className.lastIndexOf('.') + 1).startsWith("$Proxy"))
     return null;
    sb.append(className.replace('.', '/'));
    sb.append(';');
   }
  }
  sb.append(')');
  return sb.toString();
 }

 private static char getPrimTypeChar(Class clazz)
 {
  if (clazz == boolean.class)
   return 'Z';
  if (clazz == byte.class)
   return 'B';
  if (clazz == char.class)
   return 'C';
  if (clazz == short.class)
   return 'S';
  if (clazz == int.class)
   return 'I';
  if (clazz == long.class)
   return 'J';
  if (clazz == float.class)
   return 'F';
  if (clazz == double.class)
   return 'D';
  return 'L';
 }

 private static Class[] decodeMethodSig(String sig, Class clazz)
 {
  int len = sig.length();
  if (len <= 1 || sig.charAt(0) != '(')
   return null;
  int pos = 1;
  int count = 0;
  char ch;
  while ((ch = sig.charAt(pos)) != ')')
  {
   if (ch != '[')
   {
    if (ch == 'L' && (pos = sig.indexOf(';', pos + 1)) < 0)
     return null;
    count++;
   }
   if (++pos >= len)
    return null;
  }
  Class[] types = new Class[count];
  pos = 1;
  for (int i = 0; i < count; i++)
  {
   int next = pos;
   while ((ch = sig.charAt(next)) == '[')
    next++;
   next = (ch == 'L' ? sig.indexOf(';', next + 1) : next) + 1;
   Class type = classForSig(sig.substring(pos, next), clazz);
   if (type == null || type == void.class)
    return null;
   types[i] = type;
   pos = next;
  }
  return types;
 }

 private static Class classForSig(String sig, Class clazz)
 {
  int nameLen = sig.length();
  Class aclass = null;
  if (nameLen > 0)
  {
   char ch = sig.charAt(0);
   if (nameLen == 1)
    return getPrimitiveClass(ch);
   if (sig.indexOf('.', 0) < 0)
   {
    if (ch == 'L')
    {
     if (sig.charAt(nameLen - 1) != ';')
      return null;
     sig = sig.substring(1, nameLen - 1);
    }
     else if (ch != '[')
      return null;
    try
    {
     aclass = Class.forName(sig.replace('/', '.'), true,
               clazz != null ? clazz.getClassLoader() : null);
    }
    catch (ClassNotFoundException e) {}
   }
  }
  return aclass;
 }

 private static Class getPrimitiveClass(char ch)
 {
  switch (ch)
  {
  case 'Z':
   return boolean.class;
  case 'B':
   return byte.class;
  case 'C':
   return char.class;
  case 'S':
   return short.class;
  case 'I':
   return int.class;
  case 'J':
   return long.class;
  case 'F':
   return float.class;
  case 'D':
   return double.class;
  case 'V':
   return void.class;
  }
  return null;
 }

 private static String toProxyClassName(Class[] interfaces)
 {
  String className = "$Proxy";
  String pkgPrefix = null;
  int len = interfaces.length;
  for (int i = 0; i < len; i++)
  {
   Class clazz = interfaces[i];
   String name = clazz.getName();
   if (pkgPrefix == null && (clazz.getModifiers() & Modifier.PUBLIC) == 0)
    pkgPrefix = name.substring(0, name.lastIndexOf('.') + 1);
   className = className + "$00" + toProxyNamePart(name);
  }
  return pkgPrefix != null ? pkgPrefix + className : className;
 }

 private static String toProxyNamePart(String name)
 {
  int pos = -1;
  while ((pos = name.indexOf('$', pos + 1) + 1) > 0)
   name = name.substring(0, pos) + "$" + name.substring(pos);
  pos = -1;
  while ((pos = name.indexOf('.', pos + 1) + 1) > 0)
   name = name.substring(0, pos - 1) + "$0" + name.substring(pos);
  return name;
 }

 private static void addNewEntry(String callerClassName,
   String callerMethodName, String className, String name, String sig)
 {
  Hashtable currSetL = currSet;
  Hashtable oldSetL = oldSet;
  if (currSetL != null && oldSetL != null && sig != null)
  {
   sig = (className.equals(callerClassName) ? "" : className + ".") +
          name + sig;
   String str = callerClassName + "." + callerMethodName + "(*):" + sig;
   if (currSetL.get(str) == null && oldSetL.get(str) == null &&
       oldSetL.get(callerClassName + ".<native>(*):" + sig) == null &&
       oldSetL.get(callerClassName + ".*(*):" + sig) == null &&
       currSetL.put(str, "") == null)
   {
    curEntries.addElement(str);
    printPlusChar();
   }
  }
 }

 private static void printPlusChar()
 {
  PrintStream out = systemOut;
  if (out != null)
  {
   try
   {
    out.print("+");
    out.flush();
   }
   catch (RuntimeException e) {}
  }
 }

 /**
  * This method is called to initialize "trjnic" native library (JNI).
  */
 private static native void initIntercept();
}
