/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/BinaryOp.java --
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
 * Grammar production for binary non-relational operators.
 ** 
 * Formats: InclusiveOrExpression BITOR ExclusiveOrExpression
 * ExclusiveOrExpression XOR AndExpression AndExpression BITAND
 * EqualityExpression ShiftExpression FILLSHIFT_RIGHT AdditiveExpression
 * ShiftExpression SHIFT_RIGHT AdditiveExpression ShiftExpression SHIFT_LEFT
 * AdditiveExpression AdditiveExpression PLUS MultiplicativeExpression
 * AdditiveExpression MINUS MultiplicativeExpression MultiplicativeExpression
 * MOD UnaryExpression MultiplicativeExpression DIVIDE UnaryExpression
 * MultiplicativeExpression TIMES UnaryExpression
 */

final class BinaryOp extends LexNode {

    private ExpressionType exprType0;

    private ExpressionType exprType2;

    private int sym;

    private int strCnt0;

    private int strCnt2;

    private int rcvr;

    private boolean isString;

    private boolean t0IsLiteral;

    private boolean t0IsLiteralSet;

    private boolean t2IsLiteral;

    private boolean t2IsLiteralSet;

    private boolean asLiteral;

    private ConstValue constVal2;

    private LeftBrace noLeaksScope;

    private boolean isConditional;

    private boolean insideAssertStmt;

    private String stackObjCode;

    private boolean needsLocalVolatile;

    BinaryOp(Term a, Term b, Term c) {
        super(a, b, c);
    }

    boolean isJavaConstant(ClassDefinition ourClass) {
        return terms[0].isJavaConstant(ourClass)
                && terms[2].isJavaConstant(ourClass);
    }

    int getPrecedenceLevel() {
        int sym = terms[1].getSym();
        return sym == LexTerm.BITOR ? 5 : sym == LexTerm.XOR ? 6
                : sym == LexTerm.BITAND ? 7 : sym == LexTerm.FILLSHIFT_RIGHT
                        || sym == LexTerm.SHIFT_LEFT
                        || sym == LexTerm.SHIFT_RIGHT ? 10 : 11;
    }

    StringLiteral asStrLiteral() {
        return terms[1].getSym() == LexTerm.PLUS ? terms[2].asStrLiteral()
                : null;
    }

    boolean appendLiteralTo(Term t) {
        return terms[1].getSym() == LexTerm.PLUS && terms[0].appendLiteralTo(t)
                && terms[2].appendLiteralTo(t);
    }

    void processPass1(Context c) {
        if (exprType0 == null) {
            sym = terms[1].getSym();
            if (sym == LexTerm.PLUS) {
                terms[2].appendLiteralTo(terms[0]);
            }
            BranchContext oldBranch = c.saveBranch();
            terms[0].processPass1(c);
            oldBranch = c.swapBranch(oldBranch);
            exprType0 = terms[0].exprType();
            terms[2].processPass1(c);
            c.unionBranch(oldBranch);
            exprType2 = terms[2].exprType();
            assertCond(!terms[0].isType() && !terms[2].isType());
            int s0;
            int s2;
            if (sym == LexTerm.PLUS
                    && (exprType0.receiverClass().isStringOrNull() || exprType2
                            .receiverClass().isStringOrNull())) {
                if (exprType0.objectSize() == Type.VOID
                        || exprType2.objectSize() == Type.VOID) {
                    fatalError(c,
                            "Inappropriate type of value in string concatenation");
                }
                isString = true;
                insideAssertStmt = c.insideAssertStmt;
                strCnt0 = terms[0].countConcatStrs();
                strCnt2 = terms[2].countConcatStrs();
                if (strCnt2 > 1 && strCnt0 > 0) {
                    strCnt2 = 1;
                }
                if (strCnt0 + strCnt2 == 1) {
                    if (strCnt0 == 0) {
                        if (exprType2.receiverClass().isStringOrNull() ? !terms[2]
                                .isNotNull() : terms[2].actualExprType()
                                .objectSize() == Type.CLASSINTERFACE) {
                            strCnt0 = 1;
                        }
                    } else if (exprType0.receiverClass().isStringOrNull() ? !terms[0]
                            .isNotNull() : terms[0].actualExprType()
                            .objectSize() == Type.CLASSINTERFACE) {
                        strCnt2 = 1;
                    }
                }
                if (!exprType0.receiverClass().isStringOrNull()) {
                    exprType0 = (terms[0] = wrapInValueOf(c, terms[0]))
                            .exprType();
                }
                if (!exprType2.receiverClass().isStringOrNull()) {
                    exprType2 = (terms[2] = wrapInValueOf(c, terms[2]))
                            .exprType();
                }
                if (strCnt0 + strCnt2 > 2) {
                    ClassDefinition cd = Main.dict
                            .get(Names.JAVA_LANG_STRINGBUILDER);
                    cd.define(c.forClass);
                    cd.markMethod("<init>()");
                    cd.markMethod(Names.SIGN_APPEND_STRING);
                    Main.dict.get(Names.JAVA_LANG_STRING).markMethod(
                            Names.SIGN_INIT_STRINGBUILDER);
                } else {
                    Main.dict.get(Names.JAVA_LANG_VMSYSTEM).markMethod(
                            Names.SIGN_CONCAT0X);
                }
                isConditional = c.isConditional;
                noLeaksScope = c.localScope;
            } else if ((s0 = exprType0.objectSize()) == Type.BOOLEAN
                    && exprType2.objectSize() == Type.BOOLEAN) {
                if (sym != LexTerm.XOR && sym != LexTerm.BITOR
                        && sym != LexTerm.BITAND) {
                    fatalError(c, "Illegal boolean logical operator");
                }
            } else if (s0 >= Type.VOID
                    || s0 <= Type.BOOLEAN
                    || (s2 = exprType2.objectSize()) >= Type.VOID
                    || s2 <= Type.BOOLEAN
                    || ((s0 >= Type.FLOAT || s2 >= Type.FLOAT) && (sym == LexTerm.XOR
                            || sym == LexTerm.BITOR
                            || sym == LexTerm.BITAND
                            || sym == LexTerm.FILLSHIFT_RIGHT
                            || sym == LexTerm.SHIFT_RIGHT || sym == LexTerm.SHIFT_LEFT))) {
                fatalError(c,
                        "Inappropriate type of value in numeric expression");
            }
            constVal2 = terms[2].evaluateConstValue();
            if (constVal2 == null
                    && !isString
                    && (sym == LexTerm.PLUS || sym == LexTerm.TIMES
                            || sym == LexTerm.BITAND || sym == LexTerm.BITOR || sym == LexTerm.XOR)
                    && (constVal2 = terms[0].evaluateConstValue()) != null) {
                Term t = terms[0];
                terms[0] = terms[2];
                terms[2] = t;
                ExpressionType exprType = exprType0;
                exprType0 = exprType2;
                exprType2 = exprType;
            }
        }
    }

    ConstValue evaluateConstValue() {
        assertCond(exprType0 != null);
        if (constVal2 == null || isString)
            return null;
        ConstValue constVal0 = terms[0].evaluateConstValue();
        return constVal0 != null ? constVal0.arithmeticOp(constVal2, sym)
                : null;
    }

    boolean isFPZero() {
        assertCond(exprType0 != null);
        return sym != LexTerm.DIVIDE && sym != LexTerm.MOD
                && terms[0].isFPZero() && terms[2].isFPZero();
    }

    ExpressionType exprType() {
        assertCond(exprType0 != null);
        int s0 = exprType0.objectSize();
        int s2 = exprType2.objectSize();
        if (isString)
            return Main.dict.get(Names.JAVA_LANG_STRING);
        if (s0 != Type.BOOLEAN || s2 != Type.BOOLEAN) {
            if (sym == LexTerm.FILLSHIFT_RIGHT || sym == LexTerm.SHIFT_RIGHT
                    || sym == LexTerm.SHIFT_LEFT)
                return s0 >= Type.INT ? exprType0
                        : Main.dict.classTable[Type.INT];
            if (s0 < Type.INT && s2 < Type.INT)
                return Main.dict.classTable[Type.INT];
            if (s0 < s2)
                return exprType2;
        }
        return exprType0;
    }

    int countConcatStrs() {
        assertCond(exprType0 != null);
        return isString ? strCnt0 + strCnt2 : 1;
    }

    private boolean t0IsLiteral() {
        if (!t0IsLiteralSet) {
            t0IsLiteral = terms[0].isLiteral()
                    && (!isString || exprType0.receiverClass().isStringOrNull());
            t0IsLiteralSet = true;
        }
        return t0IsLiteral;
    }

    private boolean t2IsLiteral() {
        if (!t2IsLiteralSet) {
            t2IsLiteral = terms[2].isLiteral()
                    && (!isString || exprType2.receiverClass().isStringOrNull());
            t2IsLiteralSet = true;
        }
        return t2IsLiteral;
    }

    private boolean t2IsNonZeroLiteral() {
        return constVal2 != null && constVal2.isNonZero() && t2IsLiteral();
    }

    boolean isLiteral() {
        assertCond(exprType0 != null);
        if (isString)
            return strCnt0 + strCnt2 <= 1 && t0IsLiteral() && t2IsLiteral();
        if (sym == LexTerm.DIVIDE || sym == LexTerm.MOD)
            return exprType0.objectSize() < Type.FLOAT
                    && exprType2.objectSize() < Type.FLOAT && t0IsLiteral()
                    && t2IsNonZeroLiteral();
        if (exprType0.objectSize() != Type.LONG
                || (sym != LexTerm.SHIFT_LEFT && sym != LexTerm.FILLSHIFT_RIGHT && sym != LexTerm.SHIFT_RIGHT))
            return t0IsLiteral() && t2IsLiteral();
        return constVal2 != null && t0IsLiteral() && t2IsLiteral()
                && terms[0].evaluateConstValue() != null;
    }

    boolean isImmutable() {
        assertCond(exprType0 != null);
        return isString ? strCnt0 + strCnt2 <= 1 && t0IsLiteral()
                && t2IsLiteral()
                : ((sym != LexTerm.DIVIDE && sym != LexTerm.MOD)
                        || exprType0.objectSize() >= Type.FLOAT
                        || exprType2.objectSize() >= Type.FLOAT ? terms[2]
                        .isImmutable() : t2IsNonZeroLiteral())
                        && terms[0].isImmutable();
    }

    boolean isSafeExpr() {
        assertCond(exprType0 != null);
        return ((sym != LexTerm.DIVIDE && sym != LexTerm.MOD)
                || exprType0.objectSize() >= Type.FLOAT
                || exprType2.objectSize() >= Type.FLOAT ? terms[2].isSafeExpr()
                : t2IsNonZeroLiteral()) && terms[0].isSafeExpr();
    }

    boolean isSafeWithThrow() {
        return terms[0].isSafeWithThrow() && terms[2].isSafeWithThrow();
    }

    int tokenCount() {
        return isString ? (strCnt0 + strCnt2 <= 1 ? 1 : strCnt0 == 0 ? terms[2]
                .tokenCount() : strCnt2 == 0 ? terms[0].tokenCount() : terms[0]
                .tokenCount() + terms[2].tokenCount() + 2) : constVal2 != null
                && terms[0].evaluateConstValue() != null ? 1 : terms[0]
                .tokenCount()
                + terms[2].tokenCount()
                + (terms[0].isSafeExpr() || terms[2].isSafeExpr() ? 1 : 2);
    }

    static boolean isRcvrNeeded(Term t0, Term t2) {
        return t0.isSafeExpr() ? !t2.isSafeWithThrow()
                && (t0.isFieldAccessed(null) || t2.isAnyLocalVarChanged(t0))
                && !t0.isImmutable() : t0.isSafeWithThrow() ? !t2.isSafeExpr()
                : !t2.isImmutable()
                        && (!t2.isSafeExpr() || t2.isFieldAccessed(null) || t0
                                .isAnyLocalVarChanged(t2));
    }

    void allocRcvr(int[] curRcvrs) {
        assertCond(exprType0 != null);
        int[] curRcvrs2 = OutputContext.copyRcvrs(curRcvrs);
        terms[0].allocRcvr(curRcvrs);
        int s0;
        if (isRcvrNeeded(terms[0], terms[2])) {
            rcvr = ++curRcvrs2[!isString
                    && (s0 = exprType0.objectSize()) < Type.VOID ? s0
                    : Type.NULLREF];
        }
        terms[2].allocRcvr(curRcvrs2);
        OutputContext.joinRcvrs(curRcvrs, curRcvrs2);
    }

    boolean isNotNull() {
        return true;
    }

    String strLiteralValueGuess() {
        if (!isString)
            return null;
        String str0 = terms[0].strLiteralValueGuess();
        String str2 = terms[2].strLiteralValueGuess();
        return str0 != null ? (str2 != null ? str0 + str2 : str0) : str2;
    }

    void requireLiteral() {
        asLiteral = true;
        terms[0].requireLiteral();
        terms[2].requireLiteral();
    }

    void setStackObjVolatile() {
        if (!needsLocalVolatile) {
            needsLocalVolatile = true;
            if (isString) {
                if (strCnt0 + strCnt2 <= 2) {
                    terms[0].setStackObjVolatile();
                    terms[2].setStackObjVolatile();
                } else if (strCnt0 == 0) {
                    terms[2].setStackObjVolatile();
                } else if (strCnt2 == 0) {
                    terms[0].setStackObjVolatile();
                }
            }
        }
    }

    void setObjLeaks(VariableDefinition v) {
        if (isString) {
            if (strCnt0 + strCnt2 <= 2) {
                terms[0].setObjLeaks(v);
                terms[2].setObjLeaks(v);
            } else if (strCnt0 == 0) {
                terms[2].setObjLeaks(v);
            } else if (strCnt2 == 0) {
                terms[0].setObjLeaks(v);
            }
            if (strCnt0 > 1 && strCnt2 > 0) {
                noLeaksScope = VariableDefinition.addSetObjLeaksTerm(
                        noLeaksScope, v, this, isConditional);
            }
        }
    }

    String writeStackObjDefn(OutputContext oc, boolean needsLocalVolatile) {
        assertCond(isString);
        return InstanceCreation.writeStackObjectDefn(oc,
                Main.dict.get(Names.JAVA_LANG_STRING), false, insideAssertStmt,
                needsLocalVolatile && Main.dict.allowConstStr <= 0);
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        if (isString && strCnt0 > 1 && strCnt2 > 0) {
            terms[0].writeStackStrBufObj(oc, scopeTerm);
            if (noLeaksScope == scopeTerm) {
                assertCond(scopeTerm != null);
                stackObjCode = writeStackObjDefn(oc, needsLocalVolatile);
            }
        } else {
            terms[0].writeStackObjs(oc, scopeTerm);
        }
        terms[2].writeStackObjs(oc, scopeTerm);
    }

    void writeStackStrBufObj(OutputContext oc, Term scopeTerm) {
        assertCond(isString);
        if (strCnt0 == 0) {
            terms[2].writeStackStrBufObj(oc, scopeTerm);
        } else {
            if (strCnt2 == 0 || strCnt0 > 1) {
                terms[0].writeStackStrBufObj(oc, scopeTerm);
            } else {
                terms[0].writeStackObjs(oc, scopeTerm);
                if (noLeaksScope == scopeTerm) {
                    assertCond(scopeTerm != null);
                    stackObjCode = InstanceCreation.writeStackObjectDefn(oc,
                            Main.dict.get(Names.JAVA_LANG_STRINGBUILDER), true,
                            insideAssertStmt, false);
                }
            }
            terms[2].writeStackObjs(oc, scopeTerm);
        }
    }

    ExpressionType writeStackObjRetCode(OutputContext oc) {
        assertCond(isString && noLeaksScope == null);
        stackObjCode = MethodDefinition.STACKOBJ_RETNAME;
        oc.cPrint(Main.dict.get(Names.JAVA_LANG_STRING).cNewObjectCode());
        return exprType();
    }

    boolean isAtomary() {
        return isString || rcvr > 0 || sym == LexTerm.FILLSHIFT_RIGHT
                || sym == LexTerm.SHIFT_RIGHT || sym == LexTerm.SHIFT_LEFT;
    }

    static void outputDivShift(OutputContext oc, int sym, int s0) {
        if (sym == LexTerm.DIVIDE || sym == LexTerm.MOD) {
            oc.cPrint("jcgo_");
            if (s0 == Type.LONG) {
                oc.cPrint("l");
            } else if (s0 >= Type.FLOAT) {
                oc.cPrint("f");
            }
            oc.cPrint(sym == LexTerm.DIVIDE ? "div" : "mod");
            if (s0 == Type.FLOAT) {
                oc.cPrint("f");
            }
        } else {
            oc.cPrint("JCGO_");
            if (s0 == Type.LONG) {
                oc.cPrint("L");
            }
            if (sym == LexTerm.FILLSHIFT_RIGHT) {
                oc.cPrint("U");
            }
            oc.cPrint(sym == LexTerm.SHIFT_LEFT ? "SHL" : "SHR");
            oc.cPrint("_F");
        }
        oc.cPrint("(");
    }

    void parameterOutput(OutputContext oc, boolean asArg, int type) {
        assertCond(isString);
        if (strCnt0 == 0) {
            terms[2].parameterOutput(oc, asArg, type);
        } else if (strCnt2 == 0) {
            terms[0].parameterOutput(oc, asArg, type);
        } else {
            String rcvrStr = null;
            if (rcvr > 0) {
                rcvrStr = OutputContext.getRcvrName(rcvr, Type.CLASSINTERFACE);
                oc.cPrint("(");
                oc.cPrint(rcvrStr);
                oc.cPrint("= (");
                oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
                oc.cPrint(")");
                ExpressionType oldAssignmentRightType = oc.assignmentRightType;
                oc.assignmentRightType = null;
                if (strCnt0 > 1) {
                    terms[0].parameterOutput(oc, asArg, type);
                } else {
                    terms[0].atomaryOutput(oc);
                }
                oc.assignmentRightType = oldAssignmentRightType;
                oc.cPrint(", ");
            }
            ClassDefinition cd = Main.dict.get(Names.JAVA_LANG_STRINGBUILDER);
            MethodDefinition md = cd.getMethod(Names.SIGN_APPEND_STRING);
            oc.cPrint(md != null ? md.routineCName()
                    : MethodDefinition.UNKNOWN_NAME);
            oc.cPrint("(\010 ");
            if (strCnt0 <= 1) {
                oc.cPrint(md != null ? md.routineCName()
                        : MethodDefinition.UNKNOWN_NAME);
                Main.dict.normalCalls++;
                oc.cPrint("(\010 ");
                InstanceCreation.writeNewRoutineCall(oc,
                        cd.getMethod("<init>()"), stackObjCode);
                oc.cPrint("), ");
            }
            if (rcvrStr != null) {
                oc.cPrint("(");
                oc.cPrint((strCnt0 <= 1 ? Main.dict.get(Names.JAVA_LANG_STRING)
                        : cd).castName());
                oc.cPrint(")");
                oc.cPrint(rcvrStr);
            } else if (strCnt0 > 1) {
                terms[0].parameterOutput(oc, asArg, type);
            } else {
                terms[0].processOutput(oc);
            }
            if (strCnt0 <= 1) {
                oc.cPrint(")");
            }
            oc.cPrint(", ");
            terms[2].processOutput(oc);
            oc.cPrint(")");
            Main.dict.normalCalls++;
            if (rcvrStr != null) {
                oc.cPrint(")");
            }
        }
    }

    void processOutput(OutputContext oc) {
        assertCond(exprType0 != null);
        String rcvrStr = null;
        String str0 = null;
        if (isString) {
            if (strCnt0 > 1 && strCnt2 > 0) {
                OutputContext oc2 = new OutputContext();
                terms[0].parameterOutput(oc2, false, Type.NULLREF);
                str0 = oc2.instanceToString();
            } else {
                str0 = strCnt0 > 0 ? (strCnt2 == 0 && !terms[0].isAtomary() ? "("
                        + terms[0].stringOutput() + ")"
                        : terms[0].stringOutput())
                        : LexTerm.NULL_STR;
            }
        }
        if (rcvr > 0) {
            rcvrStr = OutputContext.getRcvrName(rcvr,
                    isString ? Type.CLASSINTERFACE : exprType0.objectSize());
            oc.cPrint("(");
            oc.cPrint(rcvrStr);
            oc.cPrint("= ");
            if (str0 != null) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
                oc.cPrint(")");
                oc.cPrint(str0);
            } else {
                ExpressionType oldAssignmentRightType = oc.assignmentRightType;
                oc.assignmentRightType = null;
                terms[0].processOutput(oc);
                oc.assignmentRightType = oldAssignmentRightType;
            }
            oc.cPrint(", ");
        }
        if (str0 != null) {
            if (strCnt0 > 0) {
                ClassDefinition cd = Main.dict.get(Names.JAVA_LANG_STRING);
                if (strCnt2 > 0) {
                    MethodDefinition md;
                    if (strCnt0 > 1) {
                        InstanceCreation.writeNewRoutineCall(oc,
                                cd.getMethod(Names.SIGN_INIT_STRINGBUILDER),
                                stackObjCode);
                        cd = Main.dict.get(Names.JAVA_LANG_STRINGBUILDER);
                        md = cd.getMethod(Names.SIGN_APPEND_STRING);
                    } else {
                        md = Main.dict.get(Names.JAVA_LANG_VMSYSTEM).getMethod(
                                Names.SIGN_CONCAT0X);
                    }
                    oc.cPrint(md != null ? md.routineCName()
                            : MethodDefinition.UNKNOWN_NAME);
                    Main.dict.normalCalls++;
                    oc.cPrint("(\010 ");
                }
                oc.cPrint(rcvrStr != null ? "(" + cd.castName() + ")" + rcvrStr
                        : str0);
                if (strCnt2 > 0) {
                    oc.cPrint(", ");
                }
            }
            if (strCnt2 > 0 || strCnt0 == 0) {
                if (strCnt0 > 0) {
                    terms[2].processOutput(oc);
                } else {
                    terms[2].atomaryOutput(oc);
                }
                if (strCnt0 > 0) {
                    oc.cPrint(")");
                    if (strCnt0 > 1) {
                        oc.cPrint(")");
                    }
                }
            }
        } else {
            int s0 = exprType0.objectSize();
            int s2 = exprType2.objectSize();
            int sm = s0 <= s2 ? s2 : s0;
            ConstValue value;
            if (sm == Type.LONG && (value = evaluateConstValue()) != null) {
                oc.cPrint(value.stringOutput());
            } else if (sym != LexTerm.DIVIDE && sym != LexTerm.MOD ? sym == LexTerm.FILLSHIFT_RIGHT
                    || sym == LexTerm.SHIFT_RIGHT || sym == LexTerm.SHIFT_LEFT
                    : sm >= Type.FLOAT
                            || (t0IsLiteral() ? !t2IsNonZeroLiteral()
                                    : sm == Type.LONG || constVal2 == null
                                            || !constVal2.isNonZero()
                                            || constVal2.isMinusOne())) {
                outputDivShift(oc, sym, sym == LexTerm.DIVIDE
                        || sym == LexTerm.MOD ? sm : s0);
                if (sm >= Type.LONG && sm > s0
                        && (sym == LexTerm.DIVIDE || sym == LexTerm.MOD)) {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[sm]);
                    oc.cPrint(")");
                    if (rcvrStr != null) {
                        oc.cPrint(rcvrStr);
                    } else {
                        terms[0].atomaryOutput(oc);
                    }
                } else if (rcvrStr != null) {
                    oc.cPrint(rcvrStr);
                } else {
                    terms[0].processOutput(oc);
                }
                oc.cPrint(", ");
                if (sym == LexTerm.DIVIDE || sym == LexTerm.MOD) {
                    if (sm >= Type.LONG && sm > s2) {
                        oc.cPrint("(");
                        oc.cPrint(Type.cName[sm]);
                        oc.cPrint(")");
                        terms[2].atomaryOutput(oc);
                    } else {
                        terms[2].processOutput(oc);
                    }
                } else if (s2 == Type.LONG) {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[Type.INT]);
                    oc.cPrint(")");
                    terms[2].atomaryOutput(oc);
                } else {
                    terms[2].processOutput(oc);
                }
                oc.cPrint(")");
            } else if (constVal2 != null
                    && rcvrStr == null
                    && sym == LexTerm.BITAND
                    && s0 < Type.INT
                    && s2 <= Type.INT
                    && constVal2.getIntValue() == (s0 != Type.BYTE ? 0xffff
                            : 0xff)) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[Type.INT]);
                oc.cPrint(")((");
                oc.cPrint(s0 != Type.BYTE ? Type.cName[Type.CHAR]
                        : "unsigned\003 \003" + "char");
                oc.cPrint(")");
                terms[0].atomaryOutput(oc);
                oc.cPrint(")");
            } else {
                terms[0].insideArithOp();
                terms[2].insideArithOp();
                if (sm < Type.INT && sm != Type.BOOLEAN) {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[Type.INT]);
                    oc.cPrint(")");
                }
                int opLevel = getPrecedenceLevel();
                if (sm >= Type.LONG && sm > s0) {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[sm]);
                    oc.cPrint(")");
                    ConstValue constVal0;
                    if (rcvrStr != null) {
                        oc.cPrint(rcvrStr);
                    } else if (sm >= Type.FLOAT
                            && !asLiteral
                            && (constVal0 = terms[0].evaluateConstValue()) != null
                            && !constVal0.isNonZero()) {
                        oc.cPrint(sm == Type.FLOAT ? "JCGO_FP_ZEROF"
                                : "JCGO_FP_ZERO");
                    } else {
                        terms[0].atomaryOutput(oc);
                    }
                } else if (rcvrStr != null) {
                    oc.cPrint(rcvrStr);
                } else if (s0 == Type.BOOLEAN
                        || opLevel < terms[0].getPrecedenceLevel()) {
                    terms[0].atomaryOutput(oc);
                } else {
                    terms[0].processOutput(oc);
                }
                oc.cPrint("\003 ");
                terms[1].processOutput(oc);
                oc.cPrint("\003 ");
                if (sm >= Type.LONG && sm > s2) {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[sm]);
                    oc.cPrint(")");
                    if (sm >= Type.FLOAT && constVal2 != null && !asLiteral
                            && !constVal2.isNonZero()) {
                        oc.cPrint(sm == Type.FLOAT ? "JCGO_FP_ZEROF"
                                : "JCGO_FP_ZERO");
                    } else {
                        terms[2].atomaryOutput(oc);
                    }
                } else if (s2 == Type.BOOLEAN
                        || opLevel < terms[2].getPrecedenceLevel()) {
                    terms[2].atomaryOutput(oc);
                } else {
                    terms[2].processOutput(oc);
                }
            }
        }
        if (rcvrStr != null) {
            oc.cPrint(")");
        }
    }

    static Term wrapInValueOf(Context c, Term t) {
        int s0 = t.exprType().objectSize();
        return s0 >= Type.CLASSINTERFACE && t.isNotNull() ? new MethodInvocation(
                c, t, Names.TOSTRING)
                : new MethodInvocation(c,
                        Main.dict.get(Names.JAVA_LANG_STRING), Names.VALUEOF,
                        s0 == Type.CLASSINTERFACE + Type.CHAR
                                || s0 == Type.NULLREF ? (new CastExpression(
                                new Expression(new ClassOrIfaceType(Main.dict
                                        .get(Names.JAVA_LANG_OBJECT))), t))
                                .processPassOneInner(c, false).setLineInfoFrom(
                                        t) : t);
    }

    ExpressionType traceClassInit() {
        terms[0].traceClassInit();
        terms[2].traceClassInit();
        if (isString && strCnt0 > 0 && strCnt2 > 0) {
            MethodDefinition md;
            if (strCnt0 + strCnt2 > 2) {
                ClassDefinition cd = Main.dict
                        .get(Names.JAVA_LANG_STRINGBUILDER);
                md = cd.getMethod("<init>()");
                if (md != null) {
                    md.methodTraceClassInit(false, null, null);
                }
                md = cd.getMethod(Names.SIGN_APPEND_STRING);
                if (md != null) {
                    md.methodTraceClassInit(false, null, null);
                }
                md = Main.dict.get(Names.JAVA_LANG_STRING).getMethod(
                        Names.SIGN_INIT_STRINGBUILDER);
            } else {
                md = Main.dict.get(Names.JAVA_LANG_VMSYSTEM).getMethod(
                        Names.SIGN_CONCAT0X);
            }
            if (md != null) {
                md.methodTraceClassInit(false, null, null);
            }
        }
        return null;
    }
}
