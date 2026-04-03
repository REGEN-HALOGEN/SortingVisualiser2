package sorting.benchmark;

/**
 * Pure sorting algorithm implementations for JMH benchmarking.
 * <p>
 * These are zero-overhead ports of the algorithms in {@code SortingAlgorithms}
 * from the visualiser. They operate directly on {@code int[]} with no
 * {@code TrackedArray}, {@code Operation} recording, or {@code SortMetrics}
 * tracking — ensuring JMH measures only the sorting work itself.
 * <p>
 * Each method sorts the given array <b>in-place</b>.
 */
public final class RawSortingAlgorithms {

    private RawSortingAlgorithms() { /* utility class */ }

    // ───────────────────────── Bubble Sort ─────────────────────────

    public static void bubbleSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    int tmp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = tmp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    // ───────────────────────── Selection Sort ─────────────────────────

    public static void selectionSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[minIdx]) {
                    minIdx = j;
                }
            }
            if (minIdx != i) {
                int tmp = a[i];
                a[i] = a[minIdx];
                a[minIdx] = tmp;
            }
        }
    }

    // ───────────────────────── Insertion Sort ─────────────────────────

    public static void insertionSort(int[] a) {
        int n = a.length;
        for (int i = 1; i < n; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    // ───────────────────────── Merge Sort ─────────────────────────

    public static void mergeSort(int[] a) {
        mergeSortRec(a, 0, a.length - 1);
    }

    private static void mergeSortRec(int[] a, int l, int r) {
        if (l >= r) return;
        int m = (l + r) / 2;
        mergeSortRec(a, l, m);
        mergeSortRec(a, m + 1, r);
        merge(a, l, m, r);
    }

    private static void merge(int[] a, int l, int m, int r) {
        int[] tmp = new int[r - l + 1];
        int i = l, j = m + 1, k = 0;
        while (i <= m && j <= r) {
            if (a[i] <= a[j]) {
                tmp[k++] = a[i++];
            } else {
                tmp[k++] = a[j++];
            }
        }
        while (i <= m) tmp[k++] = a[i++];
        while (j <= r) tmp[k++] = a[j++];
        System.arraycopy(tmp, 0, a, l, tmp.length);
    }

    // ───────────────────────── Quick Sort ─────────────────────────

    public static void quickSort(int[] a) {
        quickSortRec(a, 0, a.length - 1);
    }

    private static void quickSortRec(int[] a, int low, int high) {
        if (low < high) {
            int p = partition(a, low, high);
            quickSortRec(a, low, p - 1);
            quickSortRec(a, p + 1, high);
        }
    }

    private static int partition(int[] a, int low, int high) {
        int pivot = a[high];
        int i = low;
        for (int j = low; j < high; j++) {
            if (a[j] < pivot) {
                int tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
                i++;
            }
        }
        int tmp = a[i];
        a[i] = a[high];
        a[high] = tmp;
        return i;
    }

    // ───────────────────────── Heap Sort ─────────────────────────

    public static void heapSort(int[] a) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(a, n, i);
        }
        for (int i = n - 1; i >= 0; i--) {
            int tmp = a[0];
            a[0] = a[i];
            a[i] = tmp;
            heapify(a, i, 0);
        }
    }

    private static void heapify(int[] a, int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        if (l < n && a[l] > a[largest]) largest = l;
        if (r < n && a[r] > a[largest]) largest = r;
        if (largest != i) {
            int tmp = a[i];
            a[i] = a[largest];
            a[largest] = tmp;
            heapify(a, n, largest);
        }
    }

    // ───────────────────────── Shell Sort ─────────────────────────

    public static void shellSort(int[] a) {
        int n = a.length;
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                int key = a[i];
                int j = i;
                while (j >= gap && a[j - gap] > key) {
                    a[j] = a[j - gap];
                    j -= gap;
                }
                a[j] = key;
            }
        }
    }

    // ───────────────────────── Radix Sort (LSD, base 10) ─────────────────────────

    public static void radixSort(int[] a) {
        if (a.length == 0) return;
        int max = a[0];
        for (int i = 1; i < a.length; i++) {
            if (a[i] > max) max = a[i];
        }
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSortByDigit(a, exp);
        }
    }

    private static void countingSortByDigit(int[] a, int exp) {
        int n = a.length;
        int[] output = new int[n];
        int[] count = new int[10];

        for (int i = 0; i < n; i++) {
            count[(a[i] / exp) % 10]++;
        }
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }
        for (int i = n - 1; i >= 0; i--) {
            int digit = (a[i] / exp) % 10;
            output[count[digit] - 1] = a[i];
            count[digit]--;
        }
        System.arraycopy(output, 0, a, 0, n);
    }
}
