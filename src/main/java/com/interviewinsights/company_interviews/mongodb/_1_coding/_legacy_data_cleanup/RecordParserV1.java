package com.interviewinsights.company_interviews.mongodb._1_coding._legacy_data_cleanup;

import java.util.*;

class RecordParserV1 {
    public static List<Map<String, String>> parseRecords(String content) {
        List<Map<String, String>> records = new ArrayList<>();
        Map<String, String> current = new LinkedHashMap<>();

        for (String rawLine : content.split("\r?\n", -1)) {
            String line = rawLine.strip();

            if (line.isEmpty()) {                       // blank line -> record boundary
                if (!current.isEmpty()) {
                    records.add(current);
                    current = new LinkedHashMap<>();
                }
                continue;
            }
            if (line.startsWith("#")) continue;         // comment -> ignore

            int idx = line.indexOf(':');
            if (idx == -1) continue;                    // malformed -> skip

            String key = line.substring(0, idx).strip().toLowerCase();
            String value = line.substring(idx + 1).strip();
            if (!key.isEmpty()) current.put(key, value);
        }

        if (!current.isEmpty()) records.add(current);
        return records;
    }
}
