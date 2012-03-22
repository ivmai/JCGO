/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/LexNode.java --
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
 * The abstract superclass of most non-terminal grammatical terms.
 */

abstract class LexNode extends Term {

    /* final */Term[] terms;

    LexNode() {
        terms = new Term[0];
    }

    LexNode(Term a) {
        terms = new Term[1];
        terms[0] = a;
    }

    LexNode(Term a, Term b) {
        terms = new Term[2];
        terms[0] = a;
        terms[1] = b;
    }

    LexNode(Term a, Term b, Term c) {
        terms = new Term[3];
        terms[0] = a;
        terms[1] = b;
        terms[2] = c;
    }

    LexNode(Term a, Term b, Term c, Term d) {
        terms = new Term[4];
        terms[0] = a;
        terms[1] = b;
        terms[2] = c;
        terms[3] = d;
    }

    LexNode(Term a, Term b, Term c, Term d, Term e) {
        terms = new Term[5];
        terms[0] = a;
        terms[1] = b;
        terms[2] = c;
        terms[3] = d;
        terms[4] = e;
    }

    LexNode(Term a, Term b, Term c, Term d, Term e, Term f) {
        terms = new Term[6];
        terms[0] = a;
        terms[1] = b;
        terms[2] = c;
        terms[3] = d;
        terms[4] = e;
        terms[5] = f;
    }

    LexNode(Term a, Term b, Term c, Term d, Term e, Term f, Term g) {
        terms = new Term[7];
        terms[0] = a;
        terms[1] = b;
        terms[2] = c;
        terms[3] = d;
        terms[4] = e;
        terms[5] = f;
        terms[6] = g;
    }

    void processPass0(Context c) {
        Term[] terms = this.terms;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            terms[i].processPass0(c);
        }
    }

    void assignedVarNames(ObjQueue names, boolean recursive) {
        if (recursive) {
            Term[] terms = this.terms;
            int len = terms.length;
            for (int i = 0; i < len; i++) {
                terms[i].assignedVarNames(names, true);
            }
        }
    }

    void processPass1(Context c) {
        Term[] terms = this.terms;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            terms[i].processPass1(c);
        }
    }

    void storeSignature(ObjVector parmSig) {
        Term[] terms = this.terms;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            terms[i].storeSignature(parmSig);
        }
    }

    boolean isFieldAccessed(VariableDefinition v) {
        Term[] terms = this.terms;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            if (terms[i].isFieldAccessed(v))
                return true;
        }
        return false;
    }

    boolean isAnyLocalVarChanged(Term t) {
        Term[] terms = this.terms;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            if (terms[i].isAnyLocalVarChanged(t))
                return true;
        }
        return false;
    }

    void allocRcvr(int[] curRcvrs) {
        Term[] terms = this.terms;
        int last = terms.length - 1;
        if (last > 0) {
            int[] curRcvrsLast = OutputContext.copyRcvrs(curRcvrs);
            terms[0].allocRcvr(curRcvrs);
            for (int i = 1; i < last; i++) {
                int[] curRcvrs2 = OutputContext.copyRcvrs(curRcvrsLast);
                terms[i].allocRcvr(curRcvrs2);
                OutputContext.joinRcvrs(curRcvrs, curRcvrs2);
            }
            terms[last].allocRcvr(curRcvrsLast);
            OutputContext.joinRcvrs(curRcvrs, curRcvrsLast);
        } else if (last == 0) {
            terms[0].allocRcvr(curRcvrs);
        }
    }

    void requireLiteral() {
        Term[] terms = this.terms;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            terms[i].requireLiteral();
        }
    }

    int tokenCount() {
        Term[] terms = this.terms;
        int cnt = 1;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            cnt += terms[i].tokenCount();
        }
        return cnt;
    }

    int tokensExpandedCount() {
        Term[] terms = this.terms;
        int cnt = 0;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            if ((cnt += terms[i].tokensExpandedCount()) < 0)
                return -1 >>> 1;
        }
        return cnt;
    }

    void discoverObjLeaks() {
        Term[] terms = this.terms;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            terms[i].discoverObjLeaks();
        }
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        Term[] terms = this.terms;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            terms[i].writeStackObjs(oc, scopeTerm);
        }
    }

    void processOutput(OutputContext oc) {
        Term[] terms = this.terms;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            terms[i].processOutput(oc);
        }
    }

    ExpressionType traceClassInit() {
        Term[] terms = this.terms;
        int len = terms.length;
        for (int i = 0; i < len; i++) {
            terms[i].traceClassInit();
        }
        return null;
    }
}
