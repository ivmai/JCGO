/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ConstrDeclaration.java --
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
 * Grammar production for a constructor definition.
 ** 
 * Format: ID LPAREN [FormalParamList] RPAREN [Throws] LBRACE [BlockStatements]
 * RBRACE
 */

final class ConstrDeclaration extends LexNode {

    private MethodDefinition md;

    ConstrDeclaration(Term a, Term c, Term e, Term g) {
        super(a, c, e, g);
    }

    void processPass0(Context c) {
        assertCond(c.currentClass != null);
        c.passZeroMethodDefnTerm = this;
        terms[3].processPass0(c);
        c.passZeroMethodDefnTerm = null;
    }

    void processPass1(Context c) {
        ClassDefinition aclass = c.currentClass;
        if (aclass.isInterface()) {
            fatalError(c, "Constructors are not allowed for interfaces");
        }
        String classname = aclass.name();
        String id = classname.substring(classname.lastIndexOf('.') + 1);
        if (!terms[0].dottedName().equals(aclass.id())) {
            fatalError(c, "Constructor name must match its class name: "
                    + classname);
        }
        if ((c.modifiers & (AccModifier.STATIC | AccModifier.SYNCHRONIZED
                | AccModifier.VOLATILE | AccModifier.TRANSIENT
                | AccModifier.NATIVE | AccModifier.FINAL | AccModifier.ABSTRACT | AccModifier.STRICT)) != 0) {
            fatalError(c, "Illegal modifier found for constructor: " + id);
        }
        Term paramList = Empty.newTerm();
        ObjVector locals = aclass.outerLocals(c.forClass);
        for (int i = locals.size() - 1; i >= 0; i--) {
            VariableDefinition v = aclass
                    .getOuterField(
                            ((VariableDefinition) locals.elementAt(i)).id(),
                            c.forClass);
            Term dims = Empty.newTerm();
            int j = v.exprType().signatureDimensions();
            while (j-- > 0) {
                dims = new DimSpec(dims);
            }
            Term t = new FormalParameter(
                    new AccModifier(AccModifier.SYNTHETIC),
                    new ClassOrIfaceType(v.exprType().signatureClass()), dims,
                    (new VariableIdentifier(new LexTerm(LexTerm.ID, v.id())))
                            .setLineInfoFrom(this), Empty.newTerm());
            t.setObjLeaks(null);
            paramList = FormalParamList.prepend(t, paramList);
        }
        paramList = terms[1].joinParamLists(paramList);
        if (!aclass.isStaticClass()) {
            Term t = new FormalParameter(
                    new AccModifier(AccModifier.SYNTHETIC),
                    new ClassOrIfaceType(aclass.outerClass()), Empty.newTerm(),
                    (new VariableIdentifier(new LexTerm(LexTerm.ID, aclass
                            .outerThisRef().id()))).setLineInfoFrom(this),
                    Empty.newTerm());
            t.setObjLeaks(null);
            paramList = FormalParamList.prepend(t, paramList);
        }
        if (aclass.addMethod(md = new MethodDefinition(c, "<init>", c.modifiers
                | (aclass.isStrictFP() ? AccModifier.STRICT : 0), aclass,
                paramList, terms[2], (new ConstructorBlock(terms[3]))
                        .setLineInfoFrom(this))) != null) {
            fatalError(c, "Duplicate constructor definition: " + id);
        }
        c.hasConstructor = true;
    }

    MethodDefinition superMethodCall() {
        return md;
    }
}
