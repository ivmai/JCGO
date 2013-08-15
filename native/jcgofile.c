/*
 * @(#) $(JCGO)/native/jcgofile.c --
 * a part of the JCGO native layer library (file I/O impl).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2013 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_CLOCKGETTM, JCGO_ERRSTDOUT, JCGO_ERRTOLOG,
 * JCGO_JNIUSCORE, JCGO_NOCWDIR, JCGO_NOFILES, JCGO_NOTIME, JCGO_NOUTIMBUF,
 * JCGO_ONEROOTFS, JCGO_SYSDUALW, JCGO_SYSWCHAR, JCGO_UNIFSYS, JCGO_WIN32,
 * JCGO_WINFILE, JCGO_WMAIN.
 * Macros for tuning: STATIC, STATICDATA.
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

#ifndef JCGO_BUILDING_JNU
#define JCGO_BUILDING_FILEAPI
#include "jcgojnu.h"
#endif

#ifdef JCGO_VER

#include "jcgofile.h"

#ifndef STATIC
#define STATIC static
#endif

#ifndef STATICDATA
#define STATICDATA STATIC
#endif

#define JCGO_ALIGNED_BUFSIZE(type) (sizeof(type) > sizeof(void *) ? sizeof(type) + (sizeof(void *) << 1) - 1 : (sizeof(type) << 1) - 1)
#define JCGO_ALIGNED_BUFPTR(type, buf) ((type *)(volatile void *)&(buf)[((sizeof(type) > sizeof(void *) ? sizeof(void *) << 1 : sizeof(type)) - 1) - (unsigned)((char *)(buf) - &((char *)NULL)[1]) % (sizeof(type) > sizeof(void *) ? sizeof(void *) << 1 : sizeof(type))])

#ifndef JCGO_NOTIME
#define JCGO_WINEPOCH_MILLIS ((LONGLONG)134774L * 24L * 60L * 60L * 1000L)
#ifdef JCGO_WIN32
#ifndef _WINBASE_NO_QUERYPERFORMANCECOUNTER
STATICDATA double jcgo_win32PerfTickNanos = 0;
STATICDATA int jcgo_win32UsePerfCounter = 0;
#endif
#endif
#endif

#ifdef JCGO_SYSWCHAR

#ifdef JCGO_SYSDUALW

#ifndef JCGO_USEWCHAR_VAREXPORT
#ifdef JNUBIGEXPORT
#define JCGO_USEWCHAR_VAREXPORT JNUBIGEXPORT
#else
#define JCGO_USEWCHAR_VAREXPORT JNIEXPORT
#endif
#endif

JCGO_USEWCHAR_VAREXPORT int jcgo_sysWCharOn = 0;

#ifndef JCGO_USEWCHAR_OSMAJORVERMIN
#ifdef JCGO_WIN32
#define JCGO_USEWCHAR_OSMAJORVERMIN 5 /* "Windows 2000" */
#endif
#endif

#ifdef JCGO_USEWCHAR_OSMAJORVERMIN
#ifndef JCGO_JNIUSCORE
#define _Java_gnu_classpath_VMSystemProperties_getOsVerMajorMinor0 Java_gnu_classpath_VMSystemProperties_getOsVerMajorMinor0
#endif
#ifdef JCGO_BUILDING_JNU
#ifndef JCGO_NATCLASS_gnu_classpath_VMSystemProperties
#undef JCGO_USEWCHAR_OSMAJORVERMIN
#endif
#else
#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getOsVerMajorMinor0
JNIIMPORT jint JNICALL
_Java_gnu_classpath_VMSystemProperties_getOsVerMajorMinor0( JNIEnv *pJniEnv,
 jclass This, jint isMinor );
#endif
#endif
#endif

#endif /* JCGO_SYSDUALW */

#endif /* JCGO_SYSWCHAR */

#ifdef JCGO_ERRTOLOG

STATICDATA int jcgo_outLogFD = 0;

STATICDATA int jcgo_errLogFD = 0;

#ifndef JCGO_NOFILES

#ifndef JCGO_WINFILE
#define JCGO_OPENLOG_MODE (JCGO_OPEN_BASEMODE | O_WRONLY | O_TRUNC)
#endif

STATIC int jcgo_initFileEnvLog( void )
{
#ifndef _STDLIB_NO_GETENV
 char *cstr = getenv("STDERROUT_LOGFILE");
#ifdef JCGO_WINFILE
#ifdef JCGO_SYSWCHAR
 wchar_t wbuf[JCGO_PATH_MAXSIZE];
#endif
#endif
 if (cstr != NULL && *cstr)
 {
#ifdef JCGO_WINFILE
  return (int)JCGO_WINF_HANDLETONUM(JCGO_JNUTCHAR_E(CreateFileA(cstr,
          GENERIC_WRITE, FILE_SHARE_READ, NULL, CREATE_ALWAYS,
          FILE_ATTRIBUTE_NORMAL, NULL), (unsigned)mbstowcs(wbuf, cstr,
          JCGO_PATH_MAXSIZE) < (unsigned)JCGO_PATH_MAXSIZE ? CreateFileW(wbuf,
          GENERIC_WRITE, FILE_SHARE_READ, NULL, CREATE_ALWAYS,
          FILE_ATTRIBUTE_NORMAL, NULL) : INVALID_HANDLE_VALUE));
#else
  return open(cstr, JCGO_OPENLOG_MODE | O_CREAT, JCGO_PERM_OPEN);
#endif
 }
#endif
 return -1;
}

STATIC int jcgo_initFileLogA( int redirect, CONST char *argv0 )
{
 unsigned i;
 unsigned len;
#ifdef JCGO_WINFILE
#ifdef JCGO_SYSWCHAR
 wchar_t wbuf[JCGO_PATH_MAXSIZE];
#endif
#endif
 char cbuf[JCGO_PATH_MAXSIZE];
 if ((jcgo_errLogFD = jcgo_initFileEnvLog()) == -1 && argv0 != NULL)
 {
  len = 0;
  while ((cbuf[len] = argv0[len]) != '\0')
   if (++len >= sizeof(cbuf))
    break;
  if (len - (unsigned)1 < sizeof(cbuf) - 1)
  {
   i = len;
#ifdef JCGO_UNIFSYS
   cbuf[len] = '.';
#else
   do
   {
    if (!(--i) || cbuf[i - 1] == JCGO_FILE_CSEP)
    {
     i = len;
     cbuf[len] = '.';
     break;
    }
   } while (cbuf[i] != '.');
#endif
   if (i < sizeof(cbuf) - 4)
   {
    cbuf[i + 1] = 'l';
    cbuf[i + 2] = 'o';
    cbuf[i + 3] = 'g';
    cbuf[i + 4] = '\0';
#ifdef JCGO_WINFILE
    jcgo_errLogFD = (int)JCGO_WINF_HANDLETONUM(JCGO_JNUTCHAR_E(CreateFileA(
                     cbuf, GENERIC_WRITE, FILE_SHARE_READ, NULL,
                     (redirect & 0x4) != 0 ? CREATE_ALWAYS :
                     TRUNCATE_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL),
                     (unsigned)mbstowcs(wbuf, cbuf, JCGO_PATH_MAXSIZE) <
                     (unsigned)JCGO_PATH_MAXSIZE ? CreateFileW(wbuf,
                     GENERIC_WRITE, FILE_SHARE_READ, NULL,
                     (redirect & 0x4) != 0 ? CREATE_ALWAYS :
                     TRUNCATE_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL) :
                     INVALID_HANDLE_VALUE));
#else
    jcgo_errLogFD = open(cbuf, (redirect & 0x4) != 0 ? JCGO_OPENLOG_MODE |
                     O_CREAT : JCGO_OPENLOG_MODE, JCGO_PERM_OPEN);
#endif
   }
  }
 }
 (void)JCGO_FD_SETCLOEXEC(jcgo_errLogFD);
 if (jcgo_errLogFD == -1)
  return -1;
 jcgo_outLogFD = jcgo_errLogFD;
 if ((redirect & 0x2) == 0)
 {
  jcgo_outLogFD = JCGO_FD_OUTFD;
#ifndef JCGO_WINFILE
#ifndef JCGO_UNIFSYS
  setmode(JCGO_FD_OUTFD, O_BINARY);
#endif
#endif
 }
 return 0;
}

#ifdef JCGO_WMAIN

STATIC int jcgo_initFileLogW( int redirect, CONST wchar_t *wargv0 )
{
 unsigned i;
 unsigned len;
 wchar_t wbuf[JCGO_PATH_MAXSIZE];
 if ((jcgo_errLogFD = jcgo_initFileEnvLog()) == -1 && wargv0 != NULL)
 {
  len = 0;
  while ((wbuf[len] = wargv0[len]) != (wchar_t)0)
   if (++len >= sizeof(wbuf) / sizeof(wchar_t))
    break;
  if (len - (unsigned)1 < sizeof(wbuf) / sizeof(wchar_t) - 1)
  {
   i = len;
#ifdef JCGO_UNIFSYS
   wbuf[len] = (wchar_t)0x2e; /*'.'*/
#else
   do
   {
    if (!(--i) || wbuf[i - 1] == (wchar_t)JCGO_FILE_WSEP)
    {
     i = len;
     wbuf[len] = (wchar_t)0x2e; /*'.'*/
     break;
    }
   } while (wbuf[i] != (wchar_t)0x2e); /*'.'*/
#endif
   if (i < sizeof(wbuf) / sizeof(wchar_t) - 4)
   {
    wbuf[i + 1] = (wchar_t)0x6c; /*'l'*/
    wbuf[i + 2] = (wchar_t)0x6f; /*'o'*/
    wbuf[i + 3] = (wchar_t)0x67; /*'g'*/
    wbuf[i + 4] = (wchar_t)0;
#ifdef JCGO_WINFILE
    jcgo_errLogFD = (int)JCGO_WINF_HANDLETONUM(CreateFileW(wbuf,
                     GENERIC_WRITE, FILE_SHARE_READ, NULL,
                     (redirect & 0x4) != 0 ? CREATE_ALWAYS :
                     TRUNCATE_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL));
#else
    jcgo_errLogFD = _wopen(wbuf, (redirect & 0x4) != 0 ? JCGO_OPENLOG_MODE |
                     O_CREAT : JCGO_OPENLOG_MODE, JCGO_PERM_OPEN);
#endif
   }
  }
 }
 (void)JCGO_FD_SETCLOEXEC(jcgo_errLogFD);
 if (jcgo_errLogFD == -1)
  return -1;
 jcgo_outLogFD = jcgo_errLogFD;
 if ((redirect & 0x2) == 0)
 {
  jcgo_outLogFD = JCGO_FD_OUTFD;
#ifndef JCGO_WINFILE
#ifndef JCGO_UNIFSYS
  setmode(JCGO_FD_OUTFD, O_BINARY);
#endif
#endif
 }
 return 0;
}

#endif /* JCGO_WMAIN */

#endif /* ! JCGO_NOFILES */

#endif /* JCGO_ERRTOLOG */

STATIC void jcgo_initFileIO( void )
{
#ifndef JCGO_NOTIME
#ifdef JCGO_WIN32
#ifndef _WINBASE_NO_QUERYPERFORMANCECOUNTER
 LARGE_INTEGER timerFreq;
#endif
#endif
#endif
#ifdef JCGO_SYSWCHAR
#ifdef JCGO_SYSDUALW
#ifndef _STDLIB_NO_GETENV
 char *cstr;
#endif
#endif
#endif
#ifndef JCGO_UNIFSYS
#ifndef JCGO_WINFILE
#ifdef O_TEXT
 setmode(JCGO_FD_INFD, O_TEXT);
#endif
#ifndef JCGO_ERRTOLOG
 setmode(JCGO_FD_OUTFD, O_BINARY);
#ifndef JCGO_ERRSTDOUT
 setmode(JCGO_FD_ERRFD, O_BINARY);
#endif
#endif
#endif
#endif
#ifdef LC_CTYPE
 setlocale(LC_CTYPE, "");
#endif
#ifndef JCGO_NOTIME
#ifndef JCGO_WINFILE
#ifndef _TIME_NO_TZSET
 tzset();
#endif
#endif
#ifdef JCGO_WIN32
#ifndef _WINBASE_NO_QUERYPERFORMANCECOUNTER
 if (QueryPerformanceFrequency(&timerFreq) && timerFreq.QuadPart > 1L &&
     timerFreq.QuadPart <= (LONGLONG)1000L * 1000L * 1000L)
 {
  jcgo_win32PerfTickNanos = 1.0e9 / (double)timerFreq.QuadPart;
  jcgo_win32UsePerfCounter = 1;
 }
#endif
#endif
#endif
#ifdef JCGO_SYSWCHAR
#ifdef JCGO_SYSDUALW
 jcgo_sysWCharOn = 0;
#ifndef _STDLIB_NO_GETENV
 if ((cstr = getenv("USE_UNICODE_API")) != NULL && *cstr)
 {
  if (*cstr != '0' || *(cstr + 1))
   jcgo_sysWCharOn = 1;
 }
  else
#endif
  {
#ifdef JCGO_USEWCHAR_OSMAJORVERMIN
#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getOsVerMajorMinor0
   if ((int)_Java_gnu_classpath_VMSystemProperties_getOsVerMajorMinor0(
       NULL, NULL, 0) >= JCGO_USEWCHAR_OSMAJORVERMIN)
    jcgo_sysWCharOn = 1;
#endif
#endif
  }
#endif
#endif
}

JCGO_JNI_EXPF(void,
jcgo_JavaWriteLnToStderr)( CONST char *cstr, CONST char *cstr2 )
{
#ifdef JCGO_ERRTOLOG
 int fd = jcgo_errLogFD;
#else
#ifdef JCGO_ERRSTDOUT
 int fd = JCGO_FD_OUTFD;
#else
 int fd = JCGO_FD_ERRFD;
#endif
#endif
 unsigned len = 0;
#ifdef JCGO_WINFILE
 HANDLE handle = JCGO_WINF_NUMTOHANDLE(fd);
 DWORD nBytesWritten;
#else
 int errcode;
#endif
 while (*(cstr + len))
  len++;
 if (!len ||
#ifdef JCGO_WINFILE
     WriteFile(handle, cstr, (DWORD)len, &nBytesWritten, NULL)
#else
     JCGO_FD_WRITE(fd, (void *)cstr, len, &errcode) >= 0
#endif
     )
 {
  len = 0;
  while (*(cstr2 + len))
   len++;
#ifdef JCGO_WINFILE
  if (!len || WriteFile(handle, cstr2, (DWORD)len, &nBytesWritten, NULL))
   (void)WriteFile(handle, (void *)JCGO_NEW_LINE, sizeof(JCGO_NEW_LINE) - 1,
    &nBytesWritten, NULL);
#else
  if (!len || JCGO_FD_WRITE(fd, (void *)cstr2, len, &errcode) >= 0)
   (void)JCGO_FD_WRITE(fd, (void *)JCGO_NEW_LINE, sizeof(JCGO_NEW_LINE) - 1,
    &errcode);
#endif
 }
}

JCGO_JNI_EXPF(char **,
jcgo_JavaInitInOutA)( char **argv, jint redirect )
{
 JCGO_ARGV_WILDCARDSTMT(&argv);
 jcgo_initFileIO();
#ifdef JCGO_ERRTOLOG
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(redirect);
#else
 if (((int)redirect & 0x1) != 0 &&
     !jcgo_initFileLogA((int)redirect, argv != NULL ? argv[0] : NULL))
  return argv;
#endif
 jcgo_outLogFD = JCGO_FD_OUTFD;
 jcgo_errLogFD = JCGO_FD_ERRFD;
#ifndef JCGO_WINFILE
#ifndef JCGO_UNIFSYS
 setmode(JCGO_FD_OUTFD, O_BINARY);
 setmode(JCGO_FD_ERRFD, O_BINARY);
#endif
#endif
#else
 JCGO_UNUSED_VAR(redirect);
#endif
 return argv;
}

JCGO_JNI_EXPF(jstring,
jcgo_JavaConvertCmdArgA)( JNIEnv *pJniEnv, CONST char *cstr )
{
 return jcgo_JnuNewStringPlatform(pJniEnv, cstr);
}

#ifdef JCGO_WMAIN

JCGO_JNI_EXPF(wchar_t **,
jcgo_JavaInitInOutW)( wchar_t **wargv, jint redirect )
{
 JCGO_ARGV_WWILDCARDSTMT(&wargv);
 jcgo_initFileIO();
#ifdef JCGO_ERRTOLOG
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(redirect);
#else
 if (((int)redirect & 0x1) != 0 &&
     !jcgo_initFileLogW((int)redirect, wargv != NULL ? wargv[0] : NULL))
  return wargv;
#endif
 jcgo_outLogFD = JCGO_FD_OUTFD;
 jcgo_errLogFD = JCGO_FD_ERRFD;
#ifndef JCGO_WINFILE
#ifndef JCGO_UNIFSYS
 setmode(JCGO_FD_OUTFD, O_BINARY);
 setmode(JCGO_FD_ERRFD, O_BINARY);
#endif
#endif
#else
 JCGO_UNUSED_VAR(redirect);
#endif
 return wargv;
}

JCGO_JNI_EXPF(jstring,
jcgo_JavaConvertCmdArgW)( JNIEnv *pJniEnv, CONST wchar_t *wstr )
{
 return jcgo_JnuNewStringWide(pJniEnv, wstr);
}

#endif /* JCGO_WMAIN */

#ifdef JCGO_WINFILE

int jcgo_winFileErrnoGet(void)
{
 DWORD error = GetLastError();
 switch (error)
 {
 case 0:
  return 0;
 case ERROR_NOT_READY:
  return EAGAIN;
 case ERROR_INVALID_HANDLE:
 case ERROR_INVALID_TARGET_HANDLE:
  return EBADF;
 case ERROR_BUSY:
 case ERROR_BUSY_DRIVE:
 case ERROR_NO_PROC_SLOTS:
 case ERROR_PATH_BUSY:
 case ERROR_PIPE_BUSY:
  return EBUSY;
 case ERROR_ALREADY_ASSIGNED:
 case ERROR_ALREADY_EXISTS:
 case ERROR_FILE_EXISTS:
  return EEXIST;
 case ERROR_INSUFFICIENT_BUFFER:
 case ERROR_INVALID_DATA:
 case ERROR_INVALID_FUNCTION:
 case ERROR_INVALID_PARAMETER:
  return EINVAL;
 case ERROR_NO_MORE_SEARCH_HANDLES:
 case ERROR_TOO_MANY_OPEN_FILES:
  return EMFILE;
 case ERROR_BAD_PATHNAME:
 case ERROR_BUFFER_OVERFLOW:
 case ERROR_FILENAME_EXCED_RANGE:
 case ERROR_FILE_NOT_FOUND:
 case ERROR_INVALID_DRIVE:
 case ERROR_INVALID_NAME:
 case ERROR_OPEN_FAILED:
 case ERROR_PATH_NOT_FOUND:
  return ENOENT;
 case ERROR_BAD_FORMAT:
 case ERROR_BAD_LENGTH:
 case ERROR_CHILD_NOT_COMPLETE:
 case ERROR_EXE_MACHINE_TYPE_MISMATCH:
 case ERROR_TOO_MANY_MODULES:
  return ENOEXEC;
 case ERROR_NOT_ENOUGH_MEMORY:
 case ERROR_OUTOFMEMORY:
 case ERROR_OUT_OF_STRUCTURES:
  return ENOMEM;
 case ERROR_DISK_FULL:
 case ERROR_HANDLE_DISK_FULL:
  return ENOSPC;
 case ERROR_BAD_PIPE:
 case ERROR_BROKEN_PIPE:
 case ERROR_NO_DATA:
 case ERROR_PIPE_NOT_CONNECTED:
  return EPIPE;
 case ERROR_WRITE_PROTECT:
  return EROFS;
 case ERROR_NEGATIVE_SEEK:
 case ERROR_SEEK_ON_DEVICE:
  return ESPIPE;
 }
 return error == ERROR_DIRECTORY ? ENOTDIR :
         error >= ERROR_INVALID_STARTING_CODESEG &&
         error <= ERROR_INFLOOP_IN_RELOC_CHAIN ? ENOEXEC : EACCES;
}

#endif /* JCGO_WINFILE */

#ifndef JCGO_EXCLUDE_VMCHANNEL

#ifndef JCGO_NOFILES

#define JCGO_FCHMODE_READ 0x1
#define JCGO_FCHMODE_WRITE 0x2
#define JCGO_FCHMODE_APPEND 0x4
#define JCGO_FCHMODE_EXCL 0x8
#define JCGO_FCHMODE_SYNC 0x10
#define JCGO_FCHMODE_DSYNC 0x20

#ifndef JCGO_WINFILE

STATIC jlong jcgo_lseek( int fd, jlong ofs, int whence, int *perrcode )
{
 JCGO_BIGFLSEEK_T pos = (JCGO_BIGFLSEEK_T)ofs;
 if (sizeof(pos) < sizeof(jlong) && whence == SEEK_SET &&
     (ofs & ~(((jlong)2L << (sizeof(pos) < sizeof(jlong) ?
     (int)sizeof(pos) * 8 - 1 : 0)) - (jlong)1L)) != (jlong)0L)
  pos = (JCGO_BIGFLSEEK_T)-1;
 JCGO_BIGF_VLSEEK(fd, &pos, whence);
 ofs = (jlong)-1L;
 pos = (JCGO_BIGFLSEEK_T)(pos + 1);
 if (pos)
 {
  ofs = (jlong)(pos - 1);
  if (sizeof(pos) < sizeof(jlong) && ofs < (jlong)0L)
   ofs = ((jlong)2L << (sizeof(pos) < sizeof(jlong) ?
          (int)sizeof(pos) * 8 - 1 : 0)) + ofs;
 }
  else *perrcode = errno;
 return ofs;
}

STATIC int jcgo_ftruncate( int fd, int *perrcode )
{
#ifdef JCGO_FD_SETENDOFFILE
 return JCGO_FD_SETENDOFFILE(fd, perrcode);
#else
 JCGO_BIGFTRUNC_T ofs;
 JCGO_BIGFLSEEK_T pos = 0;
 JCGO_BIGF_VLSEEK(fd, &pos, SEEK_CUR);
 pos = (JCGO_BIGFLSEEK_T)(pos + 1);
 if (pos)
 {
  ofs = (JCGO_BIGFTRUNC_T)(pos - 1);
  if ((JCGO_BIGFLSEEK_T)ofs + (JCGO_BIGFLSEEK_T)1 != pos)
   ofs = (JCGO_BIGFTRUNC_T)-1;
  if (!JCGO_BIGF_TRUNC(fd, ofs))
   return 0;
 }
 *perrcode = errno;
 return -1;
#endif
}

#endif /* ! JCGO_WINFILE */

#endif /* ! JCGO_NOFILES */

#ifndef NOJAVA_gnu_java_nio_VMChannel_getStdinFD0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_getStdinFD0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return JCGO_FD_INFD;
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_getStdoutFD0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_getStdoutFD0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_ERRTOLOG
 return jcgo_outLogFD;
#else
 return JCGO_FD_OUTFD;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_getStderrFD0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_getStderrFD0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_ERRTOLOG
 return jcgo_errLogFD;
#else
#ifdef JCGO_ERRSTDOUT
 return JCGO_FD_OUTFD;
#else
 return JCGO_FD_ERRFD;
#endif
#endif
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_isIOErrorFileExists0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_isIOErrorFileExists0)( JNIEnv *pJniEnv,
 jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return (int)res == -EEXIST ? 1 : 0;
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_isIOErrorNoResources0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_isIOErrorNoResources0)( JNIEnv *pJniEnv,
 jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return (int)res == -EAGAIN || (int)res == -EMFILE || (int)res == -ENFILE ||
         (int)res == -ENOMEM ? 1 : 0;
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_isIOErrorInterrupted0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_isIOErrorInterrupted0)( JNIEnv *pJniEnv,
 jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return (int)res == -EINTR || (int)res == -EAGAIN ? 1 : 0;
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_getIOErrorMsg0
JCGO_JNI_EXPF(jstring,
Java_gnu_java_nio_VMChannel_getIOErrorMsg0)( JNIEnv *pJniEnv, jclass This,
 jint res )
{
#ifdef JCGO_WINFILE
 char *cstr = NULL;
 switch (-(int)res)
 {
 case EACCES:
  cstr = "Permission denied";
  break;
 case EAGAIN:
  cstr = "No more processes";
  break;
 case EBADF:
  cstr = "Bad file number";
  break;
 case EBUSY:
  cstr = "Device or resource busy";
  break;
 case EEXIST:
  cstr = "File exists";
  break;
 case EINVAL:
  cstr = "Invalid argument";
  break;
 case EMFILE:
  cstr = "Too many open files";
  break;
 case ENOENT:
  cstr = "No such file or directory";
  break;
 case ENOEXEC:
  cstr = "Exec format error";
  break;
 case ENOMEM:
  cstr = "Not enough space";
  break;
 case ENOSPC:
  cstr = "No space left on device";
  break;
 case ENOTDIR:
  cstr = "Not a directory";
  break;
 case EPIPE:
  cstr = "Broken pipe";
  break;
 case EROFS:
  cstr = "Read-only file system";
  break;
 case ESPIPE:
  cstr = "Illegal seek";
  break;
 }
#else
 char *cstr = strerror(-(int)res);
#endif
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, cstr != NULL ? cstr : "");
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_fileOpen0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_fileOpen0)( JNIEnv *pJniEnv, jclass This,
 jintArray fdArr, jstring path, jint mode )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fdArr);
 JCGO_UNUSED_VAR(path);
 JCGO_UNUSED_VAR(mode);
 return -ENOENT;
#else
#ifdef JCGO_WINFILE
 DWORD dwDesiredAccess;
 DWORD dwShareMode;
 DWORD dwCreationDisposition;
 DWORD dwFlagsAndAttributes;
 HANDLE handle;
 LONG distanceToMoveHigh = 0;
#else
 int flags;
 int accperm;
 int fd;
#endif
 int errcode = ENOENT;
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 JCGO_UNUSED_VAR(This);
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
  dwShareMode = FILE_SHARE_READ | FILE_SHARE_WRITE;
  dwCreationDisposition = OPEN_ALWAYS;
  dwFlagsAndAttributes = FILE_ATTRIBUTE_NORMAL;
  if (((int)mode & (JCGO_FCHMODE_READ | JCGO_FCHMODE_WRITE)) ==
      (JCGO_FCHMODE_READ | JCGO_FCHMODE_WRITE))
  {
   dwDesiredAccess = GENERIC_READ | GENERIC_WRITE;
   if (((int)mode & JCGO_FCHMODE_EXCL) != 0)
    dwCreationDisposition = CREATE_NEW;
   dwFlagsAndAttributes = FILE_ATTRIBUTE_NORMAL | FILE_FLAG_RANDOM_ACCESS;
  }
   else
   {
    if (((int)mode & JCGO_FCHMODE_READ) != 0)
    {
     dwDesiredAccess = GENERIC_READ;
     dwCreationDisposition = OPEN_EXISTING;
    }
     else
     {
      dwDesiredAccess = GENERIC_WRITE;
      if (((int)mode & JCGO_FCHMODE_EXCL) != 0)
       dwCreationDisposition = CREATE_NEW;
       else if (((int)mode & JCGO_FCHMODE_APPEND) == 0)
        dwCreationDisposition = CREATE_ALWAYS;
     }
   }
  if (((int)mode & (JCGO_FCHMODE_SYNC | JCGO_FCHMODE_DSYNC)) != 0 &&
      (dwDesiredAccess & GENERIC_WRITE) != 0)
   dwFlagsAndAttributes |= FILE_FLAG_WRITE_THROUGH;
  handle = JCGO_JNUTCHAR_E(CreateFileA(JCGO_JNUTCHAR_C(tbuf), dwDesiredAccess,
            dwShareMode, NULL, dwCreationDisposition, dwFlagsAndAttributes,
            NULL), CreateFileW(tbuf, dwDesiredAccess, dwShareMode, NULL,
            dwCreationDisposition, dwFlagsAndAttributes, NULL));
  if (handle != INVALID_HANDLE_VALUE)
  {
   if (((int)mode & JCGO_FCHMODE_APPEND) != 0)
    (void)SetFilePointer(handle, 0, &distanceToMoveHigh, FILE_END);
  }
   else errcode = jcgo_winFileErrnoGet();
#else
  flags = ((int)mode & (JCGO_FCHMODE_READ | JCGO_FCHMODE_WRITE)) ==
           (JCGO_FCHMODE_READ | JCGO_FCHMODE_WRITE) ? JCGO_OPEN_BASEMODE |
           O_CREAT | O_RDWR : ((int)mode & JCGO_FCHMODE_READ) != 0 ?
           JCGO_OPEN_BASEMODE | O_RDONLY :
           ((int)mode & JCGO_FCHMODE_APPEND) != 0 ?
           JCGO_OPEN_BASEMODE | O_CREAT | O_WRONLY | O_APPEND :
           JCGO_OPEN_BASEMODE | O_CREAT | O_WRONLY | O_TRUNC;
  accperm = JCGO_PERM_OPEN;
  if (((int)mode & JCGO_FCHMODE_EXCL) != 0 && (flags & O_CREAT) != 0)
  {
   accperm = JCGO_PERM_TMPFILE;
   flags |= O_EXCL;
  }
  if (((int)mode & JCGO_FCHMODE_SYNC) != 0)
   flags |= O_SYNC;
  if (((int)mode & JCGO_FCHMODE_DSYNC) != 0)
   flags |= O_DSYNC;
  if ((flags & (O_CREAT | O_APPEND)) != (O_CREAT | O_APPEND) ||
      (fd = JCGO_FD_TOPEN(flags & ~O_CREAT, tbuf, 0, &errcode)) == -1)
   fd = JCGO_FD_TOPEN(flags, tbuf, accperm, &errcode);
  (void)JCGO_FD_SETCLOEXEC(fd);
#endif
  JCGO_FILEIOCALL_END(pJniEnv)
#ifdef JCGO_WINFILE
  jcgo_JnuSetIntArrayElement(pJniEnv, fdArr, 0,
   (jint)JCGO_WINF_HANDLETONUM(handle));
  if (handle != INVALID_HANDLE_VALUE)
   return 0;
#else
  jcgo_JnuSetIntArrayElement(pJniEnv, fdArr, 0, (jint)fd);
  if (fd != -1)
   return 0;
#endif
 }
 return (jint)(-errcode);
#endif
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_fileRead0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_fileRead0)( JNIEnv *pJniEnv, jclass This,
 jbyteArray buffer, jint off, jint len, jint fd )
{
 int res;
 int errcode;
 char *buf =
  (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, buffer, off, len);
#ifdef JCGO_WINFILE
 DWORD nBytesRead = 0;
 if (fd == -2)
  return 0;
#endif
 JCGO_UNUSED_VAR(This);
 if (buf == NULL)
  return 0;
 res = (int)(((unsigned)-1) >> 2) + 1;
 errcode = ~(int)((volatile char *)buf - (volatile char *)NULL);
 if ((jint)res >= len)
  res = (int)len;
 if ((unsigned)res > (unsigned)errcode)
  res = errcode + 1;
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
 if (ReadFile(JCGO_WINF_NUMTOHANDLE(fd), buf, (DWORD)res, &nBytesRead,
     NULL) || nBytesRead)
  res = (int)nBytesRead;
  else
  {
   errcode = jcgo_winFileErrnoGet();
   res = -1;
  }
#else
 res = JCGO_FD_READ((int)fd, buf, (unsigned)res, &errcode);
#endif
 JCGO_FILEIOCALL_END(pJniEnv)
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, buffer, (jbyte *)buf, off);
 return (jint)(res >= 0 ? res : errcode != EPIPE ? -errcode : 0);
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_fileWrite0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_fileWrite0)( JNIEnv *pJniEnv, jclass This,
 jbyteArray buffer, jint off, jint len, jint fd )
{
 int res;
 int errcode;
 char *buf =
  (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, buffer, off, len);
#ifdef JCGO_WINFILE
 DWORD nBytesWritten;
 if (fd == -2)
  return len > 0 ? len : 0;
#endif
 JCGO_UNUSED_VAR(This);
 if (buf == NULL)
  return 0;
 res = (int)(((unsigned)-1) >> 2) + 1;
 errcode = ~(int)((volatile char *)buf - (volatile char *)NULL);
 if ((jint)res >= len)
  res = (int)len;
 if (len > 0)
 {
  if ((unsigned)res > (unsigned)errcode)
   res = errcode + 1;
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
  if (WriteFile(JCGO_WINF_NUMTOHANDLE(fd), buf, (DWORD)res, &nBytesWritten,
      NULL))
   res = (int)nBytesWritten;
   else
   {
    errcode = jcgo_winFileErrnoGet();
    res = -1;
   }
#else
  res = JCGO_FD_WRITE((int)fd, buf, (unsigned)res, &errcode);
#endif
  JCGO_FILEIOCALL_END(pJniEnv)
  if (res < 0 && errcode == ENOSPC)
   res = 0;
 }
#ifndef JCGO_NOFILES
  else
  {
   JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
   res = 0;
   if (!SetEndOfFile(JCGO_WINF_NUMTOHANDLE(fd)))
   {
    errcode = jcgo_winFileErrnoGet();
    res = -1;
   }
#else
   res = jcgo_ftruncate((int)fd, &errcode);
#endif
   JCGO_FILEIOCALL_END(pJniEnv)
  }
#endif
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, buffer, (jbyte *)buf, off);
 return (jint)(res >= 0 ? res : -errcode);
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_fileAvailable0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_fileAvailable0)( JNIEnv *pJniEnv, jclass This,
 jint fd )
{
#ifdef JCGO_FD_INAVAIL
 JCGO_FDINAVAIL_T avail = 0;
 int res;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 res = JCGO_FD_INAVAIL((int)fd, &avail);
 if (res < 0 && (res = -errno) >= 0)
  res = -EINVAL;
 JCGO_FILEIOCALL_END(pJniEnv)
 if (res >= 0 && (res = (int)avail) < 0)
  res = (int)(((unsigned)-1) >> 1);
 return (jint)res;
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 return -EINVAL;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_fileSelect0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_fileSelect0)( JNIEnv *pJniEnv, jclass This,
 jint fd, jint iswrite )
{
 int res = -EINVAL;
#ifdef JCGO_FD_SELECT
 struct timeval tv;
 fd_set fdsread;
 if ((int)fd >= 0)
 {
  FD_ZERO(&fdsread);
  tv.tv_sec = 0;
  tv.tv_usec = 0;
  FD_SET((unsigned)fd, &fdsread);
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
  res = JCGO_FD_SELECT((int)fd + 1, (int)iswrite ? NULL : &fdsread,
         (int)iswrite ? &fdsread : NULL, NULL, &tv);
  if (res < 0 && (res = -errno) >= 0)
   res = -EINVAL;
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#else
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(iswrite);
#endif
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return (jint)res;
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_fileSeek0
JCGO_JNI_EXPF(jlong,
Java_gnu_java_nio_VMChannel_fileSeek0)( JNIEnv *pJniEnv, jclass This,
 jlong ofs, jint fd, jint direction )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(ofs);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(direction);
 return -ESPIPE;
#else
 jlong pos;
#ifdef JCGO_WINFILE
 LONG distanceToMoveHigh;
#else
 int errcode = EINVAL;
#endif
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
 distanceToMoveHigh = (LONG)(((LONGLONG)ofs) >> (sizeof(LONG) * 8));
 if ((DWORD)(pos = SetFilePointer(JCGO_WINF_NUMTOHANDLE(fd), (LONG)ofs,
     &distanceToMoveHigh, (int)direction > 0 ? FILE_BEGIN :
     (int)direction < 0 ? FILE_END : FILE_CURRENT)) != (DWORD)-1L ||
     GetLastError() == 0)
 {
  pos = (jlong)(((LONGLONG)distanceToMoveHigh << (sizeof(DWORD) * 8)) |
         (DWORD)pos);
  if (pos < (jlong)0L)
   pos = (jlong)-EINVAL;
 }
  else pos = (jlong)-jcgo_winFileErrnoGet();
#else
 pos = jcgo_lseek((int)fd, ofs, (int)direction > 0 ? SEEK_SET :
        (int)direction < 0 ? SEEK_END : SEEK_CUR, &errcode);
 if (pos < (jlong)0L)
  pos = (jlong)-errcode;
#endif
 JCGO_FILEIOCALL_END(pJniEnv)
 return pos;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_lockingOpHasPos0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_lockingOpHasPos0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_WINFILE
 return 1;
#else
#ifdef JCGO_BIGFLOCK_T
#ifdef JCGO_BIGFLOCK_HASPOS
 return 1;
#else
 return 0;
#endif
#else
 return 1;
#endif
#endif
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_fileLock0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_fileLock0)( JNIEnv *pJniEnv, jclass This,
 jlong pos, jlong len, jint fd, jint sharedOrUnlock, jint doWait )
{
#ifdef JCGO_WINFILE
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(pos);
 JCGO_UNUSED_VAR(len);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(sharedOrUnlock);
 JCGO_UNUSED_VAR(doWait);
 return -EINVAL;
#else
#ifdef _WINBASE_NO_LOCKFILEEX
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(pos);
 JCGO_UNUSED_VAR(len);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(sharedOrUnlock);
 JCGO_UNUSED_VAR(doWait);
 return -EINVAL;
#else
 OVERLAPPED overlapped;
 int res = 0;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 overlapped.Offset = (DWORD)pos;
 overlapped.OffsetHigh = (DWORD)(((LONGLONG)pos) >> (sizeof(DWORD) * 8));
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 if (!((int)sharedOrUnlock >= 0 ? LockFileEx(JCGO_WINF_NUMTOHANDLE(fd),
     ((int)sharedOrUnlock ? 0 : LOCKFILE_EXCLUSIVE_LOCK) | ((int)doWait ? 0 :
     LOCKFILE_FAIL_IMMEDIATELY), 0, (DWORD)len, (DWORD)(((LONGLONG)len) >>
     (sizeof(DWORD) * 8)), &overlapped) :
     UnlockFileEx(JCGO_WINF_NUMTOHANDLE(fd), 0, (DWORD)len,
     (DWORD)(((LONGLONG)len) >> (sizeof(DWORD) * 8)), &overlapped)))
  res = -jcgo_winFileErrnoGet();
 JCGO_FILEIOCALL_END(pJniEnv)
 return (jint)res;
#endif
#endif
#else
#ifdef JCGO_BIGFLOCK_T
 int res;
 JCGO_BIGFLOCK_T fl;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_BIGFLOCK_HASPOS
 if (pos - (jlong)1L >= (jlong)(~((JCGO_BIGFLKOFF_T)1L <<
     ((int)sizeof(JCGO_BIGFLKOFF_T) * 8 - 1))))
  return -EINVAL;
#endif
 if (len >= (jlong)(~((JCGO_BIGFLKOFF_T)1L <<
     ((int)sizeof(JCGO_BIGFLKOFF_T) * 8 - 1))))
  len = (jlong)(~((JCGO_BIGFLKOFF_T)1L <<
         ((int)sizeof(JCGO_BIGFLKOFF_T) * 8 - 1)));
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 res = JCGO_BIGF_LOCK(&fl, (int)fd, (JCGO_BIGFLKOFF_T)pos,
        (JCGO_BIGFLKOFF_T)len, (int)sharedOrUnlock < 0, (int)sharedOrUnlock,
        (int)doWait);
 if (res < 0 && (res = -errno) >= 0)
  res = -EINVAL;
 JCGO_FILEIOCALL_END(pJniEnv)
 return (jint)res;
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(pos);
 JCGO_UNUSED_VAR(len);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(sharedOrUnlock);
 JCGO_UNUSED_VAR(doWait);
 return -EINVAL;
#endif
#endif
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_fileFlush0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_fileFlush0)( JNIEnv *pJniEnv, jclass This,
 jint fd, jint metadata )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(metadata);
 return 0;
#else
 int res;
 int errcode = 0;
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(metadata);
#ifdef JCGO_WINFILE
 res = 0;
 if (!FlushFileBuffers(JCGO_WINF_NUMTOHANDLE(fd)))
 {
  errcode = jcgo_winFileErrnoGet();
  res = -1;
 }
#else
 res = JCGO_FD_FSYNC((int)fd, &errcode);
#endif
 JCGO_FILEIOCALL_END(pJniEnv)
 return (jint)(res >= 0 ? res : -errcode);
#endif
}
#endif

#ifndef NOJAVA_gnu_java_nio_VMChannel_fileClose0
JCGO_JNI_EXPF(jint,
Java_gnu_java_nio_VMChannel_fileClose0)( JNIEnv *pJniEnv, jclass This,
 jint fd )
{
 int res;
 int errcode = 0;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
 res = 0;
 if (!CloseHandle(JCGO_WINF_NUMTOHANDLE(fd)))
 {
  errcode = jcgo_winFileErrnoGet();
  res = -1;
 }
#else
 res = JCGO_FD_CLOSE((int)fd, &errcode);
#endif
 JCGO_FILEIOCALL_END(pJniEnv)
 return (jint)(res < 0 && errcode != EBADF ? -errcode : 0);
}
#endif

#endif /* ! JCGO_EXCLUDE_VMCHANNEL */

#ifndef JCGO_EXCLUDE_VMFILE

#ifndef NOJAVA_java_io_VMFile_length
JCGO_JNI_EXPF(jlong,
Java_java_io_VMFile_length)( JNIEnv *pJniEnv, jclass This, jstring path )
{
 jlong filelen = (jlong)0L;
#ifndef JCGO_NOFILES
#ifdef JCGO_WINFILE
 HANDLE handle;
#ifdef JCGO_SYSWCHAR
 WIN32_FIND_DATAW finddata;
#else
 WIN32_FIND_DATAA finddata;
#endif
#else
 int res;
 JCGO_BIGFSTAT_T st;
#endif
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 JCGO_UNUSED_VAR(This);
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
  handle = JCGO_JNUTCHAR_E(FindFirstFileA(JCGO_JNUTCHAR_C(tbuf),
            JCGO_JNUTCHAR_R(WIN32_FIND_DATAA, &finddata)),
            FindFirstFileW(tbuf, &finddata));
  if (handle != INVALID_HANDLE_VALUE)
  {
   filelen = (jlong)(((LONGLONG)finddata.nFileSizeHigh <<
              (sizeof(DWORD) * 8)) | finddata.nFileSizeLow);
   FindClose(handle);
   if (JCGO_JNUTCHAR_E(GetFileAttributesA(JCGO_JNUTCHAR_C(tbuf)),
       GetFileAttributesW(tbuf)) == INVALID_FILE_ATTRIBUTES)
    filelen = (jlong)0L;
  }
#else
  res = JCGO_BIGF_TSTAT(tbuf, &st);
  if (!res && (st.st_mode & S_IFMT) == S_IFREG && (st.st_size + 1) != 0)
  {
   filelen = (jlong)st.st_size;
   if (sizeof(st.st_size) < sizeof(jlong) && filelen < (jlong)0L)
    filelen = ((jlong)2L << (sizeof(st.st_size) < sizeof(jlong) ?
               (int)sizeof(st.st_size) * 8 - 1 : 0)) + filelen;
  }
#endif
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(path);
#endif
 return filelen;
}
#endif

#ifndef NOJAVA_java_io_VMFile_currentTime0
JCGO_JNI_EXPF(jlong,
Java_java_io_VMFile_currentTime0)( JNIEnv *pJniEnv, jclass This, jint isNano )
{
#ifdef JCGO_NOTIME
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(isNano);
 return (jlong)0L;
#else
#ifdef JCGO_WINFILE
 SYSTEMTIME sysTime;
 FILETIME fileTime;
#else
 JCGO_CURTIME_T curt;
#endif
#ifdef JCGO_WIN32
#ifndef _WINBASE_NO_QUERYPERFORMANCECOUNTER
 LARGE_INTEGER timeCounter;
 if ((int)isNano && jcgo_win32UsePerfCounter &&
     QueryPerformanceCounter(&timeCounter))
  return (jlong)(timeCounter.QuadPart * jcgo_win32PerfTickNanos);
#endif
#endif
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_WINFILE
 fileTime.dwLowDateTime = 0;
 fileTime.dwHighDateTime = 0;
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 GetSystemTime(&sysTime);
 (void)SystemTimeToFileTime(&sysTime, &fileTime);
 JCGO_FILEIOCALL_END(pJniEnv)
 return (int)isNano ? (jlong)(((LONGLONG)fileTime.dwHighDateTime <<
         (sizeof(DWORD) * 8)) | fileTime.dwLowDateTime) * (jlong)100L :
         (jlong)((((LONGLONG)fileTime.dwHighDateTime << (sizeof(DWORD) * 8)) |
         fileTime.dwLowDateTime) / 10000L - JCGO_WINEPOCH_MILLIS);
#else
#ifdef JCGO_CLOCKGETTM
#ifdef _POSIX_MONOTONIC_CLOCK
 if ((int)isNano)
 {
  struct timespec ts;
  if (!clock_gettime(CLOCK_MONOTONIC, &ts))
   return (jlong)ts.tv_sec * ((jlong)1000L * (jlong)1000L * (jlong)1000L) +
           (jlong)ts.tv_nsec;
 }
#endif
#endif
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 JCGO_CURTIME_GET(&curt);
 JCGO_FILEIOCALL_END(pJniEnv)
 if (!(int)isNano)
  return JCGO_CURTIME_ASMILLIS(jlong, &curt);
 return JCGO_CURTIME_ASNANOS(jlong, &curt);
#endif
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_isCaseSensitive0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_isCaseSensitive0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_NOFILES
 return 0;
#else
 return (jint)JCGO_PATH_ISCASESENSITIVE;
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_isLongNameSupported0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_isLongNameSupported0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_NOFILES
 return 0;
#else
 return (jint)JCGO_PATH_HASLONGNAMES;
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_isUniRootFileSys0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_isUniRootFileSys0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_UNIFSYS
 return 1;
#else
#ifdef JCGO_ONEROOTFS
 return 1;
#else
 return 0;
#endif
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_isDotFileHidden0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_isDotFileHidden0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_UNIFSYS
 return 1;
#else
 return 0;
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_getFilePathSepChar0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_getFilePathSepChar0)( JNIEnv *pJniEnv, jclass This,
 jint isPath )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return (jint)((int)isPath ? JCGO_PATH_WDELIM : JCGO_FILE_WSEP);
}
#endif

#ifndef NOJAVA_java_io_VMFile_getLineSeparator0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_getLineSeparator0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, JCGO_NEW_LINE);
}
#endif

#ifndef NOJAVA_java_io_VMFile_getDosUnixDrivePrefix0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_getDosUnixDrivePrefix0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, JCGO_DOSUNIX_DRIVEPREFIX);
}
#endif

#ifndef NOJAVA_java_io_VMFile_getLibNamePrefixSuffix0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_getLibNamePrefixSuffix0)( JNIEnv *pJniEnv, jclass This,
 jint isSuffix )
{
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(isSuffix);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
#else
 return jcgo_JnuNewStringPlatform(pJniEnv, (int)isSuffix ?
         JCGO_PATH_LIBSUFFIX : JCGO_PATH_LIBPREFIX);
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_mapLibraryName0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_mapLibraryName0)( JNIEnv *pJniEnv, jclass This,
 jstring libname )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return libname;
}
#endif

#ifndef NOJAVA_java_io_VMFile_getenv0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_getenv0)( JNIEnv *pJniEnv, jclass This, jstring name )
{
#ifdef _STDLIB_NO_GETENV
 JCGO_UNUSED_VAR(name);
#else
#ifdef _STDLIB_NO_WGETENV
 char *cstr;
 char cbuf[JCGO_GETENV_MAXNAMESIZE];
 if (jcgo_JnuStringToPlatformChars(pJniEnv, name, cbuf, sizeof(cbuf)) <= 0)
  return NULL;
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 cstr = getenv(cbuf);
 JCGO_FILEIOCALL_END(pJniEnv)
 if (cstr != NULL)
  return jcgo_JnuNewStringPlatform(pJniEnv, cstr);
#else
 JCGO_JNUTCHAR_T *tstr;
 JCGO_JNUTCHAR_T tbuf[JCGO_GETENV_MAXNAMESIZE];
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, name, tbuf) <= 0)
  return NULL;
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 tstr = JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(JCGO_JNUTCHAR_T,
         getenv(JCGO_JNUTCHAR_C(tbuf))), _wgetenv(tbuf));
 JCGO_FILEIOCALL_END(pJniEnv)
 if (tstr != NULL)
  return JCGO_JNU_TNEWSTRING(pJniEnv, tstr);
#endif
#endif
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
}
#endif

#ifndef NOJAVA_java_io_VMFile_rename0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_rename0)( JNIEnv *pJniEnv, jclass This, jstring path,
 jstring destpath )
{
 int res = -EACCES;
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
 JCGO_UNUSED_VAR(destpath);
#else
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 JCGO_JNUTCHAR_T tbuf2[JCGO_PATH_MAXSIZE];
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0 &&
     JCGO_JNU_TSTRINGTOCHARS(pJniEnv, destpath, tbuf2) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
  res = JCGO_JNUTCHAR_E(MoveFileA(JCGO_JNUTCHAR_C(tbuf),
         JCGO_JNUTCHAR_C(tbuf2)), MoveFileW(tbuf, tbuf2)) ? 0 :
         -jcgo_winFileErrnoGet();
#else
  res = JCGO_JNUTCHAR_E(rename(JCGO_JNUTCHAR_C(tbuf), JCGO_JNUTCHAR_C(tbuf2)),
         _wrename(tbuf, tbuf2));
  if (res < 0 && (res = -errno) >= 0)
   res = -EACCES;
#endif
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#endif
 JCGO_UNUSED_VAR(This);
 return (jint)res;
}
#endif

#ifndef NOJAVA_java_io_VMFile_delete0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_delete0)( JNIEnv *pJniEnv, jclass This, jstring path )
{
 int res = -EACCES;
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
#else
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
  res = JCGO_JNUTCHAR_E(DeleteFileA(JCGO_JNUTCHAR_C(tbuf)) ||
         RemoveDirectoryA(JCGO_JNUTCHAR_C(tbuf)), DeleteFileW(tbuf) ||
         RemoveDirectoryW(tbuf)) ? 0 : -jcgo_winFileErrnoGet();
#else
  res = JCGO_PATH_TUNLINK(tbuf);
  if (res < 0 && (res = -errno) >= 0)
   res = -EACCES;
#endif
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#endif
 JCGO_UNUSED_VAR(This);
 return (jint)res;
}
#endif

#ifndef NOJAVA_java_io_VMFile_mkdir0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_mkdir0)( JNIEnv *pJniEnv, jclass This, jstring path )
{
 int res = -EACCES;
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
#else
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
  res = JCGO_JNUTCHAR_E(CreateDirectoryA(JCGO_JNUTCHAR_C(tbuf), NULL),
         CreateDirectoryW(tbuf, NULL)) ? 0 : -jcgo_winFileErrnoGet();
#else
  res = JCGO_PATH_TMKDIR(tbuf);
  if (res < 0 && (res = -errno) >= 0)
   res = -EACCES;
#endif
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#endif
 JCGO_UNUSED_VAR(This);
 return (jint)res;
}
#endif

#ifndef NOJAVA_java_io_VMFile_access0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_access0)( JNIEnv *pJniEnv, jclass This, jstring path,
 jint chkRead, jint chkWrite, jint chkExec )
{
 int res = -EACCES;
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
 JCGO_UNUSED_VAR(chkRead);
 JCGO_UNUSED_VAR(chkWrite);
 JCGO_UNUSED_VAR(chkExec);
#else
 int mode;
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
 JCGO_UNUSED_VAR(chkRead);
 JCGO_UNUSED_VAR(chkExec);
  if ((mode = (int)JCGO_JNUTCHAR_E(GetFileAttributesA(JCGO_JNUTCHAR_C(tbuf)),
      GetFileAttributesW(tbuf))) == (int)INVALID_FILE_ATTRIBUTES)
   res = -jcgo_winFileErrnoGet();
   else if (!(int)chkWrite || (mode & FILE_ATTRIBUTE_READONLY) == 0)
    res = 0;
#else
  mode = F_OK;
  if (((int)chkRead | (int)chkWrite | (int)chkExec) != 0)
   mode = ((int)chkRead ? R_OK : 0) | ((int)chkWrite ? W_OK : 0) |
           ((int)chkExec ? X_OK : 0);
  res = JCGO_JNUTCHAR_E(access(JCGO_JNUTCHAR_C(tbuf), mode),
         _waccess(tbuf, mode));
  if (res < 0 && (res = -errno) >= 0)
   res = -EACCES;
#endif
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#endif
 JCGO_UNUSED_VAR(This);
 return (jint)res;
}
#endif

#ifndef NOJAVA_java_io_VMFile_isRegFileOrDir0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_isRegFileOrDir0)( JNIEnv *pJniEnv, jclass This,
 jstring path )
{
 int res = -EACCES;
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_WINFILE
 DWORD attrs;
#else
 struct stat st;
#endif
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
  attrs = JCGO_JNUTCHAR_E(GetFileAttributesA(JCGO_JNUTCHAR_C(tbuf)),
           GetFileAttributesW(tbuf));
  res = attrs != INVALID_FILE_ATTRIBUTES ?
         ((attrs & FILE_ATTRIBUTE_DIRECTORY) != 0 ? 2 : 1) :
         -jcgo_winFileErrnoGet();
#else
  if ((res = JCGO_JNUTCHAR_E(stat(JCGO_JNUTCHAR_C(tbuf), &st),
      _wstat(tbuf, (void *)&st))) >= 0)
   res = (st.st_mode & S_IFMT) == S_IFREG ? 1 :
          (st.st_mode & S_IFMT) == S_IFDIR ? 2 : 0;
   else if ((res = -errno) >= 0)
    res = -EACCES;
#endif
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#endif
 JCGO_UNUSED_VAR(This);
 return (jint)res;
}
#endif

#ifndef NOJAVA_java_io_VMFile_getLastModified0
JCGO_JNI_EXPF(jlong,
Java_java_io_VMFile_getLastModified0)( JNIEnv *pJniEnv, jclass This,
 jstring path )
{
 jlong mtime = (jlong)0L;
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_NOTIME
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_WINFILE
 HANDLE handle;
#ifdef JCGO_SYSWCHAR
 WIN32_FIND_DATAW finddata;
#else
 WIN32_FIND_DATAA finddata;
#endif
#else
 int res;
 struct stat st;
#endif
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
  handle = JCGO_JNUTCHAR_E(FindFirstFileA(JCGO_JNUTCHAR_C(tbuf),
            JCGO_JNUTCHAR_R(WIN32_FIND_DATAA, &finddata)),
            FindFirstFileW(tbuf, &finddata));
  if (handle != INVALID_HANDLE_VALUE)
  {
   mtime = (jlong)(((((LONGLONG)finddata.ftLastWriteTime.dwHighDateTime <<
            (sizeof(DWORD) * 8)) | finddata.ftLastWriteTime.dwLowDateTime) /
            10000L) - JCGO_WINEPOCH_MILLIS);
   FindClose(handle);
   if (JCGO_JNUTCHAR_E(GetFileAttributesA(JCGO_JNUTCHAR_C(tbuf)),
       GetFileAttributesW(tbuf)) == INVALID_FILE_ATTRIBUTES)
    mtime = (jlong)0L;
  }
#else
  res = JCGO_JNUTCHAR_E(stat(JCGO_JNUTCHAR_C(tbuf), &st),
         _wstat(tbuf, (void *)&st));
  if (!res && (st.st_mtime + 1) != 0 &&
      ((st.st_mode & S_IFMT) == S_IFREG || (st.st_mode & S_IFMT) == S_IFDIR))
   mtime = (jlong)(unsigned long)st.st_mtime * (jlong)1000L;
#endif
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#endif
#endif
 JCGO_UNUSED_VAR(This);
 return mtime;
}
#endif

#ifndef NOJAVA_java_io_VMFile_setLastModified0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_setLastModified0)( JNIEnv *pJniEnv, jclass This,
 jlong mtime, jstring path )
{
 int res = -EACCES;
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(mtime);
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_NOTIME
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(mtime);
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_WIN32
 HANDLE handle;
 LONGLONG wintime;
 FILETIME fileTime;
#endif
#ifndef JCGO_WINFILE
#ifndef JCGO_NOUTIMBUF
 jlong msectime;
 struct utimbuf ut;
#endif
 struct stat st;
#endif
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifndef JCGO_WINFILE
  res = JCGO_JNUTCHAR_E(stat(JCGO_JNUTCHAR_C(tbuf), &st),
         _wstat(tbuf, (void *)&st));
  if (res < 0)
  {
   if ((res = -errno) >= 0)
    res = -EACCES;
  }
   else
#endif
   {
#ifdef JCGO_NOUTIMBUF
    JCGO_UNUSED_VAR(mtime);
#else
#ifndef JCGO_WINFILE
    ut.modtime = (time_t)(msectime = mtime / (jlong)1000L);
    ut.actime = st.st_atime;
    if ((jlong)ut.modtime != msectime)
     res = -EINVAL;
     else if ((res = JCGO_JNUTCHAR_E(utime(JCGO_JNUTCHAR_C(tbuf), &ut),
              _wutime(tbuf, (void *)&ut))) < 0)
#endif
#endif
     {
#ifdef JCGO_WINFILE
      JCGO_UNUSED_VAR(mtime);
#else
#ifndef JCGO_NOUTIMBUF
      if ((res = -errno) >= 0)
       res = -EACCES;
#ifdef JCGO_WIN32
       else if (res == -EACCES && (st.st_mode & S_IFMT) == S_IFDIR)
#endif
#endif
#endif
       {
#ifdef JCGO_WIN32
        wintime = ((LONGLONG)mtime + JCGO_WINEPOCH_MILLIS) * 10000L;
        fileTime.dwLowDateTime = (DWORD)wintime;
        fileTime.dwHighDateTime = (DWORD)(wintime >> (sizeof(DWORD) * 8));
#ifdef JCGO_NOUTIMBUF
#ifndef JCGO_WINFILE
        res = -EACCES;
#endif
#endif
        if ((handle = JCGO_JNUTCHAR_E(CreateFileA(JCGO_JNUTCHAR_C(tbuf),
            GENERIC_WRITE, 0, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL |
            JCGO_WINFLAG_FILEBACKUPSEM, NULL), CreateFileW(tbuf,
            GENERIC_WRITE, 0, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL |
            JCGO_WINFLAG_FILEBACKUPSEM, NULL))) != INVALID_HANDLE_VALUE)
        {
         if (SetFileTime(handle, NULL, NULL, &fileTime))
          res = 0;
#ifdef JCGO_WINFILE
          else res = -jcgo_winFileErrnoGet();
#endif
         CloseHandle(handle);
        }
#ifdef JCGO_WINFILE
         else res = -jcgo_winFileErrnoGet();
#endif
#endif
       }
     }
   }
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#endif
#endif
 JCGO_UNUSED_VAR(This);
 return (jint)res;
}
#endif

#ifndef NOJAVA_java_io_VMFile_setReadOnly0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_setReadOnly0)( JNIEnv *pJniEnv, jclass This,
 jstring path )
{
 int res = -EACCES;
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_WINFILE
 DWORD attrs;
#else
 struct stat st;
#endif
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
  res = JCGO_JNUTCHAR_E((attrs = GetFileAttributesA(JCGO_JNUTCHAR_C(tbuf))) !=
         INVALID_FILE_ATTRIBUTES && SetFileAttributesA(JCGO_JNUTCHAR_C(tbuf),
         attrs | FILE_ATTRIBUTE_READONLY),
         (attrs = GetFileAttributesW(tbuf)) != INVALID_FILE_ATTRIBUTES &&
         SetFileAttributesW(tbuf, attrs | FILE_ATTRIBUTE_READONLY)) ? 0 :
         -jcgo_winFileErrnoGet();
#else
  res = JCGO_JNUTCHAR_E(stat(JCGO_JNUTCHAR_C(tbuf), &st),
         _wstat(tbuf, (void *)&st));
  if (res >= 0 && (st.st_mode & (S_IWUSR | S_IWGRP | S_IWOTH)) != 0)
  {
   st.st_mode &= ~(S_IWUSR | S_IWGRP | S_IWOTH);
   res = JCGO_PATH_TCHMOD(tbuf, st.st_mode);
  }
  if (res < 0 && (res = -errno) >= 0)
   res = -EACCES;
#endif
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#endif
 JCGO_UNUSED_VAR(This);
 return (jint)res;
}
#endif

#ifndef NOJAVA_java_io_VMFile_isIOErrorNoEntity0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_isIOErrorNoEntity0)( JNIEnv *pJniEnv, jclass This,
 jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return (int)res == -EACCES || (int)res == -ENOENT || (int)res == -ENOTDIR ?
         1 : 0;
}
#endif

#ifndef NOJAVA_java_io_VMFile_realPath0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_realPath0)( JNIEnv *pJniEnv, jclass This, jstring path,
 jintArray resArr )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_PATH_TREALPATH
 int res;
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 JCGO_JNUTCHAR_T tbuf2[JCGO_REALPATH_BUFLEN];
 res = JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf);
 if (res < 0)
  return NULL;
 if (res > 0)
 {
  tbuf2[0] = (JCGO_JNUTCHAR_T)0;
  res = 0;
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
  if (JCGO_PATH_TREALPATH(tbuf, tbuf2) == NULL && (res = -errno) >= 0)
   res = -EACCES;
  JCGO_FILEIOCALL_END(pJniEnv)
  jcgo_JnuSetIntArrayElement(pJniEnv, resArr, 0, (jint)res);
  return tbuf2[0] && res >= 0 ? JCGO_JNU_TNEWSTRING(pJniEnv, tbuf2) : path;
 }
#else
 JCGO_UNUSED_VAR(path);
#endif
#endif
 JCGO_UNUSED_VAR(This);
 jcgo_JnuSetIntArrayElement(pJniEnv, resArr, 0, 0);
 return path;
}
#endif

#ifndef NOJAVA_java_io_VMFile_getDrivesMask0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_getDrivesMask0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return -1;
}
#endif

#ifndef NOJAVA_java_io_VMFile_getVolumeRoot0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_getVolumeRoot0)( JNIEnv *pJniEnv, jclass This,
 jint index )
{
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(index);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
}
#endif

#ifndef NOJAVA_java_io_VMFile_checkVolumeRoot0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_checkVolumeRoot0)( JNIEnv *pJniEnv, jclass This,
 jstring path )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_PATHCHKVOLROOT_VIASTAT
 int res;
 struct stat st;
 char cbuf[JCGO_PATH_MAXSIZE];
 if (jcgo_JnuStringToPlatformChars(pJniEnv, path, cbuf, sizeof(cbuf)) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
  res = stat(cbuf, &st);
  if (res < 0 && (res = -errno) >= 0)
   res = -ENOENT;
  JCGO_FILEIOCALL_END(pJniEnv)
  if (res == -EACCES || res == -EBUSY)
   return 1;
  if (res < 0)
   return 0;
  if ((st.st_mode & S_IFMT) == S_IFDIR)
   return 1;
 }
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
#endif
#endif
 JCGO_UNUSED_VAR(This);
 return -EACCES;
}
#endif

#ifndef NOJAVA_java_io_VMFile_getDriveCurDir0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_getDriveCurDir0)( JNIEnv *pJniEnv, jclass This,
 jint drive )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(drive);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
#else
#ifdef JCGO_NOCWDIR
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(drive);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
#else
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(drive);
 tbuf[0] = (JCGO_JNUTCHAR_T)0;
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 if (JCGO_PATH_TGETDCWD((int)drive, tbuf,
     sizeof(tbuf) / sizeof(JCGO_JNUTCHAR_T)) == NULL)
  tbuf[0] = (JCGO_JNUTCHAR_T)0;
 JCGO_FILEIOCALL_END(pJniEnv)
 return JCGO_JNU_TNEWSTRING(pJniEnv, tbuf);
#endif
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_getVolumeCurDir0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_getVolumeCurDir0)( JNIEnv *pJniEnv, jclass This,
 jstring path )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_NOCWDIR
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_PATHGETDCWD_VIACHDIR
 int res;
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 JCGO_JNUTCHAR_T tbuf2[JCGO_PATH_MAXSIZE];
 res = JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf);
 if (res < 0)
  return NULL;
 if (res > 0)
 {
  tbuf2[0] = (JCGO_JNUTCHAR_T)0;
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
  if (JCGO_PATH_TGETCWD(tbuf2,
      sizeof(tbuf2) / sizeof(JCGO_JNUTCHAR_T)) != NULL)
  {
   res = JCGO_PATH_TCHDIR(tbuf);
   tbuf[0] = (JCGO_JNUTCHAR_T)0;
   if (!res && JCGO_PATH_TGETCWD(tbuf,
       sizeof(tbuf) / sizeof(JCGO_JNUTCHAR_T)) == NULL)
    tbuf[0] = (JCGO_JNUTCHAR_T)0;
   (void)JCGO_PATH_TCHDIR(tbuf2);
  }
   else tbuf[0] = (JCGO_JNUTCHAR_T)0;
  JCGO_FILEIOCALL_END(pJniEnv)
  return JCGO_JNU_TNEWSTRING(pJniEnv, tbuf);
 }
#else
 JCGO_UNUSED_VAR(path);
#endif
#endif
#endif
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
}
#endif

#ifndef NOJAVA_java_io_VMFile_normPlatformPath0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_normPlatformPath0)( JNIEnv *pJniEnv, jclass This,
 jstring path )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_PATH_PLATFCONV
 int res;
 char cbuf[JCGO_REALPATH_BUFLEN];
 res = jcgo_JnuStringToPlatformChars(pJniEnv, path, cbuf, sizeof(cbuf));
 if (res < 0)
  return NULL;
 if (res > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
  res = JCGO_PATH_PLATFCONV(cbuf);
  JCGO_FILEIOCALL_END(pJniEnv)
  if (res != -1)
   return jcgo_JnuNewStringPlatform(pJniEnv, cbuf);
 }
#else
 JCGO_UNUSED_VAR(path);
#endif
#endif
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
}
#endif

#ifndef NOJAVA_java_io_VMFile_pathListPlatformSep0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_pathListPlatformSep0)( JNIEnv *pJniEnv, jclass This,
 jstring pathlist )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(pathlist);
 return 0;
}
#endif

#ifndef NOJAVA_java_io_VMFile_isHidden0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_isHidden0)( JNIEnv *pJniEnv, jclass This, jstring path )
{
 int res = -1;
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
#else
#ifdef JCGO_WINFILE
 DWORD attrs;
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
  attrs = JCGO_JNUTCHAR_E(GetFileAttributesA(JCGO_JNUTCHAR_C(tbuf)),
           GetFileAttributesW(tbuf));
  res = attrs != INVALID_FILE_ATTRIBUTES &&
         (attrs & FILE_ATTRIBUTE_HIDDEN) != 0 ? 1 : 0;
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(path);
#endif
#endif
 JCGO_UNUSED_VAR(This);
 return (jint)res;
}
#endif

#ifndef NOJAVA_java_io_VMFile_getLnkDevInode0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_getLnkDevInode0)( JNIEnv *pJniEnv, jclass This,
 jstring path, jlongArray devInodeArr )
{
 int res = -EACCES;
#ifndef JCGO_NOFILES
#ifndef JCGO_UNIFSYS
#ifndef JCGO_TFIND_T
 struct stat st;
 char cbuf[JCGO_PATH_MAXSIZE];
 if (jcgo_JnuStringToPlatformChars(pJniEnv, path, cbuf, sizeof(cbuf)) > 0)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
  res = JCGO_PATH_LSTAT(cbuf, &st);
  if (res < 0 && (res = -errno) >= 0)
   res = -EACCES;
  JCGO_FILEIOCALL_END(pJniEnv)
  if (res >= 0)
  {
   jcgo_JnuSetLongArrayElement(pJniEnv, devInodeArr, 0,
    (jlong)(st.st_dev + 1) - (jlong)1L);
   jcgo_JnuSetLongArrayElement(pJniEnv, devInodeArr, 1, (jlong)st.st_ino);
  }
 }
#endif
#endif
#endif
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(path);
 JCGO_UNUSED_VAR(devInodeArr);
 return (jint)res;
}
#endif

#ifndef NOJAVA_java_io_VMFile_dirOpNeedsSync0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_dirOpNeedsSync0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_NOFILES
 return 0;
#else
 return JCGO_TFIND_NEEDSSYNC;
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_dirDataSizeAndIsFind0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_dirDataSizeAndIsFind0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_NOFILES
 return 0;
#else
#ifdef JCGO_TFIND_T
 return (jint)JCGO_ALIGNED_BUFSIZE(JCGO_TFIND_T);
#else
 return -(jint)JCGO_ALIGNED_BUFSIZE(void *);
#endif
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_dirOpenReadFirst0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_dirOpenReadFirst0)( JNIEnv *pJniEnv, jclass This,
 jbyteArray dirdata, jstring path )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(dirdata);
 JCGO_UNUSED_VAR(path);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
#else
 int res;
 jstring str = NULL;
 JCGO_JNUTCHAR_T *tstr = NULL;
#ifdef JCGO_TFIND_T
 jbyte *databuf = jcgo_JnuGetByteArrayElemsRegion(pJniEnv, dirdata, 0,
                   (jint)JCGO_ALIGNED_BUFSIZE(JCGO_TFIND_T));
 JCGO_TFIND_T *pfinddata;
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (databuf != NULL)
 {
  res = JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf);
  if (res >= 0)
  {
   if (res > 0)
   {
    pfinddata = JCGO_ALIGNED_BUFPTR(JCGO_TFIND_T, databuf);
    JCGO_FILEIOCALL_BEGIN(pJniEnv)
    if (!JCGO_TFIND_FIRST(pfinddata, tbuf))
     tstr = JCGO_TFIND_GETNAME(pfinddata);
    JCGO_FILEIOCALL_END(pJniEnv)
   }
   str = tstr != NULL ? JCGO_JNU_TNEWSTRING(pJniEnv, tstr) :
          jcgo_JnuNewStringPlatform(pJniEnv, "");
  }
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, dirdata, databuf, 0);
 }
#else
 void *pent;
 void *dirp;
 jbyte *databuf = jcgo_JnuGetByteArrayElemsRegion(pJniEnv, dirdata, 0,
                   (jint)JCGO_ALIGNED_BUFSIZE(void *));
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (databuf != NULL)
 {
  res = JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf);
  if (res >= 0)
  {
   dirp = NULL;
   if (res > 0)
   {
    JCGO_FILEIOCALL_BEGIN(pJniEnv)
    if ((pent = JCGO_JNUTCHAR_E((dirp = opendir(JCGO_JNUTCHAR_C(tbuf))) !=
        NULL ? (void *)readdir(dirp) : NULL, (dirp = _wopendir(tbuf)) !=
        NULL ? (void *)_wreaddir(dirp) : NULL)) != NULL)
     tstr = JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(JCGO_JNUTCHAR_T,
             ((struct dirent *)pent)->d_name),
             ((struct _wdirent *)pent)->d_name);
    JCGO_FILEIOCALL_END(pJniEnv)
    if (dirp != NULL)
     *JCGO_ALIGNED_BUFPTR(void *, databuf) = dirp;
   }
   str = tstr != NULL ? JCGO_JNU_TNEWSTRING(pJniEnv, tstr) :
          jcgo_JnuNewStringPlatform(pJniEnv, dirp != NULL ? "." : "");
  }
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, dirdata, databuf, 0);
 }
#endif
 JCGO_UNUSED_VAR(This);
 return str;
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_dirReadNext0
JCGO_JNI_EXPF(jstring,
Java_java_io_VMFile_dirReadNext0)( JNIEnv *pJniEnv, jclass This,
 jbyteArray dirdata )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(dirdata);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
#else
 jstring str = NULL;
 JCGO_JNUTCHAR_T *tstr = NULL;
#ifdef JCGO_TFIND_T
 jbyte *databuf = jcgo_JnuGetByteArrayElemsRegion(pJniEnv, dirdata, 0,
                   (jint)JCGO_ALIGNED_BUFSIZE(JCGO_TFIND_T));
 JCGO_TFIND_T *pfinddata;
 if (databuf != NULL)
 {
  pfinddata = JCGO_ALIGNED_BUFPTR(JCGO_TFIND_T, databuf);
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
  if (!JCGO_TFIND_NEXT(pfinddata))
   tstr = JCGO_TFIND_GETNAME(pfinddata);
  JCGO_FILEIOCALL_END(pJniEnv)
  str = tstr != NULL ? JCGO_JNU_TNEWSTRING(pJniEnv, tstr) :
         jcgo_JnuNewStringPlatform(pJniEnv, "");
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, dirdata, databuf, 0);
 }
#else
 void *pent;
 void *dirp;
 jbyte *databuf = jcgo_JnuGetByteArrayElemsRegion(pJniEnv, dirdata, 0,
                   (jint)JCGO_ALIGNED_BUFSIZE(void *));
 if (databuf != NULL)
 {
  if ((dirp = *JCGO_ALIGNED_BUFPTR(void *, databuf)) != NULL)
  {
   JCGO_FILEIOCALL_BEGIN(pJniEnv)
   if ((pent = JCGO_JNUTCHAR_E((void *)readdir(dirp),
       (void *)_wreaddir(dirp))) != NULL)
    tstr = JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(JCGO_JNUTCHAR_T,
            ((struct dirent *)pent)->d_name),
            ((struct _wdirent *)pent)->d_name);
   JCGO_FILEIOCALL_END(pJniEnv)
  }
  str = tstr != NULL ? JCGO_JNU_TNEWSTRING(pJniEnv, tstr) :
         jcgo_JnuNewStringPlatform(pJniEnv, "");
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, dirdata, databuf, 0);
 }
#endif
 JCGO_UNUSED_VAR(This);
 return str;
#endif
}
#endif

#ifndef NOJAVA_java_io_VMFile_dirIsHiddenFound0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_dirIsHiddenFound0)( JNIEnv *pJniEnv, jclass This,
 jbyteArray dirdata )
{
 int ishidden = -1;
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(dirdata);
#else
#ifdef JCGO_TFIND_T
 jbyte *databuf = jcgo_JnuGetByteArrayElemsRegion(pJniEnv, dirdata, 0,
                   (jint)JCGO_ALIGNED_BUFSIZE(JCGO_TFIND_T));
 if (databuf != NULL)
 {
  ishidden = 0;
  if (JCGO_TFIND_ISHIDDEN(JCGO_ALIGNED_BUFPTR(JCGO_TFIND_T, databuf)))
   ishidden = 1;
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, dirdata, databuf, 0);
 }
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(dirdata);
#endif
#endif
 JCGO_UNUSED_VAR(This);
 return ishidden;
}
#endif

#ifndef NOJAVA_java_io_VMFile_dirClose0
JCGO_JNI_EXPF(jint,
Java_java_io_VMFile_dirClose0)( JNIEnv *pJniEnv, jclass This,
 jbyteArray dirdata )
{
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(dirdata);
#else
#ifdef JCGO_TFIND_T
 jbyte *databuf = jcgo_JnuGetByteArrayElemsRegion(pJniEnv, dirdata, 0,
                   (jint)JCGO_ALIGNED_BUFSIZE(JCGO_TFIND_T));
 if (databuf == NULL)
  return 0;
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 (void)JCGO_TFIND_CLOSE(JCGO_ALIGNED_BUFPTR(JCGO_TFIND_T, databuf));
 JCGO_FILEIOCALL_END(pJniEnv)
#else
 void *dirp;
 jbyte *databuf = jcgo_JnuGetByteArrayElemsRegion(pJniEnv, dirdata, 0,
                   (jint)JCGO_ALIGNED_BUFSIZE(void *));
 if (databuf == NULL)
  return 0;
 if ((dirp = *JCGO_ALIGNED_BUFPTR(void *, databuf)) != NULL)
 {
  JCGO_FILEIOCALL_BEGIN(pJniEnv)
  (void)JCGO_JNUTCHAR_E(closedir(dirp), _wclosedir(dirp));
  JCGO_FILEIOCALL_END(pJniEnv)
 }
#endif
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, dirdata, databuf, 0);
#endif
 JCGO_UNUSED_VAR(This);
 return 0;
}
#endif

#endif /* ! JCGO_EXCLUDE_VMFILE */

#ifndef JCGO_EXCLUDE_VMTIMEZONE

#ifndef NOJAVA_java_util_VMTimeZone_getCTimezoneAndDaylight0
JCGO_JNI_EXPF(jint,
Java_java_util_VMTimeZone_getCTimezoneAndDaylight0)( JNIEnv *pJniEnv,
 jclass This )
{
#ifndef JCGO_NOTIME
#ifdef JCGO_WINFILE
 TIME_ZONE_INFORMATION tzInfo;
 if (GetTimeZoneInformation(&tzInfo) != TIME_ZONE_ID_INVALID)
  return (jint)(((tzInfo.Bias * 60L) << 1) | (tzInfo.DaylightBias ? 1 : 0));
#else
 /* return (jint)((_timezone << 1) | (_daylight ? 1 : 0)); */
 struct tm *ptm;
 long tzofs = 0L;
 long tzofs2 = 0L;
 time_t value = (time_t)0x3697ed80L; /* 1999-01-10T00:00:00Z */
 JCGO_FILEIOCALL_BEGIN(pJniEnv)
 if ((ptm = localtime(&value)) != NULL)
 {
  tzofs = (((long)(10 - ptm->tm_mday) * 24L - (long)ptm->tm_hour) * 60L -
           (long)ptm->tm_min) * 60L - (long)ptm->tm_sec;
  value = (time_t)0x37868d00L; /* 1999-07-10T00:00:00Z */
  if ((ptm = localtime(&value)) != NULL)
   tzofs2 = (((long)(10 - ptm->tm_mday) * 24L - (long)ptm->tm_hour) * 60L -
             (long)ptm->tm_min) * 60L - (long)ptm->tm_sec;
 }
 JCGO_FILEIOCALL_END(pJniEnv)
 if (ptm != NULL)
  return (jint)(((tzofs > tzofs2 ? tzofs : tzofs2) << 1) |
          (tzofs != tzofs2 ? 1 : 0));
#endif
#endif
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return 0;
}
#endif

#endif /* ! JCGO_EXCLUDE_VMTIMEZONE */

#endif
