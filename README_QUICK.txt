
JCGO quick start tips (Win32)
-----------------------------

1. Download the core prerequisites:
jcgo-src-1_XX.tar.bz2
jcgo-lib-1_XX.tar.gz
ftp://ftp.gnu.org/gnu/classpath/classpath-0.93.tar.gz

2. Create "C:\JCGO" folder.

3. Unpack all the prerequisites to C:\JCGO (preserving the directory
structure).

4. Try to convert "Hello world" sample to Windows binary (unoptimized):

- create your "projects" folders (e.g., "C:\MyJcgoProjects") and change
the current directory to it;

- type:

C:\JCGO\jcgo.exe -sourcepath $~\examples\simple Hello @$~\stdpaths.in

or, alternatively:

java -jar C:\JCGO\jcgo.jar -sourcepath $~\examples\simple Hello @$~\stdpaths.in

- invoke the MinGW C compiler (GCC):

gcc -IC:\JCGO\include -IC:\JCGO\include\boehmgc -IC:\JCGO\native -DJCGO_FFDATA -o hello jcgo_Out\Main.c C:\JCGO\libs\x86\mingw\libgc.a

- or invoke Visual Studio C/C++ compiler:

cl -IC:\JCGO\include -IC:\JCGO\include\boehmgc -IC:\JCGO\native -DJCGO_INTNN -DJCGO_FFDATA -DJCGO_WIN32 -D_CRT_SECURE_NO_DEPRECATE -D_CRT_NONSTDC_NO_DEPRECATE jcgo_Out\Main.c C:\JCGO\libs\x86\msvc\gc.lib /link /out:hello.exe

- run "hello.exe" file.

5. View "README" file.


JCGO quick start tips for Linux/x86 development host
----------------------------------------------------

1. Download the core prerequisites (mentioned above).

2. Create "/usr/share/JCGO" folder.

3. Unpack all the prerequisites to /usr/share/JCGO (preserving the
directory structure).

4. Try to convert "Hello world" sample to a Linux/x86 binary (unoptimized):

- create your "projects" folders (e.g., "~/MyJcgoProjects") and change
the current directory to it;

- type:

/usr/share/JCGO/jcgo -src $~/examples/simple Hello @$~/stdpaths.in

or, alternatively:

java -jar /usr/share/JCGO/jcgo.jar -src $~/examples/simple Hello @$~/stdpaths.in

- invoke the GNU GCC:

gcc -I /usr/share/JCGO/include -I /usr/share/JCGO/include/boehmgc -I /usr/share/JCGO/native -fwrapv -DJCGO_UNIX -D_IEEEFP_H -DJCGO_UNIFSYS -o hello jcgo_Out/Main.c -lm  /usr/share/JCGO/libs/x86/linux/libgc.a

- run "hello" file.

                          --- [ End of File ] ---
