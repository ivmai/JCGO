/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ObjQueue.java --
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
 * An ordered queue of objects (single-linked list).
 */

final class ObjQueue {

    private ObjQueueEntry first;

    private ObjQueueEntry last;

    ObjQueue() {
    }

    boolean isEmpty() {
        return first == null;
    }

    void addLast(Object obj) {
        ObjQueueEntry entry = new ObjQueueEntry(obj);
        if (last != null) {
            last.next = entry;
        } else {
            first = entry;
        }
        last = entry;
    }

    Object removeFirst() {
        ObjQueueEntry entry = first;
        Term.assertCond(entry != null);
        if ((first = entry.next) == null) {
            last = null;
        }
        return entry.value;
    }

    void addAllMovedFrom(ObjQueue queue) {
        ObjQueueEntry entry = queue.first;
        if (entry != null) {
            ObjQueueEntry last;
            if ((last = this.last) != null) {
                last.next = entry;
            } else {
                first = entry;
            }
            this.last = queue.last;
            queue.first = null;
            queue.last = null;
        }
    }

    boolean contains(Object obj) {
        Term.assertCond(obj != null);
        for (ObjQueueEntry entry = first; entry != null; entry = entry.next) {
            if (obj.equals(entry.value))
                return true;
        }
        return false;
    }

    int countSize() {
        int size = 0;
        for (ObjQueueEntry entry = first; entry != null; entry = entry.next) {
            size++;
        }
        return size;
    }

    void copyInto(Object[] anArray) {
        ObjQueueEntry entry = first;
        int len = anArray.length;
        for (int i = 0; i < len; i++) {
            if (entry == null)
                break;
            anArray[i] = entry.value;
            entry = entry.next;
        }
    }

    Enumeration elements() {
        return new ObjQueueEnumerator(first);
    }
}

final class ObjQueueEntry {

    /* final */Object value;

    ObjQueueEntry next;

    ObjQueueEntry(Object value) {
        this.value = value;
    }
}

final class ObjQueueEnumerator implements Enumeration {

    private ObjQueueEntry entry;

    ObjQueueEnumerator(ObjQueueEntry first) {
        entry = first;
    }

    public boolean hasMoreElements() {
        return entry != null;
    }

    public Object nextElement() {
        ObjQueueEntry entry = this.entry;
        if (entry == null)
            throw new NoSuchElementException();
        this.entry = entry.next;
        return entry.value;
    }
}
