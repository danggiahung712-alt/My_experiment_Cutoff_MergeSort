package prj;

import java.util.*;

/**
 * Bo kiem thu tu dong (Unit Test Suite) cho OptimizedMergeSort.
 * Khong phu thuoc thu vien ben ngoai (chay truc tiep bang Java chuan).
 *
 * Kiem thu toan dien tinh dung dan toan hoc, cac truong hop bien,
 * va tinh on dinh (stability) cua thuat toan sap xep.
 */
public class OptimizedMergeSortTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    private static void assertTrue(boolean condition, String message) {
        testsRun++;
        if (!condition) {
            System.err.println("  [FAIL] " + message);
            throw new AssertionError(message);
        }
        testsPassed++;
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        testsRun++;
        if (!Objects.equals(expected, actual)) {
            System.err.printf("  [FAIL] %s - Ky vong: %s, Thuc te: %s%n", message, expected, actual);
            throw new AssertionError(message);
        }
        testsPassed++;
    }

    // 1. Kiem thu mang rong
    public static void testEmptyArray() {
        System.out.print("Test 1: Mang rong (Empty array) ... ");
        Integer[] a = new Integer[0];
        OptimizedMergeSort.sort(a, 10);
        assertTrue(a.length == 0, "Mang rong phai giu nguyen kich thuoc 0");
        assertTrue(OptimizedMergeSort.isSorted(a), "Mang rong luon duoc coi la da sap xep");
        System.out.println("PASS");
    }

    // 2. Kiem thu mang 1 phan tu
    public static void testSingleElement() {
        System.out.print("Test 2: Mang 1 phan tu (Single element) ... ");
        Integer[] a = new Integer[]{42};
        OptimizedMergeSort.sort(a, 10);
        assertEquals(42, a[0], "Phan tu duy nhat khong bi thay doi");
        assertTrue(OptimizedMergeSort.isSorted(a), "Mang 1 phan tu phai da sap xep");
        System.out.println("PASS");
    }

    // 3. Kiem thu mang da duoc sap xep san
    public static void testAlreadySorted() {
        System.out.print("Test 3: Mang da sap xep tang dan ... ");
        Integer[] a = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        Integer[] expected = a.clone();
        OptimizedMergeSort.sort(a, 7);
        assertTrue(Arrays.equals(expected, a), "Mang tang dan phai giu nguyen trat tu");
        System.out.println("PASS");
    }

    // 4. Kiem thu mang giam dan (Reverse sorted - truong hop xau nhat cua Insertion Sort)
    public static void testReverseSorted() {
        System.out.print("Test 4: Mang giam dan (Reverse sorted) ... ");
        Integer[] a = new Integer[]{20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        Integer[] expected = a.clone();
        Arrays.sort(expected);
        OptimizedMergeSort.sort(a, 10);
        assertTrue(Arrays.equals(expected, a), "Mang giam dan phai duoc dao nguoc thanh tang dan");
        System.out.println("PASS");
    }

    // 5. Kiem thu mang toan phan tu trung lap
    public static void testAllDuplicates() {
        System.out.print("Test 5: Mang toan phan tu trung lap ... ");
        Integer[] a = new Integer[]{5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
        Integer[] expected = a.clone();
        OptimizedMergeSort.sort(a, 8);
        assertTrue(Arrays.equals(expected, a), "Mang trung lap khong bi sai lech gia tri");
        assertTrue(OptimizedMergeSort.isSorted(a), "Mang trung lap phai hop le");
        System.out.println("PASS");
    }

    // 6. Kiem thu so am, so 0 va cac gia tri bien Integer
    public static void testNegativeAndExtremeValues() {
        System.out.print("Test 6: So am, so 0 va gia tri cuc bien ... ");
        Integer[] a = new Integer[]{Integer.MAX_VALUE, -500, 0, -1, 100, Integer.MIN_VALUE, 42, -500, 0};
        Integer[] expected = a.clone();
        Arrays.sort(expected);
        OptimizedMergeSort.sort(a, 10);
        assertTrue(Arrays.equals(expected, a), "Mang gom so am va cuc tri phai sap xep dung");
        System.out.println("PASS");
    }

    // 7. Kiem thu cac nguong bien cua CUTOFF (Cutoff Boundaries)
    public static void testCutoffBoundaries() {
        System.out.print("Test 7: Cac nguong bien CUTOFF (0, 1, 2, 7, 10, 15, 30) ... ");
        int[] cutoffs = {0, 1, 2, 7, 10, 15, 30};
        int[] lengths = {2, 3, 6, 7, 8, 9, 10, 11, 14, 15, 16, 25, 50};
        Random rng = new Random(1337);

        for (int c : cutoffs) {
            for (int len : lengths) {
                Integer[] a = new Integer[len];
                for (int i = 0; i < len; i++) a[i] = rng.nextInt(200) - 100;
                Integer[] expected = a.clone();
                Arrays.sort(expected);
                OptimizedMergeSort.sort(a, c);
                assertTrue(Arrays.equals(expected, a),
                        "Loi tai cutoff=" + c + ", length=" + len);
            }
        }
        System.out.println("PASS");
    }

    // 8. Kiem thu mang ngau nhien kich thuoc lon doi chieu Arrays.sort()
    public static void testLargeRandomArrays() {
        System.out.print("Test 8: 50 mang ngau nhien quy mo lon (N=10.000) ... ");
        Random rng = new Random(2026);
        for (int iter = 0; iter < 50; iter++) {
            int n = 10000;
            Integer[] a = new Integer[n];
            for (int i = 0; i < n; i++) a[i] = rng.nextInt();
            Integer[] expected = a.clone();
            Arrays.sort(expected);
            int cutoff = rng.nextInt(35); // cutoff ngau nhien tu 0 den 34
            OptimizedMergeSort.sort(a, cutoff);
            assertTrue(Arrays.equals(expected, a), "Loi sap xep tai lan thu " + iter + " voi cutoff=" + cutoff);
        }
        System.out.println("PASS");
    }

    // 9. Kiem thu Tinh on dinh (Stability test)
    static class Record implements Comparable<Record> {
        final int key;
        final int seq;

        Record(int key, int seq) {
            this.key = key;
            this.seq = seq;
        }

        @Override
        public int compareTo(Record o) {
            return Integer.compare(this.key, o.key);
        }

        @Override
        public String toString() {
            return "(" + key + ", seq=" + seq + ")";
        }
    }

    public static void testStability() {
        System.out.print("Test 9: Tinh on dinh (Stability / Stable Sort) ... ");
        int[] cutoffs = {0, 2, 5, 7, 10, 14, 20, 40};
        Random rng = new Random(9999);
        int n = 5000;

        for (int c : cutoffs) {
            Record[] arr = new Record[n];
            for (int i = 0; i < n; i++) {
                // Key trong mien hep [0, 20] de tao rat nhieu phan tu trung khoa
                arr[i] = new Record(rng.nextInt(20), i);
            }

            OptimizedMergeSort.sort(arr, c);

            for (int i = 1; i < n; i++) {
                assertTrue(arr[i].key >= arr[i - 1].key, "Thu tu khoa phai tang dan");
                if (arr[i].key == arr[i - 1].key) {
                    assertTrue(arr[i].seq > arr[i - 1].seq,
                            "Mat tinh on dinh tai cutoff=" + c + ": " + arr[i - 1] + " dung truoc " + arr[i]);
                }
            }
        }
        System.out.println("PASS");
    }

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  KIEM THU TU DONG CHAT CHE (UNIT TESTS) - OPTIMIZED MERGESORT");
        System.out.println("==========================================================");

        long start = System.currentTimeMillis();
        try {
            testEmptyArray();
            testSingleElement();
            testAlreadySorted();
            testReverseSorted();
            testAllDuplicates();
            testNegativeAndExtremeValues();
            testCutoffBoundaries();
            testLargeRandomArrays();
            testStability();
        } catch (Throwable t) {
            System.err.println("\n[!] PHAT HIEN LOI TRONG QUA TRINH KIEM THU: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("==========================================================");
        System.out.printf("  KET QUA: TOAN BO %d PHEP KIEM TRA (ASSERTIONS) DA VUOT QUA!%n", testsPassed);
        System.out.printf("  Thoi gian thuc thi kiem thu: %d ms%n", elapsed);
        System.out.println("  Ket luan: OptimizedMergeSort dat tieu chuan toan hoc & tinh on dinh!");
        System.out.println("==========================================================\n");
    }
}
