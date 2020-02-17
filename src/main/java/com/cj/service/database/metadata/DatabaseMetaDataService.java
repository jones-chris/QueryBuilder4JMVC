package com.cj.service.database.metadata;

import com.cj.model.Table;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseMetaDataService {

    String getSchemas() throws Exception;
    List<Table> getTablesAndViews(String schema) throws Exception;
    Map<String, Integer> getColumns(String schema, String table) throws SQLException;

}
