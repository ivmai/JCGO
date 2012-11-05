/*
 * @(#) $(JCGO)/include/jcgovrtm.c --
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

#ifndef JCGO_NOGC

#ifdef JCGO_THREADS

STATIC void GC_CALLBACK jcgo_finalizerNotifier( void )
{
#ifdef OBJT_java_lang_VMRuntime
 JCGO_CALLBACK_BEGIN
 java_lang_VMRuntime__finalizeObject0X__Lo(jnull);
 JCGO_CALLBACK_END
#endif
}

#endif /* JCGO_THREADS */

#endif /* ! JCGO_NOGC */

JCGO_NOSEP_STATIC jlong CFASTCALL
java_lang_VMRuntime__freeMemory__( void )
{
 jlong size = (jlong)JCGO_MEM_CORELEFT(0);
 if (JCGO_EXPECT_FALSE(size < (jlong)0L))
  size = (jlong)(((u_jlong)-1L) >> 1);
 return size;
}

JCGO_NOSEP_STATIC jlong CFASTCALL
java_lang_VMRuntime__totalMemory__( void )
{
 jlong size = (jlong)JCGO_MEM_HEAPCURSIZE(0);
 jlong left = (jlong)JCGO_MEM_CORELEFT(0);
 if (JCGO_EXPECT_FALSE((u_jlong)size <= (u_jlong)left))
  size = left != (jlong)0L ? left : jcgo_allocatedBytesCnt;
 if (JCGO_EXPECT_FALSE(size < (jlong)0L))
  size = (jlong)(((u_jlong)-1L) >> 1);
 return size;
}

JCGO_NOSEP_STATIC jlong CFASTCALL
java_lang_VMRuntime__maxMemory__( void )
{
 return (jlong)JCGO_MEM_MAXLIMIT(u_jlong);
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMRuntime__enableNotifyOnFinalization0__( void )
{
#ifdef JCGO_NOGC
 return -1;
#else
#ifdef JCGO_THREADS
 GC_set_finalizer_notifier(jcgo_finalizerNotifier);
 GC_set_finalize_on_demand(1);
 return 0;
#else
 return -1;
#endif
#endif
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMRuntime__gc0__I( jint clearRefs )
{
#ifdef OBJT_java_lang_ref_SoftReference
 unsigned count;
#ifndef JCGO_NOGC
 if ((int)clearRefs)
 {
  JCGO_MEMREFGDAT_LOCK(0);
  jcgo_softRefsProcess(1);
  JCGO_MEMREFGDAT_UNLOCK(0);
 }
#endif
#endif
#ifdef JCGO_THREADS
 jcgo_threadYield();
#endif
#ifndef JCGO_NOGC
 if ((int)clearRefs)
  GC_gcollect_and_unmap();
  else JCGO_MEM_GCOLLECT(0);
#endif
#ifdef OBJT_java_lang_ref_SoftReference
 JCGO_MEMREFGDAT_LOCK(0);
 if ((count = (unsigned)JCGO_MEM_GCGETCOUNT(0)) != jcgo_gcCount)
 {
  jcgo_gcCount = count;
#ifndef JCGO_NOGC
  jcgo_softRefsProcess(0);
  jcgo_lastAllocBytesCnt = (unsigned)jcgo_allocatedBytesCnt;
#endif
 }
 JCGO_MEMREFGDAT_UNLOCK(0);
#endif
#ifdef JCGO_THREADS
 jcgo_threadYield();
#endif
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMRuntime__runFinalization0__( void )
{
#ifdef JCGO_NOGC
 return -1;
#else
 return (jint)GC_invoke_finalizers();
#endif
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMRuntime__runFinalizationForExit0__( void )
{
#ifndef JCGO_NOGC
#ifdef JCGO_THREADS
 GC_set_finalize_on_demand(0);
#endif
 GC_finalize_all();
#endif
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMRuntime__availableProcessors0__( void )
{
#ifdef JCGO_THREADS
 JCGO_NPROCS_T data;
 JCGO_NPROCS_STMT(&data);
 return (jint)JCGO_NPROCS_GET(&data);
#else
 return 0;
#endif
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMRuntime__traceCode0__II( jint instr, jint on )
{
 /* not implemented */
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMRuntime__nativeLoad0__LsLo( java_lang_String filename,
 java_lang_Object loader )
{
#ifndef JCGO_NOJNI
 jcgo_jniOnLoad();
#endif
 return 1;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMRuntime__enableShutdownHooks0__( void )
{
#ifdef JCGO_NOCTRLC
 return -1;
#else
#ifdef JCGO_THREADS
 return 1;
#else
 return 0;
#endif
#endif
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMRuntime__preventIOBlocking0__( void )
{
#ifdef JCGO_NOMTCLIB
 return 1;
#else
 return 0;
#endif
}

JCGO_NOSEP_STATIC java_lang_String CFASTCALL
java_lang_VMRuntime__getJavaExePathname0__( void )
{
 JCGO_MAIN_TCHAR *tstr = jcgo_targv0;
 if (JCGO_EXPECT_TRUE(tstr != NULL))
 {
  JCGO_JNI_BLOCK(0)
  return (java_lang_String)jcgo_jniLeave(jcgo_pJniEnv,
          (jobject)JCGO_MAIN_TCONVERTCMDARG(jcgo_pJniEnv, tstr));
 }
 return jnull;
}

JCGO_NOSEP_STATIC java_lang_String CFASTCALL
java_lang_VMRuntime__getCustomJavaProps0__( void )
{
#ifdef JAVADEFPROPS
 JCGO_JNI_BLOCK(0)
 return (java_lang_String)jcgo_jniLeave(jcgo_pJniEnv,
         (jobject)JCGO_MAIN_TCONVERTCMDARG(jcgo_pJniEnv, JAVADEFPROPS));
#else
 return jnull;
#endif
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMRuntime__getJavaVmVersion0__( void )
{
 return JCGO_VER;
}

#endif
