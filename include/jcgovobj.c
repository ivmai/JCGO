/*
 * @(#) $(JCGO)/include/jcgovobj.c --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2010 Ivan Maidanski <ivmai@ivmaisoft.com>
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

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMObject__getObjArrayDims0__Lo( java_lang_Object obj )
{
 int dims = JCGO_METHODS_OF(obj)->jcgo_typeid - (OBJT_jarray + OBJT_void);
 if ((unsigned)dims >= (unsigned)JCGO_DIMS_MAX)
  return 0;
 if (((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(JCGO_OBJARR_COMPCLASS(
     (jObjectArr)obj), vmdata)))->jcgo_typeid > OBJT_void)
  dims++;
 return (jint)dims;
}

JCGO_NOSEP_STATIC java_lang_Class CFASTCALL
java_lang_VMObject__getClass0__Lo( java_lang_Object obj )
{
 java_lang_Class aclass;
 int typenum;
 if ((unsigned)(JCGO_METHODS_OF(obj)->jcgo_typeid -
     (OBJT_jarray + OBJT_void)) >= (unsigned)JCGO_DIMS_MAX)
  return JCGO_METHODS_OF(obj)->jcgo_class;
 aclass = JCGO_OBJARR_COMPCLASS((jObjectArr)obj);
 return (typenum = ((jvtable)&JCGO_METHODS_OF(
         JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_typeid) < OBJT_void ?
         JCGO_CORECLASS_FOR(typenum + OBJT_jarray) : aclass;
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_VMObject__clone0__Lo( java_lang_Object obj )
{
 java_lang_Object newObj;
 int size;
 int typenum;
 if ((typenum = JCGO_METHODS_OF(obj)->jcgo_typeid) <= OBJT_jarray ||
     typenum >= OBJT_jarray + OBJT_void + JCGO_DIMS_MAX)
 {
  newObj = (java_lang_Object)jcgo_newObject((jvtable)JCGO_METHODS_OF(obj));
  size = (int)JCGO_METHODS_OF(obj)->jcgo_objsize;
  JCGO_MEM_CPY((void *)&JCGO_METHODS_OF(newObj),
   (void *)&JCGO_METHODS_OF(obj), (unsigned)(size < 0 ? -size : size));
#ifdef JCGO_THREADS
  JCGO_FIELD_NZACCESS(newObj, jcgo_mon) = NULL;
#endif
 }
  else newObj = (java_lang_Object)jcgo_arrayClone((jObject)obj);
 return newObj;
}

#endif
