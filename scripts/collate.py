# Joins multiple CSV outputs from one AM run into single CSV documents: gangs,
# analogical sets and distributions.
# Output formatting of AnalogicalModelingOutput must be set to CSV

from io import StringIO
from pathlib import Path
import re

import pandas as pd


ITEM_INDEX = "classified_item_index"
ITEM_CLASS = "classified_item_class"
ITEM_REPRESENTATION = "classified_item_repr"


def clean_smartquotes(df):
    """
    Cleans smart quotes from the DataFrame column names.
    Not sure why Weka is adding smart quotes 🤔 but we'll just remove them
    """
    df.rename(columns=lambda x: x.replace("“", "").replace("”", ""), inplace=True)
    return df


class AnalogicalModelingResult:
    def __init__(
        self,
        item_index: int,
        item_class: str,
        item_csv: str,
        distribution_csv: str,
        analogical_set_csv: str,
        gang_csv: str,
    ):

        self.classified_item_df = pd.read_csv(StringIO(item_csv))
        clean_smartquotes(self.classified_item_df)
        self.classified_item_df.columns = [
            "classified_item_" + col for col in self.classified_item_df.columns
        ]
        assert (
            len(self.classified_item_df) == 1
        ), f"classified item CSV should have exactly one row (has {len(self.classified_item_df)})"

        self.distribution_df = pd.read_csv(StringIO(distribution_csv))
        clean_smartquotes(self.distribution_df)
        self.distribution_df[ITEM_INDEX] = item_index

        self.analogical_set_df = pd.read_csv(StringIO(analogical_set_csv))
        clean_smartquotes(self.analogical_set_df)
        self.analogical_set_df[ITEM_INDEX] = item_index
        self.analogical_set_df[ITEM_CLASS] = item_class

        self.gang_df = pd.read_csv(StringIO(gang_csv))
        clean_smartquotes(self.gang_df)
        self.gang_df[ITEM_INDEX] = item_index
        self.gang_df[ITEM_CLASS] = item_class


# Classifying instance 0 (he - X - - X - - e - P FirstPersonSing, class: F)
instance_line_pattern = r"Classifying instance (\d+) \(class: (.+)\)$"


def _parse_instance_line(match):
    item_index = int(match.group(1))
    item_class = match.group(2)
    return {"item_index": item_index, "item_class": item_class}


REPORT_LINE_PROGRESS_INTERVAL = 10_000


class LineParser:
    """
    A utility class to parse lines from the WEKA output file.
    """

    def __init__(self):
        self.current_result = {}
        self.in_classifying_item_csv = False
        self.in_distribution_csv = False
        self.in_analogical_set_csv = False
        self.in_gangs_csv = False
        self.line_num = 0

    def parse_line(self, line) -> AnalogicalModelingResult | None:
        """
        Parses a single line from the WEKA output file. Returns the completed result if any, otherwise None.
        """
        self.line_num += 1
        if self.line_num % REPORT_LINE_PROGRESS_INTERVAL == 0:
            print(f"Processing line {self.line_num}...")
        line = line.strip()
        if not line:
            self.in_distribution_csv = False
            self.in_analogical_set_csv = False
            self.in_gangs_csv = False
            self.in_classifying_item_csv = False
            return
        if self.in_distribution_csv:
            self.current_result["distribution_csv"] += line + "\n"
            return
        if self.in_analogical_set_csv:
            self.current_result["analogical_set_csv"] += line + "\n"
            return
        if self.in_gangs_csv:
            self.current_result["gang_csv"] += line + "\n"
            return
        if self.in_classifying_item_csv:
            self.current_result["item_csv"] += line + "\n"
            return
        match = re.match(instance_line_pattern, line)
        if match:
            ret_val = None
            if self.current_result:
                # Save the previous result before starting a new one
                ret_val = AnalogicalModelingResult(
                    **self.current_result,
                )
            self.current_result = _parse_instance_line(match)
            self.current_result["item_csv"] = ""
            self.in_classifying_item_csv = True
            return ret_val
        if line.startswith("Judgement,Expected,"):
            # Start of the distribution CSV
            self.in_distribution_csv = True
            self.current_result["distribution_csv"] = line + "\n"
            return
        if line.startswith("Analogical set:"):
            # Next line starts the analogical set CSV
            self.in_analogical_set_csv = True
            self.current_result["analogical_set_csv"] = ""
            return
        if line.startswith("Gang effects:"):
            # Next line starts the gangs CSV
            self.in_gangs_csv = True
            self.current_result["gang_csv"] = ""
            return
        # Save the very last result found when the file end is reached
        if line.startswith("End Analogical Modeling Results"):
            return AnalogicalModelingResult(
                **self.current_result,
            )


REPORT_RESULT_PROGRESS_INTERVAL = 100


def collate(weka_output_path, new_output_prefix):
    """
    Collates the output from a WEKA run into three separate CSV files:
    - gangs.csv
    - analogical_sets.csv
    - distributions.csv

    :param weka_output_path: Path to the WEKA output file.
    :param new_output_prefix: Prefix for the output files.
    """

    distributions_csv_path = Path(weka_output_path).parent / (
        new_output_prefix + "_distributions.csv"
    )
    analogical_sets_csv_path = Path(weka_output_path).parent / (
        new_output_prefix + "_analogical_sets.csv"
    )
    gangs_csv_path = Path(weka_output_path).parent / (new_output_prefix + "_gangs.csv")

    for path in [distributions_csv_path, analogical_sets_csv_path, gangs_csv_path]:
        if path.exists():
            print(
                f"{path} already exists. Please delete it before running this script."
            )
            return

    results = []
    parser = LineParser()
    with open(weka_output_path, "r") as weka_output_file:
        for line in weka_output_file:
            result = parser.parse_line(line)
            if result:
                # If we have a result, append it to the results list
                results.append(result)
                if len(results) % REPORT_RESULT_PROGRESS_INTERVAL == 0:
                    print(f"Found {len(results)} results so far...")

    print(
        f"Found {len(results)} results in the WEKA output file. Concatenating CSV docs..."
    )

    distribution_df = pd.concat(
        [result.distribution_df for result in results], ignore_index=True
    )
    # copy the classified item columns to the analogical set and gang dataframes, which
    # have many rows per classification execution
    analogical_set_df = pd.concat(
        [
            pd.concat(
                [
                    result.analogical_set_df,
                    pd.concat(
                        [result.classified_item_df] * len(result.analogical_set_df),
                        ignore_index=True,
                    ),
                ],
                axis=1,
            )
            for result in results
        ],
        ignore_index=True,
    )
    gang_df = pd.concat(
        [
            pd.concat(
                [
                    result.gang_df,
                    pd.concat(
                        [result.classified_item_df] * len(result.gang_df),
                        ignore_index=True,
                    ),
                ],
                axis=1,
            )
            for result in results
        ],
        ignore_index=True,
    )

    print("Saving results to CSV files...")
    distribution_df.to_csv(distributions_csv_path, index=False)
    analogical_set_df.to_csv(analogical_sets_csv_path, index=False)
    gang_df.to_csv(gangs_csv_path, index=False)

    print(f"Saved {len(results)} results to CSV files.")


if __name__ == "__main__":
    import sys

    if len(sys.argv) != 3:
        print("Found incorrect number of input arguments.")
        print("Usage: python collate.py <path_to_weka_output> <new_output_prefix>")
        print("Example: python collate.py /path/to/weka_output.txt my-am-results")
        print(
            "The above command would read weka_output.txt and write to my-am-results_gangs.csv, my-am-results_analogical_sets.csv, and my-am-results_distributions.csv"
        )
        print("Exiting...")
        sys.exit(1)

    weka_output_path = sys.argv[1]
    new_output_prefix = sys.argv[2]
    collate(weka_output_path, new_output_prefix)
