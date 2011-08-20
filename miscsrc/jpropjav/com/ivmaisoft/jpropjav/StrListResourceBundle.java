/*
 * @(#) com/ivmaisoft/jpropjav/StrListResourceBundle.java --
 * a resource bundle build around a "flat" list of strings.
 **
 * Copyright (C) 2007-2009 Ivan Maidanski <ivmai@mail.ru> All rights reserved.
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

/**
 * StrListResourceBundle is an abstract subclass of ResourceBundle
 * class that manages resources for a locale in a convenient and easy
 * to use list (similar to the standard ListResourceBundle class but
 * more space-efficient).
 **
 * The subclasses must override getContents() and provide an array of
 * non-null strings, where each item at an even index in the array is
 * a key, and its successor element (at an odd index) is the value
 * associated with that key.
 */

package com.ivmaisoft.jpropjav;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public abstract class StrListResourceBundle extends ResourceBundle
{

 private HashMap lookup;

 public StrListResourceBundle() {}

 /**
  * Returns an array of non-null strings (every element at an even index
  * is a key, the successor is the value corresponding to that key).
  */
 protected abstract String[] getContents();

 public final Object handleGetObject(String key)
 {
  if (key == null)
   throw new NullPointerException();
  return lookup().get(key);
 }

 public final Enumeration getKeys()
 {
  Set set = lookup().keySet();
  ResourceBundle bundle = parent;
  Enumeration en;
  if (bundle != null && (en = bundle.getKeys()).hasMoreElements())
  {
   set = new HashSet(set);
   do
   {
    set.add(en.nextElement());
   } while (en.hasMoreElements());
  }
  return Collections.enumeration(set);
 }

 protected final Set handleKeySet()
 {
  return lookup().keySet();
 }

 private synchronized HashMap lookup()
 {
  HashMap map = lookup;
  if (map == null)
  {
   String[] contents = getContents();
   int len = contents.length - 1;
   map = new HashMap(((len / 3) << 1) + 3);
   for (int i = 0; i < len; i += 2)
   {
    String key = contents[i];
    String value = contents[i + 1];
    if (key != null && value != null)
     map.put(key, value);
   }
   lookup = map;
  }
  return map;
 }
}
