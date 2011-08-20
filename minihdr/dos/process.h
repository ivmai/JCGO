/*
 * @(#) $(JCGO)/minihdr/dos/process.h --
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

#ifndef _PROCESS_H
#define _PROCESS_H

#ifndef _STDINT_H
#include <stdint.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef P_NOWAIT
#define P_NOWAIT 1 /* or 2 */
#endif

#ifndef _WAIT_CHILD
#define _WAIT_CHILD 0
#endif

#ifndef _EXPFUNC_EXEC
#define _EXPFUNC_EXEC _EXPFUNC_BUILTIN
#endif

#ifndef _RTLENTRY_EXEC
#define _RTLENTRY_EXEC _RTLENTRY_BUILTIN
#endif

#ifndef _EXECXX_ARGS_CONST
#define _EXECXX_ARGS_CONST const /* or empty */
#endif

#ifndef _EXECXX_RETURN_TYPE
#define _EXECXX_RETURN_TYPE intptr_t /* or int */
#endif

_EXPFUNC_EXEC _EXECXX_RETURN_TYPE _RTLENTRY_EXEC execvp(const char *,
 _EXECXX_ARGS_CONST char *const *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC_EXEC _EXECXX_RETURN_TYPE _RTLENTRY_EXEC execve(const char *,
 _EXECXX_ARGS_CONST char *const *, _EXECXX_ARGS_CONST char *const *)
 _ATTRIBUTE_NONNULL(1);

_EXPFUNC _EXECXX_RETURN_TYPE _RTLENTRY spawnvp(int, const char *,
 _EXECXX_ARGS_CONST char *const *) _ATTRIBUTE_NONNULL(2);
_EXPFUNC _EXECXX_RETURN_TYPE _RTLENTRY spawnvpe(int, const char *,
 _EXECXX_ARGS_CONST char *const *, _EXECXX_ARGS_CONST char *const *)
 _ATTRIBUTE_NONNULL(2);

_EXPFUNC_EXEC _EXECXX_RETURN_TYPE _RTLENTRY_EXEC _wexecvp(const wchar_t *,
 _EXECXX_ARGS_CONST wchar_t *const *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC_EXEC _EXECXX_RETURN_TYPE _RTLENTRY_EXEC _wexecve(const wchar_t *,
 _EXECXX_ARGS_CONST wchar_t *const *, _EXECXX_ARGS_CONST wchar_t *const *)
 _ATTRIBUTE_NONNULL(1);

_EXPFUNC _EXECXX_RETURN_TYPE _RTLENTRY _wspawnvp(int, const wchar_t *,
 _EXECXX_ARGS_CONST wchar_t *const *) _ATTRIBUTE_NONNULL(2);
_EXPFUNC _EXECXX_RETURN_TYPE _RTLENTRY _wspawnvpe(int, const wchar_t *,
 _EXECXX_ARGS_CONST wchar_t *const *, _EXECXX_ARGS_CONST wchar_t *const *)
 _ATTRIBUTE_NONNULL(2);

_EXPFUNC _EXECXX_RETURN_TYPE _RTLENTRY _cwait(int *, _EXECXX_RETURN_TYPE,
 int);

/* Unix-specific */
_EXPFUNC_EXEC int _RTLENTRY_EXEC fork(void);

#ifdef _BEGINTHREAD_NO_STACKPTR
#ifndef _BEGINTHREAD_RETURN_TYPE
#define _BEGINTHREAD_RETURN_TYPE unsigned long /* or uintptr_t */
#endif
_EXPFUNC_EXEC _BEGINTHREAD_RETURN_TYPE _RTLENTRY_EXEC _beginthread(
 void (_USERENTRY *)(void *), unsigned, void *) _ATTRIBUTE_NONNULL(1);
#else
#ifndef _BEGINTHREAD_RETURN_TYPE
#define _BEGINTHREAD_RETURN_TYPE int
#endif
_EXPFUNC_EXEC _BEGINTHREAD_RETURN_TYPE _RTLENTRY_EXEC _beginthread(
 void (_USERENTRY *)(void *), void *, unsigned, void *) _ATTRIBUTE_NONNULL(1);
#endif

/* Windows-specific */
#ifndef WINAPI
#define WINAPI __stdcall
#endif

/* Windows-specific */
_EXPFUNC_EXEC uintptr_t _RTLENTRY_EXEC _beginthreadex(void *, unsigned,
 unsigned (WINAPI *)(void *), void *, unsigned, unsigned *)
 _ATTRIBUTE_NONNULL(3);

#ifdef __cplusplus
}
#endif

#endif
