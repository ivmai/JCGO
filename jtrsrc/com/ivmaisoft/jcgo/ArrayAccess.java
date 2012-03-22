/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ArrayAccess.java --
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
 * Grammar production for accessing elements in an array.
 ** 
 * Formats: PrimaryNoNewArray LBRACK Expression RBRACK Expression(QualifiedName)
 * LBRACK Expression RBRACK
 */

final class ArrayAccess extends LexNode {

    private ExpressionType exprType;

    private int forceVmExc;

    private int rcvr;

    private boolean lvalue;

    ArrayAccess(Term a, Term c) {
        super(a, c);
    }

    Term getArgumentTerm(int index) {
        return index == 0 ? terms[0] : index == 1 ? terms[1] : null;
    }

    void processPass1(Context c) {
        if (exprType == null) {
            forceVmExc = c.forceVmExc;
            lvalue = c.lvalue;
            c.lvalue = false;
            c.setOnly = false;
            BranchContext oldBranch = c.saveBranch();
            terms[0].processPass1(c);
            oldBranch = c.swapBranch(oldBranch);
            terms[1].processPass1(c);
            c.unionBranch(oldBranch);
            exprType = terms[0].exprType().indirectedType();
            if (exprType == null) {
                fatalError(c, "Illegal array access");
            }
            int s1 = terms[1].exprType().objectSize();
            if (s1 < Type.BYTE || s1 > Type.INT) {
                fatalError(c, "Illegal array index");
            }
            VariableDefinition v = terms[0].getVariable(false);
            if (v != null) {
                c.setVarNotNull(v);
            }
        }
    }

    ExpressionType exprType() {
        assertCond(exprType != null);
        return exprType;
    }

    ExpressionType actualExprType() {
        assertCond(exprType != null);
        ExpressionType actual = terms[0].actualExprType().indirectedType();
        if (actual == null) {
            actual = exprType;
        }
        return actual;
    }

    boolean isSafeWithThrow() {
        return terms[0].isSafeWithThrow() && terms[1].isSafeWithThrow();
    }

    int tokenCount() {
        return terms[0].tokenCount() + terms[1].tokenCount()
                + (terms[0].isSafeExpr() || terms[1].isSafeExpr() ? 1 : 2);
    }

    void allocRcvr(int[] curRcvrs) {
        int[] curRcvrs1 = OutputContext.copyRcvrs(curRcvrs);
        terms[0].allocRcvr(curRcvrs);
        if (BinaryOp.isRcvrNeeded(terms[0], terms[1])) {
            rcvr = ++curRcvrs1[Type.NULLREF];
        }
        terms[1].allocRcvr(curRcvrs1);
        OutputContext.joinRcvrs(curRcvrs, curRcvrs1);
    }

    void discoverObjLeaks() {
        terms[0].discoverObjLeaks();
        if (lvalue) {
            terms[0].setObjLeaks(VariableDefinition.WRITABLE_ARRAY_VAR);
        }
        terms[1].discoverObjLeaks();
    }

    boolean isAtomary() {
        assertCond(exprType != null);
        return !lvalue
                && (rcvr > 0 || exprType.objectSize() >= Type.CLASSINTERFACE);
    }

    static boolean outputArrAccess(OutputContext oc, int forceVmExc, int s0,
            Term t0) {
        boolean isNotNull0 = t0.isNotNull();
        boolean forceCheck = false;
        if ((forceVmExc & ClassDefinition.INDEX_OUT_EXC) != 0
                || (!isNotNull0 && (forceVmExc & ClassDefinition.NULL_PTR_EXC) != 0)) {
            oc.cPrint("jcgo_");
            oc.cPrint(Type.cName[s0 >= Type.CLASSINTERFACE ? Type.CLASSINTERFACE
                    : s0]);
            oc.cPrint("ArrAccess");
            if (isNotNull0) {
                oc.cPrint("NZ");
            }
            forceCheck = true;
        } else {
            oc.cPrint("JCGO_ARRAY_");
            if (isNotNull0) {
                oc.cPrint("NZ");
            }
            oc.cPrint(Type.sig[s0 >= Type.CLASSINTERFACE ? Type.CLASSINTERFACE
                    : s0]);
            oc.cPrint("ACCESS");
        }
        oc.cPrint("(");
        return forceCheck;
    }

    void processOutput(OutputContext oc) {
        assertCond(exprType != null);
        Term t0 = terms[0];
        String rcvrStr = null;
        int s0 = exprType.objectSize();
        if (rcvr > 0) {
            rcvrStr = OutputContext.getRcvrName(rcvr, Type.CLASSINTERFACE);
            oc.cPrint("(");
            oc.cPrint(rcvrStr);
            oc.cPrint("= (");
            oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
            oc.cPrint(")");
            ExpressionType oldAssignmentRightType = oc.assignmentRightType;
            oc.assignmentRightType = null;
            t0.atomaryOutput(oc);
            oc.assignmentRightType = oldAssignmentRightType;
            oc.cPrint(", ");
        } else if (s0 >= Type.CLASSINTERFACE) {
            oc.cPrint("(");
        }
        if (s0 >= Type.CLASSINTERFACE) {
            oc.cPrint("(");
            oc.cPrint(exprType.castName());
            oc.cPrint(lvalue ? "*)&" : ")");
        } else if (rcvrStr != null && lvalue) {
            oc.cPrint("&");
        }
        boolean forceCheck = outputArrAccess(oc, forceVmExc, s0, t0);
        if (rcvrStr != null) {
            oc.cPrint("(");
            oc.cPrint(Type.cName[s0 >= Type.CLASSINTERFACE ? Type.OBJECTARRAY
                    : Type.CLASSINTERFACE + s0]);
            oc.cPrint(")");
            oc.cPrint(rcvrStr);
        } else {
            t0.processOutput(oc);
        }
        oc.cPrint(", ");
        terms[1].processOutput(oc);
        oc.cPrint(")");
        if (forceCheck) {
            oc.cPrint("[0]");
        }
        if (s0 >= Type.CLASSINTERFACE || rcvrStr != null) {
            oc.cPrint(")");
            if (lvalue) {
                oc.cPrint("[0]");
            }
        }
    }

    ExpressionType traceClassInit() {
        ExpressionType curTraceType0 = terms[0].traceClassInit();
        terms[1].traceClassInit();
        if (curTraceType0 != null && curTraceType0.objectSize() != Type.NULLREF) {
            curTraceType0 = curTraceType0.indirectedType();
        }
        return curTraceType0;
    }
}
