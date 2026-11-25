package com.interviewinsights.learn_dsa.advanced.module1.c2;

/**
 * # 6. Find the Row with the Maximum Number of Ones
 *
 * ## Problem statement
 *
 * Given a binary sorted matrix {@code A[N][N]}, find the row containing the
 * maximum number of {@code 1}s.
 *
 * Every row is sorted in non-decreasing order, so all {@code 0}s appear before
 * all {@code 1}s in that row. If multiple rows contain the same maximum number
 * of {@code 1}s, return the lower row index.
 *
 * ## Examples
 *
 * Example 1:
 *
 * <pre>
 * Input:  A = [[0, 1, 1],
 *              [0, 1, 1],
 *              [0, 0, 1]]
 * Output: 0
 * </pre>
 *
 * Example 2:
 *
 * <pre>
 * Input:  A = [[0, 0, 0, 0],
 *              [0, 0, 0, 1],
 *              [0, 0, 1, 1],
 *              [0, 1, 1, 1]]
 * Output: 3
 * </pre>
 *
 * ## Constraints
 *
 * - The matrix is binary and square.
 * - Every row is sorted by value.
 * - Return the smallest row index in case of a tie.
 *
 * ## Important observation
 *
 * Since every row is sorted, all {@code 1}s form a suffix of that row. Start
 * at the top-right corner and move only left or down:
 *
 * - Moving left over a {@code 1} means the current row has one more {@code 1}
 *   than rows that cannot reach that column.
 * - When a {@code 0} is reached, the current row cannot improve the answer at
 *   the current boundary, so move to the next row.
 * - The column pointer never moves right. This also preserves the first row
 *   in case of a tie, as required.
 *
 * ## Complexity
 *
 * Time: {@code O(N)}. The row pointer moves down at most {@code N} times and
 * the column pointer moves left at most {@code N} times. Together, there are
 * at most {@code 2N} pointer moves.
 *
 * Space: {@code O(1)}. Only row, column, and answer variables are used.
 */
public class _006_FindRowWithMaxOnes {

    private static int findRowWithMaxOnes(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int r = 0, c = cols - 1;
        int ans = -1;

        while (r < rows && c >= 0) {
            // Because the row is sorted, keep moving left when the val == 1.
            while (c >= 0 && matrix[r][c] == 1) {
                // This row has more leading 1s than the previous best row.
                ans = r;
                c--;
            }

            // At 0, this row cannot extend farther left than the current best.
            r++;
        }
        return ans;
    }


    public static void main(String[] args) {
        int[][] firstMatrix = {
                {0, 1, 1},
                {0, 1, 1},
                {0, 0, 1}
        };

        int[][] secondMatrix = {
                {0, 0, 0, 0},
                {0, 0, 0, 1},
                {0, 0, 1, 1},
                {0, 1, 1, 1}
        };

        // Implement your solution method, then uncomment these calls.
         System.out.println(findRowWithMaxOnes(firstMatrix));  // expected: 0
         System.out.println(findRowWithMaxOnes(secondMatrix)); // expected: 3
    }
}
