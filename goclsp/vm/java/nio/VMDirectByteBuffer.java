/*
 * @(#) $(JCGO)/goclsp/vm/java/nio/VMDirectByteBuffer.java --
 * VM specific methods for native direct byte buffer implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 **
 * Class specification origin: GNU Classpath v0.93 vm/reference
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

package java.nio;

import gnu.classpath.Pointer;

final class VMDirectByteBuffer /* hard-coded class name */
{

 private static final class DirectPointer extends Pointer
 {

  final transient Object vmdata;

  final int offset;

  DirectPointer(Object vmdata, int offset)
  {
   this.vmdata = vmdata;
   this.offset = offset;
  }
 }

 static
 {
  if ((new DirectPointer(null, 0)).vmdata != null) /* hack */
  {
   Object buf = newDirectByteBuffer0X(null, 0L); /* hack */
   getDirectBufferAddressVmData0X(buf); /* hack */
   getDirectBufferAddressOffset0X(buf); /* hack */
   getDirectBufferCapacity0X(buf); /* hack */
  }
 }

 private VMDirectByteBuffer() {}

 static Pointer allocate(int capacity)
 {
  if (capacity < 0)
   throw new NegativeArraySizeException();
  Object vmdata = allocate0(capacity != 0 ? capacity : 1);
  if (vmdata == null)
   throw new OutOfMemoryError("cannot allocate direct buffer");
  return new DirectPointer(vmdata, 0);
 }

 static void free(Pointer address)
 {
  free0(((DirectPointer) address).vmdata);
 }

 static byte get(Pointer address, int index)
 {
  byte[] dst = new byte[1];
  DirectPointer ptr = (DirectPointer) address;
  getRegion0(ptr.vmdata, dst, 0, 1, ptr.offset + index);
  return dst[0];
 }

 static void get(Pointer address, int index, byte[] dst, int offset,
   int length)
 {
  if (length > 0)
  {
   DirectPointer ptr = (DirectPointer) address;
   getRegion0(ptr.vmdata, dst, offset, length, ptr.offset + index);
  }
 }

 static void put(Pointer address, int index, byte value)
 {
  DirectPointer ptr = (DirectPointer) address;
  putRegion0(ptr.vmdata, new byte[] { value }, 0, 1, ptr.offset + index);
 }

 static void put(Pointer address, int index, byte[] src, int offset,
   int length)
 {
  if (length > 0)
  {
   DirectPointer ptr = (DirectPointer) address;
   putRegion0(ptr.vmdata, src, offset, length, ptr.offset + index);
  }
 }

 static Pointer adjustAddress(Pointer address, int offset)
 {
  DirectPointer ptr = (DirectPointer) address;
  return new DirectPointer(ptr.vmdata, ptr.offset + offset);
 }

 static void shiftDown(Pointer address, int dstOffset, int srcOffset,
   int count)
 {
  if (count > 0 && dstOffset != srcOffset)
  {
   DirectPointer ptr = (DirectPointer) address;
   moveRegion0(ptr.vmdata, ptr.offset + dstOffset, ptr.offset + srcOffset,
    count);
  }
 }

 static final Object newDirectByteBuffer0X(Object vmdata, long capacity)
 { /* called from native code */
  if (vmdata == null)
   throw new NullPointerException();
  if (capacity < 0L)
   throw new NegativeArraySizeException();
  int len = capacity < (long) (-1 >>> 1) ? (int) capacity : -1 >>> 1;
  return new DirectByteBufferImpl.ReadWrite(null,
          new DirectPointer(vmdata, 0), len, len, 0);
 }

 static final Object getDirectBufferAddressVmData0X(Object bufObj)
  throws ClassCastException
 { /* called from native code */
  Pointer address = ((Buffer) bufObj).address;
  return address instanceof DirectPointer ?
          ((DirectPointer) address).vmdata : null;
 }

 static final int getDirectBufferAddressOffset0X(Object bufObj)
  throws ClassCastException
 { /* called from native code */
  return ((DirectPointer) ((Buffer) bufObj).address).offset;
 }

 static final long getDirectBufferCapacity0X(Object bufObj)
  throws ClassCastException
 { /* called from native code */
  Buffer buf = (Buffer) bufObj;
  return buf.address != null ? buf.capacity() : -1;
 }

 private static native Object allocate0(int capacity); /* JVM-core */

 private static native int getRegion0(Object vmdata, byte[] dst, int offset,
   int len, int index); /* JVM-core */

 private static native int putRegion0(Object vmdata, byte[] src, int offset,
   int len, int index); /* JVM-core */

 private static native int moveRegion0(Object vmdata, int dstIndex,
   int srcIndex, int len); /* JVM-core */

 private static native int free0(Object vmdata); /* JVM-core */
}
