@rem Build JCGO Win64 dynamic native libraries and binaries for MS VC++ amd64.
@rem
@rem Prerequisites:
@rem * Microsoft (R) C/C++ Optimizing Compiler Version 15+ for x64
@rem * Microsoft Windows SDK for Windows 7 and .NET Framework 3.5+
@rem * cd <path_to_jcgo>
@rem * (cd contrib; curl http://www.hboehm.info/gc/gc_source/gc-7.4.0.tar.gz | tar zxf -; mv gc-7.4.0 bdwgc)
@rem * (cd contrib/bdwgc; curl http://www.hboehm.info/gc/gc_source/libatomic_ops-7.4.0.tar.gz | tar zxf -; mv libatomic_ops-7.4.0 libatomic_ops)
@rem * set INCLUDE=<path_to_vc>\include;<path_to_winsdk>\Include
@rem * set LIB=<path_to_vc>\lib\amd64;<path_to_winsdk>\Lib\x64

@set AR=lib
@set CC=cl
@set ARCH=amd64
@set BASESYS=win32
@set SYST=msvc

@rem Build BDWGC static single-threaded libraries:
mkdir libs\%ARCH%\%SYST%
mkdir .build_tmp\libs-gc-%ARCH%-%SYST%
cd .build_tmp\libs-gc-%ARCH%-%SYST%
%CC% -Ox -W4 -wd4100 -wd4127 -GF -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION -DGC_GCJ_SUPPORT -DNO_DEBUGGING -DDONT_USE_USER32_DLL -I..\..\contrib\bdwgc\include -D_CRT_SECURE_NO_DEPRECATE -wd4565 -Zl -c ..\..\contrib\bdwgc\*.c ..\..\contrib\bdwgc\*.cpp /nologo || exit /b 1
%AR% /machine:%ARCH% /out:..\..\libs\%ARCH%\%SYST%\gc.lib *.obj /nologo || exit /b 1
cd ..\..

@rem Build BDWGC static multi-threaded libraries:
mkdir .build_tmp\libs-gcmt-%ARCH%-%SYST%
cd .build_tmp\libs-gcmt-%ARCH%-%SYST%
%CC% -Ox -W4 -wd4100 -wd4127 -GF -MT -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION -DGC_GCJ_SUPPORT -DNO_DEBUGGING -DLARGE_CONFIG -DUSE_MUNMAP -DGC_THREADS -DTHREAD_LOCAL_ALLOC -DPARALLEL_MARK -DDONT_USE_USER32_DLL -I..\..\contrib\bdwgc\include -I..\..\contrib\bdwgc\libatomic_ops\src -D_CRT_SECURE_NO_DEPRECATE -wd4565 -Zl -c ..\..\contrib\bdwgc\*.c ..\..\contrib\bdwgc\*.cpp /nologo || exit /b 1
%AR% /machine:%ARCH% /out:..\..\libs\%ARCH%\%SYST%\gcmt.lib *.obj /nologo || exit /b 1
cd ..\..

@rem Build BDWGC dynamic library (multi-threaded):
mkdir dlls\%ARCH%\%BASESYS%
mkdir .build_tmp\dlls-gc-%ARCH%-%SYST%
cd .build_tmp\dlls-gc-%ARCH%-%SYST%
%CC% -Ox -W4 -wd4100 -wd4127 -GF -MT -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION -DGC_GCJ_SUPPORT -DATOMIC_UNCOLLECTABLE -DNO_DEBUGGING -DLARGE_CONFIG -DUSE_MUNMAP -DGC_THREADS -DTHREAD_LOCAL_ALLOC -DPARALLEL_MARK -I..\..\contrib\bdwgc\include -I..\..\contrib\bdwgc\libatomic_ops\src -D_CRT_SECURE_NO_DEPRECATE -wd4565 -DGC_DLL -LD ..\..\contrib\bdwgc\extra\gc.c ..\..\contrib\bdwgc\*.cpp /link /implib:gcdll.lib /out:..\..\dlls\%ARCH%\%BASESYS%\gc64.dll /nologo user32.lib || exit /b 1
copy /Y gcdll.lib ..\..\libs\%ARCH%\%SYST%\ || exit /b 1
cd ..\..

@rem Build winmain (wide-char variant):
cd libs\%ARCH%\%SYST%
%CC% -W3 -MT -DWINMAIN_SETLOCALE -DWINMAIN_WCHAR -Zl -c -Fowwinmain ..\..\..\miscsrc\winmain\winmain.c /nologo || exit /b 1
cd ..\..\..

@rem Test compile jcgon:
mkdir .build_tmp\test-jcgon-%ARCH%-%SYST%
cd .build_tmp\test-jcgon-%ARCH%-%SYST%
%CC% -Ox -W3 -GF -MT -DJCGO_INTNN -DJCGO_FFDATA -DJCGO_LARGEFILE -DJCGO_EXEC -DJCGO_WIN32 -DJCGO_INET -DJCGO_ERRTOLOG -DJCGO_WMAIN -DJCGO_SYSWCHAR -I..\..\include -D_CRT_SECURE_NO_DEPRECATE -D_CRT_NONSTDC_NO_DEPRECATE -c ..\..\native\*.c /nologo || exit /b 1
cd ..\..
