package com.sop_recommendation_service.sop_recommendation_service.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.sop_recommendation_service.sop_recommendation_service.repository")
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb+srv://amir:RFMHyDOOQBpXy2Sh@cluster0.kflmc1e.mongodb.net/analytics?retryWrites=true&w=majority&appName=Cluster0}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:analytics}")
    private String database;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    @Bean
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoUri);
    }
}