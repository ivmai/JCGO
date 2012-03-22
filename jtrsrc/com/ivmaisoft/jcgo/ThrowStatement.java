/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ThrowStatement.java --
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
 * Grammar production for 'throw'.
 ** 
 * Format: THROW Expression SEMI
 */

final class ThrowStatement extends LexNode {

    ThrowStatement(Term b) {
        super(b);
    }

    void processPass1(Context c) {
        c.currentMethod.setNeedsDummyRet();
        terms[0].processPass1(c);
        int s0 = terms[0].exprType().objectSize();
        if (s0 != Type.CLASSINTERFACE && s0 != Type.NULLREF) {
            fatalError(c, "Illegal type of expression for 'throw'");
        }
    }

    boolean allowInline(int tokenLimit) {
        return terms[0].tokenCount() < tokenLimit;
    }

    boolean hasTailReturnOrThrow() {
        return true;
    }

    boolean isReturnAtEnd(boolean allowBreakThrow) {
        return allowBreakThrow;
    }

    void discoverObjLeaks() {
        terms[0].discoverObjLeaks();
        terms[0].setObjLeaks(null);
    }

    void processOutput(OutputContext oc) {
        oc.cPrint("JCGO_THROW_EXC(");
        terms[0].processOutput(oc);
        oc.cPrint(");");
    }
}
