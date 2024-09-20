package dev.lpa;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

class ColorThreadFactory implements ThreadFactory {

  private String threadName;
  private int colorValue = 3;

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
      colorValue = 3;
    }
    thread.setName(name);
    return thread;
  }
}

public class Main {

  public static void main(String[] args) {

    int count = 3;
    var multiExecutor = Executors.newFixedThreadPool(
      count, new ColorThreadFactory()
    );

    for (int i = 0; i < count; i++) {
      multiExecutor.execute(Main::countDown);
    }
    multiExecutor.shutdown();

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
}
