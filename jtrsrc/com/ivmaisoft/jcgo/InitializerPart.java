/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/InitializerPart.java --
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
 * This class is for an instance or static initializer code fragment.
 */

final class InitializerPart {

    private/* final */InitializerPart prev;

    private/* final */Term term;

    private String code;

    private VariableDefinition v;

    private int[] curRcvrs0;

    InitializerPart(InitializerPart prev, Term term) {
        Term.assertCond(term != null);
        this.prev = prev;
        this.term = term;
    }

    boolean isJavaConstant(ClassDefinition ourClass) {
        return term.isJavaConstant(ourClass)
                && (prev == null || prev.isJavaConstant(ourClass));
    }

    void setUsedVar(VariableDefinition v) {
        this.v = v;
    }

    boolean isPrevSafeWithThrow() {
        return prev == null
                || (prev.isSafeWithThrow() && prev.isPrevSafeWithThrow());
    }

    private boolean isSafeWithThrow() {
        return v != null ? term.isSafeWithThrow() : !term.isBlock();
    }

    void setCode(String code, int[] curRcvrs) {
        this.code = code;
        curRcvrs0 = curRcvrs;
    }

    boolean hasSomeCode() {
        return code != null || (v != null && v.hasSomeCode())
                || (prev != null && prev.hasSomeCode());
    }

    void allocRcvr(int[] curRcvrs) {
        if (prev != null) {
            prev.allocRcvr(curRcvrs);
        }
        if (curRcvrs0 != null) {
            OutputContext.joinRcvrs(curRcvrs, curRcvrs0);
        }
    }

    void discoverObjLeaks() {
        if (prev != null) {
            prev.discoverObjLeaks();
        }
        if (code != null || v != null) {
            term.discoverObjLeaks();
            if (v != null && v.exprType().objectSize() >= Type.CLASSINTERFACE) {
                term.setObjLeaks(v);
            }
        }
    }

    void processOutput(OutputContext oc) {
        if (prev != null) {
            prev.processOutput(oc);
        }
        if (code != null) {
            if (v != null) {
                if (v.used()) {
                    oc.cPrint(v.stringOutput(false, true));
                    oc.cPrint("= ");
                    oc.cPrint(code);
                } else {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[Type.VOID]);
                    oc.cPrint(")(");
                    oc.cPrint(code);
                    oc.cPrint(")");
                }
                oc.cPrint(";");
            } else {
                term.processOutput(oc);
            }
        }
    }

    void traceClassInit() {
        if (prev != null) {
            prev.traceClassInit();
        }
        if (code != null) {
            term.traceClassInit();
        }
    }
}
