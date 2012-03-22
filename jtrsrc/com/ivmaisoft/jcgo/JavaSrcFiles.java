/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/JavaSrcFiles.java --
 * a part of JCGO translator.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@mail.ru>
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

package com.ivmaisoft.jcgo;

import java.io.File;

/**
 * This class contains routines related to java file names processing.
 */

final class JavaSrcFiles {

    static final String JAVA_EXT = ".java".toString();

    private String classPathList = "";

    private final ObjHashtable knownFolders = new ObjHashtable();

    private final ObjHashSet foldersTable = new ObjHashSet();

    private final ObjHashSet filesTable = new ObjHashSet();

    JavaSrcFiles() {
    }

    String toClassName(String name) {
        if (name.endsWith(JAVA_EXT)) {
            String fName = (new File(name)).getPath();
            int nameLen = fName.lastIndexOf('.');
            if (nameLen > 0) {
                fName = fName.replace(File.separatorChar, '.');
                int namePos = -1;
                while (++namePos < nameLen) {
                    if (fName.charAt(namePos) != '.')
                        break;
                }
                if (namePos < nameLen) {
                    name = fName.substring(namePos, nameLen);
                }
            }
        }
        return name;
    }

    static String adjustPathPrefix(String path, String progBasePath) {
        path = path.replace('/', File.separatorChar);
        return path.startsWith("$~" + File.separator) ? progBasePath
                + path.substring(2) : path;
    }

    void setClassPath(String pathList, String progBasePath) {
        if (pathList != null && pathList.length() > 0) {
            int i = 0;
            do {
                int j = pathList.indexOf(File.pathSeparatorChar, i);
                if (j < 0) {
                    j = pathList.length();
                }
                if (i < j) {
                    String origPath = pathList.substring(i, j);
                    String path = adjustPathPrefix(origPath, progBasePath);
                    String realPath = path;
                    int k = path.indexOf(File.separator + "$%", 0);
                    if (k >= 0 && path.length() > k + 3
                            && path.indexOf("..", k + 3) < 0
                            && path.charAt(k + 3) != '.'
                            && path.charAt(path.length() - 1) != '.'
                            && path.indexOf(File.separatorChar, k + 3) < 0) {
                        realPath = path.substring(0, k + 1)
                                + path.substring(k + 3).replace('.',
                                        File.separatorChar);
                    }
                    if (!(new File(realPath)).isDirectory()
                            && (realPath == path || !(new File(realPath
                                    + JAVA_EXT)).isFile())) {
                        System.err.println("Source path does not exist: "
                                + realPath);
                        if (!Main.dict.ignoreErrs) {
                            System.exit(2);
                        }
                    }
                    if (path != origPath) {
                        pathList = pathList.substring(0, i) + path
                                + pathList.substring(j);
                        j += path.length() - origPath.length();
                    }
                }
                i = j + 1;
            } while (i < pathList.length());
            setClassPathInner(pathList);
        }
    }

    void setClassPath() {
        String userDir = ".";
        try {
            userDir = System.getProperty("user.dir", userDir);
        } catch (SecurityException e) {
        }
        setClassPathInner(userDir);
        if (Main.dict.verbose) {
            System.out.println("Source search path: " + classPathList);
        }
    }

    private void setClassPathInner(String pathList) {
        if (classPathList.length() > 0) {
            int i = 0;
            int len = pathList.length();
            do {
                int j = classPathList.indexOf(File.pathSeparatorChar, i);
                if (j < 0) {
                    j = classPathList.length();
                }
                if (j - i == len
                        && classPathList.regionMatches(i, pathList, 0, len))
                    return;
                i = j + 1;
            } while (i < classPathList.length());
            classPathList = classPathList + File.pathSeparator + pathList;
        } else {
            classPathList = pathList;
        }
    }

    String classFilename(String className, boolean allowInner) {
        String pathName = className.replace('.', File.separatorChar) + JAVA_EXT;
        int i = 0;
        String outerName = null;
        int j;
        if (allowInner
                && (j = className.indexOf('$', className.lastIndexOf('.') + 1)) > 0) {
            outerName = pathName.substring(0, j) + JAVA_EXT;
        }
        do {
            j = classPathList.indexOf(File.pathSeparatorChar, i);
            if (j < 0) {
                j = classPathList.length();
            }
            if (i < j) {
                String prefix = classPathList.substring(i, j);
                String condOfPrefix = "";
                int k = prefix.indexOf(File.separator + "$%", 0);
                if (k >= 0) {
                    condOfPrefix = prefix.substring(k + 3).replace('.',
                            File.separatorChar);
                    prefix = prefix.substring(0, k + 1);
                }
                String name = pathName;
                do {
                    if ((k < 0
                            || name.startsWith(condOfPrefix
                                    + File.separatorChar) || name
                            .equals(condOfPrefix + JAVA_EXT))
                            && pathExists(prefix, name)) {
                        File file = new File(prefix, name);
                        String path = file.getPath();
                        if (!foldersTable.contains(path)) {
                            if (filesTable.contains(path))
                                return path;
                            if (file.isFile()) {
                                filesTable.add(path);
                                return path;
                            }
                            foldersTable.add(path);
                        }
                    }
                    if (outerName == null || name == outerName)
                        break;
                    name = outerName;
                } while (true);
            }
            i = j + 1;
        } while (i < classPathList.length());
        return null;
    }

    private boolean pathExists(String prefix, String name) {
        int i = name.lastIndexOf(File.separatorChar);
        String path = prefix;
        if (i >= 0) {
            path = (new File(prefix, name.substring(0, i))).getPath();
        }
        ObjHashtable folder = (ObjHashtable) knownFolders.get(path);
        if (folder == null) {
            String[] list = null;
            if (i < 0 || pathExists(prefix, name.substring(0, i))) {
                list = (new File(path)).list();
            }
            if (list == null) {
                list = new String[0];
            }
            folder = new ObjHashtable();
            for (int j = 0; j < list.length; j++) {
                if (list[j].endsWith(JAVA_EXT) || list[j].indexOf('.', 0) < 0) {
                    folder.put(list[j], "");
                }
            }
            knownFolders.put(path, folder);
        }
        return folder.get(name.substring(i + 1)) != null;
    }
}
