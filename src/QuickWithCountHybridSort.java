/**
 * Proposed Hybrid Sorting Algorithm — "Improving Counting Sort via Data Locality".
 * ==============
 * OVERVIEW
 * ==============
 *
 * The core insight: counting sort runs 2–3× faster on sorted than on random input
 * because sorted input produces sequential (cache-friendly) accesses to count[] and
 * output[]. The proposed algorithm artificially creates this "best case" condition by
 * partitioning the array first.
 *
 * The paper uses C = 1000
 * Larger C → fewer quicksort recursions but larger partitions (may overflow cache).
 * Smaller C → more recursions (preprocessing overhead) but better cache utilisation.
 *
 * =============
 * COMPLEXITY
 * =============
 *
 * Preprocessing (Modified QS): O(n log n) in the average case
 * Counting sort phase        : O(n + r) total across all partitions
 * Overall                    : O(n log n + n + r)  — dominated by the QS step
 */
public class QuickWithCountHybridSort {
    /**
     * Stopping threshold for the modified quicksort preprocessing step.
     * Recursion halts when (maxValue − minValue) + (high − low) ≤ C.
     * The paper uses C = 1000 in all experiments.
     */
    public static final int C = 1000;

    /**
     * Nanosecond timing for the two phases — set after each call to sort().
     * Used by BenchmarkAnalysis to report preprocessing time and counting sort time
     */
    public static long preprocessingTimeNs = 0;
    public static long countingSortTimeNs  = 0;

    /**
     * Sorts arr[] using the proposed hybrid algorithm.
     * @param arr array of non-negative integers
     */
    public static void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            preprocessingTimeNs = 0;
            countingSortTimeNs = 0;
            return;
        }

        // Reset timers
        preprocessingTimeNs = 0;
        countingSortTimeNs = 0;

        long startPreprocess = System.nanoTime();

        // 1. Find global min and max to kick off the recursion
        int min = arr[0];
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < min) {
                min = arr[i];
            } else if (arr[i] > max) {
                max = arr[i];
            }
        }

        preprocessingTimeNs += (System.nanoTime() - startPreprocess);

        // 2. Start the modified quicksort
        modifiedQuickSort(arr, 0, arr.length - 1, max, min);
    }

    /**
     * Modified QuickSort — Algorithm 2 from the paper.
     *
     * Partitions arr[low..high] recursively. Stops when the sub-partition satisfies
     * the cache-friendliness condition: (maxValue − minValue) + (high − low) ≤ C.
     * @param arr      the array being sorted
     * @param low      inclusive lower bound of current sub-array
     * @param high     inclusive upper bound of current sub-array
     * @param maxValue maximum value known to exist in arr[low..high]
     * @param minValue minimum value known to exist in arr[low..high]
     */
    private static void modifiedQuickSort(int[] arr, int low, int high,
                                          int maxValue, int minValue) {
        if (low >= high) return;

        // Condition for Cache-Friendliness: Stop QS and switch to Counting Sort
        if ((maxValue - minValue) + (high - low) <= C) {
            long startCS = System.nanoTime();
            countingSortLocal(arr, low, high, minValue, maxValue);
            countingSortTimeNs += (System.nanoTime() - startCS);
            return;
        }

        long startQS = System.nanoTime();
        int pivotIndex = partitionMedianOfThree(arr, low, high);
        int midValue = arr[pivotIndex];
        preprocessingTimeNs += (System.nanoTime() - startQS);

        // Left partition: values are <= pivot side
        if (low  < pivotIndex - 1) {
            modifiedQuickSort(arr, low, pivotIndex - 1, midValue, minValue);
        } else if (low ==  pivotIndex - 1) {
            long startCS = System.nanoTime();
            countingSortLocal(arr, low, pivotIndex - 1, minValue, midValue);
            countingSortTimeNs += (System.nanoTime() - startCS);
        }

        // Right partition
        if (pivotIndex + 1 < high) {
            modifiedQuickSort(arr, pivotIndex + 1, high, maxValue, midValue);
        } else if (pivotIndex + 1 == high) {
            long startCS = System.nanoTime();
            countingSortLocal(arr, pivotIndex + 1, high, midValue, maxValue);
            countingSortTimeNs += (System.nanoTime() - startCS);
        }
    }

    /**
     * Classic Counting Sort applied locally to a cache-friendly partition.
     */
    private static void countingSortLocal(int[] arr, int low, int high, int min, int max) {
        if (low >= high) return;

        int range = max - min + 1;
        int size = high - low + 1;

        int[] count = new int[range];
        int[] output = new int[size];

        // 1. Count occurrences (shifted by min)
        for (int i = low; i <= high; i++) {
            count[arr[i] - min]++;
        }

        // 2. Accumulate counts
        for (int i = 1; i < range; i++) {
            count[i] += count[i - 1];
        }

        // 3. Build the output array (backward traversal to maintain stability if needed)
        for (int i = high; i >= low; i--) {
            output[--count[arr[i] - min]] = arr[i];
        }

        // 4. Copy back to the original array in the correct partition
        System.arraycopy(output, 0, arr, low, size);
    }

    public static int partitionMedianOfThree(int[] arr, int low, int high) {
        int pivotIndex = medianOfThreeIndex(arr, low, high);
        swap(arr, pivotIndex, high);

        int pivot = arr[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                swap(arr, i, j);
            }
        }

        swap(arr, i + 1, high);
        return i + 1;
    }

    private static int medianOfThreeIndex(int[] arr, int low, int high) {
        int mid = low + (high - low) / 2;

        if (arr[low] > arr[mid]) {
            swap(arr, low, mid);
        }
        if (arr[low] > arr[high]) {
            swap(arr, low, high);
        }
        if (arr[mid] > arr[high]) {
            swap(arr, mid, high);
        }

        return mid;
    }

    /**
     * Helper: Swaps two elements in the array.
     */
    private static void swap(int[] arr, int i, int j) {
        if (i != j) {
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }
}