import java.io.*;

public class ExecTest extends Thread
{

 Process process;

 PrintStream dataout;

 BufferedReader datain;

 boolean iserr;

 ExecTest(Process process, boolean isin, boolean iserr)
 {
  super(isin ? "Input copier" : iserr ? "OutErr copier" : "Output copier");
  this.process = process;
  if (isin)
   dataout = new PrintStream(process.getOutputStream());
   else datain = new BufferedReader(new InputStreamReader(iserr ?
                  process.getErrorStream() : process.getInputStream()));
  this.iserr = iserr;
 }

 public void run()
 {
  try
  {
   if (dataout != null)
   {
    BufferedReader sysin =
     new BufferedReader(new InputStreamReader(System.in));
    while (true)
    {
     String s = sysin.readLine();
     if (s == null)
      break;
     if (s.equals("Kill"))
     {
      process.destroy();
      System.out.println("Destroy the process!");
     }
      else if (s.equals("Close"))
      {
       System.out.println("Closing output!");
       dataout.close();
      }
       else
       {
        dataout.println(s);
        dataout.flush();
        System.out.println("Input: " + s);
       }
    }
    sysin.close();
    dataout.close();
    System.out.println();
    System.out.println("Destroying the process...");
    try
    {
     Thread.sleep(2000);
    }
    catch (InterruptedException e) {}
    process.destroy();
    System.out.println("Destroying done!");
   }
    else
    {
     while (true)
     {
      String s = datain.readLine();
      if (s == null)
       break;
      System.out.println((iserr ? "OutErr: " : "Output: ") + s);
     }
     datain.close();
     System.out.println(iserr ? "OutErr closed!" : "Output closed!");
    }
  }
  catch (IOException e)
  {
   System.err.println(" " + getName() + ": " + e.toString());
  }
 }

 public static void main(String[] args)
 {
  if (args.length == 0)
  {
   System.err.println("Program not specified!");
   System.out.println("Done!");
   System.exit(13);
  }
  if (args.length == 1 && args[0].equals("-"))
  {
   BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
   System.out.println("Copying input!");
   while (true)
   {
    String s = null;
    try
    {
     s = sysin.readLine();
    }
    catch (IOException e)
    {
     System.err.println("Main: " + e.toString());
    }
    if (s == null || s.equals("Quit"))
     break;
    System.out.println("orig input: " + s);
   }
   System.out.println();
   System.out.println("Done copying!");
   return;
  }
  boolean async = true;
  if (args[0].equals("--"))
  {
   async = false;
   System.arraycopy(args, 1, args = new String[args.length - 1], 0,
    args.length);
  }
  String path = null;
  if (args.length > 2 && args[0].equals("-p"))
  {
   path = args[1];
   System.arraycopy(args, 2, args = new String[args.length - 2], 0,
    args.length);
  }
  String envs[] = null;
  for (int pos = 0; pos < args.length; pos++)
   if (args[pos].equals("@@"))
   {
    System.arraycopy(args, pos + 1,
     envs = new String[args.length - (pos + 1)], 0, envs.length);
    System.arraycopy(args, 0, args = new String[pos], 0, args.length);
    break;
   }
  try
  {
   Process process = Runtime.getRuntime().exec(args, envs
                      , path != null ? new File(path) : null // optional
                     );
   System.out.println("The process executed...");
   if (async)
   {
    Thread thread;
    thread = new ExecTest(process, true, false);
    thread.setDaemon(true);
    thread.start();
    thread = new ExecTest(process, false, false);
    thread.start();
    thread = new ExecTest(process, false, true);
    thread.start();
   }
   try
   {
    System.out.println("Exitcode0: " + process.exitValue());
   }
   catch (IllegalThreadStateException e)
   {
    System.out.println("The process is running...");
   }
   if (!async)
   {
    process.getOutputStream().flush();
    process.getOutputStream().close();
    System.out.println("output closed!");
    Thread thread;
    thread = new ExecTest(process, false, false);
    thread.run();
    thread = new ExecTest(process, false, true);
    thread.run();
   }
   System.out.println("Waiting for pid...");
   System.out.println("Exitcode: " + process.waitFor());
   System.out.println("Exitcode2: " + process.exitValue());
  }
  catch (IOException e)
  {
   System.err.println(e.toString());
  }
  catch (InterruptedException e)
  {
   System.err.println(e.toString());
  }
 }
}
