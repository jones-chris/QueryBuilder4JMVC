package com.cj.model;

public class Schema {

    private String databaseName;
    private String schemaName;

    public Schema(String databaseName, String schemaName) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
