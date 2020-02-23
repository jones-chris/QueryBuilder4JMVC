package com.cj.model.mappers;

import com.cj.model.Column;
import com.cj.model.Table;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ColumnMapper implements RowMapper<Column> {

    @Override
    public Column mapRow(ResultSet resultSet, int i) throws SQLException {
        String databaseName = resultSet.getString("database_name");
        String schemaName = resultSet.getString("schema_name");
        String tableName = resultSet.getString("table_name");
        String columnName = resultSet.getString("column_name");
        int dataType = resultSet.getInt("data_type");

        return new Column(databaseName, schemaName, tableName, columnName, dataType);
    }
}
