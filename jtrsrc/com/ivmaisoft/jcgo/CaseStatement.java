/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/CaseStatement.java --
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
 * Grammar production for the switch case statement.
 ** 
 * Formats: DEFAULT COLON [BlockStatements] CASE ConstantExpression COLON
 * [BlockStatements]
 */

final class CaseStatement extends LexNode {

    CaseStatement(Term c) {
        super(Empty.newTerm(), c);
    }

    CaseStatement(Term b, Term d) {
        super(b, d);
    }

    void processPass1(Context c) {
        BranchContext oldBranch = c.saveBranch();
        if (terms[0].notEmpty()) {
            terms[0].processPass1(c);
            int s0 = terms[0].exprType().objectSize();
            if (s0 < Type.BYTE || s0 > Type.INT) {
                fatalError(c, "Illegal type of switch expression");
            }
            if (!terms[0].isLiteral()
                    && !terms[0].isJavaConstant(c.currentClass)) {
                fatalError(c, "Case expression must be constant");
            }
        }
        boolean oldHasBreakSimple = c.hasBreakSimple;
        c.hasBreakSimple = false;
        boolean oldHasBreakDeep = c.hasBreakDeep;
        c.hasBreakDeep = false;
        boolean oldHasContinueSimple = c.hasContinueSimple;
        c.hasContinueSimple = false;
        boolean oldHasContinueDeep = c.hasContinueDeep;
        c.hasContinueDeep = false;
        terms[1].processPass1(c);
        if (!c.hasBreakSimple && !c.hasBreakDeep && !c.hasContinueSimple
                && !c.hasContinueDeep && terms[1].hasTailReturnOrThrow()) {
            c.swapBranch(oldBranch);
        } else {
            c.intersectBranch(oldBranch);
        }
        c.hasBreakSimple = oldHasBreakSimple;
        c.hasContinueSimple |= oldHasContinueSimple;
        c.hasContinueDeep |= oldHasContinueDeep;
        c.hasBreakDeep |= oldHasBreakDeep;
    }

    void processOutput(OutputContext oc) {
        if (terms[0].notEmpty()) {
            terms[0].requireLiteral();
            oc.cPrint(" case ");
            terms[0].processOutput(oc);
        } else {
            oc.cPrint(" default");
        }
        oc.cPrint(":");
        terms[1].processOutput(oc);
    }
}
