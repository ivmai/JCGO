/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 **
 * Comment: contains win32-specific fixes.
 */

#include "jni.h"

#ifdef WITH_UNDERSCORE
#define JVM_GetClassName _JVM_GetClassName
#endif

JNIEXPORT jstring JNICALL
JVM_GetClassName(JNIEnv *env, jclass cls)
{
  jclass clazz;
  jmethodID mid;
  jstring result = NULL;
  if ((*env)->EnsureLocalCapacity(env, 3) >= 0)
   {
     clazz = (*env)->GetObjectClass(env, (jobject)cls);
     mid = (*env)->GetMethodID(env, clazz, "getName", "()Ljava/lang/String;");
     result = mid != 0 ? (jstring)(*env)->CallObjectMethod(env,
               (jobject)cls, mid) : (*env)->NewStringUTF(env, "");
     (*env)->DeleteLocalRef(env, clazz);
   }
  return result;
}
