/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/OutputContext.java --
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
 * An output context object for C source and header files.
 */

final class OutputContext {

    private CFormattedStream cstream;

    private CFormattedStream hStream;

    private int stackObjCount;

    private final StringBuffer instanceSBuf = new StringBuffer();

    int arrInitCount = -1;

    int arrInitLevel;

    boolean insideNotSehTry;

    ExpressionType assignmentRightType;

    VariableDefinition assignmentVar;

    OutputContext() {
    }

    OutputContext(String shortname) {
        cstream = new CFormattedStream(shortname + ".c");
        hStream = new CFormattedStream(shortname + ".h");
    }

    void arrayIndent() {
        cPrint("\010");
        int level = arrInitLevel;
        for (int i = 0; i < level; i++) {
            cPrint(" ");
        }
    }

    void cPrint(String s) {
        if (cstream != null) {
            cstream.print(s);
        } else {
            instanceSBuf.append(s);
        }
    }

    void hPrint(String s) {
        Term.assertCond(hStream != null);
        hStream.print(s);
    }

    void cAndHPrint(String s) {
        cPrint(s);
        hPrint(s);
    }

    void addIncludeCFile(String shortname) {
        cPrint("#include \"");
        cPrint(shortname);
        cPrint(".c\"\010");
    }

    void addIncludeHFile(String shortname) {
        hPrint("#include \"");
        hPrint(shortname);
        hPrint(".h\"\010");
    }

    static int[] copyRcvrs(int[] curRcvrs) {
        int[] curRcvrs2 = new int[Type.VOID];
        System.arraycopy(curRcvrs, 0, curRcvrs2, 0, Type.VOID);
        return curRcvrs2;
    }

    static void joinRcvrs(int[] curRcvrs, int[] curRcvrs2) {
        for (int i = 0; i < Type.VOID; i++) {
            int rcvr = curRcvrs2[i];
            if (curRcvrs[i] < rcvr) {
                curRcvrs[i] = rcvr;
            }
        }
    }

    void writeRcvrsVar(int[] curRcvrs) {
        writeRcvrsVar(curRcvrs, Type.DOUBLE);
        writeRcvrsVar(curRcvrs, Type.LONG);
        writeRcvrsVar(curRcvrs, Type.NULLREF);
        writeRcvrsVar(curRcvrs, Type.FLOAT);
        int type = Type.INT;
        do {
            writeRcvrsVar(curRcvrs, type);
        } while (--type > Type.NULLREF);
    }

    static String getRcvrName(int rcvr, int type) {
        Term.assertCond(rcvr > 0);
        return "jcgo_rcvr"
                + Type.sig[type < Type.CLASSINTERFACE && type != Type.NULLREF ? type
                        : Type.CLASSINTERFACE] + Integer.toString(rcvr);
    }

    private void writeRcvrsVar(int[] curRcvrs, int type) {
        int cnt = curRcvrs[type];
        for (int rcvr = 1; rcvr <= cnt; rcvr++) {
            cPrint(Type.cName[type]);
            cPrint(" ");
            cPrint(getRcvrName(rcvr, type));
            cPrint(";");
        }
    }

    void stackObjCountReset() {
        stackObjCount = 0;
    }

    String nextStackObjName() {
        return "jcgo_stackobj" + Integer.toString(++stackObjCount);
    }

    private void parameterOutput(Term paramList, boolean asArg) {
        paramList.parameterOutput(this, asArg, Type.NULLREF);
        paramList.parameterOutput(this, asArg, Type.DOUBLE);
        paramList.parameterOutput(this, asArg, Type.LONG);
        paramList.parameterOutput(this, asArg, Type.FLOAT);
        int type = Type.INT;
        do {
            paramList.parameterOutput(this, asArg, type);
        } while (--type > Type.NULLREF);
    }

    void parameterOutputAsArg(Term paramList) {
        if (paramList.notEmpty()) {
            parameterOutput(paramList, true);
        }
    }

    static String paramStringOutputNoComma(Term paramList, boolean asArg) {
        if (paramList.notEmpty()) {
            OutputContext oc = new OutputContext();
            oc.parameterOutput(paramList, asArg);
            String str = oc.instanceToString();
            if (str.length() > 1)
                return str.substring(2);
        }
        return "";
    }

    void instancePrint(String s) {
        Term.assertCond(hStream != null);
        instanceSBuf.append(s);
    }

    void fileClose() {
        Term.assertCond(hStream != null);
        cstream.fileClose();
        hStream.fileClose();
    }

    void hCloseOnly() {
        Term.assertCond(hStream != null);
        hStream.close();
    }

    void close() {
        Term.assertCond(hStream != null);
        cstream.close();
        hStream.close();
        cstream = null;
        hStream = null;
    }

    String instanceToString() {
        return instanceSBuf.toString();
    }
}
