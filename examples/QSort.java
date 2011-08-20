
import java.util.*;

public final class QSort
{

 public static void main(String[] args)
 {
  int count = 40 * 1000 * 1000;
  if (args.length != 0)
  {
   try
   {
    count = Integer.parseInt(args[0]);
   }
   catch (NumberFormatException e) {}
  }
  System.out.println("Quick Sort demo: generating " + count + " integers...");
  int[] array = new int[count];
  Random rnd = new Random();
  for (int i = 0; i < count; i++)
   array[i] = rnd.nextInt();
  int time = (int)System.currentTimeMillis();
  System.out.println("Sorting integers...");
  Arrays.sort(array);
  System.out.println("Finished sorting in " +
   ((int)System.currentTimeMillis() - time) + " ms");
 }
}
