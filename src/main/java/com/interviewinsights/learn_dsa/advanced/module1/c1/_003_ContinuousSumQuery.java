package com.interviewinsights.learn_dsa.advanced.module1.c1;

import java.util.Arrays;

/**
 * Q2. Continuous Sum Query
 * Problem Description
 * There are A beggars sitting in a row outside a temple. Each beggar initially has an empty pot. When the devotees come to the temple, they donate some amount of coins to these beggars. Each devotee gives a fixed amount of coin(according to their faith and ability) to some K beggars sitting next to each other.
 * Given the amount P donated by each devotee to the beggars ranging from L to R index, where 1 <= L <= R <= A, find out the final amount of money in each beggar's pot at the end of the day, provided they don't fill their pots by any other means.
 * For ith devotee B[i][0] = L, B[i][1] = R, B[i][2] = P, given by the 2D array B
 * Problem Constraints
 * 1 <= A <= 2 * 105
 * 1 <= L <= R <= A
 * 1 <= P <= 103
 * 0 <= len(B) <= 105
 * Input Format
 * The first argument is a single integer A.
 * The second argument is a 2D integer array B.
 * Output Format
 * Return an array(0 based indexing) that stores the total number of coins in each beggars pot.
 * Example Input
 * Input 1:-
 * A = 5
 * B = [[1, 2, 10], [2, 3, 20], [2, 5, 25]]
 * Example Output
 * Output 1:-
 * 10 55 45 25 25
 * Example Explanation
 * Explanation 1:-
 * First devotee donated 10 coins to beggars ranging from 1 to 2. Final amount in each beggars pot after first devotee: [10, 10, 0, 0, 0]
 * Second devotee donated 20 coins to beggars ranging from 2 to 3. Final amount in each beggars pot after second devotee: [10, 30, 20, 0, 0]
 * Third devotee donated 25 coins to beggars ranging from 2 to 5. Final amount in each beggars pot after third devotee: [10, 55, 45, 25, 25]
 */

// Important observations:
// 1. Updating every beggar from L to R for each query can take O(A * Q) time.
// 2. Use a difference array: add P at L and subtract P immediately after R.
// 3. A prefix sum converts these boundary markers into the final amounts.
// 4. Query indexes are 1-based, so convert the starting index using L - 1.

// TC - O(A + Q), where Q is the number of queries
// SC - O(A)

public class _003_ContinuousSumQuery {

    public static int[] solve(int size, int[][] queries) {
        int queriesLength = queries.length;
        int[] ans = new int[size];

        for (int idx = 0; idx < queriesLength; idx++) {
            int startIndex = queries[idx][0] - 1;
            int endIndex = queries[idx][1];
            int value = queries[idx][2];

            // Start applying this donation at the left boundary.
            ans[startIndex] += value;

            // Stop applying it immediately after the right boundary.
            // endIndex is the first position after the range in 0-based form.
            if (endIndex < size) {
                ans[endIndex] -= value;
            }
        }

        // Propagate all range updates using prefix sums.
        for (int idx = 1; idx < size; idx++) {
            ans[idx] += ans[idx - 1];
        }

        return ans;
    }

    public static void main(String[] args) {
        // Input 1
        int a = 5;
        int[][] b = {{1, 2, 10}, {2, 3, 20}, {2, 5, 25}};

        // Expected output: [10, 55, 45, 25, 25]
        System.out.println(Arrays.toString(solve(a, b)));
    }
}
