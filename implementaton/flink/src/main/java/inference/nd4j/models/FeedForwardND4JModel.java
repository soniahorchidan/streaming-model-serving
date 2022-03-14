package inference.nd4j.models;

import org.nd4j.linalg.api.ndarray.INDArray;

public class FeedForwardND4JModel extends ND4JModel<INDArray, INDArray> {

    @Override
    public INDArray apply(INDArray input) throws Exception {
        INDArray intermediary = input;
        for (int i = 0; i < this.layers.length; i++) {
            INDArray output = this.layers[i].apply(intermediary);
            intermediary = output;
        }
        return intermediary;
    }
}
