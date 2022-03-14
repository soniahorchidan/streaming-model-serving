package experiments.feedforward;

import config.ServingConfig;
import config.defaults.ServingDefaultOptions;
import inference.onnx.ONNXModel;
import inference.tensorflow.TensorflowModel;
import input.data.feedforward.ImagesDataGeneratorSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import processing.feedforward.FeedForwardInferenceFunction;

import java.util.ArrayList;

public class FeedForwardPipeline {
    private static final ServingConfig SERVING_CONFIG = new ServingConfig("/onnx-ff-serving-config.yaml",
                                                                          ServingDefaultOptions.configs);

    public static void run(int inputRate, int batchSize, int experimentTimeInSeconds,
                           int modelReplicas, int warmUpRequestsNum, int maxInputRatePerThread,
                           String outputFile) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setString("taskmanager.memory.process.size", "32GB");
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(configuration);
        env.getConfig().registerKryoType(ONNXModel.class);
        env.getConfig().registerKryoType(TensorflowModel.class);

        // ========================================== Input Reading ==========================================
        // Read the input data stream
        int inputPotentialProducers = (int) Math.ceil((double) inputRate / maxInputRatePerThread);
        int inputProducers = inputPotentialProducers > 0 ? inputPotentialProducers : 1;
        int inputRatePerProducer = Math.min(inputRate, inputRate / inputProducers);
        System.out.println("Input producers: " + inputProducers);
        System.out.println("Input rate per producer: " + inputRatePerProducer);

        DataStream<Tuple2<ArrayList<ArrayList<Float>>, Long>> batches = env
                .addSource(
                        new ImagesDataGeneratorSource(batchSize, experimentTimeInSeconds, warmUpRequestsNum,
                                                      inputRatePerProducer))
                .setParallelism(inputProducers);

        // ========================================== Model Scoring ==========================================
        // <[scoring-result], in_timestamp, out_timestamp>
        SingleOutputStreamOperator<Tuple3<ArrayList<ArrayList<Float>>, Long, Long>> scoring = batches
                .process(new FeedForwardInferenceFunction(SERVING_CONFIG))
                .setParallelism(modelReplicas);

        // ========================================== Benchmarking ==========================================
        // Record timestamp when the scoring is done
        scoring.map(new MapFunction<Tuple3<ArrayList<ArrayList<Float>>, Long, Long>, Tuple2<Long, Long>>() {
            @Override
            public Tuple2<Long, Long> map(
                    Tuple3<ArrayList<ArrayList<Float>>, Long, Long> record) {
                return new Tuple2<>(record.f1, record.f2);
            }
        }).setParallelism(inputProducers).writeAsCsv(outputFile).setParallelism(inputProducers);

        System.out.println(env.getExecutionPlan());
        env.execute();
    }
}
