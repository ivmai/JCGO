/*
 * @(#) tpthread.c - T-PThread source.
 * Copyright (C) 2006-2007 Ivan Maidanski <ivmai@mail.ru> All rights reserved.
 **
 * Version: 1.2
 * See also files: pthread.h, sched.h
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

/* T-PThread is a tiny portable POSIX threads library */
/* T-PThread API is a subset of the standard POSIX Threads API */

/* The implemented scheduler is non-preemptive (cooperative) */

/*
 * Tested with:
 * Linux RedHat i386 (-O2 -DPTHREAD_FASTCALL= -DPTHREAD_CPUSTATE_SPOFF=16),
 * DJGPP (-O2 -Wall -DPTHREAD_FASTCALL= -DPTHREAD_USE_GETTIMEOFDAY),
 * EMX (-O2 -DPTHREAD_FASTCALL= -DPTHREAD_USE_SIGACTION -DPTHREAD_CPUSTATE_SPOFF=12),
 * Watcom i386 (-ox -DPTHREAD_NO_SIGSET -DPTHREAD_CPUSTATE_SPOFF=28),
 * DMC x32/ss=ds (-o -mx -DPTHREAD_NO_SIGSET -DPTHREAD_CPUSTATE_SPOFF=12),
 * Borland C16 (-Ox -ml -DPTHREAD_NO_SIGSET -D__esp=j_sp),
 * Borland C32 (-Ox -DPTHREAD_NO_SIGSET -D__esp=j_esp),
 * MS C16 (-Ox -AM -DPTHREAD_NO_SIGSET -DPTHREAD_CPUSTATE_SPOFF=6),
 * MS C i386 (-Ox -DPTHREAD_NO_SIGSET -D__jmp_buf=__JUMP_BUFFER -D__esp=Esp).
 */

/*
 * Control macros: PTHREAD_NO_SIGSET, PTHREAD_USE_GETTIMEOFDAY,
 * PTHREAD_USE_SIGACTION.
 * Macros for tuning (also see in pthread.h): PTHREAD_CORE_API,
 * PTHREAD_CORE_CALL, PTHREAD_CORE_FREE, PTHREAD_CORE_MALLOC,
 * PTHREAD_CPUSTATE_SIZE, PTHREAD_CPUSTATE_SPOFF, PTHREAD_DATASTATIC,
 * PTHREAD_FASTCALL, PTHREAD_SCHED_MAXPRIO, PTHREAD_STACK_DEFSIZE,
 * PTHREAD_STACK_GAPWORDS, PTHREAD_STACK_MINSIZE, PTHREAD_STACK_REDSIZE,
 * PTHREAD_STATIC.
 */

/* Compilation note: turn off stack overflow checking globally */

#ifndef _STDLIB_H
#include <stdlib.h>
/* void abort(void); */
/* void exit(int); */
/* void free(void *); */
/* void *malloc(size_t); */
#endif

#ifndef _ERRNO_H
#include <errno.h>
/* int errno; */
#endif

#ifndef _SIGNAL_H
#include <signal.h>
#endif

#ifdef PTHREAD_USE_GETTIMEOFDAY

#ifndef _SYS_TIME_H
#include <sys/time.h>
/* int gettimeofday(struct timeval *, struct timezone *); */
#endif

#else /* PTHREAD_USE_GETTIMEOFDAY */

#ifndef _SYS_TIMEB_H
#include <sys/timeb.h>
/* void ftime(struct timeb *); */
#endif

#endif /* ! PTHREAD_USE_GETTIMEOFDAY */

#include "pthread.h"

#include "sched.h"

#ifndef PTHREAD_DATASTATIC
#define PTHREAD_DATASTATIC static
#endif

#ifndef PTHREAD_STATIC
#define PTHREAD_STATIC static
#endif

#ifndef PTHREAD_FASTCALL
#define PTHREAD_FASTCALL __fastcall
#endif

#ifdef PTHREAD_CPUSTATE_SIZE

void pthread_sched_done(void);
int pthread_sched_init(void);
void pthread_sched_switch(void *pstate, void *pnewstate, void *stackptr);

#else /* PTHREAD_CPUSTATE_SIZE */

#ifndef _SETJMP_H
#include <setjmp.h>
#endif

#ifdef PTHREAD_NO_SIGSET

/* #include <setjmp.h> */
/* void longjmp(jmp_buf, int); */
/* int setjmp(jmp_buf); */

#define PTHREAD_CPUSTATE_SIZE sizeof(jmp_buf)
#define PTHREAD_CPUSTATE_SETJMP(buf) setjmp(buf)
#define PTHREAD_CPUSTATE_LONGJMP(buf) longjmp(buf, 1)

#else /* PTHREAD_NO_SIGSET */

/* #include <setjmp.h> */
/* void siglongjmp(sigjmp_buf, int); */
/* int sigsetjmp(sigjmp_buf, int); */

#define PTHREAD_CPUSTATE_SIZE sizeof(sigjmp_buf)
#define PTHREAD_CPUSTATE_SETJMP(buf) sigsetjmp(buf, 1)
#define PTHREAD_CPUSTATE_LONGJMP(buf) siglongjmp(buf, 1)

#endif /* ! PTHREAD_NO_SIGSET */

#ifndef PTHREAD_CPUSTATE_SPOFF
#define PTHREAD_CPUSTATE_SPOFF (unsigned)((char *)&((struct __jmp_buf *)0)->__esp - (char *)0)
#endif

void pthread_sched_launcher(void);

PTHREAD_STATIC void pthread_sched_done(void)
{
 /* dummy */
}

PTHREAD_STATIC int pthread_sched_init(void)
{
 /* dummy */
 /* atexit(pthread_sched_done); */
 return 0;
}

PTHREAD_STATIC void pthread_sched_switch(void *pstate, void *pnewstate,
 void *stackptr)
{
 if (!PTHREAD_CPUSTATE_SETJMP(pstate))
 {
  if (stackptr)
  {
   int i = ((PTHREAD_CPUSTATE_SIZE - 1) / sizeof(int)) * sizeof(int);
   do
   {
    *(volatile int *)((char *)pnewstate + i) =
     *(volatile int *)((char *)pstate + i);
   } while ((i -= sizeof(int)) >= 0);
   *(void *volatile *)((char *)pnewstate + PTHREAD_CPUSTATE_SPOFF) = stackptr;
  }
  PTHREAD_CPUSTATE_LONGJMP(pnewstate);
 }
 pthread_sched_launcher();
}

#endif /* ! PTHREAD_CPUSTATE_SIZE */

#ifndef NULL
#define NULL (void *)0
#endif

#ifndef PTHREAD_CORE_API
#define PTHREAD_CORE_API PTHREAD_API
#endif

#ifndef PTHREAD_CORE_CALL
#define PTHREAD_CORE_CALL PTHREAD_CALL
#endif

#ifdef PTHREAD_CORE_MALLOC
PTHREAD_CORE_API void *PTHREAD_CORE_CALL PTHREAD_CORE_MALLOC(size_t size);
#else
#define PTHREAD_CORE_MALLOC malloc
#endif

#ifdef PTHREAD_CORE_FREE
PTHREAD_CORE_API void PTHREAD_CORE_CALL PTHREAD_CORE_FREE(void *ptr);
#else
#define PTHREAD_CORE_FREE free
#endif

#ifndef PTHREAD_SCHED_MAXPRIO
#define PTHREAD_SCHED_MAXPRIO 99
#endif

#ifndef PTHREAD_STACK_REDSIZE
#define PTHREAD_STACK_REDSIZE 0x200
#endif

#ifndef PTHREAD_STACK_MINSIZE
#define PTHREAD_STACK_MINSIZE 0x1000
#endif

#ifndef PTHREAD_STACK_DEFSIZE
#define PTHREAD_STACK_DEFSIZE 0x7FF0
#endif

#ifndef PTHREAD_STACK_GAPWORDS
#define PTHREAD_STACK_GAPWORDS 8
#endif

#ifdef PTHREAD_USE_GETTIMEOFDAY

#define PTHREAD_CURTIME_T struct timeval
#define PTHREAD_CURTIME_GET(ptv, pts) (gettimeofday((void *)(ptv), NULL), (pts)->tv_sec = (time_t)(ptv)->tv_sec, (void)((pts)->tv_nsec = (long)(ptv)->tv_usec * 1000L))

#else /* PTHREAD_USE_GETTIMEOFDAY */

#define PTHREAD_CURTIME_T struct timeb
#define PTHREAD_CURTIME_GET(ptv, pts) (ftime(ptv), (pts)->tv_sec = (ptv)->time, (void)((pts)->tv_nsec = (long)(ptv)->millitm * (1000L * 1000L)))

#endif /* ! PTHREAD_USE_GETTIMEOFDAY */

#ifdef PTHREAD_USE_SIGACTION

/* #include <signal.h> */
/* int sigaction(int, const struct sigaction *, struct sigaction *); */

#define PTHREAD_SIGRAISE_T struct sigaction
#define PTHREAD_SIG_RAISE(signum, poact) (sigaction(signum, NULL, poact) ? -1 : (poact)->sa_handler != SIG_DFL && (poact)->sa_handler != SIG_ERR && (poact)->sa_handler != SIG_IGN ? ((*(poact)->sa_handler)(signum), 0) : 0)
#define PTHREAD_SIG_VALID(signum) (!sigaction(signum, NULL, NULL))

#else /* PTHREAD_USE_SIGACTION */

/* #include <signal.h> */
/* int raise(int); */

#ifndef NSIG
#ifdef _NSIG
#define NSIG _NSIG
#else
#define NSIG 256
#endif
#endif

#define PTHREAD_SIGRAISE_T int
#define PTHREAD_SIG_RAISE(signum, poact) (*(poact) = (signum), raise(*(poact)))
#define PTHREAD_SIG_VALID(signum) ((signum) > 0 && (signum) < NSIG)

#endif /* ! PTHREAD_USE_SIGACTION */

#define PTHREAD_PROCESS_ABORT abort()
#define PTHREAD_PROCESS_EXIT exit(0)

#define PTHREAD_ERRNO_SET(errcode) (void)(errno = (errcode))

#define PTHREAD_SCHEDPRIO_MIN(policy) ((policy) != SCHED_OTHER ? 1 : 0)
#define PTHREAD_SCHEDPRIO_MAX(policy) ((policy) != SCHED_OTHER ? PTHREAD_SCHED_MAXPRIO : 0)

#define PTHREAD_SCHEDPOL_TERMINATED -1

#define PTHREAD_DESCR_TID(thr) ((pthread_t)((volatile void *)(thr)))
#define PTHREAD_TID_DESCR(pthread) ((pthread_descr_t)((volatile void *)(pthread)))

#define PTHREAD_DESCR_MAGIC (int)0x50546872L /* 'PThr' */
#define PTHREAD_DESCR_VALID(thr) ((thr) != NULL && (thr) != (pthread_descr_t)-1L && (thr)->p_magic == PTHREAD_DESCR_MAGIC)

#define PTHREAD_SCHEDCMD_WAIT 1
#define PTHREAD_SCHEDCMD_YIELD 2

typedef void *(PTHREAD_USERCALL *pthread_startrtn_t)(void *);
typedef void (PTHREAD_USERCALL *pthread_destr_t)(void *);

struct pthread_descr_s;
typedef struct pthread_descr_s *pthread_descr_t;

struct pthread_descr_s
{
 int p_magic;
 int p_suspended;
 pthread_descr_t p_nextlive;
 pthread_descr_t p_prevlive;
 int p_priority;
 int p_policy;
 int p_signum;
 size_t p_stacksize;
 void *p_stackbase;
 pthread_mutex_t *p_condmutex;
 struct timespec p_waketime;
 pthread_descr_t p_nextwaiting;
 pthread_descr_t p_joining;
 int p_mutexcount;
 int p_errno;
 void *p_retval;
 pthread_startrtn_t p_startrtn;
 long p_cpustate[(PTHREAD_CPUSTATE_SIZE + sizeof(long) - 1) / sizeof(long)];
 void *p_specific[PTHREAD_KEYS_MAX];
};

volatile unsigned pthread_sched_stopped = 0;

PTHREAD_DATASTATIC pthread_descr_t pthread_descr_pendremove = NULL;

PTHREAD_DATASTATIC struct pthread_descr_s pthread_descr_main =
{
 PTHREAD_DESCR_MAGIC,
 0,
 &pthread_descr_main,
 &pthread_descr_main,
 0,
 SCHED_OTHER,
 -1,
 0,
 NULL,
 NULL,
 { 0, 0 },
 NULL,
 NULL,
 0,
 0,
 NULL,
 (pthread_startrtn_t)0,
 { 0 },
 { NULL }
};

PTHREAD_DATASTATIC pthread_descr_t pthread_descr_self = &pthread_descr_main;

PTHREAD_DATASTATIC pthread_destr_t pthread_keys_destr[PTHREAD_KEYS_MAX] =
{
 (pthread_destr_t)0
};

int pthread_noop_access(void *ptr)
{
 return *(volatile char *)ptr;
}

int pthread_stack_getptr(void **pstackptr)
{
 volatile void *stackptr;
 stackptr = &stackptr;
 pthread_noop_access((void *)&stackptr);
 *pstackptr = (void *)stackptr;
 return 0;
}

void pthread_sched_launcher(void)
{
 pthread_descr_t self = pthread_descr_self;
 pthread_startrtn_t rtn;
 void *arg;
 if ((rtn = self->p_startrtn) != (pthread_startrtn_t)0)
 {
  arg = self->p_retval;
  *(volatile pthread_startrtn_t *)&self->p_startrtn = (pthread_startrtn_t)0;
  self->p_retval = NULL;
  pthread_sched_stopped = 0;
  pthread_exit((*rtn)(arg));
 }
}

PTHREAD_STATIC void PTHREAD_FASTCALL pthread_sched_coresleep(
 struct timespec *pts)
{
 struct timespec ts2;
 PTHREAD_CURTIME_T tv;
 do
 {
  PTHREAD_CURTIME_GET(&tv, &ts2);
 } while ((long)pts->tv_sec - (long)ts2.tv_sec > 0 ||
          (pts->tv_sec == ts2.tv_sec && pts->tv_nsec > ts2.tv_nsec));
}

PTHREAD_STATIC void PTHREAD_FASTCALL pthread_sched_resume(pthread_descr_t thr)
{
 if (thr->p_suspended && thr->p_policy != PTHREAD_SCHEDPOL_TERMINATED)
  thr->p_suspended = 0;
}

PTHREAD_STATIC pthread_descr_t PTHREAD_FASTCALL pthread_sched_findnext(void)
{
 pthread_descr_t self = pthread_descr_self;
 pthread_descr_t thr = self;
 pthread_descr_t thr2 = NULL;
 struct timespec ts2;
 PTHREAD_CURTIME_T tv;
 long diff;
 ts2.tv_sec = 0;
 ts2.tv_nsec = 0;
 do
 {
  if (!(thr = thr->p_nextlive)->p_suspended)
   break;
  if (thr->p_waketime.tv_sec && (thr2 == NULL || ((diff =
      (long)thr->p_waketime.tv_sec - (long)thr2->p_waketime.tv_sec) <= 0 &&
      (diff || thr->p_waketime.tv_nsec < thr2->p_waketime.tv_nsec))))
  {
   if (thr2 == NULL)
    PTHREAD_CURTIME_GET(&tv, &ts2);
   thr2 = thr;
   if ((diff = (long)thr->p_waketime.tv_sec - (long)ts2.tv_sec) <= 0 &&
       (diff || thr->p_waketime.tv_nsec <= ts2.tv_nsec))
   {
    pthread_sched_resume(thr);
    thr->p_waketime.tv_sec = 0;
    break;
   }
  }
 } while (thr != self);
 if (thr2 != NULL && thr->p_suspended)
  thr = thr2;
 return thr;
}

PTHREAD_STATIC void PTHREAD_FASTCALL pthread_sched_enable(int schedcmd)
{
 pthread_descr_t self = pthread_descr_self;
 pthread_descr_t thr;
 void *stackptr;
 int signum = -1;
 PTHREAD_SIGRAISE_T oact;
 if (self->p_stacksize)
 {
  (void)pthread_stack_getptr(&stackptr);
  if (stackptr < (void *)(&stackptr) ?
      (void *)((volatile char *)self->p_stackbase +
      (size_t)PTHREAD_STACK_REDSIZE) > stackptr :
      (void *)((volatile char *)self->p_stackbase + (self->p_stacksize -
      (size_t)PTHREAD_STACK_REDSIZE)) <= stackptr)
  {
   pthread_sched_done();
   PTHREAD_PROCESS_ABORT;
  }
 }
 if (schedcmd == PTHREAD_SCHEDCMD_WAIT || pthread_sched_stopped == 1)
 {
  if (schedcmd)
   self->p_suspended = 1;
  thr = self;
  if (pthread_sched_stopped == 1)
   thr = pthread_sched_findnext();
  if (schedcmd == PTHREAD_SCHEDCMD_YIELD)
  {
   self->p_suspended = 0;
   if (thr->p_suspended)
    thr = self;
  }
   else
   {
    if (thr->p_suspended)
    {
     if (!thr->p_waketime.tv_sec)
     {
      pthread_sched_done();
      if (self->p_nextlive != self ||
          self->p_policy != PTHREAD_SCHEDPOL_TERMINATED)
       PTHREAD_PROCESS_ABORT;
      PTHREAD_PROCESS_EXIT;
     }
     pthread_sched_coresleep(&thr->p_waketime);
     thr->p_suspended = 0;
     thr->p_waketime.tv_sec = 0;
    }
   }
  if (thr != self)
  {
   stackptr = NULL;
   if (thr->p_startrtn)
   {
    (void)pthread_stack_getptr(&stackptr);
    stackptr = (void *)((volatile char *)thr->p_stackbase +
                (stackptr < (void *)(&self) ? thr->p_stacksize -
                (PTHREAD_STACK_GAPWORDS + 1) * sizeof(long) :
                PTHREAD_STACK_GAPWORDS * sizeof(long)));
   }
   *(volatile int *)&self->p_errno = errno;
   *(volatile pthread_descr_t *)&pthread_descr_self = thr;
   pthread_sched_switch(&self->p_cpustate, &thr->p_cpustate, stackptr);
   signum = self->p_signum;
   self->p_signum = -1;
   PTHREAD_ERRNO_SET(self->p_errno);
  }
 }
 pthread_sched_stopped--;
 if (signum != -1 && PTHREAD_SIG_RAISE(signum, &oact))
  PTHREAD_ERRNO_SET(self->p_errno);
}

PTHREAD_STATIC void PTHREAD_FASTCALL pthread_sched_disable(void)
{
 pthread_sched_stopped++;
}

PTHREAD_STATIC void PTHREAD_FASTCALL pthread_descr_destroy(
 pthread_descr_t thr)
{
 pthread_descr_t thr2 = thr->p_nextlive;
 thr->p_magic = 0;
 (thr2->p_prevlive = thr->p_prevlive)->p_nextlive = thr2;
 thr->p_joining = NULL;
 thr->p_nextlive = NULL;
 thr->p_prevlive = NULL;
}

PTHREAD_STATIC void PTHREAD_FASTCALL pthread_descr_free(pthread_descr_t thr)
{
 if (thr != &pthread_descr_main)
 {
  PTHREAD_CORE_FREE(thr->p_stackbase);
  if (!thr->p_mutexcount && thr->p_nextwaiting == NULL)
   PTHREAD_CORE_FREE(thr);
 }
}

PTHREAD_STATIC void PTHREAD_FASTCALL pthread_descr_clearpending(void)
{
 pthread_descr_t thr;
 while ((thr = pthread_descr_pendremove) != NULL)
 {
  pthread_descr_pendremove = NULL;
  pthread_descr_destroy(thr);
  pthread_sched_enable(0);
  pthread_descr_free(thr);
  pthread_sched_disable();
 }
}

PTHREAD_STATIC void PTHREAD_FASTCALL pthread_keys_destroy(void)
{
 pthread_descr_t self = pthread_descr_self;
 void *val;
 pthread_destr_t destr_rtn;
 unsigned i;
 int found;
 int retry = PTHREAD_DESTRUCTOR_ITERATIONS;
 do
 {
  found = 0;
  for (i = 0; i < PTHREAD_KEYS_MAX; i++)
   if ((destr_rtn = pthread_keys_destr[i]) != (pthread_destr_t)0 &&
       destr_rtn != (pthread_destr_t)-1L &&
       (val = self->p_specific[i]) != NULL)
   {
    found = 1;
    self->p_specific[i] = NULL;
    (*destr_rtn)(val);
   }
 } while (found && --retry > 0);
}

PTHREAD_STATIC void PTHREAD_FASTCALL pthread_queue_add(
 struct _pthread_queue *pqueue, pthread_descr_t thr)
{
 pthread_descr_t tail;
 thr->p_nextwaiting = thr;
 if ((tail = (pthread_descr_t)((volatile void *)pqueue->tail)) != NULL)
 {
  thr->p_nextwaiting = tail->p_nextwaiting;
  tail->p_nextwaiting = thr;
 }
 pqueue->tail = (_pthread_descr)((volatile void *)thr);
}

PTHREAD_STATIC void *PTHREAD_FASTCALL pthread_queue_remove(
 struct _pthread_queue *pqueue, pthread_descr_t thr)
{
 pthread_descr_t prev;
 pthread_descr_t tail;
 if ((prev = (pthread_descr_t)((volatile void *)pqueue->tail)) != NULL)
 {
  tail = prev;
  while (prev->p_nextwaiting != thr)
   if ((prev = prev->p_nextwaiting) == tail)
   {
    prev = NULL;
    break;
   }
  if (prev != NULL)
  {
   prev->p_nextwaiting = thr->p_nextwaiting;
   if (tail == thr)
    pqueue->tail = tail != prev ?
                    (_pthread_descr)((volatile void *)prev) : NULL;
   thr->p_nextwaiting = NULL;
  }
 }
 return (void *)prev;
}

PTHREAD_STATIC pthread_descr_t PTHREAD_FASTCALL pthread_queue_get(
 struct _pthread_queue *pqueue)
{
 pthread_descr_t thr;
 pthread_descr_t tail;
 do
 {
  thr = NULL;
  if ((tail = (pthread_descr_t)((volatile void *)pqueue->tail)) == NULL)
   break;
  tail->p_nextwaiting = (thr = tail->p_nextwaiting)->p_nextwaiting;
  if (thr == tail)
   pqueue->tail = NULL;
  thr->p_nextwaiting = NULL;
 } while (thr->p_policy == PTHREAD_SCHEDPOL_TERMINATED);
 return thr;
}

PTHREAD_STATIC void *PTHREAD_FASTCALL pthread_condsignal_inner(
 pthread_cond_t *pcond)
{
 pthread_mutex_t *pmutex;
 pthread_descr_t thr;
 pthread_descr_t thr2;
 if ((thr = pthread_queue_get(&pcond->opaque_c_waiting)) != NULL)
 {
  pmutex = thr->p_condmutex;
  thr->p_condmutex = NULL;
  if ((thr2 = (pthread_descr_t)((volatile void *)pmutex->opaque_m_owner)) !=
      NULL)
  {
   if (thr2 != thr)
    pthread_queue_add(&pmutex->opaque_m_waiting, thr);
  }
   else
   {
    pmutex->opaque_m_owner = (_pthread_descr)((volatile void *)thr);
    thr->p_mutexcount++;
    pthread_sched_resume(thr);
   }
 }
 return (void *)thr;
}

PTHREAD_API int PTHREAD_CALL pthread_create(pthread_t *ppthread,
 const pthread_attr_t *pattr, void *(PTHREAD_USERCALL *rtn)(void *),
 void *arg)
{
 pthread_descr_t thr;
 pthread_descr_t self;
 void *stackbase;
 size_t stacksize;
 int res = EAGAIN;
 int i;
 if (!rtn)
  ppthread = NULL;
 pthread_noop_access(ppthread);
 if (pattr == NULL || (stacksize = pattr->opaque_stacksize) == 0)
  stacksize = (size_t)PTHREAD_STACK_DEFSIZE;
 if (stacksize <= (size_t)PTHREAD_STACK_MINSIZE)
  stacksize = (size_t)PTHREAD_STACK_MINSIZE;
 stacksize = (stacksize / (sizeof(long) * 2)) * (sizeof(long) * 2);
 if ((stackbase = PTHREAD_CORE_MALLOC(stacksize)) != NULL)
 {
  if ((thr = PTHREAD_CORE_MALLOC(((sizeof(struct pthread_descr_s) +
      sizeof(int) - 1) / sizeof(int)) * sizeof(int))) != NULL)
  {
   i = ((sizeof(struct pthread_descr_s) - 1) / sizeof(int)) * sizeof(int);
   do
   {
    *(volatile int *)((char *)thr + i) = 0;
   } while ((i -= sizeof(int)) >= 0);
   thr->p_suspended = 1;
   thr->p_policy = SCHED_OTHER;
   if (pattr != NULL)
   {
    thr->p_policy = pattr->opaque_schedpolicy;
    thr->p_priority = pattr->opaque_schedparam.sched_priority;
    if (pattr->opaque_detachstate == PTHREAD_CREATE_DETACHED)
     thr->p_joining = thr;
   }
   thr->p_stackbase = stackbase;
   thr->p_stacksize = stacksize;
   thr->p_signum = -1;
   thr->p_retval = arg;
   thr->p_startrtn = (pthread_startrtn_t)rtn;
   pthread_sched_disable();
   pthread_descr_clearpending();
   self = pthread_descr_self;
   if (self->p_nextlive != self || !pthread_sched_init())
   {
    if (pattr != NULL && pattr->opaque_inheritsched == PTHREAD_INHERIT_SCHED)
    {
     thr->p_policy = self->p_policy;
     thr->p_priority = self->p_priority;
    }
    thr->p_magic = PTHREAD_DESCR_MAGIC;
    thr->p_prevlive = self;
    (thr->p_nextlive = self->p_nextlive)->p_prevlive = thr;
    self->p_nextlive = thr;
    thr->p_suspended = 0;
    *(volatile pthread_t *)ppthread = PTHREAD_DESCR_TID(thr);
    res = 0;
   }
    else
    {
     PTHREAD_CORE_FREE(thr);
     PTHREAD_CORE_FREE(stackbase);
    }
   pthread_sched_enable(0);
  }
   else PTHREAD_CORE_FREE(stackbase);
 }
 return res;
}

PTHREAD_API pthread_t PTHREAD_CALL pthread_self(void)
{
 return PTHREAD_DESCR_TID(pthread_descr_self);
}

PTHREAD_API int PTHREAD_CALL pthread_equal(pthread_t pthread,
 pthread_t pthread2)
{
 return pthread == pthread2;
}

PTHREAD_API void PTHREAD_CALL pthread_exit(void *retval)
{
 pthread_descr_t self;
 pthread_descr_t thr;
 pthread_keys_destroy();
 pthread_sched_disable();
 pthread_descr_clearpending();
 (self = pthread_descr_self)->p_waketime.tv_sec = 0;
 self->p_retval = retval;
 self->p_policy = PTHREAD_SCHEDPOL_TERMINATED;
 if ((thr = self->p_joining) != NULL)
 {
  if (thr != self)
   pthread_sched_resume(thr);
   else pthread_descr_pendremove = self;
 }
 pthread_sched_stopped = 1;
 pthread_sched_enable(PTHREAD_SCHEDCMD_WAIT);
}

PTHREAD_API int PTHREAD_CALL pthread_join(pthread_t pthread, void **pretval)
{
 pthread_descr_t thr;
 pthread_descr_t self;
 int res = ESRCH;
 if (pretval != NULL)
  pthread_noop_access(pretval);
 pthread_sched_disable();
 pthread_descr_clearpending();
 thr = PTHREAD_TID_DESCR(pthread);
 self = thr;
 if (PTHREAD_DESCR_VALID(thr))
 {
  res = EDEADLK;
  if (pthread_sched_stopped == 1 && (self = pthread_descr_self) != thr)
  {
   if (thr->p_joining != NULL)
   {
    self = thr;
    res = EINVAL;
   }
    else
    {
     thr->p_joining = self;
     while (thr->p_policy != PTHREAD_SCHEDPOL_TERMINATED)
     {
      pthread_sched_enable(PTHREAD_SCHEDCMD_WAIT);
      pthread_sched_disable();
     }
     if (pretval != NULL)
      *pretval = thr->p_retval;
     pthread_descr_destroy(thr);
     res = 0;
    }
  }
 }
 pthread_sched_enable(0);
 if (self != thr)
  pthread_descr_free(thr);
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_detach(pthread_t pthread)
{
 pthread_descr_t thr;
 pthread_descr_t thr2;
 int res = ESRCH;
 pthread_sched_disable();
 pthread_descr_clearpending();
 thr = PTHREAD_TID_DESCR(pthread);
 if (PTHREAD_DESCR_VALID(thr))
 {
  res = EINVAL;
  if ((thr2 = thr->p_joining) != NULL)
  {
   if (thr2 != thr)
    res = 0;
   thr = NULL;
  }
   else
   {
    res = 0;
    if (thr->p_policy != PTHREAD_SCHEDPOL_TERMINATED)
    {
     thr->p_joining = thr;
     thr = NULL;
    }
     else pthread_descr_destroy(thr);
   }
 }
  else thr = NULL;
 pthread_sched_enable(0);
 if (thr != NULL)
  pthread_descr_free(thr);
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_init(pthread_attr_t *pattr)
{
 pattr->opaque_detachstate = PTHREAD_CREATE_JOINABLE;
 pattr->opaque_schedpolicy = SCHED_OTHER;
 pattr->opaque_schedparam.sched_priority = 0;
 pattr->opaque_inheritsched = PTHREAD_EXPLICIT_SCHED;
 pattr->opaque_scope = 0;
 pattr->opaque_stackaddr = NULL;
 pattr->opaque_stacksize = 0;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_destroy(pthread_attr_t *pattr)
{
 pthread_noop_access(pattr);
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_setdetachstate(
 pthread_attr_t *pattr, int detachstate)
{
 int res = EINVAL;
 if (detachstate == PTHREAD_CREATE_JOINABLE ||
     detachstate == PTHREAD_CREATE_DETACHED)
 {
  pattr->opaque_detachstate = detachstate;
  res = 0;
 }
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_getdetachstate(
 const pthread_attr_t *pattr, int *pdetachstate)
{
 *pdetachstate = pattr->opaque_detachstate;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_setschedparam(pthread_attr_t *pattr,
 const struct sched_param *pparam)
{
 int priority = pparam->sched_priority;
 int policy = pattr->opaque_schedpolicy;
 int res = EINVAL;
 if (PTHREAD_SCHEDPRIO_MIN(policy) <= priority &&
     PTHREAD_SCHEDPRIO_MAX(policy) >= priority)
 {
  pattr->opaque_schedparam.sched_priority = priority;
  res = 0;
 }
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_getschedparam(
 const pthread_attr_t *pattr, struct sched_param *pparam)
{
 pparam->sched_priority = pattr->opaque_schedparam.sched_priority;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_setschedpolicy(
 pthread_attr_t *pattr, int policy)
{
 int res = EINVAL;
 if (policy == SCHED_OTHER || policy == SCHED_FIFO || policy == SCHED_RR)
 {
  pattr->opaque_schedpolicy = policy;
  res = 0;
 }
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_getschedpolicy(
 const pthread_attr_t *pattr, int *ppolicy)
{
 *ppolicy = pattr->opaque_schedpolicy;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_setinheritsched(
 pthread_attr_t *pattr, int inherit)
{
 int res = EINVAL;
 if (inherit == PTHREAD_INHERIT_SCHED || inherit == PTHREAD_EXPLICIT_SCHED)
 {
  pattr->opaque_inheritsched = inherit;
  res = 0;
 }
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_getinheritsched(
 const pthread_attr_t *pattr, int *pinherit)
{
 *pinherit = pattr->opaque_inheritsched;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_setstacksize(pthread_attr_t *pattr,
 size_t stacksize)
{
 pattr->opaque_stacksize = stacksize;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_attr_getstacksize(
 const pthread_attr_t *pattr, size_t *pstacksize)
{
 *pstacksize = pattr->opaque_stacksize;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_setschedparam(pthread_t pthread,
 int policy, const struct sched_param *pparam)
{
 int res = ESRCH;
 int priority = pparam->sched_priority;
 pthread_descr_t thr;
 pthread_sched_disable();
 thr = PTHREAD_TID_DESCR(pthread);
 if (PTHREAD_DESCR_VALID(thr) && thr->p_policy != PTHREAD_SCHEDPOL_TERMINATED)
 {
  res = EINVAL;
  if ((policy == SCHED_OTHER || policy == SCHED_FIFO || policy == SCHED_RR) &&
      PTHREAD_SCHEDPRIO_MIN(policy) <= priority &&
      PTHREAD_SCHEDPRIO_MAX(policy) >= priority)
  {
   thr->p_policy = policy;
   thr->p_priority = priority;
   res = 0;
  }
 }
 pthread_sched_enable(0);
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_getschedparam(pthread_t pthread,
 int *ppolicy, struct sched_param *pparam)
{
 int res = ESRCH;
 int policy;
 pthread_descr_t thr;
 pthread_noop_access(ppolicy);
 pthread_noop_access(pparam);
 pthread_sched_disable();
 thr = PTHREAD_TID_DESCR(pthread);
 if (PTHREAD_DESCR_VALID(thr) &&
     (policy = thr->p_policy) != PTHREAD_SCHEDPOL_TERMINATED)
 {
  *ppolicy = policy;
  pparam->sched_priority = thr->p_priority;
  res = 0;
 }
 pthread_sched_enable(0);
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_mutex_init(pthread_mutex_t *pmutex,
 const pthread_mutexattr_t *pmutexattr)
{
 pmutex->opaque_m_spinlock = 0;
 pmutex->opaque_m_count = 0;
 pmutex->opaque_m_owner = NULL;
 pmutex->opaque_m_kind = pmutexattr != NULL ? pmutexattr->opaque_mutexkind :
                          PTHREAD_MUTEX_FAST_NP;
 pmutex->opaque_m_waiting.head = NULL;
 pmutex->opaque_m_waiting.tail = NULL;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_mutex_destroy(pthread_mutex_t *pmutex)
{
 return pmutex->opaque_m_owner != NULL ? EBUSY : 0;
}

PTHREAD_API int PTHREAD_CALL pthread_mutex_trylock(pthread_mutex_t *pmutex)
{
 int res = EBUSY;
 if (pmutex->opaque_m_owner == NULL)
 {
  pthread_sched_disable();
  if (*(volatile _pthread_descr *)(&pmutex->opaque_m_owner) == NULL)
  {
   ((pthread_descr_t)((volatile void *)(pmutex->opaque_m_owner =
    (_pthread_descr)((volatile void *)pthread_descr_self))))->p_mutexcount++;
   res = 0;
  }
  pthread_sched_stopped--;
 }
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_mutex_lock(pthread_mutex_t *pmutex)
{
 pthread_descr_t self;
 pthread_descr_t thr;
 int res;
 pthread_noop_access(pmutex);
 pthread_sched_disable();
 self = pthread_descr_self;
 if ((thr = (pthread_descr_t)((volatile void *)pmutex->opaque_m_owner)) !=
     NULL)
 {
  res = EDEADLK;
  if (thr != self && pthread_sched_stopped == 1 &&
      thr->p_policy != PTHREAD_SCHEDPOL_TERMINATED &&
      self->p_nextwaiting == NULL)
  {
   pthread_queue_add(&pmutex->opaque_m_waiting, self);
   do
   {
    pthread_sched_enable(PTHREAD_SCHEDCMD_WAIT);
    pthread_sched_disable();
   } while ((pthread_descr_t)((volatile void *)pmutex->opaque_m_owner) !=
            self);
   res = 0;
  }
 }
  else
  {
   pmutex->opaque_m_owner = (_pthread_descr)((volatile void *)self);
   self->p_mutexcount++;
   res = 0;
  }
 pthread_sched_stopped--;
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_mutex_unlock(pthread_mutex_t *pmutex)
{
 pthread_descr_t thr;
 pthread_descr_t self;
 int res = EPERM;
 pthread_noop_access(pmutex);
 pthread_sched_disable();
 if ((self = pthread_descr_self) ==
     (pthread_descr_t)((volatile void *)pmutex->opaque_m_owner))
 {
  self->p_mutexcount--;
  thr = pthread_queue_get(&pmutex->opaque_m_waiting);
  if ((pmutex->opaque_m_owner = (_pthread_descr)((volatile void *)thr)) !=
      NULL)
  {
   thr->p_mutexcount++;
   pthread_sched_resume(thr);
  }
  res = 0;
 }
 pthread_sched_enable(0);
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_mutexattr_init(
 pthread_mutexattr_t *pmutexattr)
{
 pmutexattr->opaque_mutexkind = PTHREAD_MUTEX_FAST_NP;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_mutexattr_destroy(
 pthread_mutexattr_t *pmutexattr)
{
 pthread_noop_access(pmutexattr);
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_cond_init(pthread_cond_t *pcond,
 const pthread_condattr_t *pcondattr)
{
 if (pcondattr != NULL)
  pthread_noop_access(*(pthread_condattr_t **)&pcondattr);
 pcond->opaque_c_spinlock = 0;
 pcond->opaque_c_waiting.head = NULL;
 pcond->opaque_c_waiting.tail = NULL;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_cond_destroy(pthread_cond_t *pcond)
{
 return pcond->opaque_c_waiting.tail != NULL ? EBUSY : 0;
}

PTHREAD_API int PTHREAD_CALL pthread_cond_signal(pthread_cond_t *pcond)
{
 pthread_noop_access(pcond);
 pthread_sched_disable();
 (void)pthread_condsignal_inner(pcond);
 pthread_sched_stopped--;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_cond_broadcast(pthread_cond_t *pcond)
{
 pthread_noop_access(pcond);
 pthread_sched_disable();
 for (;;)
 {
  if (pthread_condsignal_inner(pcond) == NULL)
   break;
 }
 pthread_sched_enable(0);
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_cond_wait(pthread_cond_t *pcond,
 pthread_mutex_t *pmutex)
{
 pthread_descr_t self;
 pthread_descr_t thr;
 int res = EPERM;
 pthread_noop_access(pcond);
 pthread_noop_access(pmutex);
 pthread_sched_disable();
 if ((self = pthread_descr_self) ==
     (pthread_descr_t)((volatile void *)pmutex->opaque_m_owner))
 {
  res = EDEADLK;
  if (pthread_sched_stopped == 1 && self->p_nextwaiting == NULL)
  {
   self->p_condmutex = pmutex;
   pthread_queue_add(&pcond->opaque_c_waiting, self);
   self->p_mutexcount--;
   thr = pthread_queue_get(&pmutex->opaque_m_waiting);
   if ((pmutex->opaque_m_owner = (_pthread_descr)((volatile void *)thr)) !=
       NULL)
   {
    thr->p_mutexcount++;
    pthread_sched_resume(thr);
   }
   do
   {
    pthread_sched_enable(PTHREAD_SCHEDCMD_WAIT);
    pthread_sched_disable();
   } while ((pthread_descr_t)((volatile void *)pmutex->opaque_m_owner) !=
            self);
   res = 0;
  }
 }
 pthread_sched_stopped--;
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_cond_timedwait(pthread_cond_t *pcond,
 pthread_mutex_t *pmutex, const struct timespec *pabstime)
{
 pthread_descr_t self;
 pthread_descr_t thr;
 struct timespec ts;
 struct timespec ts2;
 PTHREAD_CURTIME_T tv;
 int res = EPERM;
 pthread_noop_access(pcond);
 pthread_noop_access(pmutex);
 ts.tv_sec = pabstime->tv_sec;
 ts.tv_nsec = pabstime->tv_nsec;
 PTHREAD_CURTIME_GET(&tv, &ts2);
 pthread_sched_disable();
 if ((self = pthread_descr_self) ==
     (pthread_descr_t)((volatile void *)pmutex->opaque_m_owner))
 {
  res = EDEADLK;
  if (self->p_nextwaiting == NULL)
  {
   res = ETIMEDOUT;
   if ((long)ts.tv_sec - (long)ts2.tv_sec > 0 ||
       (ts.tv_sec == ts2.tv_sec && ts.tv_nsec > ts2.tv_nsec))
   {
    self->p_condmutex = pmutex;
    pthread_queue_add(&pcond->opaque_c_waiting, self);
    self->p_mutexcount--;
    thr = pthread_queue_get(&pmutex->opaque_m_waiting);
    if ((pmutex->opaque_m_owner = (_pthread_descr)((volatile void *)thr)) !=
        NULL)
    {
     thr->p_mutexcount++;
     pthread_sched_resume(thr);
    }
    if ((self->p_waketime.tv_sec = ts.tv_sec) == 0)
     self->p_waketime.tv_sec = (time_t)-1;
    self->p_waketime.tv_nsec = ts.tv_nsec;
    pthread_sched_enable(PTHREAD_SCHEDCMD_WAIT);
    pthread_sched_disable();
    res = 0;
    if ((pthread_descr_t)((volatile void *)pmutex->opaque_m_owner) != self)
    {
     if (pthread_queue_remove(&pcond->opaque_c_waiting, self) != NULL)
     {
      self->p_condmutex = NULL;
      res = self->p_waketime.tv_sec ? EINTR : ETIMEDOUT;
      if (pmutex->opaque_m_owner != NULL)
       pthread_queue_add(&pmutex->opaque_m_waiting, self);
       else
       {
        pmutex->opaque_m_owner = (_pthread_descr)((volatile void *)self);
        self->p_mutexcount++;
       }
     }
     while ((pthread_descr_t)((volatile void *)pmutex->opaque_m_owner) !=
            self)
     {
      pthread_sched_enable(PTHREAD_SCHEDCMD_WAIT);
      pthread_sched_disable();
     }
    }
    self->p_waketime.tv_sec = 0;
    self->p_waketime.tv_nsec = 0;
   }
  }
 }
 pthread_sched_stopped--;
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_condattr_init(
 pthread_condattr_t *pcondattr)
{
 pcondattr->opaque_condflags = 0;
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_condattr_destroy(
 pthread_condattr_t *pcondattr)
{
 pthread_noop_access(pcondattr);
 return 0;
}

PTHREAD_API int PTHREAD_CALL pthread_key_create(pthread_key_t *pkey,
 void (PTHREAD_USERCALL *destr_rtn)(void *))
{
 unsigned i;
 pthread_noop_access(pkey);
 pthread_sched_disable();
 for (i = 0; i < PTHREAD_KEYS_MAX; i++)
  if (!pthread_keys_destr[i])
  {
   pthread_keys_destr[i] = destr_rtn ? (pthread_destr_t)destr_rtn :
                            (pthread_destr_t)-1L;
   *pkey = (pthread_key_t)i;
   break;
  }
 pthread_sched_enable(0);
 return i < PTHREAD_KEYS_MAX ? 0 : EAGAIN;
}

PTHREAD_API int PTHREAD_CALL pthread_key_delete(pthread_key_t key)
{
 pthread_descr_t thr;
 pthread_descr_t self;
 int res = EINVAL;
 pthread_sched_disable();
 if ((unsigned)key < PTHREAD_KEYS_MAX &&
     pthread_keys_destr[(unsigned)key] != (pthread_destr_t)0)
 {
  self = pthread_descr_self;
  pthread_keys_destr[(unsigned)key] = (pthread_destr_t)0;
  thr = self;
  do
  {
   thr->p_specific[(unsigned)key] = NULL;
  } while ((thr = thr->p_prevlive) != self);
  res = 0;
 }
 pthread_sched_enable(0);
 return res;
}

PTHREAD_API int PTHREAD_CALL pthread_setspecific(pthread_key_t key,
 const void *val)
{
 int res = EINVAL;
 if ((unsigned)key < PTHREAD_KEYS_MAX &&
     pthread_keys_destr[(unsigned)key] != (pthread_destr_t)0)
 {
  pthread_descr_self->p_specific[(unsigned)key] = (void *)val;
  res = 0;
 }
 return res;
}

PTHREAD_API void *PTHREAD_CALL pthread_getspecific(pthread_key_t key)
{
 return (unsigned)key < PTHREAD_KEYS_MAX ?
         pthread_descr_self->p_specific[(unsigned)key] : NULL;
}

PTHREAD_API int PTHREAD_CALL pthread_kill(pthread_t pthread, int signum)
{
 pthread_descr_t thr;
 int res;
 int p_errno = errno;
 PTHREAD_SIGRAISE_T oact;
 if (PTHREAD_DESCR_TID(pthread_descr_self) != pthread)
 {
  res = EINVAL;
  if (PTHREAD_SIG_VALID(signum))
  {
   res = ESRCH;
   pthread_sched_disable();
   thr = PTHREAD_TID_DESCR(pthread);
   while (PTHREAD_DESCR_VALID(thr) &&
          thr->p_policy != PTHREAD_SCHEDPOL_TERMINATED)
   {
    if ((p_errno = thr->p_signum) == signum || p_errno == -1)
    {
     thr->p_signum = signum;
     pthread_sched_resume(thr);
     res = 0;
     break;
    }
    pthread_sched_enable(PTHREAD_SCHEDCMD_YIELD);
    pthread_sched_disable();
   }
   pthread_sched_enable(0);
  }
   else PTHREAD_ERRNO_SET(p_errno);
 }
  else
  {
   res = 0;
   if (PTHREAD_SIG_RAISE(signum, &oact))
   {
    PTHREAD_ERRNO_SET(p_errno);
    res = EINVAL;
   }
  }
 return res;
}

PTHREAD_API void PTHREAD_CALL pthread_resume_all_np(void)
{
 if (pthread_sched_stopped)
  pthread_sched_stopped--;
}

PTHREAD_API void PTHREAD_CALL pthread_suspend_all_np(void)
{
 pthread_sched_disable();
}

PTHREAD_API unsigned PTHREAD_CALL pthread_usleep_np(unsigned usec)
{
 pthread_descr_t self;
 struct timespec ts;
 struct timespec ts2;
 PTHREAD_CURTIME_T tv;
 long diff;
 PTHREAD_CURTIME_GET(&tv, &ts);
 ts.tv_sec += (time_t)((unsigned long)usec / (unsigned long)(1000L * 1000L));
 if ((unsigned long)(ts.tv_nsec += (long)((unsigned long)usec %
     (unsigned long)(1000L * 1000L)) * 1000L) >=
     (unsigned long)(1000L * 1000L * 1000L))
 {
  ts.tv_sec++;
  ts.tv_nsec -= 1000L * 1000L * 1000L;
 }
 pthread_sched_disable();
 self = pthread_descr_self;
 if ((self->p_waketime.tv_sec = ts.tv_sec) == 0)
  self->p_waketime.tv_sec = (time_t)-1;
 self->p_waketime.tv_nsec = ts.tv_nsec;
 pthread_sched_enable(PTHREAD_SCHEDCMD_WAIT);
 self->p_waketime.tv_sec = 0;
 self->p_waketime.tv_nsec = 0;
 PTHREAD_CURTIME_GET(&tv, &ts2);
 usec = 0;
 if (((diff = (long)ts.tv_sec - (long)ts2.tv_sec) > 0 ||
     (!diff && ts.tv_nsec > ts2.tv_nsec)) &&
     (usec = (unsigned)(diff * (1000L * 1000L) +
     ((long)ts.tv_nsec - (long)ts2.tv_nsec) / 1000L)) != 0)
  PTHREAD_ERRNO_SET(EINTR);
 return usec;
}

PTHREAD_API int PTHREAD_CALL sched_yield(void)
{
 pthread_sched_disable();
 pthread_sched_enable(PTHREAD_SCHEDCMD_YIELD);
 return 0;
}

PTHREAD_API int PTHREAD_CALL sched_get_priority_max(int policy)
{
 if (policy != SCHED_OTHER && policy != SCHED_FIFO && policy != SCHED_RR)
 {
  PTHREAD_ERRNO_SET(EINVAL);
  return -1;
 }
 return PTHREAD_SCHEDPRIO_MAX(policy);
}

PTHREAD_API int PTHREAD_CALL sched_get_priority_min(int policy)
{
 if (policy != SCHED_OTHER && policy != SCHED_FIFO && policy != SCHED_RR)
 {
  PTHREAD_ERRNO_SET(EINVAL);
  return -1;
 }
 return PTHREAD_SCHEDPRIO_MIN(policy);
}
