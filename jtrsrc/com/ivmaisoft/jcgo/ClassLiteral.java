/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ClassLiteral.java --
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
 * Grammar production for a class literal reference.
 ** 
 * Formats: ClassOrIfaceType DOT CLASS TypeWithDims DOT CLASS
 * PrimitiveType/VoidType DOT CLASS
 */

final class ClassLiteral extends LexNode {

    private ExpressionType classLiteralValue;

    private MethodDefinition md;

    ClassLiteral(Term a) {
        super(a);
    }

    void processPass1(Context c) {
        if (classLiteralValue == null) {
            terms[0].processPass1(c);
            ExpressionType exprType0 = terms[0].exprType();
            ClassDefinition cd = exprType0.signatureClass();
            cd.predefineClass(c.forClass);
            cd.markUsed();
            classLiteralValue = exprType0.signatureDimensions() > 0 ? exprType0
                    : cd.asExactClassType();
            if (exprType0.objectSize() == Type.OBJECTARRAY) {
                cd = Main.dict.get(Names.JAVA_LANG_VMCLASS);
                cd.predefineClass(c.forClass);
                MethodDefinition md = cd.getMethod(Names.SIGN_ARRAYCLASSOF0X);
                if (md != null && md.isClassMethod()) {
                    md.markUsed(null);
                    this.md = md;
                }
            }
            classLiteralValue.signatureClass().setVTableUsed(false);
        }
    }

    ExpressionType exprType() {
        assertCond(classLiteralValue != null);
        return Main.dict.get(Names.JAVA_LANG_CLASS);
    }

    ExpressionType classLiteralValGuess() {
        assertCond(classLiteralValue != null);
        return classLiteralValue;
    }

    boolean isLiteral() {
        assertCond(classLiteralValue != null);
        return classLiteralValue.objectSize() != Type.OBJECTARRAY;
    }

    boolean isImmutable() {
        return true;
    }

    boolean isNotNull() {
        return true;
    }

    int tokenCount() {
        return 1;
    }

    boolean isAtomary() {
        return true;
    }

    void processOutput(OutputContext oc) {
        assertCond(classLiteralValue != null);
        int s0 = classLiteralValue.objectSize();
        if (s0 != Type.OBJECTARRAY) {
            oc.cPrint(classLiteralValue.signatureClass().getClassRefStr(
                    s0 > Type.CLASSINTERFACE));
        } else {
            oc.cPrint(md != null ? md.routineCName()
                    : MethodDefinition.UNKNOWN_NAME);
            Main.dict.normalCalls++;
            oc.cPrint("(");
            oc.cPrint(classLiteralValue.signatureClass().getClassRefStr(false));
            oc.cPrint(", ");
            oc.cPrint(Integer.toString(classLiteralValue.signatureDimensions()));
            oc.cPrint(")");
        }
    }

    ExpressionType traceClassInit() {
        assertCond(classLiteralValue != null);
        Main.dict.addDynClassToTrace(classLiteralValue.signatureClass());
        if (md != null) {
            md.methodTraceClassInit(false, null, null);
        }
        return null;
    }
}
