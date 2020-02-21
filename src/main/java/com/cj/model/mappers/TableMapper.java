package com.cj.model.mappers;

import com.cj.model.Table;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TableMapper implements RowMapper<Table> {

    @Override
    public Table mapRow(ResultSet resultSet, int i) throws SQLException {
        String schemaName = null;

        // Some databases do not have schemas, like SQLite, so catch exception and let schemaName be null.
        try {
            schemaName = resultSet.getString("schema_name");
        } catch (SQLException ignored) {}

        String tableName = resultSet.getString("table_name");

        return new Table(null, schemaName, tableName);
    }
}
