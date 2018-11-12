package com.cj.dao;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface DatabaseMetaDataDao {

    String getSchemas() throws Exception;
    String getTablesAndViews(String schema) throws Exception;
    Map<String, Integer> getColumns(String schema, String table) throws SQLException;
    String executeQuery(String sql, SqlParameterSource paramMap) throws Exception;

}
