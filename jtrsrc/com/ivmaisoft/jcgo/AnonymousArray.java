/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/AnonymousArray.java --
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
 * Grammar production for creating anonymous arrays.
 ** 
 * Format: NEW ClassOrIfaceType/PrimitiveType Dims ArrayInitializer
 */

final class AnonymousArray extends LexNode {

    private ExpressionType resType;

    AnonymousArray(Term b, Term c, Term d) {
        super(b, c, d);
    }

    void processPass1(Context c) {
        if (resType == null) {
            int oldTypeDims = c.typeDims;
            ClassDefinition oldTypeClassDefinition = c.typeClassDefinition;
            c.typeDims = 0;
            terms[1].processPass1(c);
            terms[0].processPass1(c);
            ClassDefinition cd = c.typeClassDefinition;
            cd.predefineClass(c.forClass);
            resType = cd.asExprType(c.typeDims);
            c.typeDims = oldTypeDims;
            c.typeClassDefinition = oldTypeClassDefinition;
            ExpressionType oldCurrentVarType = c.currentVarType;
            c.currentVarType = resType;
            terms[2].processPass1(c);
            c.currentVarType = oldCurrentVarType;
            cd.markUsed();
        }
    }

    ExpressionType exprType() {
        assertCond(resType != null);
        return resType;
    }

    boolean storeClassLiteralsGuess(ObjVector parmSig, boolean isActual) {
        assertCond(resType != null);
        return terms[2].storeClassLiteralsGuess(parmSig, isActual);
    }

    boolean isLiteral() {
        return terms[2].isLiteral();
    }

    boolean isSafeWithThrow() {
        return terms[2].isSafeWithThrow();
    }

    boolean isNotNull() {
        return true;
    }

    void setStackObjVolatile() {
        terms[2].setStackObjVolatile();
    }

    void setObjLeaks(VariableDefinition v) {
        terms[2].setObjLeaks(v);
    }

    int tokenCount() {
        return terms[2].tokenCount();
    }

    void allocRcvr(int[] curRcvrs) {
        terms[2].allocRcvr(curRcvrs);
    }

    boolean isAtomary() {
        return terms[2].isAtomary();
    }

    void processOutput(OutputContext oc) {
        assertCond(resType != null);
        terms[2].processOutput(oc);
    }

    ExpressionType traceClassInit() {
        terms[2].traceClassInit();
        return null;
    }
}
