/*
 * @(#) $(JCGO)/minihdr/unix/unistd.h --
 * a part of the minimalist "libc" headers for JCGO (Unix-specific).
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

#ifndef _UNISTD_H
#define _UNISTD_H

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef SEEK_SET
#define SEEK_SET 0
#define SEEK_CUR 1
#define SEEK_END 2
#endif

#ifndef F_OK
#define F_OK 0 /* or 0x1 */
#endif

#ifndef R_OK
#define R_OK 0x4 /* or 0x2 */
#define W_OK 0x2 /* or 0x4 */
#define X_OK 0x1 /* or 0x8 */
#endif

#ifndef _SC_NPROCESSORS_ONLN
#define _SC_NPROCESSORS_ONLN 58 /* or 6, or 10, or 15, or 85 */
#endif

#ifndef _EXPFUNC_EXEC
#define _EXPFUNC_EXEC _EXPFUNC_BUILTIN
#endif

#ifndef _RTLENTRY_EXEC
#define _RTLENTRY_EXEC _RTLENTRY_BUILTIN
#endif

#ifndef _EXECXX_ARGS_CONST
#define _EXECXX_ARGS_CONST /* empty */
#endif

_EXPFUNC long _RTLENTRY sysconf(int);

_EXPFUNC int _RTLENTRY close(int);
_EXPFUNC ssize_t _RTLENTRY read(int, void *, size_t) _ATTRIBUTE_NONNULL(2);
_EXPFUNC ssize_t _RTLENTRY write(int, const void *, size_t)
 _ATTRIBUTE_NONNULL(2);
_EXPFUNC int _RTLENTRY dup(int);
_EXPFUNC int _RTLENTRY dup2(int, int);
_EXPFUNC off_t _RTLENTRY lseek(int, off_t, int);

#ifndef _UNISTD_NO_FTRUNCATE
_EXPFUNC int _RTLENTRY ftruncate(int, off_t);
#endif

#ifndef _UNISTD_NO_FSYNC
_EXPFUNC int _RTLENTRY fsync(int);
#endif

_EXPFUNC int _RTLENTRY pipe(int *) _ATTRIBUTE_NONNULL(1);

_EXPFUNC off64_t _RTLENTRY lseek64(int, off64_t, int);
_EXPFUNC int _RTLENTRY ftruncate64(int, off64_t);

_EXPFUNC gid_t _RTLENTRY getegid(void);
_EXPFUNC uid_t _RTLENTRY geteuid(void);
_EXPFUNC uid_t _RTLENTRY getuid(void);

#ifndef _UNISTD_NETDB_NO_GETHOSTNAME
_EXPFUNC int _RTLENTRY gethostname(char *, size_t) _ATTRIBUTE_NONNULL(1);
#endif

_EXPFUNC pid_t _RTLENTRY getpid(void);

_EXPFUNC_EXEC pid_t _RTLENTRY_EXEC fork(void);

_EXPFUNC int _RTLENTRY access(const char *, int) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY chdir(const char *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY rmdir(const char *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC char *_RTLENTRY getcwd(char *, size_t) _ATTRIBUTE_NONNULL(1);

_EXPFUNC int _RTLENTRY unlink(const char *) _ATTRIBUTE_NONNULL(1);

_EXPFUNC_EXEC int _RTLENTRY_EXEC execvp(const char *,
 _EXECXX_ARGS_CONST char *const *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC_EXEC int _RTLENTRY_EXEC execve(const char *,
 _EXECXX_ARGS_CONST char *const *, _EXECXX_ARGS_CONST char *const *)
 _ATTRIBUTE_NONNULL(1);

_EXPFUNC int _RTLENTRY _waccess(const wchar_t *, int) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY _wchdir(const wchar_t *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY _wrmdir(const wchar_t *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC wchar_t *_RTLENTRY _wgetcwd(wchar_t *, size_t);

_EXPFUNC int _RTLENTRY _wunlink(const wchar_t *) _ATTRIBUTE_NONNULL(1);

_EXPFUNC_EXEC int _RTLENTRY_EXEC _wexecvp(const wchar_t *,
 _EXECXX_ARGS_CONST wchar_t *const *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC_EXEC int _RTLENTRY_EXEC _wexecve(const wchar_t *,
 _EXECXX_ARGS_CONST wchar_t *const *, _EXECXX_ARGS_CONST wchar_t *const *)
 _ATTRIBUTE_NONNULL(1);

#ifndef _USECONDS_T_DEFINED
#define _USECONDS_T_DEFINED
typedef unsigned useconds_t;
#endif

_EXPFUNC int _RTLENTRY usleep(useconds_t);

#ifdef __cplusplus
}
#endif

#endif
