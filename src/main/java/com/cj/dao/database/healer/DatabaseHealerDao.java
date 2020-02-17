package com.cj.dao.database.healer;

public interface DatabaseHealerDao {

    boolean dropDatabase();
    boolean createDatabase();
    boolean dropTable();
    boolean dropAllTablesExcept(String tableNotToDrop);
    boolean createTable();
    boolean insertTestData();
    boolean healDatabaseEntirely();

}
