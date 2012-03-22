/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ArrayCreation.java --
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
 * Grammar production for creating new arrays.
 ** 
 * Formats: NEW ClassOrIfaceType/PrimitiveType DimsList NEW
 * ClassOrIfaceType/PrimitiveType DimsList [Dims]
 */

final class ArrayCreation extends LexNode {

    static final int MAX_LITERAL_SIZE = (new ConstValue(128)).getIntValue();

    static final int MAX_FINAL_LITERSIZE = (new ConstValue(512)).getIntValue();

    static final int MAX_ONSTACK_SIZE = (new ConstValue(512)).getIntValue();

    private ClassDefinition cd;

    private int dims;

    private int extraDims;

    private ClassDefinition ourClass;

    private ConstValue constVal1;

    private LeftBrace noLeaksScope;

    private boolean isConditional;

    private boolean allowLiteral;

    private boolean isWritable;

    private boolean insideAssertStmt;

    private String stackObjCode;

    private boolean needsLocalVolatile;

    ArrayCreation(Term b, Term c) {
        super(b, c, Empty.newTerm());
    }

    ArrayCreation(Term b, Term c, Term d) {
        super(b, c, d);
    }

    void processPass1(Context c) {
        if (cd == null) {
            insideAssertStmt = c.insideAssertStmt;
            int oldModifiers = c.modifiers;
            int oldTypeDims = c.typeDims;
            ClassDefinition oldTypeClassDefinition = c.typeClassDefinition;
            c.typeDims = 0;
            terms[1].processPass1(c);
            terms[0].processPass1(c);
            cd = c.typeClassDefinition;
            dims = c.typeDims;
            assertCond(dims > 0);
            cd.predefineClass(c.forClass);
            c.typeDims = 0;
            terms[2].processPass1(c);
            extraDims = c.typeDims;
            c.typeDims = oldTypeDims;
            c.typeClassDefinition = oldTypeClassDefinition;
            cd.markUsedForArray();
            ourClass = c.currentClass;
            if (dims == 1
                    && (constVal1 = terms[1].evaluateConstValue()) != null
                    && constVal1.getIntValue() >= 0) {
                int s0 = cd.objectSize();
                int elemSize = Type.sizeInBytes[s0 < Type.CLASSINTERFACE ? s0
                        : Type.NULLREF];
                if ((oldModifiers & AccModifier.STATIC) != 0
                        && ((oldModifiers & AccModifier.FINAL) != 0 ? MAX_FINAL_LITERSIZE
                                : MAX_LITERAL_SIZE)
                                / elemSize >= constVal1.getIntValue()) {
                    allowLiteral = true;
                    noLeaksScope = ArrayInitializer.IMMUTABLE_SCOPE;
                    isConditional = true;
                } else if (MAX_ONSTACK_SIZE / elemSize >= constVal1
                        .getIntValue()) {
                    isConditional = c.isConditional;
                    noLeaksScope = c.localScope;
                }
            }
        }
    }

    ExpressionType exprType() {
        assertCond(cd != null);
        return cd.asExprType(dims + extraDims);
    }

    boolean storeClassLiteralsGuess(ObjVector parmSig, boolean isActual) {
        assertCond(cd != null);
        return constVal1 != null && !constVal1.isNonZero() && extraDims == 0
                && cd.objectSize() == Type.CLASSINTERFACE
                && (isActual || cd.name().equals(Names.JAVA_LANG_CLASS));
    }

    boolean isLiteral() {
        assertCond(cd != null);
        return allowLiteral;
    }

    boolean isSafeWithThrow() {
        return terms[1].isSafeWithThrow();
    }

    boolean isNotNull() {
        return true;
    }

    int tokenCount() {
        return terms[1].tokenCount() + 2;
    }

    void setStackObjVolatile() {
        needsLocalVolatile = true;
    }

    void setObjLeaks(VariableDefinition v) {
        noLeaksScope = VariableDefinition.addSetObjLeaksTerm(noLeaksScope, v,
                this, isConditional);
        if ((noLeaksScope == null || v == VariableDefinition.WRITABLE_ARRAY_VAR)
                && allowLiteral) {
            isWritable = true;
        }
    }

    String writeStackObjDefn(OutputContext oc, boolean needsLocalVolatile) {
        assertCond(ourClass != null && dims == 1 && constVal1 != null
                && !allowLiteral);
        return ArrayInitializer.writeStackObjArrayDefn(oc, ourClass,
                cd.asExprType(extraDims), constVal1.getIntValue(), null,
                insideAssertStmt, needsLocalVolatile);
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        terms[1].writeStackObjs(oc, scopeTerm);
        if (noLeaksScope == scopeTerm) {
            assertCond(scopeTerm != null);
            stackObjCode = writeStackObjDefn(oc, needsLocalVolatile);
        }
    }

    ExpressionType writeStackObjRetCode(OutputContext oc) {
        assertCond(ourClass != null && dims == 1 && constVal1 != null
                && noLeaksScope == null && !allowLiteral);
        stackObjCode = MethodDefinition.STACKOBJ_RETNAME;
        outputNewArray(oc);
        return exprType();
    }

    boolean isAtomary() {
        return dims != 1 || allowLiteral;
    }

    private void outputNewArray(OutputContext oc) {
        int s0 = extraDims != 0 ? Type.CLASSINTERFACE : cd.objectSize();
        oc.cPrint("(");
        oc.cPrint(Type.cName[s0 < Type.CLASSINTERFACE ? s0
                + Type.CLASSINTERFACE : Type.OBJECTARRAY]);
        oc.cPrint(")jcgo_newArray(");
        oc.cPrint(cd.getClassRefStr(false));
        oc.cPrint(", ");
        oc.cPrint(Integer.toString(extraDims));
        terms[1].processOutput(oc);
        oc.cPrint(")");
    }

    void processOutput(OutputContext oc) {
        assertCond(cd != null);
        if (allowLiteral) {
            assertCond(stackObjCode == null && dims == 1 && constVal1 != null);
            cd.setVTableUsed(false);
            ExpressionType innerType = cd.asExprType(extraDims);
            int count = constVal1.getIntValue();
            StringBuffer sb = new StringBuffer();
            if (count > 0) {
                String element = innerType.objectSize() < Type.CLASSINTERFACE ? "("
                        + innerType.castName() + ")0, "
                        : LexTerm.NULL_STR + ", ";
                for (int i = 0; i < count; i++) {
                    sb.append(element);
                }
            }
            ArrayLiteral literal = new ArrayLiteral(innerType, sb.toString(),
                    count, isWritable);
            literal.setNotSharable();
            oc.cPrint(Main.dict.addArrayLiteral(literal, ourClass, false)
                    .stringOutput());
        } else if (stackObjCode != null) {
            assertCond(dims == 1);
            oc.cPrint(stackObjCode);
        } else if (dims == 1) {
            outputNewArray(oc);
        } else {
            oc.cPrint("(");
            terms[1].produceRcvr(oc);
            oc.cPrint(dims > 4 ? "jcgo_new16DArray(" : "jcgo_new4DArray(");
            oc.cPrint(cd.getClassRefStr(false));
            oc.cPrint(", ");
            oc.cPrint(Integer.toString(dims));
            oc.cPrint(", ");
            oc.cPrint(Integer.toString(extraDims));
            terms[1].processOutput(oc);
            int tail = 4 - dims;
            if (tail < 0) {
                tail += 16 - 4;
            }
            while (tail-- > 0) {
                oc.cPrint(", 0");
            }
            oc.cPrint("))");
        }
    }

    ExpressionType traceClassInit() {
        terms[1].traceClassInit();
        return null;
    }
}
