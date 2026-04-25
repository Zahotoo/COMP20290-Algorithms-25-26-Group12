from pathlib import Path

import numpy as np
import matplotlib.pyplot as plt

# =========================================================
# OUTPUT CONFIG
# =========================================================
OUTPUT_DIR = Path("paper_bar_figures")
OUTPUT_DIR.mkdir(exist_ok=True)

plt.rcParams.update({
    "figure.dpi": 160,
    "savefig.dpi": 400,
    "font.size": 11,
    "axes.titlesize": 13,
    "axes.labelsize": 11,
    "legend.fontsize": 10,
    "xtick.labelsize": 10,
    "ytick.labelsize": 10,
    "axes.linewidth": 0.8,
    "axes.facecolor": "white",
    "figure.facecolor": "white",
    "grid.alpha": 0.20,
    "grid.linewidth": 0.5,
})

# =========================================================
# COLORS
# =========================================================
# Blue requested from your first image: approximately #2e74b5
CLASSIC_BLUE = "#2e74b5"

STYLE_MAP = {
    "Random Input (T1)": {"color": "#ef3b2c"},
    "Sorted Input (T2)": {"color": CLASSIC_BLUE},

    "Preprocess": {"color": "#ef3b2c"},
    "Local Count": {"color": "#fcae91"},
    "Other Overhead": {"color": "#d9e6f2"},
    "Classic Count": {"color": CLASSIC_BLUE},

    # Table 3: Proposed Hybrid red; the other two blue family
    "QuickSort": {"color": "#9ecae1"},
    "Quick+Insertion": {"color": CLASSIC_BLUE},
    "Proposed Hybrid": {"color": "#ef3b2c"},
}

# =========================================================
# REAL BENCHMARK DATA
# =========================================================

# -------------------------
# Table 1
# -------------------------
table1_labels = ["1,000,000", "2,000,000", "3,000,000"]
table1_random = [13.223, 28.965, 54.123]
table1_sorted = [5.835, 8.109, 13.222]
table1_speedup = [2.27, 3.57, 4.09]

# -------------------------
# Table 2
# -------------------------
table2_labels = ["1,000", "2,000", "3,000"]
table2_preprocess = [0.060, 0.098, 0.170]
table2_local_count = [0.193, 0.226, 0.223]
table2_hybrid_total = [0.278, 0.377, 0.454]
table2_classic_count = [0.779, 0.774, 0.884]

table2_other_overhead = [
    max(total - pre - local, 0.0)
    for total, pre, local in zip(table2_hybrid_total, table2_preprocess, table2_local_count)
]

# -------------------------
# Table 3
# -------------------------
table3_labels = ["1,000,000", "2,000,000"]
table3_quick = [73.089, 154.358]
table3_quick_insertion = [64.784, 136.869]
table3_proposed = [43.888, 92.038]

# =========================================================
# HELPERS
# =========================================================
def paper_axes(ax, xlabel, ylabel):
    ax.set_xlabel(xlabel, fontweight="bold")
    ax.set_ylabel(ylabel, fontweight="bold")
    ax.grid(True, axis="y")
    ax.set_axisbelow(True)
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)


def add_value_labels(ax, bars, fontsize=9, y_offset=4):
    for bar in bars:
        height = bar.get_height()
        text = f"{height:.3f}" if height < 10 else f"{height:.2f}"
        ax.annotate(
            text,
            xy=(bar.get_x() + bar.get_width() / 2, height),
            xytext=(0, y_offset),
            textcoords="offset points",
            ha="center",
            va="bottom",
            fontsize=fontsize
        )


def savefig_clean(fig, path: Path):
    fig.savefig(path, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print(f"Saved: {path}")


# =========================================================
# TABLE 1
# =========================================================
def plot_table1_bar():
    x = np.arange(len(table1_labels))
    width = 0.34

    fig, ax = plt.subplots(figsize=(10, 6))

    bars1 = ax.bar(
        x - width / 2,
        table1_random,
        width,
        label="Random Input (T1)",
        color=STYLE_MAP["Random Input (T1)"]["color"]
    )
    bars2 = ax.bar(
        x + width / 2,
        table1_sorted,
        width,
        label="Sorted Input (T2)",
        color=STYLE_MAP["Sorted Input (T2)"]["color"]
    )

    ax.set_title("Table 1: Classic Counting Sort on Random vs Sorted Input", fontweight="bold")
    ax.set_xticks(x)
    ax.set_xticklabels(table1_labels)
    paper_axes(ax, xlabel="Input size n = r", ylabel="Execution Time (ms)")
    ax.legend(frameon=False, loc="upper right")

    add_value_labels(ax, bars1)
    add_value_labels(ax, bars2)

    ymax = max(max(table1_random), max(table1_sorted))
    ax.set_ylim(0, ymax * 1.26)

    for i, s in enumerate(table1_speedup):
        ax.text(
            x[i],
            max(table1_random[i], table1_sorted[i]) + ymax * 0.10,
            f"Speedup: {s:.2f}×",
            ha="center",
            va="bottom",
            fontsize=10,
            fontweight="bold"
        )

    plt.tight_layout()
    savefig_clean(fig, OUTPUT_DIR / "table1_bar_chart.png")


# =========================================================
# TABLE 2
# =========================================================
def plot_table2_bar():
    x = np.arange(len(table2_labels))
    width = 0.30

    fig, ax = plt.subplots(figsize=(12, 6.8))

    # Hybrid stacked bar (left bar in each group)
    bars_pre = ax.bar(
        x - width / 2,
        table2_preprocess,
        width,
        label="Preprocess Modified QuickSort",
        color=STYLE_MAP["Preprocess"]["color"]
    )

    bars_local = ax.bar(
        x - width / 2,
        table2_local_count,
        width,
        bottom=table2_preprocess,
        label="Local CountingSort",
        color=STYLE_MAP["Local Count"]["color"]
    )

    bars_other = ax.bar(
        x - width / 2,
        table2_other_overhead,
        width,
        bottom=np.array(table2_preprocess) + np.array(table2_local_count),
        label="Other Overhead",
        color=STYLE_MAP["Other Overhead"]["color"]
    )

    # Classic count bar (right bar in each group)
    bars_classic = ax.bar(
        x + width / 2,
        table2_classic_count,
        width,
        label="Classic CountingSort",
        color=STYLE_MAP["Classic Count"]["color"]
    )

    ax.set_title("Table 2: Counting Sort with and without Preprocessing (r >> n)", fontweight="bold")
    ax.set_xticks(x)
    ax.set_xticklabels(table2_labels)
    paper_axes(ax, xlabel="Input size n (r = 1,000,000)", ylabel="Execution Time (ms)")
    ax.legend(frameon=False, loc="upper right")

    add_value_labels(ax, bars_classic)

    ymax = max(max(table2_hybrid_total), max(table2_classic_count))
    ax.set_ylim(0, ymax * 1.34)

    for i in range(len(table2_labels)):
        hybrid_x = x[i] - width / 2
        classic_x = x[i] + width / 2

        pre = table2_preprocess[i]
        local = table2_local_count[i]
        other = table2_other_overhead[i]
        total = table2_hybrid_total[i]
        classic = table2_classic_count[i]
        speedup = classic / total

        # Total label above hybrid stack
        ax.text(
            hybrid_x,
            total + ymax * 0.02,
            f"Total = {total:.3f}",
            ha="center",
            va="bottom",
            fontsize=10,
            fontweight="bold"
        )

        # Move part labels further left so they do not cover the bar
        part_text = (
            f"Pre: {pre:.3f}\n"
            f"Local: {local:.3f}\n"
            f"Overhead: {other:.3f}"
        )
        ax.text(
            hybrid_x - 0.23,
            total * 0.60,
            part_text,
            ha="right",
            va="center",
            fontsize=8.8
        )

        # Move faster label downward a bit so it does not hit legend/top area
        ax.text(
            classic_x - 0.02,
            max(total, classic) + ymax * 0.09,
            f"{speedup:.2f}× faster",
            ha="center",
            va="bottom",
            fontsize=12,
            fontweight="bold"
        )

    plt.tight_layout()
    savefig_clean(fig, OUTPUT_DIR / "table2_bar_chart.png")


# =========================================================
# TABLE 3
# =========================================================
def plot_table3_bar():
    x = np.arange(len(table3_labels))
    width = 0.24

    fig, ax = plt.subplots(figsize=(10.5, 6.4))

    bars1 = ax.bar(
        x - width,
        table3_quick,
        width,
        label="QuickSort",
        color=STYLE_MAP["QuickSort"]["color"]
    )
    bars2 = ax.bar(
        x,
        table3_quick_insertion,
        width,
        label="Quick+Insertion",
        color=STYLE_MAP["Quick+Insertion"]["color"]
    )
    bars3 = ax.bar(
        x + width,
        table3_proposed,
        width,
        label="Proposed Hybrid",
        color=STYLE_MAP["Proposed Hybrid"]["color"]
    )

    ax.set_title("Table 3: QuickSort vs Quick+Insertion vs Proposed Hybrid", fontweight="bold")
    ax.set_xticks(x)
    ax.set_xticklabels(table3_labels)
    paper_axes(ax, xlabel="Input size n = r", ylabel="Execution Time (ms)")
    ax.legend(frameon=False, loc="upper right")

    add_value_labels(ax, bars1)
    add_value_labels(ax, bars2)
    add_value_labels(ax, bars3)

    ymax = max(max(table3_quick), max(table3_quick_insertion), max(table3_proposed))
    ax.set_ylim(0, ymax * 1.18)

    for i in range(len(table3_labels)):
        best_other = min(table3_quick[i], table3_quick_insertion[i])
        improvement = best_other / table3_proposed[i]

        # Put annotation lower so it does not get blocked by legend/top boundary
        ax.text(
            x[i],
            max(table3_quick[i], table3_quick_insertion[i], table3_proposed[i]) + ymax * 0.05,
            f"{improvement:.2f}× better",
            ha="center",
            va="bottom",
            fontsize=12,
            fontweight="bold"
        )

    plt.tight_layout()
    savefig_clean(fig, OUTPUT_DIR / "table3_bar_chart.png")


# =========================================================
# MAIN
# =========================================================
def main():
    plot_table1_bar()
    plot_table2_bar()
    plot_table3_bar()

    print("\nAll bar charts generated successfully.")
    print(
        "\nWhy Preprocess + Local Count != Hybrid Total:\n"
        "Hybrid Total also includes extra runtime overhead such as recursion, "
        "function-call cost, condition checks, partition management, and other "
        "non-profiled control-flow work."
    )


if __name__ == "__main__":
    main()