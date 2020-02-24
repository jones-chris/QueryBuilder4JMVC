package com.cj.model.mappers;

import com.cj.model.Table;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TableMapper implements RowMapper<Table> {

    @Override
    public Table mapRow(ResultSet resultSet, int i) throws SQLException {
        String databaseName = resultSet.getString("database_name");
        String schemaName = resultSet.getString("schema_name");
        String tableName = resultSet.getString("table_name");

        return new Table(databaseName, schemaName, tableName);
    }
}
