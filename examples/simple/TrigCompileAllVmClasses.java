
// this test should trig inclusion of all VM classes in the compilation set

import java.lang.management.*;

class TrigCompileAllVmClasses implements java.io.Serializable
{
 static void main(String[] args) throws Throwable
 {
  if (args == null)
   return;

  "".intern();
  new java.lang.ref.SoftReference("");
  new java.util.Date();
  new java.io.File("!").deleteOnExit();
  java.nio.ByteBuffer.allocateDirect(1);
  Runtime.getRuntime().exec(""); // throws IndexOutOfBoundsException
  Runtime.runFinalizersOnExit(true);
  Compiler.command("");
  new java.net.Socket("", 0);
  java.lang.reflect.Array.newInstance(byte.class, 0);
  Thread.class.getDeclaredFields();
  java.lang.reflect.Proxy.getProxyClass(Object[][].class.getClassLoader(),
   new Class[] { Runnable.class });

  ManagementFactory.getClassLoadingMXBean().isVerbose();
  ManagementFactory.getCompilationMXBean().getTotalCompilationTime();
  ManagementFactory.getMemoryMXBean().setVerbose(false);
  ManagementFactory.getRuntimeMXBean().getInputArguments();
  ManagementFactory.getThreadMXBean().getThreadCpuTime(0);

  ((GarbageCollectorMXBean)ManagementFactory.getGarbageCollectorMXBeans().
   get(0)).getCollectionCount();
  ((MemoryManagerMXBean)ManagementFactory.getMemoryManagerMXBeans().
   get(0)).isValid();
  ((MemoryPoolMXBean)ManagementFactory.getMemoryPoolMXBeans().
   get(0)).getCollectionUsageThreshold();

  Math.IEEEremainder(0,0);
  Math.acos(0);
  Math.asin(0);
  Math.atan2(0,0);
  Math.atan(0);
  Math.cbrt(0);
  Math.ceil(0);
  Math.cos(0);
  Math.cosh(0);
  Math.exp(0);
  Math.expm1(0);
  Math.floor(0);
  Math.hypot(0,0);
  Math.log10(0);
  Math.log1p(0);
  Math.log(0);
  Math.pow(0,0);
  Math.rint(0);
  Math.sin(0);
  Math.sinh(0);
  Math.sqrt(0);
  Math.tan(0);
  Math.tanh(0);

  Class.forName("java.lang.VMClassLoader$ClassParser");
  Class.forName("sun.misc.Unsafe").getMethod("getUnsafe", new Class[]{});
  Class.forName("gnu.java.lang.InstrumentationImpl").getMethod(
   "getAllLoadedClasses", new Class[]{});
  Class.forName("gnu.java.lang.management.VMOperatingSystemMXBeanImpl").
   getMethod("getSystemLoadAverage", new Class[]{});
 }
}
