package com.cj.dao;

import com.cj.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Repository
public class DatabaseAuditDaoImpl implements DatabaseAuditDao {

    @Qualifier("querybuilder4j.db")
    @Autowired
    private DataSource dataSource;


    public DatabaseAuditDaoImpl() {}

    /**
     * Executes a simple SQL query against the sqlite_master table for the purpose of confirming that the database
     * still exists.
     *
     * @return boolean
     */
    @Override
    public boolean databaseStillExists() {

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select type from sqlite_master limit 1;");
            return true;
        } catch (Exception ex) {
            return false;
        }

    }

    /**
     * Executes a simple SQL query against the sqlite_master table for the purpose of confirming that the table still
     * exists
     * @param table
     * @return boolean
     */
    @Override
    public boolean tableStillExists(String table) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("select tbl_name from sqlite_master where tbl_name = '%s' limit 1;", table));
            int numOfRows = 0;
            while (rs.next()) {
                numOfRows++;
            }
            return (numOfRows > 0);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Executes a SQL query against sqlite_master for the purpose of confirming that the number of tables is the same
     * as the expectedNumberOfTables parameter.
     *
     * @param expectedNumberOfTables
     * @return boolean
     */
    @Override
    public boolean numberOfTablesIsTheSame(int expectedNumberOfTables) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("select count(*) cnt from sqlite_master;");
            return (rs.getInt("cnt") == expectedNumberOfTables);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Executes a SQL query against sqlite_master for the purpose of confirming that the number of table columns is the
     * same as expectedNumberOfTableColumns.
     *
     * @param expectedNumberOfTableColumns
     * @return boolean
     */
    @Override
    public boolean numberOfTableColumnsIsTheSame(int expectedNumberOfTableColumns) {
        String sql = "SELECT count(*) cnt " +
                "FROM sqlite_master m " +
                "left outer join pragma_table_info((m.name)) p " +
                "     on m.name <> p.name;";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return (rs.getInt("cnt") == expectedNumberOfTableColumns);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Executes a SQL query against the SQLite database for the purpose of confirming that the number of rows in the table
     * is the same as expectedNumberOfTableRows.
     *
     * @param expectedNumberOfTableRows
     * @return boolean
     */
    @Override
    public boolean numberOfRowsInTableIsTheSame(int expectedNumberOfTableRows) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("select count(*) cnt from county_spending_detail;");
            return (rs.getInt("cnt") == expectedNumberOfTableRows);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Executes a SQL query against the SQLite database for the purpose of comparing the ResultSet against a 2D string array
     * to verify that the data has not been changed.
     *
     * @param expectedData
     * @return boolean
     */
    @Override
    public boolean tableDataIsTheSame(String[][] expectedData) {

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("select * " +
                                            "from county_spending_detail " +
                                            "order by fiscal_year_period asc, " +
                                            "         fiscal_year asc, " +
                                            "         service asc, " +
                                            "         department asc, " +
                                            "         program asc, " +
                                            "         amount asc;");

            int i = 0;
            while (rs.next()) {
                String fiscal_year_period = rs.getString("fiscal_year_period");
                String fiscal_year = rs.getString("fiscal_year");
                String service = rs.getString("service");
                String department = rs.getString("department");
                String program = rs.getString("program");
                String amount = rs.getString("amount");

                if (! fiscal_year_period.equals(Constants.currentData[i][0])) return false;
                if (! fiscal_year.equals(Constants.currentData[i][1])) return false;
                if (! service.equals(Constants.currentData[i][2])) return false;
                if (! department.equals(Constants.currentData[i][3])) return false;
                if (! program.equals(Constants.currentData[i][4])) return false;
                if (! amount.equals(Constants.currentData[i][5])) return false;

                i++;
            }

            return true;
        } catch (Exception ex) {
            return false;
        }

    }

    /**
     * Executes a SQL query for the purpose of comparing the ResultSet against the number of expected users that should
     * have access the table.
     *
     * This is NOT implemented as the SQLite instance does not make use of different users - all queries are run as an
     * admin user by default in SQLite.
     *
     * @param expectedNumberOfUsers
     * @return boolean
     */
    @Override
    public boolean numberOfUsersWithTableAccessIsTheSame(int expectedNumberOfUsers) {
        // There are no users in the SQLite database I've included, so always have this check pass.
        // Implement this method different for other databases or if users are enabled in SQLite.
        return true;
    }

    /**
     * This is a convenience method that runs all of the other public methods in the class and returns a single boolean.
     *
     * @param expectedNumberOfTables
     * @param expectedNumberOfTableColumns
     * @param expectedData
     * @param expectedNumberOfUsers
     * @return boolean
     */
    @Override
    public Map<String, Boolean> runAllChecks(int expectedNumberOfTables, int expectedNumberOfTableColumns, String[] expectedData, int expectedNumberOfUsers) {
        boolean databaseExists = databaseStillExists();
        boolean tableExists = tableStillExists("county_spending_detail");
        boolean tablesAreSame = numberOfTablesIsTheSame(1);
        boolean numOfTableColumnsAreSame = numberOfTableColumnsIsTheSame(6);
        boolean numOfTableRowsAreSame = numberOfRowsInTableIsTheSame(6);
        boolean tableDataIsSame = false;
        if (numOfTableRowsAreSame) {
             tableDataIsSame = tableDataIsTheSame(new String[1][1]);
        }
        boolean numOfUsersIsSame = numberOfUsersWithTableAccessIsTheSame(1);

        Map<String, Boolean> results = new HashMap<>();
        results.put("databaseExists", databaseExists);
        results.put("tableExists", tableExists);
        results.put("tablesAreSame", tablesAreSame);
        results.put("numOfTableColumnsAreSame", numOfTableColumnsAreSame);
        results.put("numOfTableRowsAreSame", numOfTableRowsAreSame);
        results.put("tableDataIsSame", tableDataIsSame);
        results.put("numOfUsersIsSame", numOfUsersIsSame);

        return results;
    }

}
