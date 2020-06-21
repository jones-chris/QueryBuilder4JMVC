package com.cj.model;

public class Schema {

    private String fullyQualifiedName;
    private String databaseName;
    private String schemaName;

    public Schema(String databaseName, String schemaName) {
        this.fullyQualifiedName = String.format("%s.%s", databaseName, schemaName);
        this.databaseName = databaseName;
        this.schemaName = schemaName;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
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
