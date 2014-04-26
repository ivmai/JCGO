#!/bin/sh
set -ex

# Build JCGO binaries for Linux/x86_64.
#
# Prerequisites:
# * GCC 4.4.0+ (linux/x86_64)
# * (cd contrib; curl http://www.hboehm.info/gc/gc_source/gc-7.4.0.tar.gz | tar zxf -; mv gc-7.4.0 bdwgc)
# * (cd contrib/bdwgc; curl http://www.hboehm.info/gc/gc_source/libatomic_ops-7.4.0.tar.gz | tar zxf -; mv libatomic_ops-7.4.0 libatomic_ops)
# * curl ftp://ftp.gnu.org/gnu/classpath/classpath-0.93.tar.gz | tar zxf -
# * apt-get install patch libx11-dev libgtk2.0-dev libxtst-dev

AR=ar
CC="gcc -m64"
ARCH=amd64
SYST=linux

# Set current working directory to JCGO root:
cd $(dirname "$0")/..

# Build BDWGC shared library (multi-threaded):
mkdir -p dlls/$ARCH/$SYST
$CC -O2 -fno-strict-aliasing -Wall -DNO_EXECUTE_PERMISSION \
    -DALL_INTERIOR_POINTERS -DJAVA_FINALIZATION -DGC_GCJ_SUPPORT \
    -DATOMIC_UNCOLLECTABLE -DNO_DEBUGGING -DLARGE_CONFIG -DUSE_MMAP \
    -DUSE_MUNMAP -DGC_THREADS -DTHREAD_LOCAL_ALLOC -DPARALLEL_MARK \
    -I contrib/bdwgc/include -I contrib/bdwgc/libatomic_ops/src \
    -DGC_PTHREAD_START_STANDALONE -DGC_DLL -shared -fPIC \
    -o dlls/$ARCH/$SYST/libgc64.so -s contrib/bdwgc/extra/gc.c \
    contrib/bdwgc/pthread_start.c

# Build BDWGC static libraries (single- and multi-threaded):
mkdir -p libs/$ARCH/$SYST
mkdir -p .build_tmp/libs-gc-$ARCH-$SYST .build_tmp/libs-gcmt-$ARCH-$SYST
(cd .build_tmp/libs-gc-$ARCH-$SYST; $CC -O2 -fno-strict-aliasing -Wall \
    -Wextra -DNO_EXECUTE_PERMISSION -DALL_INTERIOR_POINTERS \
    -DJAVA_FINALIZATION -DGC_GCJ_SUPPORT -DNO_DEBUGGING -DUSE_MMAP \
    -I ../../contrib/bdwgc/include -c ../../contrib/bdwgc/*.c \
    ../../contrib/bdwgc/*.cpp; $AR crus ../../libs/$ARCH/$SYST/libgc.a *.o)
(cd .build_tmp/libs-gcmt-$ARCH-$SYST; $CC -O2 -fno-strict-aliasing \
    -Wall -Wextra -DNO_EXECUTE_PERMISSION -DALL_INTERIOR_POINTERS \
    -DJAVA_FINALIZATION -DGC_GCJ_SUPPORT -DNO_DEBUGGING -DLARGE_CONFIG \
    -DUSE_MMAP -DUSE_MUNMAP -DGC_THREADS -DTHREAD_LOCAL_ALLOC \
    -DPARALLEL_MARK -I ../../contrib/bdwgc/include \
    -I ../../contrib/bdwgc/libatomic_ops/src -c ../../contrib/bdwgc/*.c \
    ../../contrib/bdwgc/*.cpp; $AR crus ../../libs/$ARCH/$SYST/libgcmt.a *.o)

# Build "trjnic" shared library (to enable TraceJni utility on Linux):
mkdir -p auxbin/$ARCH/$SYST
$CC -O2 -Wall -D_REENTRANT -DJCGO_UNIX -DSTATICDATA=static \
    -I classpath-0.93/include -shared -fPIC \
    -o auxbin/$ARCH/$SYST/libtrjnic.so -s reflgen/trjnic.c

# Test compile jcgon:
mkdir -p .build_tmp/test-jcgon-$ARCH-$SYST
(cd .build_tmp/test-jcgon-$ARCH-$SYST; $CC -O2 -fwrapv \
    -fno-strict-aliasing -Wall -D_REENTRANT -DJCGO_INTFIT -DJCGO_UNIX \
    -DJCGO_UNIFSYS -DJCGO_EXEC -DJCGO_UNIPROC -DJCGO_INET -DJCGO_GNUNETDB \
    -DJCGO_ERRTOLOG -I ../../include -c ../../native/*.c)

# Build JAWT stub library (for SWT):
mkdir -p dlls/$ARCH/$SYST/jawtstub
$CC -Os -Wall -I classpath-0.93/include -shared -fPIC \
    -o dlls/$ARCH/$SYST/jawtstub/libjawt.so -s miscsrc/jawtstub/jawt.c

# Build Classpath "gtkpeer" shared libraries (libgtkpeer.so, libjawt.so):
GTK_CFLAGS_LIBS=`pkg-config --cflags --libs "gtk+-2.0 >= 2.8 gthread-2.0 >= 2.2 gdk-pixbuf-2.0"`
FREETYPE2_CFLAGS_LIBS=`pkg-config --cflags --libs "freetype2"`
PANGOFT2_CFLAGS_LIBS=`pkg-config --cflags --libs "pangoft2"`
mkdir -p dlls/$ARCH/$SYST/gtkpeer .build_tmp/gtkpeer-native-fixedsrc-$ARCH
cp classpath-0.93/native/jni/gtk-peer/*.c \
    .build_tmp/gtkpeer-native-fixedsrc-$ARCH/
patch -p4 -d .build_tmp/gtkpeer-native-fixedsrc-$ARCH \
    < goclsp/clsp_fix/native/jni/gtk-peer/classpath-0_93-jni-gtk-peer.diff
$CC -O2 -Wno-deprecated-declarations -DNDEBUG \
    -I classpath-0.93/native/jni/gtk-peer -I classpath-0.93/include \
    -I classpath-0.93/native/jni/classpath -shared -fPIC \
    -o dlls/$ARCH/$SYST/gtkpeer/libgtkpeer.so -s \
    $GTK_CFLAGS_LIBS $FREETYPE2_CFLAGS_LIBS $PANGOFT2_CFLAGS_LIBS \
    .build_tmp/gtkpeer-native-fixedsrc-$ARCH/*.c \
    classpath-0.93/native/jni/classpath/native_state.c \
    goclsp/clsp_fix/native/jni/classpath/jcl.c -lSM -lICE -lXrender -lXrandr \
    -lX11 -lXtst
(cd dlls/$ARCH/$SYST/gtkpeer; $CC -O2 -Wall \
    -I ../../../../classpath-0.93/include \
    -I ../../../../classpath-0.93/native/jni/classpath -L . \
    -shared -fPIC -o libjawt.so -s \
    ../../../../classpath-0.93/native/jawt/jawt.c -lgtkpeer)
