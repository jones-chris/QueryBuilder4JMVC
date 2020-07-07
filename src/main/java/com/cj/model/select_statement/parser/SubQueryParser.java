package com.cj.model.select_statement.parser;

import com.cj.dao.querytemplate.QueryTemplateDao;
import com.cj.model.select_statement.SelectStatement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class SubQueryParser {

    /**
     * A Map of the stmt's subqueries with the key being the subquery id (subquery0, subquery1, etc) and the value being
     * the subquery deserialized into a SelectStatement object.
     */
    protected Map<String, SelectStatement> unbuiltSubQueries = new HashMap<>();

    /**
     * A Map of the stmt's subqueries with the key being the subquery id (subquery0, subquery1, etc) and the value being
     * the SELECT SQL string generated from the SelectStatement object in the subQueries field of this class.
     */
    protected Map<String, String> builtSubQueries = new HashMap<>();

    /**
     * The SelectStatement that encapsulates the data to generate the SELECT SQL string.
     */
    protected SelectStatement stmt;

    private transient QueryTemplateDao queryTemplateDao;

    public SubQueryParser(SelectStatement stmt) throws Exception {
        this.stmt = stmt;

        // First, get all SelectStatements that are listed in subqueries.  Later we will replace the params in each subquery.
        // TODO:  this eager loads the subqueries.  It may be beneficial to consider having a class boolean field for lazy loading.
        if (! this.stmt.getSubQueries().isEmpty()) {
            this.stmt.getSubQueries().forEach((subQueryId, subQueryCall) -> {
                String subQueryName = subQueryCall.substring(0, subQueryCall.indexOf("("));
                SelectStatement queryTemplate = this.queryTemplateDao.findByName(subQueryName);

                if (queryTemplate == null) {
                    throw new RuntimeException(String.format("Could not find subquery named %s in queryTemplateDao", subQueryName));
                } else {
                    this.unbuiltSubQueries.put(subQueryId, queryTemplate);
                }
            });
        }

        buildSubQueries();
    }

    @Autowired
    public void setQueryTemplateDao(QueryTemplateDao queryTemplateDao) {
        this.queryTemplateDao = queryTemplateDao;
    }

    public Map<String, String> getBuiltSubQueries() {
        return builtSubQueries;
    }

    /**
     * Determines if all subqueries are built.
     * @return boolean
     */
    private boolean allSubQueriesAreBuilt() {
        for (String subquery : unbuiltSubQueries.keySet()) {
            if (! builtSubQueries.containsKey(subquery)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests if a String is a '$', which is the subquery qb4j expression.  If the String "$" is not at index 0 in the String, then
     * false.  Otherwise, true.
     * @param arg The arg to test whether it is a subquery.
     * @return boolean
     */
    public static boolean argIsSubQuery(String arg) {
        if (arg == null || arg.isEmpty()) {
            return false;
        } else {
            return 0 == arg.toLowerCase().indexOf("$");
        }
    }

    /**
     * Gets all of the subqueries that match the subQueryArgs.  The resulted Map is intended to be used to set a child
     * SelectStatement's subqueries property so that SQL can be generated correctly.
     *
     * @param subQueryArgs An array of subquery args in "param=arg" format.
     * @return Map<String, String>
     */
    private Map<String, String> getRelevantSubQueries(String[] subQueryArgs) {
        Map<String, String> relevantSubQueries = new HashMap<>();
        for (String paramAndArg : subQueryArgs) {
            String arg = paramAndArg.split("=")[1];
            if (argIsSubQuery(arg)) {
                String subQueryCall = this.stmt.getSubQueries().get(arg);
                relevantSubQueries.put(arg, subQueryCall);
            }
        }
        return relevantSubQueries;
    }

    /**
     * Convenience method for retrieving a subQuery SelectStatement by id, calling the SelectStatement setters (if needed),
     * calling toSql() to build the SelectStatement's SQL string, and putting the id and SQL string in the builtSubQueries
     * field.
     *
     * @param subQueryId The idea of the subquery.
     * @param subQueryName The name of the subquery.
     * @param subQueryArgs An array of subquery args in "param=arg" format.
     * @throws Exception If a subquery name cannot be found.
     */
    private void buildSubQuery(String subQueryId, String subQueryName, String[] subQueryArgs) throws Exception {
        SelectStatement stmt = unbuiltSubQueries.get(subQueryId);

        if (stmt == null) {
            String message = String.format("Could not find statement object with name:  %s", subQueryName);
            throw new Exception(message);
        } else {
            if (subQueryArgs.length != 0) {
                stmt.setCriteriaArguments(getSubQueryArgs(subQueryArgs));
                stmt.setSubQueries(getRelevantSubQueries(subQueryArgs));
            }

            String sql = stmt.toSql(this.stmt.getDatabaseMetaData().getProperties());
            builtSubQueries.put(subQueryId, sql);
        }
    }

    /**
     * Returns a Map of a subquery's arguments with the keys being the parameters and the values being the arguments.
     *
     * @param argsArray An array of subquery args in "param=arg" format.
     * @return Map<String, String>
     * @throws Exception If the argsArray parameter is not in "param=arg" format.
     */
    private Map<String, String> getSubQueryArgs(String[] argsArray) throws Exception {
        Map<String, String> args = new HashMap<>();
        for (String paramNameAndArgString : argsArray) {
            if (! paramNameAndArgString.contains("=")) {
                String message = String.format("'%s' is not formatted properly.  It should be 'paramName=argument", paramNameAndArgString);
                throw new Exception(message);
            } else {
                String[] paramAndArgArray = paramNameAndArgString.split("=");
                args.put(paramAndArgArray[0], paramAndArgArray[1]);
            }
        }

        return args;
    }

    /**
     * This method controls building subqueries.
     *
     * The overall flow is that each subquery in this.stmt.subQueries, which contains the
     * raw query name call and arguments, is retrieved using this.queryTemplateDao and deserialized into a SelectStatement,
     * which is added to this.unbuiltSubQueries to await being built.
     *
     * Then, each subquery in this.unbuiltSubQueries is
     * built by calling the toSql() method on each subquery because they are each a SelectStatement object.  When a subquery is
     * built, the resulting SELECT SQL string is added to this.builtSubQueries.
     *
     * Lastly, this.builtSubQueries is referenced by the this.createWhereClause() method to create the WHERE clause of the
     * SELECT SQL string.
     *
     * @throws Exception If the index of "(", ")", or ";" cannot be found.
     */
    protected void buildSubQueries() throws Exception {
        while (! allSubQueriesAreBuilt()) {
            for (Map.Entry<String, String> subQuery : this.stmt.getSubQueries().entrySet()) {
                String subQueryId = subQuery.getKey();
                String subQueryName = subQuery.getValue().substring(0, subQuery.getValue().indexOf("("));
                String[] subQueryArgs = subQuery.getValue().substring(subQuery.getValue().indexOf("(") + 1, subQuery.getValue().indexOf(")")).split(";");

                // If there are no args, then there will be one element in subQueryArgs and it will be an empty string.
                if (subQueryArgs.length == 1 && subQueryArgs[0].equals("")) {
                    subQueryArgs = new String[0];
                }

                if (! builtSubQueries.containsKey(subQueryId)) {
                    buildSubQuery(subQueryId, subQueryName, subQueryArgs);
                }
            }

        }
    }

}
