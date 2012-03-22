/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/MethodTraceInfo.java --
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
 * A traced method info record.
 */

final class MethodTraceInfo {

    private static final ObjVector EMPTY_VECTOR = new ObjVector();

    private/* final */MethodDefinition md;

    private/* final */ExpressionType curActualClass;

    private/* final */ObjVector parmTraceSig;

    private ObjVector classInitDepend;

    private ObjVector weakInitDepend;

    private boolean usesDynClasses;

    private ObjVector calledMethodInfos;

    private ObjVector weakMethodInfos;

    private ExpressionType resTraceType;

    private MethodTraceInfo(MethodDefinition md, ExpressionType curActualClass,
            ObjVector parmTraceSig) {
        this.md = md;
        this.curActualClass = curActualClass;
        this.parmTraceSig = parmTraceSig;
    }

    static MethodTraceInfo create(MethodDefinition md,
            ExpressionType curActualClass, ObjVector parmTraceSig) {
        ClassDefinition cd;
        if (curActualClass == null) {
            curActualClass = md.definingClass();
        } else if (curActualClass.objectSize() == Type.CLASSINTERFACE
                && (cd = curActualClass.signatureClass()) != curActualClass
                && !cd.hasInstantatedSubclasses(false)) {
            curActualClass = cd;
        }
        if (parmTraceSig != null
                && md.methodSignature().isSignEqual(parmTraceSig)) {
            parmTraceSig = null;
        }
        cd = md != null ? md.definingClass() : curActualClass.receiverClass();
        ObjHashtable knownMethodInfos;
        if ((knownMethodInfos = cd.knownMethodInfos) == null) {
            cd.knownMethodInfos = knownMethodInfos = new ObjHashtable();
        }
        String traceSig = getTraceShortSig(md, curActualClass, parmTraceSig);
        MethodTraceInfo traceInfo = (MethodTraceInfo) knownMethodInfos
                .get(traceSig);
        if (traceInfo == null) {
            knownMethodInfos.put(traceSig, traceInfo = new MethodTraceInfo(md,
                    curActualClass, parmTraceSig));
        }
        return traceInfo;
    }

    private static String getTraceShortSig(MethodDefinition md,
            ExpressionType curActualClass, ObjVector parmTraceSig) {
        return md != null ? md.methodSignature().signatureString()
                + (md.definingClass() != curActualClass ? curActualClass
                        .getJavaSignature() : "")
                + (parmTraceSig != null ? new MethodSignature(md.id(),
                        parmTraceSig).getJavaSignature() : "") : "<clinit>";
    }

    String getTraceSig() {
        return getDefiningClassName() + "."
                + getTraceShortSig(md, curActualClass, parmTraceSig);
    }

    boolean addToProcessed(ObjHashtable processed) {
        ClassDefinition cd = md != null ? md.definingClass() : curActualClass
                .receiverClass();
        ObjHashSet traceInfos = (ObjHashSet) processed.get(cd);
        if (traceInfos == null) {
            processed.put(cd, traceInfos = new ObjHashSet());
        }
        return traceInfos.add(this);
    }

    boolean isCallableNow() {
        return md == null || md.isClassMethod() || md.isConstructor()
                || !curActualClass.receiverClass().noInstanceYetOnTrace();
    }

    void setInstanceCreated() {
        if (md != null && md.isConstructor()) {
            md.definingClass().instanceCreatedOnTrace();
        }
    }

    ExpressionType curThisClass() {
        return md != null && !md.isClassMethod() ? curActualClass : null;
    }

    String getDefiningClassName() {
        return (md != null ? md.definingClass() : curActualClass).name();
    }

    void setUsesDynClasses() {
        Term.assertCond(resTraceType == null);
        usesDynClasses = true;
    }

    void addClassInitDepend(boolean isWeak, ClassDefinition cd) {
        Term.assertCond(resTraceType == null);
        if (classInitDepend == null
                || classInitDepend.identityLastIndexOf(cd) < 0) {
            int i = weakInitDepend != null ? weakInitDepend
                    .identityLastIndexOf(cd) : -1;
            if (isWeak) {
                if (i < 0) {
                    if (weakInitDepend == null) {
                        weakInitDepend = new ObjVector();
                    }
                    weakInitDepend.addElement(cd);
                }
            } else {
                if (classInitDepend == null) {
                    classInitDepend = new ObjVector();
                }
                classInitDepend.addElement(cd);
                if (i >= 0) {
                    weakInitDepend.removeElementAt(i);
                }
            }
        }
    }

    void addMethodCall(MethodTraceInfo traceInfo) {
        Term.assertCond(resTraceType == null);
        if (calledMethodInfos == null
                || calledMethodInfos.identityLastIndexOf(traceInfo) < 0) {
            int i = weakMethodInfos != null ? weakMethodInfos
                    .identityLastIndexOf(traceInfo) : -1;
            if (Main.dict.classInitWeakDepend) {
                if (i < 0) {
                    if (weakMethodInfos == null) {
                        weakMethodInfos = new ObjVector();
                    }
                    weakMethodInfos.addElement(traceInfo);
                }
            } else {
                if (calledMethodInfos == null) {
                    calledMethodInfos = new ObjVector();
                }
                calledMethodInfos.addElement(traceInfo);
                if (i >= 0) {
                    weakMethodInfos.removeElementAt(i);
                }
            }
        }
    }

    void doneMethodTracing(ExpressionType resTraceType) {
        Term.assertCond(this.resTraceType == null && resTraceType != null);
        this.resTraceType = resTraceType;
    }

    boolean isNotTraced() {
        return resTraceType == null;
    }

    void traceMethod() {
        if (resTraceType == null && Main.dict.inProgressMethodInfos.add(this)) {
            Term.assertCond(md != null);
            MethodTraceInfo oldCurTraceInfo = Main.dict.curTraceInfo;
            if (Main.dict.verboseTracing) {
                Main.dict.message("Tracing: " + getTraceSig());
            }
            Main.dict.curTraceInfo = this;
            boolean oldClassInitWeakDepend = Main.dict.classInitWeakDepend;
            Main.dict.classInitWeakDepend = false;
            MethodDefinition oldCurHelperForMethod = Main.dict.curHelperForMethod;
            Main.dict.curHelperForMethod = null;
            ExpressionType curTraceType = md.traceBody(parmTraceSig);
            Main.dict.curHelperForMethod = oldCurHelperForMethod;
            Main.dict.classInitWeakDepend = oldClassInitWeakDepend;
            doneMethodTracing(curTraceType != null ? curTraceType : md
                    .exprType());
            Main.dict.curTraceInfo = oldCurTraceInfo;
            Main.dict.inProgressMethodInfos.remove(this);
            Main.dict.tracedInfosCount++;
        }
    }

    ExpressionType getResTraceType() {
        return resTraceType != null ? resTraceType : md != null ? md
                .actualExprType() : null;
    }

    boolean usesDynClasses() {
        Term.assertCond(resTraceType != null);
        return usesDynClasses;
    }

    ClassDefinition getClassDependElementAt(boolean isWeak, int index) {
        Term.assertCond(resTraceType != null);
        ObjVector classDepend = isWeak ? weakInitDepend : classInitDepend;
        if (classDepend != null && classDepend.size() > index) {
            do {
                ClassDefinition cd = (ClassDefinition) classDepend
                        .elementAt(index);
                if (cd.classInitializerNotCalledYet())
                    return cd;
                classDepend.removeElementAt(index);
            } while (classDepend.size() > index);
            if (index == 0) {
                if (isWeak) {
                    weakInitDepend = null;
                } else {
                    classInitDepend = null;
                }
            }
        }
        return null;
    }

    ClassDefinition getDefiningClassDepend() {
        Term.assertCond(resTraceType != null);
        ClassDefinition cd;
        return md != null && (md.isConstructor() || md.isClassMethod())
                && (cd = md.definingClass()).classInitializerNotCalledYet() ? cd
                : null;
    }

    Enumeration getCalledInfosElements(boolean isWeak) {
        Term.assertCond(resTraceType != null);
        ObjVector methodInfos = isWeak ? weakMethodInfos : calledMethodInfos;
        return (methodInfos != null ? methodInfos : EMPTY_VECTOR).elements();
    }
}
