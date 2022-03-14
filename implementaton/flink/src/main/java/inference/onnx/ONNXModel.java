package inference.onnx;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import inference.commons.GenericModel;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ONNXModel extends GenericModel<ArrayList<Float>, ArrayList<Float>> implements Serializable {

    public OrtSession session = null;
    public OrtEnvironment env = null;

    @Override
    public void loadModel(String modelPath) throws Exception {
        File folder = new File(modelPath);
        // Assumes there is only one ONNX file containing the model
        File modelFilePath = folder.listFiles()[0];

        // Get the current ONNX runtime environment if not specified
        if (this.env == null) {
            env = OrtEnvironment.getEnvironment();
        }
        OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
        sessionOptions.setInterOpNumThreads(1);
        sessionOptions.setIntraOpNumThreads(1);

        // Load the ONNX model
        byte[] modelArray = Files.readAllBytes(modelFilePath.toPath());
        this.session = env.createSession(modelArray, sessionOptions);
        env.createSession(modelArray, sessionOptions);
    }

    @Override
    public ArrayList<Float> apply(ArrayList<Float> input) throws Exception {
        // Convert input of size N to float array of size 1xN
        float[][] inputArr = new float[1][input.size()];
        int j = 0;
        for (Float dd : input) {
            inputArr[0][j] = dd;
            j++;
        }

        OnnxTensor inputONNXTensor = OnnxTensor.createTensor(this.env, inputArr);
        Map<String, OnnxTensor> onnxInputs = new HashMap<>();
        onnxInputs.put("0", inputONNXTensor);

        // Run the inputs through the ONNX model
        OnnxValue onnxValueResults = this.session.run(onnxInputs).get(0);
        float[][] onnxOutput = (float[][]) onnxValueResults.getValue();

        ArrayList<Float> result = new ArrayList<>();
        for (float f : onnxOutput[0])
            result.add(f);
        return result;

    }
}