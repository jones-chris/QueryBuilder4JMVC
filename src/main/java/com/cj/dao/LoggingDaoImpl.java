package com.cj.dao;

import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;

@Repository
public class LoggingDaoImpl implements LoggingDao {
    @Qualifier("logging.db")
    @Autowired
    private DataSource dataSource;


    @Override
    public boolean add(SelectStatement stmt, String sql, Map<String, Boolean> databaseAuditResults) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        String insertSql = String.format("insert into log (timestamp, select_statement_to_string, select_statement_to_sql, database_still_exists, " +
                "table_still_exists, num_of_tables_is_same, num_of_cols_in_table_is_same, num_of_rows_in_table_is_same, " +
                "table_data_is_same, user_table_permissions_are_same) " +
                "values ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                timestamp,
                stmt.toString(),
                sql.replace("'", "''"), // escape single quotes.
                databaseAuditResults.get("databaseExists"),
                databaseAuditResults.get("tableExists"),
                databaseAuditResults.get("tablesAreSame"),
                databaseAuditResults.get("numOfTableColumnsAreSame"),
                databaseAuditResults.get("numOfTableRowsAreSame"),
                databaseAuditResults.get("tableDataIsSame"),
                databaseAuditResults.get("numOfUsersIsSame"));

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            return statement.execute(insertSql);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public ResultSet getAllRecords() throws SQLException {
        String sql = "select * from log;";
        return dataSource.getConnection().createStatement().executeQuery(sql);
    }

}
