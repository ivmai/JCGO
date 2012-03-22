/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ClassDefnWithDims.java --
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
 * An array of a class type definition.
 */

final class ClassDefnWithDims extends ExpressionType {

    private/* final */ClassDefinition aclass;

    private/* final */int dims;

    ClassDefnWithDims(ClassDefinition aclass, int dims) {
        int s = aclass.objectSize();
        Term.assertCond((s == Type.CLASSINTERFACE || (s >= Type.BOOLEAN && s <= Type.DOUBLE))
                && dims > 0);
        this.aclass = aclass;
        this.dims = dims;
    }

    ExpressionType indirectedType() {
        return aclass.asExprType(dims - 1);
    }

    String getJniName() {
        int s;
        return Type.jniName[dims > 1
                || (s = aclass.objectSize()) == Type.CLASSINTERFACE ? Type.OBJECTARRAY
                : s + Type.CLASSINTERFACE];
    }

    String name() {
        StringBuffer sb = new StringBuffer();
        sb.append(aclass.name());
        int i = dims;
        while (i-- > 0) {
            sb.append("[]");
        }
        return sb.toString();
    }

    String getJavaSignature() {
        return fillString('[', dims) + aclass.getJavaSignature();
    }

    String csign() {
        return aclass.csign() + fillString('A', dims);
    }

    static String fillString(char ch, int len) {
        char[] chars = new char[len > 0 ? len : 0];
        for (int i = 0; i < len; i++) {
            chars[i] = ch;
        }
        return new String(chars);
    }

    ClassDefinition receiverClass() {
        return Main.dict.classTable[objectSize()];
    }

    ClassDefinition signatureClass() {
        return aclass;
    }

    int signatureDimensions() {
        return dims;
    }

    boolean hasRealInstances() {
        return aclass.used();
    }

    int objectSize() {
        int type;
        return dims > 1 || (type = aclass.objectSize()) == Type.CLASSINTERFACE ? Type.OBJECTARRAY
                : type + Type.CLASSINTERFACE;
    }

    String castName() {
        return Type.cName[objectSize()];
    }
}
