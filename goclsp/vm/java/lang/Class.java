/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/Class.java --
 * VM specific Java "Class" implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2010 Ivan Maidanski <ivmai@ivmaisoft.com>
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

/* Class.java -- Representation of a Java class.
   Copyright (C) 1998, 1999, 2000, 2002, 2003, 2004, 2005, 2006
   Free Software Foundation

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.lang;

import gnu.classpath.VMStackWalker;

import gnu.java.lang.reflect.ClassSignatureParser;

import java.io.InputStream;
import java.io.Serializable;

import java.lang.annotation.Annotation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.VMAccessorJavaLangReflect;

import java.net.URL;

import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public final class Class /* hard-coded class name */ /* const data */
 implements Serializable, GenericDeclaration, Type, AnnotatedElement
{

 static final class GetInterfaces /* hack */
 { /* used by VM classes only */

  static
  {
   Object.class.getInterfaces(); /* hack */
  }

  private GetInterfaces() {}
 }

 static final class GetClassName /* hack */
 { /* used by VM classes only */

  static
  {
   getClassName(Object.class); /* hack */
  }

  private GetClassName() {}

  private static String getClassName(Class klass) /* hack */
  {
   return klass.getName();
  }
 }

 private static final class MethodKey
 {

  private final String name;

  private final Class[] params;

  private final Class returnType;

  private final int hash;

  MethodKey(Method m)
  {
   String name = m.getName();
   Class[] params = m.getParameterTypes();
   Class returnType = m.getReturnType();
   int hash = name.hashCode() ^ returnType.getName().hashCode();
   for (int i = 0; i < params.length; i++)
    hash ^= params[i].getName().hashCode();
   this.name = name;
   this.params = params;
   this.returnType = returnType;
   this.hash = hash;
  }

  public boolean equals(Object obj)
  {
   if (obj instanceof MethodKey)
   {
    MethodKey m = (MethodKey) obj;
    if (m.name.equals(name) && m.params.length == params.length &&
        m.returnType == returnType)
    {
     for (int i = 0; i < params.length; i++)
      if (m.params[i] != params[i])
       return false;
     return true;
    }
   }
   return false;
  }

  public int hashCode()
  {
   return hash;
  }
 }

 private static final class StaticData
 {

  static final ProtectionDomain unknownProtectionDomain = createProtDomain();

  private static ProtectionDomain createProtDomain()
  {
   Permissions permissions = new Permissions();
   permissions.add(new AllPermission());
   return new ProtectionDomain(null, permissions);
  }
 }

 private static final long serialVersionUID = 0x2c7e5503d9bf9553L;

 private static final int SYNTHETIC = 0x1000;

 private static final int ANNOTATION = 0x2000;

 private static final int ENUM = 0x4000;

 private static final int MODIFIER_PUBLIC = 0x1;
 private static final int MODIFIER_FINAL = 0x10;
 private static final int MODIFIER_INTERFACE = 0x200;
 private static final int MODIFIER_ABSTRACT = 0x400;

 final transient Object vmdata; /* hard-coded type and name */

 final transient String name; /* hard-coded type and name */

 final transient Class superclass; /* hard-coded type and name */

 final transient
  Class[] interfaces; /* hard-coded type and name */ /* const data */

 transient int modifiers; /* hard-coded type and name */

 Class(Object vmdata, String name, Class superclass, Class[] interfaces,
   int modifiers)
 { /* used by VM classes only */
  this.vmdata = vmdata;
  this.name = name;
  this.superclass = superclass;
  this.interfaces = interfaces;
  this.modifiers = modifiers;
 }

 public static Class forName(String name)
  throws ClassNotFoundException /* hard-coded method signature */
 {
  return VMClass.forName(name, true, VMStackWalker.getCallingClassLoader());
 }

 public static Class forName(String name, boolean initialize,
   ClassLoader loader)
  throws ClassNotFoundException /* hard-coded method signature */
 {
  if (loader == null)
  {
   SecurityManager sm = SecurityManager.current;
   if (sm != null && VMStackWalker.getCallingClassLoader() != null)
    sm.checkPermission(new RuntimePermission("getClassLoader"));
  }
  return VMClass.forName(name, initialize, loader);
 }

 public Class[] getClasses()
 {
  memberAccessCheck(Member.PUBLIC);
  ArrayList list = new ArrayList();
  Class klass = this;
  do
  {
   Class[] classes = klass.getDeclaredClasses(true);
   for (int i = 0; i < classes.length; i++)
    list.add(classes[i]);
  } while ((klass = klass.getSuperclass()) != null);
  Class[] classes = new Class[list.size()];
  list.toArray(classes);
  return classes;
 }

 public ClassLoader getClassLoader()
 {
  if (isPrimitive())
   return null;
  ClassLoader loader = VMClass.getClassLoader(this);
  SecurityManager sm = SecurityManager.current;
  if (loader != null && sm != null)
  {
   ClassLoader cl = VMStackWalker.getCallingClassLoader();
   if (cl != null && !cl.isAncestorOf(loader))
    sm.checkPermission(new RuntimePermission("getClassLoader"));
  }
  return loader;
 }

 public Class getComponentType()
 {
  return VMClass.getComponentType(this);
 }

 public Constructor getConstructor(Class[] types)
  throws NoSuchMethodException /* hard-coded method signature */
 {
  memberAccessCheck(Member.PUBLIC);
  return internalGetConstructor(types, true);
 }

 public Constructor[] getConstructors() /* hard-coded method signature */
 {
  memberAccessCheck(Member.PUBLIC);
  return getDeclaredConstructors(true);
 }

 public Constructor getDeclaredConstructor(Class[] types)
  throws NoSuchMethodException /* hard-coded method signature */
 {
  memberAccessCheck(Member.DECLARED);
  return internalGetConstructor(types, false);
 }

 private Constructor internalGetConstructor(Class[] types, boolean publicOnly)
  throws NoSuchMethodException
 {
  Constructor ctor;
  if ((ctor = VMAccessorJavaLangReflect.getDeclaredConstructorVMMethod(this,
      types)) != null && (!publicOnly ||
      Modifier.isPublic(ctor.getModifiers())))
   return ctor;
  /* Constructor[] constructors = getDeclaredConstructors(publicOnly);
  for (int i = 0; i < constructors.length; i++)
  {
   ctor = constructors[i];
   if (matchParameters(types, ctor.getParameterTypes()))
    return ctor;
  } */
  throw new NoSuchMethodException(methodFullName("<init>", types));
 }

 public Class[] getDeclaredClasses()
 {
  memberAccessCheck(Member.DECLARED);
  return getDeclaredClasses(false);
 }

 Class[] getDeclaredClasses(boolean publicOnly)
 {
  return VMClass.getDeclaredClasses(this, publicOnly);
 }

 public Constructor[] getDeclaredConstructors()
 { /* hard-coded method signature */
  memberAccessCheck(Member.DECLARED);
  return getDeclaredConstructors(false);
 }

 Constructor[] getDeclaredConstructors(boolean publicOnly)
 {
  return VMClass.getDeclaredConstructors(this, publicOnly);
 }

 public Field getDeclaredField(String fieldName)
  throws NoSuchFieldException /* hard-coded method signature */
 {
  if (fieldName == null)
   throw new NullPointerException();
  memberAccessCheck(Member.DECLARED);
  Field field = VMAccessorJavaLangReflect.getDeclaredFieldVMField(this,
                 fieldName);
  if (field != null)
   return field;
  /* Field[] fields = getDeclaredFields(false);
  for (int i = 0; i < fields.length; i++)
  {
   Field field = fields[i];
   if (field.getName().equals(fieldName))
    return field;
  } */
  throw new NoSuchFieldException(getName().concat(".").concat(fieldName));
 }

 public Field[] getDeclaredFields() /* hard-coded method signature */
 {
  memberAccessCheck(Member.DECLARED);
  return getDeclaredFields(false);
 }

 Field[] getDeclaredFields(boolean publicOnly)
 {
  return VMClass.getDeclaredFields(this, publicOnly);
 }

 public Method getDeclaredMethod(String methodName, Class[] types)
  throws NoSuchMethodException /* hard-coded method signature */
 {
  if (methodName == null)
   throw new NullPointerException();
  memberAccessCheck(Member.DECLARED);
  Method match;
  match = VMAccessorJavaLangReflect.getDeclaredMethodVMMethod(this,
           methodName, types);
  /* match = matchMethod(getDeclaredMethods(false), methodName, types); */
  if (match == null)
   throw new NoSuchMethodException(methodFullName(methodName, types));
  return match;
 }

 public Method[] getDeclaredMethods() /* hard-coded method signature */
 {
  memberAccessCheck(Member.DECLARED);
  return getDeclaredMethods(false);
 }

 Method[] getDeclaredMethods(boolean publicOnly)
 {
  return VMClass.getDeclaredMethods(this, publicOnly);
 }

 public Class getDeclaringClass()
 {
  return VMClass.getDeclaringClass(this);
 }

 public Field getField(String fieldName)
  throws NoSuchFieldException /* hard-coded method signature */
 {
  if (fieldName == null)
   throw new NullPointerException();
  memberAccessCheck(Member.PUBLIC);
  Field field = internalGetField(fieldName);
  if (field == null)
   throw new NoSuchFieldException(getName().concat(".").concat(fieldName));
  return field;
 }

 private Field internalGetField(String fieldName)
 {
  Class klass = this;
  do
  {
   Field field;
   if ((field = VMAccessorJavaLangReflect.getDeclaredFieldVMField(klass,
       fieldName)) != null && Modifier.isPublic(field.getModifiers()))
    return field;
   /* Field[] fields = klass.getDeclaredFields(true);
   for (int i = 0; i < fields.length; i++)
   {
    field = fields[i];
    if (field.getName().equals(fieldName))
     return field;
   } */
   Class[] ifaces = klass.getInterfaces();
   for (int i = 0; i < ifaces.length; i++)
   {
    field = ifaces[i].internalGetField(fieldName);
    if (field != null)
     return field;
   }
  } while ((klass = klass.getSuperclass()) != null);
  return null;
 }

 public Field[] getFields() /* hard-coded method signature */
 {
  memberAccessCheck(Member.PUBLIC);
  ArrayList list = new ArrayList();
  internalGetFields(list, new HashSet());
  Field[] fields = new Field[list.size()];
  list.toArray(fields);
  return fields;
 }

 private void internalGetFields(ArrayList list, HashSet classSet)
 {
  Class klass = this;
  do
  {
   if (!classSet.add(klass))
    break;
   Field[] fields = klass.getDeclaredFields(true);
   for (int i = 0; i < fields.length; i++)
    list.add(fields[i]);
   Class[] ifaces = klass.getInterfaces();
   for (int i = 0; i < ifaces.length; i++)
    ifaces[i].internalGetFields(list, classSet);
  } while ((klass = klass.getSuperclass()) != null);
 }

 public Package getPackage()
 {
  ClassLoader cl = getClassLoader();
  String pkgName = getPackagePortion(getName());
  return cl != null ? cl.getPackage(pkgName) :
          VMClassLoader.getPackage(pkgName);
 }

 public Class[] getInterfaces() /* hard-coded method signature */
 {
  /* return VMClass.getInterfaces(this); */
  Class[] interfaces = this.interfaces; /* hack */
  Class[] newInterfaces = new Class[interfaces.length]; /* hack */
  VMSystem.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
  return newInterfaces;
 }

 public Method getMethod(String methodName, Class[] types)
  throws NoSuchMethodException /* hard-coded method signature */
 {
  if (methodName == null)
   throw new NullPointerException();
  memberAccessCheck(Member.PUBLIC);
  Method method = internalGetMethod(methodName, types);
  if (method == null)
   throw new NoSuchMethodException(methodFullName(methodName, types));
  return method;
 }

 private Method internalGetMethod(String methodName, Class[] types)
 {
  Method match;
  if ((match = VMAccessorJavaLangReflect.getDeclaredMethodVMMethod(this,
      methodName, types)) != null && Modifier.isPublic(match.getModifiers()))
   return match;
  /* if ((match = matchMethod(getDeclaredMethods(true), methodName,
      types)) != null)
   return match; */
  Class superClass = getSuperclass();
  if (superClass == null ||
      (match = superClass.internalGetMethod(methodName, types)) == null)
  {
   Class[] ifaces = getInterfaces();
   for (int i = 0; i < ifaces.length; i++)
    if ((match = ifaces[i].internalGetMethod(methodName, types)) != null)
     break;
  }
  return match;
 }

 private static Method matchMethod(Method[] list, String methodName,
   Class[] types)
 {
  Method match = null;
  for (int i = 0; i < list.length; i++)
  {
   Method method = list[i];
   if (method.getName().equals(methodName) &&
       matchParameters(types, method.getParameterTypes()) && (match == null ||
       match.getReturnType().isAssignableFrom(method.getReturnType())))
    match = method;
  }
  return match;
 }

 private static boolean matchParameters(Class[] types,
   Class[] methodParameterTypes)
 {
  if (types == null)
   return methodParameterTypes.length == 0;
  int len = types.length;
  if (methodParameterTypes.length != len)
   return false;
  for (int i = 0; i < len; i++)
   if (types[i] != methodParameterTypes[i])
    return false;
  return true;
 }

 private String methodFullName(String methodName, Class[] types)
 {
  StringBuilder sb = new StringBuilder();
  sb.append(getName());
  sb.append('.');
  sb.append(methodName);
  sb.append('(');
  if (types != null)
  {
   int len = types.length;
   for (int i = 0; i < len; i++)
   {
    if (i > 0)
     sb.append(',');
    Class aclass = types[i];
    if (aclass != null)
    {
     int dims;
     for (dims = 0; aclass.isArray(); dims++)
      aclass = aclass.getComponentType();
     sb.append(aclass.getName());
     while (dims-- > 0)
      sb.append("[]");
    }
     else sb.append("null");
   }
  }
  sb.append(')');
  return sb.toString();
 }

 public Method[] getMethods() /* hard-coded method signature */
 {
  memberAccessCheck(Member.PUBLIC);
  HashMap map = new HashMap();
  internalGetMethods(map);
  Method[] methods = new Method[map.size()];
  Iterator itr = map.values().iterator();
  for (int i = 0; i < methods.length; i++)
   methods[i] = (Method) itr.next();
  return methods;
 }

 private void internalGetMethods(HashMap map)
 {
  Class[] ifaces = getInterfaces();
  for (int i = 0; i < ifaces.length; i++)
   ifaces[i].internalGetMethods(map);
  Class superClass = getSuperclass();
  if (superClass != null)
   superClass.internalGetMethods(map);
  Method[] methods = getDeclaredMethods(true);
  for (int i = 0; i < methods.length; i++)
   map.put(new MethodKey(methods[i]), methods[i]);
 }

 public int getModifiers()
 {
  return VMClass.getModifiers(this, false) & (Modifier.PUBLIC |
          Modifier.PROTECTED | Modifier.PRIVATE | Modifier.FINAL |
          Modifier.STATIC | Modifier.ABSTRACT | Modifier.INTERFACE |
          ANNOTATION | ENUM);
 }

 public String getName() /* hard-coded method signature */
 {
  /* return VMClass.getName(this); */
  String name;
  if ((name = this.name) == null) /* hack */
   name = "<UnknownClass>";
  return name;
 }

 public URL getResource(String resourceName)
 {
  String path = resourcePath(resourceName);
  ClassLoader cl = getClassLoader();
  return cl != null ? cl.getResource(path) :
          ClassLoader.getSystemResource(path);
 }

 public InputStream getResourceAsStream(String resourceName)
 {
  String path = resourcePath(resourceName);
  ClassLoader cl = getClassLoader();
  return cl != null ? cl.getResourceAsStream(path) :
          ClassLoader.getSystemResourceAsStream(path);
 }

 private String resourcePath(String resourceName)
 {
  if (resourceName.length() > 0)
  {
   if (resourceName.charAt(0) != '/')
   {
    Class klass = this;
    while (klass.isArray())
     klass = klass.getComponentType();
    String pkg = getPackagePortion(klass.getName());
    if (pkg.length() > 0)
     resourceName = pkg.replace('.', '/').concat("/").concat(resourceName);
   }
    else resourceName = resourceName.substring(1);
  }
  return resourceName;
 }

 public Object[] getSigners()
 {
  Object[] signers = VMClass.getClassSignersOf(this);
  if (signers != null)
  {
   Object[] signersCopy = new Object[signers.length];
   VMSystem.arraycopy(signers, 0, signersCopy, 0, signers.length);
   signers = signersCopy;
  }
  return signers;
 }

 void setSigners(Object[] signers)
 {
  VMClass.setClassSignersOf(this, signers);
 }

 public Class getSuperclass()
 {
  return VMClass.getSuperclass(this);
 }

 public boolean isArray()
 {
  return VMClass.isArray(this);
 }

 public boolean isAssignableFrom(Class aclass)
 {
  return VMClass.isAssignableFrom(this, aclass);
 }

 public boolean isInstance(Object obj)
 {
  return VMClass.isInstance(this, obj);
 }

 public boolean isInterface()
 {
  /* return VMClass.isInterface(this); */
  return (modifiers & MODIFIER_INTERFACE) != 0; /* hack */
 }

 public boolean isPrimitive()
 {
  /* return VMClass.isPrimitive(this); */
  return modifiers == (MODIFIER_PUBLIC | MODIFIER_FINAL | /* hack */
          MODIFIER_ABSTRACT) && superclass == null; /* hack */
 }

 public Object newInstance() /* hard-coded method signature */
  throws InstantiationException, IllegalAccessException
 {
  memberAccessCheck(Member.PUBLIC);
  Constructor ctor = VMClass.getBasicConstructorOf(this);
  if (ctor == null)
  {
   ctor = VMAccessorJavaLangReflect.getDeclaredConstructorVMMethod(this,
           null);
   /* Constructor[] constructors = getDeclaredConstructors(false);
   for (int i = 0; i < constructors.length; i++)
    if (constructors[i].getParameterTypes().length == 0)
    {
     ctor = constructors[i];
     break;
    } */
   if (ctor == null)
    throw new InstantiationException(getName());
   if (!Modifier.isPublic(ctor.getModifiers()) ||
       !Modifier.isPublic(VMClass.getModifiers(this, true)))
    setAccessible(ctor);
   VMClass.setBasicConstructorOf(this, ctor);
  }
  int mods = ctor.getModifiers();
  if (!Modifier.isPublic(mods) ||
      !Modifier.isPublic(VMClass.getModifiers(this, true)))
  {
   Class caller = VMStackWalker.getCallingClass();
   if (caller != null && caller != this && (Modifier.isPrivate(mods) ||
       getClassLoader() != caller.getClassLoader() ||
       !getPackagePortion(getName()).equals(
       getPackagePortion(caller.getName()))))
    throw new IllegalAccessException(getName().concat(
           " has an inaccessible constructor"));
  }
  return VMAccessorJavaLangReflect.allocateObjectVMConstructor(ctor,
          this);
  /* try
  {
   return ctor.newInstance(null);
  }
  catch (InvocationTargetException e)
  {
   VMClass.throwException(e.getTargetException());
   throw new InternalError("VMClass.throwException returned");
  } */
 }

 public ProtectionDomain getProtectionDomain()
 {
  SecurityManager sm = SecurityManager.current;
  if (sm != null)
   sm.checkPermission(new RuntimePermission("getProtectionDomain"));
  ProtectionDomain pd = VMClassLoader.getProtectionDomain(this);
  return pd != null ? pd : StaticData.unknownProtectionDomain;
 }

 public String toString()
 {
  String className = getName();
  return isPrimitive() ? className :
          (isInterface() ? "interface " : "class ").concat(className);
 }

 public boolean desiredAssertionStatus()
 {
  ClassLoader cl = getClassLoader();
  Object status;
  if (cl == null)
   return VMClassLoader.defaultAssertionStatus();
  if (cl.classAssertionStatus != null)
  {
   synchronized (cl)
   {
    status = cl.classAssertionStatus.get(getName());
   }
  }
   else status = ClassLoader.StaticData.systemClassAssertionStatus.get(
                  getName());
  if (status != null)
   return status.equals(Boolean.TRUE);
  if (cl.packageAssertionStatus != null)
  {
   synchronized (cl)
   {
    String pkgName = getPackagePortion(getName());
    if (pkgName.length() == 0)
     status = cl.packageAssertionStatus.get(null);
     else
     {
      do
      {
       status = cl.packageAssertionStatus.get(pkgName);
       pkgName = getPackagePortion(pkgName);
      } while (pkgName.length() > 0 && status == null);
     }
   }
  }
   else
   {
    String pkgName = getPackagePortion(getName());
    if (pkgName.length() == 0)
     status = ClassLoader.StaticData.systemPackageAssertionStatus.get(null);
     else
     {
      do
      {
       status =
        ClassLoader.StaticData.systemPackageAssertionStatus.get(pkgName);
       pkgName = getPackagePortion(pkgName);
      } while (pkgName.length() > 0 && status == null);
     }
   }
  return status != null ? status.equals(Boolean.TRUE) :
          cl.defaultAssertionStatus;
 }

 private static String getPackagePortion(String className)
 {
  int lastInd = className.lastIndexOf('.');
  return lastInd > 0 ? className.substring(0, lastInd) : "";
 }

 public Class asSubclass(Class klass)
 {
  if (!klass.isAssignableFrom(this))
   throw new ClassCastException();
  return this;
 }

 public Object cast(Object obj)
 {
  if (obj != null && !isInstance(obj))
   throw new ClassCastException();
  return obj;
 }

 public Object[] getEnumConstants()
 {
  if (!isEnum())
   return null;
  try
  {
   Method m = getMethod("values", null);
   setAccessible(m);
   return (Object[]) m.invoke(null, null);
  }
  catch (NoSuchMethodException e)
  {
   throw new Error("Enum lacks values() method");
  }
  catch (IllegalAccessException e)
  {
   throw new Error("unable to access Enum class");
  }
  catch (InvocationTargetException e)
  {
   throw new RuntimeException("the values method threw an exception", e);
  }
 }

 public boolean isEnum()
 {
  return (VMClass.getModifiers(this, true) & ENUM) != 0;
 }

 public boolean isSynthetic()
 {
  return (VMClass.getModifiers(this, true) & SYNTHETIC) != 0;
 }

 public boolean isAnnotation()
 {
  return (VMClass.getModifiers(this, true) & ANNOTATION) != 0;
 }

 public String getSimpleName()
 {
  return VMClass.getSimpleName(this);
 }

 public Annotation getAnnotation(Class annotationClass)
 {
  Annotation[] annotations = getAnnotations();
  for (int i = 0; i < annotations.length; i++)
   if (annotations[i].annotationType() == annotationClass)
    return annotations[i];
  return null;
 }

 public Annotation[] getAnnotations()
 {
  HashSet set = new HashSet();
  internalGetAnnotations(set);
  Annotation[] annotations = new Annotation[set.size()];
  Iterator itr = set.iterator();
  for (int i = 0; i < annotations.length; i++)
   annotations[i] = (Annotation) itr.next();
  return annotations;
 }

 private void internalGetAnnotations(HashSet set)
 {
  Class klass = this;
  do
  {
   Annotation[] annotations = klass.getDeclaredAnnotations();
   for (int i = 0; i < annotations.length; i++)
    set.add(annotations[i]);
   Class[] ifaces = klass.getInterfaces();
   for (int i = 0; i < ifaces.length; i++)
    ifaces[i].internalGetAnnotations(set);
  } while ((klass = klass.getSuperclass()) != null);
 }

 public String getCanonicalName()
 {
  return VMClass.getCanonicalName(this);
 }

 public Annotation[] getDeclaredAnnotations()
 {
  return VMClass.getDeclaredAnnotations(this);
 }

 public Class getEnclosingClass()
 {
  return VMClass.getEnclosingClass(this);
 }

 public Constructor getEnclosingConstructor()
 {
  return VMClass.getEnclosingConstructor(this);
 }

 public Method getEnclosingMethod()
 {
  return VMClass.getEnclosingMethod(this);
 }

 public Type[] getGenericInterfaces()
 {
  if (isPrimitive())
   return new Type[0];
  String sig = VMClass.getClassSignature(this);
  if (sig == null)
   return getInterfaces();
  return (new ClassSignatureParser(this, sig)).getInterfaceTypes();
 }

 public Type getGenericSuperclass()
 {
  Class superClass = getSuperclass();
  String sig;
  if (superClass == null || isArray() ||
      (sig = VMClass.getClassSignature(this)) == null)
   return superClass;
  return (new ClassSignatureParser(this, sig)).getSuperclassType();
 }

 public TypeVariable[] getTypeParameters()
 {
  String sig = VMClass.getClassSignature(this);
  return sig != null ? (new ClassSignatureParser(this,
          sig)).getTypeParameters() : new TypeVariable[0];
 }

 public boolean isAnnotationPresent(Class annotationClass)
 {
  return getAnnotation(annotationClass) != null;
 }

 public boolean isAnonymousClass()
 {
  return VMClass.isAnonymousClass(this);
 }

 public boolean isLocalClass()
 {
  return VMClass.isLocalClass(this);
 }

 public boolean isMemberClass()
 {
  return VMClass.isMemberClass(this);
 }

 static void setAccessible(final AccessibleObject obj)
 {
  AccessController.doPrivileged(
   new PrivilegedAction()
   {

    public Object run()
    {
     obj.setAccessible(true);
     return null;
    }
   });
 }

 private void memberAccessCheck(int which)
 {
  SecurityManager sm = SecurityManager.current;
  if (sm != null)
  {
   sm.checkMemberAccess(this, which);
   /* Package pkg = getPackage();
   if (pkg != null)
    sm.checkPackageAccess(pkg.getName()); */
  }
 }
}
