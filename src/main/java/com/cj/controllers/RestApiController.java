package com.cj.controllers;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.cj.service.*;
import com.cj.utils.Converter;
import com.google.gson.Gson;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
public class RestApiController {
    @Autowired
    private LoggingService loggingService;
    @Autowired
    private DatabaseAuditService databaseAuditService;
    @Autowired
    private DatabaseHealerService databaseHealerService;
    @Autowired
    private DatabaseMetaDataService databaseMetaDataService;
    @Autowired
    private QueryTemplateService queryTemplateService;
    @Autowired
    private AmazonSNS snsClient;
    @Qualifier("querybuilder4jdb_properties")
    @Autowired
    private Properties queryBuilder4JDatabaseProperties;

    /**
     * Gets the query template names given a limit, offset, and and ordering (ascending vs descending).
     *
     * @param limit
     * @param offset
     * @param ascending
     * @return
     */
    @RequestMapping(value = "/queryTemplates", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getQueryTemplates(@RequestParam(required = false) Integer limit,
                                                    @RequestParam(required = false) Integer offset,
                                                    @RequestParam(required = false) boolean ascending) {
        try {
            String jsonResults = queryTemplateService.getNames(limit, offset, ascending);
            return new ResponseEntity<>(jsonResults, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a query template by a unique name.
     *
     * @param name
     * @return
     */
    @RequestMapping(value = "/queryTemplates/{name}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getQueryTemplateById(@PathVariable String name) {
        try {
            String queryTemplate = queryTemplateService.findByName(name);
            return new ResponseEntity<>(queryTemplate, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Save a SelectStatement.
     *
     * @param selectStatement
     * @param name
     * @return
     */
    @RequestMapping(value = "/saveQueryTemplate/{name}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> saveQueryTemplate(SelectStatement selectStatement,
                                                    @PathVariable(value = "name") String name) {
        try {
            selectStatement.setName(name);

            // Set the statement's criteria parameters if not done so already.
            selectStatement.getCriteria().forEach((criterion) -> {
                if (criterion.filter.charAt(0) == '@') {
                    selectStatement.getCriteriaParameters().put(criterion.filter.substring(1), "Description placeholder");
                }
            });

            Gson gson = new Gson();
            String json = gson.toJson(selectStatement);

            queryTemplateService.save(name, json);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns all database schemas.
     *
     * @return
     */
    @RequestMapping(value = "/schemas", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getSchemas() {
        try {
            String schemasJson = databaseMetaDataService.getSchemas();
            return new ResponseEntity<>(schemasJson, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns all database tables and views.
     *
     * @param schema
     * @return
     */
    @RequestMapping(value = "/tablesAndViews/{schema}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getTablesAndViews(@PathVariable(value = "schema", required = true) String schema) {
        try {
            schema = (schema.equals("null")) ? null : schema;
            String tablesJson = databaseMetaDataService.getTablesAndViews(schema);
            return new ResponseEntity<>(tablesJson, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns all columns for any number of tables or views given a schema name and table/view name.
     *
     * @param schema
     * @param tables
     * @return
     */
    @RequestMapping(value = "/columns/{schema}/{tables}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getColumns(@PathVariable String schema,
                                             @PathVariable String tables)  {
        try {
            schema = (schema.equals("null")) ? null : schema;

            String[] splitTables = tables.split("&");

            Map<String, Integer> columnsMap = new HashMap<>();
            for (String table : splitTables) {
                Map<String, Integer> columns = databaseMetaDataService.getColumns(schema, table);
                columnsMap.putAll(columns);
            }

            String columnsJson = Converter.convertToJSON(columnsMap.keySet().toArray(), "column").toString();
            return new ResponseEntity<>(columnsJson, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    @RequestMapping(value = "/columns-members/{schema}/{table}/{column}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getColumnMembers(@PathVariable String schema,
                                                   @PathVariable String table,
                                                   @PathVariable String column,
                                                   @RequestParam int limit,
                                                   @RequestParam int offset,
                                                   @RequestParam boolean ascending,
                                                   @RequestParam(required = false) String search) {
        try {
            String jsonResults = databaseMetaDataService.getColumnMembers(schema, table, column, limit, offset, ascending, search);
            return new ResponseEntity<>(jsonResults, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Execute a SelectStatement, audits the database for any unexpected changes, heals the database if necessary, publishes
     * a request to an SNS topic (if the database needed to be healed), and returns the query's results.
     *
     * @param selectStatement
     * @return
     */
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> getQueryResults(SelectStatement selectStatement) {
        try {
            String sql = selectStatement.toSql(queryBuilder4JDatabaseProperties);
            String queryResults = databaseMetaDataService.executeQuery(sql);

            // Log the SelectStatement and the database audit results to logging.db
            Map<String, Boolean> databaseAuditResults = databaseAuditService.runAllChecks(3, 1, new String[1], 1);
            loggingService.add(selectStatement, sql, databaseAuditResults);

            Map<String, Runnable> healerFunctions;
            if (databaseAuditResults.values().contains(false)) {
                // Publish message to SNS topic to alert interested parties.
                try {
                    publishSnsMessage();
                } catch (Exception ex) {
                    // Todo:  Log the exception and SelectStatement.toString().
                }

                // Heal database so it's ready for next request.
                healerFunctions = buildHealerFunctionsMap();
                databaseAuditResults.forEach((key, passedCheck) -> {
                    if (! passedCheck) {
                        healerFunctions.get(key).run();
                    }
                });
            }

            // Create JSON string to be added to response body.  JSON string includes query results, selectStatement in SQL format,
            // and database audit results.
            JSONObject jsonObject = new JSONObject(databaseAuditResults);
            jsonObject.append("queryResults", queryResults);
            jsonObject.append("sqlResult", sql);

            return new ResponseEntity<>(jsonObject.toString(4), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Builds a Map of functions that should be run if a certain check (the key) is false.
     *
     * @return
     */
    private Map<String, Runnable> buildHealerFunctionsMap() {
        Map<String, Runnable> healerFunctions = new HashMap<>();

        healerFunctions.put("databaseExists", () -> {
            databaseHealerService.createDatabase();
            databaseHealerService.createTable();
            databaseHealerService.insertTestData();
        });
        healerFunctions.put("tableExists", () -> {
            databaseHealerService.createTable();
            databaseHealerService.insertTestData();
        });
        healerFunctions.put("tablesAreSame", () -> {
            databaseHealerService.dropAllTablesExcept("county_spending_detail");
        });
        healerFunctions.put("numOfTableColumnsAreSame", () -> {
            databaseHealerService.dropTable();
            databaseHealerService.createTable();
            databaseHealerService.insertTestData();
        });
        healerFunctions.put("numOfTableRowsAreSame", () -> {
            databaseHealerService.dropTable();
            databaseHealerService.createTable();
            databaseHealerService.insertTestData();
        });
        healerFunctions.put("tableDataIsSame", () -> {
            databaseHealerService.dropTable();
            databaseHealerService.createTable();
            databaseHealerService.insertTestData();
        });

        return healerFunctions;
    }

    /**
     * Wrapper method for publishing a message to SNS.  This method includes a throws clause in the signature header
     * to call attention to the fact that the SNS client could throw various Exception child classes.
     *
     * @throws Exception
     */
    private void publishSnsMessage() throws Exception {
        String snsMessage = "A query was submitted that caused a database audit result to fail. Please " +
                "check the S3 logs for more information.";
        String arn = "arn:aws:sns:us-east-1:526661363425:qb4j-sql-injection-success";
        PublishRequest publishRequest = new PublishRequest(arn, snsMessage);
        snsClient.publish(publishRequest);
    }
}
