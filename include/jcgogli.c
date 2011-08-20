/*
 * @(#) $(JCGO)/include/jcgogli.c --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/**
 * This file is compiled together with the files produced by the JCGO
 * translator (do not include and/or compile this file directly).
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

#ifdef JCGO_VER

JCGO_NOSEP_STATIC jlong CFASTCALL
gnu_java_lang_VMInstrumentationImpl__getObjectSize__Lo( java_lang_Object obj )
{
 int typenum;
 if ((typenum = JCGO_METHODS_OF(obj)->jcgo_typeid) <= OBJT_jarray ||
     typenum >= OBJT_jarray + OBJT_void + JCGO_DIMS_MAX)
  return (jlong)JCGO_METHODS_OF(obj)->jcgo_objsize;
 if (typenum >= OBJT_jarray + OBJT_void)
  typenum = OBJT_jarray;
 return (jlong)(jcgo_primitiveSize[typenum - OBJT_jarray] *
         (JCGO_ALLOCSIZE_T)JCGO_ARRAY_NZLENGTH((jObjectArr)obj) +
         jcgo_primitiveOffset[typenum - OBJT_jarray]);
}

JCGO_NOSEP_STATIC jint CFASTCALL
gnu_java_lang_VMInstrumentationImpl__isRedefineClassesSupported0__( void )
{
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
gnu_java_lang_VMInstrumentationImpl__redefineClasses0__LoLoA(
 java_lang_Object inst, jObjectArr definitions )
{
 return 0;
}

JCGO_NOSEP_STATIC jObjectArr CFASTCALL
gnu_java_lang_VMInstrumentationImpl__getAllLoadedClasses0__( void )
{
#ifdef OBJT_java_lang_VMClassLoader
 return (jObjectArr)JCGO_OBJREF_OF(jcgo_classTable);
#else
 return (jObjectArr)jcgo_newArray(JCGO_CLASSREF_OF(java_lang_Class__class),
         0, 0);
#endif
}

JCGO_NOSEP_STATIC jObjectArr CFASTCALL
gnu_java_lang_VMInstrumentationImpl__getInitiatedClasses0__Lo(
 java_lang_Object loader )
{
 /* not implemented */
 return (jObjectArr)jcgo_newArray(JCGO_CLASSREF_OF(java_lang_Class__class),
         0, 0);
}

#endif
