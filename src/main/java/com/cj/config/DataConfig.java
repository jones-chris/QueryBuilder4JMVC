package com.cj.config;

import com.cj.cache.DatabaseMetadataCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@PropertySource("application.properties")
public class DataConfig {

    private Environment env;

    @Value("${environment}")
    private String environment;

    @Autowired
    public DataConfig(Environment env) {
        this.env = env;
    }

    @Bean(name = "qb4jConfig")
    public Qb4jConfig getTargetDatabases() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/qb4j.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(inputStream);
        return mapper.readValue(node.get(environment).toPrettyString(), Qb4jConfig.class);
    }

}
