/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/TryStatement.java --
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
 * Grammar production for 'try' clause.
 ** 
 * Formats: TRY Block FINALLY Block TRY Block Catches Empty Empty TRY Block
 * Catches FINALLY Block
 */

class TryStatement extends LexNode {

    private TryStatement currentTry;

    private ObjHashSet changedVars;

    private ObjHashSet accessedOuterObjLocals;

    private boolean isThisAccessed;

    private ClassDefinition callerClass;

    private String catchesSuffix;

    TryStatement(Term b, Term d) {
        super(b, Empty.newTerm(), d);
    }

    TryStatement(Term b, Term c, Term e) {
        super(b, c, e);
    }

    void processPass1(Context c) {
        if (terms[2].notEmpty() && terms[1].notEmpty()) {
            terms[0] = new TryStatement(terms[0], terms[1], Empty.newTerm());
            terms[1] = Empty.newTerm();
        }
        callerClass = c.currentClass;
        currentTry = c.currentTry;
        assertCond(c.currentMethod != null);
        c.currentMethod.setNeedsDummyRet();
        c.currentTry = this;
        if (terms[1].notEmpty() || terms[2].notEmpty()) {
            int oldForceVmExc = c.forceVmExc;
            if (terms[1].notEmpty()) {
                assertCond(c.localScope != null);
                terms[1].defineClass(c, null);
                Enumeration en = terms[1].getSignature().elements();
                while (en.hasMoreElements()) {
                    c.forceVmExc |= ((ClassDefinition) en.nextElement())
                            .getVMExcMask();
                }
            }
            ObjQueue names = new ObjQueue();
            terms[0].assignedVarNames(names, true);
            c.currentMethod.resetLocalsActualType(c, names, true);
            accessedOuterObjLocals = new ObjHashSet();
            ObjHashSet oldChangedVars = c.curTryChangedVars;
            c.curTryChangedVars = new ObjHashSet();
            c.isConditional = true;
            BranchContext oldBranch = c.saveBranch();
            terms[0].processPass1(c);
            c.intersectBranch(oldBranch);
            c.forceVmExc = oldForceVmExc;
            if (c.curTryChangedVars.size() > 0) {
                changedVars = c.curTryChangedVars;
            }
            c.curTryChangedVars = oldChangedVars;
            c.currentTry = currentTry;
            ObjQueue innerVars = new ObjQueue();
            Enumeration en = accessedOuterObjLocals.elements();
            while (en.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) en.nextElement();
                if (c.currentMethod.getLocalVar(v.id()) == null) {
                    innerVars.addLast(v);
                }
            }
            Enumeration en2 = innerVars.elements();
            while (en2.hasMoreElements()) {
                accessedOuterObjLocals.remove(en2.nextElement());
            }
            catchesSuffix = "";
            if (terms[1].notEmpty()) {
                catchesSuffix = c.currentMethod.nextLabelSuffix();
                terms[1].processPass1(c);
            }
            if (oldChangedVars != null && changedVars != null
                    && !terms[2].notEmpty()) {
                Enumeration en3 = changedVars.elements();
                while (en3.hasMoreElements()) {
                    oldChangedVars.add(en3.nextElement());
                }
            }
        } else {
            terms[0].processPass1(c);
            c.currentTry = currentTry;
        }
        boolean oldBreakableHidden = c.breakableHidden;
        c.breakableHidden = true;
        terms[2].processPass1(c);
        c.breakableHidden = oldBreakableHidden;
    }

    final void setVarAccessed(VariableDefinition v) {
        if (accessedOuterObjLocals != null) {
            if (v.isLocalOrParam()) {
                if (v.exprType().objectSize() >= Type.CLASSINTERFACE) {
                    accessedOuterObjLocals.add(v);
                }
            } else if (!v.isClassVariable()) {
                isThisAccessed = true;
            }
        }
    }

    boolean hasTailReturnOrThrow() {
        return terms[2].notEmpty() ? terms[2].hasTailReturnOrThrow() : terms[0]
                .hasTailReturnOrThrow() && terms[1].hasTailReturnOrThrow();
    }

    void discoverObjLeaks() {
        terms[0].discoverObjLeaks();
        if (terms[2].notEmpty() ? Main.dict.get(Names.JAVA_LANG_THROWABLE)
                .used() : hasExceptionInstances()) {
            if (changedVars != null) {
                Enumeration en = changedVars.elements();
                changedVars = null;
                while (en.hasMoreElements()) {
                    ((VariableDefinition) en.nextElement()).setChangedSpecial();
                }
            }
            if (accessedOuterObjLocals != null) {
                Enumeration en = accessedOuterObjLocals.elements();
                while (en.hasMoreElements()) {
                    ((VariableDefinition) en.nextElement())
                            .setStackObjVolatile();
                }
                if (isThisAccessed) {
                    assertCond(callerClass != null);
                    if (Main.dict.ourMethod != null) {
                        Main.dict.ourMethod.setThisStackObjVolatile();
                    } else {
                        callerClass.setInitThisStackObjVolatile();
                    }
                }
            }
        }
        terms[1].discoverObjLeaks();
        terms[2].discoverObjLeaks();
    }

    private boolean hasExceptionInstances() {
        Enumeration en = terms[1].getSignature().elements();
        while (en.hasMoreElements()) {
            if (((ClassDefinition) en.nextElement()).hasRealInstances())
                return true;
        }
        return false;
    }

    static void outputFinallyGroup(TryStatement curTry, TryStatement lastTry,
            OutputContext oc, String code) {
        boolean needsEnd = false;
        if (curTry != null && curTry.outputFinallyGroupInner(oc, lastTry, true)) {
            if (oc.insideNotSehTry)
                return;
            oc.cPrint("\n#else\010");
            needsEnd = true;
        }
        oc.cPrint(code);
        oc.cPrint(";");
        if (needsEnd) {
            oc.cPrint("\n#endif\010");
        }
    }

    final boolean outputFinallyGroupInner(OutputContext oc,
            TryStatement lastTry, boolean allowBreakThrow) {
        if (lastTry == this)
            return false;
        boolean oldInsideNotSehTry = oc.insideNotSehTry;
        if (!oldInsideNotSehTry) {
            oc.cPrint("\n#ifndef JCGO_SEHTRY\010");
            oc.insideNotSehTry = true;
        }
        TryStatement ts = this;
        do {
            assertCond(ts != null);
            if (ts.outputFinallyCode(oc)) {
                if (!allowBreakThrow && !ts.terms[2].isReturnAtEnd(false))
                    break;
                oc.insideNotSehTry = oldInsideNotSehTry;
                return true;
            }
        } while ((ts = ts.currentTry) != lastTry);
        if (!oldInsideNotSehTry) {
            oc.insideNotSehTry = false;
            oc.cPrint("\n#endif\010");
        }
        return false;
    }

    boolean outputFinallyCode(OutputContext oc) {
        if (catchesSuffix == null)
            return false;
        oc.cPrint("jcgo_tryLeave();");
        terms[2].processOutput(oc);
        return terms[2].isReturnAtEnd(true);
    }

    boolean isReturnAtEnd(boolean allowBreakThrow) {
        return terms[2].notEmpty() ? terms[2].isReturnAtEnd(allowBreakThrow)
                : terms[0].isReturnAtEnd(allowBreakThrow)
                        && !hasExceptionInstances();
    }

    void processOutput(OutputContext oc) {
        if (terms[2].notEmpty() ? Main.dict.get(Names.JAVA_LANG_THROWABLE)
                .used() : (!terms[1].isSwitchMapAssign(false) || !terms[0]
                .isSwitchMapAssign(false)) && hasExceptionInstances()) {
            oc.cPrint("{JCGO_TRY_BLOCK");
            terms[0].processOutput(oc);
            oc.cPrint("JCGO_TRY_LEAVE\010");
            if (terms[1].notEmpty()) {
                assertCond(catchesSuffix != null);
                oc.cPrint("JCGO_TRY_CATCHES(");
                oc.cPrint(catchesSuffix);
                oc.cPrint(")\010");
                terms[1].processOutput(oc);
                oc.cPrint("JCGO_TRY_RETHROW(");
                oc.cPrint(catchesSuffix);
                oc.cPrint(")\010");
            } else {
                terms[2].processOutput(oc);
                if (!terms[2].isReturnAtEnd(true)) {
                    oc.cPrint("JCGO_TRY_FINALLYEND\010");
                }
            }
            oc.cPrint("}");
        } else {
            catchesSuffix = null;
            terms[0].processOutput(oc);
            terms[2].processOutput(oc);
        }
    }
}
