/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ForStatement.java --
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
 * Grammar production for the for statement.
 ** 
 * Format: FOR LPAREN [ForInit] SEMI [Expression] SEMI [ForUpdate] RPAREN
 * Statement/StatementNoShortIf
 */

final class ForStatement extends BreakableStmt {

    ForStatement(Term c, Term e, Term g, Term i) {
        super(c.isBlock() ? new LeftBrace() : Empty.newTerm(), c, e, g, i
                .isBlock() ? i : new Block(i), c.isBlock() ? new RightBrace()
                : Empty.newTerm());
    }

    void processPass1(Context c) {
        TryStatement oldLastBreakableTry = c.lastBreakableTry;
        c.lastBreakableTry = c.currentTry;
        terms[0].processPass1(c);
        terms[1].processPass1(c);
        ObjQueue names = new ObjQueue();
        terms[2].assignedVarNames(names, true);
        terms[3].assignedVarNames(names, true);
        terms[4].assignedVarNames(names, true);
        assertCond(c.currentMethod != null);
        c.currentMethod.resetLocalsActualType(c, names, false);
        terms[2].processPass1(c);
        if (terms[2].notEmpty()
                && terms[2].exprType().objectSize() != Type.BOOLEAN) {
            fatalError(c, "The condition expression must be of boolean type");
        }
        boolean oldHasBreakSimple = c.hasBreakSimple;
        c.hasBreakSimple = false;
        boolean oldHasBreakDeep = c.hasBreakDeep;
        c.hasBreakDeep = false;
        boolean oldHasContinueSimple = c.hasContinueSimple;
        c.hasContinueSimple = false;
        boolean oldHasContinueDeep = c.hasContinueDeep;
        c.hasContinueDeep = false;
        BranchContext oldBranch = c.saveBranch();
        terms[2].updateCondBranch(c, true);
        c.isConditional = true;
        boolean oldBreakableHidden = c.breakableHidden;
        c.breakableHidden = false;
        processPassOneBegin(c);
        terms[4].processPass1(c);
        processPassOneEnd(c);
        c.breakableHidden = oldBreakableHidden;
        if (c.hasContinueSimple || c.hasContinueDeep) {
            c.intersectBranch(oldBranch);
            oldBranch = c.saveBranch();
        }
        c.hasContinueSimple = oldHasContinueSimple;
        c.hasContinueDeep |= oldHasContinueDeep;
        terms[3].processPass1(c);
        c.intersectBranch(oldBranch);
        if (!c.hasBreakSimple && !c.hasBreakDeep) {
            terms[2].updateCondBranch(c, false);
        }
        c.hasBreakSimple = oldHasBreakSimple;
        c.hasBreakDeep |= oldHasBreakDeep;
        terms[5].processPass1(c);
        c.lastBreakableTry = oldLastBreakableTry;
    }

    void setContinueLabel(String label) {
        terms[4].setContinueLabel(label);
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        terms[1].writeStackObjs(oc, scopeTerm);
        terms[2].writeStackObjs(oc, scopeTerm);
        terms[3].writeStackObjs(oc, scopeTerm);
        terms[4].writeStackObjs(oc, scopeTerm);
    }

    void processOutput(OutputContext oc) {
        if (terms[0].notEmpty()) {
            terms[0].processOutput(oc);
            writeStackObjs(oc, terms[0]);
            terms[1].processOutput(oc);
            oc.cPrint(";");
        }
        oc.cPrint("for (");
        if (!terms[0].notEmpty()) {
            terms[1].processOutput(oc);
        }
        oc.cPrint("\003;\003");
        if (terms[2].notEmpty()) {
            oc.cPrint(" ");
        }
        terms[2].processOutput(oc);
        oc.cPrint("\003;\003");
        if (terms[3].notEmpty()) {
            oc.cPrint(" ");
        }
        terms[3].setVoidExpression();
        terms[3].processOutput(oc);
        oc.cPrint(")");
        terms[4].processOutput(oc);
        terms[5].processOutput(oc);
        outputBreakLabel(oc);
    }

    static void traceLoop(Term t0, Term t1, Term t2) {
        t0.traceClassInit();
        t1.traceClassInit();
        t2.traceClassInit();
    }

    ExpressionType traceClassInit() {
        terms[1].traceClassInit();
        traceLoop(terms[2], terms[4], terms[3]);
        return null;
    }
}
