package inference.onnx;

import inference.commons.ScoringFunction;

import java.util.ArrayList;

public class ONNXScoring extends ScoringFunction<ArrayList<ArrayList<Float>>, ArrayList<ArrayList<Float>>, ONNXModel> {

    @Override
    public void load(String modelPath) throws Exception {
        this.model = new ONNXModel();
        this.model.loadModel(modelPath);
        System.out.println("LOADED MODEL!");
    }

    @Override
    public ArrayList<ArrayList<Float>> processElement(ArrayList<ArrayList<Float>> batchedInput) throws Exception {
        ArrayList<ArrayList<Float>> batchedResult = new ArrayList<>();
        for (ArrayList<Float> input : batchedInput) {
            batchedResult.add(this.model.apply(input));
        }
        return batchedResult;
    }
}
