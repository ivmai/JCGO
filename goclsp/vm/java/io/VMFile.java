/*
 * @(#) $(JCGO)/goclsp/vm/java/io/VMFile.java --
 * VM specific native file access API implementation.
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

package java.io;

import gnu.java.nio.VMAccessorGnuJavaNio;
import gnu.java.nio.VMChannel;

import java.net.MalformedURLException;
import java.net.URL;

final class VMFile /* hard-coded class name */
{

 static final boolean IS_CASE_SENSITIVE = isCaseSensitive0() != 0;

 static final boolean IS_DOS_8_3 = isLongNameSupported0() == 0;

 private static final boolean IS_UNIX_FS = isUniRootFileSys0() != 0;

 private static final boolean IS_DOTFILE_HIDDEN = isDotFileHidden0() != 0;

 private static final char FILE_SEP = (char) getFilePathSepChar0(0);

 private static final char PATH_SEP = (char) getFilePathSepChar0(1);

 private static final int DIR_DATASIZE_ISFIND = dirDataSizeAndIsFind0();

 private static final Object DIR_OP_LOCK =
  dirOpNeedsSync0() != 0 ? new Object() : null;

 private static final String[] driveDirCache =
  new String[IS_UNIX_FS ? 1 : 'Z' - 'A' + 2];

 private static final String DOS_UNIX_DRIVE_PREFIX =
  getDosUnixDrivePrefix0().replace('/', FILE_SEP);

 private static final String LIBNAME_PREFIX = getLibNamePrefixSuffix0(0);

 private static final String LIBNAME_SUFFIX = getLibNamePrefixSuffix0(1);

 private static int defaultDrive;

 private VMFile() {}

 static long lastModified(String path)
 {
  return getLastModified0(normVolumeColon(path));
 }

 static boolean setReadOnly(String path)
 {
  return setReadOnly0(normVolumeColon(path)) >= 0;
 }

 static boolean create(String path)
  throws IOException
 {
  VMChannel ch =
   VMAccessorGnuJavaNio.createUnlessExistsVMChannel(new File(path));
  if (ch == null)
   return false;
  try
  {
   ch.close();
  }
  catch (IOException e) {}
  return true;
 }

 static String[] list(String path)
 {
  int dirDSize = DIR_DATASIZE_ISFIND;
  if (dirDSize < 0)
   dirDSize = -dirDSize;
  byte[] dirdata = new byte[dirDSize];
  path = normVolumeColon(path);
  String[] names;
  if (DIR_OP_LOCK != null)
  {
   synchronized (DIR_OP_LOCK)
   {
    names = listInner(path, dirdata);
   }
  }
   else names = listInner(path, dirdata);
  return names;
 }

 static boolean renameTo(String srcpath, String destpath)
 {
  return rename0(srcpath, destpath) >= 0;
 }

 static native long length(String path);

 static boolean exists(String path)
 {
  return path.equals(".") || isRegFileOrDir0(normVolumeColon(path)) >= 0;
 }

 static boolean delete(String path)
 {
  return delete0(path) >= 0;
 }

 static boolean setLastModified(String path, long time)
 {
  return setLastModified0(time, normVolumeColon(path)) >= 0;
 }

 static boolean mkdir(String path)
 {
  return mkdir0(path) >= 0;
 }

 static long getTotalSpace(String path)
 {
  /* not implemented */
  return 0L;
 }

 static long getFreeSpace(String path)
 {
  /* not implemented */
  return 0L;
 }

 static long getUsableSpace(String path)
 {
  /* not implemented */
  return 0L;
 }

 static boolean setReadable(String path, boolean readable,
   boolean ownerOnly)
 {
  /* not implemented */
  return false;
 }

 static boolean setWritable(String path, boolean writable,
   boolean ownerOnly)
 {
  /* not implemented */
  return false;
 }

 static boolean setExecutable(String path, boolean executable,
   boolean ownerOnly)
 {
  /* not implemented */
  return false;
 }

 static boolean isFile(String path)
 {
  return isRegFileOrDir0(path) == 1;
 }

 static boolean canWrite(String path)
 {
  return access0(path, 0, 1, 0) >= 0;
 }

 static boolean canWriteDirectory(String path)
 {
  return canWrite(normVolumeColon(path));
 }

 static boolean canWriteDirectory(File dir)
 {
  return canWriteDirectory(dir.getPath());
  /* File testFile = null;
  try
  {
   testFile = File.createTempFile(IS_DOS_8_3 ? "tst" : "test-dir-write",
               null, dir);
  }
  catch (IOException e) {}
  if (testFile != null)
  {
   String path = testFile.getPath();
   if (delete(path))
    return true;
   System.err.println("Cannot remove temporary file: " + path);
  }
  return false; */
 }

 static boolean canRead(String path)
 {
  return access0(normVolumeColon(path), 1, 0, 0) >= 0;
 }

 static boolean canExecute(String path)
 {
  return access0(normVolumeColon(path), 0, 0, 1) >= 0;
 }

 static boolean isDirectory(String path)
 {
  return path.equals(".") || isRegFileOrDir0(normVolumeColon(path)) == 2;
 }

 static File[] listRoots()
 {
  File[] roots;
  if (IS_UNIX_FS)
   roots = new File[] { new File(String.valueOf(FILE_SEP)) };
   else
   {
    int mask = getDrivesMask0();
    roots = new File['Z' - 'A' + 1];
    int count = 0;
    if (mask != -1)
    {
     int drive = 0;
     while (mask != 0)
     {
      drive++;
      if ((mask & 1) != 0)
       roots[count++] = new File(drivePathPrefix(drive) +
                         String.valueOf(FILE_SEP));
      mask >>>= 1;
     }
    }
     else
     {
      String path = getVolumeRoot0(0);
      if (path != null && path.length() > 0)
      {
       int index = 0;
       do
       {
        if (!path.equals("."))
        {
         if (roots.length <= count)
         {
          File[] newRoots = new File[(count >> 1) + count + 1];
          System.arraycopy(roots, 0, newRoots, 0, count);
          roots = newRoots;
         }
         roots[count++] = new File(path.charAt(path.length() - 1) == ':' ?
                           path + String.valueOf(FILE_SEP) : path);
        }
        path = getVolumeRoot0(++index);
       } while (path != null && path.length() > 0);
      }
       else
       {
        int drive = 0;
        do
        {
         String driveColon = drivePathPrefix(++drive);
         int res = checkVolumeRoot0(driveColon + ".");
         if (res > 0 || (res != 0 && getDriveCurDir(drive, false) != null))
          roots[count++] = new File(driveColon + String.valueOf(FILE_SEP));
        } while (drive <= 'Z' - 'A');
       }
     }
    if (roots.length > count)
    {
     File[] newRoots = new File[count];
     System.arraycopy(roots, 0, newRoots, 0, count);
     roots = newRoots;
    }
   }
  return roots;
 }

 static boolean isHidden(String path)
 {
  if (IS_DOTFILE_HIDDEN)
  {
   String name = getName(path);
   int len = name.length();
   if (len > 0)
   {
    if (name.charAt(0) != '.')
     return false;
    if (len > 2 || (len == 2 && name.charAt(1) != '.'))
     return true;
   }
   if ((path = makeAbsPath(path)) == null)
    return false;
   name = getName(collapsePathDots(path));
   return (len = name.length()) > 1 && name.charAt(0) == '.' &&
           (len > 2 || name.charAt(1) != '.');
  }
  path = normVolumeColon(path);
  int res = isHidden0(path);
  int dirDSize;
  if (res < 0 && (dirDSize = DIR_DATASIZE_ISFIND) > 0)
  {
   byte[] dirdata = new byte[dirDSize];
   String filename = realPath0(path, new int[1]);
   if (filename != path && filename != null)
    path = normPlatformPath(filename);
   if (DIR_OP_LOCK != null)
   {
    synchronized (DIR_OP_LOCK)
    {
     res = isHiddenInner(path, dirdata);
    }
   }
    else res = isHiddenInner(path, dirdata);
  }
  return res > 0;
 }

 static String getName(String path)
 {
  int prefixLen = pathPrefixLength(path);
  int pos = path.lastIndexOf(FILE_SEP);
  return path.substring(pos >= prefixLen ? pos + 1 : prefixLen);
 }

 static String getAbsolutePath(String path)
 {
  String absPath = makeAbsPath(path);
  return absPath != null ? absPath : path;
 }

 static boolean isAbsolute(String path)
 {
  int prefixLen = pathPrefixLength(path);
  return prefixLen > 0 && (IS_UNIX_FS ||
          (prefixLen > 2 && path.charAt(prefixLen - 1) != ':'));
 }

 static URL toURL(File file)
  throws MalformedURLException
 {
  String path = file.getPath();
  String normPath = makeAbsPath(path);
  normPath = (normPath != null ? normPath : path).replace(FILE_SEP, '/');
  int len = normPath.length();
  if (len > 0)
  {
   if (normPath.charAt(len - 1) != '/' && isDirectory(path))
    normPath = normPath + "/";
   if (normPath.charAt(0) == '/')
   {
    if (len > 1 && normPath.charAt(1) == '/')
     normPath = ("/" + "/") + normPath;
   }
    else normPath = "/" + normPath;
  }
  return new URL("file:" + normPath);
 }

 static String toCanonicalForm(String path)
  throws IOException
 {
  int drive;
  if (!IS_UNIX_FS && path.length() == 2 && path.charAt(1) == ':' &&
      (drive = driveIndexOf(path.charAt(0))) > 0)
   return getDriveCurDir(drive, true);
  path = makeAbsPath(realPath((new File(path)).getPath()));
  if (path == null)
   throw new IOException();
  return canonPathCase(collapsePathDots(path));
 }

 static final long currentTime(boolean isNano)
 { /* used by VM classes only */
  return currentTime0(isNano ? 1 : 0);
 }

 static final String getFileSeparator()
 { /* used by VM classes only */
  return String.valueOf(FILE_SEP);
 }

 static final String getPathSeparator()
 { /* used by VM classes only */
  return String.valueOf(PATH_SEP);
 }

 static final String pathParentOf(String path)
 { /* used by VM classes only */
  int prefixLen = pathPrefixLength(path);
  if (path.length() == prefixLen)
   return null;
  int pos = path.lastIndexOf(FILE_SEP);
  if (pos <= prefixLen)
   pos = prefixLen;
  return pos > 0 ? path.substring(0, pos) : null;
 }

 static final String makeAbsPath(String path)
 { /* used by VM classes only */
  int prefixLen = pathPrefixLength(path);
  String curDir;
  if (prefixLen > 0)
  {
   if (IS_UNIX_FS)
    return path;
   if (prefixLen > 2)
    return path.charAt(prefixLen - 1) == ':' ? path.substring(0, prefixLen) +
            String.valueOf(FILE_SEP) + path.substring(prefixLen) : path;
   if (prefixLen == 1)
    return (curDir = getCurDir()) != null &&
            (prefixLen = pathPrefixLength(curDir)) > 0 ?
            pathAppendName(curDir.substring(0, prefixLen > 3 ?
            prefixLen - 1 : prefixLen), path.substring(1)) : null;
   curDir = getDriveCurDir(driveIndexOf(path.charAt(0)), true);
   path = path.substring(2);
  }
   else
   {
    curDir = getCurDir();
    if (path.equals("."))
     return curDir;
   }
  return curDir != null ? pathAppendName(curDir, path.length() > 1 &&
          path.charAt(0) == '.' && path.charAt(1) == FILE_SEP ?
          path.substring(2) : path) : null;
 }

 static final String collapsePathDots(String path)
 { /* used by VM classes only */
  int ofs = pathPrefixLength(path);
  int skip = 0;
  for (int pos = path.length(); pos > ofs; pos--)
  {
   int next = pos;
   pos = path.lastIndexOf(FILE_SEP, next - 1) + 1;
   if (pos <= ofs)
    pos = ofs;
   if (next - 1 == pos && path.charAt(pos) == '.')
   {
    if (path.length() == next)
    {
     if (pos <= ofs)
     {
      if (pos > 0)
       path = path.substring(0, pos);
      break;
     }
     next--;
    }
    path = path.substring(0, next - 1) + path.substring(next + 1);
   }
    else
    {
     if (next - 1 > pos && path.charAt(pos) == '.' &&
         path.charAt(pos + 1) == '.')
     {
      int i = pos + 2;
      if (!IS_UNIX_FS)
       while (i < next && path.charAt(i) == '.')
        i++;
      if (i == next)
       skip += next - pos;
     }
     if (skip > 0)
     {
      int i = pos;
      skip--;
      if (path.length() == next)
      {
       next--;
       if (pos <= ofs)
       {
        if (pos <= 0)
        {
         path = ".";
         i = 1;
        }
       }
        else i--;
      }
      path = path.substring(0, i) + path.substring(next + 1);
     }
    }
  }
  return path;
 }

 static final String canonPathCase(String path)
  throws IOException
 { /* used by VM classes only */
  if (!IS_CASE_SENSITIVE)
  {
   int prefixLen = pathPrefixLength(path);
   String name = path.substring(0, prefixLen);
   String canonName = VMAccessorJavaLang.toUpperCaseLatinVMSystem(name);
   if (canonName != name)
    path = canonName + path.substring(prefixLen);
   int pos = prefixLen;
   char sep = FILE_SEP;
   int dirDSize = DIR_DATASIZE_ISFIND;
   byte[] dirdata = new byte[dirDSize >= 0 ? dirDSize : -dirDSize];
   Object lockObj = DIR_OP_LOCK;
   while (path.length() > pos)
   {
    int next = path.indexOf(sep, pos);
    if (next < 0)
     next = path.length();
    int i = pos;
    while (i < next && path.charAt(i) == '.')
     i++;
    if (i < next)
    {
     String curPath = path.substring(0, next);
     name = path.substring(pos, next);
     if (dirDSize > 0)
     {
      if (pos > prefixLen)
      {
       canonName = path.substring(0, pos - 1);
       int[] resArr = new int[1];
       String filename = realPath0(canonName, resArr);
       VMAccessorGnuJavaNio.checkIOResCodeVMChannel(resArr[0]);
       if (filename != canonName && filename != null)
        curPath = pathAppendName(normPlatformPath(filename), name);
      }
      if (lockObj != null)
      {
       synchronized (lockObj)
       {
        canonName = canonPathCaseMatch(curPath, dirdata);
       }
      }
       else canonName = canonPathCaseMatch(curPath, dirdata);
      if (canonName == null || name.indexOf('*', 0) >= 0 ||
          name.indexOf('?', 0) >= 0)
       break;
     }
      else
      {
       long[] devInodeArr = new long[2];
       devInodeArr[0] = -1L;
       int res = getLnkDevInode0(curPath, devInodeArr);
       if (res < 0 && isIOErrorNoEntity0(res) != 0)
        break;
       VMAccessorGnuJavaNio.checkIOResCodeVMChannel(res);
       if (devInodeArr[0] == -1L || devInodeArr[1] == 0L)
        break;
       curPath = path.substring(0, pos > prefixLen ? pos - 1 : pos);
       if (lockObj != null)
       {
        synchronized (lockObj)
        {
         canonName = canonPathCaseFind(curPath, dirdata, name);
        }
       }
        else canonName = canonPathCaseFind(curPath, dirdata, name);
       if (canonName == null)
        break;
       if (canonName.length() == 0)
       {
        if (lockObj != null)
        {
         synchronized (lockObj)
         {
          canonName = canonPathCaseScan(curPath, dirdata, devInodeArr);
         }
        }
         else canonName = canonPathCaseScan(curPath, dirdata, devInodeArr);
        if (canonName == null || canonName.length() == 0)
         break;
       }
      }
     if (!canonName.equals(name))
     {
      path = path.substring(0, pos) + canonName + path.substring(next);
      next = canonName.length() + pos;
     }
    }
    pos = next + 1;
   }
  }
  return path;
 }

 static final String normPlatformPath(String path)
 { /* used by VM classes only */
  int len = path.length();
  if (len == 0)
   return ".";
  String normPath = normPlatformPath0(path);
  if (normPath != null && normPath.length() > 0)
  {
   char ch;
   if (!IS_UNIX_FS && (ch = normPath.charAt(0)) >= 'a' && ch <= 'z' &&
       normPath.length() > 1 && normPath.charAt(1) == ':')
    normPath = String.valueOf((char) (ch - ('a' - 'A'))) +
                normPath.substring(1);
   return normPath;
  }
  char sep = FILE_SEP;
  if (IS_UNIX_FS)
  {
   path = path.replace('\\', sep);
   if (len > 1)
   {
    int drive;
    if (path.charAt(1) == ':' && (drive = driveIndexOf(path.charAt(0))) > 0)
    {
     path = drive == defaultDrive ? (len > 2 ? path.substring(2) : ".") :
             DOS_UNIX_DRIVE_PREFIX + String.valueOf((char) (drive +
             ('a' - 1))) + (len != 2 && path.charAt(2) != sep ?
             String.valueOf(sep) : "") + path.substring(2);
     len = path.length();
    }
    if (path.charAt(len - 1) == sep && len > 1)
     path = path.substring(0, len - 1);
   }
  }
   else
   {
    if (len > 2 && path.charAt(0) == '/')
    {
     if (path.charAt(2) == ':' && driveIndexOf(path.charAt(1)) > 0)
     {
      path = path.substring(1);
      if (len == 3)
       path += String.valueOf(sep);
      len = path.length();
     }
      else
      {
       String prefix = DOS_UNIX_DRIVE_PREFIX;
       int pos = prefix.length() + 1;
       char ch;
       if (pos <= len && path.startsWith(prefix) &&
           (ch = path.charAt(pos - 1)) >= 'a' && ch <= 'z' &&
           (len == pos || path.charAt(pos) == '/'))
       {
        path = path.substring(pos - 1, pos) + ":" +
                (len != pos ? path.substring(pos) : String.valueOf(sep));
        len = path.length();
       }
      }
    }
    path = path.replace('/', sep);
    if (len > 1)
    {
     char ch;
     if (path.charAt(1) == ':' && (ch = path.charAt(0)) >= 'a' && ch <= 'z')
      path = String.valueOf((char) (ch - ('a' - 'A'))) + path.substring(1);
     if (path.charAt(len - 1) == sep && (len > 3 ||
         (path.charAt(1) != ':' && path.charAt(len - 2) != sep)))
      path = path.substring(0, len - 1);
    }
   }
  return path;
 }

 static final String normPlatformListOfPaths(String pathlist)
 { /* used by VM classes only */
  char sep = pathListPlatformSep(pathlist);
  int pos = 0;
  String properPathSep = sep != PATH_SEP ? String.valueOf(PATH_SEP) : null;
  do
  {
   int next = pathlist.indexOf(sep, pos);
   if (next < 0)
    next = pathlist.length();
    else if (properPathSep != null)
     pathlist = pathlist.substring(0, next) + properPathSep +
                 pathlist.substring(next + 1);
   String path = pathlist.substring(pos, next);
   String normPath = normPlatformPath(path);
   if (path != normPath)
   {
    pathlist = pathlist.substring(0, pos) + normPath +
                pathlist.substring(next);
    next = normPath.length() + pos;
   }
   pos = next + 1;
  } while (pathlist.length() >= pos);
  return pathlist;
 }

 static final String normVolumeColon(String path)
 { /* used by VM classes only */
  int len = path.length();
  return len > 1 ? (path.charAt(len - 1) == ':' &&
          pathPrefixLength(path) == len ? path + "." : path) :
          len > 0 ? path : ".";
 }

 static final String mapLibraryName(String libname)
 { /* used by VM classes only */
  if (libname == null)
   throw new NullPointerException();
  String prefix = LIBNAME_PREFIX;
  String suffix = LIBNAME_SUFFIX;
  String mappedName = mapLibraryName0((prefix != null ? prefix : "") +
                       libname + (suffix != null ? suffix : ""));
  return mappedName != null ? mappedName : "";
 }

 static final String getenvPlatform(String name)
 { /* used by VM classes only */
  String value = getenv0(name);
  if (value != null && value.length() == 0)
   value = null;
  return value;
 }

 static final String getLineSeparator()
 { /* used by VM classes only */
  String newline = getLineSeparator0();
  return newline != null && newline.length() > 0 ? newline : "\n";
 }

 private static char pathListPlatformSep(String pathlist)
 {
  char ch;
  int val = pathListPlatformSep0(pathlist);
  return val > 0 ? (char) val : (pathlist.length() > 2 &&
          pathlist.charAt(1) == ':' && driveIndexOf(pathlist.charAt(0)) > 0 &&
          ((ch = pathlist.charAt(2)) == '/' || ch == '\\')) ||
          pathlist.indexOf(';', 0) >= 0 || (pathlist.indexOf('\\', 0) > 0 &&
          (val = pathlist.indexOf(':', 0)) > 1 &&
          pathlist.indexOf(':', val + 1) < 0) ? ';' :
          (ch = PATH_SEP) == ';' || (ch != ':' &&
          pathlist.indexOf(':', 0) >= 0) ? ':' : ch;
 }

 private static String pathAppendName(String path, String name)
 {
  int len = path.length();
  return len > 0 ? (name.length() > 0 ? (path.charAt(len - 1) != FILE_SEP ?
          path + String.valueOf(FILE_SEP) + name : path + name) : path) : name;
 }

 private static String[] listInner(String path, byte[] dirdata)
 {
  String filename = path;
  if (DIR_DATASIZE_ISFIND > 0)
  {
   filename = realPath0(path, new int[1]);
   filename = pathAppendName(filename != path && filename != null ?
               normPlatformPath(filename) : path, "*.*");
  }
  filename = dirOpenReadFirst0(dirdata, filename);
  if (filename == null || filename.length() == 0)
  {
   /* if (DIR_DATASIZE_ISFIND > 0 && isRegFileOrDir0(path) == 2)
    return new String[0]; */
   return null;
  }
  String[] names;
  int count;
  try
  {
   names = new String[1];
   count = 0;
   do
   {
    if (!filename.equals(".") && !filename.equals(".."))
    {
     if (names.length <= count)
     {
      String[] newNames = new String[(count >> 1) + count + 32];
      System.arraycopy(names, 0, newNames, 0, count);
      names = newNames;
     }
     names[count++] = filename;
    }
    filename = dirReadNext0(dirdata);
   } while (filename != null && filename.length() > 0);
  }
  finally
  {
   dirClose0(dirdata);
  }
  if (names.length > count)
  {
   String[] newNames = new String[count];
   System.arraycopy(names, 0, newNames, 0, count);
   names = newNames;
  }
  return names;
 }

 private static int isHiddenInner(String path, byte[] dirdata)
 {
  int res = -1;
  String filename = dirOpenReadFirst0(dirdata, path);
  if (filename != null && filename.length() > 0)
  {
   try
   {
    res = dirIsHiddenFound0(dirdata);
    filename = dirReadNext0(dirdata);
    if (filename != null && filename.length() > 0)
    {
     res = 0;
     do
     {
      filename = dirReadNext0(dirdata);
     } while (filename != null && filename.length() > 0);
    }
   }
   finally
   {
    dirClose0(dirdata);
   }
   if (res > 0)
   {
    filename = getName(path);
    if (filename.indexOf('*', 0) >= 0 || filename.indexOf('?', 0) >= 0)
     res = 0;
   }
  }
  return res;
 }

 private static String canonPathCaseMatch(String curPath, byte[] dirdata)
  throws IOException
 {
  String canonName = dirOpenReadFirst0(dirdata, curPath);
  if (canonName != null && canonName.length() > 0)
  {
   try
   {
    String filename = dirReadNext0(dirdata);
    if (filename != null && filename.length() > 0)
    {
     canonName = null;
     do
     {
      filename = dirReadNext0(dirdata);
     } while (filename != null && filename.length() > 0);
    }
   }
   finally
   {
    dirClose0(dirdata);
   }
  }
   else
   {
    int res = access0(curPath, 0, 0, 0);
    if (res < 0 && isIOErrorNoEntity0(res) == 0)
     VMAccessorGnuJavaNio.checkIOResCodeVMChannel(res);
    canonName = null;
   }
  return canonName;
 }

 private static String canonPathCaseFind(String curPath, byte[] dirdata,
   String name)
 {
  String canonName = dirOpenReadFirst0(dirdata, curPath);
  if (canonName != null)
  {
   if (canonName.length() > 0)
   {
    try
    {
     do
     {
      if (canonName.equals(name))
       break;
      canonName = dirReadNext0(dirdata);
     } while (canonName != null && canonName.length() > 0);
    }
    finally
    {
     dirClose0(dirdata);
    }
    if (canonName == null)
     canonName = "";
   }
    else canonName = null;
  }
  return canonName;
 }

 private static String canonPathCaseScan(String curPath, byte[] dirdata,
   long[] devInodeArr)
  throws IOException
 {
  String canonName = dirOpenReadFirst0(dirdata, curPath);
  if (canonName != null && canonName.length() > 0)
  {
   try
   {
    long stDev = devInodeArr[0];
    long stIno = devInodeArr[1];
    int compared = 0;
    do
    {
     if (!canonName.equals(".") && !canonName.equals(".."))
     {
      devInodeArr[0] = -1L;
      int res = getLnkDevInode0(pathAppendName(curPath, canonName),
                 devInodeArr);
      if (res >= 0)
      {
       if (devInodeArr[0] == stDev && devInodeArr[1] == stIno)
       {
        if (compared > 0)
         break;
        String filename = dirReadNext0(dirdata);
        if (filename == null || filename.length() == 0)
         break;
        devInodeArr[0] = -1L;
        if (getLnkDevInode0(pathAppendName(curPath, filename),
            devInodeArr) >= 0 && devInodeArr[0] == stDev &&
            devInodeArr[1] == stIno)
         canonName = null;
        break;
       }
       compared++;
      }
       else if (isIOErrorNoEntity0(res) == 0)
        VMAccessorGnuJavaNio.checkIOResCodeVMChannel(res);

     }
     canonName = dirReadNext0(dirdata);
    } while (canonName != null && canonName.length() > 0);
   }
   finally
   {
    dirClose0(dirdata);
   }
  }
  return canonName;
 }

 static final String realPath(String path)
  throws IOException
 { /* used by VM classes only */
  path = normVolumeColon(path);
  int[] resArr = new int[1];
  String filename = realPath0(path, resArr);
  int res = resArr[0];
  if (res >= 0)
   return filename != path && filename != null ?
           normPlatformPath(filename) : path;
  if (isIOErrorNoEntity0(res) == 0)
   VMAccessorGnuJavaNio.checkIOResCodeVMChannel(res);
  int prefixLen = pathPrefixLength(path);
  int pos = path.length();
  do
  {
   if (pos == prefixLen)
    VMAccessorGnuJavaNio.checkIOResCodeVMChannel(res);
   pos = path.lastIndexOf(FILE_SEP, pos - 1);
   if (pos <= prefixLen)
    pos = prefixLen;
   filename = realPath0(path.substring(0, pos), resArr);
   res = resArr[0];
  } while (res < 0 && isIOErrorNoEntity0(res) != 0);
  VMAccessorGnuJavaNio.checkIOResCodeVMChannel(res);
  return filename != null ? pathAppendName(normPlatformPath(filename),
          path.substring(pos + 1)) : path;
 }

 private static int pathPrefixLength(String path)
 {
  int pos = 0;
  int len = path.length();
  if (len > 0)
  {
   char sep = FILE_SEP;
   if (IS_UNIX_FS)
   {
    if (path.charAt(0) == sep)
     pos = 1;
   }
    else
    {
     if (path.charAt(0) == sep)
     {
      pos = 1;
      if (len != 1 && path.charAt(1) == sep)
      {
       while (++pos < len)
        if (path.charAt(pos) == sep)
         break;
       if (pos < len)
       {
        while (++pos < len)
         if (path.charAt(pos) == sep)
          break;
        if (pos < len)
         pos++;
       }
      }
     }
      else
      {
       pos = path.indexOf(':', 0) + 1;
       if (pos > 1)
       {
        int i;
        if (path.lastIndexOf('.', pos - 2) >= 0 ||
            ((i = path.lastIndexOf(sep, pos - 2)) > 0 &&
            path.lastIndexOf(sep, i - 1) >= 0))
         pos = 0;
         else
         {
          if (pos < len)
          {
           if (path.charAt(pos) == sep)
            pos++;
            else if (pos > 2 && path.indexOf(':', pos) < 0 &&
                     path.indexOf(sep, pos + 1) < 0)
             pos = 0;
          }
         }
       }
      }
    }
  }
  return pos;
 }

 private static int driveIndexOf(char ch)
 {
  if (ch >= 'A' && ch <= 'Z')
   return ch - ('A' - 1);
  if (ch >= 'a' && ch <= 'z')
   return ch - ('a' - 1);
  return -1;
 }

 private static String drivePathPrefix(int drive)
 {
  return drive > 0 && drive <= 'Z' - 'A' + 1 ?
          String.valueOf((char) (drive + ('A' - 1))) + ":" : null;
 }

 private static String getCurDir()
 {
  String path = driveDirCache[0];
  if (path == null)
  {
   path = getDriveCurDir0(0);
   if (path == null || path.length() == 0)
    return null;
   if (IS_UNIX_FS && path.length() > 2 && path.charAt(1) == ':' &&
       path.charAt(2) == FILE_SEP)
    defaultDrive = driveIndexOf(path.charAt(0));
   path = normPlatformPath(path);
   driveDirCache[0] = path;
   int drive;
   if (driveDirCache.length > 1 && path.length() > 2 &&
       path.charAt(1) == ':' && (drive = driveIndexOf(path.charAt(0))) > 0 &&
       path.charAt(2) == FILE_SEP)
    driveDirCache[drive] = path;
  }
  return path;
 }

 private static String getDriveCurDir(int drive, boolean useCache)
 {
  String path = null;
  if (drive > 0 && driveDirCache.length > drive &&
      (!useCache || (path = driveDirCache[drive]) == null))
  {
   path = getDriveCurDir0(drive);
   if (path != null && path.length() > 0)
   {
    path = normPlatformPath(path);
    if (path.charAt(0) == FILE_SEP)
     path = drivePathPrefix(drive) + path;
   }
    else
    {
     String drivePrefix = drivePathPrefix(drive) + ".";
     String filename = realPath0(drivePrefix, new int[1]);
     if ((filename == drivePrefix || filename == null ||
         (path = normPlatformPath(filename)).length() <= 2 ||
         driveIndexOf(path.charAt(0)) != drive || path.charAt(1) != ':' ||
         path.charAt(2) != FILE_SEP) &&
         ((path = getVolumeCurDir0(drivePrefix)) == null ||
         (path = normPlatformPath(path)).length() <= 2 ||
         driveIndexOf(path.charAt(0)) != drive || path.charAt(1) != ':' ||
         path.charAt(2) != FILE_SEP))
      return null;
    }
   if (driveDirCache[drive] == null)
    driveDirCache[drive] = path;
  }
  return path;
 }

 private static native long currentTime0(int isNano);

 private static native int isCaseSensitive0();

 private static native int isLongNameSupported0();

 private static native int isUniRootFileSys0();

 private static native int isDotFileHidden0();

 private static native int getFilePathSepChar0(int isPath);

 private static native String getLineSeparator0();

 private static native String getDosUnixDrivePrefix0();

 private static native String getLibNamePrefixSuffix0(int isSuffix);

 private static native String mapLibraryName0(String libname);

 private static native String getenv0(String name);

 private static native int rename0(String path, String destpath);

 private static native int delete0(String path);

 private static native int mkdir0(String path);

 private static native int access0(String path, int chkRead, int chkWrite,
   int chkExec);

 private static native int isRegFileOrDir0(String path);

 private static native long getLastModified0(String path);

 private static native int setLastModified0(long mtime, String path);

 private static native int setReadOnly0(String path);

 private static native int isIOErrorNoEntity0(int res);

 private static native String realPath0(String path, int[] resArr);

 private static native int getDrivesMask0();

 private static native String getVolumeRoot0(int index);

 private static native int checkVolumeRoot0(String path);

 private static native String getDriveCurDir0(int drive);

 private static native String getVolumeCurDir0(String path);

 private static native String normPlatformPath0(String path);

 private static native int pathListPlatformSep0(String pathlist);

 private static native int isHidden0(String path);

 private static native int getLnkDevInode0(String path, long[] devInodeArr);

 private static native int dirOpNeedsSync0();

 private static native int dirDataSizeAndIsFind0();

 private static native String dirOpenReadFirst0(byte[] dirdata, String path);

 private static native String dirReadNext0(byte[] dirdata);

 private static native int dirIsHiddenFound0(byte[] dirdata);

 private static native int dirClose0(byte[] dirdata);
}
