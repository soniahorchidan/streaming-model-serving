package datasources;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.examples.utils.ThrottledIterator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ImagesDataGenerator implements Iterator<ArrayList<ArrayList<Float>>>, Serializable {
    private static final Random rand = new Random();
    private static final int IMAGE_SIZE = 28;
    private final int batchSize;
    private int experimentTime;
    private int warmupRequestsNum;
    private boolean finishedWarmUp;
    private long startTime;

    ImagesDataGenerator(int batchSize, int experimentTimeInSeconds, int warmupRequestsNum) {
        this.batchSize = batchSize;
        this.experimentTime = experimentTimeInSeconds * 1000;
        this.warmupRequestsNum = warmupRequestsNum;

        this.finishedWarmUp = false;
    }

    @Override
    public boolean hasNext() {
        if (!finishedWarmUp) {
            warmupRequestsNum--;
            if (warmupRequestsNum < 0) {
                finishedWarmUp = true;
                this.startTime = System.currentTimeMillis();
            }
        } else {
            if (System.currentTimeMillis() - startTime > experimentTime)
                return false;
        }
        return true;
    }

    @Override
    public ArrayList<ArrayList<Float>> next() {
        ArrayList<ArrayList<Float>> batch = new ArrayList<>();
        for (int imgNum = 0; imgNum < this.batchSize; imgNum++) {
            int randomValIndex = rand.nextInt(IMAGE_SIZE * IMAGE_SIZE);
            ArrayList<Float> newImage = new ArrayList<>();
            // generate 0-filled image
            for (int i = 0; i < IMAGE_SIZE * IMAGE_SIZE - 1; i++) {
                newImage.add(0.0f);
            }
            // add one random value for each image
            newImage.add(randomValIndex, rand.nextFloat());
            // append the new image to the batch
            batch.add(newImage);
        }
        return batch;
    }

    public static DataStream<ArrayList<ArrayList<Float>>> getThrottledSource(
            StreamExecutionEnvironment env,
            long inputRate, int batchSize,
            int experimentTimeInSeconds,
            int warmupRequestsNum) {
        return env.fromCollection(
                new ThrottledIterator<>(new ImagesDataGenerator(batchSize, experimentTimeInSeconds, warmupRequestsNum),
                                        inputRate),
                TypeInformation.of(new TypeHint<ArrayList<ArrayList<Float>>>() {}));
    }

    public static DataStream<ArrayList<ArrayList<Float>>> getSource(StreamExecutionEnvironment env,
                                                                     int batchSize,
                                                                     int experimentTimeInSeconds,
                                                                     int warmupRequestsNum) {
        return env.fromCollection(new ImagesDataGenerator(batchSize, experimentTimeInSeconds, warmupRequestsNum),
                                  TypeInformation.of(new TypeHint<ArrayList<ArrayList<Float>>>() {}));
    }
}
