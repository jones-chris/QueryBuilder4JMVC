package com.cj.dao.database.healer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Repository
public class DatabaseHealerDaoImpl implements DatabaseHealerDao {

    private DataSource dataSource;

    @Autowired
    public DatabaseHealerDaoImpl(@Qualifier("querybuilder4j.db") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Deletes the database file.
     *
     * @return boolean
     */
    @Override
    public boolean dropDatabase() {
        String projectRootDirectory = System.getProperty("user.dir");
        return new File(projectRootDirectory + "\\data\\querybuilder4j.db").delete();
    }

    /**
     * Creates a new database.  This method is not relevant to SQLite as the DROP DATABASE command does not exist in SQLite.
     * Therefore, the database should never have to be re-created.
     *
     * @return boolean
     */
    @Override
    public boolean createDatabase() {
//        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:./data/querybuilder4j.db")) {
//            return true;
//        } catch (SQLException ex) {
//            return false;
//        }
        return true;
    }

    /**
     * Drops the specified table from the database.
     *
     * @return boolean
     */
    @Override
    public boolean dropTable() {
        String sql = "DROP TABLE county_spending_detail;";

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            return statement.execute(sql);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Drop all tables except the table name passed in to the method.
     *
     * @param tableNotToDrop
     * @return boolean
     */
    @Override
    public boolean dropAllTablesExcept(String tableNotToDrop) {
        String selectSql = "select tbl_name from sqlite_master;";
        String dropSql = "DROP TABLE %s;";

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            ResultSet rs = statement.executeQuery(selectSql);

            while (rs.next()) {
                String tableName = rs.getString("tbl_name");
                if (! tableName.equals(tableNotToDrop)) {
                    boolean success = statement.execute(String.format(dropSql, tableName));
                    //TODO:  if not success, then log in application log.  This does not need to throw an exception though,
                    //TODO cont'd:  as UI will not allow users to query on tables other than county_spending_detail.
                }
            }

            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Creates the county_spending_detail table.
     *
     * @return boolean
     */
    @Override
    public boolean createTable() {
        String sql = "CREATE TABLE county_spending_detail " +
                "(" +
                "   fiscal_year_period  INTEGER, " +
                "   fiscal_year         INTEGER, " +
                "   service             TEXT, " +
                "   department          TEXT, " +
                "   program             TEXT, " +
                "   amount              INTEGER " +
                ");";

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            return statement.execute(sql);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Inserts test data into the county_spending_detail table.
     *
     * @return boolean
     */
    @Override
    public boolean insertTestData() {
        String sql1 = "INSERT INTO county_spending_detail\n" +
                "(\n" +
                "  fiscal_year_period,\n" +
                "  fiscal_year,\n" +
                "  service,\n" +
                "  department,\n" +
                "  program,\n" +
                "  amount\n" +
                ")\n" +
                "VALUES\n" +
                "(\n" +
                "  4,\n" +
                "  2017,\n" +
                "  'General Government',\n" +
                "  'Liquor Control',\n" +
                "  'Retail Sales Operations',\n" +
                "  214.85\n" +
                ");\n";
        String sql2 = "INSERT INTO county_spending_detail\n" +
                "(\n" +
                "  fiscal_year_period,\n" +
                "  fiscal_year,\n" +
                "  service,\n" +
                "  department,\n" +
                "  program,\n" +
                "  amount\n" +
                ")\n" +
                "VALUES\n" +
                "(\n" +
                "  6,\n" +
                "  2014,\n" +
                "  'General Government',\n" +
                "  'Human Resources',\n" +
                "  'Health & Employee Welfare',\n" +
                "  1576281.42\n" +
                ");\n" +
                "\n";

        String sql3 = "INSERT INTO county_spending_detail\n" +
                "(\n" +
                "  fiscal_year_period,\n" +
                "  fiscal_year,\n" +
                "  service,\n" +
                "  department,\n" +
                "  program,\n" +
                "  amount\n" +
                ")\n" +
                "VALUES\n" +
                "(\n" +
                "  5,\n" +
                "  2017,\n" +
                "  'Housing and Community Development',\n" +
                "  'Housing and Community Affairs',\n" +
                "  'Multi-Family Housing Programs',\n" +
                "  200\n" +
                ");\n";

        String sql4 ="INSERT INTO county_spending_detail\n" +
                "(\n" +
                "  fiscal_year_period,\n" +
                "  fiscal_year,\n" +
                "  service,\n" +
                "  department,\n" +
                "  program,\n" +
                "  amount\n" +
                ")\n" +
                "VALUES\n" +
                "(\n" +
                "  5,\n" +
                "  2017,\n" +
                "  'Transportation',\n" +
                "  'Transportation',\n" +
                "  'Administration',\n" +
                "  878\n" +
                ");\n";

        String sql5 = "INSERT INTO county_spending_detail\n" +
                "(\n" +
                "  fiscal_year_period,\n" +
                "  fiscal_year,\n" +
                "  service,\n" +
                "  department,\n" +
                "  program,\n" +
                "  amount\n" +
                ")\n" +
                "VALUES\n" +
                "(\n" +
                "  5,\n" +
                "  2017,\n" +
                "  'General Government',\n" +
                "  'Liquor Control',\n" +
                "  'Warehouse Operations',\n" +
                "  198459.06\n" +
                ");\n";

        String sql6 = "INSERT INTO county_spending_detail\n" +
                "(\n" +
                "  fiscal_year_period,\n" +
                "  fiscal_year,\n" +
                "  service,\n" +
                "  department,\n" +
                "  program,\n" +
                "  amount\n" +
                ")\n" +
                "VALUES\n" +
                "(\n" +
                "  6,\n" +
                "  2017,\n" +
                "  'Health and Human Services',\n" +
                "  'Health and Human Services',\n" +
                "  'Outpatient Behavioral Health Services - Adult',\n" +
                "  169.02\n" +
                ");\n" +
                "\n";

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            statement.addBatch(sql1);
            statement.addBatch(sql2);
            statement.addBatch(sql3);
            statement.addBatch(sql4);
            statement.addBatch(sql5);
            statement.addBatch(sql6);
            statement.executeBatch();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Convenience method to call
     *
     * @return
     */
    @Override
    public boolean healDatabaseEntirely() {
        //boolean isDatabaseDropped = dropDatabase();
        //boolean isDatabaseCreated = createDatabase();
        boolean isTableCreated = createTable();
        boolean isDataInserted = insertTestData();

        return isTableCreated && isDataInserted;
    }
}
