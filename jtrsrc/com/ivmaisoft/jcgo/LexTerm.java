/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/LexTerm.java --
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
 * This is a lexical token object.
 */

class LexTerm extends Term {

    static final int CHARACTER = 51;

    static final int FLOATINGPOINT = 52;

    static final int ID = 53;

    static final int NUMBER = 54;

    static final int STRING = 55;

    static final int AND = 106;

    static final int BITAND = 107;

    static final int BITAND_EQUALS = 108;

    static final int BITNOT = 109;

    static final int BITOR = 110;

    static final int BITOR_EQUALS = 111;

    static final int DECREMENT = 112;

    static final int DIVIDE = 113;

    static final int DIVIDE_EQUALS = 114;

    static final int EQ = 115;

    static final int EQUALS = 116;

    static final int FALSE = 117;

    static final int FILLSHIFT_RIGHT = 118;

    static final int FLSHIFT_EQUALS = 119;

    static final int GE = 120;

    static final int GT = 121;

    static final int INCREMENT = 122;

    static final int LE = 123;

    static final int LT = 124;

    static final int MINUS = 125;

    static final int MINUS_EQUALS = 126;

    static final int MOD = 127;

    static final int MOD_EQUALS = 128;

    static final int NE = 129;

    static final int NOT = 130;

    static final int OR = 131;

    static final int PLUS = 132;

    static final int PLUS_EQUALS = 133;

    static final int SHIFT_LEFT = 134;

    static final int SHIFT_RIGHT = 135;

    static final int SHLEFT_EQUALS = 136;

    static final int SHRIGHT_EQUALS = 137;

    static final int TIMES = 138;

    static final int TIMES_EQUALS = 139;

    static final int TRUE = 140;

    static final int XOR = 141;

    static final int XOR_EQUALS = 142;

    static final int xNULL = 143;

    static/* final */String NULL_STR = "jnull";

    private/* final */int sym;

    private/* final */String tokenval;

    LexTerm(int sym, String tokenval) {
        if (sym != STRING && tokenval.indexOf('\\') >= 0) {
            tokenval = unUnicodeStr(tokenval);
        }
        this.sym = sym;
        this.tokenval = tokenval;
    }

    private static String unUnicodeStr(String str) {
        int len = str.length();
        StringBuffer sb = new StringBuffer(len);
        int pos = 0;
        while (pos < len) {
            char ch = str.charAt(pos++);
            if (ch == '\\') {
                if (len - 4 > pos && str.charAt(pos) == 'u') {
                    int oldPos = pos;
                    pos++;
                    while (str.charAt(pos) == 'u') {
                        if (++pos >= len)
                            break;
                    }
                    int v = 0;
                    for (int j = 0; j < 4; j++) {
                        if (pos >= len) {
                            v = '\\';
                            pos = oldPos;
                            break;
                        }
                        ch = str.charAt(pos);
                        if (ch < '0' || ch > '9') {
                            if (ch < 'A' || ch > 'F') {
                                if (ch < 'a' || ch > 'f') {
                                    v = '\\';
                                    pos = oldPos;
                                    break;
                                }
                                ch -= 'a' - 'A';
                            }
                            ch -= 'A' - '0' - 10;
                        }
                        pos++;
                        v = (v << 4) | (ch - '0');
                    }
                    ch = (char) v;
                } else {
                    sb.append('\\');
                    if (pos >= len)
                        break;
                    ch = str.charAt(pos++);
                }
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    final int getSym() {
        return sym;
    }

    String dottedName() {
        return tokenval;
    }

    final void storeDottedName(ObjVector v) {
        v.addElement(tokenval);
    }

    final boolean isJavaConstant(ClassDefinition ourClass) {
        return sym != xNULL;
    }

    ExpressionType exprType() {
        return Main.dict.classTable[sym == xNULL ? Type.NULLREF : sym == FALSE
                || sym == TRUE ? Type.BOOLEAN : Type.VOID];
    }

    ConstValue evaluateConstValue() {
        return sym == FALSE || sym == TRUE || sym == xNULL ? new ConstValue(
                sym == TRUE) : null;
    }

    final boolean isLiteral() {
        return true;
    }

    final void processOutput(OutputContext oc) {
        oc.cPrint(stringOutput());
    }

    String stringOutput() {
        if (sym == xNULL)
            return NULL_STR;
        if (sym == FALSE || sym == TRUE)
            return outputBoolean(sym == TRUE);
        return tokenval;
    }

    static String outputBoolean(boolean isTrue) {
        return isTrue ? "jtrue" : "jfalse";
    }

    final boolean isAtomary() {
        return true;
    }
}
