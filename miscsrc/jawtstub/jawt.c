/*
 * @(#) jawtstub/jawt.c - the Java AWT Native Interface stub implementation.
 * Copyright (C) 2009 Ivan Maidanski <ivmai@mail.ru> All rights reserved.
 */

/*
 * Used control macros: JAWTSTUB_NOGETDS.
 */

/*
 * Comment: this source code is for creating a dummy "jawt" shared library
 * (libjawt.so or jawt.dll) providing no access to the AWT drawing surface.
 * Usage scope: headless environments, non-AWT GUI environments.
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

#ifndef _JNI_IMPLEMENTATION_
#define _JNI_IMPLEMENTATION_ 1
#endif

#ifndef _JAWT_MD_H
#include "jawt_md.h"
#endif

#ifndef JAWTSTUB_NOGETDS

#ifndef STATIC
#define STATIC static
#endif

STATIC JAWT_DrawingSurface *JNICALL
jawt_DSGetDrawingSurface(JNIEnv *pJniEnv, jobject target)
{
 return NULL;
}

STATIC jobject JNICALL
jawt_DSGetComponent(JNIEnv *pJniEnv, void *platformInfo)
{
 return NULL;
}

STATIC void JNICALL
jawt_DSFreeDrawingSurface(JAWT_DrawingSurface *ds)
{
 /* dummy */
}


STATIC void JNICALL
jawt_DSLockAWT(JNIEnv *pJniEnv)
{
 /* dummy */
}

STATIC void JNICALL
jawt_DSUnlockAWT(JNIEnv *pJniEnv)
{
 /* dummy */
}

#endif /* ! JAWTSTUB_NOGETDS */

JNIEXPORT jboolean JNICALL
JAWT_GetAWT(JNIEnv *pJniEnv, JAWT *awt)
{
#ifndef JAWTSTUB_NOGETDS
 if (awt != NULL && awt->version >= JAWT_VERSION_1_3)
 {
  awt->GetDrawingSurface = jawt_DSGetDrawingSurface;
  awt->FreeDrawingSurface = jawt_DSFreeDrawingSurface;
  if (awt->version > JAWT_VERSION_1_3)
  {
   awt->Lock = jawt_DSLockAWT;
   awt->Unlock = jawt_DSUnlockAWT;
   awt->GetComponent = jawt_DSGetComponent;
  }
  return JNI_TRUE;
 }
#endif
 return JNI_FALSE;
}
