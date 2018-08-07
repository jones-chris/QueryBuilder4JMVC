package com.cj.service;

import com.cj.dao.DatabaseMetaDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class DatabaseMetaDataServiceImpl implements DatabaseMetaDataService {
    @Autowired
    private DatabaseMetaDataDao databaseMetaDataDao;

    @Override
    public ResultSet getSchemas() throws SQLException{
        return databaseMetaDataDao.getSchemas();
    }

    @Override
    public ResultSet getTablesAndViews(String schema) throws SQLException {
        return databaseMetaDataDao.getTablesAndViews(schema);
    }

    @Override
    public ResultSet getColumns(String schema, String table) throws SQLException {
        return databaseMetaDataDao.getColumns(schema, table);
    }
}
