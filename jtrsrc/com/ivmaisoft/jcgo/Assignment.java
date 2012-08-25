/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/Assignment.java --
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
 * Grammar production for assignments (simple and compound).
 **
 * Formats: LeftHandSide EQUALS AssignmentExpression LeftHandSide FLSHIFT_EQUALS
 * AssignmentExpression LeftHandSide SHRIGHT_EQUALS AssignmentExpression
 * LeftHandSide SHLEFT_EQUALS AssignmentExpression LeftHandSide XOR_EQUALS
 * AssignmentExpression LeftHandSide BITOR_EQUALS AssignmentExpression
 * LeftHandSide BITAND_EQUALS AssignmentExpression LeftHandSide MOD_EQUALS
 * AssignmentExpression LeftHandSide DIVIDE_EQUALS AssignmentExpression
 * LeftHandSide TIMES_EQUALS AssignmentExpression LeftHandSide MINUS_EQUALS
 * AssignmentExpression LeftHandSide PLUS_EQUALS AssignmentExpression
 */

final class Assignment extends LexNode {

    private ExpressionType exprType0;

    private ExpressionType exprType2;

    private ExpressionType actualType2;

    private boolean isString;

    private int forceVmExc;

    private int rcvr1;

    private int rcvr2;

    private int rcvr3;

    private boolean isVoidExpr;

    Assignment(Term a, Term b, Term c) {
        super(a, b, c);
    }

    void assignedVarNames(ObjQueue names, boolean recursive) {
        if (recursive) {
            terms[0].assignedVarNames(names, true);
            terms[2].assignedVarNames(names, true);
        }
        if (terms[1].getSym() == LexTerm.EQUALS) {
            String id = terms[0].dottedName();
            if (id != null) {
                names.addLast(id);
            }
        }
    }

    void processPass1(Context c) {
        if (exprType0 == null) {
            int sym = terms[1].getSym();
            c.lvalue = true;
            if (sym == LexTerm.EQUALS) {
                c.setOnly = true;
            }
            forceVmExc = c.forceVmExc;
            BranchContext oldBranch = c.saveBranch();
            terms[0].processPass1(c);
            oldBranch = c.swapBranch(oldBranch);
            c.lvalue = false;
            c.setOnly = false;
            terms[2].processPass1(c);
            c.unionBranch(oldBranch);
            exprType0 = terms[0].exprType();
            exprType2 = terms[2].exprType();
            int s0 = exprType0.objectSize();
            int s2 = exprType2.objectSize();
            if (s2 == Type.VOID) {
                fatalError(c, "Incompatible types in assignment expression");
            }
            VariableDefinition v = terms[0].getVariable(false);
            if (sym == LexTerm.PLUS_EQUALS
                    && exprType0.receiverClass().isStringOrNull()) {
                isString = true;
                if (!exprType2.receiverClass().isStringOrNull()) {
                    exprType2 = (terms[2] = BinaryOp.wrapInValueOf(c, terms[2]))
                            .exprType();
                }
                actualType2 = exprType2;
                Main.dict.get(Names.JAVA_LANG_VMSYSTEM).markMethod(
                        Names.SIGN_CONCAT0X);
                if (v != null) {
                    c.setVarNotNull(v);
                    v.setStrLiteralValue(strLiteralValueGuess());
                }
            } else {
                actualType2 = exprType2;
                if (v != null && sym == LexTerm.EQUALS && v.isLocalOrParam()) {
                    v.setUnassigned(false);
                }
                if (s0 == Type.BOOLEAN) {
                    if (s2 != Type.BOOLEAN) {
                        fatalError(c,
                                "Incompatible types in assignment expression");
                    }
                    if (sym != LexTerm.EQUALS && sym != LexTerm.XOR_EQUALS
                            && sym != LexTerm.BITOR_EQUALS
                            && sym != LexTerm.BITAND_EQUALS) {
                        fatalError(c, "Illegal assignment operator");
                    }
                } else if (s0 < Type.CLASSINTERFACE) {
                    if ((s0 >= Type.FLOAT || s2 >= Type.FLOAT)
                            && sym != LexTerm.EQUALS
                            && sym != LexTerm.PLUS_EQUALS
                            && sym != LexTerm.MINUS_EQUALS
                            && sym != LexTerm.TIMES_EQUALS
                            && sym != LexTerm.DIVIDE_EQUALS
                            && sym != LexTerm.MOD_EQUALS) {
                        fatalError(c, "Illegal assignment operator");
                    }
                    if (s2 <= Type.BOOLEAN
                            || s2 >= Type.CLASSINTERFACE
                            || (sym == LexTerm.EQUALS && s2 >= Type.LONG && s0 < s2)) {
                        fatalError(c,
                                "Incompatible types in assignment expression");
                    }
                } else {
                    if (sym != LexTerm.EQUALS) {
                        fatalError(c, "Illegal assignment operator");
                    }
                    if (s2 >= Type.BOOLEAN && s2 <= Type.VOID) {
                        fatalError(c,
                                "Incompatible types in assignment expression");
                    }
                    actualType2 = terms[2].actualExprType();
                    if (v != null) {
                        if (terms[2].isNotNull()) {
                            c.setVarNotNull(v);
                        } else {
                            c.clearVarNotNull(v);
                        }
                        c.setActualType(v, actualType2);
                        v.setClassNewInstanceCall(terms[2]
                                .getClassNewInstanceCall());
                        v.setConstructorInstanceSign(terms[2]
                                .getConstructorInstanceSign());
                        v.setClassLiteralValGuess(terms[2]
                                .classLiteralValGuess());
                        v.setStrLiteralValue(terms[2].strLiteralValueGuess());
                        ObjVector parmSig = new ObjVector();
                        if (terms[2].storeClassLiteralsGuess(parmSig, false)) {
                            v.setStoredClassLiteralsGuess(parmSig, false);
                        }
                        if (parmSig.size() != 0) {
                            parmSig = new ObjVector();
                        }
                        if (terms[2].storeClassLiteralsGuess(parmSig, true)) {
                            v.setStoredClassLiteralsGuess(parmSig, true);
                        }
                    }
                }
            }
        }
    }

    int tokenCount() {
        return terms[0].tokenCount() + terms[2].tokenCount()
                + (terms[0].isSafeExpr() || terms[2].isSafeExpr() ? 1 : 2);
    }

    void allocRcvr(int[] curRcvrs) {
        assertCond(exprType0 != null);
        int sym = terms[1].getSym();
        Term t0 = terms[0];
        Term t1 = t0.getArgumentTerm(1);
        Term t2 = terms[2];
        int[] curRcvrs2;
        if (t1 != null) {
            t0 = t0.getArgumentTerm(0);
            int rmask = 0;
            if (t0.isSafeExpr() ? (t2.isSafeExpr() ? !t1.isSafeWithThrow()
                    && (t0.isFieldAccessed(null) || t2.isFieldAccessed(null)
                            || t1.isAnyLocalVarChanged(t0) || t1
                            .isAnyLocalVarChanged(t2))
                    && (!t0.isImmutable() || !t2.isImmutable()) : !t0
                    .isImmutable()
                    || !t1.isImmutable()
                    || (sym != LexTerm.EQUALS && !t2.isSafeWithThrow())) : !t2
                    .isSafeExpr()
                    || !t1.isSafeExpr()
                    || (!t0.isSafeWithThrow()
                            && (t1.isFieldAccessed(null)
                                    || t2.isFieldAccessed(null)
                                    || t0.isAnyLocalVarChanged(t1) || t0
                                    .isAnyLocalVarChanged(t2)) && (!t1
                            .isImmutable() || !t2.isImmutable()))) {
                rmask = 1;
                if (!t2.isSafeExpr()
                        || (!t1.isSafeWithThrow() && !t2.isImmutable())) {
                    rmask = 2;
                    if (sym == LexTerm.EQUALS
                            && exprType2.objectSize() >= Type.CLASSINTERFACE
                            && exprType0.signatureClass()
                                    .hasInstantatedSubclasses(true)) {
                        if (t2.isSafeWithThrow() && t0.isSafeExpr()
                                && t1.isSafeExpr()) {
                            rmask = 0;
                        } else if (!t0.isImmutable()
                                && (!t2.isSafeWithThrow()
                                        || !t1.isSafeWithThrow() || !t0
                                        .isSafeExpr())) {
                            rmask = 1;
                            if (!t1.isImmutable()
                                    && (!t1.isSafeExpr() || !t2
                                            .isSafeWithThrow())) {
                                rmask = 3;
                            }
                        }
                    } else if (t2.isSafeExpr()) {
                        if (!t0.isImmutable()) {
                            rmask = 3;
                        }
                    } else if (sym != LexTerm.EQUALS && t2.isSafeWithThrow()) {
                        if (!t1.isSafeWithThrow()
                                || (!t0.isSafeExpr() && (!t1.isSafeExpr() || !t0
                                        .isSafeWithThrow()))) {
                            rmask = 3;
                        }
                    } else {
                        rmask = 6;
                        if (sym == LexTerm.EQUALS) {
                            if (t2.isSafeWithThrow()
                                    && t1.isSafeWithThrow()
                                    && (t0.isSafeExpr() || (t1.isSafeExpr() && t0
                                            .isSafeWithThrow()))) {
                                rmask = 4;
                            } else if (t1.isImmutable()
                                    || (t1.isSafeExpr() && t2.isSafeWithThrow())) {
                                rmask = 5;
                            } else if (!t0.isImmutable()) {
                                rmask = 7;
                            }
                        } else if (!t0.isImmutable()
                                && !t1.isImmutable()
                                && (!t1.isSafeWithThrow() || (!t0.isSafeExpr() && (!t1
                                        .isSafeExpr() || !t0.isSafeWithThrow())))) {
                            rmask = 7;
                        }
                    }
                }
                if ((rmask & 1) != 0) {
                    rcvr1 = ++curRcvrs[Type.NULLREF];
                }
            }
            int s0 = exprType0.objectSize();
            int s2;
            if ((rmask & 2) != 0
                    || (rmask == 0 && (isString || sym == LexTerm.DIVIDE_EQUALS
                            || sym == LexTerm.MOD_EQUALS
                            || sym == LexTerm.FLSHIFT_EQUALS
                            || sym == LexTerm.SHRIGHT_EQUALS
                            || sym == LexTerm.SHLEFT_EQUALS || (sym != LexTerm.EQUALS
                            && (s2 = exprType2.objectSize()) >= Type.FLOAT && s0 < s2)))) {
                rcvr2 = ++curRcvrs[sym != LexTerm.EQUALS ? Type.NULLREF
                        : Type.INT];
            }
            if (rmask > 3) {
                rcvr3 = ++curRcvrs[s0 < Type.VOID ? s0 : Type.NULLREF];
            }
            int[] curRcvrs1 = OutputContext.copyRcvrs(curRcvrs);
            t0.allocRcvr(curRcvrs);
            curRcvrs2 = OutputContext.copyRcvrs(curRcvrs1);
            t1.allocRcvr(curRcvrs1);
            OutputContext.joinRcvrs(curRcvrs, curRcvrs1);
        } else {
            curRcvrs2 = OutputContext.copyRcvrs(curRcvrs);
            t0.allocRcvr(curRcvrs);
            int s2;
            VariableDefinition leftVar;
            if (sym != LexTerm.EQUALS
                    && (leftVar = t0.getVariable(true)) != null
                    && leftVar.used() && leftVar.isThreadVolatile()) {
                rcvr1 = ++curRcvrs2[Type.NULLREF];
                rcvr2 = ++curRcvrs2[isString ? Type.NULLREF : exprType0
                        .objectSize()];
            } else if (t0.isSafeWithThrow() ? !t2.isSafeExpr()
                    && (!t0.isSafeExpr() || (sym != LexTerm.EQUALS
                            && !t2.isSafeWithThrow() && (t0
                            .isFieldAccessed(null) || t2
                            .isAnyLocalVarChanged(t0)))) : !t2.isImmutable()) {
                rcvr1 = ++curRcvrs2[Type.NULLREF];
                int s0;
                if (sym != LexTerm.EQUALS && !t2.isSafeWithThrow()) {
                    rcvr2 = ++curRcvrs2[(s0 = exprType0.objectSize()) < Type.VOID ? s0
                            : Type.NULLREF];
                }
            } else if ((isString || sym == LexTerm.DIVIDE_EQUALS
                    || sym == LexTerm.MOD_EQUALS
                    || sym == LexTerm.FLSHIFT_EQUALS
                    || sym == LexTerm.SHRIGHT_EQUALS
                    || sym == LexTerm.SHLEFT_EQUALS || (sym != LexTerm.EQUALS
                    && (s2 = exprType2.objectSize()) >= Type.FLOAT && exprType0
                    .objectSize() < s2))
                    && (!t0.isSafeExpr() || (!t2.isSafeWithThrow() && (t0
                            .isFieldAccessed(null) || t2
                            .isAnyLocalVarChanged(t0))))) {
                rcvr1 = ++curRcvrs2[Type.NULLREF];
            }
        }
        t2.allocRcvr(curRcvrs2);
        OutputContext.joinRcvrs(curRcvrs, curRcvrs2);
    }

    ExpressionType exprType() {
        assertCond(exprType0 != null);
        return exprType0;
    }

    ExpressionType actualExprType() {
        assertCond(exprType0 != null);
        return actualType2;
    }

    boolean isNotNull() {
        return isString || terms[2].isNotNull();
    }

    VariableDefinition getVariable(boolean allowInstance) {
        return terms[1].getSym() == LexTerm.EQUALS ? terms[0]
                .getVariable(allowInstance) : null;
    }

    boolean isBoolAssign() {
        assertCond(exprType0 != null);
        return terms[1].getSym() == LexTerm.EQUALS
                && exprType0.objectSize() == Type.BOOLEAN;
    }

    boolean isSwitchMapAssign(boolean isMethodCall) {
        assertCond(exprType0 != null);
        if (isMethodCall || terms[1].getSym() != LexTerm.EQUALS
                || exprType0.objectSize() != Type.INT)
            return false;
        Term t1 = terms[0].getArgumentTerm(1);
        if (t1 == null)
            return false;
        VariableDefinition v = terms[0].getArgumentTerm(0).getVariable(false);
        return v != null && v.isClassVariable() && v.isFinalVariable()
                && v.id().startsWith("$SwitchMap$")
                && terms[2].evaluateConstValue() != null
                && t1.isSwitchMapAssign(true);
    }

    String strLiteralValueGuess() {
        if (isString) {
            String str0 = terms[0].strLiteralValueGuess();
            String str2 = terms[2].strLiteralValueGuess();
            return str0 != null ? (str2 != null ? str0 + str2 : str0) : str2;
        }
        return terms[1].getSym() == LexTerm.EQUALS ? terms[2]
                .strLiteralValueGuess() : null;
    }

    ExpressionType classLiteralValGuess() {
        return terms[1].getSym() == LexTerm.EQUALS ? terms[2]
                .classLiteralValGuess() : null;
    }

    boolean storeClassLiteralsGuess(ObjVector parmSig, boolean isActual) {
        return terms[1].getSym() == LexTerm.EQUALS
                && terms[2].storeClassLiteralsGuess(parmSig, isActual);
    }

    MethodInvocation getClassNewInstanceCall() {
        return terms[1].getSym() == LexTerm.EQUALS ? terms[2]
                .getClassNewInstanceCall() : null;
    }

    MethodSignature getConstructorInstanceSign() {
        return terms[1].getSym() == LexTerm.EQUALS ? terms[2]
                .getConstructorInstanceSign() : null;
    }

    void discoverObjLeaks() {
        terms[0].discoverObjLeaks();
        terms[2].discoverObjLeaks();
        if (isString
                || (terms[1].getSym() == LexTerm.EQUALS && exprType2
                        .objectSize() >= Type.CLASSINTERFACE)) {
            terms[2].setObjLeaks(terms[0].getVariable(false));
        }
    }

    void setStackObjVolatile() {
        if (isString) {
            terms[0].setStackObjVolatile();
        }
        terms[2].setStackObjVolatile();
    }

    void setObjLeaks(VariableDefinition v) {
        if (isString) {
            terms[0].setObjLeaks(v);
        }
        terms[2].setObjLeaks(v);
    }

    boolean isAtomary() {
        return (rcvr1 | rcvr2 | rcvr3) > 0;
    }

    void setVoidExpression() {
        isVoidExpr = true;
    }

    private boolean isFinalFieldSetToZero() {
        VariableDefinition v = terms[0].getVariable(false);
        ConstValue constVal2;
        return v != null
                && (!v.used() || (v.isFinalVariable() && !v.isLocalOrParam()
                        && (constVal2 = terms[2].evaluateConstValue()) != null && !constVal2
                        .isNonZero()));
    }

    void processOutput(OutputContext oc) {
        int sym = terms[1].getSym();
        int s0 = exprType0.objectSize();
        int s2 = exprType2.objectSize();
        String cname = exprType0.castName();
        String rcvrStr1 = null;
        String rcvrStr2 = null;
        String rcvrStr3 = null;
        Term t0 = terms[0];
        Term t1 = t0.getArgumentTerm(1);
        VariableDefinition leftVar = null;
        boolean isVolatile = false;
        if (t1 != null) {
            t0 = t0.getArgumentTerm(0);
        } else if ((leftVar = t0.getVariable(true)) != null && leftVar.used()) {
            isVolatile = leftVar.isThreadVolatile();
        }
        Term t2 = terms[2];
        ExpressionType oldAssignmentRightType = oc.assignmentRightType;
        oc.assignmentRightType = null;
        if ((rcvr1 | rcvr2 | rcvr3) > 0) {
            oc.cPrint("(");
            if (t1 != null) {
                rcvrStr1 = rcvr1 > 0 ? OutputContext.getRcvrName(rcvr1,
                        Type.CLASSINTERFACE) : rcvr2 > 0 ? OutputContext
                        .getRcvrName(rcvr2,
                                sym != LexTerm.EQUALS ? Type.CLASSINTERFACE
                                        : Type.INT) : OutputContext
                        .getRcvrName(rcvr3, s0);
                if ((rcvr1 > 0 && (rcvr2 | rcvr3) > 0)
                        || (rcvr2 > 0 && rcvr3 > 0)) {
                    rcvrStr2 = rcvr1 > 0 && rcvr2 > 0 ? OutputContext
                            .getRcvrName(rcvr2,
                                    sym != LexTerm.EQUALS ? Type.CLASSINTERFACE
                                            : Type.INT) : OutputContext
                            .getRcvrName(rcvr3, s0);
                    if (rcvr1 > 0 && rcvr2 > 0 && rcvr3 > 0) {
                        rcvrStr3 = OutputContext.getRcvrName(rcvr3, s0);
                    }
                }
                if (rcvr1 > 0 || sym == LexTerm.EQUALS) {
                    oc.cPrint(rcvrStr1);
                    oc.cPrint("= ");
                    if (rcvr1 > 0) {
                        oc.cPrint("(");
                        oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
                        oc.cPrint(")");
                        t0.processOutput(oc);
                    } else if (rcvr2 > 0) {
                        t1.processOutput(oc);
                    } else if (s2 >= Type.FLOAT && s0 < s2
                                && s0 <= Type.FLOAT) {
                        if (s0 < Type.INT) {
                            oc.cPrint("(");
                            oc.cPrint(Type.cName[s0]);
                            oc.cPrint(")");
                        }
                        CastExpression.outputFloatCast(oc, s0, s2);
                        t2.processOutput(oc);
                        oc.cPrint(")");
                    } else {
                        if (s0 >= Type.CLASSINTERFACE || s0 < s2
                                || (s0 >= Type.LONG && s0 > s2)) {
                            oc.cPrint("(");
                            oc.cPrint(Type.cName[s0 < Type.CLASSINTERFACE ? s0
                                    : Type.CLASSINTERFACE]);
                            oc.cPrint(")");
                            t2.atomaryOutput(oc);
                        } else {
                            t2.processOutput(oc);
                        }
                    }
                    oc.cPrint(", ");
                }
                if ((rcvr2 | rcvr3) > 0) {
                    if (sym != LexTerm.EQUALS) {
                        if (rcvr2 > 0 && rcvr3 > 0) {
                            oc.cPrint(rcvr1 > 0 ? rcvrStr3 : rcvrStr2);
                            oc.cPrint("= *(");
                            oc.cPrint(Type.cName[s0]);
                            oc.cPrint("*)(");
                        }
                        oc.cPrint(rcvr1 > 0 ? rcvrStr2 : rcvrStr1);
                        oc.cPrint("= (void*)&");
                        boolean forceCheck = ArrayAccess.outputArrAccess(oc,
                                forceVmExc, s0, t0);
                        if (rcvr1 > 0) {
                            oc.cPrint("(");
                            oc.cPrint(Type.cName[s0 >= Type.CLASSINTERFACE ? Type.OBJECTARRAY
                                    : s0 + Type.CLASSINTERFACE]);
                            oc.cPrint(")");
                            oc.cPrint(rcvrStr1);
                        } else {
                            t0.processOutput(oc);
                        }
                        oc.cPrint(", ");
                        t1.processOutput(oc);
                        oc.cPrint(")");
                        if (forceCheck) {
                            oc.cPrint("[0]");
                        }
                        if (rcvr2 > 0 && rcvr3 > 0) {
                            oc.cPrint(")");
                        }
                        oc.cPrint(", ");
                    } else if (rcvr1 > 0 || (rcvr2 > 0 && rcvr3 > 0)) {
                        oc.cPrint(rcvrStr2);
                        oc.cPrint("= ");
                        if (rcvr1 > 0 && rcvr2 > 0) {
                            t1.processOutput(oc);
                            if (rcvr3 > 0) {
                                oc.cPrint(", ");
                                oc.cPrint(rcvrStr3);
                                oc.cPrint("= ");
                            }
                        }
                        if (rcvr3 > 0) {
                            if (s2 >= Type.FLOAT && s0 < s2
                                    && s0 <= Type.FLOAT) {
                                if (s0 < Type.INT) {
                                    oc.cPrint("(");
                                    oc.cPrint(Type.cName[s0]);
                                    oc.cPrint(")");
                                }
                                CastExpression.outputFloatCast(oc, s0, s2);
                                t2.processOutput(oc);
                                oc.cPrint(")");
                            } else if (s0 >= Type.CLASSINTERFACE || s0 < s2
                                    || (s0 >= Type.LONG && s0 > s2)) {
                                oc.cPrint("(");
                                oc.cPrint(Type.cName[s0 < Type.CLASSINTERFACE ? s0
                                        : Type.CLASSINTERFACE]);
                                oc.cPrint(")");
                                t2.atomaryOutput(oc);
                            } else {
                                t2.processOutput(oc);
                            }
                        }
                        oc.cPrint(", ");
                    }
                }
            } else {
                rcvrStr1 = OutputContext
                        .getRcvrName(rcvr1, Type.CLASSINTERFACE);
                if (rcvr2 > 0) {
                    rcvrStr2 = OutputContext.getRcvrName(rcvr2, s0);
                    oc.cPrint(rcvrStr2);
                    oc.cPrint("= ");
                    if (isVolatile) {
                        if (s0 >= Type.CLASSINTERFACE) {
                            oc.cPrint("JCGO_VLT_LFETCH(");
                            oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
                            oc.cPrint(", ");
                            oc.cPrint("((");
                            oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
                            oc.cPrint(" JCGO_THRD_VOLATILE");
                        } else {
                            oc.cPrint("JCGO_VLT_FETCH");
                            oc.cPrint(Type.sig[s0]);
                            oc.cPrint("(((JCGO_THRD_VOLATILE ");
                            oc.cPrint(cname);
                        }
                    } else {
                        oc.cPrint("*(");
                        oc.cPrint(Type.cName[s0 < Type.CLASSINTERFACE ? s0
                                : Type.CLASSINTERFACE]);
                    }
                    oc.cPrint("*)(");
                }
                oc.cPrint(rcvrStr1);
                oc.cPrint("= (void*)&");
                t0.atomaryOutput(oc);
                if (rcvr2 > 0) {
                    oc.cPrint(")");
                    if (isVolatile) {
                        oc.cPrint(")[0])");
                    }
                }
                oc.cPrint(", ");
            }
        }
        VariableDefinition oldAssignmentVar = oc.assignmentVar;
        oc.assignmentVar = leftVar;
        if (oldAssignmentVar != oc.assignmentVar
                && oldAssignmentRightType != null) {
            oc.assignmentVar = null;
        }
        ExpressionType assignRightType = oldAssignmentRightType == exprType0
                || oldAssignmentRightType == null ? exprType0
                : Main.dict.classTable[Type.VOID];
        if (sym == LexTerm.EQUALS) {
            if (t1 == null) {
                boolean castToVoid = false;
                if (isVolatile) {
                    if (s0 >= Type.CLASSINTERFACE) {
                        oc.cPrint("JCGO_VLT_LSTORE(");
                        oc.cPrint(isVoidExpr ? Type.cName[Type.VOID] : cname);
                        oc.cPrint(", ");
                    } else {
                        oc.cPrint("JCGO_VLT_STORE");
                        oc.cPrint(Type.sig[s0]);
                        oc.cPrint("((");
                        if (rcvr1 > 0) {
                            oc.cPrint("JCGO_THRD_VOLATILE ");
                            oc.cPrint(cname);
                            oc.cPrint("*)");
                        }
                    }
                    if (rcvr1 > 0) {
                        oc.cPrint(rcvrStr1);
                    } else {
                        oc.cPrint("&");
                        t0.processOutput(oc);
                        if (s0 < Type.CLASSINTERFACE) {
                            oc.cPrint(")");
                        }
                    }
                    oc.cPrint(", ");
                } else if (rcvr1 > 0) {
                    oc.cPrint("*(");
                    oc.cPrint(cname);
                    oc.cPrint("*)");
                    oc.cPrint(rcvrStr1);
                    oc.cPrint("= ");
                } else {
                    castToVoid = isVoidExpr;
                    if (!isFinalFieldSetToZero()) {
                        t0.processOutput(oc);
                        castToVoid = false;
                        oc.cPrint("= ");
                    }
                }
                if (castToVoid) {
                    assertCond(!isVolatile);
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[Type.VOID]);
                    oc.cPrint(")");
                    t2.atomaryOutput(oc);
                } else {
                    oc.assignmentRightType = assignRightType;
                    if (s0 == Type.BOOLEAN
                            || (exprType0 != exprType2 && (!isVolatile || s0 < Type.CLASSINTERFACE))) {
                        oc.cPrint("(");
                        oc.cPrint(exprType0.castName());
                        oc.cPrint(")");
                        t2.atomaryOutput(oc);
                    } else {
                        t2.processOutput(oc);
                    }
                    if (isVolatile) {
                        oc.cPrint(")");
                    }
                }
            } else if (exprType2.objectSize() >= Type.CLASSINTERFACE
                    && exprType0.signatureClass()
                            .hasInstantatedSubclasses(true)) {
                boolean forceCheck = false;
                boolean isNotNull0;
                if ((forceVmExc & (ClassDefinition.ARR_STORE_EXC | ClassDefinition.INDEX_OUT_EXC)) != 0
                        || (!(isNotNull0 = t0.isNotNull()) && (forceVmExc & ClassDefinition.NULL_PTR_EXC) != 0)) {
                    if (!isVoidExpr) {
                        oc.cPrint("(");
                        oc.cPrint(cname);
                        oc.cPrint(")");
                    }
                    oc.cPrint("jcgo_objArraySet(");
                    forceCheck = true;
                } else {
                    oc.cPrint(isNotNull0 ? "JCGO_ARRAY_NZOBJSET("
                            : "JCGO_ARRAY_OBJSET(");
                    oc.cPrint(isVoidExpr ? Type.cName[Type.VOID] : cname);
                    oc.cPrint(", ");
                }
                if (rcvr1 > 0) {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[s0 >= Type.CLASSINTERFACE ? Type.OBJECTARRAY
                            : s0 + Type.CLASSINTERFACE]);
                    oc.cPrint(")");
                    oc.cPrint(rcvrStr1);
                } else {
                    t0.processOutput(oc);
                }
                oc.cPrint(", ");
                if (rcvr2 > 0) {
                    oc.cPrint(rcvr1 > 0 ? rcvrStr2 : rcvrStr1);
                } else {
                    t1.processOutput(oc);
                }
                oc.cPrint(", ");
                if (rcvr3 > 0) {
                    oc.cPrint(rcvr1 > 0 && rcvr2 > 0 ? rcvrStr3
                            : (rcvr1 | rcvr2) > 0 ? rcvrStr2 : rcvrStr1);
                } else {
                    oc.assignmentRightType = assignRightType;
                    if (forceCheck) {
                        oc.cPrint("(");
                        oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
                        oc.cPrint(")");
                        t2.atomaryOutput(oc);
                    } else {
                        t2.processOutput(oc);
                    }
                }
                oc.cPrint(")");
            } else {
                if (s0 >= Type.CLASSINTERFACE) {
                    oc.cPrint("*(");
                    oc.cPrint(cname);
                    oc.cPrint("*)&");
                }
                boolean forceCheck = ArrayAccess.outputArrAccess(oc,
                        forceVmExc, s0, t0);
                if (rcvr1 > 0) {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[s0 >= Type.CLASSINTERFACE ? Type.OBJECTARRAY
                            : s0 + Type.CLASSINTERFACE]);
                    oc.cPrint(")");
                    oc.cPrint(rcvrStr1);
                } else {
                    t0.processOutput(oc);
                }
                oc.cPrint(", ");
                if (rcvr2 > 0) {
                    oc.cPrint(rcvr1 > 0 ? rcvrStr2 : rcvrStr1);
                } else {
                    t1.processOutput(oc);
                }
                oc.cPrint(")");
                if (forceCheck) {
                    oc.cPrint("[0]");
                }
                oc.cPrint("= ");
                if (rcvr3 > 0 ? s0 >= Type.CLASSINTERFACE : s0 == Type.BOOLEAN
                        || exprType0 != exprType2) {
                    oc.cPrint("(");
                    oc.cPrint(exprType0.castName());
                    oc.cPrint(")");
                }
                if (rcvr3 > 0) {
                    oc.cPrint(rcvr1 > 0 && rcvr2 > 0 ? rcvrStr3
                            : (rcvr1 | rcvr2) > 0 ? rcvrStr2 : rcvrStr1);
                } else {
                    oc.assignmentRightType = assignRightType;
                    t2.atomaryOutput(oc);
                }
            }
        } else {
            if (isString || (s2 >= Type.FLOAT && s0 < s2 && s0 <= Type.FLOAT)
                    || sym == LexTerm.DIVIDE_EQUALS
                    || sym == LexTerm.MOD_EQUALS
                    || sym == LexTerm.FLSHIFT_EQUALS
                    || sym == LexTerm.SHRIGHT_EQUALS
                    || sym == LexTerm.SHLEFT_EQUALS) {
                if ((rcvr1 | rcvr2 | rcvr3) > 0) {
                    if (isVolatile) {
                        if (isString) {
                            oc.cPrint("JCGO_VLT_LSTORE(");
                            oc.cPrint(isVoidExpr ? Type.cName[Type.VOID]
                                    : cname);
                            oc.cPrint(", ");
                        } else {
                            oc.cPrint("JCGO_VLT_STORE");
                            oc.cPrint(Type.sig[s0]);
                            oc.cPrint("((JCGO_THRD_VOLATILE ");
                            oc.cPrint(cname);
                            oc.cPrint("*)");
                        }
                    } else {
                        oc.cPrint("*(");
                        oc.cPrint(cname);
                        oc.cPrint("*)");
                    }
                    oc.cPrint(t1 == null || rcvr1 == 0 || rcvr2 == 0 ? rcvrStr1
                            : rcvrStr2);
                } else {
                    assertCond(!isVolatile);
                    rcvrStr1 = t0.stringOutput();
                    oc.cPrint(rcvrStr1);
                }
                oc.cPrint(isVolatile ? ", " : "= ");
                if (isString) {
                    MethodDefinition md = Main.dict.get(
                            Names.JAVA_LANG_VMSYSTEM).getMethod(
                            Names.SIGN_CONCAT0X);
                    oc.cPrint(md != null ? md.routineCName()
                            : MethodDefinition.UNKNOWN_NAME);
                    oc.cPrint("(\010 (");
                    oc.cPrint(Main.dict.get(Names.JAVA_LANG_STRING).castName());
                    oc.cPrint(")");
                    Main.dict.normalCalls++;
                } else {
                    if (s0 < Type.INT || s0 < s2) {
                        if (s0 < Type.INT
                                || (s2 == Type.LONG && (sym == LexTerm.DIVIDE_EQUALS || sym == LexTerm.MOD_EQUALS))) {
                            oc.cPrint("(");
                            oc.cPrint(cname);
                            oc.cPrint(")");
                        }
                        if (s2 >= Type.FLOAT) {
                            CastExpression.outputFloatCast(oc, s0, s2);
                        }
                    }
                    if (sym != LexTerm.MINUS_EQUALS
                            && sym != LexTerm.PLUS_EQUALS
                            && sym != LexTerm.TIMES_EQUALS) {
                        BinaryOp.outputDivShift(
                                oc,
                                sym == LexTerm.DIVIDE_EQUALS ? LexTerm.DIVIDE
                                        : sym == LexTerm.MOD_EQUALS ? LexTerm.MOD
                                                : sym == LexTerm.FLSHIFT_EQUALS ? LexTerm.FILLSHIFT_RIGHT
                                                        : sym == LexTerm.SHLEFT_EQUALS ? LexTerm.SHIFT_LEFT
                                                                : LexTerm.SHIFT_RIGHT,
                                s0 >= s2
                                        || (sym != LexTerm.DIVIDE_EQUALS && sym != LexTerm.MOD_EQUALS) ? s0
                                        : s2);
                    }
                    if (s2 >= Type.LONG && s0 < s2
                            && sym != LexTerm.FLSHIFT_EQUALS
                            && sym != LexTerm.SHRIGHT_EQUALS
                            && sym != LexTerm.SHLEFT_EQUALS) {
                        oc.cPrint("(");
                        oc.cPrint(Type.cName[s2]);
                        oc.cPrint(")");
                    }
                }
                if (rcvr2 > 0 && (t1 == null || rcvr3 > 0)) {
                    oc.cPrint(rcvr1 == 0 || rcvr3 == 0 ? rcvrStr2 : rcvrStr3);
                } else {
                    if ((rcvr1 | rcvr2 | rcvr3) > 0) {
                        oc.cPrint("*(");
                        oc.cPrint(isString ? Type.cName[Type.CLASSINTERFACE]
                                : cname);
                        oc.cPrint("*)");
                    }
                    oc.cPrint((rcvr1 > 0 && rcvr2 > 0) || rcvr3 > 0 ? rcvrStr2
                            : rcvrStr1);
                }
                oc.assignmentRightType = assignRightType;
                if (isString) {
                    oc.cPrint(", ");
                    t2.processOutput(oc);
                } else {
                    oc.cPrint(sym == LexTerm.MINUS_EQUALS ? "- "
                            : sym == LexTerm.PLUS_EQUALS ? "+ "
                                    : sym == LexTerm.TIMES_EQUALS ? "* " : ", ");
                    if (sym == LexTerm.FLSHIFT_EQUALS
                            || sym == LexTerm.SHRIGHT_EQUALS
                            || sym == LexTerm.SHLEFT_EQUALS) {
                        if (exprType2.objectSize() == Type.LONG) {
                            oc.cPrint("(");
                            oc.cPrint(Type.cName[Type.INT]);
                            oc.cPrint(")");
                            t2.atomaryOutput(oc);
                        } else {
                            t2.processOutput(oc);
                        }
                    } else {
                        if (s0 >= Type.LONG && s0 > s2) {
                            oc.cPrint("(");
                            oc.cPrint(cname);
                            oc.cPrint(")");
                            t2.atomaryOutput(oc);
                        } else if (sym == LexTerm.DIVIDE_EQUALS
                                || sym == LexTerm.MOD_EQUALS) {
                            t2.processOutput(oc);
                        } else {
                            t2.atomaryOutput(oc);
                        }
                    }
                }
                oc.cPrint(")");
                if (!isString
                        && s2 >= Type.FLOAT
                        && s0 < s2
                        && (sym == LexTerm.DIVIDE_EQUALS || sym == LexTerm.MOD_EQUALS)) {
                    oc.cPrint(")");
                }
            } else {
                if (t1 == null && rcvr1 == 0) {
                    assertCond(!isVolatile);
                    t0.processOutput(oc);
                } else if (t1 != null && (rcvr2 | rcvr3) == 0) {
                    assertCond(!isVolatile);
                    boolean forceCheck = ArrayAccess.outputArrAccess(oc,
                            forceVmExc, s0, t0);
                    if ((rcvr1 | rcvr2 | rcvr3) > 0) {
                        oc.cPrint("(");
                        oc.cPrint(Type.cName[s0 >= Type.CLASSINTERFACE ? Type.OBJECTARRAY
                                : s0 + Type.CLASSINTERFACE]);
                        oc.cPrint(")");
                        oc.cPrint(rcvrStr1);
                    } else {
                        t0.processOutput(oc);
                    }
                    oc.cPrint(", ");
                    t1.processOutput(oc);
                    oc.cPrint(")");
                    if (forceCheck) {
                        oc.cPrint("[0]");
                    }
                } else {
                    if (isVolatile) {
                        oc.cPrint("JCGO_VLT_STORE");
                        oc.cPrint(Type.sig[s0]);
                        oc.cPrint("((JCGO_THRD_VOLATILE ");
                    } else {
                        oc.cPrint("*(");
                    }
                    oc.cPrint(cname);
                    oc.cPrint("*)");
                    oc.cPrint(t1 == null || rcvr1 == 0 || rcvr2 == 0 ? rcvrStr1
                            : rcvrStr2);
                }
                if (rcvr2 > 0 && (t1 == null || rcvr3 > 0)) {
                    oc.cPrint(isVolatile ? ", " : "= ");
                    if (s0 < Type.INT) {
                        oc.cPrint("(");
                        oc.cPrint(cname);
                        oc.cPrint(")(");
                    }
                    oc.cPrint(rcvr1 > 0 && rcvr3 > 0 ? rcvrStr3 : rcvrStr2);
                    if (sym == LexTerm.XOR_EQUALS) {
                        oc.cPrint("^");
                    } else if (sym == LexTerm.BITOR_EQUALS) {
                        oc.cPrint("|");
                    } else if (sym == LexTerm.BITAND_EQUALS) {
                        oc.cPrint("&");
                    } else if (sym == LexTerm.MINUS_EQUALS) {
                        oc.cPrint("-");
                    } else if (sym == LexTerm.PLUS_EQUALS) {
                        oc.cPrint("+");
                    } else if (sym == LexTerm.TIMES_EQUALS) {
                        oc.cPrint("*");
                    }
                } else {
                    assertCond(!isVolatile);
                    terms[1].processOutput(oc);
                }
                oc.cPrint(" ");
                oc.assignmentRightType = assignRightType;
                if (s0 == Type.BOOLEAN || exprType0 != exprType2) {
                    oc.cPrint("(");
                    oc.cPrint(cname);
                    oc.cPrint(")");
                    t2.atomaryOutput(oc);
                    if (s0 < Type.INT && rcvr2 > 0 && (t1 == null || rcvr3 > 0)) {
                        oc.cPrint(")");
                    }
                } else if (rcvr2 > 0 && (t1 == null || rcvr3 > 0)) {
                    t2.atomaryOutput(oc);
                    if (s0 < Type.INT) {
                        oc.cPrint(")");
                    }
                } else {
                    t2.processOutput(oc);
                }
            }
            if (isVolatile) {
                oc.cPrint(")");
            }
        }
        oc.assignmentVar = oldAssignmentVar;
        oc.assignmentRightType = oldAssignmentRightType;
        if ((rcvr1 | rcvr2 | rcvr3) > 0) {
            oc.cPrint(")");
        }
    }

    ExpressionType traceClassInit() {
        terms[0].traceClassInit();
        ExpressionType curTraceType2 = terms[2].traceClassInit();
        if (isString) {
            MethodDefinition md = Main.dict.get(Names.JAVA_LANG_VMSYSTEM)
                    .getMethod(Names.SIGN_CONCAT0X);
            if (md != null) {
                md.methodTraceClassInit(false, null, null);
            }
            return null;
        }
        if (terms[1].getSym() != LexTerm.EQUALS
                || exprType0.objectSize() < Type.CLASSINTERFACE)
            return null;
        VariableDefinition v = terms[0].getVariable(false);
        if (v != null) {
            v.setTraceExprType(curTraceType2 != null ? curTraceType2
                    : actualType2, false);
        }
        return curTraceType2;
    }
}
