/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

#include <signal.h>

#include "jni.h"

#ifdef WITH_UNDERSCORE
#define JVM_RaiseSignal _JVM_RaiseSignal
#endif

JNIEXPORT jboolean JNICALL JVM_RaiseSignal(jint sig)
{ /* used only for SIGTERM (15) */
  raise(sig); /* should use msvcrt.dll on win32 */
  return JNI_TRUE;
}
