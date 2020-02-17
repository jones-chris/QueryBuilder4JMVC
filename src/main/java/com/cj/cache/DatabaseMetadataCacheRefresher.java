package com.cj.cache;

import com.cj.model.Column;
import com.cj.model.Schema;
import com.cj.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableScheduling
public class DatabaseMetadataCacheRefresher {

    private DatabaseMetadataCacheDaoImpl databaseMetadataCacheDao;

    @Autowired
    public DatabaseMetadataCacheRefresher(DatabaseMetadataCacheDaoImpl databaseMetadataCacheDao) {
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
    }

    @Scheduled(fixedDelay = 1000)
    public void refreshDatabaseMetadataCache() {
        try {
            System.out.println("Starting the refreshDatabaseMetadataCache method");

            // Get schemas
            List<Schema> schemas = databaseMetadataCacheDao.getSchemas();

            // Get tables
            List<Table> tables = new ArrayList<>();
            if (schemas.isEmpty()) { // Some databases don't have schemas, like SQLite.
                tables = databaseMetadataCacheDao.getTablesAndViews(null);
            } else {
                for (Schema schema : schemas) {
                    tables.addAll(databaseMetadataCacheDao.getTablesAndViews(schema.getSchemaName()));
                }
            }

            // Get columns and data types
            List<Column> columns = new ArrayList<>();
            for (Table table : tables) {
                columns.addAll(databaseMetadataCacheDao.getColumns(table.getSchemaName(), table.getTableName()));
            }

            // Save columns to cache
            databaseMetadataCacheDao.saveColumns(columns);

            System.out.println("Finished getting target database's schemas, tables, and columns");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
