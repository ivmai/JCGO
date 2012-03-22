/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ConstructorCall.java --
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
 * Grammar production for calls from one constructor to another.
 ** 
 * Formats: Empty Empty This/Super LPAREN [ArgumentList] RPAREN Primary DOT
 * Super LPAREN [ArgumentList] RPAREN
 */

final class ConstructorCall extends LexNode {

    private ClassDefinition ourClass;

    private MethodDefinition curMethod;

    private MethodDefinition md;

    ConstructorCall(Term a, Term c, Term e) {
        super(a, c, e);
    }

    void processPass1(Context c) {
        if (ourClass == null) {
            ourClass = c.currentClass;
            assertCond(ourClass != null);
            if (c.hasConstructor) {
                fatalError(c, "Duplicate constructor invocation found");
            }
            c.hasConstructor = true;
            terms[1].processPass1(c);
            ClassDefinition aclass = terms[1].exprType().receiverClass();
            Term paramList = Empty.newTerm();
            ObjVector locals = aclass.outerLocals(c.forClass);
            for (int i = locals.size() - 1; i >= 0; i--) {
                paramList = ParameterList.prepend(
                        new Argument(new Expression((new QualifiedName(
                                new LexTerm(LexTerm.ID, aclass
                                        .getOuterField(
                                                ((VariableDefinition) locals
                                                        .elementAt(i)).id(),
                                                c.forClass).id())))
                                .setLineInfoFrom(this))), paramList);
            }
            paramList = terms[2].joinParamLists(paramList);
            Term param = null;
            if (terms[0].notEmpty()) {
                param = terms[0];
            } else if (!aclass.isStaticClass()) {
                ClassDefinition outerClass = aclass.outerClass();
                ClassDefinition cd = ourClass;
                do {
                    cd = cd.outerClass();
                    assertCond(cd != null);
                } while (!outerClass.isAssignableFrom(cd, 0, c.forClass));
                param = (new This(new ClassOrIfaceType(cd)))
                        .setLineInfoFrom(this);
            }
            if (param != null) {
                paramList = ParameterList.prepend(new Argument(param),
                        paramList);
            }
            terms[2] = paramList;
            paramList.processPass1(c);
            if (terms[0].notEmpty()) {
                if (terms[0].exprType().objectSize() != Type.CLASSINTERFACE) {
                    fatalError(c,
                            "Illegal type of expression for qualified 'super'");
                }
                terms[0] = Empty.newTerm();
            }
            ObjVector parmSig = paramList.getSignature();
            md = aclass.matchConstructor(parmSig, c.forClass);
            curMethod = c.currentMethod;
            if (md != null) {
                assertCond(!md.isClassMethod());
                md.markUsed(aclass, true);
                md.incCallsCount(curMethod);
                md.processBranch(c, true);
                md.setArgsFormalType(paramList, c);
            } else {
                undefinedConstructor(aclass, parmSig, c);
            }
        }
    }

    void discoverObjLeaks() {
        assertCond(ourClass != null && curMethod != null);
        if (md != null) {
            md.copyObjLeaksTo(terms[2]);
        }
        terms[2].discoverObjLeaks();
        if (md != null) {
            if ((md.definingClass() != ourClass && !ourClass
                    .discoverInstanceObjLeaks()) || md.hasThisObjLeak(false)) {
                curMethod.setThisObjLeak(false);
            } else if ((md.definingClass() != ourClass && ourClass
                    .isInitThisStackObjVolatile())
                    || md.isThisStackObjVolatile()) {
                curMethod.setThisStackObjVolatile();
            }
        }
    }

    int tokenCount() {
        return md != null && md.definingClass() != ourClass
                && ourClass.hasInstanceInitializers() ? MethodDefinition.MAX_INLINE * 2
                : terms[2].tokenCount() + 1;
    }

    void processOutput(OutputContext oc) {
        assertCond(ourClass != null);
        if (!ourClass.isStaticClass()) {
            ourClass.outerThisRef().outerAssignment(oc, curMethod);
        }
        Enumeration en = ourClass.outerLocals(null).elements();
        while (en.hasMoreElements()) {
            ourClass.getOuterField(
                    ((VariableDefinition) en.nextElement()).id(), ourClass)
                    .outerAssignment(oc, curMethod);
        }
        if (md == null || md.definingClass().superClass() != null) {
            oc.cPrint("(");
            terms[2].produceRcvr(oc);
            oc.cPrint(md != null ? md.routineCName()
                    : MethodDefinition.UNKNOWN_NAME);
            oc.cPrint("(");
            if (md != null) {
                oc.cPrint("(");
                oc.cPrint(md.definingClass().castName());
                oc.cPrint(")");
            }
            oc.cPrint(This.CNAME);
            oc.parameterOutputAsArg(terms[2]);
            oc.cPrint("))");
            Main.dict.normalCalls++;
        }
        if (md != null && md.definingClass() != ourClass) {
            ourClass.writeInstanceInitCall(oc);
        }
    }

    ExpressionType traceClassInit() {
        assertCond(ourClass != null);
        terms[2].traceClassInit();
        if (md != null) {
            ObjVector parmTraceSig = null;
            if (terms[2].notEmpty()) {
                parmTraceSig = new ObjVector();
                terms[2].getTraceSignature(parmTraceSig);
            }
            md.methodTraceClassInit(
                    false,
                    md.definingClass().superClass() != null ? Main.dict.curTraceInfo
                            .curThisClass() : null, parmTraceSig);
            if (md.definingClass() != ourClass && ourClass.superClass() != null) {
                ourClass.instanceTraceClassInit();
            }
        }
        return null;
    }
}
