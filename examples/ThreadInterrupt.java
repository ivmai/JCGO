
// Thread interruption tests

import java.io.*;

        class Thread$1 extends Thread {
            public void run() {
                synchronized(this) {
                    try {
                        wait(100000);
                        System.out.println("FAIL: returned from wait");
                    } catch (InterruptedException e) {
                        if (isInterrupted() || interrupted()) {
                            System.out.println(
                                "FAIL: interrupted flag has been turned on.");
                        } else {
                            System.out.println("Success 0a.");
                        }
                    }
                }
            }
        }
        class Thread$2 extends Thread {
            public void run() {
                try {
                    Thread.sleep(50000);
                    System.out.println("FAIL: returned from sleep");
                } catch (InterruptedException e) {
                    if (isInterrupted() || interrupted()) {
                        System.out.println(
                            "FAIL: interrupted flag has been turned on.");
                    } else {
                        System.out.println("Success 0b.");
                    }
                }
            }
        }
        class Thread$3 extends Thread {
            public void run() {
                synchronized (this) {
                    try {
                        wait(100000);
                    } catch (InterruptedException e) {
                        if (isInterrupted() || interrupted()) {
                            System.out.println(
                                "FAIL: interrupted flag has been turned on.");
                        }
                    }
                    if (isInterrupted() || interrupted()) {
                        System.out.println(
                            "FAIL: interrupted flag has been turned on.");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("FAIL: " + e);
                    }
                }
            }
        }
        class Thread$4 extends Thread {
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException _) { }
                System.out.println("Failure 4/5.:   Time out.");
                System.exit(-1);
            }
        }

public class ThreadInterrupt {
    public static void main(String av[]) throws Exception {
        long time = System.currentTimeMillis();
        Thread t;
        t = new Thread$1();
        ThreadInterrupt.ssij(t);
        t = new Thread$2();
        ThreadInterrupt.ssij(t);
        t = new Thread$3();
        t.start();
        t.suspend();
        Thread.sleep(10);
        t.resume();
        Thread.sleep(1000);
        synchronized(t) {
            t.interrupt();
            if (t.isInterrupted()) {
                System.out.println("Success 1.");
            } else {
                System.out.println("Failure 1.");
            }
            // make sure isInterrupted doesn't clear the flag.
            if (t.isInterrupted()) {
                System.out.println("Success 2.");
            } else {
                System.out.println("Failure 2.");
            }
        }
        Thread.sleep(1000);             // let thread finish
        if (t.isInterrupted()) {
            System.out.println("Failure 3.");
        } else {
            System.out.println("Success 3.");
        }
        t.join();
        Thread watchdog = new Thread$4();
        watchdog.start();
        Thread me = Thread.currentThread();
        me.interrupt();
        synchronized(me) {
            try {
                me.wait(40000);
                System.out.println("Failure 4.");
            } catch (InterruptedException e) {
                System.out.println("Success 4.");
            }

            me.interrupt();
            try {
                Thread.sleep(40000);
                System.out.println("Failure 5.");
            } catch (InterruptedException e) {
                System.out.println("Success 5.");
            }
        }
        System.out.println("Time: " + (int)(System.currentTimeMillis() - time));
        System.exit(0);
    }

    static void ssij(Thread t) {
        t.start();
        try {
            Thread.sleep(1000);
            t.interrupt();
            t.join();
        } catch (InterruptedException e) {
            System.out.println("caught " + e);
        }
    }
}


/* Expected Output:
Success 0a.
Success 0b.
Success 1.
Success 2.
Success 3.
Success 4.
Success 5.
*/
