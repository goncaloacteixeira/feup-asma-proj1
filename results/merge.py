import datetime
from os import walk
import pandas as pd
import time
import argparse

ap = argparse.ArgumentParser()

ap.add_argument("-n", "--name", type=str, required=True, help="Output Name")

args = vars(ap.parse_args())

filenames = next(walk("."), (None, None, []))[2]  # [] if no file

signs = []

df = pd.DataFrame([], columns=["name", "path", "initiator", "original_cost", "final_cost", "shared_segments",
                               "car_service_fares", "num_shared_segments", "num_car_service_fares"])

for file in filenames:
    if file[:5] == "Human":
        df = pd.concat([df, pd.read_csv(file)], axis=0, join="inner")

df = df[df["final_cost"] != 0.0]

df.to_csv("results-" + args["name"] + ".csv", sep=",", index=False)
