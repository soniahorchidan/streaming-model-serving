package inference.tensorflow;

import inference.commons.GenericModel;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.Serializable;
import java.util.ArrayList;

public class TensorflowModel extends GenericModel<ArrayList<ArrayList<Float>>, ArrayList<ArrayList<Float>>>
        implements Serializable {
    private Session s = null;

    @Override
    public void loadModel(String modelPath) throws Exception {
        // Load the Tensorflow model
        SavedModelBundle bundle = SavedModelBundle.load(modelPath, "serve");
        this.s = bundle.session();
    }

    @Override
    public ArrayList<ArrayList<Float>> apply(ArrayList<ArrayList<Float>> input) throws Exception {
        // Convert input batch to float array
        float[][] inputArr = new float[input.size()][input.get(0).size()];
        int i = 0;
        for (ArrayList<Float> d : input) {
            int j = 0;
            for (Float dd : d) {
                inputArr[i][j] = dd;
                j++;
            }
            i++;
        }

        Tensor input_tensor = Tensor.create(inputArr);
        // Run the inputs through the Tensorflow model
        Tensor model_result = s.runner().feed("serving_default_input_1", input_tensor)
                               .fetch("StatefulPartitionedCall").run().get(0);
        float[][] outputArr = (float[][]) model_result
                .copyTo(new float[(int) model_result.shape()[0]][(int) model_result.shape()[1]]);

        ArrayList<ArrayList<Float>> result = new ArrayList<>();
        for (float[] d : outputArr) {
            ArrayList<Float> b = new ArrayList<>();
            for (float dd : d) {
                b.add(dd);
            }
            result.add(b);
        }
        return result;
    }
}