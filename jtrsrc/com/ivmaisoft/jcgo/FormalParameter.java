/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/FormalParameter.java --
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

import java.util.Enumeration;

/**
 * Grammar production for a formal parameter.
 ** 
 * Format: AccModifier/Empty PrimitiveType/ClassOrIfaceType [Dims]
 * VariableIdentifier [Dims]
 */

final class FormalParameter extends LexNode {

    private boolean hasLeaks;

    private boolean hasParamRet;

    private boolean isStackObjVolatile;

    private boolean isWritableArray;

    FormalParameter(Term a, Term b, Term c, Term d, Term e) {
        super(a, b, c, d, e);
    }

    Term joinParamLists(Term paramList) {
        if (!paramList.notEmpty())
            return this;
        return new FormalParamList(this, paramList);
    }

    Term getArgumentTerm(int index) {
        return index == 0 ? terms[3] : null;
    }

    void processPass1(Context c) {
        int oldModifiers = c.modifiers;
        c.modifiers = AccModifier.PARAMETER;
        c.varInitializer = Empty.newTerm();
        terms[0].processPass1(c);
        c.typeDims = 0;
        terms[2].processPass1(c);
        terms[4].processPass1(c);
        terms[1].processPass1(c);
        terms[3].processPass1(c);
        c.modifiers = oldModifiers;
    }

    ExpressionType exprType() {
        return terms[3].exprType();
    }

    void storeSignature(ObjVector parmSig) {
        terms[3].storeSignature(parmSig);
    }

    Term copyParamList(int[] skip) {
        if (--skip[0] >= skip[1] || skip[0] < 0)
            return Empty.newTerm();
        return new FormalParameter(new AccModifier(AccModifier.SYNTHETIC),
                terms[1], terms[2],
                (new VariableIdentifier(new LexTerm(LexTerm.ID, terms[3]
                        .dottedName()))).setLineInfoFrom(terms[3]), terms[4]);
    }

    Term makeArgumentList() {
        return new Argument(new Expression((new QualifiedName(new LexTerm(
                LexTerm.ID, terms[3].dottedName()))).setLineInfoFrom(terms[3])));
    }

    void discoverObjLeaks() {
        VariableDefinition v = terms[3].getVariable(false);
        assertCond(v != null);
        if (v.exprType().objectSize() >= Type.CLASSINTERFACE
                && !v.addSetObjLeaksTerm(this)) {
            hasLeaks = true;
        }
    }

    void setStackObjVolatile() {
        isStackObjVolatile = true;
    }

    void setObjLeaks(VariableDefinition v) {
        if (!hasLeaks) {
            if (v == VariableDefinition.WRITABLE_ARRAY_VAR) {
                isWritableArray = true;
            } else if (v == null || !v.addSetObjLeaksTerm(this)) {
                if (v == VariableDefinition.RETURN_VAR) {
                    hasParamRet = true;
                } else {
                    hasLeaks = true;
                    hasParamRet = false;
                }
            }
        }
    }

    boolean isBoolAssign() {
        return isWritableArray;
    }

    boolean isReturnAtEnd(boolean allowBreakThrow) {
        return allowBreakThrow ? hasLeaks : hasParamRet;
    }

    boolean hasTailReturnOrThrow() {
        return isStackObjVolatile;
    }

    int tokenCount() {
        return 1;
    }

    void processOutput(OutputContext oc) {
        assertCond(false);
    }

    void parameterOutput(OutputContext oc, boolean asArg, int type) {
        terms[3].parameterOutput(oc, asArg, type);
    }

    void setTraceExprType(Enumeration en) {
        terms[3].setTraceExprType(en);
    }
}
