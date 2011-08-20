
public class Empty4
{

 public static void main(String[] args)
 {
  if (args != null)
   Thread.currentThread();
 }

 public void finalize()
 {
  new Object();
 }
}
