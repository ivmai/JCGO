#!/bin/sh
set -ex

# Compile JCGO Java source files explicitly for self-testing.
#
# Prerequisites:
# * Oracle JDK 1.6.0
# * curl ftp://ftp.gnu.org/gnu/classpath/classpath-0.93.tar.gz | tar zxf -

# Default environment variables:
if [ -z "$X_PREFER_SOURCE" ]; then
    X_PREFER_SOURCE=-Xprefer:source
fi
if [ -z "$PATHSEP" ]; then
# Assuming Cygwin:
    PATHSEP=";"
fi

# Set current working directory to JCGO root:
cd $(dirname "$0")/..

# Test simple examples source:
mkdir -p .build_tmp/test-examples-simple
javac -d .build_tmp/test-examples-simple examples/simple/*.java
# Note: ignore warning about deprecated API usage.

# Test clsp_pgk/pqt/res, noopmain source:
mkdir -p .build_tmp/test-clsp_pgk .build_tmp/test-clsp_pqt .build_tmp/test-clsp_res
javac -d .build_tmp/test-clsp_pgk -source 1.3 goclsp/clsp_pgk/gnu/classpath/*
javac -d .build_tmp/test-clsp_pqt -source 1.3 goclsp/clsp_pqt/gnu/classpath/*
(cd goclsp/clsp_res; javac -d ../../.build_tmp/test-clsp_res -source 1.3 gnu/classpath/* \
    gnu/java/locale/* gnu/java/security/*)
mkdir -p .build_tmp/test-noopmain
javac -d .build_tmp/test-noopmain -source 1.3 goclsp/noopmain/*

# Test classpath-0.93 bootclasspath classes source:
CLSP_BOOT_RELPATH=.build_tmp/classpath-0.93-bootclasspath
mkdir -p $CLSP_BOOT_RELPATH
javac $X_PREFER_SOURCE -d $CLSP_BOOT_RELPATH -source 1.4 \
    -sourcepath "classpath-0.93${PATHSEP}classpath-0.93/external/relaxngDatatype${PATHSEP}classpath-0.93/external/sax${PATHSEP}classpath-0.93/external/w3c_dom${PATHSEP}classpath-0.93/vm/reference${PATHSEP}goclsp/clsp_res" \
    classpath-0.93/java/lang/*.java classpath-0.93/javax/crypto/CipherSpi.java \
    classpath-0.93/javax/swing/JTable.java classpath-0.93/gnu/java/awt/peer/gtk/GtkToolkit.java
# Note: ignore warning about deprecated API usage.

# Test clsp_ldr, fpvm, vm_str source:
mkdir -p .build_tmp/test-clsp_ldr .build_tmp/test-fpvm .build_tmp/test-vm_str
javac $X_PREFER_SOURCE -d .build_tmp/test-clsp_ldr -source 1.3 \
    -bootclasspath $CLSP_BOOT_RELPATH goclsp/clsp_ldr/java/lang/* \
    goclsp/clsp_ldr/java/security/*
javac -d .build_tmp/test-fpvm -source 1.3 -bootclasspath $CLSP_BOOT_RELPATH \
    goclsp/fpvm/java/lang/*
javac -d .build_tmp/test-vm_str -source 1.3 -bootclasspath $CLSP_BOOT_RELPATH \
    goclsp/vm_str/java/lang/*

# Test goclsp/vm source:
mkdir -p .build_tmp/test-vm
(cd goclsp/vm; javac $X_PREFER_SOURCE -d ../../.build_tmp/test-vm -source 1.4 \
    -bootclasspath ../../$CLSP_BOOT_RELPATH -sourcepath ../../classpath-0.93 \
    gnu/classpath/*.j* gnu/classpath/jdwp/* gnu/java/lang/*.j* gnu/java/lang/management/* \
    gnu/java/net/* gnu/java/nio/*.j* gnu/java/nio/charset/iconv/* java/io/* java/lang/*.j* \
    java/lang/management/* java/lang/ref/* java/lang/reflect/* java/net/* java/nio/*.j* \
    java/nio/channels/* java/security/* java/util/* sun/misc/* sun/reflect/*.j* \
    sun/reflect/misc/*)

# Test clsp_fix source:
mkdir -p .build_tmp/test-clsp_fix .build_tmp/test-clsp_fix2
(cd goclsp/clsp_fix; javac $X_PREFER_SOURCE -d ../../.build_tmp/test-clsp_fix -source 1.4 \
    -bootclasspath $CLSP_BOOT_RELPATH \
    -sourcepath "../../classpath-0.93${PATHSEP}../../classpath-0.93/external/sax${PATHSEP}../../classpath-0.93/external/w3c_dom${PATHSEP}../../classpath-0.93/vm/reference${PATHSEP}../clsp_res" \
    gnu/classpath/*.j* gnu/classpath/tools/common/* gnu/classpath/tools/getopt/* \
    gnu/java/awt/peer/gtk/* gnu/java/awt/peer/headless/* gnu/java/awt/peer/qt/* gnu/java/io/* \
    gnu/java/net/*.j* gnu/java/net/loader/* gnu/java/net/protocol/file/* \
    gnu/java/net/protocol/http/* gnu/java/nio/*.j* gnu/java/nio/charset/* \
    gnu/java/security/*.j* gnu/java/security/der/* gnu/java/security/hash/* \
    gnu/java/security/jce/sig/* gnu/java/security/key/dss/* gnu/java/security/key/rsa/* \
    gnu/java/security/pkcs/* gnu/java/security/provider/* gnu/java/security/sig/rsa/* \
    gnu/java/security/x509/*.j* gnu/java/security/x509/ext/* gnu/java/util/*.j* \
    gnu/java/util/jar/* gnu/java/util/prefs/*; \
    javac $X_PREFER_SOURCE -d ../../.build_tmp/test-clsp_fix2 -source 1.4 \
    -bootclasspath $CLSP_BOOT_RELPATH \
    -sourcepath "../../classpath-0.93${PATHSEP}../../classpath-0.93/external/relaxngDatatype${PATHSEP}../../classpath-0.93/external/sax${PATHSEP}../../classpath-0.93/external/w3c_dom${PATHSEP}../../classpath-0.93/vm/reference${PATHSEP}../clsp_res" \
    ../vm/java/lang/VMDouble.java ../vm/java/lang/VMFloat.java \
    gnu/javax/crypto/key/dh/* gnu/javax/crypto/key/srp6/* gnu/javax/crypto/prng/* \
    gnu/javax/net/ssl/provider/* gnu/javax/print/* gnu/javax/security/auth/login/* \
    gnu/javax/swing/plaf/gnu/* gnu/xml/dom/* gnu/xml/validation/relaxng/* \
    gnu/xml/validation/xmlschema/* java/applet/* java/io/* java/lang/*.j* java/lang/ref/* \
    java/lang/reflect/* java/net/* java/nio/*.j* java/nio/channels/* java/nio/charset/* \
    java/security/* java/sql/* java/text/* java/util/*.j* java/util/logging/* \
    java/util/prefs/* java/util/regex/* java/util/zip/* javax/print/*.j* \
    javax/print/attribute/standard/* javax/sound/midi/* javax/sound/sampled/*.j* \
    javax/sound/sampled/spi/* javax/swing/*.j* javax/swing/colorchooser/* \
    javax/swing/filechooser/* javax/swing/plaf/basic/* javax/swing/plaf/metal/* \
    javax/swing/text/*.j* javax/swing/text/html/* javax/swing/tree/*)

# Test clsp_asc source:
mkdir -p .build_tmp/test-clsp_asc
javac $X_PREFER_SOURCE -d .build_tmp/test-clsp_asc -source 1.4 \
    -bootclasspath $CLSP_BOOT_RELPATH -sourcepath "goclsp/vm${PATHSEP}classpath-0.93" \
    goclsp/clsp_asc/java/io/*

if [ ! -d "rflg_out" ]; then
    # Testing of 'rflg_out' content skipped since build-java.sh not executed.
    exit 0
fi

mkdir -p .build_tmp/test-rflg_out
(cd rflg_out; javac -d ../.build_tmp/test-rflg_out -source 1.3 \
    -sourcepath ../miscsrc/jpropjav gnu/classpath/tools/appletviewer/* \
    gnu/classpath/tools/common/* gnu/classpath/tools/getopt/* gnu/io/* \
    gnu/java/awt/dnd/peer/gtk/* gnu/java/awt/peer/gtk/* gnu/java/awt/peer/qt/* \
    gnu/java/awt/peer/wce/*.j* gnu/java/awt/peer/wce/font/* gnu/java/locale/* \
    gnu/java/net/local/* gnu/java/nio/charset/iconv/* gnu/java/util/prefs/gconf/* \
    gnu/java/util/regex/* gnu/javax/comm/wce/* gnu/javax/print/* \
    gnu/javax/security/auth/callback/* gnu/javax/sound/midi/alsa/* \
    gnu/javax/sound/sampled/wce/* gnu/xml/libxmlj/dom/* gnu/xml/libxmlj/sax/* \
    gnu/xml/libxmlj/transform/* java/util/* javax/imageio/plugins/jpeg/* \
    org/eclipse/swt/internal/*.j* org/eclipse/swt/internal/accessibility/gtk/* \
    org/eclipse/swt/internal/cairo/* org/eclipse/swt/internal/carbon/* \
    org/eclipse/swt/internal/cde/* org/eclipse/swt/internal/cocoa/* \
    org/eclipse/swt/internal/gdip/* org/eclipse/swt/internal/gnome/* \
    org/eclipse/swt/internal/gtk/* org/eclipse/swt/internal/image/* \
    org/eclipse/swt/internal/motif/* org/eclipse/swt/internal/mozilla/*.j* \
    org/eclipse/swt/internal/mozilla/init/* org/eclipse/swt/internal/ole/win32/* \
    org/eclipse/swt/internal/opengl/glx/* org/eclipse/swt/internal/opengl/win32/* \
    org/eclipse/swt/internal/photon/* org/eclipse/swt/internal/win32/* \
    org/eclipse/swt/internal/wpf/* org/ietf/jgss/*)
