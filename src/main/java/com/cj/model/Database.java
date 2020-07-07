package com.cj.model;


import com.cj.constants.DatabaseType;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private String databaseName;
    private DatabaseType databaseType;
    private transient List<Schema> schemas = new ArrayList<>();

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

    public List<Schema> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<Schema> schemas) {
        this.schemas = schemas;
    }

}
