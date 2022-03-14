package experiments.feedforward;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

public class ExperimentsRunner {
    public static void main(String[] args) throws Exception {
        // Load common configs
        InputStream commonExperimentConfig = new FileInputStream("expconfigs/common.properties");
        Properties commonProps = new Properties();
        commonProps.load(commonExperimentConfig);
        int experimentRuns = Integer.parseInt(commonProps.getProperty("experiment_runs"));
        int experimentTimeInSeconds = Integer.parseInt(commonProps.getProperty("experiment_time_in_seconds"));
        int warmupRequestsNum = Integer.parseInt(commonProps.getProperty("warmup_requests_num"));
        int maxInputRatePerThread = Integer.parseInt(commonProps.getProperty("max_input_rate_per_thread"));

        for (int experimentNum = 0; experimentNum < experimentRuns; experimentNum++) {
            System.out.println("=====================INPUT RATE EXPERIMENT " + experimentNum + "=====================");
            runOpenLoopExperiment(experimentTimeInSeconds, warmupRequestsNum, maxInputRatePerThread);
            System.out.println("====================================END======================================\n\n");
            Thread.sleep(5000);

            System.out.println("=====================BATCH SIZE EXPERIMENT " + experimentNum + "=====================");
            runCloseLoopExperiment(experimentTimeInSeconds, warmupRequestsNum, maxInputRatePerThread);
            System.out.println("====================================END======================================\n\n");
            Thread.sleep(5000);

            System.out.println("====================SCALABILITY EXPERIMENT " + experimentNum + "=====================");
            runScalabilityExperiment(experimentTimeInSeconds, warmupRequestsNum, maxInputRatePerThread);
            System.out.println("====================================END======================================\n\n");
            Thread.sleep(5000);
        }
    }

    private static void runOpenLoopExperiment(int experimentTimeInSeconds, int warmupRequestsNum,
                                              int maxInputRatePerThread) throws Exception {
        InputStream specificExperimentConfig = new FileInputStream("expconfigs/input-rate-exp-config.properties");
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
                    .run(inputRate, batchSize, experimentTimeInSeconds, modelReplicas, warmupRequestsNum,
                         maxInputRatePerThread, outputFile);
            System.out.println("****************END EXPERIMENT****************\n");
        }
    }


    private static void runCloseLoopExperiment(int experimentTimeInSeconds, int warmupRequestsNum,
                                               int maxInputRatePerThread) throws Exception {
        InputStream specificExperimentConfig = new FileInputStream("expconfigs/batch-size-exp-config.properties");
        Properties props = new Properties();
        props.load(specificExperimentConfig);
        int inputRate = Integer.parseInt(props.getProperty("input_rate"));
        int modelReplicas = Integer.parseInt(props.getProperty("model_replicas"));
        String batchSizeValuesString = props.getProperty("batch_size");
        int[] batchSizeValues = Arrays.stream(batchSizeValuesString.split(","))
                                      .mapToInt(Integer::parseInt).toArray();
        for (int batchSize : batchSizeValues) {
            System.out.println("****************START EXPERIMENT****************");
            System.out.println("BATCH SIZE = " + batchSize);
            System.out.println("************************************************");
            String outputFile = generateOutputFileName("batch-size", inputRate, modelReplicas, batchSize);
            FeedForwardPipeline
                    .run(inputRate, batchSize, experimentTimeInSeconds, modelReplicas, warmupRequestsNum,
                         maxInputRatePerThread, outputFile);
            System.out.println("****************END EXPERIMENT****************\n");
        }
    }

    private static void runScalabilityExperiment(int experimentTimeInSeconds, int warmupRequestsNum,
                                                 int maxInputRatePerThread) throws
                                                                            Exception {
        InputStream specificExperimentConfig = new FileInputStream("expconfigs/scalability-exp-config.properties");
        Properties props = new Properties();
        props.load(specificExperimentConfig);
        int inputRate = Integer.parseInt(props.getProperty("input_rate"));
        int batchSize = Integer.parseInt(props.getProperty("batch_size"));
        String modelReplicasValuesString = props.getProperty("model_replicas");
        int[] modelReplicasValue = Arrays.stream(modelReplicasValuesString.split(","))
                                         .mapToInt(Integer::parseInt).toArray();
        for (int modelReplicas : modelReplicasValue) {
            System.out.println("****************START EXPERIMENT****************");
            System.out.println("MODEL REPLICAS = " + modelReplicas);
            System.out.println("************************************************");
            String outputFile = generateOutputFileName("scalability", inputRate, modelReplicas, batchSize);
            FeedForwardPipeline
                    .run(inputRate, batchSize, experimentTimeInSeconds, modelReplicas, warmupRequestsNum,
                         maxInputRatePerThread, outputFile);
            System.out.println("****************END EXPERIMENT****************\n");
        }
    }

    private static String generateOutputFileName(String experimentName, int inputRate, int modelReplicas,
                                                 int batchSize) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        Date date = new Date();
        return "./experiments-results/" + experimentName + "/" + formatter.format(date) + "-ir" + inputRate + "_bs" +
               batchSize + "_rep" + modelReplicas + ".csv";
    }
}
