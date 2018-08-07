package com.cj.service;

import com.cj.dao.DatabaseAuditDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DatabaseAuditorServiceImpl implements DatabaseAuditService {

    @Autowired
    private DatabaseAuditDao databaseAuditDao;


    public boolean databaseStillExists() {
        return databaseAuditDao.databaseStillExists();
    }

    public boolean tableStillExists(String table) {
        return databaseAuditDao.tableStillExists(table);
    }

    public boolean numberOfTablesIsTheSame(int expectedNumberOfTables) {
        return databaseAuditDao.numberOfTablesIsTheSame(1);
    }

    public boolean numberOfTableColumnsIsTheSame(int expectedNumberOfTableColumns) {
        return databaseAuditDao.numberOfTableColumnsIsTheSame(20);
    }

    public boolean numberOfRowsInTableIsTheSame(int expectedNumberOfTableRows) {
        return databaseAuditDao.numberOfRowsInTableIsTheSame(10);
    }

    public boolean tableDataIsTheSame(String[][] expectedData) {
        return databaseAuditDao.tableDataIsTheSame(new String[1][1]);
    }

    public boolean numberOfUsersWithTableAccessIsTheSame(int expectedNumberOfUsers) {
        return databaseAuditDao.numberOfUsersWithTableAccessIsTheSame(1);
    }

    @Override
    public Map<String, Boolean> runAllChecks(int expectedNumberOfTables, int expectedNumberOfTableColumns, String[] expectedData, int expectedNumberOfUsers) {
        return databaseAuditDao.runAllChecks(expectedNumberOfTables, expectedNumberOfTableColumns, expectedData, expectedNumberOfUsers);
    }

}
