package com.cj.utils;

public class TableauColumnSchema {
    private String id;
    private String dataType;

    public TableauColumnSchema() { }

    public TableauColumnSchema(String id, String dataType) {
        this.id = id;
        this.dataType = dataType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
