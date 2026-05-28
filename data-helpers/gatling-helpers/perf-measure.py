#!/usr/bin/env python3

import argparse
import csv
import signal
import subprocess
import time
from datetime import datetime

# Written by ChatGPT 5.5

running = True


def handle_shutdown(signum, frame):
    global running
    running = False


signal.signal(signal.SIGINT, handle_shutdown)
signal.signal(signal.SIGTERM, handle_shutdown)


def main():
    global running

    parser = argparse.ArgumentParser()
    parser.add_argument("pid", type=int)
    parser.add_argument("--output", "-o", default="resource_usage.csv")

    args = parser.parse_args()

    pid = args.pid

    with open(args.output, "w", newline="") as csv_file:

        writer = csv.writer(csv_file)

        writer.writerow([
            "timestamp",
            "cpu_percent",
            "real_ram_mb"
        ])

        csv_file.flush()

        print("READY", flush=True)

        while running:

            result = subprocess.run(
                [
                    "ps",
                    "-p", str(pid),
                    "-o", "%cpu=,rss="
                ],
                capture_output=True,
                text=True
            )

            line = result.stdout.strip()

            if line:
                try:
                    cpu_str, rss_str = line.split()

                    cpu = float(cpu_str)

                    # RSS kommt in KB
                    ram_mb = float(rss_str) / 1024

                    timestamp = datetime.now().strftime(
                        "%Y-%m-%d %H:%M:%S.%f"
                    )[:-3]

                    writer.writerow([
                        timestamp,
                        f"{cpu:.3f}",
                        f"{ram_mb:.3f}"
                    ])

                    csv_file.flush()

                except Exception:
                    pass

            time.sleep(0.1)

if __name__ == "__main__":
    main()