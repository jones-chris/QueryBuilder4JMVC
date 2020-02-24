package com.cj.service.database.audit;

import com.cj.dao.database.audit.DatabaseAuditDao;
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


    public boolean databaseStillExists(String databaseName) {
        return databaseAuditDao.databaseStillExists(databaseName);
    }

    public boolean tableStillExists(String databaseName, String table) {
        return databaseAuditDao.tableStillExists(databaseName, table);
    }

    public boolean numberOfTablesIsTheSame(String databaseName, int expectedNumberOfTables) {
        return databaseAuditDao.numberOfTablesIsTheSame(databaseName, 1);
    }

    public boolean numberOfTableColumnsIsTheSame(String databaseName, int expectedNumberOfTableColumns) {
        return databaseAuditDao.numberOfTableColumnsIsTheSame(databaseName, expectedNumberOfTableColumns);
    }

    public boolean numberOfRowsInTableIsTheSame(String databaseName, int expectedNumberOfTableRows) {
        return databaseAuditDao.numberOfRowsInTableIsTheSame(databaseName, expectedNumberOfTableRows);
    }

    public boolean tableDataIsTheSame(String databaseName, String[][] expectedData) {
        return databaseAuditDao.tableDataIsTheSame(databaseName, expectedData);
    }

    public boolean numberOfUsersWithTableAccessIsTheSame(String databaseName, int expectedNumberOfUsers) {
        return databaseAuditDao.numberOfUsersWithTableAccessIsTheSame(databaseName, expectedNumberOfUsers);
    }

    public Map<String, Boolean> runAllChecks(String databaseName) {
        return databaseAuditDao.runAllChecks(databaseName);
    }

}
