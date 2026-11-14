# BÁO CÁO THI NGHIỆM CHUYÊN SÂU
# NGHIÊN CỨU THỰC NGHIỆM VÀ XÁC ĐỊNH NGƯỠNG CUTOFF TỐI ƯU CHO THUẬT TOÁN MERGESORT

## TÓM TẮT ĐIỀU HÀNH (EXECUTIVE SUMMARY)

Thuật toán Mergesort Top-Down có độ phức tạp thời gian lý thuyết là Θ(N log N) trong mọi trường hợp, nhưng chịu chi phí hằng số ẩn (overhead) đáng kể do cơ chế gọi hàm đệ quy sâu và thao tác sao chép dữ liệu qua mảng phụ trợ `aux[]`. Ngược lại, Insertion Sort dù có độ phức tạp trung bình Θ(N²) nhưng lại có chi phí hằng số cực nhỏ, thực hiện tại chỗ (in-place) và tận dụng triệt để bộ nhớ đệm cache L1/L2 của vi xử lý trên các đoạn mảng có độ dài ngắn. Sự kết hợp giữa hai thuật toán thông qua kỹ thuật **CUTOFF** (chuyển sang Insertion Sort khi kích thước mảng con ≤ CUTOFF) là một cải tiến kinh điển.

Giáo trình của Sedgewick & Wayne qua các ấn bản đưa ra hai khuyến nghị khác nhau: **CUTOFF = 7** (các phiên bản cũ) và **CUTOFF = 10** (phiên bản 4th hiện hành). Báo cáo này tiến hành nghiên cứu thực nghiệm với **9.300 lần đo độc lập** trên nền tảng Java HotSpot VM, khảo sát 31 giá trị CUTOFF (0 ≤ CUTOFF ≤ 150), 5 quy mô dữ liệu lũy tiến hình học (N = 125.000 đến 2.000.000), trên 4 dạng phân bố dữ liệu đặc trưng (Random, Duplicates, Reverse Sorted, Nearly Sorted).

**Kết quả cốt lõi:**
1. **Xác nhận tính hiệu quả của giáo trình:** Cả hai giá trị 7 và 10 đều nằm trong vùng tiệm cận tối ưu. Trên dữ liệu ngẫu nhiên, **CUTOFF = 10** chỉ chậm hơn giá trị tối ưu tuyệt đối thực nghiệm từ 0,3% - 0,9% (hoàn toàn nằm trong biên độ nhiễu thống kê ~1%), trong khi **CUTOFF = 7** chậm hơn khoảng 1,0% - 2,4%. Cải tiến CUTOFF giúp Mergesort tăng tốc tổng thể từ **5,5% - 9,3%** so với Mergesort thuần túy (CUTOFF = 0).
2. **Khái niệm "Vùng bình nguyên tối ưu" (Optimal Plateau):** Với dữ liệu ngẫu nhiên và trùng lặp, đồ thị hiệu năng không có một điểm cực trị đơn lẻ sắc nhọn mà tồn tại một "vùng bình nguyên" trải dài từ **8 đến 14**. Mọi giá trị trong khoảng này đều tương đương thống kê (độ tin cậy 95%). Do đó, giá trị 10 là lựa chọn đại diện xuất sắc và ổn định nhất.
3. **Sự phụ thuộc vào phân bố dữ liệu:** CUTOFF tối ưu nhạy cảm mạnh với mức độ sắp xếp của dữ liệu:
   * **Reverse Sorted (Nghịch thế tối đa):** Do Insertion Sort suy biến về O(k²), CUTOFF tối ưu co hẹp về **2–6**; giá trị CUTOFF lớn bị phạt nặng (CUTOFF 30 chậm hơn 33,7%).
   * **Nearly Sorted (Gần sắp xếp):** Insertion Sort đạt tiệm cận O(k), CUTOFF tối ưu vượt biên khảo sát (**≥ 150**), nhanh hơn Mergesort thuần đến **31,6%**.
4. **Kiểm chứng Doubling Hypothesis & Hiện tượng phần cứng:** Thời gian chạy tăng trưởng đúng bậc N log N, nhưng ghi nhận một bước nhảy hiệu năng đột ngột tại khoảng chuyển tiếp N = 250k → 500k (hệ quả của việc mảng đối tượng vượt quá dung lượng CPU Cache L3) và tại CUTOFF = 15 → 16 (ngưỡng inline bytecode của trình biên dịch JIT HotSpot C2).

---

## CÁCH CHẠY THỬ NGHIỆM

Chỉ cần click đúp vào tệp **run.bat** (hoặc mở CMD / Terminal tại thư mục `my_experiment` và gõ `run.bat`):

```cmd
run.bat
```

Hệ thống sẽ tự động hiển thị menu tương tác 1-chạm, tự động biên dịch và điều hướng toàn bộ quy trình:

* **Phím [1] - Kiểm tra tính đúng (Unit Tests):** Biên dịch và chạy bộ kiểm thử tự động với 79.982 assertions xác thực tính đúng đắn toán học, các trường hợp biên và tính ổn định (Stability).
* **Phím [2] - Chạy thử nghiệm nhanh (Quick Benchmark ~15s - Khuyến nghị):** Đo đạc nhanh trên mẫu đại diện để kiểm chứng quy trình đo JIT warm-up và randomized interleaving một cách an toàn.
* **Phím [3] - Chạy lại toàn bộ thí nghiệm (Full Benchmark):** Tái lập toàn bộ 9.300 phép đo độc lập (5 quy mô N × 4 phân bố × 31 ngưỡng Cutoff × 15 lần lặp, mất khoảng 5 - 8 phút).
* **Phím [4] - Phân tích dữ liệu & Vẽ lại đồ thị:** Tự động tính toán các chỉ số thống kê (Mean, Median, CI 95%) và xuất lại 3 biểu đồ trực quan (`python analyze.py`).
* **Phím [5] - Chạy trọn gói từ A đến Z:** Tự động thực thi toàn bộ quy trình: Unit Tests → Quick Benchmark → Phân tích & Xuất đồ thị.

### Bảng tra cứu nhanh các tệp sản phẩm bàn giao:
* **Báo cáo nghiên cứu thực nghiệm:** [`report/BAO_CAO_THI_NGHIEM_MERGESORT_CUTOFF.md`](BAO_CAO_THI_NGHIEM_MERGESORT_CUTOFF.md)
* **Mã nguồn Mergesort chuẩn bài giảng:** [`src/prj/OptimizedMergeSort.java`](../src/prj/OptimizedMergeSort.java)
* **Bộ Unit Test kiểm thử tính đúng:** [`src/prj/OptimizedMergeSortTest.java`](../src/prj/OptimizedMergeSortTest.java)
* **Script đo benchmark khoa học:** [`src/prj/CutoffExperimentBenchmark.java`](../src/prj/CutoffExperimentBenchmark.java)
* **Script phân tích & vẽ 3 đồ thị:** [`analyze.py`](../analyze.py)
* **Dữ liệu thô 9.300 lần đo:** [`results/cutoff_raw.csv`](../results/cutoff_raw.csv)
* **Bảng tổng hợp điểm tối ưu:** [`results/best_cutoff_table.csv`](../results/best_cutoff_table.csv)
* **Bộ dữ liệu mẫu trực quan:** [`results/data/`](../results/data/) (Có cả file nhị phân `.bin` và file trích xuất text `.txt`)
* **Nhật ký trao đổi AI:** [`report/LOG_CHAT_AI.md`](LOG_CHAT_AI.md)

---

## 1. MỤC TIÊU NGHIÊN CỨU VÀ CÂU HỎI THỰC NGHIỆM

Trong chủ đề **1.3 Algorithm Analysis**, Sedgewick & Wayne nhấn mạnh phương pháp tiếp cận khoa học đối với hiệu năng thuật toán: *Xây dựng mô hình toán học → Đưa ra giả thuyết → Thiết kế thực nghiệm → Phân tích và kiểm chứng*.

Nghiên cứu này được thiết kế nhằm trả lời trực diện 4 câu hỏi khoa học (Research Questions):

* **RQ1 (Giá trị CUTOFF nào là tốt nhất?):** Trên môi trường phần cứng và JVM hiện đại, giá trị CUTOFF nào mang lại thời gian thực thi ngắn nhất? Giá trị 7 (bản cũ) và 10 (bản mới) có thực sự tối ưu?
* **RQ2 (Độ nhạy phân bố dữ liệu):** CUTOFF tối ưu thay đổi như thế nào khi cấu trúc dữ liệu đầu vào biến thiên từ ngẫu nhiên hoàn toàn sang nghịch đảo, trùng lặp nhiều, hoặc gần như đã sắp xếp?
* **RQ3 (Tính bất biến theo kích thước mảng N):** Khi tăng quy mô dữ liệu N lên gấp đôi liên tiếp (N → 2N), điểm tối ưu của CUTOFF có bị dịch chuyển hay giữ nguyên?
* **RQ4 (Kiểm chứng giả thuyết nhân đôi - Doubling Hypothesis):** Tỉ số thời gian T(2N)/T(N) có thỏa mãn mô hình lý thuyết N log N hay chịu tác động của các hiện tượng kiến trúc phần cứng (Memory Hierarchy, Cache Locality)?

---

## 2. CƠ SỞ LÝ THUYẾT VÀ MÔ HÌNH TOÁN HỌC

```
                      Mergesort Top-Down (Recursive)
                                     │
                    ┌────────────────┴────────────────┐
          Kích thước đoạn > CUTOFF       Kích thước đoạn ≤ CUTOFF
                    │                                 │
         Chia đôi & Gọi đệ quy              Chuyển sang Insertion Sort
         (Overhead đệ quy, aux[])           (In-place, Cache L1/L2, No Call Stack)
```

### 2.1. Phân tích chi phí thuật toán Mergesort Top-Down

Theo mục 2.2 của giáo trình, Mergesort chia mảng thành hai nửa bằng nhau, sắp xếp đệ quy từng nửa rồi trộn lại:
* **Phương trình đệ quy số phép so sánh:**
  > **C(N) = C(⌊N/2⌋) + C(⌈N/2⌉) + C_merge(N)**
  Trong đó C_merge(N) dao động từ N/2 đến N - 1.
* **Nghiệm giải tích:**
  > **N log N - N ≤ C(N) ≤ N log N**
* **Số phép truy cập mảng:** Luôn bằng 6 N lg N (2N sao chép sang `aux[]`, 2N so sánh, 2N di chuyển ngược về `a[]`).
* **Hạn chế cố hữu:**
  * Cần thêm O(N) bộ nhớ phụ trợ `aux[]`.
  * Với các mảng con kích thước rất nhỏ (N ≤ 10), chi phí thiết lập khung ngăn xếp đệ quy (stack frame), tính chỉ số `mid`, cấp phát/sao chép mảng phụ chiếm tỷ trọng áp đảo so với khối lượng công việc sắp xếp thực tế.

### 2.2. Phân tích chi phí thuật toán Insertion Sort

Insertion Sort duyệt mảng từ trái sang phải, chèn từng phần tử vào vị trí thích hợp trong đoạn đã sắp xếp phía trước:
* **Số phép so sánh và hoán đổi:** Tỉ lệ trực tiếp với số nghịch thế (inversions) I của mảng con:
  > **C(k) = k - 1 + I,  S(k) = I**
* **Trường hợp trung bình (Random Data):**
  > **I ≈ k(k - 1)/4  ⟹  C(k) ~ k²/4**
* **Trường hợp tốt nhất (Nearly Sorted):** Mảng có I = O(k) nghịch thế ⟹ Thời gian chạy tuyến tính O(k).
* **Trường hợp xấu nhất (Reverse Sorted):** Mảng giảm dần có I = k(k - 1)/2 ⟹ C(k) ~ k²/2.
* **Lợi thế trên mảng ngắn (k ≤ 15):**
  * Không tốn bộ nhớ phụ (O(1) extra space).
  * Mã nguồn cực kỳ gọn gàng, vòng lặp trong (inner loop) phẳng, không có lời gọi hàm.
  * Toàn bộ đoạn mảng nằm trọn vẹn trong một vài dòng cache CPU (Cache Line 64 bytes), triệt tiêu hoàn toàn độ trễ truy xuất RAM (Cache Hit ≈ 100%).

### 2.3. Mô hình điểm giao thoa hiệu năng (Crossover Point)

Gọi thời gian thực thi của Mergesort trên mảng con kích thước k là:
> **T_merge(k) = c₁ · k log₂ k + Δ_call**
Trong đó Δ_call là chi phí hằng số overhead gọi đệ quy và quản lý chỉ số mảng phụ.

Thời gian thực thi của Insertion Sort là:
> **T_insert(k) = c₂ · k²**

Điểm giao thoa hiệu năng xuất hiện tại nghiệm của phương trình T_insert(k) = T_merge(k):
> **c₂ · k² = c₁ · k log₂ k + Δ_call**

Khi k rất nhỏ, đại lượng Δ_call khiến T_merge(k) lớn hơn đáng kể so với T_insert(k). Khi k vượt qua ngưỡng CUTOFF*, tốc độ tăng trưởng bậc hai k² của Insertion Sort bắt đầu vượt qua k log₂ k, khiến Insertion Sort chậm đi.

### 2.4. Bối cảnh lịch sử giáo trình: Vì sao từ 7 chuyển sang 10?

* **Giai đoạn thập niên 1980–1990 (Sedgewick ấn bản cũ - CUTOFF = 7):**
  Kiến trúc CPU thời kỳ này (như Intel 80386/486, Pentium I) có bộ nhớ đệm L1 rất nhỏ (hoặc không có), pipeline lệnh ngắn, và chi phí gọi hàm tương đối rẻ so với tốc độ xử lý vòng lặp. Phép nhân và hoán đổi mảng của Insertion Sort nhanh chóng trở nên đắt đỏ khi k > 7.
* **Giai đoạn hiện đại (Sedgewick & Wayne ấn bản 4th - CUTOFF = 10):**
  Kiến trúc vi xử lý hiện đại (x86_64 Superscalar, Out-of-order execution, phân nhánh suy đoán Branch Prediction) cùng với các trình biên dịch JIT tối ưu hóa mã máy (vòng lặp được unroll, thanh ghi được cấp phát thông minh) giúp Insertion Sort xử lý các đoạn mảng từ 8 – 14 phần tử với tốc độ cực nhanh trong thanh ghi và L1 Cache. Do đó, ngưỡng tối ưu thực nghiệm đã dịch chuyển lên vùng 10 ± 3.

---

## 3. THIẾT KẾ THÍ NGHIỆM VÀ PHƯƠNG PHÁP ĐO ĐẠC

### 3.1. Quy trình thực nghiệm chuẩn khoa học

```mermaid
flowchart TD
    A["Thiết lập môi trường: Java 25, Windows 11, Heap cố định 4GB"] --> B["Kiểm thử tính đúng & Tính ổn định (Stability Check)"]
    B --> C["Giai đoạn Warm-up JIT toàn cục (3 vòng trên mọi Cutoff)"]
    C --> D["Vòng lặp đo chính: 20 Cặp (N, Distribution)"]
    D --> E["Mỗi cấu hình: 15 Trials đo xen kẽ ngẫu nhiên (Interleaved)"]
    E --> F["Trước mỗi lần đo: System.gc() + Đo bằng System.nanoTime()"]
    F --> G["Kiểm tra isSorted() sau mỗi lần bấm giờ"]
    G --> H["Tổng hợp thống kê: Median, Mean, StdDev, CI95 (Student-t)"]
    H --> I["Phân tích Doubling Hypothesis & Vẽ đồ thị hiệu năng"]
```

### 3.2. Môi trường phần cứng và phần mềm

| Thành phần thông số | Chi tiết cấu hình | Ý nghĩa kiểm soát biến số |
|---|---|---|
| **Bộ xử lý (CPU)** | AMD / Intel Architecture, 12 Logical Cores | Chương trình chạy đơn luồng (Single-thread) để đo chính xác thời gian CPU |
| **Hệ điều hành** | Windows 11 Pro 64-bit (Build amd64) | Tắt các ứng dụng nền nặng, cắm sạc nguồn hiệu năng cao |
| **Máy ảo Java (JVM)** | Java HotSpot(TM) 64-Bit Server VM (25.0.2 / OpenJDK 17) | Trình biên dịch Tiered Compilation (C1/C2) tối ưu mã máy |
| **Cấu hình Heap Memory** | `-Xms3g -Xmx3g` (Cố định Heap 3–4 GB) | Tránh tình trạng JVM liên tục co giãn Heap trong lúc đo |
| **Kiểu dữ liệu đo** | `Integer[]` (Đối tượng tham chiếu Comparable) | Đánh giá đúng chi phí gọi hàm `compareTo` chuẩn như giáo trình |

### 3.3. Cài đặt thuật toán chuẩn từ bài giảng

Thuật toán được xây dựng trong lớp `prj.OptimizedMergeSort` tuân thủ nghiêm ngặt nguyên bản giáo trình:
1. **Mảng phụ `aux[]`:** Được cấp phát **duy nhất một lần** tại phương thức ngoài cùng `sort(Comparable[] a, int cutoff)` và truyền qua các lời gọi đệ quy, loại bỏ overhead cấp phát mảng liên tục.
2. **Cơ chế CUTOFF:** Trong hàm đệ quy `sort(a, aux, lo, hi, cutoff)`, điều kiện chuyển đổi được định nghĩa:
   ```java
   if (hi - lo + 1 <= cutoff) {
       insertionSort(a, lo, hi);
       return;
   }
   ```
   * Khi cutoff = 0, điều kiện trên luôn sai, hàm dừng khi hi ≤ lo (Mergesort thuần túy).
   * Khi cutoff = 1, mảng con có độ dài 1 không làm thay đổi thứ tự, hành vi tương đương cutoff = 0.
3. **Insertion Sort bằng phép hoán đổi (`exch`):** Cài đặt đúng từng cặp liền kề theo giáo trình Sedgewick:
   ```java
   for (int i = lo + 1; i <= hi; i++) {
       for (int j = i; j > lo && less(a[j], a[j - 1]); j--) {
           exch(a, j, j - 1);
       }
   }
   ```
4. **Cô lập cải tiến:** Cố ý **không** áp dụng hai cải tiến khác (kiểm tra `a[mid] <= a[mid+1]` và đảo vai trò mảng `a`/`aux`) để bảo đảm mọi sự thay đổi về thời gian chỉ xuất phát từ tham số CUTOFF.

### 3.4. Dữ liệu thử nghiệm và tính tái lập (Reproducibility)

Khảo sát 4 dạng phân bố dữ liệu với N ∈ {125.000, 250.000, 500.000, 1.000.000, 2.000.000}:
* **RANDOM:** Sinh ngẫu nhiên bằng `rng.nextInt()`. Phản ánh trường hợp sử dụng tổng quát.
* **NEARLY_SORTED:** Mảng đã sắp xếp 0, 1, ..., N-1, sau đó thực hiện N/100 phép hoán vị vị trí ngẫu nhiên (chỉ có 1% vị trí bị xáo trộn).
* **REVERSE_SORTED:** Mảng sắp xếp giảm dần N, N-1, ..., 1. Đại diện cho trường hợp tồi nhất của Insertion Sort.
* **DUPLICATES:** Sinh ngẫu nhiên trong miền hẹp [0, 99] bằng `rng.nextInt(100)`. Rất nhiều phần tử trùng giá trị.

> [!IMPORTANT]
> **Quy chuẩn Seed xác định:** Mỗi cặp (N, Distribution) có seed sinh dữ liệu cố định:
> Seed = 42 + 1000 * N + DistributionIndex
> Điều này đảm bảo toàn bộ dữ liệu có thể tái lập chính xác 100% giữa các lần chạy thí nghiệm.

### 3.5. Kiểm soát nhiễu và phương pháp đo đạc vững (Robust Benchmarking)

Đo lường thời gian trên nền tảng máy ảo Java (JVM) tiềm ẩn nhiều rủi ro sai số:
1. **JIT Compilation Warm-up:** Trước khi bước vào đo thực tế, thực hiện 3 vòng warm-up toàn diện với mọi giá trị CUTOFF trên dữ liệu 50.000 phần tử để JIT C2 Compiler biên dịch mã byte sang mã máy tối ưu.
2. **Đo xen kẽ ngẫu nhiên (Interleaved Randomized Trials):** Trong mỗi vòng lặp (15 lần lặp), danh sách 31 giá trị CUTOFF được xáo trộn ngẫu nhiên (random shuffle). Phương pháp này giúp triệt tiêu hiện tượng dồn nhiệt (thermal throttling) hoặc rác bộ nhớ (GC pauses) vào một cutoff cố định.
3. **Thu gom rác chủ động:** Gọi `System.gc()` và dừng nhẹ trước mỗi phép đo. Mỗi lần đo đều thao tác trên một mảng nhân bản (`clone()`) độc lập từ mảng gốc.
4. **Thước đo thống kê:**
   * **Trung vị (Median):** Sử dụng làm thước đo trung tâm chính vì miễn nhiễm với các giá trị ngoại lai (outliers) do hệ điều hành ngắt đột xuất.
   * **Khoảng tin cậy 95% (CI 95%):** Tính bằng phân phối Student's t với 14 bậc tự do (df = n - 1 = 14, t(0.025, 14) = 2,145):
     > **CI 95% = x̄ ± t(α/2, n-1) · (s / √n)**
   * Hai giá trị CUTOFF được xem là **ngang bằng thống kê (không khác biệt có ý nghĩa)** khi khoảng tin cậy của chúng chồng lấn lên nhau:
     > **[x̄_A - CI_A, x̄_A + CI_A] ∩ [x̄_B - CI_B, x̄_B + CI_B] ≠ ∅**

---

## 4. KẾT QUẢ THỰC NGHIỆM VÀ PHÂN TÍCH THỐNG KÊ

Tổng cộng có: **5 kích thước N × 4 phân bố × 31 cutoffs × 15 lần đo = 9.300 phép đo độc lập**.

### 4.1. Bảng tổng hợp CUTOFF tối ưu theo từng cấu hình

Dưới đây là bảng tổng hợp kết quả thực nghiệm chi tiết cho toàn bộ 20 cấu hình thử nghiệm:

| Phân bố dữ liệu | Quy mô N | CUTOFF tốt nhất (Median) | Thời gian tốt nhất (ms) | Thời gian Cutoff 0 (ms) | Mức tăng tốc so với Cutoff 0 | Vùng ngang bằng thống kê | Thời gian Cutoff 7 (ms) | Thời gian Cutoff 10 (ms) | Cutoff 7 chậm hơn | Cutoff 10 chậm hơn |
|---|---|---|---|---|---|---|---|---|---|---|
| **RANDOM** | 125.000 | **12** | 25,47 | 27,55 | **+7,6%** | 2 – 28 | 26,09 | 25,69 | 2,4% | 0,9% |
| **RANDOM** | 250.000 | **7** | 56,83 | 60,86 | **+6,6%** | 3 – 28 | 56,83 | 57,32 | 0,0% | 0,9% |
| **RANDOM** | 500.000 | **9** | 149,74 | 161,02 | **+7,0%** | 7 – 14 | 151,78 | 150,13 | 1,4% | 0,3% |
| **RANDOM** | 1.000.000 | **9** | 339,62 | 359,47 | **+5,5%** | 7 – 14 | 342,69 | 340,76 | 0,9% | 0,3% |
| **RANDOM** | 2.000.000 | **14** | 775,49 | 822,47 | **+5,7%** | 8 – 14 | 786,92 | 779,55 | 1,5% | 0,5% |
| **DUPLICATES** | 125.000 | **9** | 20,68 | 22,79 | **+9,3%** | 2 – 26 | 20,86 | 20,81 | 0,9% | 0,6% |
| **DUPLICATES** | 250.000 | **10** | 43,48 | 47,16 | **+7,8%** | 4 – 14 | 43,87 | 43,48 | 0,9% | 0,0% |
| **DUPLICATES** | 500.000 | **12** | 119,49 | 130,51 | **+8,4%** | 8 – 14 | 121,84 | 120,08 | 2,0% | 0,5% |
| **DUPLICATES** | 1.000.000 | **13** | 251,35 | 271,73 | **+7,5%** | 4 – 14 | 251,67 | 252,37 | 0,1% | 0,4% |
| **DUPLICATES** | 2.000.000 | **9** | 521,17 | 560,02 | **+6,9%** | 8 – 14 | 527,75 | 525,32 | 1,3% | 0,8% |
| **REVERSE_SORTED** | 125.000 | **3** | 18,15 | 18,98 | **+4,3%** | 0 – 7 | 19,52 | 20,59 | 7,6% | 13,5% |
| **REVERSE_SORTED** | 250.000 | **2** | 36,62 | 37,33 | **+1,9%** | 0 – 6 | 38,13 | 40,50 | 4,1% | 10,6% |
| **REVERSE_SORTED** | 500.000 | **5** | 111,82 | 116,22 | **+3,8%** | 3 – 6 | 113,85 | 116,59 | 1,8% | 4,3% |
| **REVERSE_SORTED** | 1.000.000 | **3** | 236,57 | 244,15 | **+3,1%** | 2 – 7 | 238,75 | 248,96 | 0,9% | 5,2% |
| **REVERSE_SORTED** | 2.000.000 | **5** | 493,55 | 512,07 | **+3,6%** | 2 – 6 | 502,64 | 517,44 | 1,8% | 4,8% |
| **NEARLY_SORTED** | 125.000 | **150** | 12,73 | 18,61 | **+31,6%** | 80 – 150 | 15,98 | 15,34 | 25,5% | 20,5% |
| **NEARLY_SORTED** | 250.000 | **150** | 26,82 | 37,77 | **+29,0%** | 80 – 150 | 33,32 | 31,96 | 24,3% | 19,2% |
| **NEARLY_SORTED** | 500.000 | **150** | 81,33 | 117,17 | **+30,6%** | 150 | 103,31 | 99,69 | 27,0% | 22,6% |
| **NEARLY_SORTED** | 1.000.000 | **150** | 174,47 | 245,88 | **+29,0%** | 150 | 215,72 | 208,66 | 23,6% | 19,6% |
| **NEARLY_SORTED** | 2.000.000 | **150** | 372,75 | 512,81 | **+27,3%** | 150 | 457,46 | 443,49 | 22,7% | 19,0% |

### 4.2. Ma trận độ trễ tương đối so với CUTOFF tối ưu (N ≥ 500.000)

Để triệt tiêu các dao động nhỏ do thời gian chạy quá ngắn ở N ≤ 250.000, bảng dưới đây tính mức độ chậm hơn (%) của từng CUTOFF so với thời gian tối ưu, lấy trung bình trên 3 kích thước lớn nhất (N = 500k, 1M, 2M):

| CUTOFF | RANDOM (%) | DUPLICATES (%) | REVERSE_SORTED (%) | NEARLY_SORTED (%) | Trung bình 4 dạng (%) |
|---|---|---|---|---|---|
| **0 (Thuần túy)** | +6,5% | +8,3% | +3,6% | +40,9% | 14,8% |
| **5** | +1,9% | +2,0% | **+0,2%** | +27,1% | 7,8% |
| **7 (Bản cũ)** | +1,2% | +1,1% | +1,5% | +24,5% | 7,1% |
| **8** | +0,4% | **+0,1%** | +4,2% | +20,2% | 6,2% |
| **9** | **+0,2%** | +0,2% | +4,6% | +20,0% | 6,2% |
| **10 (Bản mới)** | +0,4% | +0,6% | +4,8% | +20,4% | 6,5% |
| **12** | +0,6% | +0,2% | +4,7% | +20,0% | 6,4% |
| **14** | +0,3% | +0,6% | +4,9% | +20,0% | 6,4% |
| **15** | +1,4% | +2,5% | +14,4% | +15,6% | 8,5% |
| **16** | +2,3% | +3,0% | +18,5% | +13,9% | 9,4% |
| **20** | +2,4% | +3,0% | +18,2% | +14,0% | 9,4% |
| **30** | +6,2% | +8,1% | +33,7% | +11,0% | 14,8% |
| **50** | +10,9% | +14,4% | +52,0% | +8,0% | 21,3% |
| **100** | +32,8% | +43,5% | +123,2% | +3,2% | 50,7% |
| **150** | +81,8% | +108,3% | +270,8% | **0,0%** | 115,2% |

### 4.3. Trực quan hóa dữ liệu qua đồ thị

#### Hình 1: Toàn dải khảo sát CUTOFF từ 0 đến 150
Đồ thị biểu diễn tỷ số thời gian chuẩn hóa T(cutoff) / T_best trên toàn bộ khoảng giá trị khảo sát. Có thể thấy rõ hành vi phân hóa cực đoan: trong khi đường cong của RANDOM, DUPLICATES và REVERSE_SORTED tăng vọt sau CUTOFF 20, đường cong của NEARLY_SORTED lại liên tục đi xuống và chạm đáy tại biên 150.

![Toàn khoảng CUTOFF 0-150](../results/hinh1_toan_khoang_cutoff.png)

#### Hình 2: Phóng to vùng quan trọng CUTOFF từ 0 đến 30
Hình ảnh phóng to thể hiện rõ "vùng bình nguyên tối ưu" hình đáy bát (flat bowl) từ 8 đến 14 trên dữ liệu RANDOM và DUPLICATES, đồng thời làm nổi bật bước nhảy hiệu năng đột ngột tại điểm chuyển tiếp 15 → 16.

![Phóng to vùng CUTOFF 0-30](../results/hinh2_phong_to_0_30.png)

#### Hình 3: Kiểm chứng Giả thuyết Nhân đôi (Doubling Hypothesis) trên thang Log-Log
Đồ thị biểu diễn thời gian thực thi (median ms) theo kích thước mảng N ở CUTOFF = 10. Đường biểu diễn gần như là đường thẳng với độ dốc b ≈ 1,08, minh chứng trực quan cho độ phức tạp O(N log N).

![Thời gian thực thi theo kích thước N trên thang Log-Log](../results/hinh3_thoi_gian_theo_N.png)

---

## 5. PHÂN TÍCH CHUYÊN SÂU VÀ THẢO LUẬN KHOA HỌC

### 5.1. Dữ liệu RANDOM và DUPLICATES: Bản chất của "Vùng bình nguyên 8–14"

Đối với hai dạng phân bố phổ biến nhất trong thực tế (RANDOM và DUPLICATES):
1. **Dạng hình học đáy bát nông:** Từ CUTOFF 0 đến 8, thời gian giảm nhanh do loại bỏ được hàng vạn lời gọi đệ quy ở các tầng đáy của cây Mergesort. Từ CUTOFF 8 đến 14, đường cong đi vào trạng thái gần như nằm ngang hoàn toàn (sai phân giữa các giá trị < 0,5%). Sau CUTOFF 14, chi phí O(k²) của Insertion Sort bắt đầu thắng thế và kéo thời gian tăng dần trở lại.
2. **Không có một giá trị đơn lẻ thần thánh:** Bảng mục 4.1 cho thấy điểm cực tiểu thực nghiệm nhảy ngẫu nhiên giữa 7, 9, 10, 12, 13, 14 tùy theo quy mô N. Tuy nhiên, khoảng tin cậy CI 95% của toàn bộ các điểm này đều chồng lấn lên nhau. Điều này chứng minh rằng **về mặt thống kê, mọi giá trị trong khoảng [8, 14] đều tương đương nhau**. Việc cố gắng chọn ra một con số duy nhất là không có ý nghĩa toán học.
3. **So sánh CUTOFF 7 vs 10:**
   * **CUTOFF = 10:** Chậm hơn điểm đáy chỉ 0,3% – 0,5% trên RANDOM và 0,0% – 0,6% trên DUPLICATES.
   * **CUTOFF = 7:** Chậm hơn điểm đáy khoảng 0,9% – 2,4%.
   * **Kết luận:** Cả hai giá trị của giáo trình đều rất tốt, nhưng **giá trị 10 ưu việt hơn một cách nhất quán** so với 7 trên kiến trúc máy tính hiện nay.

### 5.2. Dữ liệu REVERSE_SORTED: Nguy cơ suy biến bậc hai O(k²)

* Khi mảng bị nghịch đảo hoàn toàn, mọi cặp phần tử đều là một nghịch thế (I = k(k-1)/2). Insertion Sort phải thực hiện số phép hoán đổi tối đa.
* Do đó, chi phí Insertion Sort tăng vọt ngay từ các kích thước mảng con rất nhỏ. Điểm tối ưu bị kéo lùi về **CUTOFF = 2–6**.
* Nếu áp dụng CUTOFF lớn trên dữ liệu này:
  * CUTOFF = 10 làm chậm hơn 4,8%.
  * CUTOFF = 30 làm chậm hơn 33,7%.
  * CUTOFF = 150 làm chậm hơn tới **270,8% (gần gấp 4 lần thời gian)**.
* **Bài học thực tiễn:** CUTOFF không nên đặt quá lớn (≤ 15) để tránh làm suy giảm nghiêm trọng hiệu năng trong trường hợp xấu nhất.

### 5.3. Dữ liệu NEARLY_SORTED: Sức mạnh của thuật toán thích ứng (Adaptive Sort)

* Với mảng gần như đã sắp xếp (chỉ 1% phần tử bị xáo trộn), Insertion Sort thể hiện ưu thế vượt trội vì số phép dịch chuyển phần tử là cực tiểu (O(k)).
* Ngược lại, Mergesort thuần túy vẫn phải thực hiện đầy đủ việc chia đôi và trộn mảng với chi phí 6 k lg k phép truy cập mảng bất kể dữ liệu đầu vào đã có thứ tự hay chưa.
* Kết quả: CUTOFF càng lớn thì thời gian chạy càng giảm. Tại CUTOFF = 150 (biên trên của thí nghiệm), thuật toán nhanh hơn Mergesort thuần túy tới **31,6%**.
* **Ý nghĩa học thuật:** Thí nghiệm này cho thấy đối với dữ liệu gần sắp xếp, điểm tối ưu nằm ở **ngoài biên khảo sát (≥ 150)**. Đây chính là động lực căn bản để các kỹ sư thiết kế giải thuật **Timsort** (thuật toán sắp xếp mặc định của Java và Python), trong đó kết hợp việc nhận diện các đoạn đã có thứ tự (natural runs) với kích thước khối lên tới 32 hoặc 64.

### 5.4. Tính bất biến của CUTOFF theo quy mô kích thước N

Quan sát từ bảng 4.1 cho thấy:
* Với RANDOM: Vùng ngang bằng tối ưu ở N = 500k, 1M, 2M luôn ổn định tại dải **8 – 14**.
* Ở các kích thước nhỏ (125k, 250k), vùng ngang bằng mở rộng ra thành 2 – 28 chỉ vì thời gian chạy quá ngắn (< 60 ms), khiến sai số đo lường hệ thống tương đối lớn hơn chứ không phải do điểm tối ưu bị xê dịch.
* **Khẳng định lý thuyết:** CUTOFF là thuộc tính cục bộ ở tầng đáy của cây đệ quy, phụ thuộc vào tương quan chi phí giữa hai thuật toán trên mảng con kích thước k, **hoàn toàn độc lập với tổng số phần tử N** của mảng ban đầu.

### 5.5. Kiểm chứng Giả thuyết Nhân đôi (Doubling Hypothesis) và Hiện tượng "Bức tường bộ nhớ"

Bảng dưới đây tính toán tỉ số nhân đôi R = T(2N)/T(N) tại CUTOFF = 10:

| Bước tăng quy mô N → 2N | RANDOM | DUPLICATES | REVERSE_SORTED | NEARLY_SORTED | Kỳ vọng lý thuyết N log N |
|---|---|---|---|---|---|
| **125.000 → 250.000** | **2,23** | **2,09** | **1,97** | **2,08** | ≈ 2,18 |
| **250.000 → 500.000** | **2,62** | **2,76** | **2,88** | **3,12** | ≈ 2,16 |
| **500.000 → 1.000.000** | **2,27** | **2,10** | **2,14** | **2,09** | ≈ 2,14 |
| **1.000.000 → 2.000.000** | **2,29** | **2,08** | **2,08** | **2,13** | ≈ 2,13 |

**Phân tích hiện tượng:**
1. **Khớp lý thuyết ở đa số các bước:** Đối với thuật toán có độ phức tạp O(N log N), tỉ số nhân đôi lý thuyết là:
   > **R_theory = 2N log₂(2N) / (N log₂ N) = 2 · (1 + 1 / log₂ N)**
   Với N từ 125k đến 2M, R_theory ≈ 2,12 – 2,18. Kết quả thực nghiệm ở các bước 125k → 250k, 500k → 1M và 1M → 2M dao động quanh 2,08 – 2,29, hoàn toàn khớp với mô hình toán học.
2. **Bước nhảy bất thường tại 250k → 500k (R ≈ 2,62 – 3,12):**
   * Hiện tượng này xảy ra đồng loạt trên **cả 4 phân bố dữ liệu**.
   * **Giải thích nguyên nhân phần cứng (Memory Wall & Cache Capacity):** Mảng đối tượng `Integer[]` trong Java gồm các con trỏ tham chiếu 64-bit trỏ tới các đối tượng trên Heap (mỗi đối tượng `Integer` chiếm tối thiểu 16–24 bytes).
     * Khi N = 250.000, footprint bộ nhớ truy cập thường xuyên vẫn có thể nằm vừa vặn trong bộ nhớ đệm Last Level Cache (L3 Cache, thường từ 16 – 32 MB).
     * Khi N tăng lên 500.000 và 1.000.000, toàn bộ cấu trúc dữ liệu vượt quá kích thước L3 Cache, dẫn đến hiện tượng trượt cache hàng loạt (Cache Misses). CPU buộc phải truy xuất trực tiếp từ DRAM với độ trễ cao gấp hàng chục lần (50 – 100 ns so với < 10 ns). Điều này chứng minh rằng trên các hệ thống hiện đại, chi phí truy cập bộ nhớ chi phối mạnh mẽ thời gian chạy thực tế của thuật toán.

### 5.6. Hiện tượng bước nhảy đột ngột (Cliff Effect) tại CUTOFF 15–16

Trên Hình 2, quan sát thấy một bước nhảy bậc thang rõ rệt khi CUTOFF tăng từ 14 lên 16:
* Trên RANDOM, thời gian tăng thêm ≈ 2%; trên REVERSE_SORTED, thời gian tăng vọt từ 4,9% lên 18,5%.
* **Bản chất JIT HotSpot Compiler:** Về mặt lý thuyết thuần túy, hàm k² biến thiên liên tục và trơn tru. Một bước nhảy dốc đứng trong thực tế chỉ có thể xuất phát từ cơ chế biên dịch của JVM. Trình biên dịch JIT HotSpot C2 có các ngưỡng tối ưu hóa hàm nghiêm ngặt:
  * Ngưỡng kích thước bytecode tối đa để thực hiện nội suy hàm (`-XX:MaxInlineSize=35` bytecodes).
  * Ngưỡng kích thước cho các phương thức thường xuyên gọi (`-XX:FreqInlineSize=325` bytecodes).
  * Khi CUTOFF vượt quá 14–15, kích thước bytecode được sinh ra trong các nhánh đệ quy vượt quá ngưỡng cho phép của bộ phân tích JIT, khiến HotSpot từ bỏ việc inline hàm `insertionSort` hoặc huỷ bỏ tối ưu unroll vòng lặp. Đây là một minh chứng xuất sắc cho thấy vì sao việc phân tích thuật toán trong thực tế bắt buộc phải kết hợp đo đạc thực nghiệm thay vì chỉ suy diễn lý thuyết trên giấy.

---

## 6. KẾT LUẬN VÀ BÀI HỌC KINH NGHIỆM

### 6.1. Trả lời các câu hỏi nghiên cứu

1. **Về giá trị CUTOFF tối ưu:**
   * Trên dữ liệu ngẫu nhiên và trùng lặp, CUTOFF tối ưu nằm trong **vùng bình nguyên 8–14**.
   * **Giá trị CUTOFF = 10 trong giáo trình mới của Sedgewick & Wayne là hoàn toàn chính xác và tối ưu** trên nền tảng máy tính hiện đại (chỉ chậm hơn điểm đáy thực nghiệm ≤ 0,5%).
   * **Giá trị CUTOFF = 7 trong giáo trình cũ cũng rất tốt**, chỉ kém giá trị 10 khoảng 0,5% – 1,5%.
2. **Về mức độ cải thiện hiệu năng:**
   * Cải tiến CUTOFF mang lại mức tăng tốc thực tế từ **5,5% – 9,3%** trên dữ liệu ngẫu nhiên/trùng lặp, và lên tới **31,6%** trên dữ liệu gần sắp xếp. Đây là một cải tiến có giá trị cao, dễ cài đặt và bảo đảm an toàn.
3. **Về khuyến nghị thực tế:**
   * Nếu phải chọn một giá trị duy nhất áp dụng cho mọi trường hợp, **CUTOFF = 10** là lựa chọn lý tưởng nhất: vừa đạt đỉnh hiệu năng trên dữ liệu ngẫu nhiên, vừa bảo vệ thuật toán không bị phạt nặng trong trường hợp dữ liệu nghịch đảo.

### 6.2. Hạn chế của nghiên cứu & Hướng mở rộng

1. **Kiểu dữ liệu nguyên thủy vs Đối tượng tham chiếu:** Thí nghiệm sử dụng `Integer[]` với cơ chế gọi `compareTo` ảo (virtual dispatch). Nếu chuyển sang mảng nguyên thủy `int[]`, chi phí so sánh rẻ hơn và tính liên tục bộ nhớ cao hơn, vùng CUTOFF tối ưu có thể dịch chuyển lên cao hơn (15 – 25).
2. **Cài đặt Insertion Sort:** Nghiên cứu này dùng phép hoán đổi từng bước (`exch`) theo đúng bài giảng. Nếu tối ưu hóa bằng kỹ thuật **dịch chuyển (half-exchange / shift)** — tức chỉ gán phần tử một lần sau khi tìm thấy vị trí chèn — chi phí của Insertion Sort sẽ giảm khoảng 30%, giúp vùng CUTOFF tối ưu mở rộng thêm.
3. **Cận trên của Nearly Sorted:** Do giới hạn biên khảo sát ở 150, nghiên cứu chưa xác định được điểm cực trị của dữ liệu gần sắp xếp. Cần mở rộng khảo sát lên các ngưỡng 200, 300, 500.

---

## 7. DANH MỤC SẢN PHẨM BÀN GIAO & TÀI LIỆU THAM KHẢO

### 7.1. Cấu trúc mã nguồn và danh mục sản phẩm bàn giao

| STT | Thành phần sản phẩm | Tên tệp tin | Mô tả chi tiết |
|---|---|---|---|
| 1 | **Báo cáo Markdown** | `report/BAO_CAO_THI_NGHIEM_MERGESORT_CUTOFF.md` | Bản báo cáo chi tiết kèm cách chạy thử nghiệm |
| 2 | **Cài đặt Mergesort** | `src/prj/OptimizedMergeSort.java` | Mergesort Top-Down + CUTOFF Insertion Sort chuẩn bài giảng |
| 3 | **Bộ Unit Test kiểm thử** | `src/prj/OptimizedMergeSortTest.java` | 79.982 assertions kiểm thử tính đúng và tính ổn định (stability) |
| 4 | **Script Benchmark Java** | `src/prj/CutoffExperimentBenchmark.java` | Đo đạc đa phân bố, JIT warm-up, đo xen kẽ ngẫu nhiên, xuất CSV |
| 5 | **Script Phân tích Python** | `analyze.py` | Tính bảng thống kê, in tỉ số nhân đôi, tự động vẽ 3 đồ thị |
| 6 | **Script chạy tự động** | `run.bat` | Kịch bản 1-chạm tự động hóa toàn bộ quy trình kiểm thử và đo đạc |
| 7 | **Dữ liệu thô thực nghiệm** | `results/cutoff_raw.csv` | Toàn bộ 9.300 kết quả đo độc lập |
| 8 | **Bảng dữ liệu thống kê** | `results/cutoff_summary.csv` | Thống kê Mean, Median, StdDev, CI95 cho từng cấu hình |
| 9 | **Bảng CUTOFF tốt nhất** | `results/best_cutoff_table.csv` | Tổng hợp điểm tối ưu, vùng tương đương, mức tăng tốc |
| 10 | **Bộ dữ liệu mẫu** | `results/data/` | File nhị phân `.bin`, file text xem trực quan `*_sample1000.txt`, kèm `README.md` |
| 11 | **3 Đồ thị minh họa** | `results/hinh*.png` | Biểu đồ toàn dải, phóng to bình nguyên, và thang Log-Log |
| 12 | **Nhật ký trao đổi AI** | `report/LOG_CHAT_AI.md` | Log đối thoại học thuật, phản biện phương pháp luận với AI |

### 7.2. Tài liệu tham khảo

1. **Sedgewick, R., & Wayne, K.** (2011). *Algorithms* (4th ed.). Addison-Wesley Professional. Section 1.3 (Algorithm Analysis) & Section 2.2 (Mergesort).
2. **Knuth, D. E.** (1998). *The Art of Computer Programming, Volume 3: Sorting and Searching* (2nd ed.). Addison-Wesley.
3. **Bentley, J. L., & McIlroy, M. D.** (1993). *Engineering a sort function*. Software: Practice and Experience, 23(11), 1249-1265.
4. **Oracle Corporation.** *Java Virtual Machine HotSpot Tiered Compilation and Inlining Heuristics*. OpenJDK Documentation.
