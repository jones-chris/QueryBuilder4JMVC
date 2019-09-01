package com.cj.service;

import com.cj.dao.LoggingDao;
import com.querybuilder4j.statements.SelectStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Service
public class LoggingServiceImpl implements LoggingService {
    @Autowired
    private LoggingDao loggingDao;


    @Override
    public boolean add(SelectStatement stmt, String sql, Map<String, Boolean> databaseAuditResults) {
        return loggingDao.add(stmt, sql, databaseAuditResults);
    }

    @Override
    public ResultSet getAllRecords() throws SQLException {
        return loggingDao.getAllRecords();
    }
}
