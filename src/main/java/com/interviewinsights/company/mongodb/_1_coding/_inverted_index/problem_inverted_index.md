
# 1. Problem: Implement an Inverted Index

**Category:** Data Structures  
**Concepts:** HashMap · HashSet  
**Domain:** Search Engine Design

## Problem statement

Design and implement an Inverted Index that stores documents and allows efficient retrieval of documents containing specific words.

An inverted index maintains a mapping between each unique word (term) and the set of documents in which that word appears.

Implement the following operations:

1. `insert(String doc)`: Insert a document into the index.
2. `search(String term)`: Return all documents containing the given term.
3. `delete(String doc)`: Remove a document from the index and update the index accordingly.
4. `andSearch(String term1, String term2)`: Return documents containing both terms.

### Example

Consider the following documents:

```text
Document 1: "Java is a programming language"
Document 2: "Java is used for backend development"
Document 3: "Python is a programming language"
Document 4: "Java and Python are popular languages"
```

The inverted index would look conceptually like this:

| Term | Documents |
|---|---|
| Java | Doc1, Doc2, Doc4 |
| is | Doc1, Doc2, Doc3 |
| programming | Doc1, Doc3 |
| language | Doc1, Doc3 |
| Python | Doc3, Doc4 |
| backend | Doc2 |
| development | Doc2 |
| popular | Doc4 |

### Expected operations

| Operation | Expected result |
|---|---|
| `search("Java")` | Doc1, Doc2, Doc4 |
| `search("Python")` | Doc3, Doc4 |
| `andSearch("Java", "Python")` | Doc4 |
| `andSearch("Java", "programming")` | Doc1 |
| `delete(Doc1)` | Removes Doc1 from all associated terms |

---

# 2. Recommended approach: HashMap + HashSet

We need two data structures.

### 1. Forward index

```java
Map<String, Set<String>> index;
```

Maps each word to all documents containing that word.

Example:

```text
Java -> {Doc1, Doc2, Doc4}
Python -> {Doc3, Doc4}
```

### 2. Reverse index

```java
Map<String, Set<String>> documentTerms;
```

Maps each document to all unique words contained in it.

Example:

```text
Doc1 -> {Java, is, a, programming, language}
Doc2 -> {Java, is, used, for, backend, development}
```

### Why do we need a reverse index?

Without it, deleting a document requires iterating through every word in the entire inverted index.

With a reverse index, we directly know which terms need to be removed when deleting a document.

### Architecture

**Document**

```text
"Java is a programming language"
```

**Tokenization and normalization**

```text
java, is, a, programming, language
```

**Forward Index**

Term → Documents

```text
java   → Doc1, Doc2
python → Doc3, Doc4
```

**Reverse Index**

Document → Terms

```text
Doc1 → java, is, a, programming, language
Doc2 → java, is, used, for, backend, development
```


1. **Why two indexes are useful (Forward + Reverse Index)**
   The Forward Index helps quickly find documents containing a particular word, while the Reverse Index helps quickly find all words associated with a particular document. Maintaining both makes searching and deletion efficient.
2. **Efficient deletion using a Reverse Index**
   Without a reverse index, deleting a document requires scanning every word in the entire index to find where the document exists. With a reverse index, we directly retrieve the document's words and remove it only from those entries.
3. **Optimizing AND search using the smaller posting list**
   For AND search, iterate over the smaller set of documents and check whether each document exists in the larger set. This reduces unnecessary iterations and improves performance from \(O(D_1 + D_2)\) to \(O(min(D_1,D_2))\) on average, assuming HashSet lookups are \(O(1)\).