package com.github.vitalibo.heatmap.loader;

import com.github.vitalibo.heatmap.loader.core.Job;
import com.github.vitalibo.heatmap.loader.core.Spark;
import com.github.vitalibo.heatmap.loader.infrastructure.Factory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Driver {

    private final Factory factory;

    public Driver() {
        this(Factory.getInstance());
    }

    public void run(String[] args) {
        final Job job;
        switch (args[0]) {
            case "generate":
                job = factory.createGenerateJob(args);
                break;
            default:
                throw new IllegalArgumentException("Unknown job name");
        }

        try (Spark spark = factory.createSpark()) {

            spark.submit(job);
        } catch (Exception e) {
            logger.error("Job failed execution", e);
            throw e;
        }
    }

    public static void main(String[] args) {
        Driver driver = new Driver();
        driver.run(args);
    }

}
