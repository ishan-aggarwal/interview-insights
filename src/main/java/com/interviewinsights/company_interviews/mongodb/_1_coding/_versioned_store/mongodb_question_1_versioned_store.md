# MongoDB Interview Question 1: Point-in-Time Versioned Store (MVCC)

> **Topic:** Data structures, binary search, version history,
> tombstones, concurrency, and database internals\
> **Difficulty:** Progressive / practical interview problem\
> **Reference:** [BigTechExperts -- MongoDB SWE Algorithm Question
> 1](https://www.bigtechexperts.com/companies/mongodb/swe-algorithm-questions/interview-question-1)

------------------------------------------------------------------------

## 1. Problem overview

Design a versioned key-value store that can answer:

**"What value did this key have at a particular point in time?"**

Unlike a normal dictionary, the store must retain a key's history. Each
write is associated with a timestamp. A read at time `T` returns the
value from the latest version whose timestamp is less than or equal to
`T`.

The problem is presented in three progressive parts:

1.  Support point-in-time reads and writes.
2.  Support deletion without destroying historical reads.
3.  Make the store safe when multiple threads read and write
    concurrently.

This is closely related to **Multi-Version Concurrency Control (MVCC)**
and the ideas behind database snapshot reads.

## 2. Requirements and assumptions

### Operations

  -----------------------------------------------------------------------
  Operation                           Meaning
  ----------------------------------- -----------------------------------
  `set(key, value, timestamp)`        Record a new value for a key at the
                                      given timestamp.

  `get(key, timestamp)`               Return the value from the latest
                                      version at or before the requested
                                      timestamp.

  `delete(key, timestamp)`            Record that the key is deleted
                                      starting at this timestamp.
  -----------------------------------------------------------------------

### Important timestamp assumption

For any individual key, writes arrive with **strictly increasing
timestamps**.

For example, this is valid:

``` text
set("user:1", "Alice", 5)
set("user:1", "Bob", 10)
set("user:1", "Carol", 15)
```

But appending timestamp `8` after timestamp `10` for the same key
violates the assumption.

Because each key's timestamps are increasing, its versions can be stored
in an append-only list. No sorting is required after each write.

> Timestamps only need to be increasing per key; the problem does not
> require writes to different keys to arrive in a globally increasing
> order.

------------------------------------------------------------------------

# Part 1 --- Point-in-Time Reads

## 3. Problem statement

Implement:

-   `set(key, value, timestamp)`: Store a version of the key.
-   `get(key, timestamp)`: Return the value associated with the greatest
    version timestamp that is `<= timestamp`.
-   If the key has no version at or before the requested timestamp,
    return `None`.

### Example

``` python
set("user:1", "Alice", 5)
set("user:1", "Alphonse", 12)

get("user:1", 5)    # "Alice"
get("user:1", 9)    # "Alice"
get("user:1", 12)   # "Alphonse"
get("user:1", 100)  # "Alphonse"
get("user:1", 3)    # None
get("user:2", 5)    # None
```

## 4. Approach

### Data structure

Use a dictionary where:

-   The dictionary key is the user-provided key.
-   The dictionary value is a list of `(timestamp, value)` pairs.
-   Each list is ordered by timestamp.

Example:

``` text
store = {
    "user:1": [
        (5,  "Alice"),
        (12, "Alphonse")
    ],
    "user:2": [
        (3,  "Bob"),
        (8,  "Bobby")
    ]
}
```

### Why this data structure?

-   A dictionary gives average `O(1)` access to a key's version list.
-   Increasing timestamps make writes append-only.
-   The sorted version list allows binary search for historical reads.

### Finding the correct version

For a query timestamp `T`, find the **rightmost index** whose version
timestamp is `<= T`.

This is often called finding the *floor* position in a sorted array.

Binary search maintains a candidate:

1.  If `versions[mid].timestamp <= T`, this version is valid. Save it
    and search to the right for a potentially newer valid version.
2.  If `versions[mid].timestamp > T`, it is too new. Search to the left.
3.  If no candidate was found, return `None`.

The `<=` comparison matters: a version written exactly at the requested
timestamp must be visible to that read.

## 5. Part 1 implementation

``` java
import java.util.*;

class VersionedStore {
    private static class Version {
        long timestamp;
        String value;
        Version(long timestamp, String value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    private final Map<String, List<Version>> store = new HashMap<>();

    public void set(String key, String value, long timestamp) {
        store.computeIfAbsent(key, k -> new ArrayList<>()).add(new Version(timestamp, value));
    }

    public String get(String key, long timestamp) {
        List<Version> versions = store.get(key);
        if (versions == null) return null;

        // Binary search for the rightmost version with version_ts <= timestamp.
        int lo = 0, hi = versions.size() - 1;
        String result = null;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (versions.get(mid).timestamp <= timestamp) {
                result = versions.get(mid).value;   // candidate; keep searching right
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return result;
    }
}

```

## 6. Part 1 complexity analysis

Let `n` be the number of versions for the requested key, and `m` be the
total number of versions across all keys.

  ------------------------------------------------------------------------
  Operation                          Time complexity Explanation
  --------------------- ---------------------------- ---------------------
  `set`                             `O(1)` amortized Append to the key's
                                                     list. Dynamic-array
                                                     resizing can
                                                     occasionally cost
                                                     more.

  `get`                                   `O(log n)` Binary search over
                                                     that key's versions.

  Space                                       `O(m)` Every write is
                                                     retained in the
                                                     history.
  ------------------------------------------------------------------------

Dictionary lookup is average `O(1)`; the binary search cost is therefore
`O(log n)` overall.

------------------------------------------------------------------------

# Part 2 --- Deletes and Tombstones

## 7. Problem statement

Extend the store with:

``` python
delete(key, timestamp)
```

A delete must make the key unavailable at and after the deletion
timestamp, while preserving the ability to read its earlier values.

### The key design decision: do not physically remove history

Suppose the operations are:

``` python
set("user:1", "Alice", 5)
delete("user:1", 10)
set("user:1", "Bob", 15)
```

Expected reads:

``` python
get("user:1", 3)    # None
get("user:1", 7)    # "Alice"
get("user:1", 12)   # None
get("user:1", 20)   # "Bob"
```

If the implementation physically removes `"user:1"` at timestamp `10`,
the historical read at timestamp `7` can no longer return `"Alice"`.

That would violate point-in-time semantics.

## 8. What is a tombstone?

A **tombstone** is a special version that represents deletion.

Instead of removing a key's history, append a deletion marker at the
deletion timestamp.

For example:

``` text
"user:1": [
    (5,  "Alice",  deleted=False),
    (10, None,     deleted=True),   # tombstone
    (15, "Bob",    deleted=False)
]
```

A tombstone is treated as a normal version during binary search.

After finding the newest version at or before the requested timestamp:

-   No matching version → return `None`.
-   Matching version is a tombstone → return `None`.
-   Otherwise → return its stored value.

A later `set` creates a new live version. This is sometimes described as
the key being *resurrected*.

## 9. Part 2 implementation

``` java
import java.util.*;

class VersionedStore {
    private static class Version {
        long timestamp;
        String value;
        boolean deleted;
        Version(long timestamp, String value, boolean deleted) {
            this.timestamp = timestamp;
            this.value = value;
            this.deleted = deleted;
        }
    }

    private final Map<String, List<Version>> store = new HashMap<>();

    public void set(String key, String value, long timestamp) {
        store.computeIfAbsent(key, k -> new ArrayList<>()).add(new Version(timestamp, value, false));
    }

    public void delete(String key, long timestamp) {
        // A delete is a tombstone version, NOT a physical removal.
        store.computeIfAbsent(key, k -> new ArrayList<>()).add(new Version(timestamp, null, true));
    }

    public String get(String key, long timestamp) {
        List<Version> versions = store.get(key);
        if (versions == null) return null;

        int lo = 0, hi = versions.size() - 1;
        Version found = null;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (versions.get(mid).timestamp <= timestamp) {
                found = versions.get(mid);   // candidate; keep searching right
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }

        if (found == null || found.deleted) return null;   // none, or tombstone
        return found.value;
    }
}

```

## 10. Part 2 complexity analysis

Let `n` be the number of versions for the key and `m` the total number
of versions.

  ------------------------------------------------------------------------
  Operation                          Time complexity Explanation
  --------------------- ---------------------------- ---------------------
  `set`                             `O(1)` amortized Append a live
                                                     version.

  `delete`                          `O(1)` amortized Append a tombstone.

  `get`                                   `O(log n)` Binary search is
                                                     unchanged; checking
                                                     the tombstone is
                                                     `O(1)`.

  Space                                       `O(m)` Tombstones also
                                                     occupy history
                                                     entries.
  ------------------------------------------------------------------------

**Important:** Deletion does not immediately reclaim memory. Old
versions remain available for historical reads. A database may reclaim
eligible history later through compaction or garbage collection.

------------------------------------------------------------------------

# Part 3 --- Concurrent Access

## 11. Problem statement

Now assume multiple threads can call the store simultaneously:

-   Multiple writers may call `set` or `delete`.
-   Multiple readers may call `get`.
-   Reads must not observe a corrupted or partially updated version
    list.

### What can go wrong without synchronization?

Consider two threads appending to the same key's list while another
thread is reading it.

Potential problems include:

-   Lost or inconsistent writes.
-   Races while a dynamic array grows or reallocates.
-   A reader observing the list while it is being structurally modified.
-   A reader seeing an inconsistent state across a compound operation.

The implementation must provide a clear synchronization strategy.

## 12. Read-write lock approach

A read-write lock distinguishes between readers and writers:

  -----------------------------------------------------------------------
  Operation               Lock                    Reason
  ----------------------- ----------------------- -----------------------
  `get`                   Shared/read lock        Multiple readers can
                                                  safely read at the same
                                                  time.

  `set`                   Exclusive/write lock    A writer modifies the
                                                  version list.

  `delete`                Exclusive/write lock    A tombstone is appended
                                                  to the version list.
  -----------------------------------------------------------------------

Why is this useful?

-   Multiple reads can run concurrently.
-   Writes are serialized.
-   A writer cannot modify a list while readers are traversing it.
-   Writers cannot append to the same structure simultaneously.

This is particularly useful for read-heavy workloads.

### A useful deeper observation: versions are immutable

Once a version has been appended, the implementation never changes that
historical entry. It only appends new entries.

Therefore, the historical data itself is immutable. Synchronization is
primarily needed to protect the mutable list structure while it is being
appended to or resized.

This is an important connection to MVCC: readers can continue to reason
about older versions without those versions being overwritten by newer
writes.

## 13. Python thread-safe implementation

Python's standard library does not provide a built-in read-write lock. A
straightforward implementation can use `threading.Lock` to protect all
operations.

This is correct and simple, but it serializes readers too. It is **not**
equivalent to a read-write lock.

``` java
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class VersionedStore {
    private static class Version {
        long timestamp; String value; boolean deleted;
        Version(long timestamp, String value, boolean deleted) {
            this.timestamp = timestamp; this.value = value; this.deleted = deleted;
        }
    }

    private final Map<String, List<Version>> store = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void set(String key, String value, long timestamp) {
        lock.writeLock().lock();
        try {
            store.computeIfAbsent(key, k -> new ArrayList<>()).add(new Version(timestamp, value, false));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void delete(String key, long timestamp) {
        lock.writeLock().lock();
        try {
            store.computeIfAbsent(key, k -> new ArrayList<>()).add(new Version(timestamp, null, true));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String get(String key, long timestamp) {
        lock.readLock().lock();          // many readers may hold this at once
        try {
            List<Version> versions = store.get(key);
            if (versions == null) return null;
            int lo = 0, hi = versions.size() - 1;
            Version found = null;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                if (versions.get(mid).timestamp <= timestamp) {
                    found = versions.get(mid);
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            return (found == null || found.deleted) ? null : found.value;
        } finally {
            lock.readLock().unlock();
        }
    }
}

```

### Python-specific interview note

The Global Interpreter Lock (GIL) should not be treated as a replacement
for application-level synchronization.

The GIL does not establish the complete correctness guarantees required
for compound operations involving shared data structures. Explicit
locking makes the intended safety guarantee clear.

For a production implementation with high read concurrency, a genuine
read-write lock or a carefully designed per-key locking strategy may be
preferable.

## 14. JavaScript / TypeScript concurrency note

Node.js normally executes JavaScript callbacks on a single-threaded
event loop.

If the `set`, `delete`, and `get` methods are fully synchronous and do
not yield execution, two such methods do not execute simultaneously on
the same JavaScript thread. In that situation, the synchronous Part 2
implementation does not need a lock for ordinary event-loop
interleaving.

However:

-   An `await` can yield control and allow another task to run.
-   A read-modify-write sequence spanning an `await` can interleave with
    another operation.
-   Worker threads or shared-memory designs introduce additional
    concurrency concerns.

A good interview answer explains the execution model instead of adding
locks mechanically.

## 15. Part 3 complexity analysis

Let `n` be the number of versions for the key.

  ------------------------------------------------------------------------
  Operation                          Work complexity Synchronization
  --------------------- ---------------------------- ---------------------
  `set`                             `O(1)` amortized Exclusive lock
                                                     acquisition.

  `delete`                          `O(1)` amortized Exclusive lock
                                                     acquisition.

  `get`                                   `O(log n)` Shared lock in a
                                                     read-write-lock
                                                     design; exclusive
                                                     access in the simple
                                                     Python mutex
                                                     implementation.

  Space                                       `O(m)` All versions and
                                                     tombstones remain
                                                     stored.
  ------------------------------------------------------------------------

Lock acquisition and contention add runtime overhead that is not
captured by the basic asymptotic work complexity.

### Reducing contention: per-key locks

A single global lock is simple, but it can become a bottleneck: a write
to one key may block an operation on an unrelated key.

A finer-grained design can use one lock per key:

-   Operations on the same key coordinate through the same lock.
-   Operations on different keys can proceed independently.
-   A read-write lock per key can additionally allow concurrent readers
    of that key.

This adds implementation complexity, especially when creating and
managing locks safely.

------------------------------------------------------------------------

# 16. Consolidated solution (all three functional requirements)

The following is a compact Python implementation supporting writes,
tombstone deletes, historical reads, and basic thread safety with a
mutex.

``` python
import threading


class VersionedStore:
    def __init__(self):
        self.store = {}
        self._lock = threading.Lock()

    def set(self, key: str, value: str, timestamp: int) -> None:
        with self._lock:
            self.store.setdefault(key, []).append(
                (timestamp, value, False)
            )

    def delete(self, key: str, timestamp: int) -> None:
        with self._lock:
            self.store.setdefault(key, []).append(
                (timestamp, None, True)
            )

    def get(self, key: str, timestamp: int):
        with self._lock:
            versions = self.store.get(key)

            if not versions:
                return None

            lo, hi = 0, len(versions) - 1
            found = None

            while lo <= hi:
                mid = (lo + hi) // 2

                if versions[mid][0] <= timestamp:
                    found = versions[mid]
                    lo = mid + 1
                else:
                    hi = mid - 1

            if found is None or found[2]:
                return None

            return found[1]
```

**Contract:** This implementation assumes strictly increasing timestamps
for every key. It does not validate that assumption. If the interviewer
removes that guarantee, the write strategy must change (for example, by
inserting in sorted order or using a different ordered structure).

------------------------------------------------------------------------

# 17. Dry run

Consider this operation sequence:

``` python
store = VersionedStore()

store.set("user:1", "Alice", 5)
store.delete("user:1", 10)
store.set("user:1", "Bob", 15)
```

The stored history becomes:

``` text
timestamp   value    deleted
---------   -----    -------
5           Alice    False
10          None     True
15          Bob      False
```

  Query                   Binary-search result        Expected output
  ----------------------- --------------------------- -----------------
  `get("user:1", 3)`      No version at or before 3   `None`
  `get("user:1", 5)`      Version at 5                `"Alice"`
  `get("user:1", 7)`      Version at 5                `"Alice"`
  `get("user:1", 10)`     Tombstone at 10             `None`
  `get("user:1", 12)`     Tombstone at 10             `None`
  `get("user:1", 15)`     Version at 15               `"Bob"`
  `get("user:1", 100)`    Latest version at 15        `"Bob"`
  `get("unknown", 100)`   Key does not exist          `None`

------------------------------------------------------------------------

# 18. Edge cases and tests to discuss

These are especially useful to run before declaring the solution
complete.

  -----------------------------------------------------------------------
  Case                    Operations / query      Expected behavior
  ----------------------- ----------------------- -----------------------
  Key never written       `get("x", 5)`           `None`

  Read before first write `set("x", "A", 5)`,     `None`
                          then `get("x", 4)`      

  Exact timestamp         `set("x", "A", 5)`,     `"A"`
                          then `get("x", 5)`      

  Between versions        Set at 5 and 12; query  Value from timestamp 5
                          at 9                    

  After latest version    Set at 5; query at 100  Latest value

  Delete after set        Set at 5, delete at 10; Earlier value
                          query at 7              

  Read at deletion time   Delete at 10; query at  `None`
                          10                      

  Read after deletion     Delete at 10; query at  `None`
                          12                      

  Set after deletion      Delete at 10, set at    New value
                          15; query at 20         

  Delete an unknown key   Delete at 10; query at  `None`
                          12                      

  Multiple keys           Interleave writes to    Histories remain
                          different keys          independent
  -----------------------------------------------------------------------

### Additional contract questions worth clarifying

1.  **Are timestamps unique per key?** The stated assumption says
    strictly increasing, so duplicate timestamps are not allowed.
2.  **Can writes arrive out of order?** Not under the stated assumption.
    If they can, appending is no longer sufficient.
3.  **What does deleting a nonexistent key mean?** A tombstone can still
    be recorded; reads remain `None`.
4.  **Can `None` be a legitimate stored value?** The example
    implementation uses `None` as the missing/deleted result. If `None`
    is a valid value, the API needs a distinct sentinel or result type
    to distinguish "stored None" from "not present."
5.  **What happens if a key is deleted and then set again?** The later
    set becomes the latest live version.

------------------------------------------------------------------------

# 19. Database concepts behind the problem

## MVCC --- Multi-Version Concurrency Control

MVCC is a database technique in which multiple versions of data are
retained so readers can see a consistent version of the data without
requiring every read to observe the latest write.

In this problem:

-   Each write creates a new version.
-   Old versions are not overwritten.
-   A read timestamp selects the version visible at that point in time.
-   A delete creates a tombstone rather than erasing history.

## Snapshot isolation and point-in-time reads

A snapshot read asks the database to read data as it existed at a
particular logical time.

The `get(key, timestamp)` method is a simplified, single-key
illustration of this idea.

A full database snapshot is more involved: it must ensure that reads
across multiple records observe a mutually consistent database state,
not merely independently selected versions.

## MongoDB WiredTiger

WiredTiger is MongoDB's storage engine. The question's version-history
model is conceptually related to how storage engines preserve versions
and support snapshot-oriented reads.

The interview implementation is an educational simplification, not a
literal implementation of WiredTiger internals.

## Oplog

MongoDB's oplog is an operation log used to record database changes and
support replication. The problem's append-only, timestamp-ordered
history is a useful conceptual analogy.

## Change streams

Change streams let applications observe database changes. A possible
extension to this problem is to retrieve all versions or changes within
a timestamp interval.

## Tombstones and compaction

A tombstone records that a value was deleted while preserving the
ordering of history.

A storage engine may later reclaim obsolete versions, but only when they
are no longer needed by active snapshots or other readers. Removing
history too early would break older reads.

------------------------------------------------------------------------

# 20. Possible follow-up questions

## Follow-up A: `range(key, startTime, endTime)`

**Question:** Return every version/change for a key in a time interval.

Possible approach:

1.  Binary-search for the first version whose timestamp is
    `>= startTime`.
2.  Iterate forward until timestamps exceed `endTime`.
3.  Decide whether the API should include tombstones or only live
    values.

If there are `n` versions for the key and `k` versions in the requested
interval:

-   Finding the starting position: `O(log n)`.
-   Scanning the matching interval: `O(k)`.
-   Total: **`O(log n + k)`**.
-   Additional output space: **`O(k)`** if results are materialized.

Clarify whether the interval is inclusive or exclusive at either
endpoint.

## Follow-up B: `compact()`

**Question:** How can old versions be garbage-collected to reduce
storage?

The important constraint is that versions still needed by active
snapshots must not be removed.

A possible high-level strategy:

1.  Track the oldest active snapshot timestamp.
2.  Identify versions older than that safe boundary.
3.  Retain the version needed to answer reads at the boundary, along
    with all newer versions.
4.  Reclaim older, unreachable history.
5.  Preserve tombstone semantics so deleted keys do not accidentally
    reappear.

The exact retention rule depends on the snapshot model and API
guarantees. Compaction is not simply "delete every version older than
the latest one."

------------------------------------------------------------------------

# 21. Interview tips and points to communicate

A strong solution is not just code. Explain the reasoning as you build
it.

### Part 1

-   "Each key maps to a timestamp-sorted list of versions."
-   "The timestamps are strictly increasing, so writes can append in
    amortized `O(1)` time."
-   "A read needs the rightmost timestamp less than or equal to the
    query timestamp, so I will use binary search."
-   "The important boundary is `<=`, not `<`."

### Part 2

-   "I should not physically remove the key's history because that would
    break reads from before the deletion."
-   "I will represent deletion as a tombstone version."
-   "The binary search does not change; I only inspect whether the
    selected version is deleted."

### Part 3

-   "Writers need exclusive access while appending."
-   "A read-write lock allows multiple readers while preventing writers
    from changing the list during a read."
-   "Versions are immutable after creation; synchronization protects the
    mutable collection structure."
-   "A per-key lock can reduce contention between unrelated keys."
-   "In Python, a standard mutex is a simple correct option, but it
    serializes readers. In synchronous Node.js code, event-loop
    execution changes the locking discussion."

### What interviewers are likely evaluating

-   Correct handling of historical reads.
-   Correct binary-search boundaries.
-   Understanding why deletion must preserve history.
-   Clear treatment of concurrency and race conditions.
-   Awareness of the language runtime's concurrency model.
-   Ability to reason about memory growth and safe history reclamation.
-   Working code and explicit edge-case testing.

------------------------------------------------------------------------

# 22. Quick revision sheet

  -----------------------------------------------------------------------
  Topic                               Key takeaway
  ----------------------------------- -----------------------------------
  Data structure                      Dictionary of per-key version lists

  Write                               Append
                                      `(timestamp, value, deleted)`

  Read                                Find rightmost timestamp
                                      `<= query timestamp`

  Search                              Binary search

  Delete                              Append a tombstone

  Part 1/2 write complexity           `O(1)` amortized

  Read complexity                     `O(log n)` per key

  Total retained space                `O(m)`

  Thread safety                       Exclusive writer access; shared
                                      reader access in an RW-lock design

  Python simple solution              `threading.Lock` around operations

  Contention improvement              Per-key locks

  Core database idea                  MVCC / snapshot-oriented reads

  Possible extensions                 Range queries and safe compaction
  -----------------------------------------------------------------------

## Final takeaway

The central insight is to treat a key's history as an **append-only
sequence of immutable versions**.

-   Binary search gives efficient point-in-time reads.
-   Tombstones preserve historical correctness when deleting.
-   Synchronization protects concurrent access to the version lists.
-   MVCC explains why this design is relevant to real database systems.

The problem starts as a binary-search exercise, but the deeper interview
signal is understanding history, deletion semantics, concurrency, and
storage lifecycle.
