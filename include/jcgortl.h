/*
 * @(#) $(JCGO)/include/jcgortl.h --
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
 * Used control macros: JCGO_ASSERTION, JCGO_CHKCAST, JCGO_CLINITCHK,
 * JCGO_CVOLATILE, JCGO_FASTMATH, JCGO_FPFAST, JCGO_HUGEARR, JCGO_HWNULLZ,
 * JCGO_INDEXCHK, JCGO_JNIUSCORE, JCGO_NOFLDCONST, JCGO_NOFP, JCGO_NOFRWINL,
 * JCGO_NOJNI, JCGO_NOSEGV, JCGO_PARALLEL, JCGO_SEHTRY, JCGO_SEPARATED,
 * JCGO_SFTNULLP, JCGO_STDCLINIT, JCGO_THREADS, JCGO_UNIX, JCGO_USEGCJ,
 * JCGO_USELONG.
 * Macros for tuning: ATTRIBGCBSS, ATTRIBGCDATA, ATTRIBMALLOC, ATTRIBNONGC,
 * BUILTINEXPECTR, CFASTCALL, DECLSPECNORET, GCSTATICDATA, EXTRASTATIC,
 * INLINE, STATIC, STATICDATA.
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

#define JCGO_BUILDING_JNIVM
#include "jcgojni.h"

#ifdef JCGO_VER

#ifndef NULL
#ifndef _STDDEF_H
#include <stddef.h>
#endif
#endif

#ifndef JCGO_SEHTRY
#ifndef setjmp
#ifndef _SETJMP_H
#include <setjmp.h>
/* int setjmp(jmp_buf); */
#endif
#endif
#endif

#ifndef CFASTCALL
#ifdef JCGO_UNIX
#define CFASTCALL /* empty */
#else
#define CFASTCALL __fastcall
#endif
#endif

#ifndef STATIC
#define STATIC static
#endif

#ifndef EXTRASTATIC
#define EXTRASTATIC /* empty */
#endif

#ifndef STATICDATA
#define STATICDATA STATIC
#endif

#ifndef GCSTATICDATA
#define GCSTATICDATA STATICDATA
#endif

#ifdef JCGO_SEPARATED
#define JCGO_SEP_EXTERN extern
#define JCGO_SEP_GCEXTERN JCGO_SEP_EXTERN
#define JCGO_NOSEP_DATA /* empty */
#define JCGO_NOSEP_GCDATA /* empty */
#define JCGO_NOSEP_STATIC /* empty */
#define JCGO_NOSEP_EXTRASTATIC /* empty */
#define JCGO_NOSEP_INLINE /* empty */
#define JCGO_NOSEP_FRWINL /* empty */
#define JCGO_INLFRW_FASTCALL CFASTCALL
#else
#define JCGO_NOSEP_DATA STATICDATA
#define JCGO_NOSEP_GCDATA GCSTATICDATA
#define JCGO_SEP_EXTERN JCGO_NOSEP_DATA
#define JCGO_SEP_GCEXTERN JCGO_NOSEP_GCDATA
#define JCGO_NOSEP_STATIC STATIC
#define JCGO_NOSEP_EXTRASTATIC EXTRASTATIC
#ifdef INLINE
#define JCGO_NOSEP_INLINE STATIC INLINE
#else
#ifdef JCGO_UNIX
#define JCGO_NOSEP_INLINE STATIC inline
#else
#define JCGO_NOSEP_INLINE STATIC __inline
#endif
#endif
#ifdef JCGO_NOFRWINL
#define JCGO_NOSEP_FRWINL JCGO_NOSEP_STATIC
#define JCGO_INLFRW_FASTCALL /* empty */
#else
#define JCGO_NOSEP_FRWINL JCGO_NOSEP_INLINE
#define JCGO_INLFRW_FASTCALL CFASTCALL
#endif
#endif

#ifndef ATTRIBNONGC
#define ATTRIBNONGC /* empty */
#endif

#ifndef ATTRIBGCDATA
#define ATTRIBGCDATA /* empty */
#endif

#ifndef ATTRIBGCBSS
#define ATTRIBGCBSS /* empty */
#endif

#ifndef NULL
#define NULL ((void *)0)
#endif

#ifndef ATTRIBMALLOC
#ifdef __GNUC__
#define ATTRIBMALLOC __attribute__((__malloc__))
#else
/* #define ATTRIBMALLOC __declspec(noalias) __declspec(restrict) */
#define ATTRIBMALLOC /* empty */
#endif
#endif

#ifndef DECLSPECNORET
#ifdef __GNUC__
#define DECLSPECNORET __attribute__((__noreturn__))
#else
#ifdef _MSC_VER
#define DECLSPECNORET __declspec(noreturn)
#else
#define DECLSPECNORET /* empty */
#endif
#endif
#endif

#define jnull ((void *)NULL)
#define jfalse ((jboolean)JNI_FALSE)
#define jtrue ((jboolean)JNI_TRUE)

#ifdef offsetof
#define JCGO_OFFSET_OF(t, m) offsetof(t, m)
#else
#ifdef __offsetof
#define JCGO_OFFSET_OF(t, m) __offsetof(t, m)
#else
#define JCGO_OFFSET_OF(t, m) ((unsigned)((char *)&((t *)NULL)->m - (char *)NULL))
#endif
#endif

#ifndef JCGO_TYPEID_T
#define JCGO_TYPEID_T unsigned short
#endif

#ifndef JCGO_OBJSIZE_T
#define JCGO_OBJSIZE_T short
#endif

#define OBJT_jboolean 1
#define OBJT_jbyte 2
#define OBJT_jchar 3
#define OBJT_jshort 4
#define OBJT_jint 5
#define OBJT_jlong 6
#define OBJT_jfloat 7
#define OBJT_jdouble 8

#define OBJT_void 9
#define OBJT_jarray 10

#define MAXT_jboolean OBJT_jboolean
#define MAXT_jbyte OBJT_jbyte
#define MAXT_jchar OBJT_jchar
#define MAXT_jshort OBJT_jshort
#define MAXT_jint OBJT_jint
#define MAXT_jlong OBJT_jlong
#define MAXT_jfloat OBJT_jfloat
#define MAXT_jdouble OBJT_jdouble

#ifndef GC_DS_LENGTH
#define GC_DS_LENGTH 0 /* same as in gc_mark.h */
#endif

#ifdef JCGO_USEGCJ
#define JCGO_GCJDESCR_DEFN void *jcgo_gcjdescr;
#define JCGO_GCJDESCR_ZEROINIT (void *)GC_DS_LENGTH,
#define JCGO_GCJDESCR_INIT(refstructname, lastptrfield) (void *)&((volatile char *)NULL)[((JCGO_OFFSET_OF(struct refstructname, lastptrfield) + sizeof(jObject) + 0x3) & ~0x3) | GC_DS_LENGTH],
#else
#define JCGO_GCJDESCR_DEFN /* empty */
#define JCGO_GCJDESCR_ZEROINIT /* empty */
#define JCGO_GCJDESCR_INIT(refstructname, lastptrfield) JCGO_GCJDESCR_ZEROINIT
#endif

#ifdef JCGO_JNIUSCORE
#define JCGO_JNI_FUNC(func) _##func
#else
#define JCGO_JNI_FUNC(func) func
#endif

#ifdef __GNUC__
#ifndef BUILTINEXPECTR
#define BUILTINEXPECTR(expect, expr) __builtin_expect(expr, expect)
#endif
#endif

#ifdef BUILTINEXPECTR
#define JCGO_EXPECT_FALSE(cond) BUILTINEXPECTR(jfalse, cond)
#define JCGO_EXPECT_TRUE(cond) BUILTINEXPECTR(jtrue, cond)
#else
#define JCGO_EXPECT_FALSE(cond) (cond)
#define JCGO_EXPECT_TRUE(cond) (cond)
#endif

#define JCGO_OBJREF_OF(objname) (&objname)
#define JCGO_METHODS_OF(obj) (obj)->jcgo_methods
#define JCGO_FIELD_NZACCESS(obj, field) (obj)->field

#define JCGO_ARRAY_NZLENGTH(arr) ((jint)JCGO_FIELD_NZACCESS(arr, length))
#define JCGO_ARRAY_ELENGTH(arr) jcgo_arrayLength((jObject)(arr))
#define JCGO_FIELD_EACCESS(reftype, obj, field) JCGO_FIELD_NZACCESS((reftype)jcgo_checkNull((jObject)(obj)), field)

#ifndef JCGO_ARRLENGTH_T
#ifdef JCGO_HUGEARR
#define JCGO_ARRLENGTH_T unsigned long
#else
#define JCGO_ARRLENGTH_T unsigned
#endif
#endif

#ifdef JCGO_HUGEARR
#define JCGO_HPTR_MOD __huge
#define JCGO_ARR_INTERNALACC(primtype, arr, index) ((primtype JCGO_HPTR_MOD *)(arr)->primtype)[(JCGO_ARRLENGTH_T)(index)]
#else
#define JCGO_HPTR_MOD /* empty */
#define JCGO_ARR_INTERNALACC(primtype, arr, index) (arr)->primtype[(JCGO_ARRLENGTH_T)(index)]
#endif

#define JCGO_THROW_EXC(throwable) jcgo_throwExc((jObject)(throwable))

#define JCGO_CALL_NZVFUNC(obj) JCGO_METHODS_OF(obj)
#define JCGO_CALL_EVFUNC(obj) JCGO_METHODS_OF(JCGO_EXPECT_TRUE((obj) != jnull) ? (obj) : (jcgo_throwNullPtrExc(), obj))
#define JCGO_CALL_EFINALF(obj) (void)(JCGO_EXPECT_TRUE((obj) != jnull) ? -1 : (jcgo_throwNullPtrExc(), 0)),

#include "jcgochke.h"

#ifdef JCGO_SEHTRY
#define JCGO_TRY_VOLATILE /* empty */
#define JCGO_TRY_NOCLOBBER(argvar) (void)0
#define JCGO_TRY_BLOCK __try
#define JCGO_TRY_LEAVE __finally
#define JCGO_TRY_FINALLYEND /* empty */
#define JCGO_TRY_CATCHES(suffnum) { jObject *jcgo_pthrowable; jObject jcgo_trythrowable; if (JCGO_EXPECT_TRUE((jcgo_trythrowable = *(jcgo_pthrowable = jcgo_tryCatches())) == jnull)) goto jcgo_trynoexc##suffnum;
#define JCGO_TRY_CATCH(objId, maxId) else if (JCGO_EXPECT_FALSE((unsigned)(JCGO_METHODS_OF(jcgo_trythrowable)->jcgo_typeid - (objId)) <= (unsigned)((maxId) - (objId))) ? (*jcgo_pthrowable = jnull, 1) : 0)
#define JCGO_TRY_THROWABLE(x) jcgo_trythrowable
#ifndef JCGO_TRY_SEHNOP
#define JCGO_TRY_SEHNOP (void)0
#endif
#define JCGO_TRY_RETHROW(suffnum) if (*jcgo_pthrowable == jnull) goto jcgo_trycaught##suffnum; jcgo_trynoexc##suffnum:; } JCGO_TRY_SEHNOP; jcgo_trycaught##suffnum:;
#define JCGO_TRY_CATCHALLSTORE(pthrowable) { if ((*(pthrowable) = jcgo_tryCatchAll()) != jnull) goto jcgo_trycaught; } JCGO_TRY_SEHNOP; jcgo_trycaught:;
#define jcgo_bzeroVlt jcgo_bzero
#define jcgo_memcpyVlt jcgo_memcpy
#else
#define JCGO_TRY_VOLATILE volatile
#define JCGO_TRY_NOCLOBBER(argvar) (void)(*(volatile void **)&jcgo_trashVar = &argvar)
#define JCGO_TRY_BLOCK struct jcgo_try_s jcgo_try; jcgo_tryEnter(&jcgo_try); if (!setjmp(jcgo_try.jbuf))
#define JCGO_TRY_LEAVE jcgo_tryLeave();
#define JCGO_TRY_FINALLYEND if (JCGO_EXPECT_FALSE(jcgo_try.throwable != jnull)) JCGO_THROW_EXC(jcgo_try.throwable);
#define JCGO_TRY_CATCHES(suffnum) if (JCGO_EXPECT_TRUE(jcgo_try.throwable == jnull)) { /* dummy */ }
#define JCGO_TRY_CATCH(objId, maxId) else if (JCGO_EXPECT_FALSE((unsigned)(JCGO_METHODS_OF(jcgo_try.throwable)->jcgo_typeid - (objId)) <= (unsigned)((maxId) - (objId))))
#define JCGO_TRY_THROWABLE(x) jcgo_try.throwable
#define JCGO_TRY_RETHROW(suffnum) else JCGO_THROW_EXC(jcgo_try.throwable);
#define JCGO_TRY_CATCHALLSTORE(pthrowable) (void)(*(pthrowable) = jcgo_try.throwable);
#endif

#ifdef JCGO_THREADS
#ifndef JCGO_CVOLATILE
#ifndef JCGO_PARALLEL
#define JCGO_CVOLATILE
#endif
#endif
#define JCGO_MON_DEFN void *jcgo_mon;
#define JCGO_MON_INIT NULL,
#ifdef JCGO_SEHTRY
#define JCGO_SYNC_BLOCK(obj) jObject jcgo_monObj = jcgo_monitorEnter((jObject)(obj)); __try
#define JCGO_SYNC_END __finally { jcgo_monitorLeave(jcgo_monObj); }
#define JCGO_SYNC_ENDUNREACH JCGO_SYNC_END
#else
#define JCGO_SYNC_BLOCK(obj) struct jcgo_curmon_s jcgo_curmon; jcgo_monitorEnter(&jcgo_curmon, (jObject)(obj));
#define JCGO_SYNC_JUMPLEAVE(x) jcgo_monitorLeave()
#define JCGO_SYNC_END JCGO_SYNC_JUMPLEAVE(0);
#define JCGO_SYNC_ENDUNREACH /* empty */
#endif
#define JCGO_SYNC_BLOCKSAFENZ(obj) JCGO_SYNC_BLOCK(obj)
#define JCGO_THRD_VOLATILE volatile
#define JCGO_THRD_ATTRNONGC ATTRIBNONGC
#define JCGO_NOTHR_CONST /* empty */
#define JCGO_STRREF_OF(strname) JCGO_OBJREF_OF(strname)
#define JCGO_CLASSREF_OF(clsvarname) JCGO_OBJREF_OF(clsvarname.jcgo_class)
#define JCGO_CORECLASS_FOR(typenum) JCGO_OBJREF_OF(jcgo_coreClasses[(typenum) - 1])
#else
#ifndef JCGO_CVOLATILE
#define JCGO_CVOLATILE
#endif
#define JCGO_MON_DEFN /* empty */
#define JCGO_MON_INIT /* empty */
#define JCGO_SYNC_BLOCK(obj) if (JCGO_EXPECT_FALSE((obj) == jnull)) jcgo_throwNullPtrExc();
#define JCGO_SYNC_BLOCKSAFENZ(obj) ;
#ifndef JCGO_SEHTRY
#define JCGO_SYNC_JUMPLEAVE(x) (void)0
#endif
#define JCGO_SYNC_END ;
#define JCGO_SYNC_ENDUNREACH /* empty */
#define JCGO_THRD_VOLATILE /* empty */
#define JCGO_THRD_ATTRNONGC /* empty */
#define JCGO_NOTHR_CONST CONST
#define JCGO_STRREF_OF(strname) ((java_lang_String)JCGO_OBJREF_OF(strname))
#define JCGO_CLASSREF_OF(clsvarname) ((java_lang_Class)JCGO_OBJREF_OF(clsvarname.jcgo_class))
#define JCGO_CORECLASS_FOR(typenum) ((java_lang_Class)JCGO_OBJREF_OF(jcgo_coreClasses[(typenum) - 1]))
#endif

#ifdef JCGO_NOFLDCONST
#define JCGO_IMMFLD_CONST /* empty */
#else
#define JCGO_IMMFLD_CONST CONST
#endif

#ifdef JCGO_ASSERTION
#define JCGO_ASSERT_STMT(cond, throwable) if (JCGO_EXPECT_FALSE(!(cond))) JCGO_THROW_EXC(throwable)
#else
#define JCGO_ASSERT_STMT(cond, throwable) /* empty */
#endif

#define JCGO_SHL_F(v1, v2) (((jint)(v1)) << ((int)(v2) & 0x1f))
#define JCGO_SHR_F(v1, v2) (((jint)(v1)) >> ((int)(v2) & 0x1f))
#define JCGO_USHR_F(v1, v2) ((jint)(((u_jint)(v1)) >> ((unsigned)(v2) & 0x1f)))

#ifdef JCGO_USELONG
#define JCGO_LSHL_F(v1, v2) jcgo_lshl(v1, (int)(v2))
#define JCGO_LSHR_F(v1, v2) jcgo_lshr(v1, (int)(v2))
#define JCGO_LUSHR_F(v1, v2) jcgo_lushr(v1, (int)(v2))
#else
#define JCGO_LSHL_F(v1, v2) ((v1) << ((int)(v2) & 0x3f))
#define JCGO_LSHR_F(v1, v2) ((v1) >> ((int)(v2) & 0x3f))
#define JCGO_LUSHR_F(v1, v2) ((jlong)(((u_jlong)(v1)) >> ((unsigned)(v2) & 0x3f)))
#endif

#ifdef JCGO_NOFP
#define JCGO_JFLOAT_TOJINT(f) ((jint)(f))
#define JCGO_JFLOAT_TOJLONG(f) ((jlong)(f))
#define JCGO_JDOUBLE_TOJINT(d) ((jint)(d))
#define JCGO_JDOUBLE_TOJLONG(d) ((jlong)(d))
#define JCGO_JDOUBLE_TOJFLOAT(d) ((jfloat)(d))
#define JCGO_FP_EQU(d1, d2) ((d1) == (d2))
#define JCGO_FP_EQUF(f1, f2) ((f1) == (f2))
#define JCGO_FP_LQ(d1, d2) ((d1) <= (d2))
#define JCGO_FP_LQF(f1, f2) ((f1) <= (f2))
#define JCGO_FP_LT(d1, d2) ((d1) < (d2))
#define JCGO_FP_LTF(f1, f2) ((f1) < (f2))
#define JCGO_FP_ZERO (jdouble)0
#define JCGO_FP_ZEROF (jfloat)0
#else
#define JCGO_JFLOAT_TOJINT(f) jcgo_jfloat2jint(f)
#define JCGO_JFLOAT_TOJLONG(f) jcgo_jfloat2jlong(f)
#define JCGO_JDOUBLE_TOJINT(d) jcgo_jdouble2jint(d)
#define JCGO_JDOUBLE_TOJLONG(d) jcgo_jdouble2jlong(d)
#define JCGO_JDOUBLE_TOJFLOAT(d) jcgo_jdouble2jfloat(d)
#ifdef JCGO_FPFAST
#define JCGO_FP_EQU(d1, d2) ((d1) == (d2))
#define JCGO_FP_EQUF(f1, f2) ((f1) == (f2))
#define JCGO_FP_LQ(d1, d2) ((d1) <= (d2))
#define JCGO_FP_LQF(f1, f2) ((f1) <= (f2))
#define JCGO_FP_LT(d1, d2) ((d1) < (d2))
#define JCGO_FP_LTF(f1, f2) ((f1) < (f2))
#else
#define JCGO_FP_EQU(d1, d2) jcgo_fequal(d1, d2)
#define JCGO_FP_EQUF(f1, f2) jcgo_fequalf(f1, f2)
#define JCGO_FP_LQ(d1, d2) jcgo_flessequ(d1, d2)
#define JCGO_FP_LQF(f1, f2) jcgo_flessequf(f1, f2)
#define JCGO_FP_LT(d1, d2) jcgo_flessthan(d1, d2)
#define JCGO_FP_LTF(f1, f2) jcgo_flessthanf(f1, f2)
#endif
#define JCGO_FP_ZERO jcgo_fpZero /* (jdouble)0.0 */
#define JCGO_FP_ZEROF jcgo_fpZeroF /* (jfloat)0.0 */
#endif

#define JCGO_STATIC_ARRAY(primtype, len) struct { jvtable JCGO_IMMFLD_CONST jcgo_methods; JCGO_MON_DEFN JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length; primtype primtype[len]; }
#define JCGO_STATIC_OBJARRAY(len) struct { jvtable JCGO_IMMFLD_CONST jcgo_methods; JCGO_MON_DEFN JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length; java_lang_Class jcgo_component; jObject jObject[len]; }

#define JCGO_STACKOBJ_OBJARRNEW(objname, methods, compclass, len) (jcgo_bzero((void *)JCGO_OBJREF_OF(objname), sizeof(objname)), *(jvtable *)&JCGO_METHODS_OF(JCGO_OBJREF_OF(objname)) = (jvtable)(&methods), *(JCGO_ARRLENGTH_T *)&JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(objname), length) = (len), JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(objname), jcgo_component) = (compclass), (jObjectArr)JCGO_OBJREF_OF(objname))
#define JCGO_STACKOBJ_ARRCLONE(objname, arrname) (jcgo_memcpy((void *)JCGO_OBJREF_OF(objname), (CONST void *)JCGO_OBJREF_OF(arrname), sizeof(objname)), JCGO_OBJREF_OF(objname))
#define JCGO_STACKOBJ_PRIMARRNEW(objname, methods, len) (jcgo_bzero((void *)JCGO_OBJREF_OF(objname), sizeof(objname)), *(jvtable *)&JCGO_METHODS_OF(JCGO_OBJREF_OF(objname)) = (jvtable)(&methods), *(JCGO_ARRLENGTH_T *)&JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(objname), length) = (len), JCGO_OBJREF_OF(objname))
#define JCGO_STACKOBJ_NEW(objname, methods) (jcgo_bzero((void *)JCGO_OBJREF_OF(objname), sizeof(objname)), (*(jvtable *)&JCGO_METHODS_OF(JCGO_OBJREF_OF(objname)) = (jvtable)(&methods)), JCGO_OBJREF_OF(objname))

#define JCGO_STACKOBJVLT_OBJARRNEW(objname, methods, compclass, len) (jcgo_bzeroVlt((void *)JCGO_OBJREF_OF(objname), sizeof(objname)), *(jvtable *)&JCGO_METHODS_OF(JCGO_OBJREF_OF(objname)) = (jvtable)(&methods), *(JCGO_ARRLENGTH_T *)&JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(objname), length) = (len), JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(objname), jcgo_component) = (compclass), (jObjectArr)JCGO_OBJREF_OF(objname))
#define JCGO_STACKOBJVLT_ARRCLONE(objname, arrname) (jcgo_memcpyVlt((void *)JCGO_OBJREF_OF(objname), (CONST void *)JCGO_OBJREF_OF(arrname), sizeof(objname)), JCGO_OBJREF_OF(objname))
#define JCGO_STACKOBJVLT_PRIMARRNEW(objname, methods, len) (jcgo_bzeroVlt((void *)JCGO_OBJREF_OF(objname), sizeof(objname)), *(jvtable *)&JCGO_METHODS_OF(JCGO_OBJREF_OF(objname)) = (jvtable)(&methods), *(JCGO_ARRLENGTH_T *)&JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(objname), length) = (len), JCGO_OBJREF_OF(objname))
#define JCGO_STACKOBJVLT_NEW(objname, methods) (jcgo_bzeroVlt((void *)JCGO_OBJREF_OF(objname), sizeof(objname)), (*(jvtable *)&JCGO_METHODS_OF(JCGO_OBJREF_OF(objname)) = (jvtable)(&methods)), JCGO_OBJREF_OF(objname))

#ifdef JCGO_NOJNI
#define JCGO_JNI_BLOCK(objArgsCnt) JNIEnv *jcgo_pJniEnv = jcgo_jniEnter();
#define JCGO_JNI_TOLOCALREF(index, obj) (jobject)(obj)
#else
#ifndef JCGO_JNI_DEFLOCALREFS
#define JCGO_JNI_DEFLOCALREFS 16
#endif
#define JCGO_JNI_BLOCK(objArgsCnt) JNIEnv *jcgo_pJniEnv; JCGO_STATIC_OBJARRAY((objArgsCnt) + JCGO_JNI_DEFLOCALREFS + 2) jcgo_stackjnilocals; jcgo_bzero((void *)JCGO_OBJREF_OF(jcgo_stackjnilocals), sizeof(jcgo_stackjnilocals)); *(JCGO_ARRLENGTH_T *)&JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(jcgo_stackjnilocals), length) = (objArgsCnt) + JCGO_JNI_DEFLOCALREFS + 2; jcgo_pJniEnv = jcgo_jniEnterX((jObjectArr)JCGO_OBJREF_OF(jcgo_stackjnilocals));
#define JCGO_JNI_TOLOCALREF(index, obj) ((JCGO_ARR_INTERNALACC(jObject, JCGO_OBJREF_OF(jcgo_stackjnilocals), (index) + 1) = (jObject)(obj)) != jnull ? (jobject)&JCGO_ARR_INTERNALACC(jObject, JCGO_OBJREF_OF(jcgo_stackjnilocals), (index) + 1) : NULL)
#endif

#define JCGO_ACCMOD_VOLATILE 0x40
#define JCGO_ACCMOD_TRANSIENT 0x80

#ifdef JCGO_STDCLINIT
#define JCGO_CLINIT_DEFN void (CFASTCALL *jcgo_clinitRtn)(void);
#define JCGO_CLINIT_INIT(clinit) clinit,
#define JCGO_CLINIT_BEGIN(clsvarname) (void)0
#define JCGO_CLINIT_DONE(clsvarname) (void)0
#ifdef JCGO_PARALLEL
#define JCGO_CLINIT_TRIG(clsvarname) jcgo_clinitTrig(JCGO_OBJREF_OF(clsvarname.jcgo_class))
#else
#define JCGO_CLINIT_TRIG(clsvarname) if (JCGO_EXPECT_FALSE((JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(clsvarname.jcgo_class), modifiers) & (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_TRANSIENT)) != 0)) jcgo_clinitTrig(JCGO_OBJREF_OF(clsvarname.jcgo_class))
#endif
#define JCGO_CLINIT_LITERACC(clsvarname, literalfield) (jcgo_clinitTrig(JCGO_OBJREF_OF(clsvarname.jcgo_class)), literalfield)
#define JCGO_CLINIT_VARACC(clsvarname, classfield) (jcgo_clinitTrig(JCGO_OBJREF_OF(clsvarname.jcgo_class)), &classfield)[0]
#define JCGO_STACKOBJ_NEWTRIG(objname, methods) (jcgo_clinitTrig(methods.jcgo_class), JCGO_STACKOBJ_NEW(objname, methods))
#define JCGO_STACKOBJVLT_NEWTRIG(objname, methods) (jcgo_clinitTrig(methods.jcgo_class), JCGO_STACKOBJVLT_NEW(objname, methods))
#else
#define JCGO_CLINIT_DEFN /* empty */
#define JCGO_CLINIT_INIT(clinit) /* empty */
#define JCGO_CLINIT_BEGIN(clsvarname) (void)(*(JCGO_THRD_VOLATILE jint *)&JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(clsvarname.jcgo_class), modifiers) &= ~(jint)JCGO_ACCMOD_VOLATILE)
#define JCGO_CLINIT_DONE(clsvarname) (void)(*(JCGO_THRD_VOLATILE jint *)&JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(clsvarname.jcgo_class), modifiers) &= ~(jint)JCGO_ACCMOD_TRANSIENT)
#define JCGO_CLINIT_LITERACC(clsvarname, literalfield) literalfield
#ifdef JCGO_CLINITCHK
#ifdef JCGO_THREADS
#define JCGO_CLINIT_TRIG(clsvarname) if (JCGO_EXPECT_FALSE((*(JCGO_THRD_VOLATILE jint *)&JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(clsvarname.jcgo_class), modifiers) & (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_TRANSIENT)) != 0)) jcgo_clinitCheckOrder(JCGO_OBJREF_OF(clsvarname.jcgo_class))
#else
#define JCGO_CLINIT_TRIG(clsvarname) if (JCGO_EXPECT_FALSE((JCGO_FIELD_NZACCESS(JCGO_OBJREF_OF(clsvarname.jcgo_class), modifiers) & JCGO_ACCMOD_VOLATILE) != 0)) jcgo_clinitCheckOrder(JCGO_OBJREF_OF(clsvarname.jcgo_class))
#endif
#define JCGO_CLINIT_VARACC(clsvarname, classfield) (jcgo_clinitCheckOrder(JCGO_OBJREF_OF(clsvarname.jcgo_class)), &classfield)[0]
#define JCGO_STACKOBJ_NEWTRIG(objname, methods) (jcgo_clinitCheckOrder(methods.jcgo_class), JCGO_STACKOBJ_NEW(objname, methods))
#define JCGO_STACKOBJVLT_NEWTRIG(objname, methods) (jcgo_clinitCheckOrder(methods.jcgo_class), JCGO_STACKOBJVLT_NEW(objname, methods))
#else
#define JCGO_CLINIT_TRIG(clsvarname) (void)0
#define JCGO_CLINIT_VARACC(clsvarname, classfield) classfield
#define JCGO_STACKOBJ_NEWTRIG(objname, methods) JCGO_STACKOBJ_NEW(objname, methods)
#define JCGO_STACKOBJVLT_NEWTRIG(objname, methods) JCGO_STACKOBJVLT_NEW(objname, methods)
#endif
#endif

struct jcgo_object_s;
typedef struct jcgo_object_s *jObject;

#ifdef JCGO_CVOLATILE

#define JCGO_VLT_LFETCH(reftype, field) field
#define JCGO_VLT_LSTORE(reftype, pfield, obj) (reftype)(*(jObject JCGO_THRD_VOLATILE *)pfield = (jObject)(obj))

#define JCGO_VLT_FETCHZ(field) field
#define JCGO_VLT_FETCHB(field) field
#define JCGO_VLT_FETCHC(field) field
#define JCGO_VLT_FETCHS(field) field
#define JCGO_VLT_FETCHI(field) field
#define JCGO_VLT_FETCHJ(field) field
#define JCGO_VLT_FETCHF(field) field
#define JCGO_VLT_FETCHD(field) field

#define JCGO_VLT_STOREZ(pfield, value) *pfield = value
#define JCGO_VLT_STOREB(pfield, value) *pfield = value
#define JCGO_VLT_STOREC(pfield, value) *pfield = value
#define JCGO_VLT_STORES(pfield, value) *pfield = value
#define JCGO_VLT_STOREI(pfield, value) *pfield = value
#define JCGO_VLT_STOREJ(pfield, value) *pfield = value
#define JCGO_VLT_STOREF(pfield, value) *pfield = value
#define JCGO_VLT_STORED(pfield, value) *pfield = value

#define JCGO_VLT_PREINCB(field) ++field
#define JCGO_VLT_PREINCC(field) ++field
#define JCGO_VLT_PREINCS(field) ++field
#define JCGO_VLT_PREINCI(field) ++field
#define JCGO_VLT_PREINCJ(field) ++field
#define JCGO_VLT_PREINCF(field) ++field
#define JCGO_VLT_PREINCD(field) ++field

#define JCGO_VLT_PREDECRB(field) --field
#define JCGO_VLT_PREDECRC(field) --field
#define JCGO_VLT_PREDECRS(field) --field
#define JCGO_VLT_PREDECRI(field) --field
#define JCGO_VLT_PREDECRJ(field) --field
#define JCGO_VLT_PREDECRF(field) --field
#define JCGO_VLT_PREDECRD(field) --field

#else /* JCGO_CVOLATILE */

#define JCGO_VLT_LFETCH(reftype, field) (reftype)jcgo_AO_fetchL((jObject JCGO_THRD_VOLATILE *)&field)
#define JCGO_VLT_LSTORE(reftype, pfield, obj) (reftype)jcgo_AO_storeL((jObject JCGO_THRD_VOLATILE *)pfield, (jObject)(obj))

#define JCGO_VLT_FETCHZ(field) jcgo_AO_fetchZ(&field)
#define JCGO_VLT_FETCHB(field) jcgo_AO_fetchB(&field)
#define JCGO_VLT_FETCHC(field) jcgo_AO_fetchC(&field)
#define JCGO_VLT_FETCHS(field) jcgo_AO_fetchS(&field)
#define JCGO_VLT_FETCHI(field) jcgo_AO_fetchI(&field)
#define JCGO_VLT_FETCHJ(field) jcgo_AO_fetchJ(&field)

#define JCGO_VLT_STOREZ(pfield, value) jcgo_AO_storeZ(pfield, value)
#define JCGO_VLT_STOREB(pfield, value) jcgo_AO_storeB(pfield, value)
#define JCGO_VLT_STOREC(pfield, value) jcgo_AO_storeC(pfield, value)
#define JCGO_VLT_STORES(pfield, value) jcgo_AO_storeS(pfield, value)
#define JCGO_VLT_STOREI(pfield, value) jcgo_AO_storeI(pfield, value)
#define JCGO_VLT_STOREJ(pfield, value) jcgo_AO_storeJ(pfield, value)

#define JCGO_VLT_PREINCB(field) jcgo_AO_preIncB(&field)
#define JCGO_VLT_PREINCC(field) jcgo_AO_preIncC(&field)
#define JCGO_VLT_PREINCS(field) jcgo_AO_preIncS(&field)
#define JCGO_VLT_PREINCI(field) jcgo_AO_preIncI(&field)
#define JCGO_VLT_PREINCJ(field) jcgo_AO_preIncJ(&field)

#define JCGO_VLT_PREDECRB(field) jcgo_AO_preDecrB(&field)
#define JCGO_VLT_PREDECRC(field) jcgo_AO_preDecrC(&field)
#define JCGO_VLT_PREDECRS(field) jcgo_AO_preDecrS(&field)
#define JCGO_VLT_PREDECRI(field) jcgo_AO_preDecrI(&field)
#define JCGO_VLT_PREDECRJ(field) jcgo_AO_preDecrJ(&field)

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_AO_fetchL(
 jObject JCGO_THRD_VOLATILE *pfield );
JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_AO_storeL(
 jObject JCGO_THRD_VOLATILE *pfield, jObject obj );

JCGO_NOSEP_STATIC jboolean CFASTCALL jcgo_AO_fetchZ(
 JCGO_THRD_VOLATILE jboolean *pfield );
JCGO_NOSEP_STATIC jbyte CFASTCALL jcgo_AO_fetchB(
 JCGO_THRD_VOLATILE jbyte *pfield );
JCGO_NOSEP_STATIC jchar CFASTCALL jcgo_AO_fetchC(
 JCGO_THRD_VOLATILE jchar *pfield );
JCGO_NOSEP_STATIC jshort CFASTCALL jcgo_AO_fetchS(
 JCGO_THRD_VOLATILE jshort *pfield );
JCGO_NOSEP_STATIC jint CFASTCALL jcgo_AO_fetchI(
 JCGO_THRD_VOLATILE jint *pfield );
JCGO_NOSEP_STATIC jlong CFASTCALL jcgo_AO_fetchJ(
 JCGO_THRD_VOLATILE jlong *pfield );

JCGO_NOSEP_STATIC jboolean CFASTCALL jcgo_AO_storeZ(
 JCGO_THRD_VOLATILE jboolean *pfield, jboolean value );
JCGO_NOSEP_STATIC jbyte CFASTCALL jcgo_AO_storeB(
 JCGO_THRD_VOLATILE jbyte *pfield, jbyte value );
JCGO_NOSEP_STATIC jchar CFASTCALL jcgo_AO_storeC(
 JCGO_THRD_VOLATILE jchar *pfield, jchar value );
JCGO_NOSEP_STATIC jshort CFASTCALL jcgo_AO_storeS(
 JCGO_THRD_VOLATILE jshort *pfield, jshort value );
JCGO_NOSEP_STATIC jint CFASTCALL jcgo_AO_storeI(
 JCGO_THRD_VOLATILE jint *pfield, jint value );
JCGO_NOSEP_STATIC jlong CFASTCALL jcgo_AO_storeJ(
 JCGO_THRD_VOLATILE jlong *pfield, jlong value );

JCGO_NOSEP_STATIC jbyte CFASTCALL jcgo_AO_preIncB(
 JCGO_THRD_VOLATILE jbyte *pfield );
JCGO_NOSEP_STATIC jchar CFASTCALL jcgo_AO_preIncC(
 JCGO_THRD_VOLATILE jchar *pfield );
JCGO_NOSEP_STATIC jshort CFASTCALL jcgo_AO_preIncS(
 JCGO_THRD_VOLATILE jshort *pfield );
JCGO_NOSEP_STATIC jint CFASTCALL jcgo_AO_preIncI(
 JCGO_THRD_VOLATILE jint *pfield );
JCGO_NOSEP_STATIC jlong CFASTCALL jcgo_AO_preIncJ(
 JCGO_THRD_VOLATILE jlong *pfield );

JCGO_NOSEP_STATIC jbyte CFASTCALL jcgo_AO_preDecrB(
 JCGO_THRD_VOLATILE jbyte *pfield );
JCGO_NOSEP_STATIC jchar CFASTCALL jcgo_AO_preDecrC(
 JCGO_THRD_VOLATILE jchar *pfield );
JCGO_NOSEP_STATIC jshort CFASTCALL jcgo_AO_preDecrS(
 JCGO_THRD_VOLATILE jshort *pfield );
JCGO_NOSEP_STATIC jint CFASTCALL jcgo_AO_preDecrI(
 JCGO_THRD_VOLATILE jint *pfield );
JCGO_NOSEP_STATIC jlong CFASTCALL jcgo_AO_preDecrJ(
 JCGO_THRD_VOLATILE jlong *pfield );

#ifdef JCGO_NOFP

#define JCGO_VLT_FETCHF(field) JCGO_VLT_FETCHI(field)
#define JCGO_VLT_FETCHD(field) JCGO_VLT_FETCHJ(field)
#define JCGO_VLT_STOREF(pfield, value) JCGO_VLT_STOREI(pfield, value)
#define JCGO_VLT_STORED(pfield, value) JCGO_VLT_STOREJ(pfield, value)
#define JCGO_VLT_PREINCF(field) JCGO_VLT_PREINCI(field)
#define JCGO_VLT_PREINCD(field) JCGO_VLT_PREINCJ(field)
#define JCGO_VLT_PREDECRF(field) JCGO_VLT_PREDECRI(field)
#define JCGO_VLT_PREDECRD(field) JCGO_VLT_PREDECRJ(field)

#else /* JCGO_NOFP */

#define JCGO_VLT_FETCHF(field) jcgo_AO_fetchF(&field)
#define JCGO_VLT_FETCHD(field) jcgo_AO_fetchD(&field)
#define JCGO_VLT_STOREF(pfield, value) jcgo_AO_storeF(pfield, value)
#define JCGO_VLT_STORED(pfield, value) jcgo_AO_storeD(pfield, value)
#define JCGO_VLT_PREINCF(field) jcgo_AO_preIncF(&field)
#define JCGO_VLT_PREINCD(field) jcgo_AO_preIncD(&field)
#define JCGO_VLT_PREDECRF(field) jcgo_AO_preDecrF(&field)
#define JCGO_VLT_PREDECRD(field) jcgo_AO_preDecrD(&field)

JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_AO_fetchF(
 JCGO_THRD_VOLATILE jfloat *pfield );
JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_AO_fetchD(
 JCGO_THRD_VOLATILE jdouble *pfield );

JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_AO_storeF(
 JCGO_THRD_VOLATILE jfloat *pfield, jfloat value );
JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_AO_storeD(
 JCGO_THRD_VOLATILE jdouble *pfield, jdouble value );

JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_AO_preIncF(
 JCGO_THRD_VOLATILE jfloat *pfield );
JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_AO_preIncD(
 JCGO_THRD_VOLATILE jdouble *pfield );

JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_AO_preDecrF(
 JCGO_THRD_VOLATILE jfloat *pfield );
JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_AO_preDecrD(
 JCGO_THRD_VOLATILE jdouble *pfield );

#endif /* ! JCGO_NOFP */

#endif /* ! JCGO_CVOLATILE */

#ifndef JCGO_SEHTRY

#ifdef JCGO_THREADS
struct jcgo_curmon_s
{
 jObject monObj;
 struct jcgo_curmon_s *last;
};
#endif

struct jcgo_try_s
{
 jmp_buf jbuf;
 jObject throwable;
 struct jcgo_try_s *last;
#ifdef JCGO_THREADS
 struct jcgo_curmon_s *pCurMon;
#endif
};

#endif /* ! JCGO_SEHTRY */

struct java_lang_Object_s;
struct java_lang_Class_s;
struct java_lang_String_s;
struct java_lang_Throwable_s;

typedef struct java_lang_Object_s *java_lang_Object;
typedef struct java_lang_Class_s *java_lang_Class;
typedef struct java_lang_String_s *java_lang_String;
typedef struct java_lang_Throwable_s *java_lang_Throwable;

struct jcgo_reflect_s;

struct jcgo_methods_s
{
 java_lang_Class jcgo_class; /* first */
 JCGO_GCJDESCR_DEFN /* second */
 JCGO_TYPEID_T jcgo_typeid;
 JCGO_OBJSIZE_T jcgo_objsize;
 CONST struct jcgo_reflect_s *jcgo_reflect;
 JCGO_CLINIT_DEFN
 jObject (CFASTCALL *jcgo_thisRtn)( jObject This );
};

typedef CONST struct jcgo_methods_s *jvtable;

struct jcgo_object_s
{
 jvtable JCGO_IMMFLD_CONST jcgo_methods; /* first */
 JCGO_MON_DEFN
};

struct jcgo_jobjectarr_s
{
 jvtable JCGO_IMMFLD_CONST jcgo_methods;
 JCGO_MON_DEFN
 JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length; /* third */
 java_lang_Class jcgo_component; /* fourth */
 jObject jObject[1];
};

struct jcgo_jbooleanarr_s
{
 jvtable JCGO_IMMFLD_CONST jcgo_methods;
 JCGO_MON_DEFN
 JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length;
 jboolean jboolean[1];
};

struct jcgo_jbytearr_s
{
 jvtable JCGO_IMMFLD_CONST jcgo_methods;
 JCGO_MON_DEFN
 JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length;
 jbyte jbyte[1];
};

struct jcgo_jchararr_s
{
 jvtable JCGO_IMMFLD_CONST jcgo_methods;
 JCGO_MON_DEFN
 JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length;
 jchar jchar[1];
};

struct jcgo_jshortarr_s
{
 jvtable JCGO_IMMFLD_CONST jcgo_methods;
 JCGO_MON_DEFN
 JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length;
 jshort jshort[1];
};

struct jcgo_jintarr_s
{
 jvtable JCGO_IMMFLD_CONST jcgo_methods;
 JCGO_MON_DEFN
 JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length;
 jint jint[1];
};

struct jcgo_jlongarr_s
{
 jvtable JCGO_IMMFLD_CONST jcgo_methods;
 JCGO_MON_DEFN
 JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length;
 jlong jlong[1];
};

struct jcgo_jfloatarr_s
{
 jvtable JCGO_IMMFLD_CONST jcgo_methods;
 JCGO_MON_DEFN
 JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length;
 jfloat jfloat[1];
};

struct jcgo_jdoublearr_s
{
 jvtable JCGO_IMMFLD_CONST jcgo_methods;
 JCGO_MON_DEFN
 JCGO_ARRLENGTH_T JCGO_IMMFLD_CONST length;
 jdouble jdouble[1];
};

typedef struct jcgo_jobjectarr_s *jObjectArr;
typedef struct jcgo_jbooleanarr_s *jbooleanArr;
typedef struct jcgo_jbytearr_s *jbyteArr;
typedef struct jcgo_jchararr_s *jcharArr;
typedef struct jcgo_jshortarr_s *jshortArr;
typedef struct jcgo_jintarr_s *jintArr;
typedef struct jcgo_jlongarr_s *jlongArr;
typedef struct jcgo_jfloatarr_s *jfloatArr;
typedef struct jcgo_jdoublearr_s *jdoubleArr;

struct jcgo_methodentry_s
{
 jObject (CFASTCALL *mproxy)( void (CFASTCALL *jmethod)( void ), jObject obj,
  jintArr intArgs, jlongArr longArgs, jfloatArr floatArgs,
  jdoubleArr doubleArgs, jObjectArr objectArgs );
 void (CFASTCALL *jmethod)( void );
};

struct jcgo_reflect_s
{
 jObjectArr fieldsName;
 jintArr fieldsSlot;
 jObjectArr fieldsType;
 jbyteArr fieldsDims;
 jshortArr fieldsModifiers;
 jObjectArr methodsName;
 jObjectArr methodsTypes;
 jObjectArr methodsDims;
 jObjectArr methodsThrows;
 jshortArr methodsModifiers;
 CONST struct jcgo_methodentry_s *methodsEntry;
};

JCGO_SEP_EXTERN CONST struct jcgo_methods_s jboolean_methods;
JCGO_SEP_EXTERN CONST struct jcgo_methods_s jbyte_methods;
JCGO_SEP_EXTERN CONST struct jcgo_methods_s jchar_methods;
JCGO_SEP_EXTERN CONST struct jcgo_methods_s jshort_methods;
JCGO_SEP_EXTERN CONST struct jcgo_methods_s jint_methods;
JCGO_SEP_EXTERN CONST struct jcgo_methods_s jlong_methods;
JCGO_SEP_EXTERN CONST struct jcgo_methods_s jfloat_methods;
JCGO_SEP_EXTERN CONST struct jcgo_methods_s jdouble_methods;
JCGO_SEP_EXTERN CONST struct jcgo_methods_s void_methods;

#ifndef JCGO_NOFP

#ifdef JCGO_SEPARATED
JCGO_SEP_EXTERN
#endif
jdouble jcgo_fpZero;

#ifdef JCGO_SEPARATED
JCGO_SEP_EXTERN
#endif
jfloat jcgo_fpZeroF;

#endif /* ! JCGO_NOFP */

#ifdef JCGO_SEPARATED
JCGO_SEP_EXTERN
#endif
jlong jcgo_trashVar;

#ifdef JCGO_NOJNI
JCGO_NOSEP_INLINE JNIEnv *JCGO_INLFRW_FASTCALL jcgo_jniEnter( void );
#else
JCGO_NOSEP_STATIC JNIEnv *CFASTCALL jcgo_jniEnterX( jObjectArr localObjs );
#endif

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_jniLeave( JNIEnv *pJniEnv,
 jobject obj );
JCGO_NOSEP_STATIC void CFASTCALL jcgo_jniNoNativeFunc( void );

#ifdef JCGO_THREADS
#ifdef JCGO_SEHTRY
JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_monitorEnter( jObject obj );
JCGO_NOSEP_STATIC void CFASTCALL jcgo_monitorLeave( jObject obj );
#else
JCGO_NOSEP_STATIC void CFASTCALL jcgo_monitorEnter(
 struct jcgo_curmon_s *pCurMon, jObject obj );
JCGO_NOSEP_STATIC void CFASTCALL jcgo_monitorLeave( void );
#endif
#endif

#ifdef JCGO_STDCLINIT
JCGO_NOSEP_STATIC void CFASTCALL jcgo_clinitTrig( java_lang_Class aclass );
#else
#ifdef JCGO_CLINITCHK
JCGO_NOSEP_STATIC void CFASTCALL jcgo_clinitCheckOrder(
 java_lang_Class aclass );
#endif
#endif

#ifdef JCGO_SEHTRY
JCGO_NOSEP_STATIC jObject *CFASTCALL jcgo_tryCatches( void );
#else
JCGO_NOSEP_EXTRASTATIC void CFASTCALL jcgo_tryEnter(
 struct jcgo_try_s *pCurTry );
JCGO_NOSEP_EXTRASTATIC void CFASTCALL jcgo_tryLeave( void );
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_bzeroVlt( void *dest,
 unsigned size );
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_memcpyVlt( void *dest,
 CONST void *src, unsigned size );
#endif

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_bzero( void *dest,
 unsigned size );
JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_memcpy( void *dest,
 CONST void *src, unsigned size );

JCGO_NOSEP_STATIC DECLSPECNORET void CFASTCALL jcgo_throwNullPtrExc( void );
JCGO_NOSEP_STATIC DECLSPECNORET void CFASTCALL jcgo_throwExc(
 jObject throwable );

JCGO_NOSEP_STATIC ATTRIBMALLOC jObject CFASTCALL jcgo_newObject(
 jvtable jcgo_methods );

JCGO_NOSEP_STATIC ATTRIBMALLOC jObject CFASTCALL jcgo_newArray(
 java_lang_Class aclass, int dims, jint len );
JCGO_NOSEP_INLINE ATTRIBMALLOC jObjectArr JCGO_INLFRW_FASTCALL
 jcgo_new4DArray( java_lang_Class aclass, int cnt, int dims, jint len0,
 jint len1, jint len2, jint len3 );
JCGO_NOSEP_INLINE ATTRIBMALLOC jObjectArr JCGO_INLFRW_FASTCALL
 jcgo_new16DArray( java_lang_Class aclass, int cnt, int dims, jint len0,
 jint len1, jint len2, jint len3, jint len4, jint len5, jint len6, jint len7,
 jint len8, jint len9, jint len10, jint len11, jint len12, jint len13,
 jint len14, jint len15 );
JCGO_NOSEP_STATIC ATTRIBMALLOC jObject CFASTCALL jcgo_arrayClone(
 jObject arr );

JCGO_NOSEP_INLINE int JCGO_INLFRW_FASTCALL jcgo_instanceOf0( int objId,
 int maxId, jObject obj );
JCGO_NOSEP_STATIC int CFASTCALL jcgo_instanceOf( int objId, int maxId,
 int dims, jObject obj );

JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkCast0( int objId,
 int maxId, jObject obj );
JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkCast( int objId,
 int maxId, int dims, jObject obj );
JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkNull( jObject obj );
JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkNullX( jObject obj );
JCGO_NOSEP_INLINE jint JCGO_INLFRW_FASTCALL jcgo_arrayLength( jObject arr );
JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_objArraySet( jObjectArr arr,
 jint index, jObject obj );

JCGO_NOSEP_INLINE jObject *JCGO_INLFRW_FASTCALL jcgo_jObjectArrAccess(
 jObjectArr arr, jint index );
JCGO_NOSEP_INLINE jObject *JCGO_INLFRW_FASTCALL jcgo_jObjectArrAccessNZ(
 jObjectArr arr, jint index );
JCGO_NOSEP_INLINE jboolean *JCGO_INLFRW_FASTCALL jcgo_jbooleanArrAccess(
 jbooleanArr arr, jint index );
JCGO_NOSEP_INLINE jboolean *JCGO_INLFRW_FASTCALL jcgo_jbooleanArrAccessNZ(
 jbooleanArr arr, jint index );
JCGO_NOSEP_INLINE jbyte *JCGO_INLFRW_FASTCALL jcgo_jbyteArrAccess(
 jbyteArr arr, jint index );
JCGO_NOSEP_INLINE jbyte *JCGO_INLFRW_FASTCALL jcgo_jbyteArrAccessNZ(
 jbyteArr arr, jint index );
JCGO_NOSEP_INLINE jchar *JCGO_INLFRW_FASTCALL jcgo_jcharArrAccess(
 jcharArr arr, jint index );
JCGO_NOSEP_INLINE jchar *JCGO_INLFRW_FASTCALL jcgo_jcharArrAccessNZ(
 jcharArr arr, jint index );
JCGO_NOSEP_INLINE jshort *JCGO_INLFRW_FASTCALL jcgo_jshortArrAccess(
 jshortArr arr, jint index );
JCGO_NOSEP_INLINE jshort *JCGO_INLFRW_FASTCALL jcgo_jshortArrAccessNZ(
 jshortArr arr, jint index );
JCGO_NOSEP_INLINE jint *JCGO_INLFRW_FASTCALL jcgo_jintArrAccess( jintArr arr,
 jint index );
JCGO_NOSEP_INLINE jint *JCGO_INLFRW_FASTCALL jcgo_jintArrAccessNZ(
 jintArr arr, jint index );
JCGO_NOSEP_INLINE jlong *JCGO_INLFRW_FASTCALL jcgo_jlongArrAccess(
 jlongArr arr, jint index );
JCGO_NOSEP_INLINE jlong *JCGO_INLFRW_FASTCALL jcgo_jlongArrAccessNZ(
 jlongArr arr, jint index );
JCGO_NOSEP_INLINE jfloat *JCGO_INLFRW_FASTCALL jcgo_jfloatArrAccess(
 jfloatArr arr, jint index );
JCGO_NOSEP_INLINE jfloat *JCGO_INLFRW_FASTCALL jcgo_jfloatArrAccessNZ(
 jfloatArr arr, jint index );
JCGO_NOSEP_INLINE jdouble *JCGO_INLFRW_FASTCALL jcgo_jdoubleArrAccess(
 jdoubleArr arr, jint index );
JCGO_NOSEP_INLINE jdouble *JCGO_INLFRW_FASTCALL jcgo_jdoubleArrAccessNZ(
 jdoubleArr arr, jint index );

#ifdef JCGO_SFTNULLP
JCGO_NOSEP_STATIC void CFASTCALL jcgo_throwNullPtrExcX( void );
JCGO_NOSEP_INLINE jint JCGO_INLFRW_FASTCALL jcgo_arrayLengthX( jObject arr );
#endif

#ifdef JCGO_INDEXCHK
JCGO_NOSEP_INLINE jObject *JCGO_INLFRW_FASTCALL jcgo_jObjectArrAccessX(
 jObjectArr arr, jint index );
JCGO_NOSEP_INLINE jObject *JCGO_INLFRW_FASTCALL jcgo_jObjectArrAccessNZX(
 jObjectArr arr, jint index );
JCGO_NOSEP_INLINE jboolean *JCGO_INLFRW_FASTCALL jcgo_jbooleanArrAccessX(
 jbooleanArr arr, jint index );
JCGO_NOSEP_INLINE jboolean *JCGO_INLFRW_FASTCALL jcgo_jbooleanArrAccessNZX(
 jbooleanArr arr, jint index );
JCGO_NOSEP_INLINE jbyte *JCGO_INLFRW_FASTCALL jcgo_jbyteArrAccessX(
 jbyteArr arr, jint index );
JCGO_NOSEP_INLINE jbyte *JCGO_INLFRW_FASTCALL jcgo_jbyteArrAccessNZX(
 jbyteArr arr, jint index );
JCGO_NOSEP_INLINE jchar *JCGO_INLFRW_FASTCALL jcgo_jcharArrAccessX(
 jcharArr arr, jint index );
JCGO_NOSEP_INLINE jchar *JCGO_INLFRW_FASTCALL jcgo_jcharArrAccessNZX(
 jcharArr arr, jint index );
JCGO_NOSEP_INLINE jshort *JCGO_INLFRW_FASTCALL jcgo_jshortArrAccessX(
 jshortArr arr, jint index );
JCGO_NOSEP_INLINE jshort *JCGO_INLFRW_FASTCALL jcgo_jshortArrAccessNZX(
 jshortArr arr, jint index );
JCGO_NOSEP_INLINE jint *JCGO_INLFRW_FASTCALL jcgo_jintArrAccessX( jintArr arr,
 jint index );
JCGO_NOSEP_INLINE jint *JCGO_INLFRW_FASTCALL jcgo_jintArrAccessNZX(
 jintArr arr, jint index );
JCGO_NOSEP_INLINE jlong *JCGO_INLFRW_FASTCALL jcgo_jlongArrAccessX(
 jlongArr arr, jint index );
JCGO_NOSEP_INLINE jlong *JCGO_INLFRW_FASTCALL jcgo_jlongArrAccessNZX(
 jlongArr arr, jint index );
JCGO_NOSEP_INLINE jfloat *JCGO_INLFRW_FASTCALL jcgo_jfloatArrAccessX(
 jfloatArr arr, jint index );
JCGO_NOSEP_INLINE jfloat *JCGO_INLFRW_FASTCALL jcgo_jfloatArrAccessNZX(
 jfloatArr arr, jint index );
JCGO_NOSEP_INLINE jdouble *JCGO_INLFRW_FASTCALL jcgo_jdoubleArrAccessX(
 jdoubleArr arr, jint index );
JCGO_NOSEP_INLINE jdouble *JCGO_INLFRW_FASTCALL jcgo_jdoubleArrAccessNZX(
 jdoubleArr arr, jint index );
#endif

#ifdef JCGO_CHKCAST
JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkCast0X( int objId,
 int maxId, jObject obj );
JCGO_NOSEP_INLINE jObject JCGO_INLFRW_FASTCALL jcgo_checkCastX( int objId,
 int maxId, int dims, jObject obj );
JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_objArraySetX( jObjectArr arr,
 jint index, jObject obj );
#endif

#ifdef JCGO_USELONG
JCGO_NOSEP_INLINE jlong JCGO_INLFRW_FASTCALL jcgo_lshl( jlong v, int cnt );
JCGO_NOSEP_INLINE jlong JCGO_INLFRW_FASTCALL jcgo_lshr( jlong v, int cnt );
JCGO_NOSEP_INLINE jlong JCGO_INLFRW_FASTCALL jcgo_lushr( jlong v, int cnt );
#endif

JCGO_NOSEP_INLINE jint JCGO_INLFRW_FASTCALL jcgo_div( jint v1, jint v2 );
JCGO_NOSEP_INLINE jint JCGO_INLFRW_FASTCALL jcgo_mod( jint v1, jint v2 );
JCGO_NOSEP_INLINE jlong JCGO_INLFRW_FASTCALL jcgo_ldiv( jlong v1, jlong v2 );
JCGO_NOSEP_INLINE jlong JCGO_INLFRW_FASTCALL jcgo_lmod( jlong v1, jlong v2 );

#ifdef JCGO_NOFP
JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_fdiv( jdouble d1, jdouble d2 );
JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_fdivf( jfloat f1, jfloat f2 );
JCGO_NOSEP_INLINE jdouble JCGO_INLFRW_FASTCALL jcgo_fmod( jdouble d1,
 jdouble d2 );
JCGO_NOSEP_INLINE jfloat JCGO_INLFRW_FASTCALL jcgo_fmodf( jfloat f1,
 jfloat f2 );
#else
#ifdef JCGO_FASTMATH
JCGO_NOSEP_INLINE jdouble JCGO_INLFRW_FASTCALL jcgo_fmod( jdouble d1,
 jdouble d2 );
JCGO_NOSEP_INLINE jfloat JCGO_INLFRW_FASTCALL jcgo_fmodf( jfloat f1,
 jfloat f2 );
#else
JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_fmod( jdouble d1, jdouble d2 );
JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_fmodf( jfloat f1, jfloat f2 );
#endif
JCGO_NOSEP_EXTRASTATIC jint CFASTCALL jcgo_jfloat2jint( jfloat f );
JCGO_NOSEP_EXTRASTATIC jlong CFASTCALL jcgo_jfloat2jlong( jfloat f );
JCGO_NOSEP_EXTRASTATIC jint CFASTCALL jcgo_jdouble2jint( jdouble d );
JCGO_NOSEP_EXTRASTATIC jlong CFASTCALL jcgo_jdouble2jlong( jdouble d );
JCGO_NOSEP_INLINE jfloat JCGO_INLFRW_FASTCALL jcgo_jdouble2jfloat(
 jdouble d );
#ifdef JCGO_FPFAST
#define jcgo_fdiv(d1, d2) ((jdouble)((d1) / (jdouble)(d2)))
#define jcgo_fdivf(f1, f2) ((jfloat)((f1) / (jfloat)(f2)))
#else
JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_fdiv( jdouble d1, jdouble d2 );
JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_fdivf( jfloat f1, jfloat f2 );
JCGO_NOSEP_STATIC int CFASTCALL jcgo_fequal( jdouble d1, jdouble d2 );
JCGO_NOSEP_STATIC int CFASTCALL jcgo_fequalf( jfloat f1, jfloat f2 );
JCGO_NOSEP_STATIC int CFASTCALL jcgo_flessequ( jdouble d1, jdouble d2 );
JCGO_NOSEP_STATIC int CFASTCALL jcgo_flessequf( jfloat f1, jfloat f2 );
JCGO_NOSEP_STATIC int CFASTCALL jcgo_flessthan( jdouble d1, jdouble d2 );
JCGO_NOSEP_STATIC int CFASTCALL jcgo_flessthanf( jfloat f1, jfloat f2 );
#endif
#endif

#endif
