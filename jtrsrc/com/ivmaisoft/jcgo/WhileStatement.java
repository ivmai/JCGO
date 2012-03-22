/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/WhileStatement.java --
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
 * Grammar production for a simple while statement.
 ** 
 * Format: WHILE LPAREN Expression RPAREN Statement/StatementNoShortIf
 */

final class WhileStatement extends BreakableStmt {

    WhileStatement(Term c, Term e) {
        super(c, e.isBlock() ? e : new Block(e));
    }

    void processPass1(Context c) {
        ObjQueue names = new ObjQueue();
        terms[0].assignedVarNames(names, true);
        terms[1].assignedVarNames(names, true);
        assertCond(c.currentMethod != null);
        c.currentMethod.resetLocalsActualType(c, names, false);
        TryStatement oldLastBreakableTry = c.lastBreakableTry;
        c.lastBreakableTry = c.currentTry;
        terms[0].processPass1(c);
        if (terms[0].exprType().objectSize() != Type.BOOLEAN) {
            fatalError(c, "The condition expression must be of boolean type");
        }
        boolean oldHasBreakSimple = c.hasBreakSimple;
        c.hasBreakSimple = false;
        boolean oldHasBreakDeep = c.hasBreakDeep;
        c.hasBreakDeep = false;
        boolean oldHasContinueSimple = c.hasContinueSimple;
        c.hasContinueSimple = false;
        BranchContext oldBranch = c.saveBranch();
        terms[0].updateCondBranch(c, true);
        c.isConditional = true;
        boolean oldBreakableHidden = c.breakableHidden;
        c.breakableHidden = false;
        processPassOneBegin(c);
        terms[1].processPass1(c);
        processPassOneEnd(c);
        c.breakableHidden = oldBreakableHidden;
        c.lastBreakableTry = oldLastBreakableTry;
        c.intersectBranch(oldBranch);
        c.hasContinueSimple = oldHasContinueSimple;
        if (!c.hasBreakSimple && !c.hasBreakDeep) {
            terms[0].updateCondBranch(c, false);
        }
        c.hasBreakSimple = oldHasBreakSimple;
        c.hasBreakDeep |= oldHasBreakDeep;
    }

    void setContinueLabel(String label) {
        terms[1].setContinueLabel(label);
    }

    void processOutput(OutputContext oc) {
        oc.cPrint("while (");
        terms[0].processOutput(oc);
        oc.cPrint(")");
        terms[1].processOutput(oc);
        outputBreakLabel(oc);
    }

    ExpressionType traceClassInit() {
        ForStatement.traceLoop(terms[0], terms[1], Empty.newTerm());
        return null;
    }
}
