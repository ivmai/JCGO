/*
 * @(#) $(JCGO)/include/jcgormet.c --
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

JCGO_NOSEP_STATIC jObjectArr CFASTCALL
java_lang_reflect_VMMethod__getMethodsName0__Lc( java_lang_Class klass )
{
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_reflect;
 return JCGO_EXPECT_TRUE(jcgo_reflect != NULL) ?
         jcgo_reflect->methodsName : jnull;
}

JCGO_NOSEP_STATIC jObjectArr CFASTCALL
java_lang_reflect_VMMethod__getMethodsDims0__Lc( java_lang_Class klass )
{
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_reflect;
 return JCGO_EXPECT_TRUE(jcgo_reflect != NULL) ?
         jcgo_reflect->methodsDims : jnull;
}

JCGO_NOSEP_STATIC jObjectArr CFASTCALL
java_lang_reflect_VMMethod__getMethodsThrows0__Lc( java_lang_Class klass )
{
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_reflect;
 return JCGO_EXPECT_TRUE(jcgo_reflect != NULL) ?
         jcgo_reflect->methodsThrows : jnull;
}

JCGO_NOSEP_STATIC jshortArr CFASTCALL
java_lang_reflect_VMMethod__getMethodsModifiers0__Lc( java_lang_Class klass )
{
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_reflect;
 return JCGO_EXPECT_TRUE(jcgo_reflect != NULL) ?
         jcgo_reflect->methodsModifiers : jnull;
}

JCGO_NOSEP_STATIC jObjectArr CFASTCALL
java_lang_reflect_VMMethod__getMethodsSignature0__Lc( java_lang_Class klass )
{
 /* not implemented */
 return jnull;
}

JCGO_NOSEP_STATIC jObjectArr CFASTCALL
java_lang_reflect_VMMethod__getMethodsTypes0__Lc( java_lang_Class klass )
{
 jObjectArr methodsTypes;
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_reflect;
 return JCGO_EXPECT_TRUE(jcgo_reflect != NULL) &&
         (methodsTypes = jcgo_reflect->methodsTypes) != jnull ?
         methodsTypes : ((jvtable)&JCGO_METHODS_OF(
         JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_thisRtn ?
         (jObjectArr)JCGO_OBJREF_OF(jcgo_noTypesClassArr) : jnull;
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_reflect_VMMethod__invokeNative0__LcLoBAIAJAFADALoAIII(
 java_lang_Class declaringClass, java_lang_Object obj, jbyteArr argsTypecodes,
 jintArr intArgs, jlongArr longArgs, jfloatArr floatArgs,
 jdoubleArr doubleArgs, jObjectArr objectArgs, jint argsCnt, jint slot,
 jint allowOverride )
{
 CONST struct jcgo_methodentry_s *pentry;
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(declaringClass, vmdata)))->jcgo_reflect;
 java_lang_Object value = jnull;
 if (JCGO_EXPECT_TRUE(jcgo_reflect != NULL) &&
     (pentry = jcgo_reflect->methodsEntry) != NULL)
 {
  pentry = pentry + (unsigned)slot;
  value = (java_lang_Object)(*pentry->mproxy)(obj != jnull && allowOverride ?
           *(void (CFASTCALL **)(void))((CONST char *)JCGO_METHODS_OF(obj) +
           JCGO_CAST_PFUNCTOUINT(pentry->jmethod)) : pentry->jmethod,
           (jObject)obj, intArgs, longArgs, floatArgs, doubleArgs,
           objectArgs);
 }
 return value;
}

#endif
