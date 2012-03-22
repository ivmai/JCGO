/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/CharacterLiteral.java --
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
 * Lexical term for character literals.
 */

final class CharacterLiteral extends LexTerm {

    private int decodedValue = -1;

    CharacterLiteral(String tokenval) {
        super(LexTerm.CHARACTER, tokenval);
    }

    ExpressionType exprType() {
        return Main.dict.classTable[Type.CHAR];
    }

    ConstValue evaluateConstValue() {
        return new ConstValue(decodeChar());
    }

    String strLiteralValueGuess() {
        int v = decodeChar();
        return v >= ' ' && v < 0x7f ? String.valueOf((char) v) : null;
    }

    private int decodeChar() {
        if (decodedValue == -1) {
            int[] posRef = new int[1];
            posRef[0] = 1;
            String str = dottedName();
            decodedValue = decodeCharAt(str, posRef);
        }
        return decodedValue;
    }

    static int decodeCharAt(String str, int[] posRef) {
        int pos = posRef[0];
        char ch = str.charAt(pos++);
        int len = str.length();
        int v = ch;
        if (ch == '\\' && pos < len) {
            ch = str.charAt(pos++);
            if (ch == 'u' && pos < len) {
                while (str.charAt(pos) == 'u') {
                    if (++pos >= len)
                        break;
                }
                v = 0;
                for (int j = 0; j < 4 && pos < len; j++) {
                    ch = str.charAt(pos);
                    if (ch < '0' || ch > '9') {
                        if (ch < 'A' || ch > 'F') {
                            if (ch < 'a' || ch > 'f')
                                break;
                            ch -= 'a' - 'A';
                        }
                        ch -= 'A' - '0' - 10;
                    }
                    pos++;
                    v = (v << 4) | (ch - '0');
                }
            } else if (ch >= '0' && ch <= '7') {
                v = ch - '0';
                char ch2;
                if (pos < len && (ch2 = str.charAt(pos)) >= '0' && ch2 <= '7') {
                    pos++;
                    v = (v << 3) | (ch2 - '0');
                    if (ch <= '3' && pos < len
                            && (ch2 = str.charAt(pos)) >= '0' && ch2 <= '7') {
                        pos++;
                        v = (v << 3) | (ch2 - '0');
                    }
                }
            } else {
                v = "btnvfr".indexOf(ch, 0) + 8;
                if (v < 8) {
                    v = ch;
                }
            }
        }
        posRef[0] = pos;
        return v;
    }

    static void appendJChar(StringBuffer sb, int v) {
        if (v != 0) {
            sb.append('(');
            sb.append(Type.cName[Type.CHAR]);
            sb.append(')');
        }
        if ((v & 0xff) != v) {
            sb.append("0x");
            sb.append(Integer.toHexString(v));
        } else {
            sb.append(Integer.toString(v));
        }
        if (v >= ' ' && v < 0x7f) {
            sb.append("\003/*'");
            if (v == '\\' || v == '\'') {
                sb.append('\\');
            }
            sb.append((char) v);
            sb.append("'*/\003");
        }
    }

    String stringOutput() {
        StringBuffer sb = new StringBuffer();
        appendJChar(sb, decodeChar());
        return sb.toString();
    }
}
