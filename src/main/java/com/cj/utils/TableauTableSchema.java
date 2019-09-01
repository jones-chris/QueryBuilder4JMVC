package com.cj.utils;

import java.util.ArrayList;
import java.util.List;

public class TableauTableSchema {
    private String id;
    private String alias;
    private List<TableauColumnSchema> columns = new ArrayList<>();

    public TableauTableSchema() { }

    public TableauTableSchema(String id, String alias, List<TableauColumnSchema> columns) {
        this.id = id;
        this.alias = alias;
        this.columns = columns;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<TableauColumnSchema> getColumns() {
        return columns;
    }

    public void setColumns(List<TableauColumnSchema> columns) {
        this.columns = columns;
    }
}
