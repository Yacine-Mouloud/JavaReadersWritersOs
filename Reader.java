public class Reader extends Thread {
  int myName;
  RandomSleep rSleep;

  public Reader(int name) {
    myName = name;
    rSleep = new RandomSleep();
  }

  public void run() {
    for (int i = 0; i < 5; i++) {

      log("Reader " + myName + " wants to read. Readcount=" + Synch.readcount);
      if (Synch.guiCallback != null)
        Synch.guiCallback.onReaderWaiting(myName);

      if (Synch.strategy == Synch.Strategy.WRITERS_PRIORITY) {
        acquireWritersPriority();
      } else if (Synch.strategy == Synch.Strategy.READERS_PRIORITY) {
        acquireReadersPriority();
      } else {
        acquireFair();
      }

      // ---- READING ----
      log("Reader " + myName + " is NOW READING. Readcount=" + Synch.readcount);
      if (Synch.guiCallback != null)
        Synch.guiCallback.onReaderReading(myName);
      rSleep.doSleep(2000, 4000);

      // Done reading
      try {
        Synch.readMutex.acquire();
      } catch (Exception e) {
      }
      Synch.readcount--;
      log("Reader " + myName + " FINISHED reading. Readcount=" + Synch.readcount);
      if (Synch.readcount == 0) {
        // Last reader releases resource for a writer
        Synch.resource.release();
      }
      Synch.readMutex.release();

      if (Synch.guiCallback != null)
        Synch.guiCallback.onReaderDone(myName);

      rSleep.doSleep(500, 2000);
    }
  }

  private void log(String msg) {
    System.out.println(msg);
    if (Synch.guiCallback != null)
      Synch.guiCallback.onLog(msg);
  }

  private void acquireWritersPriority() {
    acquire(Synch.readTry);
    acquireReadAccess();
    Synch.readTry.release();
  }

  private void acquireReadersPriority() {
    acquireReadAccess();
  }

  private void acquireFair() {
    acquire(Synch.serviceQueue);
    acquireReadAccess();
    Synch.serviceQueue.release();
  }

  private void acquireReadAccess() {
    acquire(Synch.readMutex);
    Synch.readcount++;
    if (Synch.readcount == 1) {
      acquire(Synch.resource);
    }
    Synch.readMutex.release();
  }

  private void acquire(java.util.concurrent.Semaphore semaphore) {
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Reader interrupted while acquiring synchronization lock", e);
    }
  }
}
