/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/StringLiteral.java --
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
 * Lexical term for a string literal.
 */

final class StringLiteral extends LexTerm {

    private static/* final */int MAX_JOINED_LEN = 0x7FE0;

    private String textVal;

    private String cname;

    private ClassDefinition ourClass;

    private ClassDefinition dynClass;

    StringLiteral(String tokenval) {
        super(LexTerm.STRING, tokenval);
    }

    StringLiteral asStrLiteral() {
        return this;
    }

    boolean appendLiteralTo(Term t) {
        if (cname == null) {
            StringLiteral liter = t.asStrLiteral();
            if (liter == null)
                return false;
            if (textVal == null) {
                String str = super.dottedName();
                textVal = normalizeStr(str.substring(1, str.length() - 1));
            }
            String text2 = liter.textVal;
            if (text2 == null) {
                text2 = liter.dottedName();
                text2 = normalizeStr(text2.substring(1, text2.length() - 1));
            }
            text2 = text2 + textVal;
            if (text2.length() < MAX_JOINED_LEN) {
                liter.textVal = text2;
                textVal = "";
            }
        }
        return true;
    }

    String dottedName() {
        return textVal != null ? "\"" + textVal + "\"" : super.dottedName();
    }

    void processPass1(Context c) {
        ourClass = c.currentClass;
        String str = super.dottedName();
        str = normalizeStr(str.substring(1, str.length() - 1));
        if (textVal == null) {
            textVal = str;
        }
        if ((str = str.trim()).length() > 0
                && (dynClass = Main.dict.dynamicDefineClass(str, ourClass,
                        c.forClass)) != null) {
            processDynClass(dynClass);
        }
    }

    ExpressionType exprType() {
        assertCond(ourClass != null);
        return Main.dict.get(Names.JAVA_LANG_STRING);
    }

    int countConcatStrs() {
        assertCond(ourClass != null);
        return textVal.length() > 0 ? 1 : 0;
    }

    boolean isNotNull() {
        return true;
    }

    String strLiteralValueGuess() {
        String str = textVal;
        assertCond(str != null);
        int strlen = str.length();
        if (strlen == 0)
            return null;
        char ch;
        for (int i = 0; i < strlen; i++) {
            if ((ch = str.charAt(i)) < ' ' || ch >= 0x7f || ch == '\\')
                return null;
        }
        return str;
    }

    String stringOutput() {
        assertCond(ourClass != null);
        if (cname == null) {
            cname = Main.dict.addStringLiteral(textVal, ourClass)
                    .stringOutput();
        }
        return cname;
    }

    private static void processDynClass(ClassDefinition dynClass) {
        ClassDefinition sc = dynClass.superClass();
        if (sc != null
                && (sc.name().equals(Names.JAVA_UTIL_LISTRESOURCEBUNDLE) || sc
                        .name()
                        .equals(Names.COM_IVMAISOFT_JPROPJAV_STRLISTRESOURCEBUNDLE))) {
            String baseName = dynClass.name();
            if (Main.dict.dynamicDefineClass(baseName + "_en", null, dynClass) != null
                    && Main.dict.dynamicDefineClass(baseName + "_en_US", null,
                            dynClass) != null) {
                Main.dict.dynamicDefineClass(baseName + "_en_US_POSIX", null,
                        dynClass);
            }
        }
    }

    private static String normalizeStr(String str) {
        if (str.indexOf('\\') < 0)
            return str;
        int strlen = str.length();
        StringBuffer sb = new StringBuffer(strlen);
        int[] posRef = new int[1];
        while (posRef[0] < strlen) {
            encodeStrChar(sb, CharacterLiteral.decodeCharAt(str, posRef));
        }
        return sb.toString();
    }

    private static void encodeStrChar(StringBuffer sb, int v) {
        if (v >= ' ' && v < 0x7f) {
            if (v == '"' || v == '\\') {
                sb.append('\\');
            }
            sb.append((char) v);
        } else {
            sb.append('\\');
            if (v <= 0xff) {
                if (v == '\n') {
                    sb.append('n');
                } else {
                    sb.append((char) ((v >> 6) + '0'));
                    sb.append((char) (((v >> 3) & 0x7) + '0'));
                    sb.append((char) ((v & 0x7) + '0'));
                }
            } else {
                sb.append('u');
                if (v <= 0xfff) {
                    sb.append('0');
                }
                sb.append(Integer.toHexString(v));
            }
        }
    }

    ExpressionType traceClassInit() {
        if (dynClass != null) {
            Main.dict.addDynClassToTrace(dynClass);
        }
        return null;
    }
}
