/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/CatchStatement.java --
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
 * Grammar production for a catch clause.
 ** 
 * Format: CATCH LPAREN AccModifier/Empty ClassOrIfaceType VariableIdentifier
 * RPAREN LBRACE [BlockStatements] RBRACE
 */

final class CatchStatement extends LexNode {

    private ClassDefinition cd;

    private VariableDefinition v;

    CatchStatement(Term c, Term d, Term e, Term h) {
        super(c, d, e, new LeftBrace(), h, new RightBrace());
    }

    ClassDefinition defineClass(Context c, ObjVector vec) {
        terms[1].processPass1(c);
        cd = c.typeClassDefinition;
        assertCond(vec == null && cd.objectSize() == Type.CLASSINTERFACE);
        return null;
    }

    void processPass1(Context c) {
        assertCond(cd != null);
        c.isConditional = true;
        terms[3].processPass1(c);
        int oldModifiers = c.modifiers;
        c.modifiers = AccModifier.LOCALVAR | AccModifier.PARAMETER;
        terms[0].processPass1(c);
        c.varInitializer = Empty.newTerm();
        c.typeDims = 0;
        c.typeClassDefinition = cd;
        terms[2].processPass1(c);
        c.modifiers = oldModifiers;
        v = terms[2].getVariable(false);
        assertCond(v != null);
        if (cd.isInterface() || (cd.defined() && !cd.isThrowable())) {
            fatalError(c,
                    "Interfaces and non-Throwable classes are not allowed in catches: "
                            + cd.name());
        }
        if (!cd.used()
                && binaryStrSearch(Names.specVmExceptions, cd.name()) >= 0) {
            cd.predefineClass(c.forClass);
            cd.markUsed();
        }
        boolean oldHasBreakSimple = c.hasBreakSimple;
        c.hasBreakSimple = false;
        boolean oldHasBreakDeep = c.hasBreakDeep;
        c.hasBreakDeep = false;
        boolean oldHasContinueSimple = c.hasContinueSimple;
        c.hasContinueSimple = false;
        boolean oldHasContinueDeep = c.hasContinueDeep;
        c.hasContinueDeep = false;
        BranchContext oldBranch = c.saveBranch();
        c.addAccessedClass(cd);
        c.setVarNotNull(v);
        v.setUnassigned(false);
        terms[4].processPass1(c);
        if (!c.hasBreakSimple && !c.hasBreakDeep && !c.hasContinueSimple
                && !c.hasContinueDeep && terms[4].hasTailReturnOrThrow()) {
            c.swapBranch(oldBranch);
        } else {
            c.intersectBranch(oldBranch);
        }
        c.hasContinueSimple |= oldHasContinueSimple;
        c.hasContinueDeep |= oldHasContinueDeep;
        c.hasBreakSimple |= oldHasBreakSimple;
        c.hasBreakDeep |= oldHasBreakDeep;
        terms[5].processPass1(c);
    }

    private static int binaryStrSearch(String[] arr, String value) {
        int low = 0;
        int high = arr.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = arr[mid].compareTo(value);
            if (cmp > 0) {
                high = mid - 1;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    void storeSignature(ObjVector parmSig) {
        assertCond(cd != null);
        parmSig.addElement(cd);
    }

    boolean isSwitchMapAssign(boolean isMethodCall) {
        assertCond(cd != null);
        return !terms[4].notEmpty()
                && cd.name().equals(Names.JAVA_LANG_NOSUCHFIELDERROR);
    }

    boolean hasTailReturnOrThrow() {
        return terms[4].hasTailReturnOrThrow();
    }

    void allocRcvr(int[] curRcvrs) {
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
    }

    void processOutput(OutputContext oc) {
        assertCond(cd != null && v != null);
        if (cd.hasRealInstances()) {
            int[] curRcvrs = new int[Type.VOID];
            terms[4].allocRcvr(curRcvrs);
            String cname = cd.castName();
            oc.cPrint("JCGO_TRY_CATCH(OBJT_");
            oc.cPrint(cname);
            oc.cPrint(", MAXT_");
            oc.cPrint(cname);
            oc.cPrint(")");
            terms[3].processOutput(oc);
            oc.writeRcvrsVar(curRcvrs);
            terms[4].writeStackObjs(oc, terms[3]);
            if (v.used()) {
                terms[2].processOutput(oc);
                oc.cPrint("= (");
                oc.cPrint(cname);
                oc.cPrint(")JCGO_TRY_THROWABLE(0);");
            }
            terms[4].processOutput(oc);
            terms[5].processOutput(oc);
        }
    }

    ExpressionType traceClassInit() {
        terms[4].traceClassInit();
        return null;
    }
}
