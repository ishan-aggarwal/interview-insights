# MongoDB Coding Round 2 --- Legacy Data Cleanup

> **Focus:** Messy text parsing, normalization, maps, version-aware
> reconciliation, edge cases, and clean code decomposition.\
> **Language:** Java\
> **Reference:** [BigTechExperts --- MongoDB SWE Algorithm Question
> 2](https://www.bigtechexperts.com/companies/mongodb/swe-algorithm-questions/interview-question-2)

------------------------------------------------------------------------

## 1. Problem overview

A legacy CRM exports customer records into inconsistent text files.
Field casing and whitespace vary, comments and blank lines are present,
some lines are malformed, and the same customer may appear in multiple
files with corrections.

The problem has two parts:

1.  Parse one messy file into structured records.
2.  Merge records from multiple files into one document per customer,
    resolving conflicts using record versions.

This is an open-ended practical problem. The interviewer is evaluating
how you clarify an ambiguous specification, choose data structures,
separate responsibilities, and handle imperfect input---not just whether
you can write a parser.

## 2. Clarify the specification first

In an interview, confirm these assumptions before coding.

  Question                        Assumption used here
  ------------------------------- -------------------------------------
  Input type                      Raw file content as a Java `String`
  Record delimiter                One or more blank lines
  Key/value delimiter             First colon (`:`) only
  Key normalization               Trim and lowercase
  Value normalization             Trim; preserve case
  Comments                        Trimmed line beginning with `#`
  Malformed lines                 Skip lines without a colon
  Duplicate keys in one record    Last occurrence wins
  Part 1 output                   `List<Map<String, String>>`
  Record identity across files    Stable `id` field
  Missing ID                      Skip record during merge
  Missing version                 Treat as `0` (reference assumption)
  Conflict rule                   Higher version wins for a field
  Equal versions                  Later input record wins
  Missing field in newer record   Preserve the older value
  Output metadata                 Keep `id`; omit `version`

Useful questions to ask:

-   Is the input a file path, `Reader`, list of lines, or raw text?
-   Is it actually CSV or JSON? If so, use a proper parser.
-   Can values contain colons or span multiple lines?
-   Should malformed lines be skipped, reported, or rejected?
-   What should happen with duplicate keys?
-   What uniquely identifies a customer?
-   Does a newer record replace the entire document or only fields it
    contains?
-   How should equal versions be resolved?
-   Can files arrive out of order?
-   Can the input be too large to fit in memory?

A good opening statement:

> "I'll assume raw text, blank lines as record boundaries, `key: value`
> fields, comments and malformed lines ignored, normalized keys, and
> `id` plus version-based conflict resolution for merging. Does that
> match the expected behavior?"

------------------------------------------------------------------------

# Part 1 --- Normalizing One Messy File

## 3. Problem statement

Implement:

``` java
List<Map<String, String>> parseRecords(String content)
```

Return a list of structured records from the raw text.

### Input

``` text
# CRM export - legacy system v3
# generated 2026-01-02

Name:  John Doe
Email: JOHN@EXAMPLE.COM
Phone: 555-1234

NAME: Jane Smith
email :  jane@sample.org

Name: Bob Jones
Phone: 555 222 3333
this line is junk with no field
Email: bob@test.io
```

### Expected output

``` java
[
    {"name": "John Doe", "email": "JOHN@EXAMPLE.COM", "phone": "555-1234"},
    {"name": "Jane Smith", "email": "jane@sample.org"},
    {"name": "Bob Jones", "phone": "555 222 3333", "email": "bob@test.io"}
]
```

### Parsing rules

1.  Blank line ends the current record.
2.  A trimmed line starting with `#` is a comment.
3.  A line without `:` is malformed and skipped.
4.  Split at the **first colon only**.
5.  Trim and lowercase the key; trim the value but preserve its case.
6.  Ignore an empty key.
7.  Last duplicate key in a record wins.
8.  Flush the final record after the loop, even if there is no trailing
    blank line.

## 4. Part 1 approach

Use a single pass with one piece of state: the record currently being
built.

-   `List<Map<String, String>> records`: completed records.
-   `Map<String, String> currentRecord`: fields in the current record.
-   `LinkedHashMap`: preserves insertion order, useful for predictable
    output and debugging.

``` text
Read line
  |
Trim
  +-- blank ------------> save current record
  +-- starts with # ----> ignore
  +-- no colon ---------> ignore malformed line
  +-- otherwise --------> split at first colon
                            normalize key/value
                            put into current record
```

**Why the first colon?** A value can contain colons,
e.g. `website: https://example.com:8443/profile`. Splitting at every
colon would corrupt the value.

## 5. Part 1 --- Java solution

``` java
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LegacyDataParser {

    public static List<Map<String, String>> parseRecords(String content) {
        List<Map<String, String>> records = new ArrayList<>();
        Map<String, String> currentRecord = new LinkedHashMap<>();

        // Handles common line separators, including LF and CRLF.
        String[] lines = content.split("\\R", -1);

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty()) {
                if (!currentRecord.isEmpty()) {
                    records.add(currentRecord);
                    currentRecord = new LinkedHashMap<>();
                }
                continue;
            }

            if (line.startsWith("#")) {
                continue;
            }

            int separatorIndex = line.indexOf(':');
            if (separatorIndex == -1) {
                continue;
            }

            String key = line.substring(0, separatorIndex)
                             .trim()
                             .toLowerCase(Locale.ROOT);
            String value = line.substring(separatorIndex + 1).trim();

            if (key.isEmpty()) {
                continue;
            }

            // Duplicate key: last occurrence wins.
            currentRecord.put(key, value);
        }

        // Critical: do not lose the last record if the file has no final blank line.
        if (!currentRecord.isEmpty()) {
            records.add(currentRecord);
        }

        return records;
    }
}
```

### Implementation observations

-   `split("\\R", -1)` supports common newline conventions and preserves
    trailing empty lines. It materializes all lines in an array.
-   `indexOf(':')` plus `substring()` splits only at the first colon.
-   `Locale.ROOT` avoids locale-dependent lowercasing.
-   `LinkedHashMap` preserves field insertion order. `HashMap` is fine
    if order is irrelevant.
-   `Map.put()` naturally implements last-duplicate-wins.
-   The final flush after the loop is a common interview edge case.

## 6. Part 1 complexity

Let `N` be the total number of characters in the input.

  ------------------------------------------------------------------------
  Metric                                  Complexity Reason
  --------------------- ---------------------------- ---------------------
  Time                                        `O(N)` Each character is
                                                     processed a constant
                                                     number of times
                                                     during splitting,
                                                     trimming, and
                                                     separator lookup.

  Output space                                `O(N)` Parsed keys and
                                                     values are retained
                                                     in returned maps.

  Auxiliary space                             `O(N)` `split()`
                                                     materializes lines;
                                                     the current record is
                                                     also built.
  ------------------------------------------------------------------------

Hash-map insertion is expected `O(1)`. For a truly streaming parser, use
`BufferedReader` and process one line at a time; the returned records
still consume output memory unless records are emitted downstream.

------------------------------------------------------------------------

# Part 2 --- Merging Multiple Files

## 7. Problem statement

Implement:

``` java
List<Map<String, String>> mergeFiles(List<String> fileContents)
```

Return one merged record per customer ID.

Rules:

1.  Reuse `parseRecords()` from Part 1.
2.  Use `id` as the stable customer identifier.
3.  Skip records without an ID.
4.  Parse `version` as an integer; missing version defaults to `0`.
5.  Keep `id`, but exclude `version` from the final document.
6.  Preserve fields absent from newer records.
7.  For a field supplied by multiple records, higher version wins.
8.  For equal versions, later input order wins.

### Input

**File 1**

``` text
id: 1
name: John
email: john@old.com
version: 1

id: 2
name: Jane
version: 1
```

**File 2**

``` text
id: 1
email: john@new.com
phone: 555-1234
version: 2

id: 3
name: Bob
version: 1
```

### Expected output

``` java
[
    {"id": "1", "name": "John", "email": "john@new.com", "phone": "555-1234"},
    {"id": "2", "name": "Jane"},
    {"id": "3", "name": "Bob"}
]
```

For customer `1`, `name` is preserved because File 2 does not mention
it; `email` is replaced by the newer value; `phone` is added.

This is a **union of fields with version-aware conflict resolution**.

## 8. Part 2 approach

Maintain:

1.  `mergedRecords`: customer ID → merged document.
2.  `fieldVersions`: customer ID → (field name → version of the value
    currently stored).

For each parsed record:

-   Skip if `id` is absent or blank.
-   Read its version (default `0`).
-   Get or create the target document.
-   For each field other than `id` and `version`:
    -   Add it if it has never been seen.
    -   Otherwise replace it only if incoming version is greater than or
        equal to the version stored for that field.
    -   Update the field's version when accepting the value.

`>=` means later input wins when versions tie.

### Correctness note: track versions per field

The reference page presents a simpler customer-level `best_version`
approach. That works for its basic example, but it can fail for
out-of-order partial records because a newer record may omit a field.

Consider:

``` text
Record A: id=1, version=3, city=London
Record B: id=1, version=5, name=John
Record C: id=1, version=4, city=Paris
```

The newest record overall is version 5, but it does not supply `city`.
The newest record that supplies `city` is version 4, so the correct
merged city is `Paris`.

A single customer-level maximum can incorrectly reject version 4's city.
Per-field version tracking implements the stated "newest value wins for
each field" rule correctly. If an interviewer explicitly requests the
reference's simpler record-level behavior, explain the distinction and
follow the agreed contract.

## 9. Part 2 --- Java solution

``` java
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LegacyDataMerger {

    public static List<Map<String, String>> mergeFiles(
            List<String> fileContents) {

        // customer ID -> merged document
        Map<String, Map<String, String>> mergedRecords =
                new LinkedHashMap<>();

        // customer ID -> (field -> version of the stored field value)
        Map<String, Map<String, Integer>> fieldVersions =
                new LinkedHashMap<>();

        for (String content : fileContents) {
            List<Map<String, String>> records =
                    LegacyDataParser.parseRecords(content);

            for (Map<String, String> record : records) {
                String id = record.get("id");

                if (id == null || id.isBlank()) {
                    continue;
                }

                int version = parseVersion(record.get("version"));

                Map<String, String> target =
                        mergedRecords.computeIfAbsent(id, ignored -> {
                            Map<String, String> doc = new LinkedHashMap<>();
                            doc.put("id", id);
                            return doc;
                        });

                Map<String, Integer> versionsForCustomer =
                        fieldVersions.computeIfAbsent(
                                id, ignored -> new LinkedHashMap<>());

                for (Map.Entry<String, String> entry : record.entrySet()) {
                    String field = entry.getKey();
                    String value = entry.getValue();

                    if (field.equals("id") || field.equals("version")) {
                        continue;
                    }

                    Integer existingVersion = versionsForCustomer.get(field);

                    // First value, newer value, or later record at equal version.
                    if (existingVersion == null || version >= existingVersion) {
                        target.put(field, value);
                        versionsForCustomer.put(field, version);
                    }
                }
            }
        }

        return new ArrayList<>(mergedRecords.values());
    }

    private static int parseVersion(String versionText) {
        if (versionText == null || versionText.isBlank()) {
            return 0;
        }
        return Integer.parseInt(versionText.trim());
    }
}
```

### Implementation observations

-   `LinkedHashMap` preserves the order in which customer IDs are first
    encountered.
-   `computeIfAbsent()` creates a document or field-version map only
    when needed.
-   Each field has its own version. A newer record omitting `email` does
    not change the version associated with the existing email value.
-   `version >= existingVersion` implements later-input-wins for ties.
-   `id` is retained; `version` is control metadata and is omitted.
-   Invalid versions currently throw `NumberFormatException`. A
    production parser should explicitly define whether to reject,
    report, or default invalid versions.

## 10. Part 2 complexity

Let `N` be total input characters and `F` the total number of parsed
field occurrences.

  ---------------------------------------------------------------------------
  Metric                                  Complexity Reason
  --------------------- ---------------------------- ------------------------
  Time                          `O(N + F)` expected, Parse all input and
                                  effectively `O(N)` process every field
                                                     once; hash-map
                                                     operations are expected
                                                     `O(1)`.

  Output space                                `O(U)` `U` is the total size of
                                                     merged documents.

  Version metadata                            `O(P)` `P` is the number of
                                                     distinct
                                                     `(customer ID, field)`
                                                     pairs.

  Parser transient           `O(N)` per file in this `split()` and parsed
  space                               implementation records are materialized
                                                     for each file.
  ---------------------------------------------------------------------------

A streaming parser can reduce intermediate memory. The final merged
documents and field-version metadata still need to remain available
unless the merge is externalized to a database or another storage
system.

------------------------------------------------------------------------

# 11. Combined usage example

The parser and merger can be kept as separate classes in the same
package.

``` java
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        String file1 = """
                id: 1
                name: John
                email: john@old.com
                version: 1

                id: 2
                name: Jane
                version: 1
                """;

        String file2 = """
                id: 1
                email: john@new.com
                phone: 555-1234
                version: 2

                id: 3
                name: Bob
                version: 1
                """;

        List<Map<String, String>> parsed =
                LegacyDataParser.parseRecords(file1);

        System.out.println("Parsed records:");
        parsed.forEach(System.out::println);

        List<Map<String, String>> merged =
                LegacyDataMerger.mergeFiles(List.of(file1, file2));

        System.out.println("\\nMerged records:");
        merged.forEach(System.out::println);
    }
}
```

**Java version:** Text blocks (`"""`) require Java 15+. For Java 8/11,
use ordinary strings with `\n` or load test data from files.

------------------------------------------------------------------------

# 12. Edge cases and tests

## Part 1 --- Parsing

  Case                           Expected behavior
  ------------------------------ ---------------------------------------
  Empty input                    Empty list
  Only blank lines               Empty list
  Only comments                  Empty list
  Leading/trailing spaces        Trim key and value
  Different key casing           Normalize to lowercase
  Malformed line without colon   Skip
  Value contains colon           Preserve everything after first colon
  Duplicate key                  Last value wins
  Empty key (`: value`)          Ignore
  Empty value (`Phone:`)         Store empty string
  No trailing blank line         Flush final record
  LF and CRLF line endings       Both supported by `\\R`

## Part 2 --- Merging

  -----------------------------------------------------------------------
  Case                                Expected behavior
  ----------------------------------- -----------------------------------
  Same ID in several files            One merged document

  Newer version changes a field       Newer value wins

  Newer version omits a field         Preserve older value

  Newer version adds a field          Add it

  Versions arrive out of order        Highest version wins per field

  Equal versions                      Later input wins

  Missing ID                          Skip record

  Missing version                     Treat as `0` under this contract

  Invalid version                     Current implementation throws;
                                      clarify policy

  Multiple records with same ID in    Merge using the same version rules
  one file                            

  IDs differ only by case             Treated as different IDs here;
                                      clarify normalization
  -----------------------------------------------------------------------

### High-value out-of-order test

``` text
File 1:
id: 1
city: London
version: 3

File 2:
id: 1
name: John
version: 5

File 3:
id: 1
city: Paris
version: 4
```

Expected:

``` java
{"id": "1", "city": "Paris", "name": "John"}
```

The highest version supplying `city` is 4; the highest version supplying
`name` is 5.

------------------------------------------------------------------------

# 13. Important design observations

## A. Keep parsing and merging separate

`parseRecords()` turns raw text into structured records. `mergeFiles()`
reconciles structured records. This decomposition makes parsing reusable
and makes each part independently testable.

## B. Split only at the first delimiter

Values can contain colons. Splitting at every colon corrupts URLs and
other values.

## C. Define malformed-data behavior

Skipping malformed lines is one valid policy, not the only one.
Production ingestion should often collect rejected lines with file name,
line number, original content, and reason.

## D. Define duplicate-key behavior

This solution uses last occurrence wins within one record. An
alternative could reject duplicates or retain all occurrences.

## E. Clarify merge semantics

"Merge" is ambiguous. It might mean whole-document replacement, field
union, per-field versioning, or explicit null/deletion handling. State
the exact contract.

## F. Define null and empty-value semantics

Here, an empty string is a real supplied value. A missing field means
"no update." If the source supports explicit nulls or delete markers,
define whether they clear a field or remove a document.

## G. Make tie-breaking deterministic

Later input wins when versions are equal. If files are processed in
parallel or arbitrary order, input order may not be a stable business
rule. Consider a source priority, timestamp, or sequence number.

## H. Consider identifier normalization

Field keys are lowercased, but IDs are case-sensitive in this
implementation. Normalize IDs only if the business domain says they are
case-insensitive.

## I. Memory usage matters

This solution loads each file as a string, splits it into lines, parses
records, and retains merged output. It is suitable for interview-sized
inputs, but may not scale to multi-gigabyte exports.

------------------------------------------------------------------------

# 14. Production-oriented follow-ups

### Follow-up 1: Stream large files

Use `BufferedReader` or accept a `Reader`:

``` java
List<Map<String, String>> parseRecords(Reader reader)
```

Read one line at a time and build the current record. This avoids
materializing the full line array. For very large data, emit records to
a consumer or merge incrementally.

### Follow-up 2: Report malformed lines

Return valid records and rejected-line information (line number, raw
text, reason). This makes ingestion auditable.

### Follow-up 3: Export NDJSON

Write one JSON document per line for bulk ingestion. Use a JSON library
such as Jackson for correct escaping and serialization; do not manually
concatenate JSON strings.

### Follow-up 4: Use per-field versions

The implementation already does this. A richer model could store
version, timestamp, and source priority per field.

### Follow-up 5: Use a real CSV or JSON parser when appropriate

Never parse CSV by naively splitting on commas: quoted commas, escaped
quotes, and embedded newlines break that approach. Use a CSV library.
Use a JSON parser for JSON.

### Follow-up 6: Support deletion

Define the difference between: - Missing field: preserve old value. -
Empty string: set the field to empty. - Explicit null: possibly clear
the field. - Delete marker: possibly remove a field or entire document.

------------------------------------------------------------------------

# 15. MongoDB connection

The problem models a practical data-ingestion pipeline:

``` text
Legacy text files
       |
       v
Parse and normalize
       |
       v
Validate / report rejected records
       |
       v
Merge and reconcile by ID
       |
       v
Convert to JSON/BSON documents
       |
       v
Bulk import into MongoDB
```

A parsed record resembles a document:

``` json
{
  "id": "1",
  "name": "John",
  "email": "john@example.com"
}
```

The Java `Map<String, String>` is only a simplified interview
representation. Real MongoDB documents can contain nested objects,
arrays, numbers, dates, booleans, and nulls; production ingestion needs
schema-aware type conversion.

The merge behavior resembles an upsert/reconciliation workflow, but do
not assume a database merge operation automatically implements this
exact per-field version policy. The application must define and enforce
its conflict semantics.

------------------------------------------------------------------------

# 16. Interview strategy

### Before coding

-   Confirm input type and record boundaries.
-   Ask how comments and malformed lines should be handled.
-   Clarify duplicate keys and delimiter behavior.
-   Define customer identity and version conflict rules.
-   State assumptions and get agreement.

### While coding Part 1

Say:

-   "I'll use a single pass over the lines."
-   "The current map is the parser state."
-   "I'll split on the first colon so values can contain colons."
-   "I'll flush the final record after the loop because there may be no
    trailing blank line."

### While coding Part 2

Say:

-   "I'll reuse the parser rather than duplicate parsing logic."
-   "I'll index merged documents by ID."
-   "A newer record should update only fields it actually supplies."
-   "I'll track versions per field so out-of-order partial updates are
    handled correctly."
-   "For equal versions, later input order is the tie-breaker."

### Before finishing

Walk through empty input, comments-only input, no trailing blank line,
duplicate fields, values containing colons, missing IDs, missing/invalid
versions, out-of-order versions, and equal-version conflicts.

### What the interviewer evaluates

1.  Requirement clarification and communication.
2.  Practical string parsing.
3.  Correct use of maps and collections.
4.  Separation of parsing from business logic.
5.  Thoughtful conflict resolution.
6.  Edge-case awareness.
7.  Time and space complexity reasoning.
8.  Awareness of memory and streaming trade-offs.

------------------------------------------------------------------------

# 17. Quick revision sheet

  -----------------------------------------------------------------------------
  Area                                Key takeaway
  ----------------------------------- -----------------------------------------
  Problem type                        Messy text parsing + record
                                      reconciliation

  Part 1 API                          `parseRecords(String content)`

  Part 1 structure                    `List<Map<String, String>>`

  Record boundary                     Blank line

  Comment                             Trimmed line starts with `#`

  Key normalization                   Trim + lowercase using `Locale.ROOT`

  Value normalization                 Trim; preserve case

  Key/value split                     First colon only

  Malformed line                      Skip under stated assumption

  Duplicate key                       Last occurrence wins

  Final record                        Flush after loop

  Part 1 time                         `O(N)`

  Part 2 API                          `mergeFiles(List<String> fileContents)`

  Record identity                     `id`

  Conflict resolution                 Highest version per field

  Equal version                       Later input wins

  Missing field in newer record       Preserve older value

  Control metadata                    Keep `id`, omit `version`

  Part 2 time                         `O(N)` expected

  Main structures                     `LinkedHashMap` for documents and field
                                      versions

  Scalability extension               `BufferedReader`, streaming output,
                                      rejected-line report

  Main interview signal               Clarify vague requirements before coding
  -----------------------------------------------------------------------------

## Final takeaway

This question tests practical engineering judgment more than a clever
algorithm:

1.  Parse messy text into clean records in one linear pass.
2.  Keep parsing separate from merging.
3.  Reconcile documents by a stable ID.
4.  Resolve conflicts using explicit version semantics.
5.  Handle malformed input and edge cases deliberately.
6.  Recognize when streaming and error reporting are needed for
    production-scale files.

**Most important interview habit: clarify the contract first, state your
assumptions, and then write the simplest implementation that satisfies
that contract.**
