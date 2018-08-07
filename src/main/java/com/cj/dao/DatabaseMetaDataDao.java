package com.cj.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseMetaDataDao {

    ResultSet getSchemas() throws SQLException;
    ResultSet getTablesAndViews(String schema) throws SQLException;
    ResultSet getColumns(String schema, String table) throws SQLException;

}
