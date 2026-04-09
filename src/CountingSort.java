/**
 * Classic Counting Sort Algorithm — Algorithm 1 from the paper.
 *
 * This is the PRIMARY BASELINE used in all three benchmark tables.
 *
 * Time Complexity : O(n + r)       r = max value in the array
 * Space Complexity: O(n + r)
 *
 * Cache behaviour: When the input is random (large r), accesses to count[] and
 * output[] are non-linear, causing many cache misses. When the input is sorted,
 * accesses are sequential and cache-friendly — this gap motivates the paper.
 */
public class CountingSort {
    /**
     * Sorts the entire array using classic counting sort (Algorithm 1).
     * @param arr array of non-negative integers */
    public static void sort(int[] arr) {
        int n = arr.length;
        if (n == 0) return;

        // find the range maximum
        int r = getMax(arr, 0, n - 1);

        int[] output = new int[n];
        int[] count  = new int[r + 1];

        // count occurrences of each value
        for (int i = 0; i < n; i++) {
            count[arr[i]]++;
        }

        // prefix sum — count[i] now holds the last position of value i
        for (int i = 1; i <= r; i++) {
            count[i] += count[i - 1];
        }

        // place each element in the correct output position (right-to-left for stability)
        for (int i = n - 1; i >= 0; i--) {
            output[count[arr[i]] - 1] = arr[i];
            count[arr[i]]--;
        }

        // copy sorted output back into arr
        System.arraycopy(output, 0, arr, 0, n);
    }

    /**
     * Sorts a sub-array arr[low..high] using counting sort with a value offset.
     * Called by HybridSort to sort each leaf partition independently.
     *
     * Using an offset (local min) keeps the count[] array small — this is the
     * key mechanism that makes counting sort cache-friendly for each partition.
     *
     * @param arr  the array containing the sub-array
     * @param low  inclusive start index
     * @param high inclusive end index
     */
    public static void sortRange(int[] arr, int low, int high) {
        if (low >= high) return;

        // Determine the local min and max for this partition
        int min = arr[low], max = arr[low];
        for (int i = low + 1; i <= high; i++) {
            if (arr[i] < min) min = arr[i];
            if (arr[i] > max) max = arr[i];
        }

        int range = max - min + 1;   // size of count[] for this partition
        int size = high - low + 1;  // number of elements in this partition

        int[] count  = new int[range];
        int[] output = new int[size];

        // Count occurrences, offsetting values by min so count[] starts at index 0
        for (int i = low; i <= high; i++) {
            count[arr[i] - min]++;
        }

        // Prefix sum
        for (int i = 1; i < range; i++) {
            count[i] += count[i - 1];
        }

        // Build output (right-to-left for stability)
        for (int i = high; i >= low; i--) {
            output[--count[arr[i] - min]] = arr[i];
        }

        // Copy sorted partition back into arr at its original position
        System.arraycopy(output, 0, arr, low, size);
    }

    // Returns the maximum value in arr[low..high]
    public static int getMax(int[] arr, int low, int high) {
        int max = arr[low];
        for (int i = low + 1; i <= high; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }

    // Returns the minimum value in arr[low..high]
    public static int getMin(int[] arr, int low, int high) {
        int min = arr[low];
        for (int i = low + 1; i <= high; i++) {
            if (arr[i] < min) {
                min = arr[i];
            }
        }
        return min;
    }
}
