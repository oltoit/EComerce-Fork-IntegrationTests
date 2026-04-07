#!/usr/bin/env python3

import re
import sys
import json
import pathlib
import pyjson5

def find_stats_js(folder: pathlib.Path) -> pathlib.Path:
    matches = list(folder.glob("*/js/stats.js"))
    return matches[0] if matches else None


def extract_stats(stats_js_path: str, output_path: str):
    content = pathlib.Path(stats_js_path).read_text(encoding="utf-8")
    match = re.search(r"var\s+stats\s*=\s*(\{.*?\})\s*(?=function|\Z)", content, re.DOTALL)
    if not match:
        raise ValueError("Keine 'stats'-Variable in der Datei gefunden")

    stats = pyjson5.loads(match.group(1))

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(stats, f, indent=2, ensure_ascii=False)
    return stats


# flat list -> can be used to create csv if needed
def flatten_stats(stats: dict) -> list[dict]:
    rows = []
    def walk(node: dict):
        if node.get("type") == "REQUEST":
            s = node["stats"]
            rows.append({
                "name":             node["name"],
                "requests_total":   s["numberOfRequests"]["total"],
                "requests_ok":      s["numberOfRequests"]["ok"],
                "requests_ko":      s["numberOfRequests"]["ko"],
                "min_ms":           s["minResponseTime"]["total"],
                "max_ms":           s["maxResponseTime"]["total"],
                "mean_ms":          s["meanResponseTime"]["total"],
                "p50_ms":           s["percentiles1"]["total"],
                "p75_ms":           s["percentiles2"]["total"],
                "p95_ms":           s["percentiles3"]["total"],
                "p99_ms":           s["percentiles4"]["total"],
            })
        for child in node.get("contents", {}).values():
            walk(child)

    walk(stats)
    return rows


def main():
    if len(sys.argv) != 2:
        print("Please use 2 arguments")
        sys.exit(1)

    folder = pathlib.Path(sys.argv[1])

    folders = [d for d in folder.iterdir() if d.is_dir()]
    for f in folders:
        stats_location = find_stats_js(f)
        extract_stats(stats_location, folder / (f.name +  ".json"))


if __name__ == "__main__":
    main()