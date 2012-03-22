/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/MethodDeclaration.java --
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
 * Grammar production for a method definition.
 ** 
 * Formats: VoidType ID LPAREN [FormalParamList] RPAREN [Throws] MethodBody
 * PrimitiveType/ClassOrIfaceType [Dims] ID LPAREN [FormalParamList] RPAREN
 * [Dims] [Throws] MethodBody
 */

final class MethodDeclaration extends LexNode {

    private MethodDefinition md;

    MethodDeclaration(Term a, Term b, Term d, Term f, Term g) {
        super(a, Empty.newTerm(), b, d, Empty.newTerm(), f, g);
    }

    MethodDeclaration(Term a, Term b, Term c, Term e, Term g, Term h, Term i) {
        super(a, b, c, e, g, h, i);
    }

    void processPass0(Context c) {
        assertCond(c.currentClass != null);
        c.passZeroMethodDefnTerm = this;
        terms[6].processPass0(c);
        c.passZeroMethodDefnTerm = null;
    }

    void processPass1(Context c) {
        c.typeDims = 0;
        terms[1].processPass1(c);
        terms[4].processPass1(c);
        terms[0].processPass1(c);
        assertCond(c.typeDims == 0
                || c.typeClassDefinition.objectSize() != Type.VOID);
        String id = terms[2].dottedName();
        if ((c.modifiers & (AccModifier.VOLATILE | AccModifier.TRANSIENT)) != 0) {
            fatalError(c, "Illegal modifier specified for method: " + id);
        }
        if (((c.modifiers & AccModifier.ABSTRACT) != 0 || c.currentClass
                .isInterface())
                && (c.modifiers & (AccModifier.PRIVATE | AccModifier.STATIC
                        | AccModifier.SYNCHRONIZED | AccModifier.NATIVE
                        | AccModifier.FINAL | AccModifier.STRICT)) != 0) {
            fatalError(c, "Illegal modifier found for an abstract method: "
                    + id);
        }
        if (c.currentClass.isInterface()
                && (c.modifiers & AccModifier.PROTECTED) != 0) {
            fatalError(c, "An interface method cannot be protected: " + id);
        }
        if ((c.modifiers & AccModifier.STATIC) != 0
                && !c.currentClass.isStaticClass()) {
            fatalError(c, "An inner class cannot have a static method: " + id);
        }
        MethodDefinition md2 = c.currentClass
                .addMethod(md = new MethodDefinition(c, id, c.modifiers,
                        c.typeClassDefinition.asExprType(c.typeDims), terms[3],
                        terms[5], terms[6]));
        if (md2 != null && !md2.isAbstract()) {
            fatalError(c, "Duplicate method definition: " + id);
        }
    }

    MethodDefinition superMethodCall() {
        return md;
    }
}
