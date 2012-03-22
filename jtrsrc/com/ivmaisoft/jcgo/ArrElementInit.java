/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ArrElementInit.java --
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
 * Grammar production for an element inside an array initializer.
 */

final class ArrElementInit extends LexNode {

    private ExpressionType exprType;

    ArrElementInit(Term a) {
        super(a);
    }

    void processPass1(Context c) {
        exprType = c.currentVarType;
        terms[0].processPass1(c);
        int s0 = terms[0].exprType().objectSize();
        c.arrInitCount++;
        int s1 = exprType.objectSize();
        if (s0 == Type.BOOLEAN ? s1 != Type.BOOLEAN
                : s0 >= Type.CLASSINTERFACE ? s1 < Type.CLASSINTERFACE
                        : s0 >= Type.LONG && s1 < s0) {
            fatalError(c, "Incompatible types in array initializer");
        }
    }

    boolean storeClassLiteralsGuess(ObjVector parmSig, boolean isActual) {
        ExpressionType exprType0;
        if (isActual) {
            exprType0 = terms[0].actualExprType();
            int s0 = exprType0.objectSize();
            if (s0 < Type.CLASSINTERFACE)
                return false;
            if (s0 == Type.CLASSINTERFACE) {
                ClassDefinition cd = exprType0.receiverClass();
                exprType0 = cd.name().equals(
                        Names.JAVAX_SWING_PLAF_COLORUIRESOURCE) ? cd
                        .superClass() : cd.mapToPrimType();
            }
        } else {
            exprType0 = terms[0].classLiteralValGuess();
            if (exprType0 == null)
                return false;
            if (exprType0.signatureDimensions() == 0) {
                exprType0 = exprType0.receiverClass();
            }
        }
        parmSig.addElement(exprType0);
        return true;
    }

    boolean isLiteral() {
        return terms[0].isLiteral();
    }

    boolean isSafeWithThrow() {
        return terms[0].isSafeWithThrow();
    }

    void discoverObjLeaks() {
        assertCond(exprType != null);
        terms[0].discoverObjLeaks();
        if (exprType.objectSize() >= Type.CLASSINTERFACE) {
            terms[0].setObjLeaks(null);
        }
    }

    int tokenCount() {
        return terms[0].tokenCount();
    }

    void processOutput(OutputContext oc) {
        int s0 = exprType.objectSize();
        if (oc.arrInitCount >= 0) {
            ConstValue constVal0;
            if (s0 >= Type.CLASSINTERFACE ? terms[0].exprType().objectSize() != Type.NULLREF
                    : (constVal0 = terms[0].evaluateConstValue()) == null
                            || constVal0.isNonZero()) {
                assertCond(oc.arrInitLevel > 0);
                oc.arrayIndent();
                oc.cPrint("JCGO_ARR_INTERNALACC(");
                oc.cPrint(Type.cName[s0 < Type.CLASSINTERFACE ? s0
                        : Type.CLASSINTERFACE]);
                oc.cPrint(", (");
                oc.cPrint(Type.cName[s0 < Type.BOOLEAN || s0 > Type.DOUBLE ? Type.OBJECTARRAY
                        : Type.CLASSINTERFACE + s0]);
                oc.cPrint(")");
                oc.cPrint(OutputContext.getRcvrName(oc.arrInitLevel,
                        Type.CLASSINTERFACE));
                oc.cPrint(", ");
                oc.cPrint(Integer.toString(oc.arrInitCount));
                oc.cPrint(")= ");
                if (s0 >= Type.CLASSINTERFACE
                        || s0 == Type.BOOLEAN
                        || terms[0].exprType().objectSize() != s0
                        || (s0 >= Type.BYTE && s0 < Type.INT && terms[0]
                                .isLiteral())) {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[s0 < Type.CLASSINTERFACE ? s0
                            : Type.CLASSINTERFACE]);
                    oc.cPrint(")");
                    terms[0].atomaryOutput(oc);
                } else {
                    terms[0].processOutput(oc);
                }
                oc.cPrint(",");
            }
            oc.arrInitCount++;
        } else {
            if (s0 >= Type.CLASSINTERFACE
                    || terms[0].exprType().objectSize() != s0) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[s0 < Type.CLASSINTERFACE ? s0
                        : Type.CLASSINTERFACE]);
                oc.cPrint(")");
                terms[0].atomaryOutput(oc);
            } else {
                terms[0].processOutput(oc);
            }
            oc.cPrint(", ");
        }
    }
}
