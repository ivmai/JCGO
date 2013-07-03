/*
 * @(#) $(JCGO)/include/jcgorctr.c --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@ivmaisoft.com>
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

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_reflect_VMConstructor__constructNative0__LcLcBAIAJAFADALoAII(
 java_lang_Class objClass, java_lang_Class declaringClass,
 jbyteArr argsTypecodes, jintArr intArgs, jlongArr longArgs,
 jfloatArr floatArgs, jdoubleArr doubleArgs, jObjectArr objectArgs,
 jint argsCnt, jint slot )
{
 jObject (CFASTCALL *rtn)(jObject) = 0;
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(declaringClass, vmdata)))->jcgo_reflect;
 CONST struct jcgo_methodentry_s *pentry = NULL;
 jObject obj = jnull;
 if ((JCGO_EXPECT_TRUE(jcgo_reflect != NULL) &&
     (pentry = jcgo_reflect->methodsEntry) != NULL) ||
     (rtn = ((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(declaringClass,
     vmdata)))->jcgo_thisRtn) != 0)
 {
  obj = jcgo_newObject((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(objClass,
         vmdata)));
  if (pentry != NULL)
  {
   pentry = pentry + (unsigned)slot;
   obj = (*pentry->mproxy)(pentry->jmethod, obj, intArgs, longArgs, floatArgs,
          doubleArgs, objectArgs);
  }
   else obj = (*rtn)(obj);
 }
 return (java_lang_Object)obj;
}

#endif
