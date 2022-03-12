package inference.nd4j;

import inference.commons.GenericModel;
import inference.commons.ScoringFunction;
import inference.nd4j.models.FeedForwardND4JModel;
import inference.nd4j.utils.INDArrayConversions;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.ArrayList;

public class FeedForwardND4JScoring
        extends ScoringFunction<ArrayList<ArrayList<Float>>, ArrayList<ArrayList<Float>>, FeedForwardND4JModel> {
    //private static final int OUT_SIZE = 10;

    @Override
    public void load(String modelPath) throws Exception {
        this.model = new FeedForwardND4JModel();
        this.model.loadModel(modelPath);
    }

    @Override
    public ArrayList<ArrayList<Float>> processElement(ArrayList<ArrayList<Float>> inputBatches) throws Exception {
        int inputSize = inputBatches.get(0).size();
        int batchSize = inputBatches.size();
        ArrayList<Float> in = new ArrayList<>();
        for (ArrayList<Float> batch : inputBatches) {
            in.addAll(batch);
        }
        INDArray inputINDArray = INDArrayConversions.convertArrayListToINDArray(in);
        inputINDArray = inputINDArray.reshape(new int[]{inputSize, batchSize});
        INDArray outputINDArray = this.model.apply(inputINDArray);
        //outputINDArray = outputINDArray.reshape(new int[]{outSize, batchSize});
        ArrayList<ArrayList<Float>> assignment = INDArrayConversions
                .convertINDArrayBatchToArrayList(outputINDArray);
        return assignment;
    }

    @Override
    public void updateModel(GenericModel newModel) {
        System.out.println("ND4JScoring got new model " + newModel);
        this.model = (FeedForwardND4JModel) newModel;
    }
}
