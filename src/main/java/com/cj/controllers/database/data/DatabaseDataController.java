package com.cj.controllers.database.data;

import com.cj.model.QueryResult;
import com.cj.model.select_statement.SelectStatement;
import com.cj.service.database.data.DatabaseDataService;
import com.cj.sql_builder.SqlBuilder;
import com.cj.sql_builder.SqlBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://querybuilder4j.net" })
@RequestMapping("/data")
public class DatabaseDataController {

    private DatabaseDataService databaseDataService;

    @Autowired
    public DatabaseDataController(DatabaseDataService databaseDataService) {
        this.databaseDataService = databaseDataService;
    }

    /**
     * Get a column's members.
     *
     * @param database The database of the column members to retrieve.
     * @param schema The schema of the column members to retrieve.
     * @param table The table of the column members to retrieve.
     * @param column The column of the column members to retrieve.
     * @param limit The maximum number of column members to retrieve (used for pagination).
     * @param offset The column member record number that the results should start at (used for pagination).
     * @param ascending Whether the query that retrieves the column members should be in ascending or descending order.
     * @param search The text that the column members should contain.
     * @return A ResponseEntity containing
     */
    @GetMapping(value = "/{database}/{schema}/{table}/{column}/column-member")
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
     * @param selectStatement The SelectStatement to build a SQL string for.
     * @return
     */
    @PostMapping(value = "/{database}/query")
    public ResponseEntity<QueryResult> getQueryResults(@PathVariable String database,
                                                       @RequestBody SelectStatement selectStatement) throws Exception {
        SqlBuilder sqlBuilder = SqlBuilderFactory.buildSqlBuilder(selectStatement);
        String sql = sqlBuilder.buildSql();

        QueryResult queryResult = databaseDataService.executeQuery(database, sql);

        return ResponseEntity.ok(queryResult);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleException(HttpServletRequest request, Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new HashMap<>().put("message", ex.getMessage()));
    }

}
