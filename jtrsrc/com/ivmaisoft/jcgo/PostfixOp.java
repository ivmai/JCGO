/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/PostfixOp.java --
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
 * Grammar production for postfix increment and decrement operations.
 ** 
 * Formats: PostfixExpression INCREMENT PostfixExpression DECREMENT
 */

final class PostfixOp extends LexNode {

    private boolean isVoidExpr;

    PostfixOp(Term a, Term b) {
        super(a, b);
    }

    void processPass1(Context c) {
        c.lvalue = true;
        terms[0].processPass1(c);
        c.lvalue = false;
        int s0 = terms[0].exprType().objectSize();
        if (s0 < Type.BYTE || s0 > Type.DOUBLE) {
            fatalError(c, "Inappropriate type in unary expression");
        }
    }

    ExpressionType exprType() {
        return terms[0].exprType();
    }

    void setVoidExpression() {
        isVoidExpr = true;
    }

    void processOutput(OutputContext oc) {
        VariableDefinition v = terms[0].getVariable(true);
        boolean isVolatile = v != null && v.isThreadVolatile();
        ExpressionType exprType0 = terms[0].exprType();
        if (isVolatile
                || (oc.assignmentRightType == exprType0 && (oc.assignmentVar == null || oc.assignmentVar == v))) {
            assertCond(isVolatile || !isVoidExpr);
            int s0 = exprType0.objectSize();
            if (isVoidExpr) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[Type.VOID]);
                oc.cPrint(")");
            } else {
                if (s0 < Type.INT) {
                    oc.cPrint("(");
                    oc.cPrint(exprType0.castName());
                    oc.cPrint(")");
                }
                oc.cPrint("(");
            }
            int sym = terms[1].getSym();
            if (isVolatile) {
                oc.cPrint(sym == LexTerm.INCREMENT ? "JCGO_VLT_PREINC"
                        : "JCGO_VLT_PREDECR");
                oc.cPrint(Type.sig[s0]);
                oc.cPrint("(");
                terms[0].processOutput(oc);
                oc.cPrint(")");
            } else {
                terms[1].processOutput(oc);
                terms[0].processOutput(oc);
            }
            if (!isVoidExpr) {
                oc.cPrint(sym == LexTerm.INCREMENT ? "-" : "+");
                oc.cPrint("1)");
            }
        } else {
            terms[0].atomaryOutput(oc);
            terms[1].processOutput(oc);
        }
    }
}
