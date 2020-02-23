package com.cj.service.database.healer;

public interface DatabaseHealerService {

    boolean dropDatabase();
    boolean createDatabase();
    boolean dropTable(String databaseName);
    boolean dropAllTablesExcept(String databaseName, String tableNotToDrop);
    boolean createTable(String databaseName);
    boolean insertTestData(String databaseName);
    boolean healDatabaseEntirely(String databaseName);

}
