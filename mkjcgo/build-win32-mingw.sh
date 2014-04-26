#!/bin/sh
set -ex

# Build JCGO Win32 binaries using/for MinGW.
#
# Prerequisites:
# * MinGW v5.1.6 (GCC 4.4.0, mingwrt-3.17, w32api-3.14, binutils-2.20.1)
# * (cd contrib; curl http://www.hboehm.info/gc/gc_source/gc-7.4.0.tar.gz | tar zxf -; mv gc-7.4.0 bdwgc)
# * (cd contrib/bdwgc; curl http://www.hboehm.info/gc/gc_source/libatomic_ops-7.4.0.tar.gz | tar zxf -; mv libatomic_ops-7.4.0 libatomic_ops)
# * (cd contrib; tar zxf tinygc-2_6.tar.bz2)
# * Oracle JDK 1.4.2+
# * set JAVA_HOME=<path_to_jdk>

AR=ar
CC=mingw32-gcc
ARCH=x86
BASESYS=win32
SYST=mingw

# Set current working directory to JCGO root:
cd $(dirname "$0")/..

# Build BDWGC dynamic library (multi-threaded):
mkdir -p dlls/$ARCH/$BASESYS/$SYST
$CC -O2 -fno-strict-aliasing -fno-omit-frame-pointer -Wall -Wextra \
    -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION -DGC_GCJ_SUPPORT \
    -DATOMIC_UNCOLLECTABLE -DNO_DEBUGGING -DLARGE_CONFIG -DUSE_MUNMAP \
    -DGC_THREADS -DTHREAD_LOCAL_ALLOC -DPARALLEL_MARK -DEMPTY_GETENV_RESULTS \
    -I contrib/bdwgc/include -I contrib/bdwgc/libatomic_ops/src \
    -DGC_UNDERSCORE_STDCALL -DGC_DLL -shared \
    -o dlls/$ARCH/$BASESYS/$SYST/gc.dll -s contrib/bdwgc/extra/gc.c -luser32

# Build BDWGC static libraries (single- and multi-threaded):
mkdir -p libs/$ARCH/$SYST
mkdir -p .build_tmp/libs-gc-$ARCH-$SYST .build_tmp/libs-gcmt-$ARCH-$SYST
(cd .build_tmp/libs-gc-$ARCH-$SYST; $CC -O2 -fno-strict-aliasing \
    -Wall -Wextra -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION \
    -DGC_GCJ_SUPPORT -DNO_DEBUGGING -DDONT_USE_USER32_DLL -DLARGE_CONFIG \
    -DEMPTY_GETENV_RESULTS -fno-omit-frame-pointer \
    -I ../../contrib/bdwgc/include -c ../../contrib/bdwgc/*.c \
    ../../contrib/bdwgc/*.cpp; $AR crus ../../libs/$ARCH/$SYST/libgc.a *.o)
(cd .build_tmp/libs-gcmt-$ARCH-$SYST; $CC -O2 -fno-strict-aliasing \
    -Wall -Wextra -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION \
    -DGC_GCJ_SUPPORT -DNO_DEBUGGING -DLARGE_CONFIG -DUSE_MUNMAP -DGC_THREADS \
    -DTHREAD_LOCAL_ALLOC -DPARALLEL_MARK -DDONT_USE_USER32_DLL \
    -DEMPTY_GETENV_RESULTS -fno-omit-frame-pointer \
    -I ../../contrib/bdwgc/include -I ../../contrib/bdwgc/libatomic_ops/src \
    -c ../../contrib/bdwgc/*.c ../../contrib/bdwgc/*.cpp; \
    $AR crus ../../libs/$ARCH/$SYST/libgcmt.a *.o)

# Build tinygc.o:
$CC -O2 -Wall -Wextra -DALL_INTERIOR_POINTERS -DGC_GCJ_SUPPORT \
    -DGC_PRINT_MSGS -c -o libs/$ARCH/$SYST/tinygc.o contrib/tinygc/tinygc.c

# Test compile jcgon:
mkdir -p .build_tmp/test-jcgon-$ARCH-$SYST
(cd .build_tmp/test-jcgon-$ARCH-$SYST; $CC -O2 -fwrapv \
    -fno-strict-aliasing -Wall -DJCGO_FFDATA -DJCGO_LARGEFILE -DJCGO_EXEC \
    -DJCGO_WIN32 -DJCGO_INET -DJCGO_ERRTOLOG -DJCGO_SYSWCHAR \
    -DJCGO_SYSDUALW -fno-optimize-sibling-calls -include wchar.h \
    -I ../../include -c ../../native/*.c)

# Build "trjnic" dynamic library (to enable TraceJni utility on Windows):
mkdir -p auxbin/$ARCH/$BASESYS
$CC -O2 -Wall -Wextra -DJCGO_WIN32 \
    -I $JAVA_HOME/include/$BASESYS -I $JAVA_HOME/include \
    -D Java_com_ivmaisoft_jcgorefl_TraceJni_initIntercept=_Java_com_ivmaisoft_jcgorefl_TraceJni_initIntercept \
    -shared -o auxbin/$ARCH/$BASESYS/trjnic.dll -s reflgen/trjnic.c

# Translate GenRefl, JPropJav, jcgo Java code to C code if not yet:
if [ ! -d ".build_tmp/jtr/jcgo_Out" ]; then
    mkjcgo/build-java.sh
fi

# Build GenRefl.exe, JPropJav.exe:
$CC -I include -I native -Os -fwrapv -fno-strict-aliasing -DJCGO_FFDATA \
    -DJCGO_NOGC -DJCGO_NOJNI -DJCGO_NOSEGV -DJNIIMPORT=static/**/inline \
    -DJNIEXPORT=JNIIMPORT -DJNUBIGEXPORT=static -DJCGO_NOFP -DJCGO_USELONG \
    -DJCGO_NOTIME -fno-optimize-sibling-calls -s \
    -o auxbin/$ARCH/$BASESYS/GenRefl.exe .build_tmp/genrefl/jcgo_Out/Main.c
$CC -I include -I native -Os -fwrapv -fno-strict-aliasing -DJCGO_FFDATA \
    -DJCGO_NOGC -DJCGO_NOJNI -DJCGO_NOSEGV -DJNIIMPORT=static/**/inline \
    -DJNIEXPORT=JNIIMPORT -DJNUBIGEXPORT=static -DJCGO_NOFP -DJCGO_USELONG \
    -DJCGO_NOTIME -fno-optimize-sibling-calls -s \
    -o auxbin/$ARCH/$BASESYS/JPropJav.exe .build_tmp/jpropjav/jcgo_Out/Main.c

# Build jcgo.exe:
$CC -I include -I include/boehmgc -I native -O2 -fwrapv -fno-strict-aliasing \
    -DJCGO_FFDATA -DJCGO_USEGCJ -DJCGO_NOJNI -DJCGO_NOSEGV \
    -DEXTRASTATIC=static -DJNIIMPORT=static/**/inline -DJNIEXPORT=JNIIMPORT \
    -DJNUBIGEXPORT=static -DGCSTATICDATA= -DJCGO_GCRESETDLS \
    -DGC_INITIAL_HEAP_SIZE=16*1024*1024 -DGC_FREE_SPACE_DIVISOR=7 \
    -DJCGO_NOFP -fno-optimize-sibling-calls -s -o jcgo.exe \
    .build_tmp/jtr/jcgo_Out/Main.c libs/$ARCH/$SYST/libgc.a
