package com.cj.service.database.metadata;

import com.cj.dao.database.metadata.DatabaseMetaDataDao;
import com.cj.model.Column;
import com.cj.model.Schema;
import com.cj.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class DatabaseMetaDataServiceImpl implements DatabaseMetaDataService {

    private DatabaseMetaDataDao databaseMetaDataDao;

    @Autowired
    public DatabaseMetaDataServiceImpl(DatabaseMetaDataDao databaseMetaDataDao) {
        this.databaseMetaDataDao = databaseMetaDataDao;
    }

    @Override
    public List<Schema> getSchemas(String databaseName) throws Exception {
        return databaseMetaDataDao.getSchemas(databaseName);
    }

    @Override
    public List<Table> getTablesAndViews(String databaseName, String schema) throws Exception {
        return databaseMetaDataDao.getTablesAndViews(databaseName, schema);
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
    public List<Column> getColumns(String databaseName, String schema, String table) throws SQLException {
        return databaseMetaDataDao.getColumns(databaseName, schema, table);
    }

}
