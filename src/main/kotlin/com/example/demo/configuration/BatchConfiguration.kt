package com.example.demo.configuration;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import com.example.demo.item.*;

@Configuration
@EnableBatchProcessing
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
    fun job() = jobBuilderFactory.get("job1")
            .flow(step1())
            .next(step2())
            .end()
            .build()
}