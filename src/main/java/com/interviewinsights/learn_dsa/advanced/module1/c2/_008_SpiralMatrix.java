package com.interviewinsights.learn_dsa.advanced.module1.c2;

import java.util.ArrayList;
import java.util.List;

/**
 * # 8. Spiral Matrix
 * <p>
 * ## Problem statement
 * <p>
 * Given an {@code N x N} matrix, print all elements in spiral order,
 * beginning at the top-left corner and moving clockwise.
 * <p>
 * Continue traversing the outer boundary and then each inner layer until
 * every element has been printed exactly once.
 * <p>
 * ## Example
 *
 * <pre>
 * Input:  A = [[ 1,  2,  3,  4,  5,  6],
 *              [ 7,  8,  9, 10, 11, 12],
 *              [13, 14, 15, 16, 17, 18],
 *              [19, 20, 21, 22, 23, 24],
 *              [25, 26, 27, 28, 29, 30],
 *              [31, 32, 33, 34, 35, 36]]
 *
 * Output: 1 2 3 4 5 6 12 18 24 30 36 35 34 33 32 31
 *         25 19 13 7 8 9 10 11 17 23 29 28 27 26 20 14
 *         15 16 22 21
 * </pre>
 * <p>
 * ## Constraints
 * <p>
 * - The matrix is square with dimensions {@code N x N}.
 * - {@code N >= 1}.
 * <p>
 * ## Expected result
 * <p>
 * Print every matrix element exactly once in clockwise spiral order.
 * <p>
 * ## Important observations
 * <p>
 * - The matrix can be processed one square layer at a time, starting with the
 *   outermost layer.
 * - Each layer is traversed as four sides: top, right, bottom, and left.
 * - After processing a layer, the starting row and column move inward by one,
 *   while the remaining side length decreases by two.
 * - Each side uses {@code N - 1} steps so that corner elements are not added
 *   twice. A one-cell layer is handled separately.
 *
 * ## Complexity
 * <p>
 * Time: {@code O(N^2)}. Every matrix element is visited and added exactly once.
 * <p>
 * Space: {@code O(N^2)} for the output list, which stores all {@code N^2}
 * elements. Apart from the output, the traversal uses {@code O(1)} extra space.
 */
public class _008_SpiralMatrix {

    private static List<Integer> ans = new ArrayList<>();

    private static void printSpiral(int[][] matrix, int r, int c, int n) {

        // Traverse the top edge from left to right.
        for (int i = 1; i <= n - 1; i++) {
            ans.add(matrix[r][c]);
            c++;
        }

        // Traverse the right edge from top to bottom.
        for (int i = 1; i <= n - 1; i++) {
            ans.add(matrix[r][c]);
            r++;
        }

        // Traverse the bottom edge from right to left.
        for (int i = 1; i <= n - 1; i++) {
            ans.add(matrix[r][c]);
            c--;
        }

        // Traverse the left edge from bottom to top.
        for (int i = 1; i <= n - 1; i++) {
            ans.add(matrix[r][c]);
            r--;
        }

        // A one-cell layer is not visited by the four edge traversals.
        if (n == 1) {
            ans.add(matrix[r][c]);
        }

    }

    public static void main(String[] args) {
        int[][] matrix = {
                {1, 2, 3, 4, 5, 6},
                {7, 8, 9, 10, 11, 12},
                {13, 14, 15, 16, 17, 18},
                {19, 20, 21, 22, 23, 24},
                {25, 26, 27, 28, 29, 30},
                {31, 32, 33, 34, 35, 36}
        };
        int n = matrix.length;
        int r = 0, c = 0;

        // Process the matrix from the outside layer toward the center.
        while (n >= 1) {
            printSpiral(matrix, r, c, n);
            r++;
            c++;
            n -= 2;
        }

        System.out.println(ans);

        // Implement your solution method, then uncomment this call.
        // printSpiral(matrix);
        // Expected output: 1 2 3 4 5 6 12 18 24 30 36 35 34 33 32 31
        //                  25 19 13 7 8 9 10 11 17 23 29 28 27 26 20 14
        //                  15 16 22 21
    }
}
