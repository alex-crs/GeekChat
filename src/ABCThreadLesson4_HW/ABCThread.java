package ABCThreadLesson4_HW;

//1. Создать три потока, каждый из которых выводит определенную букву (A, B и C) 5 раз (порядок – ABСABСABС). Используйте wait/notify/notifyAll.
public class ABCThread {
    public static void main(String[] args) {
        PrinterManager printerManager = new PrinterManager();
        new Thread(() -> {
            for (int i = 0; i < 5; ) {
                if (printerManager.getCurrentThreadPrint() == 1) {
                    System.out.print("A");
                    i++;
                    printerManager.setCurrentThreadPrint(2);
                } else {
                    printerManager.waitTurn();
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 5; ) {
                if (printerManager.getCurrentThreadPrint() == 2) {
                    System.out.print("B");
                    i++;
                    printerManager.setCurrentThreadPrint(3);
                } else {
                    printerManager.waitTurn();
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 5; ) {
                if (printerManager.getCurrentThreadPrint() == 3) {
                    System.out.print("C");
                    i++;
                    printerManager.setCurrentThreadPrint(1);
                } else {
                    printerManager.waitTurn();
                }
            }
        }).start();

    }
}

class PrinterManager extends Thread {
    private static int currentThreadPrint = 1;

    public int getCurrentThreadPrint() {
        return currentThreadPrint;
    }

    public synchronized void setCurrentThreadPrint(int currentThreadPrint) {
        PrinterManager.currentThreadPrint = currentThreadPrint;
        notifyAll();
    }

    public synchronized void waitTurn() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
