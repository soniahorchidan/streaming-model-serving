package input.models;

import inference.commons.GenericModel;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

// Monitors a directory in the local file system and creates a new model when a new model is uploaded to the file system
public class ModelSource implements SourceFunction<GenericModel> {
    private final String modelsLocation;
    private final GenericModel model;

    public ModelSource(String modelsLocation, GenericModel model) throws Exception {
        this.modelsLocation = modelsLocation;
        this.model = model;
    }

    @Override
    public void run(SourceContext<GenericModel> sourceContext) throws Exception {
        // NOTE: commented out while running benchmarks to ensure the Flink job finishes in between consecutive runs.
        //  Will need to add it back in when running experiments for model updates.

        //WatchService watchService = FileSystems.getDefault().newWatchService();
        //Path path = Paths.get(modelsLocation);
        //path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        //WatchKey key;
        //while ((key = watchService.take()) != null) {
        //    for (WatchEvent<?> event : key.pollEvents()) {
        //        Path fullPath = Paths.get(modelsLocation, event.context().toString());
        //        if (!Files.isDirectory(fullPath))
        //            continue;
        //        System.err.println("\nNew model uploaded: " + fullPath.getFileName() + "\n");
        //        model.loadModel(fullPath.toString());
        //        sourceContext.collect(model);
        //    }
        //    key.reset();
        //}
    }

    @Override
    public void cancel() {}
}
