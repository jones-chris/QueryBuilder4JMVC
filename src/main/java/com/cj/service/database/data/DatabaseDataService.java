package com.cj.service.database.data;

public interface DatabaseDataService {

    String executeQuery(String databaseName, String sql) throws Exception;
    String getColumnMembers(String databaseName, String schema, String table, String column, int limit, int offset, boolean ascending, String search) throws Exception;

}
