/*
 * @(#) $(JCGO)/include/jcgojnie.c --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
 */

/**
 * This file is compiled together with the files produced by the JCGO
 * translator (do not include and/or compile this file directly).
 */

/*
 * Used control macros: JCGO_NOCREATJVM, JCGO_NOGC, JCGO_SEHTRY, JCGO_THREADS.
 * Macros for tuning: JNICALL_INVOKE, JNIEXPORT_INVOKE, JNIONLOADDECLS,
 * JNIONLOADLIST, JNIONUNLOADLIST.
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

#ifndef JNICALL_INVOKE
#define JNICALL_INVOKE JNICALL
#endif

#ifndef JNIEXPORT_INVOKE
#define JNIEXPORT_INVOKE JNIEXPORT
#endif

STATICDATA int jcgo_jniOnLoadDone = 0;

typedef jint (JNICALL *jcgo_jnionload_t)(JavaVM *, void *);
typedef void (JNICALL *jcgo_jnionunload_t)(JavaVM *, void *);

#ifdef JNIONLOADDECLS
#define JNIONLOAD(func) JNIIMPORT jint JNICALL func(JavaVM *, void *);
#define JNIONUNLOAD(func) JNIIMPORT void JNICALL func(JavaVM *, void *);
JNIONLOADDECLS
#endif

STATICDATA jcgo_jnionload_t /* CONST */ jcgo_jniOnLoadList[] =
{
#ifdef JNIONLOADLIST
 JNIONLOADLIST,
#endif
 0
};

STATICDATA jcgo_jnionunload_t /* CONST */ jcgo_jniOnUnloadList[] =
{
#ifdef JNIONUNLOADLIST
 JNIONUNLOADLIST,
#endif
 0
};

STATIC jint JNICALL jcgo_JniGetJavaVM( JNIEnv *pJniEnv, JavaVM **pvm );

STATICDATA CONST struct JNINativeInterface_ jcgo_jniNatIface =
{
 0, 0, 0, 0,
 jcgo_JniGetVersion,
 jcgo_JniDefineClass,
 jcgo_JniFindClass,
 jcgo_JniFromReflectedMethod,
 jcgo_JniFromReflectedField,
 jcgo_JniToReflectedMethod,
 jcgo_JniGetSuperclass,
 jcgo_JniIsAssignableFrom,
 jcgo_JniToReflectedField,
 jcgo_JniThrow,
 jcgo_JniThrowNew,
 jcgo_JniExceptionOccurred,
 jcgo_JniExceptionDescribe,
 jcgo_JniExceptionClear,
 jcgo_JniFatalError,
 jcgo_JniPushLocalFrame,
 jcgo_JniPopLocalFrame,
 jcgo_JniNewGlobalRef,
 jcgo_JniDeleteGlobalRef,
 jcgo_JniDeleteLocalRef,
 jcgo_JniIsSameObject,
 jcgo_JniNewLocalRef,
 jcgo_JniEnsureLocalCapacity,
 jcgo_JniAllocObject,
 jcgo_JniNewObject,
 jcgo_JniNewObjectV,
 jcgo_JniNewObjectA,
 jcgo_JniGetObjectClass,
 jcgo_JniIsInstanceOf,
 jcgo_JniGetMethodID,
 jcgo_JniCallObjectMethod,
 jcgo_JniCallObjectMethodV,
 jcgo_JniCallObjectMethodA,
 jcgo_JniCallBooleanMethod,
 jcgo_JniCallBooleanMethodV,
 jcgo_JniCallBooleanMethodA,
 jcgo_JniCallByteMethod,
 jcgo_JniCallByteMethodV,
 jcgo_JniCallByteMethodA,
 jcgo_JniCallCharMethod,
 jcgo_JniCallCharMethodV,
 jcgo_JniCallCharMethodA,
 jcgo_JniCallShortMethod,
 jcgo_JniCallShortMethodV,
 jcgo_JniCallShortMethodA,
 jcgo_JniCallIntMethod,
 jcgo_JniCallIntMethodV,
 jcgo_JniCallIntMethodA,
 jcgo_JniCallLongMethod,
 jcgo_JniCallLongMethodV,
 jcgo_JniCallLongMethodA,
 jcgo_JniCallFloatMethod,
 jcgo_JniCallFloatMethodV,
 jcgo_JniCallFloatMethodA,
 jcgo_JniCallDoubleMethod,
 jcgo_JniCallDoubleMethodV,
 jcgo_JniCallDoubleMethodA,
 jcgo_JniCallVoidMethod,
 jcgo_JniCallVoidMethodV,
 jcgo_JniCallVoidMethodA,
 jcgo_JniCallNonvirtualObjectMethod,
 jcgo_JniCallNonvirtualObjectMethodV,
 jcgo_JniCallNonvirtualObjectMethodA,
 jcgo_JniCallNonvirtualBooleanMethod,
 jcgo_JniCallNonvirtualBooleanMethodV,
 jcgo_JniCallNonvirtualBooleanMethodA,
 jcgo_JniCallNonvirtualByteMethod,
 jcgo_JniCallNonvirtualByteMethodV,
 jcgo_JniCallNonvirtualByteMethodA,
 jcgo_JniCallNonvirtualCharMethod,
 jcgo_JniCallNonvirtualCharMethodV,
 jcgo_JniCallNonvirtualCharMethodA,
 jcgo_JniCallNonvirtualShortMethod,
 jcgo_JniCallNonvirtualShortMethodV,
 jcgo_JniCallNonvirtualShortMethodA,
 jcgo_JniCallNonvirtualIntMethod,
 jcgo_JniCallNonvirtualIntMethodV,
 jcgo_JniCallNonvirtualIntMethodA,
 jcgo_JniCallNonvirtualLongMethod,
 jcgo_JniCallNonvirtualLongMethodV,
 jcgo_JniCallNonvirtualLongMethodA,
 jcgo_JniCallNonvirtualFloatMethod,
 jcgo_JniCallNonvirtualFloatMethodV,
 jcgo_JniCallNonvirtualFloatMethodA,
 jcgo_JniCallNonvirtualDoubleMethod,
 jcgo_JniCallNonvirtualDoubleMethodV,
 jcgo_JniCallNonvirtualDoubleMethodA,
 jcgo_JniCallNonvirtualVoidMethod,
 jcgo_JniCallNonvirtualVoidMethodV,
 jcgo_JniCallNonvirtualVoidMethodA,
 jcgo_JniGetFieldID,
 jcgo_JniGetObjectField,
 jcgo_JniGetBooleanField,
 jcgo_JniGetByteField,
 jcgo_JniGetCharField,
 jcgo_JniGetShortField,
 jcgo_JniGetIntField,
 jcgo_JniGetLongField,
 jcgo_JniGetFloatField,
 jcgo_JniGetDoubleField,
 jcgo_JniSetObjectField,
 jcgo_JniSetBooleanField,
 jcgo_JniSetByteField,
 jcgo_JniSetCharField,
 jcgo_JniSetShortField,
 jcgo_JniSetIntField,
 jcgo_JniSetLongField,
 jcgo_JniSetFloatField,
 jcgo_JniSetDoubleField,
 jcgo_JniGetStaticMethodID,
 jcgo_JniCallStaticObjectMethod,
 jcgo_JniCallStaticObjectMethodV,
 jcgo_JniCallStaticObjectMethodA,
 jcgo_JniCallStaticBooleanMethod,
 jcgo_JniCallStaticBooleanMethodV,
 jcgo_JniCallStaticBooleanMethodA,
 jcgo_JniCallStaticByteMethod,
 jcgo_JniCallStaticByteMethodV,
 jcgo_JniCallStaticByteMethodA,
 jcgo_JniCallStaticCharMethod,
 jcgo_JniCallStaticCharMethodV,
 jcgo_JniCallStaticCharMethodA,
 jcgo_JniCallStaticShortMethod,
 jcgo_JniCallStaticShortMethodV,
 jcgo_JniCallStaticShortMethodA,
 jcgo_JniCallStaticIntMethod,
 jcgo_JniCallStaticIntMethodV,
 jcgo_JniCallStaticIntMethodA,
 jcgo_JniCallStaticLongMethod,
 jcgo_JniCallStaticLongMethodV,
 jcgo_JniCallStaticLongMethodA,
 jcgo_JniCallStaticFloatMethod,
 jcgo_JniCallStaticFloatMethodV,
 jcgo_JniCallStaticFloatMethodA,
 jcgo_JniCallStaticDoubleMethod,
 jcgo_JniCallStaticDoubleMethodV,
 jcgo_JniCallStaticDoubleMethodA,
 jcgo_JniCallStaticVoidMethod,
 jcgo_JniCallStaticVoidMethodV,
 jcgo_JniCallStaticVoidMethodA,
 jcgo_JniGetStaticFieldID,
 jcgo_JniGetStaticObjectField,
 jcgo_JniGetStaticBooleanField,
 jcgo_JniGetStaticByteField,
 jcgo_JniGetStaticCharField,
 jcgo_JniGetStaticShortField,
 jcgo_JniGetStaticIntField,
 jcgo_JniGetStaticLongField,
 jcgo_JniGetStaticFloatField,
 jcgo_JniGetStaticDoubleField,
 jcgo_JniSetStaticObjectField,
 jcgo_JniSetStaticBooleanField,
 jcgo_JniSetStaticByteField,
 jcgo_JniSetStaticCharField,
 jcgo_JniSetStaticShortField,
 jcgo_JniSetStaticIntField,
 jcgo_JniSetStaticLongField,
 jcgo_JniSetStaticFloatField,
 jcgo_JniSetStaticDoubleField,
 jcgo_JniNewString,
 jcgo_JniGetStringLength,
 jcgo_JniGetStringChars,
 jcgo_JniReleaseStringChars,
 jcgo_JniNewStringUTF,
 jcgo_JniGetStringUTFLength,
 jcgo_JniGetStringUTFChars,
 jcgo_JniReleaseStringUTFChars,
 jcgo_JniGetArrayLength,
 jcgo_JniNewObjectArray,
 jcgo_JniGetObjectArrayElement,
 jcgo_JniSetObjectArrayElement,
 jcgo_JniNewBooleanArray,
 jcgo_JniNewByteArray,
 jcgo_JniNewCharArray,
 jcgo_JniNewShortArray,
 jcgo_JniNewIntArray,
 jcgo_JniNewLongArray,
 jcgo_JniNewFloatArray,
 jcgo_JniNewDoubleArray,
 jcgo_JniGetBooleanArrayElements,
 jcgo_JniGetByteArrayElements,
 jcgo_JniGetCharArrayElements,
 jcgo_JniGetShortArrayElements,
 jcgo_JniGetIntArrayElements,
 jcgo_JniGetLongArrayElements,
 jcgo_JniGetFloatArrayElements,
 jcgo_JniGetDoubleArrayElements,
 jcgo_JniReleaseBooleanArrayElements,
 jcgo_JniReleaseByteArrayElements,
 jcgo_JniReleaseCharArrayElements,
 jcgo_JniReleaseShortArrayElements,
 jcgo_JniReleaseIntArrayElements,
 jcgo_JniReleaseLongArrayElements,
 jcgo_JniReleaseFloatArrayElements,
 jcgo_JniReleaseDoubleArrayElements,
 jcgo_JniGetBooleanArrayRegion,
 jcgo_JniGetByteArrayRegion,
 jcgo_JniGetCharArrayRegion,
 jcgo_JniGetShortArrayRegion,
 jcgo_JniGetIntArrayRegion,
 jcgo_JniGetLongArrayRegion,
 jcgo_JniGetFloatArrayRegion,
 jcgo_JniGetDoubleArrayRegion,
 jcgo_JniSetBooleanArrayRegion,
 jcgo_JniSetByteArrayRegion,
 jcgo_JniSetCharArrayRegion,
 jcgo_JniSetShortArrayRegion,
 jcgo_JniSetIntArrayRegion,
 jcgo_JniSetLongArrayRegion,
 jcgo_JniSetFloatArrayRegion,
 jcgo_JniSetDoubleArrayRegion,
 jcgo_JniRegisterNatives,
 jcgo_JniUnregisterNatives,
 jcgo_JniMonitorEnter,
 jcgo_JniMonitorExit,
 jcgo_JniGetJavaVM,
 jcgo_JniGetStringRegion,
 jcgo_JniGetStringUTFRegion,
 jcgo_JniGetPrimitiveArrayCritical,
 jcgo_JniReleasePrimitiveArrayCritical,
 jcgo_JniGetStringCritical,
 jcgo_JniReleaseStringCritical,
 jcgo_JniNewWeakGlobalRef,
 jcgo_JniDeleteWeakGlobalRef,
 jcgo_JniExceptionCheck,
 jcgo_JniNewDirectByteBuffer,
 jcgo_JniGetDirectBufferAddress,
 jcgo_JniGetDirectBufferCapacity,
 jcgo_JniGetObjectRefType
};

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_initJni( void )
{
#ifdef JCGO_THREADS
 jcgo_globData.jniNewTCB = jcgo_memAlloc(sizeof(struct jcgo_tcb_s),
                            JCGO_PTR_RESERVED);
#endif
}

#ifdef JCGO_THREADS

JCGO_NOSEP_INLINE void CFASTCALL jcgo_jniPrepareNewTCB(struct jcgo_tcb_s *tcb)
{
 struct jcgo_tcb_s *JCGO_TRY_VOLATILE othertcb = NULL;
 {
  JCGO_TRY_BLOCK
  {
   othertcb = jcgo_memAlloc(sizeof(struct jcgo_tcb_s), JCGO_PTR_RESERVED);
  }
  JCGO_TRY_LEAVE
  JCGO_TRY_CATCHIGNOREALL(tcb)
 }
 JCGO_CRITMOD_BEGIN(jcgo_jniMiscAttachMutex)
 jcgo_globData.jniNewTCB = (struct jcgo_tcb_s *)othertcb;
 JCGO_CRITMOD_END(jcgo_jniMiscAttachMutex)
}

#endif /* JCGO_THREADS */

#ifndef JCGO_NOCREATJVM

STATIC JNIEnv *CFASTCALL jcgo_jniVmAttachThread( JavaVM *vm,
 JavaVMAttachArgs *args, int daemon )
{
#ifdef JCGO_THREADS
#ifndef JCGO_NOGC
 struct GC_stack_base stackbase;
#endif
 jObjectArr JCGO_TRY_VOLATILE localObjs;
#ifdef OBJT_java_lang_VMThread
 java_lang_Object groupObj = jnull;
#endif
#endif
 struct jcgo_tcb_s *tcb;
 if (JCGO_EXPECT_FALSE(((jcgo_initialized + 1) >> 1) == 0))
  return NULL;
#ifdef JCGO_THREADS
 tcb = jcgo_getSelfTCB();
 if (JCGO_EXPECT_TRUE(tcb == NULL))
 {
  if (args != NULL && args->version != JNI_VERSION_1_2 &&
      args->version != JNI_VERSION_1_4 && args->version != JNI_VERSION_1_6)
   return NULL;
  for (;;)
  {
   JCGO_CRITMOD_BEGIN(jcgo_jniMiscAttachMutex)
   if ((tcb = (struct jcgo_tcb_s *)(
       *(void *volatile *)&jcgo_globData.jniNewTCB)) != NULL)
    jcgo_globData.jniNewTCB = JCGO_PTR_RESERVED;
   JCGO_CRITMOD_END(jcgo_jniMiscAttachMutex)
   if (JCGO_EXPECT_TRUE(tcb != JCGO_PTR_RESERVED))
    break;
   JCGO_THREAD_YIELD;
  }
  if (JCGO_EXPECT_FALSE(tcb == NULL))
   return NULL;
  if (jcgo_threadInitHandle(tcb) < 0)
  {
   JCGO_CRITMOD_BEGIN(jcgo_jniMiscAttachMutex)
   jcgo_globData.jniNewTCB = NULL;
   JCGO_CRITMOD_END(jcgo_jniMiscAttachMutex)
   return NULL;
  }
  tcb->jcgo_methods = (jvtable)&java_lang_Object_methods;
  (void)JCGO_MUTEX_LOCK(&jcgo_nonParallelMutex);
#ifndef JCGO_NOGC
  if (GC_get_stack_base(&stackbase))
   stackbase.mem_base = (void *)&stackbase;
  tcb->gcAttached = !GC_register_my_thread(&stackbase);
#endif
#ifdef OBJT_java_lang_VMThread
  if (args != NULL)
   groupObj = (java_lang_Object)jcgo_jniDeRef(args->group);
#endif
#ifdef JCGO_SEHTRY
#ifdef OBJT_java_lang_VMThrowable
  if (!setjmp(tcb->jbuf))
#endif
#endif
  {
   jcgo_threadAttachTCB(tcb);
   jcgo_jniPrepareNewTCB(tcb);
   localObjs = jnull;
   {
    JCGO_TRY_BLOCK
    {
     localObjs = (jObjectArr)jcgo_newArray(
                  JCGO_CLASSREF_OF(java_lang_Object__class), 0,
                  (jint)(JCGO_JNI_DEFLOCALREFS + 1));
#ifdef OBJT_java_lang_VMThread
     tcb->thread =
      (jObject)java_lang_VMThread__createAttachedThread0X__LoLsLoI(groupObj,
      args != NULL && args->name != NULL ? jcgo_utfMakeString(args->name) :
      jnull, JCGO_OBJREF_OF(*(java_lang_Object)tcb), (jint)daemon);
#endif
    }
    JCGO_TRY_LEAVE
    JCGO_TRY_CATCHIGNOREALL(tcb)
   }
   if (JCGO_EXPECT_FALSE(localObjs == jnull
#ifdef OBJT_java_lang_VMThread
       || tcb->thread == jnull
#endif
       ))
   {
    jcgo_threadDetachTCB(tcb);
    (void)JCGO_THREAD_CLOSEHND(&tcb->thrhandle);
    (void)JCGO_THREADT_CLEAR(&tcb->thrhandle);
    return NULL;
   }
   tcb->localObjs = (jObjectArr)localObjs;
  }
  jcgo_saveTCB();
  tcb->jniEnv = &jcgo_jniNatIface;
 }
#else
 tcb = &jcgo_mainTCB;
#endif
 if (tcb->jniEnv == NULL)
  jcgo_abortOnJniEnvCorrupted();
 return &tcb->jniEnv;
}

#endif /* ! JCGO_NOCREATJVM */

STATIC jint JNICALL
jcgo_JniVmDestroyJavaVM( JavaVM *vm )
{
#ifndef JCGO_NOCREATJVM
 struct jcgo_tcb_s *tcb;
 jObject ex;
 if (JCGO_EXPECT_TRUE(((jcgo_initialized + 1) >> 1) != 0))
 {
#ifdef JCGO_THREADS
  tcb = jcgo_getSelfTCB();
  if (JCGO_EXPECT_FALSE(tcb == NULL))
   return (jint)JNI_ERR;
#else
  tcb = &jcgo_mainTCB;
#endif
  if (tcb->jniEnv == NULL)
   jcgo_abortOnJniEnvCorrupted();
  tcb->jniEnv = NULL;
  ex = tcb->nativeExc;
  tcb->nativeExc = jnull;
  tcb->localObjs = jnull;
  if (jcgo_restoreTCB(tcb) < 0)
   jcgo_abortOnJniEnvCorrupted();
#ifdef JCGO_SEHTRY
#ifdef OBJT_java_lang_VMThrowable
  if (!setjmp(tcb->jbuf))
#endif
#endif
  {
   jcgo_destroyJavaVM(ex);
  }
  return 0;
 }
#endif
 return (jint)JNI_ERR;
}

STATIC jint JNICALL
jcgo_JniVmAttachCurrentThread( JavaVM *vm, void **penv, void *args )
{
#ifdef JCGO_NOCREATJVM
 *penv = NULL;
 return (jint)JNI_ERR;
#else
 return (jint)((*(JNIEnv **)penv = jcgo_jniVmAttachThread(vm,
         (JavaVMAttachArgs *)args, 0)) != NULL ? 0 : JNI_ERR);
#endif
}

STATIC jint JNICALL
jcgo_JniVmAttachCurrentThreadAsDaemon( JavaVM *vm, void **penv, void *args )
{
#ifdef JCGO_NOCREATJVM
 *penv = NULL;
 return (jint)JNI_ERR;
#else
 return (jint)((*(JNIEnv **)penv = jcgo_jniVmAttachThread(vm,
         (JavaVMAttachArgs *)args, 1)) != NULL ? 0 : JNI_ERR);
#endif
}

STATIC jint JNICALL
jcgo_JniVmDetachCurrentThread( JavaVM *vm )
{
#ifndef JCGO_NOCREATJVM
#ifdef JCGO_THREADS
 struct jcgo_tcb_s *tcb;
#ifdef OBJT_java_lang_VMThread
 jObject ex;
#endif
 if (JCGO_EXPECT_TRUE(((jcgo_initialized + 1) >> 1) != 0))
 {
  tcb = jcgo_getSelfTCB();
  if (JCGO_EXPECT_FALSE(tcb == NULL || tcb == &jcgo_mainTCB))
   return 0;
  if (tcb->jniEnv == NULL)
   jcgo_abortOnJniEnvCorrupted();
  tcb->jniEnv = NULL;
#ifdef OBJT_java_lang_VMThread
  ex = tcb->nativeExc;
#endif
  tcb->nativeExc = jnull;
  tcb->localObjs = jnull;
  if (jcgo_restoreTCB(tcb) < 0)
   jcgo_abortOnJniEnvCorrupted();
#ifdef OBJT_java_lang_VMThread
#ifdef JCGO_SEHTRY
#ifdef OBJT_java_lang_VMThrowable
  if (!setjmp(tcb->jbuf))
#endif
#endif
  {
   JCGO_TRY_BLOCK
   {
    java_lang_VMThread__detachThread0X__Lo((java_lang_Object)ex);
   }
   JCGO_TRY_LEAVE
   JCGO_TRY_CATCHIGNOREALL(tcb)
  }
#endif
  jcgo_threadDetachTCB(tcb);
  (void)JCGO_THREAD_CLOSEHND(&tcb->thrhandle);
  (void)JCGO_THREADT_CLEAR(&tcb->thrhandle);
  return 0;
 }
#endif
#endif
 return (jint)JNI_ERR;
}

STATIC jint JNICALL
jcgo_JniVmGetEnv( JavaVM *vm, void **penv, jint version )
{
 struct jcgo_tcb_s *tcb;
 if (JCGO_EXPECT_FALSE(!jcgo_initialized))
 {
  *penv = NULL;
  return (jint)JNI_EDETACHED;
 }
#ifdef JCGO_THREADS
 tcb = jcgo_getSelfTCB();
 if (JCGO_EXPECT_FALSE(tcb == NULL))
 {
  *penv = NULL;
  return (jint)JNI_EDETACHED;
 }
#else
 tcb = &jcgo_mainTCB;
#endif
 if (JCGO_EXPECT_FALSE(version < (jint)JNI_VERSION_1_1 ||
     version > (jint)JNI_VERSION_1_6))
 {
  *penv = NULL;
  return (jint)JNI_EVERSION;
 }
 if (tcb->jniEnv == NULL)
  jcgo_abortOnJniEnvCorrupted();
 *(JNIEnv **)penv = &tcb->jniEnv;
 return 0;
}

STATICDATA CONST struct JNIInvokeInterface_ jcgo_jniInvokeIface =
{
 0, 0, 0,
 jcgo_JniVmDestroyJavaVM,
 jcgo_JniVmAttachCurrentThread,
 jcgo_JniVmDetachCurrentThread,
 jcgo_JniVmGetEnv,
 jcgo_JniVmAttachCurrentThreadAsDaemon
};

STATICDATA JavaVM CONST jcgo_jniJavaVM = &jcgo_jniInvokeIface;

STATIC jint JNICALL
jcgo_JniGetJavaVM( JNIEnv *pJniEnv, JavaVM **pvm )
{
 *pvm = (JavaVM *)&jcgo_jniJavaVM;
 return 0;
}

#ifndef JCGO_NOCREATJVM

JNIEXPORT_INVOKE jint JNICALL_INVOKE
JNI_GetDefaultJavaVMInitArgs( void *args )
{
 JavaVMInitArgs *pInitArgs = (JavaVMInitArgs *)args;
 if (pInitArgs->version != (jint)JNI_VERSION_1_2 &&
     pInitArgs->version != (jint)JNI_VERSION_1_4 &&
     pInitArgs->version != (jint)JNI_VERSION_1_6)
  return (jint)JNI_EVERSION;
 pInitArgs->version = (jint)JNI_VERSION_1_6;
 pInitArgs->nOptions = 0;
 pInitArgs->options = NULL;
 pInitArgs->ignoreUnrecognized = (jboolean)JNI_TRUE;
 return 0;
}

EXTRASTATIC int /* CFASTCALL */
jcgo_jniTCreateJavaVM( void **targv )
{
 jObject throwable;
 {
  JCGO_TRY_BLOCK
  {
   (void)jcgo_tpostInit(targv);
   jcgo_mainTCB.localObjs = (jObjectArr)jcgo_newArray(
                             JCGO_CLASSREF_OF(java_lang_Object__class), 0,
                             (jint)(JCGO_JNI_DEFLOCALREFS + 1));
  }
  JCGO_TRY_LEAVE
  JCGO_TRY_CATCHALLSTORE(&throwable)
 }
 if (JCGO_EXPECT_FALSE(throwable != jnull))
 {
  jcgo_destroyJavaVM(throwable);
  return -1;
 }
 return 0;
}

JNIEXPORT_INVOKE jint JNICALL_INVOKE
JNI_CreateJavaVM( JavaVM **pvm, void **penv, void *args )
{
#ifdef JCGO_NOGC
 int (*fnLaunch)(void **);
#else
 int (*volatile fnLaunch)(void **);
#endif
 if (JCGO_EXPECT_TRUE(!jcgo_initialized))
 {
  fnLaunch = &jcgo_jniTCreateJavaVM;
#ifndef JCGO_NOGC
  JCGO_MAIN_GCSETNODLS;
  JCGO_MAIN_GCNOINTERIOR;
  GC_INIT();
#endif
#ifdef JCGO_SEHTRY
#ifdef OBJT_java_lang_VMThrowable
  if (!setjmp(jcgo_mainTCB.jbuf))
#endif
#endif
  {
   if ((*fnLaunch)(jcgo_tinitialize(NULL)) >= 0)
   {
#ifdef JCGO_THREADS
    jcgo_saveTCB();
#endif
    jcgo_mainTCB.jniEnv = &jcgo_jniNatIface;
    *(JNIEnv **)penv = &jcgo_mainTCB.jniEnv;
    *pvm = (JavaVM *)&jcgo_jniJavaVM;
    return 0;
   }
  }
 }
 *pvm = NULL;
 *penv = NULL;
 return (jint)JNI_ERR;
}

#endif /* ! JCGO_NOCREATJVM */

JNIEXPORT_INVOKE jint JNICALL_INVOKE
JNI_GetCreatedJavaVMs( JavaVM **vmBuf, jsize bufLen, jsize *nVMs )
{
 if (JCGO_EXPECT_FALSE(((jcgo_initialized + 1) >> 1) == 0))
 {
  *nVMs = 0;
  return 0;
 }
 *nVMs = 1;
 if (JCGO_EXPECT_TRUE((jint)bufLen > 0))
  vmBuf[0] = (JavaVM *)&jcgo_jniJavaVM;
 return 0;
}

JCGO_NOSEP_STATIC JNIEnv *CFASTCALL jcgo_jniEnterX( jObjectArr localObjs )
{
 struct jcgo_tcb_s *tcb;
 JCGO_GET_CURTCB(&tcb);
 *(jvtable *)&JCGO_METHODS_OF(localObjs) = (jvtable)&jObjectArr_methods;
 JCGO_FIELD_NZACCESS(localObjs, jcgo_component) =
  JCGO_CLASSREF_OF(java_lang_Object__class);
#ifdef JCGO_SEHTRY
#ifndef JCGO_NOCREATJVM
 tcb->insideJniCall++;
#endif
#endif
 tcb->localObjs = localObjs;
#ifdef JCGO_THREADS
 jcgo_saveTCB();
#endif
 tcb->jniEnv = &jcgo_jniNatIface;
 return &tcb->jniEnv;
}

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_jniLeave( JNIEnv *pJniEnv,
 jobject obj )
{
 jObject ex;
 jObject jobj = jcgo_jniDeRef(obj);
 struct jcgo_tcb_s *tcb;
 if ((tcb = JCGO_JNI_GETTCB(pJniEnv))->jniEnv != &jcgo_jniNatIface ||
     (tcb->jniEnv = NULL, ex = tcb->nativeExc, tcb->nativeExc = jnull,
     tcb->localObjs = jnull, jcgo_restoreTCB(tcb) < 0))
  jcgo_abortOnJniEnvCorrupted();
  else
  {
#ifdef JCGO_SEHTRY
#ifndef JCGO_NOCREATJVM
   tcb->insideJniCall--;
#endif
#endif
   jcgo_checkStop(tcb);
   if (JCGO_EXPECT_FALSE(ex != jnull))
    JCGO_THROW_EXC(ex);
  }
 return jobj;
}

STATIC void CFASTCALL jcgo_jniOnLoad( void )
{
 int i;
 if (!jcgo_jniOnLoadDone && ((jcgo_initialized + 1) >> 1) != 0)
 {
  i = 0;
  JCGO_CRITMOD_BEGIN(jcgo_jniMiscAttachMutex)
  if (JCGO_EXPECT_TRUE(!(*(volatile int *)&jcgo_jniOnLoadDone)))
  {
   *(volatile int *)&jcgo_jniOnLoadDone = 1;
   i = -1;
  }
  JCGO_CRITMOD_END(jcgo_jniMiscAttachMutex)
  if (JCGO_EXPECT_TRUE(i < 0))
  {
   while (jcgo_jniOnLoadList[++i])
   {
    JCGO_JNI_BLOCK(0)
    (*jcgo_jniOnLoadList[i])((JavaVM *)&jcgo_jniJavaVM, NULL);
    jcgo_jniLeave(jcgo_pJniEnv, NULL);
   }
   jcgo_jniOnLoadDone = 2;
  }
 }
}

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_jniOnUnload( void )
{
#ifdef JCGO_SEHTRY
 struct jcgo_tcb_s *tcb;
#endif
 int i;
 if (JCGO_EXPECT_TRUE(jcgo_jniOnLoadDone > 1))
 {
  *(volatile int *)&jcgo_jniOnLoadDone = -1;
#ifdef JCGO_SEHTRY
  JCGO_GET_CURTCB(&tcb);
#endif
  for (i = 0; jcgo_jniOnUnloadList[i]; i++)
  {
   JCGO_TRY_BLOCK
   {
    JCGO_JNI_BLOCK(0)
    (*jcgo_jniOnUnloadList[i])((JavaVM *)&jcgo_jniJavaVM, NULL);
    jcgo_jniLeave(jcgo_pJniEnv, NULL);
   }
   JCGO_TRY_LEAVE
   JCGO_TRY_CATCHIGNOREALL(tcb)
  }
 }
}

#endif
