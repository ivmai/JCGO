/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ClassDeclaration.java --
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
 * Grammar production for a class definition.
 ** 
 * Format: CLASS ID [Extends] [Implements] ClassBody
 */

class ClassDeclaration extends LexNode {

    private ClassDefinition classDefn;

    ClassDeclaration(Term b, Term c, Term d, Term e) {
        super(b, c, d, e);
    }

    final void processPass0(Context c) {
        String id = terms[0].dottedName();
        String name;
        if (c.currentClass == null) {
            name = c.packageName + id;
        } else {
            name = c.localScope != null ? c.currentClass.nextLocalClassName(id)
                    : c.currentClass.name() + "$" + id;
        }
        if ((c.modifiers & (AccModifier.SYNCHRONIZED | AccModifier.VOLATILE
                | AccModifier.TRANSIENT | AccModifier.NATIVE)) != 0
                || ((c.modifiers & AccModifier.STATIC) != 0 && c.currentClass == null)) {
            fatalError(c, "Illegal modifier specified for class: " + name);
        }
        if ((c.modifiers & AccModifier.FINAL) != 0 && isInterface()) {
            fatalError(c, "An interface cannot be final: " + name);
        }
        classDefn = Main.dict.get(name);
        classDefn.definePass0(c, c.modifiers, id, terms[1], terms[2], terms[3],
                isInterface());
    }

    boolean isInterface() {
        return false;
    }

    final void processPass1(Context c) {
        assertCond(classDefn != null);
        classDefn.processPass1(c);
    }
}
