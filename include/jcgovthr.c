/*
 * @(#) $(JCGO)/include/jcgovthr.c --
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

#define JCGO_JTHRSTATE_RUNNABLE 1
#define JCGO_JTHRSTATE_BLOCKED 2
#define JCGO_JTHRSTATE_WAITING 3
#define JCGO_JTHRSTATE_TIMEDWAIT 4

JCGO_NOSEP_STATIC void CFASTCALL
java_lang_VMThread__yield__( void )
{
#ifdef OBJT_java_lang_VMThread
 JCGO_CLINIT_TRIG(java_lang_VMThread__class);
#endif
#ifdef JCGO_THREADS
 jcgo_threadYield();
#else
 jcgo_checkStop(&jcgo_mainTCB);
#endif
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMThread__notify0__LoI( java_lang_Object obj, jint all )
{
#ifdef JCGO_THREADS
 struct jcgo_tcb_s *tcb;
 struct jcgo_tcb_s *othertcb;
 int res;
 JCGO_GET_CURTCB(&tcb);
#ifdef JCGO_PARALLEL
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_EVENT_WAIT(&tcb->resumeEvent);
  } while (tcb->suspended);
 jcgo_checkStop(tcb);
 if (JCGO_EXPECT_FALSE(JCGO_GETMON_OF(obj) == tcb))
  return 0;
#else
 othertcb = JCGO_GETMON_OF(obj);
 if (JCGO_EXPECT_FALSE(othertcb == tcb))
  return 0;
#endif
 res = -1;
 JCGO_MONCRIT_BEGIN(obj)
#ifdef JCGO_PARALLEL
 othertcb = JCGO_GETMON_OF(obj);
#endif
 if (JCGO_EXPECT_TRUE(othertcb != NULL && othertcb->monObj == (jObject)obj &&
     othertcb->tcbMonOwner == tcb))
 {
  res = 0;
  if ((int)all > 0)
  {
   do
   {
    othertcb->waitsleep = 0;
   } while ((othertcb = othertcb->tcbWaitNext) != NULL);
  }
   else
   {
    if (!(int)all)
    {
     while (!othertcb->waitsleep)
      if ((othertcb = othertcb->tcbWaitNext) == NULL)
       break;
     if (JCGO_EXPECT_TRUE(othertcb != NULL))
     {
      tcb = othertcb;
      if (JCGO_EXPECT_FALSE(othertcb->suspended != 0))
       while ((othertcb = othertcb->tcbWaitNext) != NULL)
        if (!othertcb->suspended && othertcb->waitsleep)
        {
         tcb = othertcb;
         break;
        }
      tcb->waitsleep = 0;
     }
    }
   }
 }
 JCGO_MONCRIT_END
 return (jint)res;
#else
 return 0;
#endif
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMThread__wait0__LoJI( java_lang_Object obj, jlong ms, jint ns )
{
#ifdef JCGO_THREADS
 JCGO_EVENTTIME_T waittime;
 struct jcgo_tcb_s *tcb;
 struct jcgo_tcb_s *othertcb;
 struct jcgo_tcb_s *prevtcb;
 struct jcgo_tcb_s *headtcb;
 struct jcgo_tcb_s *othertcb2;
 struct jcgo_tcb_s *prevtcb2;
 int interrupted = 0;
#ifdef JCGO_PARALLEL
 int monMutexInd;
#endif
 if (JCGO_EXPECT_FALSE(ms >= (jlong)0x7fffffffL))
 {
  ms = (jlong)0x7fffffffL;
  ns = 0;
 }
 (void)JCGO_EVENTTIME_PREPARE(&waittime, (long)ms, (long)ns);
 JCGO_GET_CURTCB(&tcb);
#ifdef JCGO_PARALLEL
 monMutexInd = JCGO_MUTEXIND_HASHF(JCGO_CAST_PTRTONUM(obj),
                JCGO_MONCRIT_HASHLOGSZ);
 jcgo_checkStop(tcb);
 (void)JCGO_MUTEX_LOCK(&jcgo_monCritMutexes[monMutexInd]);
#endif
 if ((othertcb = JCGO_GETMON_OF(obj)) == tcb || (othertcb != NULL &&
     othertcb->monObj == (jObject)obj && othertcb->tcbMonOwner == tcb))
 {
  if (JCGO_EXPECT_TRUE(!tcb->interruptReq))
  {
   tcb->waitsleep = (ms | (jlong)ns) > (jlong)0L ? 1 : -1;
   tcb->monObj = (jObject)obj;
   (void)JCGO_EVENT_CLEAR(&tcb->event);
   if (othertcb != tcb)
   {
    prevtcb = NULL;
    (headtcb = othertcb)->tcbMonOwner = NULL;
    while (othertcb->waitsleep)
    {
     prevtcb = othertcb;
     if ((othertcb = othertcb->tcbWaitNext) == NULL)
      break;
    }
    if (othertcb != NULL)
    {
     prevtcb2 = othertcb;
     if (JCGO_EXPECT_FALSE(othertcb->suspended != 0))
     {
      while ((othertcb2 = prevtcb2->tcbWaitNext) != NULL)
      {
       if ((othertcb2->suspended | othertcb2->waitsleep) == 0)
        break;
       prevtcb2 = othertcb2;
      }
      if (othertcb2 != NULL)
      {
       prevtcb = prevtcb2;
       othertcb = othertcb2;
       do
       {
        prevtcb2 = othertcb2;
       } while ((othertcb2 = othertcb2->tcbWaitNext) != NULL);
      }
     }
      else
      {
       while ((othertcb2 = prevtcb2->tcbWaitNext) != NULL)
        prevtcb2 = othertcb2;
      }
     prevtcb2->tcbWaitNext = tcb;
     prevtcb2 = othertcb->tcbWaitNext;
     othertcb->monObj = jnull;
     othertcb->tcbWaitNext = NULL;
     if (prevtcb != NULL)
     {
      prevtcb->tcbWaitNext = prevtcb2;
      headtcb->tcbMonOwner = othertcb;
     }
      else
      {
       prevtcb2->tcbMonOwner = othertcb;
       JCGO_FIELD_NZACCESS(obj, jcgo_mon) = prevtcb2;
      }
     (void)JCGO_EVENT_SET(&othertcb->event);
    }
     else (othertcb = prevtcb)->tcbWaitNext = tcb;
   }
   if (tcb->interruptReq)
    interrupted = 1;
  }
   else
   {
    tcb->interruptReq = 0;
    interrupted = 2;
    othertcb = NULL;
   }
 }
  else othertcb = NULL;
#ifdef JCGO_PARALLEL
 (void)JCGO_MUTEX_UNLOCK(&jcgo_monCritMutexes[monMutexInd]);
#endif
 if (JCGO_EXPECT_FALSE(othertcb == NULL))
  return (jint)(interrupted - 1);
 if (JCGO_EXPECT_TRUE(!interrupted) &&
     (tcb->insideCallback || JCGO_EXPECT_TRUE(tcb->stopExc == jnull)))
 {
#ifndef JCGO_PARALLEL
  (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
#endif
  if ((ms | (jlong)ns) > (jlong)0L)
  {
   for (;;)
   {
    if (!JCGO_EVENT_TIMEDWAIT(&tcb->event, &waittime))
     break;
   }
  }
   else
   {
    for (;;)
    {
     if (!JCGO_EVENT_WAIT(&tcb->event))
      break;
    }
   }
#ifndef JCGO_PARALLEL
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
  if (tcb->interruptReq)
   interrupted = 1;
 }
 if (tcb->monObj != jnull)
 {
#ifdef JCGO_PARALLEL
  (void)JCGO_MUTEX_LOCK(&jcgo_monCritMutexes[monMutexInd]);
#endif
  if (tcb->waitsleep)
  {
   tcb->waitsleep = 0;
   if ((headtcb = JCGO_GETMON_OF(obj))->tcbMonOwner == NULL)
   {
    tcb->monObj = jnull;
    if (headtcb != tcb)
    {
     prevtcb = headtcb;
     while ((othertcb = prevtcb->tcbWaitNext) != tcb)
      prevtcb = othertcb;
     prevtcb->tcbWaitNext = tcb->tcbWaitNext;
     tcb->tcbWaitNext = NULL;
     headtcb->tcbMonOwner = tcb;
     othertcb = NULL;
    }
     else
     {
      if ((othertcb = tcb->tcbWaitNext) != NULL)
      {
       tcb->tcbWaitNext = NULL;
       othertcb->tcbMonOwner = tcb;
       JCGO_FIELD_NZACCESS(obj, jcgo_mon) = othertcb;
       othertcb = NULL;
      }
     }
   }
  }
#ifdef JCGO_PARALLEL
  (void)JCGO_MUTEX_UNLOCK(&jcgo_monCritMutexes[monMutexInd]);
#endif
  if (othertcb != NULL)
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
 }
#ifdef JCGO_PARALLEL
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_EVENT_WAIT(&tcb->resumeEvent);
  } while (tcb->suspended);
#endif
 jcgo_checkStop(tcb);
 if (JCGO_EXPECT_FALSE(interrupted || tcb->interruptReq))
 {
  tcb->interruptReq = 0;
  return 1;
 }
#else
 jcgo_checkStop(&jcgo_mainTCB);
 if (jcgo_mainTCB.interruptReq)
 {
  jcgo_mainTCB.interruptReq = 0;
  return 1;
 }
#endif
 return 0;
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_VMThread__setupMainThread0__Lo( java_lang_Object thread )
{
 jcgo_mainTCB.thread = (jObject)thread;
 return JCGO_OBJREF_OF(*(java_lang_Object)&jcgo_mainTCB);
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_VMThread__currentThread0__( void )
{
 struct jcgo_tcb_s *tcb;
 JCGO_GET_CURTCB(&tcb);
 return (java_lang_Object)tcb->thread;
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_VMThread__start0__LoJ( java_lang_Object thread, jlong stacksize )
{
#ifdef JCGO_THREADS
 int res;
 struct jcgo_tcb_s *othertcb = jcgo_memAlloc(sizeof(struct jcgo_tcb_s),
                                JCGO_PTR_RESERVED);
 if (JCGO_EXPECT_TRUE(othertcb != NULL && !JCGO_EVENT_INIT(&othertcb->event)))
 {
#ifdef JCGO_PARALLEL
  if (JCGO_EVENT_INIT(&othertcb->resumeEvent))
  {
   (void)JCGO_EVENT_DESTROY(&othertcb->event);
   return jnull;
  }
#endif
  othertcb->jcgo_methods = (jvtable)&java_lang_Object_methods;
  (void)JCGO_THREADT_CLEAR(&othertcb->thrhandle);
  othertcb->thread = (jObject)thread;
#ifdef THREADSTACKSZ
  if (!stacksize)
   stacksize = (jlong)THREADSTACKSZ;
#endif
#ifdef JCGO_PARALLEL
 (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
#endif
  res = JCGO_THREAD_CREATE(&othertcb->thrhandle, jcgo_threadLauncher,
         (unsigned)stacksize, (void *)othertcb);
#ifdef JCGO_PARALLEL
 (void)JCGO_MUTEX_UNLOCK(&jcgo_nonParallelMutex);
#endif
  if (JCGO_EXPECT_TRUE(!res))
   return JCGO_OBJREF_OF(*(java_lang_Object)othertcb);
  othertcb->thread = jnull;
#ifdef JCGO_PARALLEL
  (void)JCGO_EVENT_DESTROY(&othertcb->resumeEvent);
#endif
  (void)JCGO_EVENT_DESTROY(&othertcb->event);
 }
#endif
 return jnull;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMThread__nativeSetPriority0__LoI( java_lang_Object vmdata,
 jint priority )
{
 int res = -1;
#ifdef JCGO_THREADS
 JCGO_THREAD_T thrhandle;
 JCGO_THREADPRIO_T sched;
 (void)JCGO_THREADT_COPY(&thrhandle,
  &((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->thrhandle);
 if (JCGO_EXPECT_TRUE(JCGO_THREADT_ISVALID(&thrhandle)))
  res = JCGO_THREADPRIO_SET(&thrhandle, &sched, (int)priority);
#endif
 return (jint)res;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMThread__nativeStop0__LoLo( java_lang_Object vmdata,
 java_lang_Object throwable )
{
#ifdef JCGO_THREADS
 ((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->stopExc =
  (jObject)throwable;
 JCGO_MONCRIT_BEGIN(&JCGO_METHODS_OF(vmdata))
 if (((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->suspended != -1)
  (void)JCGO_EVENT_SET(
   &((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->event);
 JCGO_MONCRIT_END
#ifndef JCGO_PARALLEL
 jcgo_threadYield();
#endif
#endif
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMThread__interrupt0__LoI( java_lang_Object vmdata, jint set )
{
 if (((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->interruptReq)
 {
  if ((int)set < 0)
   ((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->interruptReq = 0;
  return 1;
 }
 if ((int)set > 0)
 {
  ((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->interruptReq = 1;
#ifdef JCGO_THREADS
  JCGO_MONCRIT_BEGIN(&JCGO_METHODS_OF(vmdata))
  if (((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->suspended != -1)
   (void)JCGO_EVENT_SET(
    &((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->event);
  JCGO_MONCRIT_END
#ifndef JCGO_PARALLEL
  jcgo_threadYield();
#endif
#endif
 }
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMThread__suspend0__LoI( java_lang_Object vmdata, jint set )
{
#ifdef JCGO_THREADS
 int res;
 if (!(int)set)
  return ((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->suspended ? 1 : 0;
 res = 0;
 JCGO_MONCRIT_BEGIN(&JCGO_METHODS_OF(vmdata))
 if (((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->suspended)
 {
  res = 1;
  if ((int)set < 0 &&
      ((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->suspended != -1)
  {
   ((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->suspended = 0;
#ifdef JCGO_PARALLEL
   set = 0;
#else
   if (!((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->waitsleep)
    (void)JCGO_EVENT_SET(
     &((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->event);
#endif
  }
 }
  else
  {
   if ((int)set > 0)
    ((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->suspended = 1;
  }
 JCGO_MONCRIT_END
#ifdef JCGO_PARALLEL
 if (!(int)set)
  (void)JCGO_EVENT_SET(
   &((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->resumeEvent);
#else
 if ((int)set < 0 && res)
  jcgo_threadYield();
#endif
 return (jint)res;
#else
 return -1;
#endif
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMThread__countStackFrames0__Lo( java_lang_Object vmdata )
{
 /* not implemented */
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMThread__getState0__Lo( java_lang_Object vmdata )
{
#ifdef JCGO_THREADS
 int waitsleep = ((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->waitsleep;
 if (waitsleep)
  return (jint)(waitsleep > 0 ? JCGO_JTHRSTATE_TIMEDWAIT :
          JCGO_JTHRSTATE_WAITING);
 if (((struct jcgo_tcb_s *)&JCGO_METHODS_OF(vmdata))->monObj != jnull)
  return JCGO_JTHRSTATE_BLOCKED;
#endif
 return JCGO_JTHRSTATE_RUNNABLE;
}

#endif
