#!/usr/bin/env bash

mvn clean install

# run the experiments
mvn exec:java -Dexec.mainClass="experiments.feedforward.ExperimentsRunner" -Dexec.cleanupDaemonThreads=false
sleep 10

# merge produced csv files
for f in experiments-results-ffnn/*/; do
  for d in "$f"*; do
    #  iterate only over directories
    if [ -d "$d" ]; then
      base_name=$(basename ${d})
      cat "$d"/* >"$f"/"$base_name"-combined.csv
      sort -k2 -n -t, "$f"/"$base_name"-combined.csv >$f"/"$base_name"-sorted.csv"
      rm -r "$d"
      rm "$f"/*combined.csv
    fi
  done
done

# measurements
python3 ./compute_metrics.py
