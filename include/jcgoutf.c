/*
 * @(#) $(JCGO)/include/jcgoutf.c --
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

STATIC jint CFASTCALL jcgo_utfLenOfChars( jcharArr chars, jint ofs,
 jint count )
{
 jint utfLen = count;
 while (count-- > 0)
 {
  if (JCGO_EXPECT_FALSE(JCGO_ARR_INTERNALACC(jchar, chars, ofs) - (jchar)1 >=
      (jchar)0x7f))
  {
   utfLen++;
   if (JCGO_EXPECT_FALSE(JCGO_ARR_INTERNALACC(jchar, chars, ofs) >
       (jchar)0x7ff))
    utfLen++;
  }
  ofs++;
 }
 return utfLen;
}

STATIC jint CFASTCALL jcgo_utfLenOfBytes( jbyteArr bytes, jint ofs,
 jint count )
{
 jint utfLen = count;
 while (count-- > 0)
 {
  if (JCGO_EXPECT_FALSE((unsigned char)(JCGO_ARR_INTERNALACC(jbyte,
      bytes, ofs) - 1) >= (unsigned char)0x7f))
   utfLen++;
  ofs++;
 }
 return utfLen;
}

STATIC unsigned CFASTCALL jcgo_utfFillFromChars( char *utfbuf, jcharArr chars,
 jint ofs, jint count )
{
 unsigned pos = 0;
 jchar ch;
 while (count-- > 0)
 {
  ch = JCGO_ARR_INTERNALACC(jchar, chars, ofs);
  if (JCGO_EXPECT_TRUE(ch - (jchar)1 < (jchar)0x7f))
   *(utfbuf + pos) = (char)ch;
   else
   {
    if (JCGO_EXPECT_TRUE(ch <= (jchar)0x7ff))
     *(utfbuf + pos) = (char)((ch >> 6) | 0xc0);
     else
     {
      *(utfbuf + pos) = (char)((ch >> 12) | 0xe0);
      *(utfbuf + pos + 1) = (char)(((ch >> 6) & 0x3f) | 0x80);
      pos++;
     }
    *(utfbuf + (++pos)) = (char)((ch & 0x3f) | 0x80);
   }
  ofs++;
  pos++;
 }
 return pos;
}

STATIC unsigned CFASTCALL jcgo_utfFillFromBytes( char *utfbuf, jbyteArr bytes,
 jint ofs, jint count )
{
 unsigned pos = 0;
 jbyte ch;
 while (count-- > 0)
 {
  ch = JCGO_ARR_INTERNALACC(jbyte, bytes, ofs);
  if (JCGO_EXPECT_TRUE((unsigned char)(ch - 1) < (unsigned char)0x7f))
   *(utfbuf + pos) = (char)ch;
   else
   {
    *(utfbuf + pos) = (char)((((unsigned char)ch) >> 6) | 0xc0);
    *(utfbuf + pos + 1) = (char)((ch & 0x3f) | 0x80);
    pos++;
   }
  ofs++;
  pos++;
 }
 return pos;
}

JCGO_NOSEP_INLINE unsigned CFASTCALL jcgo_utfCountChars( CONST char *utfstrz )
{
 unsigned pos;
 unsigned count = 0;
 char ch;
 for (pos = 0; (ch = *(utfstrz + pos)) != (char)0; pos++)
  if (JCGO_EXPECT_TRUE((ch & 0xc0) != 0x80))
   count++;
 return count;
}

JCGO_NOSEP_INLINE unsigned CFASTCALL jcgo_utfToChars( CONST char *utfstr,
 jcharArr chars, jint ofs, jint count )
{
 unsigned pos = 0;
 jchar jch;
 unsigned char ch;
 unsigned char ch2;
 unsigned char ch3;
 while (count-- > 0)
 {
  ch = (unsigned char)(*(utfstr + (pos++)));
  if (JCGO_EXPECT_TRUE(ch < (unsigned char)0x80))
   jch = (jchar)ch;
   else
   {
    ch2 = (unsigned char)(*(utfstr + pos));
    jch = (jchar)0x3f; /*'?'*/
    if (JCGO_EXPECT_TRUE((ch2 & 0xc0) == 0x80))
    {
     pos++;
     if (JCGO_EXPECT_TRUE(ch < (unsigned char)0xe0))
      jch = (jchar)(((ch & 0x1f) << 6) | (ch2 & 0x3f));
      else
      {
       ch3 = (unsigned char)(*(utfstr + pos));
       if (JCGO_EXPECT_TRUE((ch3 & 0xc0) == 0x80))
       {
        pos++;
        if (JCGO_EXPECT_TRUE(ch < (unsigned char)0xf0))
         jch = (jchar)(((((ch & 0xf) << 6) | (ch2 & 0x3f)) << 6) |
                (ch3 & 0x3f));
       }
      }
    }
   }
  JCGO_ARR_INTERNALACC(jchar, chars, ofs) = jch;
  ofs++;
 }
 return pos;
}

STATIC java_lang_String CFASTCALL jcgo_utfMakeString( CONST char *utfstrz )
{
 jint count = (jint)jcgo_utfCountChars(utfstrz);
 jcharArr arr = (jcharArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jchar), 0,
                 count);
 java_lang_String str =
  (java_lang_String)jcgo_newObject((jvtable)&java_lang_String_methods);
 JCGO_FIELD_NZACCESS(str, value) = (void *)arr;
 JCGO_FIELD_NZACCESS(str, count) = count;
 jcgo_utfToChars(utfstrz, arr, 0, count);
 return str;
}

#endif
