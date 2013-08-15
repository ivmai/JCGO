/*
 * @(#) $(JCGO)/native/jcgoexec.c --
 * a part of the JCGO native layer library (process exec impl).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2013 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_EXEC, JCGO_NOCWDIR, JCGO_NOFILES, JCGO_SYSDUALW,
 * JCGO_SYSWCHAR, JCGO_UNIFSYS, JCGO_UNIPROC, JCGO_UNIX, JCGO_WINFILE.
 * Macros for tuning: STATIC.
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
#include "jcgojnu.h"
#endif

#ifdef JCGO_VER

#ifndef JCGO_EXCLUDE_VMPROCESS

#ifdef JCGO_EXEC

#include "jcgoexec.h"

#ifndef STATIC
#define STATIC static
#endif

#ifdef JCGO_WINFILE

#ifndef JCGO_CREATEPROCESS_FLAGS
#define JCGO_CREATEPROCESS_FLAGS 0
#endif

int jcgo_winFileErrnoGet(void);

STATIC HANDLE jcgo_tspawnWinExec( JCGO_JNUTCHAR_T *tprogpath,
 JCGO_JNUTCHAR_T *tcmdline, JCGO_JNUTCHAR_T *tenvz, JCGO_JNUTCHAR_T *tcurdir,
 HANDLE *pinHandle, HANDLE *poutHandle, HANDLE *perrHandle, int *perrcode,
 int redirect )
{
 PROCESS_INFORMATION pi;
#ifdef _WINBASE_NO_CREATEPIPE
#ifdef _WIN32_WCE
 void *psi = NULL;
#else
#ifdef JCGO_SYSWCHAR
 STARTUPINFOW si = { 0 };
#else
 STARTUPINFOA si = { 0 };
#endif
 void *psi = &si;
 si.cb = sizeof(si);
#endif
 if (!JCGO_JNUTCHAR_E(CreateProcessA(*JCGO_JNUTCHAR_C(tprogpath) ?
     JCGO_JNUTCHAR_C(tprogpath) : NULL, *JCGO_JNUTCHAR_C(tcmdline) ?
     JCGO_JNUTCHAR_C(tcmdline) : NULL, NULL, NULL, FALSE,
     JCGO_CREATEPROCESS_FLAGS, tenvz, *JCGO_JNUTCHAR_C(tcurdir) != '.' ||
     *(JCGO_JNUTCHAR_C(tcurdir) + 1) ? JCGO_JNUTCHAR_C(tcurdir) : NULL,
     psi, &pi), CreateProcessW(*tprogpath ? tprogpath : NULL,
     *tcmdline ? tcmdline : NULL, NULL, NULL, FALSE, tenvz != NULL ?
     JCGO_CREATEPROCESS_FLAGS | CREATE_UNICODE_ENVIRONMENT :
     JCGO_CREATEPROCESS_FLAGS, tenvz, *tcurdir != (wchar_t)0x2e ||
     *(tcurdir + 1) ? tcurdir : NULL, psi, &pi)))
 {
  *perrcode = jcgo_winFileErrnoGet();
  return INVALID_HANDLE_VALUE;
 }
 (void)CloseHandle(pi.hThread);
 *pinHandle = INVALID_HANDLE_VALUE;
 *poutHandle = INVALID_HANDLE_VALUE;
 *perrHandle = INVALID_HANDLE_VALUE;
#else
 HANDLE hInReadPipe = INVALID_HANDLE_VALUE;
 HANDLE hOutWritePipe = INVALID_HANDLE_VALUE;
 HANDLE hErrWritePipe = INVALID_HANDLE_VALUE;
 SECURITY_ATTRIBUTES sa;
#ifdef JCGO_SYSWCHAR
 STARTUPINFOW si = { 0 };
#else
 STARTUPINFOA si = { 0 };
#endif
 sa.nLength = sizeof(SECURITY_ATTRIBUTES);
 sa.lpSecurityDescriptor = NULL;
 sa.bInheritHandle = TRUE;
 *pinHandle = INVALID_HANDLE_VALUE;
 *poutHandle = INVALID_HANDLE_VALUE;
 if (!CreatePipe(&hInReadPipe, pinHandle, &sa, JCGO_SPAWN_PIPEBUFSIZE) ||
     !CreatePipe(poutHandle, &hOutWritePipe, &sa, JCGO_SPAWN_PIPEBUFSIZE) ||
     (!redirect && !CreatePipe(perrHandle, &hErrWritePipe, &sa,
     JCGO_SPAWN_PIPEBUFSIZE)))
 {
  *perrcode = jcgo_winFileErrnoGet();
  (void)CloseHandle(hInReadPipe);
  (void)CloseHandle(*pinHandle);
  if (!redirect)
  {
   (void)CloseHandle(*poutHandle);
   (void)CloseHandle(hOutWritePipe);
  }
  return INVALID_HANDLE_VALUE;
 }
#ifndef _WINBASE_NO_SETHANDLEINFORMATION
 (void)SetHandleInformation(*pinHandle, HANDLE_FLAG_INHERIT, FALSE);
 (void)SetHandleInformation(*poutHandle, HANDLE_FLAG_INHERIT, FALSE);
 if (!redirect)
  (void)SetHandleInformation(*perrHandle, HANDLE_FLAG_INHERIT, FALSE);
#endif
 si.cb = sizeof(si);
 si.dwFlags = STARTF_USESTDHANDLES;
 si.hStdInput = hInReadPipe;
 si.hStdOutput = hOutWritePipe;
 si.hStdError = redirect ? hOutWritePipe : hErrWritePipe;
 if (JCGO_JNUTCHAR_E(CreateProcessA(*JCGO_JNUTCHAR_C(tprogpath) ?
     JCGO_JNUTCHAR_C(tprogpath) : NULL, *JCGO_JNUTCHAR_C(tcmdline) ?
     JCGO_JNUTCHAR_C(tcmdline) : NULL, NULL, NULL, TRUE,
     JCGO_CREATEPROCESS_FLAGS, tenvz, *JCGO_JNUTCHAR_C(tcurdir) != '.' ||
     *(JCGO_JNUTCHAR_C(tcurdir) + 1) ? JCGO_JNUTCHAR_C(tcurdir) : NULL,
     JCGO_JNUTCHAR_R(STARTUPINFOA,  &si), &pi), CreateProcessW(*tprogpath ?
     tprogpath : NULL, *tcmdline ? tcmdline : NULL, NULL, NULL, TRUE,
     tenvz != NULL ? JCGO_CREATEPROCESS_FLAGS | CREATE_UNICODE_ENVIRONMENT :
     JCGO_CREATEPROCESS_FLAGS, tenvz, *tcurdir != (wchar_t)0x2e ||
     *(tcurdir + 1) ? tcurdir : NULL, &si, &pi)))
  (void)CloseHandle(pi.hThread);
  else
  {
   *perrcode = jcgo_winFileErrnoGet();
   pi.hProcess = INVALID_HANDLE_VALUE;
   (void)CloseHandle(*pinHandle);
   (void)CloseHandle(*poutHandle);
   if (!redirect)
    (void)CloseHandle(*perrHandle);
  }
 (void)CloseHandle(hInReadPipe);
 (void)CloseHandle(hOutWritePipe);
 if (!redirect)
  (void)CloseHandle(hErrWritePipe);
#endif
 return pi.hProcess;
}

#else /* JCGO_WINFILE */

#ifdef JCGO_SYSWCHAR
#ifndef JCGO_SYSDUALW
#define JCGO_SPAWN_WCHARONLY
#endif
#endif

#ifndef JCGO_SPAWN_WCHARONLY

STATIC char **jcgo_spawnConvZBlock( JNIEnv *pJniEnv, jstring strZBlock,
 void *buffer, jint *pofs, jint bufLen, int allowEmptyStr )
{
 int i = 0;
 unsigned pos = 0;
 jint ofs1 = *pofs;
 jint ofs2;
 char **carr = NULL;
 char *cstr = (char *)buffer + ofs1;
 if (ofs1 < bufLen && jcgo_JnuStringToPlatformChars(pJniEnv, strZBlock, cstr,
     (unsigned)bufLen - (unsigned)ofs1) >= 0)
 {
  if (*cstr)
  {
   do
   {
    pos++;
   } while (*(cstr + pos) || *(cstr + pos + 1));
   pos++;
  }
  ofs2 = (jint)(pos + sizeof(void *) - ((unsigned)(cstr - (char *)NULL) +
          pos) % sizeof(void *)) + ofs1;
  carr = (char **)((volatile char *)buffer + ofs2);
  if (*cstr)
  {
   pos = 0;
   do
   {
    if (*(cstr + pos) == ' ' && allowEmptyStr)
     pos++;
    *(carr + i) = cstr + pos;
    while (*(cstr + pos))
     pos++;
    i++;
   } while (*(cstr + (++pos)));
  }
  *pofs = (jint)((unsigned)(i + 1) * sizeof(void *)) + ofs2;
 }
 return carr;
}

#endif /* ! JCGO_SPAWN_WCHARONLY */

#ifdef JCGO_SYSWCHAR

STATIC wchar_t **jcgo_wspawnConvZBlock( JNIEnv *pJniEnv, jstring strZBlock,
 void *buffer, jint *pofs, jint bufLen, int allowEmptyStr )
{
 int i = 0;
 unsigned pos = 0;
 jint ofs1 = *pofs;
 jint ofs2;
 wchar_t **warr = NULL;
 wchar_t *wstr = (wchar_t *)((volatile char *)buffer + ofs1);
 if (ofs1 < bufLen && jcgo_JnuStringToWideChars(pJniEnv, strZBlock, wstr,
     (unsigned)bufLen - (unsigned)ofs1) >= 0)
 {
  if (*wstr)
  {
   do
   {
    pos++;
   } while (*(wstr + pos) || *(wstr + pos + 1));
   pos++;
  }
  ofs2 = (jint)(pos * sizeof(wchar_t) + (sizeof(wchar_t) +
          sizeof(void *) - 1) - ((unsigned)((volatile char *)wstr -
          (volatile char *)NULL) + pos * sizeof(wchar_t)) % sizeof(void *)) +
          ofs1;
  warr = (wchar_t **)((volatile char *)buffer + ofs2);
  if (*wstr)
  {
   pos = 0;
   do
   {
    if (*(wstr + pos) == (wchar_t)0x20 && allowEmptyStr)
     pos++;
    *(warr + i) = wstr + pos;
    while (*(wstr + pos))
     pos++;
    i++;
   } while (*(wstr + (++pos)));
  }
  *pofs = (jint)((unsigned)(i + 1) * sizeof(void *)) + ofs2;
 }
 return warr;
}

#endif /* JCGO_SYSWCHAR */

#ifdef JCGO_UNIPROC

STATIC void jcgo_restoreVMSignals( void )
{
#ifdef SIGTERM
 signal(SIGTERM, SIG_DFL);
#endif
#ifdef SIGINT
 signal(SIGINT, SIG_DFL);
#endif
#ifdef SIGHUP
 signal(SIGHUP, SIG_DFL);
#endif
#ifdef SIGSEGV
 signal(SIGSEGV, SIG_DFL);
#endif
#ifdef SIGBUS
 signal(SIGBUS, SIG_DFL);
#endif
}

STATIC void jcgo_closeInheritedFDs( void )
{
 int fd;
 int errcode;
 for (fd = 0; fd < JCGO_SPAWN_OPENMAX; fd++)
  if (fd != JCGO_FD_INFD && fd != JCGO_FD_OUTFD && fd != JCGO_FD_ERRFD)
   (void)JCGO_FD_CLOSE(fd, &errcode);
}

#else /* JCGO_UNIPROC */

#ifndef JCGO_NOCWDIR

#ifndef JCGO_UNIX

#ifndef JCGO_NOFILES

#ifndef JCGO_UNIFSYS

#ifndef JCGO_SPAWN_WCHARONLY

STATIC int jcgo_driveIndexOf( char *path )
{
 int drive;
 char *letters = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";
 char ch = *path;
 for (drive = 0; *(letters + drive) != ch; drive++)
  if (!(*(letters + drive)))
   return 0;
 return (drive >> 1) + 1;
}

#endif /* ! JCGO_SPAWN_WCHARONLY */

#ifdef JCGO_SYSWCHAR

STATIC int jcgo_wdriveIndexOf( wchar_t *wpath )
{
 wchar_t wch = *wpath;
 return wch > (wchar_t)0x40 && wch <= (wchar_t)0x5a ? /*'Z'*/
         (int)wch - 0x40 : /*('A'-1)*/
         wch > (wchar_t)0x60 && wch <= (wchar_t)0x7a ? /*'z'*/
         (int)wch - 0x60 : 0; /*('a'-1)*/
}

#endif /* JCGO_SYSWCHAR */

#endif /* ! JCGO_UNIFSYS */

#endif /* ! JCGO_NOFILES */

#endif /* ! JCGO_UNIX */

#endif /* ! JCGO_NOCWDIR */

STATIC jlong jcgo_tspawnvpeDir( int mode, JCGO_JNUTCHAR_T **targv,
 JCGO_JNUTCHAR_T **tenvp, JCGO_JNUTCHAR_T *tcurdir, int *perrcode )
{
 jlong pid = 0;
#ifndef JCGO_NOFILES
#ifndef JCGO_NOCWDIR
#ifndef JCGO_UNIFSYS
#ifndef JCGO_UNIX
 int drive = 0;
#endif
#endif
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
#endif
#endif
#ifdef JCGO_NOFILES
 if (tcurdir != NULL)
 {
  *perrcode = ENOTDIR;
  pid = (jlong)-1L;
 }
#else
#ifdef JCGO_NOCWDIR
 JCGO_UNUSED_VAR(tcurdir);
 JCGO_UNUSED_VAR(perrcode);
#else
 if (tcurdir != NULL)
 {
  tbuf[0] = (JCGO_JNUTCHAR_T)0;
  if (JCGO_PATH_TGETCWD(tbuf,
      sizeof(tbuf) / sizeof(JCGO_JNUTCHAR_T)) == NULL || !tbuf[0])
  {
   *perrcode = ENOTDIR;
   return (jlong)-1L;
  }
#ifndef JCGO_UNIFSYS
#ifndef JCGO_UNIX
  if (JCGO_JNUTCHAR_E(*JCGO_JNUTCHAR_C(tcurdir) &&
      *(JCGO_JNUTCHAR_C(tcurdir) + 1) == ':' && *JCGO_JNUTCHAR_C(tcurdir) !=
      *JCGO_JNUTCHAR_C(tbuf) && *(JCGO_JNUTCHAR_C(tbuf) + 1) == ':' &&
      (drive = jcgo_driveIndexOf(JCGO_JNUTCHAR_C(tcurdir))) != 0,
      *tcurdir && *(tcurdir + 1) == (wchar_t)0x3a && *tcurdir != tbuf[0] &&
      tbuf[1] == (wchar_t)0x3a && (drive = jcgo_wdriveIndexOf(tcurdir)) != 0))
   drive = JCGO_PATH_SETDRIVE(&drive) ? 0 :
            JCGO_JNUTCHAR_E(jcgo_driveIndexOf(JCGO_JNUTCHAR_C(tbuf)),
            jcgo_wdriveIndexOf(tbuf));
#endif
#endif
  if (JCGO_PATH_TCHDIR(tcurdir))
  {
   *perrcode = ENOTDIR;
   pid = (jlong)-1L;
#ifdef JCGO_UNIFSYS
   tcurdir = NULL;
#endif
  }
 }
#endif
#endif
 if (!pid)
  pid = tenvp != NULL ? JCGO_JNUTCHAR_E(spawnvpe(mode,
         JCGO_JNUTCHAR_C(targv[0]), (void *)targv, (void *)tenvp),
         _wspawnvpe(mode, targv[0], (void *)targv, (void *)tenvp)) :
         JCGO_JNUTCHAR_E(spawnvp(mode, JCGO_JNUTCHAR_C(targv[0]),
         (void *)targv), _wspawnvp(mode, targv[0], (void *)targv));
#ifndef JCGO_NOFILES
#ifndef JCGO_NOCWDIR
 if (tcurdir != NULL)
 {
#ifndef JCGO_UNIFSYS
#ifndef JCGO_UNIX
  if (drive)
   (void)JCGO_PATH_SETDRIVE(&drive);
#endif
#endif
  (void)JCGO_PATH_TCHDIR(tbuf);
 }
#endif
#endif
 return pid;
}

#endif /* ! JCGO_UNIPROC */

STATIC jlong jcgo_tspawnExec( JCGO_JNUTCHAR_T **targv,
 JCGO_JNUTCHAR_T **tenvp, JCGO_JNUTCHAR_T *tcurdir, int *pinfd, int *poutfd,
 int *perrfd, int *perrcode, int redirect )
{
 int inpipe[2];
 int outpipe[2];
 int errpipe[2];
 jlong pid;
 int errcode2 = -1;
#ifdef JCGO_UNIPROC
 JCGO_JNUTCHAR_T *targv0 = targv[0];
 inpipe[0] = -1;
 inpipe[1] = -1;
 outpipe[0] = -1;
 outpipe[1] = -1;
 errpipe[0] = -1;
 errpipe[1] = -1;
 pid = (jlong)errcode2;
 if (!JCGO_SPAWN_PIPE(inpipe, perrcode) &&
     !JCGO_SPAWN_PIPE(outpipe, perrcode) &&
     (redirect || !JCGO_SPAWN_PIPE(errpipe, perrcode)) &&
     (pid = fork()) == (jlong)-1L)
  *perrcode = errno;
 if (pid)
 {
  if (inpipe[0] != -1)
   (void)JCGO_FD_CLOSE(inpipe[0], &errcode2);
  if (outpipe[1] != -1)
   (void)JCGO_FD_CLOSE(outpipe[1], &errcode2);
  if (errpipe[1] != -1)
   (void)JCGO_FD_CLOSE(errpipe[1], &errcode2);
  if (pid != (jlong)-1L)
  {
   *pinfd = inpipe[1];
   *poutfd = outpipe[0];
   *perrfd = errpipe[0];
   return pid;
  }
 }
  else jcgo_restoreVMSignals();
 if (inpipe[1] != -1)
  (void)JCGO_FD_CLOSE(inpipe[1], &errcode2);
 if (outpipe[0] != -1)
  (void)JCGO_FD_CLOSE(outpipe[0], &errcode2);
 if (errpipe[0] != -1)
  (void)JCGO_FD_CLOSE(errpipe[0], &errcode2);
 if (!pid)
 {
  if (inpipe[0] != JCGO_FD_INFD)
  {
   dup2(inpipe[0], JCGO_FD_INFD);
   (void)JCGO_FD_CLOSE(inpipe[0], &errcode2);
  }
  if (outpipe[1] != JCGO_FD_OUTFD)
  {
   dup2(outpipe[1], JCGO_FD_OUTFD);
   (void)JCGO_FD_CLOSE(outpipe[1], &errcode2);
  }
  if (redirect)
   dup2(JCGO_FD_OUTFD, JCGO_FD_ERRFD);
   else
   {
    if (errpipe[1] != JCGO_FD_ERRFD)
    {
     dup2(errpipe[1], JCGO_FD_ERRFD);
     (void)JCGO_FD_CLOSE(errpipe[1], &errcode2);
    }
   }
  jcgo_closeInheritedFDs();
  if (JCGO_JNUTCHAR_E(*JCGO_JNUTCHAR_C(tcurdir) == '.' &&
      !(*(JCGO_JNUTCHAR_C(tcurdir) + 1)),
      *tcurdir == (wchar_t)0x2e && !(*(tcurdir + 1)))
#ifndef JCGO_NOFILES
#ifndef JCGO_NOCWDIR
      || !JCGO_PATH_TCHDIR(tcurdir)
#endif
#endif
      )
  {
   if (tenvp != NULL)
    (void)JCGO_JNUTCHAR_E(execve(JCGO_JNUTCHAR_C(targv0), (void *)targv,
     (void *)tenvp), _wexecve(targv0, (void *)targv, (void *)tenvp));
    else (void)JCGO_JNUTCHAR_E(execvp(JCGO_JNUTCHAR_C(targv0), (void *)targv),
          _wexecvp(targv0, (void *)targv));
  }
  JCGO_JNUTCHAR_E(perror(JCGO_JNUTCHAR_C(targv0)), _wperror(targv0));
  exit(-1);
 }
#else
 int curin;
 int curout;
 int curerr;
 if (!JCGO_SPAWN_PIPE(inpipe, perrcode))
 {
  if (!JCGO_SPAWN_PIPE(outpipe, perrcode))
  {
   errpipe[0] = -1;
   errpipe[1] = -1;
   if (redirect || !JCGO_SPAWN_PIPE(errpipe, perrcode))
   {
    curout = -1;
    curerr = errcode2;
    if ((curin = dup(JCGO_FD_INFD)) != -1 &&
        (curout = dup(JCGO_FD_OUTFD)) != -1 &&
        (curerr = dup(JCGO_FD_ERRFD)) != -1)
    {
     dup2(inpipe[0], JCGO_FD_INFD);
     dup2(outpipe[1], JCGO_FD_OUTFD);
     dup2(redirect ? JCGO_FD_OUTFD : errpipe[1], JCGO_FD_ERRFD);
    }
     else *perrcode = errno;
    (void)JCGO_FD_CLOSE(inpipe[0], &errcode2);
    (void)JCGO_FD_CLOSE(outpipe[1], &errcode2);
    if (!redirect)
     (void)JCGO_FD_CLOSE(errpipe[1], &errcode2);
    if (curin != -1)
    {
     pid = (jlong)-1L;
     if (curout != -1)
     {
      if (curerr != -1)
      {
       pid = jcgo_tspawnvpeDir(P_NOWAIT, targv, tenvp,
              JCGO_JNUTCHAR_E(*JCGO_JNUTCHAR_C(tcurdir) != '.' ||
              *(JCGO_JNUTCHAR_C(tcurdir) + 1), *tcurdir != (wchar_t)0x2e ||
              *(tcurdir + 1)) ? tcurdir : NULL, perrcode);
       dup2(curin, JCGO_FD_INFD);
       dup2(curout, JCGO_FD_OUTFD);
       dup2(curerr, JCGO_FD_ERRFD);
       (void)JCGO_FD_CLOSE(curerr, &errcode2);
      }
      (void)JCGO_FD_CLOSE(curout, &errcode2);
     }
     (void)JCGO_FD_CLOSE(curin, &errcode2);
     if (pid != (jlong)-1L && pid)
     {
      *pinfd = inpipe[1];
      *poutfd = outpipe[0];
      *perrfd = errpipe[0];
      return pid;
     }
    }
    if (!redirect)
     (void)JCGO_FD_CLOSE(errpipe[0], &errcode2);
   }
    else (void)JCGO_FD_CLOSE(outpipe[1], &errcode2);
   (void)JCGO_FD_CLOSE(outpipe[0], &errcode2);
  }
   else (void)JCGO_FD_CLOSE(inpipe[0], &errcode2);
  (void)JCGO_FD_CLOSE(inpipe[1], &errcode2);
 }
#endif
 return (jlong)-1L;
}

#endif /* ! JCGO_WINFILE */

#else /* JCGO_EXEC */

#ifndef JCGO_WINFILE
#ifndef _ERRNO_H
#include <errno.h>
#endif
#endif

#ifndef EACCES
#define EACCES 12345
#endif

#endif /* ! JCGO_EXEC */

#ifndef NOJAVA_java_lang_VMProcess_getValidProgExtsList0
JCGO_JNI_EXPF(jstring,
Java_java_lang_VMProcess_getValidProgExtsList0)( JNIEnv *pJniEnv,
 jclass This )
{
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_EXEC
 return jcgo_JnuNewStringPlatform(pJniEnv, JCGO_SPAWN_PROGEXTS);
#else
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
#endif
}
#endif

#ifndef NOJAVA_java_lang_VMProcess_isProgSearchNeeded0
JCGO_JNI_EXPF(jint,
Java_java_lang_VMProcess_isProgSearchNeeded0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_EXEC
 return JCGO_SPAWN_ISPROGSEARCHNEEDED;
#else
 return 0;
#endif
}
#endif

#ifndef NOJAVA_java_lang_VMProcess_isCmdSpaceDelimited0
JCGO_JNI_EXPF(jint,
Java_java_lang_VMProcess_isCmdSpaceDelimited0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_EXEC
#ifdef JCGO_WINFILE
#ifdef _WIN32_WCE
 return -1;
#else
 return 1;
#endif
#else
 return 0;
#endif
#else
 return 0;
#endif
}
#endif

#ifndef NOJAVA_java_lang_VMProcess_checkPermissions0
JCGO_JNI_EXPF(jint,
Java_java_lang_VMProcess_checkPermissions0)( JNIEnv *pJniEnv, jclass This,
 jstring path, jint isDirCheck )
{
#ifdef JCGO_EXEC
#ifdef JCGO_NOFILES
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(path);
 return (jint)((int)isDirCheck ? -EACCES : 0);
#else
 int res = -EACCES;
#ifdef JCGO_WINFILE
 DWORD attrs;
#else
 struct stat st;
#endif
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 JCGO_UNUSED_VAR(This);
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, path, tbuf) > 0)
 {
  JCGO_SPAWNCALL_BEGIN(pJniEnv)
#ifdef JCGO_WINFILE
  if ((attrs = JCGO_JNUTCHAR_E(GetFileAttributesA(JCGO_JNUTCHAR_C(tbuf)),
      GetFileAttributesW(tbuf))) == INVALID_FILE_ATTRIBUTES)
  {
   res = -jcgo_winFileErrnoGet();
   if ((int)isDirCheck && res == -ENOENT)
    res = -ENOTDIR;
  }
   else
   {
    if ((int)isDirCheck)
    {
     res = 0;
     if ((attrs & FILE_ATTRIBUTE_DIRECTORY) == 0)
      res = -ENOTDIR;
    }
     else if ((attrs & FILE_ATTRIBUTE_DIRECTORY) == 0)
      res = 0;
   }
#else
  res = JCGO_JNUTCHAR_E(stat(JCGO_JNUTCHAR_C(tbuf), &st),
         _wstat(tbuf, (void *)&st));
  if (res < 0)
  {
   if ((res = -errno) >= 0)
    res = -EACCES;
    else if ((int)isDirCheck && res == -ENOENT)
     res = -ENOTDIR;
  }
   else
   {
    if ((int)isDirCheck)
    {
     if ((st.st_mode & S_IFMT) != S_IFDIR)
      res = -ENOTDIR;
#ifdef JCGO_UNIPROC
#ifdef JCGO_UNIX
      else if ((st.st_mode & ((st.st_uid + 1) == 0 || geteuid() == st.st_uid ?
               S_IXUSR : getegid() == st.st_gid ? S_IXGRP : S_IXOTH)) == 0)
       res = -EACCES;
#endif
#endif
    }
     else
     {
#ifdef JCGO_UNIPROC
      if ((st.st_mode & S_IFMT) != S_IFREG || (st.st_mode & (
#ifdef JCGO_UNIX
          (st.st_uid + 1) != 0 ? (geteuid() == st.st_uid ? S_IXUSR :
          getegid() == st.st_gid ? S_IXGRP : S_IXOTH) :
#endif
          S_IXUSR | S_IXGRP | S_IXOTH)) == 0)
       res = -EACCES;
#else
      if ((st.st_mode & S_IFMT) != S_IFREG)
       res = -EACCES;
#endif
     }
   }
#endif
  JCGO_SPAWNCALL_END(pJniEnv)
 }
 return (jint)res;
#endif
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(path);
 return (jint)((int)isDirCheck ? 0 : -EACCES);
#endif
}
#endif

#ifndef NOJAVA_java_lang_VMProcess_getSpawnWorkBufSize0
JCGO_JNI_EXPF(jint,
Java_java_lang_VMProcess_getSpawnWorkBufSize0)( JNIEnv *pJniEnv, jclass This,
 jstring cmdZBlock, jstring envZBlock, jint cmdArrLen, jint envArrLen )
{
 jint size;
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_WINFILE
 JCGO_UNUSED_VAR(cmdArrLen);
 JCGO_UNUSED_VAR(envArrLen);
 size = (jint)JCGO_JNUTCHAR_E(jcgo_JnuStringSizeOfPlatform(pJniEnv,
         cmdZBlock), jcgo_JnuStringSizeOfWide(pJniEnv, cmdZBlock));
 return (size += (jint)JCGO_JNUTCHAR_E(jcgo_JnuStringSizeOfPlatform(pJniEnv,
         envZBlock), jcgo_JnuStringSizeOfWide(pJniEnv, envZBlock))) <
         (jint)(((unsigned)-1) >> 1) ? size : -1;
#else
 return (cmdArrLen | envArrLen) < (jint)((((unsigned)-1) >> 1) /
         sizeof(void *) - 2) && (size = (cmdArrLen + envArrLen) *
         (jint)sizeof(void *) + (jint)(4 * sizeof(void *) - 2)) >= 0 &&
         (size += (jint)JCGO_JNUTCHAR_E(jcgo_JnuStringSizeOfPlatform(pJniEnv,
         cmdZBlock), jcgo_JnuStringSizeOfWide(pJniEnv, cmdZBlock))) >= 0 &&
         (size += (jint)JCGO_JNUTCHAR_E(jcgo_JnuStringSizeOfPlatform(pJniEnv,
         envZBlock), jcgo_JnuStringSizeOfWide(pJniEnv, envZBlock))) <
         (jint)(((unsigned)-1) >> 1) ? size : -1;
#endif
}
#endif

#ifndef NOJAVA_java_lang_VMProcess_nativeSpawn0
JCGO_JNI_EXPF(jint,
Java_java_lang_VMProcess_nativeSpawn0)( JNIEnv *pJniEnv, jclass This,
 jstring progName, jstring cmdZBlock, jstring envZBlock, jstring dirpath,
 jintArray fdsArr, jlongArray pidArr, jbyteArray workBuf, jint bufLen,
 jint redirect )
{
 int errcode = EACCES;
#ifdef JCGO_EXEC
 jint ofs;
 void *buffer;
#ifdef JCGO_WINFILE
 HANDLE inHandle = 0;
 HANDLE outHandle = 0;
 HANDLE errHandle = 0;
 HANDLE handle;
#ifdef JCGO_NOFILES
 JCGO_JNUTCHAR_T tnamebuf[512];
#else
 JCGO_JNUTCHAR_T tnamebuf[JCGO_PATH_MAXSIZE];
#endif
#else
 int infd = 0;
 int outfd = 0;
 int errfd = 0;
 jlong pid;
 JCGO_JNUTCHAR_T **targv;
 JCGO_JNUTCHAR_T **tenvp;
#endif
#ifdef JCGO_NOFILES
 JCGO_JNUTCHAR_T tbuf[16];
#else
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
#endif
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_WINFILE
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, progName, tnamebuf) <= 0)
  return -EACCES;
#else
 JCGO_UNUSED_VAR(progName);
#endif
 if (JCGO_JNU_TSTRINGTOCHARS(pJniEnv, dirpath, tbuf) <= 0)
  return -ENOTDIR;
 if ((buffer = jcgo_JnuGetByteArrayElemsRegion(pJniEnv, workBuf, 0,
     bufLen)) != NULL)
 {
#ifdef JCGO_WINFILE
  ofs = (jint)JCGO_JNUTCHAR_E(jcgo_JnuStringSizeOfPlatform(pJniEnv,
         cmdZBlock), jcgo_JnuStringSizeOfWide(pJniEnv, cmdZBlock));
  handle = INVALID_HANDLE_VALUE;
  if (JCGO_JNUTCHAR_E(jcgo_JnuStringToPlatformChars(pJniEnv, cmdZBlock,
      buffer, (unsigned)bufLen), jcgo_JnuStringToWideChars(pJniEnv, cmdZBlock,
      buffer, (unsigned)bufLen)) >= 0 &&
      JCGO_JNUTCHAR_E(jcgo_JnuStringToPlatformChars(pJniEnv, envZBlock,
      (void *)((volatile char *)buffer + ofs), (unsigned)(bufLen - ofs)),
      jcgo_JnuStringToWideChars(pJniEnv, envZBlock,
      (JCGO_JNUTCHAR_T *)((volatile char *)buffer + ofs),
      (unsigned)(bufLen - ofs))) >= 0)
  {
   JCGO_SPAWNCALL_BEGIN(pJniEnv)
   handle = jcgo_tspawnWinExec(tnamebuf, buffer,
             JCGO_JNUTCHAR_E((JCGO_JNUTCHAR_T)(*((char *)buffer + ofs)),
             *(JCGO_JNUTCHAR_T *)((volatile char *)buffer + ofs)) ?
             (JCGO_JNUTCHAR_T *)((volatile char *)buffer + ofs) : NULL, tbuf,
             &inHandle, &outHandle, &errHandle, &errcode, (int)redirect);
   JCGO_SPAWNCALL_END(pJniEnv)
  }
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, workBuf, buffer, 0);
  if (handle != INVALID_HANDLE_VALUE)
  {
   jcgo_JnuSetIntArrayElement(pJniEnv, fdsArr, 0,
    (jint)JCGO_WINF_HANDLETONUM(inHandle));
   jcgo_JnuSetIntArrayElement(pJniEnv, fdsArr, 1,
    (jint)JCGO_WINF_HANDLETONUM(outHandle));
   jcgo_JnuSetIntArrayElement(pJniEnv, fdsArr, 2,
    (jint)JCGO_WINF_HANDLETONUM(errHandle));
   jcgo_JnuSetLongArrayElement(pJniEnv, pidArr, 0,
    (jlong)JCGO_WINF_HANDLETONUM(handle));
   return 0;
  }
#else
  ofs = 0;
  pid = (jlong)-1L;
  if ((targv = JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(JCGO_JNUTCHAR_T *,
      jcgo_spawnConvZBlock(pJniEnv, cmdZBlock, buffer, &ofs, bufLen, 1)),
      jcgo_wspawnConvZBlock(pJniEnv, cmdZBlock, buffer, &ofs,
      bufLen, 1))) != NULL && targv[0] != NULL &&
      (tenvp = JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(JCGO_JNUTCHAR_T *,
      jcgo_spawnConvZBlock(pJniEnv, envZBlock, buffer, &ofs, bufLen, 0)),
      jcgo_wspawnConvZBlock(pJniEnv, envZBlock, buffer, &ofs,
      bufLen, 0))) != NULL)
  {
   JCGO_SPAWNCALL_BEGIN(pJniEnv)
   pid = jcgo_tspawnExec(targv, *tenvp != NULL ? tenvp : NULL, tbuf, &infd,
          &outfd, &errfd, &errcode, (int)redirect);
   JCGO_SPAWNCALL_END(pJniEnv)
  }
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, workBuf, buffer, 0);
  if (pid != (jlong)-1L)
  {
   jcgo_JnuSetIntArrayElement(pJniEnv, fdsArr, 0, (jint)infd);
   jcgo_JnuSetIntArrayElement(pJniEnv, fdsArr, 1, (jint)outfd);
   jcgo_JnuSetIntArrayElement(pJniEnv, fdsArr, 2, (jint)errfd);
   jcgo_JnuSetLongArrayElement(pJniEnv, pidArr, 0, pid);
   return 0;
  }
#endif
 }
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(progName);
 JCGO_UNUSED_VAR(cmdZBlock);
 JCGO_UNUSED_VAR(envZBlock);
 JCGO_UNUSED_VAR(dirpath);
 JCGO_UNUSED_VAR(fdsArr);
 JCGO_UNUSED_VAR(pidArr);
 JCGO_UNUSED_VAR(workBuf);
 JCGO_UNUSED_VAR(bufLen);
 JCGO_UNUSED_VAR(redirect);
#endif
 return -errcode;
}
#endif

#ifndef NOJAVA_java_lang_VMProcess_nativeWaitFor0
JCGO_JNI_EXPF(jint,
Java_java_lang_VMProcess_nativeWaitFor0)( JNIEnv *pJniEnv, jclass This,
 jlong pid, jint nohang )
{
#ifdef JCGO_EXEC
#ifdef JCGO_WINFILE
 HANDLE handle = JCGO_WINF_NUMTOHANDLE(pid);
 DWORD exitcode;
 int status;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_SPAWNCALL_BEGIN(pJniEnv)
 if (!(int)nohang)
  (void)WaitForSingleObject(handle, INFINITE);
 if (GetExitCodeProcess(handle, &exitcode))
 {
  if (exitcode != STILL_ACTIVE)
  (void)CloseHandle(handle);
 }
  else exitcode = 0;
 JCGO_SPAWNCALL_END(pJniEnv)
 if (exitcode == STILL_ACTIVE)
  return -1;
 status = (int)exitcode;
#else
 int res;
 int errcode;
 int status = 0;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_SPAWNCALL_BEGIN(pJniEnv)
 res = JCGO_SPAWN_WAITPID(pid, &status, (int)nohang, &errcode);
 JCGO_SPAWNCALL_END(pJniEnv)
 if (!res)
  return -1;
 if (res == -1)
  return (jint)(errcode == EINTR ? -1 : 0);
 status = JCGO_SPAWN_EXITCODE(status);
#endif
 if (status < 0)
  status = -status;
 return (jint)status;
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(pid);
 JCGO_UNUSED_VAR(nohang);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_java_lang_VMProcess_nativeKill
JCGO_JNI_EXPF(jint,
Java_java_lang_VMProcess_nativeKill)( JNIEnv *pJniEnv, jclass This,
 jlong pid )
{
#ifdef JCGO_EXEC
 int res;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_SPAWNCALL_BEGIN(pJniEnv)
 res = JCGO_SPAWN_KILL(pid);
 JCGO_SPAWNCALL_END(pJniEnv)
 return (jint)res;
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(pid);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_java_lang_VMProcess_pipe0
JCGO_JNI_EXPF(jint,
Java_java_lang_VMProcess_pipe0)( JNIEnv *pJniEnv, jclass This,
 jintArray fdsArr )
{
#ifdef JCGO_EXEC
 int res;
#ifdef JCGO_WINFILE
#ifdef _WINBASE_NO_CREATEPIPE
 JCGO_UNUSED_VAR(fdsArr);
 res = -EACCES;
#else
 HANDLE hReadPipe;
 HANDLE hWritePipe;
 SECURITY_ATTRIBUTES sa;
 sa.nLength = sizeof(SECURITY_ATTRIBUTES);
 sa.lpSecurityDescriptor = NULL;
 sa.bInheritHandle = FALSE;
 res = 0;
 hReadPipe = INVALID_HANDLE_VALUE;
 hWritePipe = INVALID_HANDLE_VALUE;
 JCGO_SPAWNCALL_BEGIN(pJniEnv)
 if (!CreatePipe(&hReadPipe, &hWritePipe, &sa, JCGO_SPAWN_PIPEBUFSIZE))
  res = -jcgo_winFileErrnoGet();
 JCGO_SPAWNCALL_END(pJniEnv)
 if (res >= 0)
 {
  jcgo_JnuSetIntArrayElement(pJniEnv, fdsArr, 0,
   (jint)JCGO_WINF_HANDLETONUM(hReadPipe));
  jcgo_JnuSetIntArrayElement(pJniEnv, fdsArr, 1,
   (jint)JCGO_WINF_HANDLETONUM(hWritePipe));
 }
#endif
#else
 int errcode = 0;
 int fds[2];
 fds[0] = -1;
 fds[1] = -1;
 JCGO_SPAWNCALL_BEGIN(pJniEnv)
 res = JCGO_SPAWN_PIPE(fds, &errcode);
 (void)JCGO_FD_SETCLOEXEC(fds[0]);
 (void)JCGO_FD_SETCLOEXEC(fds[1]);
 JCGO_SPAWNCALL_END(pJniEnv)
 if (res >= 0)
 {
  jcgo_JnuSetIntArrayElement(pJniEnv, fdsArr, 0, (jint)fds[0]);
  jcgo_JnuSetIntArrayElement(pJniEnv, fdsArr, 1, (jint)fds[1]);
 }
  else res = -errcode;
#endif
 JCGO_UNUSED_VAR(This);
 return (jint)res;
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fdsArr);
 return -EACCES;
#endif
}
#endif

#endif /* ! JCGO_EXCLUDE_VMPROCESS */

#endif
