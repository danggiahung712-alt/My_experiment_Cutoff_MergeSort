"""Script phan tich ket qua thuc nghiem, tinh bang thong ke va ve do thi chuan khoa hoc.
Cach chay:
    python analyze.py
hoac:
    python analyze.py results/cutoff_summary.csv
"""

import sys
from pathlib import Path
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")  # Chay khong can GUI desktop
import matplotlib.pyplot as plt

def main():
    # 1. Xac dinh duong dan file du lieu
    summary_path = None
    if len(sys.argv) > 1:
        summary_path = Path(sys.argv[1])
    else:
        candidates = [
            Path("results/cutoff_summary.csv"),
            Path("cutoff_summary.csv"),
            Path("../results/cutoff_summary.csv")
        ]
        for c in candidates:
            if c.exists():
                summary_path = c
                break

    if not summary_path or not summary_path.exists():
        print(f"[!] Khong tim thay file cutoff_summary.csv!")
        sys.exit(1)

    output_dir = summary_path.parent if summary_path.parent.name == "results" else Path("results")
    output_dir.mkdir(parents=True, exist_ok=True)

    print(f"[*] Dang doc du lieu tu: {summary_path.resolve()}")
    df = pd.read_csv(summary_path)

    # 2. Xay dung bang tong hop best_cutoff_table.csv
    rows = []
    for (n, dist), g in df.groupby(["Size_N", "Distribution"]):
        g = g.sort_values("Cutoff")
        best = g.loc[g["Median_ms"].idxmin()]
        base = g[g["Cutoff"] == 0].iloc[0]
        c7 = g[g["Cutoff"] == 7].iloc[0] if 7 in g["Cutoff"].values else None
        c10 = g[g["Cutoff"] == 10].iloc[0] if 10 in g["Cutoff"].values else None

        # Vung ngang bang thong ke: khoang tin cay 95% chong lan voi diem tot nhat
        tie = g[g["Mean_ms"] - g["CI95_ms"] <= best["Mean_ms"] + best["CI95_ms"]]
        
        c7_slow = round(100 * (c7["Median_ms"] - best["Median_ms"]) / best["Median_ms"], 1) if c7 is not None else 0.0
        c10_slow = round(100 * (c10["Median_ms"] - best["Median_ms"]) / best["Median_ms"], 1) if c10 is not None else 0.0

        rows.append({
            "N": n,
            "Distribution": dist,
            "Best_cutoff(median)": int(best["Cutoff"]),
            "Best_median_ms": round(best["Median_ms"], 2),
            "Cutoff0_median_ms": round(base["Median_ms"], 2),
            "Speedup_vs_0_%": round(100 * (1 - best["Median_ms"] / base["Median_ms"]), 1),
            "Cutoffs_ngang_best": f"{int(tie['Cutoff'].min())}-{int(tie['Cutoff'].max())}",
            "Time_cutoff7_ms": round(c7["Median_ms"], 2) if c7 is not None else 0.0,
            "Time_cutoff10_ms": round(c10["Median_ms"], 2) if c10 is not None else 0.0,
            "Cutoff7_slow_%": c7_slow,
            "Cutoff10_slow_%": c10_slow
        })

    best_table = pd.DataFrame(rows)
    best_table_path = output_dir / "best_cutoff_table.csv"
    best_table.to_csv(best_table_path, index=False)
    print("\n" + "=" * 90)
    print("                    BANG TONG HOP CUTOFF TOI UU & VUNG TUONG DUONG")
    print("=" * 90)
    print(best_table[["N", "Distribution", "Best_cutoff(median)", "Best_median_ms", "Speedup_vs_0_%", "Cutoffs_ngang_best", "Cutoff7_slow_%", "Cutoff10_slow_%"]].to_string(index=False))
    print(f"\n-> Da luu: {best_table_path.resolve()}")

    # 3. Kiem chung Gia thuyet Nhan doi (Doubling Hypothesis) tai CUTOFF = 10
    df10 = df[df["Cutoff"] == 10].sort_values(["Distribution", "Size_N"])
    print("\n" + "=" * 90)
    print("      KIEM CHUNG GIA THUYET NHAN DOI (DOUBLING HYPOTHESIS: T(2N)/T(N)) TAI CUTOFF = 10")
    print("=" * 90)
    doubling_rows = []
    sizes = sorted(df["Size_N"].unique())
    for dist in sorted(df["Distribution"].unique()):
        g = df10[df10["Distribution"] == dist].set_index("Size_N")
        for i in range(len(sizes) - 1):
            n1 = sizes[i]
            n2 = sizes[i + 1]
            if n1 in g.index and n2 in g.index:
                t1 = g.loc[n1, "Median_ms"]
                t2 = g.loc[n2, "Median_ms"]
                ratio = t2 / t1
                theory_ratio = 2 * (1 + 1 / np.log2(n1))
                doubling_rows.append({
                    "Distribution": dist,
                    "Step": f"{n1:,} -> {n2:,}",
                    "T(N)_ms": round(t1, 2),
                    "T(2N)_ms": round(t2, 2),
                    "Ratio_Thuc_te": round(ratio, 2),
                    "Ratio_Ly_thuyet": round(theory_ratio, 2),
                    "Sai_lech_%": round(100 * abs(ratio - theory_ratio) / theory_ratio, 1)
                })
    doubling_df = pd.DataFrame(doubling_rows)
    print(doubling_df.to_string(index=False))

    # 4. Ve Do thi 1: Toan khoang CUTOFF 0 - 150 (hinh1_toan_khoang_cutoff.png)
    print("\n[*] Dang ve Hinh 1: Toan khoang CUTOFF 0 - 150...")
    dists = ["RANDOM", "DUPLICATES", "REVERSE_SORTED", "NEARLY_SORTED"]
    fig1, axes1 = plt.subplots(2, 2, figsize=(14, 10))
    colors = ["#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd"]

    for ax, dist in zip(axes1.ravel(), dists):
        sub = df[df["Distribution"] == dist]
        if sub.empty: continue
        for (n, g), col in zip(sub.groupby("Size_N"), colors):
            g = g.sort_values("Cutoff")
            best_med = g["Median_ms"].min()
            norm_time = g["Median_ms"] / best_med
            ax.plot(g["Cutoff"], norm_time, "-o", ms=3.5, lw=1.6, label=f"N = {n:,}", color=col)
        ax.axvline(7, color="black", linestyle="--", alpha=0.6, label="Cutoff = 7 (Bản cũ)" if dist == "RANDOM" else "")
        ax.axvline(10, color="red", linestyle=":", alpha=0.8, label="Cutoff = 10 (Bản mới)" if dist == "RANDOM" else "")
        ax.set_title(f"Phân bố: {dist}", fontsize=12, fontweight="bold")
        ax.set_xlabel("Giá trị CUTOFF", fontsize=10)
        ax.set_ylabel("Chuẩn hóa: T(Cutoff) / T(Best)", fontsize=10)
        ax.grid(True, alpha=0.3, linestyle="--")
        ax.set_xlim(-2, 152)

    axes1[0, 0].legend(fontsize=8, loc="upper right")
    fig1.suptitle("HÌNH 1: HIỆU NĂNG MERGESORT TRÊN TOÀN BỘ DẢI CUTOFF (0 - 150)", fontsize=14, fontweight="bold", y=0.995)
    plt.tight_layout()
    hinh1_path = output_dir / "hinh1_toan_khoang_cutoff.png"
    fig1.savefig(hinh1_path, dpi=200)
    plt.close(fig1)
    print(f"-> Da luu: {hinh1_path.resolve()}")

    # 5. Ve Do thi 2: Phong to vung quan trong CUTOFF 0 - 30 (hinh2_phong_to_0_30.png)
    print("[*] Dang ve Hinh 2: Phong to vung CUTOFF 0 - 30 (Vung binh nguyen & Buoc nhay JIT)...")
    fig2, axes2 = plt.subplots(2, 2, figsize=(14, 10))

    for ax, dist in zip(axes2.ravel(), dists):
        sub = df[(df["Distribution"] == dist) & (df["Cutoff"] <= 30)]
        if sub.empty: continue
        for (n, g), col in zip(sub.groupby("Size_N"), colors):
            g = g.sort_values("Cutoff")
            best_med = g["Median_ms"].min()
            norm_time = g["Median_ms"] / best_med
            ax.plot(g["Cutoff"], norm_time, "-s", ms=4, lw=1.8, label=f"N = {n:,}", color=col)

        # Highlight vung binh nguyen 8 - 14 cho RANDOM va DUPLICATES
        if dist in ["RANDOM", "DUPLICATES"]:
            ax.axvspan(8, 14, color="#ffff99", alpha=0.35, label="Vùng bình nguyên tối ưu [8-14]")
            ax.axvline(15.5, color="purple", linestyle="--", alpha=0.7, label="Bước nhảy JIT Inlining (15->16)")

        ax.axvline(7, color="black", linestyle="--", alpha=0.6, label="Cutoff = 7" if dist == "REVERSE_SORTED" else "")
        ax.axvline(10, color="red", linestyle=":", alpha=0.8, label="Cutoff = 10" if dist == "REVERSE_SORTED" else "")
        ax.set_title(f"Phân bố: {dist} (Phóng to Cutoff 0 - 30)", fontsize=12, fontweight="bold")
        ax.set_xlabel("Giá trị CUTOFF", fontsize=10)
        ax.set_ylabel("Chuẩn hóa: T(Cutoff) / T(Best)", fontsize=10)
        ax.grid(True, alpha=0.35, linestyle="--")
        ax.set_xlim(-1, 31)

    axes2[0, 0].legend(fontsize=8, loc="upper right")
    fig2.suptitle("HÌNH 2: PHÓNG TO VÙNG CUTOFF 0 - 30 (BÌNH NGUYÊN TỐI ƯU & BƯỚC NHẢY JIT C2)", fontsize=14, fontweight="bold", y=0.995)
    plt.tight_layout()
    hinh2_path = output_dir / "hinh2_phong_to_0_30.png"
    fig2.savefig(hinh2_path, dpi=200)
    plt.close(fig2)
    print(f"-> Da luu: {hinh2_path.resolve()}")

    # 6. Ve Do thi 3: Kiem chung Doubling Hypothesis Log-Log (hinh3_thoi_gian_theo_N.png)
    print("[*] Dang ve Hinh 3: Kiem chung Doubling Hypothesis tren thang Log-Log...")
    fig3, ax3 = plt.subplots(figsize=(10, 6.5))

    dist_markers = {"RANDOM": "o", "DUPLICATES": "s", "REVERSE_SORTED": "^", "NEARLY_SORTED": "D"}
    for dist in dists:
        sub = df10[df10["Distribution"] == dist].sort_values("Size_N")
        if sub.empty: continue
        ax3.plot(sub["Size_N"], sub["Median_ms"], marker=dist_markers.get(dist, "o"),
                 lw=2, ms=6, label=f"{dist} (Cutoff=10)")

    # Duong tham chieu N log N
    ref_x = np.array(sizes)
    ref_y = ref_x * np.log2(ref_x)
    ref_y = ref_y * (df10[df10["Distribution"] == "RANDOM"]["Median_ms"].iloc[0] / ref_y[0])
    ax3.plot(ref_x, ref_y, "k--", lw=1.5, alpha=0.6, label="Đường lý thuyết chuẩn N log N")

    # Danh dau buoc nhay bo nho L3 cache tai 250k -> 500k
    ax3.axvspan(250000, 500000, color="gray", alpha=0.15, label="Chuyển tiếp L3 Cache -> DRAM")

    ax3.set_xscale("log")
    ax3.set_yscale("log")
    ax3.set_title("HÌNH 3: KIỂM CHỨNG GIẢ THUYẾT NHÂN ĐÔI (DOUBLING HYPOTHESIS TẠI CUTOFF = 10)", fontsize=13, fontweight="bold")
    ax3.set_xlabel("Kích thước mảng N (Thang Log)", fontsize=11)
    ax3.set_ylabel("Thời gian thực thi trung vị (ms, Thang Log)", fontsize=11)
    ax3.grid(True, which="both", alpha=0.3, linestyle="--")
    ax3.legend(fontsize=9, loc="upper left")
    plt.tight_layout()
    hinh3_path = output_dir / "hinh3_thoi_gian_theo_N.png"
    fig3.savefig(hinh3_path, dpi=200)
    plt.close(fig3)
    print(f"-> Da luu: {hinh3_path.resolve()}")

    print("\n==========================================================")
    print("  HOAN TAT PHAN TICH THONG KE VA XUAT DO THI THANH CONG!")
    print("==========================================================\n")

if __name__ == "__main__":
    main()
