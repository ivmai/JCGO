/*
 * @(#) $(JCGO)/native/jcgoprop.h --
 * a part of the JCGO native layer library (get system props defs).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_FFBLK, JCGO_FFDOS, JCGO_NOSYSNAME, JCGO_UNIX,
 * JCGO_WIN32, JCGO_WINEXINFO.
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

#ifndef _STDIO_H
#include <stdio.h>
#endif

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifndef _LOCALE_H
#include <locale.h>
/* char *setlocale(int, const char *); */
#endif

#ifdef JCGO_UNIX

#ifndef _UNISTD_H
#include <unistd.h>
/* uid_t getuid(void); */
#endif

#ifndef JCGO_NOSYSNAME
#ifndef _SYS_UTSNAME_H
#include <sys/utsname.h>
/* int uname(struct utsname *); */
#endif
#endif

#ifndef _PWD_H
#include <pwd.h>
/* struct passwd *getpwuid(uid_t); */
#endif

#ifndef _PWD_NO_GETPWUID
#define JCGO_USERINFO_T struct passwd*
#define JCGO_USERINFO_GETNAME(pdata) ((*(pdata) = getpwuid(getuid())) != NULL && (*(pdata))->pw_name != NULL ? (*(pdata))->pw_name : "")
#define JCGO_USERINFO_GETHOME(pdata) ((*(pdata) = getpwuid(getuid())) != NULL && (*(pdata))->pw_dir != NULL ? (*(pdata))->pw_dir : "")
#endif

#endif /* JCGO_UNIX */

#ifdef JCGO_NOSYSNAME

#define JCGO_OSNAME_T char*
#define JCGO_OSNAME_GETNAME(pdata) (*(pdata) = "", *(pdata))
#define JCGO_OSNAME_GETRELEASE(pdata) (*(pdata) = "", *(pdata))

#else /* JCGO_NOSYSNAME */

#ifdef JCGO_UNIX

#define JCGO_OSNAME_T struct utsname
#define JCGO_OSNAME_GETNAME(pdata) (uname(pdata) != -1 && *(pdata)->sysname ? (pdata)->sysname : "Unix")
#ifdef __EMX__
#define JCGO_OSNAME_GETRELEASE(pdata) (uname(pdata) != -1 ? (pdata)->version : "")
#else
#define JCGO_OSNAME_GETRELEASE(pdata) (*((volatile char *)&(pdata)->release) = '\0', uname(pdata) != -1 ? (pdata)->release : "")
#endif

#define JCGO_OSVER_T int
#define JCGO_OSVER_GET(pdata) (void)(*(pdata) = -1)
#define JCGO_OSVER_MAJOR(pdata) (*(pdata))
#define JCGO_OSVER_MINOR(pdata) 0

#else /* JCGO_UNIX */

#ifdef JCGO_WIN32

#ifndef WIN32
#define WIN32
#endif

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 1
#endif

#ifndef _WINDOWS_H
#include <windows.h>
#endif

#ifndef NONLS

#ifndef JCGO_OSFILE_CODEPAGE
/* #include <windows.h> */
/* unsigned GetACP(void); */
#define JCGO_OSFILE_CODEPAGE GetACP()
#endif

/* #define JCGO_OSCONSOLE_CODEPAGE GetOEMCP() */

#ifdef LANGIDFROMLCID

#ifndef JCGO_USERLANG_ABBREVS
#define JCGO_USERLANG_ABBREVS "\000\000arbgcazhcsdadeelenesfifrhehuisitjakonlnoplptrmrorusrsksqsvthtruridukbesletlvlttgfavihyazeu\000\000mk\000\000tstn\000\000xhzuafkafohimtsegdyimskkkyswtkuzttbnpaguortateknmlasmrsamnbocykmlo\000\000gl\000\000\000\000\000\000\000\000si\000\000iuam\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000ha\000\000yoqu\000\000ba\000\000klig\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000br\000\000ugmiocco\000\000\000\000\000\000rwwo"
#endif

/* #include <windows.h> */
/* LCID GetUserDefaultLCID(void); */

#ifndef JCGO_USERLANG_T
#define JCGO_USERLANG_T struct { char *abbrPtr; unsigned primLangID; char cbuf[3]; }
#define JCGO_USERLANG_GETABBR(pdata) (((pdata)->primLangID = (unsigned)PRIMARYLANGID(LANGIDFROMLCID(GetUserDefaultLCID()))) < (sizeof(JCGO_USERLANG_ABBREVS) >> 1) ? ((pdata)->cbuf[0] = *((pdata)->abbrPtr = &(JCGO_USERLANG_ABBREVS)[(pdata)->primLangID << 1]), (pdata)->cbuf[1] = *((pdata)->abbrPtr + 1), (pdata)->cbuf[2] = '\0', (pdata)->cbuf) : "")
#endif

#ifndef JCGO_USERCNTRY_PACKEDABBREVS
#define JCGO_USERCNTRY_PACKEDABBREVS "AR\054\012AT\014\007BE\010\014BE\010\023BN\010\076BO\004\153BO\100\012BR\004\026BZ\050\011CA\014\014CA\020\011CH\010\007CH\010\020CH\020\014CL\064\012CN\010\120CO\044\012CR\024\012DO\034\012DZ\024\001EC\010\153EC\060\012FI\010\035FI\014\073GB\010\011GT\020\012HK\014\004HN\110\012IE\030\011IN\004\105IN\100\011JO\054\001LB\060\001LI\024\007LU\020\007LU\024\014MA\030\001MO\024\004MX\010\012MY\004\076MY\104\011NI\114\012NO\004\073NZ\024\011PA\030\012PE\014\153PE\050\012PH\064\011PR\120\012PY\074\012QA\100\001SA\004\001SE\010\073SG\020\004SG\110\011SV\104\012SY\050\001TN\034\001TW\004\004US\124\012UY\070\012VE\040\012YE\044\001ZA\034\011ZW\060\011"
#endif

#ifndef JCGO_USERCNTRY_T
#define JCGO_USERCNTRY_T struct { char cbuf[3]; }
#define JCGO_USERCNTRY_PREPARESTMT(pdata) { unsigned langId = (unsigned)LANGIDFROMLCID(GetUserDefaultLCID()); char *packedAbbrevs = (JCGO_USERCNTRY_PACKEDABBREVS); for ((pdata)->cbuf[0] = '\0'; *packedAbbrevs; packedAbbrevs += 4) if ((((unsigned)(*((unsigned char *)packedAbbrevs + 2)) << 8) | *((unsigned char *)packedAbbrevs + 3)) == langId) { (pdata)->cbuf[0] = *packedAbbrevs; (pdata)->cbuf[1] = *(packedAbbrevs + 1); (pdata)->cbuf[2] = '\0'; break; } }
#define JCGO_USERCNTRY_GETABBR(pdata) ((char *)(pdata)->cbuf)
#endif

#endif /* LANGIDFROMLCID */

#endif /* ! NONLS */

#ifdef GetVersionEx
/* #include <windows.h> */
/* BOOL GetVersionEx(OSVERSIONINFO *); */
#define JCGO_OSVER_T OSVERSIONINFO
#define JCGO_OSVER_GET(pdata) (void)((pdata)->dwOSVersionInfoSize = sizeof(OSVERSIONINFO), GetVersionEx(pdata) ? 0 : ((pdata)->dwMajorVersion = (DWORD)-1L, (pdata)->dwMinorVersion = (DWORD)-1L, 0))
#define JCGO_OSVER_MAJOR(pdata) ((int)(pdata)->dwMajorVersion)
#define JCGO_OSVER_MINOR(pdata) ((int)(pdata)->dwMinorVersion)
#else
/* #include <windows.h> */
/* DWORD GetVersion(void); */
#define JCGO_OSVER_T DWORD
#define JCGO_OSVER_GET(pdata) (void)(*(pdata) = GetVersion())
#define JCGO_OSVER_MAJOR(pdata) ((int)(unsigned char)(*(pdata)))
#define JCGO_OSVER_MINOR(pdata) ((int)(unsigned char)(*(pdata) >> 8))
#endif

#define JCGO_OSVER_USEWIN

#else /* JCGO_WIN32 */

#ifdef JCGO_FFDOS
#ifndef _DOS_H
#include <dos.h>
#endif
#endif

#ifdef __NT__
#define JCGO_OSVER_USEWIN
#else
#ifdef __WIN32__
#define JCGO_OSVER_USEWIN
#else
#ifdef _WIN32
#define JCGO_OSVER_USEWIN
#else
#ifdef _WINDOWS
#define JCGO_OSVER_USEWIN
#endif
#endif
#endif
#endif

#define JCGO_OSVER_T int

#ifndef JCGO_FFBLK
#ifndef __BORLANDC__
#ifdef JCGO_OSVER_USEWIN
/* #include <stdlib.h> */
/* const unsigned _winver; */
#define JCGO_OSVER_GET(pdata) (void)(*(pdata) = (int)_winver)
#define JCGO_OSVER_MAJOR(pdata) ((int)(((unsigned)(*(pdata))) >> 8))
#define JCGO_OSVER_MINOR(pdata) ((int)(unsigned char)(*(pdata)))
#else
#ifndef JCGO_FFDOS
#ifndef __WATCOMC__
/* #include <stdlib.h> */
/* const unsigned _osver; */
#define JCGO_OSVER_GET(pdata) (void)(*(pdata) = (int)_osver)
#define JCGO_OSVER_MAJOR(pdata) ((int)(((unsigned)(*(pdata))) >> 8))
#define JCGO_OSVER_MINOR(pdata) ((int)(unsigned char)(*(pdata)))
#endif
#endif
#endif
#endif
#endif

#ifndef JCGO_OSVER_GET
/* #include <stdlib.h> */
/* #include <dos.h> */
/* const unsigned char _osmajor; */
/* const unsigned char _osminor; */
#define JCGO_OSVER_GET(pdata) (void)(*(pdata) = (int)_osminor)
#define JCGO_OSVER_MAJOR(pdata) ((int)_osmajor)
#define JCGO_OSVER_MINOR(pdata) (*(pdata))
#endif

#endif /* ! JCGO_WIN32 */

#ifndef JCGO_OSNAME_STR
#ifdef __SYMBIAN32__
#define JCGO_OSNAME_STR "Symbian"
#else
#ifdef _WIN32_WCE
#define JCGO_OSNAME_STR "Windows CE"
#endif
#endif
#endif

#ifndef JCGO_OSNAME_STR
#ifdef __OS2__
#define JCGO_OSNAME_STR "OS/2"
#else
#ifdef _OS2
#define JCGO_OSNAME_STR "OS/2"
#else
#ifdef __TOS_OS2__
#define JCGO_OSNAME_STR "OS/2"
#else
#ifdef OS2
#define JCGO_OSNAME_STR "OS/2"
#endif
#endif
#endif
#endif
#endif

#ifndef JCGO_OSNAME_STR
#ifndef JCGO_OSVER_USEWIN
#ifdef __WINDOWS__
#define JCGO_OSNAME_STR "Windows"
#else
#define JCGO_OSNAME_STR "MS-DOS"
#endif
#endif
#endif

#ifdef JCGO_OSNAME_STR
#define JCGO_OSNAME_T char*
#define JCGO_OSNAME_GETNAME(pdata) (*(pdata) = JCGO_OSNAME_STR, *(pdata))
#else
#define JCGO_OSNAME_T JCGO_OSVER_T
#define JCGO_OSNAME_GETNAME(pdata) (JCGO_OSVER_GET(pdata), JCGO_OSVER_MAJOR(pdata) >= 6 ? (JCGO_OSVER_MINOR(pdata) == 0 && JCGO_OSVER_MAJOR(pdata) == 6 ? "Windows Vista" : "Windows 7") : JCGO_OSVER_MAJOR(pdata) == 5 ? (JCGO_OSVER_MINOR(pdata) == 0 ? "Windows 2000" : JCGO_OSVER_MINOR(pdata) == 1 ? "Windows XP" : "Windows 2003") : JCGO_OSVER_MAJOR(pdata) == 4 ? (JCGO_OSVER_MINOR(pdata) == 0 ? "Windows NT" /* or "Windows 95" */ : JCGO_OSVER_MINOR(pdata) == 10 ? "Windows 98" : JCGO_OSVER_MINOR(pdata) == 90 ? "Windows Me" : "Windows 95") : JCGO_OSVER_MAJOR(pdata) == 3 ? "Windows 3.1" : "Windows")
#endif

#define JCGO_OSNAME_GETRELEASE(pdata) ""

#endif /* ! JCGO_UNIX */

#endif /* ! JCGO_NOSYSNAME */

#ifdef JCGO_WINEXINFO

#ifndef _WINDOWS_H
#include <windows.h>
/* DWORD GetTempPathA(DWORD, char *); */
/* DWORD GetTempPathW(DWORD, WCHAR *); */
#endif

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

#ifndef JCGO_WINUSERNAME_BUFSIZE
#ifndef _WINBASE_NO_GETUSERNAME
#ifdef GetUserName
/* #include <windows.h> */
/* BOOL GetUserNameA(char *, DWORD *); */
/* BOOL GetUserNameW(WCHAR *, DWORD *); */
#ifdef UNLEN
#define JCGO_WINUSERNAME_BUFSIZE (UNLEN + 1)
#else
#define JCGO_WINUSERNAME_BUFSIZE 257
#endif
#endif
#endif
#endif

#ifdef GetUserHomeFolder
/* DWORD GetUserHomeFolderA(DWORD, char *); */
/* DWORD GetUserHomeFolderW(DWORD, WCHAR *); */
#endif

#endif /* JCGO_WINEXINFO */

#ifdef _P_tmpdir
#define JCGO_TMPDIR_PATH _P_tmpdir
#else
#ifdef P_tmpdir
#define JCGO_TMPDIR_PATH P_tmpdir
#else
#define JCGO_TMPDIR_PATH ""
#endif
#endif

#ifndef JCGO_OSUNICODE_ISLITTLE
#define JCGO_OSUNICODE_ISLITTLE (-1)
#endif

#ifndef JCGO_OSCONSOLE_CODEPAGE
#define JCGO_OSCONSOLE_CODEPAGE 0
#endif

#ifndef JCGO_OSFILE_CODEPAGE
#define JCGO_OSFILE_CODEPAGE 0
#endif

#ifndef JCGO_USERLANG_T
#define JCGO_USERLANG_T char*
#define JCGO_USERLANG_GETABBR(pdata) (*(pdata) = "", *(pdata))
#endif

#ifndef JCGO_USERCNTRY_T
#define JCGO_USERCNTRY_T char*
#define JCGO_USERCNTRY_GETABBR(pdata) (*(pdata) = "", *(pdata))
#endif

#ifndef JCGO_USERCNTRY_PREPARESTMT
#define JCGO_USERCNTRY_PREPARESTMT(pdata) { (void)0; }
#endif

#ifndef JCGO_USERINFO_T
#define JCGO_USERINFO_T char*
#define JCGO_USERINFO_GETNAME(pdata) (*(pdata) = "", *(pdata))
#define JCGO_USERINFO_GETHOME(pdata) (*(pdata) = "", *(pdata))
#endif

#ifndef JCGO_OSARCH_STR
#ifdef _M_ALPHA
#define JCGO_OSARCH_STR "alpha"
#else
#ifdef _ALPHA_
#define JCGO_OSARCH_STR "alpha"
#else
#ifdef __alpha__
#define JCGO_OSARCH_STR "alpha"
#else
#ifdef __AXP__
#define JCGO_OSARCH_STR "alpha"
#endif
#endif
#endif
#endif
#endif

#ifndef JCGO_OSARCH_STR
#ifdef _M_MRX000
#define JCGO_OSARCH_STR "mips"
#else
#ifdef _MIPS_
#define JCGO_OSARCH_STR "mips"
#else
#ifdef __mips__
#define JCGO_OSARCH_STR "mips"
#endif
#endif
#endif
#endif

#ifndef JCGO_OSARCH_STR
#ifdef __sparcv9
#define JCGO_OSARCH_STR "sparcv9"
#else
#ifdef __sparc
#define JCGO_OSARCH_STR "sparc"
#endif
#endif
#endif

#ifndef JCGO_OSARCH_STR
#ifdef __ppc64__
#define JCGO_OSARCH_STR "ppc64"
#else
#ifdef _M_PPC
#define JCGO_OSARCH_STR "ppc"
#else
#ifdef _M_MPPC
#define JCGO_OSARCH_STR "ppc"
#else
#ifdef __POWERPC__
#define JCGO_OSARCH_STR "ppc"
#else
#ifdef _PPC_
#define JCGO_OSARCH_STR "ppc"
#else
#ifdef __PPC__
#define JCGO_OSARCH_STR "ppc"
#else
#ifdef __ppc__
#define JCGO_OSARCH_STR "ppc"
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif

#ifndef JCGO_OSARCH_STR
#ifdef _M_M68K
#define JCGO_OSARCH_STR "m68k"
#else
#ifdef _M68K_
#define JCGO_OSARCH_STR "m68k"
#else
#ifdef _68K_
#define JCGO_OSARCH_STR "m68k"
#endif
#endif
#endif
#endif

#ifndef JCGO_OSARCH_STR
#ifdef _M_IA64
#define JCGO_OSARCH_STR "ia64"
#else
#ifdef __ia64
#define JCGO_OSARCH_STR "ia64"
#else
#ifdef __ia64__
#define JCGO_OSARCH_STR "ia64"
#endif
#endif
#endif
#endif

#ifndef JCGO_OSARCH_STR
#ifdef _M_AMD64
#define JCGO_OSARCH_STR "amd64"
#else
#ifdef __amd64
#define JCGO_OSARCH_STR "amd64"
#else
#ifdef __amd64__
#define JCGO_OSARCH_STR "amd64"
#else
#ifdef _M_X64
#define JCGO_OSARCH_STR "amd64"
#else
#ifdef __x86_64
#define JCGO_OSARCH_STR "amd64"
#else
#ifdef __x86_64__
#define JCGO_OSARCH_STR "amd64"
#endif
#endif
#endif
#endif
#endif
#endif
#endif

#ifndef JCGO_OSARCH_STR
#ifdef _M_ARM
#define JCGO_OSARCH_STR "arm"
#else
#ifdef _ARM_
#define JCGO_OSARCH_STR "arm"
#else
#ifdef __arm__
#define JCGO_OSARCH_STR "arm"
#else
#ifdef __AVR__
#define JCGO_OSARCH_STR "avr"
#else
#ifdef _M_SH
#define JCGO_OSARCH_STR "sh"
#else
#ifdef SHx
#define JCGO_OSARCH_STR "sh"
#else
#ifdef __sh__
#define JCGO_OSARCH_STR "sh"
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif

#ifndef JCGO_OSARCH_STR
#define JCGO_OSARCH_STR "x86"
#endif

#endif
