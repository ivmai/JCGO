/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/Argument.java --
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
 * Grammar production for a function argument.
 ** 
 * Format: Expression
 */

final class Argument extends LexNode {

    private ExpressionType exprType0;

    private ExpressionType actualType0;

    private ExpressionType formalType;

    private int rcvr;

    private boolean isImmutable;

    private boolean isImmutableSet;

    private boolean hasLeaks;

    private boolean hasParamRet;

    private boolean isStackObjVolatile;

    private boolean isWritableArray;

    private ExpressionType curTraceType;

    Argument(Term a) {
        super(a);
    }

    Argument(Term a, boolean analysisDone) {
        super(a);
        if (analysisDone) {
            processPassOneInner();
        }
    }

    Term joinParamLists(Term paramList) {
        if (!paramList.notEmpty())
            return this;
        return new ParameterList(this, paramList);
    }

    Term getTermAt(int index) {
        assertCond(index == 0);
        return this;
    }

    Term getArgumentTerm(int index) {
        return index == 0 ? terms[0] : null;
    }

    void processPass1(Context c) {
        terms[0].processPass1(c);
        processPassOneInner();
    }

    private void processPassOneInner() {
        exprType0 = terms[0].exprType();
        actualType0 = terms[0].actualExprType();
    }

    void setFormalType(Term formalParam, MethodDefinition md, Context c) {
        assertCond(exprType0 != null && formalType == null);
        formalType = formalParam.exprType();
        VariableDefinition v;
        if (c != null && (v = terms[0].getVariable(false)) != null) {
            VariableDefinition formalV = md.getLocalVar(formalParam
                    .getArgumentTerm(0).dottedName());
            if (md.isBranchVarNotNull(formalV)) {
                c.setVarNotNull(v);
            }
            ExpressionType actualType = md.getBranchActualType(formalV);
            if (actualType != null
                    && !ClassDefinition.isAssignableFrom(actualType,
                            actualType0, c.forClass)) {
                c.setActualType(v, actualType);
            }
        }
    }

    ExpressionType exprType() {
        assertCond(exprType0 != null);
        return exprType0;
    }

    void storeSignature(ObjVector parmSig) {
        assertCond(exprType0 != null);
        parmSig.addElement(exprType0);
    }

    boolean isNotNull() {
        assertCond(exprType0 != null);
        return terms[0].isNotNull();
    }

    boolean isImmutable() {
        if (!isImmutableSet) {
            isImmutable = terms[0].isImmutable();
            isImmutableSet = true;
        }
        return isImmutable;
    }

    boolean isSafeExpr() {
        return terms[0].isSafeExpr();
    }

    boolean isSafeWithThrow() {
        return terms[0].isSafeWithThrow();
    }

    int tokenCount() {
        return terms[0].tokenCount() + (terms[0].isSafeExpr() ? 0 : 1);
    }

    int markParamRcvr(int isNotSafe, int[] curRcvrs2) {
        assertCond(exprType0 != null);
        if (!isImmutable()) {
            if (isNotSafe <= 0) {
                if (terms[0].isSafeExpr()) {
                    if (isNotSafe != -2) {
                        if (isNotSafe == 0) {
                            isNotSafe = -1;
                        }
                        if (terms[0].isFieldAccessed(null)) {
                            if (isNotSafe == -1) {
                                isNotSafe = -2;
                            } else {
                                rcvr = -1;
                            }
                        }
                    }
                } else {
                    if (isNotSafe < -1 || terms[0].isAnyLocalVarChanged(null)) {
                        if (isNotSafe != 0) {
                            rcvr = -1;
                        }
                        isNotSafe = 1;
                    } else {
                        isNotSafe = -3;
                    }
                }
            } else {
                rcvr = -1;
            }
            if (rcvr != 0) {
                int s0 = exprType0.objectSize();
                if (s0 >= Type.VOID) {
                    s0 = Type.NULLREF;
                }
                curRcvrs2[s0]++;
            }
        }
        return isNotSafe;
    }

    void allocParamRcvr(int[] curRcvrs, int[] curRcvrs1, int[] curRcvrs2) {
        int[] curRcvrs0 = OutputContext.copyRcvrs(rcvr != 0 ? curRcvrs1
                : curRcvrs2);
        terms[0].allocRcvr(curRcvrs0);
        OutputContext.joinRcvrs(curRcvrs, curRcvrs0);
        if (rcvr != 0) {
            int s0 = exprType0.objectSize();
            if (s0 >= Type.VOID) {
                s0 = Type.NULLREF;
            }
            if ((rcvr = ++curRcvrs1[s0]) > curRcvrs[s0]) {
                curRcvrs[s0] = rcvr;
            }
        }
    }

    void produceRcvr(OutputContext oc) {
        if (rcvr > 0) {
            assertCond(exprType0 != null);
            int s0 = exprType0.objectSize();
            oc.cPrint(OutputContext.getRcvrName(rcvr, s0));
            oc.cPrint("= ");
            ExpressionType oldAssignmentRightType = oc.assignmentRightType;
            oc.assignmentRightType = null;
            if (s0 <= Type.BOOLEAN || s0 >= Type.CLASSINTERFACE) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[s0 == Type.BOOLEAN ? Type.BOOLEAN
                        : Type.CLASSINTERFACE]);
                oc.cPrint(")");
                terms[0].atomaryOutput(oc);
            } else {
                terms[0].processOutput(oc);
            }
            oc.assignmentRightType = oldAssignmentRightType;
            oc.cPrint(", ");
        }
    }

    boolean copyObjLeaksFrom(Term formalParam) {
        assertCond(exprType0 != null);
        if (formalParam == null || exprType0.objectSize() < Type.CLASSINTERFACE
                || formalParam.isReturnAtEnd(true)) {
            hasLeaks = true;
        } else {
            if (formalParam.isReturnAtEnd(false)) {
                hasParamRet = true;
            }
            if (formalParam.hasTailReturnOrThrow()) {
                isStackObjVolatile = true;
            }
            if (formalParam.isBoolAssign()) {
                isWritableArray = true;
            }
        }
        return hasLeaks;
    }

    void discoverObjLeaks() {
        assertCond(exprType0 != null);
        terms[0].discoverObjLeaks();
        if (hasLeaks && exprType0.objectSize() >= Type.CLASSINTERFACE) {
            terms[0].setObjLeaks(null);
        } else {
            if (isStackObjVolatile) {
                terms[0].setStackObjVolatile();
            }
            if (isWritableArray) {
                terms[0].setObjLeaks(VariableDefinition.WRITABLE_ARRAY_VAR);
            }
        }
    }

    void setStackObjVolatile() {
        if (!isStackObjVolatile && hasParamRet && !hasLeaks) {
            isStackObjVolatile = true;
            terms[0].setStackObjVolatile();
        }
    }

    void setObjLeaks(VariableDefinition v) {
        assertCond(exprType0 != null);
        if (hasParamRet
                && !hasLeaks
                && (!isWritableArray || v != VariableDefinition.WRITABLE_ARRAY_VAR)) {
            terms[0].setObjLeaks(v);
        }
    }

    void processOutput(OutputContext oc) {
        assertCond(false);
    }

    void parameterOutput(OutputContext oc, boolean asArg, int type) {
        assertCond(exprType0 != null);
        if (formalType == null) {
            formalType = exprType0;
        }
        int s1 = formalType.objectSize();
        if (type == s1 || (type == Type.NULLREF && s1 >= Type.CLASSINTERFACE)) {
            oc.cPrint(", ");
            int s0 = exprType0.objectSize();
            if (rcvr > 0) {
                if (s0 >= Type.CLASSINTERFACE || (s1 >= Type.LONG && s1 > s0)) {
                    oc.cPrint("(");
                    oc.cPrint(formalType.castName());
                    oc.cPrint(")");
                }
                oc.cPrint(OutputContext.getRcvrName(rcvr, s0));
            } else if (s0 >= Type.CLASSINTERFACE ? formalType != exprType0
                    : s0 == Type.BOOLEAN || (s1 >= Type.LONG && s1 > s0)) {
                oc.cPrint("(");
                oc.cPrint(formalType.castName());
                oc.cPrint(")");
                terms[0].atomaryOutput(oc);
            } else {
                terms[0].processOutput(oc);
            }
        }
    }

    void getTraceSignature(ObjVector parmTraceSig) {
        assertCond(curTraceType != null);
        parmTraceSig.addElement(curTraceType);
    }

    ExpressionType traceClassInit() {
        assertCond(exprType0 != null && formalType != null);
        if ((curTraceType = terms[0].traceClassInit()) == null) {
            curTraceType = actualType0.hasRealInstances() ? actualType0
                    : Main.dict.classTable[Type.NULLREF];
        }
        ClassDefinition cd;
        if (formalType.objectSize() < Type.VOID) {
            curTraceType = formalType;
        } else if (curTraceType.objectSize() == Type.CLASSINTERFACE
                && (cd = curTraceType.signatureClass()) != curTraceType
                && !cd.hasInstantatedSubclasses(false)) {
            curTraceType = cd;
        }
        return null;
    }
}
