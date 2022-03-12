import os
import re

import pandas as pd


def list_dirs(dir):
    r = []
    for root, dirs, files in os.walk(dir):
        for name in dirs:
            r.append(os.path.join(root, name))
    return r

# TODO(sonia): pass it as command line argument so we can run it on the graph model as well
CURRENT_DIR = "./experiments-results"
# directory = os.fsencode(CURRENT_DIR)

with open("src/main/java/experiments/feedforward/configs/common.properties") as f:
    l = [line.split("=") for line in f.readlines()]
    configs = {key.strip(): value.strip() for key, value in l}
    duration = int(configs['experiment_time_in_seconds'])
    warm_up_requests_num = int(configs['warmup_requests_num'])

print("EXPERIMENT DURATION: " + str(duration))
print("WARMUP QUERIES: " + str(warm_up_requests_num))

for directory in list_dirs(CURRENT_DIR):
    experiment_num = 0
    experiment_results = {}
    print("\n\n==================== RESULTS FOR " + os.path.basename(directory) + " EXPERIMENT ======================")
    for file in os.listdir(directory):
        # print("------------ EXPERIMENT " + str(experiment_num) + " ------------")
        experiment_num += 1
        filename = os.fsdecode(file)
        # print("FILE NAME: " + filename)
        file_path = os.path.join(directory, filename)
        times = []
        with open(file_path) as f_in:
            for line in f_in:
                times.append(list(map(int, line.rstrip('\n').split(","))))

        # strip warm-up queries
        times = times[warm_up_requests_num:]

        full_experiment_name = "2022" + file_path.split("2022", 1)[1]
        configs = re.findall("\d+", full_experiment_name)
        input_rate = int(configs[4])
        batch_size = int(configs[5])
        model_replicas = int(configs[6])
        exp_footprint = str(input_rate) + "_" + str(batch_size) + "_" + str(model_replicas)
        processed_requests = len(times)

        # print("INPUT RATE: " + str(input_rate))
        # print("BATCH SIZE: " + str(batch_size))
        # print("MODEL REPLICAS: " + str(model_replicas))
        # print("TOTAL PROCESSED REQUESTS: " + str(processed_requests) + "\n")

        # Compute average throughput
        times.sort(key=lambda x: x[1])
        total_time_nanoseconds = times[-1][1] - times[0][1]
        total_time_seconds = total_time_nanoseconds / 1000000000
        avg_throughput = processed_requests / total_time_seconds
        experiment_results.setdefault(exp_footprint, {})
        experiment_results[exp_footprint].setdefault("avg_throughput", []).append(avg_throughput)
        # print("Average throughput (scorings/sec): " + str(avg_throughput))

        # Compute average latency
        latencies = []
        for t in times:
            # convert nanoseconds to milliseconds
            latencies.append((t[-1] - t[0]) / 1000000)

        series = pd.Series(latencies)
        latency_res = series.mean()
        experiment_results[exp_footprint].setdefault("avg_latency", []).append(latency_res)
        # print("Latency per batch (ms)")
        # print(series.describe(percentiles=[0.5, 0.9, 0.95, 0.99]))
        # print("\n\n")

    for exp_footprint in experiment_results:
        configs = exp_footprint.split("_")
        print("\nINPUT RATE: " + str(configs[0]))
        print("BATCH SIZE: " + str(configs[1]))
        print("MODEL REPLICAS: " + str(configs[2]))
        for metric in experiment_results[exp_footprint]:
            t = sum(experiment_results[exp_footprint][metric]) / len(experiment_results[exp_footprint][metric])
            print(metric + ": " + str(t))
