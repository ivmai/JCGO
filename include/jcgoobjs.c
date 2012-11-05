/*
 * @(#) $(JCGO)/include/jcgoobjs.c --
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

#define JCGO_OBJARR_COMPCLASS(arr) ((java_lang_Class)JCGO_FIELD_NZACCESS(arr, jcgo_component))

JCGO_NOSEP_DATA CONST struct jcgo_methods_s jboolean_methods =
{
 JCGO_CORECLASS_FOR(OBJT_jboolean),
 JCGO_GCJDESCR_ZEROINIT
 OBJT_jboolean,
 0,
 NULL,
 JCGO_CLINIT_INIT(0)
 0
};

JCGO_NOSEP_DATA CONST struct jcgo_methods_s jbyte_methods =
{
 JCGO_CORECLASS_FOR(OBJT_jbyte),
 JCGO_GCJDESCR_ZEROINIT
 OBJT_jbyte,
 0,
 NULL,
 JCGO_CLINIT_INIT(0)
 0
};

JCGO_NOSEP_DATA CONST struct jcgo_methods_s jchar_methods =
{
 JCGO_CORECLASS_FOR(OBJT_jchar),
 JCGO_GCJDESCR_ZEROINIT
 OBJT_jchar,
 0,
 NULL,
 JCGO_CLINIT_INIT(0)
 0
};

JCGO_NOSEP_DATA CONST struct jcgo_methods_s jshort_methods =
{
 JCGO_CORECLASS_FOR(OBJT_jshort),
 JCGO_GCJDESCR_ZEROINIT
 OBJT_jshort,
 0,
 NULL,
 JCGO_CLINIT_INIT(0)
 0
};

JCGO_NOSEP_DATA CONST struct jcgo_methods_s jint_methods =
{
 JCGO_CORECLASS_FOR(OBJT_jint),
 JCGO_GCJDESCR_ZEROINIT
 OBJT_jint,
 0,
 NULL,
 JCGO_CLINIT_INIT(0)
 0
};

JCGO_NOSEP_DATA CONST struct jcgo_methods_s jlong_methods =
{
 JCGO_CORECLASS_FOR(OBJT_jlong),
 JCGO_GCJDESCR_ZEROINIT
 OBJT_jlong,
 0,
 NULL,
 JCGO_CLINIT_INIT(0)
 0
};

JCGO_NOSEP_DATA CONST struct jcgo_methods_s jfloat_methods =
{
 JCGO_CORECLASS_FOR(OBJT_jfloat),
 JCGO_GCJDESCR_ZEROINIT
 OBJT_jfloat,
 0,
 NULL,
 JCGO_CLINIT_INIT(0)
 0
};

JCGO_NOSEP_DATA CONST struct jcgo_methods_s jdouble_methods =
{
 JCGO_CORECLASS_FOR(OBJT_jdouble),
 JCGO_GCJDESCR_ZEROINIT
 OBJT_jdouble,
 0,
 NULL,
 JCGO_CLINIT_INIT(0)
 0
};

JCGO_NOSEP_DATA CONST struct jcgo_methods_s void_methods =
{
 JCGO_CORECLASS_FOR(OBJT_void),
 JCGO_GCJDESCR_ZEROINIT
 OBJT_void,
 0,
 NULL,
 JCGO_CLINIT_INIT(0)
 0
};

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_bzero( void *dest,
 unsigned size )
{
 JCGO_MEM_ZERO(dest, size);
}

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_memcpy( void *dest,
 CONST void *src, unsigned size )
{
 JCGO_MEM_CPY(dest, (void *)src, size);
}

#ifndef JCGO_SEHTRY

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_bzeroVlt( void *dest,
 unsigned size )
{
#ifdef OBJT_java_lang_Throwable
 *(volatile void **)&jcgo_trashVar = dest;
#endif
 JCGO_MEM_ZERO(dest, size);
}

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_memcpyVlt( void *dest,
 CONST void *src, unsigned size )
{
#ifdef OBJT_java_lang_Throwable
 *(volatile void **)&jcgo_trashVar = dest;
#endif
 JCGO_MEM_CPY(dest, (void *)src, size);
}

#endif /* ! JCGO_SEHTRY */

JCGO_NOSEP_STATIC jObjectArr CFASTCALL jcgo_newMultiArray(
 java_lang_Class aclass, int cnt, int dims, jint *lenlist )
{
 jint index;
 jint len = lenlist[0];
 jObjectArr arr = (jObjectArr)jcgo_newArray(aclass, (--cnt) + dims, len);
 if (JCGO_EXPECT_FALSE(cnt > 0))
  for (index = 0; index < len; index++)
   JCGO_ARR_INTERNALACC(jObject, arr, index) =
    (jObject)jcgo_newMultiArray(aclass, cnt, dims, &lenlist[1]);
 return arr;
}

JCGO_NOSEP_INLINE jObjectArr JCGO_INLFRW_FASTCALL jcgo_new4DArray(
 java_lang_Class aclass, int cnt, int dims, jint len0, jint len1, jint len2,
 jint len3 )
{
 jint lenlist[4];
 lenlist[0] = len0;
 lenlist[1] = len1;
 lenlist[2] = len2;
 lenlist[3] = len3;
 return jcgo_newMultiArray(aclass, cnt, dims, lenlist);
}

JCGO_NOSEP_INLINE jObjectArr JCGO_INLFRW_FASTCALL jcgo_new16DArray(
 java_lang_Class aclass, int cnt, int dims, jint len0, jint len1, jint len2,
 jint len3, jint len4, jint len5, jint len6, jint len7, jint len8, jint len9,
 jint len10, jint len11, jint len12, jint len13, jint len14, jint len15 )
{
 jint lenlist[16];
 lenlist[0] = len0;
 lenlist[1] = len1;
 lenlist[2] = len2;
 lenlist[3] = len3;
 lenlist[4] = len4;
 lenlist[5] = len5;
 lenlist[6] = len6;
 lenlist[7] = len7;
 lenlist[8] = len8;
 lenlist[9] = len9;
 lenlist[10] = len10;
 lenlist[11] = len11;
 lenlist[12] = len12;
 lenlist[13] = len13;
 lenlist[14] = len14;
 lenlist[15] = len15;
 return jcgo_newMultiArray(aclass, cnt, dims, lenlist);
}

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_arrayClone( jObject arr )
{
 jObject newarr;
 unsigned ofs;
 int typenum;
 jint len = JCGO_ARRAY_NZLENGTH((jObjectArr)arr);
 if ((typenum = JCGO_METHODS_OF(arr)->jcgo_typeid - OBJT_jarray) < OBJT_void)
 {
  newarr = jcgo_newArray(JCGO_CORECLASS_FOR(typenum), 0, len);
  if (JCGO_EXPECT_TRUE(len > 0))
  {
   ofs = jcgo_primitiveOffset[typenum];
   JCGO_MEM_HCOPY((void *)((volatile char JCGO_HPTR_MOD *)newarr + ofs),
    (void *)((volatile char JCGO_HPTR_MOD *)arr + ofs),
    jcgo_primitiveSize[typenum] * (JCGO_ALLOCSIZE_T)len);
  }
 }
  else
  {
   newarr = jcgo_newArray(JCGO_OBJARR_COMPCLASS((jObjectArr)arr),
             typenum - OBJT_void, len);
   if (JCGO_EXPECT_TRUE(len > 0))
    JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jObject, (jObjectArr)newarr, 0),
     &JCGO_ARR_INTERNALACC(jObject, (jObjectArr)arr, 0),
     (JCGO_ALLOCSIZE_T)len * sizeof(jObject));
  }
 return newarr;
}

JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkNull( jObject obj )
{
 if (JCGO_EXPECT_FALSE(obj == jnull))
  jcgo_throwNullPtrExc();
 return obj;
}

JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkNullX( jObject obj )
{
 if (JCGO_EXPECT_FALSE(obj == jnull))
  jcgo_throwNullPtrExcX();
 return obj;
}

JCGO_NOSEP_INLINE jint JCGO_INLFRW_FASTCALL jcgo_arrayLength( jObject arr )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExc();
 return (jint)JCGO_FIELD_NZACCESS((jObjectArr)arr, length);
}

#ifdef JCGO_SFTNULLP

JCGO_NOSEP_INLINE jint JCGO_INLFRW_FASTCALL jcgo_arrayLengthX( jObject arr )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 return (jint)JCGO_FIELD_NZACCESS((jObjectArr)arr, length);
}

#endif /* JCGO_SFTNULLP */

JCGO_NOSEP_INLINE jObject *JCGO_INLFRW_FASTCALL jcgo_jObjectArrAccess(
 jObjectArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExc();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jObject, arr, index);
}

JCGO_NOSEP_INLINE jObject *JCGO_INLFRW_FASTCALL jcgo_jObjectArrAccessNZ(
 jObjectArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jObject, arr, index);
}

JCGO_NOSEP_INLINE jboolean *JCGO_INLFRW_FASTCALL jcgo_jbooleanArrAccess(
 jbooleanArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExc();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jboolean, arr, index);
}

JCGO_NOSEP_INLINE jboolean *JCGO_INLFRW_FASTCALL jcgo_jbooleanArrAccessNZ(
 jbooleanArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jboolean, arr, index);
}

JCGO_NOSEP_INLINE jbyte *JCGO_INLFRW_FASTCALL jcgo_jbyteArrAccess(
 jbyteArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExc();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jbyte, arr, index);
}

JCGO_NOSEP_INLINE jbyte *JCGO_INLFRW_FASTCALL jcgo_jbyteArrAccessNZ(
 jbyteArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jbyte, arr, index);
}

JCGO_NOSEP_INLINE jchar *JCGO_INLFRW_FASTCALL jcgo_jcharArrAccess(
 jcharArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExc();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jchar, arr, index);
}

JCGO_NOSEP_INLINE jchar *JCGO_INLFRW_FASTCALL jcgo_jcharArrAccessNZ(
 jcharArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jchar, arr, index);
}

JCGO_NOSEP_INLINE jshort *JCGO_INLFRW_FASTCALL jcgo_jshortArrAccess(
 jshortArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExc();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jshort, arr, index);
}

JCGO_NOSEP_INLINE jshort *JCGO_INLFRW_FASTCALL jcgo_jshortArrAccessNZ(
 jshortArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jshort, arr, index);
}

JCGO_NOSEP_INLINE jint *JCGO_INLFRW_FASTCALL jcgo_jintArrAccess( jintArr arr,
 jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExc();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jint, arr, index);
}

JCGO_NOSEP_INLINE jint *JCGO_INLFRW_FASTCALL jcgo_jintArrAccessNZ(
 jintArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jint, arr, index);
}

JCGO_NOSEP_INLINE jlong *JCGO_INLFRW_FASTCALL jcgo_jlongArrAccess(
 jlongArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExc();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jlong, arr, index);
}

JCGO_NOSEP_INLINE jlong *JCGO_INLFRW_FASTCALL jcgo_jlongArrAccessNZ(
 jlongArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jlong, arr, index);
}

JCGO_NOSEP_INLINE jfloat *JCGO_INLFRW_FASTCALL jcgo_jfloatArrAccess(
 jfloatArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExc();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jfloat, arr, index);
}

JCGO_NOSEP_INLINE jfloat *JCGO_INLFRW_FASTCALL jcgo_jfloatArrAccessNZ(
 jfloatArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jfloat, arr, index);
}

JCGO_NOSEP_INLINE jdouble *JCGO_INLFRW_FASTCALL jcgo_jdoubleArrAccess(
 jdoubleArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExc();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jdouble, arr, index);
}

JCGO_NOSEP_INLINE jdouble *JCGO_INLFRW_FASTCALL jcgo_jdoubleArrAccessNZ(
 jdoubleArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
 return &JCGO_ARR_INTERNALACC(jdouble, arr, index);
}

#ifdef JCGO_INDEXCHK

JCGO_NOSEP_INLINE jObject *JCGO_INLFRW_FASTCALL jcgo_jObjectArrAccessX(
 jObjectArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jObject, arr, index);
}

JCGO_NOSEP_INLINE jObject *JCGO_INLFRW_FASTCALL jcgo_jObjectArrAccessNZX(
 jObjectArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jObject, arr, index);
}

JCGO_NOSEP_INLINE jboolean *JCGO_INLFRW_FASTCALL jcgo_jbooleanArrAccessX(
 jbooleanArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jboolean, arr, index);
}

JCGO_NOSEP_INLINE jboolean *JCGO_INLFRW_FASTCALL jcgo_jbooleanArrAccessNZX(
 jbooleanArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jboolean, arr, index);
}

JCGO_NOSEP_INLINE jbyte *JCGO_INLFRW_FASTCALL jcgo_jbyteArrAccessX(
 jbyteArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jbyte, arr, index);
}

JCGO_NOSEP_INLINE jbyte *JCGO_INLFRW_FASTCALL jcgo_jbyteArrAccessNZX(
 jbyteArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jbyte, arr, index);
}

JCGO_NOSEP_INLINE jchar *JCGO_INLFRW_FASTCALL jcgo_jcharArrAccessX(
 jcharArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jchar, arr, index);
}

JCGO_NOSEP_INLINE jchar *JCGO_INLFRW_FASTCALL jcgo_jcharArrAccessNZX(
 jcharArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jchar, arr, index);
}

JCGO_NOSEP_INLINE jshort *JCGO_INLFRW_FASTCALL jcgo_jshortArrAccessX(
 jshortArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jshort, arr, index);
}

JCGO_NOSEP_INLINE jshort *JCGO_INLFRW_FASTCALL jcgo_jshortArrAccessNZX(
 jshortArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jshort, arr, index);
}

JCGO_NOSEP_INLINE jint *JCGO_INLFRW_FASTCALL jcgo_jintArrAccessX( jintArr arr,
 jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jint, arr, index);
}

JCGO_NOSEP_INLINE jint *JCGO_INLFRW_FASTCALL jcgo_jintArrAccessNZX(
 jintArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jint, arr, index);
}

JCGO_NOSEP_INLINE jlong *JCGO_INLFRW_FASTCALL jcgo_jlongArrAccessX(
 jlongArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jlong, arr, index);
}

JCGO_NOSEP_INLINE jlong *JCGO_INLFRW_FASTCALL jcgo_jlongArrAccessNZX(
 jlongArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jlong, arr, index);
}

JCGO_NOSEP_INLINE jfloat *JCGO_INLFRW_FASTCALL jcgo_jfloatArrAccessX(
 jfloatArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jfloat, arr, index);
}

JCGO_NOSEP_INLINE jfloat *JCGO_INLFRW_FASTCALL jcgo_jfloatArrAccessNZX(
 jfloatArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jfloat, arr, index);
}

JCGO_NOSEP_INLINE jdouble *JCGO_INLFRW_FASTCALL jcgo_jdoubleArrAccessX(
 jdoubleArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jdouble, arr, index);
}

JCGO_NOSEP_INLINE jdouble *JCGO_INLFRW_FASTCALL jcgo_jdoubleArrAccessNZX(
 jdoubleArr arr, jint index )
{
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExcX();
 return &JCGO_ARR_INTERNALACC(jdouble, arr, index);
}

#endif /* JCGO_INDEXCHK */

JCGO_NOSEP_INLINE int JCGO_INLFRW_FASTCALL jcgo_instanceOf0( int objId,
 int maxId, jObject obj )
{
 return obj != jnull && (unsigned)(JCGO_METHODS_OF(obj)->jcgo_typeid -
         objId) <= (unsigned)(maxId - objId);
}

STATIC int CFASTCALL jcgo_implementsInterface( java_lang_Class aclass,
 int objId )
{
 int i;
 jObjectArr interfaces;
 for (;;)
 {
  interfaces = JCGO_FIELD_NZACCESS(aclass, interfaces);
  i = (int)JCGO_ARRAY_NZLENGTH(interfaces);
  if (i <= 0)
   break;
  for (;;)
  {
   i--;
   aclass = (java_lang_Class)JCGO_ARR_INTERNALACC(jObject, interfaces, i);
   if ((int)((jvtable)&JCGO_METHODS_OF(
       JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_typeid == objId)
    return 1;
   if (i <= 0)
    break;
   if (jcgo_implementsInterface(aclass, objId))
    return 1;
  }
 }
 return 0;
}

JCGO_NOSEP_STATIC int CFASTCALL jcgo_instanceOf( int objId, int maxId,
 int dims, jObject obj )
{
 int typenum;
 int iface = 0;
 java_lang_Class aclass;
 if (JCGO_EXPECT_TRUE(obj != jnull))
 {
  typenum = JCGO_METHODS_OF(obj)->jcgo_typeid;
  if (dims < 0)
  {
   iface = 1;
   dims = ~dims;
  }
  aclass = jnull;
  if (dims > 0)
  {
   if (typenum < dims + (OBJT_jarray + OBJT_void - 1))
   {
    if (dims > 1 || typenum <= OBJT_jarray)
     return 0;
    typenum -= OBJT_jarray;
   }
    else
    {
     if (typenum >= OBJT_jarray + OBJT_void + JCGO_DIMS_MAX)
      return 0;
     if ((typenum -= dims) == OBJT_jarray + OBJT_void - 1)
     {
      aclass = JCGO_OBJARR_COMPCLASS((jObjectArr)obj);
      typenum = ((jvtable)&JCGO_METHODS_OF(
                 JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_typeid;
     }
    }
  }
  if (typenum >= objId && typenum <= maxId)
   return 1;
  if (JCGO_EXPECT_FALSE(typenum < OBJT_jarray + OBJT_void + JCGO_DIMS_MAX))
  {
#ifdef OBJT_java_lang_Cloneable
   if (objId == OBJT_java_lang_Cloneable && typenum > OBJT_jarray)
    return 1;
#endif
#ifdef OBJT_java_io_Serializable
   if (objId == OBJT_java_io_Serializable && typenum > OBJT_jarray)
    return 1;
#endif
  }
   else
   {
    if (iface && objId >= OBJT_jarray + OBJT_void + JCGO_DIMS_MAX)
    {
     if (aclass == jnull)
      aclass = JCGO_METHODS_OF(obj)->jcgo_class;
     do
     {
      if (jcgo_implementsInterface(aclass, objId))
       return 1;
     } while ((aclass = JCGO_FIELD_NZACCESS(aclass, superclass)) != jnull);
    }
   }
 }
 return 0;
}

STATIC int CFASTCALL jcgo_isAssignable( java_lang_Class srcClass,
 java_lang_Class destClass, int srcDims, int destDims )
{
 int typenum = ((jvtable)&JCGO_METHODS_OF(
                JCGO_FIELD_NZACCESS(srcClass, vmdata)))->jcgo_typeid;
 int objId;
 if (JCGO_EXPECT_FALSE(typenum > OBJT_jarray &&
     typenum < OBJT_jarray + OBJT_void + JCGO_DIMS_MAX))
 {
  if (typenum <= OBJT_jarray + OBJT_void)
   typenum = OBJT_jarray + OBJT_void;
  typenum -= OBJT_jarray + OBJT_void - 1;
  srcDims += typenum;
  do
  {
   srcClass = JCGO_FIELD_NZACCESS(srcClass, superclass);
  } while (--typenum);
  typenum = ((jvtable)&JCGO_METHODS_OF(
             JCGO_FIELD_NZACCESS(srcClass, vmdata)))->jcgo_typeid;
 }
 objId = ((jvtable)&JCGO_METHODS_OF(
          JCGO_FIELD_NZACCESS(destClass, vmdata)))->jcgo_typeid;
 if (JCGO_EXPECT_FALSE(objId > OBJT_jarray &&
     objId < OBJT_jarray + OBJT_void + JCGO_DIMS_MAX))
 {
  if (objId <= OBJT_jarray + OBJT_void)
   objId = OBJT_jarray + OBJT_void;
  objId -= OBJT_jarray + OBJT_void - 1;
  destDims += objId;
  do
  {
   destClass = JCGO_FIELD_NZACCESS(destClass, superclass);
  } while (--objId);
  objId = ((jvtable)&JCGO_METHODS_OF(
           JCGO_FIELD_NZACCESS(destClass, vmdata)))->jcgo_typeid;
 }
 if (JCGO_EXPECT_TRUE(srcDims == destDims))
 {
  if (JCGO_EXPECT_TRUE(srcClass == destClass))
   return 1;
  if (typenum > OBJT_jarray && objId >= OBJT_jarray)
  {
   if (objId == OBJT_jarray)
    return 1;
   if (JCGO_FIELD_NZACCESS(destClass, superclass) != jnull)
   {
    if (typenum > objId)
     do
     {
      if ((srcClass = JCGO_FIELD_NZACCESS(srcClass, superclass)) == destClass)
       return 1;
     } while (srcClass != jnull);
   }
    else
    {
     do
     {
      if (jcgo_implementsInterface(srcClass, objId))
       return 1;
      srcClass = JCGO_FIELD_NZACCESS(srcClass, superclass);
     } while (srcClass != jnull);
    }
  }
 }
  else
  {
   if (JCGO_EXPECT_FALSE(srcDims > destDims))
   {
    if (objId == OBJT_jarray)
     return 1;
#ifdef OBJT_java_lang_Cloneable
    if (objId == OBJT_java_lang_Cloneable)
     return 1;
#endif
#ifdef OBJT_java_io_Serializable
    if (objId == OBJT_java_io_Serializable)
     return 1;
#endif
   }
  }
 return 0;
}

JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkCast0( int objId,
 int maxId, jObject obj )
{
 if (obj != jnull &&
     JCGO_EXPECT_FALSE((unsigned)(JCGO_METHODS_OF(obj)->jcgo_typeid - objId) >
     (unsigned)(maxId - objId)))
  jcgo_throwClassCastExc();
 return obj;
}

JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkCast( int objId,
 int maxId, int dims, jObject obj )
{
 if (obj != jnull &&
     JCGO_EXPECT_FALSE(!jcgo_instanceOf(objId, maxId, dims, obj)))
  jcgo_throwClassCastExc();
 return obj;
}

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_objArraySet( jObjectArr arr,
 jint index, jObject obj )
{
#ifdef OBJT_java_lang_VMThrowable
 java_lang_Class srcClass;
 java_lang_Class destClass;
 int typenum;
 int destDims;
#endif
 if (JCGO_EXPECT_FALSE(arr == jnull))
  JCGO_THROW_EXC(jnull);
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
  jcgo_throwArrayIndexExc();
#ifdef OBJT_java_lang_VMThrowable
 if (obj != jnull)
 {
  destClass = JCGO_OBJARR_COMPCLASS(arr);
  srcClass = JCGO_METHODS_OF(obj)->jcgo_class;
  destDims = JCGO_METHODS_OF(arr)->jcgo_typeid - (OBJT_jarray + OBJT_void);
  if (destDims || srcClass != destClass)
  {
   typenum = JCGO_METHODS_OF(obj)->jcgo_typeid;
   if (typenum >= OBJT_jarray + OBJT_void &&
       typenum < OBJT_jarray + OBJT_void + JCGO_DIMS_MAX)
    srcClass = JCGO_OBJARR_COMPCLASS((jObjectArr)obj);
    else typenum = OBJT_jarray + OBJT_void - 1;
   if (JCGO_EXPECT_FALSE(!jcgo_isAssignable(srcClass, destClass,
       typenum - (OBJT_jarray + OBJT_void - 1), destDims)))
    java_lang_VMThrowable__throwArrayStoreException0X__();
  }
 }
#endif
 JCGO_ARR_INTERNALACC(jObject, arr, index) = obj;
 return obj;
}

#ifdef JCGO_CHKCAST

JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkCast0X( int objId,
 int maxId, jObject obj )
{
 if (obj != jnull &&
     JCGO_EXPECT_FALSE((unsigned)(JCGO_METHODS_OF(obj)->jcgo_typeid - objId) >
     (unsigned)(maxId - objId)))
  jcgo_throwClassCastExcX();
 return obj;
}

JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkCastX( int objId,
 int maxId, int dims, jObject obj )
{
 if (obj != jnull &&
     JCGO_EXPECT_FALSE(!jcgo_instanceOf(objId, maxId, dims, obj)))
  jcgo_throwClassCastExcX();
 return obj;
}

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_objArraySetX( jObjectArr arr,
 jint index, jObject obj )
{
 java_lang_Class srcClass;
 java_lang_Class destClass;
 int typenum;
 int destDims;
 if (JCGO_EXPECT_FALSE(arr == jnull))
  jcgo_throwNullPtrExcX();
 if (JCGO_EXPECT_FALSE((u_jint)JCGO_ARRAY_NZLENGTH(arr) <= (u_jint)index))
 {
#ifdef JCGO_INDEXCHK
  jcgo_throwArrayIndexExcX();
#else
  jcgo_throwArrayIndexExc();
#endif
 }
 if (obj != jnull)
 {
  destClass = JCGO_OBJARR_COMPCLASS(arr);
  srcClass = JCGO_METHODS_OF(obj)->jcgo_class;
  destDims = JCGO_METHODS_OF(arr)->jcgo_typeid - (OBJT_jarray + OBJT_void);
  if (destDims || srcClass != destClass)
  {
   typenum = JCGO_METHODS_OF(obj)->jcgo_typeid;
   if (typenum >= OBJT_jarray + OBJT_void &&
       typenum < OBJT_jarray + OBJT_void + JCGO_DIMS_MAX)
    srcClass = JCGO_OBJARR_COMPCLASS((jObjectArr)obj);
    else typenum = OBJT_jarray + OBJT_void - 1;
   if (JCGO_EXPECT_FALSE(!jcgo_isAssignable(srcClass, destClass,
       typenum - (OBJT_jarray + OBJT_void - 1), destDims)))
    jcgo_throwArrayStoreExcX();
  }
 }
 JCGO_ARR_INTERNALACC(jObject, arr, index) = obj;
 return obj;
}

#endif /* JCGO_CHKCAST */

JCGO_NOSEP_INLINE java_lang_Class CFASTCALL jcgo_findClass(
 java_lang_String name, int nextInner )
{
#ifdef OBJT_java_lang_VMClassLoader
 jint cmp;
 unsigned i;
 unsigned low = 0;
 unsigned high = (unsigned)JCGO_ARRAY_NZLENGTH(
                  (jObjectArr)JCGO_OBJREF_OF(jcgo_classTable));
 do
 {
  i = (low + high) >> 1;
  cmp = java_lang_VMClassLoader__compareClassNames0X__LsLc(name,
         (java_lang_Class)JCGO_ARR_INTERNALACC(jObject,
         (jObjectArr)JCGO_OBJREF_OF(jcgo_classTable), i));
  if (JCGO_EXPECT_FALSE(!cmp))
  {
   if (nextInner && ++i >= (unsigned)JCGO_ARRAY_NZLENGTH(
       (jObjectArr)JCGO_OBJREF_OF(jcgo_classTable)))
    break;
   return (java_lang_Class)JCGO_ARR_INTERNALACC(jObject,
           (jObjectArr)JCGO_OBJREF_OF(jcgo_classTable), i);
  }
  if (cmp > 0)
   low = i + 1;
   else high = i;
 } while (JCGO_EXPECT_TRUE(low < high));
#endif
 return jnull;
}

#endif
