
import java.lang.reflect.*;

class DynProxyTest {
  public static void main(String args[]) {
     Object proxyInstance = Proxy.newProxyInstance (DynProxyTest.class.getClassLoader(),
                                                    new Class[] { I1.class, I2.class },
                                                    new IHandler());

     I1 i1 = (I1)proxyInstance;
     System.out.println ("i1.func(\"abc\")");
     System.out.println (i1.func("abc"));
     System.out.println ();

     System.out.println ("i1.func1()");
     System.out.println (i1.func1());
     System.out.println ();

     I2 i2 = (I2)proxyInstance;
     System.out.println ("i2.func(\"fgh\")");
     System.out.println (i2.func("fgh"));
     System.out.println ();

     System.out.println ("i2.func2(123)");
     i2.func2(123);
  }

interface I1 {
  public Object func (Object arg);
  public int func1 ();
}

static class IHandler implements InvocationHandler {

  static Method I1_func;
  static Method I1_func1;
  static Method I2_func2;

  static {
     try {
        I1_func  = I1.class.getDeclaredMethod ("func",  new Class[]{Object.class});
        I1_func1 = I1.class.getDeclaredMethod ("func1", new Class[]{});
        I2_func2 = I2.class.getDeclaredMethod ("func2", new Class[]{int.class});
     } catch (Exception exc) {
        System.out.println (exc);
        System.exit (1);
     }
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    if (method.equals (I1_func)) {
       System.out.println (" InvocationHandler invoke: I1.func(" + args[0] + ")");
       return "processed " + args[0];
    }

    if (method.equals (I1_func1)) {
       System.out.println (" InvocationHandler invoke: I1 or I2 func1()");
       return new Integer (456);
    }

    if (method.equals (I2_func2)) {
       System.out.println (" InvocationHandler invoke: I2.func2(" + ((Integer)args[0]).intValue() + ")");
       return null;
    }
    System.out.println ("Oops! It shouldn't happen!");
    return null;
  }
}

}

interface I2 {
  public Object func (Object arg);
  public void func2 (int arg);
}
