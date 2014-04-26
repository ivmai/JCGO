#!/bin/sh
set -ex

# Build JCGO Win64 binaries for mingw-w64 (mingw32).
#
# Prerequisites:
# * mingw-w64 x86_64-w64-mingw32-gcc.exe (GCC) 4.5.2+
# * (cd contrib; curl http://www.hboehm.info/gc/gc_source/gc-7.4.0.tar.gz | tar zxf -; mv gc-7.4.0 bdwgc)
# * (cd contrib/bdwgc; curl http://www.hboehm.info/gc/gc_source/libatomic_ops-7.4.0.tar.gz | tar zxf -; mv libatomic_ops-7.4.0 libatomic_ops)
# * Oracle JDK 1.6.0+ (Windows x64)
# * set JAVA_HOME=<path_to_jdk>

AR=x86_64-w64-mingw32-ar
CC="x86_64-w64-mingw32-gcc -m64"
ARCH=amd64
BASESYS=win32
SYST=mingw64

# Set current working directory to JCGO root:
cd $(dirname "$0")/..

# Build BDWGC (static single- and multi-threaded):
mkdir -p libs/$ARCH/$SYST
mkdir -p .build_tmp/libs-gc-$ARCH-$SYST .build_tmp/libs-gcmt-$ARCH-$SYST
(cd .build_tmp/libs-gc-$ARCH-$SYST; $CC -O2 -fno-strict-aliasing \
    -Wall -Wextra -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION \
    -DGC_GCJ_SUPPORT -DNO_DEBUGGING -DDONT_USE_USER32_DLL -DUNICODE \
    -I ../../contrib/bdwgc/include -c ../../contrib/bdwgc/*.c \
    ../../contrib/bdwgc/*.cpp; $AR crus ../../libs/$ARCH/$SYST/libgc.a *.o)
(cd .build_tmp/libs-gcmt-$ARCH-$SYST; $CC -O2 -fno-strict-aliasing \
    -Wall -Wextra -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION \
    -DGC_GCJ_SUPPORT -DNO_DEBUGGING -DLARGE_CONFIG -DUSE_MUNMAP -DGC_THREADS \
    -DTHREAD_LOCAL_ALLOC -DPARALLEL_MARK -DDONT_USE_USER32_DLL -DUNICODE \
    -I ../../contrib/bdwgc/include -I ../../contrib/bdwgc/libatomic_ops/src \
    -c ../../contrib/bdwgc/*.c ../../contrib/bdwgc/*.cpp; \
    $AR crus ../../libs/$ARCH/$SYST/libgcmt.a *.o)

# Test compile jcgon:
mkdir -p .build_tmp/test-jcgon-$ARCH-$SYST
(cd .build_tmp/test-jcgon-$ARCH-$SYST; $CC -O2 -fwrapv \
    -fno-strict-aliasing -Wall -DJCGO_FFDATA -DJCGO_LARGEFILE -DJCGO_EXEC \
    -DJCGO_WIN32 -DJCGO_INET -DJCGO_ERRTOLOG -DJCGO_SYSWCHAR \
    -I ../../include -c ../../native/*.c)

# Build "trjnic" dynamic library (to enable TraceJni utility on Windows):
mkdir -p auxbin/$ARCH/$BASESYS
$CC -O2 -Wall -Wextra -DJCGO_WIN32 \
    -I $JAVA_HOME/include/$BASESYS -I $JAVA_HOME/include \
    -shared -o auxbin/$ARCH/$BASESYS/trjnic.dll -s reflgen/trjnic.c
