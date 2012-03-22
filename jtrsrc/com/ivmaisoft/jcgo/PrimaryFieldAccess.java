/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/PrimaryFieldAccess.java --
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
 * Grammar production for a primary field access.
 ** 
 * Formats: Primary DOT ID Super DOT ID
 */

final class PrimaryFieldAccess extends LexNode {

    private VariableDefinition fieldVar;

    private boolean lvalue;

    private boolean t0IsSafeExpr;

    private boolean t0IsSafeExprSet;

    private boolean t0IsNotNull;

    private boolean t0IsNotNullSet;

    private boolean forceCheck;

    private boolean isClinitSafe;

    private boolean isFieldNotNull;

    PrimaryFieldAccess(Term a, Term c) {
        super(a, c);
    }

    void processPass1(Context c) {
        if (fieldVar == null) {
            assertCond(c.currentClass != null);
            if ((c.forceVmExc & ClassDefinition.NULL_PTR_EXC) != 0) {
                forceCheck = true;
            }
            lvalue = c.lvalue;
            boolean setOnly = c.setOnly;
            c.lvalue = false;
            c.setOnly = false;
            terms[0].processPass1(c);
            ClassDefinition aclass = terms[0].exprType().receiverClass();
            String name = terms[1].dottedName();
            fieldVar = aclass.getField(name, c.forClass);
            if (fieldVar != null) {
                fieldVar.markUsed(lvalue, setOnly && t0IsSafeExpr());
                if (lvalue) {
                    fieldVar.setChangedSpecial();
                }
                isFieldNotNull = c.isVarNotNull(fieldVar);
                if (fieldVar.isClassVariable()) {
                    isClinitSafe = c.addAccessedClassField(fieldVar);
                    if (!isClinitSafe) {
                        fieldVar.markUsed();
                    }
                } else {
                    c.addAccessedClass(terms[0].actualExprType()
                            .receiverClass());
                    VariableDefinition v = terms[0].getVariable(false);
                    if (v != null) {
                        c.setVarNotNull(v);
                        if (lvalue && v != VariableDefinition.THIS_VAR) {
                            fieldVar.markUsed();
                        }
                        if (c.currentTry != null) {
                            c.currentTry.setVarAccessed(v);
                        }
                    }
                }
            } else {
                fatalError(c, "Undefined field: " + aclass.name() + "." + name);
            }
        }
    }

    ExpressionType exprType() {
        return fieldVar != null ? fieldVar.exprType()
                : Main.dict.classTable[Type.INT];
    }

    ExpressionType actualExprType() {
        return fieldVar != null ? fieldVar.actualExprType() : exprType();
    }

    private boolean t0IsSafeExpr() {
        if (!t0IsSafeExprSet) {
            t0IsSafeExpr = terms[0].isSafeExpr();
            t0IsSafeExprSet = true;
        }
        return t0IsSafeExpr;
    }

    private boolean t0IsNotNull() {
        if (!t0IsNotNullSet) {
            t0IsNotNull = terms[0].isNotNull();
            t0IsNotNullSet = true;
        }
        return t0IsNotNull;
    }

    boolean isLiteral() {
        return fieldVar != null && fieldVar.isLiteral()
                && fieldVar.isClassVariable() && t0IsSafeExpr();
    }

    boolean isImmutable() {
        return fieldVar != null
                && (fieldVar.isClassVariable() ? fieldVar.isImmutable(fieldVar
                        .definingClass())
                        && t0IsSafeExpr()
                        && (isClinitSafe || fieldVar.isLiteral() || !fieldVar
                                .definingClass().classInitializerNotCalledYet())
                        : fieldVar.isImmutable(terms[0].exprType()
                                .receiverClass())
                                && terms[0].isImmutable()
                                && t0IsNotNull());
    }

    boolean isSafeExpr() {
        return fieldVar != null
                && t0IsSafeExpr()
                && (fieldVar.isClassVariable() ? isClinitSafe
                        || fieldVar.isLiteral()
                        || !fieldVar.definingClass()
                                .classInitializerNotCalledYet() : t0IsNotNull());
    }

    boolean isSafeWithThrow() {
        return terms[0].isSafeWithThrow();
    }

    boolean isFieldAccessed(VariableDefinition v) {
        return (v != null ? fieldVar == v : !isImmutable())
                || terms[0].isFieldAccessed(v);
    }

    boolean isNotNull() {
        if (fieldVar == null)
            return false;
        if (!isFieldNotNull) {
            if (!fieldVar.isNotNull())
                return false;
            isFieldNotNull = true;
        }
        return true;
    }

    VariableDefinition getVariable(boolean allowInstance) {
        return fieldVar != null
                && (allowInstance || fieldVar.isClassVariable() || terms[0]
                        .getVariable(false) == VariableDefinition.THIS_VAR) ? fieldVar
                : null;
    }

    String strLiteralValueGuess() {
        return fieldVar != null ? fieldVar.strLiteralValueGuess() : null;
    }

    ExpressionType classLiteralValGuess() {
        return fieldVar != null ? fieldVar.classLiteralValGuess() : null;
    }

    MethodInvocation getClassNewInstanceCall() {
        return fieldVar != null ? fieldVar.getClassNewInstanceCall() : null;
    }

    MethodSignature getConstructorInstanceSign() {
        return fieldVar != null ? fieldVar.getConstructorInstanceSign() : null;
    }

    void setObjLeaks(VariableDefinition v) {
        if (fieldVar != null) {
            fieldVar.setWritableArray(v);
        }
    }

    int tokenCount() {
        return (fieldVar != null && fieldVar.used() ? 1 : 0)
                + terms[0].tokenCount();
    }

    int tokensExpandedCount() {
        return fieldVar != null && fieldVar.isClassVariable() ? fieldVar
                .tokensExpandedCount() : terms[0].tokensExpandedCount();
    }

    void processOutput(OutputContext oc) {
        if (fieldVar != null && fieldVar.isClassVariable()) {
            boolean isNotSafe = false;
            if (!t0IsSafeExpr()) {
                oc.cPrint("(");
                terms[0].processOutput(oc);
                oc.cPrint(", ");
                isNotSafe = true;
                if (lvalue) {
                    oc.cPrint("&");
                }
            }
            oc.cPrint(fieldVar.stringOutputForStatic(isClinitSafe, lvalue));
            if (isNotSafe) {
                oc.cPrint(")");
                if (lvalue) {
                    oc.cPrint("[0]");
                }
            }
        } else {
            oc.cPrint(fieldVar != null ? fieldVar.stringOutput(terms[0]
                    .stringOutput(), t0IsNotNull() ? 1 : forceCheck ? -1 : 0,
                    lvalue) : VariableDefinition.UNKNOWN_NAME);
        }
    }

    ExpressionType traceClassInit() {
        terms[0].traceClassInit();
        return fieldVar != null ? fieldVar.traceClassInit() : null;
    }
}
