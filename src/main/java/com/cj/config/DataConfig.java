package com.cj.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration // this annotation declares that the class contains one or more @Bean annotations
@PropertySources({
        @PropertySource("application.properties"),
        @PropertySource("logging_db.properties"),
        @PropertySource("querybuilder4j_db.properties"),
        @PropertySource("query_templates_db.properties")
})
public class DataConfig {

    @Autowired
    private Environment env; // Properties values are stores here

    @Bean(name = "querybuilder4jdb_properties")
    public Properties queryBuilder4JDbProperties() {
        Properties properties = new Properties();
        properties.setProperty("driver-class-name", env.getProperty("driver-class-name"));
        properties.setProperty("url", env.getProperty("url"));
        properties.setProperty("username", env.getProperty("username"));
        properties.setProperty("password", env.getProperty("password"));
        properties.setProperty("databaseType", env.getProperty("databaseType"));
        return properties;
    }

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

    @Bean(name = "query_templates.db")
    public DataSource dataSource_queryTemplates() {
        BasicDataSource ds = new BasicDataSource();

        // Driver class name
        ds.setDriverClassName(env.getProperty("query_templates.datasource.driver-class-name"));

        // Set URL
        ds.setUrl(env.getProperty("query_templates.database.url"));

        // Set username & password
        ds.setUsername(env.getProperty("query_templates.database.username"));
        ds.setPassword(env.getProperty("query_templates.database.password"));

        return ds;
    }

    @Bean
    public AmazonSNS getSnsClient() {
        String accessKey = env.getProperty("aws.accessKey");
        String secretKey = env.getProperty("aws.secretKey");
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonSNSClient
                .builder()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

}
