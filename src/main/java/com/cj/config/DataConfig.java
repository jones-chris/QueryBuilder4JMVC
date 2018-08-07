package com.cj.config;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration // this annotation declares that the class contains one or more @Bean annotations
@PropertySource("application.properties")
public class DataConfig {

    @Autowired
    private Environment env; // application.properties values are stores here

    @Bean(name = "querybuilder4j.db")
    @Primary
    public DataSource dataSource_querybuilder4j() {
        BasicDataSource ds = new BasicDataSource();

        // Driver class name
        ds.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));

        // Set URL
        ds.setUrl(env.getProperty("spring.database.url"));

        // Set username & password
        ds.setUsername(env.getProperty("spring.database.username"));
        ds.setPassword(env.getProperty("spring.database.password"));

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

}
