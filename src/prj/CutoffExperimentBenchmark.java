package prj;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Chương trình đo đạc thực nghiệm (Benchmark) tìm giá trị CUTOFF tối ưu cho Mergesort.
 *
 * Thiết kế theo phương pháp luận trong Sedgewick & Wayne (Chủ đề 1.3 Algorithm Analysis):
 * - Đo lường trên nhiều phân bố dữ liệu và kích thước mảng N tăng dần (Doubling Hypothesis).
 * - Sử dụng kỹ thuật Warm-up toàn cục để JIT biên dịch đầy đủ các đường code.
 * - Đo xen kẽ (Interleaved randomized trials) nhằm triệt tiêu trôi dạt nhiệt và nhiễu hệ thống.
 * - Thống kê vững: Mean, Median, Sample Standard Deviation, và Khoảng tin cậy 95% (Student's t).
 */
public class CutoffExperimentBenchmark {

    public enum Distribution {
        RANDOM,
        NEARLY_SORTED,
        REVERSE_SORTED,
        DUPLICATES
    }

    private static final long BASE_SEED = 42L;

    // Phân phối Student-t 2 phía tại alpha = 0.05 (CI 95%) cho các bậc tự do df = n - 1
    private static double getCriticalT(int df) {
        switch (df) {
            case 1: return 12.706;
            case 2: return 4.303;
            case 3: return 3.182;
            case 4: return 2.776;
            case 5: return 2.571;
            case 6: return 2.447;
            case 7: return 2.365;
            case 8: return 2.306;
            case 9: return 2.262;
            case 10: return 2.228;
            case 11: return 2.201;
            case 12: return 2.179;
            case 13: return 2.160;
            case 14: return 2.145; // 15 trials -> df = 14
            case 15: return 2.131;
            case 19: return 2.093;
            case 29: return 2.045;
            default:
                if (df >= 30) return 1.960;
                return 2.0;
        }
    }

    /**
     * Sinh mảng dữ liệu thử nghiệm kiểu Integer[] theo phân bố và seed xác định.
     */
    public static Integer[] generateData(int n, Distribution dist, long seed) {
        Random rng = new Random(seed);
        Integer[] a = new Integer[n];

        switch (dist) {
            case RANDOM:
                for (int i = 0; i < n; i++) {
                    a[i] = rng.nextInt();
                }
                break;

            case NEARLY_SORTED:
                for (int i = 0; i < n; i++) {
                    a[i] = i;
                }
                // Thực hiện n / 100 hoán vị ngẫu nhiên (1% nghịch thế)
                int swaps = Math.max(1, n / 100);
                for (int k = 0; k < swaps; k++) {
                    int i = rng.nextInt(n);
                    int j = rng.nextInt(n);
                    Integer tmp = a[i];
                    a[i] = a[j];
                    a[j] = tmp;
                }
                break;

            case REVERSE_SORTED:
                for (int i = 0; i < n; i++) {
                    a[i] = n - i;
                }
                break;

            case DUPLICATES:
                for (int i = 0; i < n; i++) {
                    a[i] = rng.nextInt(100); // Chỉ có 100 giá trị khác nhau (nhiều trùng lặp)
                }
                break;
        }
        return a;
    }

    /**
     * Xuất mảng ra file nhị phân mẫu để lưu trữ bộ dữ liệu.
     */
    public static void exportSampleData(Integer[] a, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            dos.writeInt(a.length);
            for (Integer val : a) {
                dos.writeInt(val);
            }
        }
    }

    /**
     * Lớp hỗ trợ kiểm tra tính ổn định (Stability).
     */
    static class Item implements Comparable<Item> {
        final int key;
        final int originalIndex;

        Item(int key, int originalIndex) {
            this.key = key;
            this.originalIndex = originalIndex;
        }

        @Override
        public int compareTo(Item o) {
            return Integer.compare(this.key, o.key);
        }
    }

    /**
     * Kiểm tra tính đúng đắn và tính ổn định (stable sort) của OptimizedMergeSort với mọi cutoff.
     */
    private static void verifyCorrectnessAndStability() {
        System.out.println("Dang kiem tra tinh dung dan va tinh on dinh (stability)...");
        int testN = 5000;
        int[] cutoffs = {0, 2, 5, 7, 10, 15, 30, 50};
        Random rng = new Random(12345);

        for (int c : cutoffs) {
            // Kiểm tra tính đúng với Arrays.sort
            Integer[] a1 = new Integer[testN];
            Integer[] a2 = new Integer[testN];
            for (int i = 0; i < testN; i++) {
                a1[i] = rng.nextInt(1000);
                a2[i] = a1[i];
            }
            Arrays.sort(a1);
            OptimizedMergeSort.sort(a2, c);
            if (!Arrays.equals(a1, a2)) {
                throw new IllegalStateException("FAIL tinh dung dan tai cutoff = " + c);
            }

            // Kiểm tra tính ổn định (stable)
            Item[] items = new Item[testN];
            for (int i = 0; i < testN; i++) {
                items[i] = new Item(rng.nextInt(50), i);
            }
            OptimizedMergeSort.sort(items, c);
            for (int i = 1; i < testN; i++) {
                if (items[i].key < items[i - 1].key) {
                    throw new IllegalStateException("FAIL thu tu tai cutoff = " + c);
                }
                if (items[i].key == items[i - 1].key && items[i].originalIndex < items[i - 1].originalIndex) {
                    throw new IllegalStateException("FAIL tinh on dinh (stability) tai cutoff = " + c);
                }
            }
        }
        System.out.println("-> Kiem tra hoan tat: Thuat toan hoat dong chinh xac va giu nguyen tinh on dinh!\n");
    }

    public static void main(String[] args) throws Exception {
        boolean quickMode = args.length > 0 && "quick".equalsIgnoreCase(args[0]);

        int[] sizes = quickMode
                ? new int[]{25000, 50000}
                : new int[]{125000, 250000, 500000, 1000000, 2000000};

        int[] cutoffs = quickMode
                ? new int[]{0, 2, 5, 7, 10, 14, 20, 30}
                : new int[]{0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 24, 26, 28, 30, 40, 50, 60, 80, 100, 150};

        int trials = quickMode ? 3 : 15;
        int warmupRounds = quickMode ? 1 : 3;

        Distribution[] distributions = Distribution.values();

        verifyCorrectnessAndStability();

        Path outputDir = Paths.get("results");
        Files.createDirectories(outputDir);
        Path dataDir = outputDir.resolve("data");
        Files.createDirectories(dataDir);

        Path rawCsvPath = outputDir.resolve(quickMode ? "cutoff_quick_raw.csv" : "cutoff_raw.csv");
        Path summaryCsvPath = outputDir.resolve(quickMode ? "cutoff_quick_summary.csv" : "cutoff_summary.csv");
        Path metaPath = outputDir.resolve(quickMode ? "experiment_quick_meta.txt" : "experiment_meta.txt");

        // Ghi metadata thi nghiem
        try (PrintWriter metaWriter = new PrintWriter(Files.newBufferedWriter(metaPath))) {
            metaWriter.println("Java: " + System.getProperty("java.version") + " / " + System.getProperty("java.vm.name"));
            metaWriter.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
            metaWriter.println("Cores: " + Runtime.getRuntime().availableProcessors());
            metaWriter.println("MaxHeap MB: " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)));
            metaWriter.println("Seed: " + BASE_SEED + ", trials: " + trials + ", warmup rounds: " + warmupRounds);
            metaWriter.println("Sizes: " + Arrays.toString(sizes));
            metaWriter.println("Cutoffs: " + Arrays.toString(cutoffs));
        }

        System.out.println("=== THI NGHIEM DO CUTOFF MERGESORT ===");
        System.out.printf("Che do: %s | Kich thuoc N: %s | So lan lap: %d | Warm-up: %d vong%n",
                (quickMode ? "QUICK" : "FULL"), Arrays.toString(sizes), trials, warmupRounds);
        System.out.println("Bat dau qua trinh JIT Warm-up toan cuc...");

        // Warm-up toan cuc
        for (int w = 0; w < warmupRounds; w++) {
            Integer[] warmData = generateData(50000, Distribution.RANDOM, BASE_SEED + w);
            for (int c : cutoffs) {
                Integer[] copy = warmData.clone();
                OptimizedMergeSort.sort(copy, c);
            }
        }
        System.out.println("Hoan tat Warm-up.\n");

        List<String> rawLines = new ArrayList<>();
        rawLines.add("Size_N,Distribution,Cutoff,Trial,Time_ms");

        List<String> summaryLines = new ArrayList<>();
        summaryLines.add("Size_N,Distribution,Cutoff,Mean_ms,Median_ms,StdDev_ms,CI95_ms,Min_ms");

        // Tien hanh do tung cau hinh (N, Distribution)
        for (int n : sizes) {
            for (int dIdx = 0; dIdx < distributions.length; dIdx++) {
                Distribution dist = distributions[dIdx];
                long seed = BASE_SEED + 1000L * n + dIdx;
                Integer[] originalData = generateData(n, dist, seed);

                // Xuat file du lieu mau cho N nho nhat
                if (n == sizes[0]) {
                    Path binPath = dataDir.resolve(dist.name() + "_" + n + ".bin");
                    exportSampleData(originalData, binPath);
                }

                System.out.printf("Dang do: N = %,d | Phan bo: %-14s ...%n", n, dist.name());

                // Luu thoi gian do cho tung cutoff: cutoff -> danh sach ket qua (ms)
                Map<Integer, double[]> cutoffTimes = new LinkedHashMap<>();
                for (int c : cutoffs) {
                    cutoffTimes.put(c, new double[trials]);
                }

                // Do xen ke ngau nhien (Interleaved Randomized Trials)
                Integer[] cutoffOrder = Arrays.stream(cutoffs).boxed().toArray(Integer[]::new);
                Random shuffleRng = new Random(seed ^ 0xDEADBEEFL);

                for (int t = 0; t < trials; t++) {
                    // Xao tron thu tu cutoff moi vong
                    List<Integer> shuffled = new ArrayList<>(Arrays.asList(cutoffOrder));
                    Collections.shuffle(shuffled, shuffleRng);

                    for (int c : shuffled) {
                        Integer[] copy = originalData.clone();

                        System.gc();
                        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

                        long start = System.nanoTime();
                        OptimizedMergeSort.sort(copy, c);
                        long end = System.nanoTime();

                        if (!OptimizedMergeSort.isSorted(copy)) {
                            throw new IllegalStateException("Loi sap xep tai N=" + n + ", cutoff=" + c);
                        }

                        double elapsedMs = (end - start) / 1e6;
                        cutoffTimes.get(c)[t] = elapsedMs;
                        rawLines.add(String.format(Locale.US, "%d,%s,%d,%d,%.4f", n, dist.name(), c, t, elapsedMs));
                    }
                }

                // Tinh toan thong ke cho tung cutoff
                double tCrit = getCriticalT(trials - 1);
                for (int c : cutoffs) {
                    double[] times = cutoffTimes.get(c);
                    double sum = 0.0;
                    double min = Double.MAX_VALUE;
                    for (double val : times) {
                        sum += val;
                        if (val < min) min = val;
                    }
                    double mean = sum / trials;

                    double sumSqDiff = 0.0;
                    for (double val : times) {
                        sumSqDiff += (val - mean) * (val - mean);
                    }
                    double stdDev = (trials > 1) ? Math.sqrt(sumSqDiff / (trials - 1)) : 0.0;
                    double ci95 = (trials > 1) ? (tCrit * stdDev / Math.sqrt(trials)) : 0.0;

                    double[] sortedTimes = times.clone();
                    Arrays.sort(sortedTimes);
                    double median = (trials % 2 == 1)
                            ? sortedTimes[trials / 2]
                            : (sortedTimes[trials / 2 - 1] + sortedTimes[trials / 2]) / 2.0;

                    summaryLines.add(String.format(Locale.US, "%d,%s,%d,%.4f,%.4f,%.4f,%.4f,%.4f",
                            n, dist.name(), c, mean, median, stdDev, ci95, min));
                }
            }
        }

        Files.write(rawCsvPath, rawLines);
        Files.write(summaryCsvPath, summaryLines);
        System.out.println("\n-> Hoan thanh toan bo phep do!");
        System.out.println("-> Da ghi ket qua vao: " + rawCsvPath.toAbsolutePath() + " va " + summaryCsvPath.toAbsolutePath());
    }
}
