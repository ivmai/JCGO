/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/AssertionStatement.java --
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
 * Grammar production for the assertion statement.
 ** 
 * Formats: ASSERT ConditionalExpression SEMI ASSERT ConditionalExpression COLON
 * Argument SEMI
 */

final class AssertionStatement extends LexNode {

    private ClassDefinition topClinitClass;

    AssertionStatement(Term b, Term d) {
        super(b, (new InstanceCreation(new ClassOrIfaceType(
                Main.dict.get(Names.JAVA_LANG_ASSERTIONERROR)), d,
                Empty.newTerm())).setLineInfoFrom(d));
    }

    void processPass1(Context c) {
        topClinitClass = c.currentClass;
        assertCond(topClinitClass != null);
        ClassDefinition cd;
        while ((cd = topClinitClass.outerClass()) != null && !cd.isInterface()) {
            topClinitClass = cd;
        }
        topClinitClass.setHasAssertStmt();
        if (c.addAccessedClass(topClinitClass)) {
            topClinitClass = null;
        }
        c.insideAssertStmt = true;
        BranchContext oldBranch = c.saveBranch();
        terms[0].processPass1(c);
        if (terms[0].exprType().objectSize() != Type.BOOLEAN) {
            fatalError(c, "The condition expression must be of boolean type");
        }
        terms[0].updateCondBranch(c, false);
        terms[1].processPass1(c);
        c.intersectBranch(oldBranch);
        c.insideAssertStmt = false;
    }

    int tokenCount() {
        return 1;
    }

    boolean allowInline(int tokenLimit) {
        return true;
    }

    void discoverObjLeaks() {
        terms[0].discoverObjLeaks();
        terms[1].discoverObjLeaks();
        terms[1].setObjLeaks(null);
    }

    void processOutput(OutputContext oc) {
        if (topClinitClass != null) {
            topClinitClass.writeTrigClinit(oc);
        }
        oc.cPrint("JCGO_ASSERT_STMT(");
        terms[0].processOutput(oc);
        oc.cPrint(", ");
        terms[1].processOutput(oc);
        oc.cPrint(");");
    }

    ExpressionType traceClassInit() {
        if (topClinitClass != null) {
            topClinitClass.classTraceClassInit(true);
        }
        terms[0].traceClassInit();
        terms[1].traceClassInit();
        return null;
    }
}
