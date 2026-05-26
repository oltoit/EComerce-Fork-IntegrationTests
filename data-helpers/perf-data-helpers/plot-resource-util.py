import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import numpy as np
import sys
import os
import glob

# Written by Claude Sonnet 4.6
# ── Hilfsfunktionen ──────────────────────────────────────────────────────────

def choose_step_minutes(total_minutes):
    """Sinnvollen Tick-Abstand in Minuten wählen."""
    if   total_minutes <=  1:    return  1/60   # alle 1 s  (als Bruchteil Min.)
    elif total_minutes <=  2:    return  5/60   # alle 5 s
    elif total_minutes <=  5:    return 10/60   # alle 10 s
    elif total_minutes <= 15:    return  0.5    # alle 30 s
    elif total_minutes <= 60:    return  1.0    # alle 1 min
    elif total_minutes <= 300:   return  5.0    # alle 5 min
    elif total_minutes <= 1440:  return 15.0    # alle 15 min
    else:                        return 60.0    # alle 60 min

def fmt_minutes(val, _):
    total_seconds = val * 60
    m = int(total_seconds // 60)
    s = int(round(total_seconds % 60))
    return f"{m}:{s:02d}"

def plot_csv(csv_path):
    # ── Daten laden ──────────────────────────────────────────────────────────
    df = pd.read_csv(csv_path, parse_dates=["timestamp"])
    df = df.sort_values("timestamp").reset_index(drop=True)

    t0 = df["timestamp"].iloc[0]
    df["minutes"] = (df["timestamp"] - t0).dt.total_seconds() / 60.0
    total_minutes = df["minutes"].iloc[-1]

    step = choose_step_minutes(total_minutes)

    # ── Figure ───────────────────────────────────────────────────────────────
    fig, ax1 = plt.subplots(figsize=(14, 5))
    fig.patch.set_facecolor("white")
    ax1.set_facecolor("white")

    # ── CPU (linke Achse) ────────────────────────────────────────────────────
    color_cpu = "#c0392b"
    ax1.plot(df["minutes"], df["cpu_percent"],
             color=color_cpu, linewidth=0.9, alpha=0.85, label="CPU %")
    ax1.fill_between(df["minutes"], df["cpu_percent"],
                     alpha=0.12, color=color_cpu)
    ax1.set_xlabel("Zeit (mm:ss)", color="black", fontsize=11)
    ax1.set_ylabel("CPU-Auslastung (%)", color=color_cpu, fontsize=11)
    ax1.tick_params(axis="y", colors=color_cpu)
    ax1.tick_params(axis="x", colors="black")
    cpu_max = df["cpu_percent"].max()
    ax1.set_ylim(0, max(cpu_max * 1.15, 30))

    # ── RAM (rechte Achse) ───────────────────────────────────────────────────
    ax2 = ax1.twinx()
    color_ram = "#1a7a6e"
    ax2.plot(df["minutes"], df["real_ram_mb"],
             color=color_ram, linewidth=1.5, alpha=0.95,
             linestyle="--", label="RAM (MB)")
    ax2.fill_between(df["minutes"], df["real_ram_mb"],
                     alpha=0.08, color=color_ram)
    ax2.set_ylabel("RAM-Nutzung (MB)", color=color_ram, fontsize=11)
    ax2.tick_params(axis="y", colors=color_ram)
    ram_min = 0
    ram_max = df["real_ram_mb"].max()
    ax2.set_ylim(ram_min, ram_max * 1.15)

    # ── X-Achse ──────────────────────────────────────────────────────────────
    ax1.set_xlim(0, total_minutes)
    ticks = np.arange(0, total_minutes + step, step)
    ax1.set_xticks(ticks)
    ax1.xaxis.set_major_formatter(ticker.FuncFormatter(fmt_minutes))

    # ── Gitter & Rahmen ──────────────────────────────────────────────────────
    ax1.grid(True, linestyle="--", alpha=0.35, color="#aaaaaa")
    for spine in ax1.spines.values():
        spine.set_edgecolor("#999999")
    for spine in ax2.spines.values():
        spine.set_edgecolor("#999999")

    # ── Legende ──────────────────────────────────────────────────────────────
    lines1, labels1 = ax1.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(lines1 + lines2, labels1 + labels2,
               loc="upper right", facecolor="white",
               edgecolor="#aaaaaa", labelcolor="black", fontsize=10)

    # ── Titel ────────────────────────────────────────────────────────────────
    start_str = t0.strftime("%Y-%m-%d %H:%M:%S")
    csv_name  = os.path.basename(csv_path)
    plt.title(f"CPU & RAM Verlauf  –  {csv_name}  (Start: {start_str})",
              color="black", fontsize=12, pad=12)

    fig.tight_layout()

    # ── Speichern als PDF in plots/-Unterordner neben der CSV ────────────────
    csv_dir   = os.path.dirname(os.path.abspath(csv_path))
    plots_dir = os.path.join(csv_dir, "plots")
    os.makedirs(plots_dir, exist_ok=True)

    base_name = os.path.splitext(os.path.basename(csv_path))[0]
    out_path  = os.path.join(plots_dir, base_name + "_plot.pdf")

    fig.savefig(out_path, format="pdf", bbox_inches="tight",
                facecolor="white")
    plt.close(fig)
    print(f"  ✓  {out_path}")
    return out_path


# ── Einstiegspunkt ────────────────────────────────────────────────────────────

def collect_csv_files(arg):
    """Einzelne Datei oder alle CSVs in einem Ordner."""
    if os.path.isdir(arg):
        files = sorted(glob.glob(os.path.join(arg, "*.csv")))
        if not files:
            print(f"Keine CSV-Dateien gefunden in: {arg}")
        return files
    elif os.path.isfile(arg) and arg.lower().endswith(".csv"):
        return [arg]
    else:
        print(f"Ungültiges Argument: {arg}  (erwartet: .csv-Datei oder Ordner)")
        return []


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Verwendung:")
        print("  python plot_metrics.py <datei.csv>")
        print("  python plot_metrics.py <ordner/>")
        sys.exit(1)

    target = sys.argv[1]
    csv_files = collect_csv_files(target)

    if not csv_files:
        sys.exit(1)

    print(f"{len(csv_files)} CSV-Datei(en) gefunden – starte Verarbeitung …\n")
    for f in csv_files:
        print(f"  → {f}")
        try:
            plot_csv(f)
        except Exception as e:
            print(f"  ✗  Fehler bei {f}: {e}")

    print("\nFertig.")
