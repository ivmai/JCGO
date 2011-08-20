/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/ref/VMReference.java --
 * VM specific methods for Java "Reference" class.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2008 Ivan Maidanski <ivmai@ivmaisoft.com>
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

package java.lang.ref;

final class VMReference
{ /* VM class */ /* used by VM classes only */

 static
 {
  if (!"".equals("")) /* hack */
   enqueueByGC0X(null, null); /* hack */
 }

 private VMReference() {}

 static final void initReferent(Reference ref, Object referent)
 {
  if (referent != null)
   setReferenceVmData(initReferent0(referent), ref);
 }

 static final void initEnqueuedReferent(Reference ref, Object referent)
 {
  if (referent != null)
   setReferenceVmData(initEnqueuedReferent0(ref, referent,
    ref instanceof PhantomReference ? 1 : 0), ref);
 }

 static final Object getReferent(Reference ref)
 {
  Object vmdata;
  return (vmdata = ref.vmdata) != null ? (ref instanceof SoftReference ?
          updateSoftRefAndGet0(vmdata) : getReferent0(vmdata)) : null;
 }

 static final void clearReferent(Reference ref)
 {
  ref.vmdata = null;
 }

 static final int enqueueByGC0X(Object refObj, Object vmdata)
 { /* called from native code */
  Reference ref = (Reference) refObj;
  ref.vmdata = vmdata;
  ReferenceQueue queue;
  if ((queue = ref.queue) != null)
   queue.enqueue(ref);
  return 0;
 }

 private static void setReferenceVmData(Object vmdata, Reference ref)
 {
  if (vmdata == null)
   throw new OutOfMemoryError();
  ref.vmdata = vmdata;
  if (ref instanceof SoftReference)
   updateSoftRefAndGet0(vmdata);
 }

 private static native Object initReferent0(Object referent); /* JVM-core */

 private static native Object initEnqueuedReferent0(Object refObj,
   Object referent, int noclear); /* JVM-core */

 private static native Object getReferent0(Object vmdata); /* JVM-core */

 private static native Object updateSoftRefAndGet0(
   Object vmdata); /* JVM-core */
}
