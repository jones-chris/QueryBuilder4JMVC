(function () {
    var myConnector = tableau.makeConnector();

    var jsonData = {
        "name": "NullFilter",
        "databaseType": "Sqlite",
        "columns": [
          "county_spending_detail.fiscal_year",
          "county_spending_detail.amount",
          "county_spending_detail.service"
        ],
        "table": "county_spending_detail",
        "criteria": [],
        "joins": [],
        "distinct": false,
        "groupBy": false,
        "orderBy": false,
        "ascending": false,
        "suppressNulls": false,
        "subQueries": {},
        "criteriaArguments": {},
        "criteriaParameters": []
      };

    myConnector.getSchema = function (schemaCallback) {
      $.ajax({
        type: "POST",
        url: "http://localhost:8080/tableau-wdc-types",
        contentType: "application/json",
        data: JSON.stringify({
          "columns": jsonData.columns
        }),
        dataType: 'json',
        success: function(resp){
          console.log('inside GET schema callback');
          var tableSchema = resp;
          schemaCallback([tableSchema]);
        }
      });

    };

    myConnector.getData = function (table, doneCallback) {
      $.ajax({
        type: "POST",
        url: "http://localhost:8080/query",
        contentType: "application/json",
        data: JSON.stringify(jsonData),
        dataType: 'json',
        success: function(resp){
          console.log('inside getData callback');
          
          var data = JSON.parse(resp.queryResults[0]);
          var tableData = [];
          for (var i=0; i<data.length; i++) {
            tableData.push(data[i]);
          }

          table.appendRows(tableData);
          doneCallback();
        }
      });
    };

    tableau.registerConnector(myConnector);

    $(document).ready(function () {
      $("#submitButton").click(function () {
        tableau.connectionName = "qb4j test";
        tableau.submit();
      });
    });

})();
