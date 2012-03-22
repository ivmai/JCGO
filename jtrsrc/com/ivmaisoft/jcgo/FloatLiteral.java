/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/FloatLiteral.java --
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
 * Lexical term for a floating-point literal.
 */

final class FloatLiteral extends LexTerm {

    private boolean asLiteral;

    private boolean insideArith;

    FloatLiteral(String tokenval) {
        super(LexTerm.FLOATINGPOINT, tokenval);
    }

    ExpressionType exprType() {
        String s = dottedName();
        char ch = s.charAt(s.length() - 1);
        return Main.dict.classTable[ch != 'F' && ch != 'f' ? Type.DOUBLE
                : Type.FLOAT];
    }

    void requireLiteral() {
        asLiteral = true;
    }

    void insideArithOp() {
        insideArith = true;
    }

    int tokenCount() {
        return 2;
    }

    String stringOutput() {
        String s = dottedName();
        int len = s.length();
        char ch = s.charAt(len - 1);
        int type = Type.DOUBLE;
        if (ch == 'F' || ch == 'f') {
            type = Type.FLOAT;
        }
        if (type == Type.FLOAT || ch == 'D' || ch == 'd') {
            s = s.substring(0, len - 1);
        }
        if (!asLiteral && insideArith && isFPZero(s))
            return type == Type.FLOAT ? "JCGO_FP_ZEROF" : "JCGO_FP_ZERO";
        if (s.indexOf('.', 0) < 0 && s.lastIndexOf('E') < 0
                && s.lastIndexOf('e') < 0) {
            s = s + ".0";
        }
        return "(" + Type.cName[type] + ")" + s;
    }

    boolean isFPZero() {
        return isFPZero(dottedName());
    }

    private static boolean isFPZero(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (ch == 'E' || ch == 'e')
                break;
            if (ch >= '1' && ch <= '9')
                return false;
        }
        return true;
    }
}
