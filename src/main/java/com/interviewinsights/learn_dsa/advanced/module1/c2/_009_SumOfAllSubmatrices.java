package com.interviewinsights.learn_dsa.advanced.module1.c2;

/**
 * # 9. Sum of All Submatrices
 * <p>
 * ## Problem statement
 * <p>
 * Given an {@code R x C} matrix, determine the sum of all possible contiguous
 * submatrices.
 * <p>
 * A submatrix is formed by selecting a contiguous range of rows and a
 * contiguous range of columns. Include every possible submatrix exactly once.
 * <p>
 * ## Example
 *
 * <pre>
 * Input:  A = [[ 4,  9,  6],
 *              [ 5, -1,  2]]
 *
 * Output: 166
 * </pre>
 * <p>
 * The answer is the sum of the elements in all submatrices of the input
 * matrix, including the individual cells and the complete matrix.
 * <p>
 * ## Constraints
 * <p>
 * - {@code 1 <= R, C <= 1000}
 * - Matrix values are integers.
 * <p>
 * ## Expected result
 * <p>
 * Return the sum of all possible contiguous submatrices.
 * <p>
 * ## Key insight
 * <p>
 * Instead of generating every submatrix, count how many submatrices contain
 * each cell. For a cell at {@code (i, j)}:
 * <p>
 * - There are {@code (i + 1) * (j + 1)} choices for the submatrix's top-left
 *   corner.
 * - There are {@code (R - i) * (C - j)} choices for its bottom-right corner.
 *
 * Therefore, the cell contributes its value multiplied by the product of
 * those two counts. Summing this contribution for every cell gives the sum of
 * all submatrices.
 * <p>
 * ## Complexity
 * <p>
 * Time: {@code O(R * C)}. Each matrix cell is processed once.
 * <p>
 * Space: {@code O(1)} auxiliary space. No submatrices or prefix-sum arrays
 * are created.
 */

// key insights
// occurrence of an element at ith index in an array of size n
// (i - 0 + 1) * (n - 1 - i + 1)
// (i + 1) * (n - i)

// occurrence of an element at [i,j] pos in a matrix of size n, m
// top -> (i - 0 + 1) * (j - 0 + 1) = (i + 1) * (j + 1)
// bottom -> (n - 1 - i + 1) * (m - 1 - j + 1) = (n - i) * (m - j)
// so total times = top * bottom

public class _009_SumOfAllSubmatrices {

    private static long sumOfAllSubmatrices(int[][] matrix) {
        long ans = 0;
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Choose the top-left corner: it can be anywhere above or
                // to the left of (i, j), including the cell itself.
                long topLeftChoices = (long) (i + 1) * (j + 1);

                // Choose the bottom-right corner: it can be anywhere below or
                // to the right of (i, j), including the cell itself.
                long bottomRightChoices = (long) (rows - i) * (cols - j);

                // This cell appears in every combination of those choices.
                ans += matrix[i][j] * topLeftChoices * bottomRightChoices;
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        int[][] matrix = {
                {4, 9, 6},
                {5, -1, 2}
        };

        System.out.println(sumOfAllSubmatrices(matrix)); // expected: 166
    }
}
