#!/bin/sh
set -ex

# Generate Java source files of "sawt_out".
#
# Prerequisites:
# * Oracle JDK 1.4.2_19 (or 1.6.0+)
# * unzip j2sdk-1_4_2-src-scsl.zip -d <JCGO>/contrib/j2sdk-1_4_2-src-scsl

SCSL_SRC_RELPATH=contrib/j2sdk-1_4_2-src-scsl/j2se/src

# Set current working directory to JCGO root:
cd $(dirname "$0")/../..

if [ ! -d "auxbin/jre" ]; then
    mkjcgo/build-java.sh
fi

# sawt_out/rflg_out (reflection info generation):
GENREFL_JAR=auxbin/jre/GenRefl.jar
mkdir -p sawt_out/rflg_out
java -Dline.separator=$'\n' -jar $GENREFL_JAR -d sawt_out/rflg_out sunawt/csrc/jre14awt.dat

# sawt_out/rflg_snd:
mkdir -p sawt_out/rflg_snd
java -Dline.separator=$'\n' -jar $GENREFL_JAR -d sawt_out/rflg_snd sunawt/csrc/jre14snd.dat

# sawt_out/rflg_com:
mkdir -p sawt_out/rflg_com
java -Dline.separator=$'\n' -jar $GENREFL_JAR -d sawt_out/rflg_com sunawt/csrc/suncomm.dat

# sawt_out/rflg_out (convert property files to Java source):
java -Dline.separator=$'\n' -jar auxbin/jre/JPropJav.jar -d sawt_out/rflg_out \
    -sourcepath $SCSL_SRC_RELPATH/share/classes -sourcepath $SCSL_SRC_RELPATH/windows/classes \
    @sunawt/csrc/awtrescl.in
