## Evaluating Model Serving Strategies over Streaming Data

This repository contains the implementation and experiments configuration to compare different model serving alternatives over streaming data. Our repository is organized as it follows: the _implementation/_ diretcory contains the code for all the benchmarked pipelines, alonside the scripts to run the experiments and the corresponding evaluation configurations. The _models/_ directory contains: (1) the scripts used to train the TensorFlow and PyTorch models (under _models/training/_), and (2) the pre-trained feed-forward models (under _models/feedforward/_).

### Prerequisites

1. Unix-like environment
2. Maven
3. Java 8
4. Docker installation

### How to run the experiments
All the different benchmarks can be executed using their corresponding _run_benchmark.sh_ shell script. The script will first run the experiments (with their corresponding configurations), write the results to disk, and then compute the quality metrics per experiment type. The results will be printed to the console.

#### Model serving natively embedded into the stream processor
The code for the embedded inference approach is contained inside _implementation/flink_. The _run_benchmark.sh_ shell script is used to experiment with all the embedded serving libraries (i.e., ND4J, ONNX, and TensorFlow SavedBundle). Simply running the _run_benchmark.sh_ shell script will print all the computed quality metrics to the console. Thus, one only needs to execute _implementation/flink/run_benchmark.sh_ once to obtain the ND4J, ONNX, and TensorFlow SavedBundle results.

#### Model serving via external services
We test two different model serving systems: Tensorflow Serving, and TorchServe. The code and experiments configurations are located at  _implementation/tensorflowserve_ and _implementation/torchserve_ respectively. The _run_benchmark.sh_ shell script will run the evaluation for each approach. Please note that the corresponding shell scripts must be executed manually for each approach: execute _implementation/tensorflowserve/run_benchmark.sh_ to gather the TensorFlow Serving results and _implementation/torchserve/run_benchmark.sh_ for the TorchServe tests.

### Configurations
The experiment configuration files are located at _implementation/{flink | tensorflowserve | torchserve}/expconfigs/_ and can be adjusted to run the experiments with different configurations.
