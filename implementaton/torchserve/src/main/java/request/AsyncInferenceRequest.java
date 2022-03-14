package request;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class AsyncInferenceRequest
        extends RichAsyncFunction<ArrayList<ArrayList<Float>>, Tuple3<String, Long, Long>> {
    private final URL url;

    public AsyncInferenceRequest(String url) throws Exception {
        // Create a neat value object to hold the URL
        this.url = new URL(url);
    }

    @Override
    public void asyncInvoke(ArrayList<ArrayList<Float>> inputBatch,
                            ResultFuture<Tuple3<String, Long, Long>> resultFuture) throws Exception {
        long startTime = System.nanoTime();
        // Append all the edges to a string
        String data = InferenceRequest.buildRequest(inputBatch);
        String result = InferenceRequest.makeRequest(data, url);
        // NOTE: Measures the duration for the whole batch
        resultFuture.complete(Collections.singleton(new Tuple3<>(result, startTime, System.nanoTime())));
    }
}
