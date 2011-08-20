/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMThrowable.java --
 * VM specific methods for Java "Throwable" class.
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

final class VMThrowable /* hard-coded class name */
{

 private static OutOfMemoryError outOfMemoryError =
  new OutOfMemoryError(); /* hack */

 private final transient Object vmdata;

 static
 {
  setNullException0(createNullPointerException0X()); /* hack */
  if (outOfMemoryError == null) /* hack */
  {
   throwOutOfMemoryError0X(); /* hack */
   throwArithmeticException0X(); /* hack */
   throwArrayIndexOutOfBoundsException0X(); /* hack */
   throwArrayStoreException0X(); /* hack */
   throwClassCastException0X(); /* hack */
   throwNegativeArraySizeException0X(); /* hack */
   throwStringIndexOutOfBoundsException0X(); /* hack */
   throwUnsatisfiedLinkError0X(); /* hack */
   throwExceptionInInitializer0X(null, null); /* hack */
   createInstantiationException0X(null); /* hack */
   createNoClassDefFoundError0X(null, 0); /* hack */
   createNoSuchFieldError0X(null, null, null, 0); /* hack */
   createNoSuchMethodError0X(null, null, null, 0); /* hack */
  }
 }

 private VMThrowable(Object vmdata)
 {
  this.vmdata = vmdata;
 }

 static VMThrowable fillInStackTrace(Throwable throwable)
 {
  return new VMThrowable(fillInStackTrace0());
 }

 StackTraceElement[] getStackTrace(Throwable throwable)
 {
  Object vmdata = this.vmdata;
  int count = vmdata != null ? getStackTraceLen0(vmdata) : 0;
  StackTraceElement[] elements = new StackTraceElement[count];
  if (count > 0)
  {
   Class[] classes = new Class[count];
   String[] methodNames = new String[count];
   int[] lineNumbers = new int[count];
   getStackTraceFill0(vmdata, classes, methodNames, lineNumbers);
   for (int i = 0; i < count; i++)
   {
    Class aclass = classes[i];
    int lineNumber = lineNumbers[i];
    String filename = null;
    /* if (aclass != null &&
        (filename = VMClass.getSourceFilename(aclass)) != null)
     filename = filename.substring(filename.lastIndexOf('/') + 1); */
    elements[i] = new StackTraceElement(filename, lineNumber,
                   aclass != null ? getClassName(aclass) : null,
                   methodNames[i], lineNumber == -2);
   }
  }
  return elements;
 }

 static final void exit(int status)
 { /* used by VM classes only */
  if (status < 0 || status >= 255)
   status = 255;
  exit0(status);
 }

 static final Object createInstantiationException0X(Class aclass)
 { /* called from native code */
  return new InstantiationException(aclass != null ?
          "cannot instantiate class: ".concat(getClassName(aclass)) : null);
 }

 static final Object createNoClassDefFoundError0X(String name,
   int isErroneousState)
 { /* called from native code */
  NoClassDefFoundError error = new NoClassDefFoundError(name);
  if (isErroneousState != 0)
   error.initCause(new ExceptionInInitializerError());
  return error;
 }

 static final Object createNoSuchFieldError0X(Class aclass, String name,
   String sig, int isStatic)
 { /* called from native code */
  return new NoSuchFieldError(aclass != null && name != null ?
          getClassName(aclass).concat(".").concat(name) : null);
 }

 static final Object createNoSuchMethodError0X(Class aclass, String name,
   String sig, int isStatic)
 { /* called from native code */
  return new NoSuchMethodError(aclass != null && name != null ?
          getClassName(aclass).concat(".").concat(name).concat(sig != null &&
          sig.length() > 0 ? (sig.startsWith("(") ? sig : " ".concat(sig)) :
          "") : null);
 }

 static final Object createNullPointerException0X()
 { /* called from native code */
  return new NullPointerException();
 }

 static final void throwArithmeticException0X()
 { /* called from native code */
  throw new ArithmeticException("/ by zero");
 }

 static final void throwArrayIndexOutOfBoundsException0X()
 { /* called from native code */
  throw new ArrayIndexOutOfBoundsException();
 }

 static final void throwArrayStoreException0X()
 { /* called from native code */
  throw new ArrayStoreException();
 }

 static final void throwClassCastException0X()
 { /* called from native code */
  throw new ClassCastException();
 }

 static final void throwNegativeArraySizeException0X()
 { /* called from native code */
  throw new NegativeArraySizeException();
 }

 static final void throwStringIndexOutOfBoundsException0X()
 { /* called from native code */
  throw new StringIndexOutOfBoundsException();
 }

 static final void throwOutOfMemoryError0X()
 { /* called from native code */
  if (outOfMemoryError == null)
   exit0(255);
  throw outOfMemoryError;
 }

 static final void throwUnsatisfiedLinkError0X()
 { /* called from native code */
  throw new UnsatisfiedLinkError("Missing native function called!");
 }

 static final void throwExceptionInInitializer0X(Object throwableObj,
   Class aclass)
 { /* called from native code */
  throw throwableObj instanceof Error ? (Error) throwableObj :
         new ExceptionInInitializerError((Throwable) throwableObj);
 }

 private static String getClassName(Class aclass)
 {
  String name;
  if ((name = aclass.name) == null) /* hack */
   name = "<UnknownClass>";
  return name;
 }

 private static native int setNullException0(Object throwable); /* JVM-core */

 private static native Object fillInStackTrace0(); /* JVM-core */

 private static native int getStackTraceLen0(Object vmdata); /* JVM-core */

 private static native int getStackTraceFill0(Object vmdata, Class[] classes,
   String[] methodNames, int[] lineNumbers); /* JVM-core */

 private static native void exit0(
   int status); /* JVM-core */ /* never return */
}
