
# 2. Problem: Implement a Point-in-Time Versioned Key-Value Store (MVCC)

**Company:** MongoDB  
**Category:** Data Structures · System Design  
**Concepts:** HashMap · ArrayList · Binary Search · Tombstones · Concurrency

## Problem statement

Design and implement a versioned key-value store that maintains the history of values associated with each key and allows retrieving the value as it existed at a specific timestamp.

Unlike a traditional HashMap, which stores only the latest value for a key, a versioned key-value store maintains multiple versions of each key, each associated with a timestamp.

The goal is to support point-in-time reads, allowing clients to retrieve the most recent value that existed at or before a given timestamp.

Implement the following operations:

### Part 1: Point-in-Time Reads

Implement two operations:

```java
set(String key, String value, int timestamp);

get(String key, int timestamp);
```

**Requirements:**

1. `set()`: Store a new version of the key with the specified value and timestamp.
2. `get()`: Return the value associated with the largest timestamp that is less than or equal to the requested timestamp.
3. If no version exists at or before the requested timestamp, return `null`.
4. Assume timestamps for each individual key are strictly increasing.

### Example

```java
set("user:1", "Alice", 5);
set("user:1", "Alphonse", 12);

get("user:1", 5);    // Alice
get("user:1", 9);    // Alice
get("user:1", 12);   // Alphonse
get("user:1", 100);  // Alphonse
get("user:1", 3);    // null
```

**Expected approach:** HashMap + ArrayList + Binary Search.

---

### Part 2: Deletion and Tombstones

Extend the implementation to support deletion of a key at a specific timestamp.

Implement:

```java
delete(String key, int timestamp);
```

**Requirements:**

1. Deletion should not physically remove the key's historical versions.
2. Instead, record a special version called a **Tombstone** to indicate that the key was deleted at the specified timestamp.
3. A `get()` operation after the deletion timestamp should return `null` until a new value is inserted.
4. A `get()` operation before the deletion timestamp should still return the previously stored value.
5. A subsequent `set()` operation should allow the key to become available again.

### Example

```java
set("user:1", "Alice", 5);

delete("user:1", 10);

set("user:1", "Bob", 15);

get("user:1", 3);   // null
get("user:1", 7);   // Alice
get("user:1", 12);  // null
get("user:1", 20);  // Bob
```

**Expected approach:** Maintain a version history and introduce a `deleted` boolean flag to identify tombstone entries.

---

### Part 3: Thread Safety and Concurrent Access

Extend the implementation to support concurrent access from multiple threads.

Multiple threads may simultaneously execute:

- `set()`
- `get()`
- `delete()`

**Requirements:**

1. Multiple readers should be able to read concurrently.
2. Writers should not interfere with each other.
3. Readers should never observe an inconsistent or partially updated version history.
4. Ensure that point-in-time reads remain correct during concurrent updates.
5. Maintain thread safety across all operations.

**Expected approach:** Use Java's `ReentrantReadWriteLock`.

- `get()` should acquire a read lock.
- `set()` and `delete()` should acquire a write lock.

An advanced optimization would be to introduce per-key locking to reduce contention between operations on unrelated keys.

---

## Expected Complexity

Let:

- `N` = Number of versions associated with a particular key.
- `M` = Total number of versions across all keys.

| Operation | Part 1 | Part 2 |
|---|---|---|
| `set()` | O(1) amortized | O(1) amortized |
| `get()` | O(log N) | O(log N) |
| `delete()` | Not applicable | O(1) amortized |
| Space | O(M) | O(M) |

Part 3 introduces locking overhead and contention, but the underlying data structure operations retain their expected asymptotic complexity.

---

## Key Concepts to Understand

1. HashMap and ArrayList
2. Binary Search – finding the rightmost timestamp less than or equal to the target timestamp
3. Time-based Key-Value Store
4. Tombstones and Logical Deletion
5. MVCC (Multi-Version Concurrency Control)
6. Java ReentrantReadWriteLock
7. Thread Safety and Concurrent Data Structures

## Interview Takeaways

- Understand how to maintain multiple versions of a key instead of overwriting its previous value.
- Use binary search to efficiently retrieve the correct historical version.
- Understand why deletion should be represented using tombstones rather than physically removing historical data.
- Understand how MVCC supports point-in-time reads.
- Handle concurrent readers and writers without compromising data consistency.

**Recommended implementation strategy:** Solve the problem progressively in three stages: first implement point-in-time reads, then add deletion using tombstones, and finally introduce thread safety.
