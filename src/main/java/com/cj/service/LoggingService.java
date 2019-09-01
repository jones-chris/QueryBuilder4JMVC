package com.cj.service;

import com.querybuilder4j.statements.SelectStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface LoggingService {

    boolean add(SelectStatement stmt, String sql, Map<String, Boolean> databaseAuditResults);
    ResultSet getAllRecords() throws SQLException;
    
}
