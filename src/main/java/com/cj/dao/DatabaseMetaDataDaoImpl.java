package com.cj.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class DatabaseMetaDataDaoImpl implements DatabaseMetaDataDao {
    @Qualifier("querybuilder4j.db")
    @Autowired
    private DataSource dataSource;


    @Override
    public ResultSet getSchemas() throws SQLException {

        try (Connection conn = dataSource.getConnection()) {
            return conn.getMetaData().getSchemas();
        } catch (SQLException ex) {
            throw ex;
        }

    }

    @Override
    public ResultSet getTablesAndViews(String schema) throws SQLException {

        String sql = "SELECT tbl_name FROM sqlite_master where type ='table' OR type ='view';";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeQuery(sql);
        } catch (SQLException ex) {
            throw ex;
        }

    }

    @Override
    public ResultSet getColumns(String schema, String table) throws SQLException {

        try (Connection conn = dataSource.getConnection()) {
            return conn.getMetaData().getColumns(null, null, table, "%");
        } catch (SQLException ex) {
            throw ex;
        }

    }
}
