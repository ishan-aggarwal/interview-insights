package com.interviewinsights.learn_dsa.advanced.module1.c1;

import java.util.Arrays;

/**
 * Q1. Max Sum Contiguous Subarray
 * Find the maximum sum of contiguous non-empty subarray within an array A of length N.
 * Problem Constraints
 * 1 <= N <= 1e6
 * -1000 <= A[i] <= 1000
 * Input Format
 * The first and the only argument contains an integer array, A.
 * Output Format
 * Return an integer representing the maximum possible sum of the contiguous subarray.
 * Example Input
 * Input 1:
 * A = [1, 2, 3, 4, -10]
 * Input 2:
 * A = [-2, 1, -3, 4, -1, 2, 1, -5, 4]
 * <p>
 * Example Output
 * Output 1:
 * 10
 * Output 2:
 * 6
 * Example Explanation
 * Explanation 1:
 * The subarray [1, 2, 3, 4] has the maximum possible sum of 10.
 * Explanation 2:
 * The subarray [4,-1,2,1] has the maximum possible sum of 6.
 */

// Important observation:
// 1. Negative running sum is useless.
// 2. If the sum becomes negative, start fresh from next element

// TC - O(N) and SC - O(1)

public class _001_MaxSubArraySum {
    public static int maxSum(int[] arr) {
        int length = arr.length;
        int ans = Integer.MIN_VALUE;
        int currentSum = 0;
        int idx = 0;
        while (idx < length) {
            currentSum += arr[idx];
            if (currentSum > ans) {
                ans = currentSum;
            }
            if (currentSum < 0) {
                currentSum = 0;
            }
            idx++;
        }
        return ans;
    }

    public static int[] maxSumWithStartAndEnd(int[] arr) {
        int length = arr.length;
        int ans = Integer.MIN_VALUE;
        int currentSum = 0;
        int idx = 0;
        int startI = 0, start = -1, end = -1;

        while (idx < length) {
            currentSum += arr[idx];

            if (currentSum > ans) {
                ans = currentSum;
                // if the current window sum is higher
                // set the start and end
                start = startI; end = idx;
            }

            if (currentSum < 0) {
                currentSum = 0;
                // reset startI to next index position
                startI = idx + 1;
            }
            idx++;
        }
        return new int[]{start, end};
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, -10};
        System.out.println(maxSum(arr));
        System.out.println(Arrays.toString(maxSumWithStartAndEnd(arr)));

        int[] arr1 = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println(maxSum(arr1));
        System.out.println(Arrays.toString(maxSumWithStartAndEnd(arr1)));
    }
}
