#!/usr/bin/env python3

import pandas as pd
import matplotlib.pyplot as plt
from pathlib import Path
import sys

from matplotlib.ticker import FormatStrFormatter


def trim_trailing_zeros(df: pd.DataFrame, column: str, padding: int = 5) -> pd.DataFrame:
    """Trims the leading and trailing zeroes from data, leaves 5 zeroes as padding"""
    nonzero_indices = df[df[column] > 0].index

    if nonzero_indices.empty:
        return df

    first_nonzero = nonzero_indices.min()
    last_nonzero = nonzero_indices.max()

    start = max(0, first_nonzero - padding)
    end = min(len(df), last_nonzero + padding + 1)

    trimmed = df.iloc[start:end].copy()
    trimmed["Elapsed time"] = trimmed["Elapsed time"] - trimmed["Elapsed time"].iloc[0]

    return trimmed


def plot_cpu(df: pd.DataFrame, output_dir: Path, name: str):
    df = trim_trailing_zeros(df, "CPU (%)")
    titleName = name.split("-")[0]

    fig, ax = plt.subplots(figsize=(10, 5))
    ax.plot(df["Elapsed time"], df["CPU (%)"], color="steelblue")
    ax.set_xlabel("Elapsed time (s)")
    ax.set_ylabel("CPU (%)")
    ax.set_title(f"{titleName} – CPU Usage")
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(output_dir / "cpu.svg")
    plt.close()


def plot_ram(df: pd.DataFrame, output_dir: Path, name: str):
    df = trim_trailing_zeros(df, "Real (MB)")
    titleName = name.split("-")[0]

    fig, ax = plt.subplots(figsize=(10, 5))
    ax.plot(df["Elapsed time"], df["Real (MB)"], color="tomato")
    ax.set_xlabel("Elapsed time (s)")
    ax.set_ylabel("Real RAM (MB)")
    ax.set_title(f"{titleName} – RAM Usage")
    ax.grid(True, alpha=0.3)
    ax.yaxis.set_major_formatter(FormatStrFormatter('%.1f'))
    plt.tight_layout()
    plt.savefig(output_dir / "ram.svg")
    plt.close()

def plot_mixed(df: pd.DataFrame, output_dir: Path, name: str):
    df = trim_trailing_zeros(df, "CPU (%)")
    titleName = name.split("-")[0]

    fig, ax_cpu = plt.subplots(figsize=(10, 5))

    cpu_max = df["CPU (%)"].max()
    ax_cpu.plot(df["Elapsed time"], df["CPU (%)"], color="steelblue", label="CPU (%)")
    ax_cpu.set_xlabel("Elapsed time (s)")
    ax_cpu.set_ylabel("CPU (%)", color="steelblue")
    ax_cpu.tick_params(axis="y", labelcolor="steelblue")
    ax_cpu.set_ylim(bottom=0, top=cpu_max * 1.2)

    ram_max = df["Real (MB)"].max()
    ax_ram = ax_cpu.twinx()
    ax_ram.plot(df["Elapsed time"], df["Real (MB)"], color="tomato", label="Real RAM (MB)")
    ax_ram.set_ylabel("Real RAM (MB)", color="tomato")
    ax_ram.tick_params(axis="y", labelcolor="tomato")
    ax_ram.set_ylim(bottom=0, top=ram_max * 1.2)

    lines_cpu, labels_cpu = ax_cpu.get_legend_handles_labels()
    lines_ram, labels_ram = ax_ram.get_legend_handles_labels()
    ax_cpu.legend(lines_cpu + lines_ram, labels_cpu + labels_ram, loc="upper left")

    ax_cpu.set_title(f"{titleName} – CPU & RAM Usage")
    ax_cpu.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(output_dir / "cpu_and_ram.svg")
    plt.close()


def main():
    if len(sys.argv) != 2:
        print("Please use 2 arguments")
        sys.exit(1)

    folder = Path(sys.argv[1])
    if not folder.is_dir():
        print(f"Error: '{folder}' is not a valid folder.")
        sys.exit(1)

    plots_root = folder / "plots"
    plots_root.mkdir(exist_ok=True)

    print("Started plotting...")
    for csv_file in list(folder.glob("*.csv")):
        name = csv_file.stem
        df = pd.read_csv(csv_file)

        output_dir = plots_root / name
        output_dir.mkdir(exist_ok=True)

        plot_cpu(df, output_dir, name)
        plot_ram(df, output_dir, name)
        plot_mixed(df, output_dir, name)

    print("...Finished plotting!")


if __name__ == "__main__":
    main()