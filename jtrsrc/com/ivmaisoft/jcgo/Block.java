/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/Block.java --
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
 * Grammar production for a block.
 ** 
 * Format: LBRACE [BlockStatements] RBRACE
 */

class Block extends LexNode {

    boolean analysisDone;

    Block() {
        super(new LeftBrace(), Empty.newTerm(), new RightBrace());
    }

    Block(Term b) {
        super(new LeftBrace(), b, new RightBrace());
    }

    final boolean isBlock() {
        return true;
    }

    void processPass1(Context c) {
        if (!analysisDone) {
            analysisDone = true;
            terms[0].processPass1(c);
            boolean oldHasConstructor = c.hasConstructor;
            c.hasConstructor = false;
            terms[1].processPass1(c);
            if (c.hasConstructor) {
                fatalError(c, "Constructor invocation is not allowed here");
            }
            c.hasConstructor = oldHasConstructor;
            terms[2].processPass1(c);
        }
    }

    final void setContinueLabel(String label) {
        assertCond(analysisDone);
        terms[0].setContinueLabel(label);
    }

    final boolean isSwitchMapAssign(boolean isMethodCall) {
        return terms[1].isSwitchMapAssign(isMethodCall);
    }

    final boolean allowInline(int tokenLimit) {
        return terms[1].allowInline(tokenLimit);
    }

    final MethodDefinition superMethodCall() {
        return terms[1].superMethodCall();
    }

    final boolean hasTailReturnOrThrow() {
        return terms[1].hasTailReturnOrThrow();
    }

    final boolean isReturnAtEnd(boolean allowBreakThrow) {
        return terms[1].isReturnAtEnd(allowBreakThrow);
    }

    int tokenCount() {
        return terms[1].tokenCount();
    }

    final void allocRcvr(int[] curRcvrs) {
    }

    final void writeStackObjs(OutputContext oc, Term scopeTerm) {
        if (!terms[0].isBoolAssign()) {
            terms[1].writeStackObjs(oc, scopeTerm);
        }
    }

    final void processOutput(OutputContext oc) {
        assertCond(analysisDone);
        int[] curRcvrs = new int[Type.VOID];
        terms[1].allocRcvr(curRcvrs);
        terms[0].processOutput(oc);
        oc.writeRcvrsVar(curRcvrs);
        terms[1].writeStackObjs(oc, terms[0]);
        terms[1].processOutput(oc);
        terms[2].processOutput(oc);
    }

    final ExpressionType traceClassInit() {
        terms[1].traceClassInit();
        return null;
    }
}
