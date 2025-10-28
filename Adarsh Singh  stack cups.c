#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <time.h>   // for clock(), CLOCKS_PER_SEC

int main() {
    clock_t start, end;
    start = clock();  // Start time measurement

    int64_t N, H;
    while (scanf("%lld %lld", &N, &H) == 2) {
        int *used = (int *)calloc(N + 1, sizeof(int));  // initialize with 0

        if (N >= 4 && H == 2 * N + 1) {
            printf("%lld %d %lld", 2 * N - 1, 3, 2 * N - 3);
            used[2] = 1;
            used[N] = 1;
            used[N - 1] = 1;
        } else if (H < 2 * N - 1 || H == 2 * N + 1 || H == N * N - 2 || H > N * N) {
            printf("impossible\n");
            free(used);
            continue;
        } else {
            for (int i = N; i >= 1; i--) {
                if (H >= 2 * i - 1 && H != 2 * i + 1) {
                    H -= 2 * i - 1;
                    used[i] = 1;
                }
            }

            int first = 1;
            for (int i = 1; i <= N; i++) {
                if (used[i]) {
                    if (!first) printf(" ");
                    first = 0;
                    printf("%d", 2 * i - 1);
                }
            }
        }

        for (int i = N; i >= 1; i--) {
            if (!used[i]) printf(" %d", 2 * i - 1);
        }
        printf("\n");
        free(used);
    }

    end = clock();  // End time measurement
    double time_taken = ((double)(end - start)) / CLOCKS_PER_SEC;

    printf("\nExecution Time: %.6f seconds\n", time_taken);

    return 0;
}