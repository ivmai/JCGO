/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ObjVector.java --
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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A resizable array (vector of objects).
 */

final class ObjVector {

    private static final Object[] EMPTY_ELEMENT_DATA = {};

    private Object[] elementData = EMPTY_ELEMENT_DATA;

    private int elementCount;

    int modDelCnt;

    ObjVector() {
    }

    private void growCapacity() {
        Object[] elementData = this.elementData;
        int len = elementData.length;
        if (len != 0) {
            Object[] newElementData = new Object[((len * 3 + 15) >>> 3) + len];
            System.arraycopy(elementData, 0, newElementData, 0, len);
            this.elementData = newElementData;
        } else {
            this.elementData = new Object[3];
        }
    }

    int size() {
        return elementCount;
    }

    Object elementAt(int index) {
        return elementData[index];
    }

    void setElementAt(Object obj, int index) {
        Term.assertCond(index < elementCount);
        elementData[index] = obj;
    }

    void removeElementAt(int index) {
        Term.assertCond(index < elementCount);
        int cnt;
        if ((cnt = elementCount - index - 1) > 0) {
            modDelCnt++;
            System.arraycopy(elementData, index + 1, elementData, index, cnt);
        }
        elementData[--elementCount] = null;
    }

    void copyInto(Object[] anArray) {
        System.arraycopy(elementData, 0, anArray, 0, elementCount);
    }

    int identityLastIndexOf(Object obj) {
        int i = elementCount;
        Object[] elementData = this.elementData;
        while (i-- > 0) {
            if (obj == elementData[i])
                break;
        }
        return i;
    }

    int indexOf(Object obj) {
        Term.assertCond(obj != null);
        int cnt = elementCount;
        Object[] elementData = this.elementData;
        for (int i = 0; i < cnt; i++) {
            if (obj.equals(elementData[i]))
                return i;
        }
        return -1;
    }

    void addElement(Object obj) {
        if (elementData.length <= elementCount) {
            growCapacity();
        }
        elementData[elementCount++] = obj;
    }

    Enumeration elements() {
        return new ObjVectorEnumerator(this);
    }
}

final class ObjVectorEnumerator implements Enumeration {

    private/* final */ObjVector vector;

    private int index;

    private/* final */int modCnt;

    ObjVectorEnumerator(ObjVector vector) {
        this.vector = vector;
        modCnt = vector.modDelCnt;
    }

    public boolean hasMoreElements() {
        return vector.size() > index;
    }

    public Object nextElement() {
        Term.assertCond(modCnt == vector.modDelCnt);
        if (vector.size() <= index)
            throw new NoSuchElementException();
        return vector.elementAt(index++);
    }
}
