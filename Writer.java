public class Writer extends Thread {
  int myName;
  RandomSleep rSleep;

  public Writer(int name) {
    myName = name;
    rSleep = new RandomSleep();
  }

  public void run() {
    for (int i = 0; i < 5; i++) {

      log("Writer " + myName + " wants to write. Writecount=" + Synch.writecount);
      if (Synch.guiCallback != null)
        Synch.guiCallback.onWriterWaiting(myName);

      if (Synch.strategy == Synch.Strategy.WRITERS_PRIORITY) {
        acquireWritersPriority();
      } else if (Synch.strategy == Synch.Strategy.READERS_PRIORITY) {
        acquireReadersPriority();
      } else {
        acquireFair();
      }

      // ---- WRITING ----
      log("Writer " + myName + " is NOW WRITING. Writecount=" + Synch.writecount);
      if (Synch.guiCallback != null)
        Synch.guiCallback.onWriterWriting(myName);
      rSleep.doSleep(500, 1500);

      // Done writing — release resource
      Synch.resource.release();
      log("Writer " + myName + " FINISHED writing.");

      try {
        Synch.writeMutex.acquire();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Writer interrupted while updating writer count", e);
      }
      Synch.writecount--;
      if (Synch.strategy == Synch.Strategy.WRITERS_PRIORITY && Synch.writecount == 0) {
        Synch.readTry.release();
      }
      Synch.writeMutex.release();

      if (Synch.guiCallback != null)
        Synch.guiCallback.onWriterDone(myName);

      rSleep.doSleep(500, 2000);
    }
  }

  private void log(String msg) {
    System.out.println(msg);
    if (Synch.guiCallback != null)
      Synch.guiCallback.onLog(msg);
  }

  private void acquireWritersPriority() {
    acquire(Synch.writeMutex);
    Synch.writecount++;
    if (Synch.writecount == 1) {
      acquire(Synch.readTry);
    }
    Synch.writeMutex.release();
    acquire(Synch.resource);
  }

  private void acquireReadersPriority() {
    acquire(Synch.writeMutex);
    Synch.writecount++;
    Synch.writeMutex.release();
    acquire(Synch.resource);
  }

  private void acquireFair() {
    acquire(Synch.writeMutex);
    Synch.writecount++;
    Synch.writeMutex.release();
    acquire(Synch.serviceQueue);
    acquire(Synch.resource);
    Synch.serviceQueue.release();
  }

  private void acquire(java.util.concurrent.Semaphore semaphore) {
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Writer interrupted while acquiring synchronization lock", e);
    }
  }
}
