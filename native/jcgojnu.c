/*
 * @(#) $(JCGO)/native/jcgojnu.c --
 * a part of the JCGO native layer library (native layer API implementation).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2013 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_DOWCSTOMBS, JCGO_HUGEARR, JCGO_NOJNI,
 * JCGO_SYSWCHAR, JCGO_THREADS, JCGO_UTFWCTOMB, JCGO_WMAIN.
 * Macros for tuning: JNUBIGEXPORT.
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

#define JCGO_BUILDING_JNU
#ifdef JCGO_VER
#include "jcgojnu.h"
#else
#include "jcgojnu.h"
#ifdef JCGO_NOJNI
#ifndef JCGO_SEPARATED
#define JCGO_SEPARATED
#endif
#include "jcgortl.h"
jint *JNICALL jcgo_jnuStringLengthPtr( jstring str );
jchar *JNICALL jcgo_jnuStringChars( jstring str, int *pisbytes );
jstring JNICALL jcgo_jnuStringCreate( JNIEnv *pJniEnv, jint len );
#endif
#endif

#ifdef JCGO_VER

#ifndef JCGO_UTFWCTOMB

/* #include <stdlib.h> */
/* const int MB_CUR_MAX; */

#ifdef JCGO_DOWCSTOMBS
/* #include <stdlib.h> */
/* size_t mbstowcs(wchar_t *, const char *, size_t); */
/* size_t wcstombs(char *, const wchar_t *, size_t); */
#else
/* #include <stdlib.h> */
/* int mbtowc(wchar_t *, const char *, size_t); */
/* int wctomb(char *, wchar_t); */
#endif

#ifndef _LIMITS_H
#include <limits.h>
#endif

#ifndef MB_LEN_MAX
#define MB_LEN_MAX 6
#endif

#endif /* !JCGO_UTFWCTOMB */

#ifndef JNUBIGEXPORT
#define JNUBIGEXPORT JNIEXPORT
#endif

#ifndef JCGO_JNUMSG_BADARG
#define JCGO_JNUMSG_BADARG "" /* in 'UTF-8' encoding */
#endif

#ifndef JCGO_NOJNI
#ifndef JCGO_ALLOCSIZE_T
#ifdef JCGO_HUGEARR
#define JCGO_ALLOCSIZE_T unsigned long
#else
#define JCGO_ALLOCSIZE_T unsigned
#endif
#endif
#endif

#ifndef JCGO_HPTR_MOD
#ifdef JCGO_HUGEARR
#define JCGO_HPTR_MOD __huge
#else
#define JCGO_HPTR_MOD /* empty */
#endif
#endif

#ifndef JCGO_JNUNEWSTRING_WBUFSIZE
#define JCGO_JNUNEWSTRING_WBUFSIZE 128
#endif

#ifdef JCGO_NOJNI
JNIEXPORT
#else
JNUBIGEXPORT
#endif
jbyte *JNICALL
jcgo_JnuGetByteArrayElemsRegion( JNIEnv *pJniEnv, jbyteArray arr, jint offset,
 jint len )
{
#ifdef JCGO_NOJNI
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(len);
 return (jbyte *)&JCGO_ARR_INTERNALACC(jbyte, (jbyteArr)arr, offset);
#else
 jbyte *bytes = (*pJniEnv)->GetByteArrayElements(pJniEnv, arr, NULL);
 if (bytes == NULL)
  return NULL;
 if ((offset | len) < 0 ||
     (*pJniEnv)->GetArrayLength(pJniEnv, (jarray)arr) - offset < len)
 {
  (*pJniEnv)->FatalError(pJniEnv, JCGO_JNUMSG_BADARG);
  return NULL;
 }
 return (jbyte *)((jbyte JCGO_HPTR_MOD *)bytes + (JCGO_ALLOCSIZE_T)offset);
#endif
}

JNIEXPORT void JNICALL
jcgo_JnuReleaseByteArrayElemsRegion( JNIEnv *pJniEnv, jbyteArray arr,
 jbyte *bytes, jint offset )
{
#ifdef JCGO_NOJNI
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(arr);
 JCGO_UNUSED_VAR(bytes);
 JCGO_UNUSED_VAR(offset);
#else
 (*pJniEnv)->ReleaseByteArrayElements(pJniEnv, arr,
  (jbyte *)((jbyte JCGO_HPTR_MOD *)bytes - (JCGO_ALLOCSIZE_T)offset), 0);
#endif
}

JNIEXPORT jint JNICALL
jcgo_JnuGetIntArrayElement( JNIEnv *pJniEnv, jintArray arr, jint index )
{
#ifdef JCGO_NOJNI
 JCGO_UNUSED_VAR(pJniEnv);
 return JCGO_ARR_INTERNALACC(jint, (jintArr)arr, index);
#else
 jint value = 0;
 (*pJniEnv)->GetIntArrayRegion(pJniEnv, arr, (jsize)index, 1, &value);
 return value;
#endif
}

JNIEXPORT void JNICALL
jcgo_JnuSetIntArrayElement( JNIEnv *pJniEnv, jintArray arr, jint index,
 jint value )
{
#ifdef JCGO_NOJNI
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_ARR_INTERNALACC(jint, (jintArr)arr, index) = value;
#else
 (*pJniEnv)->SetIntArrayRegion(pJniEnv, arr, (jsize)index, 1, &value);
#endif
}

JNIEXPORT void JNICALL
jcgo_JnuSetLongArrayElement( JNIEnv *pJniEnv, jlongArray arr, jint index,
 jlong value )
{
#ifdef JCGO_NOJNI
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_ARR_INTERNALACC(jlong, (jlongArr)arr, index) = value;
#else
 (*pJniEnv)->SetLongArrayRegion(pJniEnv, arr, (jsize)index, 1, &value);
#endif
}

JNUBIGEXPORT unsigned JNICALL
jcgo_JnuStringSizeOfPlatform( JNIEnv *pJniEnv, jstring str )
{
 unsigned len = 0;
#ifdef JCGO_NOJNI
 jint count = *jcgo_jnuStringLengthPtr(str);
 JCGO_UNUSED_VAR(pJniEnv);
#else
 jint count = (jint)(*pJniEnv)->GetStringLength(pJniEnv, str);
#endif
 if (count > 0)
 {
#ifdef JCGO_UTFWCTOMB
  len = count <= (jint)(((((unsigned)-1) >> 1) - 16) / 3) ?
         (unsigned)count * 3 : (((unsigned)-1) >> 1) - (unsigned)16;
#else
  len = (unsigned)count;
  if (count > (jint)((((unsigned)-1) >> 1) - 16))
   len = (((unsigned)-1) >> 1) - (unsigned)16;
#ifdef MB_CUR_MAX
  count = (jint)MB_CUR_MAX;
  if (count != 1)
#endif
  {
#ifdef MB_CUR_MAX
   if ((unsigned)count < (unsigned)MB_LEN_MAX &&
       *(volatile jint *)&count != 0)
    len = ((((unsigned)-1) >> 1) - (unsigned)16) / (unsigned)count >= len ?
           (unsigned)count * len : (((unsigned)-1) >> 1) - (unsigned)16;
    else
#endif
    {
     len = len <= ((((unsigned)-1) >> 1) - (unsigned)16) /
            (unsigned)MB_LEN_MAX ? len * (unsigned)MB_LEN_MAX :
            (((unsigned)-1) >> 1) - (unsigned)16;
    }
  }
#endif
 }
 return len + 1;
}

JNUBIGEXPORT int JNICALL
jcgo_JnuStringToPlatformChars( JNIEnv *pJniEnv, jstring str, char *cbuf,
 unsigned size )
{
 int cnt;
 int i;
 unsigned pos;
 unsigned len;
 int noerr = -1;
 jint count;
 CONST jchar JCGO_HPTR_MOD *chars;
#ifdef JCGO_NOJNI
 int isbytes;
#endif
#ifdef JCGO_UTFWCTOMB
 jchar wch;
#else
 int res;
#ifdef JCGO_DOWCSTOMBS
 wchar_t wbuf[2];
#else
 int j;
 char mbbuf[MB_LEN_MAX];
#endif
#endif
 if (cbuf != NULL && size)
 {
#ifdef JCGO_NOJNI
  JCGO_UNUSED_VAR(pJniEnv);
  chars = jcgo_jnuStringChars(str, &isbytes);
  count = *jcgo_jnuStringLengthPtr(str);
#else
  chars = (*pJniEnv)->GetStringChars(pJniEnv, str, NULL);
  if (chars == NULL)
   return noerr;
  count = (jint)(*pJniEnv)->GetStringLength(pJniEnv, str);
#endif
  len = (((unsigned)-1) >> 1) - (unsigned)16;
  pos = 0;
  noerr = 1;
  if (size <= len)
   len = size - 1;
  cnt = (int)len;
  if ((jint)cnt > count)
   cnt = (int)count;
#ifndef JCGO_UTFWCTOMB
#ifdef JCGO_DOWCSTOMBS
  wbuf[1] = (wchar_t)0;
#else
  *(volatile char *)&mbbuf[0] = (char)wctomb(NULL, (wchar_t)0);
#endif
#endif
  for (i = 0; i < cnt; i++)
  {
#ifdef JCGO_UTFWCTOMB
#ifdef JCGO_NOJNI
   wch = isbytes ? (unsigned char)(*((volatile jbyte JCGO_HPTR_MOD *)chars +
          (unsigned)i)) : *(chars + (unsigned)i);
#else
   wch = *(chars + (unsigned)i);
#endif
   if (wch < 0x80 && wch)
   {
    if (pos >= len)
     break;
    cbuf[pos] = (char)wch;
   }
    else
    {
     if (wch < 0x800)
     {
      if (pos + 1 >= len)
       break;
      cbuf[pos] = (char)((wch >> 6) | 0xc0);
     }
      else
      {
       if (pos + 2 >= len)
        break;
       cbuf[pos] = (char)((wch >> 12) | 0xe0);
       cbuf[pos + 1] = (char)(((wch >> 6) & 0x3f) | 0x80);
       pos++;
      }
     cbuf[++pos] = (char)((wch & 0x3f) | 0x80);
    }
   pos++;
#else
#ifdef JCGO_DOWCSTOMBS
#ifdef JCGO_NOJNI
   wbuf[0] = (wchar_t)(isbytes ?
              (unsigned char)(*((volatile jbyte JCGO_HPTR_MOD *)chars +
              (unsigned)i)) : *(chars + (unsigned)i));
#else
   wbuf[0] = (wchar_t)(*(chars + (unsigned)i));
#endif
   res = (int)wcstombs(&cbuf[pos], wbuf, len - pos);
#else
#ifdef JCGO_NOJNI
   res = wctomb(len - pos < sizeof(mbbuf) ? mbbuf : &cbuf[pos],
          (wchar_t)(isbytes ?
          (unsigned char)(*((volatile jbyte JCGO_HPTR_MOD *)chars +
          (unsigned)i)) : *(chars + (unsigned)i)));
#else
   res = wctomb(len - pos < sizeof(mbbuf) ? mbbuf : &cbuf[pos],
          (wchar_t)(*(chars + (unsigned)i)));
#endif
#endif
   if (res > 0)
   {
#ifndef JCGO_DOWCSTOMBS
    if (len - pos < sizeof(mbbuf))
    {
     if (pos + (unsigned)res > len)
      break;
     for (j = 0; j < res; j++)
      cbuf[pos + j] = mbbuf[j];
    }
#endif
    pos = (unsigned)res + pos;
   }
    else
    {
     if (pos >= len)
      break;
     if (res < 0)
     {
      noerr = 0;
      cbuf[pos++] = '?';
     }
      else cbuf[pos++] = '\0';
    }
#endif
  }
#ifndef JCGO_NOJNI
  (*pJniEnv)->ReleaseStringChars(pJniEnv, str, (CONST jchar *)chars);
#endif
  if ((jint)i < count)
  {
   noerr = 0;
   if (pos < len)
    cbuf[pos++] = '?';
    else
    {
     if (len)
      cbuf[len - 1] = '?';
    }
  }
  cbuf[pos] = '\0';
 }
#ifndef JCGO_NOJNI
  else (*pJniEnv)->FatalError(pJniEnv, JCGO_JNUMSG_BADARG);
#endif
 return noerr;
}

JNUBIGEXPORT jstring JNICALL
jcgo_JnuNewStringPlatform( JNIEnv *pJniEnv, CONST char *cstr )
{
#ifdef JCGO_UTFWCTOMB
 jchar wch;
 char ch;
 unsigned pos;
#else
#ifndef JCGO_DOWCSTOMBS
 wchar_t wch;
 int res;
 unsigned pos;
#endif
#endif
 unsigned len = 0;
 int i = (int)(((unsigned)-1) >> 1) - 16;
 jstring str = NULL;
 jchar *chars;
#ifndef JCGO_NOJNI
 jcharArray arr;
 jchar buf[JCGO_JNUNEWSTRING_WBUFSIZE];
#endif
 if (cstr != NULL)
 {
  while (*(cstr + len))
   len++;
  if (len >= (unsigned)i)
   len = (unsigned)i;
#ifdef JCGO_NOJNI
  str = jcgo_jnuStringCreate(pJniEnv, (jint)len);
  chars = NULL;
  if (str != NULL)
   chars = jcgo_jnuStringChars(str, &i);
#else
  arr = NULL;
  chars = buf;
  if (len > sizeof(buf) / sizeof(jchar))
  {
   arr = (*pJniEnv)->NewCharArray(pJniEnv, (jsize)len);
   chars = NULL;
   if (arr != NULL)
    chars = (*pJniEnv)->GetCharArrayElements(pJniEnv, arr, NULL);
  }
#endif
  if (chars != NULL)
  {
   i = 0;
#ifdef JCGO_UTFWCTOMB
   for (pos = 0; pos < len; pos++)
   {
    wch = (unsigned char)cstr[pos];
    if (wch >= 0x80 && (wch > 0xef ||
        ((ch = cstr[++pos]) & 0xc0) != 0x80 || (wch < 0xe0 ?
        (wch = (jchar)(((wch & 0x1f) << 6) | (ch & 0x3f))) < 0x80 && wch :
        (wch = (jchar)((wch << 12) | ((jchar)(ch & 0x3f) << 6) |
        (cstr[++pos] & 0x3f))) < 0x800 || (cstr[pos] & 0xc0) != 0x80)))
    {
     *chars = (jchar)0x3f; /*'?'*/
     if (i <= 0)
      i = 1;
     break;
    }
    *(chars + (i++)) = wch;
   }
#else
#ifdef JCGO_DOWCSTOMBS
   if (len && (i = (int)mbstowcs(chars, cstr, len)) < 0)
   {
    *chars = (jchar)0x3f; /*'?'*/
    for (i = 1; i < (int)len; i++)
     if (!(*(chars + i)))
      break;
   }
#else
   *(volatile wchar_t *)&wch = (wchar_t)mbtowc(&wch, NULL, 0);
   for (pos = 0; pos < len; pos = (unsigned)res + pos)
   {
    if ((res = mbtowc(&wch, cstr + pos, len - pos + 1)) <= 0)
    {
     if (!res)
      break;
     if ((res = mbtowc(&wch, cstr + pos, 1)) <= 0)
     {
      if (!res)
       break;
      *chars = (jchar)0x3f; /*'?'*/
      if (i <= 0)
       i = 1;
      break;
     }
    }
    *(chars + (i++)) = (jchar)wch;
   }
#endif
#endif
#ifdef JCGO_NOJNI
   *jcgo_jnuStringLengthPtr(str) = i;
#else
   str = (*pJniEnv)->NewString(pJniEnv, chars, (jsize)i);
   if (arr != NULL)
    (*pJniEnv)->ReleaseCharArrayElements(pJniEnv, arr, chars, 0);
#endif
  }
#ifndef JCGO_NOJNI
  if (arr != NULL)
   (*pJniEnv)->DeleteLocalRef(pJniEnv, (jobject)arr);
#endif
 }
#ifndef JCGO_NOJNI
  else (*pJniEnv)->FatalError(pJniEnv, JCGO_JNUMSG_BADARG);
#endif
 return str;
}

#ifdef JCGO_SYSWCHAR

JNIEXPORT unsigned JNICALL
jcgo_JnuStringSizeOfWide( JNIEnv *pJniEnv, jstring str )
{
#ifdef JCGO_NOJNI
 jint count = *jcgo_jnuStringLengthPtr(str);
 JCGO_UNUSED_VAR(pJniEnv);
#else
 jint count = (jint)(*pJniEnv)->GetStringLength(pJniEnv, str);
#endif
 return count > 0 ? (count < (jint)(((((unsigned)-1) >> 1) - 16) /
         sizeof(wchar_t)) ? ((unsigned)count + 1) * sizeof(wchar_t) :
         (((unsigned)-1) >> 1) + sizeof(wchar_t) - 16) : sizeof(wchar_t);
}

JNUBIGEXPORT int JNICALL
jcgo_JnuStringToWideChars( JNIEnv *pJniEnv, jstring str, wchar_t *wbuf,
 unsigned size )
{
 int cnt;
 int i;
 int noerr = -1;
 jint count;
 CONST jchar JCGO_HPTR_MOD *chars;
#ifdef JCGO_NOJNI
 int isbytes;
#endif
 if (wbuf != NULL && size >= sizeof(wchar_t))
 {
#ifdef JCGO_NOJNI
  JCGO_UNUSED_VAR(pJniEnv);
  chars = jcgo_jnuStringChars(str, &isbytes);
  count = *jcgo_jnuStringLengthPtr(str);
#else
  chars = (*pJniEnv)->GetStringChars(pJniEnv, str, NULL);
  if (chars == NULL)
   return noerr;
  count = (jint)(*pJniEnv)->GetStringLength(pJniEnv, str);
#endif
  cnt = (int)(((unsigned)-1) >> 1) - 16;
  if (size <= (unsigned)cnt)
   cnt = (int)size;
  cnt = (int)((unsigned)cnt / sizeof(wchar_t)) - 1;
  noerr = 1;
  if ((jint)cnt < count)
  {
   noerr = 0;
   if (cnt)
    wbuf[cnt - 1] = (wchar_t)0x3f; /*'?'*/
  }
   else cnt = (int)count;
#ifdef JCGO_NOJNI
  if (isbytes)
  {
   for (i = 0; i < cnt; i++)
    wbuf[i] = (wchar_t)((unsigned char)(
               *((volatile jbyte JCGO_HPTR_MOD *)chars + i)));
  }
   else
   {
    for (i = 0; i < cnt; i++)
     wbuf[i] = (wchar_t)(*(chars + i));
   }
#else
  for (i = 0; i < cnt; i++)
   wbuf[i] = (wchar_t)(*(chars + i));
  (*pJniEnv)->ReleaseStringChars(pJniEnv, str, (CONST jchar *)chars);
#endif
  wbuf[cnt] = (wchar_t)0;
 }
#ifndef JCGO_NOJNI
  else (*pJniEnv)->FatalError(pJniEnv, JCGO_JNUMSG_BADARG);
#endif
 return noerr;
}

#else /* JCGO_SYSWCHAR */

#ifndef JCGO_WMAIN
#define JCGO_EXCLUDE_NEWSTRINGWIDE
#endif

#endif /* ! JCGO_SYSWCHAR */

#ifndef JCGO_EXCLUDE_NEWSTRINGWIDE

JNUBIGEXPORT jstring JNICALL
jcgo_JnuNewStringWide( JNIEnv *pJniEnv, CONST wchar_t *wstr )
{
 unsigned len = 0;
 int i = (int)(((((unsigned)-1) >> 1) - 16) / sizeof(wchar_t));
 jstring str = NULL;
 jchar *chars;
#ifndef JCGO_NOJNI
 jcharArray arr;
 jchar buf[JCGO_JNUNEWSTRING_WBUFSIZE];
#endif
 if (wstr != NULL)
 {
  while (*(wstr + len))
   len++;
  if (len >= (unsigned)i)
   len = (unsigned)i;
#ifdef JCGO_NOJNI
  str = jcgo_jnuStringCreate(pJniEnv, (jint)len);
  chars = NULL;
  if (str != NULL)
   chars = jcgo_jnuStringChars(str, &i);
#else
  arr = NULL;
  chars = buf;
  if (len > sizeof(buf) / sizeof(jchar))
  {
   arr = (*pJniEnv)->NewCharArray(pJniEnv, (jsize)len);
   chars = NULL;
   if (arr != NULL)
    chars = (*pJniEnv)->GetCharArrayElements(pJniEnv, arr, NULL);
  }
#endif
  if (chars != NULL)
  {
   for (i = 0; i < (int)len; i++)
    *(chars + i) = (jchar)(*(wstr + i));
#ifdef JCGO_NOJNI
   *jcgo_jnuStringLengthPtr(str) = (int)len;
#else
   str = (*pJniEnv)->NewString(pJniEnv, chars, (jsize)len);
   if (arr != NULL)
    (*pJniEnv)->ReleaseCharArrayElements(pJniEnv, arr, chars, 0);
#endif
  }
#ifndef JCGO_NOJNI
  if (arr != NULL)
   (*pJniEnv)->DeleteLocalRef(pJniEnv, (jobject)arr);
#endif
 }
#ifndef JCGO_NOJNI
  else (*pJniEnv)->FatalError(pJniEnv, JCGO_JNUMSG_BADARG);
#endif
 return str;
}

#endif /* ! JCGO_EXCLUDE_NEWSTRINGWIDE */

#endif
