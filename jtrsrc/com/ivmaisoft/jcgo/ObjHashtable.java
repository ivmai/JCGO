/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/ObjHashtable.java --
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
 * An unordered map (hash table of objects).
 */

final class ObjHashtable {

    private ObjHashtableRoots roots = new ObjHashtableRoots();

    ObjHashtable() {
    }

    int size() {
        return roots.size();
    }

    Object get(Object key) {
        ObjHashtableEntry entry = roots.getEntry(key, hashOfKey(key));
        return entry != null ? entry.value : null;
    }

    private static int hashOfKey(Object key) {
        Term.assertCond(key != null);
        int hash = key.hashCode();
        hash += ~(hash << 9);
        hash ^= hash >>> 14;
        hash += hash << 4;
        return (hash >>> 10) ^ hash;
    }

    Object remove(Object key) {
        return roots.removeEntry(key, hashOfKey(key), null);
    }

    Object put(Object key, Object value) {
        int hash = hashOfKey(key);
        ObjHashtableRoots roots = this.roots;
        int index = roots.hashToIndexAtRoot(hash);
        ObjHashtableEntry entry = roots.getEntryAtRoot(key, hash, index);
        if (entry != null) {
            Object obj = entry.value;
            entry.value = value;
            return obj;
        }
        ObjHashtableRoots last = roots;
        if (roots.needsGrow()) {
            this.roots = roots = new ObjHashtableRoots(roots);
            index = roots.hashToIndexAtRoot(hash);
        }
        roots.addEntryAtRoot(key, value, index);
        ObjHashtableRoots prev = last.prev();
        return prev != null ? prev.removeEntry(key, hash, last) : null;
    }

    void clear() {
        roots.clear();
    }

    Enumeration keys() {
        return new ObjHashtableEnumerator(roots, true);
    }

    Enumeration elements() {
        return new ObjHashtableEnumerator(roots, false);
    }
}

final class ObjHashtableRoots {

    private static final int[] PRIMES = {
            0xd, 0x1f, 0x3d, 0x7f, 0xfb, 0x1fd, 0x3fd, 0x7f7, 0xffd, 0x1fff,
            0x3ffd, 0x7fed };

    private/* final */ObjHashtableEntry[] entries;

    private ObjHashtableRoots prev;

    private int elementCount;

    ObjHashtableRoots() {
        entries = new ObjHashtableEntry[PRIMES[0]];
    }

    ObjHashtableRoots(ObjHashtableRoots roots) {
        entries = new ObjHashtableEntry[nextCapacity(roots.entries.length)];
        prev = roots;
    }

    private static int nextCapacity(int capacity) {
        int last = PRIMES.length - 1;
        if (PRIMES[last] > capacity) {
            for (int i = 0; i < last; i++) {
                if (PRIMES[i] >= capacity)
                    return PRIMES[i + 1];
            }
        }
        return (capacity << 1) + 1;
    }

    ObjHashtableRoots prev() {
        return prev;
    }

    int size() {
        int size = elementCount;
        ObjHashtableRoots roots = this;
        while ((roots = roots.prev) != null) {
            size += roots.elementCount;
        }
        return size;
    }

    ObjHashtableEntry getEntry(Object key, int hash) {
        ObjHashtableEntry entry;
        ObjHashtableRoots roots = this;
        while ((entry = hashToEntry(hash, roots.entries)) == null
                || (entry = entry.findEntry(key)) == null) {
            if ((roots = roots.prev) == null)
                break;
        }
        return entry;
    }

    private static ObjHashtableEntry hashToEntry(int hash,
            ObjHashtableEntry[] entries) {
        return entries[hashToIndex(hash, entries)];
    }

    private static int hashToIndex(int hash, ObjHashtableEntry[] entries) {
        return (hash & (-1 >>> 1)) % entries.length;
    }

    int hashToIndexAtRoot(int hash) {
        return hashToIndex(hash, entries);
    }

    boolean needsGrow() {
        int len = entries.length;
        return len - len / 5 < elementCount;
    }

    ObjHashtableEntry getEntryAtRoot(Object key, int hash, int index) {
        ObjHashtableEntry entry = entries[index];
        return entry != null ? entry.findEntry(key) : null;
    }

    void addEntryAtRoot(Object key, Object value, int index) {
        ObjHashtableEntry[] entries = this.entries;
        entries[index] = new ObjHashtableEntry(key, value, entries[index]);
        elementCount++;
    }

    Object removeEntry(Object key, int hash, ObjHashtableRoots last) {
        ObjHashtableRoots roots = this;
        do {
            ObjHashtableEntry[] rootEntries = roots.entries;
            int index = hashToIndex(hash, rootEntries);
            ObjHashtableEntry rootEntry = rootEntries[index];
            ObjHashtableEntry entry;
            if (rootEntry != null
                    && (entry = rootEntry.findAndRemoveEntry(key)) != null) {
                if (entry == rootEntry) {
                    rootEntries[index] = rootEntry.next();
                }
                if (--roots.elementCount == 0 && last != null) {
                    last.prev = roots.prev;
                }
                return entry.value;
            }
        } while ((roots = (last = roots).prev) != null);
        return null;
    }

    void clear() {
        prev = null;
        if (elementCount != 0) {
            ObjHashtableEntry[] entries = this.entries;
            int len = entries.length;
            while (len-- > 0) {
                entries[len] = null;
            }
            elementCount = 0;
        }
    }

    ObjHashtableEntry[] getEntries() {
        return elementCount != 0 ? entries : null;
    }
}

final class ObjHashtableEntry {

    private/* final */Object key;

    private ObjHashtableEntry next;

    Object value;

    ObjHashtableEntry(Object key, Object value, ObjHashtableEntry next) {
        this.key = key;
        this.value = value;
        this.next = next;
    }

    ObjHashtableEntry next() {
        return next;
    }

    ObjHashtableEntry findEntry(Object key) {
        ObjHashtableEntry entry = this;
        while (!entry.key.equals(key)) {
            if ((entry = entry.next) == null)
                break;
        }
        return entry;
    }

    ObjHashtableEntry findAndRemoveEntry(Object key) {
        ObjHashtableEntry entry = this;
        ObjHashtableEntry last = null;
        while (!entry.key.equals(key)) {
            if ((entry = (last = entry).next) == null)
                break;
        }
        if (last != null && entry != null) {
            last.next = entry.next;
        }
        return entry;
    }

    Object key() {
        return key;
    }
}

final class ObjHashtableEnumerator implements Enumeration {

    private ObjHashtableRoots roots;

    private/* final */boolean keys;

    private ObjHashtableEntry entry;

    private int index;

    private boolean filled;

    ObjHashtableEnumerator(ObjHashtableRoots roots, boolean keys) {
        this.roots = roots;
        this.keys = keys;
    }

    private void nextFill() {
        ObjHashtableEntry entry;
        if ((entry = this.entry) == null || (entry = entry.next()) == null)
            do {
                ObjHashtableEntry[] entries = roots.getEntries();
                if (entries != null) {
                    int index = this.index;
                    int len = entries.length;
                    while (index < len) {
                        if ((entry = entries[index++]) != null)
                            break;
                    }
                    this.index = index;
                    if (entry != null)
                        break;
                }
                ObjHashtableRoots prev = roots.prev();
                if (prev == null)
                    break;
                roots = prev;
                this.index = 0;
            } while (true);
        this.entry = entry;
        filled = true;
    }

    public boolean hasMoreElements() {
        if (!filled) {
            nextFill();
        }
        return entry != null;
    }

    public Object nextElement() {
        if (!filled) {
            nextFill();
        }
        if (entry == null)
            throw new NoSuchElementException();
        filled = false;
        return keys ? entry.key() : entry.value;
    }
}
