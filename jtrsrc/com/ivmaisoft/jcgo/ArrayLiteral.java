/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ArrayLiteral.java --
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
 * An array literal object.
 */

final class ArrayLiteral {

    private/* final */ExpressionType exprType;

    private/* final */String data;

    private/* final */int count;

    private/* final */boolean isWritable;

    private boolean isNotSharable;

    private String suffix;

    ArrayLiteral(ExpressionType exprType, String data, int count,
            boolean isWritable) {
        this.exprType = exprType;
        this.data = data;
        this.count = count;
        this.isWritable = isWritable;
    }

    String getData() {
        return data;
    }

    boolean isWritable() {
        return isWritable;
    }

    boolean isByteArray() {
        return exprType.objectSize() == Type.BYTE;
    }

    boolean isCharArray() {
        return exprType.objectSize() == Type.CHAR;
    }

    void setNotSharable() {
        isNotSharable = true;
    }

    boolean isNotSharable() {
        return isNotSharable;
    }

    boolean isInitialized() {
        return suffix != null;
    }

    void initSuffix(ClassDefinition ourClass) {
        Term.assertCond(suffix == null);
        suffix = ourClass.nextLiteralSuffix();
    }

    String cname() {
        Term.assertCond(suffix != null);
        return "jcgo_array" + suffix;
    }

    String stringOutput() {
        return "(("
                + exprType.signatureClass()
                        .asExprType(exprType.signatureDimensions() + 1)
                        .castName() + ")JCGO_OBJREF_OF(" + cname() + "))";
    }

    int searchSubArray(ArrayLiteral liter) {
        String otherData = liter.data;
        int otherLen = otherData.length();
        if (otherLen == 0)
            return 0;
        int pos = 0;
        for (int ofs = 0; data.length() - pos >= otherLen; ofs++) {
            if (data.regionMatches(pos, otherData, 0, otherLen))
                return ofs;
            pos = dataSkipComma(pos);
        }
        return -1;
    }

    int dataSkipComma(int pos) {
        do {
            pos = data.indexOf(',', pos);
            if (pos < 0)
                return data.length();
            pos += 2;
        } while (data.length() > pos && data.charAt(pos - 1) != ' ');
        return pos;
    }

    void processOutput(OutputContext oc, ClassDefinition ourClass) {
        Term.assertCond(suffix != null && (!isWritable || isNotSharable));
        int s0 = exprType.objectSize();
        String arrtype = Main.dict
                .addArrayTypeDefn(s0, count, ourClass, suffix);
        if (s0 < Type.CLASSINTERFACE || !isWritable || count == 0) {
            oc.hPrint("JCGO_SEP_EXTERN ");
            oc.cPrint("JCGO_NOSEP_DATA ");
            if (!isWritable) {
                oc.cAndHPrint("CONST ");
            } else if (count == 0) {
                oc.cAndHPrint("JCGO_NOTHR_CONST ");
            }
        } else {
            oc.hPrint("JCGO_SEP_GCEXTERN ");
            oc.cPrint("JCGO_NOSEP_GCDATA ");
        }
        oc.cAndHPrint(arrtype);
        oc.cAndHPrint(" ");
        oc.cAndHPrint(cname());
        if (isWritable) {
            oc.cPrint(count == 0 ? " JCGO_THRD_ATTRNONGC"
                    : s0 < Type.CLASSINTERFACE ? " ATTRIBNONGC"
                            : " ATTRIBGCDATA");
        }
        oc.cPrint("={(jvtable)&");
        oc.cPrint(ClassDefinition.arrayVTableCName(
                s0 < Type.CLASSINTERFACE ? s0 : Type.VOID,
                exprType.signatureDimensions()));
        oc.cPrint(",\010");
        oc.cPrint("JCGO_MON_INIT\010");
        oc.cPrint(Integer.toString(count));
        oc.cPrint(",\010");
        if (s0 >= Type.CLASSINTERFACE) {
            oc.cPrint(exprType.signatureClass().getClassRefStr(false));
            oc.cPrint(",\010");
        }
        oc.cPrint("{");
        oc.cPrint(count > 0 ? data.substring(0, data.lastIndexOf(','))
                : s0 < Type.CLASSINTERFACE ? "(" + Type.cName[s0] + ")0"
                        : LexTerm.NULL_STR);
        oc.cPrint("}}");
        oc.cAndHPrint(";\n\n");
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof ArrayLiteral) {
            ArrayLiteral liter = (ArrayLiteral) obj;
            if (exprType == liter.exprType && data.equals(liter.data))
                return true;
        }
        return false;
    }

    public int hashCode() {
        return data.hashCode() ^ exprType.signatureDimensions();
    }
}
