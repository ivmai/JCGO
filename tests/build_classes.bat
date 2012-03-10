rem Compile all Java class source files present in JCGO.

rem Assume Sun JDK v1.6+.
rem Assume current working dir is JCGO root.

set OUT_PATH=C:\Temp\jcgo-test-classes
mkdir %OUT_PATH%

cd classpath-0.93
javac -Xprefer:source -d %OUT_PATH% -source 1.4 -sourcepath .;external\relaxngDatatype;external\sax;external\w3c_dom;vm\reference;..\goclsp\clsp_res java\lang\*.java javax\crypto\CipherSpi.java javax\swing\JTable.java gnu\java\awt\peer\gtk\GtkToolkit.java
if errorlevel 1 goto exit

cd ..\goclsp\clsp_ldr
javac -Xprefer:source -d %OUT_PATH% -source 1.3 -bootclasspath %OUT_PATH% java\lang\* java\security\*
if errorlevel 1 goto exit

cd ..\clsp_pgk
javac -d %OUT_PATH% -source 1.3 gnu\classpath\*
if errorlevel 1 goto exit

cd ..\clsp_pqt
javac -d %OUT_PATH% -source 1.3 gnu\classpath\*
if errorlevel 1 goto exit

cd ..\clsp_res
javac -d %OUT_PATH% -source 1.3 gnu\classpath\* gnu\java\locale\* gnu\java\security\*
if errorlevel 1 goto exit

cd ..\fpvm
javac -d %OUT_PATH% -source 1.3 -bootclasspath %OUT_PATH% java\lang\*
if errorlevel 1 goto exit

cd ..\noopmain
javac -d %OUT_PATH% -source 1.3 *.j*
if errorlevel 1 goto exit

cd ..\vm_str
javac -d %OUT_PATH% -source 1.3 -bootclasspath %OUT_PATH% java\lang\*
if errorlevel 1 goto exit

cd ..\vm
javac -Xprefer:source -d %OUT_PATH% -source 1.4 -bootclasspath %OUT_PATH% -sourcepath ..\..\classpath-0.93 gnu\classpath\*.j* gnu\classpath\jdwp\* gnu\java\lang\*.j* gnu\java\lang\management\* gnu\java\net\* gnu\java\nio\*.j* gnu\java\nio\charset\iconv\* java\io\* java\lang\*.j* java\lang\management\* java\lang\ref\* java\lang\reflect\* java\net\* java\nio\*.j* java\nio\channels\* java\security\* java\util\* sun\misc\* sun\reflect\*.j* sun\reflect\misc\*
if errorlevel 1 goto exit

cd ..\clsp_fix
javac -Xprefer:source -d %OUT_PATH% -source 1.4 -bootclasspath %OUT_PATH% -sourcepath ..\..\classpath-0.93 gnu\classpath\*.j* gnu\classpath\tools\common\* gnu\classpath\tools\getopt\* gnu\java\awt\peer\gtk\* gnu\java\awt\peer\headless\* gnu\java\awt\peer\qt\* gnu\java\io\* gnu\java\net\*.j* gnu\java\net\loader\* gnu\java\net\protocol\file\* gnu\java\net\protocol\http\* gnu\java\nio\*.j* gnu\java\nio\charset\* gnu\java\security\*.j* gnu\java\security\der\* gnu\java\security\hash\* gnu\java\security\jce\sig\* gnu\java\security\key\dss\* gnu\java\security\key\rsa\* gnu\java\security\pkcs\* gnu\java\security\provider\* gnu\java\security\sig\rsa\* gnu\java\security\x509\*.j* gnu\java\security\x509\ext\* gnu\java\util\*.j* gnu\java\util\jar\* gnu\java\util\prefs\*
if errorlevel 1 goto exit
javac -Xprefer:source -d %OUT_PATH% -source 1.4 -bootclasspath %OUT_PATH% -sourcepath ..\..\classpath-0.93;..\..\classpath-0.93\external\relaxngDatatype;..\..\classpath-0.93\external\sax;..\..\classpath-0.93\external\w3c_dom gnu\javax\crypto\key\dh\* gnu\javax\crypto\key\srp6\* gnu\javax\crypto\prng\* gnu\javax\net\ssl\provider\* gnu\javax\print\* gnu\javax\security\auth\login\* gnu\javax\swing\plaf\gnu\* gnu\xml\dom\* gnu\xml\validation\relaxng\* gnu\xml\validation\xmlschema\* java\applet\* java\io\* java\lang\*.j* java\lang\ref\* java\lang\reflect\* java\net\* java\nio\*.j* java\nio\channels\* java\nio\charset\* java\security\* java\sql\* java\text\* java\util\*.j* java\util\logging\* java\util\prefs\* java\util\regex\* java\util\zip\* javax\print\*.j* javax\print\attribute\standard\* javax\sound\midi\* javax\sound\sampled\*.j* javax\sound\sampled\spi\* javax\swing\*.j* javax\swing\colorchooser\* javax\swing\filechooser\* javax\swing\plaf\basic\* javax\swing\plaf\metal\* javax\swing\text\*.j* javax\swing\text\html\* javax\swing\tree\*
if errorlevel 1 goto exit

cd ..\clsp_asc
javac -Xprefer:source -d %OUT_PATH% -source 1.3 -bootclasspath %OUT_PATH% java\io\*
if errorlevel 1 goto exit

cd ..\..\jtrsrc
javac -d %OUT_PATH% -source 1.3 com\ivmaisoft\jcgo\*.java
if errorlevel 1 goto exit

cd ..\miscsrc\jpropjav
javac -d %OUT_PATH% -source 1.3 com\ivmaisoft\jpropjav\*.java
if errorlevel 1 goto exit

cd ..\..\reflgen
javac -d %OUT_PATH% -source 1.3 com\ivmaisoft\jcgorefl\*.java
if errorlevel 1 goto exit

cd ..\rflg_out
javac -d %OUT_PATH% -source 1.3 -sourcepath ..\miscsrc\jpropjav gnu\classpath\tools\appletviewer\* gnu\classpath\tools\common\* gnu\classpath\tools\getopt\* gnu\io\* gnu\java\awt\dnd\peer\gtk\* gnu\java\awt\peer\gtk\* gnu\java\awt\peer\qt\* gnu\java\awt\peer\wce\*.j* gnu\java\awt\peer\wce\font\* gnu\java\locale\* gnu\java\net\local\* gnu\java\nio\charset\iconv\* gnu\java\util\prefs\gconf\* gnu\java\util\regex\* gnu\javax\comm\wce\* gnu\javax\print\* gnu\javax\security\auth\callback\* gnu\javax\sound\midi\alsa\* gnu\javax\sound\sampled\wce\* gnu\xml\libxmlj\dom\* gnu\xml\libxmlj\sax\* gnu\xml\libxmlj\transform\* java\util\* javax\imageio\plugins\jpeg\* org\eclipse\swt\internal\*.j* org\eclipse\swt\internal\accessibility\gtk\* org\eclipse\swt\internal\cairo\* org\eclipse\swt\internal\carbon\* org\eclipse\swt\internal\cde\* org\eclipse\swt\internal\cocoa\* org\eclipse\swt\internal\gdip\* org\eclipse\swt\internal\gnome\* org\eclipse\swt\internal\gtk\* org\eclipse\swt\internal\image\* org\eclipse\swt\internal\motif\* org\eclipse\swt\internal\mozilla\*.j* org\eclipse\swt\internal\mozilla\init\* org\eclipse\swt\internal\ole\win32\* org\eclipse\swt\internal\opengl\glx\* org\eclipse\swt\internal\opengl\win32\* org\eclipse\swt\internal\photon\* org\eclipse\swt\internal\win32\* org\eclipse\swt\internal\wpf\* org\ietf\jgss\*
if errorlevel 1 goto exit

cd ..
echo Done.
:exit
