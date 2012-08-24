rem Compile all Java class source files present in JCGO-SUNAWT.

rem Assume Sun JDK v1.4 (but not higher).
rem Assume current working dir is JCGO root.

set OUT_PATH=C:\Temp\jcgo-test-sunawt-classes
mkdir %OUT_PATH%

cd sunawt\fix
javac -d %OUT_PATH% -source 1.3 com\sun\java\swing\plaf\gtk\* com\sun\java\swing\plaf\motif\* com\sun\java\swing\plaf\windows\* java\applet\* java\awt\* javax\accessibility\* javax\imageio\* javax\print\* javax\swing\*.j* javax\swing\colorchooser\* javax\swing\filechooser\* javax\swing\plaf\basic\* javax\swing\plaf\multi\* javax\swing\text\html\* sun\applet\* sun\awt\*.j* sun\awt\datatransfer\* sun\awt\dnd\* sun\awt\font\* sun\awt\im\* sun\awt\image\* sun\awt\shell\* sun\dc\pr\* sun\java2d\*.j* sun\java2d\loops\* sun\java2d\pipe\* sun\net\www\* sun\print\* sun\reflect\misc\*
if errorlevel 1 goto exit

cd ..\fix_snd
javac -d %OUT_PATH% -source 1.3 com\sun\media\sound\* javax\sound\midi\* javax\sound\sampled\*
if errorlevel 1 goto exit

cd ..\fix_sql
javac -d %OUT_PATH% -source 1.3 java\sql\* sun\jdbc\odbc\*
if errorlevel 1 goto exit

cd ..\fix_win
javac -d %OUT_PATH% -source 1.3 java\awt\*.j* java\awt\print\* javax\print\* javax\swing\* sun\awt\*.j* sun\awt\shell\* sun\awt\windows\* sun\print\*
if errorlevel 1 goto exit

cd ..\fix_x11
javac -d %OUT_PATH% -source 1.4 -sourcepath c:\jcgo\j2sdk-1_4_2-src-scsl\j2se\src\solaris\classes com\sun\java\swing\plaf\windows\* java\awt\*.j* java\awt\print\* javax\print\* javax\swing\* sun\awt\*.j* sun\awt\image\* sun\awt\motif\* sun\awt\print\* sun\print\*
if errorlevel 1 goto exit

cd ..\..\sawt_out\rflg_com
javac -d %OUT_PATH% -source 1.3 com\sun\comm\*
if errorlevel 1 goto exit

cd ..\rflg_out
javac -d %OUT_PATH% -source 1.3 -sourcepath c:\jcgo\miscsrc\jpropjav com\sun\accessibility\internal\resources\* com\sun\imageio\plugins\jpeg\* com\sun\inputmethods\internal\indicim\resources\* com\sun\inputmethods\internal\thaiim\resources\* com\sun\java\swing\plaf\windows\* com\sun\swing\internal\plaf\basic\resources\* com\sun\swing\internal\plaf\metal\resources\* java\awt\*.j* java\awt\event\* java\awt\image\* sun\awt\*.j* sun\awt\color\* sun\awt\datatransfer\* sun\awt\font\* sun\awt\image\*.j* sun\awt\image\codec\* sun\awt\motif\* sun\awt\print\*.j* sun\awt\print\resources\* sun\awt\resources\* sun\awt\shell\* sun\awt\tiny\* sun\awt\windows\* sun\dc\pr\* sun\java2d\*.j* sun\java2d\loops\* sun\java2d\pipe\* sun\print\*.j* sun\print\resources\*
if errorlevel 1 goto exit

cd ..\rflg_snd
javac -d %OUT_PATH% -source 1.3 com\sun\media\sound\*
if errorlevel 1 goto exit

cd ..\..
echo Done.
:exit
