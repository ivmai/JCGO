/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/InstanceOf.java --
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
 * Grammar production for the instanceof statement.
 ** 
 * Format: RelationalExpression INSTANCEOF PrimitiveType/ClassOrIfaceType [Dims]
 */

final class InstanceOf extends LexNode {

    private int dims;

    private ClassDefinition cd;

    InstanceOf(Term a, Term c, Term d) {
        super(a, c, d);
    }

    void processPass1(Context c) {
        if (cd == null) {
            terms[0].processPass1(c);
            int s0 = terms[0].exprType().objectSize();
            if (s0 >= Type.BOOLEAN && s0 <= Type.VOID) {
                fatalError(c, "Illegal type of expression for 'instanceof'");
            }
            ClassDefinition oldTypeClassDefinition = c.typeClassDefinition;
            int oldTypeDims = c.typeDims;
            c.typeDims = 0;
            terms[2].processPass1(c);
            terms[1].processPass1(c);
            cd = c.typeClassDefinition;
            dims = c.typeDims;
            c.typeDims = oldTypeDims;
            c.typeClassDefinition = oldTypeClassDefinition;
            if (dims == 0 && cd.objectSize() <= Type.VOID) {
                fatalError(c, "Primitive type is not allowed in 'instanceof'");
                return;
            }
            ExpressionType actualType0 = terms[0].actualExprType();
            if (cd.isAssignableFrom(actualType0.signatureClass(), dims
                    - actualType0.signatureDimensions(), c.forClass)) {
                cd = Main.dict.get(Names.JAVA_LANG_OBJECT);
                dims = 0;
            }
            cd.markForInstanceOf(dims > 0);
        }
    }

    void updateCondBranch(Context c, boolean forTrue) {
        assertCond(cd != null);
        VariableDefinition v;
        if (forTrue && (v = terms[0].getVariable(false)) != null) {
            ExpressionType actualType0 = c.getActualType(v);
            if (actualType0 == null) {
                actualType0 = v.actualExprType();
            }
            ExpressionType exprType = cd.asExprType(dims);
            if (!ClassDefinition.isAssignableFrom(exprType, actualType0,
                    c.forClass)) {
                c.setActualType(v, exprType);
            }
            c.setVarNotNull(v);
        }
    }

    ExpressionType exprType() {
        return Main.dict.classTable[Type.BOOLEAN];
    }

    boolean isImmutable() {
        return terms[0].isImmutable();
    }

    boolean isSafeExpr() {
        return terms[0].isSafeExpr();
    }

    boolean isSafeWithThrow() {
        return terms[0].isSafeWithThrow();
    }

    int tokenCount() {
        return terms[0].tokenCount() + 2;
    }

    boolean isAtomary() {
        return true;
    }

    void processOutput(OutputContext oc) {
        assertCond(cd != null);
        if (dims == 0 && cd.name().equals(Names.JAVA_LANG_OBJECT)) {
            oc.cPrint("(");
            terms[0].atomaryOutput(oc);
            oc.cPrint("!= ");
            oc.cPrint(LexTerm.NULL_STR);
        } else {
            boolean full = false;
            if (dims > 0) {
                if (cd.used()) {
                    full = dims > 1 || cd.isInterface()
                            || cd.objectSize() == Type.CLASSINTERFACE;
                } else {
                    cd = Main.dict.classTable[Type.BOOLEAN];
                    dims = 0;
                }
            } else {
                if (cd.hasRealInstances()) {
                    cd = cd.getRealOurClass();
                    full = cd.isInterface();
                } else if (!cd.used() || cd.isInterface()) {
                    cd = Main.dict.classTable[Type.BOOLEAN];
                }
            }
            boolean simple = false;
            if (!full && !cd.hasInstantatedSubclasses(false)
                    && terms[0].isNotNull()) {
                oc.cPrint("(JCGO_METHODS_OF(");
                terms[0].processOutput(oc);
                oc.cPrint(")->jcgo_typeid== ");
                simple = true;
            } else {
                oc.cPrint("jcgo_instanceOf");
                if (!full) {
                    oc.cPrint("0");
                }
                oc.cPrint("(");
            }
            String cname = cd.castName();
            oc.cPrint("OBJT_");
            oc.cPrint(cname);
            if (dims > 0 && !full) {
                oc.cPrint("+OBJT_jarray");
            }
            if (!simple) {
                oc.cPrint(", MAXT_");
                oc.cPrint(cname);
                if (full) {
                    oc.cPrint(", ");
                    if (cd.isInterface()) {
                        oc.cPrint("~");
                    }
                    oc.cPrint(Integer.toString(dims));
                } else if (dims > 0) {
                    oc.cPrint("+OBJT_jarray");
                }
                oc.cPrint(", (");
                oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
                oc.cPrint(")");
                terms[0].atomaryOutput(oc);
            }
        }
        oc.cPrint(")");
    }

    ExpressionType traceClassInit() {
        terms[0].traceClassInit();
        return null;
    }
}
