package com.cj.dao;

import java.util.Map;

public interface DatabaseAuditDao {

    boolean databaseStillExists();
    boolean tableStillExists(String table);
    boolean numberOfTablesIsTheSame(int expectedNumberOfTables);
    boolean numberOfTableColumnsIsTheSame(int expectedNumberOfTableColumns);
    boolean numberOfRowsInTableIsTheSame(int expectedNumberOfTableRows);
    boolean tableDataIsTheSame(String[][] expectedData);
    boolean numberOfUsersWithTableAccessIsTheSame(int expectedNumberOfUsers);
    Map<String, Boolean> runAllChecks();

}
