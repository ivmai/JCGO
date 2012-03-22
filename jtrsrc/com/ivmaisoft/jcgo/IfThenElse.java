/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/IfThenElse.java --
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
 * Grammar production for the if-then-else constructions.
 ** 
 * Formats: IF LPAREN Expression RPAREN Statement IF LPAREN Expression RPAREN
 * StatementNoShortIf ELSE Statement
 */

final class IfThenElse extends LexNode {

    private ConstValue constVal0;

    private boolean isAssertion;

    IfThenElse(Term c, Term e, Term g) {
        super(c, e.isBlock() ? e : new Block(e),
                !g.notEmpty() || g.isBlock() ? g : new Block(g));
    }

    void processPass1(Context c) {
        assertCond(c.currentClass != null);
        isAssertion = terms[0].handleAssertionsDisabled(c.currentClass);
        terms[0].processPass1(c);
        if (terms[0].exprType().objectSize() != Type.BOOLEAN) {
            fatalError(c, "The condition expression must be of boolean type");
        }
        constVal0 = terms[0].evaluateConstValue();
        if (constVal0 != null) {
            terms[constVal0.isNonZero() ? 1 : 2].processPass1(c);
        } else {
            MethodDefinition md = c.currentMethod;
            assertCond(md != null);
            ObjQueue unsetVars = new ObjQueue();
            Enumeration en = md.getLocalsNames();
            while (en.hasMoreElements()) {
                VariableDefinition v = md
                        .getLocalVar((String) en.nextElement());
                if (v.isUnassigned()) {
                    unsetVars.addLast(v);
                }
            }
            BranchContext oldBranch = c.saveBranch();
            terms[0].updateCondBranch(c, true);
            boolean oldHasBreakSimple = c.hasBreakSimple;
            c.hasBreakSimple = false;
            boolean oldHasBreakDeep = c.hasBreakDeep;
            c.hasBreakDeep = false;
            boolean oldHasContinueSimple = c.hasContinueSimple;
            c.hasContinueSimple = false;
            boolean oldHasContinueDeep = c.hasContinueDeep;
            c.hasContinueDeep = false;
            c.isConditional = true;
            terms[1].processPass1(c);
            Enumeration en2 = unsetVars.elements();
            while (en2.hasMoreElements()) {
                ((VariableDefinition) en2.nextElement()).setUnassigned(true);
            }
            oldBranch = c.swapBranch(oldBranch);
            terms[0].updateCondBranch(c, false);
            if (terms[2].notEmpty()) {
                boolean hasBreakOrContinue1 = c.hasBreakSimple
                        || c.hasBreakDeep || c.hasContinueSimple
                        || c.hasContinueDeep;
                oldHasContinueSimple |= c.hasContinueSimple;
                c.hasContinueSimple = false;
                oldHasContinueDeep |= c.hasContinueDeep;
                c.hasContinueDeep = false;
                oldHasBreakSimple |= c.hasBreakSimple;
                c.hasBreakSimple = false;
                oldHasBreakDeep |= c.hasBreakDeep;
                c.hasBreakDeep = false;
                c.isConditional = true;
                terms[2].processPass1(c);
                if (!c.hasBreakSimple && !c.hasBreakDeep
                        && !c.hasContinueSimple && !c.hasContinueDeep
                        && terms[2].hasTailReturnOrThrow()) {
                    c.swapBranch(oldBranch);
                } else if (hasBreakOrContinue1
                        || !terms[1].hasTailReturnOrThrow()) {
                    c.intersectBranch(oldBranch);
                }
            } else if (c.hasBreakSimple || c.hasBreakDeep
                    || c.hasContinueSimple || c.hasContinueDeep
                    || !terms[1].hasTailReturnOrThrow()) {
                c.intersectBranch(oldBranch);
            }
            c.hasContinueSimple |= oldHasContinueSimple;
            c.hasContinueDeep |= oldHasContinueDeep;
            c.hasBreakSimple |= oldHasBreakSimple;
            c.hasBreakDeep |= oldHasBreakDeep;
        }
    }

    int tokenCount() {
        return isAssertion ? 1 : constVal0 != null ? terms[constVal0
                .isNonZero() ? 1 : 2].tokenCount() : terms[0].tokenCount()
                + terms[1].tokenCount() + terms[2].tokenCount() + 1;
    }

    boolean hasTailReturnOrThrow() {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .hasTailReturnOrThrow() : terms[1].hasTailReturnOrThrow()
                && terms[2].hasTailReturnOrThrow();
    }

    boolean isReturnAtEnd(boolean allowBreakThrow) {
        return constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                .isReturnAtEnd(allowBreakThrow) : terms[1]
                .isReturnAtEnd(allowBreakThrow)
                && terms[2].isReturnAtEnd(allowBreakThrow);
    }

    void allocRcvr(int[] curRcvrs) {
        if (constVal0 == null) {
            terms[0].allocRcvr(curRcvrs);
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

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        if (constVal0 != null) {
            terms[constVal0.isNonZero() ? 1 : 2].writeStackObjs(oc, scopeTerm);
        } else {
            terms[0].writeStackObjs(oc, scopeTerm);
            terms[1].writeStackObjs(oc, scopeTerm);
            terms[2].writeStackObjs(oc, scopeTerm);
        }
    }

    boolean allowInline(int tokenLimit) {
        return isAssertion
                || (constVal0 != null ? terms[constVal0.isNonZero() ? 1 : 2]
                        .allowInline(tokenLimit) : !terms[2].notEmpty()
                        && (tokenLimit -= terms[0].tokenCount() + 1) >= 0
                        && terms[1].allowInline(tokenLimit));
    }

    void processOutput(OutputContext oc) {
        if (isAssertion) {
            oc.cPrint("\n#ifdef JCGO_ASSERTION\010");
        }
        if (constVal0 != null) {
            if (constVal0.isNonZero()) {
                terms[1].processOutput(oc);
            }
        } else {
            oc.cPrint("if (");
            terms[0].processOutput(oc);
            oc.cPrint(")");
            terms[1].processOutput(oc);
            if (terms[2].notEmpty()) {
                oc.cPrint("else");
            }
        }
        if (isAssertion) {
            oc.cPrint("\n#endif\010");
        }
        if (constVal0 == null || !constVal0.isNonZero()) {
            terms[2].processOutput(oc);
        }
    }

    ExpressionType traceClassInit() {
        if (constVal0 != null) {
            terms[constVal0.isNonZero() ? 1 : 2].traceClassInit();
        } else {
            terms[0].traceClassInit();
            terms[1].traceClassInit();
            terms[2].traceClassInit();
        }
        return null;
    }
}
