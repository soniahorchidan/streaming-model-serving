#!/usr/bin/env bash

mvn clean install

# run close and open loop experiments
./experiment.sh -model_replicas 1
sleep 10

# run scalability experiments
declare -a model_replicas=(1 2 4 8 16)
for replicas in "${model_replicas[@]}"; do
  ./experiment.sh -model_replicas $replicas -isScalabilityExperiment true
done
sleep 10

# merge produced csv files
for f in experiments-results/*/; do
  for d in "$f"*; do
    #  iterate only over directories
    if [ -d "$d" ]; then
      base_name=$(basename ${d})
      cat "$d"/* >"$f"/"$base_name"-combined.csv
      sort -k1 -n -t, "$f"/"$base_name"-combined.csv >$f"/"$base_name"-sorted.csv"
      rm -r "$d"
      rm "$f"/*combined.csv
    fi
  done
done

# compute throughput and latency metrics
python3 ./compute_metrics.py
