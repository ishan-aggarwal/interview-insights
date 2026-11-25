package com.interviewinsights.learn_dsa.advanced.module1.c3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * # 10. Merge Sorted Overlapping Intervals
 * <p>
 * ## Problem statement
 * <p>
 * Given a sorted list of overlapping intervals, where the intervals are
 * sorted by their start time, merge all overlapping intervals and return the
 * resulting list of non-overlapping intervals sorted by start time.
 * <p>
 * Two intervals overlap when the start time of the later interval is less
 * than or equal to the end time of the current interval.
 * <p>
 * ## Example
 *
 * <pre>
 * Input:  intervals = [[0, 2], [1, 4], [5, 6], [6, 8],
 *                      [7, 10], [8, 9], [12, 14]]
 *
 * Output: [[0, 4], [5, 10], [12, 14]]
 * </pre>
 * <p>
 * ## Constraints
 * <p>
 * - {@code 1 <= intervals.length <= 10^5}
 * - Each interval contains exactly two integers: start time and end time.
 * - {@code start time <= end time} for every interval.
 * - Intervals are sorted by their start time.
 * <p>
 * ## Expected result
 * <p>
 * Return all overlapping intervals merged into a sorted list of
 * non-overlapping intervals.
 * <p>
 * ## Key insights
 * <p>
 * - Because the intervals are already sorted by start time, each interval
 *   only needs to be compared with the currently merged interval.
 * - If the current interval starts at or before the merged interval ends,
 *   the intervals overlap, so extend the end time using the larger end.
 * - Otherwise, the current merged interval is complete and can be added to
 *   the result before starting a new merged interval.
 * - The final active interval must be added after the loop.
 * <p>
 * ## Complexity
 * <p>
 * Time: {@code O(N)}. Each input interval is visited exactly once. Converting
 * the result list to an array takes {@code O(K)}, where {@code K <= N} is the
 * number of merged intervals, so the total remains {@code O(N)}.
 * <p>
 * Space: {@code O(K)} for the result, where {@code K} is the number of merged
 * intervals. In the worst case, no intervals overlap and {@code K = N}, so
 * the space complexity is {@code O(N)}.
 */
public class _010_MergeSortedOverlappingIntervals {

    private static int[][] mergeIntervals(int[][] intervals) {
        // The problem constraints guarantee at least one interval.
        int length = intervals.length;

        // [s, e] represents the merged interval currently being built.
        int s = intervals[0][0];
        int e = intervals[0][1];
        List<int[]> mergedList = new ArrayList<>();

        for (int idx = 1; idx < length; idx++) {
            int[] current = intervals[idx];

            int currentStart = current[0];
            int currentEnd = current[1];

            // Since intervals are sorted by start time, current[0] is the
            // only value needed to determine whether the intervals overlap.
            if (currentStart <= e) {
                // Extend the active interval when the intervals overlap.
                e = Math.max(e, currentEnd);
            } else {
                // The active interval cannot overlap with any later interval,
                // so store it and start tracking the current interval.
                mergedList.add(new int[]{s, e});
                s = currentStart;
                e = currentEnd;
            }
        }

        // Store the final active interval, which is not added inside the loop.
        mergedList.add(new int[]{s, e});

        // Convert List<int[]> to int[][].
        return mergedList.toArray(new int[0][]);
    }

    public static void main(String[] args) {
        int[][] intervals = {
                {0, 2},
                {1, 4},
                {5, 6},
                {6, 8},
                {7, 10},
                {8, 9},
                {12, 14}
        };

        // Implement mergeIntervals, then uncomment this call.
        int[][] mergedIntervals = mergeIntervals(intervals);
        System.out.println(Arrays.deepToString(mergedIntervals));
        // Expected output: [[0, 4], [5, 10], [12, 14]]
    }
}
