#!/usr/bin/env bash

# Store inital path
CWD=$(pwd)

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    -model_replicas|--model_replicas)
      MODEL_REPLICAS="$2"
      shift # past argument
      shift # past value
      ;;
    -isScalabilityExperiment|--isScalabilityExperiment)
      IS_SCALABILITY="$2"
      shift # past argument
      shift # past value
      ;;
    -*|--*)
      echo "Unknown option $1"
      exit 1
      ;;
  esac
done

# Move to docker file path 
PTH='src/tensorflow_serve_handler/feedforward/docker/28x28'
cd $PTH

# Scale Tensorflow Model Serving workers
echo "========================================================="
echo "Scaling the number of parallel threads used for serving to '$MODEL_REPLICAS'..."
MAX_CPU="$(($MODEL_REPLICAS - 1))"
echo "Used CPU cores: 0-$MAX_CPU"
sed -i "s/cpuset: '[^']*'/cpuset: '0-$MAX_CPU'/g" docker-compose.yml


# Start Tensorflow Model Serving
echo "========================================================="
echo "Starting Tensorflow model serving..."
docker-compose up -d
sleep 10

# Move to initial path
cd $CWD

## Start inference
echo "========================================================="
echo "Starting inference..."
mvn exec:java -Dexec.mainClass="ExperimentsRunner" -Dexec.cleanupDaemonThreads=false -Dexec.args="$IS_SCALABILITY $MODEL_REPLICAS"
echo "Inference ended!"

# Stop Tensorflow Model Serving
echo "========================================================="
echo "Stopping Tensorflow model serving..."
docker-compose kill
sleep 10