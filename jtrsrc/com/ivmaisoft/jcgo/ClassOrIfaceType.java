/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ClassOrIfaceType.java --
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
 * Grammar production for class (and interface) types.
 */

final class ClassOrIfaceType extends LexNode {

    private static final String STRING_SHORTNAME = Names.JAVA_LANG_STRING
            .substring(Names.JAVA_LANG_STRING.lastIndexOf('.') + 1);

    private Term nameTerm;

    private ClassDefinition cd;

    private boolean addedTo;

    ClassOrIfaceType(Term a) {
        nameTerm = a;
    }

    ClassOrIfaceType(ClassDefinition cd) {
        assertCond(cd != null);
        this.cd = cd;
    }

    boolean isJavaConstant(ClassDefinition ourClass) {
        if (cd == null) {
            assertCond(ourClass != null);
            String name = nameTerm.dottedName();
            if (!name.equals(STRING_SHORTNAME)
                    && !name.equals(Names.JAVA_LANG_STRING))
                return false;
            cd = ourClass.passOneContext().resolveClass(name, true, false);
            if (cd == null)
                return false;
        }
        return cd.isStringOrNull();
    }

    void processPass1(Context c) {
        if (cd == null) {
            cd = c.resolveClass(nameTerm.dottedName(), true, false);
        }
        c.typeClassDefinition = cd;
    }

    ExpressionType exprType() {
        assertCond(cd != null);
        return cd;
    }

    void addFieldsTo(ClassDefinition cd) {
        assertCond(this.cd != null);
        if (!addedTo) {
            this.cd.addFieldsTo(cd);
            addedTo = true;
        }
    }

    void storeSignature(ObjVector parmSig) {
        assertCond(cd != null);
        parmSig.addElement(cd);
    }

    boolean isType() {
        return true;
    }
}
