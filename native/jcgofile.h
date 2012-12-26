/*
 * @(#) $(JCGO)/native/jcgofile.h --
 * a part of the JCGO native layer library (file I/O defs).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_EXEC, JCGO_FCHSIZES, JCGO_FFBLK, JCGO_FFDATA,
 * JCGO_FFDOS, JCGO_LARGEFILE, JCGO_MACOSX, JCGO_NOCWDIR, JCGO_NOFILES,
 * JCGO_NOREALPATH, JCGO_NOTIME, JCGO_NOUTIMBUF, JCGO_SYSWCHAR,
 * JCGO_TIMEALLOWNEG, JCGO_UNIFSYS, JCGO_UNIX, JCGO_WIN32, JCGO_WINFILE.
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

#ifdef JCGO_UNIX
#ifdef _WINSOCK_H
#ifndef select
#define select _unix_select
#define JCGO_UNDEF_SELECT
#endif
#endif
#endif

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
/* HANDLE CreateFileA(LPCSTR, DWORD, DWORD, SECURITY_ATTRIBUTES *, DWORD, DWORD, HANDLE); */
/* HANDLE CreateFileW(LPCWSTR, DWORD, DWORD, SECURITY_ATTRIBUTES *, DWORD, DWORD, HANDLE); */
/* DWORD GetLastError(void); */
/* BOOL ReadFile(HANDLE, void *, DWORD, DWORD *, OVERLAPPED *); */
/* BOOL WriteFile(HANDLE, const void *, DWORD, DWORD *, OVERLAPPED *); */
#endif

#ifndef _STDIO_H
#include <stdio.h>
/* FILE * const stderr; */
/* FILE * const stdin; */
/* FILE * const stdout; */
#endif

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifndef ERROR_EXE_MACHINE_TYPE_MISMATCH
#define ERROR_EXE_MACHINE_TYPE_MISMATCH 216L
#endif

#ifndef JCGO_NOTIME

/* #include <windows.h> */
/* void GetSystemTime(SYSTEMTIME *); */
/* DWORD GetTimeZoneInformation(TIME_ZONE_INFORMATION *); */
/* BOOL SystemTimeToFileTime(const SYSTEMTIME *, FILETIME *); */

#ifndef TIME_ZONE_ID_INVALID
#define TIME_ZONE_ID_INVALID ((DWORD)-1L)
#endif

#endif /* ! JCGO_NOTIME */

#ifdef _IO_NO_GET_OSFHANDLE
#ifndef _get_osfhandle
#define _get_osfhandle(fd) ((long)(fd))
#endif
#else
#ifndef __EMX__
#ifndef _IO_H
#include <io.h>
/* intptr_t _get_osfhandle(int); */
#endif
#endif
#endif

#ifdef JCGO_UNIX
/* #include <stdio.h> */
/* int fileno(FILE *); */
#define JCGO_FD_FILENO(f) fileno(f)
#else
/* #include <stdio.h> */
/* int _fileno(FILE *); */
#define JCGO_FD_FILENO(f) _fileno(f)
#endif

#define JCGO_FD_INFD (int)_get_osfhandle(JCGO_FD_FILENO(stdin))
#define JCGO_FD_OUTFD (int)_get_osfhandle(JCGO_FD_FILENO(stdout))
#define JCGO_FD_ERRFD (int)_get_osfhandle(JCGO_FD_FILENO(stderr))

#define JCGO_WINF_NUMTOHANDLE(value) ((HANDLE)((ptrdiff_t)(value)))
#define JCGO_WINF_HANDLETONUM(handle) ((ptrdiff_t)(handle))

#else /* JCGO_WINFILE */

#ifndef _ERRNO_H
#include <errno.h>
/* int errno; */
#endif

#ifndef _FCNTL_H
#include <fcntl.h>
#endif

#endif /* ! JCGO_WINFILE */

#ifndef _LOCALE_H
#include <locale.h>
/* char *setlocale(int, const char *); */
#endif

#ifndef JCGO_WINFILE

#ifndef _STRING_H
#include <string.h>
/* char *strerror(int); */
#endif

#ifndef JCGO_NOTIME

#ifndef _TIME_H
#include <time.h>
/* struct tm *localtime(const time_t *); */
/* void tzset(void); */
#endif

#ifdef JCGO_WIN32

#ifndef WIN32
#define WIN32
#endif

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 1
#endif

#ifndef _WINDOWS_H
#include <windows.h>
/* BOOL QueryPerformanceCounter(LARGE_INTEGER *); */
/* BOOL QueryPerformanceFrequency(LARGE_INTEGER *); */
#endif

#endif /* JCGO_WIN32 */

#endif /* ! JCGO_NOTIME */

#ifdef JCGO_UNIX

#ifndef _UNISTD_H
#include <unistd.h>
/* int close(int); */
/* ssize_t read(int, void *, size_t); */
/* ssize_t write(int, const void *, size_t); */
#endif

#ifndef _SYS_TIME_H
#include <sys/time.h>
#endif

#ifndef BSD_COMP
#define BSD_COMP 1
#endif

#ifndef _SYS_IOCTL_H
#include <sys/ioctl.h>
/* int ioctl(int, int, ...); */
#endif

#ifndef JCGO_UNIFSYS
#ifndef _IO_H
#include <io.h>
/* int setmode(int, int); */
#endif
#endif

#ifndef FD_SET
#ifndef _SYS_SELECT_H
#include <sys/select.h>
#endif
#endif

#ifndef JCGO_NOTIME

#ifdef _SVID_GETTOD
/* #include <sys/time.h> */
/* int gettimeofday(struct timeval *); */
#define JCGO_CURTIME_GET(pcurt) (void)gettimeofday((void *)(pcurt))
#else
/* #include <sys/time.h> */
/* int gettimeofday(struct timeval *, void *); */
#define JCGO_CURTIME_GET(pcurt) (void)gettimeofday((void *)(pcurt), NULL)
#endif

#define JCGO_CURTIME_T struct timeval
#ifdef JCGO_TIMEALLOWNEG
#define JCGO_CURTIME_ASMILLIS(type, pcurt) ((type)(pcurt)->tv_sec * (type)1000L + (type)((pcurt)->tv_usec / 1000L))
#else
#define JCGO_CURTIME_ASMILLIS(type, pcurt) ((type)((unsigned long)(pcurt)->tv_sec) * (type)1000L + (type)((pcurt)->tv_usec / 1000L))
#endif
#define JCGO_CURTIME_ASNANOS(type, pcurt) (((type)((unsigned long)(pcurt)->tv_sec) * ((type)1000L * (type)1000L) + (type)(pcurt)->tv_usec) * (type)1000L)

#endif /* ! JCGO_NOTIME */

#else /* JCGO_UNIX */

#ifndef JCGO_NOTIME

#ifndef _SYS_TIMEB_H
#include <sys/timeb.h>
/* void ftime(struct timeb *); */
#endif

#define JCGO_CURTIME_T struct timeb
#define JCGO_CURTIME_GET(pcurt) ftime(pcurt)
#ifdef JCGO_TIMEALLOWNEG
#define JCGO_CURTIME_ASMILLIS(type, pcurt) ((type)(pcurt)->time * (type)1000L + (type)(pcurt)->millitm)
#else
#define JCGO_CURTIME_ASMILLIS(type, pcurt) ((type)((unsigned long)(pcurt)->time) * (type)1000L + (type)(pcurt)->millitm)
#endif
#define JCGO_CURTIME_ASNANOS(type, pcurt) (JCGO_CURTIME_ASMILLIS(type, pcurt) * ((type)1000L * (type)1000L))

#endif /* ! JCGO_NOTIME */

#ifndef _IO_H
#include <io.h>
/* int close(int); */
/* int read(int, void *, unsigned); */
/* int setmode(int, int); */
/* int write(int, const void *, unsigned); */
#endif

#endif /* ! JCGO_UNIX */

#ifndef O_BINARY
#ifdef O_RAW
#define O_BINARY O_RAW
#else
#define O_BINARY 0
#endif
#endif

#ifndef JCGO_FD_INFD
#define JCGO_FD_INFD 0
#define JCGO_FD_OUTFD 1
#define JCGO_FD_ERRFD 2
#endif

#endif /* ! JCGO_WINFILE */

#ifdef __EMX__
/* #include <stdlib.h> */
/* void _wildcard(int *, char ***); */
#define JCGO_ARGV_WILDCARDSTMT(pargv) { int argc; if (*(pargv) != NULL) { argc = 0; while ((*(pargv))[argc] != NULL) argc++; _wildcard(&argc, pargv); } }
#define JCGO_ARGV_WWILDCARDSTMT(pwargv) { int argc; if (*(pwargv) != NULL) { argc = 0; while ((*(pwargv))[argc] != NULL) argc++; _wwildcard(&argc, pwargv); } }
#else
#define JCGO_ARGV_WILDCARDSTMT(pargv) { (void)0; }
#define JCGO_ARGV_WWILDCARDSTMT(pwargv) { (void)0; }
#endif

#ifndef EACCES
#define EACCES 12345
#endif
#ifndef EAGAIN
#define EAGAIN 12346
#endif
#ifndef EBADF
#define EBADF 12347
#endif
#ifndef EBUSY
#define EBUSY 12348
#endif
#ifndef EEXIST
#define EEXIST 12349
#endif
#ifndef EINTR
#define EINTR 12350
#endif
#ifndef EINVAL
#define EINVAL 12351
#endif
#ifndef EMFILE
#define EMFILE 12352
#endif
#ifndef ENFILE
#define ENFILE 12353
#endif
#ifndef ENOENT
#define ENOENT 12354
#endif
#ifndef ENOEXEC
#define ENOEXEC 12355
#endif
#ifndef ENOMEM
#define ENOMEM 12356
#endif
#ifndef ENOSPC
#define ENOSPC 12357
#endif
#ifndef ENOTDIR
#define ENOTDIR 12358
#endif
#ifndef EPIPE
#define EPIPE 12359
#endif
#ifndef EROFS
#define EROFS 12360
#endif
#ifndef ESPIPE
#define ESPIPE 12361
#endif

#ifndef JCGO_NEW_LINE
#ifdef JCGO_UNIFSYS
#define JCGO_FILE_CSEP '/'
#define JCGO_FILE_WSEP 0x2f /*'/'*/
#define JCGO_PATH_WDELIM 0x3a /*':'*/
#ifdef JCGO_MACOSX
#define JCGO_NEW_LINE "\r"
#else
#define JCGO_NEW_LINE "\n"
#endif
#else
#define JCGO_FILE_CSEP '\\'
#define JCGO_FILE_WSEP 0x5c /*'\\'*/
#define JCGO_PATH_WDELIM 0x3b /*';'*/
#define JCGO_NEW_LINE "\r\n"
#endif
#endif

#define JCGO_FILEIOCALL_BEGIN(pJniEnv) {
#define JCGO_FILEIOCALL_END(pJniEnv) }

#ifdef JCGO_NOFILES

#ifdef __EMX__
#ifdef JCGO_UNIX
#ifdef JCGO_UNIFSYS
#ifndef _IO_H
#include <io.h>
#endif
#endif
#endif
#endif

#else /* JCGO_NOFILES */

#ifndef _STDIO_H
#include <stdio.h>
#endif

#ifndef _LIMITS_H
#include <limits.h>
#endif

#ifndef JCGO_NOTIME

#ifdef JCGO_WIN32

/* #include <windows.h> */
/* BOOL CloseHandle(HANDLE); */
/* HANDLE CreateFileA(LPCSTR, DWORD, DWORD, SECURITY_ATTRIBUTES *, DWORD, DWORD, HANDLE); */
/* HANDLE CreateFileW(LPCWSTR, DWORD, DWORD, SECURITY_ATTRIBUTES *, DWORD, DWORD, HANDLE); */
/* BOOL SetFileTime(HANDLE, const FILETIME *, const FILETIME *, const FILETIME *); */

#ifndef FILE_FLAG_BACKUP_SEMANTICS
#define FILE_FLAG_BACKUP_SEMANTICS ((DWORD)0x2000000L)
#endif

#ifndef JCGO_WINFLAG_FILEBACKUPSEM
#ifdef _WIN32_WCE
#define JCGO_WINFLAG_FILEBACKUPSEM 0
#else
#define JCGO_WINFLAG_FILEBACKUPSEM FILE_FLAG_BACKUP_SEMANTICS
#endif
#endif

#endif /* JCGO_WIN32 */

#endif /* ! JCGO_NOTIME */

#ifndef JCGO_WINFILE

/* #include <stdio.h> */
/* int rename(const char *, const char *); */

#ifndef _SYS_STAT_H
#include <sys/stat.h>
/* int stat(const char *, struct stat *); */
#endif

#ifndef JCGO_NOTIME

#ifndef JCGO_NOUTIMBUF

#ifdef JCGO_UNIX
#ifndef _UTIME_H
#include <utime.h>
/* int utime(const char *, const struct utimbuf *); */
#endif
#else
#ifdef JCGO_FFBLK
#ifndef _UTIME_H
#include <utime.h>
/* int utime(const char *, struct utimbuf *); */
#endif
#else
#ifdef __BORLANDC__
#ifndef _UTIME_H
#include <utime.h>
/* int utime(const char *, const struct utimbuf *); */
#endif
#else
#ifndef _SYS_UTIME_H
#include <sys/utime.h>
/* int utime(const char *, struct utimbuf *); */
#endif
#endif
#endif
#endif

#ifdef JCGO_SYSWCHAR
/* #include <sys/utime.h> */
/* #include <utime.h> */
/* int _wutime(const wchar_t *, struct _utimbuf *); */
/* int _wutime(const wchar_t *, const struct utimbuf *); */
#endif

#endif /* ! JCGO_NOUTIMBUF */

#endif /* ! JCGO_NOTIME */

#ifdef JCGO_UNIX

/* #include <unistd.h> */
/* int access(const char *, int); */
/* int chdir(const char *); */
/* int fsync(int); */
/* int ftruncate(int, off_t); */
/* char *getcwd(char *, size_t); */
/* off_t lseek(int, off_t, int); */
/* int rmdir(const char *); */
/* int unlink(const char *); */

/* #include <fcntl.h> */
/* int open(const char *, int, ...); */

/* #include <sys/stat.h> */
/* int chmod(const char *, mode_t); */
/* int mkdir(const char *, mode_t); */

#ifdef JCGO_SYSWCHAR

/* #include <unistd.h> */
/* int _waccess(const wchar_t *, int); */
/* int _wchdir(const wchar_t *); */
/* wchar_t *_wgetcwd(wchar_t *, size_t); */
/* int _wrmdir(const wchar_t *); */
/* int _wunlink(const wchar_t *); */

/* #include <fcntl.h> */
/* int _wopen(const wchar_t *, int, ...); */

/* #include <sys/stat.h> */
/* int _wchmod(const wchar_t *, mode_t); */
/* int _wmkdir(const wchar_t *, mode_t); */

#endif /* JCGO_SYSWCHAR */

#ifdef __EMX__
#ifndef _IO_H
#include <io.h>
/* int fsync(int); */
/* int ftruncate(int, off_t); */
#endif
#endif

#else /* JCGO_UNIX */

/* #include <io.h> */
/* int access(const char *, int); */
/* int chmod(const char *, int); */
/* int chsize(int, long); */
/* long lseek(int, long, int); */
/* int open(const char *, int, ...); */

/* #include <stdio.h> */
/* int remove(const char *); */

#ifndef _DIRECT_H
#include <direct.h>
/* int chdir(const char *); */
/* int mkdir(const char *); */
/* int rmdir(const char *); */
#endif

#ifndef _SYS_LOCKING_H
#include <sys/locking.h>
#endif

#ifdef JCGO_LARGEFILE

#ifndef JCGO_FCHSIZES

#ifdef JCGO_WIN32

#ifdef JCGO_NOTIME

#ifndef WIN32
#define WIN32
#endif

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 1
#endif

#ifndef _WINDOWS_H
#include <windows.h>
#endif

#endif /* JCGO_NOTIME */

/* #include <windows.h> */
/* BOOL SetEndOfFile(HANDLE); */

#endif /* JCGO_WIN32 */

#endif /* ! JCGO_FCHSIZES */

#endif /* JCGO_LARGEFILE */

#ifdef JCGO_SYSWCHAR

/* #include <stdio.h> */
/* int _wremove(const wchar_t *); */

/* #include <io.h> */
/* int _waccess(const wchar_t *, int); */
/* int _wchmod(const wchar_t *, int); */
/* int _wopen(const wchar_t *, int, ...); */

/* #include <direct.h> */
/* int _wchdir(const wchar_t *); */
/* int _wmkdir(const wchar_t *); */
/* int _wrmdir(const wchar_t *); */

#endif /* JCGO_SYSWCHAR */

#endif /* ! JCGO_UNIX */

#ifdef JCGO_FFBLK
#ifdef __EMX__
#ifndef _EMX_SYSCALLS_H
#include <emx/syscalls.h>
#endif
#else
#ifndef _DIR_H
#include <dir.h>
#endif
#endif
#else
#ifdef JCGO_FFDATA
#ifdef JCGO_UNIFSYS
#ifdef JCGO_UNIX
#ifndef _IO_H
#include <io.h>
#endif
#endif
#endif
#else
#ifdef JCGO_FFDOS
#ifndef _DOS_H
#include <dos.h>
#endif
#else
#ifndef _DIRENT_H
#include <dirent.h>
#endif
#endif
#endif
#endif

#endif /* ! JCGO_WINFILE */

#endif /* ! JCGO_NOFILES */

/* #include <stdlib.h> */
/* char *getenv(const char *); */

#ifdef JCGO_SYSWCHAR
/* #include <stdlib.h> */
/* wchar_t *_wgetenv(const wchar_t *); */
#endif

#ifndef JCGO_GETENV_MAXNAMESIZE
#define JCGO_GETENV_MAXNAMESIZE 256
#endif

#ifndef JCGO_DOSUNIX_DRIVEPREFIX
#ifdef __DJGPP__
#define JCGO_DOSUNIX_DRIVEPREFIX "/dev/"
#else
#define JCGO_DOSUNIX_DRIVEPREFIX "/cygdrive/"
#endif
#endif

#ifndef JCGO_WINFILE

#define JCGO_FD_CLOSE(fd, perrcode) (close(fd) != -1 ? 0 : (*(perrcode) = errno, -1))

#define JCGO_FD_READ(fd, buf, len, perrcode) ((*(perrcode) = (int)read(fd, buf, len)) != -1 ? *(perrcode) : (*(perrcode) = errno, -1))
#define JCGO_FD_WRITE(fd, buf, len, perrcode) ((*(perrcode) = (int)write(fd, buf, len)) != -1 ? *(perrcode) : (*(perrcode) = errno, -1))

#ifdef JCGO_UNIX

#ifdef FIONREAD
#ifndef JCGO_FDINAVAIL_T
#define JCGO_FDINAVAIL_T unsigned /* or size_t */
#endif
#define JCGO_FD_INAVAIL(fd, pavail) ioctl(fd, FIONREAD, (void *)(pavail))
#endif

#ifdef FD_SET
/* #include <unistd.h> */
/* #include <sys/time.h> */
/* #include <sys/select.h> */
/* int select(int, fd_set *, fd_set *, fd_set *, struct timeval *); */
#ifdef __EMX__
#define JCGO_FD_SELECT(nfds, readfds, writefds, exceptfds, ptv) ((readfds) != NULL ? select(nfds, readfds, writefds, exceptfds, ptv) : 1)
#else
#define JCGO_FD_SELECT(nfds, readfds, writefds, exceptfds, ptv) select(nfds, readfds, writefds, exceptfds, (void *)(ptv))
#endif
#endif

#endif /* JCGO_UNIX */

#endif /* ! JCGO_WINFILE */

#ifndef JCGO_NOFILES

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

#ifndef JCGO_REALPATH_BUFLEN
#define JCGO_REALPATH_BUFLEN JCGO_PATH_MAXSIZE
#endif

#ifndef JCGO_WINFILE

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

#ifndef SEEK_SET
#define SEEK_SET 0
#define SEEK_CUR 1
#define SEEK_END 2
#endif

#ifndef O_WRONLY
#define O_WRONLY O_RDWR
#endif

#ifndef O_EXCL
#define O_EXCL 0
#endif

#ifndef O_SYNC
#ifdef O_FSYNC
#define O_SYNC O_FSYNC
#else
#define O_SYNC 0
#endif
#endif

#ifndef O_DSYNC
#define O_DSYNC O_SYNC
#endif

#ifndef O_NOINHERIT
#define O_NOINHERIT 0
#endif

#ifndef F_OK
#define F_OK 0
#endif

#ifndef R_OK
#define R_OK 0x4
#define W_OK 0x2
#define X_OK 0x1
#endif

#ifndef JCGO_OPEN_BASEMODE
#ifdef JCGO_EXEC
#define JCGO_OPEN_BASEMODE (O_BINARY | O_NOINHERIT)
#else
#define JCGO_OPEN_BASEMODE O_BINARY
#endif
#endif

#ifndef JCGO_PERM_OPEN
#define JCGO_PERM_MKDIR (S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IWGRP | S_IXGRP | S_IROTH | S_IXOTH)
#ifdef JCGO_UNIFSYS
#define JCGO_PERM_OPEN (S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH)
#define JCGO_PERM_TMPFILE (S_IRUSR | S_IWUSR)
#else
#ifdef S_IREAD
#define JCGO_PERM_OPEN (S_IREAD | S_IWRITE)
#else
#define JCGO_PERM_OPEN (S_IRUSR | S_IWUSR)
#endif
#define JCGO_PERM_TMPFILE JCGO_PERM_OPEN
#endif
#endif

#ifdef JCGO_SYSWCHAR

/* #include <stdio.h> */
/* int _wrename(const wchar_t *, const wchar_t *); */

/* #include <sys/stat.h> */
/* int _wstat(const wchar_t *, struct _stat *); */
/* int _wstat(const wchar_t *, struct stat *); */

#endif /* JCGO_SYSWCHAR */

#define JCGO_FD_TOPEN(mode, pathname, accperm, perrcode) ((*(perrcode) = JCGO_JNUTCHAR_E(open(JCGO_JNUTCHAR_C(pathname), mode, accperm), _wopen(pathname, mode, accperm))) != -1 ? *(perrcode) : (*(perrcode) = errno, -1))

#ifndef JCGO_FD_SETCLOEXEC
#ifdef JCGO_EXEC
#ifdef FD_CLOEXEC
#ifdef F_GETFD
#ifdef F_SETFD
/* #include <fcntl.h> */
/* int fcntl(int, int, ...); */
#define JCGO_FD_SETCLOEXEC(fd) ((fd) != -1 ? fcntl(fd, F_SETFD, fcntl(fd, F_GETFD, 0) | FD_CLOEXEC) : -1)
#endif
#endif
#endif
#endif
#endif

#ifdef _SYS_STAT_NO_CHMOD
#define JCGO_PATH_TCHMOD(pathname, mode) 0
#else
#define JCGO_PATH_TCHMOD(pathname, mode) JCGO_JNUTCHAR_E(chmod(JCGO_JNUTCHAR_C(pathname), mode), _wchmod(pathname, mode))
#endif

#ifdef JCGO_UNIX

#define JCGO_PATH_TMKDIR(pathname) JCGO_JNUTCHAR_E(mkdir(JCGO_JNUTCHAR_C(pathname), JCGO_PERM_MKDIR), _wmkdir(pathname, JCGO_PERM_MKDIR))

#ifdef JCGO_UNIFSYS
#define JCGO_PATH_TUNLINK(pathname) JCGO_JNUTCHAR_E(unlink(JCGO_JNUTCHAR_C(pathname)) ? rmdir(JCGO_JNUTCHAR_C(pathname)) : 0, _wunlink(pathname) ? _wrmdir(pathname) : 0)
#else
#define JCGO_PATH_TUNLINK(pathname) JCGO_JNUTCHAR_E(access(JCGO_JNUTCHAR_C(pathname), W_OK) || unlink(JCGO_JNUTCHAR_C(pathname)) ? rmdir(JCGO_JNUTCHAR_C(pathname)) : 0, _waccess(pathname, W_OK) || _wunlink(pathname) ? _wrmdir(pathname) : 0)
#define JCGO_PATHCHKVOLROOT_VIASTAT 1
#endif

#ifdef _UNISTD_NO_FSYNC
#define JCGO_FD_FSYNC(fd, perrcode) ((fd) ? (*(perrcode) = EBUSY, -1) : 0)
#else
#define JCGO_FD_FSYNC(fd, perrcode) (fsync(fd) != -1 ? 0 : (*(perrcode) = errno, -1))
#endif

#else /* JCGO_UNIX */

#define JCGO_PATH_TMKDIR(pathname) JCGO_JNUTCHAR_E(mkdir(JCGO_JNUTCHAR_C(pathname)), _wmkdir(pathname))

#define JCGO_PATH_TUNLINK(pathname) JCGO_JNUTCHAR_E(remove(JCGO_JNUTCHAR_C(pathname)) ? rmdir(JCGO_JNUTCHAR_C(pathname)) : 0, _wremove(pathname) ? _wrmdir(pathname) : 0)

#ifndef JCGO_FD_FSYNC
#ifdef JCGO_FFBLK
#ifdef fsync
/* #include <io.h> */
/* int fsync(int); */
#define JCGO_FD_FSYNC(fd, perrcode) (fsync(fd) != -1 ? 0 : (*(perrcode) = errno, -1))
#endif
#else
#ifdef __BORLANDC__
#ifdef fsync
/* #include <io.h> */
/* int fsync(int); */
#define JCGO_FD_FSYNC(fd, perrcode) (fsync(fd) != -1 ? 0 : (*(perrcode) = errno, -1))
#endif
#else
#ifndef _IO_NO_COMMIT
#ifdef __WATCOMC__
/* #include <io.h> */
/* int fsync(int); */
#define JCGO_FD_FSYNC(fd, perrcode) (fsync(fd) != -1 ? 0 : (*(perrcode) = errno, -1))
#else
/* #include <io.h> */
/* int _commit(int); */
#define JCGO_FD_FSYNC(fd, perrcode) (_commit(fd) != -1 ? 0 : (*(perrcode) = errno, -1))
#endif
#endif
#endif
#endif
#ifndef JCGO_FD_FSYNC
#define JCGO_FD_FSYNC(fd, perrcode) ((fd) ? (*(perrcode) = EBUSY, -1) : 0)
#endif
#endif

#endif /* ! JCGO_UNIX */

#ifndef JCGO_UNIFSYS
#ifdef S_IFLNK
/* #include <sys/stat.h> */
/* int lstat(const char *, struct stat *); */
#define JCGO_PATH_LSTAT(pathname, pst) lstat(pathname, pst)
#else
/* #include <sys/stat.h> */
/* int stat(const char *, struct stat *); */
#define JCGO_PATH_LSTAT(pathname, pst) stat(pathname, pst)
#endif
#endif

#endif /* ! JCGO_WINFILE */

#ifndef JCGO_FD_SETCLOEXEC
#define JCGO_FD_SETCLOEXEC(fd) 0
#endif

#ifndef JCGO_NOREALPATH
#ifdef S_IFLNK
/* #include <stdlib.h> */
/* char *realpath(const char *, char *); */
#ifdef JCGO_SYSWCHAR
/* #include <stdlib.h> */
/* wchar_t *wrealpath(const wchar_t *, wchar_t *); */
#ifndef _wrealpath
#define _wrealpath wrealpath
#endif
#endif
#define JCGO_PATH_TREALPATH(pathname, tbuf) JCGO_JNUTCHAR_E((void *)realpath(JCGO_JNUTCHAR_C(pathname), JCGO_JNUTCHAR_C(tbuf)), (void *)_wrealpath(pathname, tbuf))
#endif
#endif

#ifdef JCGO_UNIFSYS

#ifndef JCGO_PATH_LIBPREFIX
#define JCGO_PATH_LIBPREFIX "lib"
#ifdef JCGO_MACOSX
#define JCGO_PATH_LIBSUFFIX ".jnilib"
#else
#define JCGO_PATH_LIBSUFFIX ".so"
#endif
#endif

#ifndef JCGO_PATH_ISCASESENSITIVE
#ifdef JCGO_MACOSX
#define JCGO_PATH_ISCASESENSITIVE 0
#else
#define JCGO_PATH_ISCASESENSITIVE 1
#endif
#define JCGO_PATH_HASLONGNAMES 1
#endif

#else /* JCGO_UNIFSYS */

#ifndef JCGO_PATH_LIBPREFIX
#define JCGO_PATH_LIBPREFIX ""
#define JCGO_PATH_LIBSUFFIX ".dll"
#endif

#ifndef JCGO_PATH_ISCASESENSITIVE
#define JCGO_PATH_ISCASESENSITIVE 0
#ifdef NAME_MAX
#define JCGO_PATH_HASLONGNAMES (NAME_MAX > 12 ? 1 : 0)
#else
#ifdef MAXFILE
#define JCGO_PATH_HASLONGNAMES (MAXFILE > 9 ? 1 : 0)
#else
#ifdef FILENAME_MAX
#define JCGO_PATH_HASLONGNAMES (FILENAME_MAX > 128 ? 1 : 0)
#else
#define JCGO_PATH_HASLONGNAMES 1
#endif
#endif
#endif
#endif

#ifdef JCGO_UNIX
#ifdef __CYGWIN__
#ifndef _SYS_CYGWIN_H
#include <sys/cygwin.h>
#endif
#ifndef _CYGWIN_VERSION_H
#include <cygwin/version.h>
#endif
#ifdef CYGWIN_VERSION_CYGWIN_CONV
/* #include <sys/cygwin.h> */
/* ssize_t cygwin_conv_path(cygwin_conv_path_t, const void *, void *, size_t); */
#define JCGO_PATH_PLATFCONV(cbuf) cygwin_conv_path(CCP_POSIX_TO_WIN_A | CCP_RELATIVE, cbuf, cbuf, JCGO_REALPATH_BUFLEN)
#else
/* #include <sys/cygwin.h> */
/* int cygwin_conv_to_win32_path(const char *, char *); */
#define JCGO_PATH_PLATFCONV(cbuf) cygwin_conv_to_win32_path(cbuf, cbuf)
#endif
#endif
#endif

#endif /* ! JCGO_UNIFSYS */

#ifndef JCGO_NOCWDIR

#ifdef JCGO_WINFILE
#ifdef JCGO_UNIX
#ifndef _UNISTD_H
#include <unistd.h>
#endif
#else
#ifndef _DIRECT_H
#include <direct.h>
#endif
#endif
#endif

#ifdef JCGO_UNIFSYS

#define JCGO_PATH_TCHDIR(pathname) JCGO_JNUTCHAR_E(chdir(JCGO_JNUTCHAR_C(pathname)), _wchdir(pathname))
#define JCGO_PATH_TGETCWD(tbuf, tsize) JCGO_JNUTCHAR_E((void *)getcwd(JCGO_JNUTCHAR_C(tbuf), tsize), (void *)_wgetcwd(tbuf, tsize))
#define JCGO_PATH_TGETDCWD(drive, tbuf, tsize) JCGO_PATH_TGETCWD(tbuf, tsize)

#else /* JCGO_UNIFSYS */

#ifdef __EMX__
/* #include <stdlib.h> */
/* int _chdir2(const char *); */
#define JCGO_PATH_TCHDIR(pathname) JCGO_JNUTCHAR_E(_chdir2(JCGO_JNUTCHAR_C(pathname)), _wchdir2(pathname))
#else
#define JCGO_PATH_TCHDIR(pathname) JCGO_JNUTCHAR_E(chdir(JCGO_JNUTCHAR_C(pathname)), _wchdir(pathname))
#endif

#ifdef JCGO_UNIX

#ifdef __EMX__
/* #include <stdlib.h> */
/* int _getcwd1(char *, char); */
/* char *_getcwd2(char *, int); */
#define JCGO_PATH_TGETCWD(tbuf, tsize) JCGO_JNUTCHAR_E((void *)_getcwd2(JCGO_JNUTCHAR_C(tbuf), tsize), (void *)_wgetcwd2(tbuf, tsize))
#define JCGO_PATH_TGETDCWD(drive, tbuf, tsize) ((drive) ? ((unsigned)(drive) <= (unsigned)26 && JCGO_JNUTCHAR_E(_getcwd1(JCGO_JNUTCHAR_C(tbuf), ("ABCDEFGHIJKLMNOPQRSTUVWXYZ")[drive - 1]), _wgetcwd1(tbuf, (wchar_t)(drive + 0x40))) != -1 ? (void *)(tbuf) : NULL) : JCGO_PATH_TGETCWD(tbuf, tsize))
#else
#define JCGO_PATH_TGETCWD(tbuf, tsize) JCGO_JNUTCHAR_E((void *)getcwd(JCGO_JNUTCHAR_C(tbuf), tsize), (void *)_wgetcwd(tbuf, tsize))
#define JCGO_PATH_TGETDCWD(drive, tbuf, tsize) ((drive) ? NULL : JCGO_PATH_TGETCWD(tbuf, tsize))
#define JCGO_PATHGETDCWD_VIACHDIR 1
#endif

#else /* JCGO_UNIX */

/* #include <direct.h> */
/* char *_getdcwd(int, char *, int); */

#ifdef JCGO_SYSWCHAR
/* #include <direct.h> */
/* wchar_t *_wgetdcwd(int, wchar_t *, int); */
#endif

#define JCGO_PATH_TGETDCWD(drive, tbuf, tsize) JCGO_JNUTCHAR_E((void *)_getdcwd(drive, JCGO_JNUTCHAR_C(tbuf), tsize), (void *)_wgetdcwd(drive, tbuf, tsize))
#define JCGO_PATH_TGETCWD(tbuf, tsize) JCGO_PATH_TGETDCWD(0, tbuf, tsize)

#endif /* ! JCGO_UNIX */

#endif /* ! JCGO_UNIFSYS */

#endif /* ! JCGO_NOCWDIR */

#ifdef JCGO_WINFILE

/* #include <windows.h> */
/* BOOL CreateDirectoryA(LPCSTR, SECURITY_ATTRIBUTES *); */
/* BOOL CreateDirectoryW(LPCWSTR, SECURITY_ATTRIBUTES *); */
/* BOOL DeleteFileA(LPCSTR); */
/* BOOL DeleteFileW(LPCWSTR); */
/* BOOL FlushFileBuffers(HANDLE); */
/* DWORD GetFileAttributesA(LPCSTR); */
/* DWORD GetFileAttributesW(LPCWSTR); */
/* BOOL LockFileEx(HANDLE, DWORD, DWORD, DWORD, DWORD, OVERLAPPED *); */
/* BOOL MoveFileA(LPCSTR, LPCSTR); */
/* BOOL MoveFileW(LPCWSTR, LPCWSTR); */
/* BOOL RemoveDirectoryA(LPCSTR); */
/* BOOL RemoveDirectoryW(LPCWSTR); */
/* BOOL SetEndOfFile(HANDLE); */
/* BOOL SetFileAttributesA(LPCSTR, DWORD); */
/* BOOL SetFileAttributesW(LPCWSTR, DWORD); */
/* DWORD SetFilePointer(HANDLE, LONG, LONG *, DWORD); */
/* BOOL UnlockFileEx(HANDLE, DWORD, DWORD, DWORD, OVERLAPPED *); */

#ifndef INVALID_FILE_ATTRIBUTES
#define INVALID_FILE_ATTRIBUTES ((DWORD)-1L)
#endif

#ifndef LOCKFILE_FAIL_IMMEDIATELY
#define LOCKFILE_FAIL_IMMEDIATELY 0x1
#endif

#ifndef LOCKFILE_EXCLUSIVE_LOCK
#define LOCKFILE_EXCLUSIVE_LOCK 0x2
#endif

/* #include <windows.h> */
/* BOOL FindClose(HANDLE); */
/* HANDLE FindFirstFileA(LPCSTR, WIN32_FIND_DATAA *); */
/* BOOL FindNextFileA(HANDLE, WIN32_FIND_DATAA *); */

#ifdef JCGO_SYSWCHAR
/* #include <windows.h> */
/* HANDLE FindFirstFileW(LPCWSTR, WIN32_FIND_DATAW *); */
/* BOOL FindNextFileW(HANDLE, WIN32_FIND_DATAW *); */
#endif

struct jcgo_tfind_s
{
 HANDLE handle;
#ifdef JCGO_SYSWCHAR
 WIN32_FIND_DATAW data;
#else
 WIN32_FIND_DATAA data;
#endif
};

#define JCGO_TFIND_T struct jcgo_tfind_s
#define JCGO_TFIND_FIRST(pfinddata, pathname) (((pfinddata)->handle = JCGO_JNUTCHAR_E(FindFirstFileA(JCGO_JNUTCHAR_C(pathname), JCGO_JNUTCHAR_R(WIN32_FIND_DATAA, &(pfinddata)->data)), FindFirstFileW(pathname, &(pfinddata)->data))) != INVALID_HANDLE_VALUE ? 0 : -1)
#define JCGO_TFIND_NEXT(pfinddata) (JCGO_JNUTCHAR_E(FindNextFileA((pfinddata)->handle, JCGO_JNUTCHAR_R(WIN32_FIND_DATAA, &(pfinddata)->data)), FindNextFileW((pfinddata)->handle, &(pfinddata)->data)) ? 0 : -1)
#define JCGO_TFIND_ISHIDDEN(pfinddata) ((JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(WIN32_FIND_DATAA, &(pfinddata)->data)->dwFileAttributes, (pfinddata)->data.dwFileAttributes) & FILE_ATTRIBUTE_HIDDEN) != 0)
#define JCGO_TFIND_GETNAME(pfinddata) JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(JCGO_JNUTCHAR_T, JCGO_JNUTCHAR_R(WIN32_FIND_DATAA, &(pfinddata)->data)->cFileName), (pfinddata)->data.cFileName)
#define JCGO_TFIND_CLOSE(pfinddata) (FindClose((pfinddata)->handle) ? 0 : -1)

#else /* JCGO_WINFILE */

#ifdef JCGO_LARGEFILE

#ifdef JCGO_UNIX

/* #include <sys/stat.h> */
/* int stat64(const char *, struct stat64 *); */

#ifdef JCGO_SYSWCHAR
/* #include <sys/stat.h> */
/* int _wstat64(const wchar_t *, struct stat64 *); */
#endif

#define JCGO_BIGFSTAT_T struct stat64
#define JCGO_BIGF_TSTAT(pathname, pst) JCGO_JNUTCHAR_E(stat64(JCGO_JNUTCHAR_C(pathname), pst), _wstat64(pathname, pst))

#ifndef JCGO_BIGFLSEEK_T
#ifdef _OFF64_T_DEFINED
#define JCGO_BIGFLSEEK_T off64_t
#else
#ifdef __int64
#define JCGO_BIGFLSEEK_T __int64
#else
#define JCGO_BIGFLSEEK_T long long
#endif
#endif
#endif

#ifndef JCGO_BIGF_VLSEEK
/* #include <unistd.h> */
/* off64_t lseek64(int, off64_t, int); */
#define JCGO_BIGF_VLSEEK(fd, pofs, whence) (void)(*(pofs) = lseek64(fd, *(pofs), whence))
#endif

#ifndef JCGO_BIGFTRUNC_T
#define JCGO_BIGFTRUNC_T JCGO_BIGFLSEEK_T
#endif

#ifndef JCGO_BIGF_TRUNC
/* #include <unistd.h> */
/* int ftruncate64(int, off64_t); */
#define JCGO_BIGF_TRUNC(fd, ofs) ftruncate64(fd, ofs)
#endif

#else /* JCGO_UNIX */

#ifdef __BORLANDC__
/* #include <sys/stat.h> */
/* int _stati64(const char *, struct stati64 *); */
#ifdef JCGO_SYSWCHAR
/* #include <sys/stat.h> */
/* int _wstati64(const wchar_t *, struct stati64 *); */
#endif
#define JCGO_BIGFSTAT_T struct stati64
#else
/* #include <sys/stat.h> */
/* int _stati64(const char *, struct _stati64 *); */
#ifdef JCGO_SYSWCHAR
/* #include <sys/stat.h> */
/* int _wstati64(const wchar_t *, struct _stati64 *); */
#endif
#define JCGO_BIGFSTAT_T struct _stati64
#endif

#define JCGO_BIGF_TSTAT(pathname, pst) JCGO_JNUTCHAR_E(_stati64(JCGO_JNUTCHAR_C(pathname), pst), _wstati64(pathname, pst))

/* #include <io.h> */
/* __int64 _lseeki64(int, __int64, int); */

#define JCGO_BIGFLSEEK_T __int64
#define JCGO_BIGF_VLSEEK(fd, pofs, whence) (void)(*(pofs) = _lseeki64(fd, *(pofs), whence))

#ifdef JCGO_FCHSIZES
/* #include <io.h> */
/* errno_t _chsize_s(int, __int64); */
#define JCGO_BIGFTRUNC_T __int64
#define JCGO_BIGF_TRUNC(fd, ofs) (_chsize_s(fd, ofs) ? -1 : 0)
#else
#ifndef JCGO_BIGFTRUNC_T
#ifdef JCGO_WIN32
/* #include <io.h> */
/* intptr_t _get_osfhandle(int); */
#ifdef _IO_NO_GET_OSFHANDLE
#ifndef _get_osfhandle
#define _get_osfhandle(fd) ((long)(fd))
#endif
#endif
#define JCGO_FD_SETENDOFFILE(fd, perrcode) (SetEndOfFile((HANDLE)_get_osfhandle(fd)) ? 0 : (*(perrcode) = EBADF, -1))
#endif
#define JCGO_BIGFTRUNC_T long
#endif
#ifndef JCGO_BIGF_TRUNC
#define JCGO_BIGF_TRUNC(fd, ofs) chsize(fd, ofs)
#endif
#endif

#endif /* ! JCGO_UNIX */

#else /* JCGO_LARGEFILE */

#define JCGO_BIGFSTAT_T struct stat
#define JCGO_BIGF_TSTAT(pathname, pst) JCGO_JNUTCHAR_E(stat(JCGO_JNUTCHAR_C(pathname), pst), _wstat(pathname, (void *)(pst)))
#define JCGO_BIGF_VLSEEK(fd, pofs, whence) (void)(*(pofs) = lseek(fd, *(pofs), whence))

#ifdef JCGO_UNIX
#define JCGO_BIGFLSEEK_T off_t
#define JCGO_BIGFTRUNC_T off_t
#ifdef _UNISTD_NO_FTRUNCATE
#define JCGO_BIGF_TRUNC(fd, ofs) (errno = EINVAL, -1)
#else
#define JCGO_BIGF_TRUNC(fd, ofs) ftruncate(fd, ofs)
#endif
#else
#define JCGO_BIGFLSEEK_T long
#define JCGO_BIGFTRUNC_T long
#define JCGO_BIGF_TRUNC(fd, ofs) chsize(fd, ofs)
#endif

#endif /* ! JCGO_LARGEFILE */

#ifdef JCGO_UNIX

#ifndef JCGO_BIGFLKOFF_T
#ifdef JCGO_LARGEFILE
#ifdef F_SETLKW64
#define JCGO_BIGFLKOFF_T JCGO_BIGFLSEEK_T
#define JCGO_BIGFLOCK_T struct flock64
#define JCGO_BIGFLOCK_SETLK F_SETLK64
#define JCGO_BIGFLOCK_SETLKW F_SETLKW64
#endif
#endif
#ifndef JCGO_BIGFLKOFF_T
#ifdef F_SETLKW
#define JCGO_BIGFLKOFF_T off_t
#define JCGO_BIGFLOCK_T struct flock
#define JCGO_BIGFLOCK_SETLK F_SETLK
#define JCGO_BIGFLOCK_SETLKW F_SETLKW
#endif
#endif
#endif

#ifdef JCGO_BIGFLOCK_T
/* #include <fcntl.h> */
/* int fcntl(int, int, ...); */
/* #include <unistd.h> */
/* pid_t getpid(void); */
#define JCGO_BIGFLOCK_HASPOS
#define JCGO_BIGF_LOCK(pfl, fd, pos, len, doUnlock, isShared, doWait) ((pfl)->l_start = (pos), (pfl)->l_len = (len) != ~((JCGO_BIGFLKOFF_T)1L << ((int)sizeof(JCGO_BIGFLKOFF_T) * 8 - 1)) ? (len) : 0, (pfl)->l_pid = getpid(), (void)((doUnlock) ? ((pfl)->l_type = F_UNLCK, 0) : (isShared) ? ((pfl)->l_type = F_RDLCK, 0) : ((pfl)->l_type = F_WRLCK, 0)), (pfl)->l_whence = SEEK_SET, fcntl(fd, (doWait) ? JCGO_BIGFLOCK_SETLKW : JCGO_BIGFLOCK_SETLK, pfl))
#endif

#else /* JCGO_UNIX */

#ifdef LK_UNLCK
#ifndef _LK_UNLCK
#define _LK_UNLCK LK_UNLCK
#define _LK_LOCK LK_LOCK
#define _LK_NBLCK LK_NBLCK
#define _LK_RLCK LK_RLCK
#define _LK_NBRLCK LK_NBRLCK
#ifndef _locking
#define _locking locking
#endif
#endif
#endif

#ifdef _LK_UNLCK
/* #include <io.h> */
/* #include <sys/locking.h> */
/* int _locking(int, int, long); */
#ifndef JCGO_BIGFLKOFF_T
#define JCGO_BIGFLKOFF_T long
#define JCGO_BIGFLOCK_FUNC _locking
#endif
#define JCGO_BIGFLOCK_T int
#define JCGO_BIGF_LOCK(pfl, fd, pos, len, doUnlock, isShared, doWait) (*(pfl) = (doUnlock) ? _LK_UNLCK : (isShared) ? ((doWait) ? _LK_RLCK : _LK_NBRLCK) : (doWait) ? _LK_LOCK : _LK_NBLCK, JCGO_BIGFLOCK_FUNC(fd, *(pfl), len))
#endif

#endif /* ! JCGO_UNIX */

#ifndef _A_HIDDEN
#ifdef FA_HIDDEN
#define _A_HIDDEN FA_HIDDEN
#else
#define _A_HIDDEN 0x2
#endif
#endif

#ifndef _A_VOLID
#ifdef FA_LABEL
#define _A_VOLID FA_LABEL
#else
#define _A_VOLID 0x8
#endif
#endif

#ifdef JCGO_FFDATA

/* #include <io.h> */
/* int _findclose(long); */
/* long _findfirst(const char *, struct _finddata_t *); */
/* int _findnext(long, struct _finddata_t *); */

#ifdef JCGO_SYSWCHAR
/* #include <io.h> */
/* long _wfindfirst(const wchar_t *, struct _wfinddata_t *); */
/* int _wfindnext(long, struct _wfinddata_t *); */
#endif

struct jcgo_tfind_s
{
 long handle;
#ifdef JCGO_SYSWCHAR
 struct _wfinddata_t data;
#else
 struct _finddata_t data;
#endif
};

#define JCGO_TFIND_T struct jcgo_tfind_s
#define JCGO_TFIND_FIRST(pfinddata, pathname) (((pfinddata)->handle = (long)JCGO_JNUTCHAR_E(_findfirst(JCGO_JNUTCHAR_C(pathname), JCGO_JNUTCHAR_R(struct _finddata_t, &(pfinddata)->data)), _wfindfirst(pathname, &(pfinddata)->data))) != -1L ? 0 : -1)
#define JCGO_TFIND_NEXT(pfinddata) JCGO_JNUTCHAR_E(_findnext((pfinddata)->handle, JCGO_JNUTCHAR_R(struct _finddata_t, &(pfinddata)->data)), _wfindnext((pfinddata)->handle, &(pfinddata)->data))
#define JCGO_TFIND_ISHIDDEN(pfinddata) ((JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(struct _finddata_t, &(pfinddata)->data)->attrib, (pfinddata)->data.attrib) & _A_HIDDEN) != 0)
#define JCGO_TFIND_GETNAME(pfinddata) JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(JCGO_JNUTCHAR_T, JCGO_JNUTCHAR_R(struct _finddata_t, &(pfinddata)->data)->name), (pfinddata)->data.name)
#define JCGO_TFIND_CLOSE(pfinddata) _findclose((pfinddata)->handle)

#else /* JCGO_FFDATA */

#ifdef JCGO_FFBLK

#ifdef __EMX__
/* #include <emx/syscalls.h> */
/* int __findfirst(const char *, int, struct _find *); */
/* int __findnext(struct _find *); */
#ifdef JCGO_SYSWCHAR
#define JCGO_TFIND_T struct _wfind
#else
#define JCGO_TFIND_T struct _find
#endif
#define JCGO_TFIND_FIRST(pfinddata, pathname) (JCGO_JNUTCHAR_E(__findfirst(JCGO_JNUTCHAR_C(pathname), (unsigned char)~_A_VOLID, JCGO_JNUTCHAR_R(struct _find, pfinddata)), __wfindfirst(pathname, (unsigned char)~_A_VOLID, pfinddata)) ? -1 : 0)
#define JCGO_TFIND_NEXT(pfinddata) (JCGO_JNUTCHAR_E(__findnext(JCGO_JNUTCHAR_R(struct _find, pfinddata)), __wfindnext(pfinddata)) ? -1 : 0)
#define JCGO_TFIND_ISHIDDEN(pfinddata) ((JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(struct _find, pfinddata)->attr, (pfinddata)->attr) & _A_HIDDEN) != 0)
#define JCGO_TFIND_GETNAME(pfinddata) JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(JCGO_JNUTCHAR_T, JCGO_JNUTCHAR_R(struct _find, pfinddata)->name), (pfinddata)->name)
#else
/* #include <dir.h> */
/* int findfirst(const char *, struct ffblk *, int); */
/* int findnext(struct ffblk *); */
#ifdef JCGO_SYSWCHAR
/* #include <dir.h> */
/* int _wfindfirst(const wchar_t *, struct _wffblk *, int); */
/* int _wfindnext(struct _wffblk *); */
#define JCGO_TFIND_T struct _wffblk
#else
#define JCGO_TFIND_T struct ffblk
#endif
#define JCGO_TFIND_FIRST(pfinddata, pathname) (JCGO_JNUTCHAR_E(findfirst(JCGO_JNUTCHAR_C(pathname), JCGO_JNUTCHAR_R(struct ffblk, pfinddata), (unsigned char)~_A_VOLID), _wfindfirst(pathname, pfinddata, (unsigned char)~_A_VOLID)) ? -1 : 0)
#define JCGO_TFIND_NEXT(pfinddata) (JCGO_JNUTCHAR_E(findnext(JCGO_JNUTCHAR_R(struct ffblk, pfinddata)), _wfindnext(pfinddata)) ? -1 : 0)
#define JCGO_TFIND_ISHIDDEN(pfinddata) ((JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(struct ffblk, pfinddata)->ff_attrib, (pfinddata)->ff_attrib) & _A_HIDDEN) != 0)
#define JCGO_TFIND_GETNAME(pfinddata) JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(JCGO_JNUTCHAR_T, JCGO_JNUTCHAR_R(struct ffblk, pfinddata)->ff_name), (pfinddata)->ff_name)
#endif

#define JCGO_TFIND_CLOSE(pfinddata) (pfinddata != NULL ? 0 : -1)

#else /* JCGO_FFBLK */

#ifdef JCGO_FFDOS

/* #include <dos.h> */
/* unsigned _dos_findfirst(const char *, unsigned, struct find_t *); */
/* unsigned _dos_findnext(struct find_t *); */

#ifdef JCGO_SYSWCHAR
/* #include <dos.h> */
/* unsigned _wdos_findfirst(const wchar_t *, unsigned, struct _wfind_t *); */
/* unsigned _wdos_findnext(struct _wfind_t *); */
#define JCGO_TFIND_T struct _wfind_t
#else
#define JCGO_TFIND_T struct find_t
#endif

#define JCGO_TFIND_FIRST(pfinddata, pathname) (JCGO_JNUTCHAR_E(_dos_findfirst((void *)(pathname), (unsigned char)~_A_VOLID, JCGO_JNUTCHAR_R(struct find_t, pfinddata)), _wdos_findfirst((void *)(pathname), (unsigned char)~_A_VOLID, pfinddata)) ? -1 : 0)
#define JCGO_TFIND_NEXT(pfinddata) (JCGO_JNUTCHAR_E(_dos_findnext(JCGO_JNUTCHAR_R(struct find_t, pfinddata)), _wdos_findnext(pfinddata)) ? -1 : 0)
#define JCGO_TFIND_ISHIDDEN(pfinddata) ((JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(struct find_t, pfinddata)->attrib, (pfinddata)->attrib) & _A_HIDDEN) != 0)
#define JCGO_TFIND_GETNAME(pfinddata) JCGO_JNUTCHAR_E(JCGO_JNUTCHAR_R(JCGO_JNUTCHAR_T, JCGO_JNUTCHAR_R(struct find_t, pfinddata)->name), (pfinddata)->name)

#ifdef _DOS_HAS_FINDCLOSE
/* #include <dos.h> */
/* unsigned _dos_findclose(struct find_t *); */
#ifdef JCGO_SYSWCHAR
/* #include <dos.h> */
/* unsigned _wdos_findclose(struct _wfind_t *); */
#endif
#define JCGO_TFIND_CLOSE(pfinddata) (JCGO_JNUTCHAR_E(_dos_findclose(JCGO_JNUTCHAR_R(struct find_t, pfinddata)), _wdos_findclose(pfinddata)) ? -1 : 0)
#else
#define JCGO_TFIND_CLOSE(pfinddata) (pfinddata != NULL ? 0 : -1)
#endif

#else /* JCGO_FFDOS */

/* #include <dirent.h> */
/* int closedir(DIR *); */
/* DIR *opendir(const char *); */
/* struct dirent *readdir(DIR *); */

#ifdef JCGO_SYSWCHAR
#ifdef __BORLANDC__
/* #include <dirent.h> */
/* int wclosedir(wDIR *); */
/* wDIR *wopendir(const wchar_t *); */
/* struct wdirent *wreaddir(wDIR *); */
#ifndef _wdirent
#define _wdirent wdirent
#endif
#ifndef _wclosedir
#define _wclosedir wclosedir
#endif
#ifndef _wopendir
#define _wopendir wopendir
#endif
#ifndef _wreaddir
#define _wreaddir wreaddir
#endif
#else
/* #include <dirent.h> */
/* int _wclosedir(_WDIR *); */
/* _WDIR *_wopendir(const wchar_t *); */
/* struct _wdirent *_wreaddir(_WDIR *); */
#endif
#endif

/* #define JCGO_TFIND_NEEDSSYNC 1 */

#endif /* ! JCGO_FFDOS */

#endif /* ! JCGO_FFBLK */

#endif /* ! JCGO_FFDATA */

#endif /* ! JCGO_WINFILE */

#ifndef JCGO_TFIND_NEEDSSYNC
#define JCGO_TFIND_NEEDSSYNC 0
#endif

#endif /* ! JCGO_NOFILES */

#ifdef JCGO_UNDEF_SELECT
#undef JCGO_UNDEF_SELECT
#undef select
#endif

#endif
