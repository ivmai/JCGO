/*
 * @(#) $(JCGO)/include/jcgobase.c --
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
 * Used control macros: JCGO_CLINITCHK, JCGO_ERRSTDOUT, JCGO_ERRTOLOG,
 * JCGO_GCALLINTER, JCGO_GCRESETDLS, JCGO_NOCREATJVM, JCGO_NOFATALMSG,
 * JCGO_NOGC, JCGO_NOJNI, JCGO_SEHTRY, JCGO_SEPARATED, JCGO_STDCLINIT,
 * JCGO_THREADS, JCGO_TOLOGFORCE, JCGO_WMAIN.
 * Macros for tuning: GCBSSFIRSTSYM, GCBSSLASTSYM, GCDATAFIRSTSYM,
 * GCDATALASTSYM.
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

#ifndef JCGO_THREADS

#define JCGO_CRITMOD_BEGIN(mutexvar) {
#define JCGO_CRITMOD_END(mutexvar) }

#define JCGO_GET_CURTCB(ptcb) (void)(*(void **)(ptcb) = (void *)&jcgo_mainTCB)

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
#ifdef JCGO_SEHTRY
#ifdef OBJT_java_lang_VMThrowable
 jmp_buf jbuf;
#endif
#endif
};

JCGO_NOSEP_GCDATA struct jcgo_tcb_s jcgo_mainTCB ATTRIBGCBSS = { NULL };

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_setSyncSignals( void );
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_checkStop(
 struct jcgo_tcb_s *tcb );

#ifndef JCGO_NOJNI

JCGO_NOSEP_INLINE int CFASTCALL jcgo_restoreTCB( struct jcgo_tcb_s *tcb )
{
 return &jcgo_mainTCB == tcb ? 0 : -1;
}

#endif /* ! JCGO_NOJNI */

#endif /* ! JCGO_THREADS */

#define JCGO_PTR_RESERVED ((void *)&((volatile char *)NULL)[-1])

#define JCGO_CAST_NUMTOPTR(value) ((void *)((ptrdiff_t)(value)))

#ifndef JCGO_CAST_PFUNCTOUINT
#define JCGO_CAST_PFUNCTOUINT(pfunc) ((unsigned)((ptrdiff_t)(pfunc)))
#endif

#define JCGO_JNI_GETTCB(pJniEnv) ((struct jcgo_tcb_s *)((volatile char *)pJniEnv - JCGO_OFFSET_OF(struct jcgo_tcb_s, jniEnv)))

#define JCGO_ACCMOD_FINAL 0x10

#ifdef JCGO_SEHTRY
#define JCGO_TRY_CATCHIGNOREALL(tcb) { if (tcb->throwable != jnull) { tcb->throwable = jnull; goto jcgo_tryallcaught; } } JCGO_TRY_SEHNOP; jcgo_tryallcaught:;
#else
#define JCGO_TRY_CATCHIGNOREALL(tcb) /* empty */
#endif

#ifdef JCGO_SEHTRY
#define JCGO_CALLBACK_BEGIN { struct jcgo_tcb_s *jcgo_tcbsave; jObject jcgo_throwable; int jcgo_interruptReq; JCGO_GET_CURTCB(&jcgo_tcbsave); jcgo_throwable = jcgo_tcbsave->throwable; jcgo_tcbsave->throwable = jnull; jcgo_interruptReq = jcgo_tcbsave->interruptReq; jcgo_tcbsave->interruptReq = 0; jcgo_tcbsave->insideCallback++; { JCGO_TRY_BLOCK {
#define JCGO_CALLBACK_END } JCGO_TRY_LEAVE JCGO_TRY_CATCHIGNOREALL(jcgo_tcbsave) } jcgo_tcbsave->insideCallback--; jcgo_tcbsave->interruptReq = jcgo_interruptReq; jcgo_tcbsave->throwable = jcgo_throwable; }
#else
#define JCGO_CALLBACK_BEGIN { struct jcgo_tcb_s *jcgo_tcbsave; int jcgo_interruptReq; JCGO_GET_CURTCB(&jcgo_tcbsave); jcgo_interruptReq = jcgo_tcbsave->interruptReq; jcgo_tcbsave->interruptReq = 0; jcgo_tcbsave->insideCallback++; { JCGO_TRY_BLOCK {
#define JCGO_CALLBACK_END } JCGO_TRY_LEAVE JCGO_TRY_CATCHIGNOREALL(jcgo_tcbsave) } jcgo_tcbsave->insideCallback--; jcgo_tcbsave->interruptReq = jcgo_interruptReq; }
#endif

#ifndef GCDATAFIRSTSYM
#define GCDATAFIRSTSYM java_lang_String__class
#ifdef JCGO_SEPARATED
#ifndef GCDATALASTSYM
#ifndef GCBSSFIRSTSYM
#ifndef GCBSSLASTSYM
#ifndef JCGO_GCRESETDLS
#define JCGO_MAIN_OMITSETROOTS
#endif
#endif
#endif
#endif
#endif
#endif

#ifndef GCDATALASTSYM
#define GCDATALASTSYM jcgo_noTypesClassArr
#endif

#ifndef GCBSSFIRSTSYM
#define GCBSSFIRSTSYM jcgo_initialized
#endif

#ifndef GCBSSLASTSYM
#define GCBSSLASTSYM jcgo_globData
#endif

#ifdef JCGO_WMAIN
#define JCGO_MAIN_TCHAR wchar_t
#define JCGO_MAIN_TINITINOUT JCGO_JNI_FUNC(jcgo_JavaInitInOutW)
#define JCGO_MAIN_TCONVERTCMDARG JCGO_JNI_FUNC(jcgo_JavaConvertCmdArgW)
#else
#define JCGO_MAIN_TCHAR char
#define JCGO_MAIN_TINITINOUT JCGO_JNI_FUNC(jcgo_JavaInitInOutA)
#define JCGO_MAIN_TCONVERTCMDARG JCGO_JNI_FUNC(jcgo_JavaConvertCmdArgA)
#endif

#ifdef JCGO_ERRTOLOG
#ifdef JCGO_TOLOGFORCE
#ifdef JCGO_ERRSTDOUT
#define JCGO_MAIN_OUTREDIRECT 0x7
#else
#define JCGO_MAIN_OUTREDIRECT 0x5
#endif
#else
#ifdef JCGO_ERRSTDOUT
#define JCGO_MAIN_OUTREDIRECT 0x3
#else
#define JCGO_MAIN_OUTREDIRECT 0x1
#endif
#endif
#else
#define JCGO_MAIN_OUTREDIRECT 0
#endif

#ifdef JCGO_SEHTRY
#ifdef OBJT_java_lang_VMThrowable
#define JCGO_MAIN_TRYINITSEHIF if (!setjmp(jcgo_mainSehJmpBuf()))
#else
#define JCGO_MAIN_TRYINITSEHIF /* empty */
#endif
#else
#define JCGO_MAIN_TRYINITSEHIF /* empty */
#endif

#ifdef JCGO_THREADS
#define JCGO_MAIN_EXITEND if (*(void *volatile *)&jcgo_noTypesClassArr.jcgo_methods != NULL) exit(0);
#else
#ifdef JCGO_NOGC
#define JCGO_MAIN_EXITEND /* empty */
#else
#define JCGO_MAIN_EXITEND *(volatile int *)&jcgo_trashVar = 0;
#endif
#endif

#define JCGO_MAIN_ARGVFIX(argc, targv) (void)((argc) > 0 && (targv) != NULL && (targv)[argc] != NULL ? ((targv)[(argc) - 1] = NULL, 0) : 0)

#ifdef JCGO_NOGC
#define JCGO_MAIN_LAUNCH(argc, targv) { JCGO_MAIN_ARGVFIX(argc, targv); JCGO_MAIN_TRYINITSEHIF jcgo_tmainBody(jcgo_tinitialize(targv)); JCGO_MAIN_EXITEND }
#else
#ifdef JCGO_GCRESETDLS
#define JCGO_MAIN_GCSETNODLS GC_set_no_dls(1)
#else
#define JCGO_MAIN_GCSETNODLS (void)0
#endif
#ifdef JCGO_GCALLINTER
#define JCGO_MAIN_GCNOINTERIOR (void)0
#else
#define JCGO_MAIN_GCNOINTERIOR GC_set_all_interior_pointers(0)
#endif
#define JCGO_MAIN_LAUNCH(argc, targv) { void (*volatile jcgo_fnLaunch)(void **) = &jcgo_tmainBody; JCGO_MAIN_ARGVFIX(argc, targv); JCGO_MAIN_GCSETNODLS; JCGO_MAIN_GCNOINTERIOR; GC_INIT(); JCGO_MAIN_TRYINITSEHIF (*jcgo_fnLaunch)(jcgo_tinitialize(targv)); JCGO_MAIN_EXITEND }
#endif

#ifndef OBJT_java_lang_Throwable
struct java_lang_Throwable_s
{
 CONST struct java_lang_Object_methods_s *JCGO_IMMFLD_CONST jcgo_methods;
 JCGO_MON_DEFN
};
#endif

#ifdef OBJT_java_lang_ref_SoftReference
struct jcgo_refkeeper_s;
#endif

struct jcgo_globData_s
{
 jObject nullExc;
#ifdef OBJT_java_lang_ref_SoftReference
 struct jcgo_refkeeper_s *refKeeperList;
#endif
#ifndef JCGO_NOJNI
 jObjectArr jniGlobalRefsQue;
#ifndef JCGO_FNLZDATA_OMITREFQUE
 jObjectArr jniWeakRefsList;
#endif
 jObjectArr jniAllocatedDataList;
 struct jcgo_tcb_s *jniNewTCB;
#endif
#ifdef JCGO_STDCLINIT
#ifdef JCGO_THREADS
 jObjectArr clinitActiveList;
#endif
#endif
};

JCGO_NOSEP_GCDATA struct jcgo_globData_s jcgo_globData ATTRIBGCBSS =
{
 jnull
#ifdef OBJT_java_lang_ref_SoftReference
 , NULL
#endif
#ifndef JCGO_NOJNI
 , jnull,
#ifndef JCGO_FNLZDATA_OMITREFQUE
 jnull,
#endif
 jnull,
 NULL
#endif
#ifdef JCGO_STDCLINIT
#ifdef JCGO_THREADS
 , jnull
#endif
#endif
};

JCGO_NOSEP_GCDATA struct jcgo_jobjectarr_s jcgo_noTypesClassArr ATTRIBGCDATA =
{
 (jvtable)&jObjectArr2_methods,
 JCGO_MON_INIT
 1,
 JCGO_CLASSREF_OF(java_lang_Class__class),
 {
  jnull
 }
};

STATICDATA JCGO_MAIN_TCHAR *jcgo_targv0 = NULL;

#ifdef JCGO_SEPARATED
jlong jcgo_trashVar;
#endif

#ifdef EXPAND_WILDCARDS
EXPAND_WILDCARDS
#else
#ifdef __MINGW32__
int _CRT_glob = 1;
#endif
#endif

#ifndef JCGO_NOGC
#ifndef JCGO_MAIN_OMITSETROOTS
JCGO_NOSEP_GCDATA void *jcgo_pDataFirstSym = (void *)(&GCDATAFIRSTSYM);
#endif
#endif

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_initFP( void );
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_initAsyncSignals( void );
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_unregSignalHandlers( void );

JNIIMPORT JCGO_MAIN_TCHAR **JNICALL JCGO_MAIN_TINITINOUT(
 JCGO_MAIN_TCHAR **targv, jint redirect );
JNIIMPORT jstring JNICALL JCGO_MAIN_TCONVERTCMDARG( JNIEnv *pJniEnv,
 CONST JCGO_MAIN_TCHAR *tstr );

#ifdef JCGO_NOFATALMSG

#define JCGO_FATAL_ABORT(msg) JCGO_ABORT_EXIT

#else /* JCGO_NOFATALMSG */

JNIIMPORT void JNICALL JCGO_JNI_FUNC(jcgo_JavaWriteLnToStderr)(
 CONST char *cstr, CONST char *cstr2 );

STATIC void CFASTCALL jcgo_printFatalMsg( CONST char *msg )
{
 JCGO_JNI_FUNC(jcgo_JavaWriteLnToStderr)((jcgo_initialized >> 1) != 0 ?
  " !!! FATAL ERROR: " : " !!! FATAL INITIALIZER ERROR: ", msg);
}

#define JCGO_FATAL_ABORT(msg) (jcgo_printFatalMsg(msg), JCGO_ABORT_EXIT)

#endif /* ! JCGO_NOFATALMSG */

JCGO_NOSEP_STATIC void CFASTCALL jcgo_jniNoNativeFunc( void )
{
#ifdef OBJT_java_lang_VMThrowable
 java_lang_VMThrowable__throwUnsatisfiedLinkError0X__();
#else
 JCGO_FATAL_ABORT("Missing native function called!");
#endif
}

#ifdef JCGO_NOJNI

JCGO_NOSEP_INLINE JNIEnv *JCGO_INLFRW_FASTCALL jcgo_jniEnter( void )
{
 struct jcgo_tcb_s *tcb;
 JCGO_GET_CURTCB(&tcb);
#ifdef JCGO_THREADS
 jcgo_saveTCB();
#endif
 tcb->jniEnv = JCGO_PTR_RESERVED;
 return &tcb->jniEnv;
}

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_jniLeave( JNIEnv *pJniEnv,
 jobject obj )
{
 struct jcgo_tcb_s *tcb = JCGO_JNI_GETTCB(pJniEnv);
 jObject ex = tcb->nativeExc;
 tcb->jniEnv = NULL;
 tcb->nativeExc = jnull;
#ifdef JCGO_THREADS
 (void)jcgo_restoreTCB(tcb);
#endif
 jcgo_checkStop(tcb);
 if (JCGO_EXPECT_FALSE(ex != jnull))
  JCGO_THROW_EXC(ex);
 return (jObject)obj;
}

#ifndef JCGO_NATSEP
EXTRASTATIC
#endif
jint *JNICALL jcgo_jnuStringLengthPtr( jstring str )
{
 return &JCGO_FIELD_NZACCESS((java_lang_String)str, count);
}

#ifndef JCGO_NATSEP
EXTRASTATIC
#endif
jchar *JNICALL jcgo_jnuStringChars( jstring str, int *pisbytes )
{
 jchar *chars;
 jObject value =
  (jObject)JCGO_FIELD_NZACCESS((java_lang_String)str, value);
 if (JCGO_METHODS_OF(value)->jcgo_typeid == OBJT_jarray + OBJT_jbyte)
 {
  *pisbytes = 1;
  chars = (jchar *)((volatile void *)&JCGO_ARR_INTERNALACC(jbyte,
           (jbyteArr)value, JCGO_FIELD_NZACCESS((java_lang_String)str,
           offset)));
 }
  else
  {
   *pisbytes = 0;
   chars = &JCGO_ARR_INTERNALACC(jchar, (jcharArr)value,
            JCGO_FIELD_NZACCESS((java_lang_String)str, offset));
  }
 return chars;
}

#ifndef JCGO_NATSEP
EXTRASTATIC
#endif
jstring JNICALL jcgo_jnuStringCreate( JNIEnv *pJniEnv, jint len )
{
 jstring JCGO_TRY_VOLATILE str = NULL;
 jcharArr arr;
 struct jcgo_tcb_s *tcb;
#ifdef JCGO_SEHTRY
 jObject throwable;
#else
 struct jcgo_try_s jcgo_try;
#endif
 if (JCGO_EXPECT_TRUE(pJniEnv != NULL) &&
     (tcb = JCGO_JNI_GETTCB(pJniEnv))->nativeExc == jnull)
 {
#ifndef JCGO_SEHTRY
  jcgo_try.throwable = jnull;
#endif
#ifdef JCGO_THREADS
  tcb->insideCallback++;
  (void)jcgo_restoreTCB(tcb);
#endif
#ifndef JCGO_SEHTRY
  jcgo_try.last = tcb->pCurTry;
  *(struct jcgo_try_s *volatile *)&tcb->pCurTry = &jcgo_try;
#ifdef JCGO_THREADS
  jcgo_try.pCurMon = tcb->pCurMon;
  tcb->pCurMon = NULL;
#endif
#endif
#ifdef JCGO_THREADS
  tcb->insideCallback--;
#endif
  tcb->jniEnv = NULL;
  {
#ifdef JCGO_SEHTRY
   JCGO_TRY_BLOCK
#else
   if (!setjmp(jcgo_try.jbuf))
#endif
   {
    arr = (jcharArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jchar), 0, len);
    str = (jstring)jcgo_newObject((jvtable)&java_lang_String_methods);
    JCGO_FIELD_NZACCESS((java_lang_String)str, value) = (void *)arr;
   }
#ifdef JCGO_SEHTRY
   JCGO_TRY_LEAVE
   JCGO_TRY_CATCHALLSTORE(&throwable)
#endif
  }
#ifndef JCGO_SEHTRY
#ifdef JCGO_THREADS
  tcb->pCurMon = jcgo_try.pCurMon;
#endif
  tcb->pCurTry = jcgo_try.last;
#endif
  tcb->jniEnv = JCGO_PTR_RESERVED;
#ifdef JCGO_THREADS
  jcgo_saveTCB();
#endif
#ifdef JCGO_SEHTRY
  tcb->nativeExc = throwable;
#else
  tcb->nativeExc = jcgo_try.throwable;
#endif
 }
 return (jstring)str;
}

#else /* JCGO_NOJNI */

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_initJni( void );
STATIC void CFASTCALL jcgo_jniOnLoad( void );
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_jniOnUnload( void );

#endif /* ! JCGO_NOJNI */

#ifdef JCGO_STDCLINIT

#ifdef JCGO_THREADS
#ifdef OBJT_java_lang_VMThread
JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMThread__notify0__LoI( java_lang_Object obj, jint all );
JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMThread__wait0__LoJI( java_lang_Object obj, jlong ms, jint ns );
#endif
#endif

JCGO_NOSEP_STATIC void CFASTCALL jcgo_clinitTrig( java_lang_Class aclass )
{
 void (CFASTCALL *rtn)(void);
#ifdef JCGO_THREADS
 struct jcgo_tcb_s *tcb;
 jObjectArr prevEntry;
 JCGO_TRY_VOLATILE int noerr;
 int flags;
 if (JCGO_EXPECT_TRUE((*(volatile jint *)&JCGO_FIELD_NZACCESS(aclass,
     modifiers) & (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_TRANSIENT)) == 0))
 {
  return;
 }
 /* FIXME: use atomic operation to re-check modifiers. */
 noerr = 0;
 {
  JCGO_SYNC_BLOCKSAFENZ(aclass)
  {
   jObjectArr listEntry;
   flags = (int)(*(volatile jint *)&JCGO_FIELD_NZACCESS(aclass, modifiers)) &
            (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_TRANSIENT);
   if (flags == JCGO_ACCMOD_TRANSIENT)
   {
    JCGO_CRITMOD_BEGIN(jcgo_clinitListMutex)
    listEntry = jcgo_globData.clinitActiveList;
    while (listEntry != jnull &&
           JCGO_ARR_INTERNALACC(jObject, listEntry, 0) != (jObject)aclass)
     listEntry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
    JCGO_CRITMOD_END(jcgo_clinitListMutex)
    JCGO_GET_CURTCB(&tcb);
    if (listEntry == jnull ||
        JCGO_ARR_INTERNALACC(jObject, listEntry, 2) == (jObject)tcb)
    {
#ifndef JCGO_SEHTRY
     JCGO_SYNC_JUMPLEAVE(0);
#endif
     return;
    }
    do
    {
#ifdef OBJT_java_lang_VMThread
     java_lang_VMThread__wait0__LoJI((java_lang_Object)aclass, (jlong)0L, 0);
#endif
     flags = (int)JCGO_FIELD_NZACCESS(aclass, modifiers) &
              (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_TRANSIENT);
    } while (flags == JCGO_ACCMOD_TRANSIENT);
   }
   if (!flags)
   {
#ifndef JCGO_SEHTRY
    JCGO_SYNC_JUMPLEAVE(0);
#endif
    return;
   }
   if (JCGO_EXPECT_FALSE(flags == JCGO_ACCMOD_VOLATILE))
   {
#ifdef OBJT_java_lang_VMThrowable
    JCGO_THROW_EXC(java_lang_VMThrowable__createNoClassDefFoundError0X__LsI(
     JCGO_FIELD_NZACCESS(aclass, name), 1));
#else
#ifndef JCGO_SEHTRY
    JCGO_SYNC_JUMPLEAVE(0);
#endif
    return;
#endif
   }
   listEntry = (jObjectArr)jcgo_newArray(
                JCGO_CLASSREF_OF(java_lang_Object__class), 0, 3);
   JCGO_GET_CURTCB(&tcb);
   JCGO_ARR_INTERNALACC(jObject, listEntry, 0) = (jObject)aclass;
   JCGO_ARR_INTERNALACC(jObject, listEntry, 2) = (jObject)tcb;
   JCGO_CRITMOD_BEGIN(jcgo_clinitListMutex)
   JCGO_ARR_INTERNALACC(jObject, listEntry, 1) =
    (jObject)jcgo_globData.clinitActiveList;
   jcgo_globData.clinitActiveList = listEntry;
   JCGO_CRITMOD_END(jcgo_clinitListMutex)
   JCGO_FIELD_NZACCESS(aclass, modifiers) &= ~(jint)JCGO_ACCMOD_VOLATILE;
  }
  JCGO_SYNC_END
 }
 {
  JCGO_TRY_BLOCK
  {
#ifdef OBJT_java_lang_Throwable
   JCGO_TRY_BLOCK
#endif
   {
    if ((rtn = ((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(aclass,
        vmdata)))->jcgo_clinitRtn) != 0)
     (*rtn)();
    noerr = 1;
   }
#ifdef OBJT_java_lang_Throwable
   JCGO_TRY_LEAVE
   JCGO_TRY_CATCHES(1)
   JCGO_TRY_CATCH(OBJT_java_lang_Throwable, MAXT_java_lang_Throwable)
   {
#ifdef OBJT_java_lang_VMThrowable
    java_lang_VMThrowable__throwExceptionInInitializer0X__LoLc(
     (java_lang_Object)JCGO_TRY_THROWABLE(0), aclass);
#endif
   }
   JCGO_TRY_RETHROW(1)
#endif
  }
  JCGO_TRY_LEAVE
  {
   JCGO_SYNC_BLOCKSAFENZ(aclass)
   {
    jObjectArr listEntry;
    if (noerr)
     JCGO_FIELD_NZACCESS(aclass, modifiers) &= ~(jint)JCGO_ACCMOD_TRANSIENT;
     else JCGO_FIELD_NZACCESS(aclass, modifiers) =
           (JCGO_FIELD_NZACCESS(aclass, modifiers) &
           ~(jint)JCGO_ACCMOD_TRANSIENT) | (jint)JCGO_ACCMOD_VOLATILE;
    prevEntry = jnull;
    JCGO_CRITMOD_BEGIN(jcgo_clinitListMutex)
    listEntry = jcgo_globData.clinitActiveList;
    while (listEntry != jnull &&
           JCGO_ARR_INTERNALACC(jObject, listEntry, 0) != (jObject)aclass)
    {
     prevEntry = listEntry;
     listEntry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
    }
    if (listEntry != jnull)
    {
     if (prevEntry != jnull)
      JCGO_ARR_INTERNALACC(jObject, prevEntry, 1) =
       JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
      else jcgo_globData.clinitActiveList =
            (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
    }
    JCGO_CRITMOD_END(jcgo_clinitListMutex)
#ifdef OBJT_java_lang_VMThread
    java_lang_VMThread__notify0__LoI((java_lang_Object)aclass, 1);
#endif
   }
   JCGO_SYNC_END
  }
  JCGO_TRY_FINALLYEND
 }
#else
 if ((JCGO_FIELD_NZACCESS(aclass, modifiers) & JCGO_ACCMOD_VOLATILE) == 0)
  return;
#ifdef OBJT_java_lang_VMThrowable
 if (JCGO_EXPECT_FALSE((JCGO_FIELD_NZACCESS(aclass,
     modifiers) & JCGO_ACCMOD_TRANSIENT) == 0))
  JCGO_THROW_EXC(java_lang_VMThrowable__createNoClassDefFoundError0X__LsI(
   JCGO_FIELD_NZACCESS(aclass, name), 1));
#endif
 if ((rtn = ((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(aclass,
     vmdata)))->jcgo_clinitRtn) != 0)
 {
#ifdef OBJT_java_lang_Throwable
  JCGO_TRY_BLOCK
#endif
  {
   JCGO_FIELD_NZACCESS(aclass, modifiers) &= ~(jint)JCGO_ACCMOD_VOLATILE;
   (*rtn)();
   JCGO_FIELD_NZACCESS(aclass, modifiers) &=
    ~(jint)(JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_TRANSIENT);
  }
#ifdef OBJT_java_lang_Throwable
  JCGO_TRY_LEAVE
  JCGO_TRY_CATCHES(2)
  JCGO_TRY_CATCH(OBJT_java_lang_Throwable, MAXT_java_lang_Throwable)
  {
   JCGO_FIELD_NZACCESS(aclass, modifiers) =
    (JCGO_FIELD_NZACCESS(aclass, modifiers) & ~(jint)JCGO_ACCMOD_TRANSIENT) |
    (jint)JCGO_ACCMOD_VOLATILE;
#ifdef OBJT_java_lang_VMThrowable
   java_lang_VMThrowable__throwExceptionInInitializer0X__LoLc(
    (java_lang_Object)JCGO_TRY_THROWABLE(0), aclass);
#endif
  }
  JCGO_TRY_RETHROW(2)
#endif
 }
  else JCGO_FIELD_NZACCESS(aclass, modifiers) &=
        ~(jint)(JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_TRANSIENT);
#endif
}

#else /* JCGO_STDCLINIT */

#ifdef JCGO_CLINITCHK

JCGO_NOSEP_STATIC void CFASTCALL jcgo_clinitCheckOrder(
 java_lang_Class aclass )
{
#ifdef JCGO_THREADS
 jint mods = *(volatile jint *)&JCGO_FIELD_NZACCESS(aclass, modifiers);
 struct jcgo_tcb_s *tcb;
 if ((mods & (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_TRANSIENT)) == 0)
  return;
 if ((mods & (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_TRANSIENT)) ==
     (jint)JCGO_ACCMOD_TRANSIENT)
 {
  JCGO_GET_CURTCB(&tcb);
  if (tcb == &jcgo_mainTCB)
   return;
 }
#else
 if ((JCGO_FIELD_NZACCESS(aclass, modifiers) & JCGO_ACCMOD_VOLATILE) == 0)
  return;
#endif
 JCGO_FATAL_ABORT("Invalid class initialization order!");
}

#endif /* JCGO_CLINITCHK */

#endif /* ! JCGO_STDCLINIT */

#ifdef JCGO_SEHTRY

EXTRASTATIC void *CFASTCALL jcgo_mainSehJmpBuf( void )
{
#ifdef OBJT_java_lang_VMThrowable
 return jcgo_mainTCB.jbuf;
#else
 return NULL;
#endif
}

#endif /* JCGO_SEHTRY */

EXTRASTATIC void **CFASTCALL jcgo_tinitialize( JCGO_MAIN_TCHAR **targv )
{
#ifndef JCGO_MAIN_OMITSETROOTS
#ifndef JCGO_NOGC
 void *pDataFirstSym = jcgo_pDataFirstSym;
#endif
#endif
 if (!jcgo_initialized)
 {
  *(volatile int *)&jcgo_initialized = 1;
  jcgo_initFP();
  JCGO_MEM_POSTINIT(0);
#ifndef JCGO_NOGC
#ifdef JCGO_GCRESETDLS
  GC_clear_roots();
#endif
#ifndef JCGO_MAIN_OMITSETROOTS
  GC_add_roots(pDataFirstSym < (void *)(&GCDATALASTSYM) ? pDataFirstSym :
   (void *)(&GCDATALASTSYM), pDataFirstSym < (void *)(&GCDATALASTSYM) ?
   (char *)(&GCDATALASTSYM) + sizeof(GCDATALASTSYM) :
   (char *)pDataFirstSym + sizeof(GCDATAFIRSTSYM));
  GC_add_roots((void *)(&GCBSSFIRSTSYM) < (void *)(&GCBSSLASTSYM) ?
   (char *)(&GCBSSFIRSTSYM) : (char *)(&GCBSSLASTSYM),
   (void *)(&GCBSSFIRSTSYM) < (void *)(&GCBSSLASTSYM) ?
   (char *)(&GCBSSLASTSYM) + sizeof(GCBSSLASTSYM) :
   (char *)(&GCBSSFIRSTSYM) + sizeof(GCBSSFIRSTSYM));
  *(void *volatile *)&jcgo_pDataFirstSym = NULL;
#endif
#endif
  if (JCGO_ARRAY_NZLENGTH(JCGO_OBJREF_OF(jcgo_noTypesClassArr)))
   jcgo_mainTCB.jcgo_methods = (jvtable)&java_lang_Object_methods;
  if ((targv = JCGO_MAIN_TINITINOUT(targv, JCGO_MAIN_OUTREDIRECT)) != NULL)
   jcgo_targv0 = targv[0];
#ifdef JCGO_THREADS
  if (jcgo_threadsInit() < 0)
   JCGO_FATAL_ABORT("Could not initialize main thread!");
#endif
  jcgo_setSyncSignals();
 }
#ifndef JCGO_NOJNI
  else if (jcgo_initialized < 0)
   JCGO_FATAL_ABORT("Cannot re-initialize destroyed JavaVM");
#endif
 return (void **)targv;
}

JCGO_NOSEP_INLINE jObjectArr CFASTCALL jcgo_tconvertArgs(
 JCGO_MAIN_TCHAR **targv )
{
#ifdef JCGO_MAINARGS_NOTUSED
 return (jObjectArr)jcgo_newArray(JCGO_CLASSREF_OF(java_lang_String__class),
         0, 0);
#else
 jObjectArr argArr;
 int i;
 int argc = 0;
 if (targv != NULL)
  while (targv[argc] != NULL)
   argc++;
 argArr = (jObjectArr)jcgo_newArray(JCGO_CLASSREF_OF(java_lang_String__class),
           0, (jint)(argc > 0 ? argc - 1 : 0));
 for (i = 1; i < argc; i++)
 {
  JCGO_JNI_BLOCK(0)
  JCGO_ARR_INTERNALACC(jObject, argArr, i - 1) = jcgo_jniLeave(jcgo_pJniEnv,
   (jobject)JCGO_MAIN_TCONVERTCMDARG(jcgo_pJniEnv, targv[i]));
 }
 return argArr;
#endif
}

EXTRASTATIC jObjectArr CFASTCALL jcgo_tpostInit( void **targv )
{
 jObjectArr argArr;
 if (jcgo_initialized == 1)
 {
  jcgo_initAsyncSignals();
#ifndef JCGO_NOJNI
  jcgo_initJni();
#endif
#ifdef JCGO_STDCLINIT
  JCGO_CLINIT_TRIG(java_lang_String__class);
#ifdef OBJT_java_lang_VMThrowable
  JCGO_CLINIT_TRIG(java_lang_VMThrowable__class);
#endif
#ifdef OBJT_java_lang_VMThread_ExitMain
  JCGO_CLINIT_TRIG(java_lang_VMThread_ExitMain__class);
#endif
#ifdef OBJT_java_lang_System
  JCGO_CLINIT_TRIG(java_lang_System__class);
#endif
#else
  jcgo_initClasses();
#ifndef JCGO_NOJNI
  jcgo_jniOnLoad();
#endif
#endif
 }
  else if (((jcgo_initialized + 1) >> 1) == 0)
   return jnull;
 argArr = jcgo_tconvertArgs((JCGO_MAIN_TCHAR **)targv);
 *(volatile int *)&jcgo_initialized = 2;
 return argArr;
}

JCGO_NOSEP_INLINE void CFASTCALL jcgo_atExit( jObject throwable )
{
#ifndef JCGO_NOJNI
 jcgo_jniOnUnload();
#endif
 if (((jcgo_initialized + 1) >> 1) != 0)
 {
  *(volatile int *)&jcgo_initialized = -1;
#ifdef OBJT_java_lang_VMThrowable
  if (throwable != jnull)
  {
#ifdef OBJT_java_lang_OutOfMemoryError
   if (jcgo_instanceOf0(OBJT_java_lang_OutOfMemoryError,
       MAXT_java_lang_OutOfMemoryError, throwable))
   {
    JCGO_MEM_HEAPDESTROY;
    JCGO_FATAL_ABORT("Out of memory!");
   }
#endif
   JCGO_FATAL_ABORT("Internal error in main thread!");
  }
#endif
  JCGO_MEM_HEAPDESTROY;
 }
}

EXTRASTATIC void CFASTCALL jcgo_destroyJavaVM( jObject throwable )
{
#ifdef OBJT_java_lang_VMThread
 jObject ex;
 {
  JCGO_TRY_BLOCK
  {
   java_lang_VMThread__destroyJavaVM0X__LoI((java_lang_Object)throwable,
    (jint)(jcgo_initialized == 1));
  }
  JCGO_TRY_LEAVE
  JCGO_TRY_CATCHALLSTORE(&ex)
 }
 jcgo_atExit(ex);
#else
 jcgo_atExit(throwable);
#endif
 jcgo_unregSignalHandlers();
#ifdef JCGO_THREADS
 (void)JCGO_THREAD_CLOSEHND(&jcgo_mainTCB.thrhandle);
 (void)JCGO_THREADT_CLEAR(&jcgo_mainTCB.thrhandle);
#endif
}

#endif
