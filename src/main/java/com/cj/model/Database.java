package com.cj.model;

import com.querybuilder4j.statements.DatabaseType;

public class Database {
    private String databaseName;
    private DatabaseType databaseType;

    public Database(String databaseName, DatabaseType databaseType) {
        this.databaseName = databaseName;
        this.databaseType = databaseType;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }
}
