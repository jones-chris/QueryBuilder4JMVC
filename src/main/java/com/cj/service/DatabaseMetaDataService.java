package com.cj.service;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface DatabaseMetaDataService {

    ResultSet getSchemas() throws SQLException;
    ResultSet getTablesAndViews(String schema) throws SQLException;
    Map<String, Integer> getColumns(String schema, String table) throws SQLException;
    String executeQuery(String sql, SqlParameterSource paramMap) throws Exception;

}
