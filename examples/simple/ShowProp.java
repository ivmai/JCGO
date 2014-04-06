
public final class ShowProp
{

 public static void main(String[] args)
 {
  for (int i = 0; i < args.length; i++)
   System.out.println("Command-line argument " + (i + 1) + ": " + args[i]);
  System.out.println();
  System.out.println("availableProcessors(): " +
   Runtime.getRuntime().availableProcessors());
  printProp("os.name");
  printProp("os.version");
  printProp("os.arch");
  printProp("sun.arch.data.model");
  printProp("sun.cpu.endian");
  printProp("sun.io.unicode.encoding");
  System.out.println();
  printProp("java.version");
  printProp("java.vm.info");
  printProp("java.vm.name");
  printProp("java.vm.version");
  printProp("java.class.version");
  printProp("gnu.classpath.version");
  System.out.println();
  printProp("user.name");
  printProp("user.language");
  if (!printProp("user.region"))
   printProp("user.country");
  printProp("user.variant");
  printProp("file.encoding");
  System.out.println();
  printProp("user.dir");
  printProp("user.home");
  printProp("java.home");
  printProp("sun.boot.class.path");
  printProp("java.class.path");
  printProp("sun.boot.library.path");
  printProp("java.library.path");
  printProp("java.ext.dirs");
  printProp("java.io.tmpdir");
  printProp("gnu.classpath.home");
 }

 private static boolean printProp(String name)
 {
  String value = System.getProperty(name);
  if (value == null)
   return false;
  System.out.println(name + ": " + value);
  return true;
 }
}
