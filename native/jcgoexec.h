/*
 * @(#) $(JCGO)/native/jcgoexec.h --
 * a part of the JCGO native layer library (process exec defs).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_FFBLK, JCGO_NOCWDIR, JCGO_NOFILES, JCGO_OS2,
 * JCGO_SYSWCHAR, JCGO_UNIFSYS, JCGO_UNIPROC, JCGO_UNIX, JCGO_WIN32,
 * JCGO_WINFILE.
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

/* #include <stdlib.h> */

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef JCGO_WINFILE

#ifndef WIN32
#define WIN32
#endif

#ifndef _WINDOWS_H
#include <windows.h>
/* BOOL CloseHandle(HANDLE); */
/* BOOL CreatePipe(HANDLE *, HANDLE *, SECURITY_ATTRIBUTES *, DWORD); */
/* BOOL CreateProcessA(LPCSTR, LPSTR, SECURITY_ATTRIBUTES *, SECURITY_ATTRIBUTES *, BOOL, DWORD, void *, LPCSTR, STARTUPINFOA *, PROCESS_INFORMATION *); */
/* BOOL GetExitCodeProcess(HANDLE, DWORD *); */
/* DWORD GetFileAttributesA(LPCSTR); */
/* BOOL SetHandleInformation(HANDLE, DWORD, DWORD); */
/* DWORD WaitForSingleObject(HANDLE, DWORD); */
#endif

#ifdef JCGO_SYSWCHAR
/* #include <windows.h> */
/* BOOL CreateProcessW(LPCWSTR, LPWSTR, SECURITY_ATTRIBUTES *, SECURITY_ATTRIBUTES *, BOOL, DWORD, void *, LPCWSTR, STARTUPINFOW *, PROCESS_INFORMATION *); */
/* DWORD GetFileAttributesW(LPCWSTR); */
#endif

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifndef _LIMITS_H
#include <limits.h>
#endif

#ifndef INVALID_FILE_ATTRIBUTES
#define INVALID_FILE_ATTRIBUTES ((DWORD)-1L)
#endif

#ifndef CREATE_UNICODE_ENVIRONMENT
#define CREATE_UNICODE_ENVIRONMENT 0x400
#endif

#ifndef JCGO_WINF_HANDLETONUM
#define JCGO_WINF_NUMTOHANDLE(value) ((HANDLE)((ptrdiff_t)(value)))
#define JCGO_WINF_HANDLETONUM(handle) ((ptrdiff_t)(handle))
#endif

#else /* JCGO_WINFILE */

#ifndef _ERRNO_H
#include <errno.h>
/* int errno; */
#endif

#ifndef _FCNTL_H
#include <fcntl.h>
#endif

#ifndef _LIMITS_H
#include <limits.h>
#endif

#ifdef JCGO_UNIX

#ifndef _UNISTD_H
#include <unistd.h>
/* int close(int); */
/* int dup2(int, int); */
/* int pipe(int *); */
#endif

#ifndef _SIGNAL_H
#include <signal.h>
/* int kill(pid_t, int); */
#endif

#ifndef _SYS_WAIT_H
#include <sys/wait.h>
/* pid_t waitpid(pid_t, int *, int); */
#endif

#ifndef JCGO_UNIFSYS

#ifndef _IO_H
#include <io.h>
/* int setmode(int, int); */
#endif

/* #include <unistd.h> */
/* int dup(int); */

#endif /* ! JCGO_UNIFSYS */

#ifdef JCGO_UNIPROC

/* #include <unistd.h> */
/* int execve(const char *, char *const *, char *const *); */
/* int execvp(const char *, char *const *); */
/* pid_t fork(void); */

#ifdef JCGO_SYSWCHAR
/* #include <unistd.h> */
/* int _wexecve(const wchar_t *, wchar_t *const *, wchar_t *const *); */
/* int _wexecvp(const wchar_t *, wchar_t *const *); */
#endif

#ifndef JCGO_NOFILES
/* #include <unistd.h> */
/* uid_t geteuid(void); */
/* gid_t getegid(void); */
#endif

#else /* JCGO_UNIPROC */

#ifndef _PROCESS_H
#include <process.h>
/* int spawnvp(int, const char *, char *const *); */
/* int spawnvpe(int, const char *, char *const *, char *const *); */
#endif

#ifdef JCGO_SYSWCHAR
/* #include <process.h> */
/* int _wspawnvp(int, const wchar_t *, wchar_t *const *); */
/* int _wspawnvpe(int, const wchar_t *, wchar_t *const *, wchar_t *const *); */
#endif

#endif /* ! JCGO_UNIPROC */

#else /* JCGO_UNIX */

#ifndef _IO_H
#include <io.h>
/* int _pipe(int *, unsigned, int); */
/* int close(int); */
/* int dup2(int, int); */
#endif

/* #include <fcntl.h> */
/* int _pipe(int *, unsigned, int); */

#ifndef _PROCESS_H
#include <process.h>
/* intptr_t _cwait(int *, intptr_t, int); */
/* int _cwait(int *, int, int); */
#endif

#ifndef JCGO_NOFILES
#ifndef JCGO_NOCWDIR
#ifndef _DIRECT_H
#include <direct.h>
#endif
#ifdef JCGO_FFBLK
#ifndef __EMX__
#ifndef _DIR_H
#include <dir.h>
#endif
#endif
#endif
#endif
#endif

#endif /* ! JCGO_UNIX */

#ifdef JCGO_UNIPROC

/* #include <stdlib.h> */
/* void exit(int); */

#ifndef _STDIO_H
#include <stdio.h>
/* void perror(const char *); */
#endif

#ifdef JCGO_SYSWCHAR
/* #include <stdio.h> */
/* void _wperror(const wchar_t *); */
#endif

#ifndef _SIGNAL_H
#include <signal.h>
/* void (*signal(int, void (*)(int)))(int); */
#endif

#ifndef JCGO_SPAWN_OPENMAX
#ifdef FD_CLOEXEC
#define JCGO_SPAWN_OPENMAX 8
#else
#ifdef OPEN_MAX
#define JCGO_SPAWN_OPENMAX (OPEN_MAX < 256 ? OPEN_MAX : 256)
#else
#define JCGO_SPAWN_OPENMAX 32
#endif
#endif
#endif

#else /* JCGO_UNIPROC */

#ifndef P_NOWAIT
#ifdef _P_NOWAIT
#define P_NOWAIT _P_NOWAIT
#endif
#endif

#endif /* ! JCGO_UNIPROC */

#endif /* ! JCGO_WINFILE */

#ifndef EACCES
#define EACCES 12345
#endif
#ifndef EINTR
#define EINTR 12350
#endif
#ifndef EINVAL
#define EINVAL 12351
#endif
#ifndef ENOENT
#define ENOENT 12354
#endif
#ifndef ENOTDIR
#define ENOTDIR 12358
#endif

#define JCGO_SPAWNCALL_BEGIN(pJniEnv) {
#define JCGO_SPAWNCALL_END(pJniEnv) }

#ifndef JCGO_SPAWN_PROGEXTS
#ifdef JCGO_NOFILES
#define JCGO_SPAWN_PROGEXTS ""
#else
#ifdef JCGO_UNIPROC
#define JCGO_SPAWN_PROGEXTS ""
#else
#ifdef JCGO_WINFILE
#ifdef _WIN32_WCE
#define JCGO_SPAWN_PROGEXTS ".EXE"
#else
#define JCGO_SPAWN_PROGEXTS ".COM;.EXE"
#endif
#else
#define JCGO_SPAWN_PROGEXTS ".COM;.EXE;.BAT;.CMD"
#endif
#endif
#endif
#endif

#ifndef JCGO_SPAWN_ISPROGSEARCHNEEDED
#ifdef JCGO_NOFILES
#define JCGO_SPAWN_ISPROGSEARCHNEEDED 0
#else
#ifdef _STDLIB_NO_GETENV
#define JCGO_SPAWN_ISPROGSEARCHNEEDED 0
#else
#define JCGO_SPAWN_ISPROGSEARCHNEEDED 1
#endif
#endif
#endif

#ifndef JCGO_SPAWN_SIGEXITCODE
#define JCGO_SPAWN_SIGEXITCODE 128
#define JCGO_SPAWN_TERMCODE (JCGO_SPAWN_SIGEXITCODE + 9 /* SIGKILL */)
#endif

#ifndef JCGO_SPAWN_PIPEBUFSIZE
#ifdef PIPE_BUF
#define JCGO_SPAWN_PIPEBUFSIZE PIPE_BUF
#else
#define JCGO_SPAWN_PIPEBUFSIZE 0x200
#endif
#endif

#ifndef JCGO_WINFILE

#ifdef JCGO_UNIX

#ifndef O_BINARY
#ifdef O_RAW
#define O_BINARY O_RAW
#else
#define O_BINARY 0
#endif
#endif

#ifdef JCGO_UNIFSYS
#define JCGO_SPAWN_PIPE(fds, perrcode) (pipe(fds) != -1 ? 0 : (*(perrcode) = errno, -1))
#else
#define JCGO_SPAWN_PIPE(fds, perrcode) (pipe(fds) != -1 ? (setmode(fds[0], O_BINARY), setmode(fds[1], O_BINARY), 0) : (*(perrcode) = errno, -1))
#endif

#define JCGO_SPAWN_WAITPID(pid, pstatus, nohang, perrcode) ((*(perrcode) = waitpid((pid_t)(pid), pstatus, nohang ? WNOHANG : 0)) != -1 ? *(perrcode) : (*(perrcode) = errno, -1))
#define JCGO_SPAWN_EXITCODE(status) (int)(WIFEXITED(status) ? WEXITSTATUS(status) : WTERMSIG(status) + JCGO_SPAWN_SIGEXITCODE)

#ifdef SIGINT
#ifdef SIGKILL
#define JCGO_SPAWN_KILL(pid) (kill((pid_t)(pid), SIGKILL) != -1 || kill((pid_t)(pid), SIGINT) != -1 ? 0 : errno == EINVAL ? JCGO_SPAWN_TERMCODE : -1)
#else
#define JCGO_SPAWN_KILL(pid) (kill((pid_t)(pid), SIGINT) != -1 ? 0 : errno == EINVAL ? JCGO_SPAWN_TERMCODE : -1)
#endif
#endif

#else /* JCGO_UNIX */

#ifndef _WAIT_CHILD
#ifdef WAIT_CHILD
#define _WAIT_CHILD WAIT_CHILD
#ifndef _cwait
#define _cwait cwait
#endif
#else
#define _WAIT_CHILD 0
#endif
#endif

#ifndef JCGO_SPAWN_PIPE
#define JCGO_SPAWN_PIPE(fds, perrcode) (_pipe(fds, JCGO_SPAWN_PIPEBUFSIZE, O_BINARY) != -1 ? 0 : (*(perrcode) = errno, -1))
#endif

#ifndef JCGO_SPAWNPID_T
#ifdef _EXECXX_RETURN_TYPE
#define JCGO_SPAWNPID_T _EXECXX_RETURN_TYPE
#else
#ifdef _WIN64
#define JCGO_SPAWNPID_T intptr_t
#else
#define JCGO_SPAWNPID_T int
#endif
#endif
#endif

#ifndef JCGO_SPAWN_WAITPID
#define JCGO_SPAWN_WAITPID(pid, pstatus, nohang, perrcode) ((*(perrcode) = (int)_cwait(pstatus, (JCGO_SPAWNPID_T)(pid), _WAIT_CHILD)) != -1 ? *(perrcode) : (*(perrcode) = errno) == EINTR && *(pstatus) != 0 ? (*(pstatus) = JCGO_SPAWN_TERMCODE, (int)(pid)) : -1)
#endif

#ifndef JCGO_SPAWN_EXITCODE
#define JCGO_SPAWN_EXITCODE(status) ((unsigned)(status) > (unsigned)0xff ? (int)(((unsigned)(status)) >> 8) : (int)(status))
#endif

#ifndef JCGO_WIN32

#ifdef JCGO_OS2

#ifndef _OS2_H
#define INCL_DOSEXCEPTIONS
#include <os2.h>
/* APIRET DosSendSignalException(PID, ULONG); */
#undef INCL_DOSEXCEPTIONS
#endif

#ifndef JCGO_SPAWN_KILL
#ifdef XCPT_SIGNAL_BREAK
#define JCGO_SPAWN_KILL(pid) (DosSendSignalException((PID)(pid), XCPT_SIGNAL_BREAK) ? JCGO_SPAWN_TERMCODE : 0)
#endif
#endif

#endif /* JCGO_OS2 */

#endif /* ! JCGO_WIN32 */

#ifdef JCGO_UNIPROC

/* #include <process.h> */
/* int execve(const char *, const char *const *, const char *const *); */
/* int execvp(const char *, const char *const *); */
/* pid_t fork(void); */

#ifdef JCGO_SYSWCHAR
/* #include <process.h> */
/* int _wexecve(const wchar_t *, const wchar_t *const *, const wchar_t *const *); */
/* int _wexecvp(const wchar_t *, const wchar_t *const *); */
#endif

#else /* JCGO_UNIPROC */

/* #include <io.h> */
/* int dup(int); */

/* #include <process.h> */
/* intptr_t spawnvp(int, const char *, const char *const *); */
/* intptr_t spawnvpe(int, const char *, const char *const *, const char *const *); */

#ifdef JCGO_SYSWCHAR
/* #include <process.h> */
/* intptr_t _wspawnvp(int, const wchar_t *, const wchar_t *const *); */
/* intptr_t _wspawnvpe(int, const wchar_t *, const wchar_t *const *, const wchar_t *const *); */
#endif

#ifndef JCGO_NOFILES
#ifndef JCGO_UNIFSYS
#ifdef __WATCOMC__
#ifndef _DOS_H
#include <dos.h>
/* void _dos_setdrive(unsigned, unsigned *); */
#endif
#define JCGO_PATH_SETDRIVE(pdrive) (_dos_setdrive((unsigned)(*(pdrive)), (unsigned *)(pdrive)), (int)(*(pdrive)) != -1 ? 0 : -1)
#else
/* #include <direct.h> */
/* int _chdrive(int); */
#define JCGO_PATH_SETDRIVE(pdrive) ((*(pdrive) = _chdrive(*(pdrive))), (int)(*(pdrive)))
#endif
#endif
#endif

#endif /* ! JCGO_UNIPROC */

#endif /* ! JCGO_UNIX */

#ifndef JCGO_FD_INFD
#define JCGO_FD_INFD 0
#define JCGO_FD_OUTFD 1
#define JCGO_FD_ERRFD 2
#endif

#ifndef JCGO_FD_CLOSE
#define JCGO_FD_CLOSE(fd, perrcode) (close(fd) != -1 ? 0 : (*(perrcode) = errno, -1))
#endif

#ifndef JCGO_FD_SETCLOEXEC
#ifdef FD_CLOEXEC
#ifdef F_GETFD
#ifdef F_SETFD
/* #include <fcntl.h> */
/* int fcntl(int, int, ...); */
#define JCGO_FD_SETCLOEXEC(fd) ((fd) != -1 ? fcntl(fd, F_SETFD, fcntl(fd, F_GETFD, 0) | FD_CLOEXEC) : -1)
#endif
#endif
#endif
#ifndef JCGO_FD_SETCLOEXEC
#define JCGO_FD_SETCLOEXEC(fd) 0
#endif
#endif

#endif /* ! JCGO_WINFILE */

#ifndef JCGO_UNIX

#ifdef JCGO_WIN32

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifndef WIN32
#define WIN32
#endif

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 1
#endif

#ifndef _WINDOWS_H
#include <windows.h>
#endif

#ifndef JCGO_SPAWN_KILL
#ifndef _WINBASE_NO_TERMINATEPROCESS
/* #include <windows.h> */
/* BOOL TerminateProcess(HANDLE, unsigned); */
#define JCGO_SPAWN_KILL(pid) (TerminateProcess((HANDLE)((ptrdiff_t)(pid)), JCGO_SPAWN_TERMCODE) ? 0 : JCGO_SPAWN_TERMCODE)
#endif
#endif

#endif /* JCGO_WIN32 */

#endif /* ! JCGO_UNIX */

#ifndef JCGO_SPAWN_KILL
#define JCGO_SPAWN_KILL(pid) ((pid) ? JCGO_SPAWN_TERMCODE : -1)
#endif

#ifndef JCGO_NOFILES

#ifndef JCGO_WINFILE

#ifndef _SYS_STAT_H
#include <sys/stat.h>
/* int stat(const char *, struct stat *); */
#endif

#ifndef S_IFMT
#define S_IFMT 0xf000
#define S_IFREG 0x8000
#define S_IFDIR 0x4000
#endif

#ifndef S_IRUSR
#define S_IRUSR 0x100
#define S_IWUSR 0x80
#define S_IXUSR 0x40
#endif

#ifndef S_IRGRP
#define S_IRGRP 0x20
#define S_IWGRP 0x10
#define S_IXGRP 0x8
#endif

#ifndef S_IROTH
#define S_IROTH 0x4
#define S_IWOTH 0x2
#define S_IXOTH 0x1
#endif

#ifdef JCGO_SYSWCHAR
/* #include <sys/stat.h> */
/* int _wstat(const wchar_t *, struct _stat *); */
/* int _wstat(const wchar_t *, struct stat *); */
#endif

#ifndef JCGO_NOCWDIR

#ifndef JCGO_PATH_TGETCWD

#ifdef JCGO_UNIFSYS

/* #include <unistd.h> */
/* int chdir(const char *); */

#ifdef JCGO_SYSWCHAR
/* #include <unistd.h> */
/* int _wchdir(const wchar_t *); */
#endif

#define JCGO_PATH_TCHDIR(pathname) JCGO_JNUTCHAR_E(chdir(JCGO_JNUTCHAR_C(pathname)), _wchdir(pathname))

#else /* JCGO_UNIFSYS */

#ifdef __EMX__
/* #include <stdlib.h> */
/* int _chdir2(const char *); */
#define JCGO_PATH_TCHDIR(pathname) JCGO_JNUTCHAR_E(_chdir2(JCGO_JNUTCHAR_C(pathname)), _wchdir2(pathname))
#else
/* #include <direct.h> */
/* int chdir(const char *); */
#ifdef JCGO_SYSWCHAR
/* #include <direct.h> */
/* int _wchdir(const wchar_t *); */
#endif
#define JCGO_PATH_TCHDIR(pathname) JCGO_JNUTCHAR_E(chdir(JCGO_JNUTCHAR_C(pathname)), _wchdir(pathname))
#endif

#endif /* ! JCGO_UNIFSYS */

#ifndef JCGO_UNIPROC

#ifdef JCGO_UNIFSYS

/* #include <unistd.h> */
/* char *getcwd(char *, size_t); */

#ifdef JCGO_SYSWCHAR
/* #include <unistd.h> */
/* wchar_t *_wgetcwd(wchar_t *, size_t); */
#endif

#define JCGO_PATH_TGETCWD(tbuf, tsize) JCGO_JNUTCHAR_E((void *)getcwd(JCGO_JNUTCHAR_C(tbuf), tsize), (void *)_wgetcwd(tbuf, tsize))

#else /* JCGO_UNIFSYS */

#ifdef JCGO_UNIX
#ifdef __EMX__
/* #include <stdlib.h> */
/* char *_getcwd2(char *, int); */
#define JCGO_PATH_TGETCWD(tbuf, tsize) JCGO_JNUTCHAR_E((void *)_getcwd2(JCGO_JNUTCHAR_C(tbuf), tsize), (void *)_wgetcwd2(tbuf, tsize))
#else
/* #include <unistd.h> */
/* char *getcwd(char *, size_t); */
#ifdef JCGO_SYSWCHAR
/* #include <unistd.h> */
/* wchar_t *_wgetcwd(wchar_t *, size_t); */
#endif
#define JCGO_PATH_TGETCWD(tbuf, tsize) JCGO_JNUTCHAR_E((void *)getcwd(JCGO_JNUTCHAR_C(tbuf), tsize), (void *)_wgetcwd(tbuf, tsize))
#endif
#else
/* #include <direct.h> */
/* char *_getdcwd(int, char *, int); */
#ifdef JCGO_SYSWCHAR
/* #include <direct.h> */
/* wchar_t *_wgetdcwd(int, wchar_t *, int); */
#endif
#define JCGO_PATH_TGETCWD(tbuf, tsize) JCGO_JNUTCHAR_E((void *)_getdcwd(0, JCGO_JNUTCHAR_C(tbuf), tsize), (void *)_wgetdcwd(0, tbuf, tsize))
#endif

#endif /* ! JCGO_UNIFSYS */

#endif /* ! JCGO_UNIPROC */

#endif /* ! JCGO_PATH_TGETCWD */

#endif /* ! JCGO_NOCWDIR */

#endif /* ! JCGO_WINFILE */

#ifndef JCGO_PATH_MAXSIZE
#ifdef MAXPATH
#define JCGO_PATH_MAXSIZE MAXPATH
#else
#ifdef _MAX_PATH
#define JCGO_PATH_MAXSIZE _MAX_PATH
#else
#ifdef MAXPATHLEN
#define JCGO_PATH_MAXSIZE (MAXPATHLEN < 4200 ? MAXPATHLEN + 1 : 4200)
#else
#ifdef PATH_MAX
#define JCGO_PATH_MAXSIZE (PATH_MAX < 4200 ? PATH_MAX + 1 : 4200)
#else
#ifdef JCGO_UNIFSYS
#define JCGO_PATH_MAXSIZE 4097
#else
#define JCGO_PATH_MAXSIZE 512
#endif
#endif
#endif
#endif
#endif
#endif

#endif /* ! JCGO_NOFILES */

#endif
