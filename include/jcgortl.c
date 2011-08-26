/*
 * @(#) $(JCGO)/include/jcgortl.c --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2011 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/**
 * This file is compiled together with the files produced by the JCGO
 * translator (do not include and/or compile this file directly).
 */

/*
 * Used control macros (also see in jcgobase.c, jcgofp.c, jcgojnie.c,
 * jcgojnmd.h, jcgomem.h, jcgortl.h, jcgothrd.c, jcgothrd.h): JCGO_CVOLATILE,
 * JCGO_HUGEARR, JCGO_NATSEP, JCGO_NOCREATJVM, JCGO_NOCTRLC, JCGO_NOGC,
 * JCGO_NOFATALMSG, JCGO_NOJNI, JCGO_RTASSERT, JCGO_SEHTRY, JCGO_THREADS,
 * JCGO_TRUEABORT, JCGO_UNIX, JCGO_WMAIN.
 * Macros for tuning: CLIBDECL, JAVADEFPROPS, MAINENTRY.
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

#ifndef _STDLIB_H
#include <stdlib.h>
#endif

#ifndef JCGO_UNIX
#ifndef _STDDEF_H
#include <stddef.h>
#endif
#endif

#ifndef _STDIO_H
#include <stdio.h>
#endif

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef OBJT_java_lang_VMThrowable
#ifndef _SETJMP_H
#include <setjmp.h>
#endif
#endif

#ifdef JCGO_UNIX
#ifndef _UNISTD_H
#include <unistd.h>
#endif
#endif

#ifndef _SIGNAL_H
#include <signal.h>
#endif

#ifndef CLIBDECL
#ifdef __CLIB
#define CLIBDECL __CLIB
#else
#ifdef _USERENTRY
#define CLIBDECL _USERENTRY
#else
#ifdef _RTL_FUNC
#define CLIBDECL _RTL_FUNC
#else
#ifdef JCGO_UNIX
#define CLIBDECL /* empty */
#else
#define CLIBDECL __cdecl
#endif
#endif
#endif
#endif
#endif

#ifndef MAINENTRY
#ifdef JCGO_WMAIN
#define MAINENTRY int CLIBDECL wmain
#else
#define MAINENTRY int CLIBDECL main
#endif
#endif

#ifndef JCGO_NOCTRLC
#ifdef OBJT_java_lang_VMRuntime_TermHandler
#ifndef SIGHUP
#ifndef SIGINT
#ifndef SIGTERM
#define JCGO_NOCTRLC
#endif
#endif
#endif
#else
#define JCGO_NOCTRLC
#endif
#endif

#ifdef JCGO_NOJNI
#ifndef JCGO_NOCREATJVM
#define JCGO_NOCREATJVM
#endif
#ifndef OBJT_java_lang_ref_ReferenceQueue
#define JCGO_FNLZDATA_OMITREFQUE
#endif
#endif

#ifdef JCGO_TRUEABORT
#define JCGO_ABORT_EXIT abort()
#else
#define JCGO_ABORT_EXIT exit(-1)
#endif

#define JCGO_CAST_PTRTONUM(ptr) ((char *)(ptr) - (char *)NULL)

#include "jcgomem.h"

#ifndef JCGO_NOFP
#include "jcgofp.h"
#endif

#ifdef JCGO_THREADS

#ifdef JCGO_NATCLASS_sun_misc_Unsafe
#ifdef JCGO_CVOLATILE
#ifdef JCGO_PARALLEL
#undef JCGO_CVOLATILE
#endif
#endif
#endif

#include "jcgothrd.c"

#ifndef JCGO_CVOLATILE
#include "jcgoaop.c"
#endif

#endif /* JCGO_THREADS */

#include "jcgobase.c"

#include "jcgoxcpt.c"

#include "jcgomem.c"

#include "jcgoobjs.c"

#include "jcgoarth.c"

#include "jcgofp.c"

#ifdef JCGO_NATCLASS_java_lang_VMClass
#include "jcgovcls.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMClassLoader
#include "jcgocldr.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMCompiler
#include "jcgocmpl.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMObject
#include "jcgovobj.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMRuntime
#include "jcgovrtm.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMString
#include "jcgovstr.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMSystem
#include "jcgosyst.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMThread
#include "jcgovthr.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMThrowable
#include "jcgothrw.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMFloat
#include "jcgovflt.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMDouble
#include "jcgovdbl.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_reflect_VMArray
#include "jcgorarr.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_reflect_VMField
#include "jcgorfld.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_reflect_VMConstructor
#include "jcgorctr.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_reflect_VMMethod
#include "jcgormet.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_ref_VMReference
#include "jcgorefs.c"
#endif

#ifdef JCGO_NATCLASS_java_nio_VMDirectByteBuffer
#include "jcgodbuf.c"
#endif

#ifdef JCGO_NATCLASS_sun_misc_Unsafe
#include "jcgounsf.c"
#endif

#ifdef JCGO_NATCLASS_java_lang_VMMath
#include "jcgomath.c"
#endif

#ifdef JCGO_NATCLASS_gnu_classpath_VMStackWalker
#include "jcgostkw.c"
#endif

#ifdef JCGO_NATCLASS_gnu_java_lang_VMInstrumentationImpl
#include "jcgogli.c"
#endif

#ifdef JCGO_NATCLASS_gnu_java_lang_management_VMClassLoadingMXBeanImpl
#include "jcgogml.c"
#endif

#ifdef JCGO_NATCLASS_gnu_java_lang_management_VMCompilationMXBeanImpl
#include "jcgogmc.c"
#endif

#ifdef JCGO_NATCLASS_gnu_java_lang_management_VMGarbageCollectorMXBeanImpl
#include "jcgogmg.c"
#endif

#ifdef JCGO_NATCLASS_gnu_java_lang_management_VMMemoryMXBeanImpl
#include "jcgogmm.c"
#endif

#ifdef JCGO_NATCLASS_gnu_java_lang_management_VMMemoryManagerMXBeanImpl
#include "jcgogmn.c"
#endif

#ifdef JCGO_NATCLASS_gnu_java_lang_management_VMMemoryPoolMXBeanImpl
#include "jcgogmp.c"
#endif

#ifdef JCGO_NATCLASS_gnu_java_lang_management_VMOperatingSystemMXBeanImpl
#include "jcgogms.c"
#endif

#ifdef JCGO_NATCLASS_gnu_java_lang_management_VMRuntimeMXBeanImpl
#include "jcgogmr.c"
#endif

#ifdef JCGO_NATCLASS_gnu_java_lang_management_VMThreadMXBeanImpl
#include "jcgogmt.c"
#endif

#ifndef JCGO_NOJNI

#include "jcgoutf.c"

#include "jcgojniv.c"

#include "jcgojnia.c"

#include "jcgojnif.c"

#include "jcgojnir.c"

#include "jcgojnie.c"

#endif /* ! JCGO_NOJNI */

#ifndef JCGO_NATSEP

#include "jcgojnu.c"

#ifdef JCGO_NATCLASS_gnu_classpath_VMSystemProperties
#include "jcgoprop.c"
#endif

#ifdef JCGO_NATCLASS_java_net_VMInetAddress
#include "jcgonet.c"
#include "jcgodns.c"
#else
#ifdef JCGO_NATCLASS_gnu_java_net_VMPlainSocketImpl
#include "jcgonet.c"
#endif
#endif

#ifndef JCGO_NATCLASS_gnu_java_nio_VMChannel
#define JCGO_EXCLUDE_VMCHANNEL
#endif
#ifndef JCGO_NATCLASS_java_io_VMFile
#define JCGO_EXCLUDE_VMFILE
#endif
#ifndef JCGO_NATCLASS_java_util_VMTimeZone
#define JCGO_EXCLUDE_VMTIMEZONE
#endif
#include "jcgofile.c"

#ifdef JCGO_NATCLASS_java_lang_VMProcess
#include "jcgoexec.c"
#endif

#endif /* ! JCGO_NATSEP */

#endif
