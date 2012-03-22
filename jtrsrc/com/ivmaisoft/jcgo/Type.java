/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/Type.java --
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
 * This class contains the base type names and their assigned numeric values.
 */

final class Type {

    static final int NULLREF = 0;

    static final int BOOLEAN = 1;

    static final int BYTE = 2;

    static final int CHAR = 3;

    static final int SHORT = 4;

    static final int INT = 5;

    static final int LONG = 6;

    static final int FLOAT = 7;

    static final int DOUBLE = 8;

    static final int VOID = 9;

    static final int CLASSINTERFACE = 10;

    static final int OBJECTARRAY = CLASSINTERFACE + VOID;

    static final String[] sig = {
            "*", "Z", "B", "C", "S", "I", "J", "F", "D", "V", "L", "[Z", "[B",
            "[C", "[S", "[I", "[J", "[F", "[D", "[" };

    static final String[] name = {
            "<null>", "boolean", "byte", "char", "short", "int", "long",
            "float", "double", "void" };

    static final String[] cName = {
            "jObject", "jboolean", "jbyte", "jchar", "jshort", "jint", "jlong",
            "jfloat", "jdouble", "void", "jObject", "jbooleanArr", "jbyteArr",
            "jcharArr", "jshortArr", "jintArr", "jlongArr", "jfloatArr",
            "jdoubleArr", "jObjectArr" };

    static final String[] jniName = {
            "jobject", "jboolean", "jbyte", "jchar", "jshort", "jint", "jlong",
            "jfloat", "jdouble", "void", "jobject", "jbooleanArray",
            "jbyteArray", "jcharArray", "jshortArray", "jintArray",
            "jlongArray", "jfloatArray", "jdoubleArray", "jobjectArray" };

    static final int[] sizeInBytes = {
            4, 1, 1, 2, 2, 4, 8, 4, 8 };

    private Type() {
    }
}
