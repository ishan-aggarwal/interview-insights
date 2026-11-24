package com.interviewinsights.company.mongodb._1_coding._inverted_index;

import java.util.*;


public class InvertedIndex {

    // Forward index: term -> documents containing the term
    private final Map<String, Set<String>> index;

    // Reverse index: document -> unique terms in the document
    private final Map<String, Set<String>> documentTerms;

    public InvertedIndex() {
        this.index = new HashMap<>();
        this.documentTerms = new HashMap<>();
    }

    // Tokenize and normalize a document
    private Set<String> tokenize(String text) {
        Set<String> terms = new HashSet<>();

        String[] words = text.toLowerCase()
                .split("[^\\p{L}\\p{N}]+"); // could be \\W+, \\s+

        for (String word : words) {
            if (word != null && !word.isEmpty()) {
                terms.add(word);
            }
        }

        return terms;
    }

    // Normalize a single search term
    private String normalizeTerm(String term) {
        Set<String> terms = tokenize(term);

        if (terms.size() != 1) {
            throw new IllegalArgumentException(
                    "Search must contain exactly one term"
            );
        }

        return terms.iterator().next();
    }

    // 1. Insert a document
    public void insert(String doc) {

        // Treat inserting the same document again as a no-op
        if (documentTerms.containsKey(doc)) {
            return;
        }

        Set<String> terms = tokenize(doc);

        documentTerms.put(doc, terms);

        for (String term : terms) {

            if (!index.containsKey(term)) {
                index.put(term, new HashSet<>());
            }
            Set<String> set = index.get(term);
            set.add(doc);

//            index.computeIfAbsent(
//                    term, k -> new HashSet<>()
//            ).add(doc);
        }
    }

    // 2. Search documents containing a term
    public List<String> search(String term) {

        String normalized = normalizeTerm(term);

        Set<String> documents = index.get(normalized);

        if (documents == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(documents);
    }

    // 3. Delete a document
    public void delete(String doc) {

        Set<String> terms = documentTerms.remove(doc);

        if (terms == null) {
            return;
        }

        for (String term : terms) {

            Set<String> documents = index.get(term);

            documents.remove(doc);

            // Remove terms that no longer have documents
            if (documents.isEmpty()) {
                index.remove(term);
            }
        }
    }

    // 4. Search documents containing BOTH terms
    public List<String> andSearch(
            String term1, String term2) {

        String t1 = normalizeTerm(term1);
        String t2 = normalizeTerm(term2);

        Set<String> set1 = index.get(t1);
        Set<String> set2 = index.get(t2);

        if (set1 == null || set2 == null) {
            return Collections.emptyList();
        }

        // Iterate over the smaller posting list
        if (set1.size() > set2.size()) {
            Set<String> temp = set1;
            set1 = set2;
            set2 = temp;
        }

        List<String> result = new ArrayList<>();

        for (String doc : set1) {
            if (set2.contains(doc)) {
                result.add(doc);
            }
        }

        return result;
    }

    public static void main(String[] args) {

        InvertedIndex invertedIndex = new InvertedIndex();

        invertedIndex.insert(
                "Java is a programming language"
        );

        invertedIndex.insert(
                "Java is used for backend development"
        );

        invertedIndex.insert(
                "Python is a programming language"
        );

        invertedIndex.insert(
                "Java and Python are popular languages"
        );

        System.out.println(
                "Search java: " +
                        invertedIndex.search("java")
        );

        System.out.println(
                "Search Python: " +
                        invertedIndex.search("Python")
        );

        System.out.println(
                "Java AND Python: " +
                        invertedIndex.andSearch("Java", "Python")
        );

        invertedIndex.delete(
                "Java is a programming language"
        );

        System.out.println(
                "After deleting Doc1, search Java: " +
                        invertedIndex.search("Java")
        );
    }
}
