package input.data.feedforward;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.examples.utils.ThrottledIterator;

import java.util.ArrayList;

public class ImagesDataGeneratorSource extends RichParallelSourceFunction<Tuple2<ArrayList<ArrayList<Float>>, Long>> {
    private final ImagesDataGenerator generator;
    private ThrottledIterator<Tuple2<ArrayList<ArrayList<Float>>, Long>> throttledIterator;
    private boolean shouldBeThrottled = false;

    public ImagesDataGeneratorSource(int batchSize, int experimentTimeInSeconds, int warmupRequestsNum, int inputRate) {
        this.generator = new ImagesDataGenerator(batchSize, experimentTimeInSeconds, warmupRequestsNum);
        // An input rate equal to 0 means that the source should not be throttled
        if (inputRate > 0) {
            this.shouldBeThrottled = true;
            this.throttledIterator = new ThrottledIterator<>(generator, inputRate / 2);
        }
    }

    @Override
    public void run(SourceContext<Tuple2<ArrayList<ArrayList<Float>>, Long>> sourceContext) throws Exception {
        if (this.shouldBeThrottled) {
            while (this.throttledIterator.hasNext()) {
                sourceContext.collect(this.throttledIterator.next());
            }
        } else {
            while (this.generator.hasNext()) {
                sourceContext.collect(this.generator.next());
            }
        }
    }

    @Override
    public void cancel() {}
}