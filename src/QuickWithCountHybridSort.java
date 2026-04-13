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
            if (arr[i] < min) min = arr[i];
            else if (arr[i] > max) max = arr[i];
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

        // Median-of-Three for better pivot selection
        int mid = low + (high - low) / 2;
        int pivot = medianOfThree(arr, low, mid, high);

        // Standard Hoare/Lomuto partitioning
        int i = low;
        int j = high;
        while (i <= j) {
            while (arr[i] < pivot) i++;
            while (arr[j] > pivot) j--;
            if (i <= j) {
                swap(arr, i, j);
                i++;
                j--;
            }
        }

        preprocessingTimeNs += (System.nanoTime() - startQS);

        // Recursively sort the left partition
        if (low < j) {
            startQS = System.nanoTime();
            int leftMin = arr[low];
            int leftMax = arr[low];
            // Find exact min/max for the left partition to ensure precise threshold checking
            for (int k = low + 1; k <= j; k++) {
                if (arr[k] < leftMin) leftMin = arr[k];
                else if (arr[k] > leftMax) leftMax = arr[k];
            }
            preprocessingTimeNs += (System.nanoTime() - startQS);
            modifiedQuickSort(arr, low, j, leftMax, leftMin);
        }

        // Recursively sort the right partition
        if (i < high) {
            startQS = System.nanoTime();
            int rightMin = arr[i];
            int rightMax = arr[i];
            // Find exact min/max for the right partition
            for (int k = i + 1; k <= high; k++) {
                if (arr[k] < rightMin) rightMin = arr[k];
                else if (arr[k] > rightMax) rightMax = arr[k];
            }
            preprocessingTimeNs += (System.nanoTime() - startQS);
            modifiedQuickSort(arr, i, high, rightMax, rightMin);
        }
    }

    /**
     * Classic Counting Sort applied locally to a cache-friendly partition.
     */
    private static void countingSortLocal(int[] arr, int low, int high, int min, int max) {
        int range = max - min + 1;
        int[] count = new int[range];
        int[] output = new int[high - low + 1];

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
            output[count[arr[i] - min] - 1] = arr[i];
            count[arr[i] - min]--;
        }

        // 4. Copy back to the original array in the correct partition
        for (int i = 0; i < output.length; i++) {
            arr[low + i] = output[i];
        }
    }

    /**
     * Helper: Selects the median of the first, middle, and last elements to use as pivot.
     */
    private static int medianOfThree(int[] arr, int low, int mid, int high) {
        if (arr[low] > arr[mid]) swap(arr, low, mid);
        if (arr[low] > arr[high]) swap(arr, low, high);
        if (arr[mid] > arr[high]) swap(arr, mid, high);
        return arr[mid];
    }

    /**
     * Helper: Swaps two elements in the array.
     */
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}