import java.util.concurrent.*;

public class Synch {

  public enum Strategy {
    WRITERS_PRIORITY("Writers Priority"),
    READERS_PRIORITY("Readers Priority"),
    FAIR("Fair / No Starvation");

    private final String displayName;

    Strategy(String displayName) {
      this.displayName = displayName;
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

  public static Semaphore readMutex;
  public static Semaphore writeMutex;
  public static Semaphore readTry;
  public static Semaphore resource;
  public static Semaphore serviceQueue;
  public static int readcount = 0; // Number of active readers
  public static int writecount = 0; // Number of active/waiting writers
  public static Strategy strategy = Strategy.WRITERS_PRIORITY;

  // --- GUI callback (set by the GUI) ---
  public static GUICallback guiCallback = null;

  public interface GUICallback {
    void onReaderWaiting(int id);

    void onReaderReading(int id);

    void onReaderDone(int id);

    void onWriterWaiting(int id);

    void onWriterWriting(int id);

    void onWriterDone(int id);

    void onLog(String message);
  }
}
