/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ParameterList.java --
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
 * Grammar production for a list of arguments.
 ** 
 * Format: Argument COMMA Argument Argument COMMA ArgumentList
 */

final class ParameterList extends LexNode {

    ParameterList(Term a, Term c) {
        super(a, c);
    }

    static Term prepend(Term param, Term paramList) {
        return paramList.notEmpty() ? new ParameterList(param, paramList)
                : param;
    }

    Term joinParamLists(Term paramList) {
        return paramList.notEmpty() ? new ParameterList(terms[0],
                terms[1].joinParamLists(paramList)) : this;
    }

    Term getTermAt(int index) {
        assertCond(index == 0 || index == 1);
        return terms[index];
    }

    Term getArgumentTerm(int index) {
        return index > 0 ? terms[1].getArgumentTerm(index - 1) : terms[0]
                .getArgumentTerm(0);
    }

    void processPass1(Context c) {
        BranchContext oldBranch = c.saveBranch();
        terms[0].processPass1(c);
        oldBranch = c.swapBranch(oldBranch);
        terms[1].processPass1(c);
        c.unionBranch(oldBranch);
    }

    void setFormalType(Term formalParam, MethodDefinition md, Context c) {
        terms[0].setFormalType(formalParam.getTermAt(0), md, c);
        terms[1].setFormalType(formalParam.getTermAt(1), md, c);
    }

    boolean isImmutable() {
        return terms[0].isImmutable() && terms[1].isImmutable();
    }

    boolean isSafeExpr() {
        return terms[0].isSafeExpr() && terms[1].isSafeExpr();
    }

    boolean isSafeWithThrow() {
        return terms[0].isSafeWithThrow() && terms[1].isSafeWithThrow();
    }

    int tokenCount() {
        return terms[0].tokenCount() + terms[1].tokenCount();
    }

    void allocRcvr(int[] curRcvrs) {
        int[] curRcvrs2 = OutputContext.copyRcvrs(curRcvrs);
        markParamRcvr(0, curRcvrs2);
        allocParamRcvr(curRcvrs, OutputContext.copyRcvrs(curRcvrs), curRcvrs2);
    }

    int markParamRcvr(int isNotSafe, int[] curRcvrs2) {
        return terms[0].markParamRcvr(
                terms[1].markParamRcvr(isNotSafe, curRcvrs2), curRcvrs2);
    }

    void allocParamRcvr(int[] curRcvrs, int[] curRcvrs1, int[] curRcvrs2) {
        terms[0].allocParamRcvr(curRcvrs, curRcvrs1, curRcvrs2);
        terms[1].allocParamRcvr(curRcvrs, curRcvrs1, curRcvrs2);
    }

    void produceRcvr(OutputContext oc) {
        terms[0].produceRcvr(oc);
        terms[1].produceRcvr(oc);
    }

    boolean copyObjLeaksFrom(Term formalParam) {
        boolean hasAllLeaks;
        if (formalParam != null) {
            hasAllLeaks = terms[0].copyObjLeaksFrom(formalParam.getTermAt(0));
            if (!terms[1].copyObjLeaksFrom(formalParam.getTermAt(1))) {
                hasAllLeaks = false;
            }
        } else {
            terms[0].copyObjLeaksFrom(null);
            terms[1].copyObjLeaksFrom(null);
            hasAllLeaks = true;
        }
        return hasAllLeaks;
    }

    void setStackObjVolatile() {
        terms[0].setStackObjVolatile();
        terms[1].setStackObjVolatile();
    }

    void setObjLeaks(VariableDefinition v) {
        terms[0].setObjLeaks(v);
        terms[1].setObjLeaks(v);
    }

    void parameterOutput(OutputContext oc, boolean asArg, int type) {
        terms[0].parameterOutput(oc, asArg, type);
        terms[1].parameterOutput(oc, asArg, type);
    }

    void getTraceSignature(ObjVector parmTraceSig) {
        terms[0].getTraceSignature(parmTraceSig);
        terms[1].getTraceSignature(parmTraceSig);
    }
}
