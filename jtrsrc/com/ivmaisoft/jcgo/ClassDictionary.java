/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ClassDictionary.java --
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

import java.util.Enumeration;

/**
 * This is the class dictionary.
 */

final class ClassDictionary {

    private final OrderedMap allClasses = new OrderedMap();

    private final ObjHashSet missingClasses = new ObjHashSet();

    private final ObjQueue classOrder = new ObjQueue();

    private final OrderedMap strpool = new OrderedMap();

    private final ObjHashtable arrpool = new ObjHashtable();

    private final OrderedMap arrtypes = new OrderedMap();

    private final SortedStrMap charArrayStrs = new SortedStrMap();

    private final SortedStrMap byteArrayStrs = new SortedStrMap();

    private final ObjHashtable mproxypool = new ObjHashtable();

    final ClassDefinition[] classTable = new ClassDefinition[Type.OBJECTARRAY + 1];

    final JavaSrcFiles javaFiles = new JavaSrcFiles();

    final NameMapper nameMapper = new NameMapper();

    final ObjHashtable forcedReflectClassOrPkg = new ObjHashtable();

    private ExpressionType strValueType;

    private final ObjHashSet exactGetNameClasses = new ObjHashSet();

    private final ObjVector usedGetNameClasses = new ObjVector();

    private boolean markInitSysErr;

    boolean markBasicCtors;

    boolean markDirectIfaces;

    boolean markStrIndexOutInit;

    boolean buildClassTable;

    boolean hasEnumValueOf;

    int allowConstClass;

    int allowConstStr;

    boolean fillStrHash;

    boolean hasReflectedMethods;

    boolean verbose;

    boolean verboseTracing;

    boolean ignoreErrs;

    String outPath = "";

    MethodDefinition ourMethod;

    ObjHashtable assignedLocals;

    ObjQueue stackObjRetCalls;

    ClassDefinition mainClass;

    int failOnClassLimit;

    String failOnClassName;

    String failOnFieldName;

    String failOnMethodId;

    final OrderedMap dynClassesToTrace = new OrderedMap();

    final ObjHashSet inProgressMethodInfos = new ObjHashSet();

    ObjHashSet instancesCreatedOnTrace;

    ObjHashtable notYetCallableTraceInfos;

    final ObjQueue pendingTraceInfos = new ObjQueue();

    OrderedMap dynCallerTraceInfos;

    boolean classInitWeakDepend;

    MethodDefinition curHelperForMethod;

    MethodTraceInfo curTraceInfo;

    int tracedInfosCount;

    int methodsAnalyzed;

    int methodsWritten;

    int normalCalls;

    int indirectCalls;

    int filesParsed;

    int inBytesCount;

    int outBytesCount;

    int outFilesCount;

    int errorsCnt;

    ClassDictionary() {
        Context context = new Context();
        for (int type = Type.NULLREF; type <= Type.VOID; type++) {
            ClassDefinition cd = new ClassDefinition(type, context);
            allClasses.put(cd.name(), classTable[type] = cd);
        }
    }

    void setupCoreClasses() {
        get(Names.JAVA_LANG_OBJECT);
        get(Names.JAVA_LANG_STRING);
        get(Names.JAVA_LANG_CLASS);
        get(Names.JAVA_LANG_CLONEABLE);
        for (int arrtype = Type.CLASSINTERFACE + Type.BOOLEAN; arrtype <= Type.CLASSINTERFACE
                + Type.VOID; arrtype++) {
            ClassDefinition cd = new ClassDefinition(arrtype);
            allClasses.put(Type.sig[arrtype], classTable[arrtype] = cd);
            Context context = new Context();
            cd.definePass0(context, AccModifier.FINAL, Type.sig[arrtype],
                    Empty.newTerm(), Empty.newTerm(), Empty.newTerm(), false);
            context.currentClass = cd;
            cd.addField(new VariableDefinition(cd, "length", AccModifier.PUBLIC
                    | AccModifier.FINAL | AccModifier.SYNTHETIC,
                    classTable[Type.INT], Empty.newTerm(), false));
        }
        String[] names = Names.specVmClasses;
        for (int i = 0; i < names.length; i++) {
            get(names[i]);
        }
        names = Names.specVmExceptions;
        for (int i = 0; i < names.length; i++) {
            get(names[i]);
        }
    }

    void message(String msg) {
        if (verbose) {
            System.out.println(msg);
        }
    }

    void fatal(String fileName, int lineNumber, String msg) {
        String s = fileName + ":" + lineNumber + ": " + msg;
        if (!ignoreErrs)
            throw new TranslateException(s);
        System.out.println(" " + s);
        if (verbose) {
            System.out.println("");
        }
        errorsCnt++;
    }

    void classNowUsed(ClassDefinition cd) {
        classOrder.addLast(cd);
    }

    Enumeration usedClasses() {
        return classOrder.elements();
    }

    int classCount() {
        return allClasses.size();
    }

    ClassDefinition get(String name) {
        ClassDefinition cd = (ClassDefinition) allClasses.get(name);
        if (cd == null) {
            cd = new ClassDefinition(name);
            allClasses.put(name, cd);
        }
        return cd;
    }

    boolean alreadyKnown(String name) {
        return allClasses.get(name) != null;
    }

    boolean exists(String name) {
        if (allClasses.get(name) != null)
            return true;
        if (!missingClasses.contains(name)) {
            if (javaFiles.classFilename(name, false) != null)
                return true;
            missingClasses.add(name);
        }
        return false;
    }

    boolean existsOrInner(String name) {
        if (exists(name))
            return true;
        int i = name.lastIndexOf('.');
        int j = name.indexOf('$', i + 1);
        if (j <= 0 || j - i == 1)
            return false;
        String baseName = name.substring(0, j);
        if (!exists(baseName))
            return false;
        get(baseName).predefineClassNoMark();
        return exists(name);
    }

    ClassDefinition getInner(String baseName, String id) {
        String className = baseName + "$" + id;
        if (exists(className))
            return get(className);
        if (exists(baseName)) {
            get(baseName).predefineClassNoMark();
            if (exists(className))
                return get(className);
        }
        return null;
    }

    void defineIfExists(String name, ClassDefinition forClass) {
        if (exists(name)) {
            ClassDefinition cd = get(name);
            cd.predefineClass(forClass);
            cd.markUsed();
        }
    }

    ClassDefinition dynamicDefineClass(String s, ClassDefinition currentClass,
            ClassDefinition forClass) {
        if (s.length() > 0 && (s.charAt(0) == '.' || s.charAt(0) == '/'))
            s = s.substring(1);
        String s1 = s;
        int i;
        ClassDefinition cd = null;
        if ((lookLikeClassname(s) || ((s1 = s.replace('/', '.')) != s && lookLikeClassname(s1)))
                && ((currentClass != null
                        && (i = currentClass.name().lastIndexOf('.')) > 0 && existsOrInner(s = currentClass
                        .name().substring(0, i + 1) + s1)) || existsOrInner(s = s1))) {
            cd = get(s);
            if (!cd.defineDynamic(forClass)) {
                cd = null;
            }
        }
        return cd;
    }

    private static boolean lookLikeClassname(String str) {
        int i = str.length();
        if (i > 0 && str.charAt(i - 1) != '.'
                && NameMapper.isLetter(str.charAt(0))) {
            while (--i > 0) {
                char ch = str.charAt(i);
                if (!NameMapper.isLetter(ch)
                        && ch != '_'
                        && ch != '$'
                        && ((ch != '.' && (ch < '0' || ch > '9')) || str
                                .charAt(i - 1) == '.'))
                    break;
            }
            if (i == 0)
                return true;
        }
        return false;
    }

    void addGetNameClass(ExpressionType classLiteralValue,
            ClassDefinition forClass) {
        ClassDefinition cd;
        if (classLiteralValue != null) {
            cd = classLiteralValue.signatureClass();
            if (cd.objectSize() != Type.CLASSINTERFACE) {
                cd = classTable[Type.CLASSINTERFACE + Type.BOOLEAN];
                exactGetNameClasses.add(cd);
                return;
            }
            if (cd.isFinal()
                    || (cd != classLiteralValue && classLiteralValue
                            .objectSize() == Type.CLASSINTERFACE)) {
                exactGetNameClasses.add(cd);
                addStringLiteral(cd.name(), cd, true);
                return;
            }
        } else {
            cd = get(Names.JAVA_LANG_OBJECT);
        }
        int i = usedGetNameClasses.size();
        while (i-- > 0) {
            if (((ClassDefinition) usedGetNameClasses.elementAt(i))
                    .isAssignableFrom(cd, 0, forClass))
                return;
        }
        i = usedGetNameClasses.size();
        while (i-- > 0) {
            if (cd.isAssignableFrom(
                    (ClassDefinition) usedGetNameClasses.elementAt(i), 0,
                    forClass)) {
                usedGetNameClasses.removeElementAt(i);
            }
        }
        usedGetNameClasses.addElement(cd);
        if (cd.name().equals(Names.JAVA_LANG_THROWABLE)
                || cd.name().equals(Names.JAVA_LANG_OBJECT)) {
            markInitSysErr = true;
        }
        Enumeration en = usedClasses();
        while (en.hasMoreElements()) {
            ClassDefinition cd2 = (ClassDefinition) en.nextElement();
            if (cd.isAssignableFrom(cd2, 0, forClass)) {
                addStringLiteral(cd2.name(), cd2, true);
            }
        }
        markInitSystemErr();
    }

    void markInitSystemErr() {
        if (markInitSysErr) {
            ClassDefinition cd = get(Names.JAVA_LANG_VMTHREAD);
            if (cd.used()) {
                MethodDefinition md = cd.getMethod(Names.SIGN_SETSYSTEMOUT);
                if (md != null && md.used()) {
                    markInitSysErr = false;
                    cd.markMethod(Names.SIGN_INITSYSTEMERR);
                }
            }
        }
    }

    boolean isClassNameUsed(ClassDefinition ourClass) {
        if (exactGetNameClasses.contains(ourClass))
            return true;
        Enumeration en = usedGetNameClasses.elements();
        while (en.hasMoreElements()) {
            if (((ClassDefinition) en.nextElement()).isAssignableFrom(ourClass,
                    0, null))
                return true;
        }
        return false;
    }

    LiteralStr addStringLiteral(String str, ClassDefinition ourClass) {
        return addStringLiteral(str, ourClass, true);
    }

    private LiteralStr addStringLiteral(String str, ClassDefinition ourClass,
            boolean create) {
        LiteralStr liter = (LiteralStr) strpool.get(str);
        if (liter == null && create) {
            liter = new LiteralStr(str, ourClass);
            strpool.put(str, liter);
            ourClass.addLiteral(liter);
        }
        return liter;
    }

    String classNameStringOutput(String str, ClassDefinition ourClass,
            boolean create) {
        LiteralStr liter = addStringLiteral(str, ourClass, create);
        return liter != null ? liter.stringOutput() : LexTerm.NULL_STR;
    }

    ArrayLiteral addArrayLiteral(ArrayLiteral liter, ClassDefinition ourClass,
            boolean forString) {
        if (!liter.isWritable()) {
            ArrayLiteral liter2 = (ArrayLiteral) arrpool.get(liter);
            if (liter2 != null) {
                if (!liter.isNotSharable())
                    return liter2;
                if (!liter2.isNotSharable()) {
                    liter2.setNotSharable();
                    return liter2;
                }
            } else {
                arrpool.put(liter, liter);
                if (!forString) {
                    putCharSubArray(liter);
                }
            }
        }
        liter.initSuffix(ourClass);
        ourClass.addArrayLiteral(liter);
        return liter;
    }

    String addArrayTypeDefn(int s0, int count, ClassDefinition ourClass,
            String suffix) {
        String typeAndLen = (s0 < Type.VOID ? Type.cName[s0] + ", " : "(")
                + Integer.toString(count > 0 ? count : 1);
        String arrtypenum = (String) arrtypes.get(typeAndLen);
        if (arrtypenum == null) {
            if (suffix == null) {
                suffix = ourClass.nextLiteralSuffix();
            }
            arrtypes.put(typeAndLen, suffix);
            arrtypenum = suffix;
        }
        return "jcgo_arrtype" + arrtypenum;
    }

    void writeArrayTypeDefs(OutputContext oc) {
        if (arrtypes.size() != 0) {
            Enumeration en = arrtypes.keys();
            while (en.hasMoreElements()) {
                String typeAndLen = (String) en.nextElement();
                oc.hPrint("typedef ");
                oc.hPrint(typeAndLen.charAt(0) != '(' ? "JCGO_STATIC_ARRAY("
                        : "JCGO_STATIC_OBJARRAY");
                oc.hPrint(typeAndLen);
                oc.hPrint(") jcgo_arrtype");
                oc.hPrint((String) arrtypes.get(typeAndLen));
                oc.hPrint(";");
            }
            oc.hPrint("\n\n");
        }
    }

    String methodProxyStringOutput(MethodDefinition md) {
        MethodProxy mproxy = new MethodProxy(md);
        String proxysign = mproxy.csign();
        MethodProxy mproxy2 = (MethodProxy) mproxypool.get(proxysign);
        if (mproxy2 == null) {
            mproxypool.put(proxysign, mproxy);
            md.definingClass().addMethodProxy(mproxy);
            mproxy2 = mproxy;
        }
        return mproxy2.stringOutput();
    }

    void setStringValueType(ExpressionType exprType) {
        strValueType = exprType;
    }

    boolean allowPackedStr() {
        return strValueType != null
                && strValueType.objectSize() == Type.CLASSINTERFACE;
    }

    String getStringValueCastName() {
        Term.assertCond(strValueType != null);
        return strValueType.castName();
    }

    void putCharSubArray(ArrayLiteral liter) {
        SortedStrMap arrayStrs;
        if (liter.isCharArray()) {
            arrayStrs = charArrayStrs;
        } else {
            if (!allowPackedStr() || !liter.isByteArray())
                return;
            arrayStrs = byteArrayStrs;
        }
        int pos = 0;
        String data = liter.getData();
        do {
            String key = data.substring(pos);
            ArrayLiteral liter2 = (ArrayLiteral) arrayStrs.addStartsWith(key,
                    liter);
            if (liter2 != null && liter2.getData().length() >= data.length()) {
                arrayStrs.addStartsWith(key, liter2);
            }
            pos = liter.dataSkipComma(pos);
        } while (data.length() > pos);
    }

    ArrayLiteral searchCharSubArray(int[] ofsRef, ArrayLiteral liter,
            ClassDefinition ourClass) {
        ArrayLiteral liter2 = (ArrayLiteral) arrpool.get(liter);
        if (liter2 != null)
            return liter2;
        liter2 = (ArrayLiteral) (liter.isByteArray() ? byteArrayStrs
                : charArrayStrs).getValueStartsWith(liter.getData());
        if (liter2 != null) {
            int ofs = liter2.searchSubArray(liter);
            if (ofs >= 0) {
                ofsRef[0] = ofs;
                return liter2;
            }
        }
        return liter;
    }

    void producePassOne() {
        int cnt;
        do {
            cnt = 0;
            Enumeration en = allClasses.keys();
            while (en.hasMoreElements()) {
                cnt += ((ClassDefinition) allClasses.get(en.nextElement()))
                        .producePassOne();
            }
        } while (cnt > 0);
    }

    ExpressionType addProxyClass(ObjVector parmSig, ClassDefinition forClass) {
        String className = "$Proxy";
        if (!exists(className)) {
            get(className).defineProxyClass(new ObjVector(), forClass);
        }
        int count = parmSig.size();
        if (count > 0) {
            String pkgName = null;
            for (int i = 0; i < count; i++) {
                ExpressionType exprType = (ExpressionType) parmSig.elementAt(i);
                if (exprType.signatureDimensions() > 0)
                    return null;
                ClassDefinition cd = exprType.signatureClass();
                if (!cd.isInterface())
                    return null;
                if (!cd.isPublic()) {
                    if (pkgName == null) {
                        pkgName = cd.getPackageName();
                    } else if (!pkgName.equals(cd.getPackageName()))
                        return null;
                }
                for (int j = 0; j < i; j++) {
                    if (parmSig.elementAt(j) == cd)
                        return null;
                }
                className = className
                        + nameMapper.nameToProxyNamePart(cd.name());
            }
            if (pkgName != null && !pkgName.equals("package")) {
                className = pkgName + "." + className;
            }
            if (!exists(className)) {
                get(className).defineProxyClass(parmSig, forClass);
            }
        }
        return get(className).asExactClassType();
    }

    void writeClassInitCalls(OutputContext oc) {
        instancesCreatedOnTrace = new ObjHashSet();
        get(Names.JAVA_LANG_CLASS).instanceCreatedOnTrace();
        get(Names.JAVA_LANG_CLONEABLE).instanceCreatedOnTrace();
        get(Names.JAVA_IO_SERIALIZABLE).instanceCreatedOnTrace();
        get(Names.JAVA_LANG_STRING).instanceCreatedOnTrace();
        get(Names.JAVA_LANG_STRING).printClassInitGroup(oc);
        get(Names.JAVA_LANG_VMTHROWABLE).printClassInitGroup(oc);
        get(Names.JAVA_LANG_VMTHREAD).printClassInitGroup(oc);
        get(Names.JAVA_NIO_VMDIRECTBYTEBUFFER).printClassInitGroup(oc);
        get(Names.JAVA_LANG_REFLECT_VMFIELD).printClassInitGroup(oc);
        get(Names.JAVA_LANG_REFLECT_VMMETHOD).printClassInitGroup(oc);
        get(Names.JAVA_LANG_VMTHREAD_EXITMAIN).printClassInitGroup(oc);
        get(Names.JAVA_LANG_SYSTEM).printClassInitGroup(oc);
        get(Names.JAVA_LANG_CLASSLOADER_STATICDATA).printClassInitGroup(oc);
        Enumeration en = usedClasses();
        while (en.hasMoreElements()) {
            ((ClassDefinition) en.nextElement()).printClassInitGroup(oc);
        }
    }

    void addDynClassToTrace(ClassDefinition cd) {
        if (cd.objectSize() == Type.CLASSINTERFACE && !cd.isNotInstantated()) {
            dynClassesToTrace.put(cd, cd);
        }
    }

    void writeInternedLiterals(OutputContext oc) {
        if (get(Names.JAVA_LANG_VMSTRING).used()) {
            int len = strpool.size();
            oc.cPrint("JCGO_NOSEP_DATA CONST JCGO_STATIC_OBJARRAY(");
            oc.cPrint(Integer.toString(len > 0 ? len : 1));
            oc.cPrint(") jcgo_internStrs={(jvtable)&");
            oc.cPrint(classTable[Type.OBJECTARRAY].vTableCName());
            oc.cPrint(",\010");
            oc.cPrint("JCGO_MON_INIT\010");
            oc.cPrint(Integer.toString(len));
            oc.cPrint(",\010");
            oc.cPrint(get(Names.JAVA_LANG_STRING).getClassRefStr(false));
            oc.cPrint(",\010");
            oc.cPrint("{");
            if (len > 0) {
                Enumeration en = strpool.keys();
                boolean next = false;
                while (en.hasMoreElements()) {
                    if (next) {
                        oc.cPrint(", ");
                    }
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
                    oc.cPrint(")");
                    oc.cPrint(((LiteralStr) strpool.get(en.nextElement()))
                            .stringOutput());
                    next = true;
                }
            } else {
                oc.cPrint(LexTerm.NULL_STR);
            }
            oc.cPrint("}};\n\n");
        }
    }
}
