
public class Empty2
{

 public static void main(String[] args)
 {
  new Empty2();
  "".getClass().getName().charAt(0);
  int[] a1 = { 1, 0 };
  int[] a2 = new int[a1.length];
  System.arraycopy(a1, 0, a2, 0, a1.length);
 }

 public void finalize()
 {
  new Object();
 }
}
