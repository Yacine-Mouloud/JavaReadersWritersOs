# Readers–Writers Synchronization Simulator

A Java desktop application that visualizes the classic readers–writers synchronization problem with **writer priority**. It demonstrates how semaphores coordinate concurrent threads while keeping shared-resource access safe.

## What it demonstrates

- Multiple readers can access the shared resource at the same time.
- Writers access the resource exclusively.
- When a writer is waiting, new readers are held back so writers get priority.
- Eight reader threads and eight writer threads run concurrently; each performs five read or write cycles.
- A Swing interface shows each thread's state, shared-resource activity, active/waiting counts, and an event log.
- Randomized delays make thread interleavings visible during a run.

## Run it

You need a JDK installed with `javac` and `java` available on your `PATH`. From the project directory, open a terminal and run:

```powershell
javac *.java
java MainMethod
```

The application window opens. Select **Start Simulation** to begin.

## Implementation

The synchronization logic uses Java semaphores:

- `resource` provides exclusive access when a writer is active and coordinates readers as a group.
- `readMutex` protects the active-reader count.
- `writeMutex` protects the active/waiting-writer count.
- `readTry` prevents new readers from entering while writers are queued.

`Reader` and `Writer` are independent threads. They report state changes through a callback, and the Swing UI schedules those updates on the event-dispatch thread.

## Project files

| File | Purpose |
| --- | --- |
| `MainMethod.java` | Application entry point and Swing simulation interface |
| `Synch.java` | Shared semaphore state and GUI callback interface |
| `Reader.java` | Reader thread behavior |
| `Writer.java` | Writer thread behavior |
| `RandomSleep.java` | Randomized delays used to show concurrency |

This is an educational operating-systems project focused on thread coordination, mutual exclusion, and synchronization policies.
