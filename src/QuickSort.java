/**
 * Classic QuickSort with median-of-three pivot selection.
 *
 * Pivot strategy: "median of three" — the median of arr[low], arr[mid], arr[high]
 * is chosen as the pivot, which avoids the O(n^2) worst case on already-sorted data.
 * This matches the pivot strategy described in the paper (Section 2.2, reference [1]).
 *
 * Partition scheme: Lomuto partition (simple and correct; pivot ends up in final position).
 *
 * Time Complexity : O(n log n) average, O(n^2) worst case
 * Space Complexity: O(log n)
 */
public class QuickSort {

    public static void sort(int[] arr) {
        if (arr.length <= 1) return;
        quickSort(arr, 0, arr.length - 1);
    }

    private static void quickSort(int[] arr, int low, int high) {
        if (low >= high) return;

        int pivotIndex = partition(arr, low, high);
        quickSort(arr, low, pivotIndex - 1);
        quickSort(arr, pivotIndex + 1, high);
    }

    /**
     * Partitions arr[low..high] using median-of-three pivot selection
     * After partitioning:
     *   - arr[low..pivotIdx-1] <= arr[pivot]
     *   - arr[pivotIdx+1..high] >= arr[pivot]
     * @return the final index of the pivot element
     */
    public static int partition(int[] arr, int low, int high) {
        int pivotIndex = medianOfThree(arr, low, high);
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

    private static int medianOfThree(int[] arr, int low, int high) {
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

    /** Swaps two elements in an array. */
    private static void swap(int[] arr, int i, int j) {
        int tmp;
        if (i != j) {
            tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }
}
