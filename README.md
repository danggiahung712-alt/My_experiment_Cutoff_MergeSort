# Thí nghiệm xác định ngưỡng CUTOFF tối ưu cho Mergesort (Thư mục thực nghiệm hoàn chỉnh)

Dự án nghiên cứu thực nghiệm xác định ngưỡng CUTOFF tối ưu khi kết hợp **Mergesort** và **Insertion Sort** trên nền tảng máy ảo Java (JVM) theo phương pháp luận trong **Chủ đề 1.3 (Algorithm Analysis)** và **2.2 (Mergesort)** thuộc giáo trình *Algorithms* (Robert Sedgewick & Kevin Wayne).

---

## 1. Cấu trúc thư mục dự án `my_experiment`

```
my_experiment/
├── run.bat                              # Kịch bản 1-chạm chạy toàn bộ thực nghiệm trên Windows (CMD)
├── analyze.py                           # Script Python tính bảng thống kê & vẽ 3 đồ thị phân giải cao
├── pom.xml                              # Cấu hình dự án Maven
├── requirements.txt                     # Thư viện Python (pandas, matplotlib, numpy)
├── src/prj/
│   ├── OptimizedMergeSort.java          # Cài đặt Mergesort Top-Down + CUTOFF Insertion Sort chuẩn bài giảng
│   ├── OptimizedMergeSortTest.java      # Bộ Unit Test tự động (79.982 assertions, kiểm thử tính đúng & stability)
│   └── CutoffExperimentBenchmark.java   # Script đo benchmark khoa học (JIT warm-up, randomized interleaving)
├── report/
│   ├── BAO_CAO_THI_NGHIEM_MERGESORT_CUTOFF.md   # Báo cáo nghiên cứu thực nghiệm chuyên sâu (Markdown)
│   └── LOG_CHAT_AI.md                   # Nhật ký thảo luận phương pháp luận và phân tích kết quả với AI
└── results/
    ├── cutoff_raw.csv                   # Dữ liệu thô 9.300 lần đo độc lập
    ├── cutoff_summary.csv               # Bảng thống kê tóm tắt (Mean, Median, StdDev, CI95, Min)
    ├── best_cutoff_table.csv            # Bảng tổng hợp CUTOFF tối ưu và vùng ngang bằng thống kê
    ├── experiment_meta.txt              # Siêu dữ liệu môi trường đo (Java HotSpot, Windows 11, Cores)
    ├── hinh1_toan_khoang_cutoff.png     # Đồ thị toàn dải CUTOFF 0 - 150
    ├── hinh2_phong_to_0_30.png          # Đồ thị phóng to dải 0 - 30 (bình nguyên tối ưu & bước nhảy JIT)
    ├── hinh3_thoi_gian_theo_N.png       # Đồ thị Log-Log kiểm chứng Giả thuyết Nhân đôi (Doubling Hypothesis)
    └── data/
        ├── README.md                    # Tài liệu đặc tả cấu trúc tệp dữ liệu nhị phân & text
        ├── *.bin                        # Dữ liệu mẫu kích thước N = 125.000 dạng nhị phân chuẩn
        └── *_sample1000.txt             # Dữ liệu trích xuất 1.000 phần tử đầu tiên xem trực quan
```

---

## 2. Cách chạy thử nghiệm

Chỉ cần click đúp vào tệp **run.bat** (hoặc mở Command Prompt / Terminal tại thư mục này và gõ `run.bat`):

```cmd
run.bat
```

Hệ thống sẽ tự động hiển thị menu tương tác 1-chạm, tự động biên dịch toàn bộ mã nguồn Java và điều hướng các chức năng:

* **Phím [1] - Kiểm tra tính đúng (Unit Tests):** Tự động biên dịch và thực thi 79.982 assertions kiểm tra tính đúng đắn toán học, các trường hợp biên và tính ổn định (Stability).
* **Phím [2] - Chạy thử nghiệm nhanh (Quick Benchmark ~15s - Khuyến nghị):** Đo đạc nhanh trên mẫu đại diện để kiểm chứng quy trình đo JIT warm-up và randomized interleaving một cách an toàn.
* **Phím [3] - Chạy lại toàn bộ thí nghiệm (Full Benchmark):** Tái lập toàn bộ 9.300 phép đo độc lập (5 quy mô N × 4 phân bố × 31 ngưỡng Cutoff × 15 lần lặp, mất khoảng 5 - 8 phút).
* **Phím [4] - Phân tích dữ liệu & Vẽ lại đồ thị:** Tự động tính toán các chỉ số thống kê (Mean, Median, CI 95%) và xuất lại 3 biểu đồ trực quan (`python analyze.py`).
* **Phím [5] - Chạy trọn gói từ A đến Z:** Tự động thực thi toàn bộ quy trình: Unit Tests → Quick Benchmark → Phân tích & Xuất đồ thị.

---

## 3. Các tài liệu sản phẩm chính
* **Báo cáo nghiên cứu chuyên sâu:** [`report/BAO_CAO_THI_NGHIEM_MERGESORT_CUTOFF.md`](report/BAO_CAO_THI_NGHIEM_MERGESORT_CUTOFF.md)
* **Nhật ký trao đổi với AI:** [`report/LOG_CHAT_AI.md`](report/LOG_CHAT_AI.md)
* **Đặc tả bộ dữ liệu thực nghiệm:** [`results/data/README.md`](results/data/README.md)
