package com.cj.dao.database.data;

public interface DatabaseDataDao {

    String executeQuery(String sql) throws Exception;
    String getColumnMembers(String schema, String table, String column, int limit, int offset, boolean ascending, String search) throws Exception;

}
