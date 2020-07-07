package com.cj.model;

public class Column {

    private String fullyQualifiedName;
    private String databaseName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private int dataType;

    public Column(String databaseName, String schemaName, String tableName, String columnName, int dataType) {
        this.fullyQualifiedName = String.format("%s.%s.%s.%s", databaseName, schemaName, tableName, columnName);
        this.databaseName = databaseName;

        // Some databases, like SQLite, do not have schemas, so change the schema name to "null" instead of null because
        // SQLite primary keys (which is used for the cache) cannot have null values.
        this.schemaName = (schemaName == null) ? "null" : schemaName;

        this.tableName = tableName;
        this.columnName = columnName;
        this.dataType = dataType;
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
        this.schemaName = (schemaName == null) ? "null" : schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String toSql(char beginningDelimiter, char endingDelimiter) {
        return String.format("  ")
    }
}
