/*
 * @(#) $(JCGO)/goclsp/vm/gnu/java/lang/VMInstrumentationImpl.java --
 * VM specific class instrumentation implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2007 Ivan Maidanski <ivmai@ivmaisoft.com>
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

package gnu.java.lang;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;

final class VMInstrumentationImpl
{

 private VMInstrumentationImpl() {}

 static boolean isRedefineClassesSupported()
 {
  return isRedefineClassesSupported0() != 0;
 }

 static void redefineClasses(Instrumentation inst,
   ClassDefinition[] definitions)
 {
  redefineClasses0(inst, definitions);
 }

 static Class[] getAllLoadedClasses()
 {
  return classesGuardCopy(getAllLoadedClasses0());
 }

 static Class[] getInitiatedClasses(ClassLoader loader)
 {
  return classesGuardCopy(getInitiatedClasses0(loader));
 }

 static native long getObjectSize(Object obj); /* JVM-core */

 static final int getAllLoadedClassCount()
 { /* used by VM classes only */
  return getAllLoadedClasses0().length;
 }

 private static Class[] classesGuardCopy(Class[] classes)
 {
  Class[] newClasses = new Class[classes.length];
  System.arraycopy(classes, 0, newClasses, 0, classes.length);
  return newClasses;
 }

 private static native int isRedefineClassesSupported0(); /* JVM-core */

 private static native int redefineClasses0(Object inst,
   Object[] definitions); /* JVM-core */

 private static native
   Class[] getAllLoadedClasses0(); /* JVM-core */ /* const data */

 private static native Class[] getInitiatedClasses0(
   Object loader); /* JVM-core */
}
