package inference.nd4j.models;

import inference.commons.GenericModel;
import inference.nd4j.layers.ActivationLayer;
import inference.nd4j.layers.Layer;
import inference.nd4j.layers.WeightLayer;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.Serializable;

public abstract class ND4JModel<IN, OUT> extends GenericModel<IN, OUT> implements Serializable {
    protected Layer[] layers;

    @Override
    public void loadModel(String path) throws Exception {
        File dir = new File(path);
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".npy") && name.contains("-"));

        int numLayers = files.length;
        this.layers = new Layer[numLayers];

        for (File file : files) {
            String[] split = file.getName().split("-");
            if (split.length < 2)
                throw new Exception("Invalid file format!");
            int layerNum = Integer.parseInt(split[0]);
            String layerType = split[1].toUpperCase();
            Layer layer = null;
            switch (layerType) {
                case "WEIGHT": {
                    layer = new WeightLayer(layerNum);
                    //Nd4j.setDataType(DataType.DOUBLE);
                    layer.set(Nd4j.createFromNpyFile(file));
                    break;
                }
                case "ACTIVATION": {
                    layer = new ActivationLayer(layerNum);
                    //Nd4j.setDataType(DataType.DOUBLE);
                    layer.set(split[2]);
                    break;

                }
                default: {
                    throw new Exception("Unknown layer type: " + layerType);
                }
            }
            this.layers[layerNum] = layer;
        }
        for (int layerNum = 0; layerNum < layers.length; layerNum++) {
            System.out.println("Loaded layer at depth: " + layerNum + " ||| Layer details: " + layers[layerNum]);
        }
    }

    public Layer[] getLayers() {
        return this.layers;
    }
}
