/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/Main.java --
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

import java.io.*;
import java.util.Enumeration;

/**
 * This is the main class.
 */

public final class Main {

    static final String VERSION = Integer.toString(VerInfo.VER_NUM / 100) + "."
            + Integer.toString((VerInfo.VER_NUM / 10) % 10)
            + Integer.toString(VerInfo.VER_NUM % 10);

    static final String VER_ABBR = "JCGO_"
            + VERSION.substring(0, VERSION.length() - 3)
            + VERSION.substring(VERSION.length() - 2);

    static CompilationUnit curUnit;

    static final ClassDictionary dict = new ClassDictionary();

    private Main() {
    }

    static void showProgInfo() {
        System.out.println("JCGO v" + VERSION
                + " - Java source to C code translator");
        System.out.println(VerInfo.COPYRIGHT_AUTHOR);
        System.out.println(VerInfo.COPYRIGHT);
        System.out.println("This is free software. All rights reserved."
                + " See LICENSE and README files.");
        System.out.println("");
    }

    private static void showUsage() {
        System.out
                .println("Usage: jcgo [options] <target_class> [<additional_class1> ...]");
        System.out.println("Options:");
        System.out.println(" -sourcepath <pathlist> "
                + " Specify where to find input source files");
        System.out.println(" -d <directory> "
                + " Specify where to place generated files");
        System.out.println(" -verbose " + " Verbose mode");
        System.out.println(" -r[[p]c][g|[p]m][[p]f] <packageOrClass> "
                + " Force class members reflection");
        System.out.println("@<filename> "
                + " Specify additional classes in the file");
        System.out.println("");
    }

    public static void main(String[] argv) {
        int consumed = getAmountOfUsedMemory();
        try {
            boolean showHelp = false;
            if (argv.length == 0 || (argv.length == 1 && argv[0].equals("-h"))) {
                showProgInfo();
                showUsage();
                showHelp = true;
            }
            if (showHelp) {
                System.exit(0);
            }
            boolean skipClinitTrace = false;
            String progBasePath = getProgBasePath();
            String mainClassName = null;
            ObjQueue classnames = new ObjQueue();
            for (int i = 0; i < argv.length; i++) {
                if (argv[i].startsWith("-")) {
                    if (argv[i].equals("-t")) {
                        skipClinitTrace = true;
                    } else if (!processSimpleOption(argv[i])) {
                        if (i + 1 >= argv.length) {
                            System.err
                                    .println("missing argument for last option");
                            System.exit(2);
                        }
                        if (argv[i].equals("-sourcepath")
                                || argv[i].equals("-src")) {
                            dict.javaFiles.setClassPath(argv[i + 1],
                                    progBasePath);
                        } else if (!processOptionWithArg(argv[i], argv[i + 1])) {
                            System.err.println("invalid option: " + argv[i]);
                            System.exit(2);
                        }
                        i++;
                    }
                } else {
                    String className = argv[i];
                    if (className.charAt(0) == '@') {
                        readResponseFile(
                                classnames,
                                className.length() == 1 && i + 1 < argv.length ? argv[++i]
                                        : className.substring(1), progBasePath);
                    } else if (mainClassName != null) {
                        classnames.addLast(dict.javaFiles
                                .toClassName(className));
                    } else {
                        mainClassName = dict.javaFiles.toClassName(className);
                    }
                }
            }
            if (mainClassName == null) {
                System.err.println("Target class must be specified");
                System.exit(2);
            }
            dict.javaFiles.setClassPath();
            String[] sortednames = strsQueueToSortedArr(classnames);
            if (dict.outPath.length() == 0) {
                if (isOutPathRequired(progBasePath)) {
                    System.err
                            .println("Use -d to specify existing directory for output");
                    System.exit(2);
                }
                (new File(dict.outPath = "jcgo_Out")).mkdir();
            }
            int time = (int) System.currentTimeMillis();
            ClassDefinition mainClass = processFirst(mainClassName);
            for (int i = 0; i < sortednames.length; i++) {
                ObjQueue proxyIfaceNames = dict.nameMapper
                        .decodeProxyClassName(sortednames[i]);
                if (proxyIfaceNames != null) {
                    Enumeration en = proxyIfaceNames.elements();
                    while (en.hasMoreElements()) {
                        dict.get((String) en.nextElement())
                                .predefineClass(null);
                    }
                } else {
                    dict.get(sortednames[i]).predefineClass(null);
                }
            }
            processAll(mainClass, sortednames, time, skipClinitTrace);
        } catch (TranslateException e) {
            System.err.println(e.getMessage());
            System.exit(6);
        } catch (RuntimeException e) {
            if (dict.verbose) {
                e.printStackTrace(System.out);
            }
            System.err.println(e.toString());
            System.exit(7);
        } catch (OutOfMemoryError e) {
            System.err.print(" Out of memory ");
            try {
                System.err.print("(consumed "
                        + Integer.toString(getAmountOfUsedMemory() - consumed)
                        + " KiB)");
            } catch (OutOfMemoryError ee) {
            }
            System.err.println("");
            System.exit(255);
        }
        System.exit(0);
    }

    private static boolean processSimpleOption(String option) {
        if (option.equals("-verbose") || option.equals("-v")) {
            dict.verbose = true;
            return true;
        } else if (option.equals("-v2")) {
            dict.verbose = dict.verboseTracing = true;
            return true;
        } else if (option.equals("-e")) {
            dict.ignoreErrs = true;
            return true;
        } else if (option.startsWith("-l") && dict.failOnClassLimit == 0) {
            dict.failOnClassLimit = (new ConstValue(option.substring(2)))
                    .getIntValue();
            return true;
        } else {
            return false;
        }
    }

    private static boolean processOptionWithArg(String option, String value) {
        if (option.equals("-d") && dict.outPath.length() == 0) {
            if (!new File(value).isDirectory()) {
                System.err.println("Output directory does not exist: " + value);
                System.exit(2);
            }
            dict.outPath = value;
            return true;
        } else if (option.startsWith("-r")) {
            processReflectOption(option.substring(2), value);
            return true;
        } else if (option.equals("-c") && dict.failOnClassName == null) {
            dict.failOnClassName = value;
            return true;
        } else if (option.equals("-f") && dict.failOnFieldName == null) {
            dict.failOnFieldName = value;
            return true;
        } else if (option.equals("-m") && dict.failOnMethodId == null) {
            dict.failOnMethodId = value;
            return true;
        } else {
            return false;
        }
    }

    private static void processReflectOption(String flags, String packageOrClass) {
        if (packageOrClass.equals("*") || packageOrClass.equals(".*")) {
            packageOrClass = "";
        }
        if (dict.forcedReflectClassOrPkg.put(packageOrClass, flags) != null
                || packageOrClass.indexOf('/', 0) > 0
                || (File.separatorChar != '/' && packageOrClass.indexOf(
                        File.separatorChar, 0) > 0)) {
            System.err.println("Invalid or duplicate -r<flags> for: "
                    + (packageOrClass.length() > 0 ? packageOrClass
                            : "(unnamed package)"));
            System.exit(2);
        }
    }

    private static String[] strsQueueToSortedArr(ObjQueue queue) {
        String[] strs = new String[queue.countSize()];
        queue.copyInto(strs);
        String[] sortedstrs = new String[strs.length];
        System.arraycopy(strs, 0, sortedstrs, 0, strs.length);
        ClassDefinition.mergeSort(strs, sortedstrs, 0, strs.length);
        return sortedstrs;
    }

    private static boolean isOutPathRequired(String progBasePath) {
        String userDir = ".";
        try {
            userDir = System.getProperty("user.dir", userDir);
        } catch (SecurityException e) {
        }
        return userDir.equals(progBasePath) || progBasePath.equals(".")
                || userDir.startsWith(progBasePath + File.separator);
    }

    private static String getProgBasePath() {
        String pathList = "";
        try {
            pathList = System.getProperty("java.class.path", pathList);
        } catch (SecurityException e) {
        }
        int pos = 0;
        String firstBasePath = ".";
        do {
            int next = pathList.indexOf(File.pathSeparatorChar, pos);
            if (next < 0) {
                next = pathList.length();
            }
            String basePath = getParentPathIfJar(pathList.substring(pos, next));
            if ((new File(basePath, "goclsp")).isDirectory())
                return basePath;
            if (firstBasePath.equals(".")) {
                firstBasePath = basePath;
            }
            pos = next + 1;
        } while (pathList.length() >= pos);
        return firstBasePath;
    }

    static String getParentPathIfJar(String path) {
        if ((path.endsWith(".jar") || path.endsWith(".Jar")
                || path.endsWith(".JAR") || path.endsWith(".zip")
                || path.endsWith(".Zip") || path.endsWith(".ZIP"))
                && (path = (new File(path)).getParent()) == null) {
            path = "";
        }
        return path.length() > 0 ? path : ".";
    }

    private static int getAmountOfUsedMemory() {
        return (int) ((Runtime.getRuntime().totalMemory() - Runtime
                .getRuntime().freeMemory()) >> 10);
    }

    static void parseJavaFile(String classname) {
        String fileName = dict.javaFiles.classFilename(classname, true);
        if (fileName == null) {
            System.err.println("Cannot find class: " + classname);
            System.exit(5);
        }
        dict.filesParsed++;
        dict.message("Parsing file: " + fileName);
        Scanner.Init(fileName);
        dict.inBytesCount += (int) (new File(fileName)).length();
        Parser.Parse();
        Term t = curUnit;
        if (Scanner.err.count > 0)
            throw new TranslateException("Lexical error in file: " + fileName);
        Context c = new Context();
        c.fileName = fileName;
        t.processPass0(c);
        t.processPass1(c);
    }

    static ObjQueue readFileOfLines(String fname) {
        ObjQueue lines = new ObjQueue();
        try {
            BufferedReader infile = new BufferedReader(new FileReader(fname));
            // Portability - use the following for Java 1.0
            // DataInputStream infile = new DataInputStream(new
            // BufferedInputStream(new FileInputStream(fname)));
            String str;
            while ((str = infile.readLine()) != null) {
                lines.addLast(str);
            }
            infile.close();
        } catch (IOException e) {
            lines = null;
        }
        return lines;
    }

    private static void readResponseFile(ObjQueue classnames, String fname,
            String progBasePath) {
        ObjQueue lines = readFileOfLines(JavaSrcFiles.adjustPathPrefix(fname,
                progBasePath));
        if (lines == null) {
            System.err.println("Cannot read response file: " + fname);
            System.exit(2);
        }
        Enumeration en = lines.elements();
        while (en.hasMoreElements()) {
            String str = ((String) en.nextElement()).trim();
            char ch;
            if (str.length() > 0 && (ch = str.charAt(0)) != '!' && ch != '#'
                    && ch != ';') {
                String path = cutLinePrefix(str, "-sourcepath");
                int pos;
                if (path != null || (path = cutLinePrefix(str, "-src")) != null) {
                    dict.javaFiles.setClassPath(path, progBasePath);
                } else if (str.startsWith("-r")
                        && ((pos = str.indexOf(' ', 2)) >= 0 || (pos = str
                                .indexOf('\t', 2)) >= 0)) {
                    processReflectOption(str.substring(2, pos),
                            str.substring(pos + 1).trim());
                } else {
                    classnames.addLast(dict.javaFiles.toClassName(str));
                }
            }
        }
    }

    private static String cutLinePrefix(String str, String prefix) {
        return str.startsWith(prefix) && str.length() > prefix.length()
                && str.charAt(prefix.length()) <= ' ' ? str.substring(
                prefix.length() + 1).trim() : null;
    }

    private static ClassDefinition processFirst(String classname) {
        System.out.println("Initializing...");
        dict.setupCoreClasses();
        ClassDefinition mainClass = dict.get(classname);
        MethodDefinition md = mainClass.getMethod(Names.SIGN_MAIN);
        if (md == null || !md.isClassMethod()) {
            dict.fatal(mainClass.passOneContext().fileName, 1,
                    "Cannot find static method: main(String[])");
        }
        dict.get(Names.JAVA_LANG_CLASS).define(mainClass);
        dict.get(Names.JAVA_LANG_STRING).define(mainClass);
        return mainClass;
    }

    private static ObjVector namesToClassLiterals(ObjQueue classNames) {
        ObjVector parmSig = new ObjVector();
        Enumeration en = classNames.elements();
        while (en.hasMoreElements()) {
            parmSig.addElement(dict.get((String) en.nextElement()));
        }
        return parmSig;
    }

    private static void processAll(ClassDefinition mainClass,
            String[] sortednames, int time, boolean skipClinitTrace) {
        System.out.println("Analysis pass...");
        mainClass.markMethod(Names.SIGN_MAIN);
        for (int i = 0; i < sortednames.length; i++) {
            ObjQueue proxyIfaceNames = dict.nameMapper
                    .decodeProxyClassName(sortednames[i]);
            if (proxyIfaceNames == null
                    || dict.addProxyClass(
                            namesToClassLiterals(proxyIfaceNames), null) == null) {
                ClassDefinition cd = dict.get(sortednames[i]);
                cd.markUsed();
                cd.markAllNatives();
                dict.dynClassesToTrace.put(cd, cd);
            }
        }
        mainClass.markAllPublicStaticMethods();
        dict.producePassOne();
        boolean mainReflected = false;
        if (dict.forcedReflectClassOrPkg.size() > 0) {
            ObjQueue forcedClassNames = new ObjQueue();
            Enumeration en = dict.forcedReflectClassOrPkg.keys();
            while (en.hasMoreElements()) {
                String packageOrClass = (String) en.nextElement();
                if (packageOrClass.length() > 0
                        && !packageOrClass.endsWith(".*")) {
                    forcedClassNames.addLast(packageOrClass);
                }
            }
            sortednames = strsQueueToSortedArr(forcedClassNames);
            if (sortednames.length > 0) {
                for (int i = 0; i < sortednames.length; i++) {
                    dict.get(sortednames[i]).define(mainClass);
                }
                if (dict.hasReflectedMethods) {
                    mainClass.reflectAllPublicStaticMethods();
                    mainReflected = true;
                }
                dict.producePassOne();
            }
        }
        if (dict.hasReflectedMethods && !mainReflected
                && mainClass.reflectAllPublicStaticMethods()) {
            dict.producePassOne();
        }
        dict.allowConstStr = Main.dict.get(Names.JAVA_LANG_STRING)
                .otherInstanceFieldsUnused(Names.fieldsOrderString);
        System.out.println("Output pass...");
        dict.mainClass = mainClass;
        Enumeration en = dict.usedClasses();
        while (en.hasMoreElements()) {
            ((ClassDefinition) en.nextElement()).prepareMethodsForOutput();
        }
        dict.get(Names.JAVA_LANG_OBJECT).produceOutputRecursive(
                new ObjHashSet());
        System.out.println("Writing class tables...");
        ClassDefinition.setSpecialVirtual();
        Enumeration en2 = dict.usedClasses();
        while (en2.hasMoreElements()) {
            ((ClassDefinition) en2.nextElement()).finishClass();
        }
        System.out.println("Creating main file...");
        mainClass.buildMain(skipClinitTrace);
        System.out.println("Parsed: " + Integer.toString(dict.filesParsed)
                + " java files (" + Integer.toString(dict.inBytesCount >> 10)
                + " KiB). Analyzed: " + Integer.toString(dict.methodsAnalyzed)
                + " methods.");
        System.out.println("Produced: " + Integer.toString(dict.outFilesCount)
                + " c/h files (" + Integer.toString(dict.outBytesCount >> 10)
                + " KiB).");
        System.out.println("Contains: " + Integer.toString(dict.methodsWritten)
                + " java methods, " + Integer.toString(dict.normalCalls)
                + " normal and " + Integer.toString(dict.indirectCalls)
                + " indirect calls.");
        System.out
                .println("Done conversion in "
                        + Integer.toString(((int) System.currentTimeMillis() - time) / 1000)
                        + " seconds. Total heap size: "
                        + Integer.toString((int) (Runtime.getRuntime()
                                .totalMemory() >> 10)) + " KiB.");
        if (dict.errorsCnt > 0) {
            System.out.println("");
            System.out.println(" Translated with " + dict.errorsCnt
                    + " errors!");
            System.exit(3);
        }
    }
}
