/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ParenExpression.java --
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
 * Grammar production for a parenthesised expression.
 ** 
 * Format: LPAREN Expression RPAREN
 */

class ParenExpression extends LexNode {

    ParenExpression(Term b) {
        super(b);
    }

    final StringLiteral asStrLiteral() {
        return terms[0].asStrLiteral();
    }

    final boolean appendLiteralTo(Term t) {
        return terms[0].appendLiteralTo(t);
    }

    final int countConcatStrs() {
        return terms[0].countConcatStrs();
    }

    final String dottedName() {
        return terms[0].dottedName();
    }

    final void assignedVarNames(ObjQueue names, boolean recursive) {
        terms[0].assignedVarNames(names, recursive);
    }

    final boolean handleAssertionsDisabled(ClassDefinition ourClass) {
        return terms[0].handleAssertionsDisabled(ourClass);
    }

    final boolean isJavaConstant(ClassDefinition ourClass) {
        return terms[0].isJavaConstant(ourClass);
    }

    final void updateCondBranch(Context c, boolean forTrue) {
        terms[0].updateCondBranch(c, forTrue);
    }

    final ExpressionType exprType() {
        return terms[0].exprType();
    }

    final ExpressionType actualExprType() {
        return terms[0].actualExprType();
    }

    final boolean isLiteral() {
        return terms[0].isLiteral();
    }

    final boolean isImmutable() {
        return terms[0].isImmutable();
    }

    final boolean isSafeExpr() {
        return terms[0].isSafeExpr();
    }

    final boolean isSafeWithThrow() {
        return terms[0].isSafeWithThrow();
    }

    final boolean isNotNull() {
        return terms[0].isNotNull();
    }

    final ConstValue evaluateConstValue() {
        return terms[0].evaluateConstValue();
    }

    final boolean isFPZero() {
        return terms[0].isFPZero();
    }

    final boolean isBoolAssign() {
        return terms[0].isBoolAssign();
    }

    final boolean isSwitchMapAssign(boolean isMethodCall) {
        return terms[0].isSwitchMapAssign(isMethodCall);
    }

    final VariableDefinition getVariable(boolean allowInstance) {
        return terms[0].getVariable(allowInstance);
    }

    final MethodInvocation getClassNewInstanceCall() {
        return terms[0].getClassNewInstanceCall();
    }

    final MethodSignature getConstructorInstanceSign() {
        return terms[0].getConstructorInstanceSign();
    }

    final MethodDefinition superMethodCall() {
        return terms[0].superMethodCall();
    }

    final String strLiteralValueGuess() {
        return terms[0].strLiteralValueGuess();
    }

    final ExpressionType classLiteralValGuess() {
        return terms[0].classLiteralValGuess();
    }

    final boolean storeClassLiteralsGuess(ObjVector parmSig, boolean isActual) {
        return terms[0].storeClassLiteralsGuess(parmSig, isActual);
    }

    final void insideArithOp() {
        terms[0].insideArithOp();
    }

    final void setStackObjVolatile() {
        terms[0].setStackObjVolatile();
    }

    final void setObjLeaks(VariableDefinition v) {
        terms[0].setObjLeaks(v);
    }

    final void writeStackStrBufObj(OutputContext oc, Term scopeTerm) {
        terms[0].writeStackStrBufObj(oc, scopeTerm);
    }

    final void setVoidExpression() {
        terms[0].setVoidExpression();
    }

    boolean isAtomary() {
        return true;
    }

    final int tokenCount() {
        return terms[0].tokenCount();
    }

    final void parameterOutput(OutputContext oc, boolean asArg, int type) {
        terms[0].parameterOutput(oc, asArg, type);
    }

    void processOutput(OutputContext oc) {
        terms[0].atomaryOutput(oc);
    }

    final ExpressionType traceClassInit() {
        return terms[0].traceClassInit();
    }
}
