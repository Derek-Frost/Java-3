public class WaitNotify {
    private final Object mon = new Object();
    private volatile char currentLetter = 'A';

    public static void main(String[] args) {
        WaitNotify waitNotify = new WaitNotify();
        new Thread(() -> waitNotify.printA()).start();
        new Thread(() -> waitNotify.printB()).start();
        new Thread(() -> waitNotify.printC()).start();

    }

    public void printA() {
        synchronized (mon) {
            for (int i = 0; i < 5; i++) {
                while (currentLetter != 'A') {
                    try {
                        mon.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("A");
                currentLetter = 'B';
                mon.notifyAll();
            }
        }
    }
    public void printB() {
        synchronized (mon) {
            for (int i = 0; i < 5; i++) {
                while (currentLetter != 'B') {
                    try {
                        mon.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("B");
                currentLetter = 'C';
                mon.notifyAll();
            }
        }
    }

    public void printC() {
        synchronized (mon) {
            for (int i = 0; i < 5; i++) {
                while (currentLetter != 'C') {
                    try {
                        mon.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("C");
                currentLetter = 'A';
                mon.notifyAll();
            }
        }
    }

}


