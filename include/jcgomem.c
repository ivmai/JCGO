/*
 * @(#) $(JCGO)/include/jcgomem.c --
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

#define JCGO_DIMS_MAX 16

struct jcgo_refhidden_s
{
 jvtable jcgo_methods;
 JCGO_MON_DEFN /* second */
 jObject obj; /* third */
#ifdef OBJT_java_lang_ref_SoftReference
 struct jcgo_refkeeper_s *keeper;
#endif
};

#ifndef JCGO_FNLZDATA_OMITREFQUE
struct jcgo_refexthidden_s
{
 struct jcgo_refhidden_s hidden; /* first */
#ifndef JCGO_NOGC
 jObject ref;
#endif
};
#endif

STATICDATA CONST JCGO_ALLOCSIZE_T jcgo_arrayMaxLen[OBJT_void] =
{
 (~(JCGO_ALLOCSIZE_T)sizeof(struct jcgo_jobjectarr_s)) / sizeof(jObject),
 (~(JCGO_ALLOCSIZE_T)sizeof(struct jcgo_jbooleanarr_s)) / sizeof(jboolean),
 ~(JCGO_ALLOCSIZE_T)sizeof(struct jcgo_jbytearr_s),
 (~(JCGO_ALLOCSIZE_T)sizeof(struct jcgo_jchararr_s)) / sizeof(jchar),
 (~(JCGO_ALLOCSIZE_T)sizeof(struct jcgo_jshortarr_s)) / sizeof(jshort),
 (~(JCGO_ALLOCSIZE_T)sizeof(struct jcgo_jintarr_s)) / sizeof(jint),
 (~(JCGO_ALLOCSIZE_T)sizeof(struct jcgo_jlongarr_s)) / sizeof(jlong),
 (~(JCGO_ALLOCSIZE_T)sizeof(struct jcgo_jfloatarr_s)) / sizeof(jfloat),
 (~(JCGO_ALLOCSIZE_T)sizeof(struct jcgo_jdoublearr_s)) / sizeof(jdouble)
};

STATICDATA CONST unsigned jcgo_primitiveOffset[OBJT_void] =
{
 JCGO_OFFSET_OF(struct jcgo_jobjectarr_s, jObject),
 JCGO_OFFSET_OF(struct jcgo_jbooleanarr_s, jboolean),
 JCGO_OFFSET_OF(struct jcgo_jbytearr_s, jbyte),
 JCGO_OFFSET_OF(struct jcgo_jchararr_s, jchar),
 JCGO_OFFSET_OF(struct jcgo_jshortarr_s, jshort),
 JCGO_OFFSET_OF(struct jcgo_jintarr_s, jint),
 JCGO_OFFSET_OF(struct jcgo_jlongarr_s, jlong),
 JCGO_OFFSET_OF(struct jcgo_jfloatarr_s, jfloat),
 JCGO_OFFSET_OF(struct jcgo_jdoublearr_s, jdouble)
};

STATICDATA CONST unsigned jcgo_primitiveSize[OBJT_void] =
{
 sizeof(jObject),
 sizeof(jboolean),
 1,
 sizeof(jchar),
 sizeof(jshort),
 sizeof(jint),
 sizeof(jlong),
 sizeof(jfloat),
 sizeof(jdouble)
};

STATICDATA JCGO_THRD_VOLATILE jlong jcgo_allocatedBytesCnt = (jlong)0L;

#ifndef JCGO_NOGC

#ifdef OBJT_java_lang_VMRuntime

#ifndef JCGO_FNLZDATA_OMITREFQUE
struct jcgo_reffnlzdata_s
{
 struct jcgo_refhidden_s hidden; /* first */
 struct jcgo_refexthidden_s *pexthidden;
 struct jcgo_reffnlzdata_s *next;
 struct jcgo_reffnlzdata_s *prev;
};
#endif

STATIC void GC_CALLBACK jcgo_refFinalizer( void *objbase, void *client_data );

#endif /* OBJT_java_lang_VMRuntime */

#ifdef JCGO_THREADS

STATIC void *GC_CALLBACK jcgo_refGetPtr( void *client_data )
{
 return *(void **)client_data;
}

#endif /* JCGO_THREADS */

#endif /* ! JCGO_NOGC */

#ifdef JCGO_HUGEARR

STATIC void CFASTCALL jcgo_hbzero( void JCGO_HPTR_MOD *dest,
 JCGO_ALLOCSIZE_T size )
{
 unsigned blocksize;
 unsigned invofs;
 if (size > 0)
  for (;;)
  {
   blocksize = (((unsigned)-1) >> 1) - 0xfff;
   if ((invofs = ~(unsigned)JCGO_CAST_PTRTONUM(dest)) < blocksize)
    blocksize = invofs + 1;
   if ((JCGO_ALLOCSIZE_T)blocksize >= size)
    blocksize = (unsigned)size;
   JCGO_MEM_ZERO(dest, blocksize);
   if ((size -= blocksize) == 0)
    break;
   dest = (char JCGO_HPTR_MOD *)dest + blocksize;
  }
}

STATIC void CFASTCALL jcgo_hmemmove( void JCGO_HPTR_MOD *dest,
 CONST void JCGO_HPTR_MOD *src, JCGO_ALLOCSIZE_T size )
{
 unsigned blocksize;
 unsigned invofs;
 if (size > 0 && dest != src)
 {
  if ((char JCGO_HPTR_MOD *)dest - (char JCGO_HPTR_MOD *)NULL <
      (char JCGO_HPTR_MOD *)src - (char JCGO_HPTR_MOD *)NULL)
  {
   for (;;)
   {
    blocksize = (((unsigned)-1) >> 1) - 0xfff;
    if ((invofs = ~(unsigned)JCGO_CAST_PTRTONUM(dest)) < blocksize)
     blocksize = invofs + 1;
    if ((invofs = ~(unsigned)JCGO_CAST_PTRTONUM(src)) < blocksize)
     blocksize = invofs + 1;
    if ((JCGO_ALLOCSIZE_T)blocksize >= size)
     blocksize = (unsigned)size;
    memmove(dest, src, blocksize);
    if ((size -= blocksize) == 0)
     break;
    dest = (char JCGO_HPTR_MOD *)dest + blocksize;
    src = (CONST char JCGO_HPTR_MOD *)src + blocksize;
   }
  }
   else
   {
    dest = (char JCGO_HPTR_MOD *)dest + size;
    src = (CONST char JCGO_HPTR_MOD *)src + size;
    do
    {
     blocksize = (((unsigned)-1) >> 1) - 0xfff;
     if ((invofs = ~(unsigned)JCGO_CAST_PTRTONUM(dest)) < blocksize)
      blocksize = invofs + 1;
     if ((invofs = ~(unsigned)JCGO_CAST_PTRTONUM(src)) < blocksize)
      blocksize = invofs + 1;
     if ((JCGO_ALLOCSIZE_T)blocksize >= size)
      blocksize = (unsigned)size;
     dest = (char JCGO_HPTR_MOD *)dest - blocksize;
     src = (CONST char JCGO_HPTR_MOD *)src - blocksize;
     memmove(dest, src, blocksize);
    } while ((size -= blocksize) > 0);
   }
 }
}

#define JCGO_MEM_HMOVE(dest, src, size) jcgo_hmemmove(dest, src, size)
#define JCGO_MEM_HCOPY(dest, src, size) JCGO_MEM_HMOVE(dest, src, size)

#else /* JCGO_HUGEARR */

#define JCGO_MEM_HMOVE(dest, src, size) (void)memmove(dest, src, size)
#define JCGO_MEM_HCOPY(dest, src, size) JCGO_MEM_CPY(dest, src, size)

#endif /* ! JCGO_HUGEARR */

#ifdef OBJT_java_lang_ref_SoftReference

#ifndef JCGO_SOFTREF_MINAGE
#define JCGO_SOFTREF_MINAGE 1
#define JCGO_SOFTREF_MAXAGE 8
#define JCGO_SOFTREF_TOPAGE 32
#endif

#ifndef JCGO_MEMREFGDAT_LOCK
#define JCGO_MEMREFGDAT_LOCK(x) (void)0
#define JCGO_MEMREFGDAT_UNLOCK(x) (void)0
#endif

struct jcgo_refkeeper_s
{
 jObject obj;
 struct jcgo_refkeeper_s *next;
 unsigned timestamp;
};

STATICDATA unsigned jcgo_gcCount = 0;

#ifndef JCGO_NOGC

#ifndef JCGO_SOFTREF_ALLOCBYTESMIN
#define JCGO_SOFTREF_ALLOCBYTESMIN 0x1000
#endif

STATICDATA unsigned jcgo_lastAllocBytesCnt = 0;

STATICDATA unsigned jcgo_heapExpandHist = 0;

STATICDATA unsigned long jcgo_heapLastSize = 0L;

STATICDATA u_jlong jcgo_heapCurMaxSize = 0L;

STATIC int CFASTCALL jcgo_softRefsProcess( int clearAll )
{
 struct jcgo_refkeeper_s *keeper;
 struct jcgo_refkeeper_s **pnextkeep;
 u_jlong size;
 long deltaSize;
 int age;
 unsigned hist = 0;
 if (JCGO_EXPECT_TRUE(!clearAll))
 {
  size = (u_jlong)JCGO_MEM_HEAPCURSIZE(0);
  deltaSize = (long)jcgo_heapLastSize;
  jcgo_heapLastSize = (unsigned long)size;
  hist = jcgo_heapExpandHist << 2;
  if (jcgo_heapCurMaxSize < size)
  {
   clearAll = 1;
   jcgo_heapCurMaxSize = size;
  }
  if ((deltaSize -= (long)size) != 0L)
  {
   if (deltaSize < 0L)
   {
    hist++;
    if (jcgo_heapExpandHist == ~(unsigned)0)
     clearAll = 1;
   }
   hist++;
  }
  jcgo_heapExpandHist = hist;
 }
 if ((keeper = jcgo_globData.refKeeperList) == NULL)
  return 0;
 if (JCGO_EXPECT_FALSE(clearAll))
 {
  jcgo_globData.refKeeperList = NULL;
  do
  {
   keeper->obj = jnull;
  } while ((keeper = keeper->next) != NULL);
  return 1;
 }
 for (age = 0; hist; hist >>= 2)
  if ((hist & 0x3) != 0)
   age = ((hist & 0x3) << 1) + age - 3;
 age = age > 0 ? (((sizeof(hist) << 2) - age) * (JCGO_SOFTREF_MAXAGE -
        JCGO_SOFTREF_MINAGE + 1)) / (sizeof(hist) << 2) +
        (JCGO_SOFTREF_MINAGE + 1) : JCGO_SOFTREF_TOPAGE + 1;
 pnextkeep = &jcgo_globData.refKeeperList;
 hist = jcgo_gcCount;
 do
 {
  if (keeper->obj == jnull || (int)hist - (int)keeper->timestamp > age)
  {
   do
   {
    keeper->obj = jnull;
   } while ((keeper = keeper->next) != NULL && (keeper->obj == jnull ||
            (int)hist - (int)keeper->timestamp > age));
   if ((*pnextkeep = keeper) == NULL)
    break;
  }
 } while ((keeper = *(pnextkeep = &keeper->next)) != NULL);
 return 1;
}

#endif /* ! JCGO_NOGC */

#endif /* OBJT_java_lang_ref_SoftReference */

STATIC void *CFASTCALL jcgo_memAlloc( JCGO_ALLOCSIZE_T size,
 void *jcgo_methods )
{
 void *ptr = NULL;
#ifdef OBJT_java_lang_ref_SoftReference
#ifdef JCGO_THREADS
#ifndef JCGO_PARALLEL
 struct jcgo_tcb_s *tcb;
#endif
#endif
 unsigned count = 0;
 int changed;
 for (;;)
#endif
 {
  if (JCGO_EXPECT_TRUE(size != 0))
  {
   JCGO_MEM_CALLOC(&ptr, size, jcgo_methods);
   if (JCGO_EXPECT_TRUE(ptr != NULL))
   {
#ifdef OBJT_java_lang_ref_SoftReference
#ifdef JCGO_NOGC
    jcgo_allocatedBytesCnt += (jlong)size;
#else
    if ((unsigned)(jcgo_allocatedBytesCnt += (jlong)size) -
        jcgo_lastAllocBytesCnt < (unsigned)JCGO_SOFTREF_ALLOCBYTESMIN)
     break;
#endif
    if (JCGO_EXPECT_TRUE((unsigned)JCGO_MEM_GCGETCOUNT(0) == jcgo_gcCount))
    {
#ifndef JCGO_NOGC
     jcgo_lastAllocBytesCnt = (unsigned)jcgo_allocatedBytesCnt;
#endif
     break;
    }
#else
    jcgo_allocatedBytesCnt += (jlong)size;
#endif
   }
  }
#ifdef OBJT_java_lang_ref_SoftReference
  changed = 0;
  JCGO_MEMREFGDAT_LOCK(0);
  if ((count = (unsigned)JCGO_MEM_GCGETCOUNT(0)) ==
      (unsigned)(*(volatile int *)&jcgo_gcCount))
  {
   if (ptr == NULL)
   {
    JCGO_MEMREFGDAT_UNLOCK(0);
    JCGO_MEM_GCOLLECT(0);
    changed = 1;
    JCGO_MEMREFGDAT_LOCK(0);
    jcgo_gcCount = (unsigned)JCGO_MEM_GCGETCOUNT(0);
   }
  }
   else
   {
    jcgo_gcCount = count;
    changed = 1;
   }
#ifndef JCGO_NOGC
  if (changed)
   count = (unsigned)jcgo_softRefsProcess(ptr == NULL);
  jcgo_lastAllocBytesCnt = (unsigned)jcgo_allocatedBytesCnt;
#endif
  JCGO_MEMREFGDAT_UNLOCK(0);
  if (!changed)
   break;
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
  JCGO_THREAD_YIELD;
#else
  JCGO_GET_CURTCB(&tcb);
  (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
  JCGO_THREAD_YIELD;
  (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
  jcgo_curTCB = tcb;
#endif
#endif
  if (ptr != NULL || !count)
   break;
#endif
 }
 return ptr;
}

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_newObject( jvtable jcgo_methods )
{
 void *ptr = (void *)jcgo_methods;
#ifndef JCGO_NOGC
#ifdef OBJT_java_lang_VMRuntime
 void *next;
#endif
#endif
 int size;
 if ((size = (int)jcgo_methods->jcgo_objsize) <= 0)
 {
  if (!size)
   JCGO_FATAL_ABORT("Cannot create abstract class instance!");
  ptr = NULL;
  size = -size;
 }
 ptr = jcgo_memAlloc((unsigned)size, ptr);
 if (JCGO_EXPECT_FALSE(ptr == NULL)
#ifndef JCGO_NOGC
#ifdef OBJT_java_lang_VMRuntime
     || (((CONST struct java_lang_Object_methods_s *)
     jcgo_methods)->finalize__ != java_lang_Object__finalize__ &&
     (next = JCGO_PTR_RESERVED, JCGO_MEM_REGJFINALIZER(ptr,
     jcgo_refFinalizer, JCGO_PTR_RESERVED, &next), next != NULL))
#endif
#endif
     )
 {
#ifdef OBJT_java_lang_VMThrowable
  java_lang_VMThrowable__throwOutOfMemoryError0X__();
#else
  JCGO_MEM_HEAPDESTROY;
  JCGO_FATAL_ABORT("Out of memory!");
#endif
 }
 *(jvtable *)&JCGO_METHODS_OF(JCGO_OBJREF_OF(*(jObject)ptr)) = jcgo_methods;
 return JCGO_OBJREF_OF(*(jObject)ptr);
}

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_newArray( java_lang_Class aclass,
 int dims, jint len )
{
 JCGO_ALLOCSIZE_T size;
 int typenum;
 void *ptr = NULL;
 if (JCGO_EXPECT_FALSE(aclass == jnull))
  JCGO_THROW_EXC(jnull);
 if ((typenum = ((jvtable)&JCGO_METHODS_OF(
     JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_typeid) > OBJT_jarray &&
     typenum < OBJT_jarray + OBJT_void + JCGO_DIMS_MAX)
 {
  if (typenum < OBJT_jarray + OBJT_void)
  {
   typenum -= OBJT_jarray;
   aclass = JCGO_CORECLASS_FOR(typenum);
   dims++;
  }
   else
   {
    dims = (typenum -= OBJT_jarray + OBJT_void - 1) + dims;
    do
    {
     aclass = JCGO_FIELD_NZACCESS(aclass, superclass);
    } while (--typenum);
    typenum = ((jvtable)&JCGO_METHODS_OF(
               JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_typeid;
   }
 }
 if (JCGO_EXPECT_FALSE(typenum == OBJT_void ||
     ((jint)(((JCGO_DIMS_MAX - 1) - dims) | dims) | len) < 0))
 {
#ifdef OBJT_java_lang_VMThrowable
  java_lang_VMThrowable__throwNegativeArraySizeException0X__();
#else
  JCGO_FATAL_ABORT("Negative array size specified!");
#endif
 }
 if (JCGO_EXPECT_TRUE(len != (jint)(((u_jint)-1) >> 1)))
 {
  ptr = JCGO_PTR_RESERVED;
  typenum = dims > 0 ? (OBJT_jarray + OBJT_void) + dims :
             typenum < OBJT_void ? OBJT_jarray + typenum :
             OBJT_jarray + OBJT_void;
  dims = 0;
  if (typenum < OBJT_jarray + OBJT_void)
  {
   ptr = NULL;
   dims = typenum - OBJT_jarray;
  }
  if (JCGO_EXPECT_TRUE(len != 0))
  {
   size = 0;
   if (JCGO_EXPECT_TRUE(jcgo_arrayMaxLen[dims] >= (JCGO_ALLOCSIZE_T)len))
   {
    size = jcgo_primitiveSize[dims] * (JCGO_ALLOCSIZE_T)len +
            jcgo_primitiveOffset[dims];
#ifdef JCGO_HUGEARR
    if (size - (JCGO_ALLOCSIZE_T)1 > (unsigned)-1 &&
        (((JCGO_OFFSET_OF(struct jcgo_jobjectarr_s, jObject) %
        sizeof(jObject) == 0) |
        ((JCGO_OFFSET_OF(struct jcgo_jbooleanarr_s, jboolean) %
        sizeof(jboolean) == 0) << OBJT_jboolean) | (1 << OBJT_jbyte) |
        ((JCGO_OFFSET_OF(struct jcgo_jchararr_s, jchar) %
        sizeof(jchar) == 0) << OBJT_jchar) |
        ((JCGO_OFFSET_OF(struct jcgo_jshortarr_s, jshort) %
        sizeof(jshort) == 0) << OBJT_jshort) |
        ((JCGO_OFFSET_OF(struct jcgo_jintarr_s, jint) %
        sizeof(jint) == 0) << OBJT_jint) |
        ((JCGO_OFFSET_OF(struct jcgo_jlongarr_s, jlong) %
        sizeof(jlong) == 0) << OBJT_jlong) |
        ((JCGO_OFFSET_OF(struct jcgo_jfloatarr_s, jfloat) %
        sizeof(jfloat) == 0) << OBJT_jfloat) |
        ((JCGO_OFFSET_OF(struct jcgo_jdoublearr_s, jdouble) %
        sizeof(jdouble) == 0) << OBJT_jdouble)) & (1 << dims)) == 0)
     size = 0;
#endif
   }
  }
   else
   {
    size = jcgo_primitiveOffset[dims];
    ptr = NULL;
   }
  ptr = jcgo_memAlloc(size, ptr);
 }
 if (JCGO_EXPECT_FALSE(ptr == NULL))
 {
#ifdef OBJT_java_lang_VMThrowable
  java_lang_VMThrowable__throwOutOfMemoryError0X__();
#else
  JCGO_MEM_HEAPDESTROY;
  JCGO_FATAL_ABORT("No memory for array!");
#endif
 }
 if (dims)
  *(jvtable *)&JCGO_METHODS_OF(JCGO_OBJREF_OF(*(jObjectArr)ptr)) =
   (jvtable)&JCGO_METHODS_OF(
   jcgo_coreClasses[(unsigned)(typenum - 1)].vmdata);
  else
  {
   *(jvtable *)&JCGO_METHODS_OF(JCGO_OBJREF_OF(*(jObjectArr)ptr)) =
    (jvtable)&JCGO_METHODS_OF(
    jcgo_objArrStubClasses[typenum - (OBJT_jarray + OBJT_void)].vmdata);
   JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(*(jObjectArr)ptr), jcgo_component) =
    aclass;
  }
 *(JCGO_ARRLENGTH_T *)&JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(*(jObjectArr)ptr),
  length) = (JCGO_ARRLENGTH_T)len;
 return JCGO_OBJREF_OF(*(jObject)ptr);
}

#ifndef JCGO_NOGC

#ifdef OBJT_java_lang_VMRuntime

#ifndef JCGO_FNLZDATA_OMITREFQUE

#ifdef OBJT_java_lang_ref_VMReference

#ifdef JCGO_PARALLEL
STATIC void *GC_CALLBACK
#else
JCGO_NOSEP_INLINE void *CFASTCALL
#endif
jcgo_fnlzDataGetRef( void *client_data )
{
 struct jcgo_refexthidden_s *pexthidden =
  ((struct jcgo_reffnlzdata_s *)client_data)->pexthidden;
 return pexthidden != NULL ? pexthidden->ref : jnull;
}

#ifdef JCGO_PARALLEL
#define JCGO_FNLZDATA_GETREF(pfnlzdata) (*(void *volatile *)&(pfnlzdata)->pexthidden != NULL ? (jObject)JCGO_MEM_CALLWITHSYNC(&jcgo_fnlzDataGetRef, pfnlzdata) : jnull)
#else
#define JCGO_FNLZDATA_GETREF(pfnlzdata) (jObject)jcgo_fnlzDataGetRef(pfnlzdata)
#endif

#endif /* OBJT_java_lang_ref_VMReference */

STATIC void *GC_CALLBACK jcgo_fnlzDataAttachNew( void *client_data )
{
 struct jcgo_reffnlzdata_s *pfnlzdata =
  ((struct jcgo_reffnlzdata_s *)client_data)->next;
 struct jcgo_reffnlzdata_s *next;
#ifdef JCGO_PARALLEL
 if (JCGO_EXPECT_TRUE(pfnlzdata != NULL))
#endif
 {
  if (pfnlzdata->pexthidden != NULL)
   pfnlzdata->prev = client_data;
   else
   {
    next = pfnlzdata->next;
    if ((((struct jcgo_reffnlzdata_s *)client_data)->next = next) != NULL &&
        next != JCGO_PTR_RESERVED)
     next->prev = client_data;
   }
 }
 return NULL;
}

#ifdef JCGO_PARALLEL
STATIC void *GC_CALLBACK
#else
JCGO_NOSEP_INLINE void CFASTCALL
#endif
jcgo_fnlzDataExcludeAny( void *client_data )
{
 struct jcgo_reffnlzdata_s *next =
  ((struct jcgo_reffnlzdata_s *)client_data)->next;
 struct jcgo_reffnlzdata_s *prev;
 if ((prev = ((struct jcgo_reffnlzdata_s *)client_data)->prev) != NULL)
  prev->next = next;
 if (next != NULL)
 {
  if (next != JCGO_PTR_RESERVED)
   next->prev = prev;
  ((struct jcgo_reffnlzdata_s *)client_data)->next = NULL;
 }
#ifdef JCGO_PARALLEL
 return NULL;
#endif
}

STATIC void *CFASTCALL jcgo_fnlzDataProcess( void *objbase,
 struct jcgo_reffnlzdata_s *pfnlzdata )
{
 struct jcgo_refexthidden_s *pexthidden;
 struct jcgo_reffnlzdata_s *pfnlzphantoms = NULL;
 struct jcgo_reffnlzdata_s *lastphantom = NULL;
 struct jcgo_reffnlzdata_s *next;
 void *alivelastexthidden = NULL;
 jObject refObj;
#ifdef OBJT_java_lang_ref_VMReference
 struct jcgo_reffnlzdata_s *pfnlzhead = NULL;
#ifndef JCGO_NOJNI
 int hasJniWeak = 0;
#endif
#endif
 do
 {
  next = pfnlzdata->next;
  if ((pexthidden = pfnlzdata->pexthidden) != NULL)
  {
   if (pexthidden->hidden.obj != jnull)
   {
#ifdef JCGO_PARALLEL
    JCGO_MEM_CALLWITHSYNC(&jcgo_fnlzDataExcludeAny, pfnlzdata);
#else
    jcgo_fnlzDataExcludeAny(pfnlzdata);
#endif
    if ((pfnlzdata->prev = lastphantom) != NULL)
     lastphantom->next = pfnlzdata;
     else pfnlzphantoms = pfnlzdata;
    lastphantom = pfnlzdata;
    *(void *volatile *)&alivelastexthidden = pexthidden;
#ifndef JCGO_NOJNI
#ifdef OBJT_java_lang_ref_VMReference
    if (pexthidden->hidden.obj != (jObject)JCGO_PTR_RESERVED)
     hasJniWeak = 1;
#endif
#endif
   }
    else
    {
     JCGO_MEM_REGJFINALIZER(pexthidden, 0, NULL, (void **)&refObj);
#ifdef OBJT_java_lang_ref_VMReference
     if (pfnlzhead == NULL)
      pfnlzhead = pfnlzdata;
#endif
    }
  }
 } while ((pfnlzdata = next) != NULL && pfnlzdata != JCGO_PTR_RESERVED);
 if (pfnlzphantoms != NULL && (pfnlzdata != NULL
#ifndef JCGO_NOJNI
#ifdef OBJT_java_lang_ref_VMReference
     || (hasJniWeak && pfnlzhead != NULL)
#endif
#endif
     ))
 {
  lastphantom->next = JCGO_PTR_RESERVED;
  JCGO_MEM_REGJFINALIZER(objbase, jcgo_refFinalizer, pfnlzphantoms,
   (void **)&lastphantom->next);
  if ((next = lastphantom->next) != JCGO_PTR_RESERVED)
  {
   if (next != NULL)
   {
#ifdef JCGO_PARALLEL
    JCGO_MEM_CALLWITHSYNC(&jcgo_fnlzDataAttachNew, lastphantom);
#else
    jcgo_fnlzDataAttachNew(lastphantom);
#endif
   }
   if (*(void *volatile *)&alivelastexthidden != NULL)
    pfnlzphantoms = NULL;
  }
   else lastphantom->next = NULL;
 }
 for (next = pfnlzphantoms; next != NULL; next = next->next)
  if ((pexthidden = next->pexthidden) != NULL)
  {
   JCGO_MEM_REGJFINALIZER(pexthidden, 0, NULL, (void **)&refObj);
#ifdef OBJT_java_lang_ref_VMReference
   if (pexthidden->hidden.obj != (jObject)JCGO_PTR_RESERVED)
   {
    next->pexthidden = NULL;
    pexthidden->hidden.obj = jnull;
   }
#else
   pexthidden->hidden.obj = jnull;
#endif
  }
#ifdef OBJT_java_lang_ref_VMReference
 if (pfnlzhead != NULL)
  do
  {
   if ((refObj = JCGO_FNLZDATA_GETREF(pfnlzhead)) != jnull)
    java_lang_ref_VMReference__enqueueByGC0X__LoLo((java_lang_Object)refObj,
     jnull);
  } while ((pfnlzhead = pfnlzhead->next) != NULL &&
           pfnlzhead != JCGO_PTR_RESERVED);
 while (pfnlzphantoms != NULL)
 {
  next = pfnlzphantoms->next;
  if ((refObj = JCGO_FNLZDATA_GETREF(pfnlzphantoms)) != jnull)
  {
   pfnlzphantoms->hidden.obj = JCGO_OBJREF_OF(*(jObject)objbase);
   pfnlzphantoms->pexthidden = NULL;
   pfnlzphantoms->next = NULL;
   pfnlzphantoms->prev = NULL;
   java_lang_ref_VMReference__enqueueByGC0X__LoLo((java_lang_Object)refObj,
    (java_lang_Object)pfnlzphantoms);
  }
  pfnlzphantoms = next;
 }
#endif
 return pfnlzdata;
}

#ifdef JCGO_PARALLEL
STATIC void *GC_CALLBACK
#else
JCGO_NOSEP_INLINE void CFASTCALL
#endif
jcgo_fnlzDataDelUnreach( void *client_data )
{
 struct jcgo_reffnlzdata_s *next;
 struct jcgo_reffnlzdata_s *prev;
 if ((prev = ((struct jcgo_reffnlzdata_s *)client_data)->prev) != NULL)
 {
  next = ((struct jcgo_reffnlzdata_s *)client_data)->next;
  ((struct jcgo_reffnlzdata_s *)client_data)->prev = NULL;
  if ((prev->next = next) != NULL && next != JCGO_PTR_RESERVED)
   next->prev = prev;
 }
#ifdef JCGO_PARALLEL
 return NULL;
#endif
}

STATIC void GC_CALLBACK jcgo_refExtHiddenFnlz( void *objbase,
 void *client_data )
{
#ifdef JCGO_PARALLEL
 if (((struct jcgo_reffnlzdata_s *)client_data)->prev != NULL &&
     ((struct jcgo_reffnlzdata_s *)client_data)->pexthidden == NULL)
  JCGO_MEM_CALLWITHSYNC(&jcgo_fnlzDataDelUnreach, client_data);
#else
 if (((struct jcgo_reffnlzdata_s *)client_data)->pexthidden == NULL)
  jcgo_fnlzDataDelUnreach(client_data);
#endif
}

#endif /* ! JCGO_FNLZDATA_OMITREFQUE */

STATIC void GC_CALLBACK jcgo_refFinalizer( void *objbase, void *client_data )
{
 JCGO_CALLBACK_BEGIN
#ifndef JCGO_FNLZDATA_OMITREFQUE
 if (client_data == JCGO_PTR_RESERVED || jcgo_fnlzDataProcess(objbase,
     (struct jcgo_reffnlzdata_s *)client_data) != NULL)
#endif
 {
  java_lang_VMRuntime__finalizeObject0X__Lo(
   JCGO_OBJREF_OF(*(java_lang_Object)objbase));
 }
 JCGO_CALLBACK_END
}

#endif /* OBJT_java_lang_VMRuntime */

#endif /* ! JCGO_NOGC */

#ifndef JCGO_FNLZDATA_OMITREFQUE

#ifdef OBJT_java_lang_ref_ReferenceQueue
STATIC
#else
JCGO_NOSEP_INLINE
#endif
struct jcgo_refexthidden_s *CFASTCALL jcgo_newWeakQueRef( jObject refObj,
 jObject ownerObj, jObject referent, int noclear )
{
 struct jcgo_refexthidden_s *pexthidden =
  jcgo_memAlloc(sizeof(struct jcgo_refexthidden_s), NULL);
#ifndef JCGO_NOGC
#ifdef OBJT_java_lang_VMRuntime
 struct jcgo_reffnlzdata_s *pfnlzdata;
 struct jcgo_reffnlzdata_s *next;
#endif
#endif
 if (JCGO_EXPECT_TRUE(pexthidden != NULL))
 {
  pexthidden->hidden.jcgo_methods = (jvtable)&java_lang_Object_methods;
#ifdef JCGO_NOGC
  pexthidden->hidden.obj = referent;
#else
#ifdef OBJT_java_lang_VMRuntime
  pfnlzdata = jcgo_memAlloc(sizeof(struct jcgo_reffnlzdata_s),
               JCGO_PTR_RESERVED);
  if (JCGO_EXPECT_FALSE(pfnlzdata == NULL))
   return NULL;
  if (noclear)
  {
   pexthidden->hidden.obj = refObj != jnull ? JCGO_PTR_RESERVED : referent;
   pfnlzdata->hidden.jcgo_methods = (jvtable)&java_lang_Object_methods;
  }
   else
#endif
   {
    pexthidden->hidden.obj = referent;
    if (JCGO_MEM_SAFEREGWEAKLINK((void **)&pexthidden->hidden.obj,
        (void *)&JCGO_METHODS_OF(referent),
        JCGO_EXPECT_TRUE(JCGO_METHODS_OF(referent)->jcgo_typeid >
        OBJT_java_lang_String)))
     return NULL;
   }
#ifdef OBJT_java_lang_VMRuntime
  pfnlzdata->pexthidden = pexthidden;
  if (JCGO_MEM_REGWEAKLINK((void **)&pfnlzdata->pexthidden,
      (void *)&JCGO_METHODS_OF(ownerObj)))
   return NULL;
  next = JCGO_PTR_RESERVED;
  JCGO_MEM_REGJFINALIZER(pexthidden, jcgo_refExtHiddenFnlz, pfnlzdata,
   (void **)&next);
  if (JCGO_EXPECT_FALSE(next != NULL))
   return NULL;
  pfnlzdata->next = pfnlzdata;
  pexthidden->ref = refObj;
  JCGO_MEM_SAFEREGJFINALIZER((void *)&JCGO_METHODS_OF(referent),
   jcgo_refFinalizer, pfnlzdata, (void **)&pfnlzdata->next,
   JCGO_EXPECT_TRUE(JCGO_METHODS_OF(referent)->jcgo_typeid >
   OBJT_java_lang_String));
  next = pfnlzdata->next;
  if (JCGO_EXPECT_FALSE(next == pfnlzdata))
  {
   JCGO_MEM_REGJFINALIZER(pexthidden, 0, NULL, (void **)&next);
   return NULL;
  }
  if (next != NULL && next != JCGO_PTR_RESERVED)
  {
#ifdef JCGO_PARALLEL
   JCGO_MEM_CALLWITHSYNC(&jcgo_fnlzDataAttachNew, pfnlzdata);
#else
   jcgo_fnlzDataAttachNew(pfnlzdata);
#endif
  }
#endif
#endif
 }
 return pexthidden;
}

#endif /* ! JCGO_FNLZDATA_OMITREFQUE */

#endif
