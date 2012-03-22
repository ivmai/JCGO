/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/UnaryExpression.java --
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
 * Grammar production for unary not, plus or minus expressions.
 ** 
 * Formats: PLUS UnaryExpression MINUS UnaryExpression BITNOT UnaryExpression
 * NOT UnaryExpression
 */

final class UnaryExpression extends LexNode {

    private ExpressionType exprType1;

    UnaryExpression(Term a, Term b) {
        super(a, b);
    }

    boolean isJavaConstant(ClassDefinition ourClass) {
        return terms[1].isJavaConstant(ourClass);
    }

    boolean handleAssertionsDisabled(ClassDefinition ourClass) {
        if (terms[0].getSym() != LexTerm.NOT
                || !Names.ASSERTIONSDISABLED.equals(terms[1].dottedName()))
            return false;
        VariableDefinition v = ourClass
                .getField(Names.ASSERTIONSDISABLED, null);
        if (v != null
                && (!v.isClassVariable() || !v.isFinalVariable()
                        || v.exprType().objectSize() != Type.BOOLEAN || v
                        .definingClass() != ourClass))
            return false;
        terms[1] = (new LexTerm(LexTerm.FALSE, "")).setLineInfoFrom(terms[1]);
        return true;
    }

    void processPass1(Context c) {
        if (exprType1 == null) {
            int sym = terms[0].getSym();
            terms[1].processPass1(c);
            exprType1 = terms[1].exprType();
            int s1 = exprType1.objectSize();
            if (sym != LexTerm.NOT ? s1 < Type.BYTE || s1 > Type.DOUBLE
                    || (s1 > Type.LONG && sym == LexTerm.BITNOT)
                    : s1 != Type.BOOLEAN) {
                fatalError(c, "Inappropriate type in unary expression");
            }
        }
    }

    void updateCondBranch(Context c, boolean forTrue) {
        terms[1].updateCondBranch(c, !forTrue);
    }

    ExpressionType exprType() {
        assertCond(exprType1 != null);
        int s1 = exprType1.objectSize();
        return s1 < Type.BYTE || s1 >= Type.INT ? exprType1
                : Main.dict.classTable[Type.INT];
    }

    boolean isLiteral() {
        return terms[1].isLiteral();
    }

    boolean isImmutable() {
        return terms[1].isImmutable();
    }

    boolean isSafeExpr() {
        return terms[1].isSafeExpr();
    }

    boolean isSafeWithThrow() {
        return terms[1].isSafeWithThrow();
    }

    MethodDefinition superMethodCall() {
        return terms[0].getSym() == LexTerm.PLUS ? terms[1].superMethodCall()
                : null;
    }

    ConstValue evaluateConstValue() {
        ConstValue value = terms[1].evaluateConstValue();
        if (value != null) {
            int sym = terms[0].getSym();
            if (sym == LexTerm.MINUS) {
                value = value.neg();
            } else if (sym == LexTerm.BITNOT || sym == LexTerm.NOT) {
                value = value.bitNot();
            }
        }
        return value;
    }

    boolean isFPZero() {
        return terms[0].getSym() == LexTerm.PLUS && terms[1].isFPZero();
    }

    int tokenCount() {
        int sym = terms[0].getSym();
        return terms[1].tokenCount()
                + (sym == LexTerm.PLUS || sym == LexTerm.NOT
                        || terms[1].evaluateConstValue() != null ? 0 : 1);
    }

    boolean isAtomary() {
        int sym = terms[0].getSym();
        return sym == LexTerm.PLUS || sym == LexTerm.MINUS;
    }

    void processOutput(OutputContext oc) {
        assertCond(exprType1 != null);
        int s1 = exprType1.objectSize();
        terms[1].insideArithOp();
        ConstValue value;
        int sym;
        if ((s1 == Type.LONG && (value = evaluateConstValue()) != null)
                || ((sym = terms[0].getSym()) == LexTerm.MINUS
                        && s1 == Type.INT
                        && (value = evaluateConstValue()) != null && value
                        .getIntValue() == ~(-1 >>> 1))) {
            oc.cPrint(value.stringOutput());
        } else {
            if (sym == LexTerm.PLUS || sym == LexTerm.MINUS) {
                oc.cPrint("(");
            }
            terms[0].processOutput(oc);
            if (s1 >= Type.BYTE && s1 < Type.INT) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[Type.INT]);
                oc.cPrint(")");
            }
            terms[1].atomaryOutput(oc);
            if (sym == LexTerm.PLUS || sym == LexTerm.MINUS) {
                oc.cPrint(")");
            }
        }
    }
}
