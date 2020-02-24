package com.cj.service.database.metadata;

import com.cj.model.Column;
import com.cj.model.Schema;
import com.cj.model.Table;

import java.sql.SQLException;
import java.util.List;

public interface DatabaseMetaDataService {

    List<Schema> getSchemas(String databaseName) throws Exception;
    List<Table> getTablesAndViews(String databaseName, String schema) throws Exception;
    List<Column> getColumns(String databaseName, String schema, String table) throws SQLException;

}
