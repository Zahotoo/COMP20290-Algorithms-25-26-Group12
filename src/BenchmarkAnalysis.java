import java.util.Arrays;
import java.util.Random;

/**
 * Benchmark Analysis for the paper:
 * "Improving Counting Sort Algorithm Via Data Locality"
 *
 * This benchmark is organized to more closely reflect the paper:
 *
 * Table 1 style:
 *   - Classic Counting Sort on random input vs sorted input
 *
 * Table 2 style:
 *   - Counting Sort with preprocessing vs without preprocessing
 *   - Worst-style condition: r >> n
 *
 * Table 3 style:
 *   - QuickSort
 *   - QuickSort + Insertion Sort
 *   - Proposed QuickSort + Counting Sort hybrid
 *   - Best-style condition: n = r
 *
 * Extra analysis is also included:
 *   - averages over multiple runs
 *   - speedup ratios
 *   - detailed preprocessing / counting phase breakdown for the proposed method
 *
 * Notes:
 * - Fixed random seeds are used for reproducibility.
 * - Java Arrays.sort() is used only for correctness verification, not as a paper baseline.
 */
public class BenchmarkAnalysis {

    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 5;

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("      BENCHMARK: Improving Counting Sort Algorithm Via Data Locality");
        System.out.println("=========================================================================\n");

        warmup();

        runTable1Experiment();
        runTable2Experiment();
        runTable3Experiment();

        runAverageOverviewExperiment();
    }

    // =========================================================================
    // TABLE 1 STYLE
    // =========================================================================

    /**
     * Table 1 in the paper:
     * Compare classic counting sort on random input vs sorted input.
     *
     * Paper settings:
     * n = r = 1,000,000 / 2,000,000 / 3,000,000
     */
    private static void runTable1Experiment() {
        System.out.println("TABLE 1 STYLE EXPERIMENT");
        System.out.println("Classic Counting Sort on Random Input vs Sorted Input");
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("%12s %12s %18s %18s %16s%n",
                "n", "r", "Random T1 (ms)", "Sorted T2 (ms)", "Speedup T1/T2");

        int[] sizes = {1_000_000, 2_000_000, 3_000_000};

        for (int n : sizes) {
            int r = n;

            double randomTime = averageTimeMsClassicCountingRandom(n, r);
            double sortedTime = averageTimeMsClassicCountingSorted(n);
            double speedup = randomTime / sortedTime;

            System.out.printf("%12s %12s %18.3f %18.3f %16.2f%n",
                    formatInt(n), formatInt(r), randomTime, sortedTime, speedup);
        }
        System.out.println();
    }

    private static double averageTimeMsClassicCountingRandom(int size, int maxRange) {
        double total = 0.0;

        for (int run = 0; run < MEASURED_RUNS; run++) {
            int[] arr = generateRandomArray(size, maxRange, 1000L + run);
            int[] expected = Arrays.copyOf(arr, arr.length);
            Arrays.sort(expected);

            long start = System.nanoTime();
            CountingSort.sort(arr);
            long end = System.nanoTime();

            verifySort(expected, arr, "Classic Counting Sort (Random)");
            total += (end - start) / 1_000_000.0;
        }

        return total / MEASURED_RUNS;
    }

    private static double averageTimeMsClassicCountingSorted(int size) {
        double total = 0.0;

        for (int run = 0; run < MEASURED_RUNS; run++) {
            int[] arr = generateSortedArray(size);
            int[] expected = Arrays.copyOf(arr, arr.length);

            long start = System.nanoTime();
            CountingSort.sort(arr);
            long end = System.nanoTime();

            verifySort(expected, arr, "Classic Counting Sort (Sorted)");
            total += (end - start) / 1_000_000.0;
        }

        return total / MEASURED_RUNS;
    }

    // =========================================================================
    // TABLE 2 STYLE
    // =========================================================================

    /**
     * Table 2 in the paper:
     * Running times for counting sort with and without preprocessing.
     *
     * Paper settings:
     * r = 1,000,000
     * n = 1000 / 2000 / 3000
     */
    private static void runTable2Experiment() {
        System.out.println("TABLE 2 STYLE EXPERIMENT");
        System.out.println("Counting Sort with and without Preprocessing (r >> n)");
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("%12s %12s %18s %18s %18s %18s%n",
                "n", "r", "Preprocess (ms)", "Local Count (ms)", "Hybrid Total", "Classic Count");

        int r = 1_000_000;
        int[] sizes = {1_000, 2_000, 3_000};

        for (int n : sizes) {
            Result hybrid = averageTimeProposedHybrid(n, r, 2000L);
            double classic = averageTimeClassicCounting(n, r, 3000L);

            System.out.printf("%12s %12s %18.3f %18.3f %18.3f %18.3f%n",
                    formatInt(n), formatInt(r),
                    hybrid.preprocessMs, hybrid.countingMs, hybrid.totalMs, classic);
        }
        System.out.println();
    }

    // =========================================================================
    // TABLE 3 STYLE
    // =========================================================================

    /**
     * Table 3 in the paper:
     * Compare quicksort, quicksort + insertion sort, and proposed algorithm.
     *
     * Paper settings:
     * n = r = 1,000,000 / 2,000,000
     */
    private static void runTable3Experiment() {
        System.out.println("TABLE 3 STYLE EXPERIMENT");
        System.out.println("QuickSort vs QuickSort+InsertionSort vs Proposed Hybrid (n = r)");
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("%12s %18s %18s %18s%n",
                "n = r", "QuickSort", "Quick+Insertion", "Proposed Hybrid");

        int[] sizes = {1_000_000, 2_000_000};

        for (int n : sizes) {
            int r = n;

            double quick = averageTimeQuickSort(n, r, 4000L);
            double quickInsertion = averageTimeQuickInsertion(n, r, 5000L);
            Result proposed = averageTimeProposedHybrid(n, r, 6000L);

            System.out.printf("%12s %18.3f %18.3f %18.3f%n",
                    formatInt(n), quick, quickInsertion, proposed.totalMs);
        }
        System.out.println();
    }


    private static void runAverageOverviewExperiment() {
        System.out.println("AVERAGE OVERVIEW EXPERIMENT");
        System.out.println("n,Hybrid,QuickSort,Quick+Insertion,JavaSort,MergeSort");

        int[] sizes = {1000, 2000, 5000, 10000, 15000, 20000,
                30000, 40000, 50000, 70000, 100000,
                150000, 200000, 300000, 500000};
        int maxRange = 500000;

        for (int n : sizes) {

            double hybrid = averageTimeProposedHybrid(n, maxRange, 1000L).totalMs;
            double quick = averageTimeQuickSort(n, maxRange, 2000L);
            double quickInsert = averageTimeQuickInsertion(n, maxRange, 3000L);
            double javaSort = averageTimeJavaSort(n, maxRange, 4000L);
            double merge = averageTimeMergeSort(n, maxRange, 5000L);

            System.out.printf("%d,%.3f,%.3f,%.3f,%.3f,%.3f%n",
                    n, hybrid, quick, quickInsert, javaSort, merge);
        }

        System.out.println();
    }


    // =========================================================================
    // AVERAGE TIME HELPERS
    // =========================================================================

    private static double averageTimeClassicCounting(int size, int maxRange, long seedBase) {
        double total = 0.0;

        for (int run = 0; run < MEASURED_RUNS; run++) {
            int[] arr = generateRandomArray(size, maxRange, seedBase + run);
            int[] expected = Arrays.copyOf(arr, arr.length);
            Arrays.sort(expected);

            long start = System.nanoTime();
            CountingSort.sort(arr);
            long end = System.nanoTime();

            verifySort(expected, arr, "Classic Counting Sort");
            total += (end - start) / 1_000_000.0;
        }

        return total / MEASURED_RUNS;
    }

    private static double averageTimeQuickSort(int size, int maxRange, long seedBase) {
        double total = 0.0;

        for (int run = 0; run < MEASURED_RUNS; run++) {
            int[] arr = generateRandomArray(size, maxRange, seedBase + run);
            int[] expected = Arrays.copyOf(arr, arr.length);
            Arrays.sort(expected);

            long start = System.nanoTime();
            QuickSort.sort(arr);
            long end = System.nanoTime();

            verifySort(expected, arr, "Classic Quick Sort");
            total += (end - start) / 1_000_000.0;
        }

        return total / MEASURED_RUNS;
    }

    private static double averageTimeQuickInsertion(int size, int maxRange, long seedBase) {
        double total = 0.0;

        for (int run = 0; run < MEASURED_RUNS; run++) {
            int[] arr = generateRandomArray(size, maxRange, seedBase + run);
            int[] expected = Arrays.copyOf(arr, arr.length);
            Arrays.sort(expected);

            long start = System.nanoTime();
            QuickWithInsertionSort.sort(arr);
            long end = System.nanoTime();

            verifySort(expected, arr, "Quick + Insertion Sort");
            total += (end - start) / 1_000_000.0;
        }

        return total / MEASURED_RUNS;
    }

    private static Result averageTimeProposedHybrid(int size, int maxRange, long seedBase) {
        double totalMs = 0.0;
        double preprocessMs = 0.0;
        double countingMs = 0.0;

        for (int run = 0; run < MEASURED_RUNS; run++) {
            int[] arr = generateRandomArray(size, maxRange, seedBase + run);
            int[] expected = Arrays.copyOf(arr, arr.length);
            Arrays.sort(expected);

            long start = System.nanoTime();
            QuickWithCountHybridSort.sort(arr);
            long end = System.nanoTime();

            verifySort(expected, arr, "Proposed Hybrid Sort");

            totalMs += (end - start) / 1_000_000.0;
            preprocessMs += QuickWithCountHybridSort.preprocessingTimeNs / 1_000_000.0;
            countingMs += QuickWithCountHybridSort.countingSortTimeNs / 1_000_000.0;
        }

        return new Result(
                totalMs / MEASURED_RUNS,
                preprocessMs / MEASURED_RUNS,
                countingMs / MEASURED_RUNS
        );
    }

    private static double averageTimeJavaSort(int size, int maxRange, long seedBase) {
        double total = 0.0;

        for (int run = 0; run < MEASURED_RUNS; run++) {
            int[] arr = generateRandomArray(size, maxRange, seedBase + run);
            int[] expected = Arrays.copyOf(arr, arr.length);
            Arrays.sort(expected);

            long start = System.nanoTime();
            Arrays.sort(arr);
            long end = System.nanoTime();

            verifySort(expected, arr, "Java Sort");
            total += (end - start) / 1_000_000.0;
        }

        return total / MEASURED_RUNS;
    }

    private static class MergeSort {

        public static void sort(int[] arr) {
            mergeSort(arr, 0, arr.length - 1);
        }

        private static void mergeSort(int[] arr, int left, int right) {
            if (left >= right) return;

            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);

            merge(arr, left, mid, right);
        }

        private static void merge(int[] arr, int left, int mid, int right) {
            int[] temp = new int[right - left + 1];

            int i = left, j = mid + 1, k = 0;

            while (i <= mid && j <= right) {
                if (arr[i] <= arr[j]) temp[k++] = arr[i++];
                else temp[k++] = arr[j++];
            }

            while (i <= mid) temp[k++] = arr[i++];
            while (j <= right) temp[k++] = arr[j++];

            System.arraycopy(temp, 0, arr, left, temp.length);
        }
    }

    private static double averageTimeMergeSort(int size, int maxRange, long seedBase) {
        double total = 0.0;

        for (int run = 0; run < MEASURED_RUNS; run++) {
            int[] arr = generateRandomArray(size, maxRange, seedBase + run);
            int[] expected = Arrays.copyOf(arr, arr.length);
            Arrays.sort(expected);

            long start = System.nanoTime();
            MergeSort.sort(arr);
            long end = System.nanoTime();

            verifySort(expected, arr, "Merge Sort");
            total += (end - start) / 1_000_000.0;
        }

        return total / MEASURED_RUNS;
    }

    // =========================================================================
    // DATA GENERATION
    // =========================================================================

    /**
     * Generates random non-negative integer data in [0, maxRange].
     */
    private static int[] generateRandomArray(int size, int maxRange, long seed) {
        Random random = new Random(seed);
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(maxRange + 1);
        }
        return arr;
    }

    /**
     * Generates sorted data: 0, 1, 2, ..., size-1
     * This is used to reproduce the sorted-input side of Table 1.
     */
    private static int[] generateSortedArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = i;
        }
        return arr;
    }

    // =========================================================================
    // VALIDATION / WARMUP / UTIL
    // =========================================================================

    private static void verifySort(int[] expected, int[] actual, String algName) {
        if (!Arrays.equals(expected, actual)) {
            throw new IllegalStateException("[ERROR] " + algName + " failed to sort correctly.");
        }
    }

    private static void warmup() {
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_RUNS; i++) {
            int[] dummy = generateRandomArray(100_000, 50_000, 12345L + i);

            int[] a1 = Arrays.copyOf(dummy, dummy.length);
            int[] a2 = Arrays.copyOf(dummy, dummy.length);
            int[] a3 = Arrays.copyOf(dummy, dummy.length);
            int[] a4 = Arrays.copyOf(dummy, dummy.length);

            CountingSort.sort(a1);
            QuickSort.sort(a2);
            QuickWithInsertionSort.sort(a3);
            QuickWithCountHybridSort.sort(a4);
        }
        System.out.println("Warmup complete.\n");
    }

    private static String formatInt(int value) {
        return String.format("%,d", value);
    }

    /**
     * Helper record-like container for proposed hybrid timing.
     */
    private static class Result {
        double totalMs;
        double preprocessMs;
        double countingMs;

        Result(double totalMs, double preprocessMs, double countingMs) {
            this.totalMs = totalMs;
            this.preprocessMs = preprocessMs;
            this.countingMs = countingMs;
        }
    }
}