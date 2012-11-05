/*
 * @(#) $(JCGO)/include/jcgojniv.c --
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

#ifndef JCGO_JNILOCKEDOBJS_DEFLEN
#define JCGO_JNILOCKEDOBJS_DEFLEN 28
#endif

#define JCGO_ACCMOD_INTERFACE 0x200
#define JCGO_ACCMOD_ABSTRACT 0x400

#ifdef JCGO_SEHTRY
#ifdef OBJT_java_lang_VMThrowable
#ifdef JCGO_NOCREATJVM
#define JCGO_NATCBACK_BEGIN(pJniEnv) { jObject jcgo_throwable; jObjectArr jcgo_localObjs; (void)jcgo_jniCallbackEnter(pJniEnv, &jcgo_throwable, &jcgo_localObjs); { JCGO_TRY_BLOCK {
#else
#define JCGO_NATCBACK_BEGIN(pJniEnv) { jObject jcgo_throwable; jObjectArr jcgo_localObjs; if (jcgo_jniCallbackEnter(pJniEnv, &jcgo_throwable, &jcgo_localObjs) || !setjmp(JCGO_JNI_GETTCB(pJniEnv)->jbuf)) { JCGO_TRY_BLOCK {
#endif
#else
#define JCGO_NATCBACK_BEGIN(pJniEnv) { jObject jcgo_throwable; jObjectArr jcgo_localObjs; (void)jcgo_jniCallbackEnter(pJniEnv, &jcgo_throwable, &jcgo_localObjs); { JCGO_TRY_BLOCK {
#endif
#define JCGO_NATCBACK_END(pJniEnv) } JCGO_TRY_LEAVE { if (JCGO_JNI_GETTCB(pJniEnv)->throwable != jnull) { jcgo_throwable = JCGO_JNI_GETTCB(pJniEnv)->throwable; JCGO_JNI_GETTCB(pJniEnv)->throwable = jnull; goto jcgo_trynatcaught; } } JCGO_TRY_SEHNOP; jcgo_trynatcaught:; } jcgo_jniCallbackLeave(pJniEnv, jcgo_throwable, jcgo_localObjs); }
#else
#define JCGO_NATCBACK_BEGIN(pJniEnv) { struct jcgo_try_s jcgo_try; jObjectArr jcgo_localObjs; jcgo_jniCallbackEnter(pJniEnv, &jcgo_try, &jcgo_localObjs); if (!setjmp(jcgo_try.jbuf)) {
#define JCGO_NATCBACK_END(pJniEnv) } jcgo_jniCallbackLeave(pJniEnv, jcgo_localObjs); }
#endif

STATICDATA CONST struct JNINativeInterface_ jcgo_jniNatIface;

STATIC jint JNICALL
jcgo_JniEnsureLocalCapacity( JNIEnv *pJniEnv, jint capacity );

JCGO_NOSEP_INLINE void CFASTCALL jcgo_abortOnJniEnvCorrupted( void )
{
 JCGO_FATAL_ABORT("JNIEnv corrupted!");
}

JCGO_NOSEP_INLINE void CFASTCALL jcgo_abortOnNonThrowable( void )
{
 JCGO_FATAL_ABORT("Cannot throw non-throwable object!");
}

#ifndef OBJT_java_lang_VMClass

JCGO_NOSEP_INLINE void CFASTCALL jcgo_abortOnVMClassNotFound( void )
{
 JCGO_FATAL_ABORT("Cannot find java.lang.VMClass!");
}

#endif /* ! OBJT_java_lang_VMClass */

#ifdef JCGO_NOGC
JCGO_NOSEP_INLINE
#else
#ifdef JCGO_THREADS
JCGO_NOSEP_STATIC
#else
JCGO_NOSEP_INLINE
#endif
#endif
jObject CFASTCALL jcgo_jniDeRef( jobject obj )
{
 if (obj != NULL)
 {
#ifndef JCGO_NOGC
#ifdef JCGO_THREADS
#ifndef JCGO_FNLZDATA_OMITREFQUE
  if (JCGO_EXPECT_FALSE(*(void **)((volatile char *)obj -
      (int)sizeof(void *)) == JCGO_PTR_RESERVED))
   return *(jObject volatile *)obj != jnull ?
           (jObject)JCGO_MEM_CALLWITHSYNC(&jcgo_refGetPtr, obj) : jnull;
#endif
#endif
#endif
  return *(jObject *)obj;
 }
 return jnull;
}

STATIC jobject CFASTCALL jcgo_jniToLocalRef( JNIEnv *pJniEnv, jObject obj )
{
 jObjectArr localObjs;
 jObjectArr prevLocalObjs;
 jObject *pobj;
 jint len;
 if (obj != jnull)
  for (;;)
  {
   localObjs = JCGO_JNI_GETTCB(pJniEnv)->localObjs;
   while (localObjs != jnull)
   {
    len = JCGO_ARRAY_NZLENGTH(localObjs);
    while (--len > 0)
     if (JCGO_ARR_INTERNALACC(jObject, localObjs, len) == jnull)
     {
      *(pobj = &JCGO_ARR_INTERNALACC(jObject, localObjs, len)) = obj;
      return (jobject)pobj;
     }
    prevLocalObjs = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, localObjs, 1);
    if ((jObjectArr)JCGO_ARR_INTERNALACC(jObject, localObjs, 0) !=
        prevLocalObjs)
     break;
    localObjs = prevLocalObjs;
   }
   if (jcgo_JniEnsureLocalCapacity(pJniEnv, 1))
    JCGO_FATAL_ABORT("Too many JNI local references!");
  }
 return NULL;
}

#ifdef JCGO_SEHTRY

STATIC int CFASTCALL jcgo_jniCallbackEnter( JNIEnv *pJniEnv,
 jObject *pthrowable, jObjectArr *plocalObjs )
{
 struct jcgo_tcb_s *tcb;
 if (pJniEnv == NULL)
  jcgo_abortOnJniEnvCorrupted();
 if ((tcb = JCGO_JNI_GETTCB(pJniEnv))->jniEnv != &jcgo_jniNatIface ||
     (tcb->insideCallback++, jcgo_restoreTCB(tcb) < 0))
  jcgo_abortOnJniEnvCorrupted();
 *pthrowable = tcb->nativeExc;
 tcb->nativeExc = jnull;
 *plocalObjs = tcb->localObjs;
 tcb->localObjs = jnull;
 tcb->insideCallback--;
 tcb->jniEnv = NULL;
#ifdef JCGO_NOCREATJVM
 return 0;
#else
 return (int)tcb->insideJniCall;
#endif
}

JCGO_NOSEP_INLINE void CFASTCALL jcgo_jniCallbackLeave( JNIEnv *pJniEnv,
 jObject throwable, jObjectArr localObjs )
{
 struct jcgo_tcb_s *tcb;
 (tcb = JCGO_JNI_GETTCB(pJniEnv))->jniEnv = &jcgo_jniNatIface;
 tcb->localObjs = localObjs;
#ifdef JCGO_THREADS
 jcgo_saveTCB();
#endif
 tcb->nativeExc = throwable;
}

#else /* JCGO_SEHTRY */

STATIC void CFASTCALL jcgo_jniCallbackEnter( JNIEnv *pJniEnv,
 struct jcgo_try_s *pCurTry, jObjectArr *plocalObjs )
{
 struct jcgo_tcb_s *tcb;
 if (JCGO_EXPECT_FALSE(pJniEnv == NULL))
  jcgo_abortOnJniEnvCorrupted();
 if ((tcb = JCGO_JNI_GETTCB(pJniEnv))->jniEnv != &jcgo_jniNatIface ||
     (tcb->insideCallback++, jcgo_restoreTCB(tcb) < 0))
  jcgo_abortOnJniEnvCorrupted();
 pCurTry->throwable = tcb->nativeExc;
 pCurTry->last = tcb->pCurTry;
 tcb->pCurTry = pCurTry;
#ifdef JCGO_THREADS
 pCurTry->pCurMon = tcb->pCurMon;
 tcb->pCurMon = NULL;
#endif
 tcb->nativeExc = jnull;
 *plocalObjs = tcb->localObjs;
 tcb->localObjs = jnull;
 tcb->insideCallback--;
 tcb->jniEnv = NULL;
}

STATIC void CFASTCALL jcgo_jniCallbackLeave( JNIEnv *pJniEnv,
 jObjectArr localObjs )
{
 struct jcgo_tcb_s *tcb = JCGO_JNI_GETTCB(pJniEnv);
 struct jcgo_try_s *pCurTry = tcb->pCurTry;
#ifdef JCGO_THREADS
 tcb->pCurMon = pCurTry->pCurMon;
#endif
 tcb->pCurTry = pCurTry->last;
 tcb->jniEnv = &jcgo_jniNatIface;
 tcb->localObjs = localObjs;
#ifdef JCGO_THREADS
 jcgo_saveTCB();
#endif
 tcb->nativeExc = pCurTry->throwable;
}

#endif /* ! JCGO_SEHTRY */

STATIC void CFASTCALL jcgo_jniThrowNullPointerException( JNIEnv *pJniEnv )
{
#ifdef OBJT_java_lang_VMThrowable
 JCGO_NATCBACK_BEGIN(pJniEnv)
 JCGO_THROW_EXC(java_lang_VMThrowable__createNullPointerException0X__());
 JCGO_NATCBACK_END(pJniEnv)
#endif
}

JCGO_NOSEP_INLINE void CFASTCALL jcgo_jniHandleInstanceOfNullClass(
 JNIEnv *pJniEnv )
{
 jObject nativeExc;
 if ((nativeExc = JCGO_JNI_GETTCB(pJniEnv)->nativeExc) != jnull)
 {
#ifdef OBJT_java_lang_NoClassDefFoundError
  if (jcgo_instanceOf0(OBJT_java_lang_NoClassDefFoundError,
      MAXT_java_lang_NoClassDefFoundError, nativeExc))
   JCGO_JNI_GETTCB(pJniEnv)->nativeExc = jnull;
#endif
 }
  else jcgo_jniThrowNullPointerException(pJniEnv);
}

STATIC jint JNICALL
jcgo_JniGetVersion( JNIEnv *pJniEnv )
{
 return (jint)JNI_VERSION_1_6;
}

STATIC jclass JNICALL
jcgo_JniDefineClass( JNIEnv *pJniEnv, CONST char *name, jobject loader,
 CONST jbyte *buf, jsize bufLen )
{
#ifdef OBJT_java_lang_VMClassLoader_ClassParser
 java_lang_Class JCGO_TRY_VOLATILE aclass;
 jbyteArr buffer;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 if (JCGO_EXPECT_FALSE(buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 aclass = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 buffer = (jbyteArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jbyte), 0,
           (jint)bufLen);
 JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jbyte, buffer, 0), (void *)buf,
  (JCGO_ALLOCSIZE_T)bufLen);
 aclass = java_lang_VMClassLoader_ClassParser__defineClass0X__LsLoBA(
           JCGO_EXPECT_TRUE(name != NULL) ? jcgo_utfMakeString(name) : jnull,
           (java_lang_Object)jcgo_jniDeRef(loader), buffer);
 JCGO_NATCBACK_END(pJniEnv)
 return (jclass)jcgo_jniToLocalRef(pJniEnv, (jObject)aclass);
#else
 if (*(void *volatile *)&jcgo_noTypesClassArr.jcgo_methods != NULL)
  JCGO_FATAL_ABORT("Cannot find java.lang.VMClassLoader$ClassParser!");
 return NULL;
#endif
}

STATIC jclass JNICALL
jcgo_JniFindClass( JNIEnv *pJniEnv, CONST char *name )
{
 java_lang_Class baseClass;
 java_lang_Class JCGO_TRY_VOLATILE aclass;
 java_lang_String str;
 jcharArr arr;
 unsigned len;
 unsigned dims;
 int i;
 int j;
 unsigned char ch;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 if (JCGO_EXPECT_FALSE(name == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 dims = 0;
 while (*(name + dims) == (char)0x5b) /*'['*/
  dims++;
 aclass = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 len = dims;
 while (*(name + len))
  len++;
 baseClass = jnull;
 if (dims && (int)(len - dims) == 1)
  switch ((int)(*(name + dims)))
  {
  case 0x5a: /*'Z'*/
   baseClass = JCGO_CORECLASS_FOR(OBJT_jboolean);
   break;
  case 0x42: /*'B'*/
   baseClass = JCGO_CORECLASS_FOR(OBJT_jbyte);
   break;
  case 0x43: /*'C'*/
   baseClass = JCGO_CORECLASS_FOR(OBJT_jchar);
   break;
  case 0x53: /*'S'*/
   baseClass = JCGO_CORECLASS_FOR(OBJT_jshort);
   break;
  case 0x49: /*'I'*/
   baseClass = JCGO_CORECLASS_FOR(OBJT_jint);
   break;
  case 0x4a: /*'J'*/
   baseClass = JCGO_CORECLASS_FOR(OBJT_jlong);
   break;
  case 0x46: /*'F'*/
   baseClass = JCGO_CORECLASS_FOR(OBJT_jfloat);
   break;
  case 0x44: /*'D'*/
   baseClass = JCGO_CORECLASS_FOR(OBJT_jdouble);
   break;
  default:
   break;
  }
 str = jnull;
 if ((!dims || *(name + len - 1) == (char)0x3b) && /*';'*/
     len <= (((unsigned)-1) >> 1) - (unsigned)16)
 {
  i = (int)dims;
  if (*(name + dims) == (char)0x4c && /*'L'*/
      *(name + len - 1) == (char)0x3b) /*';'*/
  {
   i++;
   len--;
  }
  if (JCGO_EXPECT_TRUE(i < (int)len))
  {
   arr = (jcharArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jchar), 0,
          (int)len - i);
   str = (java_lang_String)jcgo_newObject((jvtable)&java_lang_String_methods);
   JCGO_FIELD_NZACCESS(str, value) = (void *)arr;
   JCGO_FIELD_NZACCESS(str, count) = (int)len - i;
   j = 0;
   do
   {
    ch = (unsigned char)(*(name + i));
    if (JCGO_EXPECT_FALSE(ch == (unsigned char)0x2e || /*'.'*/
        ch > (unsigned char)0x7f))
    {
     str = jnull;
     break;
    }
    JCGO_ARR_INTERNALACC(jchar, arr, j) =
     (jchar)(ch != (unsigned char)0x2f ? /*'/'*/
     ch : (unsigned char)0x2e); /*'.'*/
    j++;
   } while (++i < (int)len);
  }
 }
 if (JCGO_EXPECT_TRUE(str != jnull))
  baseClass = jcgo_findClass(str, 0);
 if (JCGO_EXPECT_TRUE(baseClass != jnull))
 {
  if (dims)
  {
#ifdef OBJT_java_lang_VMClass
   aclass = java_lang_VMClass__arrayClassOf0X__LcI(baseClass, (jint)dims);
#else
   jcgo_abortOnVMClassNotFound();
#endif
  }
   else
   {
    aclass = baseClass;
   }
 }
#ifdef OBJT_java_lang_VMThrowable
  else JCGO_THROW_EXC(
        java_lang_VMThrowable__createNoClassDefFoundError0X__LsI(str, 0));
#endif
 JCGO_NATCBACK_END(pJniEnv)
 return (jclass)jcgo_jniToLocalRef(pJniEnv, (jObject)aclass);
}

STATIC jclass JNICALL
jcgo_JniGetSuperclass( JNIEnv *pJniEnv, jclass clazz )
{
 java_lang_Class aclass;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 if (JCGO_EXPECT_FALSE(aclass == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 return (jclass)jcgo_jniToLocalRef(pJniEnv,
         (unsigned)((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(aclass,
         vmdata)))->jcgo_typeid - (unsigned)(OBJT_jarray + 1) >=
         (unsigned)(OBJT_void + JCGO_DIMS_MAX - 1) ?
         (jObject)JCGO_FIELD_NZACCESS(aclass, superclass) :
         (jObject)JCGO_CLASSREF_OF(java_lang_Object__class));
}

STATIC jboolean JNICALL
jcgo_JniIsAssignableFrom( JNIEnv *pJniEnv, jclass subclass, jclass clazz )
{
 java_lang_Class srcClass = (java_lang_Class)jcgo_jniDeRef((jobject)subclass);
 java_lang_Class aclass;
 if (srcClass == jnull ||
     (aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz)) == jnull)
 {
  jcgo_jniHandleInstanceOfNullClass(pJniEnv);
  return (jboolean)JNI_FALSE;
 }
 return (jboolean)(jcgo_isAssignable(srcClass, aclass, 0, 0) ? JNI_TRUE :
         JNI_FALSE);
}

STATIC jint JNICALL
jcgo_JniThrow( JNIEnv *pJniEnv, jthrowable obj )
{
#ifdef OBJT_java_lang_Throwable
 jObject jobj;
 struct jcgo_tcb_s *tcb = JCGO_JNI_GETTCB(pJniEnv);
 if (JCGO_EXPECT_FALSE(tcb->nativeExc != jnull))
  return (jint)JNI_ERR;
 jobj = jcgo_jniDeRef((jobject)obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jint)JNI_ERR;
 }
 if (!jcgo_instanceOf0(OBJT_java_lang_Throwable, MAXT_java_lang_Throwable,
     jobj))
  jcgo_abortOnNonThrowable();
#ifdef JCGO_THREADS
 if (JCGO_EXPECT_FALSE(tcb->stopExc != jnull))
  return (jint)JNI_ERR;
#endif
 tcb->nativeExc = jobj;
#else
 jcgo_abortOnNonThrowable();
#endif
 return 0;
}

STATIC jint JNICALL
jcgo_JniThrowNew( JNIEnv *pJniEnv, jclass clazz, CONST char *msg )
{
#ifdef OBJT_java_lang_Throwable
 java_lang_Class aclass;
 java_lang_String str;
 java_lang_String name;
 jObject jobj;
 struct jcgo_jobjectarr_s jcgo_stackobj1;
 CONST struct jcgo_reflect_s *jcgo_reflect;
 CONST struct jcgo_methodentry_s *pentry;
 jObject (CFASTCALL *rtn)(jObject) = 0;
 jObjectArr methodsTypes;
 jObjectArr methodsName;
 jObjectArr methodsDims;
 jObjectArr argTypes;
 jbyteArr paramDims;
 jObjectArr objectArgs;
 int slotNoArg;
 int slotStrArg;
 int i;
 int count;
 JCGO_TRY_VOLATILE int res;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jint)JNI_ERR;
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 if (JCGO_EXPECT_FALSE(aclass == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jint)JNI_ERR;
 }
#ifdef JCGO_THREADS
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->stopExc != jnull))
  return (jint)JNI_ERR;
#endif
 res = (int)JNI_ERR;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 str = msg != NULL ? jcgo_utfMakeString(msg) : jnull;
 jobj = jnull;
 pentry = NULL;
 slotNoArg = -1;
 slotStrArg = -1;
 if (JCGO_EXPECT_TRUE((JCGO_FIELD_NZACCESS(aclass, modifiers) &
     (JCGO_ACCMOD_INTERFACE | JCGO_ACCMOD_ABSTRACT)) == 0))
 {
  jcgo_reflect = ((jvtable)&JCGO_METHODS_OF(
                  JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_reflect;
  if (JCGO_EXPECT_TRUE(jcgo_reflect != NULL) &&
      (methodsTypes = jcgo_reflect->methodsTypes) != jnull)
  {
   count = (int)JCGO_ARRAY_NZLENGTH(methodsTypes);
   methodsName = jcgo_reflect->methodsName;
   methodsDims = jcgo_reflect->methodsDims;
   pentry = jcgo_reflect->methodsEntry;
   for (i = 0; i < count; i++)
    if (methodsName == jnull || (name = (java_lang_String)
        JCGO_ARR_INTERNALACC(jObject, methodsName, i)) == jnull ||
        !JCGO_FIELD_NZACCESS(name, count))
    {
     argTypes = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, methodsTypes, i);
     if (argTypes != jnull && JCGO_ARRAY_NZLENGTH(argTypes))
     {
      if (str != jnull && JCGO_ARRAY_NZLENGTH(argTypes) == 1 &&
          (java_lang_Class)JCGO_ARR_INTERNALACC(jObject, argTypes, 0) ==
          JCGO_CLASSREF_OF(java_lang_String__class) &&
          (methodsDims == jnull || (paramDims =
          (jbyteArr)JCGO_ARR_INTERNALACC(jObject, methodsDims, i)) == jnull ||
          !JCGO_ARR_INTERNALACC(jbyte, paramDims, 0)))
      {
       slotStrArg = i;
       break;
      }
     }
      else
      {
       slotNoArg = i;
       if (str == jnull)
        break;
      }
    }
  }
   else if ((rtn = ((jvtable)&JCGO_METHODS_OF(
            JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_thisRtn) != 0)
    slotNoArg = 0;
  if (JCGO_EXPECT_TRUE((slotNoArg & slotStrArg) >= 0))
   jobj = jcgo_newObject(
           (jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(aclass, vmdata)));
 }
 if (!jcgo_instanceOf0(OBJT_java_lang_Throwable, MAXT_java_lang_Throwable,
     jobj))
  jcgo_abortOnNonThrowable();
#ifdef JCGO_STDCLINIT
 jcgo_clinitTrig(aclass);
#else
#ifdef JCGO_CLINITCHK
 jcgo_clinitCheckOrder(aclass);
#endif
#endif
 if (pentry != NULL)
 {
  objectArgs = jnull;
  if (slotStrArg >= 0)
  {
   objectArgs = JCGO_STACKOBJ_OBJARRNEW(jcgo_stackobj1, jObjectArr_methods,
                 JCGO_CLASSREF_OF(java_lang_Object__class), 1);
   JCGO_ARR_INTERNALACC(jObject, objectArgs, 0) = (jObject)str;
   pentry = pentry + (unsigned)slotStrArg;
  }
   else pentry = pentry + (unsigned)slotNoArg;
  jobj = (*pentry->mproxy)(pentry->jmethod, jobj, jnull, jnull, jnull, jnull,
          objectArgs);
 }
  else jobj = (*rtn)(jobj);
 res = 0;
 if (JCGO_EXPECT_FALSE(*(void *volatile *)
     &jcgo_noTypesClassArr.jcgo_methods != NULL))
  JCGO_THROW_EXC(jobj);
 JCGO_NATCBACK_END(pJniEnv)
 return (jint)res;
#else
 jcgo_abortOnNonThrowable();
 return 0;
#endif
}

STATIC jthrowable JNICALL
jcgo_JniExceptionOccurred( JNIEnv *pJniEnv )
{
 jObject ex = JCGO_JNI_GETTCB(pJniEnv)->nativeExc;
#ifdef JCGO_THREADS
 if (JCGO_EXPECT_TRUE(ex == jnull))
  ex = JCGO_JNI_GETTCB(pJniEnv)->stopExc;
#endif
 return (jthrowable)jcgo_jniToLocalRef(pJniEnv, ex);
}

STATIC void JNICALL
jcgo_JniExceptionDescribe( JNIEnv *pJniEnv )
{
#ifdef OBJT_java_lang_VMThread
 JCGO_TRY_VOLATILE int res = 0;
 jObject ex;
 if ((ex = JCGO_JNI_GETTCB(pJniEnv)->nativeExc) == jnull)
  return;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 res = (int)java_lang_VMThread__jniExceptionDescribe0X__Lo(
        (java_lang_Object)ex);
 JCGO_NATCBACK_END(pJniEnv)
 if (JCGO_EXPECT_TRUE(res != 0))
  return;
 JCGO_JNI_GETTCB(pJniEnv)->nativeExc = ex;
#else
#ifndef JCGO_NOFATALMSG
 if (JCGO_JNI_GETTCB(pJniEnv)->nativeExc == jnull)
  return;
#endif
#endif
#ifndef JCGO_NOFATALMSG
 JCGO_JNI_FUNC(jcgo_JavaWriteLnToStderr)(" JNI exception occurred!", "");
#endif
}

STATIC void JNICALL
jcgo_JniExceptionClear( JNIEnv *pJniEnv )
{
 JCGO_JNI_GETTCB(pJniEnv)->nativeExc = jnull;
}

STATIC void JNICALL
jcgo_JniFatalError( JNIEnv *pJniEnv, CONST char *msg )
{
 JCGO_FATAL_ABORT(msg != NULL && *msg ? msg : "JNI error!");
}

STATIC jint JNICALL
jcgo_JniPushLocalFrame( JNIEnv *pJniEnv, jint capacity )
{
 jObjectArr JCGO_TRY_VOLATILE localObjs = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 localObjs = (jObjectArr)jcgo_newArray(
              JCGO_CLASSREF_OF(java_lang_Object__class), 0,
              (capacity > 0 ? capacity : 1) + 3);
 JCGO_NATCBACK_END(pJniEnv)
 if (JCGO_EXPECT_FALSE(localObjs == jnull))
  return (jint)JNI_ERR;
 JCGO_ARR_INTERNALACC(jObject, localObjs, 0) =
  (jObject)JCGO_JNI_GETTCB(pJniEnv)->localObjs;
 JCGO_JNI_GETTCB(pJniEnv)->localObjs = (jObjectArr)localObjs;
 return 0;
}

STATIC jobject JNICALL
jcgo_JniPopLocalFrame( JNIEnv *pJniEnv, jobject obj )
{
 jint len;
 jObject jobj = jcgo_jniDeRef(obj);
 jObjectArr localObjs = JCGO_JNI_GETTCB(pJniEnv)->localObjs;
 jObjectArr prevLocalObjs;
 do
 {
  if (localObjs == jnull || (JCGO_JNI_GETTCB(pJniEnv)->localObjs =
      (jObjectArr)JCGO_ARR_INTERNALACC(jObject, localObjs, 0)) == jnull)
   JCGO_FATAL_ABORT("Cannot delete JNI first local frame!");
  len = JCGO_ARRAY_NZLENGTH(localObjs);
  prevLocalObjs = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, localObjs, 1);
  while (--len > 0)
   JCGO_ARR_INTERNALACC(jObject, localObjs, len) = jnull;
 } while ((localObjs = JCGO_JNI_GETTCB(pJniEnv)->localObjs) == prevLocalObjs);
 return jcgo_jniToLocalRef(pJniEnv, jobj);
}

STATIC jobject JNICALL
jcgo_JniNewGlobalRef( JNIEnv *pJniEnv, jobject obj )
{
 jObjectArr nextEntry;
 jObjectArr prevEntry;
 jObjectArr JCGO_TRY_VOLATILE queEntry;
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull))
  return NULL;
 queEntry = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 prevEntry = (jObjectArr)jcgo_newArray(
              JCGO_CLASSREF_OF(java_lang_Object__class), 0, 3);
 JCGO_ARR_INTERNALACC(jObject, prevEntry, 2) = jobj;
 JCGO_CRITMOD_BEGIN(jcgo_jniGlobalRefsMutex)
 nextEntry = jcgo_globData.jniGlobalRefsQue;
 if (JCGO_EXPECT_TRUE(nextEntry != jnull))
 {
  JCGO_ARR_INTERNALACC(jObject, prevEntry, 0) = (jObject)nextEntry;
  JCGO_ARR_INTERNALACC(jObject, nextEntry, 1) = (jObject)prevEntry;
 }
 jcgo_globData.jniGlobalRefsQue = prevEntry;
 JCGO_CRITMOD_END(jcgo_jniGlobalRefsMutex)
 queEntry = prevEntry;
 JCGO_NATCBACK_END(pJniEnv)
 return JCGO_EXPECT_TRUE(queEntry != jnull) ?
         (jobject)&JCGO_ARR_INTERNALACC(jObject, queEntry, 2) : NULL;
}

STATIC void JNICALL
jcgo_JniDeleteGlobalRef( JNIEnv *pJniEnv, jobject globalref )
{
 jObjectArr queEntry;
 jObjectArr nextEntry;
 jObjectArr prevEntry;
 if (JCGO_EXPECT_TRUE(globalref != NULL))
 {
  queEntry = (jObjectArr)((volatile char *)globalref -
              (JCGO_OFFSET_OF(struct jcgo_jobjectarr_s, jObject) +
              sizeof(jObject) * 2));
  if (JCGO_ARRAY_NZLENGTH(queEntry) != 3 ||
      JCGO_METHODS_OF(queEntry)->jcgo_typeid != OBJT_jarray + OBJT_void ||
      JCGO_ARR_INTERNALACC(jObject, queEntry, 2) == jnull)
   JCGO_FATAL_ABORT("Invalid JNI global reference!");
#ifndef JCGO_PARALLEL
  JCGO_NATCBACK_BEGIN(pJniEnv)
#endif
  JCGO_ARR_INTERNALACC(jObject, queEntry, 2) = jnull;
  JCGO_CRITMOD_BEGIN(jcgo_jniGlobalRefsMutex)
  nextEntry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, queEntry, 0);
  prevEntry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, queEntry, 1);
  if (JCGO_EXPECT_TRUE(nextEntry != jnull))
   JCGO_ARR_INTERNALACC(jObject, nextEntry, 1) = (jObject)prevEntry;
  if (JCGO_EXPECT_TRUE(prevEntry != jnull))
   JCGO_ARR_INTERNALACC(jObject, prevEntry, 0) = (jObject)nextEntry;
   else jcgo_globData.jniGlobalRefsQue = nextEntry;
  JCGO_CRITMOD_END(jcgo_jniGlobalRefsMutex)
#ifndef JCGO_PARALLEL
  JCGO_NATCBACK_END(pJniEnv)
#endif
 }
}

STATIC void JNICALL
jcgo_JniDeleteLocalRef( JNIEnv *pJniEnv, jobject localref )
{
 if (JCGO_EXPECT_TRUE(localref != NULL))
  *(jObject *)localref = jnull;
}

STATIC jboolean JNICALL
jcgo_JniIsSameObject( JNIEnv *pJniEnv, jobject obj1, jobject obj2 )
{
 return (jboolean)(jcgo_jniDeRef(obj1) == jcgo_jniDeRef(obj2) ? JNI_TRUE :
         JNI_FALSE);
}

STATIC jobject JNICALL
jcgo_JniNewLocalRef( JNIEnv *pJniEnv, jobject obj )
{
 return jcgo_jniToLocalRef(pJniEnv, jcgo_jniDeRef(obj));
}

STATIC jint JNICALL
jcgo_JniEnsureLocalCapacity( JNIEnv *pJniEnv, jint capacity )
{
 jint len = 0;
 jObjectArr localObjs;
 jObjectArr JCGO_TRY_VOLATILE newLocalObjs;
 localObjs = JCGO_JNI_GETTCB(pJniEnv)->localObjs;
 if (JCGO_EXPECT_TRUE(capacity > 0))
 {
  if (JCGO_EXPECT_TRUE(localObjs != jnull) &&
      (len = JCGO_ARRAY_NZLENGTH(localObjs)) > capacity)
  {
   while (--len > 0)
    if (JCGO_ARR_INTERNALACC(jObject, localObjs, len) == jnull &&
        --capacity <= 0)
     return 0;
   len = JCGO_ARRAY_NZLENGTH(localObjs);
  }
  if (JCGO_EXPECT_FALSE(localObjs == jnull || (len += capacity) <= 0))
   len = (jint)(((u_jint)-1) >> 1);
  newLocalObjs = jnull;
  JCGO_NATCBACK_BEGIN(pJniEnv)
  newLocalObjs = (jObjectArr)jcgo_newArray(
                  JCGO_CLASSREF_OF(java_lang_Object__class), 0, len);
  JCGO_NATCBACK_END(pJniEnv)
  if (JCGO_EXPECT_FALSE(newLocalObjs == jnull))
   return (jint)JNI_ERR;
  JCGO_ARR_INTERNALACC(jObject, newLocalObjs, 0) = (jObject)localObjs;
  JCGO_ARR_INTERNALACC(jObject, newLocalObjs, 1) = (jObject)localObjs;
  JCGO_JNI_GETTCB(pJniEnv)->localObjs = (jObjectArr)newLocalObjs;
 }
 return 0;
}

STATIC jobject JNICALL
jcgo_JniAllocObject( JNIEnv *pJniEnv, jclass clazz )
{
 java_lang_Class aclass;
 jObject JCGO_TRY_VOLATILE jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 if (JCGO_EXPECT_FALSE(aclass == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 jobj = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 if (JCGO_EXPECT_TRUE((JCGO_FIELD_NZACCESS(aclass, modifiers) &
     (JCGO_ACCMOD_INTERFACE | JCGO_ACCMOD_ABSTRACT)) == 0))
  jobj = jcgo_newObject(
          (jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(aclass, vmdata)));
#ifdef OBJT_java_lang_VMThrowable
  else JCGO_THROW_EXC(
        java_lang_VMThrowable__createInstantiationException0X__Lc(aclass));
#endif
 JCGO_NATCBACK_END(pJniEnv)
 return jcgo_jniToLocalRef(pJniEnv, (jObject)jobj);
}

STATIC jclass JNICALL
jcgo_JniGetObjectClass( JNIEnv *pJniEnv, jobject obj )
{
 jObject jobj;
 java_lang_Class JCGO_TRY_VOLATILE aclass;
 int dims;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 dims = JCGO_METHODS_OF(jobj)->jcgo_typeid - (OBJT_jarray + OBJT_void);
 if ((unsigned)dims < (unsigned)JCGO_DIMS_MAX)
 {
  aclass = jnull;
#ifdef OBJT_java_lang_VMClass
  JCGO_NATCBACK_BEGIN(pJniEnv)
  aclass = java_lang_VMClass__arrayClassOf0X__LcI(
            JCGO_OBJARR_COMPCLASS((jObjectArr)jobj), (jint)(dims + 1));
  JCGO_NATCBACK_END(pJniEnv)
#else
  jcgo_abortOnVMClassNotFound();
#endif
 }
  else aclass = JCGO_METHODS_OF(jobj)->jcgo_class;
 return (jclass)jcgo_jniToLocalRef(pJniEnv, (jObject)aclass);
}

STATIC jboolean JNICALL
jcgo_JniIsInstanceOf( JNIEnv *pJniEnv, jobject obj, jclass clazz )
{
 jObject jobj;
 java_lang_Class aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 java_lang_Class srcClass;
 int typenum;
 if (JCGO_EXPECT_FALSE(aclass == jnull))
 {
  jcgo_jniHandleInstanceOfNullClass(pJniEnv);
  return (jboolean)JNI_FALSE;
 }
 jobj = jcgo_jniDeRef(obj);
 if (jobj == jnull)
  return (jboolean)JNI_FALSE;
 typenum = JCGO_METHODS_OF(jobj)->jcgo_typeid;
 srcClass = JCGO_METHODS_OF(jobj)->jcgo_class;
 if (typenum >= OBJT_jarray + OBJT_void &&
     typenum < OBJT_jarray + OBJT_void + JCGO_DIMS_MAX)
  srcClass = JCGO_OBJARR_COMPCLASS((jObjectArr)jobj);
  else typenum = OBJT_jarray + OBJT_void - 1;
 return (jboolean)(jcgo_isAssignable(srcClass, aclass,
         typenum - (OBJT_jarray + OBJT_void - 1), 0) ? JNI_TRUE : JNI_FALSE);
}

STATIC jint JNICALL
jcgo_JniRegisterNatives( JNIEnv *pJniEnv, jclass clazz,
 CONST JNINativeMethod *methods, jint nMethods )
{
 /* dummy */
 return 0;
}

STATIC jint JNICALL
jcgo_JniUnregisterNatives( JNIEnv *pJniEnv, jclass clazz )
{
 /* dummy */
 return 0;
}

STATIC jint JNICALL
jcgo_JniMonitorEnter( JNIEnv *pJniEnv, jobject obj )
{
 jObject jobj = jcgo_jniDeRef(obj);
#ifdef JCGO_THREADS
 JCGO_TRY_VOLATILE int res;
 struct jcgo_tcb_s *tcb;
 jObjectArr listEntry;
 jObjectArr *pentry;
 jint len;
#endif
 if (JCGO_EXPECT_FALSE(jobj == jnull))
 {
  if (JCGO_JNI_GETTCB(pJniEnv)->nativeExc == jnull)
   jcgo_jniThrowNullPointerException(pJniEnv);
  return (jint)JNI_ERR;
 }
#ifdef JCGO_THREADS
 res = (int)JNI_ERR;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 JCGO_GET_CURTCB(&tcb);
 if (jcgo_monEnterInner(jobj, tcb))
 {
  pentry = &tcb->jniLockedObjs;
  for (;;)
  {
   listEntry = *pentry;
   if (JCGO_EXPECT_FALSE(listEntry == jnull))
   {
    {
     JCGO_TRY_BLOCK
     {
      *pentry = (jObjectArr)jcgo_newArray(
                 JCGO_CLASSREF_OF(java_lang_Object__class), 0,
                 JCGO_JNILOCKEDOBJS_DEFLEN);
     }
     JCGO_TRY_LEAVE
     {
      if (JCGO_EXPECT_FALSE(*pentry == jnull))
       jcgo_monLeaveInner(jobj, tcb);
     }
     JCGO_TRY_FINALLYEND
    }
    JCGO_ARR_INTERNALACC(jObject, *pentry, JCGO_JNILOCKEDOBJS_DEFLEN - 1) =
     jobj;
    break;
   }
   len = JCGO_ARRAY_NZLENGTH(listEntry);
   while (--len > 0)
    if (JCGO_ARR_INTERNALACC(jObject, listEntry, len) == jnull)
     break;
   if (JCGO_EXPECT_TRUE(len != 0))
   {
    JCGO_ARR_INTERNALACC(jObject, listEntry, len) = jobj;
    break;
   }
   pentry = (jObjectArr *)&JCGO_ARR_INTERNALACC(jObject, listEntry, 0);
  }
 }
  else
  {
   listEntry = (jObjectArr)jcgo_newArray(
                JCGO_CLASSREF_OF(java_lang_Object__class), 0, 2);
   JCGO_ARR_INTERNALACC(jObject, listEntry, 0) = jobj;
   JCGO_ARR_INTERNALACC(jObject, listEntry, 1) =
    (jObject)tcb->overlockedObjsList;
   tcb->overlockedObjsList = listEntry;
  }
 res = 0;
 JCGO_NATCBACK_END(pJniEnv)
 return (jint)res;
#else
 return 0;
#endif
}

STATIC jint JNICALL
jcgo_JniMonitorExit( JNIEnv *pJniEnv, jobject obj )
{
 jObject jobj = jcgo_jniDeRef(obj);
#ifdef JCGO_THREADS
 JCGO_TRY_VOLATILE int res;
 struct jcgo_tcb_s *tcb;
 jObjectArr listEntry;
 jObjectArr *pentry;
 jint len;
#endif
 if (JCGO_EXPECT_FALSE(jobj == jnull))
 {
  if (JCGO_JNI_GETTCB(pJniEnv)->nativeExc == jnull)
   jcgo_jniThrowNullPointerException(pJniEnv);
  return (jint)JNI_ERR;
 }
#ifdef JCGO_THREADS
 res = (int)JNI_ERR;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 JCGO_GET_CURTCB(&tcb);
 pentry = &tcb->overlockedObjsList;
 for (;;)
 {
  if ((listEntry = *pentry) == jnull)
  {
#ifdef OBJT_java_lang_VMThread
   if (jcgo_monLeaveInner(jobj, tcb) < 0)
    java_lang_VMThread__throwIllegalMonitorStateException0X__();
#else
   jcgo_monLeaveInner(jobj, tcb);
#endif
   for (listEntry = tcb->jniLockedObjs; listEntry != jnull;
        listEntry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 0))
   {
    len = JCGO_ARRAY_NZLENGTH(listEntry);
    while (--len > 0)
     if (JCGO_EXPECT_FALSE(JCGO_ARR_INTERNALACC(jObject, listEntry, len) ==
         jobj))
      break;
    if (JCGO_EXPECT_TRUE(len != 0))
    {
     JCGO_ARR_INTERNALACC(jObject, listEntry, len) = jnull;
     break;
    }
   }
   break;
  }
  if (JCGO_EXPECT_FALSE(JCGO_ARR_INTERNALACC(jObject, listEntry, 0) == jobj))
  {
   *pentry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
   break;
  }
  pentry = (jObjectArr *)&JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
 }
 res = 0;
 JCGO_NATCBACK_END(pJniEnv)
 return (jint)res;
#else
 return 0;
#endif
}

STATIC jweak JNICALL
jcgo_JniNewWeakGlobalRef( JNIEnv *pJniEnv, jobject obj )
{
 jObject jobj;
#ifndef JCGO_FNLZDATA_OMITREFQUE
 jObjectArr listEntry;
 struct jcgo_refexthidden_s *pexthidden;
#endif
 jweak JCGO_TRY_VOLATILE weakref;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jobj = jcgo_jniDeRef(obj);
 weakref = NULL;
 if (JCGO_EXPECT_TRUE(jobj != jnull))
 {
  JCGO_NATCBACK_BEGIN(pJniEnv)
#ifdef JCGO_FNLZDATA_OMITREFQUE
#ifdef OBJT_java_lang_VMThrowable
  java_lang_VMThrowable__throwOutOfMemoryError0X__();
#endif
#else
  listEntry = (jObjectArr)jcgo_newArray(
               JCGO_CLASSREF_OF(java_lang_Object__class), 0, 2);
  pexthidden = jcgo_newWeakQueRef(jnull, (jObject)listEntry, jobj, 1);
  if (JCGO_EXPECT_TRUE(pexthidden != NULL))
  {
#ifdef JCGO_THREADS
   *(void **)((volatile char *)&pexthidden->hidden.obj -
    (int)sizeof(void *)) = JCGO_PTR_RESERVED;
#endif
   JCGO_ARR_INTERNALACC(jObject, listEntry, 0) = (jObject)pexthidden;
   JCGO_CRITMOD_BEGIN(jcgo_jniWeakRefsMutex)
   JCGO_ARR_INTERNALACC(jObject, listEntry, 1) =
    (jObject)jcgo_globData.jniWeakRefsList;
   jcgo_globData.jniWeakRefsList = listEntry;
   JCGO_CRITMOD_END(jcgo_jniWeakRefsMutex)
   weakref = (jweak)&pexthidden->hidden.obj;
  }
#ifdef OBJT_java_lang_VMThrowable
   else java_lang_VMThrowable__throwOutOfMemoryError0X__();
#endif
#endif
  JCGO_NATCBACK_END(pJniEnv)
 }
 return (jweak)weakref;
}

STATIC void JNICALL
jcgo_JniDeleteWeakGlobalRef( JNIEnv *pJniEnv, jweak weakref )
{
#ifndef JCGO_FNLZDATA_OMITREFQUE
 jObjectArr listEntry;
 jObjectArr prevEntry;
 struct jcgo_refexthidden_s *pexthidden;
 if (JCGO_EXPECT_TRUE(weakref != NULL))
 {
#ifndef JCGO_PARALLEL
  JCGO_NATCBACK_BEGIN(pJniEnv)
#endif
  prevEntry = jnull;
  pexthidden = NULL;
  JCGO_CRITMOD_BEGIN(jcgo_jniWeakRefsMutex)
  listEntry = jcgo_globData.jniWeakRefsList;
  while (listEntry != jnull)
  {
   pexthidden =
    (struct jcgo_refexthidden_s *)JCGO_ARR_INTERNALACC(jObject, listEntry, 0);
   if (JCGO_EXPECT_TRUE(pexthidden != NULL) &&
       JCGO_EXPECT_FALSE((jweak)(&pexthidden->hidden.obj) == weakref))
    break;
   prevEntry = listEntry;
   listEntry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
  }
  if (JCGO_EXPECT_TRUE(listEntry != jnull))
  {
   if (JCGO_EXPECT_TRUE(prevEntry != jnull))
    JCGO_ARR_INTERNALACC(jObject, prevEntry, 1) =
     JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
    else jcgo_globData.jniWeakRefsList =
          (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
  }
  JCGO_CRITMOD_END(jcgo_jniWeakRefsMutex)
  if (JCGO_EXPECT_TRUE(listEntry != jnull))
   pexthidden->hidden.obj = jnull;
   else JCGO_FATAL_ABORT("Invalid JNI weak global reference!");
#ifndef JCGO_PARALLEL
  JCGO_NATCBACK_END(pJniEnv)
#endif
 }
#endif
}

STATIC jobjectRefType JNICALL
jcgo_JniGetObjectRefType( JNIEnv *pJniEnv, jobject obj )
{
 jObjectArr localObjs;
 jObjectArr listEntry;
#ifndef JCGO_FNLZDATA_OMITREFQUE
 struct jcgo_refexthidden_s *pexthidden;
#endif
 ptrdiff_t index;
#ifdef JCGO_PARALLEL
 jobjectRefType reftype;
#else
 jobjectRefType JCGO_TRY_VOLATILE reftype;
#endif
 reftype = JNIInvalidRefType;
 if (JCGO_EXPECT_TRUE(obj != NULL))
 {
  localObjs = JCGO_JNI_GETTCB(pJniEnv)->localObjs;
  while (localObjs != jnull)
  {
   index = (jObject *)obj - &JCGO_ARR_INTERNALACC(jObject, localObjs, 0);
   if (index >= 2 && JCGO_ARRAY_NZLENGTH(localObjs) > index &&
       JCGO_EXPECT_TRUE((unsigned)(
       (char *)obj - (char *)&JCGO_ARR_INTERNALACC(jObject,
       localObjs, 0)) % sizeof(jObject) == 0))
    return JNILocalRefType;
   listEntry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, localObjs, 0);
   if ((jobject)&JCGO_ARR_INTERNALACC(jObject, localObjs, 1) == obj)
    return JCGO_EXPECT_TRUE((jObjectArr)JCGO_ARR_INTERNALACC(jObject,
            localObjs, 1) != listEntry) ? JNILocalRefType : JNIInvalidRefType;
   localObjs = listEntry;
  }
#ifndef JCGO_PARALLEL
  JCGO_NATCBACK_BEGIN(pJniEnv)
#endif
#ifndef JCGO_FNLZDATA_OMITREFQUE
  JCGO_CRITMOD_BEGIN(jcgo_jniWeakRefsMutex)
  listEntry = jcgo_globData.jniWeakRefsList;
  while (listEntry != jnull)
  {
   pexthidden =
    (struct jcgo_refexthidden_s *)JCGO_ARR_INTERNALACC(jObject, listEntry, 0);
   if (JCGO_EXPECT_TRUE(pexthidden != NULL) &&
       JCGO_EXPECT_FALSE((jobject)(&pexthidden->hidden.obj) == obj))
    break;
   listEntry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
  }
  JCGO_CRITMOD_END(jcgo_jniWeakRefsMutex)
  if (JCGO_EXPECT_FALSE(listEntry != jnull))
   reftype = JNIWeakGlobalRefType;
   else
#endif
  {
   JCGO_CRITMOD_BEGIN(jcgo_jniGlobalRefsMutex)
   listEntry = jcgo_globData.jniGlobalRefsQue;
   while (listEntry != jnull)
   {
    if (JCGO_EXPECT_FALSE((jobject)&JCGO_ARR_INTERNALACC(jObject,
        listEntry, 2) == obj))
     break;
    listEntry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 0);
   }
   JCGO_CRITMOD_END(jcgo_jniGlobalRefsMutex)
   if (JCGO_EXPECT_TRUE(listEntry != jnull))
    reftype = JNIGlobalRefType;
  }
#ifndef JCGO_PARALLEL
  JCGO_NATCBACK_END(pJniEnv)
#endif
 }
 return reftype;
}

STATIC jboolean JNICALL
jcgo_JniExceptionCheck( JNIEnv *pJniEnv )
{
#ifdef JCGO_THREADS
 if (JCGO_JNI_GETTCB(pJniEnv)->stopExc != jnull)
  return (jboolean)JNI_TRUE;
#endif
 return (jboolean)(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull ? JNI_TRUE :
         JNI_FALSE);
}

#endif
