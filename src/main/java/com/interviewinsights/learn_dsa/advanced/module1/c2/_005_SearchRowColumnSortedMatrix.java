package com.interviewinsights.learn_dsa.advanced.module1.c2;

/**
 * # 5. Search in a Row-wise and Column-wise Sorted Matrix
 *
 * ## Problem statement
 *
 * Given an `m x n` integer matrix with the following properties:
 *
 * - Each row is sorted in non-decreasing order from left to right.
 * - Each column is sorted in non-decreasing order from top to bottom.
 *
 * Given an integer `target`, return `true` if `target` appears in the matrix; otherwise, return `false`.
 *
 * Design an algorithm with better performance than checking every matrix element. An `O(m + n)` solution is expected.
 *
 * ## Examples
 *
 * ### Example 1
 *
 * ```text
 * Input:  matrix = [[1,4,7,11,15], [2,5,8,12,19], [3,6,9,16,22], [10,13,14,17,24], [18,21,23,26,30]], target = 5
 * Output: true
 * ```
 *
 * ### Example 2
 *
 * ```text
 * Input:  matrix = [[1,4,7,11,15], [2,5,8,12,19], [3,6,9,16,22], [10,13,14,17,24], [18,21,23,26,30]], target = 20
 * Output: false
 * ```
 *
 * ## Constraints
 *
 * - `1 <= m, n <= 300`
 * - `-10^9 <= matrix[i][j], target <= 10^9`
 * - Every row is sorted in non-decreasing order.
 * - Every column is sorted in non-decreasing order.
 *
 * ## Expected result
 *
 * Return whether the target value exists in the matrix.
 *
 * ## Important observation
 *
 * Start at the top-right corner. From this position, moving left always
 * decreases the value and moving down always increases it:
 *
 * - If the current value is greater than the target, the entire current
 *   column below it is also too large, so move left.
 * - If the current value is smaller than the target, the entire current row
 *   to its left is also too small, so move down.
 * - If the current value equals the target, the search is complete.
 *
 * Each move eliminates one complete row or column, so no matrix element is
 * visited more than once.
 *
 * ## Complexity
 *
 * Time: {@code O(m + n)}. There can be at most {@code m} downward moves and
 * {@code n} leftward moves.
 *
 * Space: {@code O(1)}. The search uses only row and column indices.
 */

public class _005_SearchRowColumnSortedMatrix {

    private static boolean searchMatrix(int[][] matrix, int presentTarget) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        // Begin at the top-right corner, where both directions are useful.
        int r = 0, c = cols - 1;

        while (r < rows && c >= 0) {
            int current = matrix[r][c];

            // The target is found at the current position.
            if (current == presentTarget) {
                return true;
            // Everything below current in this column is even larger.
            } else if (current > presentTarget) {
                c--;
            // Everything to the left of current in this row is even smaller.
            } else {
                r++;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        int[][] matrix = {
                {1, 4, 7, 11, 15},
                {2, 5, 8, 12, 19},
                {3, 6, 9, 16, 22},
                {10, 13, 14, 17, 24},
                {18, 21, 23, 26, 30}
        };

        int presentTarget = 5;
        int absentTarget = 20;

        // Implement your search method, then uncomment these calls.
         System.out.println(searchMatrix(matrix, presentTarget)); // expected: true
         System.out.println(searchMatrix(matrix, absentTarget));  // expected: false
    }
}
