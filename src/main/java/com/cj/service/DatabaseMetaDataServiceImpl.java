package com.cj.service;

import com.cj.dao.DatabaseMetaDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DatabaseMetaDataServiceImpl implements DatabaseMetaDataService {

    private DatabaseMetaDataDao databaseMetaDataDao;

    @Autowired
    public DatabaseMetaDataServiceImpl(DatabaseMetaDataDao databaseMetaDataDao) {
        this.databaseMetaDataDao = databaseMetaDataDao;
    }

    @Override
    public String getSchemas() throws Exception {
        String results = databaseMetaDataDao.getSchemas();
        return (results.equals("[]")) ? "[{\"data\":\"null\"}]" : results;
    }

    @Override
    public String getTablesAndViews(String schema) throws Exception {
        return databaseMetaDataDao.getTablesAndViews(schema);
    }

    /**
     * Because this service gets data from a SQLite database and SQLite does not have a concise SQL query for getting all table
     * columns, I have to write Java code to concatenate the table columns with the table name.
     *
     * @param schema
     * @param table
     * @return
     * @throws SQLException
     */
    @Override
    public Map<String, Integer> getColumns(String schema, String table) throws SQLException {
        Map<String, Integer> firstMap = databaseMetaDataDao.getColumns(schema, table);

        Map<String, Integer> finalMap = new HashMap<>();
        for (Map.Entry<String, Integer> pair : firstMap.entrySet()) {
            String key = table + "." + pair.getKey();
            finalMap.put(key, pair.getValue());
        }

        return finalMap;
    }

    @Override
    public String executeQuery(String sql) throws Exception {
        return databaseMetaDataDao.executeQuery(sql);
    }

    @Override
    public String getColumnMembers(String schema,
                                   String table,
                                   String column,
                                   int limit,
                                   int offset,
                                   boolean ascending,
                                   String search) throws Exception {
        return databaseMetaDataDao.getColumnMembers(schema, table, column, limit, offset, ascending, search);
    }
}
