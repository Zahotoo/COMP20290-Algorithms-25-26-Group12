import java.util.Arrays;
import java.util.Random;

/**
 * Benchmark Analysis for evaluating sorting algorithms based on the paper:
 * "Improving Counting Sort Algorithm Via Data Locality"
 * * This class benchmarks and compares the execution time of 4 custom algorithms:
 * 1. Proposed Hybrid Sort (Quick Sort + Counting Sort)
 * 2. Classic Counting Sort
 * 3. Classic Quick Sort
 * 4. Hybrid Quick Sort + Insertion Sort
 * * It also includes Java's built-in Arrays.sort() (Dual-Pivot Quicksort) as a baseline.
 */
public class BenchmarkAnalysis {

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("   BENCHMARK: Improving Counting Sort Algorithm Via Data Locality");
        System.out.println("=========================================================================\n");

        // Warm up the JVM to ensure the JIT compiler optimizes the bytecode
        // before the actual timing begins. This prevents skewed results.
        warmup();

        // Test Case 1: Moderate Array Size, Moderate Range
        // Counting Sort should be very fast here, but let's see how the proposed hybrid compares.
        runBenchmark(1_000_000, 100_000);

        // Test Case 2: Large Array Size, Small Range
        // Heavy duplicates. Counting sort dominates. Quick sort might struggle if not optimized.
        runBenchmark(10_000_000, 10_000);

        // Test Case 3: Large Array Size, Huge Range
        // THE CRITICAL TEST: Classic Counting Sort will suffer severe cache misses here
        // due to the massive range. The Proposed Hybrid Sort should show its data locality advantage.
        runBenchmark(10_000_000, 10_000_000);
    }

    /**
     * Generates a random array and runs all sorting algorithms to compare execution time.
     * * @param size     The number of elements in the array (N)
     * @param maxRange The maximum value of the elements (Range: 0 to maxRange)
     */
    private static void runBenchmark(int size, int maxRange) {
        System.out.printf("Test Setup: N (Elements) = %,d | Range = [0, %,d]%n", size, maxRange);
        System.out.println("-------------------------------------------------------------------------");

        // Generate the base array to ensure all algorithms sort the exact same data
        int[] originalArray = generateRandomArray(size, maxRange);

        // ---------------------------------------------------------
        // 0. Java's Built-in Sort (Baseline)
        // ---------------------------------------------------------
        int[] arrBuiltIn = Arrays.copyOf(originalArray, originalArray.length);
        long start = System.nanoTime();
        Arrays.sort(arrBuiltIn);
        long timeBuiltIn = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-35s : %5d ms%n", "0. Java Arrays.sort() (Baseline)", timeBuiltIn);

        // ---------------------------------------------------------
        // 1. Classic Counting Sort
        // ---------------------------------------------------------
        int[] arrCounting = Arrays.copyOf(originalArray, originalArray.length);
        start = System.nanoTime();
        CountingSort.sort(arrCounting);
        long timeCounting = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-35s : %5d ms%n", "1. Classic Counting Sort", timeCounting);
        verifySort(arrBuiltIn, arrCounting, "Classic Counting Sort");

        // ---------------------------------------------------------
        // 2. Classic Quick Sort (Median-of-Three)
        // ---------------------------------------------------------
        int[] arrQuick = Arrays.copyOf(originalArray, originalArray.length);
        start = System.nanoTime();
        QuickSort.sort(arrQuick);
        long timeQuick = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-35s : %5d ms%n", "2. Classic Quick Sort", timeQuick);
        verifySort(arrBuiltIn, arrQuick, "Classic Quick Sort");

        // ---------------------------------------------------------
        // 3. Quick Sort + Insertion Sort (Hybrid 1)
        // ---------------------------------------------------------
        int[] arrQuickInsert = Arrays.copyOf(originalArray, originalArray.length);
        start = System.nanoTime();
        QuickWithInsertionSort.sort(arrQuickInsert);
        long timeQuickInsert = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-35s : %5d ms%n", "3. Quick + Insertion Sort", timeQuickInsert);
        verifySort(arrBuiltIn, arrQuickInsert, "Quick + Insertion Sort");

        // ---------------------------------------------------------
        // 4. Proposed Hybrid Sort (Quick + Counting)
        // ---------------------------------------------------------
        int[] arrProposed = Arrays.copyOf(originalArray, originalArray.length);
        start = System.nanoTime();
        QuickWithCountHybridSort.sort(arrProposed);
        long timeProposedTotal = (System.nanoTime() - start) / 1_000_000;

        // Fetch detailed timing from the hybrid class to see the breakdown
        long qsTimeMs = QuickWithCountHybridSort.preprocessingTimeNs / 1_000_000;
        long csTimeMs = QuickWithCountHybridSort.countingSortTimeNs / 1_000_000;

        System.out.printf("%-35s : %5d ms%n", "4. Proposed Hybrid Sort", timeProposedTotal);
        System.out.printf("      -> QS Preprocessing Phase     : %5d ms%n", qsTimeMs);
        System.out.printf("      -> Counting Sort Phase        : %5d ms%n", csTimeMs);
        verifySort(arrBuiltIn, arrProposed, "Proposed Hybrid Sort");

        System.out.println("=========================================================================\n");
    }

    /**
     * Helper: Generates an array of random non-negative integers.
     */
    private static int[] generateRandomArray(int size, int maxRange) {
        // Fixed seed ensures that every time you run the program,
        // the random numbers are generated in the exact same sequence.
        Random random = new Random(42);
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(maxRange + 1);
        }
        return arr;
    }

    /**
     * Helper: Verifies if the custom sorting algorithm worked correctly
     * by comparing its output to Java's built-in sort.
     */
    private static void verifySort(int[] expected, int[] actual, String algName) {
        if (!Arrays.equals(expected, actual)) {
            System.err.println("   [ERROR] " + algName + " failed to sort correctly!");
        }
    }

    /**
     * Helper: Runs a small sorting workload so the Java Virtual Machine (JVM)
     * can compile the bytecode to native machine code before the real tests begin.
     */
    private static void warmup() {
        int[] dummy = generateRandomArray(50_000, 10_000);
        CountingSort.sort(Arrays.copyOf(dummy, dummy.length));
        QuickSort.sort(Arrays.copyOf(dummy, dummy.length));
        QuickWithInsertionSort.sort(Arrays.copyOf(dummy, dummy.length));
        QuickWithCountHybridSort.sort(Arrays.copyOf(dummy, dummy.length));
        Arrays.sort(Arrays.copyOf(dummy, dummy.length));
    }
}