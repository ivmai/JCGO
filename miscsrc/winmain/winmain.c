/*
 * @(#) winmain.c - Windows "WinMain" wrapper for C "main" entry.
 * Copyright (C) 2007-2009 Ivan Maidanski <ivmai@mail.ru> All rights reserved.
 **
 * Version: 1.4
 * Required: any ANSI C compiler for Windows (Win32, Win64, WinCE).
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

/*
 * Tested with:
 * Borland BCC32 (-w-par -tW),
 * MsDev CL (/link /subsystem:windows),
 * MsDev CL with MSVCRT (-MD -DWINMAIN_MSVCRT /link /subsystem:windows),
 * MsDev WinCE CL (-DWINMAIN_PARSECMDLINE -DWINMAIN_EMPTYENV -DUNDER_CE),
 * mingw32ce (-DWINMAIN_PARSECMDLINE -DWINMAIN_EMPTYENV -DWINMAIN_NOSTOREARGV).
 */

/*
 * Control macros:
 * UNDER_CE, WINMAIN_CALLEXIT, WINMAIN_EMPTYENV, WINMAIN_MSVCRT,
 * WINMAIN_NOGLOB, WINMAIN_NOSTOREARGV, WINMAIN_PARSECMDLINE,
 * WINMAIN_SETLOCALE, WINMAIN_TWOARGS, WINMAIN_USECRTGLOB, WINMAIN_WCHAR;
 * WINMAIN_CLIBDECL, WINMAIN_CONST, WINMAIN_FREE, WINMAIN_INIT,
 * WINMAIN_MALLOC, WINMAIN_STATIC.
 */

#ifndef _STDLIB_H
#include <stdlib.h>
/* void exit(int); */
/* void free(void *); */
/* void *malloc(size_t); */
/* int __argc; */
/* char **__argv; */
/* wchar_t **__wargv; */
/* char **_environ; */
/* wchar_t **_wenviron; */
#endif

#ifndef WINMAIN_NOGLOB
#ifndef _STRING_H
#include <string.h>
/* void *memcpy(void *, const void *, size_t); */
#endif
#endif

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 1
#endif

#ifndef _WINDOWS_H
#include <windows.h>
/* BOOL FindClose(HANDLE); */
/* HANDLE FindFirstFileA(LPCSTR, WIN32_FIND_DATAA *); */
/* HANDLE FindFirstFileW(LPCWSTR, WIN32_FIND_DATAW *); */
/* BOOL FindNextFileA(HANDLE, WIN32_FIND_DATAA *); */
/* BOOL FindNextFileW(HANDLE, WIN32_FIND_DATAW *); */
/* DWORD GetModuleFileNameA(HMODULE, LPSTR, DWORD); */
/* DWORD GetModuleFileNameW(HMODULE, LPWSTR, DWORD); */
/* LCID GetSystemDefaultLCID(void); */
/* BOOL SetThreadLocale(LCID); */
#endif

#ifndef NULL
#define NULL ((void *)0)
#endif

#ifndef WINMAIN_CLIBDECL
#define WINMAIN_CLIBDECL __cdecl
#endif

#ifndef WINMAIN_CONST
#define WINMAIN_CONST const
#endif

#ifndef WINMAIN_STATIC
#define WINMAIN_STATIC static
#endif

#ifndef WINMAIN_INIT
#define WINMAIN_INIT /* empty */
#endif

#ifdef WINMAIN_PARSECMDLINE
#ifndef WINMAIN_WCHAR
#ifdef UNDER_CE
#define WINMAIN_WCHAR
#endif
#endif
#endif

#ifdef WINMAIN_WCHAR
#define WINMAIN_TCHAR_OF(a, w) w
#else
#define WINMAIN_TCHAR_OF(a, w) a
#endif

#define WINMAIN_CHAR_T WINMAIN_TCHAR_OF(char, wchar_t)

#ifdef WINMAIN_TWOARGS
#define WINMAIN_MAIN_F(ac, av, env) WINMAIN_TCHAR_OF(main, wmain)(ac, av)
#else
#define WINMAIN_MAIN_F(ac, av, env) WINMAIN_TCHAR_OF(main, wmain)(ac, av, env)
#endif

#ifdef WINMAIN_EMPTYENV
#ifndef WINMAIN_TWOARGS
WINMAIN_STATIC WINMAIN_CHAR_T *WINMAIN_CONST winmain_empty_environ = NULL;
#endif
#define WINMAIN_MAIN_ENVARG ((WINMAIN_CHAR_T **)&winmain_empty_environ)
#else
#define WINMAIN_MAIN_ENVARG WINMAIN_TCHAR_OF(_environ, _wenviron)
#endif

#ifdef WINMAIN_MSVCRT
__declspec(dllimport) char **__initenv;
#ifdef WINMAIN_WCHAR
__declspec(dllimport) wchar_t **__winitenv;
#endif
#endif

extern int WINMAIN_CLIBDECL WINMAIN_MAIN_F(int argc, WINMAIN_CHAR_T **targv,
 WINMAIN_CHAR_T **tenv);

#ifdef WINMAIN_PARSECMDLINE

#ifndef WINMAIN_PROGPATH_BUFSIZE
#define WINMAIN_PROGPATH_BUFSIZE 512
#endif

#ifndef WINMAIN_MALLOC
#define WINMAIN_MALLOC malloc
#endif

#ifndef WINMAIN_FREE
#define WINMAIN_FREE free
#endif

#ifndef WINMAIN_NOMEM_ABORT
#define WINMAIN_NOMEM_ABORT exit(-1)
#endif

#ifdef WINMAIN_USECRTGLOB
#ifndef WINMAIN_NOGLOB
extern int _CRT_glob;
#endif
#endif

WINMAIN_STATIC WINMAIN_CHAR_T *winmain_get_progname(void)
{
 WINMAIN_CHAR_T *tpath = NULL;
 int len;
 WINMAIN_CHAR_T tbuf[WINMAIN_PROGPATH_BUFSIZE];
 len = (int)WINMAIN_TCHAR_OF(GetModuleFileNameA, GetModuleFileNameW)(NULL,
        tbuf, WINMAIN_PROGPATH_BUFSIZE);
 if (len > 0 && (tpath = WINMAIN_MALLOC((len + 1) *
     sizeof(WINMAIN_CHAR_T))) != NULL)
 {
  tpath[len] = (WINMAIN_CHAR_T)0;
#ifdef WINMAIN_NOGLOB
  while (len-- > 0)
   tpath[len] = tbuf[len];
#else
  (void)memcpy(tpath, tbuf, len * sizeof(WINMAIN_CHAR_T));
#endif
 }
 return tpath;
}

WINMAIN_STATIC int winmain_scan_token(int *plen, int pos,
 WINMAIN_CONST WINMAIN_CHAR_T *tcmdline)
{
 int isquote = 0;
 if (tcmdline[pos] == '"')
 {
  isquote = 1;
  pos++;
 }
 for(;;)
 {
  if (tcmdline[pos] == '"')
  {
   pos++;
   isquote = 1 - isquote;
  }
   else
   {
    if (!tcmdline[pos] ||
        (!isquote && (tcmdline[pos] == ' ' || tcmdline[pos] == '\t')))
     break;
    if (tcmdline[pos] != '\\' || tcmdline[pos + 1] != '"' ||
        ++pos == 1 || tcmdline[pos - 2] != '\\')
    {
     (*plen)++;
     pos++;
    }
   }
 }
 return pos;
}

WINMAIN_STATIC int winmain_copy_token(WINMAIN_CHAR_T *tstr,
 WINMAIN_CONST WINMAIN_CHAR_T *tcmdline, int pos)
{
 int len = 0;
 int isquote = 0;
 int wildcard = 0;
 if (tcmdline[pos] == '"')
 {
  isquote = 1;
  pos++;
 }
 for(;;)
 {
  if (tcmdline[pos] == '"')
  {
   pos++;
   isquote = 1 - isquote;
  }
   else
   {
    if (!tcmdline[pos] ||
        (!isquote && (tcmdline[pos] == ' ' || tcmdline[pos] == '\t')))
     break;
    if (tcmdline[pos] != '\\')
    {
#ifndef WINMAIN_NOGLOB
     if ((tcmdline[pos] == '*' || tcmdline[pos] == '?') && !isquote)
      wildcard = 1;
#endif
     tstr[len++] = tcmdline[pos++];
    }
     else if (tcmdline[pos + 1] != '"' || ++pos == 1 ||
              tcmdline[pos - 2] != '\\')
      tstr[len++] = tcmdline[pos++];
   }
 }
 tstr[len] = (WINMAIN_CHAR_T)0;
 return wildcard;
}

#ifndef WINMAIN_NOGLOB

WINMAIN_STATIC WINMAIN_CHAR_T **winmain_grow_argv(WINMAIN_CHAR_T **targv,
 int *pcapacity)
{
 int newsize = *pcapacity / 3 + *pcapacity + 3;
 WINMAIN_CHAR_T **tnewargv = WINMAIN_MALLOC((newsize + 1) * sizeof(void *));
 if (tnewargv != NULL)
 {
  (void)memcpy(tnewargv, targv, *pcapacity * sizeof(void *));
  *pcapacity = newsize;
  WINMAIN_FREE(targv);
 }
 return tnewargv;
}

WINMAIN_STATIC WINMAIN_CHAR_T **winmain_argv_adjsize(WINMAIN_CHAR_T **targv,
 int argc)
{
 WINMAIN_CHAR_T **tnewargv;
 if ((tnewargv = WINMAIN_MALLOC((argc + 1) * sizeof(void *))) != NULL)
 {
  (void)memcpy(tnewargv, targv, argc * sizeof(void *));
  WINMAIN_FREE(targv);
  targv = tnewargv;
 }
 return targv;
}

WINMAIN_STATIC WINMAIN_CHAR_T **winmain_process_wildcards(
 WINMAIN_CHAR_T **targv, int *pindex, int *pargc, int *pcapacity)
{
 HANDLE handle;
 WINMAIN_CHAR_T *tstr = targv[*pindex];
 WINMAIN_CHAR_T *tpath;
 int pos;
 int len;
 int found;
 WINMAIN_TCHAR_OF(WIN32_FIND_DATAA, WIN32_FIND_DATAW) finddata;
 if ((handle = WINMAIN_TCHAR_OF(FindFirstFileA, FindFirstFileW)(tstr,
     &finddata)) != INVALID_HANDLE_VALUE)
 {
  pos = 0;
  while (tstr[pos])
   pos++;
  while (pos-- > 0)
   if (tstr[pos] == '\\' || tstr[pos] == '/')
    break;
  if (++pos == 0 && tstr[1] == ':' && ((tstr[0] >= 'A' && tstr[0] <= 'Z') ||
      (tstr[0] >= 'a' && tstr[0] <= 'z')))
   pos = 2;
  found = 0;
  do
  {
   if (finddata.cFileName[0] != '.' || (finddata.cFileName[1] &&
       (finddata.cFileName[1] != '.' || finddata.cFileName[2])))
   {
    if (*pargc > *pcapacity &&
        (targv = winmain_grow_argv(targv, pcapacity)) == NULL)
     break;
    len = 0;
    while (finddata.cFileName[len])
     len++;
    if ((tpath = WINMAIN_MALLOC((pos + len + 1) *
        sizeof(WINMAIN_CHAR_T))) == NULL)
    {
     targv = NULL;
     break;
    }
    if (pos > 0)
     (void)memcpy(tpath, tstr, pos * sizeof(WINMAIN_CHAR_T));
    (void)memcpy(&tpath[pos], finddata.cFileName,
     len * sizeof(WINMAIN_CHAR_T));
    tpath[pos + len] = (WINMAIN_CHAR_T)0;
    targv[(*pindex)++] = tpath;
    (*pargc)++;
    found = 1;
   }
  } while (WINMAIN_TCHAR_OF(FindNextFileA, FindNextFileW)(handle, &finddata));
  (void)FindClose(handle);
  if (found)
  {
   (*pindex)--;
   (*pargc)--;
   WINMAIN_FREE(tstr);
  }
 }
 return targv;
}

#endif /* ! WINMAIN_NOGLOB */

WINMAIN_STATIC WINMAIN_CHAR_T **winmain_parse_cmdline(int *pargc,
 WINMAIN_CONST WINMAIN_CHAR_T *tcmdline)
{
 WINMAIN_CHAR_T **targv;
 int argc = 1;
 int index;
 int start;
 int pos = 0;
 int len = 0;
#ifndef WINMAIN_NOGLOB
 int capacity;
#endif
 do
 {
  while (tcmdline[pos] == ' ' || tcmdline[pos] == '\t')
   pos++;
  if (!tcmdline[pos])
   break;
  pos = winmain_scan_token(&len, pos, tcmdline);
  argc++;
 } while (tcmdline[pos++]);
 if ((targv = WINMAIN_MALLOC((argc + 1) * sizeof(void *))) == NULL ||
     (targv[0] = winmain_get_progname()) == NULL)
  WINMAIN_NOMEM_ABORT;
 pos = 0;
#ifndef WINMAIN_NOGLOB
 capacity = argc;
#endif
 for (index = 1; index < argc; index++)
 {
  while (tcmdline[pos] == ' ' || tcmdline[pos] == '\t')
   pos++;
  start = pos;
  len = 0;
  pos = winmain_scan_token(&len, pos, tcmdline) + 1;
  if ((targv[index] = WINMAIN_MALLOC((len + 1) *
      sizeof(WINMAIN_CHAR_T))) == NULL)
   WINMAIN_NOMEM_ABORT;
#ifdef WINMAIN_NOGLOB
  (void)winmain_copy_token(targv[index], tcmdline, start);
#else
  if (winmain_copy_token(targv[index], tcmdline, start) &&
#ifdef WINMAIN_USECRTGLOB
      _CRT_glob &&
#endif
      (targv = winmain_process_wildcards(targv, &index, &argc,
      &capacity)) == NULL)
   WINMAIN_NOMEM_ABORT;
#endif
 }
#ifndef WINMAIN_NOGLOB
 if (argc < capacity)
  targv = winmain_argv_adjsize(targv, argc);
#endif
 targv[argc] = NULL;
 *pargc = argc;
 return targv;
}

#endif /* WINMAIN_PARSECMDLINE */

/*ARGSUSED*/
int WINAPI /* APIENTRY */
#ifdef UNDER_CE
WinMain(HINSTANCE inst, HINSTANCE previnst, LPWSTR cmdline, int cmdshow)
#else
WINMAIN_TCHAR_OF(WinMain, wWinMain)(HINSTANCE inst, HINSTANCE previnst,
 WINMAIN_TCHAR_OF(LPSTR, LPWSTR) cmdline, int cmdshow)
#endif
{
 int res;
#ifdef WINMAIN_PARSECMDLINE
 WINMAIN_CHAR_T **targv;
#endif
 WINMAIN_INIT;
#ifdef WINMAIN_SETLOCALE
 SetThreadLocale(GetSystemDefaultLCID());
#endif
#ifdef WINMAIN_MSVCRT
 __initenv = _environ;
#ifdef WINMAIN_WCHAR
 __winitenv = _wenviron;
#endif
#endif
#ifdef WINMAIN_PARSECMDLINE
 targv = winmain_parse_cmdline(&res, cmdline);
#ifndef WINMAIN_NOSTOREARGV
 WINMAIN_TCHAR_OF(__argv, __wargv) = targv;
 __argc = res;
#endif
 res = WINMAIN_MAIN_F(res, targv, WINMAIN_MAIN_ENVARG);
#else
 res = WINMAIN_MAIN_F(__argc, WINMAIN_TCHAR_OF(__argv, __wargv),
        WINMAIN_MAIN_ENVARG);
#endif
#ifdef WINMAIN_CALLEXIT
 if (inst)
  exit(res);
#endif
 return res;
}
