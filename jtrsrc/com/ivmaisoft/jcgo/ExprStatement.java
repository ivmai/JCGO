/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ExprStatement.java --
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
 * Grammar production for the empty, expression or local variable declaration
 * statements.
 ** 
 * Formats: SEMI StatementExpression SEMI LocalVariableDecl SEMI ConstructorCall
 * SEMI
 */

final class ExprStatement extends LexNode {

    ExprStatement() {
        super(Empty.newTerm());
    }

    ExprStatement(Term a) {
        super(a);
    }

    void processPass1(Context c) {
        assertCond(c.currentMethod != null);
        if (c.currentMethod.id().equals("<clinit>")) {
            ObjQueue names = new ObjQueue();
            terms[0].assignedVarNames(names, false);
            if (names.contains(Names.ASSERTIONSDISABLED)) {
                assertCond(c.currentClass != null);
                VariableDefinition v = c.currentClass.getField(
                        Names.ASSERTIONSDISABLED, null);
                if (v != null && v.isClassVariable() && v.isFinalVariable()
                        && v.exprType().objectSize() == Type.BOOLEAN
                        && v.definingClass() == c.currentClass) {
                    terms[0] = Empty.newTerm();
                }
            }
        }
        terms[0].processPass1(c);
    }

    boolean isSwitchMapAssign(boolean isMethodCall) {
        return terms[0].isSwitchMapAssign(isMethodCall);
    }

    boolean allowInline(int tokenLimit) {
        return tokenCount() <= tokenLimit;
    }

    int tokenCount() {
        return terms[0].tokenCount();
    }

    MethodDefinition superMethodCall() {
        return terms[0].superMethodCall();
    }

    void processOutput(OutputContext oc) {
        terms[0].setVoidExpression();
        terms[0].processOutput(oc);
        oc.cPrint(";");
    }
}
