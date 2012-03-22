/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/MethodProxy.java --
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
 * A proxy function generator for reflected methods.
 */

final class MethodProxy {

    private MethodDefinition md;

    private String csign;

    MethodProxy(MethodDefinition md) {
        this.md = md;
        StringBuffer sb = new StringBuffer();
        int s0 = md.exprType().objectSize();
        sb.append(Type.sig[s0 < Type.CLASSINTERFACE ? s0 : Type.CLASSINTERFACE]);
        if (!md.isClassMethod()) {
            sb.append('T');
        }
        Enumeration en = md.methodSignature().elements();
        while (en.hasMoreElements()) {
            s0 = ((ExpressionType) en.nextElement()).objectSize();
            sb.append(Type.sig[s0 < Type.CLASSINTERFACE ? s0
                    : Type.CLASSINTERFACE]);
        }
        csign = sb.toString();
    }

    String csign() {
        return csign;
    }

    String stringOutput() {
        return "jcgo_mproxy_" + csign;
    }

    void processOutput(OutputContext oc) {
        oc.cAndHPrint("JCGO_NOSEP_STATIC ");
        oc.cAndHPrint(Type.cName[Type.CLASSINTERFACE]);
        oc.cAndHPrint(" CFASTCALL\n");
        oc.cAndHPrint("jcgo_mproxy_");
        oc.cAndHPrint(csign);
        oc.cAndHPrint("( ");
        oc.cAndHPrint(Type.cName[Type.VOID]);
        oc.cAndHPrint(" (CFASTCALL *jmethod)( ");
        oc.cAndHPrint(Type.cName[Type.VOID]);
        oc.cAndHPrint(" ), ");
        oc.cAndHPrint(Type.cName[Type.CLASSINTERFACE]);
        oc.cAndHPrint(" obj, ");
        for (int type = Type.INT; type <= Type.DOUBLE; type++) {
            oc.cAndHPrint(Type.cName[type + Type.CLASSINTERFACE]);
            oc.cAndHPrint(" ");
            oc.cAndHPrint(Type.name[type]);
            oc.cAndHPrint("Args, ");
        }
        oc.cAndHPrint(Type.cName[Type.OBJECTARRAY]);
        oc.cAndHPrint(" objectArgs )");
        oc.hPrint(";\n\n");
        oc.cPrint("{");
        ExpressionType resType = md.exprType();
        int s0 = resType.objectSize();
        if (s0 < Type.CLASSINTERFACE) {
            if (s0 != Type.VOID) {
                oc.cPrint("JCGO_ARR_INTERNALACC(");
                oc.cPrint(Type.cName[s0 > Type.INT ? s0 : Type.INT]);
                oc.cPrint(", ");
                oc.cPrint(Type.name[s0 > Type.INT ? s0 : Type.INT]);
                oc.cPrint("Args, 0)= ");
                if (s0 == Type.CHAR) {
                    oc.cPrint("(");
                    oc.cPrint(Type.cName[Type.INT]);
                    oc.cPrint(")");
                }
            }
            oc.cPrint("((");
            oc.cPrint(Type.cName[s0]);
        } else {
            oc.cPrint("return ((");
            oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
        }
        oc.cPrint(" (CFASTCALL*)( ");
        boolean next = false;
        if (!md.isClassMethod()) {
            oc.cPrint(Type.cName[Type.CLASSINTERFACE]);
            next = true;
        }
        MethodSignature msig = null;
        if (md.hasParameters()) {
            msig = md.methodSignature();
            int type = Type.VOID;
            do {
                int type2 = type == Type.LONG ? Type.FLOAT
                        : type == Type.FLOAT ? Type.LONG : type;
                Enumeration en = msig.elements();
                while (en.hasMoreElements()) {
                    int s = ((ExpressionType) en.nextElement()).objectSize();
                    if (type2 == s
                            || (type == Type.VOID && s >= Type.CLASSINTERFACE)) {
                        if (next) {
                            oc.cPrint(", ");
                        }
                        oc.cPrint(Type.cName[s < Type.CLASSINTERFACE ? s
                                : Type.CLASSINTERFACE]);
                        next = true;
                    }
                }
            } while (--type > Type.NULLREF);
        }
        if (!next) {
            oc.cPrint(Type.cName[Type.VOID]);
        }
        oc.cPrint(" ))jmethod)(");
        next = false;
        if (!md.isClassMethod()) {
            oc.cPrint("obj");
            next = true;
        }
        if (msig != null) {
            int[] indices = new int[Type.CLASSINTERFACE - Type.INT + 1];
            int type = Type.VOID;
            do {
                int type2 = type == Type.LONG ? Type.FLOAT
                        : type == Type.FLOAT ? Type.LONG : type;
                indices[0] = 0;
                Enumeration en = msig.elements();
                while (en.hasMoreElements()) {
                    int s = ((ExpressionType) en.nextElement()).objectSize();
                    if (type2 == s
                            || (type == Type.VOID && s >= Type.CLASSINTERFACE)) {
                        if (next) {
                            oc.cPrint(", ");
                        }
                        boolean isBool = false;
                        if (s < Type.INT) {
                            oc.cPrint("(");
                            oc.cPrint(Type.cName[s]);
                            oc.cPrint(")");
                            if (s == Type.BOOLEAN) {
                                oc.cPrint("(");
                                isBool = true;
                            }
                            s = Type.INT;
                        } else if (s >= Type.CLASSINTERFACE) {
                            s = Type.CLASSINTERFACE;
                        }
                        oc.cPrint("JCGO_ARR_INTERNALACC(");
                        oc.cPrint(Type.cName[s]);
                        oc.cPrint(", ");
                        oc.cPrint(s != Type.CLASSINTERFACE ? Type.name[s]
                                : "object");
                        oc.cPrint("Args, ");
                        oc.cPrint(Integer.toString(indices[s - Type.INT]++));
                        oc.cPrint(")");
                        if (isBool) {
                            oc.cPrint("!= 0)");
                        }
                        next = true;
                    } else if (s <= Type.INT) {
                        indices[0]++;
                    }

                }
            } while (--type > Type.NULLREF);
        }
        oc.cPrint(")");
        if (s0 == Type.BOOLEAN) {
            oc.cPrint("? 1\003 :\003 0");
        }
        oc.cPrint(";");
        if (s0 < Type.CLASSINTERFACE) {
            oc.cPrint("return ");
            oc.cPrint(LexTerm.NULL_STR);
            oc.cPrint(";");
        }
        oc.cPrint("}\n\n");
    }
}
