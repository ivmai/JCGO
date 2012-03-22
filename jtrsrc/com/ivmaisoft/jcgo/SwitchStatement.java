/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/SwitchStatement.java --
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
 * Grammar production for 'switch'.
 ** 
 * Format: SWITCH LPAREN Expression RPAREN LBRACE
 * SwitchBlockStatementGroups/Empty RBRACE
 */

final class SwitchStatement extends BreakableStmt {

    SwitchStatement(Term c, Term f) {
        super(c, new LeftBrace(), f, new RightBrace());
    }

    void processPass1(Context c) {
        TryStatement oldLastBreakableTry = c.lastBreakableTry;
        c.lastBreakableTry = c.currentTry;
        terms[0].processPass1(c);
        int s0 = terms[0].exprType().objectSize();
        if (s0 < Type.BYTE || s0 > Type.INT) {
            fatalError(c, "Illegal type of switch expression");
        }
        terms[1].processPass1(c);
        boolean oldBreakableHidden = c.breakableHidden;
        c.breakableHidden = false;
        processPassOneBegin(c);
        terms[2].processPass1(c);
        processPassOneEnd(c);
        c.breakableHidden = oldBreakableHidden;
        terms[3].processPass1(c);
        c.lastBreakableTry = oldLastBreakableTry;
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        terms[0].writeStackObjs(oc, scopeTerm);
        terms[2].writeStackObjs(oc, scopeTerm);
    }

    void processOutput(OutputContext oc) {
        oc.cPrint("switch (");
        terms[0].processOutput(oc);
        oc.cPrint(")");
        terms[1].processOutput(oc);
        if (terms[2].notEmpty()) {
            terms[2].writeStackObjs(oc, terms[1]);
            terms[2].processOutput(oc);
            oc.cPrint(";");
        }
        terms[3].processOutput(oc);
        outputBreakLabel(oc);
    }
}
