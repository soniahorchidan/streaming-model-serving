package inference.tensorflow;

import inference.commons.ScoringFunction;

import java.util.ArrayList;

public class TensorflowScoring
        extends ScoringFunction<ArrayList<ArrayList<Float>>, ArrayList<ArrayList<Float>>, TensorflowModel> {

    @Override
    public void load(String modelPath) throws Exception {
        this.model = new TensorflowModel();
        this.model.loadModel(modelPath);
        System.out.println("LOADED MODEL!");
    }

    @Override
    public ArrayList<ArrayList<Float>> processElement(ArrayList<ArrayList<Float>> batchedInput) throws Exception {
        ArrayList<ArrayList<Float>> batchedResult = this.model.apply(batchedInput);
        return batchedResult;
    }
}
