/*
 * @(#) $(JCGO)/include/jcgosyst.c --
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

JCGO_NOSEP_STATIC void CFASTCALL
java_lang_VMSystem__arraycopy__LoILoII( java_lang_Object src,
 java_lang_Object dest, jint srcStart, jint destStart, jint len )
{
 int typenum;
 int destDims;
#ifdef JCGO_CHKCAST
 java_lang_Class srcClass;
 java_lang_Class destClass;
 jObject obj;
#endif
 if (JCGO_EXPECT_FALSE(src == jnull || dest == jnull))
  JCGO_THROW_EXC(jnull);
 typenum = (int)JCGO_METHODS_OF(src)->jcgo_typeid - OBJT_jarray;
 destDims = (int)JCGO_METHODS_OF(dest)->jcgo_typeid -
             (OBJT_jarray + OBJT_void);
 if (JCGO_EXPECT_FALSE(typenum <= 0 || typenum >= OBJT_void + JCGO_DIMS_MAX ||
     (typenum - destDims != OBJT_void && (typenum < OBJT_void ||
     (unsigned)destDims >= (unsigned)JCGO_DIMS_MAX))))
 {
#ifdef OBJT_java_lang_VMThrowable
  java_lang_VMThrowable__throwArrayStoreException0X__();
#else
  return;
#endif
 }
 if (JCGO_EXPECT_FALSE((srcStart | destStart | len) < 0 ||
     JCGO_ARRAY_NZLENGTH((jObjectArr)src) - len < srcStart ||
     JCGO_ARRAY_NZLENGTH((jObjectArr)dest) - len < destStart))
 {
#ifdef OBJT_java_lang_VMThrowable
  java_lang_VMThrowable__throwArrayIndexOutOfBoundsException0X__();
#else
  return;
#endif
 }
 if (JCGO_EXPECT_TRUE(len > 0 && (srcStart != destStart || src != dest)))
 {
  if (typenum >= OBJT_void)
  {
#ifdef JCGO_CHKCAST
   destClass = JCGO_OBJARR_COMPCLASS((jObjectArr)dest);
   if (src != dest && JCGO_EXPECT_FALSE(!jcgo_isAssignable(
       JCGO_OBJARR_COMPCLASS((jObjectArr)src), destClass,
       typenum - OBJT_void, destDims)))
   {
    do
    {
     if ((obj = JCGO_ARR_INTERNALACC(jObject, (jObjectArr)src, srcStart)) !=
         jnull && ((srcClass = JCGO_METHODS_OF(obj)->jcgo_class) !=
         destClass || destDims > 0))
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
     JCGO_ARR_INTERNALACC(jObject, (jObjectArr)dest, destStart) = obj;
     srcStart++;
     destStart++;
    } while (--len > 0);
    return;
   }
#endif
   JCGO_MEM_HMOVE(&JCGO_ARR_INTERNALACC(jObject, (jObjectArr)dest, destStart),
    &JCGO_ARR_INTERNALACC(jObject, (jObjectArr)src, srcStart),
    (JCGO_ALLOCSIZE_T)len * sizeof(jObject));
  }
   else
   {
    destDims = (int)jcgo_primitiveOffset[typenum];
    typenum = (int)jcgo_primitiveSize[typenum];
    JCGO_MEM_HMOVE((void *)((volatile char JCGO_HPTR_MOD *)dest +
     ((unsigned)typenum * (JCGO_ALLOCSIZE_T)destStart + (unsigned)destDims)),
     (void *)((volatile char JCGO_HPTR_MOD *)src +
     ((unsigned)typenum * (JCGO_ALLOCSIZE_T)srcStart + (unsigned)destDims)),
     (unsigned)typenum * (JCGO_ALLOCSIZE_T)len);
   }
 }
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMSystem__identityHashCode__Lo( java_lang_Object obj )
{
 return (jint)(((u_jint)JCGO_CAST_PTRTONUM(obj)) >> 1);
}

#endif
