/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/ref/ReferenceQueue.java --
 * VM specific Java "ReferenceQueue" class implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2008 Ivan Maidanski <ivmai@ivmaisoft.com>
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

/* java.lang.ref.ReferenceQueue
   Copyright (C) 1999, 2006 Free Software Foundation, Inc.

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

package java.lang.ref;

public class ReferenceQueue
{ /* VM class */

 private final Object lock = new Object();

 private Reference first;

 private Reference registeredRefs;

 public ReferenceQueue() {}

 final void registerReference(Reference ref)
 { /* used by VM classes only */
  synchronized (lock)
  {
   Reference prev;
   if ((prev = registeredRefs) != null)
   {
    ref.prevOnRegQueue = prev;
    (ref.nextOnQueue = prev.nextOnQueue).prevOnRegQueue = ref;
    prev.nextOnQueue = ref;
   }
    else
    {
     (ref.nextOnQueue = ref).prevOnRegQueue = ref;
     registeredRefs = ref;
    }
  }
 }

 final boolean enqueue(Reference ref)
 {
  synchronized (lock)
  {
   if (ref.queue != this)
    return false;
   Reference prev;
   if ((prev = ref.prevOnRegQueue) != ref)
   {
    (prev.nextOnQueue = ref.nextOnQueue).prevOnRegQueue = prev;
    if (registeredRefs == ref)
     registeredRefs = prev;
   }
    else registeredRefs = null;
   ref.nextOnQueue = first != null ? first : ref;
   ref.prevOnRegQueue = null;
   ref.queue = null;
   first = ref;
   lock.notify();
  }
  return true;
 }

 private Reference dequeue()
 {
  Reference ref;
  if ((ref = first) != null)
  {
   Reference next = ref.nextOnQueue;
   first = next != ref ? next : null;
   ref.nextOnQueue = null;
  }
  return ref;
 }

 public Reference poll()
 {
  synchronized (lock)
  {
   return dequeue();
  }
 }

 public Reference remove(long timeout)
  throws InterruptedException
 {
  if (timeout < 0L)
   throw new IllegalArgumentException("timeout value is negative");
  Reference ref;
  synchronized (lock)
  {
   ref = dequeue();
   while (ref == null)
   {
    lock.wait(timeout);
    ref = dequeue();
    if (timeout > 0L)
     break;
   }
  }
  return ref;
 }

 public Reference remove()
  throws InterruptedException
 {
  return remove(0L);
 }
}
