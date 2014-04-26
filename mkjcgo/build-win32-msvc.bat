@rem Build JCGO Win32 dynamic native libraries and binaries for MS VC++ x86.
@rem
@rem Prerequisites:
@rem * Microsoft (R) 32-bit C/C++ Optimizing Compiler Version 15+ for x80x86
@rem * Microsoft Windows SDK for Windows 7 and .NET Framework 3.5+
@rem * cd <path_to_jcgo>
@rem * (cd contrib; curl http://www.hboehm.info/gc/gc_source/gc-7.4.0.tar.gz | tar zxf -; mv gc-7.4.0 bdwgc)
@rem * (cd contrib/bdwgc; curl http://www.hboehm.info/gc/gc_source/libatomic_ops-7.4.0.tar.gz | tar zxf -; mv libatomic_ops-7.4.0 libatomic_ops)
@rem * (cd contrib; tar zxf tinygc-2_6.tar.bz2)
@rem * set INCLUDE=<path_to_vc>\include;<path_to_winsdk>\Include
@rem * set LIB=<path_to_vc>\lib;<path_to_winsdk>\Lib

@set AR=lib
@set CC=cl
@set ARCH=x86
@set BASESYS=win32
@set SYST=msvc

@rem Build BDWGC static single-threaded libraries:
mkdir libs\%ARCH%\%SYST%
mkdir .build_tmp\libs-gc-%ARCH%-%SYST%
cd .build_tmp\libs-gc-%ARCH%-%SYST%
%CC% -Ox -W3 -GF -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION -DGC_GCJ_SUPPORT -DNO_DEBUGGING -DLARGE_CONFIG -DEMPTY_GETENV_RESULTS -DDONT_USE_USER32_DLL -I..\..\contrib\bdwgc\include -D_CRT_SECURE_NO_DEPRECATE -Zl -c ..\..\contrib\bdwgc\*.c ..\..\contrib\bdwgc\*.cpp /nologo || exit /b 1
%AR% /machine:%ARCH% /out:..\..\libs\%ARCH%\%SYST%\gc.lib *.obj /nologo || exit /b 1
cd ..\..

@rem Build BDWGC static multi-threaded libraries:
mkdir .build_tmp\libs-gcmt-%ARCH%-%SYST%
cd .build_tmp\libs-gcmt-%ARCH%-%SYST%
%CC% -Ox -W3 -GF -MT -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION -DGC_GCJ_SUPPORT -DNO_DEBUGGING -DLARGE_CONFIG -DUSE_MUNMAP -DGC_THREADS -DTHREAD_LOCAL_ALLOC -DPARALLEL_MARK -DEMPTY_GETENV_RESULTS -DDONT_USE_USER32_DLL -I..\..\contrib\bdwgc\include -I..\..\contrib\bdwgc\libatomic_ops\src -D_CRT_SECURE_NO_DEPRECATE -Zl -c ..\..\contrib\bdwgc\*.c ..\..\contrib\bdwgc\*.cpp /nologo || exit /b 1
%AR% /machine:%ARCH% /out:..\..\libs\%ARCH%\%SYST%\gcmt.lib *.obj /nologo || exit /b 1
cd ..\..

@rem Build BDWGC dynamic library (multi-threaded):
mkdir dlls\%ARCH%\%BASESYS%
mkdir .build_tmp\dlls-gc-%ARCH%-%SYST%
cd .build_tmp\dlls-gc-%ARCH%-%SYST%
%CC% -Ox -W3 -GF -MT -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION -DGC_GCJ_SUPPORT -DATOMIC_UNCOLLECTABLE -DNO_DEBUGGING -DLARGE_CONFIG -DUSE_MUNMAP -DGC_THREADS -DTHREAD_LOCAL_ALLOC -DPARALLEL_MARK -I..\..\contrib\bdwgc\include -I..\..\contrib\bdwgc\libatomic_ops\src -D_CRT_SECURE_NO_DEPRECATE -DGC_DLL -LD ..\..\contrib\bdwgc\extra\gc.c ..\..\contrib\bdwgc\*.cpp /link /implib:gcdll.lib /out:..\..\dlls\%ARCH%\%BASESYS%\gc.dll /nologo user32.lib || exit /b 1
copy /Y gcdll.lib ..\..\libs\%ARCH%\%SYST%\ || exit /b 1
cd ..\..

@rem Build winmain (ASCII and wide-char variants):
cd libs\%ARCH%\%SYST%
%CC% -W3 -MT -DWINMAIN_SETLOCALE -Zl -c ..\..\..\miscsrc\winmain\winmain.c /nologo || exit /b 1
%CC% -W3 -MT -DWINMAIN_SETLOCALE -DWINMAIN_WCHAR -Zl -c -Fowwinmain ..\..\..\miscsrc\winmain\winmain.c /nologo || exit /b 1
mkdir md
cd md
%CC% -W3 -MD -DWINMAIN_SETLOCALE -DWINMAIN_MSVCRT -Zl -c ..\..\..\..\miscsrc\winmain\winmain.c /nologo || exit /b 1
%CC% -W3 -MD -DWINMAIN_SETLOCALE -DWINMAIN_MSVCRT -DWINMAIN_WCHAR -Zl -c -Fowwinmain ..\..\..\..\miscsrc\winmain\winmain.c /nologo || exit /b 1
cd ..\..\..\..

@rem Test compile jcgon:
mkdir .build_tmp\test-jcgon-%ARCH%-%SYST%
cd .build_tmp\test-jcgon-%ARCH%-%SYST%
%CC% -O2 -W3 -GF -MT -DJCGO_INTNN -DJCGO_FFDATA -DJCGO_LARGEFILE -DJCGO_EXEC -DJCGO_WIN32 -DJCGO_INET -DJCGO_ERRTOLOG -DJCGO_WMAIN -DJCGO_SYSWCHAR -DJCGO_SYSDUALW -I..\..\include -D_CRT_SECURE_NO_DEPRECATE -D_CRT_NONSTDC_NO_DEPRECATE -Zl -c ..\..\native\*.c /nologo || exit /b 1
cd ..\..

@rem Build TinyGC dynamic library (multi-threaded):
mkdir dlls\%ARCH%\%BASESYS%\tinygc
mkdir .build_tmp\dlls-tinygc-%ARCH%-%SYST%
cd .build_tmp\dlls-tinygc-%ARCH%-%SYST%
%CC% -Ox -W3 -GF -MT -DALL_INTERIOR_POINTERS -DGC_GCJ_SUPPORT -DGC_WIN32_THREADS -D_CRT_SECURE_NO_DEPRECATE -DGC_PRINT_MSGS -DGC_USE_WIN32_SYSTEMTIME -DGC_DLL -LD ..\..\contrib\tinygc\tinygc.c /link /out:..\..\dlls\%ARCH%\%BASESYS%\tinygc\gc.dll /nologo || exit /b 1
cd ..\..
