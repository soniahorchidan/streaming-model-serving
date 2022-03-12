package inference.nd4j.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;

public class INDArrayConversions {

    public static INDArray convertArrayListToINDArray(ArrayList<Float> list) {
        float[] data = ArrayUtils.toPrimitive((list.toArray(new Float[list.size()])));
        return Nd4j.create(data);
    }

    public static ArrayList<Float> convertINDArrayToArrayList(INDArray list) {
        return convertToArrayList(list.toFloatVector());
    }

    public static ArrayList<ArrayList<Float>> convertINDArrayBatchToArrayList(INDArray batch) {
        return convertToArrayListBatch(batch.toFloatMatrix());
    }

    private static ArrayList<Float> convertToArrayList(float[] inp) {
        ArrayList<Float> srcEmb = new ArrayList<>();
        for (float e : inp) {
            srcEmb.add(e);
        }
        return srcEmb;
    }

    private static ArrayList<ArrayList<Float>> convertToArrayListBatch(float[][] batch) {
        ArrayList<ArrayList<Float>> srcEmb = new ArrayList<>();
        for (float[] e : batch) {
            ArrayList<Float> batchResult = new ArrayList<>();
            for (float ee : e) {
                batchResult.add(ee);
            }
            srcEmb.add(batchResult);
        }
        return srcEmb;
    }


}
