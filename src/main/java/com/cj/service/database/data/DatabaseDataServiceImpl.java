package com.cj.service.database.data;

import com.cj.dao.database.data.DatabaseDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseDataServiceImpl implements DatabaseDataService {

    private DatabaseDataDao databaseDataDao;

    @Autowired
    public DatabaseDataServiceImpl(DatabaseDataDao databaseDataDao) {
        this.databaseDataDao = databaseDataDao;
    }

    @Override
    public String executeQuery(String databaseName, String sql) throws Exception {
        return databaseDataDao.executeQuery(databaseName, sql);
    }

    @Override
    public String getColumnMembers(String databaseName,
                                   String schema,
                                   String table,
                                   String column,
                                   int limit,
                                   int offset,
                                   boolean ascending,
                                   String search) throws Exception {
        return databaseDataDao.getColumnMembers(databaseName, schema, table, column, limit, offset, ascending, search);
    }
}
