package inference.nd4j.layers;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.Serializable;

public abstract class Layer<TYPE> implements Serializable {
    private final int layerNum;

    protected Layer(int layerNum) {
        this.layerNum = layerNum;
    }

    public int getLayerNum() {
        return this.layerNum;
    }

    public abstract INDArray apply(INDArray input) throws Exception;

    public abstract void set(TYPE content);
}
