/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 **
 * Comment: contains x11-specific fixes.
 */

#include "jni.h"

#ifdef WITH_UNDERSCORE
#define JVM_Sleep _JVM_Sleep
#endif

JNIEXPORT void JNICALL
JVM_Sleep(JNIEnv *env, jclass threadClass, jlong millis)
{
  jmethodID mid =
   (*env)->GetStaticMethodID(env, threadClass, "sleep", "(J)V");
  if (mid != 0)
    (*env)->CallStaticVoidMethod(env, threadClass, mid, millis);
}
