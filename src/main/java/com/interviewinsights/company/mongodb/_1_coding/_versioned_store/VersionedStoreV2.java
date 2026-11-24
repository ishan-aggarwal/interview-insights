package com.interviewinsights.company.mongodb._1_coding._versioned_store;

import java.util.*;

class VersionedStoreV2 {
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
