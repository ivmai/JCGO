/*
 * @(#) $(JCGO)/include/jcgothrd.h --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2013 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
 */

/**
 * This file is compiled together with the files produced by the JCGO
 * translator (do not include and/or compile this file directly).
 */

/*
 * Used control macros: JCGO_CLOCKGETTM, JCGO_CREATHREAD, JCGO_GCGETPAR,
 * JCGO_MONOTWAIT, JCGO_OS2, JCGO_PARALLEL, JCGO_SOLTHR, JCGO_TIMEHIRES,
 * JCGO_UNIX, JCGO_WIN32.
 * Macros for tuning: CLIBDECL.
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

/* #include <stdlib.h> */

#define JCGO_JTHREAD_MINPRIO 1
#define JCGO_JTHREAD_MAXPRIO 10

#ifdef JCGO_GCGETPAR
#ifndef JCGO_NPROCS_STMT
#ifndef JCGO_NOGC
/* #include "gc.h" */
/* int GC_get_parallel(void); */
#define JCGO_NPROCS_STMT(pdata) { *(pdata) = GC_get_parallel() + 1; }
#endif
#endif
#endif

#ifdef JCGO_WIN32

#ifndef JCGO_CREATHREAD
#ifndef _PROCESS_H
#include <process.h>
#endif
#endif

#ifndef _WINDOWS_H
#include <windows.h>
/* BOOL CloseHandle(HANDLE); */
/* HANDLE CreateEvent(SECURITY_ATTRIBUTES *, BOOL, BOOL, LPCTSTR); */
/* BOOL DuplicateHandle(HANDLE, HANDLE, HANDLE, HANDLE *, DWORD, BOOL, DWORD); */
/* DWORD GetCurrentThreadId(void); */
/* LONG InterlockedExchange(LONG *, LONG); */
/* BOOL SetEvent(HANDLE); */
/* BOOL SetThreadPriority(HANDLE, int); */
/* BOOL ResetEvent(HANDLE); */
/* DWORD WaitForSingleObject(HANDLE, DWORD); */
#endif

#ifdef JCGO_TIMEHIRES
#ifndef _MMSYSTEM_H
#include <mmsystem.h>
/* MMRESULT timeBeginPeriod(unsigned); */
/* MMRESULT timeEndPeriod(unsigned); */
#endif
#endif

#define JCGO_THREAD_T struct { HANDLE handle; DWORD id; }
#define JCGO_THREADT_CLEAR(pthread) ((pthread)->handle = (HANDLE)0, (pthread)->id = (DWORD)-1L, 0)
#define JCGO_THREADT_ISVALID(pthread) (*(volatile HANDLE *)(&(pthread)->handle) != (HANDLE)0 && (pthread)->handle != (HANDLE)((ptrdiff_t)-1L))
#define JCGO_THREADT_COPY(pthread, psrcthread) ((pthread)->handle = (psrcthread)->handle, (pthread)->id = (psrcthread)->id, 0)
#define JCGO_THREADT_ISEQUALIDENT(pthread, psrcthread) ((pthread)->id == (psrcthread)->id)

#ifndef JCGO_THREAD_CREATE
#ifdef JCGO_CREATHREAD
/* #include <windows.h> */
/* HANDLE CreateThread(SECURITY_ATTRIBUTES *, DWORD, DWORD (WINAPI *)(void *), void *, DWORD, DWORD *); */
#define JCGO_THREAD_CREATE(pthread, rtn, stacksize, param) (((pthread)->handle = CreateThread(NULL, (DWORD)(stacksize), rtn, param, 0, NULL)) != (HANDLE)0 ? 0 : -1)
#define JCGO_THREADRET_T DWORD
#define JCGO_THREAD_RTNDECL WINAPI
#else
#ifdef JCGO_UNIX
#ifdef _BEGINTHREAD_NO_STACKPTR
/* #include <process.h> */
/* int _beginthread(void (*)(void *), unsigned, void *); */
#define JCGO_THREAD_CREATE(pthread, rtn, stacksize, param) (((pthread)->handle = (HANDLE)_beginthread(rtn, stacksize, param)) != (HANDLE)((ptrdiff_t)-1L) ? 0 : -1)
#else
/* #include <process.h> */
/* int _beginthread(void (*)(void *), void *, unsigned, void *); */
#define JCGO_THREAD_CREATE(pthread, rtn, stacksize, param) (((pthread)->handle = (HANDLE)_beginthread(rtn, NULL, stacksize, param)) != (HANDLE)((ptrdiff_t)-1L) ? 0 : -1)
#endif
#define JCGO_THREADRET_T void
#define JCGO_THREAD_RETVALUE /* empty */
#else
/* #include <process.h> */
/* uintptr_t _beginthreadex(void *, unsigned, unsigned (*)(void *), void *, unsigned, unsigned *); */
#define JCGO_THREAD_CREATE(pthread, rtn, stacksize, param) (((pthread)->handle = (HANDLE)_beginthreadex(NULL, stacksize, rtn, param, 0, (unsigned *)&jcgo_trashVar)) != (HANDLE)((ptrdiff_t)-1L) && (pthread)->handle ? 0 : -1)
#define JCGO_THREADRET_T unsigned
#ifndef JCGO_THREAD_RTNDECL
#define JCGO_THREAD_RTNDECL __stdcall
#endif
#endif
#endif
#endif

#define JCGO_THREAD_IDENTSELF(pthread) (((pthread)->id = GetCurrentThreadId()) != (DWORD)-1L ? 0 : -1)
#define JCGO_THREAD_STOREIDENT(pthread) JCGO_THREAD_IDENTSELF(pthread)

#ifdef _WINBASE_NO_GETCURRENTTHREAD
#define JCGO_THREAD_SELF(pthread, pmainthread) (JCGO_THREAD_IDENTSELF(pthread) ? -1 : ((pthread)->handle = (HANDLE)((ptrdiff_t)-2L), 0))
#else
/* #include <windows.h> */
/* HANDLE GetCurrentProcess(void); */
/* HANDLE GetCurrentThread(void); */
#define JCGO_THREAD_SELF(pthread, pmainthread) (JCGO_THREAD_IDENTSELF(pthread) ? -1 : (pthread) != (pmainthread) && (pmainthread)->handle == (HANDLE)((ptrdiff_t)-2L) ? ((pthread)->handle = (HANDLE)((ptrdiff_t)-2L), 0) : ((pthread)->handle = GetCurrentProcess(), DuplicateHandle((pthread)->handle, GetCurrentThread(), (pthread)->handle, &(pthread)->handle, 0, FALSE, DUPLICATE_SAME_ACCESS) ? 0 : (pthread) == (pmainthread) ? ((pthread)->handle = (HANDLE)((ptrdiff_t)-2L), 0) : ((pthread)->handle = (HANDLE)0, -1)))
#endif

#define JCGO_THREAD_CLOSEHND(pthread) ((pthread)->handle == (HANDLE)((ptrdiff_t)-2L) || CloseHandle((pthread)->handle) ? 0 : -1)
#define JCGO_THREAD_DETACH(pthread) JCGO_THREAD_CLOSEHND(pthread)
#define JCGO_THREADPRIO_T int
#define JCGO_THREADPRIO_SET(pthread, psched, priority) (*(psched) = (priority) == JCGO_JTHREAD_MAXPRIO ? THREAD_PRIORITY_TIME_CRITICAL : ((priority) / ((JCGO_JTHREAD_MINPRIO + JCGO_JTHREAD_MAXPRIO + 1) / 2) + (priority) - JCGO_JTHREAD_MINPRIO) / ((JCGO_JTHREAD_MAXPRIO - JCGO_JTHREAD_MINPRIO + 1) / (THREAD_PRIORITY_HIGHEST - THREAD_PRIORITY_LOWEST + (THREAD_PRIORITY_HIGHEST >= THREAD_PRIORITY_LOWEST ? 1 : -1))) + THREAD_PRIORITY_LOWEST, SetThreadPriority((pthread)->handle == (HANDLE)((ptrdiff_t)-2L) ? (HANDLE)((ptrdiff_t)(pthread)->id) : (pthread)->handle, *(psched)) ? 0 : -1)

struct jcgo_win32Mutex_s
{
 HANDLE event;
 LONG state;
};

#define JCGO_MUTEX_T struct jcgo_win32Mutex_s
#define JCGO_MUTEX_INIT(pmutex) (((pmutex)->event = CreateEvent(NULL, FALSE, FALSE, NULL)) != (HANDLE)0 ? (int)((pmutex)->state = 0) : -1)
#define JCGO_MUTEX_LOCK(pmutex) (JCGO_EXPECT_FALSE(InterlockedExchange(&(pmutex)->state, 1) != 0) ? jcgo_win32BlockOnMutex(pmutex) : 0)
#define JCGO_MUTEX_UNLOCK(pmutex) (JCGO_EXPECT_TRUE(InterlockedExchange(&(pmutex)->state, 0) >= 0) || SetEvent((pmutex)->event) ? 0 : -1)
#define JCGO_EVENT_T HANDLE
#define JCGO_EVENT_INIT(pevent) ((*(pevent) = CreateEvent(NULL, FALSE, FALSE, NULL)) != (HANDLE)0 ? 0 : -1)
#define JCGO_EVENT_CLEAR(pevent) (ResetEvent(*(pevent)) ? 0 : -1)
#define JCGO_EVENT_SET(pevent) (SetEvent(*(pevent)) ? 0 : -1)
#define JCGO_EVENT_WAIT(pevent) (WaitForSingleObject(*(pevent), INFINITE) != WAIT_FAILED ? 0 : -1)
#define JCGO_EVENT_DESTROY(pevent) (CloseHandle(*(pevent)) ? 0 : -1)
#define JCGO_EVENTTIME_T long
#define JCGO_EVENTTIME_PREPARE(pwaittime, timeout, ns) (*(pwaittime) = ((timeout) != 0L ? (timeout) : 1L), 0)

#ifdef JCGO_TIMEHIRES
#ifndef JCGO_EVENTHIGHRES_MODVAL
#define JCGO_EVENTHIGHRES_MODVAL 10
#endif
#ifdef _STDLIB_NO_GETENV
#define jcgo_threadNoTimeHighRes 0
#else
/* #include <stdlib.h> */
/* char *getenv(const char *); */
#define JCGO_EVENTHIGHRESCONFVAR_DEFN STATICDATA int jcgo_threadNoTimeHighRes = 0;
#define JCGO_EVENTHIGHRESCONFVAR_INITSTMT { char *cstr; if ((cstr = getenv("NO_TIME_HIGH_RESOLUTION")) != NULL && *cstr) jcgo_threadNoTimeHighRes = 1; }
#endif
#define JCGO_EVENT_TIMEDWAIT(pevent, pwaittime) (!jcgo_threadNoTimeHighRes && (DWORD)(*(pwaittime)) % (DWORD)JCGO_EVENTHIGHRES_MODVAL != 0 ? (timeBeginPeriod(1), WaitForSingleObject(*(pevent), (DWORD)(*(pwaittime))) != WAIT_FAILED ? (timeEndPeriod(1), 0) : (timeEndPeriod(1), -1)) : WaitForSingleObject(*(pevent), (DWORD)(*(pwaittime))) != WAIT_FAILED ? 0 : -1)
#else
#define JCGO_EVENT_TIMEDWAIT(pevent, pwaittime) (WaitForSingleObject(*(pevent), (DWORD)(*(pwaittime))) != WAIT_FAILED ? 0 : -1)
#endif

#ifndef JCGO_THREAD_YIELD
/* #include <windows.h> */
/* void Sleep(DWORD); */
#define JCGO_THREAD_YIELD Sleep((DWORD)0)
#endif

#ifndef JCGO_NPROCS_STMT
#ifdef _WINBASE_NO_GETPROCESSAFFINITYMASK
#ifndef _WINBASE_NO_GETSYSTEMINFO
/* #include <windows.h> */
/* void GetSystemInfo(SYSTEM_INFO *); */
#define JCGO_NPROCS_T SYSTEM_INFO
#define JCGO_NPROCS_STMT(pdata) { (pdata)->dwNumberOfProcessors = 0; GetSystemInfo(pdata); }
#define JCGO_NPROCS_GET(pdata) ((pdata)->dwActiveProcessorMask ? (pdata)->dwNumberOfProcessors : 0)
#endif
#else
/* #include <windows.h> */
/* HANDLE GetCurrentProcess(void); */
#ifdef _WIN64
/* #include <windows.h> */
/* BOOL GetProcessAffinityMask(HANDLE, DWORD_PTR *, DWORD_PTR *); */
#define JCGO_NPROCS_T struct { DWORD_PTR procMask; DWORD_PTR sysMask; int ncpu; }
#else
/* BOOL GetProcessAffinityMask(HANDLE, DWORD *, DWORD *); */
#define JCGO_NPROCS_T struct { DWORD procMask; DWORD procMaskPad; DWORD sysMask; DWORD sysMaskPad; int ncpu; }
#endif
#define JCGO_NPROCS_STMT(pdata) { (pdata)->procMask = 0; (pdata)->ncpu = 0; if (GetProcessAffinityMask(GetCurrentProcess(), (void *)&(pdata)->procMask, (void *)&(pdata)->sysMask) && (pdata)->procMask) do { (pdata)->ncpu++; } while (((pdata)->procMask &= (pdata)->procMask - 1) != 0); }
#define JCGO_NPROCS_GET(pdata) ((pdata)->ncpu)
#endif
#endif

#ifdef JCGO_PARALLEL
/* #include <windows.h> */
/* DWORD TlsAlloc(void); */
/* void *TlsGetValue(DWORD); */
/* BOOL TlsSetValue(DWORD, void *); */
#ifndef TLS_OUT_OF_INDEXES
#define TLS_OUT_OF_INDEXES ((DWORD)-1L)
#endif
#define JCGO_THREADLOCAL_VARDECL DWORD jcgo_threadLocal
#define JCGO_THREADLOCAL_INIT(x) ((jcgo_threadLocal = TlsAlloc()) != TLS_OUT_OF_INDEXES ? 0 : -1)
#define JCGO_THREADLOCAL_STORE(value) (TlsSetValue(jcgo_threadLocal, value) ? 0 : -1)
#define JCGO_THREADLOCAL_GET(pvalue) (*(pvalue) = TlsGetValue(jcgo_threadLocal), 0)
#endif

#else /* JCGO_WIN32 */

#ifdef JCGO_OS2

#ifndef JCGO_CREATHREAD
#ifndef _PROCESS_H
#include <process.h>
#endif
#endif

#ifndef _STDDEF_H
#include <stddef.h>
/* unsigned *_threadid; */
#endif

#ifndef _OS2_H
#define INCL_DOSERRORS
#define INCL_DOSEXCEPTIONS
#define INCL_DOSPROCESS
#define INCL_DOSSEMAPHORES
#include <os2.h>
/* APIRET DosCloseEventSem(HEV); */
/* APIRET DosCreateEventSem(PCSZ, HEV *, ULONG, BOOL32); */
/* APIRET DosCreateMutexSem(PCSZ, HMTX *, ULONG, BOOL32); */
/* APIRET DosPostEventSem(HEV); */
/* APIRET DosReleaseMutexSem(HMTX); */
/* APIRET DosRequestMutexSem(HMTX, ULONG); */
/* APIRET DosResetEventSem(HEV, ULONG *); */
/* APIRET DosSetPriority(ULONG, ULONG, LONG, ULONG); */
/* APIRET DosWaitEventSem(HEV, ULONG); */
#undef INCL_DOSERRORS
#undef INCL_DOSEXCEPTIONS
#undef INCL_DOSPROCESS
#undef INCL_DOSSEMAPHORES
#endif

#ifdef JCGO_CREATHREAD
/* #include <os2.h> */
/* APIRET DosCreateThread(TID *, void (APIENTRY *)(ULONG), ULONG, ULONG, ULONG); */
#define JCGO_THREAD_T TID
#define JCGO_THREAD_CREATE(pthread, rtn, stacksize, param) (DosCreateThread(pthread, rtn, (ULONG)(param), 0, (ULONG)(stacksize)) ? -1 : 0)
#define JCGO_THREAD_RTNDECL APIENTRY
#define JCGO_THREADPARAM_T ULONG
#else
#define JCGO_THREAD_T long
#ifdef _BEGINTHREAD_NO_STACKPTR
/* #include <process.h> */
/* int _beginthread(void (*)(void *), unsigned, void *); */
#define JCGO_THREAD_CREATE(pthread, rtn, stacksize, param) ((*(pthread) = (long)_beginthread(rtn, stacksize, param)) != -1L ? 0 : -1)
#else
/* #include <process.h> */
/* int _beginthread(void (*)(void *), void *, unsigned, void *); */
#define JCGO_THREAD_CREATE(pthread, rtn, stacksize, param) ((*(pthread) = (long)_beginthread(rtn, NULL, stacksize, param)) != -1L ? 0 : -1)
#endif
#ifndef JCGO_THREAD_RTNDECL
#ifdef _USERENTRY
#define JCGO_THREAD_RTNDECL _USERENTRY
#endif
#endif
#endif

#define JCGO_THREADRET_T void
#define JCGO_THREAD_RETVALUE /* empty */

#define JCGO_THREAD_SELF(pthread, pmainthread) ((*(pthread) = (long)(*_threadid)) != 0L ? 0 : -1)
#define JCGO_THREAD_DETACH(pthread) 0
#define JCGO_THREADPRIO_T int
#define JCGO_THREADPRIO_SET(pthread, psched, priority) (*(psched) = (priority), (*(psched) < (JCGO_JTHREAD_MINPRIO + JCGO_JTHREAD_MAXPRIO) / 2 ? DosSetPriority(PRTYS_THREAD, PRTYC_IDLETIME, (*(psched) - JCGO_JTHREAD_MINPRIO) * ((PRTYD_MAXIMUM + 1) / ((JCGO_JTHREAD_MAXPRIO - JCGO_JTHREAD_MINPRIO) / 2)), *(pthread)) : DosSetPriority(PRTYS_THREAD, PRTYC_REGULAR, ((*(psched) - (JCGO_JTHREAD_MINPRIO + JCGO_JTHREAD_MAXPRIO) / 2) * PRTYD_MAXIMUM) / ((JCGO_JTHREAD_MAXPRIO - JCGO_JTHREAD_MINPRIO + 1) / 2), *(pthread))) ? -1 : 0)
#define JCGO_MUTEX_T HMTX
#define JCGO_MUTEX_INIT(pmutex) (DosCreateMutexSem(NULL, pmutex, 0, 0) ? -1 : 0)
#define JCGO_MUTEX_LOCK(pmutex) (DosRequestMutexSem(*(pmutex), SEM_INDEFINITE_WAIT) ? -1 : 0)
#define JCGO_MUTEX_UNLOCK(pmutex) (DosReleaseMutexSem(*(pmutex)) ? -1 : 0)
#define JCGO_EVENT_T HEV
#define JCGO_EVENT_INIT(pevent) (DosCreateEventSem(NULL, pevent, 0, 0) ? -1 : 0)
#define JCGO_EVENT_CLEAR(pevent) (DosResetEventSem(*(pevent), (ULONG *)&jcgo_trashVar) == ERROR_INVALID_HANDLE ? -1 : 0)
#define JCGO_EVENT_SET(pevent) (DosPostEventSem(*(pevent)) ? -1 : 0)
#define JCGO_EVENT_WAIT(pevent) (DosWaitEventSem(*(pevent), SEM_INDEFINITE_WAIT) || DosResetEventSem(*(pevent), (ULONG *)&jcgo_trashVar) == ERROR_INVALID_HANDLE ? -1 : 0)
#define JCGO_EVENT_TIMEDWAIT(pevent, pwaittime) ((unsigned)(DosWaitEventSem(*(pevent), (ULONG)(*(pwaittime))) - 1) < (unsigned)(ERROR_TIMEOUT - 1) || DosResetEventSem(*(pevent), (ULONG *)&jcgo_trashVar) == ERROR_INVALID_HANDLE ? -1 : 0)
#define JCGO_EVENT_DESTROY(pevent) (DosCloseEventSem(*(pevent)) ? -1 : 0)
#define JCGO_EVENTTIME_T long
#define JCGO_EVENTTIME_PREPARE(pwaittime, timeout, ns) (*(pwaittime) = ((timeout) != 0L ? (timeout) : 1L), 0)

#ifndef JCGO_THREAD_YIELD
/* #include <os2.h> */
/* APIRET DosSleep(ULONG); */
#define JCGO_THREAD_YIELD (void)DosSleep(1)
#endif

#ifdef JCGO_PARALLEL
#ifdef JCGO_UNIX
/* #include <stdlib.h> */
/* void **_threadstore(void); */
#define JCGO_THREADLOCAL_VARDECL int jcgo_threadLocal
#define JCGO_THREADLOCAL_INIT(x) (_threadstore() != NULL ? (jcgo_threadLocal = 0) : -1)
#define JCGO_THREADLOCAL_STORE(value) (*_threadstore() = (value), 0)
#define JCGO_THREADLOCAL_GET(pvalue) (*(pvalue) = *_threadstore(), 0)
#else
#define JCGO_THREADLOCAL_VARDECL __declspec(thread) long jcgo_threadLocal
#define JCGO_THREADLOCAL_INIT(x) 0
#define JCGO_THREADLOCAL_STORE(value) (jcgo_threadLocal = (long)(value), 0)
#define JCGO_THREADLOCAL_GET(pvalue) (*(pvalue) = (void *)jcgo_threadLocal, 0)
#endif
#endif

#else /* JCGO_OS2 */

#ifdef JCGO_SOLTHR

#ifndef _SYS_TIME_H
#include <sys/time.h>
#endif

#ifdef JCGO_CLOCKGETTM
#include <time.h>
/* int clock_gettime(clockid_t, struct timespec *); */
#endif

#ifndef _THREAD_H
#include <thread.h>
/* int thr_create(void *, size_t, void *(*)(void *), void *, long, thread_t *); */
/* thread_t thr_self(void); */
/* int thr_setprio(thread_t, int); */
#endif

#ifndef _ERRNO_H
#include <errno.h>
#endif

#ifndef _SYNCH_H
#include <synch.h>
/* int cond_destroy(cond_t *); */
/* int cond_init(cond_t *, int, void *); */
/* int cond_signal(cond_t *); */
/* int cond_timedwait(cond_t *, mutex_t *, const timestruc_t *); */
/* int cond_wait(cond_t *, mutex_t *); */
/* int mutex_destroy(mutex_t *); */
/* int mutex_init(mutex_t *, int, void *); */
/* int mutex_lock(mutex_t *); */
/* int mutex_unlock(mutex_t *); */
#endif

#ifndef JCGO_PTHREADSETPRIO_MAX
#define JCGO_PTHREADSETPRIO_MAX 126
#endif

#define JCGO_THREAD_T thread_t
#define JCGO_THREAD_CREATE(pthread, rtn, stacksize, param) thr_create(NULL, stacksize, rtn, param, THR_DETACHED, pthread)
#define JCGO_THREAD_SELF(pthread, pmainthread) ((*(pthread) = thr_self()) != (thread_t)0 ? 0 : -1)
#define JCGO_THREAD_DETACH(pthread) 0
#define JCGO_THREADPRIO_T int
#define JCGO_THREADPRIO_SET(pthread, psched, priority) (*(psched) = (priority) <= (JCGO_JTHREAD_MINPRIO + JCGO_JTHREAD_MAXPRIO) / 2 ? ((priority) - JCGO_JTHREAD_MINPRIO) * ((JCGO_PTHREADSETPRIO_MAX / 2 + 1) / ((JCGO_JTHREAD_MAXPRIO - JCGO_JTHREAD_MINPRIO - 1) / 2)) : (((priority) - (JCGO_JTHREAD_MINPRIO - 1)) * (JCGO_PTHREADSETPRIO_MAX - 1) + ((JCGO_JTHREAD_MAXPRIO * 3 - JCGO_JTHREAD_MINPRIO) / 2 + 1)) / (JCGO_JTHREAD_MAXPRIO - JCGO_JTHREAD_MINPRIO + 1), thr_setprio(*(pthread), *(psched)))
#define JCGO_MUTEX_T mutex_t
#define JCGO_MUTEX_INIT(pmutex) mutex_init(pmutex, USYNC_THREAD, NULL)
#define JCGO_MUTEX_LOCK(pmutex) mutex_lock(pmutex)
#define JCGO_MUTEX_UNLOCK(pmutex) mutex_unlock(pmutex)
#define JCGO_EVENT_T struct { mutex_t mutex; cond_t cond; int signaled; }
#define JCGO_EVENT_INIT(pevent) (mutex_init(&(pevent)->mutex, USYNC_THREAD, NULL) ? -1 : cond_init(&(pevent)->cond, USYNC_THREAD, NULL) ? (mutex_destroy(&(pevent)->mutex), -1) : (int)((pevent)->signaled = 0))
#define JCGO_EVENT_CLEAR(pevent) (mutex_lock(&(pevent)->mutex) ? -1 : ((pevent)->signaled = 0, mutex_unlock(&(pevent)->mutex)))
#define JCGO_EVENT_SET(pevent) (JCGO_EXPECT_FALSE(mutex_lock(&(pevent)->mutex) != 0) ? -1 : ((pevent)->signaled = 1, cond_signal(&(pevent)->cond), mutex_unlock(&(pevent)->mutex)))
#define JCGO_EVENT_WAIT(pevent) (JCGO_EXPECT_FALSE(mutex_lock(&(pevent)->mutex) != 0) ? 0 : (pevent)->signaled || (cond_wait(&(pevent)->cond, &(pevent)->mutex), JCGO_EXPECT_TRUE((pevent)->signaled)) ? ((pevent)->signaled = 0, mutex_unlock(&(pevent)->mutex), 0) : (mutex_unlock(&(pevent)->mutex), -1))
#define JCGO_EVENT_TIMEDWAIT(pevent, pwaittime) (JCGO_EXPECT_FALSE(mutex_lock(&(pevent)->mutex) != 0) ? 0 : (pevent)->signaled || cond_timedwait(&(pevent)->cond, &(pevent)->mutex, &(pwaittime)->ts) == ETIMEDOUT || *(volatile int *)&(pevent)->signaled != 0 ? ((pevent)->signaled = 0, mutex_unlock(&(pevent)->mutex), 0) : (mutex_unlock(&(pevent)->mutex), -1))
#define JCGO_EVENT_DESTROY(pevent) (cond_destroy(&(pevent)->cond), mutex_destroy(&(pevent)->mutex))

#ifdef JCGO_CLOCKGETTM
#define JCGO_EVENTTIME_T struct { timestruc_t ts; }
#define JCGO_EVENTTIME_PREPARE(pwaittime, timeout, ns) (((timeout) | (ns)) > 0L ? (clock_gettime(CLOCK_REALTIME, &(pwaittime)->ts), (pwaittime)->ts.tv_sec += (time_t)((timeout) / 1000L), ((pwaittime)->ts.tv_nsec += ((timeout) % 1000L) * (1000L * 1000L) + (ns)) >= 1000L * 1000L * 1000L ? ((pwaittime)->ts.tv_sec++, (pwaittime)->ts.tv_nsec -= 1000L * 1000L * 1000L, 0) : 0) : ((pwaittime)->ts.tv_sec = 0, (pwaittime)->ts.tv_nsec = 0, 0))
#else
#define JCGO_EVENTTIME_T struct { timestruc_t ts; struct timeval tv; }
#ifdef _SVID_GETTOD
/* #include <sys/time.h> */
/* int gettimeofday(struct timeval *); */
#define JCGO_EVENTTIME_GETTOD(ptv) gettimeofday((void *)(ptv))
#else
/* #include <sys/time.h> */
/* int gettimeofday(struct timeval *, void *); */
#define JCGO_EVENTTIME_GETTOD(ptv) gettimeofday((void *)(ptv), NULL)
#endif
#define JCGO_EVENTTIME_PREPARE(pwaittime, timeout, ns) (((timeout) | (ns)) > 0L ? (JCGO_EVENTTIME_GETTOD(&(pwaittime)->tv), (pwaittime)->ts.tv_sec = (time_t)((pwaittime)->tv.tv_sec + (timeout) / 1000L), ((pwaittime)->ts.tv_nsec = (((timeout) % 1000L) * 1000L + (pwaittime)->tv.tv_usec) * 1000L + (ns)) >= 1000L * 1000L * 1000L ? ((pwaittime)->ts.tv_sec++, (pwaittime)->ts.tv_nsec -= 1000L * 1000L * 1000L, 0) : 0) : ((pwaittime)->ts.tv_sec = 0, (pwaittime)->ts.tv_nsec = 0, 0))
#endif

#ifndef JCGO_THREAD_YIELD
/* #include <thread.h> */
/* void thr_yield(void); */
#define JCGO_THREAD_YIELD thr_yield()
#endif

#ifdef JCGO_PARALLEL
/* #include <thread.h> */
/* int thr_getspecific(thread_key_t, void **); */
/* int thr_keycreate(thread_key_t *, void (*)(void *)); */
/* int thr_setspecific(thread_key_t, void *); */
#define JCGO_THREADLOCAL_VARDECL thread_key_t jcgo_threadLocal
#define JCGO_THREADLOCAL_INIT(x) thr_keycreate(&jcgo_threadLocal, 0)
#define JCGO_THREADLOCAL_STORE(value) thr_setspecific(jcgo_threadLocal, value)
#define JCGO_THREADLOCAL_GET(pvalue) thr_getspecific(jcgo_threadLocal, pvalue)
#endif

#else /* JCGO_SOLTHR */

#ifdef JCGO_UNIX
#ifndef _SYS_TIME_H
#include <sys/time.h>
#endif
#else
#ifndef _SYS_TIMEB_H
#include <sys/timeb.h>
/* void ftime(struct timeb *); */
#endif
#endif

#ifdef JCGO_MONOTWAIT
#include <time.h>
/* int clock_gettime(clockid_t, struct timespec *); */
#else
#ifdef JCGO_CLOCKGETTM
#include <time.h>
/* int clock_gettime(clockid_t, struct timespec *); */
#endif
#endif

#ifndef _PTHREAD_H
#include <pthread.h>
/* int pthread_cond_destroy(pthread_cond_t *); */
/* int pthread_cond_init(pthread_cond_t *, const pthread_condattr_t *); */
/* int pthread_cond_signal(pthread_cond_t *); */
/* int pthread_cond_wait(pthread_cond_t *, pthread_mutex_t *); */
/* int pthread_create(pthread_t *, const pthread_attr_t *, void *(*)(void *), void *); */
/* int pthread_mutex_destroy(pthread_mutex_t *); */
/* int pthread_mutex_init(pthread_mutex_t *, const pthread_mutexattr_t *); */
/* int pthread_mutex_lock(pthread_mutex_t *); */
/* int pthread_mutex_unlock(pthread_mutex_t *); */
/* pthread_t pthread_self(void); */
#endif

#ifndef _ERRNO_H
#include <errno.h>
#endif

#define JCGO_THREAD_T pthread_t

#define JCGO_THREADT_CLEAR(pthread) (JCGO_MEM_ZERO(pthread, sizeof(pthread_t)), 0)
#define JCGO_THREADT_COPY(pthread, psrcthread) (JCGO_MEM_CPY(pthread, psrcthread, sizeof(pthread_t)), 0)

#ifndef _PTHREAD_NO_PTHREAD_EQUAL
/* #include <pthread.h> */
/* int pthread_equal(pthread_t, pthread_t); */
#define JCGO_THREADT_ISEQUALIDENT(pthread, psrcthread) pthread_equal(*(pthread), *(psrcthread))
#endif

#ifndef JCGO_THREADT_ISVALID
#define JCGO_THREADT_ISVALID(pthread) (sizeof(pthread_t) > sizeof(long) ? *(volatile jlong *)(pthread) != 0 : sizeof(pthread_t) > sizeof(int) ? *(volatile long *)(pthread) != 0 : *(volatile int *)(pthread) != 0)
#endif

#ifdef pthread_attr_default
#define JCGO_THREAD_CREATE(pthread, rtn, stacksize, param) pthread_create(pthread, pthread_attr_default, rtn, param)
#else
#define JCGO_THREAD_CREATE(pthread, rtn, stacksize, param) pthread_create(pthread, NULL, rtn, param)
#endif

#ifndef JCGO_THREAD_RTNDECL
#ifdef PTHREAD_USERCALL
#define JCGO_THREAD_RTNDECL PTHREAD_USERCALL
#endif
#endif

#define JCGO_THREAD_SELF(pthread, pmainthread) ((*(pthread) = pthread_self()) != (pthread_t)0 ? 0 : -1)

#ifdef __EMX__
/* #include <pthread.h> */
/* int pthread_detach(pthread_t *); */
#define JCGO_THREAD_DETACH(pthread) pthread_detach(pthread)
#ifndef JCGO_PTHREADSETPRIO_MAX
#define JCGO_PTHREADSETPRIO_MAX 126
#endif
#else
/* #include <pthread.h> */
/* int pthread_detach(pthread_t); */
#define JCGO_THREAD_DETACH(pthread) pthread_detach(*(pthread))
#endif

#ifndef JCGO_THREAD_YIELD
#ifdef pthread_attr_default
/* #include <pthread.h> */
/* void pthread_yield(void); */
#define JCGO_THREAD_YIELD pthread_yield()
#else
/* #include <pthread.h> */
/* int sched_yield(void); */
#define JCGO_THREAD_YIELD (void)sched_yield()
#endif
#endif

#ifdef JCGO_PTHREADSETPRIO_MAX
/* #include <pthread.h> */
/* int pthread_setprio(pthread_t, int); */
#define JCGO_THREADPRIO_T int
#define JCGO_THREADPRIO_SET(pthread, psched, priority) (*(psched) = (priority) <= (JCGO_JTHREAD_MINPRIO + JCGO_JTHREAD_MAXPRIO) / 2 ? ((priority) - JCGO_JTHREAD_MINPRIO) * ((JCGO_PTHREADSETPRIO_MAX / 2 + 1) / ((JCGO_JTHREAD_MAXPRIO - JCGO_JTHREAD_MINPRIO - 1) / 2)) : (((priority) - (JCGO_JTHREAD_MINPRIO - 1)) * (JCGO_PTHREADSETPRIO_MAX - 1) + ((JCGO_JTHREAD_MAXPRIO * 3 - JCGO_JTHREAD_MINPRIO) / 2 + 1)) / (JCGO_JTHREAD_MAXPRIO - JCGO_JTHREAD_MINPRIO + 1), pthread_setprio(*(pthread), *(psched)))
#else
#ifdef _PTHREAD_NO_SETSCHEDPARAM
#define JCGO_THREADPRIO_T int
#define JCGO_THREADPRIO_SET(pthread, psched, priority) (*(psched) = 0, *(psched))
#else
/* #include <pthread.h> */
/* int pthread_getschedparam(pthread_t, int *, struct sched_param *); */
/* int pthread_setschedparam(pthread_t, int, const struct sched_param *); */
/* #include <sched.h> */
/* int sched_get_priority_max(int); */
/* int sched_get_priority_min(int); */
#define JCGO_THREADPRIO_T struct { struct sched_param param; int policy; int minprio; int maxprio; }
#define JCGO_THREADPRIO_SET(pthread, psched, priority) ((psched)->policy = 0, pthread_getschedparam(*(pthread), &(psched)->policy, &(psched)->param) || ((psched)->minprio = sched_get_priority_min((psched)->policy)) == -1 || ((psched)->maxprio = sched_get_priority_max((psched)->policy)) == -1 ? -1 : ((psched)->param.sched_priority = ((priority) <= (JCGO_JTHREAD_MINPRIO + JCGO_JTHREAD_MAXPRIO) / 2 ? (((priority) - JCGO_JTHREAD_MINPRIO) * ((psched)->maxprio - (psched)->minprio)) / ((JCGO_JTHREAD_MAXPRIO - JCGO_JTHREAD_MINPRIO) - 1) : (((priority) - (JCGO_JTHREAD_MINPRIO - 1)) * ((psched)->maxprio - (psched)->minprio)) / (JCGO_JTHREAD_MAXPRIO - JCGO_JTHREAD_MINPRIO + 1)) + (psched)->minprio, pthread_setschedparam(*(pthread), (psched)->policy, &(psched)->param)))
#endif
#endif

#ifdef pthread_condattr_default
#define JCGO_CONDINIT_DEFATTR pthread_condattr_default
#else
#define JCGO_CONDINIT_DEFATTR NULL
#endif

#ifdef pthread_mutexattr_default
#define JCGO_MUTEXINIT_DEFATTR pthread_mutexattr_default
#else
#define JCGO_MUTEXINIT_DEFATTR NULL
#endif

#define JCGO_MUTEX_T pthread_mutex_t
#define JCGO_MUTEX_INIT(pmutex) pthread_mutex_init(pmutex, JCGO_MUTEXINIT_DEFATTR)
#define JCGO_MUTEX_LOCK(pmutex) pthread_mutex_lock(pmutex)
#define JCGO_MUTEX_UNLOCK(pmutex) pthread_mutex_unlock(pmutex)
#define JCGO_EVENT_T struct { pthread_mutex_t mutex; pthread_cond_t cond; int signaled; }
#define JCGO_EVENT_INIT(pevent) (pthread_mutex_init(&(pevent)->mutex, JCGO_MUTEXINIT_DEFATTR) ? -1 : pthread_cond_init(&(pevent)->cond, JCGO_CONDINIT_DEFATTR) ? (pthread_mutex_destroy(&(pevent)->mutex), -1) : (int)((pevent)->signaled = 0))
#define JCGO_EVENT_CLEAR(pevent) (pthread_mutex_lock(&(pevent)->mutex) ? -1 : ((pevent)->signaled = 0, pthread_mutex_unlock(&(pevent)->mutex)))
#define JCGO_EVENT_SET(pevent) (JCGO_EXPECT_FALSE(pthread_mutex_lock(&(pevent)->mutex) != 0) ? -1 : ((pevent)->signaled = 1, pthread_cond_signal(&(pevent)->cond), pthread_mutex_unlock(&(pevent)->mutex)))
#define JCGO_EVENT_WAIT(pevent) (JCGO_EXPECT_FALSE(pthread_mutex_lock(&(pevent)->mutex) != 0) ? 0 : (pevent)->signaled || (pthread_cond_wait(&(pevent)->cond, &(pevent)->mutex), JCGO_EXPECT_TRUE((pevent)->signaled)) ? ((pevent)->signaled = 0, pthread_mutex_unlock(&(pevent)->mutex), 0) : (pthread_mutex_unlock(&(pevent)->mutex), -1))
#define JCGO_EVENT_DESTROY(pevent) (pthread_cond_destroy(&(pevent)->cond), pthread_mutex_destroy(&(pevent)->mutex))

#ifdef JCGO_MONOTWAIT
/* #include <pthread.h> */
/* int pthread_cond_timedwait_monotonic_np(pthread_cond_t *, pthread_mutex_t *, const struct timespec *); */
#define JCGO_EVENTTIME_T struct timespec
#define JCGO_EVENTTIME_PREPARE(pwaittime, timeout, ns) (((timeout) | (ns)) > 0L ? (clock_gettime(CLOCK_MONOTONIC, pwaittime), (pwaittime)->tv_sec += (time_t)((timeout) / 1000L), ((pwaittime)->tv_nsec += ((timeout) % 1000L) * (1000L * 1000L) + (ns)) >= 1000L * 1000L * 1000L ? ((pwaittime)->tv_sec++, (pwaittime)->tv_nsec -= 1000L * 1000L * 1000L, 0) : 0) : ((pwaittime)->tv_sec = 0, (pwaittime)->tv_nsec = 0, 0))
#define JCGO_EVENT_CONDTMWAIT(pcond, pmutex, pwaittime) pthread_cond_timedwait_monotonic_np(pcond, pmutex, pwaittime)
#else
/* #include <pthread.h> */
/* int pthread_cond_timedwait(pthread_cond_t *, pthread_mutex_t *, const struct timespec *); */
#ifdef JCGO_CLOCKGETTM
#define JCGO_EVENTTIME_T struct timespec
#define JCGO_EVENTTIME_PREPARE(pwaittime, timeout, ns) (((timeout) | (ns)) > 0L ? (clock_gettime(CLOCK_REALTIME, pwaittime), (pwaittime)->tv_sec += (time_t)((timeout) / 1000L), ((pwaittime)->tv_nsec += ((timeout) % 1000L) * (1000L * 1000L) + (ns)) >= 1000L * 1000L * 1000L ? ((pwaittime)->tv_sec++, (pwaittime)->tv_nsec -= 1000L * 1000L * 1000L, 0) : 0) : ((pwaittime)->tv_sec = 0, (pwaittime)->tv_nsec = 0, 0))
#define JCGO_EVENT_CONDTMWAIT(pcond, pmutex, pwaittime) pthread_cond_timedwait(pcond, pmutex, pwaittime)
#else
#ifdef JCGO_UNIX
#define JCGO_EVENTTIME_T struct { struct timespec ts; struct timeval tv; }
#ifdef _SVID_GETTOD
/* #include <sys/time.h> */
/* int gettimeofday(struct timeval *); */
#define JCGO_EVENTTIME_GETTOD(ptv) gettimeofday((void *)(ptv))
#else
/* #include <sys/time.h> */
/* int gettimeofday(struct timeval *, struct timezone *); */
#define JCGO_EVENTTIME_GETTOD(ptv) gettimeofday((void *)(ptv), NULL)
#endif
#define JCGO_EVENTTIME_PREPARE(pwaittime, timeout, ns) (((timeout) | (ns)) > 0L ? (JCGO_EVENTTIME_GETTOD(&(pwaittime)->tv), (pwaittime)->ts.tv_sec = (time_t)((pwaittime)->tv.tv_sec + (timeout) / 1000L), ((pwaittime)->ts.tv_nsec = (((timeout) % 1000L) * 1000L + (pwaittime)->tv.tv_usec) * 1000L + (ns)) >= 1000L * 1000L * 1000L ? ((pwaittime)->ts.tv_sec++, (pwaittime)->ts.tv_nsec -= 1000L * 1000L * 1000L, 0) : 0) : ((pwaittime)->ts.tv_sec = 0, (pwaittime)->ts.tv_nsec = 0, 0))
#else
#define JCGO_EVENTTIME_T struct { struct timespec ts; struct timeb tb; }
#define JCGO_EVENTTIME_PREPARE(pwaittime, timeout, ns) (((timeout) | (ns)) > 0L ? (ftime(&(pwaittime)->tb), (pwaittime)->ts.tv_sec = (time_t)((pwaittime)->tb.time + (timeout) / 1000L), ((pwaittime)->ts.tv_nsec = ((timeout) % 1000L + (pwaittime)->tb.millitm) * (1000L * 1000L) + (ns)) >= 1000L * 1000L * 1000L ? ((pwaittime)->ts.tv_sec++, (pwaittime)->ts.tv_nsec -= 1000L * 1000L * 1000L, 0) : 0) : ((pwaittime)->ts.tv_sec = 0, (pwaittime)->ts.tv_nsec = 0, 0))
#endif
#define JCGO_EVENT_CONDTMWAIT(pcond, pmutex, pwaittime) pthread_cond_timedwait(pcond, pmutex, &(pwaittime)->ts)
#endif
#endif

#define JCGO_EVENT_TIMEDWAIT(pevent, pwaittime) (JCGO_EXPECT_FALSE(pthread_mutex_lock(&(pevent)->mutex) != 0) ? 0 : (pevent)->signaled || JCGO_EVENT_CONDTMWAIT(&(pevent)->cond, &(pevent)->mutex, pwaittime) == ETIMEDOUT || *(volatile int *)&(pevent)->signaled != 0 ? ((pevent)->signaled = 0, pthread_mutex_unlock(&(pevent)->mutex), 0) : (pthread_mutex_unlock(&(pevent)->mutex), -1))

#ifdef JCGO_PARALLEL
/* #include <pthread.h> */
/* void *pthread_getspecific(pthread_key_t); */
/* int pthread_key_create(pthread_key_t *, void (*)(void *)); */
/* int pthread_setspecific(pthread_key_t, const void *); */
#define JCGO_THREADLOCAL_VARDECL pthread_key_t jcgo_threadLocal
#define JCGO_THREADLOCAL_INIT(x) pthread_key_create(&jcgo_threadLocal, 0)
#define JCGO_THREADLOCAL_STORE(value) pthread_setspecific(jcgo_threadLocal, value)
#define JCGO_THREADLOCAL_GET(pvalue) (*(pvalue) = pthread_getspecific(jcgo_threadLocal), 0)
#endif

#ifdef pthread_suspend_all_np
/* #include <pthread.h> */
/* void pthread_resume_all_np(void); */
/* void pthread_suspend_all_np(void); */
#define JCGO_SCHED_STOP(x) (pthread_suspend_all_np(), 0)
#define JCGO_SCHED_RESUME(x) (pthread_resume_all_np(), 0)
#endif

#ifdef pthread_num_processors_np
#ifndef JCGO_NPROCS_STMT
/* #include <pthread.h> */
/* int pthread_num_processors_np(void); */
#define JCGO_NPROCS_STMT(pdata) { *(pdata) = pthread_num_processors_np(); }
#endif
#endif

#endif /* ! JCGO_SOLTHR */

#endif /* ! JCGO_OS2 */

#define JCGO_THREAD_IDENTSELF(pthread) JCGO_THREAD_SELF(pthread, pthread)
#define JCGO_THREAD_STOREIDENT(pthread) 0
#define JCGO_THREAD_CLOSEHND(pthread) 0

#ifndef JCGO_THREADT_CLEAR
#define JCGO_THREADT_CLEAR(pthread) ((*(pthread) = 0), 0)
#define JCGO_THREADT_ISVALID(pthread) (*(JCGO_THREAD_T volatile *)(pthread) != 0)
#define JCGO_THREADT_COPY(pthread, psrcthread) (*(pthread) = *(psrcthread), 0)
#endif

#ifndef JCGO_THREADT_ISEQUALIDENT
#define JCGO_THREADT_ISEQUALIDENT(pthread, psrcthread) (*(pthread) == *(psrcthread))
#endif

#ifdef JCGO_UNIX
#ifndef JCGO_NPROCS_STMT
/* #include <unistd.h> */
/* long sysconf(int); */
#ifdef _SC_NPROCESSORS_ONLN
#define JCGO_NPROCS_STMT(pdata) { *(pdata) = (int)sysconf(_SC_NPROCESSORS_ONLN); }
#else
#ifdef _SC_NPROC_ONLN
#define JCGO_NPROCS_STMT(pdata) { *(pdata) = (int)sysconf(_SC_NPROC_ONLN); }
#endif
#endif
#endif
#endif

#endif /* ! JCGO_WIN32 */

#ifndef JCGO_NPROCS_T
#define JCGO_NPROCS_T int
#define JCGO_NPROCS_GET(pdata) (*(pdata))
#endif

#ifndef JCGO_NPROCS_STMT
#define JCGO_NPROCS_STMT(pdata) { *(pdata) = 0; }
#endif

#ifndef JCGO_THREADRET_T
#define JCGO_THREADRET_T void*
#endif

#ifndef JCGO_THREADPARAM_T
#define JCGO_THREADPARAM_T void*
#endif

#ifndef JCGO_THREAD_RTNDECL
#define JCGO_THREAD_RTNDECL CLIBDECL
#endif

#ifndef JCGO_THREAD_RETVALUE
#define JCGO_THREAD_RETVALUE ((JCGO_THREADRET_T)0)
#endif

#ifndef JCGO_SCHED_STOP
#define JCGO_SCHED_STOP(x) 0
#define JCGO_SCHED_RESUME(x) 0
#endif

#ifndef JCGO_EVENTHIGHRESCONFVAR_DEFN
#define JCGO_EVENTHIGHRESCONFVAR_DEFN /* empty */
#define JCGO_EVENTHIGHRESCONFVAR_INITSTMT { (void)0; }
#endif

#endif
