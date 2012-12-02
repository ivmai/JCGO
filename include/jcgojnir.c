/*
 * @(#) $(JCGO)/include/jcgojnir.c --
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

/* #include <stdarg.h> */
/* <type> va_arg(va_list&, <type>); */
/* void va_end(va_list&); */
/* void va_start(va_list&, <id>); */

#define JCGO_ACCMOD_PRIVATE 0x2
#define JCGO_ACCMOD_STATIC 0x8

#ifdef OBJT_java_lang_reflect_VMMethod

#ifndef JCGO_JNIINVOKE_MAXSTACKARGS
#define JCGO_JNIINVOKE_MAXSTACKARGS 6
#endif

#else /* OBJT_java_lang_reflect_VMMethod */

JCGO_NOSEP_INLINE void CFASTCALL jcgo_abortOnMethodNotFound( void )
{
 JCGO_FATAL_ABORT("Cannot find java.lang.reflect.VMMethod!");
}

#endif /* ! OBJT_java_lang_reflect_VMMethod */

JCGO_NOSEP_INLINE void CFASTCALL jcgo_abortOnInvalidMethodId( void )
{
 JCGO_FATAL_ABORT("Invalid JNI methodID!");
}

STATIC java_lang_Class CFASTCALL jcgo_methodDeclClass( java_lang_Class aclass,
 jmethodID methodID, int isStatic )
{
 java_lang_Class declClass;
 jObjectArr interfaces;
 CONST struct jcgo_reflect_s *jcgo_reflect;
 CONST struct jcgo_methodentry_s *pentry;
 int i;
 do
 {
  if (isStatic > 0)
  {
   if (JCGO_EXPECT_FALSE((jmethodID)(&((jvtable)&JCGO_METHODS_OF(
       JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_reflect) == methodID))
    break;
  }
   else if ((jmethodID)(&((jvtable)&JCGO_METHODS_OF(
            JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_thisRtn) == methodID)
    return aclass;
  jcgo_reflect = ((jvtable)&JCGO_METHODS_OF(
                  JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_reflect;
  if (JCGO_EXPECT_TRUE(jcgo_reflect != NULL) &&
      (pentry = jcgo_reflect->methodsEntry) != NULL &&
      ((CONST struct jcgo_methodentry_s *)methodID - pentry) >= 0 &&
      JCGO_ARRAY_NZLENGTH(jcgo_reflect->methodsTypes) >
      ((CONST struct jcgo_methodentry_s *)methodID - pentry))
  {
   if (isStatic > 0 &&
       JCGO_EXPECT_FALSE(jcgo_reflect->methodsModifiers == jnull))
    break;
   return aclass;
  }
  if (isStatic <= 0)
  {
   interfaces = JCGO_FIELD_NZACCESS(aclass, interfaces);
   for (i = 0; (int)JCGO_ARRAY_NZLENGTH(interfaces) > i; i++)
    if ((declClass = jcgo_methodDeclClass(
        (java_lang_Class)JCGO_ARR_INTERNALACC(jObject, interfaces, i),
        methodID, 0)) != jnull)
     return declClass;
  }
 } while ((aclass = JCGO_FIELD_NZACCESS(aclass, superclass)) != jnull);
 return jnull;
}

STATIC java_lang_Class CFASTCALL jcgo_jniMethodInvokePrep( jObject *pjobj,
 java_lang_Class aclass, jmethodID methodID, int isStatic )
{
#ifdef OBJT_java_lang_VMThrowable
 if (JCGO_EXPECT_FALSE(!methodID ||
     (*pjobj == jnull && isStatic <= 0 && isStatic != -2) ||
     (isStatic && aclass == jnull)))
  JCGO_THROW_EXC(java_lang_VMThrowable__createNullPointerException0X__());
#endif
 if (aclass != jnull)
 {
  if ((unsigned)((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(aclass,
      vmdata)))->jcgo_typeid - (unsigned)OBJT_jarray <
      (unsigned)(OBJT_void + JCGO_DIMS_MAX))
   aclass = JCGO_CLASSREF_OF(java_lang_Object__class);
 }
  else aclass = JCGO_METHODS_OF(*pjobj)->jcgo_typeid >=
                 OBJT_jarray + OBJT_void + JCGO_DIMS_MAX ?
                 JCGO_METHODS_OF(*pjobj)->jcgo_class :
                 JCGO_CLASSREF_OF(java_lang_Object__class);
 if ((aclass = jcgo_methodDeclClass(aclass, methodID, isStatic)) == jnull)
  jcgo_abortOnInvalidMethodId();
 if (isStatic == -2)
 {
#ifdef OBJT_java_lang_VMThrowable
  if (JCGO_EXPECT_FALSE((JCGO_FIELD_NZACCESS(aclass,
      modifiers) & (JCGO_ACCMOD_INTERFACE | JCGO_ACCMOD_ABSTRACT)) != 0))
   JCGO_THROW_EXC(java_lang_VMThrowable__createInstantiationException0X__Lc(
    aclass));
#endif
  *pjobj = jcgo_newObject(
            (jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(aclass, vmdata)));
 }
 return aclass;
}

#ifdef OBJT_java_lang_reflect_VMMethod

STATIC int CFASTCALL jcgo_jniMethodInvokeCount( int *pargsCnt, int *pintCount,
 int *plongCount, int *pfloatCount, int *pdoubleCount, int *pobjectCount,
 CONST struct jcgo_reflect_s *jcgo_reflect, jObjectArr argTypes,
 jbyteArr paramDims, jint slot )
{
 jObjectArr methodsName;
 java_lang_String name;
 int argsCnt = 0;
 int restype = OBJT_void;
 int i;
 int intCount;
 int longCount;
 int floatCount;
 int doubleCount;
 if (JCGO_EXPECT_TRUE(argTypes != jnull) &&
     (argsCnt = (int)JCGO_ARRAY_NZLENGTH(argTypes)) > 0 &&
     (methodsName = jcgo_reflect->methodsName) != jnull &&
     (name = (java_lang_String)
     JCGO_ARR_INTERNALACC(jObject, methodsName, slot)) != jnull &&
     JCGO_FIELD_NZACCESS(name, count) > 0)
 {
  argsCnt--;
  if (paramDims == jnull || !JCGO_ARR_INTERNALACC(jbyte, paramDims, argsCnt))
   restype =
    ((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS((java_lang_Class)
    JCGO_ARR_INTERNALACC(jObject, argTypes, argsCnt), vmdata)))->jcgo_typeid;
 }
 *pargsCnt = argsCnt;
 if (JCGO_EXPECT_FALSE(argsCnt > JCGO_JNIINVOKE_MAXSTACKARGS))
 {
  intCount = 0;
  longCount = 0;
  floatCount = 0;
  doubleCount = 0;
  for (i = 0; i < argsCnt; i++)
   switch (paramDims == jnull || !JCGO_ARR_INTERNALACC(jbyte, paramDims, i) ?
           ((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS((java_lang_Class)
           JCGO_ARR_INTERNALACC(jObject, argTypes, i),
           vmdata)))->jcgo_typeid : OBJT_void)
   {
   case OBJT_jboolean:
   case OBJT_jbyte:
   case OBJT_jchar:
   case OBJT_jshort:
   case OBJT_jint:
    intCount++;
    break;
   case OBJT_jlong:
    longCount++;
    break;
   case OBJT_jfloat:
    floatCount++;
    break;
   case OBJT_jdouble:
    doubleCount++;
    break;
   default:
    break;
   }
  *pobjectCount = argsCnt - intCount - longCount - floatCount - doubleCount;
  *pintCount = intCount ? intCount : 1;
  *plongCount = longCount ? longCount : 1;
  *pfloatCount = floatCount ? floatCount : 1;
  *pdoubleCount = doubleCount ? doubleCount : 1;
 }
  else
  {
   if ((intCount = argsCnt) == 0)
    intCount = 1;
   *pobjectCount = argsCnt;
   *pintCount = intCount;
   *plongCount = intCount;
   *pfloatCount = intCount;
   *pdoubleCount = intCount;
  }
 return restype;
}

STATIC jObject CFASTCALL jcgo_jniMethodInvokeRun( jObject jobj,
 java_lang_Class aclass, CONST struct jcgo_reflect_s *jcgo_reflect,
 jvalue *pretval, jintArr intArgs, jlongArr longArgs, jfloatArr floatArgs,
 jdoubleArr doubleArgs, jObjectArr objectArgs, jint slot, int restype,
 int isStatic )
{
 CONST struct jcgo_methodentry_s *pentry =
  jcgo_reflect->methodsEntry + (unsigned)slot;
 jshortArr methodsModifiers;
 jObjectArr methodsName;
 java_lang_String name;
 jObject value =
  (*pentry->mproxy)(jobj != jnull &&
  (JCGO_FIELD_NZACCESS(aclass, modifiers) & JCGO_ACCMOD_FINAL) == 0 &&
  (methodsName = jcgo_reflect->methodsName) != jnull &&
  (name = (java_lang_String)
  JCGO_ARR_INTERNALACC(jObject, methodsName, slot)) != jnull &&
  JCGO_FIELD_NZACCESS(name, count) > 0 &&
  ((methodsModifiers = jcgo_reflect->methodsModifiers) == jnull ||
  (JCGO_ARR_INTERNALACC(jshort, methodsModifiers, slot) &
  (JCGO_ACCMOD_PRIVATE | JCGO_ACCMOD_STATIC | JCGO_ACCMOD_FINAL)) == 0) ?
  *(void (CFASTCALL **)(void))((CONST char *)(isStatic ?
  (jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(aclass, vmdata)) :
  JCGO_METHODS_OF(jobj)) + JCGO_CAST_PFUNCTOUINT(pentry->jmethod)) :
  pentry->jmethod, jobj, intArgs, longArgs, floatArgs, doubleArgs,
  objectArgs);
 switch (restype)
 {
 case OBJT_jboolean:
  pretval->z = (jboolean)(JCGO_ARR_INTERNALACC(jint, intArgs, 0) ? JNI_TRUE :
                JNI_FALSE);
  break;
 case OBJT_jbyte:
  pretval->b = (jbyte)JCGO_ARR_INTERNALACC(jint, intArgs, 0);
  break;
 case OBJT_jchar:
  pretval->c = (jchar)JCGO_ARR_INTERNALACC(jint, intArgs, 0);
  break;
 case OBJT_jshort:
  pretval->s = (jshort)JCGO_ARR_INTERNALACC(jint, intArgs, 0);
  break;
 case OBJT_jint:
  pretval->i = JCGO_ARR_INTERNALACC(jint, intArgs, 0);
  break;
 case OBJT_jlong:
  pretval->j = JCGO_ARR_INTERNALACC(jlong, longArgs, 0);
  break;
 case OBJT_jfloat:
  pretval->f = JCGO_ARR_INTERNALACC(jfloat, floatArgs, 0);
  break;
 case OBJT_jdouble:
  pretval->d = JCGO_ARR_INTERNALACC(jdouble, doubleArgs, 0);
  break;
 default:
  break;
 }
 return value;
}

#endif /* OBJT_java_lang_reflect_VMMethod */

STATIC jobject CFASTCALL jcgo_jniMethodInvokeA( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jvalue *pretval, jmethodID methodID, int isStatic,
 CONST jvalue *args )
{
 jObject jobj;
 java_lang_Class aclass;
 jObject JCGO_TRY_VOLATILE value;
#ifdef OBJT_java_lang_reflect_VMMethod
 CONST struct jcgo_reflect_s *jcgo_reflect;
 jObjectArr argTypes;
 jbyteArr paramDims;
 jintArr intArgs;
 jlongArr longArgs;
 jfloatArr floatArgs;
 jdoubleArr doubleArgs;
 jObjectArr objectArgs;
 jint slot;
 int restype;
 int argsCnt;
 int i;
 int intInd;
 int longInd;
 int floatInd;
 int doubleInd;
 int objectInd;
 JCGO_STATIC_ARRAY(jint, JCGO_JNIINVOKE_MAXSTACKARGS) jcgo_stackobj1;
 JCGO_STATIC_ARRAY(jlong, JCGO_JNIINVOKE_MAXSTACKARGS) jcgo_stackobj2;
 JCGO_STATIC_ARRAY(jfloat, JCGO_JNIINVOKE_MAXSTACKARGS) jcgo_stackobj3;
 JCGO_STATIC_ARRAY(jdouble, JCGO_JNIINVOKE_MAXSTACKARGS) jcgo_stackobj4;
 JCGO_STATIC_OBJARRAY(JCGO_JNIINVOKE_MAXSTACKARGS) jcgo_stackobj5;
#endif
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jobj = jcgo_jniDeRef(obj);
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 value = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 aclass = jcgo_jniMethodInvokePrep(&jobj, aclass, methodID, isStatic);
 if ((jmethodID)(&((jvtable)&JCGO_METHODS_OF(
     JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_thisRtn) != methodID)
 {
#ifdef OBJT_java_lang_reflect_VMMethod
  jcgo_reflect = ((jvtable)&JCGO_METHODS_OF(
                  JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_reflect;
  slot = (jint)((CONST struct jcgo_methodentry_s *)methodID -
          jcgo_reflect->methodsEntry);
  paramDims = (argTypes = jcgo_reflect->methodsDims) != jnull ?
               (jbyteArr)JCGO_ARR_INTERNALACC(jObject, argTypes, slot) :
               jnull;
  argTypes = (jObjectArr)JCGO_ARR_INTERNALACC(jObject,
              jcgo_reflect->methodsTypes, slot);
  restype = jcgo_jniMethodInvokeCount(&argsCnt, &intInd, &longInd, &floatInd,
             &doubleInd, &objectInd, jcgo_reflect, argTypes, paramDims, slot);
  intArgs = JCGO_EXPECT_FALSE(intInd > JCGO_JNIINVOKE_MAXSTACKARGS) ?
             (jintArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jint), 0,
             (jint)intInd) : (jintArr)JCGO_STACKOBJ_PRIMARRNEW(jcgo_stackobj1,
             jintArr_methods, intInd);
  longArgs = JCGO_EXPECT_FALSE(longInd > JCGO_JNIINVOKE_MAXSTACKARGS) ?
              (jlongArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jlong), 0,
              (jint)longInd) : (jlongArr)JCGO_STACKOBJ_PRIMARRNEW(
              jcgo_stackobj2, jlongArr_methods, longInd);
  floatArgs = JCGO_EXPECT_FALSE(floatInd > JCGO_JNIINVOKE_MAXSTACKARGS) ?
               (jfloatArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jfloat), 0,
               (jint)floatInd) : (jfloatArr)JCGO_STACKOBJ_PRIMARRNEW(
               jcgo_stackobj3, jfloatArr_methods, floatInd);
  doubleArgs = JCGO_EXPECT_FALSE(doubleInd > JCGO_JNIINVOKE_MAXSTACKARGS) ?
                (jdoubleArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jdouble), 0,
                (jint)doubleInd) : (jdoubleArr)JCGO_STACKOBJ_PRIMARRNEW(
                jcgo_stackobj4, jdoubleArr_methods, doubleInd);
  objectArgs = JCGO_EXPECT_FALSE(objectInd > JCGO_JNIINVOKE_MAXSTACKARGS) ?
                (jObjectArr)jcgo_newArray(JCGO_CLASSREF_OF(
                java_lang_Object__class), 0, (jint)objectInd) :
                JCGO_STACKOBJ_OBJARRNEW(jcgo_stackobj5, jObjectArr_methods,
                JCGO_CLASSREF_OF(java_lang_Object__class), objectInd);
  intInd = 0;
  longInd = 0;
  floatInd = 0;
  doubleInd = 0;
  objectInd = 0;
  for (i = 0; i < argsCnt; i++)
   switch (paramDims == jnull || !JCGO_ARR_INTERNALACC(jbyte, paramDims, i) ?
           ((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(
           (java_lang_Class)JCGO_ARR_INTERNALACC(jObject, argTypes, i),
           vmdata)))->jcgo_typeid : OBJT_void)
   {
   case OBJT_jboolean:
    JCGO_ARR_INTERNALACC(jint, intArgs, intInd) =
     (jint)((args + i)->z ? 1 : 0);
    intInd++;
    break;
   case OBJT_jbyte:
    JCGO_ARR_INTERNALACC(jint, intArgs, intInd) = (jint)(args + i)->b;
    intInd++;
    break;
   case OBJT_jchar:
    JCGO_ARR_INTERNALACC(jint, intArgs, intInd) = (jint)(args + i)->c;
    intInd++;
    break;
   case OBJT_jshort:
    JCGO_ARR_INTERNALACC(jint, intArgs, intInd) = (jint)(args + i)->s;
    intInd++;
    break;
   case OBJT_jint:
    JCGO_ARR_INTERNALACC(jint, intArgs, intInd) = (args + i)->i;
    intInd++;
    break;
   case OBJT_jlong:
    JCGO_ARR_INTERNALACC(jlong, longArgs, longInd) = (args + i)->j;
    longInd++;
    break;
   case OBJT_jfloat:
    JCGO_ARR_INTERNALACC(jfloat, floatArgs, floatInd) = (args + i)->f;
    floatInd++;
    break;
   case OBJT_jdouble:
    JCGO_ARR_INTERNALACC(jdouble, doubleArgs, doubleInd) = (args + i)->d;
    doubleInd++;
    break;
   default:
    JCGO_ARR_INTERNALACC(jObject, objectArgs, objectInd) =
     jcgo_jniDeRef((args + i)->l);
    objectInd++;
    break;
   }
  value = jcgo_jniMethodInvokeRun(jobj, aclass, jcgo_reflect, pretval,
           intArgs, longArgs, floatArgs, doubleArgs, objectArgs, slot,
           restype, isStatic);
#endif
 }
  else value = (*((jvtable)&JCGO_METHODS_OF(
                JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_thisRtn)(jobj);
 JCGO_NATCBACK_END(pJniEnv)
 return jcgo_jniToLocalRef(pJniEnv, (jObject)value);
}

STATIC jobject /* CFASTCALL */
jcgo_jniMethodInvokeV( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jvalue *pretval, jmethodID methodID, int isStatic, va_list args )
{
 jObject jobj;
 java_lang_Class aclass;
 jObject JCGO_TRY_VOLATILE value;
#ifdef OBJT_java_lang_reflect_VMMethod
 CONST struct jcgo_reflect_s *jcgo_reflect;
 jObjectArr argTypes;
 jbyteArr paramDims;
 jintArr intArgs;
 jlongArr longArgs;
 jfloatArr floatArgs;
 jdoubleArr doubleArgs;
 jObjectArr objectArgs;
 jint slot;
 int restype;
 int argsCnt;
 int i;
 int intInd;
 int longInd;
 int floatInd;
 int doubleInd;
 int objectInd;
 JCGO_STATIC_ARRAY(jint, JCGO_JNIINVOKE_MAXSTACKARGS) jcgo_stackobj1;
 JCGO_STATIC_ARRAY(jlong, JCGO_JNIINVOKE_MAXSTACKARGS) jcgo_stackobj2;
 JCGO_STATIC_ARRAY(jfloat, JCGO_JNIINVOKE_MAXSTACKARGS) jcgo_stackobj3;
 JCGO_STATIC_ARRAY(jdouble, JCGO_JNIINVOKE_MAXSTACKARGS) jcgo_stackobj4;
 JCGO_STATIC_OBJARRAY(JCGO_JNIINVOKE_MAXSTACKARGS) jcgo_stackobj5;
#endif
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jobj = jcgo_jniDeRef(obj);
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 value = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 aclass = jcgo_jniMethodInvokePrep(&jobj, aclass, methodID, isStatic);
 if ((jmethodID)(&((jvtable)&JCGO_METHODS_OF(
     JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_thisRtn) != methodID)
 {
#ifdef OBJT_java_lang_reflect_VMMethod
  jcgo_reflect = ((jvtable)&JCGO_METHODS_OF(
                  JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_reflect;
  slot = (jint)((CONST struct jcgo_methodentry_s *)methodID -
          jcgo_reflect->methodsEntry);
  paramDims = (argTypes = jcgo_reflect->methodsDims) != jnull ?
               (jbyteArr)JCGO_ARR_INTERNALACC(jObject, argTypes, slot) :
               jnull;
  argTypes = (jObjectArr)JCGO_ARR_INTERNALACC(jObject,
              jcgo_reflect->methodsTypes, slot);
  restype = jcgo_jniMethodInvokeCount(&argsCnt, &intInd, &longInd, &floatInd,
             &doubleInd, &objectInd, jcgo_reflect, argTypes, paramDims, slot);
  intArgs = JCGO_EXPECT_FALSE(intInd > JCGO_JNIINVOKE_MAXSTACKARGS) ?
             (jintArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jint), 0,
             (jint)intInd) : (jintArr)JCGO_STACKOBJ_PRIMARRNEW(jcgo_stackobj1,
             jintArr_methods, intInd);
  longArgs = JCGO_EXPECT_FALSE(longInd > JCGO_JNIINVOKE_MAXSTACKARGS) ?
              (jlongArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jlong), 0,
              (jint)longInd) : (jlongArr)JCGO_STACKOBJ_PRIMARRNEW(
              jcgo_stackobj2, jlongArr_methods, longInd);
  floatArgs = JCGO_EXPECT_FALSE(floatInd > JCGO_JNIINVOKE_MAXSTACKARGS) ?
               (jfloatArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jfloat), 0,
               (jint)floatInd) : (jfloatArr)JCGO_STACKOBJ_PRIMARRNEW(
               jcgo_stackobj3, jfloatArr_methods, floatInd);
  doubleArgs = JCGO_EXPECT_FALSE(doubleInd > JCGO_JNIINVOKE_MAXSTACKARGS) ?
                (jdoubleArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jdouble), 0,
                (jint)doubleInd) : (jdoubleArr)JCGO_STACKOBJ_PRIMARRNEW(
                jcgo_stackobj4, jdoubleArr_methods, doubleInd);
  objectArgs = JCGO_EXPECT_FALSE(objectInd > JCGO_JNIINVOKE_MAXSTACKARGS) ?
                (jObjectArr)jcgo_newArray(JCGO_CLASSREF_OF(
                java_lang_Object__class), 0, (jint)objectInd) :
                JCGO_STACKOBJ_OBJARRNEW(jcgo_stackobj5, jObjectArr_methods,
                JCGO_CLASSREF_OF(java_lang_Object__class), objectInd);
  intInd = 0;
  longInd = 0;
  floatInd = 0;
  doubleInd = 0;
  objectInd = 0;
  for (i = 0; i < argsCnt; i++)
   switch (paramDims == jnull || !JCGO_ARR_INTERNALACC(jbyte, paramDims, i) ?
           ((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(
           (java_lang_Class)JCGO_ARR_INTERNALACC(jObject, argTypes, i),
           vmdata)))->jcgo_typeid : OBJT_void)
   {
   case OBJT_jboolean:
    JCGO_ARR_INTERNALACC(jint, intArgs, intInd) =
     (jint)((jboolean)va_arg(args, int) ? 1 : 0);
    intInd++;
    break;
   case OBJT_jbyte:
    JCGO_ARR_INTERNALACC(jint, intArgs, intInd) = (jbyte)va_arg(args, int);
    intInd++;
    break;
   case OBJT_jchar:
    JCGO_ARR_INTERNALACC(jint, intArgs, intInd) =
     (jint)((jchar)va_arg(args, int));
    intInd++;
    break;
   case OBJT_jshort:
    JCGO_ARR_INTERNALACC(jint, intArgs, intInd) = (jshort)va_arg(args, int);
    intInd++;
    break;
   case OBJT_jint:
    JCGO_ARR_INTERNALACC(jint, intArgs, intInd) = va_arg(args, jint);
    intInd++;
    break;
   case OBJT_jlong:
    JCGO_ARR_INTERNALACC(jlong, longArgs, longInd) = va_arg(args, jlong);
    longInd++;
    break;
   case OBJT_jfloat:
    JCGO_ARR_INTERNALACC(jfloat, floatArgs, floatInd) =
     (jfloat)va_arg(args, JCGO_VAARG_JFLOAT);
    floatInd++;
    break;
   case OBJT_jdouble:
    JCGO_ARR_INTERNALACC(jdouble, doubleArgs, doubleInd) =
     va_arg(args, jdouble);
    doubleInd++;
    break;
   default:
    JCGO_ARR_INTERNALACC(jObject, objectArgs, objectInd) =
     jcgo_jniDeRef(va_arg(args, jobject));
    objectInd++;
    break;
   }
  value = jcgo_jniMethodInvokeRun(jobj, aclass, jcgo_reflect, pretval,
           intArgs, longArgs, floatArgs, doubleArgs, objectArgs, slot,
           restype, isStatic);
#endif
 }
  else value = (*((jvtable)&JCGO_METHODS_OF(
                JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_thisRtn)(jobj);
 JCGO_NATCBACK_END(pJniEnv)
 return jcgo_jniToLocalRef(pJniEnv, (jObject)value);
}

STATIC jmethodID JNICALL
jcgo_JniFromReflectedMethod( JNIEnv *pJniEnv, jobject method )
{
 java_lang_Object jobj;
 jmethodID JCGO_TRY_VOLATILE methodID;
#ifdef OBJT_java_lang_reflect_VMMethod
 java_lang_Class aclass;
 CONST struct jcgo_reflect_s *jcgo_reflect;
 CONST struct jcgo_methodentry_s *pentry;
#endif
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jmethodID)0;
 jobj = (java_lang_Object)jcgo_jniDeRef(method);
 if (JCGO_EXPECT_FALSE(jobj == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jmethodID)0;
 }
 methodID = (jmethodID)0;
#ifdef OBJT_java_lang_reflect_VMMethod
 JCGO_NATCBACK_BEGIN(pJniEnv)
 aclass = java_lang_reflect_VMMethod__getMethodDeclClass0X__Lo(jobj);
 jcgo_reflect = ((jvtable)&JCGO_METHODS_OF(
                 JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_reflect;
 methodID = JCGO_EXPECT_TRUE(jcgo_reflect != NULL) && (pentry =
             jcgo_reflect->methodsEntry) != NULL ? (jmethodID)(pentry +
             (unsigned)java_lang_reflect_VMMethod__getMethodSlot0X__Lo(
             jobj)) : (jmethodID)(&((jvtable)&JCGO_METHODS_OF(
             JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_thisRtn);
 JCGO_NATCBACK_END(pJniEnv)
#endif
 return (jmethodID)methodID;
}

STATIC jobject JNICALL
jcgo_JniToReflectedMethod( JNIEnv *pJniEnv, jclass clazz, jmethodID methodID,
 jboolean isStatic )
{
 java_lang_Class aclass;
 java_lang_Object JCGO_TRY_VOLATILE jobj;
#ifdef OBJT_java_lang_reflect_VMMethod
 CONST struct jcgo_reflect_s *jcgo_reflect;
 CONST struct jcgo_methodentry_s *pentry;
#endif
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 if (JCGO_EXPECT_FALSE(aclass == jnull || !methodID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 jobj = jnull;
#ifdef OBJT_java_lang_reflect_VMMethod
 if ((unsigned)((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(aclass,
     vmdata)))->jcgo_typeid - (unsigned)OBJT_jarray <
     (unsigned)(OBJT_void + JCGO_DIMS_MAX))
  aclass = JCGO_CLASSREF_OF(java_lang_Object__class);
 JCGO_NATCBACK_BEGIN(pJniEnv)
 aclass = jcgo_methodDeclClass(aclass, methodID, isStatic ? 1 : 0);
 if (JCGO_EXPECT_TRUE(aclass != jnull))
 {
  jcgo_reflect = ((jvtable)&JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(aclass,
                  vmdata)))->jcgo_reflect;
  jobj = java_lang_reflect_VMMethod__getMethodBySlot0X__LcJ(aclass,
          JCGO_EXPECT_TRUE(jcgo_reflect != NULL) &&
          (pentry = jcgo_reflect->methodsEntry) != NULL ?
          (jlong)((CONST struct jcgo_methodentry_s *)methodID - pentry) : 0);
 }
 if (jobj == jnull)
  jcgo_abortOnInvalidMethodId();
 JCGO_NATCBACK_END(pJniEnv)
#else
 jcgo_abortOnMethodNotFound();
#endif
 return jcgo_jniToLocalRef(pJniEnv, (jObject)jobj);
}

STATIC jobject JNICALL
jcgo_JniNewObject( JNIEnv *pJniEnv, jclass clazz, jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 va_start(args, methodID);
 retval.l = jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, -2,
             args);
 va_end(args);
 return retval.l;
}

STATIC jobject JNICALL
jcgo_JniNewObjectV( JNIEnv *pJniEnv, jclass clazz, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 return jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, -2,
         args);
}

STATIC jobject JNICALL
jcgo_JniNewObjectA( JNIEnv *pJniEnv, jclass clazz, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 return jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, -2,
         args);
}

STATIC jmethodID JNICALL
jcgo_JniGetMethodID( JNIEnv *pJniEnv, jclass clazz, CONST char *name,
 CONST char *sig )
{
 java_lang_Class aclass;
 jmethodID JCGO_TRY_VOLATILE methodID;
#ifdef OBJT_java_lang_reflect_VMMethod
 java_lang_String str;
 java_lang_String sigstr;
 java_lang_Object jobj;
 CONST struct jcgo_reflect_s *jcgo_reflect;
 CONST struct jcgo_methodentry_s *pentry;
#endif
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jmethodID)0;
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 if (JCGO_EXPECT_FALSE(aclass == jnull || name == NULL || sig == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jmethodID)0;
 }
 methodID = (jmethodID)0;
#ifdef OBJT_java_lang_reflect_VMMethod
 JCGO_NATCBACK_BEGIN(pJniEnv)
 str = jcgo_utfMakeString(name);
 sigstr = jcgo_utfMakeString(sig);
 jobj = java_lang_reflect_VMMethod__getMethodByName0X__LcLsLsI(aclass, str,
         sigstr, 0);
 if (JCGO_EXPECT_TRUE(jobj != jnull))
 {
  aclass = java_lang_reflect_VMMethod__getMethodDeclClass0X__Lo(jobj);
  jcgo_reflect = ((jvtable)&JCGO_METHODS_OF(
                  JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_reflect;
  methodID = JCGO_EXPECT_TRUE(jcgo_reflect != NULL) && (pentry =
              jcgo_reflect->methodsEntry) != NULL ? (jmethodID)(pentry +
              (unsigned)java_lang_reflect_VMMethod__getMethodSlot0X__Lo(
              jobj)) : (jmethodID)&((jvtable)&JCGO_METHODS_OF(
              JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_thisRtn;
 }
#ifdef OBJT_java_lang_VMThrowable
  else JCGO_THROW_EXC(
        java_lang_VMThrowable__createNoSuchMethodError0X__LcLsLsI(aclass, str,
        sigstr, 0));
#endif
 JCGO_NATCBACK_END(pJniEnv)
#else
 if (*name != (char)0x3c || *(name + 1) != (char)0x69 || /* "<init>" */
     *(name + 2) != (char)0x6e || *(name + 3) != (char)0x69 ||
     *(name + 4) != (char)0x74 || *(name + 5) != (char)0x3e || *(name + 6) ||
     *sig != (char)0x28 || *(sig + 1) != (char)0x29 || /* "()V" */
     *(sig + 2) != (char)0x56 || *(sig + 3))
  jcgo_abortOnMethodNotFound();
 if (JCGO_EXPECT_TRUE(((jvtable)&JCGO_METHODS_OF(
     JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_thisRtn != 0))
  methodID = (jmethodID)&((jvtable)&JCGO_METHODS_OF(
              JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_thisRtn;
#ifdef OBJT_java_lang_VMThrowable
  else
  {
   JCGO_NATCBACK_BEGIN(pJniEnv)
   JCGO_THROW_EXC(java_lang_VMThrowable__createNoSuchMethodError0X__LcLsLsI(
    aclass, jcgo_utfMakeString(name), jcgo_utfMakeString(sig), 0));
   JCGO_NATCBACK_END(pJniEnv)
  }
#endif
#endif
 return (jmethodID)methodID;
}

STATIC jobject JNICALL
jcgo_JniCallObjectMethod( JNIEnv *pJniEnv, jobject obj,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 va_start(args, methodID);
 retval.l = jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0,
             args);
 va_end(args);
 return retval.l;
}

STATIC jobject JNICALL
jcgo_JniCallObjectMethodV( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 return jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
}

STATIC jobject JNICALL
jcgo_JniCallObjectMethodA( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 return jcgo_jniMethodInvokeA(pJniEnv, obj, NULL, &retval, methodID, 0, args);
}

STATIC jboolean JNICALL
jcgo_JniCallBooleanMethod( JNIEnv *pJniEnv, jobject obj,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.z = (jboolean)JNI_FALSE;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 va_end(args);
 return retval.z;
}

STATIC jboolean JNICALL
jcgo_JniCallBooleanMethodV( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 retval.z = (jboolean)JNI_FALSE;
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.z;
}

STATIC jboolean JNICALL
jcgo_JniCallBooleanMethodA( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 retval.z = (jboolean)JNI_FALSE;
 jcgo_jniMethodInvokeA(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.z;
}

STATIC jbyte JNICALL
jcgo_JniCallByteMethod( JNIEnv *pJniEnv, jobject obj,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.b = (jbyte)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 va_end(args);
 return retval.b;
}

STATIC jbyte JNICALL
jcgo_JniCallByteMethodV( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 retval.b = (jbyte)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.b;
}

STATIC jbyte JNICALL
jcgo_JniCallByteMethodA( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 retval.b = (jbyte)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.b;
}

STATIC jchar JNICALL
jcgo_JniCallCharMethod( JNIEnv *pJniEnv, jobject obj,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.c = (jchar)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 va_end(args);
 return retval.c;
}

STATIC jchar JNICALL
jcgo_JniCallCharMethodV( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 retval.c = (jchar)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.c;
}

STATIC jchar JNICALL
jcgo_JniCallCharMethodA( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 retval.c = (jchar)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.c;
}

STATIC jshort JNICALL
jcgo_JniCallShortMethod( JNIEnv *pJniEnv, jobject obj,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.s = (jshort)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 va_end(args);
 return retval.s;
}

STATIC jshort JNICALL
jcgo_JniCallShortMethodV( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 retval.s = (jshort)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.s;
}

STATIC jshort JNICALL
jcgo_JniCallShortMethodA( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 retval.s = (jshort)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.s;
}

STATIC jint JNICALL
jcgo_JniCallIntMethod( JNIEnv *pJniEnv, jobject obj, jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.i = (jint)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 va_end(args);
 return retval.i;
}

STATIC jint JNICALL
jcgo_JniCallIntMethodV( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 retval.i = (jint)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.i;
}

STATIC jint JNICALL
jcgo_JniCallIntMethodA( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 retval.i = (jint)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.i;
}

STATIC jlong JNICALL
jcgo_JniCallLongMethod( JNIEnv *pJniEnv, jobject obj,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.j = (jlong)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 va_end(args);
 return retval.j;
}

STATIC jlong JNICALL
jcgo_JniCallLongMethodV( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 retval.j = (jlong)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.j;
}

STATIC jlong JNICALL
jcgo_JniCallLongMethodA( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 retval.j = (jlong)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.j;
}

STATIC jfloat JNICALL
jcgo_JniCallFloatMethod( JNIEnv *pJniEnv, jobject obj,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.f = (jfloat)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 va_end(args);
 return retval.f;
}

STATIC jfloat JNICALL
jcgo_JniCallFloatMethodV( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 retval.f = (jfloat)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.f;
}

STATIC jfloat JNICALL
jcgo_JniCallFloatMethodA( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 retval.f = (jfloat)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.f;
}

STATIC jdouble JNICALL
jcgo_JniCallDoubleMethod( JNIEnv *pJniEnv, jobject obj,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.d = (jdouble)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 va_end(args);
 return retval.d;
}

STATIC jdouble JNICALL
jcgo_JniCallDoubleMethodV( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 retval.d = (jdouble)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.d;
}

STATIC jdouble JNICALL
jcgo_JniCallDoubleMethodA( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 retval.d = (jdouble)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 return retval.d;
}

STATIC void JNICALL
jcgo_JniCallVoidMethod( JNIEnv *pJniEnv, jobject obj,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
 va_end(args);
}

STATIC void JNICALL
jcgo_JniCallVoidMethodV( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 va_list args )
{
 jvalue retval;
 jcgo_jniMethodInvokeV(pJniEnv, obj, NULL, &retval, methodID, 0, args);
}

STATIC void JNICALL
jcgo_JniCallVoidMethodA( JNIEnv *pJniEnv, jobject obj, jmethodID methodID,
 CONST jvalue *args )
{
 jvalue retval;
 jcgo_jniMethodInvokeA(pJniEnv, obj, NULL, &retval, methodID, 0, args);
}

STATIC jobject JNICALL
jcgo_JniCallNonvirtualObjectMethod( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 va_start(args, methodID);
 retval.l = jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1,
             args);
 va_end(args);
 return retval.l;
}

STATIC jobject JNICALL
jcgo_JniCallNonvirtualObjectMethodV( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, va_list args )
{
 jvalue retval;
 return jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1,
         args);
}

STATIC jobject JNICALL
jcgo_JniCallNonvirtualObjectMethodA( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 return jcgo_jniMethodInvokeA(pJniEnv, obj, clazz, &retval, methodID, -1,
         args);
}

STATIC jboolean JNICALL
jcgo_JniCallNonvirtualBooleanMethod( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.z = (jboolean)JNI_FALSE;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 va_end(args);
 return retval.z;
}

STATIC jboolean JNICALL
jcgo_JniCallNonvirtualBooleanMethodV( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.z = (jboolean)JNI_FALSE;
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.z;
}

STATIC jboolean JNICALL
jcgo_JniCallNonvirtualBooleanMethodA( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.z = (jboolean)JNI_FALSE;
 jcgo_jniMethodInvokeA(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.z;
}

STATIC jbyte JNICALL
jcgo_JniCallNonvirtualByteMethod( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.b = (jbyte)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 va_end(args);
 return retval.b;
}

STATIC jbyte JNICALL
jcgo_JniCallNonvirtualByteMethodV( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.b = (jbyte)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.b;
}

STATIC jbyte JNICALL
jcgo_JniCallNonvirtualByteMethodA( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.b = (jbyte)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.b;
}

STATIC jchar JNICALL
jcgo_JniCallNonvirtualCharMethod( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.c = (jchar)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 va_end(args);
 return retval.c;
}

STATIC jchar JNICALL
jcgo_JniCallNonvirtualCharMethodV( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.c = (jchar)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.c;
}

STATIC jchar JNICALL
jcgo_JniCallNonvirtualCharMethodA( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.c = (jchar)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.c;
}

STATIC jshort JNICALL
jcgo_JniCallNonvirtualShortMethod( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.s = (jshort)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 va_end(args);
 return retval.s;
}

STATIC jshort JNICALL
jcgo_JniCallNonvirtualShortMethodV( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.s = (jshort)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.s;
}

STATIC jshort JNICALL
jcgo_JniCallNonvirtualShortMethodA( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.s = (jshort)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.s;
}

STATIC jint JNICALL
jcgo_JniCallNonvirtualIntMethod( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.i = (jint)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 va_end(args);
 return retval.i;
}

STATIC jint JNICALL
jcgo_JniCallNonvirtualIntMethodV( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.i = (jint)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.i;
}

STATIC jint JNICALL
jcgo_JniCallNonvirtualIntMethodA( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.i = (jint)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.i;
}

STATIC jlong JNICALL
jcgo_JniCallNonvirtualLongMethod( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.j = (jlong)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 va_end(args);
 return retval.j;
}

STATIC jlong JNICALL
jcgo_JniCallNonvirtualLongMethodV( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.j = (jlong)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.j;
}

STATIC jlong JNICALL
jcgo_JniCallNonvirtualLongMethodA( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.j = (jlong)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.j;
}

STATIC jfloat JNICALL
jcgo_JniCallNonvirtualFloatMethod( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.f = (jfloat)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 va_end(args);
 return retval.f;
}

STATIC jfloat JNICALL
jcgo_JniCallNonvirtualFloatMethodV( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.f = (jfloat)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.f;
}

STATIC jfloat JNICALL
jcgo_JniCallNonvirtualFloatMethodA( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.f = (jfloat)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.f;
}

STATIC jdouble JNICALL
jcgo_JniCallNonvirtualDoubleMethod( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.d = (jdouble)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 va_end(args);
 return retval.d;
}

STATIC jdouble JNICALL
jcgo_JniCallNonvirtualDoubleMethodV( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.d = (jdouble)0;
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.d;
}

STATIC jdouble JNICALL
jcgo_JniCallNonvirtualDoubleMethodA( JNIEnv *pJniEnv, jobject obj,
 jclass clazz, jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.d = (jdouble)0;
 jcgo_jniMethodInvokeA(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 return retval.d;
}

STATIC void JNICALL
jcgo_JniCallNonvirtualVoidMethod( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
 va_end(args);
}

STATIC void JNICALL
jcgo_JniCallNonvirtualVoidMethodV( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 jcgo_jniMethodInvokeV(pJniEnv, obj, clazz, &retval, methodID, -1, args);
}

STATIC void JNICALL
jcgo_JniCallNonvirtualVoidMethodA( JNIEnv *pJniEnv, jobject obj, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 jcgo_jniMethodInvokeA(pJniEnv, obj, clazz, &retval, methodID, -1, args);
}

STATIC jmethodID JNICALL
jcgo_JniGetStaticMethodID( JNIEnv *pJniEnv, jclass clazz, CONST char *name,
 CONST char *sig )
{
 java_lang_Class aclass;
 jmethodID JCGO_TRY_VOLATILE methodID;
#ifdef OBJT_java_lang_reflect_VMMethod
 java_lang_String str;
 java_lang_String sigstr;
 java_lang_Object jobj;
 jint slot;
#endif
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jmethodID)0;
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 if (JCGO_EXPECT_FALSE(aclass == jnull || name == NULL || sig == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jmethodID)0;
 }
 methodID = (jmethodID)0;
#ifdef OBJT_java_lang_reflect_VMMethod
 JCGO_NATCBACK_BEGIN(pJniEnv)
 str = jcgo_utfMakeString(name);
 sigstr = jcgo_utfMakeString(sig);
 jobj = java_lang_reflect_VMMethod__getMethodByName0X__LcLsLsI(aclass, str,
         sigstr, 1);
 if (JCGO_EXPECT_TRUE(jobj != jnull))
 {
  aclass = java_lang_reflect_VMMethod__getMethodDeclClass0X__Lo(jobj);
  slot = java_lang_reflect_VMMethod__getMethodSlot0X__Lo(jobj);
  methodID =
   JCGO_EXPECT_TRUE(slot != -1) ? (jmethodID)(((jvtable)&JCGO_METHODS_OF(
   JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_reflect->methodsEntry +
   (unsigned)slot) : (jmethodID)&((jvtable)&JCGO_METHODS_OF(
   JCGO_FIELD_NZACCESS(aclass, vmdata)))->jcgo_reflect;
 }
#ifdef OBJT_java_lang_VMThrowable
  else JCGO_THROW_EXC(
        java_lang_VMThrowable__createNoSuchMethodError0X__LcLsLsI(aclass, str,
        sigstr, 1));
#endif
 JCGO_NATCBACK_END(pJniEnv)
#else
 jcgo_abortOnMethodNotFound();
#endif
 return (jmethodID)methodID;
}

STATIC jobject JNICALL
jcgo_JniCallStaticObjectMethod( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 va_start(args, methodID);
 retval.l = jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1,
             args);
 va_end(args);
 return retval.l;
}

STATIC jobject JNICALL
jcgo_JniCallStaticObjectMethodV( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 return jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1,
         args);
}

STATIC jobject JNICALL
jcgo_JniCallStaticObjectMethodA( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 return jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, 1,
         args);
}

STATIC jboolean JNICALL
jcgo_JniCallStaticBooleanMethod( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.z = (jboolean)JNI_FALSE;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 va_end(args);
 return retval.z;
}

STATIC jboolean JNICALL
jcgo_JniCallStaticBooleanMethodV( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.z = (jboolean)JNI_FALSE;
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.z;
}

STATIC jboolean JNICALL
jcgo_JniCallStaticBooleanMethodA( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.z = (jboolean)JNI_FALSE;
 jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.z;
}

STATIC jbyte JNICALL
jcgo_JniCallStaticByteMethod( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.b = (jbyte)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 va_end(args);
 return retval.b;
}

STATIC jbyte JNICALL
jcgo_JniCallStaticByteMethodV( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.b = (jbyte)0;
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.b;
}

STATIC jbyte JNICALL
jcgo_JniCallStaticByteMethodA( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.b = (jbyte)0;
 jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.b;
}

STATIC jchar JNICALL
jcgo_JniCallStaticCharMethod( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.c = (jchar)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 va_end(args);
 return retval.c;
}

STATIC jchar JNICALL
jcgo_JniCallStaticCharMethodV( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.c = (jchar)0;
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.c;
}

STATIC jchar JNICALL
jcgo_JniCallStaticCharMethodA( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.c = (jchar)0;
 jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.c;
}

STATIC jshort JNICALL
jcgo_JniCallStaticShortMethod( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.s = (jshort)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 va_end(args);
 return retval.s;
}

STATIC jshort JNICALL
jcgo_JniCallStaticShortMethodV( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.s = (jshort)0;
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.s;
}

STATIC jshort JNICALL
jcgo_JniCallStaticShortMethodA( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.s = (jshort)0;
 jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.s;
}

STATIC jint JNICALL
jcgo_JniCallStaticIntMethod( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.i = (jint)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 va_end(args);
 return retval.i;
}

STATIC jint JNICALL
jcgo_JniCallStaticIntMethodV( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.i = (jint)0;
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.i;
}

STATIC jint JNICALL
jcgo_JniCallStaticIntMethodA( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.i = (jint)0;
 jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.i;
}

STATIC jlong JNICALL
jcgo_JniCallStaticLongMethod( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.j = (jlong)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 va_end(args);
 return retval.j;
}

STATIC jlong JNICALL
jcgo_JniCallStaticLongMethodV( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.j = (jlong)0;
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.j;
}

STATIC jlong JNICALL
jcgo_JniCallStaticLongMethodA( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.j = (jlong)0;
 jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.j;
}

STATIC jfloat JNICALL
jcgo_JniCallStaticFloatMethod( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.f = (jfloat)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 va_end(args);
 return retval.f;
}

STATIC jfloat JNICALL
jcgo_JniCallStaticFloatMethodV( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.f = (jfloat)0;
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.f;
}

STATIC jfloat JNICALL
jcgo_JniCallStaticFloatMethodA( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.f = (jfloat)0;
 jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.f;
}

STATIC jdouble JNICALL
jcgo_JniCallStaticDoubleMethod( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 retval.d = (jdouble)0;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 va_end(args);
 return retval.d;
}

STATIC jdouble JNICALL
jcgo_JniCallStaticDoubleMethodV( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 retval.d = (jdouble)0;
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.d;
}

STATIC jdouble JNICALL
jcgo_JniCallStaticDoubleMethodA( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 retval.d = (jdouble)0;
 jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 return retval.d;
}

STATIC void JNICALL
jcgo_JniCallStaticVoidMethod( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, ... )
{
 va_list args;
 jvalue retval;
 va_start(args, methodID);
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
 va_end(args);
}

STATIC void JNICALL
jcgo_JniCallStaticVoidMethodV( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, va_list args )
{
 jvalue retval;
 jcgo_jniMethodInvokeV(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
}

STATIC void JNICALL
jcgo_JniCallStaticVoidMethodA( JNIEnv *pJniEnv, jclass clazz,
 jmethodID methodID, CONST jvalue *args )
{
 jvalue retval;
 jcgo_jniMethodInvokeA(pJniEnv, NULL, clazz, &retval, methodID, 1, args);
}

#endif
