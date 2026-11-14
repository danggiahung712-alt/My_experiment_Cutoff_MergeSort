package prj;

/**
 * Cài đặt Mergesort Top-Down kết hợp cải tiến CUTOFF chuyển sang Insertion Sort
 * đối với các mảng con kích thước nhỏ.
 *
 * Tham khảo: Robert Sedgewick & Kevin Wayne, Algorithms 4th Edition, Section 2.2.
 */
public class OptimizedMergeSort {

    /**
     * Sắp xếp chèn (Insertion Sort) cho mảng con a[lo..hi] sử dụng phép hoán đổi liên tiếp (exch).
     * Cài đặt đúng theo nguyên bản giáo trình Sedgewick & Wayne.
     *
     * @param a  mảng đối tượng Comparable
     * @param lo chỉ số bắt đầu
     * @param hi chỉ số kết thúc
     */
    public static void insertionSort(Comparable[] a, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            for (int j = i; j > lo && less(a[j], a[j - 1]); j--) {
                exch(a, j, j - 1);
            }
        }
    }

    /**
     * Trộn hai mảng con đã được sắp xếp a[lo..mid] và a[mid+1..hi] bằng mảng phụ aux[].
     *
     * @param a   mảng đích
     * @param aux mảng phụ trợ được cấp phát trước
     * @param lo  chỉ số đầu mảng con trái
     * @param mid chỉ số cuối mảng con trái
     * @param hi  chỉ số cuối mảng con phải
     */
    private static void merge(Comparable[] a, Comparable[] aux, int lo, int mid, int hi) {
        // Sao chép sang mảng phụ aux[]
        for (int k = lo; k <= hi; k++) {
            aux[k] = a[k];
        }

        // Trộn ngược lại mảng chính a[]
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                a[k] = aux[j++];
            } else if (j > hi) {
                a[k] = aux[i++];
            } else if (less(aux[j], aux[i])) {
                a[k] = aux[j++];
            } else {
                a[k] = aux[i++]; // Giữ tính ổn định (stable) khi aux[i] <= aux[j]
            }
        }
    }

    /**
     * Hàm sắp xếp đệ quy với ngưỡng CUTOFF.
     *
     * @param a      mảng cần sắp xếp
     * @param aux    mảng phụ trợ
     * @param lo     chỉ số bắt đầu
     * @param hi     chỉ số kết thúc
     * @param cutoff ngưỡng chuyển sang Insertion Sort
     */
    private static void sort(Comparable[] a, Comparable[] aux, int lo, int hi, int cutoff) {
        // Cải tiến CUTOFF: nếu mảng con có kích thước <= cutoff thì dùng Insertion Sort
        if (hi - lo + 1 <= cutoff) {
            insertionSort(a, lo, hi);
            return;
        }

        // Trường hợp dừng cơ sở khi cutoff == 0 (Mergesort thuần túy)
        if (hi <= lo) {
            return;
        }

        int mid = lo + (hi - lo) / 2;
        sort(a, aux, lo, mid, cutoff);
        sort(a, aux, mid + 1, hi, cutoff);
        merge(a, aux, lo, mid, hi);
    }

    /**
     * Phương thức chính công khai để sắp xếp mảng với tham số CUTOFF tùy chỉnh.
     *
     * @param a      mảng các phần tử Comparable cần sắp xếp
     * @param cutoff ngưỡng kích thước mảng con chuyển sang Insertion Sort (0 = Mergesort thuần)
     */
    public static void sort(Comparable[] a, int cutoff) {
        if (a == null || a.length <= 1) return;
        Comparable[] aux = new Comparable[a.length];
        sort(a, aux, 0, a.length - 1, cutoff);
    }

    /**
     * Phương thức tiện ích sắp xếp mặc định (cutoff = 10 theo giáo trình hiện đại).
     */
    public static void sort(Comparable[] a) {
        sort(a, 10);
    }

    // Các hàm trợ giúp (Helper methods) theo quy ước Sedgewick

    @SuppressWarnings("unchecked")
    private static boolean less(Comparable v, Comparable w) {
        return v.compareTo(w) < 0;
    }

    private static void exch(Comparable[] a, int i, int j) {
        Comparable t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    /**
     * Kiểm tra mảng a[lo..hi] đã được sắp xếp tăng dần hay chưa.
     */
    public static boolean isSorted(Comparable[] a, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            if (less(a[i], a[i - 1])) return false;
        }
        return true;
    }

    public static boolean isSorted(Comparable[] a) {
        return isSorted(a, 0, a.length - 1);
    }
}
