package com.cj.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration // this annotation declares that the class contains one or more @Bean annotations
@PropertySources({
        @PropertySource("application.properties"),
        @PropertySource("logging_db.properties"),
        @PropertySource("querybuilder4j_db.properties"),
        @PropertySource("dynamo_db.properties")
})
//@PropertySource("application.properties")
public class DataConfig {

    @Autowired
    private Environment env; // application.properties values are stores here

    @Bean(name = "querybuilder4j.db")
    @Primary
    public DataSource dataSource_querybuilder4j() {
        BasicDataSource ds = new BasicDataSource();

        // Driver class name
        ds.setDriverClassName(env.getProperty("driver-class-name"));

        // Set URL
        ds.setUrl(env.getProperty("url"));

        // Set username & password
        ds.setUsername(env.getProperty("username"));
        ds.setPassword(env.getProperty("password"));

        return ds;
    }

    @Bean(name = "logging.db")
    public DataSource dataSource_logging() {
        BasicDataSource ds = new BasicDataSource();

        // Driver class name
        ds.setDriverClassName(env.getProperty("logging.datasource.driver-class-name"));

        // Set URL
        ds.setUrl(env.getProperty("logging.database.url"));

        // Set username & password
        ds.setUsername(env.getProperty("logging.database.username"));
        ds.setPassword(env.getProperty("logging.database.password"));

        return ds;
    }

    @Bean(name = "dynamo.db")
    public DynamoDB dataSource_dynamo() {
        String accessKey = env.getProperty("aws.accessKey");
        String secretKey = env.getProperty("aws.secretKey");
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                                                           .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                                                           .withRegion(Regions.US_EAST_2)
                                                           .build();

        return new DynamoDB(client);
    }

}
