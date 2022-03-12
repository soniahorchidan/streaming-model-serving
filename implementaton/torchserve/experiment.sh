#!/usr/bin/env bash

<<readme
  INSTRUCTIONS:
    This script is used to automate the execution of experiments. It has the following command line arguments:
     -tmin_workers = minimum number of workers that will be used by PyTorch Serve for inference; defaults to 1 if not set
     -tmax_workers = maximum number of workers that will be used by PyTorch Serve for inference; defaults to 1 if not set
     -archive = archive the model (will delete the previous archive if already existed); defaults to false
     -is_scalability_experiment = defaults to false

  EXECUTION EXAMPLE: ./experiment.sh -tmin_workers 2 -tmax_workers 2 -archive false -isScalabilityExperiment true

  DISCLAIMER: this script is used for experimenting on the Twitch dataset currently.
readme

# Parse command line arguments
while echo $1 | grep -q ^-; do
  eval $(echo $1 | sed 's/^-//')=$2
  shift
  shift
done

TORCH_MIN_WORKERS=${tmin_workers:-1}
TORCH_MAX_WORKERS=${tmax_workers:-1}
ARCHIVE=${archive:-false}
IS_SCALABILITY=${is_scalability_experiment:-false}

# Archive twitch model
mkdir -p src/model_store
if [[ $ARCHIVE == true ]]; then
  ARCHIVED_TWITCH_MODEL_PATH=src/model_store/twitch-unsup-6-partitions.mar
  if test -f "$ARCHIVED_TWITCH_MODEL_PATH"; then
    echo "$ARCHIVED_TWITCH_MODEL_PATH already exists! Deleting existing model...."
    rm $ARCHIVED_TWITCH_MODEL_PATH
  fi
  echo "Archiving model..."
  torch-model-archiver --model-name ffnn \
    --version 1.0 \
    --serialized-file ../../models/feedforward/torch/ffnn.torch \
    --export-path model_store \
    --handler src/torch_serve_handler/feedforward/model_handler.py \
    --extra-files src/torch_serve_handler/feedforward/model.py
fi
sleep 5

# Start PyTorch Model Serving
echo "========================================================="
echo "Starting PyTorch model serving..."
torchserve --start --ncs --model-store model_store/ --ts-config src/torch_serve_handler/config.properties >logs.txt
sleep 5

# Scale PyTorch Model Serving workers
echo "========================================================="
echo "Scaling the number of workers used for serving..."
curl -v -X PUT "http://localhost:8081/models/ffnn?min_worker=$TORCH_MIN_WORKERS&max_worker=$TORCH_MAX_WORKERS"
sleep 5

# Check model serving configuration
echo "========================================================="
echo "Updated model serving configuration:"
curl "localhost:8081/models/ffnn"
sleep 5

## Start inference
echo "========================================================="
echo "Starting inference..."
mvn exec:java -Dexec.mainClass="ExperimentsRunner" -Dexec.cleanupDaemonThreads=false -Dexec.args="$IS_SCALABILITY $TORCH_MAX_WORKERS"
echo "Inference ended!"
sleep 5

## Stop PytorchServe server
torchserve --stop
