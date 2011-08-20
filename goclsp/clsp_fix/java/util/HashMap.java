/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Class root location: $(JCGO)/goclsp/clsp_fix
 * Origin: GNU Classpath v0.93
 */

/* HashMap.java -- a class providing a basic hashtable data structure,
   mapping Object --> Object
   Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

// NOTE: This implementation is very similar to that of Hashtable. If you fix
// a bug in here, chances are you should make a similar change to the Hashtable
// code.

// NOTE: This implementation has some nasty coding style in order to
// support LinkedHashMap, which extends this.

/**
 * This class provides a hashtable-backed implementation of the
 * Map interface.
 * <p>
 *
 * It uses a hash-bucket approach; that is, hash collisions are handled
 * by linking the new node off of the pre-existing node (or list of
 * nodes).  In this manner, techniques such as linear probing (which
 * can cause primary clustering) and rehashing (which does not fit very
 * well with Java's method of precomputing hash codes) are avoided.
 * <p>
 *
 * Under ideal circumstances (no collisions), HashMap offers O(1)
 * performance on most operations (<code>containsValue()</code> is,
 * of course, O(n)).  In the worst case (all keys map to the same
 * hash code -- very unlikely), most operations are O(n).
 * <p>
 *
 * HashMap is part of the JDK1.2 Collections API.  It differs from
 * Hashtable in that it accepts the null key and null values, and it
 * does not support "Enumeration views." Also, it is not synchronized;
 * if you plan to use it in multiple threads, consider using:<br>
 * <code>Map m = Collections.synchronizedMap(new HashMap(...));</code>
 * <p>
 *
 * The iterators are <i>fail-fast</i>, meaning that any structural
 * modification, except for <code>remove()</code> called on the iterator
 * itself, cause the iterator to throw a
 * <code>ConcurrentModificationException</code> rather than exhibit
 * non-deterministic behavior.
 *
 * @author Jon Zeppieri
 * @author Jochen Hoenicke
 * @author Bryce McKinlay
 * @author Eric Blake (ebb9@email.byu.edu)
 * @see Object#hashCode()
 * @see Collection
 * @see Map
 * @see TreeMap
 * @see LinkedHashMap
 * @see IdentityHashMap
 * @see Hashtable
 * @since 1.2
 * @status updated to 1.4
 */
public class HashMap extends AbstractMap
  implements Map, Cloneable, Serializable
{
  /**
   * Default number of buckets. This is the value the JDK 1.3 uses. Some
   * early documentation specified this value as 101. That is incorrect.
   * Package visible for use by HashSet.
   */
  static final int DEFAULT_CAPACITY = 16;

  private static final int MAXIMUM_CAPACITY = 1 << 30;

  /**
   * The default load factor; this is explicitly specified by the spec.
   * Package visible for use by HashSet.
   */
  static final float DEFAULT_LOAD_FACTOR = 0.75f;

  /**
   * Compatible with JDK 1.2.
   */
  private static final long serialVersionUID = 362498820763181265L;

  /**
   * The rounded product of the capacity and the load factor; when the number
   * of elements exceeds the threshold, the HashMap calls
   * <code>rehash()</code>.
   * @serial the threshold for rehashing
   */
  private int threshold;

  /**
   * Load factor of this HashMap:  used in computing the threshold.
   * Package visible for use by HashSet.
   * @serial the load factor
   */
  final float loadFactor;

  /**
   * Array containing the actual key-value mappings.
   * Package visible for use by nested and subclasses.
   */
  transient HashEntry[] buckets;

  /**
   * Counts the number of modifications this HashMap has undergone, used
   * by Iterators to know when to throw ConcurrentModificationExceptions.
   * Package visible for use by nested and subclasses.
   */
  transient volatile int modCount;

  /**
   * The size of this HashMap:  denotes the number of key-value pairs.
   * Package visible for use by nested and subclasses.
   */
  transient int size;

  /**
   * The cache for {@link #entrySet()}.
   */
  private transient Set entries;

  private transient int loadFactorPercent;

  /**
   * Class to represent an entry in the hash table. Holds a single key-value
   * pair. Package visible for use by subclass.
   *
   * @author Eric Blake (ebb9@email.byu.edu)
   */
  static class HashEntry extends AbstractMap.BasicMapEntry
  {

    final int hash;

    /**
     * The next entry in the linked list. Package visible for use by subclass.
     */
    HashEntry next;

    /**
     * Simple constructor.
     * @param key the key
     * @param value the value
     */
    HashEntry(Object key, Object value, int hash)
    {
      super(key, value);
      this.hash = hash;
    }

    /**
     * Called when this entry is accessed via {@link #put(Object, Object)}.
     * This version does nothing, but in LinkedHashMap, it must do some
     * bookkeeping for access-traversal mode.
     */
    void access()
    {
    }

    /**
     * Called when this entry is removed from the map. This version simply
     * returns the value, but in LinkedHashMap, it must also do bookkeeping.
     *
     * @return the value of this key as it is removed
     */
    Object cleanup()
    {
      return value;
    }
  }

  /**
   * Construct a new HashMap with the default capacity (16) and the default
   * load factor (0.75).
   */
  public HashMap()
  {
    this(DEFAULT_CAPACITY);
  }

  /**
   * Construct a new HashMap from the given Map, with initial capacity
   * the greater of the size of <code>m</code> or the default of 16.
   * <p>
   *
   * Every element in Map m will be put into this new HashMap.
   *
   * @param m a Map whose key / value pairs will be put into the new HashMap.
   *        <b>NOTE: key / value pairs are not cloned in this constructor.</b>
   * @throws NullPointerException if m is null
   */
  public HashMap(Map m)
  {
    this(Math.max(divBy075(m.size()) + 1, DEFAULT_CAPACITY));
    putAll(m);
  }

  private static final int divBy075(int size)
  {
    return size / 3 + size;
  }

  /**
   * Construct a new HashMap with a specific inital capacity and
   * default load factor of 0.75.
   *
   * @param initialCapacity the initial capacity of this HashMap (&gt;=0)
   * @throws IllegalArgumentException if (initialCapacity &lt; 0)
   */
  public HashMap(int initialCapacity)
  {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal Capacity: "
                                         + initialCapacity);

    if (initialCapacity >= MAXIMUM_CAPACITY)
      initialCapacity = MAXIMUM_CAPACITY;
    int capacity = 1;
    while (capacity < initialCapacity)
      capacity <<= 1;

    buckets = new HashEntry[capacity];
    loadFactor = 0.75f;
    loadFactorPercent = 75;
    threshold = capacity - ((capacity + 3) >> 2);
  }

  /**
   * Construct a new HashMap with a specific inital capacity and load factor.
   *
   * @param initialCapacity the initial capacity (&gt;=0)
   * @param loadFactor the load factor (&gt; 0, not NaN)
   * @throws IllegalArgumentException if (initialCapacity &lt; 0) ||
   *                                     ! (loadFactor &gt; 0.0)
   */
  public HashMap(int initialCapacity, float loadFactor)
  {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal Capacity: "
                                         + initialCapacity);
    if (!(loadFactor >= 0))
      throw new IllegalArgumentException("Illegal Load");

    if (initialCapacity >= MAXIMUM_CAPACITY)
      initialCapacity = MAXIMUM_CAPACITY;
    int capacity = 1;
    while (capacity < initialCapacity)
      capacity <<= 1;

    buckets = new HashEntry[capacity];
    this.loadFactor = loadFactor;

    int intFactor;
    if (loadFactor != 0)
      {
        intFactor = (int) (loadFactor * 100);
        if ((float) intFactor / 100 != loadFactor)
          intFactor = 0;
        threshold = (int) (capacity * loadFactor);
      }
    else
      {
        intFactor = 75;
        threshold = capacity - ((capacity + 3) >> 2);
      }
    loadFactorPercent = intFactor;
  }

  /**
   * Returns the number of kay-value mappings currently in this Map.
   *
   * @return the size
   */
  public int size()
  {
    return size;
  }

  /**
   * Returns true if there are no key-value mappings currently in this Map.
   *
   * @return <code>size() == 0</code>
   */
  public boolean isEmpty()
  {
    return size == 0;
  }

  /**
   * Return the value in this HashMap associated with the supplied key,
   * or <code>null</code> if the key maps to nothing.  NOTE: Since the value
   * could also be null, you must use containsKey to see if this key
   * actually maps to something.
   *
   * @param key the key for which to fetch an associated value
   * @return what the key maps to, if present
   * @see #put(Object, Object)
   * @see #containsKey(Object)
   */
  public Object get(Object key)
  {
    HashEntry e = getHashEntryByKey(key);
    return e != null ? e.value : null;
  }

  // Package visible, for use in subclasses.
  final HashEntry getHashEntryByKey(Object key)
  {
    int hash = hashOf(key);
    HashEntry e = buckets[indexFor(hash)];
    while (e != null)
      {
        if (hash == e.hash && equals(key, e.key))
          break;
        e = e.next;
      }
    return e;
  }

  /**
   * Returns true if the supplied object <code>equals()</code> a key
   * in this HashMap.
   *
   * @param key the key to search for in this HashMap
   * @return true if the key is in the table
   * @see #containsValue(Object)
   */
  public boolean containsKey(Object key)
  {
    return getHashEntryByKey(key) != null;
  }

  /**
   * Puts the supplied value into the Map, mapped by the supplied key.
   * The value may be retrieved by any object which <code>equals()</code>
   * this key. NOTE: Since the prior value could also be null, you must
   * first use containsKey if you want to see if you are replacing the
   * key's mapping.
   *
   * @param key the key used to locate the value
   * @param value the value to be stored in the HashMap
   * @return the prior mapping of the key, or null if there was none
   * @see #get(Object)
   * @see Object#equals(Object)
   */
  public Object put(Object key, Object value)
  {
    int hash = hashOf(key);
    int idx = indexFor(hash);
    HashEntry e = buckets[idx];

    while (e != null)
      {
        if (hash == e.hash && equals(key, e.key))
          {
            e.access(); // Must call this for bookkeeping in LinkedHashMap.
            Object r = e.value;
            e.value = value;
            return r;
          }
        else
          e = e.next;
      }

    // At this point, we know we need to add a new entry.
    modCount++;

    // LinkedHashMap cannot override put(), hence this call.
    size++;
    addEntry(key, value, idx, true, hash);

    if (size > threshold)
      rehash();

    return null;
  }

  /**
   * Removes from the HashMap and returns the value which is mapped by the
   * supplied key. If the key maps to nothing, then the HashMap remains
   * unchanged, and <code>null</code> is returned. NOTE: Since the value
   * could also be null, you must use containsKey to see if you are
   * actually removing a mapping.
   *
   * @param key the key used to locate the value to remove
   * @return whatever the key mapped to, if present
   */
  public Object remove(Object key)
  {
    int hash = hashOf(key);
    int idx = indexFor(hash);
    HashEntry e = buckets[idx];
    HashEntry last = null;

    while (e != null)
      {
        if (hash == e.hash && equals(key, e.key))
          {
            modCount++;
            if (last == null)
              buckets[idx] = e.next;
            else
              last.next = e.next;
            size--;
            // Method call necessary for LinkedHashMap to work correctly.
            return e.cleanup();
          }
        last = e;
        e = e.next;
      }
    return null;
  }

  /**
   * Clears the Map so it has no keys. This is O(n).
   */
  public void clear()
  {
    if (size != 0)
      {
        modCount++;
        Arrays.fill(buckets, null);
        size = 0;
      }
  }

  /**
   * Returns true if this HashMap contains a value <code>o</code>, such that
   * <code>o.equals(value)</code>.
   *
   * @param value the value to search for in this HashMap
   * @return true if at least one key maps to the value
   * @see #containsKey(Object)
   */
  public boolean containsValue(Object value)
  {
    for (int i = buckets.length - 1; i >= 0; i--)
      {
        HashEntry e = buckets[i];
        while (e != null)
          {
            if (equals(value, e.value))
              return true;
            e = e.next;
          }
      }
    return false;
  }

  /**
   * Returns a shallow clone of this HashMap. The Map itself is cloned,
   * but its contents are not.  This is O(n).
   *
   * @return the clone
   */
  public Object clone()
  {
    HashMap copy = null;
    try
      {
        copy = (HashMap) super.clone();
      }
    catch (CloneNotSupportedException x)
      {
        // This is impossible.
      }
    copy.buckets = new HashEntry[buckets.length];
    copy.putAllInternal(this);
    // Clear the entry cache. AbstractMap.clone() does the others.
    copy.entries = null;
    return copy;
  }

  /**
   * Returns a "set view" of this HashMap's keys. The set is backed by the
   * HashMap, so changes in one show up in the other.  The set supports
   * element removal, but not element addition.
   *
   * @return a set view of the keys
   * @see #values()
   * @see #entrySet()
   */
  public Set keySet()
  {
    if (keys == null)
      // Create an AbstractSet with custom implementations of those methods
      // that can be overridden easily and efficiently.
      keys = new AbstractSet()
      {
        public int size()
        {
          return size;
        }

        public Iterator iterator()
        {
          // Cannot create the iterator directly, because of LinkedHashMap.
          return HashMap.this.iterator(KEYS);
        }

        public void clear()
        {
          HashMap.this.clear();
        }

        public boolean contains(Object o)
        {
          return containsKey(o);
        }

        public boolean remove(Object o)
        {
          // Test against the size of the HashMap to determine if anything
          // really got removed. This is necessary because the return value
          // of HashMap.remove() is ambiguous in the null case.
          int oldsize = size;
          HashMap.this.remove(o);
          return oldsize != size;
        }
      };
    return keys;
  }

  /**
   * Returns a "collection view" (or "bag view") of this HashMap's values.
   * The collection is backed by the HashMap, so changes in one show up
   * in the other.  The collection supports element removal, but not element
   * addition.
   *
   * @return a bag view of the values
   * @see #keySet()
   * @see #entrySet()
   */
  public Collection values()
  {
    if (values == null)
      // We don't bother overriding many of the optional methods, as doing so
      // wouldn't provide any significant performance advantage.
      values = new AbstractCollection()
      {
        public int size()
        {
          return size;
        }

        public Iterator iterator()
        {
          // Cannot create the iterator directly, because of LinkedHashMap.
          return HashMap.this.iterator(VALUES);
        }

        public void clear()
        {
          HashMap.this.clear();
        }
      };
    return values;
  }

  /**
   * Returns a "set view" of this HashMap's entries. The set is backed by
   * the HashMap, so changes in one show up in the other.  The set supports
   * element removal, but not element addition.<p>
   *
   * Note that the iterators for all three views, from keySet(), entrySet(),
   * and values(), traverse the HashMap in the same sequence.
   *
   * @return a set view of the entries
   * @see #keySet()
   * @see #values()
   * @see Map.Entry
   */
  public Set entrySet()
  {
    if (entries == null)
      // Create an AbstractSet with custom implementations of those methods
      // that can be overridden easily and efficiently.
      entries = new AbstractSet()
      {
        public int size()
        {
          return size;
        }

        public Iterator iterator()
        {
          // Cannot create the iterator directly, because of LinkedHashMap.
          return HashMap.this.iterator(ENTRIES);
        }

        public void clear()
        {
          HashMap.this.clear();
        }

        public boolean contains(Object o)
        {
          return getEntry(o) != null;
        }

        public boolean remove(Object o)
        {
          HashEntry e = getEntry(o);
          if (e != null)
            {
              HashMap.this.remove(e.key);
              return true;
            }
          return false;
        }
      };
    return entries;
  }

  /**
   * Helper method for put, that creates and adds a new Entry.  This is
   * overridden in LinkedHashMap for bookkeeping purposes.
   *
   * @param key the key of the new Entry
   * @param value the value
   * @param idx the index in buckets where the new Entry belongs
   * @param callRemove whether to call the removeEldestEntry method
   * @see #put(Object, Object)
   */
  void addEntry(Object key, Object value, int idx, boolean callRemove,
    int hash)
  {
    HashEntry e = new HashEntry(key, value, hash);
    e.next = buckets[idx];
    buckets[idx] = e;
  }

  /**
   * Helper method for entrySet(), which matches both key and value
   * simultaneously.
   *
   * @param o the entry to match
   * @return the matching entry, if found, or null
   * @see #entrySet()
   */
  // Package visible, for use in nested classes.
  final HashEntry getEntry(Object o)
  {
    if (! (o instanceof Map.Entry))
      return null;
    Map.Entry me = (Map.Entry) o;
    Object key = me.getKey();
    int hash = hashOf(key);
    HashEntry e = buckets[indexFor(hash)];
    while (e != null)
      {
        if (hash == e.hash && equals(e.key, key))
          return equals(e.value, me.getValue()) ? e : null;
        e = e.next;
      }
    return null;
  }

  /**
   * Helper method that returns the hash value for `key' based on its
   * hashCode().
   *
   * @param key the key
   * @return the key hash
   */
  private final int hashOf(Object key)
  {
    int hash = hashCode(key);
    hash += ~(hash << 9);
    hash ^= hash >>> 14;
    hash += hash << 4;
    return (hash >>> 10) ^ hash;
  }

  /**
   * Helper method that returns an index in the buckets array for `key'.
   *
   * @param hash the key hash
   * @return the bucket number
   */
  private final int indexFor(int hash)
  {
    return hash & (buckets.length - 1);
  }

  /**
   * Generates a parameterized iterator.  Must be overrideable, since
   * LinkedHashMap iterates in a different order.
   *
   * @param type {@link #KEYS}, {@link #VALUES}, or {@link #ENTRIES}
   * @return the appropriate iterator
   */
  Iterator iterator(int type)
  {
    return new HashIterator(type);
  }

  /**
   * A simplified, more efficient internal implementation of putAll(). clone()
   * should not call putAll or put, in order to be compatible with the JDK
   * implementation with respect to subclasses.
   *
   * @param m the map to initialize this from
   */
  void putAllInternal(HashMap m)
  {
    Iterator itr = m.iterator(ENTRIES);
    int pos = size;
    while (--pos >= 0)
      {
        HashEntry e = (HashEntry) itr.next();
        addEntry(e.key, e.value, indexFor(e.hash), false, e.hash);
      }
  }

  /**
   * Increases the size of the HashMap and rehashes all keys to new
   * array indices; this is called when the addition of a new value
   * would cause size() &gt; threshold. Note that the existing Entry
   * objects are reused in the new hash table.
   *
   * <p>This is not specified, but the new size is twice the current size.
   */
  private void rehash()
  {
    HashEntry[] oldBuckets = buckets;

    if (oldBuckets.length == MAXIMUM_CAPACITY)
      {
        threshold = -1 >>> 1;
        return;
      }
    int newcapacity = oldBuckets.length << 1;

    int intFactor = loadFactorPercent;
    if (intFactor > 0)
      threshold = (-1 >>> 1) / intFactor >= newcapacity ?
                   newcapacity * intFactor / 100 :
                   (newcapacity / 100 + 1) * intFactor;
    else
      threshold = (int) (newcapacity * loadFactor);

    HashEntry[] buckets = new HashEntry[newcapacity];
    this.buckets = buckets;

    for (int i = 0; i < oldBuckets.length; i++)
      {
        HashEntry e = oldBuckets[i];
        while (e != null)
          {
            int idx = indexFor(e.hash);
            HashEntry next = e.next;
            e.next = buckets[idx];
            buckets[idx] = e;
            e = next;
          }
      }
  }

  /**
   * Serializes this object to the given stream.
   *
   * @param s the stream to write to
   * @throws IOException if the underlying stream fails
   * @serialData the <i>capacity</i>(int) that is the length of the
   *             bucket array, the <i>size</i>(int) of the hash map
   *             are emitted first.  They are followed by size entries,
   *             each consisting of a key (Object) and a value (Object).
   */
  private void writeObject(ObjectOutputStream s) throws IOException
  {
    // Write the threshold and loadFactor fields.
    s.defaultWriteObject();

    s.writeInt(buckets.length);
    s.writeInt(size);
    // Avoid creating a wasted Set by creating the iterator directly.
    Iterator it = iterator(ENTRIES);
    while (it.hasNext())
      {
        HashEntry entry = (HashEntry) it.next();
        s.writeObject(entry.key);
        s.writeObject(entry.value);
      }
  }

  /**
   * Deserializes this object from the given stream.
   *
   * @param s the stream to read from
   * @throws ClassNotFoundException if the underlying stream fails
   * @throws IOException if the underlying stream fails
   * @serialData the <i>capacity</i>(int) that is the length of the
   *             bucket array, the <i>size</i>(int) of the hash map
   *             are emitted first.  They are followed by size entries,
   *             each consisting of a key (Object) and a value (Object).
   */
  private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException
  {
    // Read the threshold and loadFactor fields.
    s.defaultReadObject();

    int intFactor = 75;
    if (loadFactor != 0)
      {
        intFactor = (int) (loadFactor * 100);
        if ((float) intFactor / 100 != loadFactor)
          intFactor = 0;
      }
    loadFactorPercent = intFactor;

    // Read and use capacity, followed by key/value pairs.
    buckets = new HashEntry[s.readInt()];
    int len = s.readInt();
    size = len;
    while (len-- > 0)
      {
        Object key = s.readObject();
        int hash = hashOf(key);
        addEntry(key, s.readObject(), indexFor(hash), false, hash);
      }
  }

  /**
   * Iterate over HashMap's entries.
   * This implementation is parameterized to give a sequential view of
   * keys, values, or entries.
   *
   * @author Jon Zeppieri
   */
  private final class HashIterator implements Iterator
  {
    /**
     * The type of this Iterator: {@link #KEYS}, {@link #VALUES},
     * or {@link #ENTRIES}.
     */
    private final int type;
    /**
     * The number of modifications to the backing HashMap that we know about.
     */
    private int knownMod = modCount;
    /** The number of elements remaining to be returned by next(). */
    private int count = size;
    /** Current index in the physical hash table. */
    private int idx = buckets.length;
    /** The last Entry returned by a next() call. */
    private HashEntry last;
    /**
     * The next entry that should be returned by next(). It is set to something
     * if we're iterating through a bucket that contains multiple linked
     * entries. It is null if next() needs to find a new bucket.
     */
    private HashEntry next;

    /**
     * Construct a new HashIterator with the supplied type.
     * @param type {@link #KEYS}, {@link #VALUES}, or {@link #ENTRIES}
     */
    HashIterator(int type)
    {
      this.type = type;
    }

    /**
     * Returns true if the Iterator has more elements.
     * @return true if there are more elements
     */
    public boolean hasNext()
    {
      return count > 0;
    }

    /**
     * Returns the next element in the Iterator's sequential view.
     * @return the next element
     * @throws ConcurrentModificationException if the HashMap was modified
     * @throws NoSuchElementException if there is none
     */
    public Object next()
    {
      if (knownMod != modCount)
        throw new ConcurrentModificationException();
      if (count == 0)
        throw new NoSuchElementException();
      count--;
      HashEntry e = next;

      while (e == null)
        e = buckets[--idx];

      next = e.next;
      last = e;
      if (type == VALUES)
        return e.value;
      if (type == KEYS)
        return e.key;
      return e;
    }

    /**
     * Removes from the backing HashMap the last element which was fetched
     * with the <code>next()</code> method.
     * @throws ConcurrentModificationException if the HashMap was modified
     * @throws IllegalStateException if called when there is no last element
     */
    public void remove()
    {
      if (knownMod != modCount)
        throw new ConcurrentModificationException();
      if (last == null)
        throw new IllegalStateException();

      HashMap.this.remove(last.key);
      last = null;
      knownMod++;
    }
  }
}
