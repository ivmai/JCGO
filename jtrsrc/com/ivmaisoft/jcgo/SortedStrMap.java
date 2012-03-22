/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/SortedStrMap.java --
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
 * A sorted map with string keys.
 */

final class SortedStrMap {

    static final SortedStrMapEntry NIL = new SortedStrMapEntry();

    private SortedStrMapEntry root = NIL;

    SortedStrMap() {
    }

    Object addStartsWith(String key, Object value) {
        SortedStrMapEntry entry = root;
        int cmp = 0;
        if (entry != NIL) {
            while ((cmp = key.compareTo(entry.key)) != 0) {
                if (cmp < 0) {
                    if (entry.left == NIL)
                        break;
                    entry = entry.left;
                } else {
                    if (entry.right == NIL)
                        break;
                    entry = entry.right;
                }
            }
        }
        if (cmp == 0) {
            if (entry != NIL) {
                Object oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
            root = new SortedStrMapEntry(key, value, entry);
        } else {
            SortedStrMapEntry next = cmp < 0 ? entry : successor(entry);
            if (next != null && next.key.startsWith(key))
                return null;
            next = cmp < 0 ? predecessor(entry) : entry;
            if (next != null && key.startsWith(next.key)) {
                next.key = key;
                next.value = value;
            } else if (cmp < 0) {
                entry.left = new SortedStrMapEntry(key, value, entry);
                fixAfterInsertion(entry.left);
            } else {
                entry.right = new SortedStrMapEntry(key, value, entry);
                fixAfterInsertion(entry.right);
            }
        }
        return null;
    }

    Object getValueStartsWith(String key) {
        SortedStrMapEntry entry = root;
        int cmp = 0;
        if (entry != NIL) {
            while ((cmp = key.compareTo(entry.key)) != 0) {
                if (cmp < 0) {
                    if (entry.left == NIL)
                        break;
                    entry = entry.left;
                } else {
                    if (entry.right == NIL)
                        break;
                    entry = entry.right;
                }
            }
        }
        if (cmp > 0) {
            entry = successor(entry);
        }
        return entry != null && entry.key.startsWith(key) ? entry.value : null;
    }

    private SortedStrMapEntry predecessor(SortedStrMapEntry entry) {
        SortedStrMapEntry child = entry.left;
        if (child != NIL) {
            while (child.right != NIL) {
                child = child.right;
            }
            return child;
        }
        if (entry != NIL) {
            do {
                child = entry;
                entry = entry.parent;
            } while (entry.left == child);
        }
        return entry;
    }

    private SortedStrMapEntry successor(SortedStrMapEntry entry) {
        SortedStrMapEntry child = entry.right;
        if (child != NIL) {
            while (child.left != NIL) {
                child = child.left;
            }
            return child;
        }
        if (entry != NIL) {
            do {
                child = entry;
                entry = entry.parent;
            } while (entry.right == child);
        }
        return entry;
    }

    private void fixAfterInsertion(SortedStrMapEntry entry) {
        SortedStrMapEntry pp;
        entry.isRed = true;
        while (entry != NIL && entry != root && entry.parent.isRed) {
            if ((pp = entry.parent.parent).left == entry.parent) {
                SortedStrMapEntry next = pp.right;
                if (next.isRed) {
                    entry.parent.isRed = false;
                    next.isRed = false;
                    entry = pp;
                    if (entry != NIL) {
                        entry.isRed = true;
                    }
                } else {
                    if (entry.parent.right == entry) {
                        entry = entry.parent;
                        rotateLeft(entry);
                        pp = entry.parent.parent;
                    }
                    entry.parent.isRed = false;
                    if (pp != NIL) {
                        pp.isRed = true;
                        rotateRight(pp);
                    }
                }
            } else {
                SortedStrMapEntry next = pp.left;
                if (next.isRed) {
                    entry.parent.isRed = false;
                    next.isRed = false;
                    entry = pp;
                    if (entry != NIL) {
                        entry.isRed = true;
                    }
                } else {
                    if (entry.parent.left == entry) {
                        entry = entry.parent;
                        rotateRight(entry);
                        pp = entry.parent.parent;
                    }
                    entry.parent.isRed = false;
                    if (pp != NIL) {
                        pp.isRed = true;
                        rotateLeft(pp);
                    }
                }
            }
        }
        root.isRed = false;
    }

    private void rotateLeft(SortedStrMapEntry entry) {
        SortedStrMapEntry next = entry.right;
        entry.right = next.left;
        if (next.left != NIL) {
            next.left.parent = entry;
        }
        next.parent = entry.parent;
        if (entry.parent == NIL) {
            root = next;
        } else if (entry.parent.left == entry) {
            entry.parent.left = next;
        } else {
            entry.parent.right = next;
        }
        next.left = entry;
        entry.parent = next;
    }

    private void rotateRight(SortedStrMapEntry entry) {
        SortedStrMapEntry next = entry.left;
        entry.left = next.right;
        if (next.right != NIL) {
            next.right.parent = entry;
        }
        next.parent = entry.parent;
        if (entry.parent == NIL) {
            root = next;
        } else if (entry.parent.right == entry) {
            entry.parent.right = next;
        } else {
            entry.parent.left = next;
        }
        next.right = entry;
        entry.parent = next;
    }
}

final class SortedStrMapEntry {

    String key;

    Object value;

    SortedStrMapEntry left = SortedStrMap.NIL;

    SortedStrMapEntry right = SortedStrMap.NIL;

    SortedStrMapEntry parent;

    boolean isRed;

    SortedStrMapEntry() {
        key = "";
        parent = SortedStrMap.NIL;
    }

    SortedStrMapEntry(String key, Object value, SortedStrMapEntry parent) {
        this.key = key;
        this.value = value;
        this.parent = parent;
    }
}
