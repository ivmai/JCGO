/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/CastExpression.java --
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
 * Grammar production for an expression cast.
 **
 * Formats: LPAREN Expression(Name) RPAREN UnaryExpressionNotPlusMinus LPAREN
 * Expression(PrimitiveType) RPAREN UnaryExpression LPAREN
 * Expression(TypeWithDims) RPAREN UnaryExpressionNotPlusMinus
 */

final class CastExpression extends LexNode {

    private ExpressionType exprType0;

    private ExpressionType exprType1;

    private ExpressionType actualType0;

    private ExpressionType actualType1;

    private boolean t1IsLiteral;

    private boolean t1IsLiteralSet;

    private boolean forceCheck;

    CastExpression(Term b, Term d) {
        super(b, d);
    }

    boolean isJavaConstant(ClassDefinition ourClass) {
        return terms[0].isJavaConstant(ourClass)
                && terms[1].isJavaConstant(ourClass);
    }

    void processPass1(Context c) {
        if (exprType0 == null) {
            processPassOneInner(c, true);
        }
    }

    Term processPassOneInner(Context c, boolean processSecondArg) {
        if ((c.forceVmExc & ClassDefinition.CLASS_CAST_EXC) != 0) {
            forceCheck = true;
        }
        Term t0 = terms[0];
        Term t1 = terms[1];
        int s1;
        if (t0.isType()) {
            t0.processPass1(c);
            exprType0 = t0.exprType();
            if (processSecondArg) {
                t1.processPass1(c);
            }
            exprType1 = t1.exprType();
            int s0 = exprType0.objectSize();
            s1 = exprType1.objectSize();
            if (s0 != Type.BOOLEAN ? (s0 < Type.BYTE || s0 > Type.VOID ? s1 >= Type.BOOLEAN
                    && s1 <= Type.VOID
                    : s0 == Type.VOID || s1 <= Type.BOOLEAN || s1 >= Type.VOID)
                    : s1 != Type.BOOLEAN) {
                fatalError(c, "Inappropriate type of casted expression");
            }
            actualType0 = exprType0;
            if (s1 == Type.CLASSINTERFACE || s1 == Type.OBJECTARRAY) {
                actualType0 = t1.actualExprType();
                if (ClassDefinition.isAssignableFrom(exprType0, actualType0,
                        c.forClass)) {
                    actualType1 = actualType0;
                } else {
                    actualType0 = exprType0;
                }
            } else if (s1 == Type.NULLREF
                    || (s0 == Type.CLASSINTERFACE && exprType0.signatureClass()
                            .superClass() == null)) {
                actualType0 = exprType1;
                actualType1 = exprType1;
            }
        } else {
            ClassDefinition cd;
            if (!t0.isName()
                    || (cd = c.resolveClass(t0.dottedName(), false, false)) == null) {
                fatalError(c, "Inappropriate type of casted expression");
                cd = Main.dict.get(Names.JAVA_LANG_OBJECT);
            }
            exprType0 = cd;
            if (processSecondArg) {
                t1.processPass1(c);
            }
            exprType1 = t1.exprType();
            s1 = exprType1.objectSize();
            if (s1 >= Type.BOOLEAN && s1 <= Type.VOID) {
                fatalError(c, "Inappropriate type of casted expression");
            }
            actualType1 = t1.actualExprType();
            actualType0 = exprType0;
            if (ClassDefinition.isAssignableFrom(exprType0, actualType1,
                    c.forClass)) {
                actualType0 = actualType1;
            }
            if (!cd.name().equals(Names.JAVA_LANG_OBJECT)) {
                MethodInvocation mcall = t1.getClassNewInstanceCall();
                if (mcall != null) {
                    cd.predefineClass(c.forClass);
                    cd.markAllDefinedClasses();
                    mcall.reflectConstructors(cd);
                    forceCheck = true;
                }
                MethodSignature msig = t1.getConstructorInstanceSign();
                if (msig != null) {
                    cd.predefineClass(c.forClass);
                    cd.reflectConstructors(false, msig.signatureString(), false);
                    forceCheck = true;
                }
            }
        }
        ClassDefinition cd = exprType0.signatureClass();
        if (cd.isInterface()) {
            cd.markForInstanceOf(exprType0.signatureDimensions() > 0);
        }
        VariableDefinition v = t1.getVariable(false);
        if (v != null) {
            c.setActualType(v, actualType0);
        }
        return this;
    }

    ConstValue evaluateConstValue() {
        assertCond(exprType0 != null);
        ConstValue constVal = terms[1].evaluateConstValue();
        return constVal != null ? constVal.castTo(exprType0.objectSize())
                : null;
    }

    boolean isFPZero() {
        return terms[1].isFPZero();
    }

    ExpressionType exprType() {
        assertCond(exprType0 != null);
        return exprType0;
    }

    ExpressionType actualExprType() {
        assertCond(exprType0 != null);
        return actualType0;
    }

    private boolean t1IsLiteral() {
        if (!t1IsLiteralSet) {
            t1IsLiteral = terms[1].isLiteral();
            t1IsLiteralSet = true;
        }
        return t1IsLiteral;
    }

    boolean isLiteral() {
        assertCond(exprType0 != null);
        int s1;
        return t1IsLiteral()
                && (actualType1 != null
                        || ((s1 = exprType1.objectSize()) != Type.FLOAT && s1 != Type.DOUBLE) || exprType0
                        .objectSize() >= s1);
    }

    boolean isImmutable() {
        assertCond(exprType0 != null);
        return terms[1].isImmutable()
                && (actualType0 == actualType1
                        || exprType1.objectSize() <= Type.DOUBLE || t1IsLiteral());
    }

    boolean isSafeExpr() {
        assertCond(exprType0 != null);
        return terms[1].isSafeExpr()
                && (actualType0 == actualType1
                        || exprType1.objectSize() <= Type.DOUBLE || t1IsLiteral());
    }

    boolean isSafeWithThrow() {
        return terms[1].isSafeWithThrow();
    }

    boolean isFieldAccessed(VariableDefinition v) {
        return terms[1].isFieldAccessed(v);
    }

    boolean isAnyLocalVarChanged(Term t) {
        return terms[1].isAnyLocalVarChanged(t);
    }

    boolean isNotNull() {
        return terms[1].isNotNull();
    }

    VariableDefinition getVariable(boolean allowInstance) {
        return terms[1].getVariable(allowInstance);
    }

    String strLiteralValueGuess() {
        assertCond(exprType0 != null);
        return exprType0 == Main.dict.get(Names.JAVA_LANG_OBJECT)
                || exprType0 == Main.dict.get(Names.JAVA_LANG_STRING) ? terms[1]
                .strLiteralValueGuess() : null;
    }

    ExpressionType classLiteralValGuess() {
        assertCond(exprType0 != null);
        return exprType0 == Main.dict.get(Names.JAVA_LANG_OBJECT)
                || exprType0 == Main.dict.get(Names.JAVA_LANG_CLASS) ? terms[1]
                .classLiteralValGuess() : null;
    }

    MethodInvocation getClassNewInstanceCall() {
        assertCond(exprType0 != null);
        return exprType0 == Main.dict.get(Names.JAVA_LANG_OBJECT) ? terms[1]
                .getClassNewInstanceCall() : null;
    }

    MethodSignature getConstructorInstanceSign() {
        assertCond(exprType0 != null);
        return exprType0 == Main.dict.get(Names.JAVA_LANG_OBJECT) ? terms[1]
                .getConstructorInstanceSign() : null;
    }

    void insideArithOp() {
        terms[1].insideArithOp();
    }

    void setStackObjVolatile() {
        terms[1].setStackObjVolatile();
    }

    void setObjLeaks(VariableDefinition v) {
        terms[1].setObjLeaks(v);
    }

    int tokenCount() {
        return terms[1].tokenCount() + 1;
    }

    static void outputFloatCast(OutputContext oc, int s0, int s1) {
        assertCond(s0 < Type.VOID);
        assertCond(s1 < Type.VOID);
        oc.cPrint(s1 > Type.FLOAT ? (s0 <= Type.INT ? "JCGO_JDOUBLE_TOJINT"
                : s0 < Type.FLOAT ? "JCGO_JDOUBLE_TOJLONG"
                        : "JCGO_JDOUBLE_TOJFLOAT")
                : s0 <= Type.INT ? "JCGO_JFLOAT_TOJINT" : "JCGO_JFLOAT_TOJLONG");
        oc.cPrint("(");
    }

    void processOutput(OutputContext oc) {
        assertCond(exprType0 != null);
        ClassDefinition cd;
        if (actualType1 != null) {
            oc.cPrint("(");
            oc.cPrint(exprType0.castName());
            oc.cPrint(")");
            if (actualType0 == actualType1 || t1IsLiteral()) {
                terms[1].atomaryOutput(oc);
                return;
            }
            cd = exprType0.signatureClass();
        } else {
            int s0 = exprType0.objectSize();
            int s1 = exprType1.objectSize();
            ConstValue value;
            if ((s0 == Type.LONG || s1 == Type.LONG)
                    && (value = evaluateConstValue()) != null) {
                oc.cPrint(value.stringOutput());
                return;
            }
            cd = null;
            if ((s1 != Type.FLOAT && s1 != Type.DOUBLE) || s0 >= s1
                    || s0 < Type.INT) {
                oc.cPrint("(");
                oc.cPrint(exprType0.castName());
                oc.cPrint(")");
                if (s1 >= Type.CLASSINTERFACE && !t1IsLiteral()) {
                    cd = exprType0.signatureClass();
                }
            }
            if (cd == null) {
                if ((s1 == Type.FLOAT || s1 == Type.DOUBLE) && s0 < s1) {
                    outputFloatCast(oc, s0, s1);
                    terms[1].processOutput(oc);
                    oc.cPrint(")");
                } else {
                    terms[1].processOutput(oc);
                }
                return;
            }
        }
        int dims;
        boolean full;
        if (exprType0.hasRealInstances()) {
            dims = exprType0.signatureDimensions();
            if (dims == 0) {
                cd = cd.getRealOurClass();
            }
            full = dims > 1 || cd.isInterface()
                    || (dims == 1 && cd.objectSize() == Type.CLASSINTERFACE);
        } else {
            dims = 0;
            full = false;
            if (!cd.used() || cd.isInterface()
                    || exprType0.signatureDimensions() > 0) {
                cd = Main.dict.classTable[Type.BOOLEAN];
            }
        }
        oc.cPrint(forceCheck ? "jcgo_checkCast" : "JCGO_CAST_OBJECT");
        if (!full) {
            oc.cPrint("0");
        }
        String cname = cd.castName();
        oc.cPrint("(OBJT_");
        oc.cPrint(cname);
        if (dims > 0 && !full) {
            oc.cPrint("+OBJT_jarray");
        }
        oc.cPrint(", MAXT_");
        oc.cPrint(cname);
        if (full) {
            oc.cPrint(", ");
            if (cd.isInterface()) {
                oc.cPrint("~");
            }
            oc.cPrint(Integer.toString(dims));
        } else if (dims > 0) {
            oc.cPrint("+OBJT_jarray");
        }
        oc.cPrint(", ");
        if (forceCheck) {
            oc.cPrint("(");
            oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
            oc.cPrint(")");
            terms[1].atomaryOutput(oc);
        } else {
            terms[1].processOutput(oc);
        }
        oc.cPrint(")");
    }

    ExpressionType traceClassInit() {
        ExpressionType curTraceType1 = terms[1].traceClassInit();
        if (!terms[0].isType()) {
            ClassDefinition cd = exprType0.signatureClass();
            MethodSignature msig;
            if (!cd.name().equals(Names.JAVA_LANG_OBJECT)
                    && (msig = terms[1].getConstructorInstanceSign()) != null) {
                cd.traceReflectedConstructor(false, msig.signatureString(),
                        false);
            }
        }
        if (curTraceType1 != null
                && (actualType0 == curTraceType1
                        || !actualType0.hasRealInstances() || !ClassDefinition
                        .isAssignableFrom(actualType0, curTraceType1, null))) {
            curTraceType1 = null;
        }
        return curTraceType1;
    }
}
