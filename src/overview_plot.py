from pathlib import Path

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.ticker import ScalarFormatter

# =========================================================
# CONFIGURATION
# =========================================================
INPUT_CSV = "overview.csv"
OUTPUT_DIR = Path("overview_paper_figures")
OUTPUT_DIR.mkdir(exist_ok=True)

ALGORITHM_ORDER = ["Hybrid", "QuickSort", "Quick+Insertion", "JavaSort", "MergeSort"]

STYLE_MAP = {
    "Hybrid": {
        "marker": "o",
        "linewidth_raw": 1.2,
        "linewidth_trend": 2.4,
        "markersize": 4.8,
        "color": "#ef3b2c",
    },
    "QuickSort": {
        "marker": "s",
        "linewidth_raw": 1.2,
        "linewidth_trend": 2.2,
        "markersize": 4.6,
        "color": "#31a354",
    },
    "Quick+Insertion": {
        "marker": "^",
        "linewidth_raw": 1.2,
        "linewidth_trend": 2.2,
        "markersize": 4.8,
        "color": "#2170b5",
    },
    "JavaSort": {
        "marker": "D",
        "linewidth_raw": 1.2,
        "linewidth_trend": 2.2,
        "markersize": 4.4,
        "color": "#6baed6",
    },
    "MergeSort": {
        "marker": "P",
        "linewidth_raw": 1.2,
        "linewidth_trend": 2.2,
        "markersize": 4.6,
        "color": "#756bb1",
    },
}

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
# DATA PARSER
# =========================================================
def parse_overview_csv(path: str) -> pd.DataFrame:
    """
    Read overview.csv and convert it into tidy long format.

    Expected input format:
    n,Hybrid,QuickSort,Quick+Insertion,JavaSort,MergeSort
    1000,0.012,0.008,0.007,0.006,0.009
    ...
    """
    df_wide = pd.read_csv(path)

    required_cols = ["n"] + ALGORITHM_ORDER
    missing = [c for c in required_cols if c not in df_wide.columns]
    if missing:
        raise ValueError(f"Missing columns in overview.csv: {missing}")

    rows = []
    for _, row in df_wide.iterrows():
        n = int(row["n"])
        for alg in ALGORITHM_ORDER:
            rows.append({
                "algorithm": alg,
                "n": n,
                "time_ms": float(row[alg])
            })

    df = pd.DataFrame(rows)
    df["algorithm"] = pd.Categorical(df["algorithm"], categories=ALGORITHM_ORDER, ordered=True)
    df = df.sort_values(["algorithm", "n"]).reset_index(drop=True)
    return df

# =========================================================
# TITLE AND AXIS HELPERS
# =========================================================
def add_bold_suptitle(fig, title, y=0.985, fontsize=15):
    fig.suptitle(title, fontsize=fontsize, fontweight="bold", y=y)


def paper_axes(ax, xlabel="Input size n", ylabel="Average time (ms)", use_log_x=True):
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    ax.grid(True, which="major", axis="both")
    ax.set_axisbelow(True)

    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)

    if use_log_x:
        ax.set_xscale("log")

        # Better ticks for 1,000 ~ 500,000
        xticks = [1000, 2000, 5000,
                  10000, 20000, 50000,
                  100000, 200000, 500000]
        ax.set_xticks(xticks)
        ax.get_xaxis().set_major_formatter(ScalarFormatter())
        ax.ticklabel_format(style="plain", axis="x")
        ax.set_xlim(900, 550000)

    else:
        xfmt = ScalarFormatter(useMathText=False)
        xfmt.set_scientific(False)
        ax.xaxis.set_major_formatter(xfmt)

# =========================================================
# TREND LINE HELPERS
# =========================================================
def linear_trend_line(x, y, fit_on_log_x=True, clip_nonnegative=True):
    x = np.asarray(x, dtype=float)
    y = np.asarray(y, dtype=float)

    if len(x) < 2:
        trend = y.copy()
    else:
        if fit_on_log_x:
            coeffs = np.polyfit(np.log10(x), y, 1)
            trend = np.polyval(coeffs, np.log10(x))
        else:
            coeffs = np.polyfit(x, y, 1)
            trend = np.polyval(coeffs, x)

    if clip_nonnegative:
        trend = np.maximum(trend, 0.0)

    return trend


def add_series(ax, df_sub):
    for algorithm in ALGORITHM_ORDER:
        sdata = df_sub[df_sub["algorithm"] == algorithm].sort_values("n")
        if sdata.empty:
            continue

        style = STYLE_MAP[algorithm]
        x = sdata["n"].to_numpy()
        y = sdata["time_ms"].to_numpy()

        # raw dashed line + markers
        ax.plot(
            x,
            y,
            label=algorithm,
            marker=style["marker"],
            linestyle="--",
            linewidth=style["linewidth_raw"],
            markersize=style["markersize"],
            color=style["color"],
            alpha=0.95,
        )

        # fitted straight trend line
        y_trend = linear_trend_line(x, y, fit_on_log_x=True, clip_nonnegative=True)
        ax.plot(
            x,
            y_trend,
            linestyle="-",
            linewidth=style["linewidth_trend"],
            color=style["color"],
            alpha=0.95,
        )

# =========================================================
# LEGEND AND SAVE HELPERS
# =========================================================
def savefig_clean(fig, path: Path):
    fig.savefig(path, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print(f"Saved: {path}")


def sanitize_filename(name: str) -> str:
    return (
        name.lower()
        .replace(" ", "_")
        .replace("+", "plus")
        .replace(".", "")
        .replace("/", "_")
        .replace("__", "_")
    )

# =========================================================
# FIGURE GENERATORS
# =========================================================
def plot_compact_overview(df: pd.DataFrame):
    """
    Compact single figure averaged across all runs.
    This is the main figure similar to your example.
    """
    fig, ax = plt.subplots(figsize=(11.2, 6.3))

    add_series(ax, df)
    ax.set_title("Average Over All Tested Input Sizes", loc="left", fontweight="bold")
    paper_axes(ax, ylabel="Average time (ms)", use_log_x=True)

    handles, labels = ax.get_legend_handles_labels()
    fig.legend(
        handles,
        labels,
        loc="upper right",
        bbox_to_anchor=(0.995, 0.98),
        ncol=1,
        frameon=False,
    )

    add_bold_suptitle(
        fig,
        "Compact Overview of Sorting Performance",
        y=0.995,
        fontsize=15
    )

    plt.tight_layout(rect=[0, 0, 0.88, 0.94])
    out = OUTPUT_DIR / "compact_overview_sorting.png"
    savefig_clean(fig, out)


def plot_individual_overview(df: pd.DataFrame):
    """
    Single full-size figure without left subtitle.
    Useful for poster insertion.
    """
    fig, ax = plt.subplots(figsize=(12.0, 6.8))

    add_series(ax, df)
    paper_axes(ax, ylabel="Average time (ms)", use_log_x=True)

    handles, labels = ax.get_legend_handles_labels()
    fig.legend(
        handles,
        labels,
        loc="upper right",
        bbox_to_anchor=(0.995, 0.985),
        ncol=1,
        frameon=False,
    )

    add_bold_suptitle(
        fig,
        "Average Time Comparison Across Sorting Algorithms",
        y=0.99,
        fontsize=15
    )

    plt.tight_layout(rect=[0, 0, 0.88, 0.95])
    out = OUTPUT_DIR / "average_time_comparison.png"
    savefig_clean(fig, out)


def plot_hybrid_vs_baselines(df: pd.DataFrame):
    """
    Extra figure focusing on Hybrid vs all baselines.
    Same data, but often useful as a cleaner poster figure.
    """
    sub = df[df["algorithm"].isin(ALGORITHM_ORDER)].copy()

    fig, ax = plt.subplots(figsize=(11.2, 6.3))

    add_series(ax, sub)
    ax.set_title("Hybrid vs Standard Sorting Baselines", loc="left", fontweight="bold")
    paper_axes(ax, ylabel="Average time (ms)", use_log_x=True)

    handles, labels = ax.get_legend_handles_labels()
    fig.legend(
        handles,
        labels,
        loc="upper right",
        bbox_to_anchor=(0.995, 0.98),
        ncol=1,
        frameon=False,
    )

    add_bold_suptitle(
        fig,
        "Overall Average-Time Comparison",
        y=0.995,
        fontsize=15
    )

    plt.tight_layout(rect=[0, 0, 0.88, 0.94])
    out = OUTPUT_DIR / "hybrid_vs_baselines.png"
    savefig_clean(fig, out)

# =========================================================
# MAIN
# =========================================================
def main():
    df = parse_overview_csv(INPUT_CSV)

    csv_path = OUTPUT_DIR / "overview_parsed_long.csv"
    df.to_csv(csv_path, index=False)
    print(f"Saved: {csv_path}")

    plot_compact_overview(df)
    plot_individual_overview(df)
    plot_hybrid_vs_baselines(df)

    print("\nAll overview figures generated successfully.")


if __name__ == "__main__":
    main()