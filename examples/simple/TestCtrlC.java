class TestCtrlC
{
 static void main(final String[] args) // first arg should be: 0 .. 5
 {
  final char c = args.length > 0 && args[0].length() > 0 ?
                  args[0].charAt(0) : '\0';
  Runtime.getRuntime().addShutdownHook(new Thread()
  {
   public void run()
   {
    System.out.println("Shutdown hook!");
   }
  });
  if (c == '4' || c == '5')
  {
   Thread t =
    new Thread()
    {
     public void run()
     {
      try
      {
       for (;;)
       {
        System.out.println("Next 2!");
        int ch;
        if ((ch = System.in.read()) < 0)
        {
         System.out.println();
         System.out.println("EOF 2!");
         break;
        }
        if (ch == 'q')
         break;
        if (ch == 'e')
         System.exit(1);
       }
      }
      catch (Exception e)
      {
       System.out.println("Exc2: " + e);
      }
      System.out.println("End 2!");
     }
    };
   t.setDaemon(args.length == 1);
   t.start();
   t =
    new Thread()
    {
     public void run()
     {
      for (;;)
      {
       System.out.print(".");
       System.out.flush();
       try
       {
        Thread.sleep(500);
       }
       catch (Exception e)
       {
        System.out.println("Exc3: " + e);
       }
      }
     }
    };
   t.setDaemon(true);
   t.start();
  }
  try
  {
   System.out.println("Loop!");
   for (;;)
   {
    Thread.yield();
    if (c == '1')
     Thread.currentThread().stop();
    if (c == '\0' || c == '0')
     break;
    try
    {
     if (c == '3' || c == '5')
     {
      int ch;
      if ((ch = System.in.read()) < 0)
      {
       System.out.println();
       System.out.println("EOF!");
       break;
      }
      if (ch == 'Q')
       break;
      System.out.println("Next!");
     }
    }
    catch (Exception e)
    {
     System.out.println("Exc2: " + e);
    }
   }
  }
  catch (ThreadDeath e)
  {
   System.out.println("Death!");
   throw e;
  }
  System.out.println("End!");
 }
}
