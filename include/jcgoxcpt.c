/*
 * @(#) $(JCGO)/include/jcgoxcpt.c --
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

/* #include <setjmp.h> */
/* void longjmp(jmp_buf, int); */

/* #include <signal.h> */
/* void (*signal(int, void (*)(int)))(int); */

#ifndef JCGO_NOSEGV
#ifndef SIGBUS
#ifndef SIGSEGV
#define JCGO_NOSEGV
#endif
#endif
#endif

#ifdef SIG_ACK
#define JCGO_SIGNAL_RESET(sig, func) (signal(sig, func) != SIG_DFL ? (void)signal(sig, SIG_ACK) : (void)0)
#else
#define JCGO_SIGNAL_RESET(sig, func) ((void)signal(sig, func))
#endif

#ifndef JCGO_NOCTRLC

STATICDATA int jcgo_sigTermNum = 0;

STATIC void CLIBDECL jcgo_catchSigTerm( int sig )
{
 JCGO_SIGNAL_RESET(sig, jcgo_catchSigTerm);
 jcgo_sigTermNum = sig;
}

#endif /* ! JCGO_NOCTRLC */

#ifndef JCGO_NOSEGV

STATIC void CLIBDECL jcgo_catchSigSegV( int sig )
{
 JCGO_SIGNAL_RESET(sig, jcgo_catchSigSegV);
 JCGO_THROW_EXC(jcgo_globData.nullExc);
}

#endif /* ! JCGO_NOSEGV */

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_setSyncSignals( void )
{
#ifndef JCGO_NOSEGV
#ifdef SIGSEGV
 signal(SIGSEGV, jcgo_catchSigSegV);
#endif
#ifdef SIGBUS
 signal(SIGBUS, jcgo_catchSigSegV);
#endif
#endif
#ifdef SIGPIPE
 signal(SIGPIPE, SIG_IGN);
#endif
}

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_initAsyncSignals( void )
{
#ifndef JCGO_NOCTRLC
#ifdef SIGTERM
 signal(SIGTERM, jcgo_catchSigTerm);
#endif
#ifdef SIGINT
 signal(SIGINT, jcgo_catchSigTerm);
#endif
#ifdef SIGHUP
 signal(SIGHUP, jcgo_catchSigTerm);
#endif
#endif
}

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_unregSignalHandlers( void )
{
#ifndef JCGO_NOSEGV
#ifndef JCGO_PARALLEL
#ifdef SIGSEGV
 signal(SIGSEGV, SIG_DFL);
#endif
#ifdef SIGBUS
 signal(SIGBUS, SIG_DFL);
#endif
#endif
#endif
#ifndef JCGO_NOCTRLC
#ifdef SIGTERM
 signal(SIGTERM, SIG_DFL);
#endif
#ifdef SIGINT
 signal(SIGINT, SIG_DFL);
#endif
#ifdef SIGHUP
 signal(SIGHUP, SIG_DFL);
#endif
#endif
}

#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
#ifdef JCGO_NOCTRLC
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL
#else
STATIC void CFASTCALL
#endif
#else
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL
#endif
#else
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL
#endif
jcgo_checkStop( struct jcgo_tcb_s *tcb )
{
#ifdef JCGO_THREADS
 jObject ex;
#endif
#ifndef JCGO_NOCTRLC
 int sig = jcgo_sigTermNum;
 if (JCGO_EXPECT_FALSE(sig > 0) && !tcb->insideCallback)
 {
  *(volatile int *)&jcgo_sigTermNum = 0;
  JCGO_CALLBACK_BEGIN
  java_lang_VMRuntime__handleSigTerm0X__I((jint)sig);
  JCGO_CALLBACK_END
 }
#endif
#ifdef JCGO_THREADS
 ex = tcb->stopExc;
 if (JCGO_EXPECT_FALSE(ex != jnull) && !tcb->insideCallback)
 {
  tcb->stopExc = jnull;
  JCGO_THROW_EXC(ex);
 }
#endif
}

#ifdef JCGO_SEHTRY

JCGO_NOSEP_STATIC jObject *CFASTCALL jcgo_tryCatches( void )
{
 struct jcgo_tcb_s *tcb;
 JCGO_GET_CURTCB(&tcb);
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_EVENT_WAIT(&tcb->resumeEvent);
  } while (tcb->suspended);
#ifdef OBJT_java_lang_VMThrowable
 if (JCGO_EXPECT_FALSE(tcb->stopExc != jnull) && !tcb->insideCallback &&
     tcb->throwable == jnull && (tcb->throwable =
     (jObject)(*(jObject volatile *)&tcb->stopExc)) != jnull)
 {
  tcb->stopExc = jnull;
  longjmp(tcb->jbuf, 1);
 }
#endif
#endif
#endif
 return &tcb->throwable;
}

EXTRASTATIC jObject CFASTCALL jcgo_tryCatchAll( void )
{
 jObject ex;
 struct jcgo_tcb_s *tcb;
 JCGO_GET_CURTCB(&tcb);
 ex = tcb->throwable;
 tcb->throwable = jnull;
 return ex;
}

#else /* JCGO_SEHTRY */

JCGO_NOSEP_EXTRASTATIC void CFASTCALL jcgo_tryEnter(
 struct jcgo_try_s *pCurTry )
{
 struct jcgo_tcb_s *tcb;
 JCGO_GET_CURTCB(&tcb);
 pCurTry->throwable = jnull;
 pCurTry->last = tcb->pCurTry;
 *(struct jcgo_try_s *volatile *)&tcb->pCurTry = pCurTry;
#ifdef JCGO_THREADS
 pCurTry->pCurMon = tcb->pCurMon;
 tcb->pCurMon = NULL;
#endif
}

JCGO_NOSEP_EXTRASTATIC void CFASTCALL jcgo_tryLeave( void )
{
 struct jcgo_tcb_s *tcb;
#ifdef JCGO_THREADS
 struct jcgo_try_s *pCurTry;
#endif
 JCGO_GET_CURTCB(&tcb);
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (JCGO_EXPECT_FALSE(tcb->suspended != 0) && !tcb->insideCallback)
  do
  {
   (void)JCGO_EVENT_WAIT(&tcb->resumeEvent);
  } while (tcb->suspended);
 if (JCGO_EXPECT_FALSE(tcb->stopExc != jnull) && !tcb->insideCallback &&
     tcb->pCurTry->throwable == jnull)
 {
  tcb->pCurTry->throwable = tcb->stopExc;
  tcb->stopExc = jnull;
 }
#endif
 tcb->pCurMon = (pCurTry = tcb->pCurTry)->pCurMon;
 tcb->pCurTry = pCurTry->last;
#else
 tcb->pCurTry = tcb->pCurTry->last;
#endif
}

#endif /* ! JCGO_SEHTRY */

#ifdef OBJT_java_lang_VMThrowable

#define jcgo_throwArithmeticExc java_lang_VMThrowable__throwArithmeticException0X__
#define jcgo_throwArrayIndexExc java_lang_VMThrowable__throwArrayIndexOutOfBoundsException0X__
#define jcgo_throwClassCastExc java_lang_VMThrowable__throwClassCastException0X__

#else /* OBJT_java_lang_VMThrowable */

STATIC void CFASTCALL jcgo_throwArithmeticExc( void )
{
 JCGO_FATAL_ABORT("Arithmetic divide-by-zero exception!");
}

STATIC void CFASTCALL jcgo_throwArrayIndexExc( void )
{
 JCGO_FATAL_ABORT("Array index out of bounds exception!");
}

STATIC void CFASTCALL jcgo_throwClassCastExc( void )
{
 JCGO_FATAL_ABORT("Class cast exception!");
}

#endif /* ! OBJT_java_lang_VMThrowable */

#ifdef JCGO_INDEXCHK

#ifdef JCGO_RTASSERT

STATIC void CFASTCALL jcgo_throwArrayIndexExcX( void )
{
 JCGO_FATAL_ABORT("Array index bounds assertion!");
}

#else /* JCGO_RTASSERT */

#define jcgo_throwArrayIndexExcX jcgo_throwArrayIndexExc

#endif /* ! JCGO_RTASSERT */

#endif /* JCGO_INDEXCHK */

#ifdef JCGO_CHKCAST

#ifdef JCGO_RTASSERT

STATIC void CFASTCALL jcgo_throwClassCastExcX( void )
{
 JCGO_FATAL_ABORT("Class cast assertion!");
}

#else /* JCGO_RTASSERT */

#define jcgo_throwClassCastExcX jcgo_throwClassCastExc

#endif /* ! JCGO_RTASSERT */

JCGO_NOSEP_INLINE void CFASTCALL jcgo_throwArrayStoreExcX( void )
{
#ifdef JCGO_RTASSERT
 JCGO_FATAL_ABORT("Array store assertion!");
#else
#ifdef OBJT_java_lang_VMThrowable
 java_lang_VMThrowable__throwArrayStoreException0X__();
#else
 JCGO_FATAL_ABORT("Array store exception!");
#endif
#endif
}

#endif /* JCGO_CHKCAST */

JCGO_NOSEP_STATIC void CFASTCALL jcgo_throwNullPtrExcX( void )
{
#ifdef JCGO_RTASSERT
 JCGO_FATAL_ABORT("Null pointer assertion!");
#else
 JCGO_THROW_EXC(jnull);
#endif
}

JCGO_NOSEP_STATIC void CFASTCALL jcgo_throwNullPtrExc( void )
{
 JCGO_THROW_EXC(jnull);
}

JCGO_NOSEP_STATIC void CFASTCALL jcgo_throwExc( jObject throwable )
{
#ifdef OBJT_java_lang_VMThrowable
 struct jcgo_tcb_s *tcb;
#ifndef JCGO_SEHTRY
 struct jcgo_try_s *pCurTry;
#ifdef JCGO_THREADS
 struct jcgo_curmon_s *pCurMon;
 jObject obj;
#endif
#endif
 JCGO_GET_CURTCB(&tcb);
 if (JCGO_EXPECT_FALSE(throwable == jnull) && tcb->jniEnv == NULL)
  throwable =
   (jObject)java_lang_VMThrowable__createNullPointerException0X__();
#ifdef JCGO_SEHTRY
 if (JCGO_EXPECT_TRUE(tcb->jniEnv == NULL))
 {
  tcb->throwable = throwable;
  longjmp(tcb->jbuf, 1);
 }
#else
 pCurTry = tcb->pCurTry;
 if (JCGO_EXPECT_TRUE(pCurTry != NULL) && tcb->jniEnv == NULL)
 {
#ifdef JCGO_THREADS
  for (pCurMon = tcb->pCurMon; pCurMon != NULL; pCurMon = pCurMon->last)
   if ((obj = pCurMon->monObj) != jnull)
    jcgo_monLeaveInner(obj, tcb);
#endif
  pCurTry->throwable = throwable;
  longjmp(pCurTry->jbuf, 1);
 }
#endif
#endif
 /* if (jcgo_noTypesClassArr.jcgo_methods != NULL) */
 {
#ifndef JCGO_NOFATALMSG
#ifdef JCGO_NOSEGV
  jcgo_printFatalMsg("Null pointer exception!");
#else
#ifndef OBJT_java_lang_VMThrowable
  struct jcgo_tcb_s *tcb;
  JCGO_GET_CURTCB(&tcb);
#endif
  jcgo_printFatalMsg(tcb->jniEnv != NULL ?
   "[JNI] Segmentation fault detected!" : "Null pointer exception!");
#endif
#endif
  JCGO_ABORT_EXIT;
 }
}

#endif
