/*
 * @(#) $(JCGO)/goclsp/vm/gnu/java/lang/management/VMMemoryManagerMXBeanImpl.java --
 * VM specific memory manager bean implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
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

package gnu.java.lang.management;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

import java.util.ArrayList;
import java.util.Iterator;

final class VMMemoryManagerMXBeanImpl
{

 private VMMemoryManagerMXBeanImpl() {}

 static String[] getMemoryPoolNames(String name)
 {
  ArrayList pools = new ArrayList();
  Iterator beans = ManagementFactory.getMemoryPoolMXBeans().iterator();
  while (beans.hasNext())
  {
   MemoryPoolMXBean bean = (MemoryPoolMXBean) beans.next();
   String[] managers = bean.getMemoryManagerNames();
   for (int i = 0; i < managers.length; i++)
    if (managers[i].equals(name))
    {
     pools.add(bean.getName());
     break;
    }
  }
  String[] names = new String[pools.size()];
  pools.toArray(names);
  return names;
 }

 static boolean isValid(String name)
 {
  return isValid0(name) != 0;
 }

 private static native int isValid0(String name); /* JVM-core */
}
