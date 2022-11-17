package com.example.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@SpringBootApplication
@EnableBatchProcessing
public class DemoApplication {

    private static Logger LOGGER = LogManager.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public Job job(JobRepository jobRepository,
                   PlatformTransactionManager platformTransactionManager) {

        TaskletStep step1 = new StepBuilder("demo step")
                .transactionManager(platformTransactionManager)
                .repository(jobRepository)
                .<String, String>chunk(1)
                .reader(new ListItemReader<>(List.of("1", "2", "3")))
                .writer(items -> {
                    LOGGER.info("About to write " + items);
                    throw new RuntimeException();
                })
                .faultTolerant()
                .skipPolicy((throwable, skipCount) -> true)
                .retryPolicy(new MaxAttemptsRetryPolicy(1))
                .build();

        return new JobBuilder("demo")
                .repository(jobRepository)
                .start(step1)
                .build();
    }
}
