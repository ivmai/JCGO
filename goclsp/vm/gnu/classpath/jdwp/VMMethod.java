/*
 * @(#) $(JCGO)/goclsp/vm/gnu/classpath/jdwp/VMMethod.java --
 * VM specific implementation for JDWP VM method.
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

package gnu.classpath.jdwp;

import java.io.DataOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;

import gnu.classpath.jdwp.exception.JdwpException;
import gnu.classpath.jdwp.exception.NotImplementedException;

import gnu.classpath.jdwp.util.LineTable;
import gnu.classpath.jdwp.util.VariableTable;

public final class VMMethod
{

 public static final int SIZE = 8;

 private final Class klass;

 private final long id;

 private final String name;

 private final String signature;

 private final int modifiers;

 VMMethod(Class klass, long id, String name, String signature, int modifiers)
 { /* used by VM classes only */
  this.klass = klass;
  this.id = id;
  this.name = name;
  this.signature = signature;
  this.modifiers = modifiers;
 }

 public long getId()
 {
  return id;
 }

 public Class getDeclaringClass()
 {
  return klass;
 }

 public String getName()
 {
  return name;
 }

 public String getSignature()
 {
  return signature;
 }

 public int getModifiers()
 {
  return modifiers;
 }

 public void writeId(DataOutputStream ostream)
  throws IOException
 {
  ostream.writeLong(getId());
 }

 public static VMMethod readId(Class klass, ByteBuffer bb)
  throws JdwpException, IOException
 {
  return VMVirtualMachine.getClassMethod(klass, bb.getLong());
 }

 public String toString()
 {
  return getDeclaringClass().getName() + "." + getName();
 }

 public LineTable getLineTable()
  throws JdwpException
 {
  /* not implemented */
  throw new NotImplementedException("getLineTable");
 }

 public VariableTable getVariableTable()
  throws JdwpException
 {
  /* not implemented */
  throw new NotImplementedException("getVariableTable");
 }

 public boolean equals(Object obj)
 {
  return obj == this ||
          (obj instanceof VMMethod && ((VMMethod) obj).getId() == getId());
 }
}
