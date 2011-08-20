/*
 * @(#) $(JCGO)/goclsp/vm/gnu/java/nio/KqueueSelectorImpl.java --
 * VM selector implementation for systems with "kqueue" notification.
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

package gnu.java.nio;

import java.io.IOException;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

import java.util.Collections;
import java.util.Set;

public class KqueueSelectorImpl extends AbstractSelector
{ /* VM class */

 public KqueueSelectorImpl(SelectorProvider provider)
  throws IOException
 {
  /* not implemented */
  super(provider);
  throw new InternalError("KqueueSelectorImpl() not implemented");
 }

 public Set keys()
 {
  /* not implemented */
  return Collections.EMPTY_SET;
 }

 public int select()
  throws IOException
 {
  return doSelect(-1);
 }

 public int select(long timeout)
  throws IOException
 {
  if (timeout == 0)
   timeout = -1;
  return doSelect(timeout);
 }

 public Set selectedKeys()
 {
  /* not implemented */
  return Collections.EMPTY_SET;
 }

 public int selectNow()
  throws IOException
 {
  return doSelect(0);
 }

 int doSelect(long timeout)
  throws IOException
 {
  /* not implemented */
  return 0;
 }

 public Selector wakeup()
 {
  /* not implemented */
  return this;
 }

 protected void implCloseSelector()
  throws IOException
 {
  /* not implemented */
 }

 protected SelectionKey register(AbstractSelectableChannel ch, int ops,
   Object attachment)
 {
  /* not implemented */
  throw new IllegalArgumentException("unsupported channel type");
 }

 void setInterestOps(KqueueSelectionKeyImpl key, int ops)
 {
  /* not implemented */
 }

 public static boolean kqueue_supported()
 {
  return false;
 }
}
