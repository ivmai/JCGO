/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/Term.java --
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
 * The abstract superclass of all grammatical terms.
 */

abstract class Term {

    private int lineNum;

    Term() {
        if (Scanner.err != null) {
            lineNum = Parser.token.line;
        }
    }

    Term(int lineNum) {
        this.lineNum = lineNum;
    }

    static void assertCond(boolean condition) {
        if (VerInfo.ASSERT_ENABLED && !condition)
            throw new AssertException();
    }

    final Term setLineInfoFrom(Term t) {
        lineNum = t.lineNum;
        return this;
    }

    final void fatalError(Context c, String msg) {
        Main.dict.fatal(c != null ? c.fileName : "", lineNum, msg);
    }

    final void undefinedMethod(ClassDefinition aclass, MethodSignature msig,
            Context c) {
        fatalError(c, "Undefined: " + aclass.name() + "." + msig.getInfo());
    }

    final void undefinedConstructor(ClassDefinition aclass, ObjVector parmSig,
            Context c) {
        fatalError(c, "Undefined constructor: " + aclass.name()
                + (new MethodSignature("", parmSig)).getInfo());
    }

    void processPass0(Context c) {
    }

    int getSym() {
        assertCond(false);
        return 0;
    }

    int getPrecedenceLevel() {
        return 0;
    }

    boolean notEmpty() {
        return true;
    }

    boolean isBlock() {
        return false;
    }

    String dottedName() {
        return null;
    }

    void storeDottedName(ObjVector v) {
    }

    boolean isName() {
        return false;
    }

    boolean isJavaConstant(ClassDefinition ourClass) {
        return false;
    }

    Term joinParamLists(Term paramList) {
        assertCond(false);
        return null;
    }

    Term getTermAt(int index) {
        assertCond(false);
        return null;
    }

    Term getArgumentTerm(int index) {
        return null;
    }

    Term copyParamList(int[] skip) {
        assertCond(false);
        return null;
    }

    Term makeArgumentList() {
        assertCond(false);
        return null;
    }

    StringLiteral asStrLiteral() {
        return null;
    }

    boolean appendLiteralTo(Term t) {
        return false;
    }

    int countConcatStrs() {
        return 1;
    }

    boolean isType() {
        return false;
    }

    void assignedVarNames(ObjQueue names, boolean recursive) {
    }

    boolean handleAssertionsDisabled(ClassDefinition ourClass) {
        return false;
    }

    void processPass1(Context c) {
    }

    BranchContext staticInitializerPass(BranchContext prevBranch,
            boolean isStatic) {
        return prevBranch;
    }

    void updateCondBranch(Context c, boolean forTrue) {
    }

    ClassDefinition defineClass(Context c, ObjVector vec) {
        assertCond(false);
        return null;
    }

    boolean hasTailReturnOrThrow() {
        return false;
    }

    boolean isReturnAtEnd(boolean allowBreakThrow) {
        return false;
    }

    boolean allowInline(int tokenLimit) {
        return false;
    }

    int tokenCount() {
        return 1;
    }

    int tokensExpandedCount() {
        return 0;
    }

    void addFieldsTo(ClassDefinition cd) {
    }

    void setContinueLabel(String label) {
        Main.dict.fatal("?", lineNum, "Illegal continue label: " + label);
    }

    void setFormalType(Term formalParam, MethodDefinition md, Context c) {
    }

    boolean isLiteral() {
        return false;
    }

    boolean isImmutable() {
        return isLiteral();
    }

    boolean isSafeExpr() {
        return isImmutable();
    }

    boolean isSafeWithThrow() {
        return isSafeExpr();
    }

    boolean isFieldAccessed(VariableDefinition v) {
        return false;
    }

    boolean isAnyLocalVarChanged(Term t) {
        return false;
    }

    boolean isNotNull() {
        return false;
    }

    ConstValue evaluateConstValue() {
        return null;
    }

    boolean isFPZero() {
        return false;
    }

    ExpressionType exprType() {
        throw new AssertException("Illegal exprType() on line " + lineNum);
    }

    ExpressionType actualExprType() {
        return exprType();
    }

    final ObjVector getSignature() {
        ObjVector parmSig = new ObjVector();
        storeSignature(parmSig);
        return parmSig;
    }

    void storeSignature(ObjVector parmSig) {
    }

    boolean isSuper(boolean onlyEmpty) {
        return false;
    }

    boolean isBoolAssign() {
        return false;
    }

    boolean isSwitchMapAssign(boolean isMethodCall) {
        return false;
    }

    VariableDefinition getVariable(boolean allowInstance) {
        return null;
    }

    String strLiteralValueGuess() {
        return null;
    }

    ExpressionType classLiteralValGuess() {
        return null;
    }

    MethodInvocation getClassNewInstanceCall() {
        return null;
    }

    MethodSignature getConstructorInstanceSign() {
        return null;
    }

    MethodDefinition superMethodCall() {
        return null;
    }

    boolean storeClassLiteralsGuess(ObjVector parmSig, boolean isActual) {
        return false;
    }

    void requireLiteral() {
    }

    void insideArithOp() {
    }

    void allocRcvr(int[] curRcvrs) {
    }

    int markParamRcvr(int isNotSafe, int[] curRcvrs2) {
        assertCond(false);
        return isNotSafe;
    }

    void allocParamRcvr(int[] curRcvrs, int[] curRcvrs1, int[] curRcvrs2) {
        assertCond(false);
    }

    void produceRcvr(OutputContext oc) {
    }

    boolean copyObjLeaksFrom(Term formalParam) {
        return true;
    }

    void discoverObjLeaks() {
    }

    void setStackObjVolatile() {
    }

    void setObjLeaks(VariableDefinition v) {
    }

    MethodDefinition stackObjRetMethodCall() {
        return null;
    }

    String writeStackObjDefn(OutputContext oc, boolean needsLocalVolatile) {
        assertCond(false);
        return null;
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
    }

    void writeStackStrBufObj(OutputContext oc, Term scopeTerm) {
        assertCond(false);
    }

    void writeStackObjTrigClinit(OutputContext oc) {
    }

    ExpressionType writeStackObjRetCode(OutputContext oc) {
        assertCond(false);
        return null;
    }

    String stringOutput() {
        OutputContext oc = new OutputContext();
        processOutput(oc);
        return oc.instanceToString();
    }

    void processOutput(OutputContext oc) {
    }

    void parameterOutput(OutputContext oc, boolean asArg, int type) {
    }

    void setVoidExpression() {
    }

    boolean isAtomary() {
        return false;
    }

    final void atomaryOutput(OutputContext oc) {
        boolean atomary = isAtomary();
        if (!atomary) {
            oc.cPrint("(");
        }
        processOutput(oc);
        if (!atomary) {
            oc.cPrint(")");
        }
    }

    void getTraceSignature(ObjVector parmTraceSig) {
        assertCond(false);
    }

    void setTraceExprType(Enumeration en) {
        assertCond(false);
    }

    ExpressionType traceClassInit() {
        return null;
    }
}
