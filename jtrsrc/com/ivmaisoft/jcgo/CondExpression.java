/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/CondExpression.java --
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
 * Grammar production for conditional expressions.
 ** 
 * Format: ConditionalOrExpression QUESTION Expression COLON
 * ConditionalExpression
 */

final class CondExpression extends LexNode {

    private ExpressionType resType;

    private boolean castLeft;

    private boolean castRight;

    private ClassDefinition forClass;

    private ConstValue constVal0;

    CondExpression(Term a, Term c, Term e) {
        super(a, c, e);
    }

    boolean isJavaConstant(ClassDefinition ourClass) {
        return terms[0].isJavaConstant(ourClass)
                && terms[1].isJavaConstant(ourClass)
                && terms[2].isJavaConstant(ourClass);
    }

    void processPass1(Context c) {
        if (resType == null) {
            terms[0].processPass1(c);
            if (terms[0].exprType().objectSize() != Type.BOOLEAN) {
                fatalError(c, "Condition expression must be of boolean type");
            }
            constVal0 = terms[0].evaluateConstValue();
            ExpressionType exprType1;
            ExpressionType exprType2;
            if (constVal0 != null) {
                if (constVal0.isNonZero()) {
                    terms[1].processPass1(c);
                    exprType1 = terms[1].exprType();
                    exprType2 = exprType1;
                } else {
                    terms[2].processPass1(c);
                    exprType2 = terms[2].exprType();
                    exprType1 = exprType2;
                }
            } else {
                BranchContext oldBranch = c.saveBranch();
                terms[0].updateCondBranch(c, true);
                boolean oldIsConditional = c.isConditional;
                c.isConditional = true;
                terms[1].processPass1(c);
                oldBranch = c.swapBranch(oldBranch);
                exprType1 = terms[1].exprType();
                terms[0].updateCondBranch(c, false);
                terms[2].processPass1(c);
                c.isConditional = oldIsConditional;
                c.intersectBranch(oldBranch);
                exprType2 = terms[2].exprType();
            }
            int s1 = exprType1.objectSize();
            int s2 = exprType2.objectSize();
            if (s1 != Type.BOOLEAN ? (s1 < Type.BYTE || s1 > Type.VOID ? s2 >= Type.BOOLEAN
                    && s2 <= Type.VOID
                    : s1 == Type.VOID || s2 <= Type.BOOLEAN || s2 >= Type.VOID)
                    : s2 != Type.BOOLEAN) {
                fatalError(c, "Incompatible types of expressions");
            }
            forClass = c.forClass;
            if (s1 == Type.BOOLEAN) {
                resType = exprType1;
                return;
            }
            if (s1 >= Type.BYTE && s1 <= Type.DOUBLE) {
                if (s1 == s2) {
                    resType = exprType1;
                    if (s1 < Type.INT) {
                        castLeft = true;
                        castRight = true;
                    }
                    return;
                }
                if (s1 > s2) {
                    if (s1 > Type.INT) {
                        resType = exprType1;
                        castRight = true;
                        return;
                    }
                    if (s1 == Type.SHORT && s2 == Type.BYTE) {
                        resType = exprType1;
                        castLeft = true;
                        castRight = true;
                        return;
                    }
                    ConstValue constVal1;
                    if (s1 == Type.INT
                            && (constVal1 = terms[1].evaluateConstValue()) != null
                            && (constVal1 = constVal1.isEqual(constVal1
                                    .castTo(s2))) != null
                            && constVal1.isNonZero()) {
                        resType = exprType2;
                        castLeft = true;
                        castRight = true;
                        return;
                    }
                    if (s1 == Type.CHAR) {
                        castLeft = true;
                    } else {
                        castRight = true;
                    }
                } else {
                    if (s2 > Type.INT) {
                        resType = exprType2;
                        castLeft = true;
                        return;
                    }
                    if (s2 == Type.SHORT && s1 == Type.BYTE) {
                        resType = exprType2;
                        castLeft = true;
                        castRight = true;
                        return;
                    }
                    ConstValue constVal2;
                    if (s2 == Type.INT
                            && (constVal2 = terms[2].evaluateConstValue()) != null
                            && (constVal2 = constVal2.isEqual(constVal2
                                    .castTo(s1))) != null
                            && constVal2.isNonZero()) {
                        resType = exprType1;
                        castLeft = true;
                        castRight = true;
                        return;
                    }
                    if (s2 == Type.CHAR) {
                        castRight = true;
                    } else {
                        castLeft = true;
                    }
                }
                resType = Main.dict.classTable[Type.INT];
                return;
            }
            if (exprType1 == exprType2 || s2 == Type.NULLREF) {
                resType = exprType1;
                return;
            }
            if (s1 == Type.NULLREF) {
                resType = exprType2;
                return;
            }
            if (ClassDefinition.isAssignableFrom(exprType2, exprType1,
                    c.forClass)) {
                resType = exprType2;
                castLeft = true;
            } else {
                resType = exprType1;
                castRight = true;
            }
        }
    }

    void updateCondBranch(Context c, boolean forTrue) {
        if (constVal0 != null) {
            terms[constVal0.isNonZero() ? 1 : 2].updateCondBranch(c, forTrue);
        } else {
            BranchContext oldBranch = c.saveBranch();
            terms[1].updateCondBranch(c, forTrue);
            oldBranch = c.swapBranch(oldBranch);
            terms[2].updateCondBranch(c, forTrue);
            c.intersectBranch(oldBranch);
        }
    }

    void requireLiteral() {
        if (constVal0 != null) {
            terms[constVal0.isNonZero() ? 1 : 2].requireLiteral();
        } else {
            terms[0].requireLiteral();
            terms[1].requireLiteral();
            terms[2].requireLiteral();
        }
    }

    int tokenCount() {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .tokenCount() : terms[0].tokenCount() + terms[1].tokenCount()
                + terms[2].tokenCount() + 1;
    }

    int tokensExpandedCount() {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .tokensExpandedCount() : super.tokensExpandedCount();
    }

    ConstValue evaluateConstValue() {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .evaluateConstValue() : null;
    }

    boolean isFPZero() {
        assertCond(resType != null);
        return constVal0 != null
                && terms[constVal0.isNonZero() ? 1 : 2].isFPZero();
    }

    ExpressionType exprType() {
        assertCond(resType != null);
        return resType;
    }

    ExpressionType actualExprType() {
        assertCond(resType != null);
        int s0 = resType.objectSize();
        if (s0 == Type.CLASSINTERFACE || s0 == Type.OBJECTARRAY) {
            if (constVal0 != null)
                return terms[constVal0.isNonZero() ? 1 : 2].actualExprType();
            ExpressionType actualType = ClassDefinition.maxCommonExprOf(
                    terms[1].actualExprType(), terms[2].actualExprType(),
                    forClass);
            if (ClassDefinition.isAssignableFrom(resType, actualType, forClass))
                return actualType;
        }
        return resType;
    }

    boolean isLiteral() {
        assertCond(resType != null);
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .isLiteral() : terms[0].isLiteral() && terms[1].isLiteral()
                && terms[2].isLiteral();
    }

    boolean isImmutable() {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .isImmutable() : terms[0].isImmutable()
                && terms[1].isImmutable() && terms[2].isImmutable();
    }

    boolean isSafeExpr() {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .isSafeExpr() : terms[0].isSafeExpr() && terms[1].isSafeExpr()
                && terms[2].isSafeExpr();
    }

    boolean isSafeWithThrow() {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .isSafeWithThrow() : terms[0].isSafeWithThrow()
                && terms[1].isSafeWithThrow() && terms[2].isSafeWithThrow();
    }

    boolean isFieldAccessed(VariableDefinition v) {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .isFieldAccessed(v) : terms[0].isFieldAccessed(v)
                || terms[1].isFieldAccessed(v) || terms[2].isFieldAccessed(v);
    }

    boolean isAnyLocalVarChanged(Term t) {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .isAnyLocalVarChanged(t) : terms[0].isAnyLocalVarChanged(t)
                || terms[1].isAnyLocalVarChanged(t)
                || terms[2].isAnyLocalVarChanged(t);
    }

    boolean isNotNull() {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .isNotNull() : terms[1].isNotNull() && terms[2].isNotNull();
    }

    VariableDefinition getVariable(boolean allowInstance) {
        if (constVal0 != null)
            return terms[constVal0.isNonZero() ? 1 : 2]
                    .getVariable(allowInstance);
        VariableDefinition v = terms[1].getVariable(allowInstance);
        return v != null && terms[2].getVariable(allowInstance) == v ? v : null;
    }

    String strLiteralValueGuess() {
        if (constVal0 != null)
            return terms[constVal0.isNonZero() ? 1 : 2].strLiteralValueGuess();
        String str = terms[1].strLiteralValueGuess();
        return str != null ? str : terms[2].strLiteralValueGuess();
    }

    ExpressionType classLiteralValGuess() {
        if (constVal0 != null)
            return terms[constVal0.isNonZero() ? 1 : 2].classLiteralValGuess();
        ExpressionType exprType1 = terms[1].classLiteralValGuess();
        ExpressionType exprType2 = terms[2].classLiteralValGuess();
        if (exprType1 == null)
            return exprType2;
        if (exprType2 == null)
            return exprType1;
        ExpressionType exprType = ClassDefinition.maxCommonExprOf(exprType1,
                exprType2, forClass);
        return exprType.objectSize() != Type.CLASSINTERFACE
                || exprType.signatureClass().superClass() != null ? exprType
                : exprType1;
    }

    MethodInvocation getClassNewInstanceCall() {
        if (constVal0 != null)
            return terms[constVal0.isNonZero() ? 1 : 2]
                    .getClassNewInstanceCall();
        MethodInvocation mcall = terms[1].getClassNewInstanceCall();
        return mcall != null ? mcall : terms[2].getClassNewInstanceCall();
    }

    MethodSignature getConstructorInstanceSign() {
        if (constVal0 != null)
            return terms[constVal0.isNonZero() ? 1 : 2]
                    .getConstructorInstanceSign();
        MethodSignature msig = terms[1].getConstructorInstanceSign();
        return msig != null ? msig : terms[2].getConstructorInstanceSign();
    }

    void allocRcvr(int[] curRcvrs) {
        if (constVal0 != null) {
            terms[constVal0.isNonZero() ? 1 : 2].allocRcvr(curRcvrs);
        } else {
            int[] curRcvrs1 = OutputContext.copyRcvrs(curRcvrs);
            terms[0].allocRcvr(curRcvrs);
            int[] curRcvrs2 = OutputContext.copyRcvrs(curRcvrs1);
            terms[1].allocRcvr(curRcvrs1);
            OutputContext.joinRcvrs(curRcvrs, curRcvrs1);
            terms[2].allocRcvr(curRcvrs2);
            OutputContext.joinRcvrs(curRcvrs, curRcvrs2);
        }
    }

    void discoverObjLeaks() {
        if (constVal0 != null) {
            terms[constVal0.isNonZero() ? 1 : 2].discoverObjLeaks();
        } else {
            terms[0].discoverObjLeaks();
            terms[1].discoverObjLeaks();
            terms[2].discoverObjLeaks();
        }
    }

    void setStackObjVolatile() {
        if (constVal0 != null) {
            terms[constVal0.isNonZero() ? 1 : 2].setStackObjVolatile();
        } else {
            terms[1].setStackObjVolatile();
            terms[2].setStackObjVolatile();
        }
    }

    void setObjLeaks(VariableDefinition v) {
        if (constVal0 != null) {
            terms[constVal0.isNonZero() ? 1 : 2].setObjLeaks(v);
        } else {
            terms[1].setObjLeaks(v);
            terms[2].setObjLeaks(v);
        }
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        if (constVal0 != null) {
            terms[constVal0.isNonZero() ? 1 : 2].writeStackObjs(oc, scopeTerm);
        } else {
            terms[0].writeStackObjs(oc, scopeTerm);
            terms[1].writeStackObjs(oc, scopeTerm);
            terms[2].writeStackObjs(oc, scopeTerm);
        }
    }

    void processOutput(OutputContext oc) {
        assertCond(resType != null);
        if (constVal0 != null) {
            if (constVal0.isNonZero()) {
                if (castLeft) {
                    oc.cPrint("(");
                    oc.cPrint(resType.castName());
                    oc.cPrint(")");
                    terms[1].atomaryOutput(oc);
                } else {
                    terms[1].processOutput(oc);
                }
            } else if (castRight) {
                oc.cPrint("(");
                oc.cPrint(resType.castName());
                oc.cPrint(")");
                terms[2].atomaryOutput(oc);
            } else {
                terms[2].processOutput(oc);
            }
        } else {
            if (castLeft && castRight) {
                oc.cPrint("(");
                oc.cPrint(resType.castName());
                oc.cPrint(")(");
            }
            ExpressionType oldAssignmentRightType = oc.assignmentRightType;
            oc.assignmentRightType = null;
            terms[0].processOutput(oc);
            oc.assignmentRightType = oldAssignmentRightType;
            oc.cPrint("? ");
            if (castLeft && !castRight) {
                oc.cPrint("(");
                oc.cPrint(resType.castName());
                oc.cPrint(")");
                terms[1].atomaryOutput(oc);
            } else {
                terms[1].processOutput(oc);
            }
            oc.cPrint("\003 :\003 ");
            if (!castLeft && castRight) {
                oc.cPrint("(");
                oc.cPrint(resType.castName());
                oc.cPrint(")");
                terms[2].atomaryOutput(oc);
            } else {
                terms[2].processOutput(oc);
            }
            if (castLeft && castRight) {
                oc.cPrint(")");
            }
        }
    }

    ExpressionType traceClassInit() {
        if (constVal0 != null)
            return terms[constVal0.isNonZero() ? 1 : 2].traceClassInit();
        terms[0].traceClassInit();
        ExpressionType curTraceType1 = terms[1].traceClassInit();
        ExpressionType curTraceType2 = terms[2].traceClassInit();
        if (curTraceType1 != null && curTraceType2 != null) {
            ExpressionType curType = ClassDefinition.maxCommonExprOf(
                    curTraceType1, curTraceType2, null);
            if (ClassDefinition.isAssignableFrom(resType, curType, null))
                return curType;
        }
        return null;
    }
}
