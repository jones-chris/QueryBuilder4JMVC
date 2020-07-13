package com.cj.dao.database.data;

import com.cj.model.QueryResult;

public interface DatabaseDataDao {

    QueryResult executeQuery(String databaseName, String sql) throws Exception;
    QueryResult getColumnMembers(String databaseName, String schema, String table, String column, int limit, int offset, boolean ascending, String search) throws Exception;

}
