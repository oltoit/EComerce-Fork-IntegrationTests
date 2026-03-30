#!/usr/bin/env python3

import pandas as pd
import numpy as np
import re
import sys
from pathlib import Path
from collections import defaultdict

def parse_psrecord_file(filepath):
    """Parses a psrecord txt-file"""
    return pd.read_csv(
        filepath,
        sep=r'\s+',
        comment='#',
        header=None,
        names=['Elapsed time', 'CPU (%)', 'Real (MB)', 'Virtual (MB)']
    )

def find_file_groups(folder: Path):
    """Groups files according to their prefix"""
    groups = defaultdict(list)
    pattern = re.compile(r'^(.+?)(\d+)\.txt$')

    for f in sorted(folder.glob('*.txt')):
        match = pattern.match(f.name)
        if match:
            prefix = match.group(1)
            groups[prefix].append(f)

    return groups

# TODO: consider if using Median here is the correct thing to do -> if not change strategy
# FIXME: probably not the right tool -> a lot of variation
def aggregate_group(files: list[Path]) -> pd.DataFrame:
    """Creates the Median per data-point over the different iterations"""
    dataframes = []
    min_len = None
    for f in files:
        try:
            df = parse_psrecord_file(f)
            dataframes.append(df)
            min_len = len(df) if min_len is None else min(min_len, len(df))
        except Exception as e:
            print(f"Error: {f.name} could not be loaded: {e}")

    trimmed = [df.iloc[:min_len].reset_index(drop=True) for df in dataframes]
    stacked = np.stack([df.values for df in trimmed])
    median_values = np.median(stacked, axis=0)

    return pd.DataFrame(median_values, columns=trimmed[0].columns)

def main():
    if len(sys.argv) != 2:
        print("Please use 2 arguments")
        sys.exit(1)

    folder = Path(sys.argv[1])
    output_folder = folder / "aggregated/"
    output_folder.mkdir(parents=True, exist_ok=True)
    if not folder.is_dir():
        print(f"Error: '{folder}' is not a valid folder.")
        sys.exit(1)

    groups = find_file_groups(folder)
    print("Started aggregation...")
    for prefix, files in groups.items():
        result = aggregate_group(files)

        if result is not None:
            output_path = output_folder / f"{prefix}aggregated.csv"
            result.to_csv(output_path, index=False)

    print("...Finished aggregation!")

if __name__ == "__main__":
    main()