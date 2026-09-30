# Operating Systems Project 1 — Nachos Threads & Synchronization

A Java-based operating systems coursework project built on **Nachos 5.0j**, an educational OS framework. The project explores kernel threads, scheduling, interrupt handling, and synchronization inside a simulated machine.

**Stack:** Java · Nachos · GNU Make

## Overview

Project 1 runs a threaded kernel rather than user programs. Its configuration selects `ThreadedKernel` and `RoundRobinScheduler`, with the simulated processor, console, disk, network, elevator bank, and stub file system disabled.

The repository includes the broader Nachos framework, but the focus here is the thread subsystem in [`src/nachos/threads/`](src/nachos/threads/).

## Implemented components

The local Project 1 implementations are included in this repository. This is an educational project with known scheduler defects, not a fully verified release.

| Component | Implementation |
| --- | --- |
| `KThread.join()` | Blocks a joining thread until the target finishes and wakes waiting threads on completion. |
| `Alarm` | Tracks sleeping threads and wake-up times; timer interrupts wake eligible threads and yield the CPU. |
| `Condition2` | Uses interrupt control and a waiting queue for sleep, wake, and wake-all operations. |
| `Communicator` | Transfers 32-bit messages synchronously between speakers and listeners using a lock and condition variables. |
| `PriorityScheduler` | Selects waiting threads by effective priority and computes donation through owned queues; see known issues below. |
| `Boat` | Coordinates adult and child threads in the island-crossing synchronization exercise. |

Round-robin remains the default scheduler. The repository also contains the original Nachos framework; framework functionality is not claimed as original project work.

## Validation and known issues

Checked on September 30, 2026 using Temurin OpenJDK **11.0.28** in a separate clean-build copy:

- Project 1 compiled successfully from source.
- Existing kernel self-tests completed.
- Focused checks for thread joining, positive timed waits, and ten-speaker/ten-listener message transfer passed with seeds 1, 2, and 3. These exercise `Condition2` indirectly.
- The included boat scenarios (0 adults/2 children, 1 adult/2 children, and 3 adults/3 children) completed with seeds 1, 2, and 3. This is not exhaustive verification of boat safety or all population sizes.

Two focused checks failed:

1. **Interrupt restoration at the priority limit:** `increasePriority()` returns at maximum priority without restoring interrupts. The minimum-priority path in `decreasePriority()` has the same pattern on code inspection.
2. **Stale queue ownership:** `PriorityQueue.nextThread()` returns `null` for an empty queue without clearing its previous owner. A subsequent waiter can incorrectly donate priority to that former owner.

These defects remain in the source intentionally as part of the uploaded snapshot. The temporary review harness is not included; the default self-tests alone do not reproduce all of the focused checks or establish full correctness. Nachos can report an assertion failure while returning process exit code 0, so inspect output as well as the exit status.

## Concepts explored

- **Kernel thread lifecycle:** transitioning between ready, running, blocked, and finished states.
- **Scheduling:** managing a ready queue and choosing the next thread.
- **Interrupt handling:** protecting operations that must be atomic in the simulated kernel.
- **Synchronization:** coordinating shared-state access with locks, semaphores, and condition variables.
- **Blocking communication:** pairing a sender with a receiver without busy waiting.
- **Priority inversion:** understanding why priority donation may be needed when threads share resources.

## Repository layout

```text
.
├── README.md
├── .vscode/                  # Java editor configuration
├── bin/nachos/              # Checked-in compiled output and copied resources
└── src/nachos/
    ├── Makefile             # Shared Java build rules
    ├── README               # Original Nachos documentation and copyright
    ├── proj1/
    │   ├── Makefile         # Project 1 build entry point
    │   └── nachos.conf      # Threaded-kernel and simulated-machine settings
    ├── threads/             # Kernel threads, schedulers, synchronization
    ├── machine/             # Simulated hardware and machine support
    ├── security/            # Nachos security support
    ├── ag/                  # Autograder support
    ├── userprog/            # Framework code for later projects
    ├── vm/                  # Framework code for later projects
    └── network/             # Framework code for later projects
```

## Build and run

### Requirements

- A Java Development Kit with `java` and `javac` on your PATH.
- GNU Make, invoked as `make` or `gmake` depending on your system.
- A terminal on Linux, macOS, or a compatible Unix-like environment.

The source was compiled and exercised with Temurin OpenJDK 11.0.28. Nachos is a legacy Java framework; compatibility with newer JDKs has not been verified.

### Compile from source

From the repository root:

```sh
cd src/nachos/proj1
make -B
```

If GNU Make is installed as `gmake`, substitute `gmake` for `make`. The `-B` flag forces recompilation rather than relying on checked-in class files. The build compiles the Project 1 packages into `src/nachos/proj1/nachos/`.

### Start the simulated kernel

Run from the same `src/nachos/proj1` directory so Nachos can find `nachos.conf`:

```sh
java -cp . nachos.machine.Machine
```

Useful options:

```sh
# Display command-line help
java -cp . nachos.machine.Machine -h

# Enable thread and interrupt debugging
java -cp . nachos.machine.Machine -d ti

# Set a reproducible random seed
java -cp . nachos.machine.Machine -s 1
```

The included thread self-test prints messages such as `*** thread 0 looped 0 times`. Output order and timing statistics depend on execution. See the validation section for checks performed and their limits. Rebuild from source; the checked-in `bin/` output may be stale.

## Code guide

Start with these files:

1. [`ThreadedKernel.java`](src/nachos/threads/ThreadedKernel.java) — kernel initialization, scheduler selection, and self-tests.
2. [`KThread.java`](src/nachos/threads/KThread.java) — thread lifecycle and context switching.
3. [`Alarm.java`](src/nachos/threads/Alarm.java) — timer-based sleeping and wake-up behavior.
4. [`Condition2.java`](src/nachos/threads/Condition2.java) — condition-variable exercise using interrupt control.
5. [`Communicator.java`](src/nachos/threads/Communicator.java) — synchronous 32-bit message-passing exercise.
6. [`PriorityScheduler.java`](src/nachos/threads/PriorityScheduler.java) — priority scheduling and donation exercise.

## Next steps

- Fix priority-limit interrupt restoration and empty-queue ownership cleanup.
- Add committed regression tests for the two scheduler defects.
- Expand tests for condition-variable wake-up ordering, priority donation chains, and boat safety.
- Keep later user-program coursework separate from this Project 1 update.

## Acknowledgments

This project builds on the Nachos educational operating system framework. The original Nachos authors are Wayne A. Christopher, Steven J. Procter, and Thomas E. Anderson; the Java version was written by Daniel Hettena.

See [the original Nachos README](src/nachos/README) for upstream documentation, copyright, and permission terms. Framework functionality is credited to Nachos rather than presented as original project work.

