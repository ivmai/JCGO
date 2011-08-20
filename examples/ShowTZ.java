
import java.util.*;

public final class ShowTZ
{

 public static void main(String[] args)
 {
  long millis = System.currentTimeMillis();
  String tz = null;
  try
  {
   tz = System.getenv("TZ");
  }
  catch (Error e) {}
  if (tz != null && tz.length() > 0)
   System.out.println("getenv(TZ): " + tz);
  System.out.println("UTC milliseconds: " + millis);
  System.out.println("TimeZone ID: " + TimeZone.getDefault().getID());
  Date date = new Date(millis);
  System.out.println("GMT time: " + date.toGMTString());
  System.out.println("Local time: " + date);
  System.out.println("Locale-formatted time (" + Locale.getDefault() + "): " +
   date.toLocaleString());
 }
}
