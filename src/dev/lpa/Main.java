package dev.lpa;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

class ColorThreadFactory implements ThreadFactory {

  private String threadName;
  private int colorValue = 1;

  public ColorThreadFactory() {
  }

  public ColorThreadFactory(ThreadColor color) {
    this.threadName = color.name();
  }

  @Override
  public Thread newThread(Runnable r) {

    Thread thread = new Thread(r);
    String name = threadName;
    if (name == null) {
      name = ThreadColor.values()[colorValue].name();
    }
    if (++colorValue > (ThreadColor.values().length - 1)) {
      colorValue = 1;
    }
    thread.setName(name);
    return thread;
  }
}

public class Main {

  public static void main(String[] args) {

    var multiExecutor = Executors.newCachedThreadPool();
    try {
      multiExecutor.execute(() -> Main.sum(1, 10, 1, "red"));
      multiExecutor.execute(() -> Main.sum(10, 100, 10, "blue"));
      multiExecutor.execute(() -> Main.sum(2, 20, 2, "green"));
    } finally {
      multiExecutor.shutdown();
    }
  }
  public static void fixedMain(String[] args) {

    int count = 6;
    var multiExecutor = Executors.newFixedThreadPool(
      3, new ColorThreadFactory()
    );

    try {
      for (int i = 0; i < count; i++) {
        multiExecutor.execute(Main::countDown);
      }
    } finally {
      multiExecutor.shutdown();
    }

    boolean isDone = false;
    try {
      isDone = multiExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    if(isDone) {
      System.out.println("Tasks are finished.");
    }
  }

  public static void singleMain(String[] args) {

    var blueExecutor = Executors.newSingleThreadExecutor(
      new ColorThreadFactory(ThreadColor.ANSI_BLUE));
//      r -> new Thread(r, ThreadColor.ANSI_BLUE.name()));
    blueExecutor.execute(Main::countDown); // running tasks sequentially
    blueExecutor.shutdown();

    boolean isDone = false;
    try {
      isDone = blueExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    if (isDone) {
      System.out.println("Blue finished, starting Yellow");
      var yellowExecutor = Executors.newSingleThreadExecutor(
        new ColorThreadFactory(ThreadColor.ANSI_YELLOW));
//      r -> new Thread(r, ThreadColor.ANSI_YELLOW.name()));
      yellowExecutor.execute(Main::countDown); // running tasks sequentially
      yellowExecutor.shutdown();
      try {
        isDone = yellowExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    if (isDone) {
      System.out.println("Yello finished, starting Red");
      var redExecutor = Executors.newSingleThreadExecutor(
        new ColorThreadFactory(ThreadColor.ANSI_RED));
//      r -> new Thread(r, ThreadColor.ANSI_RED.name()));
      redExecutor.execute(Main::countDown); // running tasks sequentially
      redExecutor.shutdown();
      try {
        isDone = redExecutor.awaitTermination(400, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    if (isDone) {
      System.out.println("All processes finished.");
    }
  }
  public static void notmain(String[] args) {

    Thread blue = new Thread(Main::countDown, ThreadColor.ANSI_BLUE.name());
    Thread yellow = new Thread(Main::countDown, ThreadColor.ANSI_YELLOW.name());
    Thread red = new Thread(Main::countDown, ThreadColor.ANSI_RED.name());

    blue.start();

    try {
      blue.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
      // means critical issue either handled above or terminates app
      // 'converts' checked to unchecked exception, that does not have to be handled
    }

    yellow.start();

    try {
      yellow.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    red.start();

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

  private static void sum(int start, int end , int delta, String colorString) {

    var threadColor = ThreadColor.ANSI_RESET;
    try {
      threadColor = ThreadColor.valueOf("ANSI_" + colorString.toUpperCase());
    } catch (IllegalArgumentException ignore) {
      // User may pass a bad color name, Will just ignore this error.
    }
    String color = threadColor.color();
    int sum = 0;
    for (int i = start; i <= end; i += delta) {
      sum += i;
    }
    System.out.println(color + Thread.currentThread().getName() + ", " + colorString + " " + sum);
  }
}
