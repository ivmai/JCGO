/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/AccModifier.java --
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
 * Grammar production for a class or method, or field modifier.
 */

final class AccModifier extends LexNode {

    static final int PUBLIC = 0x1;
    static final int PRIVATE = 0x2;
    static final int PROTECTED = 0x4;
    static final int STATIC = 0x8;
    static final int FINAL = 0x10;
    static final int SYNCHRONIZED = 0x20;
    static final int VOLATILE = 0x40;
    static final int TRANSIENT = 0x80;
    static final int NATIVE = 0x100;
    static final int INTERFACE = 0x200;
    static final int ABSTRACT = 0x400;
    static final int STRICT = 0x800;

    static final int SYNTHETIC = 0x1000;
    static final int ENUM = 0x4000;

    static final int LOCALVAR = 0x2000;
    static final int PARAMETER = 0x8000;

    private int modifier;

    AccModifier(int modifier) {
        this.modifier = modifier;
    }

    void processPass0(Context c) {
        c.modifiers |= modifier;
    }

    void processPass1(Context c) {
        if ((c.modifiers & modifier) != 0) {
            fatalError(c, "Repeated modifier found");
        }
        c.modifiers |= modifier;
    }
}
