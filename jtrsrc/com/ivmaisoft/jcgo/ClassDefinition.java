/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ClassDefinition.java --
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
 * Class type definition.
 */

final class ClassDefinition extends ExpressionType {

    static final int ARR_STORE_EXC = 0x1;
    static final int CLASS_CAST_EXC = 0x2;
    static final int INDEX_OUT_EXC = 0x4;
    static final int NULL_PTR_EXC = 0x8;

    static final int MAX_DIMS = (new ConstValue(16)).getIntValue();

    private int type = Type.CLASSINTERFACE;

    private String name;

    private String cname;

    private String jniname;

    private String id;

    private String shortname;

    private String signName;

    private ClassDefinition superClass;

    private int modifiers;

    private final ObjVector arrayClasses = new ObjVector();

    private ExactClassType exactClassType;

    private Context context;

    private OutputContext outputContext;

    private boolean predefining;

    private boolean parsed;

    private boolean isDefined;

    private boolean used;

    private boolean forArraysUsed;

    private boolean vTableUsed;

    private boolean headerWritten;

    private boolean addedToHeader;

    private boolean finished;

    private boolean hasInstances;

    private boolean hasExactInstance;

    private boolean addedToSuper;

    private boolean insideStaticMethod;

    private boolean needStaticInitPass;

    private boolean markIfDefined;

    private boolean markIfHasInstances;

    private boolean markIfImplemented;

    private boolean checkMarkDone;

    private final ObjQueue subclasses = new ObjQueue();

    private ObjQueue implementedBy;

    private Term extendsTerm;

    private Term ifaceListTerm;

    private Term outerMethodDefnTerm;

    private ClassDefinition outerClass;

    private VariableDefinition outerThisRef;

    private LeftBrace outerScope;

    private ObjVector outerLocals;

    private ObjHashtable outerFields;

    private Term constrParamList;

    private ExpressionType constrSuperType;

    private final ObjQueue interfaceClasses = new ObjQueue();

    private final ObjQueue specifiedInterfaces = new ObjQueue();

    private Term classbody;

    private OrderedMap fieldDictionary;

    private OrderedMap methodDictionary;

    private OrderedMap usedMethodSigns;

    private boolean mayContainClinit;

    private InitializerPart classInitializers;

    private InitializerPart instanceInitializers;

    private boolean leaksDiscoverProcessing;

    private boolean leaksDiscoverDone;

    private boolean hasInstanceInitLeaks;

    private boolean isInitThisStackObjVolatile;

    private MethodDefinition basicConstructor;

    private ClassDefinition fromClass;

    private int dummyEntryCnt;

    private int literNum;

    private int scopesNum = 1;

    private int anonymousNum;

    private ObjVector methodTableSigns;

    private ObjQueue arrpool;

    private ObjQueue strpool;

    private MethodTraceInfo clinitTraceInfo;

    private OrderedMap classInitDepend;

    private ObjHashSet fieldCNames;

    private ObjHashSet reflectedFieldNames;

    private ObjHashtable reflectedMethods;

    private ObjQueue inheritedReflectedSigns;

    private ObjQueue inheritedReflFieldNames;

    private ObjQueue mproxypool;

    private MethodDefinition clinitForStaticField;

    private ClassDefinition helperClass;

    private VariableDefinition lastObjectRefField;

    private OrderedMap inclassCalls;

    ObjHashtable knownMethodInfos;

    int nextClinitLabel;

    int nextInitZLabel;

    private boolean definePassOneDone;

    ClassDefinition(String name) {
        this.name = name;
        NameMapper nameMapper = Main.dict.nameMapper;
        cname = nameMapper.classToCName(name);
        jniname = nameMapper.nameToJniName(name);
        signName = Type.sig[Type.CLASSINTERFACE] + nameMapper.classToSign(name);
        shortname = nameMapper.cnameToShort(cname);
        outputContext = new OutputContext(shortname);
        fieldCNames = new ObjHashSet();
    }

    ClassDefinition(int arrtype) {
        name = Type.sig[arrtype];
        cname = Type.cName[arrtype];
        used = true;
        hasInstances = true;
        type = arrtype;
        vTableUsed = true;
    }

    ClassDefinition(int type, Context c) {
        this.type = type;
        name = Type.name[type];
        cname = Type.cName[type];
        signName = Type.sig[type];
        used = true;
        hasInstances = true;
        vTableUsed = true;
        definePass0(c, AccModifier.PUBLIC | AccModifier.FINAL, name,
                Empty.newTerm(), Empty.newTerm(), Empty.newTerm(), false);
    }

    ClassDefinition signatureClass() {
        return this;
    }

    int signatureDimensions() {
        return 0;
    }

    int objectSize() {
        return type;
    }

    ExpressionType indirectedType() {
        return null;
    }

    ClassDefinition receiverClass() {
        return this;
    }

    Context passOneContext() {
        predefineClass(null);
        Term.assertCond(context != null);
        return context;
    }

    String vTableCName() {
        Term.assertCond(used && vTableUsed);
        return cname + "_methods";
    }

    String cNewObjectCode() {
        return "(" + castName() + ")jcgo_newObject((jvtable)&" + vTableCName()
                + ")";
    }

    static String arrayVTableCName(int type, int dims) {
        Term.assertCond((type == Type.VOID && dims >= 0)
                || (type >= Type.BOOLEAN && type <= Type.DOUBLE && dims == 0));
        return Type.cName[Type.CLASSINTERFACE + type]
                + (dims > 0 ? Integer.toString(dims + 1) : "") + "_methods";
    }

    String routineNameOf(String csign) {
        Term.assertCond(used);
        return cname + "__" + csign;
    }

    String csign() {
        Term.assertCond(signName != null);
        return signName;
    }

    String jniClassName() {
        Term.assertCond(jniname != null);
        return jniname;
    }

    ExpressionType asExprType(int dims) {
        if (dims == 0)
            return this;
        for (int cnt = dims - arrayClasses.size(); cnt > 0; cnt--) {
            arrayClasses.addElement(null);
        }
        ExpressionType exprType = (ExpressionType) arrayClasses
                .elementAt(dims - 1);
        if (exprType == null) {
            exprType = new ClassDefnWithDims(this, dims);
            arrayClasses.setElementAt(exprType, dims - 1);
        }
        return exprType;
    }

    ExpressionType asExactClassType() {
        if (isFinal())
            return this;
        if (exactClassType == null) {
            exactClassType = new ExactClassType(this);
        }
        return exactClassType;
    }

    ExpressionType mapToPrimType() {
        if (name.equals(Names.JAVA_LANG_BOOLEAN))
            return Main.dict.classTable[Type.BOOLEAN];
        if (name.equals(Names.JAVA_LANG_BYTE))
            return Main.dict.classTable[Type.BYTE];
        if (name.equals(Names.JAVA_LANG_CHARACTER))
            return Main.dict.classTable[Type.CHAR];
        if (name.equals(Names.JAVA_LANG_SHORT))
            return Main.dict.classTable[Type.SHORT];
        if (name.equals(Names.JAVA_LANG_INTEGER))
            return Main.dict.classTable[Type.INT];
        if (name.equals(Names.JAVA_LANG_LONG))
            return Main.dict.classTable[Type.LONG];
        if (name.equals(Names.JAVA_LANG_FLOAT))
            return Main.dict.classTable[Type.FLOAT];
        if (name.equals(Names.JAVA_LANG_DOUBLE))
            return Main.dict.classTable[Type.DOUBLE];
        return this;
    }

    boolean hasInstantatedSubclasses(boolean allowArrays) {
        define(null);
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if ((cd.hasInstances || (allowArrays && (cd.forArraysUsed || cd.vTableUsed)))
                        && cd.used)
                    return true;
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            if ((cd.hasInstances || (allowArrays && (cd.forArraysUsed || cd.vTableUsed)))
                    && cd.used)
                return true;
        }
        return false;
    }

    int getSubclassDepth(ClassDefinition aclass, ClassDefinition forClass) {
        if (aclass != this) {
            int depth = 0;
            ClassDefinition cd = this;
            do {
                depth++;
                cd = cd.superClass(forClass);
                if (cd == null)
                    break;
                if (cd == aclass)
                    return depth;
            } while (true);
        }
        return 0;
    }

    int getImplementedByDepth(ClassDefinition aclass, ClassDefinition forClass) {
        int depth = 0;
        while (aclass != null) {
            depth++;
            if (aclass.doesImplement(this, forClass))
                return depth;
            aclass = aclass.superClass();
        }
        return 0;
    }

    boolean doesImplement(ClassDefinition anInterface, ClassDefinition forClass) {
        return interfaceClasses(forClass).contains(anInterface);
    }

    boolean isAssignableFrom(ClassDefinition cd, int dimsDiff,
            ClassDefinition forClass) {
        if (dimsDiff != 0)
            return dimsDiff < 0 && isObjectOrCloneable();
        if (cd == this)
            return true;
        if (cd.signName == null)
            return isObjectOrCloneable();
        do {
            if (cd.doesImplement(this, forClass)
                    || (cd = cd.superClass(forClass)) == this)
                return true;
        } while (cd != null);
        return false;
    }

    static boolean isAssignableFrom(ExpressionType exprType1,
            ExpressionType exprType2, ClassDefinition forClass) {
        return exprType1 == exprType2
                || exprType2.objectSize() == Type.NULLREF
                || exprType1.signatureClass().isAssignableFrom(
                        exprType2.signatureClass(),
                        exprType1.signatureDimensions()
                                - exprType2.signatureDimensions(), forClass);
    }

    static ClassDefinition maxSuperclassOf(ClassDefinition cd1,
            ClassDefinition cd2, ClassDefinition forClass) {
        if (cd1 != cd2) {
            if (cd2.type != Type.CLASSINTERFACE)
                return cd1;
            if (cd1.type != Type.CLASSINTERFACE)
                return cd2;
            if (cd1.isInterface() && cd2.isInterface()) {
                if (cd1.doesImplement(cd2, forClass))
                    return cd2;
                if (cd2.doesImplement(cd1, forClass))
                    return cd1;
                Enumeration en = cd1.specifiedInterfaces.elements();
                while (en.hasMoreElements()) {
                    ClassDefinition iface1 = (ClassDefinition) en.nextElement();
                    Enumeration en2 = cd2.specifiedInterfaces.elements();
                    while (en2.hasMoreElements()) {
                        ClassDefinition cd = maxSuperclassOf(iface1,
                                (ClassDefinition) en2.nextElement(), forClass);
                        if (cd.superClass(forClass) != null)
                            return cd;
                    }
                }
                cd1 = cd1.superClass(null);
            } else {
                do {
                    if (cd1.isAssignableFrom(cd2, 0, forClass))
                        break;
                    if (cd2.isAssignableFrom(cd1, 0, forClass)) {
                        cd1 = cd2;
                        break;
                    }
                    cd1 = cd1.superClass(forClass);
                    cd2 = cd2.superClass(forClass);
                } while (cd1 != cd2);
            }
        }
        return cd1;
    }

    static ExpressionType maxCommonExprOf(ExpressionType exprType1,
            ExpressionType exprType2, ClassDefinition forClass) {
        if (exprType1 == exprType2 || exprType2.objectSize() == Type.NULLREF)
            return exprType1;
        if (exprType1.objectSize() == Type.NULLREF)
            return exprType2;
        int dims1 = exprType1.signatureDimensions();
        int dims2 = exprType2.signatureDimensions();
        return (dims1 != dims2 ? Main.dict.get(Names.JAVA_LANG_OBJECT)
                : maxSuperclassOf(exprType1.signatureClass(),
                        exprType2.signatureClass(), forClass))
                .asExprType(dims1 < dims2 ? dims1 : dims2);
    }

    boolean isStringOrNull() {
        return type == Type.NULLREF || Names.JAVA_LANG_STRING.equals(name);
    }

    boolean isThrowable() {
        ClassDefinition cd = this;
        while (cd.superClass() != null) {
            if (cd.name.equals(Names.JAVA_LANG_THROWABLE))
                return true;
            cd = cd.superClass;
        }
        return false;
    }

    boolean isUncheckedException(ClassDefinition forClass) {
        ClassDefinition cd = this;
        while (cd.superClass(forClass) != null) {
            if (cd.name.equals(Names.JAVA_LANG_ERROR)
                    || cd.name.equals(Names.JAVA_LANG_RUNTIMEEXCEPTION))
                return true;
            cd = cd.superClass;
        }
        return false;
    }

    int getVMExcMask() {
        return name.equals(Names.JAVA_LANG_THROWABLE)
                || name.equals(Names.JAVA_LANG_EXCEPTION)
                || name.equals(Names.JAVA_LANG_RUNTIMEEXCEPTION) ? ARR_STORE_EXC
                | CLASS_CAST_EXC | INDEX_OUT_EXC | NULL_PTR_EXC
                : name.equals(Names.JAVA_LANG_ARRAYSTOREEXCEPTION) ? ARR_STORE_EXC
                        : name.equals(Names.JAVA_LANG_CLASSCASTEXCEPTION) ? CLASS_CAST_EXC
                                : name.equals(Names.JAVA_LANG_INDEXOUTOFBOUNDSEXCEPTION)
                                        || name.equals(Names.JAVA_LANG_ARRAYINDEXOUTOFBOUNDSEXCEPTION) ? INDEX_OUT_EXC
                                        : name.equals(Names.JAVA_LANG_NULLPOINTEREXCEPTION) ? NULL_PTR_EXC
                                                : 0;
    }

    String getJniName() {
        return type != Type.CLASSINTERFACE ? Type.jniName[type] : name
                .equals(Names.JAVA_LANG_CLASS) ? "jclass" : name
                .equals(Names.JAVA_LANG_STRING) ? "jstring" : !isInterface()
                && isThrowable() ? "jthrowable"
                : Type.jniName[Type.CLASSINTERFACE];
    }

    boolean isInterface() {
        predefineClassNoMark();
        return implementedBy != null;
    }

    boolean isPublic() {
        predefineClassNoMark();
        return (modifiers & AccModifier.PUBLIC) != 0;
    }

    boolean isStrictFP() {
        predefineClassNoMark();
        return (modifiers & AccModifier.STRICT) != 0;
    }

    boolean isAbstractOrInterface() {
        predefineClassNoMark();
        return (modifiers & (AccModifier.ABSTRACT | AccModifier.INTERFACE)) != 0;
    }

    boolean isNotInstantated() {
        return isAbstractOrInterface() || !hasInstances;
    }

    boolean hasInstances() {
        return hasInstances;
    }

    boolean hasRealInstances() {
        return hasInstances && used;
    }

    void setHasInstances() {
        if (!hasInstances) {
            if (markIfHasInstances) {
                markUsed();
            }
            hasInstances = true;
            checkMethodsMarkedUsed();
            if (used) {
                markFieldInitializers(false);
            }
            Enumeration en2 = specifiedInterfaces.elements();
            while (en2.hasMoreElements()) {
                ((ClassDefinition) en2.nextElement()).setHasInstances();
            }
            if (superClass != null) {
                superClass.setHasInstances();
            }
            setVTableUsed(false);
        }
    }

    boolean hasExactInstance() {
        return hasExactInstance;
    }

    void setHasExactInstance(boolean value) {
        if (!hasExactInstance && !isAbstractOrInterface()) {
            hasExactInstance = value;
            ClassDefinition cd = this;
            do {
                cd.checkMethodsMarkedUsed();
            } while ((cd = cd.superClass) != null);
        }
    }

    private void checkMethodsMarkedUsed() {
        Enumeration en = methodDictionary().keys();
        while (en.hasMoreElements()) {
            getMethodNoInheritance((String) en.nextElement()).checkMarkedUsed();
        }
    }

    boolean overriddenByAllUsed(MethodDefinition md) {
        String sigString = md.methodSignature().signatureString();
        String pkgName = md.isProtectedOrPublic() ? null : getPackageName();
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            if (cd.used || cd.hasExactInstance) {
                MethodDefinition md2 = cd.getMethodNoInheritance(sigString);
                if (md2 != null ? md2.isClassMethod()
                        || (pkgName != null && !pkgName.equals(cd
                                .getPackageName())) : cd.hasExactInstance
                        || !cd.overriddenByAllUsed(md))
                    return false;
            }
        }
        return true;
    }

    void setVTableUsed(boolean markClassUsed) {
        if (markClassUsed) {
            markUsed();
        }
        if (!vTableUsed) {
            Term.assertCond(!finished);
            vTableUsed = true;
            ClassDefinition sc = superClass();
            if (sc != null && !sc.vTableUsed && !isInterface()) {
                do {
                    Term.assertCond(!sc.finished);
                    sc.vTableUsed = true;
                } while ((sc = sc.superClass(this)) != null && !sc.vTableUsed);
            }
        }
    }

    boolean isStaticClass() {
        predefineClassNoMark();
        return (modifiers & AccModifier.STATIC) != 0 || outerClass == null
                || insideStaticMethod;
    }

    boolean isFinal() {
        predefineClassNoMark();
        return (modifiers & AccModifier.FINAL) != 0;
    }

    ClassDefinition outerClass() {
        predefineClassNoMark();
        return outerClass;
    }

    private ClassDefinition topOuterClass() {
        ClassDefinition cd = this;
        ClassDefinition aclass;
        while ((aclass = cd.outerClass()) != null) {
            cd = aclass;
        }
        return cd;
    }

    LeftBrace outerScope() {
        predefineClassNoMark();
        return outerScope;
    }

    ObjVector outerLocals(ClassDefinition forClass) {
        predefineClass(forClass);
        Term.assertCond(outerLocals != null);
        return outerLocals;
    }

    VariableDefinition outerThisRef() {
        define(null);
        return outerThisRef;
    }

    boolean hasConstrSuperExpr() {
        return constrSuperType != null;
    }

    void setConstrSuperExpr(ExpressionType exprType0, MethodDefinition md,
            int skipHeadCnt, int skipTailCnt) {
        constrSuperType = exprType0;
        constrParamList = md.copyParamList(skipHeadCnt, skipTailCnt);
    }

    Term constrMakeArgumentList() {
        Term.assertCond(constrParamList != null);
        return constrParamList.makeArgumentList();
    }

    MethodDefinition clinitForStaticField() {
        if (clinitForStaticField == null) {
            clinitForStaticField = new MethodDefinition(this);
        }
        return clinitForStaticField;
    }

    String nextLiteralSuffix() {
        return Integer.toString(++literNum) + "_" + shortname();
    }

    String nextAnonymousId() {
        return Integer.toString(++anonymousNum);
    }

    String nextLocalClassName(String ident) {
        ClassDefinition cd = topOuterClass();
        String nextName;
        do {
            nextName = cd.name + "$" + Integer.toString(cd.scopesNum) + ident;
            if (!Main.dict.exists(nextName))
                break;
            cd.scopesNum++;
        } while (true);
        return nextName;
    }

    String resolveInnerClass(String className, boolean checkFields,
            ClassDefinition forClass) {
        int i = className.lastIndexOf('.');
        ClassDefinition aclass = this;
        if (i >= 0) {
            String qualified = resolveInnerClass(className.substring(0, i),
                    checkFields, forClass);
            if (qualified == null)
                return null;
            aclass = Main.dict.get(qualified);
        }
        String fieldName = className.substring(i + 1);
        String innerName = "$" + fieldName;
        do {
            String qualified = aclass.name + innerName;
            aclass.predefineClassNoMark();
            if (Main.dict.exists(qualified))
                return qualified;
            if (checkFields) {
                aclass.define(forClass);
                if (aclass.fieldDictionary.get(fieldName) != null)
                    return "";
                Enumeration en = aclass.interfaceClasses.elements();
                while (en.hasMoreElements()) {
                    ClassDefinition cd = (ClassDefinition) en.nextElement();
                    cd.define(forClass);
                    if (cd.fieldDictionary.get(fieldName) != null)
                        return "";
                }
            }
            Enumeration en = aclass.interfaceClasses(forClass).elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                qualified = cd.name + innerName;
                cd.predefineClassNoMark();
                if (Main.dict.exists(qualified))
                    return qualified;
            }
        } while ((aclass = aclass.superClass()) != null);
        return null;
    }

    String name() {
        return name;
    }

    String getPackageName() {
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(0, i) : "package";
    }

    String id() {
        return id;
    }

    String castName() {
        ClassDefinition cd = this;
        do {
            if (cd.used)
                return cd.cname;
        } while ((cd = cd.superClass) != null);
        return Main.dict.get(Names.JAVA_LANG_OBJECT).cname;
    }

    String getClassRefStr(boolean isArray) {
        Term.assertCond(used);
        setVTableUsed(false);
        return type != Type.CLASSINTERFACE ? "JCGO_CORECLASS_FOR(OBJT_"
                + (isArray ? "jarray+OBJT_" : "") + cname + ")"
                : "JCGO_CLASSREF_OF(" + cname + "__class)";
    }

    private String clinitCName() {
        Term.assertCond(used);
        return cname + "__class__0";
    }

    boolean isObjectOrCloneable() {
        return name.equals(Names.JAVA_LANG_OBJECT)
                || name.equals(Names.JAVA_LANG_CLONEABLE)
                || name.equals(Names.JAVA_IO_SERIALIZABLE);
    }

    private void defineObjectStructure(OutputContext oc) {
        Term.assertCond(used);
        if (superClass != null && !Names.JAVA_LANG_CLASS.equals(name)
                && !Names.JAVA_LANG_STRING.equals(name)
                && !Names.JAVA_LANG_THROWABLE.equals(name)) {
            oc.hPrint("typedef struct ");
            oc.hPrint(cname);
            oc.hPrint("_s *");
            oc.hPrint(cname);
            oc.hPrint(";");
        }
    }

    String shortname() {
        Term.assertCond(shortname != null);
        return shortname;
    }

    private int enumerateClass(OutputContext oc, int n, ObjHashSet processed) {
        if (type == Type.CLASSINTERFACE && processed.add(this)) {
            if (used) {
                if (!name.equals(Names.JAVA_LANG_CLASS)
                        && !name.equals(Names.JAVA_LANG_STRING)) {
                    if (superClass == null) {
                        oc.addIncludeCFile(Main.dict
                                .get(Names.JAVA_LANG_STRING).shortname());
                        oc.addIncludeCFile(Main.dict.get(Names.JAVA_LANG_CLASS)
                                .shortname());
                    }
                    oc.addIncludeCFile(shortname());
                }
                defineObjectStructure(oc);
                oc.hPrint("#define OBJT_");
                oc.hPrint(cname);
                oc.hPrint(" ");
                oc.hPrint(Integer.toString(++n));
                oc.hPrint("\010");
                if (superClass == null) {
                    n = Main.dict.get(Names.JAVA_LANG_STRING).enumerateClass(
                            oc,
                            Main.dict.get(Names.JAVA_LANG_CLASS)
                                    .enumerateClass(oc, 63, processed),
                            processed);
                }
            }
            Enumeration en = subclasses.elements();
            while (en.hasMoreElements()) {
                n = ((ClassDefinition) en.nextElement()).enumerateClass(oc, n,
                        processed);
            }
            if (used) {
                oc.hPrint("#define MAXT_");
                oc.hPrint(cname);
                oc.hPrint(" ");
                oc.hPrint(Integer.toString(n));
                oc.hPrint("\010");
            }
        }
        return n;
    }

    void produceOutputRecursive(ObjHashSet processed) {
        if (type == Type.CLASSINTERFACE && processed.add(this)) {
            if (used && !name.equals(Names.JAVA_LANG_CLASS)
                    && !name.equals(Names.JAVA_LANG_STRING)) {
                if (superClass == null) {
                    Main.dict.get(Names.JAVA_LANG_STRING).produceOutput();
                    Main.dict.get(Names.JAVA_LANG_CLASS).produceOutput();
                }
                produceOutput();
            }
            Enumeration en = subclasses.elements();
            while (en.hasMoreElements()) {
                ((ClassDefinition) en.nextElement())
                        .produceOutputRecursive(processed);
            }
        }
    }

    private static String[] sortStrings(String[] arr, int size) {
        String[] sorted = new String[size];
        System.arraycopy(arr, 0, sorted, 0, size);
        mergeSort(arr, sorted, 0, size);
        return sorted;
    }

    static void mergeSort(String[] src, String[] dest, int low, int high) {
        int len = high - low;
        if (len < 7) {
            int i = low;
            while (++i < high) {
                for (int j = i; j > low && dest[j - 1].compareTo(dest[j]) > 0; j--) {
                    String s = dest[j];
                    dest[j] = dest[j - 1];
                    dest[j - 1] = s;
                }
            }
        } else {
            int mid = (low + high) >>> 1;
            mergeSort(dest, src, low, mid);
            mergeSort(dest, src, mid, high);
            if (src[mid - 1].compareTo(src[mid]) <= 0) {
                System.arraycopy(src, low, dest, low, len);
            } else {
                int p = low;
                int q = mid;
                for (int i = low; i < high; i++) {
                    dest[i] = q < high
                            && (p >= mid || src[p].compareTo(src[q]) > 0) ? src[q++]
                            : src[p++];
                }
            }
        }
    }

    private void createSubclassList(ObjHashSet classnames) {
        if (type == Type.CLASSINTERFACE) {
            if (used && !classnames.add(name))
                return;
            Enumeration en = subclasses.elements();
            while (en.hasMoreElements()) {
                ((ClassDefinition) en.nextElement())
                        .createSubclassList(classnames);
            }
        }
    }

    private void addToHeaderFile(OutputContext oc) {
        if (type == Type.CLASSINTERFACE && !addedToHeader) {
            addedToHeader = true;
            if (used && !name.equals(Names.JAVA_LANG_CLASS)
                    && !name.equals(Names.JAVA_LANG_STRING)) {
                if (superClass == null) {
                    oc.addIncludeHFile(Main.dict.get(Names.JAVA_LANG_STRING)
                            .shortname());
                }
                oc.addIncludeHFile(shortname());
                if (superClass == null) {
                    oc.addIncludeHFile(Main.dict.get(Names.JAVA_LANG_CLASS)
                            .shortname());
                }
            }
            Enumeration en = subclasses.elements();
            while (en.hasMoreElements()) {
                ((ClassDefinition) en.nextElement()).addToHeaderFile(oc);
            }
        }
    }

    void defineProxyClass(ObjVector interfaces, ClassDefinition forClass) {
        ClassDefinition sc = Main.dict.get(Names.JAVA_LANG_REFLECT_PROXY);
        sc.define(forClass);
        int count = interfaces.size();
        int i;
        for (i = 0; i < count; i++) {
            ClassDefinition cd = (ClassDefinition) interfaces.elementAt(i);
            cd.define(forClass);
            cd.reflectMethods(null, false, null, false);
            if (i > 0) {
                Enumeration en = cd.methodDictionary().keys();
                while (en.hasMoreElements()) {
                    String sigString = (String) en.nextElement();
                    ExpressionType minType = cd.getMethodNoInheritance(
                            sigString).exprType();
                    ExpressionType maxType = minType;
                    for (int j = 0; j < i; j++) {
                        MethodDefinition md = ((ClassDefinition) interfaces
                                .elementAt(j)).getMethod(sigString);
                        if (md != null) {
                            ExpressionType resType = md.exprType();
                            if (isAssignableFrom(maxType, resType, forClass)) {
                                maxType = resType;
                            } else if (isAssignableFrom(resType, minType,
                                    forClass)) {
                                minType = resType;
                            } else if (maxType == minType
                                    || !isAssignableFrom(minType, resType,
                                            forClass)
                                    || !isAssignableFrom(resType, maxType,
                                            forClass))
                                return;
                        }
                    }
                }
            }
        }
        Term ifaceListTerm = Empty.newTerm();
        if (count > 0) {
            i = count - 1;
            ifaceListTerm = new ClassOrIfaceType(
                    (ClassDefinition) interfaces.elementAt(i));
            while (i-- > 0) {
                ifaceListTerm = new Seq(new ClassOrIfaceType(
                        (ClassDefinition) interfaces.elementAt(i)),
                        ifaceListTerm);
            }
        }
        definePass0(
                new Context(),
                AccModifier.PUBLIC | AccModifier.FINAL,
                (i = name.lastIndexOf('.')) >= 0 ? name.substring(i + 1) : name,
                new ClassOrIfaceType(sc), ifaceListTerm, Empty.newTerm(), false);
        define(forClass);
        markUsed();
        Enumeration en = methodDictionary.keys();
        while (en.hasMoreElements()) {
            String sigString = (String) en.nextElement();
            MethodDefinition md = getMethodNoInheritance(sigString);
            if (!md.isConstructor()) {
                MethodDefinition md2 = getOverridenMethod(sigString,
                        md.exprType());
                Term.assertCond(md2 != null);
                if (!md.hasSameThrows(md2)) {
                    reflectMethodInner(md);
                }
            }
        }
    }

    MethodDefinition getOverridenMethod(String sigString, ExpressionType resType) {
        ClassDefinition cd = superClass();
        MethodDefinition md;
        if (cd != null && (md = cd.getMethod(sigString)) != null
                && md.exprType() == resType)
            return md;
        Enumeration en = interfaceClasses(null).elements();
        while (en.hasMoreElements()) {
            md = ((ClassDefinition) en.nextElement())
                    .getMethodNoInheritance(sigString);
            if (md != null && md.exprType() == resType)
                return md;
        }
        return null;
    }

    void setMayContainClinit() {
        mayContainClinit = true;
    }

    boolean isProxyClass() {
        return !classbody.notEmpty() && isFinal() && id.startsWith("$Proxy")
                && superClass == Main.dict.get(Names.JAVA_LANG_REFLECT_PROXY);
    }

    boolean defineDynamic(ClassDefinition forClass) {
        if (!isDefined) {
            Main.dict.message("Trying dynamic class: " + name);
            try {
                predefineClass(forClass);
            } catch (TranslateException e) {
                if (isDefined)
                    throw e;
                Main.dict.message("Bad file ignored for class: " + name);
                return false;
            }
            if (outerMethodDefnTerm != null) {
                Term.assertCond(outerClass != null);
                outerClass.define(this);
                MethodDefinition md = outerMethodDefnTerm.superMethodCall();
                Term.assertCond(md != null);
                md.markUsedThisOnly();
                md.producePassOne(null);
            }
            define(forClass);
        }
        markUsed();
        return true;
    }

    int define(ClassDefinition forClass) {
        if (isDefined)
            return -1;
        if (forClass == this) {
            forClass = null;
        }
        predefineClass(forClass);
        processExtends();
        if (superClass != null) {
            superClass.define(this);
        }
        Enumeration en = ifaceListTerm.getSignature().elements();
        while (en.hasMoreElements()) {
            ((ClassDefinition) en.nextElement()).predefineClass(this);
        }
        if (isDefined)
            return 1;
        processSuperClasses();
        if (isDefined)
            return 1;
        isDefined = true;
        ifaceListTerm.addFieldsTo(this);
        definePass1(forClass);
        int marked = 0;
        if (used) {
            used = false;
            markUsed();
            marked = 1;
        }
        if (outerClass != null) {
            outerClass.predefineClass(this);
        }
        return marked;
    }

    private boolean checkMarkIfDefined() {
        boolean marked = false;
        if (!checkMarkDone) {
            checkMarkDone = true;
            if (name.equals(Names.JAVA_LANG_CLASS)
                    || name.equals(Names.JAVA_LANG_STRING)) {
                markUsed();
                setHasInstances();
                marked = true;
            }
            inheritMarkIfDefined();
            String flags = (String) Main.dict.forcedReflectClassOrPkg
                    .remove(name);
            if (flags != null
                    || (outerMethodDefnTerm == null
                            && (flags = pkgNeedsForcedReflection(name)) != null && !isProxyClass())) {
                forceReflection(flags);
                if (used) {
                    marked = true;
                }
            }
        }
        return marked;
    }

    private static String pkgNeedsForcedReflection(String name) {
        int pos = name.lastIndexOf('.');
        ObjHashtable forcedReflectClassOrPkg = Main.dict.forcedReflectClassOrPkg;
        if (pos < 0)
            return (String) forcedReflectClassOrPkg.get("");
        String pkgName = name.substring(0, pos);
        String pkgDotStar = pkgName + ".*";
        String flags = (String) forcedReflectClassOrPkg.get(pkgDotStar);
        if (flags == null
                && (flags = (String) forcedReflectClassOrPkg.remove(pkgName)) != null) {
            forcedReflectClassOrPkg.put(pkgDotStar, flags);
        }
        return flags;
    }

    private void forceReflection(String flags) {
        int reflectCtors = 0;
        int reflectMethods = 0;
        int reflectFields = 0;
        if (flags.length() > 0) {
            if (flags.equals("p")) {
                reflectCtors = 1;
                reflectMethods = 1;
                reflectFields = 1;
            } else {
                int pos;
                if ((pos = flags.indexOf('c')) < 0) {
                    reflectCtors = -1;
                } else if (pos > 0 && flags.charAt(pos - 1) == 'p') {
                    reflectCtors = 1;
                }
                if ((pos = flags.indexOf('m')) < 0) {
                    reflectMethods = flags.indexOf('g') < 0 ? -1 : 2;
                } else if (pos > 0 && flags.charAt(pos - 1) == 'p') {
                    reflectMethods = 1;
                }
                if ((pos = flags.indexOf('f')) < 0) {
                    reflectFields = -1;
                } else if (pos > 0 && flags.charAt(pos - 1) == 'p') {
                    reflectFields = 1;
                }
            }
        }
        if (reflectFields >= 0) {
            Enumeration en = fieldDictionary().keys();
            while (en.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) fieldDictionary
                        .get(en.nextElement());
                if (reflectFields == 0 || v.isPublic()) {
                    reflectFieldInner(v);
                }
            }
        }
        if ((reflectCtors & reflectMethods) >= 0) {
            Enumeration en = methodDictionary().keys();
            while (en.hasMoreElements()) {
                MethodDefinition md = getMethodNoInheritance((String) en
                        .nextElement());
                if (md.isConstructor() ? reflectCtors >= 0
                        && (reflectCtors == 0 || md.isPublic())
                        : reflectMethods >= 0
                                && md.definingClass() == this
                                && (reflectMethods == 0 || (md.isPublic() && (reflectMethods == 1 || md
                                        .isGetterSetter()))))
                    reflectMethodInner(md);
            }
        }
    }

    void predefineClassNoMark() {
        if (!parsed && type == Type.CLASSINTERFACE) {
            Main.parseJavaFile(name);
            if (!parsed)
                throw new TranslateException(
                        "File bad for class: "
                                + name
                                + (name.indexOf('.', 0) < 0 ? " (unnamed package)"
                                        : ""));
        }
    }

    void predefineClass(ClassDefinition forClass) {
        predefining = true;
        if (fromClass == null && forClass != this
                && type == Type.CLASSINTERFACE) {
            fromClass = forClass;
        }
        predefineClassNoMark();
    }

    void definePass0(Context c, int modifiers, String id, Term extendsTerm,
            Term ifaceListTerm, Term classbody, boolean isInterface) {
        Term.assertCond(c != null);
        if (parsed) {
            classbody.fatalError(c, "Class duplicate or recursive definition: "
                    + name);
            return;
        }
        parsed = true;
        context = c;
        if (type == Type.CLASSINTERFACE) {
            outerMethodDefnTerm = c.passZeroMethodDefnTerm;
            outerClass = c.currentClass;
            context = c.cloneForClass(this, this);
        } else {
            isDefined = true;
            if (type >= Type.CLASSINTERFACE) {
                superClass = Main.dict.get(Names.JAVA_LANG_OBJECT);
                definePassOneDone = true;
            }
        }
        if (isInterface) {
            implementedBy = new ObjQueue();
            modifiers |= AccModifier.INTERFACE;
            if (outerClass != null) {
                modifiers |= AccModifier.STATIC;
            }
        } else if (outerClass != null && outerClass.implementedBy != null) {
            modifiers |= AccModifier.PUBLIC | AccModifier.STATIC;
        }
        this.modifiers = modifiers;
        this.id = id;
        this.extendsTerm = extendsTerm;
        this.ifaceListTerm = ifaceListTerm;
        this.classbody = classbody;
        fieldDictionary = new OrderedMap();
        methodDictionary = new OrderedMap();
        usedMethodSigns = new OrderedMap();
        constrParamList = Empty.newTerm();
        outerLocals = new ObjVector();
        classbody.processPass0(context);
    }

    void processPass1(Context c) {
        Term.assertCond(parsed);
        if (outerScope != null)
            return;
        outerScope = c.localScope;
        if (outerScope != null) {
            Term.assertCond(outerClass != null);
            Term.assertCond(c.currentMethod != null);
            if (isDefined) {
                classbody.fatalError(c,
                        "Local class is used outside its scope: " + name);
                return;
            }
            if (c.currentMethod.isClassMethod()) {
                insideStaticMethod = true;
                if (id.charAt(0) >= '0' && id.charAt(0) <= '9') {
                    modifiers |= AccModifier.STATIC;
                }
            }
            outerScope.addLocalClass(this);
            Enumeration en = c.currentMethod.getLocalsNames();
            while (en.hasMoreElements()) {
                VariableDefinition v = c.currentMethod.getLocalVar((String) en
                        .nextElement());
                Term.assertCond(v != null);
                if (v.isFinalVariable() && !v.isUnassigned()) {
                    outerLocals.addElement(v);
                }
            }
        }
    }

    void changeExtendsTerm(ClassDefinition aclass, boolean hasPrimary) {
        Term.assertCond(parsed && !isDefined);
        if (hasPrimary) {
            extendsTerm = new ClassOrIfaceType(aclass);
        }
        if (aclass.isInterface()) {
            ifaceListTerm = extendsTerm;
            extendsTerm = Empty.newTerm();
        }
    }

    private void processExtends() {
        if (!isDefined) {
            Term.assertCond(parsed);
            Context c = context.cloneForClass(outerClass, this);
            c.localScope = outerScope;
            if (superClass == null && !name.equals(Names.JAVA_LANG_OBJECT)) {
                c.typeClassDefinition = Main.dict.get(Names.JAVA_LANG_OBJECT);
                extendsTerm.processPass1(c);
                superClass = c.typeClassDefinition;
            }
            ifaceListTerm.processPass1(c);
        }
    }

    private void processSuperClasses() {
        Term.assertCond(parsed);
        if (superClass != null && !addedToSuper) {
            if (implementedBy != null
                    && !superClass.name.equals(Names.JAVA_LANG_OBJECT)) {
                addedToSuper = true;
                specifiedInterfaces.addLast(superClass);
            }
            if (implementedBy == null || !ifaceListTerm.notEmpty()) {
                addedToSuper = true;
                if (superClass.implementedBy != null) {
                    classbody.fatalError(context,
                            "A class cannot be extended from an interface: "
                                    + superClass.name);
                }
                superClass.subclasses.addLast(this);
            }
        }
    }

    private void addFieldsToInner(ClassDefinition cd) {
        define(cd);
        if (superClass != null) {
            Term.assertCond(implementedBy != null);
            superClass.addFieldsToInner(cd);
            if (!cd.interfaceClasses.contains(this)) {
                cd.interfaceClasses.addLast(this);
                implementedBy.addLast(cd);
                Enumeration en = interfaceClasses(cd).elements();
                while (en.hasMoreElements()) {
                    ClassDefinition classDefn = (ClassDefinition) en
                            .nextElement();
                    cd.interfaceClasses.addLast(classDefn);
                    classDefn.implementedBy.addLast(cd);
                }
            }
        }
    }

    void addFieldsTo(ClassDefinition cd) {
        Term.assertCond(cd.isDefined);
        if (implementedBy == null) {
            classbody
                    .fatalError(context,
                            "Not an interface is specified after 'implements': "
                                    + name);
        }
        cd.specifiedInterfaces.addLast(this);
        if (cd.implementedBy != null
                && cd.superClass.name.equals(Names.JAVA_LANG_OBJECT)) {
            if (!cd.addedToSuper) {
                cd.superClass = this;
                cd.addedToSuper = true;
                subclasses.addLast(cd);
            }
        } else {
            addFieldsToInner(cd);
        }
    }

    private void definePass1(ClassDefinition forClass) {
        Main.dict.message("Analyzing: " + name
                + (forClass != null ? " (for " + forClass.name + ")" : ""));
        Term.assertCond(context.currentClass == this);
        Term.assertCond(context.currentMethod == null);
        if (!isStaticClass()) {
            int nesting = 0;
            for (ClassDefinition cd = outerClass; !cd.isStaticClass(); cd = cd.outerClass) {
                nesting++;
            }
            outerThisRef = new VariableDefinition(this, "this$" + nesting,
                    AccModifier.FINAL | AccModifier.SYNTHETIC, outerClass,
                    Empty.newTerm(), true);
            addField(outerThisRef);
        }
        if (outerScope != null) {
            outerFields = new ObjHashtable();
            Enumeration en = outerLocals.elements();
            while (en.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) en.nextElement();
                String varName = v.id();
                VariableDefinition field;
                if (superClass == null
                        || superClass.outerClass() != outerClass
                        || (field = superClass.getOuterField(varName, this)) == null) {
                    addField(field = new VariableDefinition(this, "val$"
                            + varName, AccModifier.FINAL
                            | AccModifier.SYNTHETIC, v.exprType(),
                            Empty.newTerm(), false));
                }
                field.markUsed(true, true);
                outerFields.put(varName, field);
            }
        }
        boolean oldHasConstructor = context.hasConstructor;
        context.hasConstructor = false;
        classbody.processPass1(context);
        mayContainClinit = false;
        boolean isProxy = isProxyClass();
        if (!context.hasConstructor && implementedBy == null) {
            Term constr;
            if (isProxy) {
                ClassDefinition objectClass = Main.dict
                        .get(Names.JAVA_LANG_OBJECT);
                Enumeration en = objectClass.methodDictionary().keys();
                while (en.hasMoreElements()) {
                    MethodDefinition md = objectClass
                            .getMethodNoInheritance((String) en.nextElement());
                    if (md.isPublic() && !md.isConstructor()
                            && md.allowOverride()) {
                        objectClass.reflectMethodInner(md);
                        addMethod(md.cloneMethodFor(this, true));
                    }
                }
                constr = new TypeDeclaration(
                        new AccModifier(AccModifier.PUBLIC
                                | AccModifier.SYNTHETIC),
                        new ConstrDeclaration(
                                new LexTerm(LexTerm.ID, id),
                                new FormalParameter(
                                        new AccModifier(AccModifier.SYNTHETIC),
                                        new ClassOrIfaceType(
                                                Main.dict
                                                        .get(Names.JAVA_LANG_REFLECT_INVOCATIONHANDLER)),
                                        Empty.newTerm(),
                                        new VariableIdentifier(new LexTerm(
                                                LexTerm.ID, "handler")), Empty
                                                .newTerm()), Empty.newTerm(),
                                new ExprStatement(new ConstructorCall(Empty
                                        .newTerm(), new Super(), new Argument(
                                        new Expression(new QualifiedName(
                                                new LexTerm(LexTerm.ID,
                                                        "handler"))))))));
            } else {
                Term paramList = constrParamList;
                if (constrSuperType != null) {
                    paramList = FormalParamList.prepend(
                            new FormalParameter(new AccModifier(
                                    AccModifier.FINAL | AccModifier.SYNTHETIC),
                                    new ClassOrIfaceType(constrSuperType
                                            .receiverClass()), Empty.newTerm(),
                                    (new VariableIdentifier(new LexTerm(
                                            LexTerm.ID, "this$00")))
                                            .setLineInfoFrom(classbody), Empty
                                            .newTerm()), paramList);
                }
                constr = new TypeDeclaration(
                        new AccModifier((modifiers & (AccModifier.PUBLIC
                                | AccModifier.PRIVATE | AccModifier.PROTECTED))
                                | ((modifiers & AccModifier.PUBLIC) != 0 ? 0
                                        : AccModifier.SYNTHETIC)),
                        (new ConstrDeclaration(new LexTerm(LexTerm.ID, id),
                                paramList, Empty.newTerm(), Empty.newTerm()))
                                .setLineInfoFrom(classbody));
                classbody = (new Seq(classbody, constr))
                        .setLineInfoFrom(classbody);
            }
            constr.processPass1(context);
            Main.dict.methodsAnalyzed--;
        }
        context.hasConstructor = oldHasConstructor;
        if (classInitializers != null
                && !classInitializers.isJavaConstant(this)) {
            modifiers |= AccModifier.NATIVE;
        }
        Enumeration en = methodDictionary().keys();
        while (en.hasMoreElements()) {
            MethodDefinition md = getMethodNoInheritance((String) en
                    .nextElement());
            if (!checkOverridenMethodRetType(md)) {
                classbody.fatalError(context, "Incompatible return type in: "
                        + name + "." + md.methodSignature().getInfo());
            }
        }
        if (isInterface()) {
            ClassDefinition cd = superClass;
            cd.define(this);
            if (cd.superClass != null) {
                Enumeration en2 = cd.methodDictionary().keys();
                while (en2.hasMoreElements()) {
                    String sigString = (String) en2.nextElement();
                    if (getMethod(sigString) == null) {
                        addInheritedMethod(sigString, false);
                    }
                }
            }
        }
        Enumeration en2 = interfaceClasses(null).elements();
        while (en2.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en2.nextElement();
            cd.define(this);
            Term.assertCond(cd.definePassOneDone);
            Enumeration en3 = cd.methodDictionary().keys();
            while (en3.hasMoreElements()) {
                String sigString = (String) en3.nextElement();
                MethodDefinition md = getMethod(sigString);
                if (md == null) {
                    md = addInheritedMethod(sigString, isProxy);
                }
                if (isProxy) {
                    md.intersectThrowsWith(cd.getMethodNoInheritance(sigString));
                }
            }
        }
        definePassOneDone = true;
    }

    private boolean checkOverridenMethodRetType(MethodDefinition md) {
        String sigString = md.methodSignature().signatureString();
        int retSize = md.exprType().objectSize();
        if (retSize >= Type.CLASSINTERFACE) {
            retSize = Type.CLASSINTERFACE;
        }
        ClassDefinition cd = superClass;
        if (cd != null) {
            MethodDefinition md2 = cd.getMethodSamePkg(sigString,
                    getPackageName());
            if (md2 != null) {
                int retSize2 = md2.exprType().objectSize();
                if (retSize2 >= Type.CLASSINTERFACE) {
                    retSize2 = Type.CLASSINTERFACE;
                }
                if (retSize != retSize2)
                    return false;
            }
        }
        Enumeration en = interfaceClasses(null).elements();
        while (en.hasMoreElements()) {
            cd = (ClassDefinition) en.nextElement();
            cd.define(this);
            MethodDefinition md2 = cd.getMethodNoInheritance(sigString);
            if (md2 != null) {
                int retSize2 = md2.exprType().objectSize();
                if (retSize2 >= Type.CLASSINTERFACE) {
                    retSize2 = Type.CLASSINTERFACE;
                }
                if (retSize != retSize2)
                    return false;
            }
        }
        return true;
    }

    private MethodDefinition addInheritedMethod(String sigString,
            boolean isProxy) {
        MethodDefinition md = null;
        ExpressionType resType = null;
        if (isInterface()) {
            ClassDefinition cd = superClass;
            if (cd.superClass() != null
                    && (md = cd.getMethodNoInheritance(sigString)) != null) {
                resType = md.exprType();
            }
        }
        Enumeration en = interfaceClasses(null).elements();
        while (en.hasMoreElements()) {
            MethodDefinition md2 = ((ClassDefinition) en.nextElement())
                    .getMethodNoInheritance(sigString);
            if (md2 != null) {
                ExpressionType resType2 = md2.exprType();
                if (md == null
                        || (resType != resType2 && isAssignableFrom(resType,
                                resType2, this))) {
                    md = md2;
                    resType = resType2;
                }
            }
        }
        Term.assertCond(md != null);
        addMethod(md.cloneMethodFor(this, isProxy));
        return md;
    }

    void setHasAssertStmt() {
        markUsed();
        modifiers |= AccModifier.NATIVE;
        if (fieldDictionary.get(Names.ASSERTIONSDISABLED) == null) {
            addField(new VariableDefinition(this, Names.ASSERTIONSDISABLED,
                    AccModifier.STATIC | AccModifier.FINAL
                            | AccModifier.SYNTHETIC,
                    Main.dict.classTable[Type.BOOLEAN], Empty.newTerm(), false));
        }
    }

    private void addNativeMethodDepend() {
        MethodDefinition md = getMethodNoInheritance(Names.SIGN_FINALIZE);
        if (md != null) {
            if (superClass != null) {
                Main.dict.get(Names.JAVA_LANG_VMRUNTIME).markUsed();
            } else {
                md.markUsedThisOnly();
            }
        }
        if (name.equals(Names.JAVA_LANG_STRING)) {
            markFields(Names.fieldsOrderString, 1);
            VariableDefinition v = getField(Names.fieldsOrderString[0], null);
            if (v != null) {
                Main.dict.setStringValueType(v.exprType());
            }
        } else if (name.equals(Names.JAVA_LANG_STRINGINDEXOUTOFBOUNDSEXCEPTION)) {
            if (Main.dict.markStrIndexOutInit) {
                markMethod(Names.SIGN_INIT_INT);
                if (basicConstructor != null) {
                    basicConstructor.markUsed(this, false);
                }
            }
        } else if (name.equals(Names.JAVA_LANG_CLASS)) {
            markFields(Names.fieldsOrderClass, 0);
        } else if (name.equals(Names.JAVA_LANG_VMCLASS)) {
            markMethod(Names.SIGN_ARRAYCLASSOF0X);
        } else if (name.equals(Names.JAVA_LANG_VMCLASSLOADER)) {
            Main.dict.buildClassTable = true;
        } else if (name.equals(Names.JAVA_LANG_VMRUNTIME)) {
            markMethod(Names.SIGN_FINALIZEOBJECT0X);
        } else if (name.equals(Names.JAVA_LANG_VMTHREAD)) {
            markMethod(Names.SIGN_DESTROYJAVAVM0X);
        }
    }

    int otherInstanceFieldsUnused(String[] names) {
        Term.assertCond(used);
        boolean hasOther = false;
        Enumeration en = fieldDictionary().unorderedElements();
        while (en.hasMoreElements()) {
            VariableDefinition v = (VariableDefinition) en.nextElement();
            String varName = v.id();
            if (v.used() && !v.isClassVariable()
                    && (!v.isFinalVariable() || isReflectedField(varName))) {
                int i = names.length;
                while (i-- > 0) {
                    if (varName.equals(names[i]))
                        break;
                }
                if (i < 0) {
                    if (v.exprType().objectSize() >= Type.CLASSINTERFACE)
                        return -1;
                    hasOther = true;
                }
            }
        }
        return hasOther ? 0 : 1;
    }

    static void setSpecialVirtual() {
        MethodDefinition md = Main.dict.get(Names.JAVA_LANG_OBJECT).getMethod(
                Names.SIGN_FINALIZE);
        if (md != null && Main.dict.get(Names.JAVA_LANG_VMRUNTIME).used) {
            md.setVirtual();
        }
        Main.dict.allowConstClass = Main.dict.get(Names.JAVA_LANG_CLASS)
                .otherInstanceFieldsUnused(Names.fieldsOrderClass);
        VariableDefinition v = Main.dict.get(Names.JAVA_LANG_STRING).getField(
                Names.fieldsOrderString[Names.fieldsOrderString.length - 1],
                null);
        if (v != null && v.used() && !v.isClassVariable()) {
            Main.dict.fillStrHash = true;
        }
    }

    private static void markAllBasicCtors() {
        if (!Main.dict.markBasicCtors) {
            Main.dict.markBasicCtors = true;
            Enumeration en = Main.dict.usedClasses();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                MethodDefinition md = cd.basicConstructor;
                if (md != null && !cd.isInterface() && !md.isPrivate()) {
                    md.markUsed(cd, true);
                }
            }
        }
    }

    static void markAllDirectIfaces() {
        if (!Main.dict.markDirectIfaces) {
            Main.dict.markDirectIfaces = true;
            Main.dict.get(Names.JAVA_LANG_CLONEABLE).setVTableUsed(true);
            Main.dict.get(Names.JAVA_IO_SERIALIZABLE).setVTableUsed(true);
            Enumeration en = Main.dict.usedClasses();
            while (en.hasMoreElements()) {
                Enumeration en2 = ((ClassDefinition) en.nextElement()).specifiedInterfaces
                        .elements();
                while (en2.hasMoreElements()) {
                    ((ClassDefinition) en2.nextElement()).setVTableUsed(true);
                }
            }
        }
    }

    void markForInstanceOf(boolean isArray) {
        if (isObjectOrCloneable()) {
            markUsed();
        } else if (isArray && isInterface()) {
            if (!markIfImplemented) {
                markIfImplemented = true;
                Enumeration en = implementedBy.elements();
                while (en.hasMoreElements()) {
                    if (((ClassDefinition) en.nextElement()).used) {
                        setVTableUsed(true);
                        break;
                    }
                }
            }
        } else if (hasInstances) {
            markUsed();
        } else {
            markIfHasInstances = true;
        }
    }

    static void processEnumValueOf() {
        if (!Main.dict.hasEnumValueOf) {
            Main.dict.hasEnumValueOf = true;
            Enumeration en = Main.dict.get(Names.JAVA_LANG_ENUM).subclasses
                    .elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (cd.used) {
                    cd.processEnumClass();
                }
            }
        }
    }

    private void processEnumClass() {
        Enumeration en = fieldDictionary().keys();
        while (en.hasMoreElements()) {
            VariableDefinition v = (VariableDefinition) fieldDictionary.get(en
                    .nextElement());
            if (isEnumValueField(v)) {
                reflectFieldInner(v);
            }
        }
    }

    private boolean isEnumValueField(VariableDefinition v) {
        ExpressionType resType;
        return v.isClassVariable()
                && v.isFinalVariable()
                && !v.isPrivate()
                && ((resType = v.exprType()) == this || resType.receiverClass()
                        .superClass() == this);
    }

    private void processSerialization() {
        if (!isProxyClass()) {
            if (name.equals(Names.JAVA_IO_OBJECTINPUTSTREAM)) {
                Enumeration en = Main.dict.usedClasses();
                while (en.hasMoreElements()) {
                    ((ClassDefinition) en.nextElement())
                            .markForObjectInputStream();
                }
            } else if (Main.dict.get(Names.JAVA_IO_OBJECTINPUTSTREAM).used) {
                markForObjectInputStream();
            }
            if (name.equals(Names.JAVA_IO_OBJECTOUTPUTSTREAM)) {
                Enumeration en = Main.dict.usedClasses();
                while (en.hasMoreElements()) {
                    ((ClassDefinition) en.nextElement())
                            .markForObjectOutputStream();
                }
            } else if (Main.dict.get(Names.JAVA_IO_OBJECTOUTPUTSTREAM).used) {
                markForObjectOutputStream();
            }
            if (name.equals(Names.JAVA_IO_OBJECTSTREAMCLASS)) {
                Enumeration en = Main.dict.usedClasses();
                while (en.hasMoreElements()) {
                    ((ClassDefinition) en.nextElement())
                            .markForObjectStreamClass();
                }
            } else if (Main.dict.get(Names.JAVA_IO_OBJECTSTREAMCLASS).used) {
                markForObjectStreamClass();
            }
        }
    }

    private void markForObjectInputStream() {
        ClassDefinition serializableClass = Main.dict
                .get(Names.JAVA_IO_SERIALIZABLE);
        if (!isInterface() && serializableClass.isAssignableFrom(this, 0, this)) {
            if (!isAbstractOrInterface()) {
                setHasInstances();
                if (!hasExactInstance) {
                    ClassDefinition sc = superClass;
                    while (sc != null
                            && serializableClass.isAssignableFrom(sc, 0, this)) {
                        sc = sc.superClass();
                    }
                    if (sc != null && sc.basicConstructor != null
                            && !sc.basicConstructor.isPrivate()) {
                        setHasExactInstance(true);
                    }
                }
            }
            MethodDefinition md = getMethodNoInheritance(Names.SIGN_READOBJECT);
            if (md != null && !md.isAbstract()) {
                reflectMethodInner(md);
            }
            md = getMethodNoInheritance(Names.SIGN_READRESOLVE);
            if (md != null && !md.isAbstract()) {
                reflectMethodInner(md);
            }
        }
    }

    private void markForObjectOutputStream() {
        if (!isInterface()
                && Main.dict.get(Names.JAVA_IO_SERIALIZABLE).isAssignableFrom(
                        this, 0, this)) {
            MethodDefinition md = getMethodNoInheritance(Names.SIGN_WRITEOBJECT);
            if (md != null && !md.isAbstract()) {
                reflectMethodInner(md);
            }
            md = getMethodNoInheritance(Names.SIGN_WRITEREPLACE);
            if (md != null && !md.isAbstract()) {
                reflectMethodInner(md);
            }
        }
    }

    private void markForObjectStreamClass() {
        if (Main.dict.get(Names.JAVA_IO_SERIALIZABLE).isAssignableFrom(this, 0,
                this)) {
            VariableDefinition v = (VariableDefinition) fieldDictionary
                    .get(Names.SERIALVERSIONUID);
            if (v == null) {
                v = new VariableDefinition(this, Names.SERIALVERSIONUID,
                        AccModifier.PRIVATE | AccModifier.STATIC
                                | AccModifier.FINAL | AccModifier.SYNTHETIC,
                        Main.dict.classTable[Type.LONG], new Expression(
                                new IntLiteral(computeSerialVersion())), false);
                addField(v);
            }
            reflectFieldInner(v);
            if (!Main.dict.get(Names.JAVA_IO_EXTERNALIZABLE).isAssignableFrom(
                    this, 0, this)
                    && !name.equals(Names.JAVA_LANG_CLASS)
                    && !name.equals(Names.JAVA_LANG_STRING)) {
                v = (VariableDefinition) fieldDictionary
                        .get(Names.SERIALPERSISTENTFIELDS);
                if (v != null && v.isClassVariable()) {
                    reflectFieldInner(v);
                }
                Enumeration en = fieldDictionary().keys();
                while (en.hasMoreElements()) {
                    v = (VariableDefinition) fieldDictionary.get(en
                            .nextElement());
                    if (!v.isClassVariable() && !v.isTransient()) {
                        reflectFieldInner(v);
                    }
                }
            }
        }
    }

    String getJavaSignature() {
        return type == Type.CLASSINTERFACE ? Type.sig[Type.CLASSINTERFACE]
                + name.replace('.', '/') + ";" : Type.sig[type];
    }

    boolean classInitializerNotCalledYet() {
        return mayContainClinit || classInitializers != null;
    }

    InitializerPart addInitializer(Term term, boolean isClassVariable) {
        return isClassVariable ? (classInitializers = new InitializerPart(
                classInitializers, term))
                : (instanceInitializers = new InitializerPart(
                        instanceInitializers, term));
    }

    private ConstValue computeSerialVersion() {
        SecHashAlg sha = new SecHashAlg();
        sha.updateUTF(name);
        int classaccess = modifiers
                & (AccModifier.PUBLIC | AccModifier.INTERFACE | AccModifier.ABSTRACT);
        if (isInterface()) {
            classaccess &= AccModifier.PUBLIC | AccModifier.INTERFACE;
            Enumeration en = methodDictionary().unorderedElements();
            while (en.hasMoreElements()) {
                MethodDefinition md = (MethodDefinition) en.nextElement();
                if (md.definingClass() == this && !md.isCopiedFromIface()) {
                    classaccess |= AccModifier.ABSTRACT;
                    break;
                }
            }
        } else if (isFinal()) {
            classaccess |= AccModifier.FINAL;
        }
        sha.updateInt(classaccess);
        int size = specifiedInterfaces.countSize();
        String[] names;
        if (size > 0) {
            names = new String[size];
            Enumeration en = specifiedInterfaces.elements();
            for (int i = 0; en.hasMoreElements(); i++) {
                names[i] = ((ClassDefinition) en.nextElement()).name;
            }
            names = sortStrings(names, size);
            for (int i = 0; i < size; i++) {
                sha.updateUTF(names[i]);
            }
        }
        size = fieldDictionary().size();
        if (size > 0) {
            names = new String[size];
            fieldDictionary().copyKeysInto(names);
            names = sortStrings(names, size);
            for (int i = 0; i < size; i++) {
                VariableDefinition v = (VariableDefinition) fieldDictionary
                        .get(names[i]);
                if (!v.isPrivate()
                        || (!v.isClassVariable() && !v.isTransient())) {
                    sha.updateUTF(names[i]);
                    sha.updateInt(v.getJavaModifiers()
                            & (AccModifier.PUBLIC | AccModifier.PRIVATE
                                    | AccModifier.PROTECTED
                                    | AccModifier.STATIC | AccModifier.FINAL
                                    | AccModifier.VOLATILE | AccModifier.TRANSIENT));
                    sha.updateUTF(v.exprType().getJavaSignature());
                }
            }
        }
        if ((modifiers & AccModifier.NATIVE) != 0) {
            sha.updateUTF("<clinit>");
            sha.updateInt(AccModifier.STATIC);
            sha.updateUTF("()V");
        }
        names = new String[methodDictionary().size()];
        size = 0;
        Enumeration en = methodDictionary.keys();
        while (en.hasMoreElements()) {
            String sigString = (String) en.nextElement();
            MethodDefinition md = getMethodNoInheritance(sigString);
            if (md.definingClass() == this && !md.isPrivate()
                    && !md.isCopiedFromIface()) {
                names[size++] = (md.isConstructor() ? " " : "") + md.id() + " "
                        + md.getJavaSignature() + " " + sigString;
            }
        }
        if (size > 0) {
            names = sortStrings(names, size);
            for (int i = 0; i < size; i++) {
                String s = names[i];
                int j = s.indexOf(' ', 1);
                int k = s.lastIndexOf(' ');
                sha.updateUTF(s.substring(s.charAt(0) != ' ' ? 0 : 1, j));
                sha.updateInt(getMethodNoInheritance(s.substring(k + 1))
                        .getJavaModifiers()
                        & (AccModifier.PUBLIC | AccModifier.PRIVATE
                                | AccModifier.PROTECTED | AccModifier.STATIC
                                | AccModifier.FINAL | AccModifier.SYNCHRONIZED
                                | AccModifier.NATIVE | AccModifier.ABSTRACT | AccModifier.STRICT));
                sha.updateUTF(s.substring(j + 1, k).replace('/', '.'));
            }
        }
        sha.finishUpdate();
        byte[] hashvalue = new byte[20];
        sha.engineDigestVal(hashvalue);
        return new ConstValue(((hashvalue[3] & 0xff) << 24)
                | ((hashvalue[2] & 0xff) << 16) | ((hashvalue[1] & 0xff) << 8)
                | (hashvalue[0] & 0xff), ((hashvalue[7] & 0xff) << 24)
                | ((hashvalue[6] & 0xff) << 16) | ((hashvalue[5] & 0xff) << 8)
                | (hashvalue[4] & 0xff));
    }

    private void processRemoteMethodInvocation() {
        if (Main.dict.get("java.rmi.server." + "RemoteStub").isAssignableFrom(
                this, 0, this)) {
            ObjVector parmSig = new ObjVector();
            parmSig.addElement(Main.dict.get("java.rmi.server." + "RemoteRef"));
            MethodDefinition md = matchConstructor(parmSig, this);
            if (md != null) {
                reflectMethodInner(md);
            }
        } else {
            if (Main.dict.get("java.rmi." + "Remote").isAssignableFrom(this, 0,
                    this)) {
                Main.dict.defineIfExists(name + "_Stub", this);
                Main.dict.defineIfExists(name + "_Skel", this);
                ClassDefinition cd = this;
                do {
                    Enumeration en = cd.methodDictionary().keys();
                    while (en.hasMoreElements()) {
                        MethodDefinition md = cd
                                .getMethodNoInheritance((String) en
                                        .nextElement());
                        if (!md.isConstructor()) {
                            ClassDefinition classDefn = md.definingClass();
                            if (!md.isPrivate()
                                    && !classDefn.name.startsWith(Names.JAVA_0)) {
                                classDefn.reflectMethodInner(md);
                            }
                        }
                    }
                    cd = cd.superClass();
                    cd.define(this);
                } while (cd.superClass != null);
            }
        }
        if (Main.dict.get("org.omg.CORBA.portable." + "Streamable")
                .isAssignableFrom(this, 0, this)) {
            VariableDefinition v = getField("value", null);
            if (v != null) {
                ClassDefinition cd = v.definingClass();
                cd.reflectFieldInner(v);
                cd = v.exprType().receiverClass();
                if (cd.objectSize() == Type.CLASSINTERFACE) {
                    cd.reflectAllFields(false);
                }
            }
        }
    }

    boolean defined() {
        return isDefined;
    }

    void markUsed() {
        if (!used) {
            used = true;
            if (--Main.dict.failOnClassLimit == 0)
                throw new TranslateException(
                        "Processed classes limit exceeded!");
            if (name.equals(Main.dict.failOnClassName)
                    && Main.dict.failOnMethodId == null
                    && Main.dict.failOnFieldName == null)
                throw new AssertException("Specified class is required!");
            if (!isDefined) {
                predefineClass(null);
                return;
            }
            Main.dict.classNowUsed(this);
            addNativeMethodDepend();
            ClassDefinition sc = superClass();
            if (sc != null) {
                if (!isInterface()) {
                    sc.markUsed();
                }
                if (Main.dict.markDirectIfaces) {
                    Enumeration en = specifiedInterfaces.elements();
                    while (en.hasMoreElements()) {
                        ((ClassDefinition) en.nextElement())
                                .setVTableUsed(true);
                    }
                }
                if (sc.used) {
                    Enumeration en = sc.usedMethodSigns.keys();
                    while (en.hasMoreElements()) {
                        String sigString = (String) en.nextElement();
                        String pkgName = (String) sc.usedMethodSigns
                                .get(sigString);
                        if (pkgName.length() == 0) {
                            pkgName = null;
                        }
                        MethodDefinition md = getMethodNoInheritance(sigString);
                        if (md != null
                                && !md.isClassMethod()
                                && (pkgName == null || pkgName
                                        .equals(getPackageName()))) {
                            md.markUsed(this, false);
                        } else {
                            markMethodInSubclasses(sigString, pkgName);
                        }
                    }
                }
                reflectInheritedFrom(sc);
                reflectInheritedFieldsFrom(sc);
                Enumeration en = interfaceClasses(null).elements();
                while (en.hasMoreElements()) {
                    ClassDefinition cd = (ClassDefinition) en.nextElement();
                    if (cd.markIfImplemented) {
                        cd.setVTableUsed(true);
                    }
                    Enumeration en2 = cd.usedMethodSigns.keys();
                    while (en2.hasMoreElements()) {
                        String sigString = (String) en2.nextElement();
                        MethodDefinition md = getMethod(sigString);
                        if (md != null) {
                            md.markUsed(this, false);
                        }
                        if (md == null || md.definingClass() != this) {
                            cd.markMethodInSubclasses(sigString, null);
                        }
                    }
                    reflectInheritedFrom(cd);
                }
                if (sc.name.equals(Names.JAVA_UTIL_LISTRESOURCEBUNDLE)
                        || sc.name
                                .equals(Names.COM_IVMAISOFT_JPROPJAV_STRLISTRESOURCEBUNDLE)) {
                    int pos = name.lastIndexOf('_');
                    if (pos > 0 && name.indexOf('.', pos + 1) < 0) {
                        Main.dict.dynamicDefineClass(name.substring(0, pos),
                                null, this);
                    }
                }
            }
            if (basicConstructor != null
                    && !isInterface()
                    && ((Main.dict.markBasicCtors && !basicConstructor
                            .isPrivate()) || name
                            .equals(Names.JAVA_LANG_THROWABLE))) {
                basicConstructor.markUsed(this, true);
            }
            if (name.equals(Names.JAVA_LANG_REFLECT_VMCONSTRUCTOR)) {
                markAllBasicCtors();
            }
            if (isObjectOrCloneable() || name.equals(Names.JAVA_LANG_THROWABLE)) {
                setHasExactInstance(true);
                setHasInstances();
            } else {
                setHasExactInstance(false);
            }
            if (sc != null && Main.dict.hasEnumValueOf
                    && sc.name.equals(Names.JAVA_LANG_ENUM)) {
                processEnumClass();
            }
            processSerialization();
            processRemoteMethodInvocation();
            if (name.equals(Names.JAVA_LANG_REFLECT_PROXY)) {
                reflectConstructors(false, Names.SIGN_INIT_INVOCATIONHANDLER,
                        false);
            }
            if (name.equals(Names.JAVA_LANG_CLASSLOADER_STATICDATA)) {
                VariableDefinition v = (VariableDefinition) fieldDictionary
                        .get(Names.SYSTEMCLASSLOADER);
                if (v != null) {
                    reflectFieldInner(v);
                }
            }
            if (!name.startsWith(Names.JAVA_LANG_0)
                    && !name.equals(Names.JAVA_IO_VMFILE)) {
                markFieldInitializers(true);
            }
            if (hasInstances) {
                markFieldInitializers(false);
            }
            needStaticInitPass = true;
            if (outerClass != null) {
                outerClass.markUsed();
            }
            if (Main.dict.isClassNameUsed(this)) {
                Main.dict.addStringLiteral(name, this);
            }
        }
    }

    boolean used() {
        return used;
    }

    void markUsedForArray() {
        forArraysUsed = true;
        markUsed();
    }

    void markAllDefinedClasses() {
        if (!markIfDefined) {
            markIfDefined = true;
            markUsed();
            if (implementedBy != null) {
                Enumeration en = implementedBy.elements();
                while (en.hasMoreElements()) {
                    ((ClassDefinition) en.nextElement())
                            .markAllDefinedClasses();
                }
            }
            Enumeration en = subclasses.elements();
            while (en.hasMoreElements()) {
                ((ClassDefinition) en.nextElement()).markAllDefinedClasses();
            }
        }
    }

    private void inheritMarkIfDefined() {
        if (superClass != null) {
            if (superClass.markIfDefined) {
                markAllDefinedClasses();
            } else {
                Enumeration en = specifiedInterfaces.elements();
                while (en.hasMoreElements()) {
                    if (((ClassDefinition) en.nextElement()).markIfDefined) {
                        markAllDefinedClasses();
                        break;
                    }
                }
            }
        }
    }

    boolean hasNativeIdCollision(String ident) {
        boolean found = false;
        Enumeration en = methodDictionary().unorderedElements();
        while (en.hasMoreElements()) {
            MethodDefinition md = (MethodDefinition) en.nextElement();
            if (md.isNative() && ident.equals(md.id())) {
                if (found)
                    return true;
                found = true;
            }
        }
        return false;
    }

    void markAllPublicStaticMethods() {
        Enumeration en = methodDictionary().keys();
        while (en.hasMoreElements()) {
            MethodDefinition md = getMethodNoInheritance((String) en
                    .nextElement());
            if (md.isPublic() && md.isClassMethod()) {
                md.markUsedThisOnly();
            }
        }
    }

    boolean reflectAllPublicStaticMethods() {
        boolean reflected = false;
        MethodDefinition mdMain = getMethodNoInheritance(Names.SIGN_MAIN);
        Enumeration en = methodDictionary().keys();
        while (en.hasMoreElements()) {
            MethodDefinition md = getMethodNoInheritance((String) en
                    .nextElement());
            if (md.isPublic() && md.isClassMethod() && md != mdMain) {
                reflectMethodInner(md);
                reflected = true;
            }
        }
        return reflected;
    }

    void markAllNatives() {
        boolean isJavaCore = name.startsWith(Names.JAVA_0)
                || name.startsWith(Names.GNU_0);
        Enumeration en = methodDictionary().keys();
        while (en.hasMoreElements()) {
            MethodDefinition md = getMethodNoInheritance((String) en
                    .nextElement());
            if (md.isNative() || (isJavaCore && !md.isPrivate())) {
                md.markUsedThisOnly();
                md.markUsed(this, false);
            }
        }
    }

    private void markFieldInitializers(boolean isStatic) {
        Enumeration en = fieldDictionary().keys();
        while (en.hasMoreElements()) {
            VariableDefinition v = (VariableDefinition) fieldDictionary
                    .get((String) en.nextElement());
            if (v.isClassVariable() == isStatic) {
                v.markInitializerOnly();
            }
        }
    }

    private void markFields(String[] names, int ignoreLastCnt) {
        int len = names.length - ignoreLastCnt;
        VariableDefinition v;
        for (int i = 0; i < len; i++) {
            if ((v = getField(names[i], null)) != null) {
                v.markUsed();
            }
        }
    }

    void markMethod(String sigString) {
        MethodDefinition md = getMethod(sigString);
        if (md != null) {
            md.markUsed(null);
            if (md.isConstructor()) {
                md.markNew();
            }
        }
    }

    int markMethodInSubclasses(String sigString, String pkgName) {
        if (pkgName == null) {
            usedMethodSigns.put(sigString, "");
        } else {
            String prevPkgName = (String) usedMethodSigns.get(sigString);
            if (prevPkgName == null) {
                usedMethodSigns.put(sigString, pkgName);
            } else if (prevPkgName.length() > 0 && !pkgName.equals(prevPkgName)) {
                usedMethodSigns.put(sigString, "");
            }
        }
        int cnt = 0;
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (cd.used) {
                    MethodDefinition md = cd.getMethod(sigString);
                    cnt += md != null ? md.markUsed(cd, false) : cd
                            .markMethodInSubclasses(sigString, null);
                }
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            MethodDefinition md;
            cnt += cd.used
                    && (md = cd.getMethodNoInheritance(sigString)) != null
                    && !md.isClassMethod()
                    && (pkgName == null || pkgName.equals(cd.getPackageName())) ? md
                    .markUsed(cd, false) : cd.markMethodInSubclasses(sigString,
                    pkgName);
        }
        return cnt;
    }

    void setVirtualInSubclasses(String sigString) {
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (cd.used) {
                    MethodDefinition md = cd.getMethod(sigString);
                    if (md != null) {
                        md.setVirtual();
                    } else {
                        cd.setVirtualInSubclasses(sigString);
                    }
                }
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            MethodDefinition md = null;
            if (cd.used && (md = cd.getMethodNoInheritance(sigString)) != null) {
                md.setVirtual();
            }
            if (md == null || md.definingClass() != cd) {
                cd.setVirtualInSubclasses(sigString);
            }
        }
    }

    boolean subclassHasMethod(String sigString, String pkgName) {
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (cd.used) {
                    MethodDefinition md = cd.getMethod(sigString);
                    if ((md != null && !md.isAbstract() && !md.isClassMethod())
                            || cd.subclassHasMethod(sigString, null))
                        return true;
                }
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            MethodDefinition md;
            if ((cd.used && (md = cd.getMethodNoInheritance(sigString)) != null
                    && !md.isAbstract() && !md.isClassMethod() && (pkgName == null || pkgName
                    .equals(cd.getPackageName())))
                    || cd.subclassHasMethod(sigString, pkgName))
                return true;
        }
        return false;
    }

    private void getTwoRealSubclasses(ObjVector classes) {
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (cd.hasInstances && cd.used) {
                    if (cd.isAbstractOrInterface()) {
                        cd.getTwoRealSubclasses(classes);
                    } else if (classes.identityLastIndexOf(cd) < 0) {
                        classes.addElement(cd);
                    }
                    if (classes.size() > 1)
                        return;
                }
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            if (cd.hasInstances && cd.used) {
                if (cd.isAbstractOrInterface()) {
                    cd.getTwoRealSubclasses(classes);
                } else if (classes.identityLastIndexOf(cd) < 0) {
                    classes.addElement(cd);
                }
                if (classes.size() > 1)
                    break;
            }
        }
    }

    ClassDefinition getRealOurClass() {
        if (isAbstractOrInterface() && !isObjectOrCloneable()) {
            ObjVector classes = new ObjVector();
            getTwoRealSubclasses(classes);
            if (classes.size() == 1)
                return (ClassDefinition) classes.elementAt(0);
        }
        return this;
    }

    MethodDefinition getSingleRealMethodInSubclasses(MethodDefinition md,
            ClassDefinition[] cdArr) {
        ObjVector methods = new ObjVector();
        getTwoRealMethodsInSubclasses(methods, md.methodSignature()
                .signatureString(), md.isProtectedOrPublic() ? null : md
                .definingClass().getPackageName(), cdArr);
        return methods.size() == 1 ? (MethodDefinition) methods.elementAt(0)
                : null;
    }

    private void getTwoRealMethodsInSubclasses(ObjVector methods,
            String sigString, String pkgName, ClassDefinition[] cdArr) {
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (cd.hasInstances && cd.used) {
                    MethodDefinition md = cd.getMethod(sigString);
                    if (md != null && md.usedExact() && !md.isAbstract()) {
                        if (methods.identityLastIndexOf(md) < 0) {
                            methods.addElement(md);
                            cdArr[0] = cd;
                        }
                    } else {
                        cd.getTwoRealMethodsInSubclasses(methods, sigString,
                                null, cdArr);
                    }
                    if (methods.size() > 1)
                        return;
                }
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            if (cd.hasInstances && cd.used) {
                MethodDefinition md = cd.getMethodNoInheritance(sigString);
                if (md != null
                        && md.usedExact()
                        && !md.isAbstract()
                        && !md.isClassMethod()
                        && (pkgName == null || pkgName.equals(cd
                                .getPackageName()))) {
                    if (methods.identityLastIndexOf(md) < 0) {
                        methods.addElement(md);
                        cdArr[0] = cd;
                    }
                } else {
                    cd.getTwoRealMethodsInSubclasses(methods, sigString,
                            pkgName, cdArr);
                }
                if (methods.size() > 1)
                    break;
            }
        }
    }

    boolean copyObjLeaksInSubclasses(String sigString, Term argsList) {
        ClassDefinition cd;
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                if ((cd = (ClassDefinition) en.nextElement()).hasInstances
                        && cd.used) {
                    MethodDefinition md = cd.getMethod(sigString);
                    if ((md != null && !md.isAbstract() && md
                            .copyObjLeaksTo(argsList))
                            || cd.copyObjLeaksInSubclasses(sigString, argsList))
                        return true;
                }
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            if ((cd = (ClassDefinition) en.nextElement()).hasInstances
                    && cd.used) {
                MethodDefinition md = cd.getMethodNoInheritance(sigString);
                if ((md != null && !md.isAbstract() && md
                        .copyObjLeaksTo(argsList))
                        || cd.copyObjLeaksInSubclasses(sigString, argsList))
                    return true;
            }
        }
        return false;
    }

    boolean hasThisObjLeakInSubclasses(String sigString, boolean isReturned) {
        ClassDefinition cd;
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                if ((cd = (ClassDefinition) en.nextElement()).hasInstances
                        && cd.used) {
                    MethodDefinition md = cd.getMethod(sigString);
                    if ((md != null && !md.isAbstract() && md
                            .hasThisObjLeak(isReturned))
                            || cd.hasThisObjLeakInSubclasses(sigString,
                                    isReturned))
                        return true;
                }
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            if ((cd = (ClassDefinition) en.nextElement()).hasInstances
                    && cd.used) {
                MethodDefinition md = cd.getMethodNoInheritance(sigString);
                if ((md != null && !md.isAbstract() && md
                        .hasThisObjLeak(isReturned))
                        || cd.hasThisObjLeakInSubclasses(sigString, isReturned))
                    return true;
            }
        }
        return false;
    }

    boolean isThisStackObjVltInSubclasses(String sigString) {
        ClassDefinition cd;
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                if ((cd = (ClassDefinition) en.nextElement()).hasInstances
                        && cd.used) {
                    MethodDefinition md = cd.getMethod(sigString);
                    if ((md != null && !md.isAbstract() && md
                            .isThisStackObjVolatile())
                            || cd.isThisStackObjVltInSubclasses(sigString))
                        return true;
                }
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            if ((cd = (ClassDefinition) en.nextElement()).hasInstances
                    && cd.used) {
                MethodDefinition md = cd.getMethodNoInheritance(sigString);
                if ((md != null && !md.isAbstract() && md
                        .isThisStackObjVolatile())
                        || cd.isThisStackObjVltInSubclasses(sigString))
                    return true;
            }
        }
        return false;
    }

    void setWritableArrayRetInSubclasses(String sigString) {
        ClassDefinition cd;
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                if ((cd = (ClassDefinition) en.nextElement()).hasInstances
                        && cd.used) {
                    MethodDefinition md = cd.getMethod(sigString);
                    if (md != null && !md.isAbstract()) {
                        md.setWritableArray();
                    } else {
                        cd.setWritableArrayRetInSubclasses(sigString);
                    }
                }
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            if ((cd = (ClassDefinition) en.nextElement()).hasInstances
                    && cd.used) {
                MethodDefinition md = cd.getMethodNoInheritance(sigString);
                if (md != null && !md.isAbstract()) {
                    md.setWritableArray();
                } else {
                    cd.setWritableArrayRetInSubclasses(sigString);
                }
            }
        }
    }

    Context cloneContextFor(MethodDefinition md) {
        Term.assertCond(definePassOneDone);
        Context c = context.cloneForClass(this, this);
        c.currentMethod = md;
        return c;
    }

    ClassDefinition superClass() {
        return superClass(null);
    }

    ClassDefinition superClass(ClassDefinition forClass) {
        if (!isDefined && !name.equals(Names.JAVA_LANG_OBJECT)) {
            predefineClass(forClass);
            processExtends();
        }
        return superClass;
    }

    private ObjQueue interfaceClasses(ClassDefinition forClass) {
        define(forClass);
        return interfaceClasses;
    }

    VariableDefinition addField(VariableDefinition v) {
        Term.assertCond(fieldDictionary != null);
        return (VariableDefinition) fieldDictionary.put(v.id(), v);
    }

    MethodDefinition addMethod(MethodDefinition md) {
        Term.assertCond(isDefined);
        if (md.isConstructor() && !md.hasParameters()) {
            basicConstructor = md;
        }
        return (MethodDefinition) methodDictionary.put(md.methodSignature()
                .signatureString(), md);
    }

    private OrderedMap methodDictionary() {
        define(null);
        return methodDictionary;
    }

    private OrderedMap fieldDictionary() {
        define(null);
        return fieldDictionary;
    }

    VariableDefinition getOuterField(String fieldName, ClassDefinition forClass) {
        define(forClass);
        return outerFields != null ? (VariableDefinition) outerFields
                .get(fieldName) : null;
    }

    VariableDefinition getField(String fieldName, ClassDefinition forClass) {
        ClassDefinition cd = this;
        do {
            cd.define(forClass);
            VariableDefinition v = cd.getOuterField(fieldName, forClass);
            if ((v != null || (v = (VariableDefinition) cd.fieldDictionary
                    .get(fieldName)) != null)
                    && (cd == this || v.isProtectedOrPublic() || (!v
                            .isPrivate() && cd.getPackageName().equals(
                            getPackageName()))))
                return v;
            Enumeration en = cd.specifiedInterfaces.elements();
            while (en.hasMoreElements()) {
                if ((v = ((ClassDefinition) en.nextElement()).getField(
                        fieldName, forClass)) != null)
                    return v;
            }
        } while (!isInterface() && (cd = cd.superClass) != null);
        return null;
    }

    int countHiddenFields(String fieldName) {
        int hiddenCount = 0;
        ClassDefinition cd = this;
        while ((cd = cd.superClass()) != null) {
            VariableDefinition v = (VariableDefinition) cd.fieldDictionary()
                    .get(fieldName);
            if (v != null && !v.isClassVariable()) {
                hiddenCount++;
            }
        }
        return hiddenCount;
    }

    ClassDefinition getReflectHelperClass() {
        if (helperClass != null)
            return helperClass != this ? helperClass : null;
        String className = topOuterClass().name;
        int pos = className.lastIndexOf('.') + 1;
        String baseName = className.substring(0, pos) + "VMReflector";
        className = className.substring(pos);
        helperClass = Main.dict.getInner(baseName, "_" + className);
        if (helperClass != null) {
            if (!helperClass.isStaticClass()) {
                helperClass.classbody.fatalError(helperClass.context,
                        "An inner class of VMReflector cannot be non-static: "
                                + helperClass.name);
            }
            return helperClass;
        }
        helperClass = Main.dict.getInner(baseName, className);
        if (helperClass != null) {
            helperClass.classbody.fatalError(helperClass.context,
                    "VMReflector inner class name shall begin with '_': "
                            + helperClass.name);
            return helperClass;
        }
        helperClass = this;
        return null;
    }

    private void reflectFieldInner(VariableDefinition v) {
        ClassDefinition cd = v.exprType().signatureClass();
        cd.predefineClass(this);
        v.markUsed();
        v.setChangedSpecial();
        cd.setVTableUsed(true);
        if (reflectedFieldNames == null) {
            reflectedFieldNames = new ObjHashSet();
            Main.dict.buildClassTable = true;
        }
        String fieldName = v.id();
        reflectedFieldNames.add(fieldName);
        Main.dict.addStringLiteral(fieldName, this);
        if (v.isClassVariable()) {
            setVTableUsed(false);
            Main.dict.get(Names.JAVA_LANG_VMCLASS).markUsed();
        }
    }

    ClassDefinition reflectField(String fieldName, boolean declaredOnly,
            boolean isExactType) {
        VariableDefinition v = getField(fieldName, null);
        if (v != null) {
            ClassDefinition classDefn = v.definingClass();
            if (declaredOnly ? classDefn == this : !v.isPrivate()) {
                classDefn.reflectFieldInner(v);
                if (!v.isClassVariable())
                    return null;
                if (!isExactType) {
                    addToInheritedFieldReflected(fieldName);
                }
                return classDefn;
            }
        }
        if (!isExactType) {
            addToInheritedFieldReflected(fieldName);
        }
        return null;
    }

    private void addToInheritedFieldReflected(String fieldName) {
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            if (cd.used) {
                cd.reflectField(fieldName, true, false);
            }
        }
        if (inheritedReflFieldNames == null) {
            inheritedReflFieldNames = new ObjQueue();
        }
        if (!inheritedReflFieldNames.contains(fieldName)) {
            inheritedReflFieldNames.addLast(fieldName);
        }
    }

    private void reflectInheritedFieldsFrom(ClassDefinition cd) {
        if (cd.inheritedReflFieldNames != null) {
            Enumeration en = cd.inheritedReflFieldNames.elements();
            while (en.hasMoreElements()) {
                reflectField((String) en.nextElement(), true, false);
            }
        }
    }

    ClassDefinition reflectAllFields(boolean declaredOnly) {
        ClassDefinition cd = this;
        ClassDefinition classToTrace = null;
        do {
            cd.define(this);
            Enumeration en = cd.fieldDictionary().keys();
            while (en.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) cd.fieldDictionary
                        .get((String) en.nextElement());
                if (declaredOnly || !v.isPrivate()) {
                    v.definingClass().reflectFieldInner(v);
                    if (classToTrace == null && v.isClassVariable()) {
                        classToTrace = cd;
                    }
                }
            }
            if (declaredOnly)
                break;
            Enumeration en2 = cd.specifiedInterfaces.elements();
            while (en2.hasMoreElements()) {
                ((ClassDefinition) en2.nextElement()).reflectAllFields(false);
            }
        } while ((cd = cd.superClass()) != null);
        return classToTrace;
    }

    boolean addFieldOutputName(String outputName) {
        Term.assertCond(fieldCNames != null);
        ClassDefinition aclass = this;
        do {
            if (aclass.fieldCNames.contains(outputName))
                return false;
            aclass = aclass.superClass();
        } while (aclass != null);
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            if (((ClassDefinition) en.nextElement()).fieldCNames
                    .contains(outputName))
                return false;
        }
        fieldCNames.add(outputName);
        return true;
    }

    private void reflectMethodInner(MethodDefinition md) {
        Term.assertCond(md.definingClass() == this);
        if (md.isCopiedFromIface()) {
            Enumeration en = interfaceClasses(null).elements();
            while (en.hasMoreElements()) {
                MethodDefinition md2 = ((ClassDefinition) en.nextElement())
                        .getMethod(md.methodSignature());
                if (md2 != null) {
                    md2.definingClass().reflectMethodInner(md2);
                    return;
                }
            }
        }
        md.markUsedThisOnly();
        md.markUsed(this, true);
        md.markAllTypes(true);
        if (reflectedMethods == null) {
            reflectedMethods = new ObjHashtable();
            Main.dict.buildClassTable = true;
            if (basicConstructor != null && basicConstructor != md
                    && !isInterface() && !basicConstructor.isPrivate()) {
                basicConstructor.markUsed(this, true);
                basicConstructor.markAllTypes(true);
                reflectedMethods.put(basicConstructor.methodSignature()
                        .signatureString(), basicConstructor);
            }
        }
        reflectedMethods.put(md.methodSignature().signatureString(), md);
        if (md.isConstructor()) {
            setHasExactInstance(true);
        } else {
            Main.dict.addStringLiteral(md.id(), this);
            Main.dict.hasReflectedMethods = true;
        }
    }

    int getReflectedMethodSlot(String sigString) {
        if (reflectedMethods != null && reflectedMethods.get(sigString) != null) {
            int slot = 0;
            Enumeration en = methodDictionary.keys();
            while (en.hasMoreElements()) {
                String sign = (String) en.nextElement();
                if (reflectedMethods.get(sign) != null) {
                    if (sigString.equals(sign))
                        return slot;
                    slot++;
                }
            }
        }
        return -1;
    }

    void reflectConstructors(boolean declaredOnly, String sigString,
            boolean isExactType) {
        if (used) {
            if (sigString != null && !sigString.equals("<init>(*)")) {
                MethodDefinition md = getMethodNoInheritance(sigString);
                if (md != null && md.isConstructor()) {
                    reflectMethodInner(md);
                }
            } else {
                Enumeration en = methodDictionary().keys();
                while (en.hasMoreElements()) {
                    MethodDefinition md = getMethodNoInheritance((String) en
                            .nextElement());
                    if (md.isConstructor() && (declaredOnly || !md.isPrivate())) {
                        reflectMethodInner(md);
                    }
                }
            }
        }
        if (!isExactType && reflectConstructorsInherit(sigString)) {
            if (used) {
                addToInheritedReflected(sigString);
                if (implementedBy != null) {
                    Enumeration en = implementedBy.elements();
                    while (en.hasMoreElements()) {
                        ClassDefinition cd = (ClassDefinition) en.nextElement();
                        if (cd.used) {
                            cd.reflectConstructors(declaredOnly, sigString,
                                    false);
                        }
                    }
                }
            }
            Enumeration en = subclasses.elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (cd.used || isInterface()) {
                    cd.reflectConstructors(declaredOnly, sigString, false);
                }
            }
        }
    }

    private boolean reflectConstructorsInherit(String sigString) {
        return sigString != null
                && !isFinal()
                && superClass != null
                && (!isInterface() || (!sigString.equals("<init>(*)") && (!sigString
                        .equals("<init>()") || (!name.startsWith(Names.JAVA_0) && !name
                        .startsWith(Names.JAVAX_0)))));
    }

    private void addToInheritedReflected(String sigString) {
        if (inheritedReflectedSigns == null) {
            inheritedReflectedSigns = new ObjQueue();
        }
        if (!inheritedReflectedSigns.contains(sigString)) {
            inheritedReflectedSigns.addLast(sigString);
        }
    }

    private void reflectInheritedFrom(ClassDefinition cd) {
        if (cd.inheritedReflectedSigns != null) {
            Enumeration en = cd.inheritedReflectedSigns.elements();
            while (en.hasMoreElements()) {
                String sigString = (String) en.nextElement();
                MethodDefinition md;
                if (sigString.equals("<init>(*)")) {
                    Enumeration en2 = methodDictionary().keys();
                    while (en2.hasMoreElements()) {
                        md = getMethodNoInheritance((String) en2.nextElement());
                        if (md.isConstructor() && !md.isPrivate()) {
                            reflectMethodInner(md);
                        }
                    }
                    md = null;
                } else {
                    md = getMethodNoInheritance(sigString);
                    if (md != null) {
                        if (!md.isPrivate() && md.definingClass() == this) {
                            reflectMethodInner(md);
                        } else {
                            md = null;
                        }
                    }
                }
                if (md == null || md.isConstructor() || md.isClassMethod()) {
                    addToInheritedReflected(sigString);
                }
            }
        }
    }

    void reflectMethods(String methodId, boolean declaredOnly,
            ObjVector parmSig, boolean isExactType) {
        if (methodId != null && parmSig != null) {
            String sigString = (new MethodSignature(methodId, parmSig))
                    .signatureString();
            MethodDefinition md = getMethod(sigString);
            if (md != null
                    && !md.isConstructor()
                    && (declaredOnly ? md.definingClass() == this : !md
                            .isPrivate())) {
                md.definingClass().reflectMethodInner(md);
                if (!md.isClassMethod())
                    return;
            }
            if (!isExactType) {
                addToInheritedReflected(sigString);
                if (implementedBy != null) {
                    Enumeration en = implementedBy.elements();
                    while (en.hasMoreElements()) {
                        ClassDefinition cd = (ClassDefinition) en.nextElement();
                        if (cd.used) {
                            cd.reflectMethods(methodId, declaredOnly, parmSig,
                                    false);
                        }
                    }
                }
                reflectMethodsInSubclasses(declaredOnly, methodId, parmSig);
            }
        } else {
            ClassDefinition cd = this;
            do {
                cd.define(this);
                Enumeration en = cd.methodDictionary().keys();
                while (en.hasMoreElements()) {
                    MethodDefinition md = cd.getMethodNoInheritance((String) en
                            .nextElement());
                    if (!md.isConstructor()
                            && (methodId == null || methodId.equals(md.id()))
                            && (parmSig == null || md.methodSignature()
                                    .isSignEqual(parmSig))) {
                        ClassDefinition classDefn = md.definingClass();
                        if (declaredOnly ? classDefn == this : !md.isPrivate()) {
                            classDefn.reflectMethodInner(md);
                        }
                    }
                }
                if (declaredOnly)
                    break;
                cd = cd.superClass();
            } while (cd != null
                    && (!isInterface() || !cd.name
                            .equals(Names.JAVA_LANG_OBJECT)));
        }
    }

    private void reflectMethodsInSubclasses(boolean declaredOnly,
            String methodId, ObjVector parmSig) {
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            if (cd.used) {
                cd.reflectMethods(methodId, declaredOnly, parmSig, false);
            } else if (isInterface()) {
                cd.reflectMethodsInSubclasses(declaredOnly, methodId, parmSig);
            }
        }
    }

    private MethodDefinition getMethodNoInheritance(String sigString) {
        return (MethodDefinition) methodDictionary().get(sigString);
    }

    MethodDefinition getMethodNoInheritance(MethodSignature msig) {
        return (MethodDefinition) methodDictionary()
                .get(msig.signatureString());
    }

    MethodDefinition getMethod(MethodSignature msig) {
        return getMethod(msig.signatureString());
    }

    MethodDefinition getMethod(String sigString) {
        define(null);
        MethodDefinition md = (MethodDefinition) methodDictionary
                .get(sigString);
        if (md != null)
            return md;
        ClassDefinition aclass = isInterface() ? Main.dict
                .get(Names.JAVA_LANG_OBJECT) : superClass();
        if (aclass != null) {
            String pkgName = getPackageName();
            do {
                aclass.define(this);
                Term.assertCond(aclass.definePassOneDone);
                md = aclass.getMethodNoInheritance(sigString);
                if (md != null
                        && !md.isPrivate()
                        && !md.isConstructor()
                        && (md.isProtectedOrPublic() || aclass.getPackageName()
                                .equals(pkgName)))
                    return md;
            } while ((aclass = aclass.superClass()) != null);
        }
        return null;
    }

    private MethodDefinition getMethodSamePkg(String sigString, String pkgName) {
        ClassDefinition aclass = this;
        do {
            MethodDefinition md = aclass.getMethodNoInheritance(sigString);
            if (md != null && !md.isPrivate() && !md.isClassMethod()
                    && aclass.getPackageName().equals(pkgName))
                return aclass != this && md.isProtectedOrPublic() ? getMethod(sigString)
                        : md;
            aclass = aclass.superClass();
        } while (aclass != null);
        return null;
    }

    MethodDefinition getSameMethod(MethodDefinition md) {
        return md.isProtectedOrPublic() ? getMethod(md.methodSignature())
                : getMethodSamePkg(md.methodSignature().signatureString(), md
                        .definingClass().getPackageName());
    }

    int countHiddenMethods(String sigString) {
        ObjVector pkgNameList = new ObjVector();
        ClassDefinition aclass = this;
        String ourPkgName = getPackageName();
        pkgNameList.addElement(ourPkgName);
        while ((aclass = aclass.superClass()) != null) {
            MethodDefinition md = aclass.getMethodNoInheritance(sigString);
            if (md != null && md.used() && md.allowVirtual()
                    && !md.isProtectedOrPublic()) {
                String pkgName = aclass.getPackageName();
                int i = pkgNameList.indexOf(pkgName);
                if (pkgNameList.size() - 1 > i) {
                    if (i >= 0) {
                        pkgNameList.removeElementAt(i);
                    }
                    pkgNameList.addElement(pkgName);
                }
            }
        }
        return pkgNameList.size() - pkgNameList.indexOf(ourPkgName) - 1;
    }

    MethodDefinition matchMethod(MethodSignature msig, ClassDefinition forClass) {
        define(forClass);
        Term.assertCond(definePassOneDone);
        MethodDefinition bestMethod = getMethod(msig);
        if (bestMethod == null) {
            int lowestMatch = -1 >>> 1;
            ClassDefinition cd = this;
            do {
                Enumeration en = cd.methodDictionary().keys();
                while (en.hasMoreElements()) {
                    MethodDefinition md2 = getMethod((String) en.nextElement());
                    if (md2 != null) {
                        int value = md2.methodSignature().match(msig, forClass);
                        if (value < lowestMatch) {
                            lowestMatch = value;
                            bestMethod = md2;
                        }
                    }
                }
                cd = cd.superClass();
            } while (cd != null);
        }
        return bestMethod;
    }

    MethodDefinition matchConstructor(ObjVector parmSig,
            ClassDefinition forClass) {
        MethodSignature msig = new MethodSignature("<init>", parmSig);
        define(forClass);
        MethodDefinition bestMethod = getMethodNoInheritance(msig
                .signatureString());
        if (bestMethod == null || !bestMethod.isConstructor()) {
            int lowestMatch = -1 >>> 1;
            bestMethod = null;
            Enumeration en = methodDictionary().keys();
            while (en.hasMoreElements()) {
                MethodDefinition md2 = getMethodNoInheritance((String) en
                        .nextElement());
                int value;
                if (md2.isConstructor()
                        && (value = md2.methodSignature().match(msig, forClass)) < lowestMatch) {
                    lowestMatch = value;
                    bestMethod = md2;
                }
            }
        }
        return bestMethod;
    }

    int producePassOne() {
        if (type != Type.CLASSINTERFACE)
            return 0;
        int count = 0;
        if (!isDefined) {
            if (!predefining)
                return 0;
            if (define(fromClass) > 0) {
                count++;
            }
        }
        if (checkMarkIfDefined()) {
            count++;
        }
        if (used) {
            if (Main.dict.verboseTracing) {
                Main.dict.message("Processing used class: " + name);
            }
            if (needStaticInitPass) {
                needStaticInitPass = false;
                if (classbody.staticInitializerPass(null, true) != null) {
                    count++;
                }
                if (classbody.staticInitializerPass(null, false) != null) {
                    count++;
                }
            }
            Enumeration en = methodDictionary().keys();
            while (en.hasMoreElements()) {
                MethodDefinition md = getMethodNoInheritance((String) en
                        .nextElement());
                if (md.usedExact() && md.producePassOne(null) != null) {
                    count++;
                }
            }
            Enumeration en2 = fieldDictionary.keys();
            while (en2.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) fieldDictionary
                        .get(en2.nextElement());
                if (v.used() || v.isInitializerUsedOnly()) {
                    count += v.producePassOne();
                }
            }
        }
        return count;
    }

    OutputContext outputContext() {
        return outputContext;
    }

    private void fixClassInitializers() {
        Term.assertCond(used);
        if (classInitializers != null) {
            if (classInitializers.hasSomeCode()) {
                setVTableUsed(false);
                if (superClass != null && !name.equals(Names.JAVA_LANG_CLASS))
                    return;
                classbody.fatalError(context,
                        "Object and Class cannot have <clinit>()");
            }
            initializerDiscoverObjLeaks(classInitializers);
            classInitializers = null;
        }
        if (!isInterface() && superClass != null) {
            if (superClass.superClass != null) {
                superClass.fixClassInitializers();
            }
            if (superClass.classInitializers != null
                    || name.equals(Names.JAVA_LANG_STRING)
                    || name.equals(Names.JAVA_LANG_SYSTEM)) {
                classInitializers = new InitializerPart(null, Empty.newTerm());
                setVTableUsed(false);
            }
        }
    }

    void prepareMethodsForOutput() {
        Term.assertCond(used);
        Main.dict.message("Preparing methods in: " + name);
        fixClassInitializers();
        if (classInitializers != null) {
            initializerDiscoverObjLeaks(classInitializers);
        }
        discoverInstanceObjLeaks();
        if (reflectedFieldNames != null) {
            Enumeration en = fieldDictionary.keys();
            while (en.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) fieldDictionary
                        .get((String) en.nextElement());
                if (v.isClassVariable() && reflectedFieldNames.contains(v.id())
                        && v.exprType().objectSize() >= Type.CLASSINTERFACE) {
                    Term.assertCond(v.used());
                    v.setWritableArray(null);
                }
            }
        }
        Enumeration en = methodDictionary().keys();
        while (en.hasMoreElements()) {
            MethodDefinition md = getMethodNoInheritance((String) en
                    .nextElement());
            if (md.usedExact()) {
                md.discoverObjLeaks();
            }
        }
    }

    void addInclassCall(MethodDefinition md) {
        if (inclassCalls == null) {
            inclassCalls = new OrderedMap();
        }
        inclassCalls.put(md, "");
    }

    private void produceOutput() {
        writeHeaders();
        if (reflectedMethods != null) {
            setVirtualForReflectedMethods();
        }
        produceAllFields();
        writeInstanceInitializer();
        if (inclassCalls != null) {
            Enumeration en = inclassCalls.keys();
            while (en.hasMoreElements()) {
                MethodDefinition md = (MethodDefinition) en.nextElement();
                if (md.usedExact() && (md.isConstructor() || md.allowInline())) {
                    md.produceOutput(outputContext);
                }
            }
        }
        Enumeration en = methodDictionary().keys();
        while (en.hasMoreElements()) {
            MethodDefinition md = getMethodNoInheritance((String) en
                    .nextElement());
            if (md.usedExact()) {
                md.produceOutput(outputContext);
            }
        }
        writeClassInitializer();
        outputContext.fileClose();
    }

    private void setVirtualForReflectedMethods() {
        Enumeration en = methodDictionary().keys();
        while (en.hasMoreElements()) {
            String sigString = (String) en.nextElement();
            if (reflectedMethods.get(sigString) != null) {
                MethodDefinition md = getMethodNoInheritance(sigString);
                if (md.allowOverride()) {
                    md.setVirtual();
                }
            }
        }
    }

    private void produceAllFields() {
        if (superClass != null) {
            lastObjectRefField = superClass.lastObjectRefField;
        }
        int size = fieldDictionary().size();
        if (size > 0) {
            Enumeration en = fieldDictionary.keys();
            while (en.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) fieldDictionary
                        .get(en.nextElement());
                if (v.isClassVariable() && v.needsOutputFor()) {
                    v.produceOutput(outputContext);
                }
            }
            Enumeration en2 = fieldDictionary.keys();
            VariableDefinition[] fields = new VariableDefinition[size];
            size = 0;
            while (en2.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) fieldDictionary
                        .get(en2.nextElement());
                if (!v.isClassVariable() && v.needsOutputFor()) {
                    fields[size++] = v;
                }
            }
            VariableDefinition.sortBySize(fields, size);
            if (name.equals(Names.JAVA_LANG_CLASS)) {
                VariableDefinition.orderFirstFields(fields, size,
                        Names.fieldsOrderClass);
            } else if (name.equals(Names.JAVA_LANG_STRING)) {
                VariableDefinition.orderFirstFields(fields, size,
                        Names.fieldsOrderString);
            }
            for (int i = 0; i < size; i++) {
                VariableDefinition v = fields[i];
                v.produceOutput(outputContext);
                if (v.exprType().objectSize() >= Type.CLASSINTERFACE
                        && v.used()) {
                    lastObjectRefField = v;
                }
            }
        }
    }

    boolean hasInstanceInitializers() {
        return instanceInitializers != null
                && instanceInitializers.hasSomeCode();
    }

    void writeInstanceInitCall(OutputContext oc) {
        Term.assertCond(used && hasInstances);
        if (superClass != null && instanceInitializers != null) {
            oc.cPrint(";");
            oc.cPrint(cname);
            oc.cPrint("__void(");
            oc.cPrint(This.CNAME);
            oc.cPrint(")");
        }
    }

    private void writeInstanceInitializer() {
        if (instanceInitializers != null && !isInterface()
                && superClass != null && hasInstances && used) {
            outputContext.cAndHPrint("JCGO_NOSEP_INLINE ");
            outputContext.cAndHPrint(Type.cName[Type.VOID]);
            outputContext.cAndHPrint(" CFASTCALL\n");
            outputContext.cAndHPrint(cname);
            outputContext.cAndHPrint("__void( ");
            outputContext.cAndHPrint(cname);
            outputContext.cAndHPrint(" ");
            outputContext.cAndHPrint(This.CNAME);
            outputContext.cAndHPrint(" )");
            outputContext.hPrint(";\n\n");
            outputContext.cPrint("{");
            if (instanceInitializers.hasSomeCode()) {
                Main.dict.message("Writing instance initializer: " + name);
                int[] curRcvrs = new int[Type.VOID];
                instanceInitializers.allocRcvr(curRcvrs);
                outputContext.writeRcvrsVar(curRcvrs);
                outputContext.stackObjCountReset();
                instanceInitializers.processOutput(outputContext);
            } else {
                instanceInitializers = null;
            }
            outputContext.cPrint("}\n\n");
        }
    }

    private void writeClassInitializer() {
        if (classInitializers != null) {
            Main.dict.message("Writing static initializer: " + name);
            int[] curRcvrs = new int[Type.VOID];
            classInitializers.allocRcvr(curRcvrs);
            outputContext.cPrint("JCGO_NOSEP_STATIC ");
            outputContext.cPrint(Type.cName[Type.VOID]);
            outputContext.cPrint(" CFASTCALL\n");
            outputContext.cPrint(clinitCName());
            outputContext.cPrint("( ");
            outputContext.cPrint(Type.cName[Type.VOID]);
            outputContext.cPrint(" ){");
            outputContext.writeRcvrsVar(curRcvrs);
            outputContext.stackObjCountReset();
            outputContext.cPrint("JCGO_CLINIT_BEGIN(");
            outputContext.cPrint(cname);
            outputContext.cPrint("__class);");
            if (!isInterface() && superClass != null) {
                superClass.writeTrigClinit(outputContext);
            }
            classInitializers.processOutput(outputContext);
            outputContext.cPrint("JCGO_CLINIT_DONE(");
            outputContext.cPrint(cname);
            outputContext.cPrint("__class);}\n\n");
        }
        if (Main.dict.buildClassTable) {
            vTableUsed = true;
        }
    }

    private static void initializerDiscoverObjLeaks(InitializerPart initializers) {
        ObjQueue oldStackObjRetCalls = Main.dict.stackObjRetCalls;
        Main.dict.stackObjRetCalls = new ObjQueue();
        MethodDefinition oldOurMethod = Main.dict.ourMethod;
        Main.dict.ourMethod = null;
        ObjHashtable oldAssignedLocals = Main.dict.assignedLocals;
        Main.dict.assignedLocals = new ObjHashtable();
        initializers.discoverObjLeaks();
        Main.dict.assignedLocals = oldAssignedLocals;
        Term.assertCond(Main.dict.stackObjRetCalls != null);
        MethodDefinition.setRequiredStackObjRets(Main.dict.stackObjRetCalls);
        Main.dict.ourMethod = oldOurMethod;
        Main.dict.stackObjRetCalls = oldStackObjRetCalls;
    }

    boolean discoverInstanceObjLeaks() {
        if (!leaksDiscoverDone) {
            if (leaksDiscoverProcessing)
                return false;
            Term.assertCond(used && !needStaticInitPass);
            if (instanceInitializers != null) {
                leaksDiscoverProcessing = true;
                initializerDiscoverObjLeaks(instanceInitializers);
            }
            leaksDiscoverDone = true;
            leaksDiscoverProcessing = false;
        }
        return !hasInstanceInitLeaks;
    }

    void setInstanceInitLeaks() {
        Term.assertCond(leaksDiscoverProcessing);
        hasInstanceInitLeaks = true;
    }

    void setInitThisStackObjVolatile() {
        Term.assertCond(leaksDiscoverProcessing);
        isInitThisStackObjVolatile = true;
    }

    boolean isInitThisStackObjVolatile() {
        return isInitThisStackObjVolatile;
    }

    private void writeHeaders() {
        Term.assertCond(isDefined);
        if (!headerWritten) {
            headerWritten = true;
            outputContext.cPrint("#ifdef JCGO_SEPARATED\010");
            outputContext.cPrint("#define ");
            outputContext.cPrint(Main.VER_ABBR);
            outputContext.cPrint("\010");
            outputContext.cPrint("#include \"jcgortl.h\"\010");
            outputContext.cPrint("#include \"Main.h\"\010");
            outputContext.cPrint("#endif\n\n");
            outputContext.cAndHPrint("#ifdef ");
            outputContext.cAndHPrint(Main.VER_ABBR);
            outputContext.cAndHPrint("\n\n");
            outputContext.cPrint("#ifdef CHKALL_");
            outputContext.cPrint(topOuterClass().jniname);
            outputContext.cPrint("\010");
            outputContext.cPrint("#include \"jcgobchk.h\"\010");
            outputContext.cPrint("#endif\n\n");
        }
    }

    void finishClass() {
        if (!finished && type == Type.CLASSINTERFACE) {
            if (superClass != null) {
                superClass.finishClass();
            }
            if (used) {
                Main.dict.message("Finishing output for class: " + name);
                if (reflectedFieldNames != null || reflectedMethods != null) {
                    outputReflectionInfo();
                }
                finished = true;
                startMethodDefinitions();
                produceObjectJumpTable();
                if (superClass != null) {
                    if (methodTableSigns == null) {
                        ObjVector ifaces = new ObjVector();
                        gatherChainedIfaces(ifaces, new ObjHashSet());
                        prepareIfaceJumpTables(ifaces);
                        if (methodTableSigns == null) {
                            methodTableSigns = new ObjVector();
                        }
                    }
                    if (!isInterface() || methodTableSigns.size() == 0) {
                        prepareClassJumpTable();
                    }
                    produceJumpTable();
                }
                outputContext.hPrint("};\n\n");
                writeClassVariables();
                if (superClass == null) {
                    outputCoreClasses();
                }
                outputPool();
                outputContext.cPrint("#ifdef CHKALL_");
                outputContext.cPrint(topOuterClass().jniname);
                outputContext.cPrint("\010");
                outputContext.cPrint("#include \"jcgochke.h\"\010");
                outputContext.cPrint("#endif\n\n");
                outputContext.cAndHPrint("#endif\n");
                outputContext.close();
            }
        }
    }

    private void produceObjectJumpTable() {
        ClassDefinition objectClass = Main.dict.get(Names.JAVA_LANG_OBJECT);
        Enumeration en = objectClass.methodDictionary().keys();
        while (en.hasMoreElements()) {
            String sigString = (String) en.nextElement();
            if (objectClass.getMethodNoInheritance(sigString).isVirtualUsed()) {
                MethodDefinition md = getMethod(sigString);
                if (md.isVirtualUsed()) {
                    md.produceJumpTableEntry(this, false);
                } else {
                    writeDummyJumpEntry();
                }
            }
        }
    }

    private void gatherChainedIfaces(ObjVector ifaces, ObjHashSet processed) {
        if (hasInstances && superClass != null) {
            if (processed.add(this)) {
                if (isInterface()) {
                    if (!hasUsedIfaceMethods())
                        return;
                    if (used) {
                        ifaces.addElement(this);
                    }
                } else {
                    if (!used)
                        return;
                    superClass.gatherChainedIfaces(ifaces, processed);
                }
                if (methodTableSigns == null) {
                    methodTableSigns = new ObjVector();
                }
                Enumeration en = specifiedInterfaces.elements();
                while (en.hasMoreElements()) {
                    ((ClassDefinition) en.nextElement()).gatherChainedIfaces(
                            ifaces, processed);
                }
                if (used) {
                    Enumeration en2 = subclasses.elements();
                    while (en2.hasMoreElements()) {
                        ((ClassDefinition) en2.nextElement())
                                .gatherChainedIfaces(ifaces, processed);
                    }
                    if (implementedBy != null) {
                        Enumeration en3 = implementedBy.elements();
                        while (en3.hasMoreElements()) {
                            ((ClassDefinition) en3.nextElement())
                                    .gatherChainedIfaces(ifaces, processed);
                        }
                    }
                }
            } else {
                int i;
                if (isInterface()
                        && (i = ifaces.identityLastIndexOf(this)) >= 0) {
                    ifaces.removeElementAt(i);
                    ifaces.addElement(this);
                }
            }
        }
    }

    private boolean hasUsedIfaceMethods() {
        if (used) {
            ClassDefinition objectClass = Main.dict.get(Names.JAVA_LANG_OBJECT);
            Enumeration en = methodDictionary().unorderedElements();
            while (en.hasMoreElements()) {
                MethodDefinition md = (MethodDefinition) en.nextElement();
                MethodDefinition md2;
                if (md.used()
                        && ((md2 = objectClass.getMethodNoInheritance(md
                                .methodSignature())) == null || !md2
                                .isVirtualUsed()))
                    return true;
            }
            Enumeration en2 = specifiedInterfaces.elements();
            while (en2.hasMoreElements()) {
                if (((ClassDefinition) en2.nextElement()).hasUsedIfaceMethods())
                    return true;
            }
        }
        return false;
    }

    private static void prepareIfaceJumpTables(ObjVector ifaces) {
        ClassDefinition objectClass = Main.dict.get(Names.JAVA_LANG_OBJECT);
        int i = ifaces.size();
        while (i-- > 0) {
            ClassDefinition cd = (ClassDefinition) ifaces.elementAt(i);
            Term.assertCond(cd.methodTableSigns != null);
            Enumeration en = cd.methodDictionary().keys();
            while (en.hasMoreElements()) {
                String sigString = (String) en.nextElement();
                MethodDefinition md2;
                if (cd.getMethodNoInheritance(sigString).used()
                        && ((md2 = objectClass
                                .getMethodNoInheritance(sigString)) == null || !md2
                                .isVirtualUsed())
                        && cd.methodTableSigns.indexOf(sigString) < 0) {
                    fillJumpTableEntryFor(
                            getJumpTableIndexFor(sigString, ifaces, i),
                            sigString, ifaces, i);
                }
            }
        }
    }

    private static int getJumpTableIndexFor(String sigString, ObjVector ifaces,
            int i) {
        int index = 0;
        boolean[] changedArr = new boolean[1];
        ObjHashSet processed = new ObjHashSet();
        do {
            processed.clear();
            changedArr[0] = false;
            index = ((ClassDefinition) ifaces.elementAt(i))
                    .guessEmptyJumpTableIndex(index, sigString, changedArr,
                            processed);
            if (!changedArr[0]) {
                int j = i;
                while (j-- > 0) {
                    ClassDefinition cd = (ClassDefinition) ifaces.elementAt(j);
                    MethodDefinition md = cd.getMethodNoInheritance(sigString);
                    if (md != null && md.used()) {
                        index = cd.guessEmptyJumpTableIndex(index, sigString,
                                changedArr, processed);
                    }
                }
            }
        } while (changedArr[0]);
        return index;
    }

    private int guessEmptyJumpTableIndex(int index, String sigString,
            boolean[] changedArr, ObjHashSet processed) {
        if (used && processed.add(this)) {
            if (methodTableSigns == null) {
                methodTableSigns = new ObjVector();
            }
            for (int size = methodTableSigns.size(); index < size; index++) {
                if ("".equals(methodTableSigns.elementAt(index)))
                    break;
            }
            int oldIndex = index;
            Enumeration en = subclasses.elements();
            while (en.hasMoreElements()) {
                index = ((ClassDefinition) en.nextElement())
                        .guessEmptyJumpTableIndex(index, sigString, changedArr,
                                processed);
            }
            if (implementedBy != null) {
                Enumeration en2 = implementedBy.elements();
                while (en2.hasMoreElements()) {
                    index = ((ClassDefinition) en2.nextElement())
                            .guessEmptyJumpTableIndex(index, sigString,
                                    changedArr, processed);
                }
            }
            if (!isInterface()) {
                ClassDefinition sc = superClass;
                while (sc.superClass != null) {
                    MethodDefinition md = sc.getMethodNoInheritance(sigString);
                    if (md != null && md.isVirtualUsed()
                            && md.isProtectedOrPublic()) {
                        index = sc.guessEmptyJumpTableIndex(index, sigString,
                                changedArr, processed);
                    }
                    sc = sc.superClass;
                }
            }
            if (index > oldIndex) {
                changedArr[0] = true;
            }
        }
        return index;
    }

    private int getClassEmptyJumpTableIndex(int index) {
        if (used) {
            if (methodTableSigns == null) {
                methodTableSigns = new ObjVector();
            }
            int oldIndex;
            do {
                for (int size = methodTableSigns.size(); index < size; index++) {
                    if ("".equals(methodTableSigns.elementAt(index)))
                        break;
                }
                oldIndex = index;
                Enumeration en = subclasses.elements();
                while (en.hasMoreElements()) {
                    index = ((ClassDefinition) en.nextElement())
                            .getClassEmptyJumpTableIndex(index);
                }
            } while (index > oldIndex);
        }
        return index;
    }

    private static void fillJumpTableEntryFor(int index, String sigString,
            ObjVector ifaces, int i) {
        ((ClassDefinition) ifaces.elementAt(i)).fillJumpTableAt(index,
                sigString);
        while (i-- > 0) {
            ClassDefinition cd = (ClassDefinition) ifaces.elementAt(i);
            MethodDefinition md = cd.getMethodNoInheritance(sigString);
            if (md != null && md.used()) {
                cd.fillJumpTableAt(index, sigString);
            }
        }
    }

    private void fillJumpTableAt(int index, String sigString) {
        if (used && fillJumpTableOneEntry(index, sigString)) {
            Enumeration en = subclasses.elements();
            while (en.hasMoreElements()) {
                ((ClassDefinition) en.nextElement()).fillJumpTableAt(index,
                        sigString);
            }
            if (implementedBy != null) {
                Enumeration en2 = implementedBy.elements();
                while (en2.hasMoreElements()) {
                    ((ClassDefinition) en2.nextElement()).fillJumpTableAt(
                            index, sigString);
                }
            }
            if (!isInterface()) {
                ClassDefinition sc = superClass;
                while (sc.superClass != null) {
                    MethodDefinition md = sc.getMethodNoInheritance(sigString);
                    if (md != null && md.isVirtualUsed()
                            && md.isProtectedOrPublic()) {
                        sc.fillJumpTableAt(index, sigString);
                    }
                    sc = sc.superClass;
                }
            }
        }
    }

    private boolean fillJumpTableOneEntry(int index, String sigString) {
        Term.assertCond(methodTableSigns != null);
        for (int size = methodTableSigns.size(); size <= index; size++) {
            methodTableSigns.addElement("");
        }
        String str = (String) methodTableSigns.elementAt(index);
        if (sigString.equals(str))
            return false;
        Term.assertCond(str != null && str.length() == 0);
        methodTableSigns.setElementAt(sigString, index);
        return true;
    }

    private void fillClassJumpTableAt(int index, String sigString) {
        if (used) {
            fillJumpTableOneEntry(index, sigString);
            Enumeration en = subclasses.elements();
            while (en.hasMoreElements()) {
                ((ClassDefinition) en.nextElement()).fillClassJumpTableAt(
                        index, sigString);
            }
        }
    }

    private void prepareClassJumpTable() {
        ClassDefinition objectClass = Main.dict.get(Names.JAVA_LANG_OBJECT);
        Enumeration en = methodDictionary().keys();
        while (en.hasMoreElements()) {
            String sigString = (String) en.nextElement();
            MethodDefinition md = getMethodNoInheritance(sigString);
            Term.assertCond(md != null);
            MethodDefinition md2;
            if (md.isVirtualUsed()
                    && methodTableSigns.indexOf(sigString) < 0
                    && ((md2 = objectClass.getMethodNoInheritance(sigString)) == null || !md2
                            .isVirtualUsed())
                    && (md.isProtectedOrPublic() || methodTableSigns
                            .indexOf(sigString = getPackageName() + ":"
                                    + sigString) < 0)) {
                fillClassJumpTableAt(getClassEmptyJumpTableIndex(0), sigString);
            }
        }
    }

    private void produceJumpTable() {
        Term.assertCond(methodTableSigns != null);
        int dummyCnt = 0;
        Enumeration en = methodTableSigns.elements();
        while (en.hasMoreElements()) {
            String sigString = (String) en.nextElement();
            int pos = sigString.indexOf(':', 0);
            MethodDefinition md;
            if (pos >= 0) {
                String pkgName = sigString.substring(0, pos);
                sigString = sigString.substring(pos + 1);
                md = getMethodSamePkg(sigString, pkgName);
            } else {
                md = getMethod(sigString);
            }
            if (md != null && md.isVirtualUsed()) {
                while (dummyCnt-- > 0) {
                    writeDummyJumpEntry();
                }
                md.produceJumpTableEntry(this,
                        pos >= 0 && methodTableSigns.indexOf(sigString) >= 0
                                && md.isProtectedOrPublic());
            }
            dummyCnt++;
        }
    }

    String nextDummyEntryName() {
        return "jcgo_dummy" + Integer.toString(++dummyEntryCnt);
    }

    private void writeDummyJumpEntry() {
        Term.assertCond(used);
        outputContext.hPrint(Type.cName[Type.VOID]);
        outputContext.hPrint(" (CFASTCALL *");
        outputContext.hPrint(nextDummyEntryName());
        outputContext.hPrint(")( ");
        outputContext.hPrint(Type.cName[Type.VOID]);
        outputContext.hPrint(" );");
        if (!isNotInstantated()) {
            Term.assertCond(vTableUsed);
            outputContext.cPrint(",\010");
            outputContext.cPrint("0");
        }
    }

    boolean isReflectedField(String fieldName) {
        return reflectedFieldNames != null
                && reflectedFieldNames.contains(fieldName);
    }

    private boolean hasReflectedStaticFields(boolean isNonPrimOnly) {
        Term.assertCond(used);
        if (reflectedFieldNames != null) {
            Enumeration en = reflectedFieldNames.elements();
            while (en.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) fieldDictionary
                        .get(en.nextElement());
                Term.assertCond(v != null && v.used());
                if (v.isClassVariable()
                        && (!isNonPrimOnly || v.exprType().objectSize() >= Type.CLASSINTERFACE))
                    return true;
            }
        }
        return false;
    }

    private int buildIfacesArrContent(StringBuffer sb) {
        int ifaceCnt = 0;
        Enumeration en = specifiedInterfaces.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            if (cd.vTableUsed) {
                if (cd.used) {
                    sb.append('(').append(Type.cName[Type.CLASSINTERFACE])
                            .append(')');
                    sb.append(cd.getClassRefStr(false)).append(", ");
                    ifaceCnt++;
                } else {
                    ifaceCnt += cd.buildIfacesArrContent(sb);
                }
            }
        }
        return ifaceCnt;
    }

    private void writeClassVariables() {
        Term.assertCond(used);
        ClassDefinition classClassDefn = Main.dict.get(Names.JAVA_LANG_CLASS);
        if (vTableUsed) {
            outputContext.cPrint("};\n\n");
            boolean isNonGc = false;
            boolean allowConst = false;
            if (name.equals(Names.JAVA_LANG_STRING)) {
                classClassDefn.writeInstanceDefinition(outputContext);
                outputContext.cPrint("JCGO_NOSEP_GCDATA ");
            } else if (Main.dict.allowConstClass >= 0
                    && !hasReflectedStaticFields(true)) {
                outputContext.cPrint("JCGO_NOSEP_DATA ");
                isNonGc = true;
                if (classInitializers == null && Main.dict.allowConstClass > 0
                        && !hasReflectedStaticFields(false)) {
                    allowConst = true;
                    outputContext.cPrint("JCGO_NOTHR_CONST ");
                }
            } else {
                outputContext.cPrint("JCGO_NOSEP_GCDATA ");
            }
            outputContext.cAndHPrint("struct ");
            outputContext.cAndHPrint(cname);
            outputContext.cAndHPrint("_class_s");
            outputContext.cPrint(" ");
            outputContext.cPrint(cname);
            outputContext.cPrint("__class");
            outputContext.cPrint(allowConst ? " JCGO_THRD_ATTRNONGC"
                    : isNonGc ? " ATTRIBNONGC" : " ATTRIBGCDATA");
            outputContext.cPrint("=");
            outputContext.cAndHPrint("{");
            outputContext.hPrint("struct ");
            outputContext.hPrint(classClassDefn.cname);
            outputContext.hPrint("_s jcgo_class;");
            outputContext.cPrint("{&");
            outputContext.cPrint(classClassDefn.vTableCName());
            outputContext.cPrint(",\010");
            outputContext.cPrint("JCGO_MON_INIT\010");
            outputContext.cPrint("JCGO_OBJREF_OF(*(");
            outputContext.cPrint(Main.dict.get(Names.JAVA_LANG_OBJECT).cname);
            outputContext.cPrint(")&");
            outputContext.cPrint(vTableCName());
            outputContext.cPrint("),\010");
            outputContext.cPrint(Main.dict.classNameStringOutput(name, this,
                    false));
            outputContext.cPrint(",\010");
            outputContext
                    .cPrint(!isInterface() && superClass != null ? superClass
                            .getClassRefStr(false) : LexTerm.NULL_STR);
            outputContext.cPrint(",\010");
            StringBuffer sb = new StringBuffer();
            int ifaceCnt = buildIfacesArrContent(sb);
            outputContext.cPrint(addImmutableArray(classClassDefn,
                    sb.toString(), ifaceCnt));
            outputContext.cPrint(",\010");
            outputContext.cPrint("0x");
            outputContext.cPrint(Integer
                    .toHexString((isAbstractOrInterface() ? modifiers
                            | AccModifier.ABSTRACT : modifiers)
                            | (classInitializers != null ? AccModifier.VOLATILE
                                    | AccModifier.TRANSIENT : 0)
                            | (isEnum() ? AccModifier.ENUM : 0)));
            outputContext.cPrint("}");
            if (reflectedFieldNames != null) {
                VariableDefinition[] fields = new VariableDefinition[reflectedFieldNames
                        .size()];
                int size = 0;
                Enumeration en2 = fieldDictionary.keys();
                while (en2.hasMoreElements()) {
                    VariableDefinition v = (VariableDefinition) fieldDictionary
                            .get(en2.nextElement());
                    if (v.used() && v.isClassVariable()
                            && reflectedFieldNames.contains(v.id())) {
                        fields[size++] = v;
                    }
                }
                VariableDefinition.sortBySize(fields, size);
                for (int i = 0; i < size; i++) {
                    outputContext.cPrint(",\010");
                    fields[i].outputClassVar(outputContext, true);
                }
            }
            outputContext.cAndHPrint("};\n\n");
            outputContext.hPrint("JCGO_SEP_EXTERN CONST struct ");
            outputContext.hPrint(isNotInstantated() ? "jcgo_methods_s "
                    : vTableCName() + "_s ");
            outputContext.hPrint(vTableCName());
            outputContext.hPrint(";");
            outputContext.hPrint(isNonGc ? "JCGO_SEP_EXTERN"
                    : "JCGO_SEP_GCEXTERN");
            if (allowConst) {
                outputContext.hPrint(" JCGO_NOTHR_CONST");
            }
            outputContext.hPrint(" struct ");
            outputContext.hPrint(cname);
            outputContext.hPrint("_class_s ");
            outputContext.hPrint(cname);
            outputContext.hPrint("__class;\n\n");
            if (classInitializers != null) {
                if (name.equals(Names.JAVA_LANG_STRING)) {
                    outputContext.hPrint("JCGO_SEP_GCEXTERN");
                    outputContext.cPrint("JCGO_NOSEP_GCDATA");
                    outputContext.cAndHPrint(" int jcgo_initialized");
                    outputContext.cPrint(" ATTRIBGCBSS= 0");
                    outputContext.cAndHPrint(";\n\n");
                }
                outputContext.hPrint("JCGO_NOSEP_STATIC ");
                outputContext.hPrint(Type.cName[Type.VOID]);
                outputContext.hPrint(" CFASTCALL\n");
                outputContext.hPrint(clinitCName());
                outputContext.hPrint("( ");
                outputContext.hPrint(Type.cName[Type.VOID]);
                outputContext.hPrint(" );\n\n");
            }
        }
        int size = fieldDictionary().size();
        if (size > 0) {
            Enumeration en = fieldDictionary.keys();
            VariableDefinition[] fields = new VariableDefinition[size];
            size = 0;
            while (en.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) fieldDictionary
                        .get(en.nextElement());
                if (v.used()
                        && v.isClassVariable()
                        && !v.isLiteral()
                        && (reflectedFieldNames == null || !reflectedFieldNames
                                .contains(v.id()))) {
                    fields[size++] = v;
                }
            }
            VariableDefinition.sortBySize(fields, size);
            for (int i = 0; i < size; i++) {
                fields[i].outputClassVar(outputContext, false);
                outputContext.cPrint(";\n\n");
            }
            if (size > 0) {
                outputContext.hPrint("\n\n");
            }
        }
        if (classClassDefn != this) {
            writeInstanceDefinition(outputContext);
        }
    }

    private boolean isEnum() {
        Term.assertCond(used);
        VariableDefinition v;
        return superClass != null
                && superClass.name.equals(Names.JAVA_LANG_ENUM)
                && (v = (VariableDefinition) fieldDictionary.get("$VALUES")) != null
                && v.isPrivate() && v.isClassVariable() && v.isFinalVariable();
    }

    private void writeInstanceDefinition(OutputContext oc) {
        Term.assertCond(used);
        oc.hPrint("struct ");
        oc.hPrint(cname);
        oc.hPrint("_s{");
        oc.hPrint("CONST struct ");
        oc.hPrint(cname);
        oc.hPrint("_methods_s *JCGO_IMMFLD_CONST jcgo_methods;");
        oc.hPrint("JCGO_MON_DEFN\010");
        printInstanceDefinition(oc);
        oc.hPrint("};\n\n");
    }

    private void outputCoreClasses() {
        ClassDefinition classClassDefn = Main.dict.get(Names.JAVA_LANG_CLASS);
        ArrayLiteral noIfacesArr = Main.dict.addArrayLiteral(new ArrayLiteral(
                classClassDefn, "", 0, false), this, false);
        StringBuffer sb = new StringBuffer();
        int cnt = 0;
        ClassDefinition cd = Main.dict.get(Names.JAVA_LANG_CLONEABLE);
        if (cd.used) {
            sb.append('(').append(Type.cName[Type.CLASSINTERFACE]).append(')');
            sb.append(cd.getClassRefStr(false)).append(", ");
            cnt++;
        }
        cd = Main.dict.get(Names.JAVA_IO_SERIALIZABLE);
        if (cd.used) {
            sb.append('(').append(Type.cName[Type.CLASSINTERFACE]).append(')');
            sb.append(cd.getClassRefStr(false)).append(", ");
            cnt++;
        }
        ArrayLiteral arrayIfacesArr = Main.dict.addArrayLiteral(
                new ArrayLiteral(classClassDefn, sb.toString(), cnt, false),
                this, false);
        if (Main.dict.allowConstClass >= 0) {
            outputContext.hPrint("JCGO_SEP_EXTERN");
            outputContext.cPrint("JCGO_NOSEP_DATA");
            if (Main.dict.allowConstClass > 0) {
                outputContext.cAndHPrint(" JCGO_NOTHR_CONST");
            }
        } else {
            outputContext.hPrint("JCGO_SEP_GCEXTERN");
            outputContext.cPrint("JCGO_NOSEP_GCDATA");
        }
        outputContext.cAndHPrint(" struct ");
        outputContext.cAndHPrint(classClassDefn.cname);
        outputContext.cAndHPrint("_s jcgo_coreClasses[OBJT_jarray+OBJT_");
        outputContext.cAndHPrint(Type.cName[Type.VOID]);
        outputContext.cAndHPrint("-1]");
        outputContext.hPrint(";\n\n");
        outputContext
                .cPrint(Main.dict.allowConstClass > 0 ? " JCGO_THRD_ATTRNONGC"
                        : Main.dict.allowConstClass == 0 ? " ATTRIBNONGC"
                                : " ATTRIBGCDATA");
        outputContext.cPrint("={");
        boolean next = false;
        boolean isArray = false;
        boolean createNames = Main.dict
                .isClassNameUsed(Main.dict.classTable[Type.CLASSINTERFACE
                        + Type.BOOLEAN]);
        int type = Type.BOOLEAN;
        do {
            do {
                if (next) {
                    if (isArray && type == Type.VOID)
                        break;
                    outputContext.cPrint(",");
                }
                next = true;
                outputContext.cPrint("{&");
                outputContext.cPrint(classClassDefn.vTableCName());
                outputContext.cPrint(",\010");
                outputContext.cPrint("JCGO_MON_INIT\010");
                if (type == Type.NULLREF) {
                    outputContext.cPrint(LexTerm.NULL_STR);
                    outputContext.cPrint(", ");
                    outputContext.cPrint(LexTerm.NULL_STR);
                    outputContext.cPrint(", ");
                    outputContext.cPrint(LexTerm.NULL_STR);
                    outputContext.cPrint(", ");
                    outputContext.cPrint(LexTerm.NULL_STR);
                    outputContext.cPrint(", 0");
                } else {
                    outputContext.cPrint("JCGO_OBJREF_OF(*(");
                    outputContext.cPrint(cname);
                    outputContext.cPrint(")&");
                    outputContext
                            .cPrint(Main.dict.classTable[isArray ? Type.CLASSINTERFACE
                                    + type
                                    : type].vTableCName());
                    outputContext.cPrint("),\010");
                    outputContext.cPrint(Main.dict.classNameStringOutput(
                            isArray ? Type.sig[Type.CLASSINTERFACE + type]
                                    : Type.name[type], this, createNames));
                    outputContext.cPrint(",\010");
                    outputContext.cPrint(isArray ? Main.dict.classTable[type]
                            .getClassRefStr(false) : LexTerm.NULL_STR);
                    outputContext.cPrint(",\010");
                    outputContext.cPrint((isArray ? arrayIfacesArr
                            : noIfacesArr).stringOutput());
                    outputContext.cPrint(",\010");
                    outputContext.cPrint("0x");
                    outputContext.cPrint(Integer.toHexString(AccModifier.PUBLIC
                            | AccModifier.FINAL | AccModifier.ABSTRACT));
                }
                outputContext.cPrint("}");
            } while (++type <= Type.VOID);
            if (isArray)
                break;
            type = Type.NULLREF;
            isArray = true;
        } while (true);
        outputContext.cPrint("};\n\n");
        outputContext.hPrint("JCGO_SEP_EXTERN");
        outputContext.cPrint("JCGO_NOSEP_DATA");
        outputContext.cAndHPrint(" CONST struct ");
        outputContext.cAndHPrint(classClassDefn.cname);
        outputContext.cAndHPrint("_s jcgo_objArrStubClasses[");
        outputContext.cAndHPrint(Integer.toString(MAX_DIMS));
        outputContext.cAndHPrint("]");
        outputContext.hPrint(";\n\n");
        outputContext.cPrint("={");
        for (int dims = 0; dims < MAX_DIMS; dims++) {
            if (dims > 0) {
                outputContext.cPrint(",");
            }
            outputContext.cPrint("{&");
            outputContext.cPrint(classClassDefn.vTableCName());
            outputContext.cPrint(",\010");
            outputContext.cPrint("JCGO_MON_INIT\010");
            outputContext.cPrint("JCGO_OBJREF_OF(*(");
            outputContext.cPrint(cname);
            outputContext.cPrint(")&");
            outputContext.cPrint(arrayVTableCName(Type.VOID, dims));
            outputContext.cPrint("),\010");
            outputContext.cPrint(LexTerm.NULL_STR);
            outputContext.cPrint(",\010");
            outputContext.cPrint(getClassRefStr(false));
            outputContext.cPrint(",\010");
            outputContext.cPrint(arrayIfacesArr.stringOutput());
            outputContext.cPrint(",\010");
            outputContext.cPrint("0x");
            outputContext.cPrint(Integer.toHexString(AccModifier.FINAL
                    | AccModifier.ABSTRACT));
            outputContext.cPrint("}");
        }
        outputContext.cPrint("};\n\n");
    }

    private void printInstanceDefinition(OutputContext oc) {
        if (superClass != null) {
            superClass.printInstanceDefinition(oc);
        }
        if (used) {
            oc.hPrint(outputContext.instanceToString());
        }
    }

    private void outputReflectionInfo() {
        Term.assertCond(used);
        int count = 0;
        if (reflectedMethods != null && (count = reflectedMethods.size()) > 0) {
            if (count == 1 && reflectedFieldNames == null
                    && basicConstructor != null && basicConstructor.isPublic()
                    && !isAbstractOrInterface()) {
                reflectedMethods = null;
                return;
            }
            outputContext
                    .cPrint("JCGO_NOSEP_DATA CONST struct jcgo_methodentry_s ");
            outputContext.cPrint(cname);
            outputContext.cPrint("__abstract[");
            outputContext.cPrint(Integer.toString(count));
            outputContext.cPrint("]={");
            Enumeration en = methodDictionary().keys();
            boolean next = false;
            while (en.hasMoreElements()) {
                MethodDefinition md = (MethodDefinition) reflectedMethods
                        .get((String) en.nextElement());
                if (md != null) {
                    Term.assertCond(md.used());
                    Main.dict.message("Reflecting method: " + name + "."
                            + md.methodSignature().getInfo());
                    if (next) {
                        outputContext.cPrint(",\010");
                    }
                    outputContext.cPrint("{");
                    outputContext.cPrint(Main.dict.methodProxyStringOutput(md));
                    outputContext.cPrint(",\010");
                    outputContext.cPrint("(");
                    outputContext.cPrint(Type.cName[Type.VOID]);
                    outputContext.cPrint(" (CFASTCALL*)(");
                    outputContext.cPrint(Type.cName[Type.VOID]);
                    outputContext.cPrint("))");
                    outputContext.cPrint(md.allowOverride()
                            && !md.isConstructor() ? "JCGO_OFFSET_OF(struct "
                            + vTableCName() + "_s, " + md.csign() + ")" : md
                            .routineCName());
                    outputContext.cPrint("}");
                    next = true;
                }
            }
            outputContext.cPrint("};\n\n");
        }
        outputContext.cPrint("JCGO_NOSEP_DATA CONST struct jcgo_reflect_s ");
        outputContext.cPrint(cname);
        outputContext.cPrint("__transient={");
        StringBuffer namesSBuf = null;
        StringBuffer slotsSBuf = null;
        StringBuffer typesSBuf = null;
        StringBuffer dimsSBuf = null;
        StringBuffer modsSBuf = null;
        if (reflectedFieldNames != null) {
            namesSBuf = new StringBuffer();
            slotsSBuf = new StringBuffer();
            typesSBuf = new StringBuffer();
            dimsSBuf = new StringBuffer();
            modsSBuf = new StringBuffer();
            boolean hasDims = false;
            boolean hasMods = false;
            count = 0;
            StringBuffer infoSBuf = null;
            Enumeration en = fieldDictionary().keys();
            while (en.hasMoreElements()) {
                String fieldName = (String) en.nextElement();
                VariableDefinition v = (VariableDefinition) fieldDictionary
                        .get(fieldName);
                if (v.used() && reflectedFieldNames.contains(fieldName)) {
                    if (infoSBuf != null) {
                        infoSBuf.append(", ");
                    } else {
                        infoSBuf = new StringBuffer();
                        infoSBuf.append("Reflected fields for ").append(name)
                                .append(": ");
                    }
                    infoSBuf.append(fieldName);
                    namesSBuf.append('(');
                    namesSBuf.append(Type.cName[Type.CLASSINTERFACE]);
                    namesSBuf.append(')');
                    namesSBuf.append(Main.dict
                            .addStringLiteral(fieldName, this).stringOutput());
                    namesSBuf.append(", ");
                    slotsSBuf.append(v.fieldOffsetStr());
                    slotsSBuf.append(",\010");
                    typesSBuf.append('(');
                    typesSBuf.append(Type.cName[Type.CLASSINTERFACE]);
                    typesSBuf.append(')');
                    ClassDefinition signClass = v.exprType().signatureClass();
                    int dims = v.exprType().signatureDimensions();
                    if (dims == 1
                            && signClass.objectSize() < Type.CLASSINTERFACE) {
                        typesSBuf.append(signClass.getClassRefStr(true));
                        dims--;
                    } else {
                        typesSBuf.append(signClass.getClassRefStr(false));
                    }
                    typesSBuf.append(",\010");
                    dimsSBuf.append('(');
                    dimsSBuf.append(Type.cName[Type.BYTE]);
                    dimsSBuf.append(')');
                    dimsSBuf.append(Integer.toString(dims));
                    dimsSBuf.append(", ");
                    if (dims > 0) {
                        hasDims = true;
                    }
                    modsSBuf.append('(');
                    modsSBuf.append(Type.cName[Type.SHORT]);
                    int mods = v.getJavaModifiers();
                    if (superClass != null
                            && superClass.name.equals(Names.JAVA_LANG_ENUM)
                            && isEnumValueField(v)) {
                        mods |= AccModifier.ENUM;
                    }
                    modsSBuf.append(")0x");
                    modsSBuf.append(Integer.toHexString(mods));
                    modsSBuf.append(", ");
                    if ((isInterface() ? AccModifier.PUBLIC
                            | AccModifier.STATIC | AccModifier.FINAL : 0) != mods) {
                        hasMods = true;
                    }
                    count++;
                }
            }
            if (infoSBuf != null) {
                Main.dict.message(infoSBuf.toString());
            }
            if (!hasDims) {
                dimsSBuf = null;
            }
            if (!hasMods) {
                modsSBuf = null;
            }
        }
        outputContext.cPrint(namesSBuf != null ? addImmutableArray(
                Main.dict.get(Names.JAVA_LANG_STRING), namesSBuf.toString(),
                count) : LexTerm.NULL_STR);
        outputContext.cPrint(",\010");
        outputContext.cPrint(slotsSBuf != null ? addImmutableArray(
                Main.dict.classTable[Type.INT], slotsSBuf.toString(), count)
                : LexTerm.NULL_STR);
        outputContext.cPrint(",\010");
        outputContext.cPrint(typesSBuf != null ? addImmutableArray(
                Main.dict.get(Names.JAVA_LANG_CLASS), typesSBuf.toString(),
                count) : LexTerm.NULL_STR);
        outputContext.cPrint(",\010");
        outputContext.cPrint(dimsSBuf != null ? addImmutableArray(
                Main.dict.classTable[Type.BYTE], dimsSBuf.toString(), count)
                : LexTerm.NULL_STR);
        outputContext.cPrint(",\010");
        outputContext.cPrint(modsSBuf != null ? addImmutableArray(
                Main.dict.classTable[Type.SHORT], modsSBuf.toString(), count)
                : LexTerm.NULL_STR);
        outputContext.cPrint(",\010");
        namesSBuf = null;
        typesSBuf = null;
        dimsSBuf = null;
        boolean dimsPresent = false;
        StringBuffer throwsSBuf = null;
        boolean throwsPresent = false;
        modsSBuf = null;
        if (reflectedMethods != null) {
            count = 0;
            namesSBuf = new StringBuffer();
            typesSBuf = new StringBuffer();
            dimsSBuf = new StringBuffer();
            throwsSBuf = new StringBuffer();
            modsSBuf = new StringBuffer();
            boolean hasNames = false;
            boolean hasMods = false;
            Enumeration en = methodDictionary().keys();
            while (en.hasMoreElements()) {
                MethodDefinition md = (MethodDefinition) reflectedMethods
                        .get((String) en.nextElement());
                if (md != null) {
                    Term.assertCond(md.used());
                    boolean isConstr = md.isConstructor();
                    if (isConstr) {
                        namesSBuf.append(LexTerm.NULL_STR);
                    } else {
                        namesSBuf.append('(');
                        namesSBuf.append(Type.cName[Type.CLASSINTERFACE]);
                        namesSBuf.append(')');
                        namesSBuf.append(Main.dict.addStringLiteral(md.id(),
                                this).stringOutput());
                        hasNames = true;
                    }
                    namesSBuf.append(", ");
                    if ((!isConstr && md.exprType().objectSize() != Type.VOID)
                            || md.hasParameters()) {
                        StringBuffer sb = new StringBuffer();
                        StringBuffer sb2 = new StringBuffer();
                        int cnt = 0;
                        boolean hasDims = false;
                        Enumeration en2 = md.methodSignature().elements();
                        boolean noMore = false;
                        do {
                            ExpressionType exprType;
                            if (en2.hasMoreElements()) {
                                exprType = (ExpressionType) en2.nextElement();
                            } else {
                                if (isConstr)
                                    break;
                                exprType = md.exprType();
                                noMore = true;
                            }
                            sb.append('(');
                            sb.append(Type.cName[Type.CLASSINTERFACE]);
                            sb.append(')');
                            ClassDefinition signClass = exprType
                                    .signatureClass();
                            int dims = exprType.signatureDimensions();
                            if (dims == 1
                                    && signClass.objectSize() < Type.CLASSINTERFACE) {
                                sb.append(signClass.getClassRefStr(true));
                                dims--;
                            } else {
                                sb.append(signClass.getClassRefStr(false));
                            }
                            sb.append(",\010");
                            sb2.append('(');
                            sb2.append(Type.cName[Type.BYTE]);
                            sb2.append(')');
                            sb2.append(Integer.toString(dims));
                            sb2.append(", ");
                            cnt++;
                            if (dims > 0) {
                                hasDims = true;
                            }
                        } while (!noMore);
                        typesSBuf.append('(');
                        typesSBuf.append(Type.cName[Type.CLASSINTERFACE]);
                        typesSBuf.append(')');
                        typesSBuf.append(addImmutableArray(
                                Main.dict.get(Names.JAVA_LANG_CLASS),
                                sb.toString(), cnt));
                        if (hasDims) {
                            dimsPresent = true;
                            dimsSBuf.append('(');
                            dimsSBuf.append(Type.cName[Type.CLASSINTERFACE]);
                            dimsSBuf.append(')');
                            dimsSBuf.append(addImmutableArray(
                                    Main.dict.classTable[Type.BYTE],
                                    sb2.toString(), cnt));
                        } else {
                            dimsSBuf.append(LexTerm.NULL_STR);
                        }
                    } else {
                        typesSBuf.append(LexTerm.NULL_STR);
                        dimsSBuf.append(LexTerm.NULL_STR);
                    }
                    typesSBuf.append(", ");
                    dimsSBuf.append(", ");
                    Enumeration en2 = md.thrownClassesElements();
                    if (en2.hasMoreElements()) {
                        StringBuffer sb = new StringBuffer();
                        int cnt = 0;
                        do {
                            sb.append('(');
                            sb.append(Type.cName[Type.CLASSINTERFACE]);
                            sb.append(')');
                            sb.append(((ClassDefinition) en2.nextElement())
                                    .getClassRefStr(false));
                            sb.append(",\010");
                            cnt++;
                        } while (en2.hasMoreElements());
                        throwsPresent = true;
                        throwsSBuf.append('(');
                        throwsSBuf.append(Type.cName[Type.CLASSINTERFACE]);
                        throwsSBuf.append(')');
                        throwsSBuf.append(addImmutableArray(
                                Main.dict.get(Names.JAVA_LANG_CLASS),
                                sb.toString(), cnt));
                    } else {
                        throwsSBuf.append(LexTerm.NULL_STR);
                    }
                    throwsSBuf.append(",\010");
                    modsSBuf.append('(');
                    modsSBuf.append(Type.cName[Type.SHORT]);
                    int mods = md.getJavaModifiers();
                    modsSBuf.append(")0x");
                    modsSBuf.append(Integer.toHexString(mods));
                    modsSBuf.append(", ");
                    if ((isInterface() ? AccModifier.PUBLIC
                            | AccModifier.ABSTRACT : AccModifier.PUBLIC) != mods) {
                        hasMods = true;
                    }
                    count++;
                }
            }
            if (!hasNames) {
                namesSBuf = null;
            }
            if (!hasMods) {
                modsSBuf = null;
            }
        }
        outputContext.cPrint(namesSBuf != null ? addImmutableArray(
                Main.dict.get(Names.JAVA_LANG_STRING), namesSBuf.toString(),
                count) : LexTerm.NULL_STR);
        outputContext.cPrint(",\010");
        outputContext.cPrint(typesSBuf != null ? addImmutableArray(Main.dict
                .get(Names.JAVA_LANG_CLASS).asExprType(1),
                typesSBuf.toString(), count) : LexTerm.NULL_STR);
        outputContext.cPrint(",\010");
        outputContext.cPrint(dimsPresent ? addImmutableArray(
                Main.dict.classTable[Type.BYTE].asExprType(1),
                dimsSBuf.toString(), count) : LexTerm.NULL_STR);
        outputContext.cPrint(",\010");
        outputContext.cPrint(throwsPresent ? addImmutableArray(
                Main.dict.get(Names.JAVA_LANG_CLASS).asExprType(1),
                throwsSBuf.toString(), count) : LexTerm.NULL_STR);
        outputContext.cPrint(",\010");
        outputContext.cPrint(modsSBuf != null ? addImmutableArray(
                Main.dict.classTable[Type.SHORT], modsSBuf.toString(), count)
                : LexTerm.NULL_STR);
        outputContext.cPrint(",\010");
        if (reflectedMethods != null && reflectedMethods.size() > 0) {
            outputContext.cPrint(cname);
            outputContext.cPrint("__abstract");
        } else {
            outputContext.cPrint("NULL");
        }
        outputContext.cPrint("};\n\n");
    }

    private boolean hasPrimInstanceFields() {
        Enumeration en = fieldDictionary().unorderedElements();
        while (en.hasMoreElements()) {
            VariableDefinition v = (VariableDefinition) en.nextElement();
            if (v.used() && !v.isClassVariable()
                    && v.exprType().objectSize() < Type.CLASSINTERFACE)
                return true;
        }
        return superClass != null && superClass.hasPrimInstanceFields();
    }

    private void startMethodDefinitions() {
        Term.assertCond(used);
        Enumeration en = methodDictionary().unorderedElements();
        while (en.hasMoreElements()) {
            MethodDefinition md = (MethodDefinition) en.nextElement();
            if (md.isNative() && md.usedExact()) {
                outputContext.hPrint("#define JCGO_NATCLASS_");
                outputContext.hPrint(jniname);
                outputContext.hPrint("\n\n");
                break;
            }
        }
        outputContext.hPrint("struct ");
        outputContext.hPrint(cname);
        outputContext.hPrint("_methods_s{");
        outputContext.hPrint(Main.dict.get(Names.JAVA_LANG_CLASS).cname);
        outputContext.hPrint(" jcgo_class;");
        outputContext.hPrint("JCGO_GCJDESCR_DEFN\010");
        outputContext
                .hPrint("JCGO_TYPEID_T jcgo_typeid;JCGO_OBJSIZE_T jcgo_objsize;");
        outputContext.hPrint("CONST struct jcgo_reflect_s *jcgo_reflect;");
        outputContext.hPrint("JCGO_CLINIT_DEFN\010");
        outputContext.hPrint(cname);
        outputContext.hPrint(" (CFASTCALL *jcgo_thisRtn)( ");
        outputContext.hPrint(cname);
        outputContext.hPrint(" This );");
        if (vTableUsed) {
            outputContext.cPrint("JCGO_NOSEP_DATA CONST struct ");
            outputContext.cPrint(isNotInstantated() ? "jcgo_methods_s "
                    : vTableCName() + "_s ");
            outputContext.cPrint(vTableCName());
            outputContext.cPrint("={");
            outputContext.cPrint(getClassRefStr(false));
            outputContext.cPrint(",\010");
            if (lastObjectRefField != null && !isNotInstantated()
                    && hasPrimInstanceFields()) {
                outputContext.cPrint("JCGO_GCJDESCR_INIT(");
                outputContext.cPrint(cname);
                outputContext.cPrint("_s, ");
                outputContext.cPrint(lastObjectRefField.outputName());
                outputContext.cPrint(")\010");
            } else {
                outputContext.cPrint("JCGO_GCJDESCR_ZEROINIT\010");
            }
            outputContext.cPrint("OBJT_");
            outputContext.cPrint(cname);
            outputContext.cPrint(",\010");
            if (isNotInstantated()) {
                outputContext.cPrint("0");
            } else {
                if (lastObjectRefField == null) {
                    outputContext.cPrint("-");
                }
                outputContext.cPrint("(JCGO_OBJSIZE_T)sizeof(struct ");
                outputContext.cPrint(cname);
                outputContext.cPrint("_s)");
            }
            outputContext.cPrint(",\010");
            if (reflectedFieldNames != null || reflectedMethods != null) {
                outputContext.cPrint("&");
                outputContext.cPrint(cname);
                outputContext.cPrint("__transient");
            } else {
                outputContext.cPrint("NULL");
            }
            outputContext.cPrint(",\010");
            outputContext.cPrint("JCGO_CLINIT_INIT(");
            outputContext.cPrint(classInitializers != null ? clinitCName()
                    : "0");
            outputContext.cPrint(")\010");
            if (basicConstructor != null && reflectedMethods == null
                    && basicConstructor.used() && !basicConstructor.isPrivate()) {
                if (isNotInstantated()) {
                    outputContext.cPrint("(");
                    outputContext.cPrint(Type.cName[Type.CLASSINTERFACE]);
                    outputContext.cPrint(" (CFASTCALL*)(");
                    outputContext.cPrint(Type.cName[Type.CLASSINTERFACE]);
                    outputContext.cPrint("))");
                }
                outputContext.cPrint(basicConstructor.routineCName());
            } else {
                outputContext.cPrint("0");
            }
        }
    }

    boolean writeTrigClinit(OutputContext oc) {
        Term.assertCond(used);
        if (classInitializers == null)
            return false;
        oc.cPrint("JCGO_CLINIT_TRIG(");
        oc.cPrint(cname);
        oc.cPrint("__class);");
        return true;
    }

    private void coreMethodDefinitions(OutputContext oc, int type, int dims) {
        oc.hPrint("JCGO_SEP_EXTERN");
        oc.cPrint("JCGO_NOSEP_DATA");
        oc.cAndHPrint(" CONST struct ");
        oc.cAndHPrint(vTableCName() + "_s ");
        oc.cAndHPrint(arrayVTableCName(type, dims));
        oc.hPrint(";");
        String typeStr = "OBJT_jarray+OBJT_" + Type.cName[type]
                + (dims > 0 ? "+" + Integer.toString(dims) : "");
        oc.cPrint("={");
        if (type < Type.VOID) {
            oc.cPrint("JCGO_CORECLASS_FOR(");
            oc.cPrint(typeStr);
            oc.cPrint("),\010");
        } else {
            oc.cPrint("(");
            oc.cPrint(Main.dict.get(Names.JAVA_LANG_CLASS).cname);
            oc.cPrint(")JCGO_OBJREF_OF(jcgo_objArrStubClasses[");
            oc.cPrint(Integer.toString(dims));
            oc.cPrint("]),\010");
        }
        oc.cPrint("JCGO_GCJDESCR_ZEROINIT\010");
        oc.cPrint(typeStr);
        oc.cPrint(",\010");
        oc.cPrint("0,\010");
        oc.cPrint("NULL,\010");
        oc.cPrint("JCGO_CLINIT_INIT(0)\010");
        oc.cPrint("0");
        Enumeration en = methodDictionary().keys();
        while (en.hasMoreElements()) {
            MethodDefinition md = getMethodNoInheritance((String) en
                    .nextElement());
            if (md.isVirtualUsed()) {
                oc.cPrint(",\010");
                oc.cPrint(md.isAbstract() ? "0" : md.routineCName());
            }
        }
        oc.cPrint("};\n\n");
    }

    private void writeClassTableArray(OutputContext oc) {
        ObjHashSet classnames = new ObjHashSet();
        createSubclassList(classnames);
        String[] names = new String[classnames.size()];
        Enumeration en = classnames.elements();
        for (int i = 0; en.hasMoreElements(); i++) {
            names[i] = (String) en.nextElement();
        }
        names = sortStrings(names, names.length);
        oc.cPrint("JCGO_NOSEP_DATA CONST JCGO_STATIC_OBJARRAY(");
        oc.cPrint(Integer.toString(names.length));
        oc.cPrint(") jcgo_classTable={(jvtable)&");
        oc.cPrint(Main.dict.classTable[Type.OBJECTARRAY].vTableCName());
        oc.cPrint(",\010");
        oc.cPrint("JCGO_MON_INIT\010");
        oc.cPrint(Integer.toString(names.length));
        oc.cPrint(",\010");
        oc.cPrint(Main.dict.get(Names.JAVA_LANG_CLASS).getClassRefStr(false));
        oc.cPrint(",\010");
        oc.cPrint("{");
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                oc.cPrint(",\010");
            }
            oc.cPrint("(");
            oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
            oc.cPrint(")");
            oc.cPrint(Main.dict.get(names[i]).getClassRefStr(false));
        }
        oc.cPrint("}};\n\n");
    }

    void buildMain(boolean skipClinitTrace) {
        OutputContext oc = new OutputContext("Main");
        ClassDefinition objectClassDefn = Main.dict.get(Names.JAVA_LANG_OBJECT);
        oc.cPrint("#define ");
        oc.cPrint(Main.VER_ABBR);
        oc.cPrint("\n\n");
        oc.cPrint("#include \"jcgortl.h\"\n\n");
        oc.cAndHPrint("#ifdef ");
        oc.cPrint("JCGO_VER");
        oc.hPrint(Main.VER_ABBR);
        oc.cAndHPrint("\n\n");
        oc.cPrint("#include \"Main.h\"\n\n");
        oc.cPrint("#ifndef JCGO_MAIN_SEP\n\n");
        if (Main.dict.buildClassTable) {
            objectClassDefn.writeClassTableArray(oc);
        }
        Main.dict.writeInternedLiterals(oc);
        oc.cPrint("#endif\n\n");
        oc.cPrint("#ifndef JCGO_SEPARATED\n");
        objectClassDefn.enumerateClass(oc, Type.VOID, new ObjHashSet());
        oc.cPrint("\n#endif");
        oc.cAndHPrint("\n\n");
        Main.dict.writeArrayTypeDefs(oc);
        objectClassDefn.addToHeaderFile(oc);
        oc.hPrint("\n\n");
        oc.cPrint("#ifndef JCGO_MAIN_SEP\n\n");
        for (int type = Type.BOOLEAN; type <= Type.DOUBLE; type++) {
            objectClassDefn.coreMethodDefinitions(oc, type, 0);
        }
        for (int dims = 0; dims < MAX_DIMS; dims++) {
            objectClassDefn.coreMethodDefinitions(oc, Type.VOID, dims);
        }
        oc.hPrint("\n\n");
        oc.hPrint("#endif\n");
        oc.cPrint("EXTRASTATIC ");
        oc.cPrint(Type.cName[Type.OBJECTARRAY]);
        oc.cPrint(" CFASTCALL jcgo_tpostInit( void **targv );\n\n");
        oc.cPrint("EXTRASTATIC ");
        oc.cPrint(Type.cName[Type.VOID]);
        oc.cPrint(" CFASTCALL jcgo_destroyJavaVM( ");
        oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
        oc.cPrint(" throwable );\n\n");
        oc.cPrint("#ifdef JCGO_SEHTRY\n");
        oc.cPrint("EXTRASTATIC ");
        oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
        oc.cPrint(" CFASTCALL jcgo_tryCatchAll( ");
        oc.cPrint(Type.cName[Type.VOID]);
        oc.cPrint(" );");
        oc.cPrint("#endif\n\n");
        oc.cPrint("EXTRASTATIC ");
        oc.cPrint(Type.cName[Type.VOID]);
        oc.cPrint(" jcgo_tmainBody( void **targv ){");
        ClassDefinition throwableClassDefn = Main.dict
                .get(Names.JAVA_LANG_THROWABLE);
        if (throwableClassDefn.used) {
            oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
            oc.cPrint(" throwable;");
            oc.cPrint("{JCGO_TRY_BLOCK{");
        }
        MethodDefinition mdMain = getMethod(Names.SIGN_MAIN);
        if (mdMain != null && mdMain.used()) {
            oc.cPrint(mdMain.routineCName());
        }
        oc.cPrint("(jcgo_tpostInit(targv));");
        if (throwableClassDefn.used) {
            oc.cPrint("}");
            oc.cPrint("JCGO_TRY_LEAVE\010");
            oc.cPrint("JCGO_TRY_CATCHALLSTORE(&throwable)\010");
            oc.cPrint("}");
        }
        oc.cPrint("jcgo_destroyJavaVM(");
        oc.cPrint(throwableClassDefn.used ? "throwable" : LexTerm.NULL_STR);
        oc.cPrint(");}\n\n");
        if (mdMain == null || !mdMain.used() || !mdMain.isFirstParamUsed()) {
            oc.cPrint("#define JCGO_MAINARGS_NOTUSED\n\n");
        }
        oc.hCloseOnly();
        oc.cPrint("#ifndef JCGO_STDCLINIT\n\n");
        if (skipClinitTrace) {
            System.out.println("Class init order not evaluated!");
            oc.cPrint(" % Class init order not evaluated!");
        } else {
            oc.cPrint("JCGO_NOSEP_INLINE ");
            oc.cPrint(Type.cName[Type.VOID]);
            oc.cPrint(" CFASTCALL\n");
            oc.cPrint("jcgo_initClasses( ");
            oc.cPrint(Type.cName[Type.VOID]);
            oc.cPrint(" ){");
            Main.dict.writeClassInitCalls(oc);
            oc.cPrint("}");
        }
        oc.cPrint("\n\n");
        oc.cPrint("#endif\n\n");
        oc.cPrint("#include \"jcgortl.c\"\n\n");
        oc.cPrint("MAINENTRY\n( int argc, JCGO_MAIN_TCHAR **targv ){");
        oc.cPrint("JCGO_MAIN_LAUNCH(argc, targv);");
        oc.cPrint("return 0;}\n\n");
        oc.cPrint("#endif\n\n");
        oc.cPrint("#endif\n");
        oc.close();
    }

    private String addImmutableArray(ExpressionType exprType, String data,
            int count) {
        return Main.dict.addArrayLiteral(
                new ArrayLiteral(exprType, data, count, false), this, false)
                .stringOutput();
    }

    void addArrayLiteral(ArrayLiteral liter) {
        Term.assertCond(used);
        if (arrpool == null) {
            arrpool = new ObjQueue();
        }
        arrpool.addLast(liter);
    }

    void addLiteral(LiteralStr liter) {
        Term.assertCond(used);
        if (strpool == null) {
            strpool = new ObjQueue();
        }
        strpool.addLast(liter);
    }

    void addMethodProxy(MethodProxy mproxy) {
        Term.assertCond(used);
        if (mproxypool == null) {
            mproxypool = new ObjQueue();
        }
        mproxypool.addLast(mproxy);
    }

    private void outputPool() {
        if (mproxypool != null) {
            Enumeration en = mproxypool.elements();
            while (en.hasMoreElements()) {
                ((MethodProxy) en.nextElement()).processOutput(outputContext);
            }
            mproxypool = null;
        }
        if (strpool != null) {
            Enumeration en = strpool.elements();
            while (en.hasMoreElements()) {
                ((LiteralStr) en.nextElement()).initArrayLiteral(this);
            }
        }
        if (arrpool != null) {
            Enumeration en = arrpool.elements();
            while (en.hasMoreElements()) {
                ((ArrayLiteral) en.nextElement()).processOutput(outputContext,
                        this);
            }
            arrpool = null;
        }
        if (strpool != null) {
            Enumeration en = strpool.elements();
            while (en.hasMoreElements()) {
                ((LiteralStr) en.nextElement()).processOutput(outputContext);
            }
            strpool = null;
        }
    }

    void traceReflectedConstructor(boolean declaredOnly, String sigString,
            boolean isExactType) {
        if (sigString != null && !sigString.equals("<init>(*)")) {
            MethodDefinition md = getMethodNoInheritance(sigString);
            if (md != null && md.isConstructor()
                    && (declaredOnly || !md.isPrivate())) {
                md.methodTraceClassInit(true, null, null);
            }
        } else {
            Enumeration en = methodDictionary().keys();
            while (en.hasMoreElements()) {
                MethodDefinition md = getMethodNoInheritance((String) en
                        .nextElement());
                if (md.isConstructor() && (declaredOnly || !md.isPrivate())) {
                    md.methodTraceClassInit(true, null, null);
                }
            }
        }
        if (!isExactType && reflectConstructorsInherit(sigString)) {
            if (implementedBy != null) {
                Enumeration en = implementedBy.elements();
                while (en.hasMoreElements()) {
                    ClassDefinition cd = (ClassDefinition) en.nextElement();
                    if (cd.used) {
                        cd.traceReflectedConstructor(declaredOnly, sigString,
                                false);
                    }
                }
            }
            Enumeration en = subclasses.elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (cd.used || isInterface()) {
                    cd.traceReflectedConstructor(declaredOnly, sigString, false);
                }
            }
        }
    }

    void traceReflectedMethod(String methodId, boolean declaredOnly,
            ObjVector parmSig) {
        if (methodId != null && parmSig != null) {
            MethodDefinition md = getMethod(new MethodSignature(methodId,
                    parmSig));
            if (md != null) {
                if (!md.isConstructor()
                        && (declaredOnly ? md.definingClass() == this : !md
                                .isPrivate())) {
                    md.methodTraceClassInit(true, this, null);
                }
            } else {
                if (implementedBy != null) {
                    Enumeration en = implementedBy.elements();
                    while (en.hasMoreElements()) {
                        ClassDefinition cd = (ClassDefinition) en.nextElement();
                        if (cd.used) {
                            cd.traceReflectedMethod(methodId, declaredOnly,
                                    parmSig);
                        }
                    }
                }
                Enumeration en = subclasses.elements();
                while (en.hasMoreElements()) {
                    ClassDefinition cd = (ClassDefinition) en.nextElement();
                    if (cd.used || isInterface()) {
                        cd.traceReflectedMethod(methodId, declaredOnly, parmSig);
                    }
                }
            }
        } else {
            ClassDefinition cd = this;
            do {
                Enumeration en = cd.methodDictionary().keys();
                while (en.hasMoreElements()) {
                    MethodDefinition md = cd.getMethodNoInheritance((String) en
                            .nextElement());
                    if (!md.isConstructor()
                            && (methodId == null || methodId.equals(md.id()))
                            && (parmSig == null || md.methodSignature()
                                    .isSignEqual(parmSig))
                            && (declaredOnly ? md.definingClass() == this : !md
                                    .isPrivate())) {
                        md.methodTraceClassInit(true, this, null);
                    }
                }
            } while (!declaredOnly
                    && (cd = cd.superClass()) != null
                    && (!isInterface() || !cd.name
                            .equals(Names.JAVA_LANG_OBJECT)));
        }
    }

    ExpressionType traceClInitInSubclasses(String sigString, String pkgName,
            boolean isWeak, ObjVector parmTraceSig) {
        ExpressionType curTraceType = Main.dict.classTable[Type.NULLREF];
        if (implementedBy != null) {
            Enumeration en = implementedBy.elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (cd.used) {
                    ExpressionType curType;
                    MethodDefinition md = cd.getMethod(sigString);
                    if (md != null && !md.isAbstract()) {
                        curType = md.methodTraceClassInit(isWeak, cd,
                                parmTraceSig);
                        if (curType == null) {
                            curType = md.exprType();
                        }
                    } else {
                        curType = cd.traceClInitInSubclasses(sigString, null,
                                isWeak, parmTraceSig);
                    }
                    curTraceType = maxCommonExprOf(curTraceType, curType, null);
                }
            }
        }
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ClassDefinition cd = (ClassDefinition) en.nextElement();
            ExpressionType curType;
            MethodDefinition md;
            if (cd.used && (md = cd.getMethodNoInheritance(sigString)) != null
                    && !md.isAbstract()
                    && (pkgName == null || pkgName.equals(cd.getPackageName()))) {
                curType = md.methodTraceClassInit(isWeak, null, parmTraceSig);
                if (curType == null) {
                    curType = md.exprType();
                }
            } else {
                curType = cd.traceClInitInSubclasses(sigString, pkgName,
                        isWeak, parmTraceSig);
            }
            curTraceType = maxCommonExprOf(curTraceType, curType, null);
        }
        return curTraceType;
    }

    void instanceTraceClassInit() {
        if (hasInstances && used && instanceInitializers != null) {
            instanceInitializers.traceClassInit();
        }
    }

    private boolean isBasicConstructorNormal() {
        return basicConstructor != null && basicConstructor.used()
                && basicConstructor.isPublic() && !isAbstractOrInterface();
    }

    boolean constructorTraceClassInit(boolean isWeak) {
        if (basicConstructor == null)
            return false;
        basicConstructor.methodTraceClassInit(isWeak, null, null);
        return true;
    }

    void constructorTraceClassInitInSubclasses() {
        Enumeration en = subclasses.elements();
        while (en.hasMoreElements()) {
            ((ClassDefinition) en.nextElement())
                    .constructorTraceClassInit(true);
        }
    }

    void classTraceForSupers() {
        Term.assertCond(used);
        if (isInterface())
            return;
        ClassDefinition sc = superClass();
        while (sc != null) {
            sc.classTraceClassInit(true);
            sc = sc.superClass();
        }
        Enumeration en = interfaceClasses(null).elements();
        while (en.hasMoreElements()) {
            ((ClassDefinition) en.nextElement()).classTraceClassInit(true);
        }
    }

    void classTraceClassInit(boolean isWeak) {
        if (used && classInitializers != null) {
            Main.dict.curTraceInfo.addClassInitDepend(isWeak, this);
        }
    }

    boolean noInstanceYetOnTrace() {
        return !hasInstances
                || !used
                || (finished && !Main.dict.instancesCreatedOnTrace
                        .contains(this));
    }

    void instanceCreatedOnTrace() {
        if (hasInstances && used && finished) {
            ClassDefinition cd = this;
            while (Main.dict.instancesCreatedOnTrace.add(cd)) {
                ObjQueue traceInfos;
                if (Main.dict.notYetCallableTraceInfos != null
                        && (traceInfos = (ObjQueue) Main.dict.notYetCallableTraceInfos
                                .remove(cd)) != null)
                    Main.dict.pendingTraceInfos.addAllMovedFrom(traceInfos);
                Enumeration en = cd.specifiedInterfaces.elements();
                while (en.hasMoreElements()) {
                    ((ClassDefinition) en.nextElement())
                            .instanceCreatedOnTrace();
                }
                if (isInterface() || (cd = cd.superClass()) == null
                        || !cd.finished)
                    break;
            }
        }
    }

    private void buildClassInitTrace() {
        ObjHashtable processed = new ObjHashtable();
        Main.dict.notYetCallableTraceInfos = new ObjHashtable();
        Main.dict.dynCallerTraceInfos = new OrderedMap();
        int oldDynCount = Main.dict.dynClassesToTrace.size();
        recursiveTracing(processed);
        int dynCount;
        while ((dynCount = Main.dict.dynClassesToTrace.size()) > oldDynCount) {
            for (int i = 0; Main.dict.dynCallerTraceInfos.size() > i; i++) {
                recursiveTracingInner(
                        (MethodTraceInfo) Main.dict.dynCallerTraceInfos
                                .keyAt(i),
                        processed);
            }
            oldDynCount = dynCount;
        }
        Main.dict.notYetCallableTraceInfos = null;
        Main.dict.dynCallerTraceInfos = null;
    }

    private void createTraceInfo() {
        if (clinitTraceInfo == null) {
            Term.assertCond(classInitializers != null);
            Main.dict.message("Analyzing class initializer: " + name);
            clinitTraceInfo = MethodTraceInfo.create(null, this, null);
            Main.dict.curTraceInfo = clinitTraceInfo;
            classInitializers.traceClassInit();
            clinitTraceInfo.doneMethodTracing(Main.dict.classTable[Type.VOID]);
        }
    }

    private void recursiveTracing(ObjHashtable processed) {
        createTraceInfo();
        recursiveTracing(clinitTraceInfo, processed);
        if (!isInterface() && superClass != null
                && superClass.classInitializers != null) {
            superClass.recursiveTracing(processed);
        }
    }

    private static void recursiveTracing(MethodTraceInfo traceInfo,
            ObjHashtable processed) {
        if (traceInfo.addToProcessed(processed)) {
            if (traceInfo.isCallableNow()) {
                recursiveTracingInner(traceInfo, processed);
            } else {
                ExpressionType expr = traceInfo.curThisClass();
                Term.assertCond(expr != null);
                ClassDefinition cd = expr.receiverClass();
                ObjQueue traceInfos = (ObjQueue) Main.dict.notYetCallableTraceInfos
                        .get(cd);
                if (traceInfos == null) {
                    Main.dict.notYetCallableTraceInfos.put(cd,
                            traceInfos = new ObjQueue());
                }
                traceInfos.addLast(traceInfo);
            }
        }
    }

    private static void recursiveTracingInner(MethodTraceInfo traceInfo,
            ObjHashtable processed) {
        traceInfo.setInstanceCreated();
        traceInfo.traceMethod();
        while (!Main.dict.pendingTraceInfos.isEmpty()) {
            recursiveTracingInner(
                    (MethodTraceInfo) Main.dict.pendingTraceInfos.removeFirst(),
                    processed);
        }
        boolean isWeak = false;
        do {
            Enumeration en = traceInfo.getCalledInfosElements(isWeak);
            while (en.hasMoreElements()) {
                recursiveTracing((MethodTraceInfo) en.nextElement(), processed);
            }
            if (isWeak)
                break;
            isWeak = true;
        } while (true);
        if (traceInfo.usesDynClasses()) {
            ClassDefinition[] dynClasses = new ClassDefinition[Main.dict.dynClassesToTrace
                    .size()];
            Main.dict.dynClassesToTrace.copyKeysInto(dynClasses);
            for (int i = 0; i < dynClasses.length; i++) {
                if (dynClasses[i].isBasicConstructorNormal()) {
                    recursiveTracing(MethodTraceInfo.create(
                            dynClasses[i].basicConstructor, null, null),
                            processed);
                }
            }
            Main.dict.dynCallerTraceInfos.put(traceInfo, traceInfo);
        }
        isWeak = false;
        do {
            ClassDefinition cd;
            for (int i = 0; (cd = traceInfo.getClassDependElementAt(isWeak, i)) != null; i++) {
                cd.recursiveTracing(processed);
            }
            if (isWeak)
                break;
            isWeak = true;
        } while (true);
        ClassDefinition cd = traceInfo.getDefiningClassDepend();
        if (cd != null) {
            cd.recursiveTracing(processed);
        }
    }

    private void collectClassInitDepend(ObjHashtable classToInstancesMap) {
        ObjHashSet curInstancesCreatedOnTrace = (ObjHashSet) classToInstancesMap
                .get(this);
        if (curInstancesCreatedOnTrace == null) {
            Term.assertCond(clinitTraceInfo != null
                    && classInitializers != null);
            if (classInitDepend == null) {
                classInitDepend = new OrderedMap();
                if (!isInterface() && superClass != null
                        && superClass.classInitializers != null) {
                    classInitDepend.put(superClass, new ConstValue(-1 >>> 1));
                }
            }
            curInstancesCreatedOnTrace = new ObjHashSet();
            classToInstancesMap.put(this, curInstancesCreatedOnTrace);
            ObjHashSet oldInstancesCreatedOnTrace = Main.dict.instancesCreatedOnTrace;
            Main.dict.instancesCreatedOnTrace = curInstancesCreatedOnTrace;
            int oldInstCount = 0;
            ObjHashtable processed = new ObjHashtable();
            ObjVector traceInfos = new ObjVector();
            do {
                processed.clear();
                traceInfos.addElement(clinitTraceInfo);
                collectClassInitDepend(traceInfos, 0, processed);
                Term.assertCond(traceInfos.size() == 1);
                for (int j = 0; classInitDepend.size() > j; j++) {
                    ((ClassDefinition) classInitDepend.keyAt(j))
                            .collectClassInitDepend(classToInstancesMap);
                }
                int instCount = curInstancesCreatedOnTrace.size();
                if (instCount == oldInstCount)
                    break;
                oldInstCount = instCount;
                traceInfos.removeElementAt(0);
            } while (true);
            Main.dict.instancesCreatedOnTrace = oldInstancesCreatedOnTrace;
        }
        Enumeration en = curInstancesCreatedOnTrace.elements();
        while (en.hasMoreElements()) {
            ((ClassDefinition) en.nextElement()).instanceCreatedOnTrace();
        }
    }

    private void collectClassInitDepend(ObjVector traceInfos, int weakness,
            ObjHashtable processed) {
        MethodTraceInfo traceInfo = (MethodTraceInfo) traceInfos
                .elementAt(traceInfos.size() - 1);
        if (traceInfo.addToProcessed(processed) && traceInfo.isCallableNow()) {
            traceInfo.setInstanceCreated();
            boolean isWeak = false;
            do {
                ClassDefinition cd;
                for (int i = 0; (cd = traceInfo.getClassDependElementAt(isWeak,
                        i)) != null; i++) {
                    if (cd != this) {
                        recordClassInitDepend(cd, weakness, traceInfos);
                    }
                }
                if (isWeak)
                    break;
                weakness++;
                isWeak = true;
            } while (true);
            ClassDefinition cd = traceInfo.getDefiningClassDepend();
            if (cd != null && cd != this) {
                recordClassInitDepend(cd, weakness + 2, traceInfos);
            }
            weakness--;
            isWeak = false;
            do {
                Enumeration en = traceInfo.getCalledInfosElements(isWeak);
                while (en.hasMoreElements()) {
                    traceInfos.addElement(en.nextElement());
                    collectClassInitDepend(traceInfos, weakness, processed);
                    traceInfos.removeElementAt(traceInfos.size() - 1);
                }
                if (isWeak)
                    break;
                weakness++;
                isWeak = true;
            } while (true);
            if (traceInfo.usesDynClasses()) {
                Enumeration en = Main.dict.dynClassesToTrace.keys();
                while (en.hasMoreElements()) {
                    cd = (ClassDefinition) en.nextElement();
                    if (cd.isBasicConstructorNormal()) {
                        traceInfos.addElement(MethodTraceInfo.create(
                                cd.basicConstructor, null, null));
                        collectClassInitDepend(traceInfos, weakness, processed);
                        traceInfos.removeElementAt(traceInfos.size() - 1);
                    }
                }
            }
        }
    }

    private void recordClassInitDepend(ClassDefinition cd, int weakness,
            ObjVector traceInfos) {
        Term.assertCond(classInitializers != null
                && cd.classInitializers != null);
        if (superClass != cd && !isInterface() && superClass != null
                && superClass.getSubclassDepth(cd, null) > 0) {
            Term.assertCond(superClass.classInitializers != null);
            if (superClass.classInitDepend == null) {
                superClass.classInitDepend = new OrderedMap();
            }
            superClass.recordClassInitDepend(cd, weakness, traceInfos);
            cd = superClass;
        }
        ConstValue constVal = (ConstValue) classInitDepend.get(cd);
        if (constVal == null || constVal.getIntValue() > weakness) {
            classInitDepend.put(cd, new ConstValue(weakness));
            if (Main.dict.verboseTracing) {
                Main.dict.message("Clinit dependency: " + name + " -> "
                        + cd.name);
                int i = traceInfos.size();
                while (i-- > 0) {
                    Main.dict.message(" Called from: "
                            + ((MethodTraceInfo) traceInfos.elementAt(i))
                                    .getTraceSig());
                }
            }
        }
    }

    void printClassInitGroup(OutputContext oc) {
        if (used && classInitializers != null) {
            Enumeration en = Main.dict.instancesCreatedOnTrace.elements();
            while (en.hasMoreElements()) {
                ((ClassDefinition) en.nextElement()).finished = false;
            }
            Main.dict.instancesCreatedOnTrace = new ObjHashSet();
            buildClassInitTrace();
            Main.dict.message("Grouping initializers for: " + name + " ("
                    + Integer.toString(Main.dict.tracedInfosCount)
                    + " methods traced)");
            Main.dict.tracedInfosCount = 0;
            collectClassInitDepend(new ObjHashtable());
            oc.cPrint("{");
            ObjHashSet clinitBeginSet = new ObjHashSet();
            if (!printClassInitSimpleWithSub(oc, clinitBeginSet)) {
                ObjVector cdRoots = new ObjVector();
                cdRoots.addElement(this);
                breakClassInitWeakCycles(cdRoots, (-1 >>> 1) - 1);
                int i = 0;
                do {
                    if (((ClassDefinition) cdRoots.elementAt(i))
                            .printClassInitSimpleWithSub(oc, clinitBeginSet)) {
                        cdRoots.removeElementAt(i);
                    } else {
                        i++;
                    }
                } while (cdRoots.size() > i);
                if (i > 0) {
                    oc.cPrint(" ;");
                    breakClassInitWeakCycles(cdRoots, 0);
                    ObjHashSet processed = new ObjHashSet();
                    i = 0;
                    do {
                        if (((ClassDefinition) cdRoots.elementAt(i))
                                .printClassInitSimple(oc, processed, null)) {
                            cdRoots.removeElementAt(i);
                        } else {
                            i++;
                        }
                    } while (cdRoots.size() > i);
                    Enumeration en2 = cdRoots.elements();
                    while (en2.hasMoreElements()) {
                        ((ClassDefinition) en2.nextElement())
                                .printClassInitCyclic(oc, new ObjHashSet(),
                                        null);
                    }
                }
            }
            oc.cPrint("}");
        }
    }

    private boolean printClassInitSimpleWithSub(OutputContext oc,
            ObjHashSet clinitBeginSet) {
        ObjHashSet processed = new ObjHashSet();
        do {
            processed.clear();
            if (printClassInitSimple(oc, processed, clinitBeginSet))
                return true;
            processed.clear();
        } while (printClassInitForSubclasses(oc, processed, clinitBeginSet));
        return false;
    }

    private boolean printClassInitForSubclasses(OutputContext oc,
            ObjHashSet processed, ObjHashSet clinitBeginSet) {
        if (classInitializers != null && processed.add(this)) {
            if (!isInterface() && superClass != null) {
                boolean depend = false;
                ClassDefinition sc = this;
                do {
                    Enumeration en = sc.classInitDepend.keys();
                    ClassDefinition cd;
                    while (en.hasMoreElements()) {
                        if ((cd = (ClassDefinition) en.nextElement()) != sc.superClass
                                && cd.classInitializers != null) {
                            if (cd.getSubclassDepth(sc, null) <= 0) {
                                depend = true;
                                break;
                            }
                            if (sc == this)
                                return cd.printClassInitForSubclasses(oc,
                                        processed, clinitBeginSet);
                            if (cd != this && getSubclassDepth(cd, null) <= 0) {
                                depend = true;
                                break;
                            }
                        }
                    }
                } while (!depend && (sc = sc.superClass) != null
                        && sc.classInitializers != null);
                if (!depend && superClass.classInitializers != null) {
                    printOneClassInitWithSuper(oc, clinitBeginSet);
                    return true;
                }
            }
            Enumeration en = classInitDepend.keys();
            while (en.hasMoreElements()) {
                if (((ClassDefinition) en.nextElement())
                        .printClassInitForSubclasses(oc, processed,
                                clinitBeginSet))
                    return true;
            }
        }
        return false;
    }

    private boolean printClassInitSimple(OutputContext oc,
            ObjHashSet processed, ObjHashSet clinitBeginSet) {
        Term.assertCond(used);
        if (classInitializers != null) {
            if (!processed.add(this))
                return false;
            Term.assertCond(classInitDepend != null);
            boolean full = true;
            Enumeration en = classInitDepend.keys();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (!cd.printClassInitSimple(oc, processed, clinitBeginSet)
                        && (clinitBeginSet == null
                                || !clinitBeginSet.contains(cd) || ((ConstValue) classInitDepend
                                .get(cd)).getIntValue() == 0)) {
                    full = false;
                }
            }
            if (!full)
                return false;
            printOneClassInit(oc, null, clinitBeginSet);
        }
        return true;
    }

    private ClassDefinition printClassInitCyclic(OutputContext oc,
            ObjHashSet commented, ClassDefinition prevCommented) {
        Term.assertCond(used);
        if (classInitializers == null)
            return prevCommented;
        if (headerWritten) {
            headerWritten = false;
            Enumeration en = classInitDepend.keys();
            while (en.hasMoreElements()) {
                prevCommented = ((ClassDefinition) en.nextElement())
                        .printClassInitCyclic(oc, commented, prevCommented);
            }
            headerWritten = true;
            printOneClassInit(oc, null, null);
        } else if (this != prevCommented) {
            printOneClassInit(oc, commented, null);
        }
        return this;
    }

    private void printOneClassInitWithSuper(OutputContext oc,
            ObjHashSet clinitBeginSet) {
        if (!isInterface() && superClass != null
                && superClass.classInitializers != null) {
            if (clinitBeginSet.add(this)) {
                oc.cPrint("JCGO_CLINIT_BEGIN(" + cname + "__class);");
            }
            superClass.printOneClassInitWithSuper(oc, clinitBeginSet);
        }
        printOneClassInit(oc, null, null);
    }

    private void printOneClassInit(OutputContext oc, ObjHashSet commented,
            ObjHashSet clinitBeginSet) {
        Term.assertCond(classInitializers != null);
        if (commented != null) {
            if (commented.add(this)) {
                System.out.println("Warning: cannot detect class init order: "
                        + name);
            }
            oc.cPrint("/" + "*");
        } else if (clinitBeginSet != null && !isInterface()
                && superClass != null && superClass.classInitializers != null
                && clinitBeginSet.add(superClass)) {
            oc.cPrint("JCGO_CLINIT_BEGIN(" + superClass.cname + "__class);");
        }
        oc.cPrint(clinitCName());
        oc.cPrint("()");
        if (commented != null) {
            oc.cPrint("*" + "/");
        }
        oc.cPrint(";");
        if (commented == null) {
            classInitializers = null;
        }
    }

    private static void breakClassInitWeakCycles(ObjVector cdRoots,
            int nonWeakMax) {
        int i = 0;
        do {
            ((ClassDefinition) cdRoots.elementAt(i)).breakClassInitWeakCycles(
                    cdRoots, nonWeakMax, new ObjVector(), new ObjHashSet());
        } while (++i < cdRoots.size());
    }

    private void breakClassInitWeakCycles(ObjVector cdRoots, int nonWeakMax,
            ObjVector cdStack, ObjHashSet processed) {
        if (classInitializers != null) {
            int i = cdStack.identityLastIndexOf(this);
            if (i >= 0) {
                int j = cdStack.size();
                int k = -1;
                ClassDefinition cdNext = this;
                ClassDefinition cd;
                do {
                    cd = (ClassDefinition) cdStack.elementAt(--j);
                    ConstValue constVal = (ConstValue) cd.classInitDepend
                            .get(cdNext);
                    if (constVal == null)
                        return;
                    int weakness = constVal.getIntValue();
                    if (nonWeakMax < weakness) {
                        nonWeakMax = weakness;
                        k = j;
                    }
                } while ((cdNext = cd) != this);
                if (k >= 0) {
                    if (cdStack.size() - 1 > k) {
                        cdNext = (ClassDefinition) cdStack.elementAt(k + 1);
                        if (i > 0) {
                            cd = (ClassDefinition) cdStack.elementAt(i - 1);
                            ConstValue weaknessVal = (ConstValue) cd.classInitDepend
                                    .get(this);
                            if (weaknessVal == null)
                                return;
                            ConstValue constVal = (ConstValue) cd.classInitDepend
                                    .get(cdNext);
                            if (constVal == null
                                    || constVal.getIntValue() > weaknessVal
                                            .getIntValue()) {
                                cd.classInitDepend.put(cdNext, weaknessVal);
                            }
                        }
                    }
                    if (cdRoots.identityLastIndexOf(cdNext) < 0) {
                        cdRoots.addElement(cdNext);
                    }
                    ((ClassDefinition) cdStack.elementAt(k)).classInitDepend
                            .remove(cdNext);
                }
            } else if (processed.add(this)) {
                cdStack.addElement(this);
                for (int j = 0; classInitDepend.size() > j; j++) {
                    ((ClassDefinition) classInitDepend.keyAt(j))
                            .breakClassInitWeakCycles(cdRoots, nonWeakMax,
                                    cdStack, processed);
                }
                cdStack.removeElementAt(cdStack.size() - 1);
            }
        }
    }
}
