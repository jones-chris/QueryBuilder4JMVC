package com.cj.cache;

import com.cj.config.Qb4jConfig;
import com.cj.config.Qb4jConfig.TargetDataSource;
import com.cj.model.Column;
import com.cj.model.Schema;
import com.cj.model.Table;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
@PropertySource("application.properties")
public class DatabaseMetadataCacheDaoImpl {

    private Qb4jConfig qb4jConfig;

    @Value("${query.cache.insert}")
    private String cacheInsertSql;

    @Value("${query.cache.delete}")
    private String cacheDeleteSql;

    @Autowired
    public DatabaseMetadataCacheDaoImpl(Qb4jConfig qb4jConfig) {
        this.qb4jConfig = qb4jConfig;
    }

    public List<Schema> getSchemas() throws Exception {
        List<Schema> schemas = new ArrayList<>();
        for (TargetDataSource targetDataSource : qb4jConfig.getTargetDataSources()) {
            try (Connection conn = targetDataSource.getDataSource().getConnection()) {
                ResultSet rs = conn.getMetaData().getSchemas();

                String databaseName = targetDataSource.getName();
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEM");
                    Schema schema = new Schema(databaseName, schemaName);
                    schemas.add(schema);
                }

            } catch (Exception ex) {
                throw ex;
            }
        }

        return schemas;
    }

    public List<Table> getTablesAndViews(String schema) throws Exception {
        List<Table> tables = new ArrayList<>();
        for (TargetDataSource targetDataSource : qb4jConfig.getTargetDataSources()) {
            try (Connection conn = targetDataSource.getDataSource().getConnection()) {
                ResultSet rs = conn.getMetaData().getTables(null, schema, null, null);

                String databaseName = targetDataSource.getName();
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEM");
                    String tableName = rs.getString("TABLE_NAME");
                    Table table = new Table(databaseName, schemaName, tableName);
                    tables.add(table);
                }

            } catch (Exception ex) {
                throw ex;
            }
        }

        return tables;
    }

    public List<Column> getColumns(String schema, String table) throws Exception {
        List<Column> columns = new ArrayList<>();
        for (TargetDataSource targetDataSource : qb4jConfig.getTargetDataSources()) {
            try (Connection conn = targetDataSource.getDataSource().getConnection()) {
                ResultSet rs = conn.getMetaData().getColumns(null, schema, table, "%");

                String databaseName = targetDataSource.getName();
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEM");
                    String tableName = rs.getString("TABLE_NAME");
                    String columnName = rs.getString("COLUMN_NAME");
                    int dataType = rs.getInt("DATA_TYPE");

                    Column column = new Column(databaseName, schemaName, tableName, columnName, dataType);

                    columns.add(column);
                }

            } catch (Exception ex) {
                throw ex;
            }
        }

        return columns;
    }

    public void deleteColumns() {
        DataSource databaseMetadataCacheDataSource = this.qb4jConfig.getDatabaseMetaDataCache().getDataSource();
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(databaseMetadataCacheDataSource);

        namedParameterJdbcTemplate.update(cacheDeleteSql, new HashMap<>());
    }

    public void saveColumns(List<Column> columns) {
        DataSource databaseMetadataCacheDataSource = this.qb4jConfig.getDatabaseMetaDataCache().getDataSource();
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(databaseMetadataCacheDataSource);

        int numOfColumns = columns.size();
        MapSqlParameterSource[] paramsArray = new MapSqlParameterSource[numOfColumns];
        for (int i=0; i<columns.size(); i++) {
            Column column = columns.get(i);

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("databaseName", column.getDatabaseName());
            params.addValue("schemaName", column.getSchemaName());
            params.addValue("tableName", column.getTableName());
            params.addValue("columnName", column.getColumnName());
            params.addValue("dataType", column.getDataType());

            paramsArray[i] = params;
        }

        namedParameterJdbcTemplate.batchUpdate(cacheInsertSql, paramsArray);

    }

}
