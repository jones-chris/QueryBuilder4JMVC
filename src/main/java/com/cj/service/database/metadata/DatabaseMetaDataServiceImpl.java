package com.cj.service.database.metadata;

import com.cj.dao.database.metadata.DatabaseMetaDataDao;
import com.cj.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
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
        return databaseMetaDataDao.getSchemas();
    }

    @Override
    public List<Table> getTablesAndViews(String schema) throws Exception {
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

}
