/*
 * @(#) $(JCGO)/minihdr/common/stdio.h --
 * a part of the minimalist "libc" headers for JCGO.
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

#ifndef _STDIO_H
#define _STDIO_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef P_tmpdir
#define P_tmpdir "" /* or "/tmp", or "/" */
#endif

#ifndef FILENAME_MAX
#define FILENAME_MAX 1024 /* or 127, or 259 */
#endif

#ifndef SEEK_SET
#define SEEK_SET 0
#define SEEK_CUR 1
#define SEEK_END 2
#endif

_EXPFUNC int _RTLENTRY remove(const char *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY rename(const char *, const char *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

_EXPFUNC void _RTLENTRY perror(const char *);

_EXPFUNC int _RTLENTRY _wremove(const wchar_t *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY _wrename(const wchar_t *, const wchar_t *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

_EXPFUNC void _RTLENTRY _wperror(const wchar_t *);

#ifndef _FILE_DEFINED
#define _FILE_DEFINED
#ifndef _FILE_OPAQUE_SIZE
#define _FILE_OPAQUE_SIZE 64
#endif
typedef struct
{
 char opaque[_FILE_OPAQUE_SIZE]; /* unused */
} FILE;
#endif

#ifndef stdin
_EXPFUNC FILE *const *_RTLENTRY __p__stdin(void) _ATTRIBUTE_PURE;
#define stdin (*__p__stdin())
#endif

#ifndef stdout
_EXPFUNC FILE *const *_RTLENTRY __p__stdout(void) _ATTRIBUTE_PURE;
#define stdout (*__p__stdout())
#endif

#ifndef stderr
_EXPFUNC FILE *const *_RTLENTRY __p__stderr(void) _ATTRIBUTE_PURE;
#define stderr (*__p__stderr())
#endif

#ifndef _fileno
#define _fileno fileno
#endif
_EXPFUNC int _RTLENTRY _fileno(FILE *) _ATTRIBUTE_NONNULL(1);

_EXPFUNC_BUILTIN int _RTLENTRY_BUILTIN fprintf(FILE *, const char *, ...)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

#ifdef __cplusplus
}
#endif

#endif
