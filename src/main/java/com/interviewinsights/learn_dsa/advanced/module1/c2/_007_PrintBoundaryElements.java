package com.interviewinsights.learn_dsa.advanced.module1.c2;

import java.util.ArrayList;
import java.util.List;

/**
 * # 7. Print Boundary Elements
 *
 * ## Problem statement
 *
 * Given an {@code N x N} matrix, print all of its boundary elements in
 * clockwise order, starting from the top-left element.
 *
 * Each boundary element should be printed exactly once. For a square matrix,
 * the traversal is:
 *
 * 1. Top row, from left to right.
 * 2. Right column, from top to bottom.
 * 3. Bottom row, from right to left.
 * 4. Left column, from bottom to top.
 *
 * ## Example
 *
 * <pre>
 * Input:  A = [[ 1,  2,  3,  4,  5],
 *              [ 6,  7,  8,  9, 10],
 *              [11, 12, 13, 14, 15],
 *              [16, 17, 18, 19, 20],
 *              [21, 22, 23, 24, 25]]
 *
 * Output: 1 2 3 4 5 10 15 20 25 24 23 22 21 16 11 6
 * </pre>
 *
 * ## Constraints
 *
 * - The matrix is square with dimensions {@code N x N}.
 * - {@code N >= 1}.
 *
 * ## Expected result
 *
 * Print the boundary elements in clockwise order.
 *
 * ## Important observations
 *
 * - The traversal can be divided into four straight sides: top, right,
 *   bottom, and left.
 * - Each side is traversed for {@code N - 1} elements so that corners are not
 *   printed twice.
 * - A {@code 1 x 1} matrix is a special case because all four sides refer to
 *   the same element.
 *
 * ## Complexity
 *
 * Time: {@code O(N)}. The four loops together visit exactly {@code 4N - 4}
 * boundary positions for {@code N > 1}, which is linear in {@code N}.
 *
 * Space: {@code O(N)}. The result list stores the {@code 4N - 4} boundary
 * elements. Apart from the returned/output collection, the traversal uses
 * {@code O(1)} extra space.
 */

public class _007_PrintBoundaryElements {

    private static void printBoundaryElements(int[][] matrix, int r, int c, int n) {
        List<Integer> answer = new ArrayList<>();

        // Traverse the top edge from left to right.
        for (int i = 1; i <= n - 1; i++) {
            answer.add(matrix[r][c]);
            c++;
        }

        // Traverse the right edge from top to bottom.
        for (int i = 1; i <= n - 1; i++) {
            answer.add(matrix[r][c]);
            r++;
        }

        // Traverse the bottom edge from right to left.
        for (int i = 1; i <= n - 1; i++) {
            answer.add(matrix[r][c]);
            c--;
        }

        // Traverse the left edge from bottom to top.
        for (int i = 1; i <= n - 1; i++) {
            answer.add(matrix[r][c]);
            r--;
        }

        // For a 1 x 1 matrix, the four edge loops do not visit any element.
        if (n == 1) {
            answer.add(matrix[r][c]);
        }

        System.out.println(answer);
    }

    public static void main(String[] args) {
        int[][] matrix = {
                {1, 2, 3, 4, 5},
                {6, 7, 8, 9, 10},
                {11, 12, 13, 14, 15},
                {16, 17, 18, 19, 20},
                {21, 22, 23, 24, 25}
        };

        int n = matrix.length;

        // Implement your solution method, then uncomment this call.
         printBoundaryElements(matrix, 0, 0, n);
        // Expected output: 1 2 3 4 5 10 15 20 25 24 23 22 21 16 11 6
    }
}
