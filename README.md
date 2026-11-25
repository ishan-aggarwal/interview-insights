# Interview Insights

Questions, experiences, and practical solutions for technical interviews.

The repository is organized into three major parts:

## `learn_dsa`

Data structures and algorithms, organized by difficulty and module.

| # | Problem                               | Key observation                                                                                                                                   | Time&nbsp;Complexity | Space&nbsp;Complexity | Links                                                                                                            |
|--:|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------:|:---------------------:|------------------------------------------------------------------------------------------------------------------|
| 1 | Maximum subarray sum                  | A negative running sum cannot improve a future subarray, so restart at the next element.                                                          |        `O(N)`        |        `O(1)`         | [Solution](src/main/java/com/interviewinsights/learn_dsa/advanced/module1/c1/_001_MaxSubArraySum.java)           |
| 2 | Continuous sum query                  | Use a difference array: add the donation at `L`, subtract it immediately after `R`, then use prefix sums to calculate each beggar's final amount. |      `O(A + Q)`      |        `O(A)`         | [Solution](src/main/java/com/interviewinsights/learn_dsa/advanced/module1/c1/_003_ContinuousSumQuery.java)       |
| 3 | Continuous sum query — suffix updates | Store each donation at its starting index `L`; a prefix sum propagates it through the suffix.                                                     |      `O(A + Q)`      |        `O(A)`         | [Solution](src/main/java/com/interviewinsights/learn_dsa/advanced/module1/c1/_002_ContinuousSumQuerySuffix.java) |
| 4 | Trapping rain water                   | The water above each bar is limited by the shorter of the tallest bars to its left and right.                                                     |        `O(N)`        |        `O(1)`         | [Solution](src/main/java/com/interviewinsights/learn_dsa/advanced/module1/c1/_004_TrappingRainWater.java)        |

The maximum subarray solution also includes a variant that returns the start and end indices of the best subarray.

## `learn_java`

Java language features and examples.

| Topic              | Resources                                                                                                                                                                                                                                                                                                                                                           |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Records            | [Notes](src/main/java/com/interviewinsights/learn_java/records/records.md) / [Employee record](src/main/java/com/interviewinsights/learn_java/records/EmployeeRecord.java) / [Equivalent class](src/main/java/com/interviewinsights/learn_java/records/EmployeeClass.java) / [Example test](src/main/java/com/interviewinsights/learn_java/records/RecordTest.java) |
| Switch expressions | [Notes](src/main/java/com/interviewinsights/learn_java/switch_expressions/switch_expressions.md)                                                                                                                                                                                                                                                                    |

## `company_interviews`

Company-specific interview questions and solutions. Current examples are from MongoDB interviews:

| Topic               | Problem statement                                                                                                                                        | Solution                                                                                                                                                                                                                                                                                                                                                              |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Inverted index      | [Question](src/main/java/com/interviewinsights/company_interviews/mongodb/_1_coding/_inverted_index/problem_inverted_index.md)                           | [Java implementation](src/main/java/com/interviewinsights/company_interviews/mongodb/_1_coding/_inverted_index/InvertedIndex.java)                                                                                                                                                                                                                                    |
| Legacy data cleanup | [Question](src/main/java/com/interviewinsights/company_interviews/mongodb/_1_coding/_legacy_data_cleanup/mongodb_question_2_legacy_data_cleanup_java.md) | [Java implementation](src/main/java/com/interviewinsights/company_interviews/mongodb/_1_coding/_legacy_data_cleanup/RecordParserV1.java)                                                                                                                                                                                                                              |
| LRU cache           | N/A                                                                                                                                                      | [Java implementation](src/main/java/com/interviewinsights/company_interviews/mongodb/_1_coding/_lru_cache/LRUCache.java)                                                                                                                                                                                                                                              |
| Versioned store     | [Question](src/main/java/com/interviewinsights/company_interviews/mongodb/_1_coding/_versioned_store/mongodb_question_1_versioned_store.md)              | [V1](src/main/java/com/interviewinsights/company_interviews/mongodb/_1_coding/_versioned_store/VersionedStoreV1.java) / [V2](src/main/java/com/interviewinsights/company_interviews/mongodb/_1_coding/_versioned_store/VersionedStoreV2.java) / [V3](src/main/java/com/interviewinsights/company_interviews/mongodb/_1_coding/_versioned_store/VersionedStoreV3.java) |

## Build

This project uses [Apache Maven](https://maven.apache.org/) and requires Java **21** or newer.

```bash
mvn verify
mvn -q exec:java -Dexec.mainClass=com.interviewinsights.Application
```

In IntelliJ IDEA, reload the Maven project and run `Application` from the gutter next to `main`.
