/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/DimExpr.java --
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
 * Grammar production for an expression with a dimension specifier.
 ** 
 * Format: LBRACK Expression RBRACK
 */

final class DimExpr extends LexNode {

    private int rcvr;

    DimExpr(Term b) {
        super(b);
    }

    void processPass1(Context c) {
        c.typeDims++;
        terms[0].processPass1(c);
        int s0 = terms[0].exprType().objectSize();
        if (s0 < Type.BYTE || s0 > Type.INT) {
            fatalError(c, "Illegal type of array size expression");
        }
    }

    ConstValue evaluateConstValue() {
        return terms[0].evaluateConstValue();
    }

    boolean isSafeWithThrow() {
        return terms[0].isSafeWithThrow();
    }

    int tokenCount() {
        return terms[0].tokenCount() + (terms[0].isLiteral() ? 0 : 1);
    }

    void allocParamRcvr(int[] curRcvrs, int[] curRcvrs1, int[] curRcvrs2) {
        if (!terms[0].isLiteral()) {
            terms[0].allocRcvr(curRcvrs);
            rcvr = ++curRcvrs[Type.INT];
        }
    }

    void produceRcvr(OutputContext oc) {
        if (rcvr > 0) {
            oc.cPrint(OutputContext.getRcvrName(rcvr, Type.INT));
            oc.cPrint("= ");
            if (terms[0].exprType().objectSize() == Type.CHAR) {
                oc.cPrint("(");
                oc.cPrint(Type.cName[Type.INT]);
                oc.cPrint(")");
                terms[0].atomaryOutput(oc);
            } else {
                terms[0].processOutput(oc);
            }
            oc.cPrint(", ");
        }
    }

    void processOutput(OutputContext oc) {
        oc.cPrint(", ");
        if (rcvr > 0) {
            oc.cPrint(OutputContext.getRcvrName(rcvr, Type.INT));
        } else {
            terms[0].processOutput(oc);
        }
    }
}
