package com.cj.controllers.database.metadata;

import com.cj.config.Qb4jConfig;
import com.cj.model.Column;
import com.cj.model.Database;
import com.cj.model.Schema;
import com.cj.model.Table;
import com.cj.service.database.metadata.DatabaseMetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://querybuilder4j.net" })
@RequestMapping("/metadata")
public class DatabaseMetadataController {

    private DatabaseMetaDataService databaseMetaDataService;
    private Qb4jConfig qb4jConfig;

    @Autowired
    public DatabaseMetadataController(DatabaseMetaDataService databaseMetaDataService,
                                      Qb4jConfig qb4jConfig) {
        this.databaseMetaDataService = databaseMetaDataService;
        this.qb4jConfig = qb4jConfig;
    }

    /**
     * Returns all QueryBuilder4J target data sources.
     *
     * @return A ResponseEntity containing a List of Database objects.
     */
    @GetMapping(value = "/database")
    public ResponseEntity<List<Database>> getDatabases() {
        List<Database> databases = qb4jConfig.getTargetDataSources().stream()
                .map(targetDataSource -> new Database(targetDataSource.getName(), targetDataSource.getDatabaseType()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(databases);
    }

    /**
     * Returns all database schemas that a given user has access to.
     *
     * @return A ResponseEntity containing a List of Schema objects.
     */
    @GetMapping(value = "/{database}/schema")
    public ResponseEntity<List<Schema>> getSchemas(@PathVariable String database) throws Exception {
        List<Schema> schemas = databaseMetaDataService.getSchemas(database);
        return ResponseEntity.ok(schemas);
    }

    /**
     * Returns all database tables and views that a given user has access to.
     *
     * @param database The database of the tables and views to retrieve.
     * @param schemas The schemas of the tables and views to retrieve.
     * @return A ResponseEntity containing a List of Table objects.
     */
    @GetMapping(value = "/{database}/{schemas}/table-and-view")
    public ResponseEntity<List<Table>> getTablesAndViews(@PathVariable String database,
                                                         @PathVariable String schemas) throws Exception {
        String[] splitSchemas = schemas.split("&");
        List<Table> allTables = new ArrayList<>();
        for (String schema : splitSchemas) {
            List<Table> tables = databaseMetaDataService.getTablesAndViews(database, schema);
            allTables.addAll(tables);
        }

        return ResponseEntity.ok(allTables);
    }

    /**
     * Returns all columns for any number of tables or views given a schema name and table/view name (user permissions apply).
     *
     * @param tables A List of Table objects for which to retrieve columns
     * @return A ResponseEntity containing a List of Column objects.
     */
    @PostMapping(value = "/database/schema/table/column")
    public ResponseEntity<List<Column>> getColumns(@RequestBody List<Table> tables) throws Exception {
        List<Column> allColumns = new ArrayList<>();
        // todo:  instead  of making a cache trip for each table, make cache SQL include WHERE IN clause?
        for (Table table : tables) {
            String databaseName = table.getDatabaseName();
            String schemaName = table.getSchemaName();
            String tableName = table.getTableName();

            List<Column> columns = databaseMetaDataService.getColumns(databaseName, schemaName, tableName);
            allColumns.addAll(columns);
        }

        return ResponseEntity.ok(allColumns);
    }

}
