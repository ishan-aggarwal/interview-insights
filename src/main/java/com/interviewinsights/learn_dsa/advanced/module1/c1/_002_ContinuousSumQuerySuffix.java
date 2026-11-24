package com.interviewinsights.learn_dsa.advanced.module1.c1;

import java.util.Arrays;

/**
 * Q2. Continuous Sum Query – Suffix Updates
 * <p>
 * Problem Description
 * <p>
 * There are A beggars sitting in a row outside a temple. Each beggar initially
 * has an empty pot.
 * <p>
 * Devotees donate a fixed amount of P coins to every beggar starting from index
 * L until the last beggar in the row.
 * <p>
 * For each devotee, you are given the starting index L and the donation amount P.
 * The donation is applied to all beggars from L to A.
 * <p>
 * Find the final amount of money in each beggar's pot after all donations.
 * <p>
 * The input is given as a 2D integer array B, where:
 * <p>
 * B[i][0] = L
 * B[i][1] = P
 * <p>
 * Constraints
 * <p>
 * 1 <= A <= 2 * 10^5
 * 1 <= L <= A
 * 1 <= P <= 10^3
 * 0 <= length(B) <= 10^5
 * <p>
 * Input Format
 * <p>
 * The first argument is an integer A, representing the number of beggars.
 * The second argument is a 2D integer array B, where each row contains:
 * [L, P]
 * <p>
 * Output Format
 * <p>
 * Return an integer array of size A containing the final amount of money in
 * each beggar's pot using 0-based indexing.
 * <p>
 * Example Input
 * <p>
 * A = 5
 * B = [[1, 10], [3, 20], [2, 25]]
 * <p>
 * Example Output
 * <p>
 * 10 35 55 55 55
 * <p>
 * Example Explanation
 * <p>
 * The first devotee gives 10 coins to beggars from index 1 to 5:
 * [10, 10, 10, 10, 10]
 * <p>
 * The second devotee gives 20 coins to beggars from index 3 to 5:
 * [10, 10, 30, 30, 30]
 * <p>
 * The third devotee gives 25 coins to beggars from index 2 to 5:
 * [10, 35, 55, 55, 55]
 */

// Important observations:
// 1. Every query adds its value to the beggar at position L and all beggars
//    to its right. Therefore, it is enough to store the donation only at L.
// 2. A prefix sum propagates each stored donation to all positions on its
//    right, which converts the suffix updates into the final pot amounts.
// 3. Since the input index L is 1-based and Java arrays are 0-based, use L - 1.

// TC - O(A + Q), where Q is the number of queries
// SC - O(A)

public class _002_ContinuousSumQuerySuffix {

    public static int[] solve(int a, int[][] queries) {
        int[] ans = new int[a];
        int queriesLength = queries.length;

        // Store each donation at its 0-based starting position.
        // The prefix-sum pass below will propagate it to the suffix.
        for (int idx = 0; idx < queriesLength; idx++) {
            int pos = queries[idx][0] - 1;
            int val = queries[idx][1];
            ans[pos] += val;
        }

        // Calculate prefix sums to apply every donation from its starting
        // position through the last beggar.
        for (int idx = 1; idx < ans.length; idx++) {
            ans[idx] += ans[idx - 1];
        }

        return ans;
    }

    public static void main(String[] args) {
        int a = 5;
        int[][] b = {{1, 10}, {3, 20}, {2, 25}};
        System.out.println(Arrays.toString(solve(a, b)));
    }
}
