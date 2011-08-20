/*
 * @(#) $(JCGO)/goclsp/vm/gnu/classpath/jdwp/VMIdManager.java --
 * VM specific implementation of JDWP IDs manager.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2007 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 **
 * Class specification origin: GNU Classpath v0.93
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

/* VMIdManager.java -- A reference/example implementation of a manager for
   JDWP object/reference type IDs

   Copyright (C) 2005, 2006 Free Software Foundation

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
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package gnu.classpath.jdwp;

import gnu.classpath.jdwp.exception.InvalidClassException;
import gnu.classpath.jdwp.exception.InvalidObjectException;

import gnu.classpath.jdwp.id.ArrayId;
import gnu.classpath.jdwp.id.ArrayReferenceTypeId;
import gnu.classpath.jdwp.id.ClassLoaderId;
import gnu.classpath.jdwp.id.ClassObjectId;
import gnu.classpath.jdwp.id.ClassReferenceTypeId;
import gnu.classpath.jdwp.id.InterfaceReferenceTypeId;
import gnu.classpath.jdwp.id.ObjectId;
import gnu.classpath.jdwp.id.ReferenceTypeId;
import gnu.classpath.jdwp.id.StringId;
import gnu.classpath.jdwp.id.ThreadGroupId;
import gnu.classpath.jdwp.id.ThreadId;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import java.nio.ByteBuffer;

import java.util.HashMap;
import java.util.Hashtable;

public final class VMIdManager
{

 class ReferenceKey extends SoftReference
 {

  private final int hash;

  public ReferenceKey(Object referent)
  {
   super(referent);
   hash = referent.hashCode();
  }

  public ReferenceKey(Object referent, ReferenceQueue queue)
  {
   super(referent, queue);
   hash = referent.hashCode();
  }

  public int hashCode()
  {
   return hash;
  }

  public boolean equals(Object obj)
  {
   return this == obj || (obj instanceof ReferenceKey &&
           ((ReferenceKey) obj).get() == get());
  }
 }

 private static long lastId;

 private static long lastRid;

 private static final Object idLock = new Object();

 private static final Object ridLock = new Object();

 private static final HashMap idList = new HashMap();

 static
 {
  idList.put(ClassLoaderId.typeClass, ClassLoaderId.class);
  idList.put(ClassObjectId.typeClass, ClassObjectId.class);
  idList.put(StringId.typeClass, StringId.class);
  idList.put(ThreadId.typeClass, ThreadId.class);
  idList.put(ThreadGroupId.typeClass, ThreadGroupId.class);
 }

 private static final VMIdManager idm = new VMIdManager();

 private final ReferenceQueue refQueue = new ReferenceQueue();

 private final Hashtable oidTable = new Hashtable(50);

 private final Hashtable idTable = new Hashtable(50);

 private final Hashtable classTable = new Hashtable(20);

 private final Hashtable ridTable = new Hashtable(20);

 private VMIdManager() {}

 public static VMIdManager getDefault()
 {
  return idm;
 }

 public ObjectId getObjectId(Object theObject)
 {
  /* if (theObject == null)
   return new NullObjectId(); */
  ReferenceKey ref = new ReferenceKey(theObject, refQueue);
  ObjectId id = (ObjectId) oidTable.get(ref);
  if (id == null)
  {
   update();
   id = newObjectId(ref);
   oidTable.put(ref, id);
   idTable.put(new Long(id.getId()), id);
  }
  return id;
 }

 public ObjectId get(long id)
  throws InvalidObjectException
 {
  ObjectId oid = (ObjectId) idTable.get(new Long(id));
  if (oid == null)
   throw new InvalidObjectException(id);
  return oid;
 }

 public ObjectId readObjectId(ByteBuffer bb)
  throws InvalidObjectException
 {
  return get(bb.getLong());
 }

 public ReferenceTypeId getReferenceTypeId(Class clazz)
 {
  ReferenceKey ref = new ReferenceKey(clazz);
  ReferenceTypeId id = (ReferenceTypeId) classTable.get(ref);
  if (id == null)
  {
   id = newReferenceTypeId(ref);
   classTable.put(ref, id);
   ridTable.put(new Long(id.getId()), id);
  }
  return id;
 }

 public ReferenceTypeId getReferenceType(long id)
  throws InvalidClassException
 {
  /* if (id == 0L)
   return new NullObjectId(); */
  ReferenceTypeId rid = (ReferenceTypeId) ridTable.get(new Long(id));
  if (rid == null)
   throw new InvalidClassException(id);
  return rid;
 }

 public ReferenceTypeId readReferenceTypeId(ByteBuffer bb)
  throws InvalidClassException
 {
  return getReferenceType(bb.getLong());
 }

 private void update()
 {
  Reference ref;
  while ((ref = refQueue.poll()) != null)
  {
   ObjectId id = (ObjectId) oidTable.get(ref);
   oidTable.remove(ref);
   idTable.remove(new Long(id.getId()));
  }
 }

 private static ObjectId newObjectId(SoftReference obj)
 {
  ObjectId id = null;
  Object object = obj.get();
  if (object.getClass().isArray())
   id = new ArrayId();
   else
   {
    for (Class myClass = object.getClass(); myClass != null;
         myClass = myClass.getSuperclass())
    {
     Class clz = (Class) idList.get(myClass);
     if (clz != null)
     {
      try
      {
       id = (ObjectId) clz.newInstance();
       synchronized (idLock)
       {
        id.setId(++lastId);
       }
       id.setReference(obj);
       return id;
      }
      catch (InstantiationException e)
      {
       throw new RuntimeException("cannot create new ID", e);
      }
      catch (IllegalAccessException e)
      {
       throw new RuntimeException("illegal access of ID", e);
      }
     }
    }
    id = new ObjectId();
   }
  synchronized (idLock)
  {
   id.setId(++lastId);
  }
  id.setReference(obj);
  return id;
 }

 private static ReferenceTypeId newReferenceTypeId(SoftReference ref)
 {
  Class clazz = (Class) ref.get();
  if (clazz == null)
   return null;
  ReferenceTypeId id;
  if (clazz.isArray())
   id = new ArrayReferenceTypeId();
   else if (clazz.isInterface())
    id = new InterfaceReferenceTypeId();
    else id = new ClassReferenceTypeId();
  id.setReference(ref);
  synchronized (ridLock)
  {
   id.setId(++lastRid);
  }
  return id;
 }
}
