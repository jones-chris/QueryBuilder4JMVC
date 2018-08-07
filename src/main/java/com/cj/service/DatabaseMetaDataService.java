package com.cj.service;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseMetaDataService {

    ResultSet getSchemas() throws SQLException;
    ResultSet getTablesAndViews(String schema) throws SQLException;
    ResultSet getColumns(String schema, String table) throws SQLException;

}
