package com.cj.controllers;

import com.cj.config.Constants;
import com.cj.config.Qb4jConfig;
import com.cj.model.Column;
import com.cj.model.Schema;
import com.cj.model.Table;
import com.cj.service.database.audit.DatabaseAuditService;
import com.cj.service.database.data.DatabaseDataService;
import com.cj.service.database.healer.DatabaseHealerService;
import com.cj.service.database.metadata.DatabaseMetaDataService;
import com.cj.service.querytemplate.QueryTemplateService;
import com.cj.utils.TableauColumnSchema;
import com.cj.utils.TableauColumns;
import com.cj.utils.TableauTableSchema;
import com.google.gson.Gson;
import com.querybuilder4j.databasemetadata.QueryTemplateDao;
import com.querybuilder4j.statements.SelectStatement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class RestApiController {
    private DatabaseAuditService databaseAuditService;
    private DatabaseHealerService databaseHealerService;
    private DatabaseMetaDataService databaseMetaDataService;
    private QueryTemplateService queryTemplateService;
    private QueryTemplateDao queryTemplateDao;  // todo:  can this be a prebuilt class from qb4j and connection properties are just passed or injected into it?
    private DatabaseDataService databaseDataService;
    private Qb4jConfig qb4jConfig;

    @Autowired
    public RestApiController(DatabaseAuditService databaseAuditService,
                             DatabaseHealerService databaseHealerService,
                             DatabaseMetaDataService databaseMetaDataService,
                             QueryTemplateService queryTemplateService,
                             QueryTemplateDao queryTemplateDao,
                             DatabaseDataService databaseDataService,
                             Qb4jConfig qb4jConfig) {
        this.databaseAuditService = databaseAuditService;
        this.databaseHealerService = databaseHealerService;
        this.databaseMetaDataService = databaseMetaDataService;
        this.queryTemplateService = queryTemplateService;
        this.queryTemplateDao = queryTemplateDao;
        this.databaseDataService = databaseDataService;
        this.qb4jConfig = qb4jConfig;
    }

    /**
     * Gets the query template names given a limit, offset, and and ordering (ascending vs descending).
     *
     * @param limit
     * @param offset
     * @param ascending
     * @return
     */
    @GetMapping(value = "/queryTemplates")
    public ResponseEntity<String> getQueryTemplates(@RequestParam(required = false) Integer limit,
                                                    @RequestParam(required = false) Integer offset,
                                                    @RequestParam(required = false) boolean ascending) throws Exception {
        String jsonResults = queryTemplateService.getNames(limit, offset, ascending);
        return new ResponseEntity<>(jsonResults, HttpStatus.OK);
    }

    /**
     * Get a query template by a unique name.
     *
     * @param name
     * @return
     */
    @GetMapping(value = "/queryTemplates/{name}")
    public ResponseEntity<String> getQueryTemplateById(@PathVariable String name) {
        String queryTemplate = queryTemplateService.findByName(name);
        return new ResponseEntity<>(queryTemplate, HttpStatus.OK);
    }

    /**
     * Save a SelectStatement.
     *
     * @param selectStatement
     * @return
     */
    @PostMapping(value = "/saveQueryTemplate")
    public ResponseEntity<String> saveQueryTemplate(@RequestBody SelectStatement selectStatement) {
        if (selectStatement.getName() == null) {
            throw new RuntimeException("The name of the select statement cannot be null when saving the statement " +
                    "as a query template");
        }

        Gson gson = new Gson();
        String json = gson.toJson(selectStatement);

        queryTemplateService.save(selectStatement.getName(), json);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Returns all database schemas that a given user has access to.
     *
     * @return
     */
    @GetMapping(value = "/metadata/{database}/schemas")
    public ResponseEntity<List<Schema>> getSchemas(@PathVariable String database) throws Exception {
        List<Schema> schemas = databaseMetaDataService.getSchemas(database);
        return ResponseEntity.ok(schemas);
    }

    /**
     * Returns all database tables and views that a given user has access to.
     *
     * @param schema
     * @return
     */
    @GetMapping(value = "/metadata/{database}/{schema}/tables-and-views")
    public ResponseEntity<List<Table>> getTablesAndViews(@PathVariable String database,
                                                         @PathVariable String schema) throws Exception {
        List<Table> tables = databaseMetaDataService.getTablesAndViews(database, schema);
        return ResponseEntity.ok(tables);
    }

    /**
     * Returns all columns for any number of tables or views given a schema name and table/view name (user permissions apply).
     *
     * @param schema
     * @param tables
     * @return
     */
    @GetMapping(value = "/metadata/{database}/{schema}/{tables}/columns")
    public ResponseEntity<List<Column>> getColumns(@PathVariable String database,
                                             @PathVariable String schema,
                                             @PathVariable String tables) throws Exception {
        String[] splitTables = tables.split("&");

        List<Column> allColumns = new ArrayList<>();
        // todo:  instead  of making a cache trip for each table, make cache SQL include WHERE IN clause?
        for (String table : splitTables) {
            List<Column> columns = databaseMetaDataService.getColumns(database, schema, table);
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
    @GetMapping(value = "/data/{database}/{schema}/{table}/{column}/column-members")
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
    public ResponseEntity<String> getQueryResults(@PathVariable String database,
                                                  @RequestBody SelectStatement selectStatement) throws Exception {
        selectStatement.setQueryTemplateDao(queryTemplateDao);
        Properties properties = this.qb4jConfig.getTargetDataSource(database).getProperties();
        String sql = selectStatement.toSql(properties);
        String queryResults = databaseDataService.executeQuery(database, sql);

//        JSONObject jsonObject = getDatabaseAuditResults(database, selectStatement, sql);
        JSONObject jsonObject = new JSONObject();
        jsonObject.append("queryResults", queryResults);
        jsonObject.append("sqlResult", sql);

        return new ResponseEntity<>(jsonObject.toString(4), HttpStatus.OK);
    }

//    @PostMapping("/{database}/tableau-wdc-types")
//    public ResponseEntity<String> getColumnDataTypes(@PathVariable String database,
//                                                     @RequestBody TableauColumns columns) throws SQLException {
//        List<String> tables = new ArrayList<>();
//        columns.getColumns().forEach((column) -> {
//            String table = column.split("\\.")[0];
//            if (! tables.contains(table)) { tables.add(table); }
//        });
//
//        List<Column> allColumns = new ArrayList<>();
//        for (String table : tables) {
//            List<Column> tableMetaData = databaseMetaDataService.getColumns(database,null, table);
//            allColumns.addAll(tableMetaData);
//        }
//
//        List<TableauColumnSchema> columnSchemas = new ArrayList<>();
//        for (String column : columns.getColumns()) {
//            Integer sqlType = sqlTypes.get(column);
//            String tableType = Constants.tableauDataTypeMappings.get(sqlType);
//            columnSchemas.add(new TableauColumnSchema(column.split("\\.")[1], tableType));
//        }
//        TableauTableSchema tableauTableSchema = new TableauTableSchema("qb4j", "my alias", columnSchemas);
//
//        return ResponseEntity.ok(new Gson().toJson(tableauTableSchema));
//    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, Exception ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private JSONObject getDatabaseAuditResults(String databaseName, SelectStatement selectStatement, String sql) {
//      Log the SelectStatement and the database audit results to logging.db
        Map<String, Boolean> databaseAuditResults = databaseAuditService.runAllChecks(databaseName);

//      Set QueryTemplateDao to null before persisting SelectStatement.
        selectStatement.setQueryTemplateDao(null);

        Map<String, Runnable> healerFunctions;
        if (databaseAuditResults.values().contains(false)) {
            // Publish message to SNS topic to alert interested parties.
//                try {
//                    publishSnsMessage();
//                } catch (Exception ex) {
//                    // Todo:  Log the exception and SelectStatement.toString().
//                }

            // Heal database so it's ready for next request.
            healerFunctions = buildHealerFunctionsMap(databaseName);
            databaseAuditResults.forEach((key, passedCheck) -> {
                if (! passedCheck) {
                    healerFunctions.get(key).run();
                }
            });
        }

        return new JSONObject(databaseAuditResults);
    }

    /**
     * Builds a Map of functions that should be run if a certain check (the key) is false.
     *
     * @return
     */
    private Map<String, Runnable> buildHealerFunctionsMap(String databaseName) {
        Map<String, Runnable> healerFunctions = new HashMap<>();

        healerFunctions.put("databaseExists", () -> {
            databaseHealerService.createDatabase();
            databaseHealerService.createTable(databaseName);
            databaseHealerService.insertTestData(databaseName);
        });
        healerFunctions.put("tableExists", () -> {
            databaseHealerService.createTable(databaseName);
            databaseHealerService.insertTestData(databaseName);
        });
        healerFunctions.put("tablesAreSame", () -> {
            databaseHealerService.dropAllTablesExcept(databaseName, "county_spending_detail");
        });
        healerFunctions.put("numOfTableColumnsAreSame", () -> {
            databaseHealerService.dropTable(databaseName);
            databaseHealerService.createTable(databaseName);
            databaseHealerService.insertTestData(databaseName);
        });
        healerFunctions.put("numOfTableRowsAreSame", () -> {
            databaseHealerService.dropTable(databaseName);
            databaseHealerService.createTable(databaseName);
            databaseHealerService.insertTestData(databaseName);
        });
        healerFunctions.put("tableDataIsSame", () -> {
            databaseHealerService.dropTable(databaseName);
            databaseHealerService.createTable(databaseName);
            databaseHealerService.insertTestData(databaseName);
        });

        return healerFunctions;
    }

    /**
     * Wrapper method for publishing a message to SNS.  This method includes a throws clause in the signature header
     * to call attention to the fact that the SNS client could throw various Exception child classes.
     *
     * @throws Exception
     */
//    private void publishSnsMessage() throws Exception {
//        String snsMessage = "A query was submitted that caused a database audit result to fail. Please " +
//                "check the S3 logs for more information.";
//        String arn = "arn:aws:sns:us-east-1:526661363425:qb4j-sql-injection-success";
//        PublishRequest publishRequest = new PublishRequest(arn, snsMessage);
//        snsClient.publish(publishRequest);
//    }
}
