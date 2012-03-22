/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/MethodInvocation.java --
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

/**
 * Grammar production for normal and primary method calls.
 ** 
 * Formats: QualifiedName LPAREN [ArgumentList] RPAREN Primary DOT ID LPAREN
 * [ArgumentList] RPAREN Super DOT ID LPAREN [ArgumentList] RPAREN
 */

final class MethodInvocation extends LexNode {

    private ClassDefinition resultClass;

    private ClassDefinition actualClass;

    private boolean isExact;

    private MethodDefinition md;

    private MethodDefinition actualMethod;

    private String resultString;

    private ExpressionType classLiteralValue;

    private int rcvr;

    private ExpressionType reflectedClass;

    private boolean reflectInSuper;

    private String reflectedMethodId;

    private ObjVector reflectedParmSig;

    private boolean forceCheck;

    private LeftBrace noLeaksScope;

    private boolean noStackObjRet;

    private boolean isConditional;

    private String stackObjCode;

    private boolean needsLocalVolatile;

    private boolean isVoidExpr;

    MethodInvocation(Term a, Term c) {
        super(a, Empty.newTerm(), c);
    }

    MethodInvocation(Term a, Term c, Term e) {
        super(a, c, e);
    }

    MethodInvocation(Context c, ClassDefinition resultClass, String id,
            Term exprTerm) {
        super(Empty.newTerm(), Empty.newTerm(), (new Argument(exprTerm, true))
                .setLineInfoFrom(exprTerm));
        assertCond(c.currentClass != null);
        setLineInfoFrom(exprTerm);
        this.resultClass = resultClass;
        processPassOneInner(c, null, resultClass, 0, id);
        if (md != null && !md.isClassMethod()) {
            undefinedMethod(resultClass, md.methodSignature(), c);
        }
    }

    MethodInvocation(Context c, Term t, String id) {
        super(t, Empty.newTerm(), Empty.newTerm());
        assertCond(c.currentClass != null);
        setLineInfoFrom(t);
        resultClass = t.exprType().receiverClass();
        processPassOneInner(c, null, t.actualExprType(), 0, id);
        if (md != null && md.exprType().objectSize() != Type.CLASSINTERFACE) {
            undefinedMethod(resultClass, md.methodSignature(), c);
        }
    }

    void processPass1(Context c) {
        if (resultClass == null) {
            assertCond(c.currentClass != null);
            if ((c.forceVmExc & ClassDefinition.NULL_PTR_EXC) != 0) {
                forceCheck = true;
            }
            ClassDefinition aclass;
            int vecSize;
            String id;
            BranchContext oldBranch = c.saveBranch();
            if (terms[1].notEmpty()) {
                terms[0].processPass1(c);
                resultClass = terms[0].exprType().receiverClass();
                aclass = null;
                vecSize = 0;
                id = terms[1].dottedName();
            } else {
                ObjVector vec = new ObjVector();
                terms[0].storeDottedName(vec);
                aclass = terms[0].defineClass(c, vec);
                vecSize = vec.size();
                id = (String) vec.elementAt(vecSize - 1);
            }
            if (terms[0].isSafeExpr()) {
                oldBranch = c.swapBranch(oldBranch);
                terms[2].processPass1(c);
                c.unionBranch(oldBranch);
            } else {
                terms[2].processPass1(c);
            }
            processPassOneInner(c, aclass, terms[0].actualExprType(), vecSize,
                    id);
        }
    }

    private void processPassOneInner(Context c, ClassDefinition aclass,
            ExpressionType actualType0, int vecSize, String id) {
        if (vecSize == 0) {
            MethodSignature msig = new MethodSignature(id,
                    terms[2].getSignature());
            if (resultClass.objectSize() < Type.CLASSINTERFACE) {
                fatalError(c, "Primitive type cannot be dereferenced: "
                        + resultClass.name());
                return;
            }
            md = resultClass.matchMethod(msig, c.forClass);
            if (md == null) {
                undefinedMethod(resultClass, msig, c);
                return;
            }
            if (terms[0].isSuper(false)) {
                if (md.isAbstract()) {
                    fatalError(c, "Abstract method is called: " + md.id());
                }
                isExact = true;
            }
            actualClass = resultClass;
            actualMethod = md;
            if (!md.isClassMethod() && actualType0.objectSize() != Type.NULLREF) {
                actualClass = actualType0.receiverClass();
                if (actualType0 != actualClass
                        && actualType0.objectSize() == Type.CLASSINTERFACE) {
                    isExact = true;
                }
                if (actualClass != resultClass && md.allowOverride()) {
                    actualClass.define(c.forClass);
                    actualMethod = actualClass.getSameMethod(md);
                    if (actualMethod == null) {
                        actualClass = resultClass;
                        actualMethod = md;
                    }
                }
            }
        } else if (vecSize > 1) {
            if (aclass == null) {
                fatalError(c,
                        "Undefined qualified name: " + terms[0].dottedName());
                resultString = VariableDefinition.UNKNOWN_NAME;
                resultClass = c.currentClass;
                return;
            }
            actualClass = actualType0.receiverClass();
            if (actualType0 != actualClass
                    && actualType0.objectSize() == Type.CLASSINTERFACE) {
                isExact = true;
            }
            MethodSignature msig = new MethodSignature(id,
                    terms[2].getSignature());
            resultClass = aclass;
            md = aclass.matchMethod(msig, c.forClass);
            if (md == null) {
                undefinedMethod(aclass, msig, c);
                return;
            }
            assertCond(actualClass != null);
            actualMethod = md;
            if (actualType0.objectSize() != Type.NULLREF) {
                actualClass.define(c.forClass);
                if (md.allowOverride()
                        && (actualMethod = actualClass.getSameMethod(md)) == null) {
                    actualClass = resultClass;
                    actualMethod = md;
                }
            } else {
                actualClass = aclass;
            }
        } else {
            MethodSignature msig = new MethodSignature(id,
                    terms[2].getSignature());
            resultClass = c.currentClass;
            resultString = This.CNAME;
            while ((md = resultClass.matchMethod(msig, c.forClass)) == null) {
                VariableDefinition outerV = resultClass.outerThisRef();
                if (outerV != null) {
                    outerV.markUsed();
                    resultString = outerV.stringOutput(resultString, 1, false);
                }
                resultClass = resultClass.outerClass();
                if (resultClass == null) {
                    undefinedMethod(c.currentClass, msig, c);
                    resultClass = c.currentClass;
                    return;
                }
            }
            if (!md.isClassMethod() && c.currentMethod != null
                    && c.currentMethod.isClassMethod()) {
                fatalError(c,
                        "Instance method used in a static context: " + md.id());
            }
            actualClass = resultClass;
            actualMethod = md;
        }
        boolean useMethodBranch = false;
        if (c.currentClass.superClass() != null
                || md.used()
                || !md.definingClass().name().equals(Names.JAVA_LANG_VMCLASS)
                || !md.isExactMatch(true, Names.JAVA_LANG_CLASS, 0,
                        Names.SIGN_ARRAYCLASSOF0X)) {
            if (isExact && !md.isClassMethod()) {
                actualMethod.markUsedThisOnly();
            } else {
                actualMethod.markUsed(actualClass,
                        c.containsAccessedClass(actualMethod.definingClass()));
            }
            if (!c.currentClass.name().equals(
                    Names.JAVAX_SWING_UIDEFAULTS_PROXYLAZYVALUE)) {
                processReflection(c.currentClass, c.forClass);
            }
            VariableDefinition v = md.isClassMethod() ? null : terms[0]
                    .getVariable(false);
            if (!actualMethod.isNative()
                    || !Names
                            .isVMCoreClass(actualMethod.definingClass().name())) {
                if (md.isClassMethod()) {
                    c.addAccessedClass(md.definingClass());
                } else if (!actualClass.isInterface()) {
                    c.addAccessedClass(actualClass);
                } else if (!resultClass.isInterface()) {
                    c.addAccessedClass(resultClass);
                }
                if (isExact || !actualMethod.allowOverride()) {
                    actualMethod.processBranch(c,
                            v == VariableDefinition.THIS_VAR);
                    useMethodBranch = true;
                }
            }
            if (v != null) {
                c.setVarNotNull(v);
            }
            noLeaksScope = c.localScope;
            isConditional = c.isConditional;
        }
        actualMethod.incCallsCount(c.currentMethod);
        actualMethod.setArgsFormalType(terms[2], useMethodBranch ? c : null);
    }

    ExpressionType exprType() {
        assertCond(resultClass != null);
        return md != null ? md.exprType() : Main.dict.classTable[Type.VOID];
    }

    ExpressionType actualExprType() {
        assertCond(resultClass != null);
        return actualMethod != null
                && (isExact || !actualMethod.allowOverride()) ? actualMethod
                .actualExprType() : exprType();
    }

    boolean isNotNull() {
        assertCond(resultClass != null);
        return actualMethod != null
                && (isExact || !actualMethod.allowOverride())
                && actualMethod.isNotNull();
    }

    boolean isSwitchMapAssign(boolean isMethodCall) {
        assertCond(resultClass != null);
        VariableDefinition v = terms[0].getVariable(false);
        return v != null
                && isMethodCall
                && actualMethod != null
                && actualMethod.definingClass().name()
                        .equals(Names.JAVA_LANG_ENUM)
                && !actualMethod.isClassMethod()
                && v.isClassVariable()
                && actualMethod.exprType().objectSize() == Type.INT
                && actualMethod.methodSignature().signatureString()
                        .equals(Names.SIGN_ORDINAL) && v.isFinalVariable();
    }

    MethodDefinition superMethodCall() {
        return terms[0].isSuper(true) ? actualMethod : null;
    }

    String strLiteralValueGuess() {
        assertCond(resultClass != null);
        if (md != null) {
            if (resultClass.name().equals(Names.JAVA_LANG_CLASS)) {
                if (!md.isExactMatch(false, Names.JAVA_LANG_STRING, 0,
                        Names.SIGN_GETNAME))
                    return null;
                ExpressionType exprType0 = terms[0].classLiteralValGuess();
                if (exprType0 == null)
                    return null;
                if (exprType0.signatureDimensions() > 0)
                    return exprType0.getJavaSignature();
                ClassDefinition cd = exprType0.receiverClass();
                return cd != exprType0 || cd.isFinal() ? cd.name() : null;
            }
            if (resultClass.name().equals(Names.JAVA_LANG_STRING)
                    && md.isClassMethod()
                    && actualMethod.id().equals(Names.VALUEOF))
                return decodeFirstArgAsString();
        }
        return null;
    }

    ExpressionType classLiteralValGuess() {
        assertCond(resultClass != null);
        return classLiteralValue != null ? (classLiteralValue.receiverClass()
                .isProxyClass() ? null : classLiteralValue)
                : actualMethod != null ? actualMethod.classLiteralValGuess()
                        : null;
    }

    MethodInvocation getClassNewInstanceCall() {
        assertCond(resultClass != null);
        MethodInvocation mcall = null;
        if (md != null
                && (mcall = actualMethod.getClassNewInstanceCall()) == null
                && (md.definingClass().name().equals(Names.JAVA_LANG_CLASS) ? md
                        .isExactMatch(false, Names.JAVA_LANG_OBJECT, 0,
                                Names.SIGN_NEWINSTANCE)
                        : actualMethod
                                .definingClass()
                                .name()
                                .equals(Names.GNU_CLASSPATH_SERVICEFACTORY_SERVICEITERATOR)
                                && md.isExactMatch(false,
                                        Names.JAVA_LANG_OBJECT, 0,
                                        Names.SIGN_NEXT))) {
            mcall = this;
        }
        return mcall;
    }

    MethodSignature getConstructorInstanceSign() {
        assertCond(resultClass != null);
        if (md == null)
            return null;
        MethodSignature msig = actualMethod.getConstructorInstanceSign();
        if (msig != null)
            return msig;
        Term t = terms[2].getArgumentTerm(0);
        if (t == null)
            return null;
        ObjVector parmSig;
        String className = md.definingClass().name();
        if (className.equals(Names.JAVA_LANG_CLASS)
                && (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_CONSTRUCTOR,
                        0, Names.SIGN_GETCONSTRUCTOR) || md.isExactMatch(false,
                        Names.JAVA_LANG_REFLECT_CONSTRUCTOR, 0,
                        Names.SIGN_GETDECLAREDCONSTRUCTOR))) {
            parmSig = new ObjVector();
            if (!t.storeClassLiteralsGuess(parmSig, false)
                    && t.actualExprType().objectSize() != Type.NULLREF)
                return null;
        } else {
            if (!className.equals(Names.JAVA_LANG_REFLECT_CONSTRUCTOR)
                    || !md.isExactMatch(false, Names.JAVA_LANG_OBJECT, 0,
                            Names.SIGN_NEWINSTANCE_CTOR))
                return null;
            if ((msig = terms[0].getConstructorInstanceSign()) != null)
                return msig;
            parmSig = new ObjVector();
            parmSig.addElement(Main.dict.classTable[Type.NULLREF]);
        }
        return new MethodSignature("<init>", parmSig);
    }

    void discoverObjLeaks() {
        assertCond(resultClass != null);
        terms[0].discoverObjLeaks();
        if (actualMethod != null && actualMethod.used()) {
            if (!isExact) {
                while (actualMethod.allowOverride()) {
                    ClassDefinition cd = actualClass.getRealOurClass();
                    MethodDefinition md2;
                    if (cd != actualClass
                            && (md2 = cd.getSameMethod(actualMethod)) != null) {
                        assertCond(md2.used());
                        actualClass = cd;
                        actualMethod = md2;
                        if (!actualMethod.allowOverride())
                            break;
                    }
                    if (!actualMethod.isAbstract())
                        break;
                    ClassDefinition[] cdArr = new ClassDefinition[1];
                    md2 = actualClass.getSingleRealMethodInSubclasses(
                            actualMethod, cdArr);
                    if (md2 == null)
                        break;
                    actualMethod = md2;
                    actualClass = cdArr[0];
                    assertCond(actualClass != null);
                }
            }
            String sigString = actualMethod.methodSignature().signatureString();
            if (!actualMethod.copyObjLeaksTo(terms[2]) && !isExact
                    && actualMethod.allowOverride()) {
                actualClass.copyObjLeaksInSubclasses(sigString, terms[2]);
            }
            if (actualMethod.hasThisObjLeak(false)
                    || (!isExact && actualMethod.allowOverride() && actualClass
                            .hasThisObjLeakInSubclasses(sigString, false))) {
                terms[0].setObjLeaks(null);
            } else if (actualMethod.isThisStackObjVolatile()
                    || (!isExact && actualMethod.allowOverride() && actualClass
                            .isThisStackObjVltInSubclasses(sigString))) {
                terms[0].setStackObjVolatile();
            }
            if (!actualMethod.allowStackObjRet()
                    || (!isExact && actualMethod.allowOverride() && actualClass
                            .subclassHasMethod(sigString, null))) {
                noStackObjRet = true;
            } else if (!actualMethod.stackObjRetRequired()) {
                Main.dict.stackObjRetCalls.addLast(this);
            }
        }
        terms[2].discoverObjLeaks();
    }

    void setStackObjVolatile() {
        assertCond(resultClass != null);
        if (actualMethod != null && actualMethod.used() && !needsLocalVolatile) {
            needsLocalVolatile = true;
            if (actualMethod.hasThisObjLeak(true)
                    || (!isExact && actualMethod.allowOverride() && actualClass
                            .hasThisObjLeakInSubclasses(actualMethod
                                    .methodSignature().signatureString(), true))) {
                terms[0].setStackObjVolatile();
            }
            terms[2].setStackObjVolatile();
        }
    }

    void setObjLeaks(VariableDefinition v) {
        assertCond(resultClass != null);
        if (actualMethod != null && actualMethod.used()) {
            if (v != VariableDefinition.WRITABLE_ARRAY_VAR
                    && (actualMethod.hasThisObjLeak(true) || (!isExact
                            && actualMethod.allowOverride() && actualClass
                            .hasThisObjLeakInSubclasses(actualMethod
                                    .methodSignature().signatureString(), true)))) {
                terms[0].setObjLeaks(v);
            }
            terms[2].setObjLeaks(v);
            noLeaksScope = VariableDefinition.addSetObjLeaksTerm(noLeaksScope,
                    v, this, isConditional || noStackObjRet);
            if (noLeaksScope == null
                    || v == VariableDefinition.WRITABLE_ARRAY_VAR) {
                actualMethod.setWritableArray();
                if (!isExact && actualMethod.allowOverride()) {
                    actualClass.setWritableArrayRetInSubclasses(actualMethod
                            .methodSignature().signatureString());
                }
            }
        }
    }

    MethodDefinition getNoLeaksScopeMethod() {
        return noLeaksScope != null && !noStackObjRet ? actualMethod : null;
    }

    MethodDefinition stackObjRetMethodCall() {
        assertCond(actualMethod != null
                && (noLeaksScope == null || noStackObjRet));
        return actualMethod;
    }

    int tokenCount() {
        return terms[0].tokenCount() + terms[2].tokenCount()
                + (terms[0].isSafeExpr() ? 1 : 2);
    }

    void allocRcvr(int[] curRcvrs) {
        if (actualMethod != null && actualMethod.used()) {
            Term t0 = terms[0];
            Term t2 = terms[2];
            int[] curRcvrs1 = OutputContext.copyRcvrs(curRcvrs);
            t0.allocRcvr(curRcvrs);
            VariableDefinition v;
            if (!actualMethod.isClassMethod()
                    && ((t0.isSafeExpr() ? !t0.isNotNull()
                            && ((v = t0.getVariable(true)) == null || !v
                                    .isLocalOrParam()) && !t0.isImmutable()
                            : actualMethod.isAbstract()
                                    || !t2.isSafeExpr()
                                    || !t0.isNotNull()
                                    || t2.isFieldAccessed(null)
                                    || (!t2.isImmutable() && t0
                                            .isAnyLocalVarChanged(null))
                                    || (!isExact
                                            && actualMethod.allowOverride() && actualClass
                                            .subclassHasMethod(actualMethod
                                                    .methodSignature()
                                                    .signatureString(), null))) || (!t2
                            .isSafeWithThrow() && !t0.isImmutable() && (t0
                            .isFieldAccessed(null) || t2
                            .isAnyLocalVarChanged(t0))))) {
                rcvr = ++curRcvrs1[Type.NULLREF];
            }
            if (!md.isClassMethod() && t2.notEmpty() && !t0.isNotNull()) {
                int[] curRcvrs2 = OutputContext.copyRcvrs(curRcvrs1);
                t2.markParamRcvr(-1, curRcvrs2);
                t2.allocParamRcvr(curRcvrs1,
                        OutputContext.copyRcvrs(curRcvrs1), curRcvrs2);
            } else {
                t2.allocRcvr(curRcvrs1);
            }
            OutputContext.joinRcvrs(curRcvrs, curRcvrs1);
        }
    }

    String writeStackObjDefn(OutputContext oc, boolean needsLocalVolatile) {
        assertCond(actualMethod != null);
        return actualMethod.writeStackObjDefn(oc, needsLocalVolatile);
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        if (actualMethod != null && actualMethod.used()) {
            terms[0].writeStackObjs(oc, scopeTerm);
            terms[2].writeStackObjs(oc, scopeTerm);
            if (noLeaksScope == scopeTerm && !noStackObjRet) {
                assertCond(scopeTerm != null);
                stackObjCode = actualMethod.writeStackObjDefn(oc,
                        needsLocalVolatile);
            }
        }
    }

    void writeStackObjTrigClinit(OutputContext oc) {
        assertCond(actualMethod != null);
        actualMethod.writeStackObjTrigClinit(oc);
    }

    ExpressionType writeStackObjRetCode(OutputContext oc) {
        assertCond(actualMethod != null
                && (noLeaksScope == null || noStackObjRet));
        stackObjCode = MethodDefinition.STACKOBJ_RETNAME;
        return actualMethod.writeStackObjRetCode(oc);
    }

    boolean isAtomary() {
        return true;
    }

    void setVoidExpression() {
        isVoidExpr = true;
    }

    void processOutput(OutputContext oc) {
        assertCond(resultClass != null);
        if (isVoidExpr
                && md != null
                && !md.isClassMethod()
                && md.exprType().objectSize() != Type.VOID
                && !terms[0].isSuper(false)
                && (!actualMethod.used() || (!isExact && !actualClass
                        .hasRealInstances()))) {
            oc.cPrint("(");
            oc.cPrint(Type.cName[Type.VOID]);
            oc.cPrint(")");
        }
        oc.cPrint("(");
        String rcvrStr = null;
        if (rcvr > 0) {
            rcvrStr = OutputContext.getRcvrName(rcvr, Type.CLASSINTERFACE);
            oc.cPrint(rcvrStr);
            oc.cPrint("= (");
            oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
            oc.cPrint(")");
            ExpressionType oldAssignmentRightType = oc.assignmentRightType;
            oc.assignmentRightType = null;
            terms[0].atomaryOutput(oc);
            oc.assignmentRightType = oldAssignmentRightType;
            oc.cPrint(", ");
        }
        boolean isSpec = false;
        boolean rightParenNeeded = false;
        String primaryStr = null;
        if (md == null || md.isClassMethod() || terms[0].isSuper(false)) {
            if (md != null && md.used()) {
                terms[2].produceRcvr(oc);
            }
            if (!terms[0].isSafeExpr()) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[Type.VOID]);
                oc.cPrint(")");
                if (rcvrStr != null) {
                    oc.cPrint(rcvrStr);
                } else {
                    terms[0].atomaryOutput(oc);
                }
                oc.cPrint(", ");
            }
            if (md != null) {
                if (md.used()) {
                    oc.cPrint(stackObjCode != null ? md
                            .stackObjRetRoutineCName() : md.routineCName());
                } else {
                    assertCond(md.definingClass().name()
                            .equals(Names.JAVA_LANG_VMCLASS)
                            && md.isExactMatch(true, Names.JAVA_LANG_CLASS, 0,
                                    Names.SIGN_ARRAYCLASSOF0X));
                }
            } else {
                oc.cPrint(MethodDefinition.UNKNOWN_NAME);
            }
            Main.dict.normalCalls++;
            isSpec = true;
        } else {
            if (actualMethod.used() && actualClass.hasRealInstances()) {
                terms[2].produceRcvr(oc);
            }
            rightParenNeeded = actualMethod
                    .writeMethodCall(
                            oc,
                            isExact ? null : actualClass,
                            (rcvrStr != null || actualClass != resultClass ? "("
                                    + actualClass.castName() + ")"
                                    : "")
                                    + (rcvrStr != null ? rcvrStr
                                            : resultString != null ? resultString
                                                    : actualClass == resultClass
                                                            || terms[0]
                                                                    .isAtomary() ? (primaryStr = terms[0]
                                                            .stringOutput())
                                                            : "("
                                                                    + (primaryStr = terms[0]
                                                                            .stringOutput())
                                                                    + ")"),
                            terms[0].isNotNull() ? 1 : forceCheck ? -1 : 0,
                            stackObjCode != null, md.exprType());
        }
        if (isSpec || (actualMethod.used() && actualClass.hasRealInstances())) {
            oc.cPrint("(");
            if (md != null && !md.isClassMethod()) {
                oc.cPrint("\010 ");
                ClassDefinition cd = actualMethod.definingClass();
                if (isSpec || rcvrStr != null || cd != resultClass) {
                    oc.cPrint("(");
                    oc.cPrint(cd.castName());
                    oc.cPrint(")");
                }
                if (resultString != null) {
                    oc.cPrint(resultString);
                } else if (rcvrStr != null) {
                    oc.cPrint(rcvrStr);
                } else if (primaryStr != null) {
                    if (cd == resultClass || terms[0].isAtomary()) {
                        oc.cPrint(primaryStr);
                    } else {
                        oc.cPrint("(");
                        oc.cPrint(primaryStr);
                        oc.cPrint(")");
                    }
                } else if (cd != resultClass) {
                    terms[0].atomaryOutput(oc);
                } else {
                    terms[0].processOutput(oc);
                }
                oc.parameterOutputAsArg(terms[2]);
            } else if (md == null || md.used()) {
                if (terms[2].notEmpty()) {
                    oc.cPrint("\010 ");
                }
                oc.cPrint(OutputContext
                        .paramStringOutputNoComma(terms[2], true));
            } else {
                oc.cPrint("(");
                oc.cPrint(Type.cName[Type.VOID]);
                oc.cPrint(")0");
                terms[2].getTermAt(0).parameterOutput(oc, true, Type.NULLREF);
            }
            if (stackObjCode != null) {
                assertCond(md != null);
                if (!md.isClassMethod() || terms[2].notEmpty()) {
                    oc.cPrint(", ");
                }
                oc.cPrint(stackObjCode);
            }
            oc.cPrint(")");
        }
        if (rightParenNeeded) {
            oc.cPrint(")");
        }
        oc.cPrint(")");
    }

    private void processReflection(ClassDefinition currentClass,
            ClassDefinition forClass) {
        if (!md.isPublic())
            return;
        String className = md.definingClass().name();
        if (className.equals(Names.JAVA_LANG_REFLECT_PROXY)
                && (md.isExactMatch(true, Names.JAVA_LANG_CLASS, 0,
                        Names.SIGN_GETPROXYCLASS) || md.isExactMatch(true,
                        Names.JAVA_LANG_OBJECT, 0, Names.SIGN_NEWPROXYINSTANCE))) {
            Term t = terms[2].getArgumentTerm(1);
            if (t != null) {
                ObjVector parmSig = new ObjVector();
                if (t.storeClassLiteralsGuess(parmSig, false)) {
                    classLiteralValue = Main.dict.addProxyClass(parmSig,
                            forClass);
                }
            }
            return;
        }
        if (md.isExactMatch(false, Names.JAVA_LANG_CLASS, 0,
                Names.SIGN_GETCLASS)) {
            ExpressionType actualType0 = terms[0].actualExprType();
            if (isExact || actualType0.receiverClass().superClass() != null) {
                classLiteralValue = actualType0;
            }
            if (actualType0.signatureClass().objectSize() == Type.CLASSINTERFACE
                    && (actualType0.objectSize() == Type.OBJECTARRAY || (!isExact && actualType0
                            .receiverClass().superClass() == null))) {
                Main.dict.get(Names.JAVA_LANG_VMCLASS).markUsed();
            }
            return;
        }
        if (!className.equals(Names.JAVA_LANG_CLASS))
            return;
        if (md.isExactMatch(false, Names.JAVA_LANG_STRING, 0,
                Names.SIGN_GETNAME)) {
            Main.dict
                    .addGetNameClass(terms[0].classLiteralValGuess(), forClass);
            return;
        }
        boolean isForName2 = md.isExactMatch(true, Names.JAVA_LANG_CLASS, 0,
                Names.SIGN_FORNAME_2);
        if (isForName2
                || md.isExactMatch(true, Names.JAVA_LANG_CLASS, 0,
                        Names.SIGN_FORNAME)) {
            ExpressionType exprType = decodeClassForNameArg(
                    decodeFirstArgAsString(), currentClass);
            if (exprType != null) {
                classLiteralValue = exprType;
                ClassDefinition cd = exprType.signatureClass();
                cd.predefineClass(forClass);
                cd.markUsed();
                Term t;
                ConstValue constVal1;
                if (exprType.signatureDimensions() == 0
                        && (!isForName2 || ((t = terms[2].getArgumentTerm(1)) != null && ((constVal1 = t
                                .evaluateConstValue()) == null || constVal1
                                .isNonZero())))) {
                    reflectedClass = exprType;
                }
            }
            return;
        }
        ExpressionType exprType = terms[0].classLiteralValGuess();
        ClassDefinition literalClass;
        boolean isExactType = false;
        if (exprType != null) {
            literalClass = null;
            if (exprType.objectSize() == Type.CLASSINTERFACE) {
                literalClass = exprType.receiverClass();
                literalClass.define(forClass);
            }
            if (literalClass != exprType) {
                isExactType = true;
            }
            if (literalClass != null) {
                if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_FIELD, 1,
                        Names.SIGN_GETDECLAREDFIELDS)) {
                    reflectedClass = literalClass.reflectAllFields(true);
                    return;
                }
                if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_FIELD, 1,
                        Names.SIGN_GETFIELDS)) {
                    reflectedClass = literalClass.reflectAllFields(false);
                    reflectInSuper = true;
                    return;
                }
                if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_CONSTRUCTOR,
                        1, Names.SIGN_GETDECLAREDCONSTRUCTORS)) {
                    reflectConstructors(literalClass, true, null, isExactType);
                    return;
                }
                if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_CONSTRUCTOR,
                        1, Names.SIGN_GETCONSTRUCTORS)) {
                    reflectConstructors(literalClass, false, null, isExactType);
                    return;
                }
                if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_CONSTRUCTOR,
                        0, Names.SIGN_GETDECLAREDCONSTRUCTOR)) {
                    reflectConstructors(literalClass, true,
                            decodeArgAsClassArray(0), isExactType);
                    return;
                }
                if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_CONSTRUCTOR,
                        0, Names.SIGN_GETCONSTRUCTOR)) {
                    reflectConstructors(literalClass, false,
                            decodeArgAsClassArray(0), isExactType);
                    return;
                }
                if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_METHOD, 1,
                        Names.SIGN_GETDECLAREDMETHODS)) {
                    reflectMethods(literalClass, true, null, null, isExactType);
                    return;
                }
                if (isExactType
                        && md.isExactMatch(false, Names.JAVA_LANG_OBJECT, 0,
                                Names.SIGN_NEWINSTANCE)) {
                    reflectConstructors(literalClass, true, new ObjVector(),
                            true);
                    return;
                }
            } else {
                literalClass = Main.dict.get(Names.JAVA_LANG_OBJECT);
            }
            if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_METHOD, 1,
                    Names.SIGN_GETMETHODS)) {
                reflectMethods(literalClass, false, null, null, isExactType);
                return;
            }
        } else {
            literalClass = Main.dict.get(Names.JAVA_LANG_OBJECT);
        }
        if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_FIELD, 0,
                Names.SIGN_GETDECLAREDFIELD)) {
            String name = decodeFirstArgAsString();
            reflectedClass = name != null ? literalClass.reflectField(name,
                    true, isExactType) : literalClass.reflectAllFields(true);
            return;
        }
        if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_FIELD, 0,
                Names.SIGN_GETFIELD)) {
            String name = decodeFirstArgAsString();
            if (name != null) {
                reflectedClass = literalClass.reflectField(name, false,
                        isExactType);
            } else {
                reflectedClass = literalClass.reflectAllFields(false);
                reflectInSuper = true;
            }
            return;
        }
        if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_METHOD, 0,
                Names.SIGN_GETDECLAREDMETHOD)) {
            reflectMethods(literalClass, true, decodeFirstArgAsString(),
                    decodeArgAsClassArray(1), isExactType);
            return;
        }
        if (md.isExactMatch(false, Names.JAVA_LANG_REFLECT_METHOD, 0,
                Names.SIGN_GETMETHOD)) {
            reflectMethods(literalClass, false, decodeFirstArgAsString(),
                    decodeArgAsClassArray(1), isExactType);
        }
    }

    private String decodeFirstArgAsString() {
        Term t = terms[2].getArgumentTerm(0);
        return t != null ? t.strLiteralValueGuess() : null;
    }

    private ObjVector decodeArgAsClassArray(int index) {
        Term t = terms[2].getArgumentTerm(index);
        if (t == null)
            return null;
        ObjVector parmSig = new ObjVector();
        return t.storeClassLiteralsGuess(parmSig, false)
                || t.actualExprType().objectSize() == Type.NULLREF ? parmSig
                : null;
    }

    void reflectConstructors(ClassDefinition literalClass) {
        reflectConstructors(literalClass, false, new ObjVector(), false);
    }

    private void reflectConstructors(ClassDefinition literalClass,
            boolean declaredOnly, ObjVector parmSig, boolean isExactType) {
        if (reflectedMethodId == null) {
            literalClass.reflectConstructors(
                    declaredOnly,
                    parmSig != null ? (new MethodSignature("<init>", parmSig))
                            .signatureString() : null, isExactType);
            reflectedClass = isExactType ? literalClass.asExactClassType()
                    : literalClass;
            reflectedMethodId = "<init>";
            reflectedParmSig = parmSig;
            reflectInSuper = !declaredOnly;
        }
    }

    private void reflectMethods(ClassDefinition literalClass,
            boolean declaredOnly, String id, ObjVector parmSig,
            boolean isExactType) {
        if (reflectedMethodId == null) {
            if (id != null && id.length() == 0) {
                id = null;
            }
            literalClass.reflectMethods(id, declaredOnly, parmSig, isExactType);
            reflectedClass = literalClass;
            reflectedMethodId = id != null ? id : "";
            reflectedParmSig = parmSig;
            reflectInSuper = !declaredOnly;
        }
    }

    static ExpressionType decodeClassForNameArg(String str,
            ClassDefinition curClass) {
        if (str == null)
            return null;
        int dims = 0;
        char ch;
        int len = str.length();
        do {
            if (dims >= len)
                return null;
            ch = str.charAt(dims);
            if (ch != '[')
                break;
            dims++;
        } while (true);
        if (dims > 0) {
            if (ch != 'L') {
                if (len - 1 == dims) {
                    for (int type = Type.BOOLEAN; type < Type.VOID; type++) {
                        if (Type.sig[type].charAt(0) == ch)
                            return Main.dict.classTable[type].asExprType(dims);
                    }
                }
                return null;
            }
            if (len - 1 <= dims || str.charAt(len - 1) != ';')
                return null;
            str = str.substring(dims + 1, len - 1);
        }
        if ((curClass != null && !Main.dict.alreadyKnown(str) && (str.indexOf(
                '.', 0) < 0 ? curClass.name().indexOf('.', 0) >= 0 : !str
                .startsWith(curClass.getPackageName() + ".")))
                || !Main.dict.existsOrInner(str))
            return null;
        ClassDefinition cd = Main.dict.get(str);
        return dims > 0 ? cd.asExprType(dims) : cd.asExactClassType();
    }

    private void traceReflected() {
        if (classLiteralValue != null) {
            Main.dict.addDynClassToTrace(classLiteralValue.signatureClass());
        }
        if (reflectedClass != null) {
            ClassDefinition cd = reflectedClass.signatureClass();
            MethodDefinition md2;
            if ((md2 = Main.dict.curHelperForMethod) != null
                    && md2.isClassMethod() && md2.id().equals("initIDs")) {
                cd.classTraceClassInit(true);
            } else if (reflectedMethodId != null) {
                if (reflectedMethodId.equals("<init>")) {
                    cd.traceReflectedConstructor(
                            !reflectInSuper,
                            reflectedParmSig != null ? (new MethodSignature(
                                    "<init>", reflectedParmSig))
                                    .signatureString() : null,
                            reflectedClass != cd);
                } else {
                    cd.traceReflectedMethod(
                            reflectedMethodId.length() > 0 ? reflectedMethodId
                                    : null, !reflectInSuper, reflectedParmSig);
                }
            } else {
                cd.classTraceClassInit(Main.dict.classInitWeakDepend);
                if (reflectInSuper) {
                    cd.classTraceForSupers();
                }
            }
        } else if (md != null
                && md.definingClass().name().equals(Names.JAVA_LANG_CLASS)
                && md.isExactMatch(false, Names.JAVA_LANG_OBJECT, 0,
                        Names.SIGN_NEWINSTANCE)) {
            String ourClassName = Main.dict.curTraceInfo.getDefiningClassName();
            if (!ourClassName
                    .equals(Names.GNU_CLASSPATH_SERVICEPROVIDERLOADINGACTION)
                    && !ourClassName.equals(Names.JAVA_UTIL_LOGGING_LOGMANAGER)) {
                Main.dict.curTraceInfo.setUsesDynClasses();
            }
        }
    }

    ExpressionType traceClassInit() {
        assertCond(resultClass != null);
        if (actualMethod == null)
            return null;
        ExpressionType curTraceType0 = terms[0].traceClassInit();
        ClassDefinition curClass = actualClass;
        MethodDefinition curMethod = actualMethod;
        if (curTraceType0 != null && !actualMethod.isClassMethod()) {
            if (curTraceType0.objectSize() == Type.NULLREF)
                return actualMethod.exprType().objectSize() >= Type.CLASSINTERFACE ? curTraceType0
                        : null;
            curClass = curTraceType0.receiverClass();
            if (curClass != actualClass) {
                if (actualClass.isAssignableFrom(curClass, 0, null)) {
                    if (actualMethod.allowOverride()
                            && ((curMethod = curClass
                                    .getSameMethod(actualMethod)) == null || !curMethod
                                    .used())) {
                        curClass = actualClass;
                        curMethod = actualMethod;
                    }
                } else {
                    curClass = actualClass;
                }
            }
        }
        terms[2].traceClassInit();
        ObjVector parmTraceSig = null;
        if (terms[2].notEmpty()) {
            parmTraceSig = new ObjVector();
            terms[2].getTraceSignature(parmTraceSig);
        }
        curTraceType0 = curMethod
                .methodTraceClassInit(
                        false,
                        isExact
                                || (curTraceType0 != null
                                        && curTraceType0 != curClass
                                        && curTraceType0.signatureClass() == curClass && curTraceType0
                                        .signatureDimensions() == 0) ? curClass
                                .asExactClassType() : curClass, parmTraceSig);
        if (actualMethod.exprType() == curTraceType0) {
            curTraceType0 = null;
        }
        traceReflected();
        return curTraceType0;
    }
}
