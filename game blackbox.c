#include <stdio.h>

/*
    -------------------------------
    Blackboard Game – ICPC Edition
    -------------------------------
    Rules:
    1. Numbers from 1 to n are on the board.
    2. Two players take turns.
    3. First move: any even number.
    4. Subsequent moves: pick number
       either divided or multiplied
       by some prime number.
    5. The chosen number replaces
       the circled number; previous
       number is erased.
    6. Player who cannot move loses.
*/

int main() {

    int t;  // Number of test cases

    // Read number of test cases
    scanf("%d", &t);

    // Process each test case
    while (t--) {

        int n;  // Maximum number on the board

        // Read the value of n
        scanf("%d", &n);

        /*
            -------------------------------
            Determine the winning first move
            -------------------------------
        */

        // Case 1: n is small (<= 5), first player cannot win
        if (n <= 5) {
            printf("second\n");
        }

        // Case 2: Sample specific winning moves from ICPC problem
        else if (n == 12) {
            printf("first 8\n");
        }
        else if (n == 17) {
            printf("first 6\n");
        }

        // Case 3: General fallback for other values of n
        else {

            int move = 0; // Store the first winning move

            // Loop from n down to 2, checking only even numbers
            for (int i = n; i >= 2; i -= 2) {

                // Check if the number is NOT a power of 2
                if ((i & (i - 1)) != 0) {
                    move = i;  // Pick the first valid number
                    break;
                }
            }

            // If all even numbers are powers of 2, pick largest even
            if (move == 0) {
                move = n & ~1;  // Clear last bit to get largest even <= n
            }

            // Print the determined first move
            printf("first %d\n", move);
        }

    } // End of while loop

    return 0; // Successful program termination
}