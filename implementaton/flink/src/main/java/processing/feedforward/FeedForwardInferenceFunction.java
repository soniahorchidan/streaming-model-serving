package processing.feedforward;

import config.ServingConfig;
import inference.commons.ScoringFunction;
import inference.nd4j.FeedForwardND4JScoring;
import inference.onnx.ONNXScoring;
import inference.tensorflow.TensorflowScoring;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;

public class FeedForwardInferenceFunction
        extends
        ProcessFunction<Tuple2<ArrayList<ArrayList<Float>>, Long>, Tuple3<ArrayList<ArrayList<Float>>, Long, Long>> {
    private final String initialModelPath;
    private final String modelType;
    private ScoringFunction scoringFunction;

    public FeedForwardInferenceFunction(ServingConfig servingConfig) {
        this.initialModelPath = (String) servingConfig.getParam("initial.model.path");

        this.modelType = (String) servingConfig.getParam("model.type");
        if (this.modelType.equals("nd4j")) {
            this.scoringFunction = new FeedForwardND4JScoring();
            this.scoringFunction.setModelType("feedforward");
        } else if (modelType.equals("onnx")) {
            this.scoringFunction = new ONNXScoring();
        } else if (modelType.equals("tensorflow")) {
            this.scoringFunction = new TensorflowScoring();
        } else
            System.err.println("Unsupported model type!");
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        this.scoringFunction.load(this.initialModelPath);
    }

    @Override
    public void processElement(Tuple2<ArrayList<ArrayList<Float>>, Long> inputBatches, Context context,
                               Collector<Tuple3<ArrayList<ArrayList<Float>>, Long, Long>> collector) throws Exception {
        ArrayList<ArrayList<Float>> result = null;
        if (this.modelType.equals("nd4j")) {
            result = ((FeedForwardND4JScoring) this.scoringFunction).processElement(inputBatches.f0);
        } else if (modelType.equals("onnx")) {
            result = ((ONNXScoring) this.scoringFunction).processElement(inputBatches.f0);
        } else if (modelType.equals("tensorflow")) {
            result = ((TensorflowScoring) this.scoringFunction).processElement(inputBatches.f0);
        }
        collector.collect(new Tuple3<>(result, inputBatches.f1, System.nanoTime()));

    }
}
