package com.cj.service;

import com.cj.dao.DatabaseAuditDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DatabaseAuditorServiceImpl implements DatabaseAuditService {

    private DatabaseAuditDao databaseAuditDao;

    @Autowired
    public DatabaseAuditorServiceImpl(DatabaseAuditDao databaseAuditDao) {
        this.databaseAuditDao = databaseAuditDao;
    }


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
        return databaseAuditDao.numberOfTableColumnsIsTheSame(expectedNumberOfTableColumns);
    }

    public boolean numberOfRowsInTableIsTheSame(int expectedNumberOfTableRows) {
        return databaseAuditDao.numberOfRowsInTableIsTheSame(expectedNumberOfTableRows);
    }

    public boolean tableDataIsTheSame(String[][] expectedData) {
        return databaseAuditDao.tableDataIsTheSame(expectedData);
    }

    public boolean numberOfUsersWithTableAccessIsTheSame(int expectedNumberOfUsers) {
        return databaseAuditDao.numberOfUsersWithTableAccessIsTheSame(expectedNumberOfUsers);
    }

    @Override
    public Map<String, Boolean> runAllChecks() {
        return databaseAuditDao.runAllChecks();
    }

}
