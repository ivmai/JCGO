/*
 * @(#) $(JCGO)/minihdr/dos/stdlib.h --
 * a part of the minimalist "libc" headers for JCGO (PC-specific).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
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

#ifndef _STDLIB_H
#define _STDLIB_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _MAX_PATH
#define _MAX_PATH 260
#endif

typedef void (_USERENTRY *__atexit_t)(void);

_EXPFUNC_BUILTIN int _RTLENTRY_BUILTIN atexit(__atexit_t);

_EXPFUNC_BUILTIN _ATTRIBUTE_NORETURN void _RTLENTRY_BUILTIN exit(int);
_EXPFUNC_BUILTIN _ATTRIBUTE_NORETURN void _RTLENTRY_BUILTIN abort(void);

_EXPFUNC_BUILTIN void *_RTLENTRY_BUILTIN calloc(size_t, size_t);
_EXPFUNC_BUILTIN void *_RTLENTRY_BUILTIN malloc(size_t);
_EXPFUNC void _RTLENTRY free(void *);

_EXPFUNC long _RTLENTRY atol(const char *) _ATTRIBUTE_PURE
 _ATTRIBUTE_NONNULL(1);

#ifndef _STDLIB_NO_GETENV
_EXPFUNC char *_RTLENTRY getenv(const char *) _ATTRIBUTE_NONNULL(1);
#ifndef _STDLIB_NO_WGETENV
_EXPFUNC wchar_t *_RTLENTRY _wgetenv(const wchar_t *) _ATTRIBUTE_NONNULL(1);
#endif
#endif

_EXPFUNC int _RTLENTRY mbtowc(wchar_t *, const char *, size_t);
_EXPFUNC int _RTLENTRY wctomb(char *, wchar_t);

_EXPFUNC size_t _RTLENTRY mbstowcs(wchar_t *, const char *, size_t)
 _ATTRIBUTE_NONNULL(2);
_EXPFUNC size_t _RTLENTRY wcstombs(char *, const wchar_t *, size_t)
 _ATTRIBUTE_NONNULL(2);

#ifndef MB_CUR_MAX
_EXPFUNC int _RTLENTRY ___mb_cur_max_func(void) _ATTRIBUTE_PURE;
#define MB_CUR_MAX ___mb_cur_max_func()
#endif

#ifndef _osver
_EXPFUNC const unsigned *_RTLENTRY __p__osver(void) _ATTRIBUTE_PURE;
#define _osver (*__p__osver())
#endif

#ifndef _osmajor
#ifdef CHAR_BIT
#define _osmajor ((unsigned char)(_osver >> CHAR_BIT))
#else
#define _osmajor ((unsigned char)(_osver >> 8))
#endif
#define _osminor ((unsigned char)_osver)
#endif

/* Windows-specific */
#ifndef _winver
_EXPFUNC const unsigned *_RTLENTRY __p__winver(void) _ATTRIBUTE_PURE;
#define _winver (*__p__winver())
#endif

/* OS/2-specific */
_EXPFUNC void **_RTLENTRY _threadstore(void);

#ifdef __cplusplus
}
#endif

#endif
