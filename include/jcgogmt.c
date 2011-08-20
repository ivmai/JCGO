/*
 * @(#) $(JCGO)/include/jcgogmt.c --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
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

JCGO_NOSEP_STATIC jlongArr CFASTCALL
gnu_java_lang_management_VMThreadMXBeanImpl__findDeadlockedThreads0__I(
 jint isMonitorOnly )
{
 /* not implemented */
 return (jlongArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jlong), 0, 0);
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
gnu_java_lang_management_VMThreadMXBeanImpl__getThreadInfoForId0__J(
 jlong id )
{
 /* not implemented */
 return jnull;
}

JCGO_NOSEP_STATIC jlong CFASTCALL
gnu_java_lang_management_VMThreadMXBeanImpl__getThreadCpuUserTime0__JI(
 jlong id, jint isUserTime )
{
 /* not implemented */
 return (jlong)0L;
}

#endif
