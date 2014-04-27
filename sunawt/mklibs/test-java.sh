#!/bin/sh
set -ex

# Compile JCGO-SUNAWT Java source files explicitly for self-testing.
#
# Prerequisites:
# * Oracle JDK 1.4.2_19 (not 1.6+)
# * unzip j2sdk-1_4_2-src-scsl.zip -d <JCGO>/contrib/j2sdk-1_4_2-src-scsl

SCSL_SRC_RELPATH=contrib/j2sdk-1_4_2-src-scsl/j2se/src

# Set current working directory to JCGO root:
cd $(dirname "$0")/../..

# Test sunawt/fix:
mkdir -p .build_tmp/test-sunawt-fix
(cd sunawt/fix; javac -d ../../.build_tmp/test-sunawt-fix -source 1.3 \
    com/sun/java/swing/plaf/gtk/* com/sun/java/swing/plaf/motif/* \
    com/sun/java/swing/plaf/windows/* java/applet/* java/awt/* javax/accessibility/* \
    javax/imageio/* javax/print/* javax/swing/*.j* javax/swing/colorchooser/* \
    javax/swing/filechooser/* javax/swing/plaf/basic/* javax/swing/plaf/multi/* \
    javax/swing/text/html/* sun/applet/* sun/awt/*.j* sun/awt/datatransfer/* sun/awt/dnd/* \
    sun/awt/font/* sun/awt/im/* sun/awt/image/* sun/awt/shell/* sun/dc/pr/* sun/java2d/*.j* \
    sun/java2d/loops/* sun/java2d/pipe/* sun/net/www/* sun/print/* sun/reflect/misc/*)
# Note: ignore warning about deprecated API usage.

# Test sunawt/fix_snd:
mkdir -p .build_tmp/test-sunawt-fix_snd
javac -d .build_tmp/test-sunawt-fix_snd -source 1.3 sunawt/fix_snd/com/sun/media/sound/* \
    sunawt/fix_snd/javax/sound/midi/* sunawt/fix_snd/javax/sound/sampled/*

# Test sunawt/fix_sql:
mkdir -p .build_tmp/test-sunawt-fix_sql
javac -d .build_tmp/test-sunawt-fix_sql -source 1.3 sunawt/fix_sql/java/sql/* \
    sunawt/fix_sql/sun/jdbc/odbc/*

# Test sunawt/fix_win:
mkdir -p .build_tmp/test-sunawt-fix_win
(cd sunawt/fix_win; javac -d ../../.build_tmp/test-sunawt-fix_win -source 1.3 \
    -sourcepath ../../$SCSL_SRC_RELPATH/windows/classes \
    ../../$SCSL_SRC_RELPATH/share/classes/sun/misc/Cache.java \
    java/awt/*.j* java/awt/print/* javax/print/* javax/swing/* sun/awt/*.j* sun/awt/shell/* \
    sun/awt/windows/* sun/print/*)
# Note: ignore warning about deprecated API usage.

# Test sunawt/fix_x11:
mkdir -p .build_tmp/test-sunawt-fix_x11
(cd sunawt/fix_x11; javac -d ../../.build_tmp/test-sunawt-fix_x11 -source 1.4 \
    -sourcepath ../../$SCSL_SRC_RELPATH/solaris/classes \
    com/sun/java/swing/plaf/windows/* java/awt/*.j* java/awt/print/* javax/print/* \
    javax/swing/* sun/awt/*.j* sun/awt/image/* sun/awt/motif/* sun/awt/print/* sun/print/*)
# Note: ignore warning about deprecated API usage.

if [ ! -d "sawt_out/rflg_out" ]; then
    # Testing of 'sawt_out' content skipped since build-java.sh not executed.
    exit 0
fi

mkdir -p .build_tmp/test-sunawt-rflg_com
javac -d .build_tmp/test-sunawt-rflg_com -source 1.3 sawt_out/rflg_com/com/sun/comm/*

mkdir -p .build_tmp/test-sunawt-rflg_out
(cd sawt_out/rflg_out; javac -d ../../.build_tmp/test-sunawt-rflg_out -source 1.3 \
    -sourcepath ../../miscsrc/jpropjav com/sun/accessibility/internal/resources/* \
    com/sun/imageio/plugins/jpeg/* com/sun/inputmethods/internal/indicim/resources/* \
    com/sun/inputmethods/internal/thaiim/resources/* com/sun/java/swing/plaf/windows/* \
    com/sun/swing/internal/plaf/basic/resources/* \
    com/sun/swing/internal/plaf/metal/resources/* java/awt/*.j* java/awt/event/* \
    java/awt/image/* sun/awt/*.j* sun/awt/color/* sun/awt/datatransfer/* sun/awt/font/* \
    sun/awt/image/*.j* sun/awt/image/codec/* sun/awt/motif/* sun/awt/print/*.j* \
    sun/awt/print/resources/* sun/awt/resources/* sun/awt/shell/* sun/awt/tiny/* \
    sun/awt/windows/* sun/dc/pr/* sun/java2d/*.j* sun/java2d/loops/* sun/java2d/pipe/* \
    sun/print/*.j* sun/print/resources/*)

mkdir -p .build_tmp/test-sunawt-rflg_snd
javac -d .build_tmp/test-sunawt-rflg_snd -source 1.3 sawt_out/rflg_snd/com/sun/media/sound/*
