package dev.lpa;

public class Main {

  public static void main(String[] args) {


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
