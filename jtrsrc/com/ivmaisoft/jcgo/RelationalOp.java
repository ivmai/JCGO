/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/RelationalOp.java --
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
 * Grammar production for relational operators.
 ** 
 * Formats: EqualityExpression NE RelationalExpression EqualityExpression EQ
 * RelationalExpression RelationalExpression GE ShiftExpression
 * RelationalExpression LE ShiftExpression RelationalExpression GT
 * ShiftExpression RelationalExpression LT ShiftExpression
 */

final class RelationalOp extends LexNode {

    private ExpressionType exprType0;

    private ExpressionType exprType2;

    private int rcvr;

    private ConstValue constVal;

    RelationalOp(Term a, Term b, Term c) {
        super(a, b, c);
    }

    boolean isJavaConstant(ClassDefinition ourClass) {
        return terms[0].isJavaConstant(ourClass)
                && terms[2].isJavaConstant(ourClass);
    }

    void processPass1(Context c) {
        if (exprType0 == null) {
            BranchContext oldBranch = c.saveBranch();
            terms[0].processPass1(c);
            oldBranch = c.swapBranch(oldBranch);
            exprType0 = terms[0].exprType();
            int sym = terms[1].getSym();
            terms[2].processPass1(c);
            c.unionBranch(oldBranch);
            exprType2 = terms[2].exprType();
            int s0 = exprType0.objectSize();
            int s2 = exprType2.objectSize();
            if (s0 < Type.CLASSINTERFACE && s0 != Type.NULLREF ? (s0 != Type.BOOLEAN ? s0 == Type.VOID
                    || s2 <= Type.BOOLEAN || s2 > Type.DOUBLE
                    : s2 != Type.BOOLEAN)
                    : (sym != LexTerm.EQ && sym != LexTerm.NE)
                            || (s2 >= Type.BOOLEAN && s2 <= Type.VOID)) {
                fatalError(c,
                        "Inappropriate type of expressions for comparison");
            }
            ConstValue constVal0 = terms[0].evaluateConstValue();
            if (constVal0 != null
                    && (constVal = constVal0.comparisonOp(
                            terms[2].evaluateConstValue(), sym)) == null
                    && (sym == LexTerm.EQ || sym == LexTerm.NE)) {
                Term t = terms[0];
                terms[0] = terms[2];
                terms[2] = t;
                ExpressionType exprType = exprType0;
                exprType0 = exprType2;
                exprType2 = exprType;
            }
        }
    }

    void updateCondBranch(Context c, boolean forTrue) {
        int sym;
        if (constVal == null
                && ((sym = terms[1].getSym()) == LexTerm.EQ || sym == LexTerm.NE)) {
            int s0 = exprType0.objectSize();
            if (sym != LexTerm.EQ) {
                forTrue = !forTrue;
            }
            if (s0 == Type.BOOLEAN) {
                ConstValue constVal2 = terms[2].evaluateConstValue();
                if (constVal2 != null) {
                    terms[0].updateCondBranch(c,
                            constVal2.isNonZero() == forTrue);
                }
            } else if (!forTrue
                    && (s0 >= Type.CLASSINTERFACE || s0 == Type.NULLREF)) {
                VariableDefinition v = null;
                if (terms[2].actualExprType().objectSize() == Type.NULLREF) {
                    v = terms[0].getVariable(false);
                } else if (terms[0].actualExprType().objectSize() == Type.NULLREF) {
                    v = terms[2].getVariable(false);
                }
                if (v != null) {
                    c.setVarNotNull(v);
                }
            }
        }
    }

    ConstValue evaluateConstValue() {
        return constVal;
    }

    ExpressionType exprType() {
        return Main.dict.classTable[Type.BOOLEAN];
    }

    boolean isLiteral() {
        return constVal != null
                || (terms[0].isLiteral() && terms[2].isLiteral());
    }

    boolean isImmutable() {
        return constVal != null
                || (terms[0].isImmutable() && terms[2].isImmutable());
    }

    boolean isSafeExpr() {
        return constVal != null
                || (terms[0].isSafeExpr() && terms[2].isSafeExpr());
    }

    boolean isSafeWithThrow() {
        return constVal != null
                || (terms[0].isSafeWithThrow() && terms[2].isSafeWithThrow());
    }

    int tokenCount() {
        if (constVal != null)
            return 1;
        Term t0 = terms[0];
        Term t2 = terms[2];
        return t0.tokenCount()
                + t2.tokenCount()
                + (t0.isSafeExpr() || t2.isSafeExpr() ? (t0.isImmutable()
                        || t2.isImmutable() ? 0 : 1) : 2);
    }

    void allocRcvr(int[] curRcvrs) {
        assertCond(exprType0 != null);
        if (constVal == null) {
            int[] curRcvrs2 = OutputContext.copyRcvrs(curRcvrs);
            terms[0].allocRcvr(curRcvrs);
            int s0;
            if (BinaryOp.isRcvrNeeded(terms[0], terms[2])) {
                rcvr = ++curRcvrs2[(s0 = exprType0.objectSize()) < Type.VOID ? s0
                        : Type.NULLREF];
            }
            terms[2].allocRcvr(curRcvrs2);
            OutputContext.joinRcvrs(curRcvrs, curRcvrs2);
        }
    }

    boolean isAtomary() {
        return constVal != null || rcvr > 0;
    }

    void processOutput(OutputContext oc) {
        assertCond(exprType0 != null);
        if (constVal != null) {
            oc.cPrint(constVal.outputBoolean());
            return;
        }
        int s0 = exprType0.objectSize();
        int s2 = exprType2.objectSize();
        String rcvrStr = null;
        if (rcvr > 0) {
            rcvrStr = OutputContext.getRcvrName(rcvr, s0);
            oc.cPrint("(");
            oc.cPrint(rcvrStr);
            oc.cPrint("= ");
            ExpressionType oldAssignmentRightType = oc.assignmentRightType;
            oc.assignmentRightType = null;
            if (s0 == Type.BOOLEAN || s0 >= Type.CLASSINTERFACE) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[s0 == Type.BOOLEAN ? Type.BOOLEAN
                        : Type.CLASSINTERFACE]);
                oc.cPrint(")");
                terms[0].atomaryOutput(oc);
            } else {
                terms[0].processOutput(oc);
            }
            oc.assignmentRightType = oldAssignmentRightType;
            oc.cPrint(", ");
        }
        String cast0 = null;
        String cast2 = null;
        boolean special = false;
        int sym = terms[1].getSym();
        if ((s0 == Type.FLOAT || s0 == Type.DOUBLE || s2 == Type.FLOAT || s2 == Type.DOUBLE)
                && !isLiteral()) {
            if (s0 < Type.FLOAT) {
                cast0 = Type.cName[s2];
            } else if (s2 < Type.FLOAT) {
                cast2 = Type.cName[s0];
            }
            oc.cPrint(sym == LexTerm.EQ ? "JCGO_FP_EQU"
                    : sym == LexTerm.NE ? "!JCGO_FP_EQU" : sym == LexTerm.LT
                            || sym == LexTerm.GT ? "JCGO_FP_LT" : "JCGO_FP_LQ");
            if (s0 <= Type.FLOAT && s2 <= Type.FLOAT) {
                oc.cPrint("F");
            }
            oc.cPrint("(");
            if (sym == LexTerm.GT || sym == LexTerm.GE) {
                if (cast2 != null) {
                    oc.cPrint("(");
                    oc.cPrint(cast2);
                    oc.cPrint(")");
                    terms[2].atomaryOutput(oc);
                } else {
                    terms[2].processOutput(oc);
                }
                oc.cPrint(", ");
            }
            special = true;
        } else if (s0 == Type.CHAR) {
            if (s2 != Type.CHAR) {
                cast0 = Type.cName[Type.INT];
            }
        } else if (s2 == Type.CHAR) {
            cast2 = Type.cName[Type.INT];
        } else {
            if (s0 >= Type.CLASSINTERFACE && s2 >= Type.CLASSINTERFACE
                    && exprType0 != exprType2
                    && (s0 == Type.CLASSINTERFACE || s2 == Type.CLASSINTERFACE)) {
                if (s0 != Type.CLASSINTERFACE) {
                    cast2 = exprType0.castName();
                } else if (s2 != Type.CLASSINTERFACE
                        || ClassDefinition.isAssignableFrom(exprType2,
                                exprType0, null)) {
                    cast0 = exprType2.castName();
                } else {
                    cast2 = exprType0.castName();
                }
            }
            if (cast0 == null && rcvrStr != null
                    && (s0 == Type.NULLREF || s0 >= Type.CLASSINTERFACE)) {
                cast0 = exprType0.castName();
            }
        }
        if (cast0 != null) {
            oc.cPrint("(");
            oc.cPrint(cast0);
            oc.cPrint(")");
        }
        if (rcvrStr != null) {
            oc.cPrint(rcvrStr);
        } else if (cast0 != null) {
            terms[0].atomaryOutput(oc);
        } else {
            terms[0].processOutput(oc);
        }
        if (!special || (sym != LexTerm.GT && sym != LexTerm.GE)) {
            if (special) {
                oc.cPrint(", ");
            } else {
                oc.cPrint("\003 ");
                terms[1].processOutput(oc);
                oc.cPrint("\003 ");
            }
            if (cast2 != null) {
                oc.cPrint("(");
                oc.cPrint(cast2);
                oc.cPrint(")");
                terms[2].atomaryOutput(oc);
            } else {
                terms[2].processOutput(oc);
            }
        }
        if (special) {
            oc.cPrint(")");
        }
        if (rcvrStr != null) {
            oc.cPrint(")");
        }
    }

    ExpressionType traceClassInit() {
        if (constVal == null) {
            terms[0].traceClassInit();
            terms[2].traceClassInit();
        }
        return null;
    }
}
