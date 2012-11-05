/*
 * @(#) $(JCGO)/include/jcgomem.h --
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
 * Used control macros: GC_DEBUG, GC_DLL, JCGO_GCINC, JCGO_HUGEARR, JCGO_NOGC,
 * JCGO_THREADS, JCGO_USEGCJ, JCGO_WIN32.
 * Macros for tuning: GC_CALLBACK.
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

#ifndef _STRING_H
#include <string.h>
/* void *memmove(void *, const void *, size_t); */
/* void *memset(void *, int, size_t); */
#endif

#define JCGO_MEM_ZERO(dest, size) (void)memset(dest, '\0', size) /* bzero(dest, size) */
#define JCGO_MEM_CPY(dest, src, size) (void)memmove(dest, src, size) /* (void)memcpy(dest, src, size) */

#ifdef JCGO_WIN32

#ifndef STRICT
#define STRICT 1
#endif

#ifndef WIN32
#define WIN32
#endif

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 1
#endif

#endif /* JCGO_WIN32 */

#ifdef JCGO_HUGEARR
#define JCGO_ALLOCSIZE_T unsigned long
#endif

#define JCGO_MEM_MAXLIMIT(ulongtype) (sizeof(void *) < sizeof(ulongtype) ? ((ulongtype)2L << (sizeof(void *) * 8 - 1)) - (ulongtype)(sizeof(void *) > 2 ? 0x1000 : 0x10) : ((ulongtype)-1L) >> 1)

#ifdef JCGO_NOGC

#ifndef JCGO_ALLOCSIZE_T
#define JCGO_ALLOCSIZE_T unsigned /* size_t */
#endif

#ifdef JCGO_HUGEARR

#ifndef _MALLOC_H
#include <malloc.h>
#endif

/* #include <malloc.h> */
/* #include <stdlib.h> */
/* void *halloc(long, size_t); */

#define JCGO_MEM_CALLOC(pobjptr, size, jcgo_methods) ((void)((long)(size) >= 0 ? (void *)((*(pobjptr) = halloc((long)(size), 1)) != NULL ? (jcgo_hbzero(*(pobjptr), size), NULL) : NULL) : (*(pobjptr) = NULL)))

#else /* JCGO_HUGEARR */

/* #include <stdlib.h> */
/* void *calloc(size_t, size_t); */

#define JCGO_MEM_CALLOC(pobjptr, size, jcgo_methods) ((void)(*(pobjptr) = JCGO_EXPECT_TRUE((size) <= (sizeof(JCGO_ALLOCSIZE_T) < 4 ? ~(JCGO_ALLOCSIZE_T)0xf : ~(JCGO_ALLOCSIZE_T)0xfff)) ? calloc(size, 1) : NULL))

#endif /* ! JCGO_HUGEARR */

#define JCGO_MEM_POSTINIT(x) (void)0

#define JCGO_MEM_REGWEAKLINK(plink, objptr) 0

#define JCGO_MEM_GCGETCOUNT(x) 0
#define JCGO_MEM_GCOLLECT(x) (void)0

#define JCGO_MEM_HEAPCURSIZE(x) 0
#define JCGO_MEM_CORELEFT(x) 0

#else /* JCGO_NOGC */

#ifdef JCGO_THREADS
#ifndef GC_THREADS
#ifdef JCGO_WIN32
#ifndef GC_WIN32_THREADS
#define GC_WIN32_THREADS
#endif
#else
#define GC_THREADS
#endif
#endif
#endif

#ifndef GC_DLL
#ifndef GC_NOT_DLL
#define GC_NOT_DLL
#endif
#endif

#ifndef GC_DEBUG
#ifndef GC_IGNORE_WARN
#define GC_IGNORE_WARN
#endif
#endif

#include "gc.h"
/* int GC_GENERAL_REGISTER_DISAPPEARING_LINK(void **, void *); */
/* void GC_INIT(void); */
/* void GC_REGISTER_FINALIZER_NO_ORDER(void *, GC_finalization_proc, void *, GC_finalization_proc *, void **); */
/* void *GC_MALLOC_ATOMIC(size_t); */
/* void *GC_MALLOC(size_t); */
/* void GC_add_roots(void *, void *); */
/* void GC_gcollect(void); */
/* void GC_gcollect_and_unmap(void); */
/* size_t GC_get_free_bytes(void); */
/* GC_word GC_get_gc_no(void); */
/* size_t GC_get_heap_size(void); */
/* int GC_invoke_finalizers(void); */
/* void GC_set_finalize_on_demand(int); */
/* void GC_set_java_finalization(int); */

#include "javaxfc.h"
/* void GC_finalize_all(void); */

/* #include "gc.h" */
/* void GC_clear_roots(void); */
/* void GC_set_all_interior_pointers(int); */
/* void GC_set_no_dls(int); */

#ifdef JCGO_GCINC
/* #include "gc.h" */
/* void GC_enable_incremental(void); */
#define JCGO_MEMINIT_INCMODE GC_enable_incremental()
#else
#define JCGO_MEMINIT_INCMODE (void)0
#endif

#ifndef JCGO_ALLOCSIZE_T
#define JCGO_ALLOCSIZE_T GC_word
#endif

#ifdef JCGO_USEGCJ

#ifndef _GC_GCJ_H
#include "gc_gcj.h"
/* void *GC_GCJ_MALLOC(size_t, void *); */
/* void GC_init_gcj_malloc(int, void *); */
#endif

#define JCGO_MEMINIT_GCJSUPP GC_init_gcj_malloc(5, 0)

#define JCGO_MEM_CALLOC(pobjptr, size, jcgo_methods) ((void)(JCGO_EXPECT_TRUE((size) < (JCGO_ALLOCSIZE_T)((size_t)-1L < (size_t)1 ? ~((size_t)1L << (sizeof(size_t) * 8 - 1)) : ~(size_t)0)) ? ((jcgo_methods) != NULL ? (*(pobjptr) = (jcgo_methods) != (void *)&((volatile char *)NULL)[-1] && ((struct jcgo_methods_s *)(jcgo_methods))->jcgo_gcjdescr != (void *)GC_DS_LENGTH ? GC_GCJ_MALLOC((size_t)(size), (void *)(jcgo_methods)) : GC_MALLOC((size_t)(size))) : (void *)(JCGO_EXPECT_TRUE((*(pobjptr) = GC_MALLOC_ATOMIC((size_t)(size))) != NULL) ? (JCGO_MEM_ZERO(*(pobjptr), (size_t)(size)), NULL) : NULL)) : (*(pobjptr) = NULL)))

#else /* JCGO_USEGCJ */

#define JCGO_MEMINIT_GCJSUPP (void)0

#define JCGO_MEM_CALLOC(pobjptr, size, jcgo_methods) ((void)(JCGO_EXPECT_TRUE((size) < (JCGO_ALLOCSIZE_T)((size_t)-1L < (size_t)1 ? ~((size_t)1L << (sizeof(size_t) * 8 - 1)) : ~(size_t)0)) ? ((jcgo_methods) != NULL ? (*(pobjptr) = GC_MALLOC((size_t)(size))) : (void *)(JCGO_EXPECT_TRUE((*(pobjptr) = GC_MALLOC_ATOMIC((size_t)(size))) != NULL) ? (JCGO_MEM_ZERO(*(pobjptr), (size_t)(size)), NULL) : NULL)) : (*(pobjptr) = NULL)))

#endif /* ! JCGO_USEGCJ */

#define JCGO_MEM_POSTINIT(x) (GC_set_finalize_on_demand(0), GC_set_java_finalization(1), JCGO_MEMINIT_INCMODE, JCGO_MEMINIT_GCJSUPP)

#define JCGO_MEM_REGWEAKLINK(plink, objptr) GC_GENERAL_REGISTER_DISAPPEARING_LINK(plink, objptr)

/* #include "gc.h" */
/* void *GC_base(void *); */
#define JCGO_MEM_SAFEREGWEAKLINK(plink, objptr, alreadysafe) ((alreadysafe) || GC_base(objptr) != NULL ? GC_GENERAL_REGISTER_DISAPPEARING_LINK(plink, objptr) : 0)

#define JCGO_MEM_GCGETCOUNT(x) GC_get_gc_no()
#define JCGO_MEM_GCOLLECT(x) GC_gcollect()

#define JCGO_MEM_HEAPCURSIZE(x) GC_get_heap_size()
#define JCGO_MEM_CORELEFT(x) GC_get_free_bytes()

#ifdef JCGO_THREADS

/* #include "gc.h" */
/* void GC_allow_register_threads(void); */
/* void *GC_call_with_alloc_lock(GC_fn_type, void *); */
/* void *GC_call_with_stack_base(GC_stack_base_func, void *); */
/* int GC_get_stack_base(struct GC_stack_base *); */
/* int GC_register_my_thread(struct GC_stack_base *); */
/* void GC_set_finalizer_notifier(GC_finalizer_notifier_proc); */
/* int GC_unregister_my_thread(void); */

#define JCGO_MEM_CALLWITHSYNC(fn, data) GC_call_with_alloc_lock(fn, data)

#endif /* JCGO_THREADS */

#define JCGO_MEM_REGJFINALIZER(objptr, fn, data, polddata) GC_REGISTER_FINALIZER_NO_ORDER(objptr, fn, data, NULL, polddata)

#ifndef JCGO_MEM_SAFEREGJFINALIZER
#define JCGO_MEM_SAFEREGJFINALIZER(objptr, fn, data, polddata, alreadysafe) JCGO_MEM_REGJFINALIZER(objptr, fn, data, polddata)
#endif

#endif /* ! JCGO_NOGC */

#ifndef JCGO_MEM_SAFEREGWEAKLINK
#define JCGO_MEM_SAFEREGWEAKLINK(plink, objptr, alreadysafe) JCGO_MEM_REGWEAKLINK(plink, objptr)
#endif

#ifndef JCGO_MEM_CALLWITHSYNC
#define JCGO_MEM_CALLWITHSYNC(fn, data) (*(fn))(data)
#endif

#ifndef GC_CALLBACK
#define GC_CALLBACK /* empty */
#endif

#ifndef JCGO_MEM_HEAPDESTROY
#define JCGO_MEM_HEAPDESTROY (void)0
#endif

#endif
