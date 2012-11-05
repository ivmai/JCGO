/*
 * @(#) $(JCGO)/include/jcgothrd.c --
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
 * Used control macros: GC_NO_THREAD_REDIRECTS, JCGO_CVOLATILE,
 * JCGO_NOCREATJVM, JCGO_NOFATALMSG, JCGO_NOGC, JCGO_NOJNI, JCGO_NOMTCLIB,
 * JCGO_PARALLEL, JCGO_SEHTRY, JCGO_STDCLINIT, JCGO_WIN32.
 * Macros for tuning: GC_CALLBACK, THREADSINIT, THREADSTACKSZ.
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
/* void exit(int); */

#include "jcgothrd.h"

#define JCGO_GETMON_OF(obj) ((struct jcgo_tcb_s *)(*(void *volatile *)&JCGO_FIELD_NZACCESS(obj, jcgo_mon)))

#ifdef JCGO_NOGC
#define JCGO_THRDGC_OMITREGNORMAL
#else
#ifdef GC_NO_THREAD_REDIRECTS
#define JCGO_THRDGC_UNREGNEEDED
#else
#define JCGO_THRDGC_OMITREGNORMAL
#ifndef JCGO_NOCREATJVM
#define JCGO_THRDGC_UNREGNEEDED
#endif
#endif
#endif

struct jcgo_tcb_s
{
 jvtable jcgo_methods;
 JCGO_MON_DEFN
 JNIEnv jniEnv;
 jObject thread;
 jObject nativeExc;
#ifndef JCGO_NOJNI
 jObjectArr localObjs;
#endif
#ifdef JCGO_SEHTRY
 jObject throwable;
#ifndef JCGO_NOCREATJVM
 unsigned insideJniCall;
#endif
#else
 struct jcgo_try_s *pCurTry;
#endif
 unsigned insideCallback;
 int interruptReq;
 int suspended;
 int waitsleep;
 jObject monObj;
 struct jcgo_tcb_s *tcbMonOwner;
 struct jcgo_tcb_s *tcbWaitNext;
#ifndef JCGO_SEHTRY
 struct jcgo_curmon_s *pCurMon;
#endif
 jObject stopExc;
#ifndef JCGO_NOJNI
 jObjectArr jniLockedObjs;
 jObjectArr overlockedObjsList;
#endif
#ifdef JCGO_THRDGC_UNREGNEEDED
 int gcAttached;
#endif
 JCGO_THREAD_T thrhandle;
 JCGO_EVENT_T event;
#ifdef JCGO_PARALLEL
 JCGO_EVENT_T resumeEvent;
#else
#ifndef JCGO_NOJNI
 struct jcgo_tcb_s *tcbList;
#endif
#endif
#ifdef JCGO_SEHTRY
#ifdef OBJT_java_lang_VMThrowable
 jmp_buf jbuf;
#endif
#endif
};

JCGO_NOSEP_GCDATA struct jcgo_tcb_s jcgo_mainTCB ATTRIBGCBSS = { NULL };

JCGO_EVENTHIGHRESCONFVAR_DEFN

JCGO_MUTEX_T jcgo_nonParallelMutex;

#ifndef JCGO_NOFATALMSG
STATIC void CFASTCALL jcgo_printFatalMsg( CONST char *msg );
#endif

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_setSyncSignals( void );

#ifdef JCGO_PARALLEL

#ifndef JCGO_MONCRIT_HASHLOGSZ
#define JCGO_MONCRIT_HASHLOGSZ 4
#endif

#define JCGO_MUTEXIND_HASHF(val, logsz) (((int)((val) >> (((logsz) << 1) + 3)) ^ (int)((val) >> ((logsz) + 3)) ^ (int)((val) >> 3)) & ((1 << (logsz)) - 1))

#define JCGO_MONCRIT_BEGIN(addr) { int jcgo_monMutexInd = JCGO_MUTEXIND_HASHF(JCGO_CAST_PTRTONUM(addr), JCGO_MONCRIT_HASHLOGSZ); (void)JCGO_MUTEX_LOCK(&jcgo_monCritMutexes[jcgo_monMutexInd]); {
#define JCGO_MONCRIT_END } (void)JCGO_MUTEX_UNLOCK(&jcgo_monCritMutexes[jcgo_monMutexInd]); }

#define JCGO_CRITMOD_BEGIN(mutexvar) { (void)JCGO_MUTEX_LOCK(&mutexvar); {
#define JCGO_CRITMOD_END(mutexvar) } (void)JCGO_MUTEX_UNLOCK(&mutexvar); }

#ifdef OBJT_java_lang_ref_SoftReference
#define JCGO_MEMREFGDAT_LOCK(x) (void)JCGO_MUTEX_LOCK(&jcgo_softRefMutex)
#define JCGO_MEMREFGDAT_UNLOCK(x) (void)JCGO_MUTEX_UNLOCK(&jcgo_softRefMutex)
#endif

#define JCGO_GET_CURTCB(ptcb) (void)JCGO_THREADLOCAL_GET((void **)(ptcb))

JCGO_THREADLOCAL_VARDECL;

JCGO_MUTEX_T jcgo_monCritMutexes[1 << JCGO_MONCRIT_HASHLOGSZ];

#ifndef JCGO_NOJNI

JCGO_MUTEX_T jcgo_jniAllocDataMutex;

JCGO_MUTEX_T jcgo_jniGlobalRefsMutex;

#ifndef JCGO_FNLZDATA_OMITREFQUE
JCGO_MUTEX_T jcgo_jniWeakRefsMutex;
#endif

JCGO_MUTEX_T jcgo_jniMiscAttachMutex;

#endif /* ! JCGO_NOJNI */

#ifdef JCGO_STDCLINIT
JCGO_MUTEX_T jcgo_clinitListMutex;
#endif

#ifdef OBJT_java_lang_ref_SoftReference
JCGO_MUTEX_T jcgo_softRefMutex;
#endif

#ifdef JCGO_NOCTRLC
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL
#else
STATIC void CFASTCALL
#endif
jcgo_checkStop( struct jcgo_tcb_s *tcb );

#ifndef JCGO_CVOLATILE
JCGO_NOSEP_INLINE int JCGO_INLFRW_FASTCALL jcgo_atomicOpsInit( void );
#endif

JCGO_NOSEP_INLINE int CFASTCALL jcgo_monCritInit( void )
{
 int i = 1 << JCGO_MONCRIT_HASHLOGSZ;
 while (i-- > 0)
  if (JCGO_MUTEX_INIT(&jcgo_monCritMutexes[i]))
   return -1;
 return 0;
}

#else /* JCGO_PARALLEL */

#define JCGO_MONCRIT_BEGIN(addr) {
#define JCGO_MONCRIT_END }

#define JCGO_CRITMOD_BEGIN(mutexvar) {
#define JCGO_CRITMOD_END(mutexvar) }

#define JCGO_GET_CURTCB(ptcb) (void)(*(void **)(ptcb) = (void *)jcgo_curTCB)

STATICDATA struct jcgo_tcb_s *jcgo_curTCB = NULL;

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_checkStop(
 struct jcgo_tcb_s *tcb );

#endif /* ! JCGO_PARALLEL */

#ifdef JCGO_WIN32

STATIC int CFASTCALL jcgo_win32BlockOnMutex( JCGO_MUTEX_T *pmutex )
{
 while (InterlockedExchange(&pmutex->state, -1))
  if (WaitForSingleObject(pmutex->event, INFINITE) == WAIT_FAILED)
   return -1;
 return 0;
}

#endif /* JCGO_WIN32 */

STATIC int CFASTCALL jcgo_threadInitHandle( struct jcgo_tcb_s *tcb )
{
 if (JCGO_EVENT_INIT(&tcb->event))
  return -1;
#ifdef JCGO_PARALLEL
 if (JCGO_EVENT_INIT(&tcb->resumeEvent))
 {
  (void)JCGO_EVENT_DESTROY(&tcb->event);
  return -1;
 }
#endif
 if (JCGO_THREAD_SELF(&tcb->thrhandle, &jcgo_mainTCB.thrhandle))
 {
#ifdef JCGO_PARALLEL
  (void)JCGO_EVENT_DESTROY(&tcb->resumeEvent);
#endif
  (void)JCGO_EVENT_DESTROY(&tcb->event);
  return -1;
 }
 return 0;
}

JCGO_NOSEP_INLINE int CFASTCALL jcgo_threadsInit( void )
{
 struct jcgo_tcb_s *tcb;
#ifdef THREADSINIT
 THREADSINIT;
#endif
 tcb = &jcgo_mainTCB;
 JCGO_EVENTHIGHRESCONFVAR_INITSTMT
 if (!jcgo_threadInitHandle(tcb))
 {
  if (!JCGO_MUTEX_INIT(&jcgo_nonParallelMutex))
  {
#ifdef JCGO_THRDGC_UNREGNEEDED
   GC_allow_register_threads();
#endif
#ifdef JCGO_PARALLEL
   if (!JCGO_THREADLOCAL_INIT(0) &&
#ifndef JCGO_NOJNI
       !JCGO_MUTEX_INIT(&jcgo_jniAllocDataMutex) &&
       !JCGO_MUTEX_INIT(&jcgo_jniGlobalRefsMutex) &&
#ifndef JCGO_FNLZDATA_OMITREFQUE
       !JCGO_MUTEX_INIT(&jcgo_jniWeakRefsMutex) &&
#endif
       !JCGO_MUTEX_INIT(&jcgo_jniMiscAttachMutex) &&
#endif
#ifdef JCGO_STDCLINIT
       !JCGO_MUTEX_INIT(&jcgo_clinitListMutex) &&
#endif
#ifdef OBJT_java_lang_ref_SoftReference
       !JCGO_MUTEX_INIT(&jcgo_softRefMutex) &&
#endif
#ifndef JCGO_CVOLATILE
       !jcgo_atomicOpsInit() &&
#endif
       !jcgo_monCritInit())
   {
    (void)JCGO_THREADLOCAL_STORE((void *)tcb);
    return 0;
   }
#else
   (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
   jcgo_curTCB = tcb;
   return 0;
#endif
  }
#ifdef JCGO_PARALLEL
  (void)JCGO_EVENT_DESTROY(&tcb->resumeEvent);
#endif
  (void)JCGO_EVENT_DESTROY(&tcb->event);
 }
 return -1;
}

STATIC void CFASTCALL jcgo_threadYield( void )
{
 struct jcgo_tcb_s *tcb;
 JCGO_GET_CURTCB(&tcb);
#ifdef JCGO_PARALLEL
 JCGO_THREAD_YIELD;
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_EVENT_WAIT(&tcb->resumeEvent);
  } while (tcb->suspended);
#else
 (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
 JCGO_THREAD_YIELD;
 (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
   (void)JCGO_EVENT_WAIT(&tcb->event);
   (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
  } while (tcb->suspended);
 jcgo_curTCB = tcb;
#endif
 jcgo_checkStop(tcb);
}

#ifdef JCGO_NOMTCLIB
STATIC
#else
JCGO_NOSEP_INLINE
#endif
void CFASTCALL jcgo_saveTCB( void )
{
#ifdef JCGO_NOMTCLIB
#ifndef JCGO_PARALLEL
 struct jcgo_tcb_s *tcb;
 JCGO_GET_CURTCB(&tcb);
 (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
 JCGO_THREAD_YIELD;
#endif
 (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
#ifndef JCGO_PARALLEL
 jcgo_curTCB = tcb;
#endif
 (void)JCGO_SCHED_STOP(0);
#else
#ifndef JCGO_PARALLEL
 (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
#endif
#endif
}

STATIC int CFASTCALL jcgo_restoreTCB( struct jcgo_tcb_s *tcb )
{
#ifdef JCGO_PARALLEL
 struct jcgo_tcb_s *othertcb;
#endif
#ifdef JCGO_NOMTCLIB
 (void)JCGO_SCHED_RESUME(0);
 (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
#endif
#ifdef JCGO_PARALLEL
 JCGO_GET_CURTCB(&othertcb);
 if (JCGO_EXPECT_FALSE(othertcb != tcb))
  return -1;
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_EVENT_WAIT(&tcb->resumeEvent);
  } while (tcb->suspended);
#else
 if (JCGO_EXPECT_FALSE(tcb->jcgo_methods !=
     (jvtable)(&java_lang_Object_methods)))
  return -1;
 (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
   (void)JCGO_EVENT_WAIT(&tcb->event);
   (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
  } while (tcb->suspended);
 jcgo_curTCB = tcb;
#endif
 return 0;
}

#ifdef JCGO_NOJNI
JCGO_NOSEP_INLINE
#else
STATIC
#endif
int CFASTCALL jcgo_monEnterInner( jObject obj, struct jcgo_tcb_s *tcb )
{
 struct jcgo_tcb_s *othertcb;
 struct jcgo_tcb_s *nexttcb;
 int res;
#ifdef JCGO_PARALLEL
 if ((nexttcb = JCGO_GETMON_OF(obj)) == tcb || (nexttcb != NULL &&
     nexttcb->tcbMonOwner == tcb && nexttcb->monObj == obj))
  return 0;
#else
 if ((othertcb = JCGO_GETMON_OF(obj)) == tcb)
  return 0;
#endif
 res = 1;
 JCGO_MONCRIT_BEGIN(obj)
#ifdef JCGO_PARALLEL
 othertcb = JCGO_GETMON_OF(obj);
#endif
 if (JCGO_EXPECT_FALSE(othertcb != NULL))
 {
  if (othertcb->monObj != obj)
  {
   *(jObject volatile *)&tcb->monObj = obj;
   tcb->tcbMonOwner = othertcb;
   JCGO_FIELD_NZACCESS(obj, jcgo_mon) = tcb;
  }
   else
   {
    if ((nexttcb = othertcb->tcbMonOwner) != tcb)
    {
     if (nexttcb != NULL)
     {
      while ((nexttcb = othertcb->tcbWaitNext) != NULL)
       othertcb = nexttcb;
      tcb->monObj = obj;
      othertcb->tcbWaitNext = tcb;
     }
      else
      {
       othertcb->tcbMonOwner = tcb;
       othertcb = NULL;
      }
    }
     else
     {
      res = 0;
      othertcb = NULL;
     }
   }
 }
  else JCGO_FIELD_NZACCESS(obj, jcgo_mon) = tcb;
 JCGO_MONCRIT_END
 if (JCGO_EXPECT_FALSE(othertcb != NULL))
 {
  do
  {
#ifndef JCGO_PARALLEL
   (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
#endif
   (void)JCGO_EVENT_WAIT(&tcb->event);
#ifndef JCGO_PARALLEL
   (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
#endif
  } while (tcb->monObj != jnull);
#ifndef JCGO_PARALLEL
  jcgo_curTCB = tcb;
#endif
 }
 return res;
}

STATIC int CFASTCALL jcgo_monLeaveInner( jObject obj, struct jcgo_tcb_s *tcb )
{
 struct jcgo_tcb_s *othertcb;
 struct jcgo_tcb_s *prevtcb;
 struct jcgo_tcb_s *othertcb2;
 struct jcgo_tcb_s *prevtcb2;
 int res = 0;
 JCGO_MONCRIT_BEGIN(obj)
 othertcb = JCGO_GETMON_OF(obj);
 if (JCGO_EXPECT_FALSE(othertcb != tcb))
 {
  if (othertcb != NULL && othertcb->monObj == obj &&
      othertcb->tcbMonOwner == tcb)
  {
   prevtcb = NULL;
   tcb = othertcb;
   while (othertcb->waitsleep)
    if ((othertcb = (prevtcb = othertcb)->tcbWaitNext) == NULL)
     break;
   if (othertcb != NULL)
   {
    if (JCGO_EXPECT_FALSE(othertcb->suspended != 0))
     for (prevtcb2 = othertcb; (othertcb2 = prevtcb2->tcbWaitNext) != NULL;
          prevtcb2 = othertcb2)
      if ((othertcb2->suspended | othertcb2->waitsleep) == 0)
      {
       prevtcb = prevtcb2;
       othertcb = othertcb2;
       break;
      }
    othertcb->monObj = jnull;
    if (prevtcb != NULL)
    {
     prevtcb->tcbWaitNext = othertcb->tcbWaitNext;
     othertcb->tcbWaitNext = NULL;
     tcb->tcbMonOwner = othertcb;
    }
     else
     {
      othertcb->tcbMonOwner = NULL;
      if ((tcb = othertcb->tcbWaitNext) != NULL)
      {
       othertcb->tcbWaitNext = NULL;
       tcb->tcbMonOwner = othertcb;
       JCGO_FIELD_NZACCESS(obj, jcgo_mon) = tcb;
      }
     }
    (void)JCGO_EVENT_SET(&othertcb->event);
   }
    else tcb->tcbMonOwner = NULL;
  }
   else res = -1;
 }
  else JCGO_FIELD_NZACCESS(obj, jcgo_mon) = NULL;
 JCGO_MONCRIT_END
 return res;
}

#ifdef JCGO_SEHTRY

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_monitorEnter( jObject obj )
{
 struct jcgo_tcb_s *tcb;
#ifndef JCGO_PARALLEL
 jObject ex;
#endif
 if (JCGO_EXPECT_FALSE(obj == jnull))
  JCGO_THROW_EXC(jnull);
 JCGO_GET_CURTCB(&tcb);
#ifdef JCGO_PARALLEL
 jcgo_checkStop(tcb);
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_EVENT_WAIT(&tcb->resumeEvent);
  } while (tcb->suspended);
 if (!jcgo_monEnterInner(obj, tcb))
  obj = jnull;
#else
 if (!jcgo_monEnterInner(obj, tcb))
  return jnull;
 ex = tcb->stopExc;
 if (JCGO_EXPECT_FALSE(ex != jnull) && !tcb->insideCallback)
 {
  tcb->stopExc = jnull;
  if (obj != jnull)
   jcgo_monLeaveInner(obj, tcb);
  JCGO_THROW_EXC(ex);
 }
#endif
 return obj;
}

JCGO_NOSEP_STATIC void CFASTCALL jcgo_monitorLeave( jObject obj )
{
 struct jcgo_tcb_s *tcb;
#ifdef JCGO_PARALLEL
 JCGO_GET_CURTCB(&tcb);
 if (JCGO_EXPECT_TRUE(obj != jnull))
  jcgo_monLeaveInner(obj, tcb);
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_EVENT_WAIT(&tcb->resumeEvent);
  } while (tcb->suspended);
 jcgo_checkStop(tcb);
#else
 if (JCGO_EXPECT_TRUE(obj != jnull))
 {
  JCGO_GET_CURTCB(&tcb);
  jcgo_monLeaveInner(obj, tcb);
 }
#endif
}

#else /* JCGO_SEHTRY */

JCGO_NOSEP_STATIC void CFASTCALL jcgo_monitorEnter(
 struct jcgo_curmon_s *pCurMon, jObject obj )
{
 struct jcgo_tcb_s *tcb;
 if (JCGO_EXPECT_FALSE(obj == jnull))
  JCGO_THROW_EXC(jnull);
 pCurMon->monObj = jnull;
 JCGO_GET_CURTCB(&tcb);
#ifdef JCGO_PARALLEL
 jcgo_checkStop(tcb);
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_EVENT_WAIT(&tcb->resumeEvent);
  } while (tcb->suspended);
#endif
 if (jcgo_monEnterInner(obj, tcb))
  pCurMon->monObj = obj;
 pCurMon->last = tcb->pCurMon;
 *(struct jcgo_curmon_s *volatile *)&tcb->pCurMon = pCurMon;
#ifndef JCGO_PARALLEL
 obj = tcb->stopExc;
 if (JCGO_EXPECT_FALSE(obj != jnull) && !tcb->insideCallback)
 {
  tcb->stopExc = jnull;
  JCGO_THROW_EXC(obj);
 }
#endif
}

JCGO_NOSEP_STATIC void CFASTCALL jcgo_monitorLeave( void )
{
 struct jcgo_tcb_s *tcb;
 struct jcgo_curmon_s *pCurMon;
 jObject obj;
 JCGO_GET_CURTCB(&tcb);
 pCurMon = tcb->pCurMon;
 tcb->pCurMon = pCurMon->last;
 if ((obj = pCurMon->monObj) != jnull)
  jcgo_monLeaveInner(obj, tcb);
#ifdef JCGO_PARALLEL
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_EVENT_WAIT(&tcb->resumeEvent);
  } while (tcb->suspended);
 jcgo_checkStop(tcb);
#endif
}

#endif /* ! JCGO_SEHTRY */

#ifndef JCGO_NOJNI

JCGO_NOSEP_INLINE struct jcgo_tcb_s *CFASTCALL jcgo_getSelfTCB( void )
{
 struct jcgo_tcb_s *tcb;
#ifdef JCGO_PARALLEL
 JCGO_GET_CURTCB(&tcb);
#else
 JCGO_THREAD_T thrhandle;
 tcb = NULL;
 if (JCGO_EXPECT_TRUE(!JCGO_THREAD_IDENTSELF(&thrhandle)))
 {
  tcb = &jcgo_mainTCB;
  while (!JCGO_THREADT_ISEQUALIDENT(&tcb->thrhandle, &thrhandle))
   if ((tcb = tcb->tcbList) == NULL)
    break;
 }
#endif
 return tcb;
}

#endif /* ! JCGO_NOJNI */

STATIC void CFASTCALL jcgo_threadAttachTCB( struct jcgo_tcb_s *tcb )
{
#ifdef FPINIT
 FPINIT;
#endif
#ifdef JCGO_PARALLEL
 (void)JCGO_THREADLOCAL_STORE((void *)tcb);
#else
#ifndef JCGO_NOJNI
 tcb->tcbList = jcgo_mainTCB.tcbList;
 jcgo_mainTCB.tcbList = tcb;
#endif
 jcgo_curTCB = tcb;
#endif
 jcgo_setSyncSignals();
#ifdef JCGO_PARALLEL
 (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
#endif
}

STATIC void CFASTCALL jcgo_threadDetachTCB( struct jcgo_tcb_s *tcb )
{
#ifndef JCGO_NOJNI
 jint len;
 jObjectArr jniLockedObjs;
 jObject obj;
#ifndef JCGO_PARALLEL
 struct jcgo_tcb_s *othertcb;
 struct jcgo_tcb_s **ptcb = &jcgo_mainTCB.tcbList;
 while ((othertcb = *ptcb) != tcb)
  ptcb = &othertcb->tcbList;
 *ptcb = tcb->tcbList;
#endif
 for (jniLockedObjs = tcb->jniLockedObjs; jniLockedObjs != jnull;
      jniLockedObjs = (jObjectArr)JCGO_ARR_INTERNALACC(jObject,
      jniLockedObjs, 0))
 {
  len = JCGO_ARRAY_NZLENGTH(jniLockedObjs);
  while (--len > 0)
   if ((obj = JCGO_ARR_INTERNALACC(jObject, jniLockedObjs, len)) != jnull)
    jcgo_monLeaveInner(obj, tcb);
 }
#endif
 tcb->thread = jnull;
 JCGO_MONCRIT_BEGIN(tcb)
 tcb->suspended = -1;
#ifdef JCGO_PARALLEL
 (void)JCGO_EVENT_DESTROY(&tcb->resumeEvent);
#endif
 (void)JCGO_EVENT_DESTROY(&tcb->event);
 JCGO_MONCRIT_END
#ifdef JCGO_THRDGC_UNREGNEEDED
 if (tcb->gcAttached)
  GC_unregister_my_thread();
#endif
#ifdef JCGO_PARALLEL
 (void)JCGO_THREADLOCAL_STORE(NULL);
#else
 (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
#endif
}

#ifdef JCGO_THRDGC_OMITREGNORMAL
JCGO_NOSEP_INLINE void *CFASTCALL jcgo_threadLaunchBody( void *param )
#else
EXTRASTATIC void *GC_CALLBACK jcgo_threadLaunchBody(
 struct GC_stack_base *pstackbase, void *param )
#endif
{
#ifdef OBJT_java_lang_VMThread
 jObject throwable;
#endif
#ifndef JCGO_THRDGC_OMITREGNORMAL
 ((struct jcgo_tcb_s *)param)->gcAttached =
  !GC_register_my_thread(pstackbase);
#endif
 jcgo_threadAttachTCB((struct jcgo_tcb_s *)param);
#ifdef OBJT_java_lang_VMThread
 {
  JCGO_TRY_BLOCK
  {
   java_lang_VMThread__run0X__Lo(JCGO_OBJREF_OF(*(java_lang_Object)param));
  }
  JCGO_TRY_LEAVE
  JCGO_TRY_CATCHALLSTORE(&throwable)
 }
#ifndef JCGO_NOFATALMSG
 if (JCGO_EXPECT_FALSE(throwable != jnull))
  jcgo_printFatalMsg("Out of memory or internal error in non-main thread!");
#endif
#endif
 jcgo_threadDetachTCB((struct jcgo_tcb_s *)param);
 (void)JCGO_THREAD_DETACH(&((struct jcgo_tcb_s *)param)->thrhandle);
 (void)JCGO_THREADT_CLEAR(&((struct jcgo_tcb_s *)param)->thrhandle);
#ifdef OBJT_java_lang_VMThread
 if (throwable != jnull)
  JCGO_ABORT_EXIT;
#endif
 return NULL;
}

EXTRASTATIC JCGO_THREADRET_T JCGO_THREAD_RTNDECL jcgo_threadLauncher(
 JCGO_THREADPARAM_T param )
{
#ifdef JCGO_PARALLEL
 for (;;)
 {
  (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
  if (JCGO_THREADT_ISVALID(&((struct jcgo_tcb_s *)param)->thrhandle))
   break;
  (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
  JCGO_THREAD_YIELD;
 }
#else
 (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
#ifndef JCGO_NOJNI
 (void)JCGO_THREAD_STOREIDENT(&((struct jcgo_tcb_s *)param)->thrhandle);
#endif
#endif
#ifdef JCGO_SEHTRY
#ifdef OBJT_java_lang_VMThrowable
 if (!setjmp(((struct jcgo_tcb_s *)param)->jbuf))
#endif
#endif
 {
#ifdef JCGO_THRDGC_OMITREGNORMAL
  (void)jcgo_threadLaunchBody((void *)param);
#else
  (void)GC_call_with_stack_base(jcgo_threadLaunchBody, (void *)param);
#endif
 }
 return JCGO_THREAD_RETVALUE;
}

#endif
