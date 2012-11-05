/*
 * @(#) $(JCGO)/include/jcgorefs.c --
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

#ifndef JCGO_NOGC
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
#define JCGO_WEAKPTR_GET(ptrval) (*(void *volatile *)&(ptrval) != NULL ? JCGO_MEM_CALLWITHSYNC(&jcgo_refGetPtr, &(ptrval)) : NULL)
#endif
#endif
#endif

#ifndef JCGO_WEAKPTR_GET
#define JCGO_WEAKPTR_GET(ptrval) (*(void **)&(ptrval))
#endif

#ifdef OBJT_java_lang_ref_SoftReference

#ifdef JCGO_PARALLEL
STATIC void *GC_CALLBACK
#else
JCGO_NOSEP_INLINE void *CFASTCALL
#endif
jcgo_softRefGetReferent( void *client_data )
{
 jObject referent;
 struct jcgo_refhidden_s *phidden = *(struct jcgo_refhidden_s **)client_data;
 if ((*(struct jcgo_refkeeper_s **)client_data = phidden->keeper) != NULL)
 {
  if ((referent = (*(struct jcgo_refkeeper_s **)client_data)->obj) != jnull)
   return referent;
  *(void **)client_data = NULL;
 }
 return phidden->obj;
}

#endif /* OBJT_java_lang_ref_SoftReference */

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_ref_VMReference__initReferent0__Lo( java_lang_Object referent )
{
 struct jcgo_refhidden_s *phidden =
  jcgo_memAlloc(sizeof(struct jcgo_refhidden_s), NULL);
 if (JCGO_EXPECT_FALSE(phidden == NULL))
  return jnull;
 phidden->jcgo_methods = (jvtable)&java_lang_Object_methods;
 phidden->obj = (jObject)referent;
 return JCGO_MEM_SAFEREGWEAKLINK((void **)&phidden->obj,
         (void *)&JCGO_METHODS_OF(referent),
         JCGO_EXPECT_TRUE(JCGO_METHODS_OF(referent)->jcgo_typeid >
         OBJT_java_lang_String)) ? jnull :
         JCGO_OBJREF_OF(*(java_lang_Object)phidden);
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_ref_VMReference__initEnqueuedReferent0__LoLoI(
 java_lang_Object refObj, java_lang_Object referent, jint noclear )
{
#ifndef JCGO_FNLZDATA_OMITREFQUE
 struct jcgo_refexthidden_s *pexthidden =
  jcgo_newWeakQueRef((jObject)refObj, (jObject)refObj, (jObject)referent,
  (int)noclear);
 if (JCGO_EXPECT_TRUE(pexthidden != NULL))
  return JCGO_OBJREF_OF(*(java_lang_Object)pexthidden);
#endif
 return jnull;
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_ref_VMReference__getReferent0__Lo( java_lang_Object vmdata )
{
 return (java_lang_Object)JCGO_WEAKPTR_GET(((struct jcgo_refhidden_s *)
         &JCGO_METHODS_OF(vmdata))->obj);
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_ref_VMReference__updateSoftRefAndGet0__Lo( java_lang_Object vmdata )
{
 jObject referent = jnull;
#ifdef OBJT_java_lang_ref_SoftReference
 struct jcgo_refkeeper_s *keeper;
#ifndef JCGO_NOGC
 int res;
#endif
 if (((struct jcgo_refhidden_s *)&JCGO_METHODS_OF(vmdata))->obj != jnull)
 {
  keeper = (struct jcgo_refkeeper_s *)&JCGO_METHODS_OF(vmdata);
#ifdef JCGO_PARALLEL
  referent = (jObject)JCGO_MEM_CALLWITHSYNC(&jcgo_softRefGetReferent,
              &keeper);
#else
  referent = (jObject)jcgo_softRefGetReferent(&keeper);
#endif
#ifndef JCGO_NOGC
  if (referent != jnull)
  {
   if (keeper != NULL)
    keeper->timestamp = jcgo_gcCount;
    else
    {
     keeper = jcgo_memAlloc(sizeof(struct jcgo_refkeeper_s),
               JCGO_PTR_RESERVED);
     if (JCGO_EXPECT_TRUE(keeper != NULL))
     {
      keeper->obj = referent;
      res = JCGO_MEM_REGWEAKLINK((void **)&keeper->obj,
             (void *)&JCGO_METHODS_OF(vmdata));
      keeper->timestamp = jcgo_gcCount;
      if (JCGO_EXPECT_TRUE(!res))
      {
       JCGO_MEMREFGDAT_LOCK(0);
       keeper->next = jcgo_globData.refKeeperList;
       jcgo_globData.refKeeperList = keeper;
       JCGO_MEMREFGDAT_UNLOCK(0);
       ((struct jcgo_refhidden_s *)&JCGO_METHODS_OF(vmdata))->keeper = keeper;
       res = JCGO_MEM_REGWEAKLINK((void **)&((struct jcgo_refhidden_s *)
              &JCGO_METHODS_OF(vmdata))->keeper, keeper);
      }
      if (JCGO_EXPECT_FALSE((unsigned)res > 1))
      {
       keeper->obj = jnull;
       ((struct jcgo_refhidden_s *)&JCGO_METHODS_OF(vmdata))->keeper = NULL;
      }
     }
    }
  }
#endif
 }
#endif
 return (java_lang_Object)referent;
}

#endif
