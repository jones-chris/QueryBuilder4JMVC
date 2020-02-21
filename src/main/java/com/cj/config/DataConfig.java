package com.cj.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Configuration
@org.springframework.context.annotation.PropertySource("application.properties")
public class DataConfig {

    private ConfigurableEnvironment env;

    @Value("${environment}")
    private String environment;

    @Autowired
    public DataConfig(ConfigurableEnvironment env) {
        this.env = env;
    }

    @Bean(name = "querybuilder4jdb_properties")
    public Properties getQueryBuilder4JDbProperties() {
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

    @Bean(name = "qb4jConfig")
    public Qb4jConfig getTargetDatabases() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getResourceAsStream("/qb4j.json");
        JsonNode node = mapper.readTree(inputStream);
        return mapper.readValue(node.get(environment).toPrettyString(), Qb4jConfig.class);
    }

    @Bean(name = "querybuilder4j.db")
    @Primary
    public DataSource getQuerybuilder4JDataSource() {
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

    @Bean(name = "db_metadata_cache.db")
    public DataSource getCacheDataSource() {
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
    public DataSource getQueryTemplatesDataSource() {
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

}
