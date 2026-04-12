/**
 * Hybrid QuickSort + InsertionSort algorithm.
 *
 * Baseline T2 in Table 3 of the paper. The paper includes this as a second
 * comparison point to show that simply hybridising quicksort with another sort
 * (insertion sort) is less effective than the proposed quicksort + counting sort hybrid.
 *
 * Strategy: Run standard quicksort recursively, but switch to insertion sort once
 * the sub-array size drops below INSERTION_THRESHOLD.
 *
 * Time Complexity : O(n log n) average
 * Space Complexity: O(log n) stack space
 */
public class QuickWithInsertionSort {
    // Sub-array size below which insertion sort is used instead of quicksort.
    private static final int INSERTION_THRESHOLD = 10;

    public static void sort(int[] arr) {
        if (arr.length <= 1) return;
        quickSortHybrid(arr, 0, arr.length - 1);
    }

    private static void quickSortHybrid(int[] arr, int low, int high) {
        if (high - low < INSERTION_THRESHOLD) {
            insertionSort(arr, low, high);
            return;
        }

        int pivotIndex = QuickSort.partition(arr, low, high);
        quickSortHybrid(arr, low, pivotIndex - 1);
        quickSortHybrid(arr, pivotIndex + 1, high);
    }

    /**
     * Insertion sort on arr[low..high].
     * Efficient for small, nearly-sorted sub-arrays.
     */
    private static void insertionSort(int[] arr, int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            int key = arr[i];
            int j   = i - 1;
            while (j >= low && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }
}
