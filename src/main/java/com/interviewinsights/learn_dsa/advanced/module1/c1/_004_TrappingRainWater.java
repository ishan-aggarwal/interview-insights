package com.interviewinsights.learn_dsa.advanced.module1.c1;

/**
 * # 4. Trapping Rain Water
 * <p>
 * ## Problem statement
 * <p>
 * Given an array of `n` non-negative integers representing an elevation map, where the width of each bar is `1`, compute how much rainwater can be trapped after it rains.
 * <p>
 * ## Examples
 * <p>
 * ### Example 1
 * <p>
 * ```text
 * Input:  height = [0,1,0,2,1,0,1,3,2,1,2,1]
 * Output: 6
 * ```
 * <p>
 * The elevation map traps `6` units of rainwater.
 * <p>
 * ### Example 2
 * <p>
 * ```text
 * Input:  height = [4,2,0,3,2,5]
 * Output: 9
 * ```
 * <p>
 * ## Constraints
 * <p>
 * - `n == height.length`
 * - `1 <= n <= 2 * 10^4`
 * - `0 <= height[i] <= 10^5`
 * <p>
 * ## Expected result
 * <p>
 * Return the total number of units of rainwater trapped between the bars.
 * <p>
 * ## Approach
 * <p>
 * For each bar, the water above it is determined by the shorter of the tallest
 * bars on its left and right. Store those two maxima in arrays, then add the
 * water held above each bar.
 * <p>
 * ## Complexity
 * <p>
 * Time: {@code O(n)} — three linear scans of the input.
 * <br>
 * Space: {@code O(n)} — two auxiliary arrays storing left and right maxima.
 */

public class _004_TrappingRainWater {
    public static int solve(int[] heights) {
        int ans = 0;
        int size = heights.length;
        int[] lMax = new int[size];
        int[] rMax = new int[size];

        // lMax[idx] is the tallest bar from index 0 through idx.
        lMax[0] = heights[0];
        for (int idx = 1; idx < size; idx++) {
            lMax[idx] = Math.max(heights[idx], lMax[idx - 1]);
        }

        // rMax[idx] is the tallest bar from index idx through the last index.
        rMax[size - 1] = heights[size - 1];
        for (int idx = size - 2; idx >= 0; idx--) {
            rMax[idx] = Math.max(heights[idx], rMax[idx + 1]);
        }

        // Water above a bar is bounded by the shorter side maximum.
        for (int idx = 1; idx < size - 1; idx++) {
            int waterLevel = Math.min(lMax[idx], rMax[idx]);
            ans += waterLevel - heights[idx];
        }

        return ans;
    }

    public static void main(String[] args) {
        int[] heights = {4, 2, 0, 3, 2, 5};
        System.out.println(solve(heights));
    }
}

