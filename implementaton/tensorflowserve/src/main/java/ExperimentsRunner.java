import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

public class ExperimentsRunner {
    public static void main(String[] args) throws Exception {
        // Load common configs
        InputStream commonExperimentConfig = new FileInputStream(
                "src/main/java/expconfigs/common.properties");
        Properties commonProps = new Properties();
        commonProps.load(commonExperimentConfig);
        int experimentRuns = Integer.parseInt(commonProps.getProperty("experiment_runs"));
        int experimentTimeInSeconds = Integer.parseInt(commonProps.getProperty("experiment_time_in_seconds"));
        int warmupRequestsNum = Integer.parseInt(commonProps.getProperty("warmup_requests_num"));
        int maxInputRatePerThread = Integer.parseInt(commonProps.getProperty("max_input_rate_per_thread"));

        boolean isScalabilityExperiment = args.length > 0 && Boolean.parseBoolean(args[0]);
        for (int experimentNum = 0; experimentNum < experimentRuns; experimentNum++) {
            // needed because the scalability experiment is configured in the external model server (e.g. TensorflowServe)
            if (!isScalabilityExperiment) {
                System.out.println("==================INPUT RATE EXPERIMENT " + experimentNum + "==================");
                runOpenLoopExperiment(experimentTimeInSeconds, warmupRequestsNum, maxInputRatePerThread);
                System.out.println("==================================END====================================\n\n");

                System.out.println("===================BATCH SIZE EXPERIMENT " + experimentNum + "=================");
                runCloseLoopExperiment(experimentTimeInSeconds, warmupRequestsNum, maxInputRatePerThread);
                System.out.println("==================================END====================================\n\n");
            } else {
                int modelReplicasNum = args.length > 1 ? Integer.parseInt(args[1]) : 1;
                System.out.println("=================SCALABILITY EXPERIMENT " + experimentNum + "==================");
                runScalabilityExperiment(experimentTimeInSeconds, warmupRequestsNum, maxInputRatePerThread,
                                         modelReplicasNum);
                System.out.println("==================================END====================================\n\n");
            }
        }
    }

    /**
     * Open loop experiment - we measure the throughput of the system when varying the input rate. The requests happen
     * async.
     *
     * @param experimentTimeInSeconds
     * @param warmupRequestsNum
     * @throws Exception
     */
    private static void runOpenLoopExperiment(int experimentTimeInSeconds, int warmupRequestsNum,
                                              int maxInputRatePerThread) throws Exception {
        InputStream specificExperimentConfig = new FileInputStream(
                "src/main/java/expconfigs/input-rate-exp-config.properties");
        Properties props = new Properties();
        props.load(specificExperimentConfig);
        int batchSize = Integer.parseInt(props.getProperty("batch_size"));
        int modelReplicas = Integer.parseInt(props.getProperty("model_replicas"));
        String inputRateValuesString = props.getProperty("input_rate");
        int[] inputRateValues = Arrays.stream(inputRateValuesString.split(","))
                                      .mapToInt(Integer::parseInt).toArray();
        for (int inputRate : inputRateValues) {
            System.out.println("****************START EXPERIMENT****************");
            System.out.println("INPUT RATE = " + inputRate);
            System.out.println("************************************************");
            String outputFile = generateOutputFileName("input-rate", inputRate, modelReplicas, batchSize);
            FeedForwardPipeline
                    .run(inputRate, batchSize, experimentTimeInSeconds, warmupRequestsNum, true, maxInputRatePerThread,
                         outputFile);
            System.out.println("****************END EXPERIMENT****************\n");
            Thread.sleep(10000);
        }
    }


    /**
     * Close loop experiment - we measure the latency of the system when varying the batch size. The requests happen
     * synchronously; the input producers wait until the previous request completes before sending a new one.
     *
     * @param experimentTimeInSeconds
     * @param warmupRequestsNum
     * @throws Exception
     */
    private static void runCloseLoopExperiment(int experimentTimeInSeconds, int warmupRequestsNum,
                                               int maxInputRatePerThread) throws Exception {
        InputStream specificExperimentConfig = new FileInputStream(
                "src/main/java/expconfigs/batch-size-exp-config.properties");
        Properties props = new Properties();
        props.load(specificExperimentConfig);
        int modelReplicas = Integer.parseInt(props.getProperty("model_replicas"));
        int inputRate = Integer.parseInt(props.getProperty("input_rate"));
        String batchSizeValuesString = props.getProperty("batch_size");
        int[] batchSizeValues = Arrays.stream(batchSizeValuesString.split(","))
                                      .mapToInt(Integer::parseInt).toArray();
        for (int batchSize : batchSizeValues) {
            System.out.println("****************START EXPERIMENT****************");
            System.out.println("BATCH SIZE = " + batchSize);
            System.out.println("************************************************");
            String outputFile = generateOutputFileName("batch-size", inputRate, modelReplicas, batchSize);
            FeedForwardPipeline
                    .run(inputRate, batchSize, experimentTimeInSeconds, warmupRequestsNum, false, maxInputRatePerThread,
                         outputFile);
            System.out.println("****************END EXPERIMENT****************\n");
            Thread.sleep(10000);
        }
    }

    private static void runScalabilityExperiment(int experimentTimeInSeconds, int warmupRequestsNum,
                                                 int maxInputRatePerThread, int modelReplicas) throws
                                                                                               Exception {
        InputStream specificExperimentConfig = new FileInputStream(
                "src/main/java/expconfigs/scalability-exp-config.properties");
        Properties props = new Properties();
        props.load(specificExperimentConfig);
        int batchSize = Integer.parseInt(props.getProperty("batch_size"));
        int inputRate = Integer.parseInt(props.getProperty("input_rate"));
        System.out.println("MODEL REPLICAS = " + modelReplicas);
        String outputFile = generateOutputFileName("scalability", inputRate, modelReplicas, batchSize);
        FeedForwardPipeline
                .run(inputRate, batchSize, experimentTimeInSeconds, warmupRequestsNum,true, maxInputRatePerThread,
                     outputFile);
        Thread.sleep(10000);
    }

    private static String generateOutputFileName(String experimentName, int inputRate, int modelReplicas,
                                                 int batchSize) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        Date date = new Date();
        return "./experiments-results/" + experimentName + "/" + formatter.format(date) + "-ir" + inputRate + "_bs" +
               batchSize + "_rep" + modelReplicas + ".csv";
    }
}
