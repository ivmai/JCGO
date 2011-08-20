/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/Object.java --
 * The universal superclass in Java.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
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

package java.lang;

public class Object /* hard-coded class name */
{

 /* Other hard-coded non-VM class names (and class member names):
  gnu.classpath.ServiceFactory$ServiceIterator (next()),
  gnu.classpath.ServiceProviderLoadingAction,
  gnu.java.lang.CharData,
  java.io.Externalizable,
  java.io.ObjectInputStream,
  java.io.ObjectOutputStream,
  java.io.ObjectStreamClass,
  java.io.PrintStream,
  java.io.Serializable (readObject(java.io.ObjectInputStream), readResolve(),
   serialPersistentFields, serialVersionUID,
   writeObject(java.io.ObjectOutputStream), writeReplace()),
  java.lang.ArithmeticException,
  java.lang.ArrayIndexOutOfBoundsException,
  java.lang.ArrayStoreException,
  java.lang.AssertionError,
  java.lang.Boolean,
  java.lang.Byte,
  java.lang.Character,
  java.lang.ClassCastException,
  java.lang.ClassLoader$StaticData (systemClassLoader),
  java.lang.Cloneable,
  java.lang.Double,
  java.lang.Enum (ordinal(), valueOf(java.lang.Class, java.lang.String)),
  java.lang.Error,
  java.lang.Exception,
  java.lang.ExceptionInInitializerError,
  java.lang.Float,
  java.lang.IncompatibleClassChangeError,
  java.lang.IndexOutOfBoundsException,
  java.lang.InstantiationException,
  java.lang.Integer,
  java.lang.LinkageError,
  java.lang.Long,
  java.lang.NegativeArraySizeException,
  java.lang.NoClassDefFoundError,
  java.lang.NoSuchFieldError,
  java.lang.NoSuchMethodError,
  java.lang.NullPointerException,
  java.lang.OutOfMemoryError,
  java.lang.Runnable (run()),
  java.lang.RuntimeException,
  java.lang.Short,
  java.lang.StringIndexOutOfBoundsException (<init>(int)),
  java.lang.System,
  java.lang.Thread (<init>(java.lang.ThreadGroup, java.lang.Runnable,
   java.lang.String, long)),
  java.lang.ThreadGroup,
  java.lang.Throwable,
  java.lang.VirtualMachineError,
  java.lang.ref.ReferenceQueue,
  java.lang.ref.SoftReference,
  java.lang.reflect.InvocationHandler,
  java.lang.reflect.Proxy (<init>(java.lang.reflect.Invocationhandler),
   getProxyClass(java.lang.ClassLoader, java.lang.Class[]),
   newProxyInstance(java.lang.ClassLoader, java.lang.Class[],
   java.lang.reflect.InvocationHandler)),
  java.util.ListResourceBundle,
  java.util.logging.LogManager,
  javax.swing.UIDefaults$ProxyLazyValue (<init>(java.lang.String),
   <init>(java.lang.String, java.lang.Object[]), <init>(java.lang.String,
   java.lang.String), <init>(java.lang.String, java.lang.String,
   java.lang.Object[])),
  javax.swing.plaf.ColorUIResource.
 */

 public Object() {}

 public boolean equals(Object obj)
 {
  return this == obj;
 }

 public int hashCode()
 {
  return VMSystem.identityHashCode(this);
 }

 public String toString() /* hard-coded method signature */
 {
  StringBuilder sb = new StringBuilder();
  VMObject.appendClassName(this, sb);
  sb.append('@');
  sb.append(Integer.toHexString(hashCode()));
  return sb.toString();
 }

 protected void finalize() /* hard-coded method signature */
  throws Throwable {}

 protected Object clone()
  throws CloneNotSupportedException
 {
  if (!(this instanceof Cloneable))
   throw new CloneNotSupportedException("Object not cloneable");
  return VMObject.clone((Cloneable) this);
 }

 public final Class getClass() /* hard-coded method signature */
 {
  /* return VMObject.getClass(this); */
  return VMClass.arrayClassOf0X(VMObject.getClass0(this), /* hack */
          VMObject.getObjArrayDims0(this));
 }

 public final void notify()
 {
  VMThread.notify(this, false);
 }

 public final void notifyAll()
 {
  VMThread.notify(this, true);
 }

 public final void wait()
  throws InterruptedException
 {
  VMThread.wait(this, 0L, 0);
 }

 public final void wait(long ms)
  throws InterruptedException
 {
  if (ms < 0L)
   throw new IllegalArgumentException("timeout value is negative");
  VMThread.wait(this, ms, 0);
 }

 public final void wait(long ms, int ns)
  throws InterruptedException
 {
  if (ms < 0L)
   throw new IllegalArgumentException("timeout value is negative");
  if (((999999 - ns) | ns) < 0)
   throw new IllegalArgumentException(
          "nanosecond timeout value is out of range");
  VMThread.wait(this, ms, ns);
 }
}
