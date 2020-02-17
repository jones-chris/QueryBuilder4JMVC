package com.cj.dao.database.metadata;


import com.cj.model.Table;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseMetaDataDao {

    String getSchemas() throws Exception;
    List<Table> getTablesAndViews(String schema) throws Exception;
    Map<String, Integer> getColumns(String schema, String table) throws SQLException;

}
