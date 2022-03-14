import datasources.ImagesDataGeneratorSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import request.AsyncInferenceRequest;
import request.InferenceRequest;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FeedForwardPipeline {
    private static final String TORCH_SERVING_URL = "http://127.0.0.1:8080/predictions/ffnn";
    private static final int ASYNC_OPERATOR_CAPACITY = 10000;
    private static final long ASYNC_OPERATOR_TIMEOUT = 10000;

    public static void run(int inputRate, int batchSize, int experimentTimeInSeconds, int warmUpRequestsNum,
                           boolean isAsync, int maxInputRatePerThread, String outputFile) throws Exception {

        // set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Add data sources
        int inputPotentialProducers = (int) Math.ceil((double) inputRate / maxInputRatePerThread);
        int inputProducers = inputPotentialProducers > 0 ? inputPotentialProducers : 1;
        int inputRatePerProducer = Math.min(inputRate, inputRate / inputProducers);
        System.out.println("Input producers: " + inputProducers);
        System.out.println("Input rate per producer: " + inputRatePerProducer);

        env.setParallelism(inputProducers);

        DataStream<ArrayList<ArrayList<Float>>> batches = env
                .addSource(
                        new ImagesDataGeneratorSource(batchSize, experimentTimeInSeconds, warmUpRequestsNum,
                                                      inputRatePerProducer));


        // send inference request
        DataStream<Tuple3<String, Long, Long>> results;
        if (isAsync) {
            results = performRequestAsync(TORCH_SERVING_URL, batches);
        } else {
            // NOTE: output looks like (scoring_result, in_timestamp, out_timestamp)
            results = performRequest(TORCH_SERVING_URL, batches);
        }

        // Benchmarking - record timestamp when the scoring is done
        results.map(new MapFunction<Tuple3<String, Long, Long>, Tuple2<Long, Long>>() {
            @Override
            public Tuple2<Long, Long> map(Tuple3<String, Long, Long> record) throws Exception {
                // filter out failed requests
                if (record.f0 != null) {
                    return new Tuple2<>(record.f1, record.f2);
                } else {
                    return new Tuple2<>(-1L, -1L);
                }
            }
        }).writeAsCsv(outputFile);

        env.execute();
    }

    private static DataStream<Tuple3<String, Long, Long>> performRequest(String torchURL,
                                                                         DataStream<ArrayList<ArrayList<Float>>> batches) {
        DataStream<Tuple3<String, Long, Long>> results = batches
                .map(new MapFunction<ArrayList<ArrayList<Float>>, Tuple3<String, Long, Long>>() {
                    @Override
                    public Tuple3<String, Long, Long> map(
                            ArrayList<ArrayList<Float>> batch) throws Exception {
                        URL url = new URL(torchURL);
                        long startTime = System.nanoTime();
                        String data = InferenceRequest.buildRequest(batch);
                        String result = InferenceRequest.makeRequest(data, url);
                        // NOTE: Measures the duration for the whole batch
                        return new Tuple3<>(result, startTime, System.nanoTime());
                    }
                });
        return results;
    }

    private static DataStream<Tuple3<String, Long, Long>> performRequestAsync(String torchURL,
                                                                              DataStream<ArrayList<ArrayList<Float>>> batches) throws
                                                                                                                               Exception {
        AsyncInferenceRequest asyncFunction = new AsyncInferenceRequest(torchURL);
        DataStream<Tuple3<String, Long, Long>> results = AsyncDataStream.orderedWait(
                batches,
                asyncFunction,
                ASYNC_OPERATOR_TIMEOUT,
                TimeUnit.MILLISECONDS,
                ASYNC_OPERATOR_CAPACITY);
        return results;
    }
}
