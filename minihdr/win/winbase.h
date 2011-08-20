/*
 * @(#) $(JCGO)/minihdr/win/winbase.h --
 * a part of the minimalist "Win32" headers for JCGO.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
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

#ifndef _WINBASE_H
#define _WINBASE_H

#ifdef _WINDOWS_H

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef WINBASEAPI
#define WINBASEAPI __declspec(dllimport)
#endif

#ifndef WINADVAPI
#define WINADVAPI WINBASEAPI
#endif

#define THREAD_PRIORITY_HIGHEST 2 /* or 1 */
#define THREAD_PRIORITY_LOWEST (-2) /* or 5 */
#define THREAD_PRIORITY_TIME_CRITICAL 15 /* or 0 */

#define INVALID_HANDLE_VALUE ((HANDLE)(DWORD_PTR)-1L)

#define INVALID_FILE_ATTRIBUTES ((DWORD)-1L)

#define FILE_BEGIN 0
#define FILE_CURRENT 1
#define FILE_END 2

#define TIME_ZONE_ID_INVALID ((DWORD)-1L)

#define WAIT_FAILED ((DWORD)-1L)

#define INFINITE ((DWORD)-1L)

#define STILL_ACTIVE ((DWORD)0x103L)

#define FILE_FLAG_BACKUP_SEMANTICS ((DWORD)0x2000000L)
#define FILE_FLAG_RANDOM_ACCESS ((DWORD)0x10000000L)
#define FILE_FLAG_WRITE_THROUGH ((DWORD)0x80000000L)

#define CREATE_NEW 1
#define CREATE_ALWAYS 2
#define OPEN_EXISTING 3
#define OPEN_ALWAYS 4
#define TRUNCATE_EXISTING 5

#define TLS_OUT_OF_INDEXES ((DWORD)-1L)

#define CREATE_UNICODE_ENVIRONMENT 0x400

#ifndef _WINBASE_NO_CREATEPIPE
#define STARTF_USESTDHANDLES 0x100
#endif

typedef struct _OVERLAPPED
{
 DWORD_PTR Internal; /* unused */
 DWORD_PTR InternalHigh; /* unused */
 union
 {
  struct
  {
   DWORD Offset;
   DWORD OffsetHigh;
  };
  LPVOID Pointer; /* unused */
 };
 HANDLE hEvent; /* unused */
} OVERLAPPED, FAR *LPOVERLAPPED;

typedef struct _SECURITY_ATTRIBUTES
{
 DWORD nLength; /* unused */
 LPVOID lpSecurityDescriptor; /* unused */
 BOOL bInheritHandle; /* unused */
} SECURITY_ATTRIBUTES, FAR *LPSECURITY_ATTRIBUTES;

typedef struct _PROCESS_INFORMATION
{
 HANDLE hProcess;
 HANDLE hThread;
 DWORD dwProcessId; /* unused */
 DWORD dwThreadId; /* unused */
} PROCESS_INFORMATION, FAR *LPPROCESS_INFORMATION;

#ifndef _FILETIME_
#define _FILETIME_
typedef struct _FILETIME
{
 DWORD dwLowDateTime;
 DWORD dwHighDateTime;
} FILETIME, FAR *LPFILETIME;
#endif

typedef struct _SYSTEMTIME
{
 WORD wYear;
 WORD wMonth;
 WORD wDayOfWeek; /* unused */
 WORD wDay;
 WORD wHour;
 WORD wMinute;
 WORD wSecond;
 WORD wMilliseconds;
} SYSTEMTIME, FAR *LPSYSTEMTIME;

typedef DWORD (WINAPI *LPTHREAD_START_ROUTINE)(LPVOID);

typedef struct _SYSTEM_INFO
{
 union
 {
  DWORD dwOemId; /* unused */
  struct
  {
   WORD wProcessorArchitecture; /* unused */
   WORD wReserved; /* unused */
  };
 };
 DWORD dwPageSize; /* unused */
 LPVOID lpMinimumApplicationAddress; /* unused */
 LPVOID lpMaximumApplicationAddress; /* unused */
 DWORD_PTR dwActiveProcessorMask;
 DWORD dwNumberOfProcessors;
 DWORD dwProcessorType; /* unused */
 DWORD dwAllocationGranularity; /* unused */
 WORD wProcessorLevel; /* unused */
 WORD wProcessorRevision; /* unused */
} SYSTEM_INFO, FAR *LPSYSTEM_INFO;

typedef struct _TIME_ZONE_INFORMATION
{
 LONG Bias;
 WCHAR StandardName[32]; /* unused */
 SYSTEMTIME StandardDate; /* unused */
 LONG StandardBias; /* unused */
 WCHAR DaylightName[32]; /* unused */
 SYSTEMTIME DaylightDate; /* unused */
 LONG DaylightBias;
} TIME_ZONE_INFORMATION, FAR *LPTIME_ZONE_INFORMATION;

typedef struct _STARTUPINFOA
{
 DWORD cb;
 LPSTR lpReserved; /* unused */
 LPSTR lpDesktop; /* unused */
 LPSTR lpTitle; /* unused */
 DWORD dwX; /* unused */
 DWORD dwY; /* unused */
 DWORD dwXSize; /* unused */
 DWORD dwYSize; /* unused */
 DWORD dwXCountChars; /* unused */
 DWORD dwYCountChars; /* unused */
 DWORD dwFillAttribute; /* unused */
 DWORD dwFlags;
 WORD wShowWindow; /* unused */
 WORD cbReserved2; /* unused */
 LPBYTE lpReserved2; /* unused */
 HANDLE hStdInput;
 HANDLE hStdOutput;
 HANDLE hStdError;
} STARTUPINFOA, FAR *LPSTARTUPINFOA;

typedef struct _STARTUPINFOW
{
 DWORD cb;
 LPWSTR lpReserved; /* unused */
 LPWSTR lpDesktop; /* unused */
 LPWSTR lpTitle; /* unused */
 DWORD dwX; /* unused */
 DWORD dwY; /* unused */
 DWORD dwXSize; /* unused */
 DWORD dwYSize; /* unused */
 DWORD dwXCountChars; /* unused */
 DWORD dwYCountChars; /* unused */
 DWORD dwFillAttribute; /* unused */
 DWORD dwFlags;
 WORD wShowWindow; /* unused */
 WORD cbReserved2; /* unused */
 LPBYTE lpReserved2; /* unused */
 HANDLE hStdInput;
 HANDLE hStdOutput;
 HANDLE hStdError;
} STARTUPINFOW, FAR *LPSTARTUPINFOW;

#ifdef UNICODE
typedef STARTUPINFOW STARTUPINFO;
typedef LPSTARTUPINFOW LPSTARTUPINFO;
#else
typedef STARTUPINFOA STARTUPINFO;
typedef LPSTARTUPINFOA LPSTARTUPINFO;
#endif

typedef struct _WIN32_FIND_DATAA
{
 DWORD dwFileAttributes;
 FILETIME ftCreationTime; /* unused */
 FILETIME ftLastAccessTime; /* unused */
 FILETIME ftLastWriteTime;
 DWORD nFileSizeHigh;
 DWORD nFileSizeLow;
 DWORD dwReserved0; /* unused */
 DWORD dwReserved1; /* unused */
 char cFileName[MAX_PATH];
 char cAlternateFileName[14]; /* unused */
#ifdef _MAC
 DWORD dwFileType; /* unused */
 DWORD dwCreatorType; /* unused */
 WORD wFinderFlags; /* unused */
#endif
} WIN32_FIND_DATAA, FAR *LPWIN32_FIND_DATAA;

typedef struct _WIN32_FIND_DATAW
{
 DWORD dwFileAttributes;
 FILETIME ftCreationTime; /* unused */
 FILETIME ftLastAccessTime; /* unused */
 FILETIME ftLastWriteTime;
 DWORD nFileSizeHigh;
 DWORD nFileSizeLow;
 DWORD dwReserved0; /* unused */
 DWORD dwReserved1; /* unused */
 WCHAR cFileName[MAX_PATH];
 WCHAR cAlternateFileName[14]; /* unused */
#ifdef _MAC
 DWORD dwFileType; /* unused */
 DWORD dwCreatorType; /* unused */
 WORD wFinderFlags; /* unused */
#endif
} WIN32_FIND_DATAW, FAR *LPWIN32_FIND_DATAW;

#ifdef UNICODE
typedef WIN32_FIND_DATAW WIN32_FIND_DATA;
typedef LPWIN32_FIND_DATAW LPWIN32_FIND_DATA;
#else
typedef WIN32_FIND_DATAA WIN32_FIND_DATA;
typedef LPWIN32_FIND_DATAA LPWIN32_FIND_DATA;
#endif

WINBASEAPI LONG WINAPI InterlockedExchange(LONG volatile FAR *, LONG);

#ifndef _WINBASE_NO_GETPROCESSAFFINITYMASK
WINBASEAPI BOOL WINAPI GetProcessAffinityMask(HANDLE, DWORD_PTR FAR *,
 DWORD_PTR FAR *);
#endif

WINBASEAPI HANDLE WINAPI GetCurrentProcess(void);

#ifndef _WINBASE_NO_TERMINATEPROCESS
WINBASEAPI BOOL WINAPI TerminateProcess(HANDLE, UINT);
#endif

WINBASEAPI BOOL WINAPI GetExitCodeProcess(HANDLE, LPDWORD);

WINBASEAPI HANDLE WINAPI CreateThread(LPSECURITY_ATTRIBUTES, DWORD,
 LPTHREAD_START_ROUTINE, LPVOID, DWORD, LPDWORD);

#ifndef _WINBASE_NO_GETCURRENTTHREAD
WINBASEAPI HANDLE WINAPI GetCurrentThread(void);
#endif

WINBASEAPI DWORD WINAPI GetCurrentThreadId(void);
WINBASEAPI BOOL WINAPI SetThreadPriority(HANDLE, int);

#ifndef GetLastError
WINBASEAPI DWORD WINAPI GetLastError(void);
#endif

WINBASEAPI BOOL WINAPI GetThreadContext(HANDLE, CONTEXT FAR *);
WINBASEAPI DWORD WINAPI SuspendThread(HANDLE);
WINBASEAPI DWORD WINAPI ResumeThread(HANDLE);

WINBASEAPI BOOL WINAPI SetEvent(HANDLE);
WINBASEAPI BOOL WINAPI ResetEvent(HANDLE);
WINBASEAPI DWORD WINAPI WaitForSingleObject(HANDLE, DWORD);

WINBASEAPI void WINAPI Sleep(DWORD);

#ifndef _WINBASE_NO_LOCKFILEEX
#define LOCKFILE_FAIL_IMMEDIATELY 0x1
#define LOCKFILE_EXCLUSIVE_LOCK 0x2
WINBASEAPI BOOL WINAPI LockFileEx(HANDLE, DWORD, DWORD, DWORD, DWORD,
 LPOVERLAPPED);
WINBASEAPI BOOL WINAPI UnlockFileEx(HANDLE, DWORD, DWORD, DWORD,
 LPOVERLAPPED);
#endif

WINBASEAPI BOOL WINAPI WriteFile(HANDLE, LPCVOID, DWORD, LPDWORD,
 LPOVERLAPPED);
WINBASEAPI BOOL WINAPI ReadFile(HANDLE, LPVOID, DWORD, LPDWORD, LPOVERLAPPED);
WINBASEAPI BOOL WINAPI FlushFileBuffers(HANDLE);

WINBASEAPI BOOL WINAPI SetEndOfFile(HANDLE);

WINBASEAPI DWORD WINAPI SetFilePointer(HANDLE, LONG, LPLONG, DWORD);

WINBASEAPI BOOL WINAPI FindClose(HANDLE);

WINBASEAPI BOOL WINAPI SetFileTime(HANDLE, const FILETIME FAR *,
 const FILETIME FAR *, const FILETIME FAR *);

WINBASEAPI BOOL WINAPI CloseHandle(HANDLE);

WINBASEAPI BOOL WINAPI DuplicateHandle(HANDLE, HANDLE, HANDLE, LPHANDLE,
 DWORD, BOOL, DWORD);

#ifndef _WINBASE_NO_SETHANDLEINFORMATION
#define HANDLE_FLAG_INHERIT 0x1
WINBASEAPI BOOL WINAPI SetHandleInformation(HANDLE, DWORD, DWORD);
#endif

WINBASEAPI void WINAPI GetSystemTime(LPSYSTEMTIME);

#ifndef _WINBASE_NO_GETSYSTEMINFO
WINBASEAPI void WINAPI GetSystemInfo(LPSYSTEM_INFO);
#endif

WINBASEAPI DWORD WINAPI GetTimeZoneInformation(LPTIME_ZONE_INFORMATION);

WINBASEAPI BOOL WINAPI SystemTimeToFileTime(const SYSTEMTIME FAR *,
 LPFILETIME);

#ifndef _WINBASE_NO_CREATEPIPE
WINBASEAPI BOOL WINAPI CreatePipe(LPHANDLE, LPHANDLE, LPSECURITY_ATTRIBUTES,
 DWORD);
#endif

WINBASEAPI DWORD WINAPI TlsAlloc(void);
WINBASEAPI LPVOID WINAPI TlsGetValue(DWORD);
WINBASEAPI BOOL WINAPI TlsSetValue(DWORD, LPVOID);

WINBASEAPI HANDLE WINAPI CreateEventA(LPSECURITY_ATTRIBUTES, BOOL, BOOL,
 LPCSTR);
WINBASEAPI HANDLE WINAPI CreateEventW(LPSECURITY_ATTRIBUTES, BOOL, BOOL,
 LPCWSTR);
#ifdef UNICODE
#define CreateEvent CreateEventW
#else
#define CreateEvent CreateEventA
#endif

WINBASEAPI BOOL WINAPI CreateProcessA(LPCSTR, LPSTR, LPSECURITY_ATTRIBUTES,
 LPSECURITY_ATTRIBUTES, BOOL, DWORD, LPVOID, LPCSTR, LPSTARTUPINFOA,
 LPPROCESS_INFORMATION);
WINBASEAPI BOOL WINAPI CreateProcessW(LPCWSTR, LPWSTR, LPSECURITY_ATTRIBUTES,
 LPSECURITY_ATTRIBUTES, BOOL, DWORD, LPVOID, LPCWSTR, LPSTARTUPINFOW,
 LPPROCESS_INFORMATION);
#ifdef UNICODE
#define CreateProcess CreateProcessW
#else
#define CreateProcess CreateProcessA
#endif

WINBASEAPI DWORD WINAPI GetTempPathA(DWORD, LPSTR);
WINBASEAPI DWORD WINAPI GetTempPathW(DWORD, LPWSTR);
#ifdef UNICODE
#define GetTempPath GetTempPathW
#else
#define GetTempPath GetTempPathA
#endif

WINBASEAPI BOOL WINAPI CreateDirectoryA(LPCSTR, LPSECURITY_ATTRIBUTES);
WINBASEAPI BOOL WINAPI CreateDirectoryW(LPCWSTR, LPSECURITY_ATTRIBUTES);
#ifdef UNICODE
#define CreateDirectory CreateDirectoryW
#else
#define CreateDirectory CreateDirectoryA
#endif

WINBASEAPI BOOL WINAPI RemoveDirectoryA(LPCSTR);
WINBASEAPI BOOL WINAPI RemoveDirectoryW(LPCWSTR);
#ifdef UNICODE
#define RemoveDirectory RemoveDirectoryW
#else
#define RemoveDirectory RemoveDirectoryA
#endif

WINBASEAPI HANDLE WINAPI CreateFileA(LPCSTR, DWORD, DWORD,
 LPSECURITY_ATTRIBUTES, DWORD, DWORD, HANDLE);
WINBASEAPI HANDLE WINAPI CreateFileW(LPCWSTR, DWORD, DWORD,
 LPSECURITY_ATTRIBUTES, DWORD, DWORD, HANDLE);
#ifdef UNICODE
#define CreateFile CreateFileW
#else
#define CreateFile CreateFileA
#endif

WINBASEAPI BOOL WINAPI SetFileAttributesA(LPCSTR, DWORD);
WINBASEAPI BOOL WINAPI SetFileAttributesW(LPCWSTR, DWORD);
#ifdef UNICODE
#define SetFileAttributes SetFileAttributesW
#else
#define SetFileAttributes SetFileAttributesA
#endif

WINBASEAPI DWORD WINAPI GetFileAttributesA(LPCSTR);
WINBASEAPI DWORD WINAPI GetFileAttributesW(LPCWSTR);
#ifdef UNICODE
#define GetFileAttributes GetFileAttributesW
#else
#define GetFileAttributes GetFileAttributesA
#endif

WINBASEAPI BOOL WINAPI DeleteFileA(LPCSTR);
WINBASEAPI BOOL WINAPI DeleteFileW(LPCWSTR);
#ifdef UNICODE
#define DeleteFile DeleteFileW
#else
#define DeleteFile DeleteFileA
#endif

WINBASEAPI HANDLE WINAPI FindFirstFileA(LPCSTR, LPWIN32_FIND_DATAA);
WINBASEAPI HANDLE WINAPI FindFirstFileW(LPCWSTR, LPWIN32_FIND_DATAW);
#ifdef UNICODE
#define FindFirstFile FindFirstFileW
#else
#define FindFirstFile FindFirstFileA
#endif

WINBASEAPI BOOL WINAPI FindNextFileA(HANDLE, LPWIN32_FIND_DATAA);
WINBASEAPI BOOL WINAPI FindNextFileW(HANDLE, LPWIN32_FIND_DATAW);
#ifdef UNICODE
#define FindNextFile FindNextFileW
#else
#define FindNextFile FindNextFileA
#endif

WINBASEAPI BOOL WINAPI MoveFileA(LPCSTR, LPCSTR);
WINBASEAPI BOOL WINAPI MoveFileW(LPCWSTR, LPCWSTR);
#ifdef UNICODE
#define MoveFile MoveFileW
#else
#define MoveFile MoveFileA
#endif

#ifndef _WINBASE_NO_GETUSERNAME
WINADVAPI BOOL WINAPI GetUserNameA(LPSTR, LPDWORD);
WINADVAPI BOOL WINAPI GetUserNameW(LPWSTR, LPDWORD);
#ifdef UNICODE
#define GetUserName GetUserNameW
#else
#define GetUserName GetUserNameA
#endif
#endif

#ifndef _WINBASE_NO_QUERYPERFORMANCECOUNTER
WINBASEAPI BOOL WINAPI QueryPerformanceCounter(PLARGE_INTEGER);
WINBASEAPI BOOL WINAPI QueryPerformanceFrequency(PLARGE_INTEGER);
#endif

WINBASEAPI BOOL WINAPI GetVersionExA(LPOSVERSIONINFOA);
WINBASEAPI BOOL WINAPI GetVersionExW(LPOSVERSIONINFOW);
#ifdef UNICODE
#define GetVersionEx GetVersionExW
#else
#define GetVersionEx GetVersionExA
#endif

#ifdef __cplusplus
}
#endif

#include <winerror.h>

#endif

#endif
