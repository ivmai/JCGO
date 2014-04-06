#!/bin/sh
set -ex

# Compile rarely used C source files explicitly for self-testing.
# Target platform: Win32 (Cygwin)
#
# Prerequisites:
# * Cygwin v2.8+ (GCC 4.8.2)
# * wget ftp://ftp.gnu.org/gnu/classpath/classpath-0.93.tar.gz | tar xf -

# Set current working directory to JCGO root:
cd $(dirname "$0")/..

# Test jnidload:
mkdir -p .build_tmp/test-jnidload-cygwin
gcc -Os -Wall -I classpath-0.93/include -DJNIDLOAD_EXPNAME=JNI_OnLoad_test \
    -DJNIDLOAD_QLIBNAME=\"jnidload-test.dll\" -shared \
    -o .build_tmp/test-jnidload-cygwin/jnidload.dll jnidload/jnidload.c

# Test miscsrc (jawtstub, tpthread, winmain):
mkdir -p .build_tmp/test-miscsrc-cygwin/inc/X11
echo "typedef int Drawable; typedef int Display; typedef int VisualID; typedef int Colormap;" \
    > .build_tmp/test-miscsrc-cygwin/inc/X11/Xlib.h
gcc -Os -Wall -I classpath-0.93/include -I .build_tmp/test-miscsrc-cygwin/inc -shared \
    -o .build_tmp/test-miscsrc-cygwin/jawt.dll miscsrc/jawtstub/jawt.c
gcc -Os -Wall -Wextra -I minihdr/unix -I minihdr/common -DPTHREAD_USE_GETTIMEOFDAY \
    -DPTHREAD_NO_SIGSET -DPTHREAD_CPUSTATE_SPOFF=16 -c \
    -o .build_tmp/test-miscsrc-cygwin/tpthread.o miscsrc/tpthread/tpthread.c
gcc -Os -Wall -DWINMAIN_PARSECMDLINE -DWINMAIN_EMPTYENV -DWINMAIN_NOSTOREARGV \
    -DWINMAIN_CALLEXIT -c -o .build_tmp/test-miscsrc-cygwin/winmain.o miscsrc/winmain/winmain.c

# Test minihdr common/unix compilation:
mkdir -p .build_tmp/test-minihdr-unix-cygwin
(cd .build_tmp/test-minihdr-unix-cygwin; gcc -Os -Wall -Wextra -x c -I ../../minihdr/unix \
    -I ../../minihdr/common -c ../../minihdr/common/*.h ../../minihdr/common/sys/*.h \
    ../../minihdr/unix/*.h ../../minihdr/unix/netinet/*.h ../../minihdr/unix/sys/*.h)

# Test minihdr common/dos compilation:
mkdir -p .build_tmp/test-minihdr-dos-cygwin
(cd .build_tmp/test-minihdr-dos-cygwin; gcc -Os -Wall -Wextra -x c -I ../../minihdr/dos \
    -I ../../minihdr/common -c ../../minihdr/common/*.h ../../minihdr/common/sys/*.h \
    ../../minihdr/dos/*.h ../../minihdr/dos/sys/*.h)

# Test minihdr-win compilation:
mkdir -p .build_tmp/test-minihdr-win-cygwin
(cd .build_tmp/test-minihdr-win-cygwin; gcc -Os -Wall -Wextra -x c -I ../../minihdr/win \
    -I ../../minihdr/common -c ../../minihdr/win/*.h)
