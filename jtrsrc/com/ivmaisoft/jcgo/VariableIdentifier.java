/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/VariableIdentifier.java --
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
 * Grammar production for the identifier in a variable definition.
 ** 
 * Format: ID
 */

final class VariableIdentifier extends LexNode {

    private VariableDefinition v;

    VariableIdentifier(Term a) {
        super(a);
    }

    void processPass1(Context c) {
        if (v == null) {
            ClassDefinition ourClass = c.currentClass;
            String name = terms[0].dottedName();
            v = new VariableDefinition(
                    ourClass,
                    name,
                    c.modifiers,
                    c.typeClassDefinition.asExprType(c.typeDims),
                    c.varInitializer,
                    (c.modifiers & (AccModifier.STATIC | AccModifier.FINAL
                            | AccModifier.LOCALVAR | AccModifier.PARAMETER)) == AccModifier.FINAL
                            && ourClass.name().equals(Names.JAVA_LANG_STRING)
                            && name.equals(Names.fieldsOrderString[0]));
            if (v.isLocalOrParam()) {
                assertCond(c.currentMethod != null);
                if (!c.currentMethod.addLocalVariable(v)) {
                    fatalError(c, "Duplicate local variable definition: "
                            + name);
                }
                v.setLocalScope(c.localScope);
                v.initializerPassOne(c);
                if (v.hasInitializer()) {
                    v.setUnassigned(false);
                }
            } else {
                if (!v.isClassVariable()
                        && (ourClass.isInterface() || ourClass.name().equals(
                                Names.JAVA_LANG_OBJECT))) {
                    fatalError(c,
                            "An interface cannot contain instance field: "
                                    + name);
                }
                VariableDefinition old = ourClass.addField(v);
                if (old != null && old.definingClass() == ourClass) {
                    fatalError(c, "Duplicate field definition: " + name);
                }
            }
        }
    }

    String dottedName() {
        return terms[0].dottedName();
    }

    ExpressionType exprType() {
        assertCond(v != null);
        return v.exprType();
    }

    boolean isLiteral() {
        return v != null && v.isLiteral();
    }

    VariableDefinition getVariable(boolean allowInstance) {
        return v;
    }

    void processOutput(OutputContext oc) {
        assertCond(v != null);
        oc.cPrint(v.stringOutput(This.CNAME, 1, true));
    }

    void parameterOutput(OutputContext oc, boolean asArg, int type) {
        assertCond(v != null);
        v.parameterOutput(oc, asArg, type);
    }

    void storeSignature(ObjVector parmSig) {
        assertCond(v != null);
        parmSig.addElement(v.exprType());
    }

    void setTraceExprType(Enumeration en) {
        if (en != null) {
            boolean hasElements = en.hasMoreElements();
            assertCond(v != null && hasElements);
            v.setTraceExprType((ExpressionType) en.nextElement(), true);
        } else {
            assertCond(v != null);
            v.setTraceExprType(v.exprType(), true);
        }
    }
}
