package inference.commons;

import java.io.Serializable;

public abstract class ScoringFunction<IN, OUT, M_TYPE extends GenericModel> implements Serializable {
    // TODO(sonia): replace all types with enums!
    protected String modelType = null;
    protected M_TYPE model = null;

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public abstract void load(String modelPath) throws Exception;

    public abstract OUT processElement(IN input) throws Exception;

    public abstract void updateModel(GenericModel genericModel);
}
