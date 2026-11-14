# BỘ DỮ LIỆU THỰC NGHIỆM MERGESORT CUTOFF (DATASET SPECIFICATION)

Thư mục này chứa bộ dữ liệu mẫu phục vụ việc kiểm thử, đo đạc hiệu năng và tái lập thực nghiệm xác định ngưỡng CUTOFF tối ưu cho thuật toán Mergesort.

---

## 1. Danh mục các tệp dữ liệu

| Tên tệp tin | Định dạng | Quy mô N | Mô tả đặc trưng phân bố |
|---|---|---|---|
| `RANDOM_125000.bin` | Nhị phân (Binary) | 125.000 | Mảng số nguyên ngẫu nhiên 32-bit phân bố đều trên toàn miền `Integer` |
| `DUPLICATES_125000.bin` | Nhị phân (Binary) | 125.000 | Mảng số nguyên miền hẹp [0, 99], chứa tỷ lệ phần tử trùng lặp rất cao |
| `REVERSE_SORTED_125000.bin` | Nhị phân (Binary) | 125.000 | Mảng giảm dần N, N-1, ..., 1 (trường hợp tồi nhất của Insertion Sort) |
| `NEARLY_SORTED_125000.bin` | Nhị phân (Binary) | 125.000 | Mảng tăng dần với 1% phép hoán vị ngẫu nhiên (gần như đã sắp xếp) |
| `*_sample1000.txt` | Văn bản (Plaintext) | 1.000 | Trích xuất 1.000 phần tử đầu tiên của mỗi phân bố phục vụ việc xem trực quan bằng Notepad |

---

## 2. Quy chuẩn cấu trúc tệp nhị phân (.bin)

Các tệp `.bin` được ghi theo chuẩn chuẩn `java.io.DataOutputStream` (Big-Endian, Network Byte Order):
* **4 bytes đầu tiên:** Số nguyên 32-bit có dấu biểu diễn kích thước mảng N (`int`).
* **Các bytes tiếp theo:** Dãy N × 4 bytes tuần tự biểu diễn từng phần tử số nguyên 32-bit (`int`).

### Đoạn mã Java mẫu để đọc tệp:

```java
import java.io.*;
import java.nio.file.*;

public class ReadBinaryDataExample {
    public static Integer[] loadData(Path path) throws IOException {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            int n = dis.readInt();
            Integer[] arr = new Integer[n];
            for (int i = 0; i < n; i++) {
                arr[i] = dis.readInt();
            }
            return arr;
        }
    }
}
```

### Đoạn mã Python mẫu để đọc tệp:

```python
import struct

def load_data(filename):
    with open(filename, "rb") as f:
        (n,) = struct.unpack(">i", f.read(4))
        data = struct.unpack(f">{n}i", f.read(4 * n))
    return list(data)
```

---

## 3. Công thức sinh dữ liệu và tính tái lập (Reproducibility)

Để bảo đảm tính khách quan và khả năng tái lập 100% độc lập giữa các lần chạy, mọi phân bố dữ liệu được sinh bằng bộ sinh số giả ngẫu nhiên `java.util.Random` với công thức Seed tất định:

**Seed = 42 + 1000 * N + DistributionIndex**

Trong đó:
* Base Seed = 42
* DistributionIndex:
  - `RANDOM` = 0
  - `NEARLY_SORTED` = 1
  - `REVERSE_SORTED` = 2
  - `DUPLICATES` = 3
