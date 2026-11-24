package com.interviewinsights.company.mongodb._1_coding._versioned_store;

import java.util.*;

class VersionedStoreV1 {
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
