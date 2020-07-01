package com.cj.controllers;

import com.cj.config.Qb4jConfig;
import com.cj.model.*;
import com.cj.service.database.audit.DatabaseAuditService;
import com.cj.service.database.data.DatabaseDataService;
import com.cj.service.database.healer.DatabaseHealerService;
import com.cj.service.database.metadata.DatabaseMetaDataService;
import com.cj.service.querytemplate.QueryTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.querybuilder4j.databasemetadata.QueryTemplateDao;
import com.querybuilder4j.statements.SelectStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@CrossOrigin(origins = { "http://localhost:3000", "http://querybuilder4j.net" })
@RestController
public class RestApiController {
    private DatabaseMetaDataService databaseMetaDataService;
    private QueryTemplateService queryTemplateService;
    private QueryTemplateDao queryTemplateDao;  // todo:  can this be a prebuilt class from qb4j and connection properties are just passed or injected into it?
    private DatabaseDataService databaseDataService;
    private Qb4jConfig qb4jConfig;

    @Autowired
    public RestApiController(DatabaseMetaDataService databaseMetaDataService,
                             QueryTemplateService queryTemplateService,
                             QueryTemplateDao queryTemplateDao,
                             DatabaseDataService databaseDataService,
                             Qb4jConfig qb4jConfig) {
        this.databaseMetaDataService = databaseMetaDataService;
        this.queryTemplateService = queryTemplateService;
        this.queryTemplateDao = queryTemplateDao;
        this.databaseDataService = databaseDataService;
        this.qb4jConfig = qb4jConfig;
    }

    /**
     *
     */
    @GetMapping(value = "/metadata/database")
    public ResponseEntity<List<Database>> getDatabases() {
        List<Database> databases = qb4jConfig.getTargetDataSources().stream()
                .map(targetDataSource -> new Database(targetDataSource.getName(), targetDataSource.getDatabaseType()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(databases);
    }

    /**
     * Gets the query template names given a limit, offset, and and ordering (ascending vs descending).
     *
     * @param limit
     * @param offset
     * @param ascending
     * @return
     */
    @GetMapping(value = "/query-template")
    public ResponseEntity<List<String>> getQueryTemplates(@RequestParam(required = false) Integer limit,
                                                          @RequestParam(required = false) Integer offset,
                                                          @RequestParam(required = false) boolean ascending) throws Exception {
        List<String> queryTemplateNames = queryTemplateService.getNames(limit, offset, ascending);
        return ResponseEntity.ok(queryTemplateNames);
    }

    /**
     * Get a query template by a unique name.
     *
     * @param name
     * @return
     */
    @GetMapping(value = "/query-template/{name}")
    public ResponseEntity<SelectStatement> getQueryTemplateById(@PathVariable String name) {
        SelectStatement queryTemplate = queryTemplateService.findByName(name);
        return ResponseEntity.ok(queryTemplate);
    }

    /**
     * Save a SelectStatement.
     *
     * @param selectStatement
     * @return
     */
    @PostMapping(value = "/query-template")
    public ResponseEntity<?> saveQueryTemplate(@RequestBody SelectStatement selectStatement) {
        if (selectStatement.getName() == null) {
            throw new RuntimeException("The name of the select statement cannot be null when saving the statement " +
                    "as a query template");
        }

        Gson gson = new Gson();
        String json = gson.toJson(selectStatement);

        queryTemplateService.save(selectStatement.getName(), json);

        return ResponseEntity.ok().build();
    }

    /**
     * Returns all database schemas that a given user has access to.
     *
     * @return
     */
    @GetMapping(value = "/metadata/{database}/schema")
    public ResponseEntity<List<Schema>> getSchemas(@PathVariable String database) throws Exception {
        List<Schema> schemas = databaseMetaDataService.getSchemas(database);
        return ResponseEntity.ok(schemas);
    }

    /**
     * Returns all database tables and views that a given user has access to.
     *
     * @param schemas
     * @return
     */
    @GetMapping(value = "/metadata/{database}/{schemas}/table-and-view")
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
     * @param schema
     * @param tables
     * @return
     */
    @PostMapping(value = "/metadata/database/schema/table/column")
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

    /**
     * Get a column's members.
     *
     * @param schema
     * @param table
     * @param column
     * @param limit
     * @param offset
     * @param ascending
     * @param search
     * @return
     */
    @GetMapping(value = "/data/{database}/{schema}/{table}/{column}/column-member")
    public ResponseEntity<String> getColumnMembers(@PathVariable String database,
                                                   @PathVariable String schema,
                                                   @PathVariable String table,
                                                   @PathVariable String column,
                                                   @RequestParam int limit,
                                                   @RequestParam int offset,
                                                   @RequestParam boolean ascending,
                                                   @RequestParam(required = false) String search) throws Exception {
        String jsonResults = databaseDataService.getColumnMembers(database, schema, table, column, limit, offset, ascending, search);
        return new ResponseEntity<>(jsonResults, HttpStatus.OK);
    }

    /**
     * Execute a SelectStatement, audits the database for any unexpected changes, heals the database if necessary, publishes
     * a request to an SNS topic (if the database needed to be healed), and returns the query's results.
     *
     * @param selectStatement
     * @return
     */
    @PostMapping(value = "/data/{database}/query")
    public ResponseEntity<QueryResult> getQueryResults(@PathVariable String database,
                                                       @RequestBody SelectStatement selectStatement) throws Exception {
        selectStatement.setQueryTemplateDao(queryTemplateDao);
        Properties properties = this.qb4jConfig.getTargetDataSource(database).getProperties();
        String sql = selectStatement.toSql(properties);
        QueryResult queryResult = databaseDataService.executeQuery(database, sql);

        return ResponseEntity.ok(queryResult);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleException(HttpServletRequest request, Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new HashMap<>().put("message", ex.getMessage()));
    }

}
