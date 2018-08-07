package com.cj.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class DatabaseMetaDataDaoImpl implements DatabaseMetaDataDao {
    @Qualifier("querybuilder4j.db")
    @Autowired
    private DataSource dataSource;


    @Override
    public ResultSet getSchemas() throws SQLException {
        return dataSource.getConnection().getMetaData().getSchemas();
    }

    @Override
    public ResultSet getTablesAndViews(String schema) throws SQLException {
        String sql = "SELECT tbl_name FROM sqlite_master where type ='table' OR type ='view';";
        return dataSource.getConnection().createStatement().executeQuery(sql);
    }

    @Override
    public ResultSet getColumns(String schema, String table) throws SQLException {
//        String sql = "SELECT p.name as columnName " +
//                "FROM sqlite_master m " +
//                "left outer join pragma_table_info((m.name)) p " +
//                "     on m.name <> p.name " +
//                "order by columnName;";
//        return dataSource.getConnection().createStatement().executeQuery(sql);
        return dataSource.getConnection().getMetaData().getColumns(null, null, table, "%");
    }
}
