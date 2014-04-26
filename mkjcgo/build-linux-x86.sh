#!/bin/sh
set -ex

# Build JCGO binaries for Linux/x86.
#
# Prerequisites:
# * GCC 4.4.0+ (linux/x86)
# * (cd contrib; curl http://www.hboehm.info/gc/gc_source/gc-7.4.0.tar.gz | tar zxf -; mv gc-7.4.0 bdwgc)
# * (cd contrib/bdwgc; curl http://www.hboehm.info/gc/gc_source/libatomic_ops-7.4.0.tar.gz | tar zxf -; mv libatomic_ops-7.4.0 libatomic_ops)
# * (cd contrib; tar zxf tinygc-2_6.tar.bz2)
# * curl ftp://ftp.gnu.org/gnu/classpath/classpath-0.93.tar.gz | tar zxf -
# * apt-get install patch libx11-dev libgtk2.0-dev libxtst-dev

AR=ar
CC="gcc -m32"
ARCH=x86
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
    -o dlls/$ARCH/$SYST/libgc.so -s contrib/bdwgc/extra/gc.c \
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

# Build TinyGC static libraries (single- and multi-threaded):
$CC -O2 -Wall -DGC_FASTCALL= -DALL_INTERIOR_POINTERS -DGC_GCJ_SUPPORT \
    -c -o libs/$ARCH/$SYST/tinygc.o contrib/tinygc/tinygc.c
$CC -O2 -Wall -D_REENTRANT -DGC_FASTCALL= -DALL_INTERIOR_POINTERS \
    -DGC_GCJ_SUPPORT -DGC_CLIBDECL= -DGC_THREADS -c \
    -o libs/$ARCH/$SYST/tinygcmt.o contrib/tinygc/tinygc.c

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
    -D_LARGEFILE64_SOURCE -DJCGO_LARGEFILE \
    -DJCGO_ERRTOLOG -I ../../include -c ../../native/*.c)

# Build JAWT stub library (for SWT):
mkdir -p dlls/$ARCH/$SYST/jawtstub
$CC -Os -Wall -I classpath-0.93/include -shared -fPIC \
    -o dlls/$ARCH/$SYST/jawtstub/libjawt.so -s miscsrc/jawtstub/jawt.c

# Translate GenRefl, JPropJav, jcgo Java code to C code if not yet:
if [ ! -d ".build_tmp/jtr/jcgo_Out" ]; then
    mkjcgo/build-java.sh
fi

# Build GenRefl, JPropJav native binaries:
$CC -I include -I native -Os -fwrapv -fno-strict-aliasing \
    -DJCGO_INTFIT -DJCGO_UNIX -DJCGO_UNIFSYS -DJCGO_NOGC -DJCGO_NOJNI \
    -DJCGO_NOSEGV -DJNIIMPORT=static/**/inline -DJNIEXPORT=JNIIMPORT \
    -DJNUBIGEXPORT=static -DJCGO_NOFP -DJCGO_USELONG -DJCGO_NOTIME \
    -s -o auxbin/$ARCH/$SYST/GenRefl .build_tmp/genrefl/jcgo_Out/Main.c
$CC -I include -I native -Os -fwrapv -fno-strict-aliasing \
    -DJCGO_INTFIT -DJCGO_UNIX -DJCGO_UNIFSYS -DJCGO_NOGC -DJCGO_NOJNI \
    -DJCGO_NOSEGV -DJNIIMPORT=static/**/inline -DJNIEXPORT=JNIIMPORT \
    -DJNUBIGEXPORT=static -DJCGO_NOFP -DJCGO_USELONG -DJCGO_NOTIME \
    -s -o auxbin/$ARCH/$SYST/JPropJav .build_tmp/jpropjav/jcgo_Out/Main.c

# Build jcgo itself Linux native binary:
$CC -I include -I include/boehmgc -I native -O2 -fwrapv \
    -fno-strict-aliasing -DJCGO_INTFIT -DJCGO_UNIX -DJCGO_UNIFSYS \
    -DJCGO_USEGCJ -DJCGO_NOJNI -DJCGO_NOSEGV -DEXTRASTATIC=static \
    -DJNIIMPORT=static/**/inline -DJNIEXPORT=JNIIMPORT -DJNUBIGEXPORT=static \
    -DGCSTATICDATA= -DJCGO_GCRESETDLS -DGC_FREE_SPACE_DIVISOR=7 -DJCGO_NOFP \
    -s -o jcgo .build_tmp/jtr/jcgo_Out/Main.c libs/$ARCH/$SYST/libgc.a

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
