/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ArrayInitializer.java --
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
 * Grammar production for an array initializer. An array initializer is the code
 * that follow array definition and it may be either a literal one or built at
 * run-time.
 */

final class ArrayInitializer extends LexNode {

    static final LeftBrace IMMUTABLE_SCOPE = new LeftBrace();

    private ExpressionType exprType;

    private ClassDefinition ourClass;

    private boolean isStatic;

    private boolean allowLiteral;

    private int count;

    private LeftBrace noLeaksScope;

    private boolean isConditional;

    private boolean isWritable;

    private boolean insideAssertStmt;

    private ArrayLiteral arrayLiteral;

    private String stackObjCode;

    private boolean needsLocalVolatile;

    private int rcvr;

    ArrayInitializer(Term a) {
        super(a);
    }

    void processPass1(Context c) {
        if (exprType == null) {
            if ((c.modifiers & AccModifier.STATIC) != 0) {
                isStatic = true;
            }
            insideAssertStmt = c.insideAssertStmt;
            ourClass = c.currentClass;
            exprType = c.currentVarType;
            ExpressionType innerType = exprType.indirectedType();
            assertCond(innerType != null);
            c.currentVarType = innerType;
            int oldArrInitCount = c.arrInitCount;
            c.arrInitCount = 0;
            terms[0].processPass1(c);
            count = c.arrInitCount;
            c.arrInitCount = oldArrInitCount;
            c.currentVarType = exprType;
            innerType.signatureClass().markUsedForArray();
            if ((isStatic || exprType.signatureDimensions() == 1)
                    && terms[0].isLiteral()) {
                allowLiteral = true;
                if (isStatic) {
                    noLeaksScope = IMMUTABLE_SCOPE;
                    isConditional = true;
                }
            }
            if (!isStatic) {
                int s0 = innerType.objectSize();
                if (ArrayCreation.MAX_ONSTACK_SIZE
                        / Type.sizeInBytes[s0 < Type.CLASSINTERFACE ? s0
                                : Type.NULLREF] >= count) {
                    isConditional = c.isConditional;
                    noLeaksScope = c.localScope;
                }
            }
        }
    }

    ExpressionType exprType() {
        assertCond(exprType != null);
        return exprType;
    }

    boolean storeClassLiteralsGuess(ObjVector parmSig, boolean isActual) {
        assertCond(exprType != null);
        return exprType.objectSize() == Type.OBJECTARRAY
                && exprType.signatureDimensions() == 1
                && (isActual || exprType.signatureClass().name()
                        .equals(Names.JAVA_LANG_CLASS))
                && terms[0].storeClassLiteralsGuess(parmSig, isActual);
    }

    boolean isLiteral() {
        assertCond(exprType != null);
        return isStatic && allowLiteral;
    }

    boolean isSafeWithThrow() {
        return terms[0].isSafeWithThrow();
    }

    boolean isNotNull() {
        return true;
    }

    int tokenCount() {
        return allowLiteral ? 1 : terms[0].tokenCount() + 4;
    }

    int tokensExpandedCount() {
        return allowLiteral ? 0 : terms[0].tokensExpandedCount();
    }

    void allocRcvr(int[] curRcvrs) {
        assertCond(exprType != null);
        if (!allowLiteral) {
            rcvr = ++curRcvrs[Type.NULLREF];
        }
        terms[0].allocRcvr(curRcvrs);
    }

    void setStackObjVolatile() {
        needsLocalVolatile = true;
    }

    void setObjLeaks(VariableDefinition v) {
        noLeaksScope = VariableDefinition.addSetObjLeaksTerm(noLeaksScope, v,
                this, isConditional);
        if (noLeaksScope == null || v == VariableDefinition.WRITABLE_ARRAY_VAR) {
            isWritable = true;
        }
    }

    static String writeStackObjArrayDefn(OutputContext oc,
            ClassDefinition ourClass, ExpressionType innerType, int count,
            String cname, boolean insideAssertStmt, boolean needsLocalVolatile) {
        assertCond(ourClass != null);
        String stackObjName = oc.nextStackObjName();
        int s0 = innerType.objectSize();
        if (insideAssertStmt) {
            oc.cPrint("\n#ifdef JCGO_ASSERTION\010");
        }
        oc.cPrint(Main.dict.addArrayTypeDefn(s0, count, ourClass, null));
        oc.cPrint(" ");
        oc.cPrint(stackObjName);
        oc.cPrint(";");
        if (insideAssertStmt) {
            oc.cPrint("\n#endif\010");
        }
        return (cname != null ? "("
                + Type.cName[s0 < Type.CLASSINTERFACE ? s0
                        + Type.CLASSINTERFACE : Type.OBJECTARRAY]
                + ")JCGO_STACKOBJ" + (needsLocalVolatile ? "VLT" : "")
                + "_ARRCLONE" : s0 < Type.CLASSINTERFACE ? "("
                + Type.cName[s0 + Type.CLASSINTERFACE] + ")JCGO_STACKOBJ"
                + (needsLocalVolatile ? "VLT" : "") + "_PRIMARRNEW"
                : "JCGO_STACKOBJ" + (needsLocalVolatile ? "VLT" : "")
                        + "_OBJARRNEW")
                + "("
                + stackObjName
                + ", "
                + (cname != null ? cname : ClassDefinition.arrayVTableCName(
                        s0 < Type.CLASSINTERFACE ? s0 : Type.VOID,
                        innerType.signatureDimensions())
                        + (s0 < Type.CLASSINTERFACE ? "" : ", "
                                + innerType.signatureClass().getClassRefStr(
                                        false))
                        + ", "
                        + Integer.toString(count)) + ")";
    }

    private ArrayLiteral addArrayLiteral(boolean isReallyWritable,
            boolean isNotSharable) {
        if (arrayLiteral == null) {
            assertCond(ourClass != null && exprType != null && allowLiteral);
            terms[0].requireLiteral();
            exprType.signatureClass().setVTableUsed(false);
            ArrayLiteral literal = new ArrayLiteral(exprType.indirectedType(),
                    terms[0].stringOutput(), count, isReallyWritable);
            if (isNotSharable) {
                literal.setNotSharable();
            }
            arrayLiteral = Main.dict.addArrayLiteral(literal, ourClass, false);
        }
        return arrayLiteral;
    }

    String writeStackObjDefn(OutputContext oc, boolean needsLocalVolatile) {
        assertCond(exprType != null && !isStatic);
        return writeStackObjArrayDefn(oc, ourClass, exprType.indirectedType(),
                count, allowLiteral ? addArrayLiteral(false, false).cname()
                        : null, insideAssertStmt, needsLocalVolatile);
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        terms[0].writeStackObjs(oc, scopeTerm);
        if (noLeaksScope == scopeTerm) {
            assertCond(scopeTerm != null);
            stackObjCode = writeStackObjDefn(oc, needsLocalVolatile);
        }
    }

    ExpressionType writeStackObjRetCode(OutputContext oc) {
        assertCond(exprType != null && !isStatic && noLeaksScope == null);
        stackObjCode = MethodDefinition.STACKOBJ_RETNAME;
        outputArrayCreate(oc, true);
        return exprType();
    }

    boolean isAtomary() {
        return !allowLiteral || isStatic;
    }

    private void outputArrayCreate(OutputContext oc, boolean needsCast) {
        if (needsCast) {
            oc.cPrint("(");
            oc.cPrint(Type.cName[exprType.objectSize()]);
            oc.cPrint(")");
        }
        if (allowLiteral) {
            oc.cPrint("jcgo_arrayClone((");
            oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
            oc.cPrint(")JCGO_OBJREF_OF(");
            oc.cPrint(addArrayLiteral(false, false).cname());
            oc.cPrint("))");
        } else {
            oc.cPrint("jcgo_newArray(");
            oc.cPrint(exprType.signatureClass().getClassRefStr(false));
            oc.cPrint(", ");
            oc.cPrint(Integer.toString(exprType.signatureDimensions() - 1));
            oc.cPrint(", ");
            oc.cPrint(Integer.toString(count));
            oc.cPrint(")");
        }
    }

    void processOutput(OutputContext oc) {
        assertCond(exprType != null);
        if (allowLiteral) {
            if (stackObjCode != null) {
                oc.cPrint(stackObjCode);
            } else if (isStatic) {
                oc.cPrint(addArrayLiteral(isWritable, true).stringOutput());
            } else {
                outputArrayCreate(oc, true);
            }
        } else {
            oc.cPrint("(");
            if (rcvr > 0) {
                int oldArrInitCount = oc.arrInitCount;
                int oldArrInitLevel = oc.arrInitLevel;
                oc.arrInitCount = 0;
                oc.arrInitLevel = rcvr;
                String rcvrStr = OutputContext.getRcvrName(rcvr,
                        Type.CLASSINTERFACE);
                oc.cPrint(rcvrStr);
                oc.cPrint("= ");
                if (stackObjCode != null) {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
                    oc.cPrint(")");
                    oc.cPrint(stackObjCode);
                } else {
                    outputArrayCreate(oc, false);
                }
                oc.cPrint(",");
                terms[0].processOutput(oc);
                oc.arrayIndent();
                oc.cPrint("(");
                oc.cPrint(exprType.castName());
                oc.cPrint(")");
                oc.cPrint(rcvrStr);
                oc.arrInitLevel = oldArrInitLevel;
                oc.arrInitCount = oldArrInitCount;
            } else {
                oc.cPrint(LexTerm.NULL_STR);
            }
            oc.cPrint(")");
        }
    }
}
