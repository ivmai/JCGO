/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/LeftBrace.java --
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
 * Grammar production for a block start ('{').
 */

final class LeftBrace extends LexNode {

    private LeftBrace outerScope;

    private boolean isConditional;

    private ObjQueue localVars;

    private String continueLabel;

    private ObjQueue classes;

    LeftBrace() {
    }

    void processPass0(Context c) {
        outerScope = c.localScope;
        c.localScope = this;
    }

    int tokenCount() {
        return 0;
    }

    void processPass1(Context c) {
        outerScope = c.localScope;
        isConditional = c.isConditional;
        c.localScope = this;
        c.isConditional = false;
    }

    LeftBrace outerScope() {
        return outerScope;
    }

    boolean isBoolAssign() {
        return isConditional;
    }

    void addLocalClass(ClassDefinition cd) {
        if (classes == null) {
            classes = new ObjQueue();
        }
        if (cd != null) {
            classes.addLast(cd);
        }
    }

    String resolveLocalClass(String name) {
        if (classes != null) {
            Enumeration en = classes.elements();
            while (en.hasMoreElements()) {
                ClassDefinition cd = (ClassDefinition) en.nextElement();
                if (name.equals(cd.id()))
                    return cd.name();
            }
        }
        return outerScope != null ? outerScope.resolveLocalClass(name) : null;
    }

    void setContinueLabel(String label) {
        assertCond(continueLabel == null);
        continueLabel = label;
    }

    String continueLabel() {
        return continueLabel;
    }

    void addLocal(VariableDefinition v) {
        if (localVars == null) {
            localVars = new ObjQueue();
        }
        localVars.addLast(v);
    }

    Enumeration localElements() {
        return localVars != null ? localVars.elements() : null;
    }

    void processOutput(OutputContext oc) {
        oc.cPrint("{");
        if (localVars != null) {
            VariableDefinition[] vars = new VariableDefinition[localVars
                    .countSize()];
            localVars.copyInto(vars);
            VariableDefinition.sortBySize(vars, vars.length);
            for (int i = 0; i < vars.length; i++) {
                vars[i].cdefinition(oc);
            }
        }
    }
}
