/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMClassLoader.java --
 * VM specific methods for Java "ClassLoader" class.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2010 Ivan Maidanski <ivmai@ivmaisoft.com>
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

import gnu.classpath.Configuration;
import gnu.classpath.SystemProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.lang.ref.SoftReference;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import java.security.ProtectionDomain;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class VMClassLoader /* hard-coded class name */
{

 static final class ClassParser /* hard-coded class name */
 { /* used by VM classes only */

  private static final int USHORT_SIZE = 2;

  private static final int INT_SIZE = 4;

  private static final int LONG_SIZE = 8;

  private static final int MIN_MAJOR_VER = 45;

  private static final int MAJOR_VER_OFFSET = INT_SIZE + USHORT_SIZE;

  private static final int CONSTPOOL_COUNT_OFFSET =
   MAJOR_VER_OFFSET + USHORT_SIZE;

  private static final int CONST_POOL_OFFSET =
   CONSTPOOL_COUNT_OFFSET + USHORT_SIZE;

  private static final int BODY_BYTES_MINLEN = USHORT_SIZE * 7;

  private static final int CONST_TAG_CLASS = 7;
  private static final int CONST_TAG_FIELDREF = 9;
  private static final int CONST_TAG_METHODREF = 10;
  private static final int CONST_TAG_INTERFACEMETHODREF = 11;
  private static final int CONST_TAG_STRING = 8;
  private static final int CONST_TAG_INTEGER  = 3;
  private static final int CONST_TAG_FLOAT = 4;
  private static final int CONST_TAG_LONG = 5;
  private static final int CONST_TAG_DOUBLE = 6;
  private static final int CONST_TAG_NAMEANDTYPE = 12;
  private static final int CONST_TAG_UTF8 = 1;

  static
  {
   if (!"".equals("")) /* hack */
    defineClass0X(null, null, new byte[0]); /* hack */
  }

  private ClassParser() {}

  static final Class defineClass0X(String name, Object loader,
    byte[] bytes) /* hard-coded method signature */
   throws ClassCastException
  { /* called from native code */
   return defineClass((ClassLoader) loader, name, bytes, 0, bytes.length,
           null);
  }

  static final String decodeJavaClassName(byte[] bytes, int offset, int len)
  { /* used by VM classes only */
   if (bytes.length - offset < len || (offset | len) < 0)
    throw new ArrayIndexOutOfBoundsException();
   if (len < CONST_POOL_OFFSET || bytes[offset] != (byte) 0xca ||
       bytes[offset + 1] != (byte) 0xfe || bytes[offset + 2] != (byte) 0xba ||
       bytes[offset + 3] != (byte) 0xbe ||
       getUShortAt(bytes, offset + MAJOR_VER_OFFSET) < MIN_MAJOR_VER)
    throw new ClassFormatError();
   int cpEndOfs = getJavaClassConstPoolEntryOfs(getUShortAt(bytes,
                   offset + CONSTPOOL_COUNT_OFFSET), bytes, offset);
   if (offset + len - cpEndOfs < BODY_BYTES_MINLEN)
    throw new ClassFormatError();
   int thisClassEntryOfs = getJavaClassConstPoolEntryOfs(getUShortAt(bytes,
                            cpEndOfs + USHORT_SIZE), bytes, offset);
   if (thisClassEntryOfs >= cpEndOfs ||
       bytes[thisClassEntryOfs] != CONST_TAG_CLASS)
    throw new ClassFormatError();
   int nameEntryOfs = getJavaClassConstPoolEntryOfs(getUShortAt(bytes,
                       thisClassEntryOfs + 1), bytes, offset);
   if (nameEntryOfs >= cpEndOfs || bytes[nameEntryOfs] != CONST_TAG_UTF8)
    throw new ClassFormatError();
   String name = new String(bytes, 0, nameEntryOfs + (1 + USHORT_SIZE),
                  getUShortAt(bytes, nameEntryOfs + 1));
   if (name.length() == 0 || name.indexOf('.') >= 0)
    throw new ClassFormatError();
   return name.replace('/', '.');
  }

  private static int getJavaClassConstPoolEntryOfs(int index, byte[] bytes,
    int offset)
  {
   offset += CONST_POOL_OFFSET;
   int bytesLen = bytes.length;
   while (--index > 0 && bytesLen - offset > USHORT_SIZE)
    switch (bytes[offset])
    {
    case CONST_TAG_UTF8:
     offset += getUShortAt(bytes, offset + 1) + (1 + USHORT_SIZE);
     break;
    case CONST_TAG_CLASS:
    case CONST_TAG_STRING:
     offset += 1 + USHORT_SIZE;
     break;
    case CONST_TAG_INTEGER:
    case CONST_TAG_FLOAT:
    case CONST_TAG_FIELDREF:
    case CONST_TAG_METHODREF:
    case CONST_TAG_INTERFACEMETHODREF:
    case CONST_TAG_NAMEANDTYPE:
     offset += 1 + INT_SIZE;
     break;
    case CONST_TAG_LONG:
    case CONST_TAG_DOUBLE:
     offset += 1 + LONG_SIZE;
     index--;
     break;
    default:
     throw new ClassFormatError();
    }
   if (index != 0 || offset < 0)
    throw new ClassFormatError();
   return offset;
  }

  private static int getUShortAt(byte[] bytes, int offset)
  {
   return ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
  }
 }

 private static final class StaticData
 {

  static final HashMap bootJars = new HashMap();

  static final String JAVA_BOOT_CLASS_PATH = removeNonExistingPaths(
   SystemProperties.getProperty("java.boot.class.path", "."));

  static final HashMap definedPackages = getBootDefinedPackages();

  private static HashMap getBootDefinedPackages()
  {
   HashMap map = new HashMap();
   String[] packages = getBootPackages();
   if (packages != null)
   {
    String specName = SystemProperties.getProperty("java.specification.name");
    String vendor = SystemProperties.getProperty("java.specification.vendor");
    String version =
     SystemProperties.getProperty("java.specification.version");
    for (int i = 0; i < packages.length; i++)
    {
     String name = packages[i];
     map.put(name, new Package(name, specName, vendor, version,
      "GNU Classpath", "GNU", Configuration.CLASSPATH_VERSION, null, null));
    }
   }
   return map;
  }
 }

 private static final class AppClassLoader extends ClassLoader
 {

  AppClassLoader()
  {
   super(null);
  }

  protected URL findResource(String name)
  {
   Vector list = new Vector(1);
   findResourcesInner(list, name, true,
    SystemProperties.getProperty("java.class.path", "."));
   return list.size() > 0 ? (URL) list.elementAt(0) : null;
  }

  protected Enumeration findResources(String name)
   throws IOException
  {
   Vector list = new Vector();
   findResourcesInner(list, name, false,
    SystemProperties.getProperty("java.class.path", "."));
   return list.elements();
  }

  protected Package getPackage(String name)
  {
   Package pkg = super.getPackage(name);
   if (pkg == null)
   {
    pkg = new Package(name, null, null, null, null, null, null, null, this);
    synchronized (this.definedPackages)
    {
     this.definedPackages.put(name, pkg);
    }
   }
   return pkg;
  }

  public String toString()
  {
   return getClass().getName();
  }
 }

 private static final class JarUrlStreamHandler extends URLStreamHandler
 {

  private SimpleDateFormat dateFormat;

  JarUrlStreamHandler() {}

  protected URLConnection openConnection(URL url)
  {
   return
    new URLConnection(url)
    {

     private ZipFile zip;

     private ZipEntry entry;

     public synchronized void connect()
      throws MalformedURLException
     {
      if (!connected)
      {
       URL url = getURL();
       String spec = url.getFile();
       int pos;
       if (!"jar".equals(url.getProtocol()) || (pos = spec.indexOf("!/")) < 0)
        throw new MalformedURLException("not jar: " + url.toString());
       ZipFile zip = openUrlZipFile(spec.substring(0, pos), null);
       if (zip == null || (entry =
           zip.getEntry(unquoteUrl(spec.substring(pos + 2)))) == null)
        throw new MalformedURLException("not found: " + url.toString());
       this.zip = zip;
       connected = true;
      }
     }

     public InputStream getInputStream()
      throws IOException
     {
      if (!connected)
       connect();
      return zip.getInputStream(entry);
     }

     public String getHeaderField(String name)
     {
      if (!connected)
      {
       try
       {
        connect();
       }
       catch (IOException e)
       {
        return null;
       }
      }
      if (name.equals("content-type"))
       return guessContentTypeFromName(entry.getName());
      if (name.equals("content-length"))
       return Long.toString(entry.getSize());
      if (name.equals("last-modified"))
       synchronized (JarUrlStreamHandler.this)
       {
        if (dateFormat == null)
         dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss 'GMT'",
                       Locale.US);
        return dateFormat.format(new Date(entry.getTime()));
       }
      return null;
     }
    };
  }
 }

 private static volatile URLStreamHandler jarUrlStreamHandler;

 private static final VMClass.IdentityHashMap defnClassToLoader =
  new VMClass.IdentityHashMap();

 private static final VMClass.IdentityHashMap definedClassToPDomain =
  new VMClass.IdentityHashMap();

 static
 {
  compareClassNames0X("", boolean.class); /* hack */
 }

 private VMClassLoader() {}

 static final Class defineClass(ClassLoader cl, String name, byte[] data,
   int offset, int len, ProtectionDomain pd)
 {
  /* implemented for the pre-compiled classes only */
  String className = ClassParser.decodeJavaClassName(data, offset, len);
  if (name != null && !name.equals(className))
   throw new NoClassDefFoundError("invalid class name: " + name);
  if (className.startsWith("java."))
   throw new SecurityException("prohibited package name: " +
          className.substring(0, className.lastIndexOf('.')));
  Class aclass = loadClass0(className, 0);
  if (aclass != null)
  {
   if (cl != null)
    synchronized (defnClassToLoader)
    {
     Object old = defnClassToLoader.put(aclass, cl);
     if (old != null && old != cl)
      defnClassToLoader.put(aclass, old);
    }
   if (pd != null)
    synchronized (definedClassToPDomain)
    {
     Object old = definedClassToPDomain.put(aclass, pd);
     if (old != null && old != pd)
      definedClassToPDomain.put(aclass, old);
    }
   return aclass;
  }
  throw new InternalError(
         "VMClassLoader.defineClass() not implemented, class " + className);
 }

 static final Class defineClassWithTransformers(ClassLoader cl, String name,
   byte[] data, int offset, int len, ProtectionDomain pd)
 {
  /* not implemented */
  return defineClass(cl, name, data, offset, len, pd);
 }

 static final void resolveClass(Class aclass)
 {
  /* dummy */
 }

 static final Class loadClass(String name, boolean resolve)
  throws ClassNotFoundException
 {
  if (name.length() == 0)
   throw new ClassNotFoundException(name);
  Class aclass = loadClass0(name, 0);
  if (resolve && aclass != null)
   resolveClass(aclass);
  return aclass;
 }

 static URL getResource(String name)
 {
  Vector list = new Vector(1);
  findResourcesInner(list, name, true, StaticData.JAVA_BOOT_CLASS_PATH);
  return list.size() > 0 ? (URL) list.elementAt(0) : null;
 }

 static Enumeration getResources(String name)
 {
  Vector list = new Vector();
  findResourcesInner(list, name, false, StaticData.JAVA_BOOT_CLASS_PATH);
  return list.elements();
 }

 static Package getPackage(String name)
 {
  return (Package) StaticData.definedPackages.get(name);
 }

 static Package[] getPackages()
 {
  int size = StaticData.definedPackages.size();
  Package[] packages = new Package[size];
  Iterator itr = StaticData.definedPackages.values().iterator();
  for (int i = 0; i < size; i++)
   packages[i] = (Package) itr.next();
  return packages;
 }

 static String[] getBootPackages()
 {
  return new String[0];
 }

 static final Class getPrimitiveClass(char ch)
 {
  int type = "VZBCSIJFD".indexOf(ch, 0);
  return type >= 0 ? getPrimitiveClass0(type) : null;
 }

 static final boolean defaultAssertionStatus()
 {
  /* not implemented */
  return true;
 }

 static final Map packageAssertionStatus()
 {
  /* not implemented */
  return new HashMap();
 }

 static final Map classAssertionStatus()
 {
  /* not implemented */
  return new HashMap();
 }

 static ClassLoader getSystemClassLoader()
 {
  return "gnu.java.net.content.text.plain".equals( /* hack */
          "gnu.java.net.protocol.file.Handler") ? null : /* hack */
          new AppClassLoader();
 }

 static Class findLoadedClass(ClassLoader cl, String name)
 {
  Class aclass = loadClass0(name, 0);
  if (aclass != null && getLoaderOfDefinedClass(aclass) == cl)
   return aclass;
  return null;
 }

 static final ClassLoader getLoaderOfDefinedClass(Class klass)
 { /* used by VM classes only */
  synchronized (defnClassToLoader)
  {
   return (ClassLoader) defnClassToLoader.get(klass);
  }
 }

 static final ProtectionDomain getProtectionDomain(Class klass)
 { /* used by VM classes only */
  Class aclass;
  while ((aclass = klass.getComponentType()) != null)
   klass = aclass;
  synchronized (definedClassToPDomain)
  {
   return (ProtectionDomain) definedClassToPDomain.get(klass);
  }
 }

 static final Class getNextInnerClass(String name)
 { /* used by VM classes only */
  return loadClass0(name, 1);
 }

 private static int unescapeUrlChar(String spec, int pos)
  throws MalformedURLException
 {
  if (pos + 2 >= spec.length() || spec.charAt(pos) != '%')
   throw new MalformedURLException(spec);
  int high = Character.digit(spec.charAt(pos + 1), 16);
  int low = Character.digit(spec.charAt(pos + 2), 16);
  if ((high | low) < 0)
   throw new MalformedURLException(spec);
  return (high << 4) | low;
 }

 static String unquoteUrl(String spec)
  throws MalformedURLException
 { /* used by VM classes only */
  int pos = spec.indexOf('%');
  if (pos < 0)
   return spec;
  StringBuilder sb = new StringBuilder(spec.length());
  int prev = 0;
  do
  {
   sb.append(spec.substring(prev, pos));
   int c = unescapeUrlChar(spec, pos);
   if ((c & 0x80) != 0)
   {
    pos += 3;
    int c2 = unescapeUrlChar(spec, pos);
    if ((c2 & 0xc0) != 0x80)
     throw new MalformedURLException(spec);
    if ((c & 0xe0) == 0xc0)
     c = ((c & 0x1f) << 6) | (c2 & 0x3f);
     else
     {
      pos += 3;
      int c3 = unescapeUrlChar(spec, pos);
      if ((c3 & 0xc0) != 0x80 || (c & 0xf0) != 0xe0)
       throw new MalformedURLException(spec);
      c = ((c & 0xf) << 12) | ((c2 & 0x3f) << 6) | (c3 & 0x3f);
     }
   }
   sb.append((char) c);
   prev = pos + 3;
  } while ((pos = spec.indexOf('%', prev)) >= 0);
  return sb.append(spec.substring(prev)).toString();
 }

 static final ZipFile openUrlZipFile(String baseSpec, File file)
 { /* used by VM classes only */
  SoftReference ref;
  synchronized (StaticData.bootJars)
  {
   ref = (SoftReference) StaticData.bootJars.get(baseSpec);
  }
  ZipFile zip;
  if (ref == null || (zip = (ZipFile) ref.get()) == null)
  {
   if (file == null)
   {
    if (baseSpec.length() <= 6 || !baseSpec.startsWith("file:"))
     return null;
    String path;
    try
    {
     path = unquoteUrl(baseSpec.substring(5));
    }
    catch (MalformedURLException e)
    {
     return null;
    }
    if (path.charAt(0) != '/' ||
        !(file = new File(path.substring(1))).isAbsolute())
     file = new File(path);
   }
   zip = null;
   try
   {
    zip = new ZipFile(file);
   }
   catch (IOException e) {}
   catch (SecurityException e) {}
   if (zip != null)
   {
    ref = new SoftReference(zip);
    synchronized (StaticData.bootJars)
    {
     StaticData.bootJars.put(baseSpec, ref);
    }
   }
  }
  return zip;
 }

 static final String removeNonExistingPaths(String pathList)
 { /* used by VM classes only */
  StringBuilder sb = new StringBuilder(pathList.length());
  int pos = 0;
  int nextPos;
  do
  {
   nextPos = pathList.indexOf(File.pathSeparatorChar, pos);
   if (nextPos < 0)
    nextPos = pathList.length();
   File file = new File(pos < nextPos ?
                pathList.substring(pos, nextPos) : ".");
   try
   {
    if (!file.exists())
     file = null;
   }
   catch (SecurityException e) {}
   if (file != null)
   {
    if (sb.length() > 0)
     sb.append(File.pathSeparatorChar);
    sb.append(file.getPath());
   }
   pos = nextPos + 1;
  } while (pathList.length() > nextPos);
  return sb.toString();
 }

 static final void findResourcesInner(Vector list, String name,
  boolean findSingle, String pathList)
 { /* used by VM classes only */
  int pos;
  int nextPos = 0;
  do
  {
   if (name.length() <= nextPos)
    return;
   pos = nextPos;
   char ch;
   while ((ch = name.charAt(nextPos++)) == '.')
    if (name.length() <= nextPos)
     return;
   if (ch != '/' && ch != File.separatorChar)
    break;
  } while (true);
  if (pos > 0)
  {
   name = name.substring(pos);
   pos = 0;
  }
  do
  {
   nextPos = pathList.indexOf(File.pathSeparatorChar, pos);
   if (nextPos < 0)
    nextPos = pathList.length();
   File file = new File(pos < nextPos ?
                pathList.substring(pos, nextPos) : ".");
   try
   {
    String baseSpec = file.toURL().toString();
    if (baseSpec.endsWith("/"))
    {
     File f = new File(file, name);
     if (f.exists())
     {
      list.addElement(f.toURL());
      if (findSingle)
       break;
     }
    }
     else
     {
      ZipFile zip = openUrlZipFile(baseSpec, file);
      if (zip != null && zip.getEntry(name) != null)
      {
       String spec = baseSpec + "!/" + name;
       try
       {
        list.addElement(new URL("jar", null, spec));
       }
       catch (MalformedURLException x)
       {
        URLStreamHandler handler = jarUrlStreamHandler;
        if (handler == null)
         jarUrlStreamHandler = handler = new JarUrlStreamHandler();
        list.addElement(new URL("jar", null, -1, spec, handler));
       }
       if (findSingle)
        break;
      }
     }
   }
   catch (SecurityException e) {}
   catch (MalformedURLException e)
   {
    try
    {
     if (file.exists())
      throw (Error) (new InternalError("findResources")).initCause(e);
    }
    catch (SecurityException x) {}
   }
   pos = nextPos + 1;
  } while (pathList.length() > nextPos);
 }

 static final int compareClassNames0X(String name, Class aclass)
 { /* called from native code */
  return name.compareTo(aclass.getName());
 }

 private static native Class getPrimitiveClass0(int type); /* JVM-core */

 private static native Class loadClass0(String name,
   int nextInner); /* JVM-core */
}
