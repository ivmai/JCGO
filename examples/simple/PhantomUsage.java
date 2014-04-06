import java.lang.ref.*;

class PhantomReference2 extends PhantomReference
{
 private static int num = 0;
 int i = --num;
 public PhantomReference2(Object referent, ReferenceQueue q)
 {
  super(referent, q);
 }
 protected void finalize() throws Throwable
 {
  System.out.println("Finalizing reference: " + this);
 }
 public String toString() { return "Phantom{" + "" +i + "}"; }
}

class PhantomUsage
{
 static class MyObject
 {
  static int cnt;
  int i = ++cnt;
  public String toString() { return "[" + "" +i + "]"; }
  protected void finalize() throws Throwable
  {
   System.out.println("Finalizing object: " + this);
  }
 }

 public static void main(String args[])
 {
  Runtime.runFinalizersOnExit(true);
  ReferenceQueue rq = new ReferenceQueue();
  Reference refs[] = new Reference[10];
  for (int i=0;i<5;i++)
   refs[i] = test(rq);
  System.gc();
  System.runFinalization();
  System.out.println("Polling the queue returns " + rq.poll());
  System.out.println("Ok");
 }

 public static Reference test(ReferenceQueue rq)
 {
   MyObject obj = new MyObject();
   Reference wr = new PhantomReference2(obj, rq);
   obj = null;
   System.gc();
   System.out.println("Polling the queue returns " + rq.poll());
   return wr;
 }
}

/* approx output:
Polling the queue returns null
Polling the queue returns null
Finalizing object: [1]
Finalizing object: [3]
Polling the queue returns null
Finalizing object: [2]
Polling the queue returns Phantom{-1}
Finalizing object: [4]
Polling the queue returns Phantom{-2}
Finalizing object: [5]
Polling the queue returns Phantom{-4}
Ok
Finalizing reference: Phantom{-5}
Finalizing reference: Phantom{-4}
Finalizing reference: Phantom{-3}
Finalizing reference: Phantom{-2}
Finalizing reference: Phantom{-1}
*/
