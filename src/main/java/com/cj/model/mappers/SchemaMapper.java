package com.cj.model.mappers;

import com.cj.model.Schema;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SchemaMapper implements RowMapper<Schema> {

    @Override
    public Schema mapRow(ResultSet resultSet, int i) throws SQLException {
        String databaseName = resultSet.getString("database_name");
        String schemaName = resultSet.getString("schema_name");

        return new Schema(databaseName, schemaName);
    }

}
