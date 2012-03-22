/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/SynchroStatement.java --
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
 * Grammar production for a synchronized statement.
 ** 
 * Format: SYNCHRONIZED LPAREN Expression RPAREN Block
 */

final class SynchroStatement extends SynchroMethod {

    private Term exprTerm;

    private MethodDefinition curMethod;

    SynchroStatement(Term c, Term e) {
        super(e);
        exprTerm = c;
    }

    void processPass0(Context c) {
        exprTerm.processPass0(c);
        terms[0].processPass0(c);
    }

    void assignedVarNames(ObjQueue names, boolean recursive) {
        if (recursive) {
            exprTerm.assignedVarNames(names, true);
            terms[0].assignedVarNames(names, true);
        }
    }

    void processPass1(Context c) {
        exprTerm.processPass1(c);
        int s0 = exprTerm.exprType().objectSize();
        if (s0 >= Type.BOOLEAN && s0 <= Type.VOID) {
            fatalError(c, "Illegal type of expression for 'synchronized'");
        }
        VariableDefinition v = exprTerm.getVariable(false);
        if (v != null) {
            c.setVarNotNull(v);
        }
        curMethod = c.currentMethod;
        super.processPass1(c);
    }

    boolean hasTailReturnOrThrow() {
        return terms[0].hasTailReturnOrThrow();
    }

    boolean allowInline(int tokenLimit) {
        assertCond(curMethod != null);
        return (tokenLimit -= exprTerm.tokenCount()
                + (curMethod.exprType().objectSize() != Type.VOID ? 5 : 2)) >= 0
                && terms[0].allowInline(tokenLimit);
    }

    void allocRcvr(int[] curRcvrs) {
        exprTerm.allocRcvr(curRcvrs);
    }

    MethodDefinition superMethodCall() {
        return null;
    }

    void discoverObjLeaks() {
        exprTerm.discoverObjLeaks();
        exprTerm.setObjLeaks(VariableDefinition.WRITABLE_ARRAY_VAR);
        terms[0].discoverObjLeaks();
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        exprTerm.writeStackObjs(oc, scopeTerm);
        terms[0].writeStackObjs(oc, scopeTerm);
    }

    void processOutput(OutputContext oc) {
        oc.cPrint("{");
        oc.cPrint(exprTerm.isSafeExpr() && exprTerm.isNotNull() ? "JCGO_SYNC_BLOCKSAFENZ("
                : "JCGO_SYNC_BLOCK(");
        exprTerm.processOutput(oc);
        oc.cPrint(")\010");
        super.processOutput(oc);
        oc.cPrint("}");
    }

    ExpressionType traceClassInit() {
        exprTerm.traceClassInit();
        terms[0].traceClassInit();
        return null;
    }
}
