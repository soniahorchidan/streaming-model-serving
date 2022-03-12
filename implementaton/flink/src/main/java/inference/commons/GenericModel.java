package inference.commons;

public abstract class GenericModel<IN, OUT> {
    public abstract void loadModel(String modelPath) throws Exception;

    public abstract OUT apply(IN input) throws Exception;
}
