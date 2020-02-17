package com.cj.config;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration // this annotation declares that the class contains one or more @Bean annotations
@PropertySources({
        @PropertySource("application.properties")
})
public class DataConfig {

    private Environment env;

    @Value("${environment}")
    private String environment;

    @Autowired
    public DataConfig(Environment env) {
        this.env = env;
    }

    @Bean(name = "querybuilder4jdb_properties")
    public Properties queryBuilder4JDbProperties() {
        String driverClassName = env.getProperty(environment + ".driver-class-name");
        String url = env.getProperty(environment + ".url");
        String username = env.getProperty(environment + ".username");
        String password = env.getProperty(environment + ".password");
        String databaseType = env.getProperty(environment + ".databaseType");

        Properties properties = new Properties();
        properties.setProperty("driver-class-name", driverClassName);
        properties.setProperty("url", url);
        properties.setProperty("username", username);
        properties.setProperty("password", password);
        properties.setProperty("databaseType", databaseType);
        return properties;
    }

    @Bean(name = "querybuilder4j.db")
    @Primary
    public DataSource dataSource_querybuilder4j() {
        BasicDataSource ds = new BasicDataSource();

        String driverClassName = env.getProperty(environment + ".driver-class-name");
        ds.setDriverClassName(driverClassName);

        String url = env.getProperty(environment + ".url");
        ds.setUrl(url);

        String username = env.getProperty(environment + ".username");
        ds.setUsername(username);

        String password = env.getProperty(environment + ".password");
        ds.setPassword(password);

        return ds;
    }

    @Bean(name = "logging.db")
    public DataSource dataSource_logging() {
        BasicDataSource ds = new BasicDataSource();

        String driverClassName = env.getProperty(environment + ".logging.datasource.driver-class-name");
        ds.setDriverClassName(driverClassName);

        String url = env.getProperty(environment + ".logging.database.url");
        ds.setUrl(url);

        String username = env.getProperty(environment + ".logging.database.username");
        ds.setUsername(username);

        String password = env.getProperty(environment + ".logging.database.password");
        ds.setPassword(password);

        return ds;
    }

    @Bean(name = "db_metadata_cache.db")
    public DataSource dataSource_cache() {
        BasicDataSource ds = new BasicDataSource();

        String driverClassName = env.getProperty(environment + ".db_metadata_cache.datasource.driver-class-name");
        ds.setDriverClassName(driverClassName);

        String url = env.getProperty(environment + ".db_metadata_cache.database.url");
        ds.setUrl(url);

        String username = env.getProperty(environment + ".db_metadata_cache.database.username");
        ds.setUsername(username);

        String password = env.getProperty(environment + ".db_metadata_cache.database.password");
        ds.setPassword(password);

        return ds;
    }

    @Bean(name = "query_templates.db")
    public DataSource dataSource_queryTemplates() {
        BasicDataSource ds = new BasicDataSource();

        String driverClassName = env.getProperty(environment + ".query_templates.datasource.driver-class-name");
        ds.setDriverClassName(driverClassName);

        String url = env.getProperty(environment + ".query_templates.database.url");
        ds.setUrl(url);

        String username = env.getProperty(environment + ".query_templates.database.username");
        ds.setUsername(username);

        String password = env.getProperty(environment + ".query_templates.database.password");
        ds.setPassword(password);

        return ds;
    }

//    @Bean
//    public AmazonSNS getSnsClient() {
//        String accessKey = env.getProperty("aws.accessKey");
//        String secretKey = env.getProperty("aws.secretKey");
//        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
//
//        return AmazonSNSClient
//                .builder()
//                .withRegion(Regions.US_EAST_1)
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .build();
//    }

}
