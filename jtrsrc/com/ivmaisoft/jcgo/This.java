/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/This.java --
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
 * Grammar production for "this".
 ** 
 * Formats: THIS ClassOrIfaceType DOT THIS
 */

class This extends LexNode {

    static final String CNAME = "This".toString();

    private String prefix;

    private boolean isOurClass;

    private ClassDefinition classDefn;

    private ExpressionType actualType;

    This() {
        super(Empty.newTerm());
    }

    This(Term a) {
        super(a);
    }

    void processPass1(Context c) {
        if (classDefn == null) {
            ClassDefinition ourClass = c.currentClass;
            assertCond(ourClass != null);
            c.typeClassDefinition = ourClass;
            terms[0].processPass1(c);
            classDefn = c.typeClassDefinition;
            if (c.currentMethod != null && c.currentMethod.isClassMethod()) {
                fatalError(c,
                        "'this' and 'super' cannot be used inside a static context");
            }
            if ((prefix = searchOuter(CNAME, ourClass)) == null) {
                fatalError(c, "Not an enclosing class is specified: "
                        + classDefn.name());
                prefix = CNAME;
            }
            if (classDefn == ourClass) {
                isOurClass = true;
            }
            if ((actualType = c.getActualType(VariableDefinition.THIS_VAR)) == null) {
                actualType = classDefn;
            }
        }
    }

    private String searchOuter(String curPrefix, ClassDefinition ourClass) {
        if (ourClass == classDefn)
            return curPrefix;
        String str = null;
        do {
            VariableDefinition outerV = ourClass.outerThisRef();
            if (outerV != null) {
                outerV.markUsed();
                String nextPrefix = outerV.stringOutput(curPrefix, 1, false);
                str = searchOuter(nextPrefix, ourClass.outerClass());
                if (str != null)
                    break;
            }
            if ((ourClass = ourClass.superClass()) == null)
                break;
            if (ourClass == classDefn)
                return "((" + ourClass.castName() + ")" + curPrefix + ")";
        } while (true);
        return str;
    }

    ExpressionType exprType() {
        return classDefn;
    }

    ExpressionType actualExprType() {
        assertCond(classDefn != null);
        return actualType;
    }

    final boolean isImmutable() {
        return true;
    }

    final boolean isNotNull() {
        return true;
    }

    final VariableDefinition getVariable(boolean allowInstance) {
        return VariableDefinition.THIS_VAR;
    }

    void setStackObjVolatile() {
        assertCond(classDefn != null);
        if (isOurClass) {
            if (Main.dict.ourMethod != null) {
                Main.dict.ourMethod.setThisStackObjVolatile();
            } else {
                classDefn.setInitThisStackObjVolatile();
            }
        }
    }

    final void setObjLeaks(VariableDefinition v) {
        assertCond(classDefn != null);
        if (isOurClass && (v == null || !v.addSetObjLeaksTerm(this))) {
            if (Main.dict.ourMethod != null) {
                Main.dict.ourMethod
                        .setThisObjLeak(v == VariableDefinition.RETURN_VAR);
            } else {
                classDefn.setInstanceInitLeaks();
            }
        }
    }

    int tokenCount() {
        return 1;
    }

    final boolean isAtomary() {
        return true;
    }

    final void processOutput(OutputContext oc) {
        assertCond(prefix != null);
        oc.cPrint(prefix);
    }

    ExpressionType traceClassInit() {
        return Main.dict.curTraceInfo.curThisClass();
    }
}
