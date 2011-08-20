import java.lang.ref.*;
import java.util.*;
class RefsTest
{
 static Object o;
 protected void finalize()
 {
  o = null;
 }

 public static void main(String[] args)
 {
  Runtime.getRuntime().runFinalizersOnExit(true);
  for (int i = 0; i < 50; i++)
  {
   Object x = new RefsTest();
   ReferenceQueue q = new ReferenceQueue();
   Vector v = new Vector();
   for (int j = 0; j < 20; j++)
   {
    for (int k = 0; k < 20; k++)
    {

     new WeakReference("");
     new WeakReference(new String(""));
     new WeakReference(new RefsTest());
     new WeakReference(x);
     new WeakReference(args);

     new WeakReference("", new ReferenceQueue());
     new WeakReference(new String(""), new ReferenceQueue());
     new WeakReference(new RefsTest(), new ReferenceQueue());
     new WeakReference(args, new ReferenceQueue());
     new WeakReference(x, new ReferenceQueue());

     new WeakReference("", q);
     new WeakReference(new String(""), q);
     new WeakReference(new RefsTest(), q);
     new WeakReference(x, q);

     v.add(new WeakReference(""));
     v.add(new WeakReference(new String("")));
     v.add(new WeakReference(new RefsTest()));
     v.add(new WeakReference(x));
     v.add(new WeakReference(args));

     v.add(new WeakReference("", new ReferenceQueue()));
     v.add(new WeakReference(new String(""), new ReferenceQueue()));
     v.add(new WeakReference(new RefsTest(), new ReferenceQueue()));
     v.add(new WeakReference(x, new ReferenceQueue()));
     v.add(new WeakReference(args, new ReferenceQueue()));

     v.add(new WeakReference("", q));
     v.add(new WeakReference(new String(""), q));
     v.add(new WeakReference(new RefsTest(), q));
     v.add(new WeakReference(x, q));

     new SoftReference("");
     new SoftReference(new String(""));
     new SoftReference(new RefsTest());
     new SoftReference(x);
     new SoftReference(args);

     new SoftReference("", new ReferenceQueue());
     new SoftReference(new String(""), new ReferenceQueue());
     new SoftReference(new RefsTest(), new ReferenceQueue());
     new SoftReference(x, new ReferenceQueue());
     new SoftReference(args, new ReferenceQueue());

     new SoftReference("", q);
     new SoftReference(new String(""), q);
     new SoftReference(new RefsTest(), q);
     new SoftReference(x, q);

     v.add(new SoftReference(""));
     v.add(new SoftReference(new String("")));
     v.add(new SoftReference(new RefsTest()));
     v.add(new SoftReference(x));
     v.add(new SoftReference(args));

     v.add(new SoftReference("", new ReferenceQueue()));
     v.add(new SoftReference(new String(""), new ReferenceQueue()));
     v.add(new SoftReference(new RefsTest(), new ReferenceQueue()));
     v.add(new SoftReference(x, new ReferenceQueue()));
     v.add(new SoftReference(args, new ReferenceQueue()));

     v.add(new SoftReference("", q));
     v.add(new SoftReference(new String(""), q));
     v.add(new SoftReference(new RefsTest(), q));
     v.add(new SoftReference(x, q));

     new PhantomReference("", new ReferenceQueue());
     new PhantomReference(new String(""), new ReferenceQueue());
     new PhantomReference(new RefsTest(), new ReferenceQueue());
     new PhantomReference(x, new ReferenceQueue());
     new PhantomReference(args, new ReferenceQueue());

     new PhantomReference("", q);
     new PhantomReference(new String(""), q);
     new PhantomReference(new RefsTest(), q);
     new PhantomReference(x, q);

     v.add(new PhantomReference("", new ReferenceQueue()));
     v.add(new PhantomReference(new String(""), new ReferenceQueue()));
     v.add(new PhantomReference(new RefsTest(), new ReferenceQueue()));
     v.add(new PhantomReference(x, new ReferenceQueue()));
     v.add(new PhantomReference(args, new ReferenceQueue()));

     v.add(new PhantomReference("", q));
     v.add(new PhantomReference(new String(""), q));
     v.add(new PhantomReference(new RefsTest(), q));
     v.add(new PhantomReference(x, q));

     /* */
     new WeakReference(args, q);
     v.add(new WeakReference(args, q));
     new SoftReference(args, q);
     v.add(new SoftReference(args, q));
     new PhantomReference(args, q);
     v.add(new PhantomReference(args, q));
    }
    System.gc();
    System.out.println((Runtime.getRuntime().totalMemory() -
     Runtime.getRuntime().freeMemory()) >> 10);
    // the maximum value (for each outer iteration) should be nearly stable
   }
   v.clear();
   System.gc();
  }
 }
}