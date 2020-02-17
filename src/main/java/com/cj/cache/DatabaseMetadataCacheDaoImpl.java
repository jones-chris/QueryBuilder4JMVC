package com.cj.cache;

import com.cj.model.Column;
import com.cj.model.Schema;
import com.cj.model.Table;
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
import java.util.List;

@Repository
@PropertySource("application.properties")
public class DatabaseMetadataCacheDaoImpl {

    private DataSource targetDataSource;

    private DataSource cacheDataSource;

    @Value("${query.cache.insert}")
    private String cacheInsertSql;

    @Autowired
    public DatabaseMetadataCacheDaoImpl(@Qualifier("querybuilder4j.db") DataSource targetDataSource,
                                        @Qualifier("db_metadata_cache.db") DataSource cacheDataSource) {
        this.targetDataSource = targetDataSource;
        this.cacheDataSource = cacheDataSource;
    }

    public List<Schema> getSchemas() throws SQLException {
        try (Connection conn = targetDataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getSchemas();

            List<Schema> schemas = new ArrayList<>();
            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                Schema schema = new Schema(schemaName);
                schemas.add(schema);
            }

            return schemas;

        } catch (Exception ex) {
            throw ex;
        }
    }

    public List<Table> getTablesAndViews(String schema) throws SQLException {
        try (Connection conn = targetDataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getTables(null, schema, null, null);

            List<Table> tables = new ArrayList<>();
            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                Table table = new Table(schemaName, tableName);
                tables.add(table);
            }

            return tables;

        } catch (Exception ex) {
            throw ex;
        }
    }

    public List<Column> getColumns(String schema, String table) throws SQLException {
        try (Connection conn = targetDataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getColumns(null, schema, table, "%");

            List<Column> columns = new ArrayList<>();
            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                int dataType = rs.getInt("DATA_TYPE");

                Column column = new Column(schemaName, tableName, columnName, dataType);

                columns.add(column);
            }

            return columns;

        } catch (Exception ex) {
            throw ex;
        }
    }

    public void saveColumns(List<Column> columns) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(cacheDataSource);

        int numOfColumns = columns.size();
        MapSqlParameterSource[] paramsArray = new MapSqlParameterSource[numOfColumns];
        for (int i=0; i<columns.size(); i++) {
            Column column = columns.get(i);

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("schemaName", column.getSchemaName());
            params.addValue("tableName", column.getTableName());
            params.addValue("columnName", column.getColumnName());
            params.addValue("dataType", column.getDataType());

            paramsArray[i] = params;
        }

        namedParameterJdbcTemplate.batchUpdate(cacheInsertSql, paramsArray);

    }

}
