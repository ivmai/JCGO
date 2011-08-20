/*
 * @(#) pthread.h - T-PThread main header.
 * Copyright (C) 2006-2007 Ivan Maidanski <ivmai@mail.ru> All rights reserved.
 **
 * Version: 1.2
 * See also files: sched.h, tpthread.c
 * Required: any ANSI C compiler.
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

#ifndef _PTHREAD_H
#define _PTHREAD_H

#define PTHREAD_TPTHREAD_VER 120 /* T-PThread v1.2 */

/* T-PThread API is a subset of the standard POSIX Threads API */

/*
 * Macros for tuning: PTHREAD_API, PTHREAD_CALL,
 * PTHREAD_DESTRUCTOR_ITERATIONS, PTHREAD_FAR, PTHREAD_KEYS_MAX,
 * PTHREAD_USERCALL.
 */

#ifndef _REENTRANT
#define _REENTRANT
#endif

#ifndef _STDDEF_H
#include <stddef.h>
/* typedef size_t; */
#endif

#ifndef _ERRNO_H
#include <errno.h>
#endif

#ifndef _TIME_H
#include <time.h>
#endif

#ifndef _SCHED_H
#include "sched.h"
#endif

#ifndef EAGAIN
#define EAGAIN 7651
#endif
#ifndef EBUSY
#define EBUSY 7652
#endif
#ifndef EDEADLK
#define EDEADLK 7653
#endif
#ifndef EINTR
#define EINTR 7654
#endif
#ifndef EINVAL
#define EINVAL 7655
#endif
#ifndef EPERM
#define EPERM 7656
#endif
#ifndef ESRCH
#define ESRCH 7657
#endif
#ifndef ETIMEDOUT
#define ETIMEDOUT 7658
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _TIMESPEC_DEFINED
#ifndef __timespec_defined
#ifndef TIMEVAL_TO_TIMESPEC
#define _TIMESPEC_DEFINED
#define __timespec_defined 1
struct timespec
{
 time_t tv_sec;
 long tv_nsec;
};
#endif
#endif
#endif

#ifndef PTHREAD_API
#define PTHREAD_API extern
#endif

#ifndef PTHREAD_CALL
#define PTHREAD_CALL /* empty */
#endif

#ifndef PTHREAD_USERCALL
#define PTHREAD_USERCALL PTHREAD_CALL
#endif

#ifndef PTHREAD_FAR
#define PTHREAD_FAR /* empty */
#endif

#ifndef PTHREAD_KEYS_MAX
#define PTHREAD_KEYS_MAX 32
#endif

#ifndef PTHREAD_DESTRUCTOR_ITERATIONS
#define PTHREAD_DESTRUCTOR_ITERATIONS 4
#endif

#define _POSIX_THREADS
#define _POSIX_THREAD_PRIORITY_SCHEDULING
#define _POSIX_THREAD_ATTR_STACKSIZE

#define PTHREAD_CREATE_JOINABLE 0
#define PTHREAD_CREATE_DETACHED 1

#define PTHREAD_INHERIT_SCHED 0
#define PTHREAD_EXPLICIT_SCHED 1

#define PTHREAD_MUTEX_FAST_NP 0
#define PTHREAD_MUTEX_INITIALIZER {0, 0, 0, PTHREAD_MUTEX_FAST_NP, {0, 0}}

#define PTHREAD_COND_INITIALIZER {0, {0, 0}}

typedef struct { int opaque_pthread; } PTHREAD_FAR *pthread_t;

typedef struct { int opaque_descr; } PTHREAD_FAR *_pthread_descr;

struct _pthread_queue
{
 _pthread_descr head; /* unused */
 _pthread_descr tail;
};

typedef struct {
 int opaque_m_spinlock; /* unused */
 int opaque_m_count; /* unused */
 _pthread_descr opaque_m_owner;
 int opaque_m_kind; /* unused */
 struct _pthread_queue opaque_m_waiting;
} pthread_mutex_t;

typedef struct {
 int opaque_c_spinlock; /* unused */
 struct _pthread_queue opaque_c_waiting;
} pthread_cond_t;

typedef struct {
 int opaque_detachstate;
 int opaque_schedpolicy;
 struct sched_param opaque_schedparam;
 int opaque_inheritsched;
 int opaque_scope; /* unused */
 void PTHREAD_FAR *opaque_stackaddr; /* unused */
 size_t opaque_stacksize;
} pthread_attr_t;

typedef struct { int opaque_mutexkind; /* unused */ } pthread_mutexattr_t;
typedef struct { int opaque_condflags; /* unused */ } pthread_condattr_t;
typedef unsigned pthread_key_t;

PTHREAD_API int PTHREAD_CALL pthread_create(pthread_t PTHREAD_FAR *ppthread,
 const pthread_attr_t PTHREAD_FAR *pattr,
 void PTHREAD_FAR *(PTHREAD_USERCALL *rtn)(void PTHREAD_FAR *),
 void PTHREAD_FAR *arg);
PTHREAD_API pthread_t PTHREAD_CALL pthread_self(void);
PTHREAD_API int PTHREAD_CALL pthread_equal(pthread_t pthread,
 pthread_t pthread2);
PTHREAD_API void PTHREAD_CALL pthread_exit(void PTHREAD_FAR *retval);
PTHREAD_API int PTHREAD_CALL pthread_join(pthread_t pthread,
 void PTHREAD_FAR *PTHREAD_FAR *pretval);
PTHREAD_API int PTHREAD_CALL pthread_detach(pthread_t pthread);

PTHREAD_API int PTHREAD_CALL pthread_attr_init(
 pthread_attr_t PTHREAD_FAR *pattr);
PTHREAD_API int PTHREAD_CALL pthread_attr_destroy(
 pthread_attr_t PTHREAD_FAR *pattr);
PTHREAD_API int PTHREAD_CALL pthread_attr_setdetachstate(
 pthread_attr_t PTHREAD_FAR *pattr, int detachstate);
PTHREAD_API int PTHREAD_CALL pthread_attr_getdetachstate(
 const pthread_attr_t PTHREAD_FAR *pattr, int PTHREAD_FAR *pdetachstate);
PTHREAD_API int PTHREAD_CALL pthread_attr_setschedparam(
 pthread_attr_t PTHREAD_FAR *pattr,
 const struct sched_param PTHREAD_FAR *pparam);
PTHREAD_API int PTHREAD_CALL pthread_attr_getschedparam(
 const pthread_attr_t PTHREAD_FAR *pattr,
 struct sched_param PTHREAD_FAR *pparam);
PTHREAD_API int PTHREAD_CALL pthread_attr_setschedpolicy(
 pthread_attr_t PTHREAD_FAR *pattr, int policy);
PTHREAD_API int PTHREAD_CALL pthread_attr_getschedpolicy(
 const pthread_attr_t PTHREAD_FAR *pattr, int PTHREAD_FAR *ppolicy);
PTHREAD_API int PTHREAD_CALL pthread_attr_setinheritsched(
 pthread_attr_t PTHREAD_FAR *pattr, int inherit);
PTHREAD_API int PTHREAD_CALL pthread_attr_getinheritsched(
 const pthread_attr_t PTHREAD_FAR *pattr, int PTHREAD_FAR *pinherit);
PTHREAD_API int PTHREAD_CALL pthread_attr_setstacksize(
 pthread_attr_t PTHREAD_FAR *pattr, size_t stacksize);
PTHREAD_API int PTHREAD_CALL pthread_attr_getstacksize(
 const pthread_attr_t PTHREAD_FAR *pattr, size_t *pstacksize);

PTHREAD_API int PTHREAD_CALL pthread_setschedparam(pthread_t pthread,
 int policy, const struct sched_param PTHREAD_FAR *pparam);
PTHREAD_API int PTHREAD_CALL pthread_getschedparam(pthread_t pthread,
 int PTHREAD_FAR *ppolicy, struct sched_param PTHREAD_FAR *pparam);

PTHREAD_API int PTHREAD_CALL pthread_mutex_init(
 pthread_mutex_t PTHREAD_FAR *pmutex,
 const pthread_mutexattr_t PTHREAD_FAR *pmutexattr);
PTHREAD_API int PTHREAD_CALL pthread_mutex_destroy(
 pthread_mutex_t PTHREAD_FAR *pmutex);
PTHREAD_API int PTHREAD_CALL pthread_mutex_trylock(
 pthread_mutex_t PTHREAD_FAR *pmutex);
PTHREAD_API int PTHREAD_CALL pthread_mutex_lock(
 pthread_mutex_t PTHREAD_FAR *pmutex);
PTHREAD_API int PTHREAD_CALL pthread_mutex_unlock(
 pthread_mutex_t PTHREAD_FAR *pmutex);

PTHREAD_API int PTHREAD_CALL pthread_mutexattr_init(
 pthread_mutexattr_t PTHREAD_FAR *pmutexattr);
PTHREAD_API int PTHREAD_CALL pthread_mutexattr_destroy(
 pthread_mutexattr_t PTHREAD_FAR *pmutexattr);

PTHREAD_API int PTHREAD_CALL pthread_cond_init(
 pthread_cond_t PTHREAD_FAR *pcond,
 const pthread_condattr_t PTHREAD_FAR *pcondattr);
PTHREAD_API int PTHREAD_CALL pthread_cond_destroy(
 pthread_cond_t PTHREAD_FAR *pcond);
PTHREAD_API int PTHREAD_CALL pthread_cond_signal(
 pthread_cond_t PTHREAD_FAR *pcond);
PTHREAD_API int PTHREAD_CALL pthread_cond_broadcast(
 pthread_cond_t PTHREAD_FAR *pcond);
PTHREAD_API int PTHREAD_CALL pthread_cond_wait(
 pthread_cond_t PTHREAD_FAR *pcond,
 pthread_mutex_t PTHREAD_FAR *pmutex);

PTHREAD_API int PTHREAD_CALL pthread_cond_timedwait(
 pthread_cond_t PTHREAD_FAR *pcond, pthread_mutex_t PTHREAD_FAR *pmutex,
 const struct timespec PTHREAD_FAR *pabstime);

PTHREAD_API int PTHREAD_CALL pthread_condattr_init(
 pthread_condattr_t PTHREAD_FAR *pcondattr);
PTHREAD_API int PTHREAD_CALL pthread_condattr_destroy(
 pthread_condattr_t PTHREAD_FAR *pcondattr);

PTHREAD_API int PTHREAD_CALL pthread_key_create(
 pthread_key_t PTHREAD_FAR *pkey,
 void (PTHREAD_USERCALL *destr_rtn)(void PTHREAD_FAR *));
PTHREAD_API int PTHREAD_CALL pthread_key_delete(pthread_key_t key);
PTHREAD_API int PTHREAD_CALL pthread_setspecific(pthread_key_t key,
 const void PTHREAD_FAR *val);
PTHREAD_API void PTHREAD_FAR *PTHREAD_CALL pthread_getspecific(
 pthread_key_t key);

PTHREAD_API int PTHREAD_CALL pthread_kill(pthread_t pthread, int signum);

#define pthread_resume_all_np pthread_resume_all_np
#define pthread_suspend_all_np pthread_suspend_all_np
#define pthread_usleep_np pthread_usleep_np

PTHREAD_API void PTHREAD_CALL pthread_resume_all_np(void);
PTHREAD_API void PTHREAD_CALL pthread_suspend_all_np(void);
PTHREAD_API unsigned PTHREAD_CALL pthread_usleep_np(unsigned usec);

#ifdef __cplusplus
}
#endif

#endif
