/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/MethodDefinition.java --
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
 * A method entity definition.
 */

final class MethodDefinition {

    static/* final */int MAX_INLINE = 10;

    static final String UNKNOWN_NAME = "<unknown?>".toString();

    static final String STACKOBJ_RETNAME = "jcgo_stackparam".toString();

    private static final ObjVector EMPTY_VECTOR = new ObjVector();

    private final OrderedMap fieldDictionary = new OrderedMap();

    private ClassDefinition ourClass;

    private ExpressionType resType;

    private ExpressionType actualType;

    private Term paramList;

    private ObjVector thrownClasses;

    private Term body;

    private String id;

    private MethodSignature ourSig;

    private int modifiers;

    private MethodDefinition singleCaller;

    private int returnsCnt;

    private boolean copied;

    private boolean used;

    private boolean usedExact;

    private boolean allUsed;

    private boolean innerUsed;

    private boolean isNewUsed;

    private boolean outputDone;

    private boolean referenced;

    private boolean newReferenced;

    private boolean analysisBegin;

    private boolean analysisDone;

    private BranchContext methodBranch;

    private MethodInvocation classNewInstanceCall;

    private MethodSignature constructorInstanceSign;

    private ExpressionType classLiteralValue;

    private boolean needsTrigClinit;

    private boolean needsDummyRet;

    private boolean isVirtual;

    private boolean isNullReturned;

    private boolean leaksDiscoverProcessing;

    private ObjVector retWritableArrays;

    private Term stackObjRetTerm;

    private boolean leaksDiscoverDone;

    private boolean stackObjRetRequired;

    private boolean hasThisObjLeak;

    private boolean hasThisObjRet;

    private boolean isThisStackObjVolatile;

    private int nextLabel;

    private MethodDefinition helperMethod;

    private ExpressionType curTraceType;

    MethodDefinition(ClassDefinition ourClass) {
        this.ourClass = ourClass;
        id = "<clinit>";
        modifiers = AccModifier.STATIC;
        resType = Main.dict.classTable[Type.VOID];
        paramList = Empty.newTerm();
        body = Empty.newTerm();
        thrownClasses = EMPTY_VECTOR;
    }

    MethodDefinition(Context c, String id, int modifiers,
            ExpressionType resType, Term paramList, Term throwsList, Term body) {
        ourClass = c.currentClass;
        this.id = id;
        if ((modifiers & (AccModifier.PRIVATE | AccModifier.PROTECTED | AccModifier.PUBLIC)) == (AccModifier.PRIVATE | AccModifier.PROTECTED)) {
            modifiers &= ~(AccModifier.PRIVATE | AccModifier.PROTECTED);
        }
        this.modifiers = modifiers;
        this.resType = resType;
        this.paramList = paramList;
        this.body = body;
        Term.assertCond(c.currentClass != null);
        Term.assertCond(c.currentMethod == null);
        if (ourClass.isInterface() && !isClassMethod()) {
            this.modifiers |= AccModifier.PUBLIC | AccModifier.ABSTRACT;
        }
        if (!isAbstract() && !isNative()) {
            Main.dict.methodsAnalyzed++;
        }
        c.currentMethod = this;
        paramList.processPass1(c);
        c.currentMethod = null;
        thrownClasses = EMPTY_VECTOR;
        if (throwsList.notEmpty()) {
            throwsList.processPass1(c);
            thrownClasses = throwsList.getSignature();
        }
    }

    private MethodDefinition(ClassDefinition ourClass, String id,
            int modifiers, ExpressionType resType, Term paramList,
            ObjVector thrownClasses, Term body, boolean copied) {
        this.ourClass = ourClass;
        this.id = id;
        this.modifiers = modifiers;
        this.resType = resType;
        this.paramList = paramList;
        this.thrownClasses = thrownClasses;
        this.body = body;
        this.copied = copied;
    }

    MethodDefinition cloneMethodFor(ClassDefinition ourClass,
            boolean isProxyClass) {
        return new MethodDefinition(ourClass, id,
                isProxyClass ? AccModifier.PUBLIC | AccModifier.FINAL
                        | AccModifier.SYNTHETIC : modifiers, resType,
                paramList, isProxyClass ? copyCheckedExceptions(ourClass)
                        : thrownClasses, new Block(), !isProxyClass);
    }

    private ObjVector copyCheckedExceptions(ClassDefinition forClass) {
        int size = thrownClasses.size();
        ObjVector newThrownClasses = thrownClasses;
        if (size > 0) {
            newThrownClasses = new ObjVector();
            for (int i = 0; i < size; i++) {
                ClassDefinition cd = (ClassDefinition) thrownClasses
                        .elementAt(i);
                if (!cd.isUncheckedException(forClass)) {
                    boolean found = false;
                    int j = newThrownClasses.size();
                    while (j-- > 0) {
                        ClassDefinition cd2 = (ClassDefinition) newThrownClasses
                                .elementAt(j);
                        if (cd2.isAssignableFrom(cd, 0, forClass)) {
                            found = true;
                            break;
                        }
                        if (cd.isAssignableFrom(cd2, 0, forClass)) {
                            newThrownClasses.removeElementAt(j);
                        }
                    }
                    if (!found) {
                        newThrownClasses.addElement(cd);
                    }
                }
            }
        }
        return newThrownClasses;
    }

    void intersectThrowsWith(MethodDefinition md2) {
        int i = thrownClasses.size();
        while (i-- > 0) {
            ClassDefinition cd = (ClassDefinition) thrownClasses.elementAt(i);
            if (md2.thrownClasses.size() <= i
                    || md2.thrownClasses.elementAt(i) != cd) {
                ClassDefinition maxClass = null;
                Enumeration en = md2.thrownClassesElements();
                while (en.hasMoreElements()) {
                    ClassDefinition cd2 = (ClassDefinition) en.nextElement();
                    if (cd2.isAssignableFrom(cd, 0, ourClass)) {
                        maxClass = cd;
                        break;
                    }
                    if (cd.isAssignableFrom(cd2, 0, ourClass)
                            && (maxClass == null || cd2.isAssignableFrom(
                                    maxClass, 0, ourClass))) {
                        maxClass = cd2;
                    }
                }
                if (maxClass != null) {
                    thrownClasses.setElementAt(maxClass, i);
                } else {
                    thrownClasses.removeElementAt(i);
                }
            }
        }
    }

    boolean hasSameThrows(MethodDefinition md2) {
        int size = thrownClasses.size();
        if (md2.thrownClasses.size() != size)
            return false;
        for (int i = 0; i < size; i++) {
            if (thrownClasses.elementAt(i) != md2.thrownClasses.elementAt(i))
                return false;
        }
        return true;
    }

    Enumeration thrownClassesElements() {
        return thrownClasses.elements();
    }

    boolean hasNonEmptyBody() {
        Term.assertCond(analysisDone);
        return body.tokenCount() != 0;
    }

    String id() {
        return id;
    }

    ClassDefinition definingClass() {
        return ourClass;
    }

    void setVirtual() {
        if (!isVirtual && used && allowVirtual()) {
            isVirtual = true;
            if (!isFinal()) {
                ourClass.setVirtualInSubclasses(methodSignature()
                        .signatureString());
            }
        }
    }

    boolean isCopiedFromIface() {
        return copied;
    }

    int markUsed(MethodDefinition callerMethod) {
        incCallsCount(callerMethod);
        String className = ourClass.name();
        return markUsed(ourClass, className.equals(Names.JAVA_LANG_CLASS)
                || className.equals(Names.JAVA_LANG_STRING));
    }

    int markUsed(ClassDefinition receiverClass, boolean isClinitSafe) {
        if (!isClinitSafe) {
            needsTrigClinit = true;
        }
        int cnt = 0;
        if (!allUsed) {
            if (isClassMethod() || isConstructor()) {
                cnt = markUsedThisOnly();
            } else if (!ourClass.hasInstances()) {
                if (!innerUsed) {
                    innerUsed = true;
                    cnt = 1;
                }
            } else if (!isAbstract() && allowOverride()
                    && !ourClass.hasExactInstance()
                    && ourClass.overriddenByAllUsed(this)) {
                if (!innerUsed) {
                    innerUsed = true;
                    cnt = 1;
                    used = true;
                }
            } else {
                cnt = markUsedThisOnly();
            }
            if (!isConstructor() && allowOverride()) {
                cnt += receiverClass.markMethodInSubclasses(methodSignature()
                        .signatureString(), isProtectedOrPublic() ? null
                        : ourClass.getPackageName());
                if (receiverClass == ourClass) {
                    allUsed = true;
                }
            }
        }
        return cnt;
    }

    int markUsedThisOnly() {
        if (usedExact)
            return 0;
        String ourClassName = ourClass.name();
        if (id.equals(Main.dict.failOnMethodId)
                && (Main.dict.failOnClassName == null || ourClassName
                        .equals(Main.dict.failOnClassName)))
            throw new AssertException("Specified method is required!");
        ourClass.markUsed();
        usedExact = true;
        used = true;
        if (isNative()) {
            markAllTypes(false);
        }
        if (isConstructor()) {
            if (!ourClass.isAbstractOrInterface()) {
                ourClass.setHasInstances();
            }
            if (!hasParameters() && !isPrivate()) {
                ourClass.setHasExactInstance(true);
            }
        }
        resType.signatureClass().predefineClass(ourClass);
        if (ourClassName.equals(Names.JAVA_LANG_CLASS)) {
            if (isExactMatch(false, Names.JAVA_LANG_CLASS, 1,
                    Names.SIGN_GETINTERFACES)) {
                ClassDefinition.markAllDirectIfaces();
            }
        } else if (ourClassName.equals(Names.JAVA_LANG_VMTHREAD)) {
            Main.dict.markInitSystemErr();
        } else if (ourClassName.equals(Names.JAVA_LANG_ENUM)
                && isExactMatch(true, Names.JAVA_LANG_ENUM, 0,
                        Names.SIGN_VALUEOF_ENUM)) {
            ClassDefinition.processEnumValueOf();
        }
        return 1;
    }

    void markNew() {
        if (!isNewUsed) {
            isNewUsed = true;
            ourClass.setHasExactInstance(true);
        }
    }

    void markAllTypes(boolean vTableUsed) {
        ClassDefinition cd = resType.signatureClass();
        cd.define(ourClass);
        if (vTableUsed) {
            cd.setVTableUsed(true);
        } else {
            cd.markUsed();
        }
        Enumeration en = methodSignature().elements();
        while (en.hasMoreElements()) {
            cd = ((ExpressionType) en.nextElement()).signatureClass();
            cd.predefineClass(ourClass);
            if (vTableUsed) {
                cd.setVTableUsed(true);
            } else {
                cd.markUsed();
            }
        }
        Enumeration en2 = thrownClasses.elements();
        while (en2.hasMoreElements()) {
            cd = (ClassDefinition) en2.nextElement();
            cd.predefineClass(ourClass);
            if (vTableUsed) {
                cd.setVTableUsed(true);
            } else {
                cd.markUsed();
            }
        }
    }

    void checkMarkedUsed() {
        if (innerUsed) {
            if (used) {
                if (!usedExact
                        && (ourClass.hasExactInstance() || !ourClass
                                .overriddenByAllUsed(this))) {
                    markUsedThisOnly();
                }
            } else if (ourClass.hasInstances()) {
                if (!isAbstract() && allowOverride()
                        && !ourClass.hasExactInstance()
                        && ourClass.overriddenByAllUsed(this)) {
                    used = true;
                } else {
                    markUsedThisOnly();
                }
            }
        }
    }

    void incCallsCount(MethodDefinition callerMethod) {
        singleCaller = singleCaller == null && callerMethod != null ? callerMethod
                : this;
        if (callerMethod != null && callerMethod.definingClass() == ourClass) {
            if (callerMethod != this) {
                ourClass.addInclassCall(this);
            } else {
                referenced = true;
            }
        }
    }

    String nextLabelSuffix() {
        return Integer
                .toString(id.equals("<clinit>") ? ++ourClass.nextClinitLabel
                        : id.equals("<init0>") ? ++ourClass.nextInitZLabel
                                : ++nextLabel);
    }

    boolean used() {
        return used;
    }

    boolean usedExact() {
        return usedExact;
    }

    int getJavaModifiers() {
        return modifiers;
    }

    boolean isConstructor() {
        return id.equals("<init>");
    }

    boolean isAbstract() {
        return (modifiers & AccModifier.ABSTRACT) != 0;
    }

    boolean isClassMethod() {
        return (modifiers & AccModifier.STATIC) != 0;
    }

    boolean isFinal() {
        return (modifiers & AccModifier.FINAL) != 0;
    }

    boolean isNative() {
        return (modifiers & AccModifier.NATIVE) != 0;
    }

    boolean isSynchronized() {
        return (modifiers & AccModifier.SYNCHRONIZED) != 0;
    }

    boolean isPrivate() {
        return (modifiers & AccModifier.PRIVATE) != 0;
    }

    boolean isPublic() {
        return (modifiers & AccModifier.PUBLIC) != 0;
    }

    boolean isProtectedOrPublic() {
        return (modifiers & (AccModifier.PUBLIC | AccModifier.PROTECTED)) != 0;
    }

    boolean allowVirtual() {
        return (modifiers & (AccModifier.PRIVATE | AccModifier.STATIC)) == 0
                && !isConstructor();
    }

    boolean allowOverride() {
        return (modifiers & (AccModifier.PRIVATE | AccModifier.STATIC | AccModifier.FINAL)) == 0
                && !ourClass.isFinal();
    }

    boolean isGetterSetter() {
        if (isClassMethod())
            return false;
        int s1 = resType.objectSize();
        return id.startsWith("get") ? s1 != Type.VOID && !paramList.notEmpty()
                : id.startsWith("is") ? s1 == Type.BOOLEAN && id.length() > 2
                        && !paramList.notEmpty() : id.startsWith("set")
                        && (s1 == Type.VOID || s1 == Type.BOOLEAN)
                        && paramList.tokenCount() == 1;
    }

    Term copyParamList(int skipHeadCnt, int skipTailCnt) {
        int[] skip = new int[2];
        skip[1] = (skip[0] = methodSignature().paramCount() - skipTailCnt)
                - skipHeadCnt;
        return paramList.copyParamList(skip);
    }

    private MethodDefinition replaceMethodEntry() {
        if (!isNative()) {
            MethodDefinition md = body.superMethodCall();
            if (md != null && md != this && md.usedExact()
                    && !md.hasParameters() && resType == md.resType
                    && !md.isClassMethod() && !md.isAbstract()
                    && md.isSynchronized() == isSynchronized())
                return md.replaceMethodEntry();
        }
        return this;
    }

    ExpressionType exprType() {
        return resType;
    }

    ExpressionType actualExprType() {
        if (actualType != null)
            return analysisDone ? actualType : resType;
        int s1;
        if (!usedExact
                || isConstructor()
                || ((s1 = resType.objectSize()) != Type.CLASSINTERFACE && s1 != Type.OBJECTARRAY))
            return resType;
        if (isAbstract()) {
            actualType = resType;
            return actualType;
        }
        producePassOne(null);
        return actualType != null ? actualType : resType;
    }

    void setActualType(ExpressionType actual, Term t) {
        Term.assertCond(analysisBegin);
        returnsCnt++;
        int s0 = actual.objectSize();
        if (s0 == Type.NULLREF || s0 >= Type.CLASSINTERFACE) {
            if (actualType != null) {
                actualType = ClassDefinition.maxCommonExprOf(actualType,
                        actual, ourClass);
                if (!ClassDefinition.isAssignableFrom(resType, actualType,
                        ourClass)) {
                    actualType = resType;
                }
            } else {
                actualType = actual;
            }
            if (s0 == Type.NULLREF || !t.isNotNull()) {
                isNullReturned = true;
            }
        }
        if (s0 == Type.CLASSINTERFACE) {
            if (classNewInstanceCall == null) {
                classNewInstanceCall = t.getClassNewInstanceCall();
            }
            if (constructorInstanceSign == null) {
                constructorInstanceSign = t.getConstructorInstanceSign();
            }
            if (classLiteralValue == null
                    && (resType.name().equals(Names.JAVA_LANG_CLASS) || resType
                            .receiverClass().superClass() == null)) {
                classLiteralValue = t.classLiteralValGuess();
            }
        }
    }

    boolean isNotNull() {
        if (!usedExact || isConstructor() || isAbstract()
                || resType.objectSize() < Type.CLASSINTERFACE)
            return false;
        producePassOne(null);
        return !isNullReturned && actualType != null && analysisDone;
    }

    void resetLocalsActualType(Context c, ObjQueue names, boolean forTry) {
        Enumeration en = names.elements();
        while (en.hasMoreElements()) {
            VariableDefinition v = getLocalVar((String) en.nextElement());
            if (v != null) {
                c.resetVarForLoopOrTry(v);
                if (!forTry) {
                    v.resetTypeForLoop();
                }
            }
        }
    }

    MethodInvocation getClassNewInstanceCall() {
        if (!usedExact || isConstructor() || isAbstract()
                || resType.objectSize() != Type.CLASSINTERFACE)
            return null;
        producePassOne(null);
        return classNewInstanceCall;
    }

    MethodSignature getConstructorInstanceSign() {
        if (!usedExact || isConstructor() || isAbstract()
                || resType.objectSize() != Type.CLASSINTERFACE)
            return null;
        producePassOne(null);
        return constructorInstanceSign;
    }

    ExpressionType classLiteralValGuess() {
        if (!usedExact || isConstructor() || isAbstract()
                || resType.objectSize() != Type.CLASSINTERFACE)
            return null;
        producePassOne(null);
        return classLiteralValue;
    }

    String getJavaSignature() {
        return methodSignature().getJavaSignature()
                + (isConstructor() ? Type.sig[Type.VOID] : resType
                        .getJavaSignature());
    }

    MethodSignature methodSignature() {
        if (ourSig == null) {
            ourSig = new MethodSignature(id, paramList.getSignature());
        }
        return ourSig;
    }

    boolean needsTrigClinit() {
        Term.assertCond(used);
        return needsTrigClinit;
    }

    String newRoutineCName() {
        Term.assertCond(used && isConstructor());
        newReferenced = true;
        return ourClass.routineNameOf(methodSignature().csignForNew(ourClass));
    }

    String routineCName() {
        Term.assertCond(usedExact);
        referenced = true;
        return ourClass.routineNameOf(csign());
    }

    String stackObjRetRoutineCName() {
        Term.assertCond(stackObjRetRequired);
        referenced = true;
        return ourClass.routineNameOf(csign()) + "X";
    }

    String csign() {
        return methodSignature()
                .csign(allowVirtual() && !ourClass.isInterface() ? ourClass
                        : null,
                        isClassMethod() || isConstructor() ? ourClass
                                .castName() : null);
    }

    boolean isExactMatch(boolean isStatic, String exprTypeName,
            int exprTypeDims, String sigString) {
        return isClassMethod() == isStatic
                && resType.signatureDimensions() == exprTypeDims
                && resType.signatureClass().name().equals(exprTypeName)
                && methodSignature().signatureString().equals(sigString);
    }

    boolean addLocalVariable(VariableDefinition v) {
        return fieldDictionary.put(v.id(), v) == null;
    }

    VariableDefinition getLocalVar(String name) {
        return (VariableDefinition) fieldDictionary.get(name);
    }

    Enumeration getLocalsNames() {
        return fieldDictionary.keys();
    }

    void delLocalVar(String name) {
        fieldDictionary.remove(name);
    }

    private void locateHelperMethod() {
        ClassDefinition helperClass = ourClass.getReflectHelperClass();
        if (helperClass != null) {
            helperClass.define(ourClass);
            if (id.equals("<clinit>")) {
                helperMethod = helperClass
                        .getMethodNoInheritance(new MethodSignature("_clinit",
                                EMPTY_VECTOR));
            } else {
                helperMethod = helperClass
                        .getMethodNoInheritance(methodSignature());
                if (helperMethod == null) {
                    helperMethod = helperClass
                            .getMethodNoInheritance(new MethodSignature(id,
                                    EMPTY_VECTOR));
                    if (helperMethod == null && isNative()) {
                        helperMethod = helperClass
                                .getMethodNoInheritance(new MethodSignature(
                                        "_native", EMPTY_VECTOR));
                    }
                    if (helperMethod == null) {
                        helperMethod = helperClass
                                .getMethodNoInheritance(new MethodSignature(
                                        "_", EMPTY_VECTOR));
                    }
                }
            }
        }
    }

    private int initForceVmExc() {
        int forceVmExc = 0;
        Enumeration en = thrownClasses.elements();
        while (en.hasMoreElements()) {
            forceVmExc |= ((ClassDefinition) en.nextElement()).getVMExcMask();
        }
        return forceVmExc;
    }

    BranchContext producePassOne(BranchContext prevBranch) {
        Term.assertCond(ourClass != null);
        if (analysisBegin)
            return null;
        analysisBegin = true;
        Context c = ourClass.cloneContextFor(this);
        if (isSynchronized() && !isNative()) {
            body = new SynchroMethod(body);
        }
        Term.assertCond(c.currentClass == ourClass && c.currentMethod == this);
        locateHelperMethod();
        c.forceVmExc = initForceVmExc();
        if (helperMethod != null) {
            c.forceVmExc |= helperMethod.initForceVmExc();
        }
        c.initBranch();
        if (prevBranch != null) {
            c.unionBranchExceptLocals(prevBranch, !isClassMethod());
        }
        body.processPass1(c);
        if (methodBranch == null || resType.objectSize() == Type.VOID
                || isConstructor()) {
            setMethodBranchFrom(c);
        }
        if (helperMethod != null && prevBranch == null)
            helperMethod.producePassOne(null);
        analysisDone = true;
        return methodBranch;
    }

    void setMethodBranchFrom(Context c) {
        Term.assertCond(analysisBegin && !analysisDone);
        if (methodBranch != null) {
            BranchContext otherBranch = c.swapBranch(methodBranch);
            c.intersectBranch(otherBranch);
            methodBranch = c.swapBranch(otherBranch);
        } else {
            methodBranch = c.saveBranch();
        }
    }

    void processBranch(Context c, boolean isThis) {
        if (usedExact) {
            producePassOne(null);
            if (analysisDone) {
                c.unionBranchExceptLocals(methodBranch, isThis);
            }
        }
    }

    boolean isBranchVarNotNull(VariableDefinition v) {
        if (!analysisDone)
            return false;
        Term.assertCond(v != null);
        return methodBranch.nonNullVars.identityLastIndexOf(v) >= 0;
    }

    ExpressionType getBranchActualType(VariableDefinition v) {
        if (!analysisDone)
            return null;
        Term.assertCond(v != null);
        return methodBranch.getActualType(v);
    }

    void setArgsFormalType(Term actualParamList, Context c) {
        actualParamList.setFormalType(paramList, this, c);
    }

    void setNeedsDummyRet() {
        needsDummyRet = true;
    }

    boolean hasParameters() {
        return paramList.notEmpty();
    }

    boolean isFirstParamUsed() {
        Term t = paramList.getArgumentTerm(0);
        Term.assertCond(t != null);
        VariableDefinition v = getLocalVar(t.dottedName());
        Term.assertCond(v != null);
        return v.used();
    }

    boolean discoverObjLeaks() {
        if (leaksDiscoverProcessing)
            return false;
        Term.assertCond(usedExact);
        if (ourClass.isProxyClass()) {
            leaksDiscoverDone = true;
            return false;
        }
        boolean isArrayCopy = false;
        if (isNative()) {
            if (!ourClass.name().equals(Names.JAVA_LANG_VMOBJECT)
                    || !isExactMatch(true, Names.JAVA_LANG_OBJECT, 0,
                            Names.SIGN_CLONE0)) {
                if (!isClassMethod()
                        || !ourClass.name().equals(Names.JAVA_LANG_VMSYSTEM)
                        || resType.objectSize() != Type.VOID
                        || !methodSignature().signatureString().equals(
                                Names.SIGN_ARRAYCOPY)) {
                    leaksDiscoverDone = true;
                    return false;
                }
                isArrayCopy = true;
            }
        }
        if (!leaksDiscoverDone) {
            ObjQueue oldStackObjRetCalls = Main.dict.stackObjRetCalls;
            Main.dict.stackObjRetCalls = new ObjQueue();
            leaksDiscoverProcessing = true;
            MethodDefinition oldOurMethod = Main.dict.ourMethod;
            Main.dict.ourMethod = this;
            ObjHashtable oldAssignedLocals = Main.dict.assignedLocals;
            Main.dict.assignedLocals = new ObjHashtable();
            paramList.discoverObjLeaks();
            if (isArrayCopy) {
                paramList.getTermAt(1).getTermAt(1).getTermAt(0)
                        .setObjLeaks(VariableDefinition.WRITABLE_ARRAY_VAR);
            }
            body.discoverObjLeaks();
            Main.dict.assignedLocals = oldAssignedLocals;
            leaksDiscoverDone = true;
            setRequiredStackObjRets(Main.dict.stackObjRetCalls);
            leaksDiscoverProcessing = false;
            Main.dict.ourMethod = oldOurMethod;
            Main.dict.stackObjRetCalls = oldStackObjRetCalls;
        }
        return true;
    }

    boolean copyObjLeaksTo(Term argsList) {
        if (!usedExact)
            return false;
        if (!leaksDiscoverProcessing && !leaksDiscoverDone && isClassMethod()) {
            ObjVector parmSig = new ObjVector();
            paramList.storeSignature(parmSig);
            int i = parmSig.size();
            while (i-- > 0) {
                if (((ExpressionType) parmSig.elementAt(i)).objectSize() >= Type.CLASSINTERFACE)
                    break;
            }
            if (i < 0)
                return true;
        }
        return argsList.copyObjLeaksFrom(discoverObjLeaks() ? paramList : null);
    }

    void setThisObjLeak(boolean isReturned) {
        Term.assertCond(leaksDiscoverProcessing);
        if (isReturned) {
            hasThisObjRet = true;
        } else {
            hasThisObjLeak = true;
        }
    }

    boolean hasThisObjLeak(boolean isReturned) {
        if (isClassMethod() || !usedExact)
            return false;
        if (!discoverObjLeaks())
            return !isReturned;
        return isReturned ? hasThisObjRet && !hasThisObjLeak : hasThisObjLeak;
    }

    void setThisStackObjVolatile() {
        Term.assertCond(leaksDiscoverProcessing);
        isThisStackObjVolatile = true;
    }

    boolean isThisStackObjVolatile() {
        return isThisStackObjVolatile;
    }

    boolean attachRetWritableArray(VariableDefinition v) {
        Term.assertCond(leaksDiscoverProcessing && !isConstructor()
                && resType.objectSize() >= Type.CLASSINTERFACE);
        if (retWritableArrays == EMPTY_VECTOR)
            return false;
        if (retWritableArrays == null) {
            retWritableArrays = new ObjVector();
        }
        if (retWritableArrays.identityLastIndexOf(v) < 0) {
            retWritableArrays.addElement(v);
        }
        return true;
    }

    void setWritableArray() {
        Term.assertCond(resType.objectSize() >= Type.CLASSINTERFACE);
        if (retWritableArrays != EMPTY_VECTOR) {
            if (retWritableArrays != null) {
                Enumeration en = retWritableArrays.elements();
                retWritableArrays = EMPTY_VECTOR;
                while (en.hasMoreElements()) {
                    ((VariableDefinition) en.nextElement())
                            .setWritableArray(null);
                }
            } else {
                retWritableArrays = EMPTY_VECTOR;
            }
        }
    }

    void setStackObjRetTerm(Term t) {
        Term.assertCond(leaksDiscoverProcessing && !isConstructor()
                && resType.objectSize() >= Type.CLASSINTERFACE);
        if (stackObjRetTerm == null) {
            stackObjRetTerm = t;
        }
    }

    void attachStackObjRetTerm(VariableDefinition v, Term t) {
        Term.assertCond(leaksDiscoverProcessing);
        if (stackObjRetTerm == t && (v == null || !v.addSetObjLeaksTerm(t))) {
            stackObjRetTerm = null;
        }
    }

    boolean allowStackObjRet() {
        return (!usedExact || (returnsCnt == 1
                && resType.objectSize() >= Type.CLASSINTERFACE
                && discoverObjLeaks() && stackObjRetTerm != null))
                && !isVMSpecialMethod();
    }

    static void setRequiredStackObjRets(ObjQueue stackObjRetCalls) {
        Enumeration en = stackObjRetCalls.elements();
        while (en.hasMoreElements()) {
            MethodDefinition md = ((MethodInvocation) en.nextElement())
                    .getNoLeaksScopeMethod();
            while (md != null && !md.stackObjRetRequired) {
                Term t = md.stackObjRetTerm;
                Term.assertCond(t != null);
                md.stackObjRetRequired = true;
                md = t.stackObjRetMethodCall();
            }
        }
    }

    boolean stackObjRetRequired() {
        if (!usedExact)
            return true;
        Term.assertCond(leaksDiscoverDone && stackObjRetTerm != null);
        return stackObjRetRequired;
    }

    String writeStackObjDefn(OutputContext oc, boolean needsLocalVolatile) {
        if (!usedExact)
            return null;
        Term.assertCond(!leaksDiscoverProcessing && leaksDiscoverDone
                && stackObjRetTerm != null && stackObjRetRequired);
        return stackObjRetTerm.writeStackObjDefn(oc, needsLocalVolatile);
    }

    void writeStackObjTrigClinit(OutputContext oc) {
        Term.assertCond(stackObjRetTerm != null);
        stackObjRetTerm.writeStackObjTrigClinit(oc);
    }

    ExpressionType writeStackObjRetCode(OutputContext oc) {
        Term.assertCond(!leaksDiscoverProcessing && leaksDiscoverDone
                && stackObjRetTerm != null && stackObjRetRequired);
        return stackObjRetTerm.writeStackObjRetCode(oc);
    }

    boolean allowInline() {
        if (isConstructor())
            return (hasParameters() && allowSingleInline())
                    || allowBodyInline();
        if (isNative())
            return allowSingleInline() && !Names.isVMCoreClass(ourClass.name());
        return ((!isClassMethod() && !ourClass.hasRealInstances())
                || allowSingleInline() || (allowBodyInline() && !ourClass
                .isProxyClass())) && !isVMSpecialMethod();
    }

    private boolean allowBodyInline() {
        return body.allowInline(paramList.tokenCount()
                + MAX_INLINE
                + (isClassMethod() || isConstructor() ? (needsTrigClinit
                        && ourClass.classInitializerNotCalledYet() ? -2 : 0)
                        : 1)
                - (isSynchronized() && resType.objectSize() != Type.VOID ? 2
                        : 0));
    }

    private boolean allowSingleInline() {
        return !isVirtual
                && hasSingleCaller()
                && (singleCaller.hasSingleCaller()
                        || singleCaller.id.equals("<clinit>") || !singleCaller
                        .allowInline());
    }

    private boolean hasSingleCaller() {
        return singleCaller != null && singleCaller != this;
    }

    private boolean isVMSpecialMethod() {
        if (ourClass.name().equals(Names.JAVA_LANG_OBJECT)
                && !isClassMethod()
                && resType.objectSize() == Type.VOID
                && methodSignature().signatureString().equals(
                        Names.SIGN_FINALIZE))
            return true;
        int pos = id.length();
        char ch;
        if (pos > 2 && (id.charAt(pos - 1) != 'X' || --pos > 2)
                && id.charAt(pos - 1) == '0'
                && ((ch = id.charAt(pos - 2)) < '0' || ch > '9')) {
            String className = ourClass.name();
            if (className.startsWith(Names.JAVA_LANG_REF_0)
                    || className.startsWith(Names.JAVA_LANG_REFLECT_0)
                    || ((pos = className.lastIndexOf('.')) > 0
                            && className.length() - pos > 2
                            && className.charAt(pos + 1) == 'V' && className
                            .charAt(pos + 2) == 'M'))
                return true;
        }
        return false;
    }

    boolean writeMethodCall(OutputContext oc, ClassDefinition receiverClass,
            String receiverString, int isNotNull, boolean hasStackObjRet,
            ExpressionType origResType) {
        Term.assertCond(receiverString != null);
        boolean rightParenNeeded = false;
        if (origResType != resType) {
            oc.cPrint("(");
            oc.cPrint(origResType.castName());
            oc.cPrint(")");
        }
        if (used
                && (receiverClass == null || receiverClass.hasRealInstances())
                && (isAbstract() || (receiverClass != null && allowOverride() && receiverClass
                        .subclassHasMethod(
                                methodSignature().signatureString(),
                                isProtectedOrPublic() ? null : ourClass
                                        .getPackageName())))) {
            Term.assertCond(!hasStackObjRet);
            if (!isVirtual) {
                isVirtual = true;
                ourClass.setVirtualInSubclasses(methodSignature()
                        .signatureString());
            }
            oc.cPrint(isNotNull > 0 ? "JCGO_CALL_NZVFUNC"
                    : isNotNull != 0 ? "JCGO_CALL_EVFUNC" : "JCGO_CALL_VFUNC");
            oc.cPrint("(");
            oc.cPrint(receiverString);
            oc.cPrint(")->");
            oc.cPrint(csign());
            Main.dict.indirectCalls++;
        } else {
            if (isNotNull <= 0) {
                if (origResType != resType) {
                    oc.cPrint("(");
                    rightParenNeeded = true;
                }
                oc.cPrint(isNotNull != 0 ? "JCGO_CALL_EFINALF"
                        : "JCGO_CALL_FINALF");
                oc.cPrint("(");
                oc.cPrint(receiverString);
                oc.cPrint(") ");
            }
            if (usedExact
                    && (receiverClass == null || receiverClass
                            .hasRealInstances())) {
                oc.cPrint(hasStackObjRet ? stackObjRetRoutineCName()
                        : routineCName());
                Main.dict.normalCalls++;
            } else {
                oc.cPrint("(");
                oc.cPrint(resType.castName());
                oc.cPrint(")");
                oc.cPrint(resType.objectSize() >= Type.CLASSINTERFACE ? LexTerm.NULL_STR
                        : "0");
            }
        }
        return rightParenNeeded;
    }

    void produceOutput(OutputContext oc) {
        Term.assertCond(analysisDone && leaksDiscoverDone);
        if (outputDone)
            return;
        outputDone = true;
        if (isAbstract())
            return;
        Main.dict.methodsWritten++;
        Main.dict.message("Writing method: " + ourClass.name() + "."
                + methodSignature().getInfo());
        if (!stackObjRetRequired) {
            stackObjRetTerm = null;
        }
        boolean alreadyReferenced = referenced;
        String parms = OutputContext.paramStringOutputNoComma(paramList, false);
        boolean hasParams = true;
        if (parms.length() == 0) {
            hasParams = false;
            parms = Type.cName[Type.VOID];
        }
        String jniName = null;
        if (isNative() && !Names.isVMCoreClass(ourClass.name())) {
            jniName = methodSignature().getJniNameNoPrefix(ourClass);
            oc.hPrint("#ifndef NOJAVA_");
            oc.hPrint(jniName);
            oc.hPrint("\n");
            oc.hPrint("JNIIMPORT ");
            oc.hPrint(resType.getJniName());
            oc.hPrint(" JNICALL JCGO_JNI_FUNC(Java_");
            oc.hPrint(jniName);
            oc.hPrint(")( JNIEnv *pJniEnv, ");
            oc.hPrint((isClassMethod() ? Main.dict.get(Names.JAVA_LANG_CLASS)
                    : ourClass).getJniName());
            oc.hPrint(" ");
            oc.hPrint(This.CNAME);
            OutputContext oc2 = new OutputContext();
            paramList.parameterOutput(oc2, false, Type.VOID);
            oc.hPrint(oc2.instanceToString());
            oc.hPrint(" );");
            oc.hPrint("#endif\n");
        }
        StringBuffer sb = new StringBuffer();
        if (!isClassMethod()
                || ourClass != Main.dict.mainClass
                || (modifiers & (AccModifier.PRIVATE | AccModifier.PROTECTED | AccModifier.NATIVE)) != 0)
            sb.append(stackObjRetRequired || allowInline() ? (alreadyReferenced ? "JCGO_NOSEP_FRWINL "
                    : "JCGO_NOSEP_INLINE ")
                    : "JCGO_NOSEP_STATIC ");
        sb.append(resType.castName());
        sb.append(" CFASTCALL\n");
        sb.append(routineCName());
        sb.append("( ");
        if (isClassMethod()) {
            sb.append(parms);
        } else {
            sb.append(ourClass.castName());
            sb.append(' ');
            sb.append(This.CNAME);
            if (hasParams) {
                sb.append(", ");
                sb.append(parms);
            }
        }
        sb.append(" )");
        if (isNative() && ourClass.name().equals(Names.JAVA_LANG_VMMATH)) {
            oc.hPrint("#define JCGO_NATMATH_");
            oc.hPrint(csign());
            oc.hPrint("\n");
        }
        oc.hPrint(sb.toString());
        oc.hPrint(";");
        if (jniName != null || !isNative()) {
            String stackObjRetCode = null;
            if (stackObjRetRequired) {
                oc.cAndHPrint(allowInline() ? (alreadyReferenced ? "JCGO_NOSEP_FRWINL "
                        : "JCGO_NOSEP_INLINE ")
                        : "JCGO_NOSEP_STATIC ");
                oc.cAndHPrint(resType.castName());
                oc.cAndHPrint(" CFASTCALL\n");
                oc.cAndHPrint(stackObjRetRoutineCName());
                oc.cAndHPrint("( ");
                if (!isClassMethod()) {
                    oc.cAndHPrint(ourClass.castName());
                    oc.cAndHPrint(" ");
                    oc.cAndHPrint(This.CNAME);
                    oc.cAndHPrint(", ");
                }
                if (hasParams) {
                    oc.cAndHPrint(parms);
                    oc.cAndHPrint(", ");
                }
                OutputContext oc2 = new OutputContext();
                oc.cAndHPrint(writeStackObjRetCode(oc2).castName());
                stackObjRetCode = oc2.instanceToString();
                oc.cAndHPrint(" ");
                oc.cAndHPrint(STACKOBJ_RETNAME);
                oc.cAndHPrint(" )");
                oc.hPrint(";");
            } else {
                oc.cPrint(sb.toString());
            }
            oc.cPrint("{");
            if (jniName != null) {
                oc.cPrint("\n#ifdef NOJAVA_");
                oc.cPrint(jniName);
                oc.cPrint("\010");
                oc.cPrint("jcgo_jniNoNativeFunc();");
                if (resType.objectSize() != Type.VOID) {
                    oc.cPrint("return ");
                    oc.cPrint(resType.objectSize() < Type.CLASSINTERFACE ? "("
                            + resType.castName() + ")0" : LexTerm.NULL_STR);
                    oc.cPrint(";");
                }
                oc.cPrint("\n#else\010");
                String targetStr = This.CNAME;
                boolean hasRetVal = false;
                if (resType.objectSize() < Type.VOID
                        || (isSynchronized() && resType.objectSize() > Type.VOID)) {
                    oc.cPrint(resType.castName());
                    oc.cPrint(" jcgo_retval;");
                    hasRetVal = true;
                }
                boolean needsBrace = false;
                if (isClassMethod()) {
                    targetStr = ourClass.getClassRefStr(false);
                    if (needsTrigClinit && ourClass.writeTrigClinit(oc)) {
                        oc.cPrint("{");
                        needsBrace = true;
                    }
                }
                if (isSynchronized()) {
                    oc.cPrint("JCGO_SYNC_BLOCKSAFENZ(");
                    oc.cPrint(targetStr);
                    oc.cPrint("){");
                }
                int objArgsCnt = 0;
                Enumeration en = methodSignature().elements();
                while (en.hasMoreElements()) {
                    if (((ExpressionType) en.nextElement()).objectSize() > Type.VOID) {
                        objArgsCnt++;
                    }
                }
                oc.cPrint("JCGO_JNI_BLOCK(");
                oc.cPrint(Integer.toString(objArgsCnt));
                oc.cPrint(")\010");
                if (resType.objectSize() != Type.VOID) {
                    oc.cPrint(hasRetVal ? "jcgo_retval= " : "return ");
                    if (resType.objectSize() > Type.VOID) {
                        oc.cPrint("(");
                        oc.cPrint(resType.castName());
                        oc.cPrint(")jcgo_jniLeave(jcgo_pJniEnv, ");
                        if (!resType.getJniName().equals(
                                Type.jniName[Type.CLASSINTERFACE])) {
                            oc.cPrint("(");
                            oc.cPrint(Type.jniName[Type.CLASSINTERFACE]);
                            oc.cPrint(")");
                        }
                    }
                }
                oc.cPrint("JCGO_JNI_FUNC(Java_");
                oc.cPrint(jniName);
                oc.cPrint(")(jcgo_pJniEnv, ");
                Term.assertCond(oc.arrInitCount == -1);
                VariableDefinition.outputJniParam(oc,
                        isClassMethod() ? Main.dict.get(Names.JAVA_LANG_CLASS)
                                : ourClass, targetStr);
                paramList.parameterOutput(oc, true, Type.VOID);
                oc.arrInitCount = -1;
                oc.cPrint(")");
                if (resType.objectSize() <= Type.VOID) {
                    oc.cPrint(";");
                    oc.cPrint("jcgo_jniLeave(jcgo_pJniEnv, NULL");
                }
                oc.cPrint(");");
                if (isSynchronized()) {
                    oc.cPrint("}");
                    oc.cPrint("JCGO_SYNC_END\010");
                }
                if (needsBrace) {
                    oc.cPrint("}");
                }
                if (hasRetVal) {
                    oc.cPrint("return jcgo_retval;");
                }
                oc.cPrint("\n#endif\010");
            } else if (ourClass.isProxyClass() && !isConstructor()) {
                produceProxyMethodBody(oc);
            } else {
                if (ourClass.hasRealInstances() || isClassMethod()) {
                    boolean needsBrace = false;
                    if (isClassMethod()
                            && (needsTrigClinit || isVMSpecialMethod())) {
                        needsBrace = ourClass.writeTrigClinit(oc);
                    }
                    Enumeration en = getLocalsNames();
                    while (en.hasMoreElements()) {
                        if (getLocalVar((String) en.nextElement())
                                .outputParamNoClobber(oc)) {
                            needsBrace = true;
                        }
                    }
                    if (isSynchronized()) {
                        if (needsBrace) {
                            oc.cPrint("{");
                        }
                        oc.cPrint("JCGO_SYNC_BLOCKSAFENZ(");
                        oc.cPrint(isClassMethod() ? ourClass
                                .getClassRefStr(false) : This.CNAME);
                        oc.cPrint(")\010");
                    } else {
                        needsBrace = false;
                    }
                    oc.stackObjCountReset();
                    body.processOutput(oc);
                    if (needsBrace) {
                        oc.cPrint("}");
                    }
                    if (needsDummyRet && body.isReturnAtEnd(false)) {
                        needsDummyRet = false;
                    }
                } else {
                    needsDummyRet = true;
                }
                if (needsDummyRet && resType.objectSize() != Type.VOID) {
                    oc.cPrint("return ");
                    oc.cPrint(isConstructor() ? "This"
                            : resType.objectSize() < Type.CLASSINTERFACE ? "("
                                    + resType.castName() + ")0"
                                    : LexTerm.NULL_STR);
                    oc.cPrint(";");
                }
            }
            oc.cPrint("}\n\n");
            if (stackObjRetRequired) {
                Term.assertCond(jniName == null);
                oc.cPrint(sb.toString());
                oc.cPrint("{");
                writeStackObjTrigClinit(oc);
                oc.cPrint("return ");
                oc.cPrint(stackObjRetRoutineCName());
                oc.cPrint("(");
                if (isClassMethod()) {
                    oc.cPrint("\010 ");
                } else {
                    oc.cPrint(This.CNAME);
                    oc.cPrint(", ");
                }
                if (hasParams) {
                    oc.cPrint(OutputContext.paramStringOutputNoComma(paramList,
                            true));
                    oc.cPrint(", ");
                }
                oc.cPrint(stackObjRetCode);
                oc.cPrint(");}\n\n");
            }
        }
        if (isConstructor() && isNewUsed) {
            Term.assertCond(!ourClass.isAbstractOrInterface());
            oc.cAndHPrint(newReferenced ? "JCGO_NOSEP_FRWINL "
                    : "JCGO_NOSEP_INLINE ");
            oc.cAndHPrint(resType.castName());
            oc.cAndHPrint(" CFASTCALL\n");
            oc.cAndHPrint(newRoutineCName());
            oc.cAndHPrint("( ");
            oc.cAndHPrint(parms);
            oc.cAndHPrint(" )");
            oc.hPrint(";");
            oc.cPrint("{");
            if (needsTrigClinit) {
                ourClass.writeTrigClinit(oc);
            }
            oc.cPrint("return ");
            oc.cPrint(routineCName());
            oc.cPrint("(\010 ");
            oc.cPrint(ourClass.cNewObjectCode());
            oc.parameterOutputAsArg(paramList);
            oc.cPrint(");}\n\n");
        }
        oc.hPrint("\n\n");
    }

    private void produceProxyMethodBody(OutputContext oc) {
        MethodDefinition md = Main.dict.get(Names.JAVA_LANG_REFLECT_VMPROXY)
                .getMethod(Names.SIGN_INVOKEPROXYHANDLER0X);
        if (md == null || !md.usedExact() || !md.isClassMethod()) {
            if (resType.objectSize() != Type.VOID) {
                oc.cPrint("return ");
                oc.cPrint(resType.objectSize() < Type.CLASSINTERFACE ? "("
                        + resType.castName() + ")0" : LexTerm.NULL_STR);
                oc.cPrint(";");
            }
            return;
        }
        int[] indices = new int[Type.VOID - Type.INT + 1];
        MethodSignature msig = methodSignature();
        Enumeration en = msig.elements();
        while (en.hasMoreElements()) {
            int s0 = ((ExpressionType) en.nextElement()).objectSize();
            indices[s0 <= Type.INT ? 0 : s0 < Type.VOID ? s0 - Type.INT
                    : Type.VOID - Type.INT]++;
        }
        if (resType.objectSize() < Type.VOID) {
            int s1 = resType.objectSize();
            if (s1 <= Type.INT) {
                s1 = Type.INT;
            }
            if (indices[s1 - Type.INT] == 0) {
                indices[s1 - Type.INT] = 1;
            }
        }
        for (int s1 = Type.INT; s1 <= Type.VOID; s1++) {
            if (indices[s1 - Type.INT] > 0) {
                oc.cPrint(Main.dict.addArrayTypeDefn(s1,
                        indices[s1 - Type.INT], ourClass, null));
                oc.cPrint(" jcgo_");
                oc.cPrint(s1 != Type.VOID ? Type.name[s1] : "object");
                oc.cPrint("Args;");
            }
        }
        for (int s1 = Type.INT; s1 <= Type.VOID; s1++) {
            if (indices[s1 - Type.INT] > 0) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[Type.VOID]);
                oc.cPrint(")");
                oc.cPrint(s1 != Type.VOID ? "JCGO_STACKOBJ_PRIMARRNEW"
                        : "JCGO_STACKOBJ_OBJARRNEW");
                oc.cPrint("(jcgo_");
                oc.cPrint(s1 != Type.VOID ? Type.name[s1] : "object");
                oc.cPrint("Args, ");
                oc.cPrint(ClassDefinition.arrayVTableCName(s1, 0));
                oc.cPrint(", ");
                if (s1 == Type.VOID) {
                    oc.cPrint(Main.dict.get(Names.JAVA_LANG_OBJECT)
                            .getClassRefStr(false));
                    oc.cPrint(", ");
                }
                oc.cPrint(Integer.toString(indices[s1 - Type.INT]));
                oc.cPrint(");");
                indices[s1 - Type.INT] = 0;
            }
        }
        Enumeration en2 = msig.elements();
        for (int index = 0; en2.hasMoreElements(); index++) {
            int s0 = ((ExpressionType) en2.nextElement()).objectSize();
            int s1 = s0 <= Type.INT ? Type.INT : s0 < Type.VOID ? s0
                    : Type.VOID;
            oc.cPrint("JCGO_ARR_INTERNALACC(");
            oc.cPrint(Type.cName[s1 != Type.VOID ? s1 : Type.CLASSINTERFACE]);
            oc.cPrint(", (");
            oc.cPrint(Type.cName[s1 + Type.CLASSINTERFACE]);
            oc.cPrint(")JCGO_OBJREF_OF(jcgo_");
            oc.cPrint(s1 != Type.VOID ? Type.name[s1] : "object");
            oc.cPrint("Args), ");
            oc.cPrint(Integer.toString(indices[s1 - Type.INT]++));
            oc.cPrint(")= ");
            if (s0 >= Type.CLASSINTERFACE) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
                oc.cPrint(")");
            } else if (s0 < Type.INT && s0 > Type.BOOLEAN) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[Type.INT]);
                oc.cPrint(")");
            }
            Term t = paramList.getArgumentTerm(index);
            Term.assertCond(t != null);
            OutputContext oc2 = new OutputContext();
            t.parameterOutput(oc2, true, s0 < Type.CLASSINTERFACE ? s0
                    : Type.NULLREF);
            oc.cPrint(oc2.instanceToString().substring(2));
            if (s0 == Type.BOOLEAN) {
                oc.cPrint("? 1\003 :\003 0");
            }
            oc.cPrint(";");
        }
        if (resType.objectSize() >= Type.CLASSINTERFACE) {
            oc.cPrint("return ");
            if (md.exprType() != resType) {
                oc.cPrint("(");
                oc.cPrint(resType.castName());
                oc.cPrint(")");
            }
        }
        oc.cPrint(md.routineCName());
        oc.cPrint("((");
        oc.cPrint(Main.dict.get(Names.JAVA_LANG_OBJECT).castName());
        oc.cPrint(")");
        oc.cPrint(This.CNAME);
        oc.cPrint(", ");
        String sigString = methodSignature().signatureString();
        md = ourClass.getOverridenMethod(sigString, resType);
        Term.assertCond(md != null && md.used);
        ClassDefinition cd = md.definingClass();
        oc.cPrint(cd.getClassRefStr(false));
        int s0 = resType.objectSize();
        for (int s1 = Type.INT; s1 <= Type.VOID; s1++) {
            oc.cPrint(", ");
            if (indices[s1 - Type.INT] > 0
                    || (s0 < Type.VOID && (s0 > Type.INT ? s0 : Type.INT) == s1)) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[s1 + Type.CLASSINTERFACE]);
                oc.cPrint(")JCGO_OBJREF_OF(jcgo_");
                oc.cPrint(s1 != Type.VOID ? Type.name[s1] : "object");
                oc.cPrint("Args)");
            } else {
                oc.cPrint(LexTerm.NULL_STR);
            }
        }
        oc.cPrint(", ");
        oc.cPrint(Integer.toString(cd.getReflectedMethodSlot(sigString)));
        oc.cPrint(", ");
        oc.cPrint(Integer.toString(ourClass.getReflectedMethodSlot(sigString)));
        oc.cPrint(");");
        if (s0 < Type.VOID) {
            oc.cPrint("return ");
            boolean isBool = false;
            if (s0 < Type.INT) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[s0]);
                oc.cPrint(")");
                if (s0 == Type.BOOLEAN) {
                    oc.cPrint("(");
                    isBool = true;
                }
                s0 = Type.INT;
            }
            oc.cPrint("JCGO_ARR_INTERNALACC(");
            oc.cPrint(Type.cName[s0]);
            oc.cPrint(", (");
            oc.cPrint(Type.cName[s0 + Type.CLASSINTERFACE]);
            oc.cPrint(")JCGO_OBJREF_OF(jcgo_");
            oc.cPrint(Type.name[s0]);
            oc.cPrint("Args), 0)");
            if (isBool) {
                oc.cPrint("!= 0)");
            }
            oc.cPrint(";");
        }
    }

    boolean isVirtualUsed() {
        return isVirtual;
    }

    void produceJumpTableEntry(ClassDefinition currentClass, boolean isDuplicate) {
        Term.assertCond(used);
        OutputContext oc = currentClass.outputContext();
        String parms = OutputContext.paramStringOutputNoComma(paramList, false);
        oc.hPrint(resType.castName());
        oc.hPrint(" (CFASTCALL *");
        oc.hPrint(isDuplicate ? currentClass.nextDummyEntryName() : csign());
        oc.hPrint(")( ");
        oc.hPrint(ourClass.castName());
        oc.hPrint(" ");
        oc.hPrint(This.CNAME);
        if (parms.length() > 0) {
            oc.hPrint(", ");
            oc.hPrint(parms);
        }
        oc.hPrint(" );");
        if (!currentClass.isNotInstantated()) {
            oc.cPrint(",\010");
            if (isAbstract() || !usedExact) {
                oc.cPrint("0");
            } else {
                MethodDefinition md = this;
                if (!hasParameters()) {
                    md = replaceMethodEntry();
                    if (resType.objectSize() == Type.VOID && !md.isNative()
                            && !md.hasNonEmptyBody()) {
                        md = Main.dict.get(Names.JAVA_LANG_OBJECT).getMethod(
                                Names.SIGN_FINALIZE);
                        if (md == null || !md.usedExact()
                                || resType != md.resType || md.isClassMethod()) {
                            md = this;
                        }
                    }
                    if (md.definingClass() != ourClass) {
                        oc.cPrint("(");
                        oc.cPrint(resType.castName());
                        oc.cPrint(" (CFASTCALL*)(");
                        oc.cPrint(ourClass.castName());
                        oc.cPrint("))");
                    }
                }
                oc.cPrint(md.routineCName());
            }
        }
    }

    void setTraceExprType(ExpressionType curType) {
        if (resType.objectSize() >= Type.CLASSINTERFACE) {
            if (!curType.hasRealInstances()) {
                curType = Main.dict.classTable[Type.NULLREF];
            }
            curTraceType = curTraceType != null ? ClassDefinition
                    .maxCommonExprOf(curTraceType, curType, null) : curType;
        }
    }

    ExpressionType traceBody(ObjVector parmTraceSig) {
        Term.assertCond(usedExact);
        if (!isConstructor() && !isClassMethod() && ourClass.isProxyClass()) {
            MethodDefinition md = Main.dict
                    .get(Names.JAVA_LANG_REFLECT_VMPROXY).getMethod(
                            Names.SIGN_INVOKEPROXYHANDLER0X);
            if (md != null) {
                md.methodTraceClassInit(false, null, null);
            }
            return null;
        }
        if (helperMethod != null) {
            Main.dict.curHelperForMethod = this;
            helperMethod.body.traceClassInit();
            Main.dict.curHelperForMethod = null;
        }
        if (isNative()) {
            if (!Names.isVMCoreClass(ourClass.name())) {
                ourClass.classTraceClassInit(false);
                ClassDefinition cd = Main.dict.get(Names.JAVA_LANG_VMCLASS);
                MethodDefinition md;
                if (cd.used()
                        && (md = cd.getMethod(Names.SIGN_ARRAYCLASSOF0X)) != null
                        && md.isClassMethod()) {
                    md.methodTraceClassInit(true, null, null);
                }
            }
            ClassDefinition cd = resType.signatureClass();
            if (cd.objectSize() == Type.CLASSINTERFACE && cd.used()) {
                if (!cd.constructorTraceClassInit(false)) {
                    cd.instanceCreatedOnTrace();
                }
                if (cd.superClass() != null) {
                    cd.constructorTraceClassInitInSubclasses();
                }
            }
            return null;
        }
        if (hasParameters()) {
            paramList.setTraceExprType(parmTraceSig != null ? parmTraceSig
                    .elements() : null);
        }
        ExpressionType oldCurTraceType = curTraceType;
        curTraceType = null;
        body.traceClassInit();
        ExpressionType curType = null;
        if (actualType != null && (curType = curTraceType) != null) {
            if (!actualType.hasRealInstances()) {
                curType = Main.dict.classTable[Type.NULLREF];
            } else if (!ClassDefinition.isAssignableFrom(actualType, curType,
                    null) || actualType == curType) {
                curType = null;
            }
        }
        curTraceType = oldCurTraceType;
        return curType;
    }

    ExpressionType methodTraceClassInit(boolean isWeak,
            ExpressionType curActualClass, ObjVector parmTraceSig) {
        ExpressionType curType = null;
        if (usedExact) {
            if (isWeak && !Main.dict.classInitWeakDepend) {
                Main.dict.classInitWeakDepend = true;
                curType = methodTraceClassInit(false, curActualClass,
                        parmTraceSig);
                Main.dict.classInitWeakDepend = false;
                return curType;
            }
            if (isClassMethod()) {
                curActualClass = null;
            } else {
                Term.assertCond(curActualClass == null
                        || ourClass.isAssignableFrom(
                                curActualClass.receiverClass(), 0, null));
                if (!ourClass.hasRealInstances() && !isConstructor())
                    return resType.objectSize() >= Type.CLASSINTERFACE ? Main.dict.classTable[Type.NULLREF]
                            : null;
            }
            MethodTraceInfo traceInfo = MethodTraceInfo.create(this,
                    curActualClass, isNative() ? null : parmTraceSig);
            Main.dict.curTraceInfo.addMethodCall(traceInfo);
            if (traceInfo.isNotTraced() && !isConstructor()) {
                ExpressionType actual = actualExprType();
                int s0;
                if (((s0 = actual.objectSize()) == Type.CLASSINTERFACE || s0 == Type.OBJECTARRAY)
                        && actual.signatureClass().hasInstantatedSubclasses(
                                false)) {
                    traceInfo.traceMethod();
                }
            }
            curType = traceInfo.getResTraceType();
        }
        ClassDefinition aclass;
        if (used
                && curActualClass != null
                && isVirtual
                && allowOverride()
                && (aclass = curActualClass.receiverClass()) == curActualClass
                && aclass.superClass() != null
                && (!aclass.name().equals(Names.JAVA_LANG_THROWABLE) || !resType
                        .name().equals(Names.JAVA_LANG_STRING))) {
            ExpressionType curType2 = aclass.traceClInitInSubclasses(
                    methodSignature().signatureString(),
                    isProtectedOrPublic() ? null : ourClass.getPackageName(),
                    !isAbstract(), parmTraceSig);
            if (curType != null) {
                curType = isAbstract() ? curType2 : ClassDefinition
                        .maxCommonExprOf(curType, curType2, null);
                if (actualType != null
                        && !ClassDefinition.isAssignableFrom(actualType,
                                curType, null)) {
                    curType = actualType;
                }
            } else if (!usedExact) {
                curType = curType2;
            }
        }
        if (usedExact
                && ourClass.name().equals(Names.JAVA_LANG_THREAD)
                && methodSignature().signatureString().equals(
                        Names.SIGN_INIT_THREADGROUP_2)) {
            MethodDefinition md = null;
            if ((curActualClass != null
                    && (md = curActualClass.receiverClass().getMethod(
                            Names.SIGN_RUN)) != null && md.definingClass() != ourClass)
                    || (parmTraceSig == null && (md != null || (md = ourClass
                            .getMethod(Names.SIGN_RUN)) != null))) {
                md.methodTraceClassInit(false, curActualClass, null);
            } else {
                ExpressionType curType2;
                if (parmTraceSig != null
                        && parmTraceSig.size() > 1
                        && (curType2 = (ExpressionType) parmTraceSig
                                .elementAt(1)).objectSize() != Type.NULLREF) {
                    aclass = Main.dict.get(Names.JAVA_LANG_RUNNABLE);
                    if (aclass.used()
                            && aclass.isAssignableFrom(
                                    curType2.receiverClass(), 0, null)
                            && (md = curType2.receiverClass().getMethod(
                                    Names.SIGN_RUN)) != null) {
                        md.methodTraceClassInit(false, curType2, null);
                    } else if ((md = ourClass.getMethod(Names.SIGN_RUN)) != null) {
                        md.methodTraceClassInit(false, curActualClass, null);
                    }
                }
            }
        }
        return curType;
    }
}
