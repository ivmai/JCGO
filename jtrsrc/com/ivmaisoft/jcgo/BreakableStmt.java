/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/BreakableStmt.java --
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
 * The abstract superclass for all "breakable" statements.
 */

abstract class BreakableStmt extends LexNode {

    private BreakableStmt currentLabelStmt;

    private TryStatement currentTry;

    private String breakLabel;

    private String continueLabel;

    BreakableStmt(Term a, Term c) {
        super(a, c);
    }

    BreakableStmt(Term a, Term b, Term c, Term d) {
        super(a, b, c, d);
    }

    BreakableStmt(Term a, Term b, Term c, Term d, Term e, Term f) {
        super(a, b, c, d, e, f);
    }

    final void processPassOneBegin(Context c) {
        currentTry = c.currentTry;
        currentLabelStmt = c.currentLabelStmt;
        c.currentLabelStmt = this;
    }

    final void processPassOneEnd(Context c) {
        assertCond(c.currentLabelStmt == this);
        c.currentLabelStmt = currentLabelStmt;
    }

    BreakableStmt find(String label) {
        return currentLabelStmt != null ? currentLabelStmt.find(label) : null;
    }

    final void makeBreakLabel(MethodDefinition currentMethod) {
        assertCond(currentMethod != null);
        if (breakLabel == null) {
            breakLabel = "jcgo_break" + currentMethod.nextLabelSuffix();
        }
    }

    final void makeContinueLabel(MethodDefinition currentMethod) {
        assertCond(currentMethod != null);
        if (continueLabel == null) {
            continueLabel = "jcgo_continue" + currentMethod.nextLabelSuffix();
            setContinueLabel(continueLabel);
        }
    }

    final void writeGoto(OutputContext oc, TryStatement ts, boolean isBreak) {
        String label = isBreak ? breakLabel : continueLabel;
        assertCond(label != null);
        TryStatement.outputFinallyGroup(ts, currentTry, oc, "goto " + label);
    }

    final void outputBreakLabel(OutputContext oc) {
        if (breakLabel != null) {
            oc.cPrint("\n");
            oc.cPrint(breakLabel);
            oc.cPrint(":;");
        }
    }
}
