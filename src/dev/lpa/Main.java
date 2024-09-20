package dev.lpa;

public class Main {

  public static void main(String[] args) {

    Thread blue = new Thread(Main::countDown, ThreadColor.ANSI_BLUE.name());
    Thread yellow = new Thread(Main::countDown, ThreadColor.ANSI_YELLOW.name());
    Thread red = new Thread(Main::countDown, ThreadColor.ANSI_RED.name());

    blue.start();
    yellow.start();
    red.start();

    try {
      blue.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
      // means critical issue either handled above or terminates app
      // 'converts' checked to unchecked exception, that does not have to be handled
    }
    try {
      yellow.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    try {
      red.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static void countDown() {

    String threadName = Thread.currentThread().getName();
    var threadColor = ThreadColor.ANSI_RESET;
    try {
      threadColor = ThreadColor.valueOf(threadName.toUpperCase());
    } catch (IllegalArgumentException ignore) {
      // User may pass a bad color name, Will just ignore this error.
    }

    String color = threadColor.color();
    for (int i = 20; i >= 0; i--) {
      System.out.println(color + " " + threadName.replace("ANSI_", "") + " " + i);
    }
  }
}
