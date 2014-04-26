#!/bin/sh
set -ex

# Build jcgo.jar and "auxbin" .jar files; generate Java source files of "rflg_out".
#
# Prerequisites:
# * Oracle JDK 1.6.0 (or 1.4.2_19)
# * curl ftp://ftp.gnu.org/gnu/classpath/classpath-0.93.tar.gz | tar zxf -
# * mkdir -p contrib/swt
# * unzip swt-3.8-win32-win32-x86.zip src.zip -d contrib/swt/swt-win32-win32-x86
# * (cd contrib/swt/swt-win32-win32-x86; unzip src.zip -d src)

SWT_SRC_RELPATH=contrib/swt/swt-win32-win32-x86/src

# Set current working directory to JCGO root:
cd $(dirname "$0")/..

# jcgo.jar:
mkdir -p .build_tmp/jtr/bin
echo "Main-Class: com.ivmaisoft.jcgo.Main" > .build_tmp/jtr/MANIFEST.MF
javac -d .build_tmp/jtr/bin -source 1.3 -target 1.3 jtrsrc/com/ivmaisoft/jcgo/*.java
jar cfm jcgo.jar .build_tmp/jtr/MANIFEST.MF -C .build_tmp/jtr/bin com

# auxbin/jre/GenRefl.jar:
GENREFL_JAR=auxbin/jre/GenRefl.jar
mkdir -p .build_tmp/genrefl/bin auxbin/jre
echo "Main-Class: com.ivmaisoft.jcgorefl.GenRefl" > .build_tmp/genrefl/MANIFEST.MF
javac -d .build_tmp/genrefl/bin -source 1.3 -target 1.3 \
    reflgen/com/ivmaisoft/jcgorefl/GenRefl.java
jar cfm $GENREFL_JAR .build_tmp/genrefl/MANIFEST.MF -C .build_tmp/genrefl/bin com

# auxbin/jre/JPropJav.jar:
JPROPJAV_JAR=auxbin/jre/JPropJav.jar
mkdir -p .build_tmp/jpropjav/bin auxbin/jre
echo "Main-Class: com.ivmaisoft.jpropjav.Main" > .build_tmp/jpropjav/MANIFEST.MF
javac -d .build_tmp/jpropjav/bin -source 1.3 -target 1.3 \
    miscsrc/jpropjav/com/ivmaisoft/jpropjav/*.java
# Note: ignore warning about deprecated API usage.
jar cfm $JPROPJAV_JAR .build_tmp/jpropjav/MANIFEST.MF -C .build_tmp/jpropjav/bin com

# auxbin/jre/TraceJni.jar:
mkdir -p .build_tmp/tracejni/bin auxbin/jre
echo "Main-Class: com.ivmaisoft.jcgorefl.TraceJni" > .build_tmp/tracejni/MANIFEST.MF
javac -d .build_tmp/tracejni/bin -source 1.3 -target 1.3 \
    reflgen/com/ivmaisoft/jcgorefl/TraceJni.java
jar cfm auxbin/jre/TraceJni.jar .build_tmp/tracejni/MANIFEST.MF -C .build_tmp/tracejni/bin com

# rflg_out (reflection info generation):
mkdir -p rflg_out
java -Dline.separator=$'\n' -jar $GENREFL_JAR -d rflg_out reflgen/*.dat

# rflg_out (convert property files to Java source):
java -Dline.separator=$'\n' -jar $JPROPJAV_JAR -d rflg_out \
    -sourcepath goclsp/clsp_fix/resource -sourcepath classpath-0.93/resource \
    -sourcepath $SWT_SRC_RELPATH @goclsp/clsp_res/jreslist.in

# Translated C code for GenRefl:
mkdir -p .build_tmp/genrefl/jcgo_Out
java -jar jcgo.jar -d .build_tmp/genrefl/jcgo_Out -src reflgen -src goclsp/clsp_asc \
    com.ivmaisoft.jcgorefl.GenRefl @stdpaths.in

# Translated C code for JPropJav:
mkdir -p .build_tmp/jpropjav/jcgo_Out
java -jar jcgo.jar -d .build_tmp/jpropjav/jcgo_Out -src miscsrc/jpropjav -src goclsp/clsp_asc \
    com.ivmaisoft.jpropjav.Main @stdpaths.in

# Translated C code for jcgo native binary:
mkdir -p .build_tmp/jtr/jcgo_Out
java -Xss1M -jar jcgo.jar -d .build_tmp/jtr/jcgo_Out -src jtrsrc -src goclsp/clsp_asc \
    com.ivmaisoft.jcgo.Main @stdpaths.in
