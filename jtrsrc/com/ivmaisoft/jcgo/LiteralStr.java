/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/LiteralStr.java --
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
 * A string literal object.
 */

final class LiteralStr {

    private/* final */String suffix;

    private/* final */int count;

    private/* final */int hash;

    private ArrayLiteral arrLiter;

    private int ofs;

    LiteralStr(String str, ClassDefinition ourClass) {
        suffix = ourClass.nextLiteralSuffix();
        int strlen = str.length();
        StringBuffer sb = new StringBuffer(strlen << 2);
        int count = 0;
        int[] posRef = new int[1];
        boolean isPacked = false;
        int hash = 0;
        if (Main.dict.allowPackedStr()
                && !Names.GNU_JAVA_LANG_CHARDATA.equals(ourClass.name())) {
            while (posRef[0] < strlen) {
                int v = CharacterLiteral.decodeCharAt(str, posRef);
                if ((v & 0xff) != v) {
                    count = 0;
                    break;
                }
                sb.append('(');
                sb.append(Type.cName[Type.BYTE]);
                sb.append(')');
                CharacterLiteral.appendJChar(sb, v);
                hash = v + (hash << 5) - hash;
                count++;
                sb.append(", ");
            }
            if (count > 0) {
                isPacked = true;
            } else {
                sb.setLength(0);
                posRef[0] = 0;
                hash = 0;
            }
        }
        while (posRef[0] < strlen) {
            int v = CharacterLiteral.decodeCharAt(str, posRef);
            CharacterLiteral.appendJChar(sb, v);
            hash = v + (hash << 5) - hash;
            count++;
            sb.append(", ");
        }
        this.count = count;
        this.hash = hash;
        arrLiter = new ArrayLiteral(Main.dict.classTable[isPacked ? Type.BYTE
                : Type.CHAR], sb.toString(), count, false);
        Main.dict.putCharSubArray(arrLiter);
    }

    String stringOutput() {
        return "JCGO_STRREF_OF(jcgo_string" + suffix + ")";
    }

    void initArrayLiteral(ClassDefinition ourClass) {
        if (!arrLiter.isInitialized()) {
            int[] ofsRef = new int[1];
            ArrayLiteral liter = Main.dict.searchCharSubArray(ofsRef, arrLiter,
                    ourClass);
            if (arrLiter != liter) {
                ofs = ofsRef[0];
                if (liter.isInitialized()) {
                    arrLiter = liter;
                    return;
                }
            }
            arrLiter = Main.dict.addArrayLiteral(liter, ourClass, true);
        }
    }

    void processOutput(OutputContext oc) {
        Term.assertCond(arrLiter != null);
        String lenstr = Integer.toString(count);
        ClassDefinition cd = Main.dict.get(Names.JAVA_LANG_STRING);
        boolean useConst = false;
        if (Main.dict.allowConstStr >= 0) {
            oc.hPrint("JCGO_SEP_EXTERN");
            oc.cPrint("JCGO_NOSEP_DATA");
            if (Main.dict.allowConstStr > 0
                    && ((hash & 0xffff) != 0 || !Main.dict.fillStrHash)) {
                oc.cAndHPrint(" JCGO_NOTHR_CONST");
                useConst = true;
            }
        } else {
            oc.hPrint("JCGO_SEP_GCEXTERN");
            oc.cPrint("JCGO_NOSEP_GCDATA");
        }
        oc.cAndHPrint(" struct ");
        oc.cAndHPrint(cd.castName());
        oc.cAndHPrint("_s jcgo_string");
        oc.cAndHPrint(suffix);
        oc.cPrint(useConst ? " JCGO_THRD_ATTRNONGC"
                : Main.dict.allowConstStr >= 0 ? " ATTRIBNONGC"
                        : " ATTRIBGCDATA");
        oc.cPrint("={&");
        oc.cPrint(cd.vTableCName());
        oc.cPrint(",\010");
        oc.cPrint("JCGO_MON_INIT\010");
        oc.cPrint("(");
        oc.cPrint(Main.dict.getStringValueCastName());
        oc.cPrint(")JCGO_OBJREF_OF(");
        oc.cPrint(arrLiter.cname());
        oc.cPrint("),\010");
        oc.cPrint(Integer.toString(ofs));
        oc.cPrint(", ");
        oc.cPrint(lenstr);
        if (Main.dict.fillStrHash) {
            oc.cPrint(", (");
            oc.cPrint(Type.cName[Type.INT]);
            oc.cPrint(")0x");
            oc.cPrint(Integer.toHexString(hash));
            oc.cPrint("L");
        }
        oc.cPrint("}");
        oc.cAndHPrint(";\n\n");
    }
}
