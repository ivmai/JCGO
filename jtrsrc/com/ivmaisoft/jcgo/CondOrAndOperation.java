/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/CondOrAndOperation.java --
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
 * Grammar production for boolean logical operators.
 ** 
 * Formats: ConditionalAndExpression OR ConditionalOrExpression
 * InclusiveOrExpression AND ConditionalAndExpression
 */

final class CondOrAndOperation extends LexNode {

    private ConstValue constVal;

    private boolean omitFirstPart;

    CondOrAndOperation(Term a, Term b, Term c) {
        super(a, b, c);
    }

    boolean isJavaConstant(ClassDefinition ourClass) {
        return terms[0].isJavaConstant(ourClass)
                && terms[2].isJavaConstant(ourClass);
    }

    boolean handleAssertionsDisabled(ClassDefinition ourClass) {
        return terms[1].getSym() == LexTerm.AND
                && terms[0].handleAssertionsDisabled(ourClass);
    }

    int getPrecedenceLevel() {
        return terms[1].getSym() == LexTerm.AND ? 4 : 3;
    }

    void processPass1(Context c) {
        terms[0].processPass1(c);
        if (terms[0].exprType().objectSize() != Type.BOOLEAN) {
            fatalError(c, "Conditional expression must be of boolean type");
        }
        constVal = terms[0].evaluateConstValue();
        boolean isAnd = terms[1].getSym() == LexTerm.AND;
        if (constVal == null || constVal.isNonZero() == isAnd) {
            if (constVal != null) {
                terms[2].processPass1(c);
            } else {
                BranchContext oldBranch = c.saveBranch();
                terms[0].updateCondBranch(c, isAnd);
                terms[2].processPass1(c);
                c.intersectBranch(oldBranch);
            }
            if (terms[2].exprType().objectSize() != Type.BOOLEAN) {
                fatalError(c, "Conditional expression must be of boolean type");
            }
            if (constVal != null) {
                omitFirstPart = true;
                ConstValue constVal2 = terms[2].evaluateConstValue();
                constVal = isAnd ? constVal.bitAnd(constVal2) : constVal
                        .bitOr(constVal2);
            }
        }
    }

    void updateCondBranch(Context c, boolean forTrue) {
        if (constVal == null) {
            if (omitFirstPart) {
                terms[2].updateCondBranch(c, forTrue);
            } else {
                boolean isAnd = terms[1].getSym() == LexTerm.AND;
                if (forTrue == isAnd) {
                    terms[0].updateCondBranch(c, forTrue);
                    terms[2].updateCondBranch(c, forTrue);
                } else {
                    BranchContext oldBranch = c.saveBranch();
                    terms[0].updateCondBranch(c, forTrue);
                    oldBranch = c.swapBranch(oldBranch);
                    terms[2].updateCondBranch(c, forTrue);
                    c.intersectBranch(oldBranch);
                }
            }
        }
    }

    void requireLiteral() {
        if (constVal == null) {
            terms[0].requireLiteral();
            terms[2].requireLiteral();
        }
    }

    int tokenCount() {
        return constVal != null ? 1 : (omitFirstPart ? 0 : terms[0]
                .tokenCount() + 1) + terms[2].tokenCount();
    }

    int tokensExpandedCount() {
        return constVal != null ? 0 : omitFirstPart ? terms[2]
                .tokensExpandedCount() : super.tokensExpandedCount();
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

    boolean isFieldAccessed(VariableDefinition v) {
        return constVal == null
                && (terms[0].isFieldAccessed(v) || terms[2].isFieldAccessed(v));
    }

    boolean isAnyLocalVarChanged(Term t) {
        return constVal == null
                && (terms[0].isAnyLocalVarChanged(t) || terms[2]
                        .isAnyLocalVarChanged(t));
    }

    void allocRcvr(int[] curRcvrs) {
        if (constVal == null) {
            if (omitFirstPart) {
                terms[2].allocRcvr(curRcvrs);
            } else {
                int[] curRcvrs2 = OutputContext.copyRcvrs(curRcvrs);
                terms[0].allocRcvr(curRcvrs);
                terms[2].allocRcvr(curRcvrs2);
                OutputContext.joinRcvrs(curRcvrs, curRcvrs2);
            }
        }
    }

    void discoverObjLeaks() {
        if (constVal == null) {
            terms[0].discoverObjLeaks();
            terms[2].discoverObjLeaks();
        }
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        if (constVal == null) {
            terms[0].writeStackObjs(oc, scopeTerm);
            terms[2].writeStackObjs(oc, scopeTerm);
        }
    }

    void processOutput(OutputContext oc) {
        if (constVal != null) {
            oc.cPrint(constVal.outputBoolean());
        } else if (omitFirstPart) {
            terms[2].processOutput(oc);
        } else {
            int opLevel = getPrecedenceLevel();
            if (opLevel < terms[0].getPrecedenceLevel()) {
                terms[0].atomaryOutput(oc);
            } else {
                terms[0].processOutput(oc);
            }
            oc.cPrint("\003 ");
            terms[1].processOutput(oc);
            oc.cPrint("\003 ");
            if (opLevel < terms[2].getPrecedenceLevel()) {
                terms[2].atomaryOutput(oc);
            } else {
                terms[2].processOutput(oc);
            }
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
