package com.cj.dao.database.metadata;

import com.cj.config.Qb4jConfig;
import com.cj.model.Column;
import com.cj.model.Schema;
import com.cj.model.Table;
import com.cj.model.mappers.ColumnMapper;
import com.cj.model.mappers.SchemaMapper;
import com.cj.model.mappers.TableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
@PropertySource("application.properties")
public class DatabaseMetaDataDaoImpl implements DatabaseMetaDataDao {

    private Qb4jConfig qb4jConfig;

    @Value("${qb4j.cache.schemas}")
    private String schemasSql;

    @Value("${qb4j.cache.tables_and_views}")
    private String tablesAndViewsSql;

    @Value("${qb4j.cache.columns}")
    private String columnsSql;

    @Autowired
    public DatabaseMetaDataDaoImpl(Qb4jConfig qb4jConfig) {
        this.qb4jConfig = qb4jConfig;
    }

    public List<Schema> getSchemas(String databaseName) throws Exception {
        DataSource dataSource = qb4jConfig.getDatabaseMetaDataCache().getDataSource();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("databaseName", databaseName);

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        return namedParameterJdbcTemplate.query(schemasSql, params, new SchemaMapper());
    }

    public List<Table> getTablesAndViews(String databaseName, String schema) {
        DataSource dataSource = qb4jConfig.getDatabaseMetaDataCache().getDataSource();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("databaseName", databaseName);
        params.addValue("schemaName", schema);

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        return namedParameterJdbcTemplate.query(tablesAndViewsSql, params, new TableMapper());
    }

    public List<Column> getColumns(String databaseName, String schema, String table) {
        DataSource dataSource = qb4jConfig.getDatabaseMetaDataCache().getDataSource();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("databaseName", databaseName);
        params.addValue("schemaName", schema);
        params.addValue("tableName", table);

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        return namedParameterJdbcTemplate.query(columnsSql, params, new ColumnMapper());
    }

}
