package com.cj.dao.database.audit;

import java.util.Map;

public interface DatabaseAuditDao {

    boolean databaseStillExists(String databaseName);
    boolean tableStillExists(String databaseName, String table);
    boolean numberOfTablesIsTheSame(String databaseName, int expectedNumberOfTables);
    boolean numberOfTableColumnsIsTheSame(String databaseName, int expectedNumberOfTableColumns);
    boolean numberOfRowsInTableIsTheSame(String databaseName, int expectedNumberOfTableRows);
    boolean tableDataIsTheSame(String databaseName, String[][] expectedData);
    boolean numberOfUsersWithTableAccessIsTheSame(String databaseName, int expectedNumberOfUsers);
    Map<String, Boolean> runAllChecks(String databaseName);

}
