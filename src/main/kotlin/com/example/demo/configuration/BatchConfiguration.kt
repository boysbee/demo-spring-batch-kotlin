package com.example.demo.configuration;

import com.example.demo.item.ItemProcessor
import com.example.demo.item.ItemReader
import com.example.demo.item.ItemWriter
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled


@Configuration
@EnableBatchProcessing
@EnableScheduling
class BatchConfiguration {

    @Autowired
    lateinit var stepBuilderFactory: StepBuilderFactory

    @Autowired
    lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    lateinit var reader: ItemReader

    @Autowired
    lateinit var processor: ItemProcessor

    @Autowired
    lateinit var writer: ItemWriter

    @Autowired
    lateinit var jobLauncher: JobLauncher;

    @Bean
    fun step1() = stepBuilderFactory.get("step1")
            .tasklet { stepContribution, chunkContext ->
                println("Simple spring batch with Kotlin")
                RepeatStatus.FINISHED
            }
            .build()

    @Bean
    open fun step2() = stepBuilderFactory.get("step2")
            .chunk<String, String>(10)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build()

    @Bean
    @Qualifier("scheduledJob")
    fun job() = jobBuilderFactory.get("basicJob")
            .flow(step1())
            .next(step2())
            .end()
            .build()

    // This job runs in every 5 seconds
    @Scheduled(cron = "0/5 * * * * ?")
    fun launch() = {
        jobLauncher.run(job(), JobParametersBuilder()
                .addLong("launchTime", System.currentTimeMillis())
                .toJobParameters())
    }

    // This job runs in every 10 seconds
    @Scheduled(fixedRate = 10000)
    fun printMessage() {
        try {
            val jobParameters = JobParametersBuilder().addLong(
                    "time", System.currentTimeMillis()).toJobParameters()
            jobLauncher.run(job(), jobParameters)
            println("I have been scheduled with Spring scheduler")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Bean
    fun processJob() = jobBuilderFactory.get("processJob")
            .incrementer(RunIdIncrementer())
            .flow(step2()).end().build()


}