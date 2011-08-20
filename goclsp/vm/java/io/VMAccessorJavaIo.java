/*
 * @(#) $(JCGO)/goclsp/vm/java/io/VMAccessorJavaIo.java --
 * VM cross-package access helper for "java.io".
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
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

import gnu.java.nio.FileChannelImpl;

public final class VMAccessorJavaIo
{ /* used by VM classes only */

 private VMAccessorJavaIo() {}

 public static FileInputStream newFileInputStream(FileChannelImpl ch)
 {
  return new FileInputStream(ch);
 }

 public static FileOutputStream newFileOutputStream(FileChannelImpl ch)
 {
  return new FileOutputStream(ch);
 }

 public static String canonPathCaseVMFile(String path)
  throws IOException
 {
  return VMFile.canonPathCase(path);
 }

 public static String collapsePathDotsVMFile(String path)
 {
  return VMFile.collapsePathDots(path);
 }

 public static long currentTimeVMFile(boolean isNano)
 {
  return VMFile.currentTime(isNano);
 }

 public static String getenvPlatformVMFile(String name)
 {
  return VMFile.getenvPlatform(name);
 }

 public static String getFileSeparatorVMFile()
 {
  return VMFile.getFileSeparator();
 }

 public static String getLineSeparatorVMFile()
 {
  return VMFile.getLineSeparator();
 }

 public static String getPathSeparatorVMFile()
 {
  return VMFile.getPathSeparator();
 }

 public static boolean isDirectoryVMFile(String path)
 {
  return VMFile.isDirectory(path);
 }

 public static String makeAbsPathVMFile(String path)
 {
  return VMFile.makeAbsPath(path);
 }

 public static String mapLibraryNameVMFile(String libname)
 {
  return VMFile.mapLibraryName(libname);
 }

 public static String normVolumeColonVMFile(String path)
 {
  return VMFile.normVolumeColon(path);
 }

 public static String normPlatformListOfPathsVMFile(String pathlist)
 {
  return VMFile.normPlatformListOfPaths(pathlist);
 }

 public static String normPlatformPathVMFile(String path)
 {
  return VMFile.normPlatformPath(path);
 }

 public static String pathParentOfVMFile(String path)
 {
  return VMFile.pathParentOf(path);
 }

 public static String realPathVMFile(String path)
  throws IOException
 {
  return VMFile.realPath(path);
 }
}
