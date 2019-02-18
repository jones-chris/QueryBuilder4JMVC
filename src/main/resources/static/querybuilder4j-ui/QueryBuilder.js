const scriptVariables = {
    // The base URL where the join png images are held.  Set the value to 'https://s3.amazonaws.com/qb4j-ui/images/' for official qb4j images.
    joinImageBaseURL: 'https://s3.amazonaws.com/qb4j-ui/images/',
    // Set contents of array to any of the following: 'queryTemplatesDiv', 'schemasDiv', 'tablesDiv', 'joinsDiv', 'columnsDiv',
    // 'criteriaDiv', or 'otherOptionsDiv' in order to show these divs when the page is rendered.
    landingDivs : ['schemasDiv', 'tablesDiv'],
    // Always leave as null.
    columnMembersWindow : null,
    // set to the number of milliseconds that should elapse when hiding/showing the various divs.
    phaseOutMilliseconds : 200,
    // set to your query template endpoint
    getQueryTemplateEndpoint : null,
    // set to your schemas endpoint
    getSchemaEndpoint : "/schemas",
    // set to your tables endpoint
    getTablesEndpoint : "/tablesAndViews/",
    // set to your table columns endpoint
    getColumnsEndpoint : "/columns/",
    // set to your query endpoint
    formSubmissionEndpoint : "/query",
    // set to the HTTP method that your query endpoint above accepts
    formMethod : "POST",
    // assign a function here to handle form submission.
    formSubmissionFunction : function () {
        $.ajax({
            type: 'POST',
            url: scriptVariables['formSubmissionEndpoint'],
            data: buildRequestData(),
            success: function (data) {
                console.log(data);
                document.getElementById('ajaxError').innerHTML = null;
                document.getElementById('queryResults').innerHTML = data.queryResults[0];
                document.getElementById('sqlResult').innerHTML = data.sqlResult[0];
                document.getElementById('databaseExists').innerHTML = data.databaseExists;
                document.getElementById('tableExists').innerHTML = data.tableExists;
                document.getElementById('numOfTablesIsSame').innerHTML = data.tablesAreSame;
                document.getElementById('numOfColumnsIsSame').innerHTML = data.numOfTableColumnsAreSame;
                document.getElementById('numOfRowsIsSame').innerHTML = data.numOfTableRowsAreSame;
                document.getElementById('tableDataIsSame').innerHTML = data.tableDataIsSame;
            },
            error: function (textStatus, errorThrown) {
                console.log(textStatus);
                console.log(errorThrown);
                document.getElementById('ajaxError').innerHTML = errorThrown + ':  ' + textStatus.responseText;
                document.getElementById('queryResults').innerHTML = null;
                document.getElementById('sqlResult').innerHTML = null;
                document.getElementById('databaseExists').innerHTML = null;
                document.getElementById('tableExists').innerHTML = null;
                document.getElementById('numOfTablesIsSame').innerHTML = null;
                document.getElementById('numOfColumnsIsSame').innerHTML = null;
                document.getElementById('numOfRowsIsSame').innerHTML = null;
                document.getElementById('tableDataIsSame').innerHTML = null;
            },
            dataType: 'json'
        });
    },
    // set to [] to render
    queryTemplates : null,
    // set to [] to render
    schemas : [],
    // set to [] to render
    tables : [],
    // set to true to render
    allowJoins : true,
    // set to [] to render
    availableColumns : [],
    // set to [] to render
    selectedColumns : [],
    // set to [] to render
    criteria : [],
    // set to true to render
    distinct : true,
    // set to [] to render
    orderByColumns : null,
    // set to [] to render
    groupByColumns : null,
    //set to true to render
    suppressNulls : true,
    // set to [] to render
    limitChoices : [5, 10, 50, 500],
    // set to [] to render
    offsetChoices : [5, 10, 50, 500],
    queryTemplatesSize : 5,
    schemasSize : 5,
    tablesSize : 5,
    availableColumnsSize : 30,
    selectedColumnsSize : 30,
    orderByColumnsSize : 10,
    groupByColumnsSize : 10
}

function sendAjaxRequest(endpoint, paramString, method, callbackFunction) {
    $.ajax({
        method: method,
        url: endpoint,
        data: paramString,
        success: function(responseText) {
            callbackFunction(responseText);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            alert(jqXHR);
            alert(textStatus);
            alert(errorThrown);
        },
        dataType: 'json'
    });
}

function setColumnMembersWindow() {
    scriptVariables['columnMembersWindow'] = new ColumnMembers();
}

function showColumnMembersWindow() {
    var windowProps = "toolbar=no,menubar=no,location=no,resizable=yes,scrollbars=yes,status=no,dependent=yes";
    window.open("/ColumnMembers.html", "columnMembers", windowProps);
}

function getQueryTemplates() {
    sendAjaxRequest(scriptVariables['getQueryTemplateEndpoint'],
        null,
        "GET",
        function(queryTemplatesData) {
            fillArrayProperty('queryTemplates', queryTemplatesData);
            syncSelectOptionsWithDataModel('queryTemplates', scriptVariables['queryTemplates']);
        });
}

function getQueryTemplatById(id) {
    sendAjaxRequest(scriptVariables['getQueryTemplateEndpoint'] + '/' + id,
        null,
        "GET",
        function(queryTemplateData) {
            alert('Query Template retrieved:  ' + queryTemplateData);
            //TODO write logic to populate query template's data on screen.

            // update each array variable based on queryTemplateData.

            // call syncSelectOptionsWithData for each array variable.
        });
}

function getSchemas() {
    sendAjaxRequest(scriptVariables['getSchemaEndpoint'],
        null,
        "GET",
        function(schemasData) {
            fillArrayProperty('schemas', schemasData);
            syncSelectOptionsWithDataModel('schemas', scriptVariables['schemas']);
        });
}

function getTables(schema) {
    sendAjaxRequest(scriptVariables['getTablesEndpoint'] + schema,
        null,
        "GET",
        function (tablesData) {
            fillArrayProperty('tables', tablesData);
            syncSelectOptionsWithDataModel('table', scriptVariables['tables']);
        });
}

function getAvailableColumns(schema, tablesArray) {
    let tableParamString = tablesArray.join('&');
    sendAjaxRequest(scriptVariables['getColumnsEndpoint'] + schema + "/" + tableParamString,
        null,
        "GET",
        function(columnsData) {
            fillArrayProperty('availableColumns', columnsData);
            syncSelectOptionsWithDataModel('availableColumns', scriptVariables['availableColumns']);
        });
}

function addSelectedColumns(members) {
    fillArrayProperty('selectedColumns', members, false);
    syncSelectOptionsWithDataModel('columns', scriptVariables['selectedColumns']);
}

function removeSelectedColumn(memberIndeces) {
    deleteArrayPropertyMembers('selectedColumns', memberIndeces);
    syncSelectOptionsWithDataModel('columns', scriptVariables['selectedColumns']);
}

function moveSelectedColumnUp(index) {
    if (index === 0) return null;

    // get destination item
    var itemToDelete = scriptVariables['selectedColumns'][index - 1];

    // set destination item to current item
    scriptVariables['selectedColumns'][index - 1] = scriptVariables['selectedColumns'][index];

    // insert destination item at current item's index
    scriptVariables['selectedColumns'][index] = itemToDelete;

    syncSelectOptionsWithDataModel('selectedColumns', scriptVariables['selectedColumns']);
    //updateSelectedMembersHTML();
}

function moveSelectedColumnDown(index) {
    if (index === scriptVariables['selectedColumns'].length - 1) return null;

    // get destination item
    var itemToDelete = scriptVariables['selectedColumns'][index + 1];

    // set destination item to current item
    scriptVariables['selectedColumns'][index + 1] = scriptVariables['selectedColumns'][index];

    // insert destination item at current item's index
    scriptVariables['selectedColumns'][index] = itemToDelete;

    syncSelectOptionsWithDataModel('selectedColumns', scriptVariables['selectedColumns']);
    //updateSelectedMembersHTML();
}

function addOrderByColumns(members) {
    fillArrayProperty('orderByColumns', members);
    syncSelectOptionsWithDataModel('orderBy', scriptVariables['orderByColumns']);
}

function removeOrderByColumns(memberIndeces) {
    deleteArrayPropertyMembers('orderByColumns', memberIndeces);
    syncSelectOptionsWithDataModel('orderBy', scriptVariables['orderByColumns']);
}

function moveOrderByColumnUp(index) {
    moveArrayPropertyItem('orderByColumns', index, 'up');
    syncSelectOptionsWithDataModel('orderBy', scriptVariables['orderByColumns']);
}

function moveOrderByColumnDown(index) {
    moveArrayPropertyItem('orderByColumns', index, 'down');
    syncSelectOptionsWithDataModel('orderBy', scriptVariables['orderByColumns']);
}

function addGroupByColumns(members) {

    fillArrayProperty('groupByColumns', members);
    syncSelectOptionsWithDataModel('groupBy', scriptVariables['groupByColumns']);
}

function removeGroupByColumns(memberIndeces) {
    deleteArrayPropertyMembers('groupByColumns', memberIndeces);
    syncSelectOptionsWithDataModel('groupBy', scriptVariables['groupByColumns']);
}

function moveGroupByColumnUp(index) {
    moveArrayPropertyItem('groupByColumns', index, 'up');
    syncSelectOptionsWithDataModel('groupBy', scriptVariables['groupByColumns']);
}

function moveGroupByColumnDown(index) {
    moveArrayPropertyItem('groupByColumns', index, 'down');
    syncSelectOptionsWithDataModel('groupBy', scriptVariables['groupByColumns']);
}

// id:  The criteria row that is being added or was removed
// addOrRemove:  A string (either 'add' or 'remove')
function renumberCriteria(id, addOrRemove) {
    let criteria = document.getElementsByClassName('criteria-row');

    for (var i=0; i<criteria.length; i++) {

        // get new id
        let currentId = parseInt(criteria[i].id.slice(-1));
        let newId = currentId;
        if (currentId >= id && addOrRemove === 'add') {
            newId = currentId + 1;
        } else if (currentId > id && addOrRemove === 'remove') {
            newId = currentId - 1;
        }

        // get new parent id
        let currentParentId = criteria[i].children[1].value;;
        let newParentId = (currentParentId === null) ? '' : currentParentId;
        if (currentId >= id && addOrRemove === 'add') {
            if (currentParentId !== "") {
                newParentId = parseInt(currentParentId) + 1;
            }
        } else if (currentId > id && addOrRemove === 'remove') {
            if (currentParentId !== "") {
                if (parseInt(currentParentId) === 0 && parseInt(id) !== 0) {
                    newParentId = currentParentId;
                }
                if (parseInt(currentParentId) === 0 && parseInt(id) === 0) {
                    newParentId = null;
                }
                if (parseInt(currentParentId) > 0) {
                    newParentId = parseInt(currentParentId) - 1;
                }
            }
        }

        // if greater, subtract by one
        criteria[i].id = 'row.' + newId;

        //id
        criteria[i].children[0].id = 'criteria' + newId + '.id'; //id
        criteria[i].children[0].name = 'criteria[' + newId + '].id'; //name
        criteria[i].children[0].value = newId; //value

        //parentId
        criteria[i].children[1].id = 'criteria' + newId + '.parentId'; //id
        criteria[i].children[1].name = 'criteria[' + newId + '].parentId'; //name
        if (addOrRemove === 'add') {
            if (currentParentId >= id) {
                criteria[i].children[1].value = newParentId; //value
            }
        } else {
            criteria[i].children[1].value = newParentId; //value
        }


        //conjunction
        criteria[i].children[2].id = 'criteria' + newId + '.conjunction'; //id
        criteria[i].children[2].name = 'criteria[' + newId + '].conjunction'; //name

        //front parenthesis
        criteria[i].children[3].id = 'criteria' + newId + '.frontParenthesis'; //id
        criteria[i].children[3].name = 'criteria[' + newId + '].frontParenthesis'; //name

        //column
        criteria[i].children[4].id = 'criteria' + newId + '.column'; //id
        criteria[i].children[4].name = 'criteria[' + newId + '].column'; //name

        //operator
        criteria[i].children[5].id = 'criteria' + newId + '.operator'; //id
        criteria[i].children[5].name = 'criteria[' + newId + '].operator'; //name

        //filter
        criteria[i].children[6].id = 'criteria' + newId + '.filter'; //id
        criteria[i].children[6].name = 'criteria[' + newId + '].filter'; //name

        //end parenthesis
        criteria[i].children[7].id = 'criteria' + newId + '.endParenthesis'; //id
        criteria[i].children[7].name = 'criteria[' + newId + '].endParenthesis'; //name
    }

}

function reindentCriteria() {
    let criteria = document.getElementsByClassName('criteria-row');

    for (var i=0; i<criteria.length; i++) {
        //remove indenting
        criteria[i].style.paddingLeft = "0px";

        //get parentId
        let parentId = criteria[i].children[1].value;
        if (parentId !== "") {
            //find parentId row's padding left indent
            let parentRowIndent = document.getElementById('row.' + parseInt(parentId)).style.paddingLeft;
            if (parentRowIndent === "") {
                parentRowIndent = "0px";
            }
            //set this rows padding left indent + 20px
            let newPaddingLeft = parseInt(parentRowIndent) + 100;
            criteria[i].style.paddingLeft = newPaddingLeft + 'px';
        }
    }
}

// parentNode:  The criteria node to insert this child node after
function addCriteria(parentNode) {
    // These default assignments for parentId and id assume we are adding a new root criteria.
    let parentId = '';
    let id = 0;

    // If the parentNode parameter is not null, then that means we are adding a child criteria and will reassign the
    //   parentId and id variables.
    if (parentNode !== null) {
        parentId = parentNode.id.slice(-1);
        id = parseInt(parentId) + 1;
    }

    renumberCriteria(id, 'add');

    //inserts new row after row where 'Add Criteria' button was clicked.
    let newDiv = createNewElement('div', {
        'id': 'row.' + id,
        'class': 'criteria-row'
    }, null);

    // create id input element
    let idInput = createNewElement('input', {
        'type': 'hidden',
        'id': 'criteria' + id + '.id',
        'name': 'criteria[' + id + '].id',
        'value': id

    }, null);
    newDiv.appendChild(idInput);

    // create parentId input element
    let parentInputId = createNewElement('input', {
        'type': 'hidden',
        'id': 'criteria' + id + '.parentId',
        'name': 'criteria[' + id + '].parentId',
        'value': parentId
    }, null);
    newDiv.appendChild(parentInputId);

    // create conjunction select element
    let optionAnd = createNewElement('option', {
        'value': 'And'
    }, null);
    optionAnd.innerHTML = 'And';
    let optionOr = createNewElement('option', {
        'value': 'Or'
    }, null);
    optionOr.innerHTML = 'Or';
    let conjunctionEl = createNewElement('select', {
        'id': `criteria${id}.conjunction`,
        'name': `criteria[${id}].conjunction`,
        'class': 'criteria-conjuction-and-operator'
    }, null);
    conjunctionEl.appendChild(optionAnd);
    conjunctionEl.appendChild(optionOr);
    newDiv.appendChild(conjunctionEl);

    // create front parenthesis input element
    let frontParenInput = createNewElement('input', {
        'type': 'hidden',
        'id': 'criteria' + id + '.frontParenthesis',
        'name': 'criteria[' + id + '].frontParenthesis'
    }, null);
    newDiv.appendChild(frontParenInput);

    // create column select element
    let columnEl = createNewElement('select', {
        'id': `criteria${id}.column`,
        'name': `criteria[${id}].column`,
        'class': 'criteria-column-and-filter'
    }, scriptVariables['availableColumns']);
    newDiv.appendChild(columnEl);

    // create operator select element
    let optionEqual =               createNewElement('option', {'value': 'equalTo'}, null, '=');
    let optionNotEqualTo =          createNewElement('option', {'value': 'notEqualTo'}, null, '<>');
    let optionGreaterThanOrEquals = createNewElement('option', {'value': 'greaterThanOrEquals'}, null, '>=');
    let optionLessThanOrequals =    createNewElement('option', {'value': 'lessThanOrEquals'}, null, '<=');
    let optionGreaterThan =         createNewElement('option', {'value': 'greaterThan'}, null, '>');
    let optionLessThan =            createNewElement('option', {'value': 'lessThan'}, null, '<');
    let optionLike =                createNewElement('option', {'value': 'like'}, null, 'like');
    let optionNotLike =             createNewElement('option', {'value': 'notLike'}, null, 'not like');
    let optionIn =                  createNewElement('option', {'value': 'in'}, null, 'in');
    let optionNotIn =               createNewElement('option', {'value': 'notIn'}, null, 'not in');
    let optionIsNull =              createNewElement('option', {'value': 'isNull'}, null, 'is null');
    let optionIsNotNull =           createNewElement('option', {'value': 'isNotNull'}, null, 'is not null');

    let operatorEl = createNewElement('select', {
        'id': `criteria${id}.operator`,
        'name': `criteria[${id}].operator`,
        'class': 'criteria-conjuction-and-operator'
    }, null);
    operatorEl.appendChild(optionEqual);
    operatorEl.appendChild(optionNotEqualTo);
    operatorEl.appendChild(optionGreaterThanOrEquals);
    operatorEl.appendChild(optionLessThanOrequals);
    operatorEl.appendChild(optionGreaterThan);
    operatorEl.appendChild(optionLessThan);
    operatorEl.appendChild(optionLike);
    operatorEl.appendChild(optionNotLike);
    operatorEl.appendChild(optionIn);
    operatorEl.appendChild(optionNotIn);
    operatorEl.appendChild(optionIsNull);
    operatorEl.appendChild(optionIsNotNull);

    newDiv.appendChild(operatorEl);

    // create filter input element
    let filterInput = createNewElement('input', {
        'id': 'criteria' + id + '.filter',
        'name': 'criteria[' + id + '].filter',
        'class': 'criteria-column-and-filter'
    }, null);
    newDiv.appendChild(filterInput);

    // create end parenthesis input element
    let endParenInput = createNewElement('input', {
        'type': 'hidden',
        'id': 'criteria' + id + '.endParenthesis',
        'name': 'criteria[' + id + '].endParenthesis'
    }, null);
    newDiv.appendChild(endParenInput);

    // create 'Add Criteria' button
    let addCriteriaButton = createNewElement('input', {
        'type': 'button',
        'value': '+',
        'class': 'criteria-add-remove-buttons'
    }, null);
    addCriteriaButton.onclick = function () {
        addCriteria(newDiv);
    }
    newDiv.appendChild(addCriteriaButton);

    // create 'Remove Criteria' button
    let removeCriteriaButton = createNewElement('input', {
        'type': 'button',
        'value': 'X',
        'class': 'criteria-add-remove-buttons'
    }, null);
    removeCriteriaButton.onclick = function () {
        newDiv.remove();
        renumberCriteria(newDiv.id.slice(-1), 'remove');
        reindentCriteria();
    }
    newDiv.appendChild(removeCriteriaButton);

    // insert newDiv into the DOM
    if (parentNode === null) {
        document.getElementById('criteriaAnchor').prepend(newDiv);
    } else {
        parentNode.insertAdjacentElement('afterend', newDiv);
    }

    reindentCriteria();
}

function renderHTML(beforeNode) {
    var form = document.createElement('form');
    form.setAttribute('id', 'queryBuilder');
    form.setAttribute('name', 'queryBuilder');
    form.setAttribute('action', scriptVariables['formSubmissionEndpoint']);
    form.setAttribute('method', scriptVariables['formMethod']);

    var el = null;

    //Query Templates
    // NOTE:  THIS IS NOW DONE AT THE BOTTOM OF THIS FILE WHERE THE SCRIPT STARTS
    // if (scriptVariables['getQueryTemplateEndpoint'] !== null) {
    //     getQueryTemplates();
    // }

    el = renderStatementButtonsHTML();
    if (el !== undefined) {
        form.appendChild(el);
    }

    el = renderQueryTemplatesHTML();
    if (el !== undefined) {
        form.appendChild(el);
        let brEl = createNewElement('br');
        form.appendChild(brEl);
    }

    //Available Columns
    el = renderAvailableColumnsHTML();
    if (el !== undefined) {
        form.appendChild(el);
        let brEl = createNewElement('br');
        form.appendChild(brEl);
    }

    //Schemas
    el = renderSchemaHTML();
    if (el !== undefined) {
        form.appendChild(el);
        let brEl = createNewElement('br');
        form.appendChild(brEl);
    }

    //Tables
    el = renderTablesHTML();
    if (el !== undefined)
        form.appendChild(el);

    //Joins
    el = renderJoinsHTML();
    if (el !== undefined)
        form.appendChild(el);

    //Criteria
    el = renderCriteriaHTML();
    if (el !== undefined)
        form.appendChild(el);

    //Other Options
    el = renderOtherOptionsHTML();
    if (el !== undefined)
        form.appendChild(el);

    // el = renderQueryButtonHTML();
    // if (el !== undefined) {
    //     form.appendChild(el);
    // }

    if (beforeNode === undefined) {
        document.body.appendChild(form);
    } else {
        document.getElementById(beforeNode).appendChild(form);
    }

}

// This method assumes you are feeding it a JSON object with key-value pairs.
function fillArrayProperty(arrayPropertyName, data, clearPropertyArray=true) {
    if (clearPropertyArray) {
        scriptVariables[arrayPropertyName] = [];
    }

    for (var i=0; i<data.length; i++) {
        for (var key in data[i]) {
            scriptVariables[arrayPropertyName].push(data[i][key]);
        }
    }
}

function deleteArrayPropertyMembers(arrayPropertyName, indecesToDelete) {
    newArray = scriptVariables[arrayPropertyName].slice();
    for (var i in indecesToDelete) {
        for (var key in indecesToDelete[i]) {
            let indexToDelete = indecesToDelete[i][key];
            newArray.splice(indexToDelete, 1);
        }
    }

    scriptVariables[arrayPropertyName] = newArray;
}

function moveArrayPropertyItem(arrayPropertyName, index, direction) {
    if (direction === 'up') {
        if (index === 0) return null;

        // get destination item
        var itemToDelete = this[arrayPropertyName][index - 1];

        // set destination item to current item
        this[arrayPropertyName][index - 1] = this[arrayPropertyName][index];

        // insert destination item at current item's index
        this[arrayPropertyName][index] = itemToDelete;
    } else if (direction === 'down') {
        if (index === this[arrayPropertyName].length - 1) return null;

        // get destination item
        var itemToDelete = this[arrayPropertyName][index + 1];

        // set destination item to current item
        this[arrayPropertyName][index + 1] = this[arrayPropertyNames][index];

        // insert destination item at current item's index
        this[arrayPropertyName][index] = itemToDelete;
    }
}

function createNewElement(type, attributesMap, dataProperty, innerHtml=null) {
    if (this[dataProperty] === null) return null;

    let select = document.createElement(type);
    for (var key in attributesMap) {
        let attributeName = key;
        let attributeValue = attributesMap[key];
        select.setAttribute(attributeName, attributeValue);
    }

    if (dataProperty !== null) {
        for (var item in dataProperty) {
            let option = document.createElement('option');
            option.value = dataProperty[item];
            option.innerHTML = dataProperty[item];

            select.add(option);
        }
    }

    if (innerHtml !== null) {
        select.innerHTML = innerHtml;
    }

    return select;
}

// divsToShow:  Array of strings.
function hideAllDivsExcept(divsToShow) {
    //let phaseOutMilliseconds = 200;
    let divs = [
        'queryTemplatesDiv',
        'tableColumns',
        'schemasDiv',
        'tablesDiv',
        'joinsDiv',
        'criteria',
        'otherOptionsDiv'
    ];

    // Hide all divs in array above.
    for (var i=0; i<divs.length; i++) {
        $('#' + divs[i]).hide(scriptVariables['phaseOutMilliseconds'])
    }

    // Show all divs in divsToShow array.
    for (var i=0; i<divsToShow.length; i++) {
        $('#' + divsToShow[i]).show(scriptVariables['phaseOutMilliseconds']);
    }
}

function renderStatementButtonsHTML() {
    let queryTemplatesButton = null;
    let schemasAndTablesButton = null;
    let joinsButton = null;
    let columnsButton = null;
    let criteriaButton = null;
    let otherOptionsButton = null;

    if (scriptVariables['queryTemplates'] !== null) {
        let attributesMap = {
            'id': 'queryTemplatesButton',
            'name': 'queryTemplatesButton',
            'class': 'query-template-button',
            'type': 'button'
        };
        queryTemplatesButton = createNewElement('button', attributesMap);
        queryTemplatesButton.innerHTML = 'Query Templates';
        queryTemplatesButton.onclick = function() {
            hideAllDivsExcept(['queryTemplatesDiv']);
        };
    }

    if (scriptVariables['schemas'] !== null || scriptVariables['tables'] !== null) {
        let attributesMap = {
            'id': 'schemasButton',
            'name': 'schemasButton',
            'class': 'schemas-button',
            'type': 'button'
        };
        schemasAndTablesButton = createNewElement('button', attributesMap);
        schemasAndTablesButton.innerHTML = 'Schemas & Tables';
        schemasAndTablesButton.onclick = function() {
            hideAllDivsExcept(['schemasDiv', 'tablesDiv']);
        };
    }

    if (scriptVariables['allowJoins'] !== null) {
        let attributesMap = {
            'id': 'joinsButton',
            'name': 'joinsButton',
            'class': 'joins-button',
            'type': 'button'
        };
        joinsButton = createNewElement('button', attributesMap);
        joinsButton.innerHTML = 'Joins';
        joinsButton.onclick = function() {
            hideAllDivsExcept(['joinsDiv']);
        };
    }

    if (scriptVariables['availableColumns'] !== null) {
        let attributesMap = {
            'id': 'columnsButton',
            'name': 'columnsButton',
            'class': 'columns-button',
            'type': 'button'
        };
        columnsButton = createNewElement('button', attributesMap);
        columnsButton.innerHTML = 'Columns';
        columnsButton.onclick = function() {
            hideAllDivsExcept(['tableColumns']);
        };
    }

    if (scriptVariables['criteria'] !== null) {
        let attributesMap = {
            'id': 'criteriaButton',
            'name': 'criteriaButton',
            'class': 'criteria-button',
            'type': 'button'
        };
        criteriaButton = createNewElement('button', attributesMap);
        criteriaButton.innerHTML = 'Criteria';
        criteriaButton.onclick = function() {
            hideAllDivsExcept(['criteria']);
        };
    }

    if (scriptVariables['distinct'] !== null || scriptVariables['orderByColumns'] !== null || scriptVariables['groupByColumns'] !== null
        || scriptVariables['limitChoices'] !== null || scriptVariables['offsetChoices'] !== null || scriptVariables['suppressNulls'] !== null) {
        let attributesMap = {
            'id': 'criteriaButton',
            'name': 'criteriaButton',
            'class': 'criteria-button',
            'type': 'button'
        };
        otherOptionsButton = createNewElement('button', attributesMap);
        otherOptionsButton.innerHTML = 'Other Options';
        otherOptionsButton.onclick = function() {
            hideAllDivsExcept(['otherOptionsDiv']);
        };
    }

    let attributesMap = {
        'id': 'runQuery',
        'name': 'runQuery',
        'type': 'button',
        'class': 'run-query-button'
    };
    let runQueryButton = createNewElement('button', attributesMap, null);
    runQueryButton.innerHTML = 'Run Query';
    runQueryButton.onclick = function () {
        scriptVariables['formSubmissionFunction']();
    };

    let div = createNewElement('div', {
        'id': 'statementButtonsDiv',
        'class': 'statement-buttons-div'
    });
    if (queryTemplatesButton !== null) div.appendChild(queryTemplatesButton);
    if (schemasAndTablesButton !== null) div.appendChild(schemasAndTablesButton);
    if (joinsButton !== null) div.appendChild(joinsButton);
    if (columnsButton !== null) div.appendChild(columnsButton);
    if (criteriaButton !== null) div.appendChild(criteriaButton);
    if (otherOptionsButton !== null) div.appendChild(otherOptionsButton);
    div.appendChild(runQueryButton);

    return div;
}

function renderQueryTemplatesHTML() {
    if (scriptVariables['queryTemplates'] !== null) {
        let attributesMap = {
            'id': 'queryTemplates',
            'name': 'queryTemplates',
            'class': 'form-control'
        };
        let select = createNewElement('select', attributesMap, scriptVariables['queryTemplates']);
        select.onchange = function() {
            let queryTemplateId = document.getElementById('queryTemplates').value;
            getQueryTemplatById(queryTemplateId);
        };

        let label = createNewElement('label', {'for': 'queryTemplates'});
        label.innerHTML = 'Query Templates';

        let div = createNewElement('div', {'id': 'queryTemplatesDiv', 'class': 'query-templates-div'});
        div.appendChild(label);
        div.appendChild(select);

        return div;
    }
}

function renderSchemaHTML() {
    if (scriptVariables['schemas'] !== null) {
        let attributesMap = {
            'id': 'schemas',
            'name': 'schemas',
            'class': 'form-control',
            'size': scriptVariables['schemasSize']
        };

        let select = createNewElement('select', attributesMap, scriptVariables['schemas']);
        select.onchange = function () {
            let schema = document.getElementById('schemas').value;
            if (schema !== "") {
                getTables(schema);
            } else {
                alert('Please select a schema before retrieving tables');
            }
        };

        let label = createNewElement('label', {'for': 'schemas'});
        label.innerHTML = 'Database Schemas';

        let div = createNewElement('div', {
            'id': 'schemasDiv',
            'class': 'schemas-div'
        });
        div.appendChild(label);
        div.appendChild(select);

        return div;
    }
}

function renderTablesHTML() {
    if (scriptVariables['tables'] !== null) {
        let attributesMap = {
            'id': 'table', //TODO change this back to 'tables' once qb4j can handle multiple tables.
            'name': 'table',
            'multiple': 'true',
            'class': 'form-control',
            'size': scriptVariables['tablesSize']
        };

        let select = createNewElement('select', attributesMap, scriptVariables['tables']);
        select.onchange = function() {
            let schema = document.getElementById('schemas').value;
            let tables = getSelectedOptionsAsArray('table');
            if (schema !== "" && tables !== "") {
                getAvailableColumns(schema, tables);
            } else {
                alert('Please select a schema before retrieving tables');
            }
        };

        let label = createNewElement('label', {
            'for': 'table'
        });
        label.innerHTML = 'Database Tables';

        let div = createNewElement('div', {
            'id': 'tablesDiv',
            'class': 'tables-div'
        });
        div.appendChild(label);
        div.appendChild(select);

        return div;
    }
}

function renderJoinsHTML() {
    if (scriptVariables['allowJoins']) {
        // create parent div for all joins.
        let joinsDiv = createNewElement('div', {
            'id': 'joinsDiv',
            'class': 'joins-div'
        });

        // create add join
        // add parent join text box, add target join text box, add png, add 'on' select boxes, add delete button
        let addJoinButton = createNewElement('button', {
            'id': 'addJoin',
            'name': 'addJoin',
            'type': 'button'
        });
        addJoinButton.innerHTML = 'Add Join';
        addJoinButton.onclick = function() {
            let maxId = 0;
            $('#joinsDiv div').each(function() {
                let idLength = $(this)[0].id.length;
                let id = parseInt($(this)[0].id[idLength - 1]);
                if (id >= maxId) {
                    maxId = id + 1;
                }
            });

            let newJoinDiv = createNewElement('div', {
                'id': `join-row${maxId}`,
                'name': `join-row${maxId}`,
                'class': 'join-row'
            });
            let newJoinType = createNewElement('input', {
                'id': `joins${maxId}.joinType`,
                'name': `joins[${maxId}].joinType`,
                'hidden': 'true',
                'value': 'LEFT_EXCLUDING' // Note: The name must match the case of the Java enum class.
            });
            let newJoinImage = createNewElement('img', {
                'id': `joins${maxId}.image`,
                'name': `joins[${maxId}].image`,
                'src': scriptVariables['joinImageBaseURL'] + 'left_join_excluding.png',
                'width': '100',
                'height': '80'
            });
            newJoinImage.onclick = function() {
                // Note:  Names must match the case that the Java enum is in.  The Java enum is in all caaps, so the names
                // here must be in all caps also or Spring will throw an exception.
                let images = [
                    {'name': 'LEFT_EXCLUDING',         'file_path': scriptVariables['joinImageBaseURL'] + 'left_join_excluding.png'},
                    {'name': 'LEFT',                   'file_path': scriptVariables['joinImageBaseURL'] + 'left_join.png'},
                    {'name': 'INNER',                  'file_path': scriptVariables['joinImageBaseURL'] + 'inner_join.png'},
                    {'name': 'RIGHT',                  'file_path': scriptVariables['joinImageBaseURL'] + 'right_join.png'},
                    {'name': 'RIGHT_EXCLUDING',        'file_path': scriptVariables['joinImageBaseURL'] + 'right_join_excluding.png'},
                    {'name': 'FULL_OUTER',             'file_path': scriptVariables['joinImageBaseURL'] + 'full_outer_join.png'},
                    {'name': 'FULL_OUTER_EXCLUDING',   'file_path': scriptVariables['joinImageBaseURL'] + 'full_outer_join_excluding.png'}
                ];

                let joinType = document.getElementById(`joins${maxId}.joinType`);
                let currentJoinName = joinType.value;
                let joinImage = document.getElementById(`joins${maxId}.image`);

                // Set newJoinType and newJoinImage to next join name and file_path in array.
                for (var i=0; i<images.length; i++) {
                    if (images[i].name === currentJoinName) {
                        // If index is less than last index, then get file_path of index + 1.
                        // Else (if index is last index), then get first file_path in array.
                        if (i < images.length-1) {
                            joinType.setAttribute('value', images[i+1].name);
                            joinImage.src = images[i+1].file_path;
                            break;
                        } else {
                            joinType.setAttribute('value', images[0].name);
                            joinImage.src = images[0].file_path;
                            break;
                        }
                    }
                }
            };

            let newJoinParentColumn = createNewElement('select', {
                'id': `joins${maxId}.parentJoinColumns`,
                'name': `joins[${maxId}].parentJoinColumns`
            });
            let newJoinTargetColumn = createNewElement('select', {
                'id': `joins${maxId}.targetJoinColumns`,
                'name': `joins[${maxId}].targetJoinColumns`
            });

            // slice() is to copy the 'tables' array.
            let parentAndTargetJoinTables = scriptVariables['tables'].slice(0);
            // splice() is to add null as first item in array so that a blank choice is displayed first in resulting select element.
            parentAndTargetJoinTables.splice(0, 0, null);

            let newJoinParentTable = createNewElement('select', {
                'id': `joins${maxId}.parentTable`,
                'name': `joins[${maxId}].parentTable`
            }, parentAndTargetJoinTables);

            newJoinParentTable.onchange = function() {
                // Get selected table from select element.
                let selectedOption = getSelectedOption(this);

                // Get all available columns that are from the selected table.
                let tableColumns = getTableColumns(selectedOption);

                // Add parent table columns to parent table select elements.
                let parentJoinColumns = document.getElementsByName(newJoinParentColumn.name);
                Array.from(parentJoinColumns).forEach(parentJoinColumn => syncSelectOptionsWithDataModel(parentJoinColumn, tableColumns));
            };

            let newJoinTargetTable = createNewElement('select', {
                'id': `joins${maxId}.targetTable`,
                'name': `joins[${maxId}].targetTable`
            }, parentAndTargetJoinTables);
            newJoinTargetTable.onchange = function() {
                // Get selected table from select element.
                let selectedOption = getSelectedOption(this);

                // Get all available columns that are from the selected table.
                let tableColumns = getTableColumns(selectedOption);

                // Add target table columns to target table select elements.
                let targetJoinColumns = document.getElementsByName(newJoinTargetColumn.name);
                Array.from(targetJoinColumns).forEach(targetJoinColumn => syncSelectOptionsWithDataModel(targetJoinColumn, tableColumns));
            };

            let newJoinDeleteButton = createNewElement('button', {
                'id': `joins${maxId}.deleteButton`,
                'name': `joins[${maxId}].deleteButton`,
                'class': 'delete-join-button',
                'type': 'button'
            });
            newJoinDeleteButton.onclick = function() {
                // Get the numerical id of this button.
                let id = parseInt($(this)[0].id[5]);

                $(`#join-row${id}`).remove();

                // For each div in joinsDiv, change the id if it is greater than the id of the div that was deleted.
                let divs = document.querySelectorAll('#joinsDiv div');
                for (var i=0; i<divs.length; i++) {
                    let idLength = divs[i].id.length;
                    let id = parseInt(divs[i].id[idLength - 1]);
                    if (id > maxId) {
                        // Renumber div
                        divs[i].setAttribute('name', `join-row${id - 1}`);
                        divs[i].id = `join-row${id - 1}`;
                        // Renumber joinType
                        document.getElementById(`joins${id}.joinType`).name = `joins[${id - 1}].joinType`;
                        document.getElementById(`joins${id}.joinType`).id = `joins${id - 1}.joinType`;
                        // Renumber image
                        document.getElementById(`joins${id}.image`).name = `joins[${id - 1}].image`;
                        document.getElementById(`joins${id}.image`).id = `joins${id - 1}.image`;
                        // Renumber parentTable
                        document.getElementById(`joins${id}.parentTable`).name = `joins[${id - 1}].parentTable`;
                        document.getElementById(`joins${id}.parentTable`).id = `joins${id - 1}.parentTable`;
                        // Renumber targetTable
                        document.getElementById(`joins${id}.targetTable`).name = `joins[${id - 1}].targetTable`;
                        document.getElementById(`joins${id}.targetTable`).id = `joins${id - 1}.targetTable`;

                        // Renumber parentColumns
                        let parentJoinColumns = document.getElementsByName(`joins[${id}].parentJoinColumns`);
                        Array.from(parentJoinColumns).forEach(parentJoinColumn => {
                            parentJoinColumn.name = `joins[${id - 1}].parentJoinColumns`;
                        parentJoinColumn.id = `joins${id - 1}.parentJoinColumns`;
                    });

                        // Renumber targetColumns
                        let targetJoinColumns = document.getElementsByName(`joins[${id}].targetJoinColumns`);
                        Array.from(targetJoinColumns).forEach(targetJoinColumn => {
                            targetJoinColumn.name = `joins[${id - 1}].targetJoinColumns`;
                        targetJoinColumn.id = `joins${id - 1}.targetJoinColumns`;
                    });

                        // Renumber deleteButton
                        let deleteButtons = document.getElementsByName(`joins[${id}].deleteButton`);
                        Array.from(deleteButtons).forEach(deleteButton => {
                            deleteButton.name = `joins[${id - 1}].deleteButton`;
                        deleteButton.id = `joins${id - 1}.deleteButton`;
                    });

                        // Renumber addParentAndTargetColumn
                        let addParentAndTargetColumnButtons = document.getElementsByName(`joins[${id}].addParentAndTargetColumn`);
                        Array.from(addParentAndTargetColumnButtons).forEach(addParentAndTargetColumn => {
                            addParentAndTargetColumn.name = `joins[${id - 1}].addParentAndTargetColumn`;
                        addParentAndTargetColumn.id = `joins${id - 1}.addParentAndTargetColumn`;
                    });

                        // Renumber deleteJoinColumnsButtons
                        let deleteJoinColumnsButtons = document.getElementsByName(`joins[${id}].deleteJoinColumnsButton`);
                        Array.from(deleteJoinColumnsButtons).forEach(deleteJoinColumnsButton => {
                            deleteJoinColumnsButton.name = `joins[${id - 1}].deleteJoinColumnsButton`;
                        deleteJoinColumnsButton.id = `joins${id - 1}.deleteJoinColumnsButton`;
                    });
                    }
                }
            };
            newJoinDeleteButton.innerHTML = 'X';

            let newJoinAddParentAndTargetColumn = createNewElement('button', {
                'id': `joins${maxId}.addParentAndTargetColumn`,
                'name': `joins[${maxId}].addParentAndTargetColumn`,
                'class': 'add-parent-and-target-column',
                'type': 'button'
            });
            newJoinAddParentAndTargetColumn.innerHTML = '+';

            // The onclick event should generate another parentAndTargetColumnPair with the data source identical to first parentAndTargetColumnPair.
            newJoinAddParentAndTargetColumn.onclick = function() {
                let parentTableColumns = Array.from(document.getElementById(`joins${maxId}.parentJoinColumns`).options).map(option => option.value);
                let targetTableColumns = Array.from(document.getElementById(`joins${maxId}.targetJoinColumns`).options).map(option => option.value);

                let anotherParentColumn = createNewElement('select', {
                    'id': `joins${maxId}.parentJoinColumns`,
                    'name': `joins[${maxId}].parentJoinColumns`
                }, parentTableColumns);

                let anotherTargetColumn = createNewElement('select', {
                    'id': `joins${maxId}.targetJoinColumns`,
                    'name': `joins[${maxId}].targetJoinColumns`
                }, targetTableColumns);

                let equalSign = createNewElement('b');
                equalSign.innerHTML = ' = ';

                let joinColumnsDeleteButton = createNewElement('button', {
                    'id': `joins${maxId}.deleteJoinColumnsButton`,
                    'name': `joins[${maxId}].deleteJoinColumnsButton`,
                    'class': 'delete-join-columns-button',
                    'type': 'button'
                })
                joinColumnsDeleteButton.innerHTML = 'X';
                joinColumnsDeleteButton.onclick = function() {
                    this.parentNode.remove();
                };

                let div = createNewElement('div');
                div.appendChild(anotherParentColumn);
                div.appendChild(equalSign);
                div.appendChild(anotherTargetColumn);
                div.appendChild(joinColumnsDeleteButton);
                document.getElementById(`join-row${maxId}`).appendChild(div);
            };

            newJoinDiv.appendChild(newJoinDeleteButton);
            newJoinDiv.appendChild(newJoinType);
            newJoinDiv.appendChild(newJoinParentTable);
            newJoinDiv.appendChild(newJoinImage);
            newJoinDiv.appendChild(newJoinTargetTable);

            let firstParentAndTargetColumnDiv = createNewElement('div');
            firstParentAndTargetColumnDiv.appendChild(newJoinParentColumn);
            let equalSign = createNewElement('b');
            equalSign.innerHTML = ' = ';
            firstParentAndTargetColumnDiv.appendChild(equalSign);
            firstParentAndTargetColumnDiv.appendChild(newJoinTargetColumn);
            firstParentAndTargetColumnDiv.appendChild(newJoinAddParentAndTargetColumn);
            newJoinDiv.appendChild(firstParentAndTargetColumnDiv);

            document.getElementById('joinsDiv').appendChild(newJoinDiv);
        };

        // Add addjoinButton to parent div.
        joinsDiv.appendChild(addJoinButton);

        return joinsDiv;
    }
}

function getSelectedOption(node) {
    let options = node.children;
    let selectedOption = null;
    for (var i=0; i<options.length; i++) {
        if (options[i].selected === true) {
            selectedOption = options[i];
        }
    }

    return selectedOption.value;
}

function getTableColumns(table) {
    return scriptVariables['availableColumns'].filter(column => column.split('.')[0] === table);
}

function renderAvailableColumnsHTML() {
    if (scriptVariables['availableColumns'] !== null) {
        // Create Available Columns Div, Label, and Select elements
        let attributesMapAvailableColumns = {
            'id': 'availableColumns',
            'name': 'availableColumns',
            'multiple': 'true',
            'class': 'form-control',
            'size': scriptVariables['availableColumnsSize']
        };

        let selectAvailableColumns = createNewElement('select', attributesMapAvailableColumns, scriptVariables['availableColumns']);
        let labelAvailableColumns = createNewElement('label', {'for': 'availableColumns'});
        labelAvailableColumns.innerHTML = 'Table Columns';

        let availableColumnsDiv = createNewElement('div', {
            'id': 'availableColumnsDiv',
            'class': 'available-columns-div'
        });
        availableColumnsDiv.appendChild(labelAvailableColumns);
        availableColumnsDiv.appendChild(selectAvailableColumns);

        // Create Add and Remove Columns Div and Button elements
        let addColumnButton = createNewElement('button', {
            'id': 'addColumnsButton',
            'name': 'addColumnsButton',
            'type': 'button',
            'class': 'available-columns-add-button'
        }, null);
        addColumnButton.innerHTML = 'Add';
        addColumnButton.onclick = function() {
            let selectedColumns = getSelectedOptionsAsJSON('availableColumns');
            addSelectedColumns(selectedColumns);
        };

        let removeColumnButton = createNewElement('button', {
            'id': 'removeColumnsButton',
            'name': 'removeColumnsButton',
            'type': 'button', 'class':
                'available-columns-remove-button'
        }, null);
        removeColumnButton.innerHTML = 'Remove';
        removeColumnButton.onclick = function() {
            let selectedColumns = getSelectedOptionsAsJSON('columns', 'indeces');
            removeSelectedColumn(selectedColumns);
        };

        let addRemoveButtonsDiv = createNewElement('div', {
            'id': 'addRemoveColumns',
            'class': 'available-columns-buttons-div'
        });
        addRemoveButtonsDiv.appendChild(addColumnButton);
        addRemoveButtonsDiv.appendChild(createNewElement('br'));
        addRemoveButtonsDiv.appendChild(removeColumnButton);

        // Create Selected Columns Div, Label, and Select elements.
        let attributesMapSelectedColumns = {
            'id': 'columns', // This is the selectedColumns select box, but the id has to be 'columns' so that Spring MVC binds the request parameter to the Java object automatically.
            'name': 'columns',
            'class': 'form-control',
            'size': scriptVariables['selectedColumnsSize']
        };

        let selectSelectedColumns = createNewElement('select', attributesMapSelectedColumns, scriptVariables['selectedColumns']);
        let labelSelectedColumns = createNewElement('label', {'for': 'selectedColumns'});
        labelSelectedColumns.innerHTML = 'Selected Columns';

        let selectedColumnsDiv = createNewElement('div', {
            'id': 'selectedColumnsDiv',
            'class': 'selected-columns-div'
        });
        selectedColumnsDiv.appendChild(labelSelectedColumns);
        selectedColumnsDiv.appendChild(selectSelectedColumns);

        let parentDiv = createNewElement('div', {
            'id': 'tableColumns',
            'name': 'tableColumns',
            'class': 'table-columns',
            'width': '600px',
            'height': '191px'
        }, null);
        parentDiv.appendChild(availableColumnsDiv);
        parentDiv.insertAdjacentElement('beforeend', addRemoveButtonsDiv);
        parentDiv.insertAdjacentElement('beforeend', selectedColumnsDiv);

        return parentDiv;
    }
}

function renderCriteriaHTML() {
    let pEl = createNewElement('p', {
        'id': 'criteriaPEl',
        'name': 'criteriaPEl'
    }, null);
    //pEl.innerHTML = 'Criteria';

    let addRootCriteriaButton = createNewElement('button', {
        'id': 'addRootCriteriaButton',
        'name': 'addRootCriteriaButton',
        'type': 'button',
        'class': 'add-root-criteria-button'
    }, null);
    addRootCriteriaButton.innerHTML = 'Add Root Criteria';
    addRootCriteriaButton.onclick = function () {
        addCriteria(null);
    };

    let pCriteriaAnchorEl = createNewElement('p', {'id': 'criteriaAnchor'}, null);

    let attributesMap = {
        'id': 'criteria',
        'name': 'criteria',
        'class': 'criteria-div'
    };
    let div = createNewElement('div', attributesMap, null);
    div.appendChild(pEl);
    div.appendChild(addRootCriteriaButton);
    div.appendChild(pCriteriaAnchorEl);

    return div;
}

function renderOtherOptionsHTML() {
    let distinctEl = null;
    let orderByEl = null;
    let groupByEl = null;
    let suppressNullsEl = null;
    let limitEl = null;
    let offsetEl = null;
    let parentDiv = createNewElement('div', {'id': 'otherOptionsDiv', 'name': 'otherOptioinsDiv', 'class': 'other-options-div'}, null);

    if (scriptVariables['distinct']) {
        let attributesMap = {
            'id': 'distinct',
            'name': 'distinct',
            'type': 'checkbox',
            'class': 'custom-control-input'
        };
        distinctEl = createNewElement('input', attributesMap, null);
        labelDistinct = createNewElement('label', {'for': 'distinct'}, null);
        labelDistinct.innerHTML = 'Distinct';
        parentDiv.appendChild(labelDistinct);
        parentDiv.appendChild(distinctEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    if (scriptVariables['orderByColumns'] !== null) {
        let attributesMap = {
            'id': 'orderBy',
            'name': 'orderBy',
            'class': 'form-control'
        };
        orderByEl = createNewElement('select', attributesMap,  scriptVariables['orderByColumns']);
        labelOrderBy = createNewElement('label', {'for': 'orderBy'}, null);
        labelOrderBy.innerHTML = 'Order By';
        parentDiv.appendChild(labelOrderBy);
        parentDiv.appendChild(orderByEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    if (scriptVariables['groupByColumns'] !== null) {
        let attributesMap = {
            'id': 'groupBy',
            'name': 'groupBy',
            'class': 'form-control'
        };
        groupByEl = createNewElement('select', attributesMap,  scriptVariables['groupByColumns']);
        labelGroupBy = createNewElement('label', {'for': 'groupBy'}, null);
        labelGroupBy.innerHTML = 'Group By';
        parentDiv.appendChild(labelGroupBy);
        parentDiv.appendChild(groupByEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    if (scriptVariables['suppressNulls']) {
        let attributesMap = {
            'id': 'suppressNulls',
            'name': 'suppressNulls',
            'type': 'checkbox',
            'class': 'custom-control-input'
        };
        suppressNullsEl = createNewElement('input', attributesMap, null);
        labelSuppressNulls = createNewElement('label', {'for': 'suppressNulls'}, null);
        labelSuppressNulls.innerHTML = 'Suppress Null Records';
        parentDiv.appendChild(labelSuppressNulls);
        parentDiv.appendChild(suppressNullsEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    if (scriptVariables['limitChoices'] !== null) {
        let attributesMap = {
            'id': 'limit',
            'name': 'limit',
            'class': 'form-control'
        };
        limitEl = createNewElement('select', attributesMap, scriptVariables['limitChoices']);
        labelLimit = createNewElement('label', {'for': 'limit'}, null);
        labelLimit.innerHTML = 'Limit  ';
        parentDiv.appendChild(labelLimit);
        parentDiv.appendChild(limitEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    if (scriptVariables['offsetChoices'] !== null) {
        let attributesMap = {
            'id': 'offset',
            'name': 'offset',
            'class': 'form-control'
        };
        offsetEl = createNewElement('select', attributesMap, scriptVariables['offsetChoices']);
        labelOffset = createNewElement('label', {'for': 'offset'}, null);
        labelOffset.innerHTML = 'Offset  ';
        parentDiv.appendChild(labelOffset);
        parentDiv.appendChild(offsetEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    return parentDiv;
}

// function renderQueryButtonHTML() {
//     let attributesMap = {
//         'id': 'runQuery',
//         'name': 'runQuery',
//         'type': 'button',
//         'class': 'run-query-button'
//     };
//     let runQueryButton = createNewElement('button', attributesMap, null);
//     runQueryButton.innerHTML = 'Run Query';
//     runQueryButton.onclick = function () {
//         scriptVariables['formSubmissionFunction']();
//     }

//     let div = createNewElement('div', {'id': 'runQueryDiv', 'name': 'runQueryDiv', 'class': 'run-query-div'}, null);
//     div.appendChild(runQueryButton);

//     return div;
// }

function syncSelectOptionsWithDataModel(HtmlId, dataProperty, ) {
    clearOptions(HtmlId);
    addOptionsToSelectElement(HtmlId, dataProperty);
}

function addOptionsToSelectElement(HtmlId, dataProperty) {
    if (dataProperty !== null) {
        let selectElement = (typeof(HtmlId) === 'object') ? HtmlId : document.getElementById(HtmlId);
        for (var i=0; i<dataProperty.length; i++) {
            var option = document.createElement("option");
            option.text = dataProperty[i];
            option.value = dataProperty[i];
            if (typeof(HtmlId) === 'object') {
                selectElement.add(option);
            } else {
                document.getElementById(HtmlId).add(option);
            }
        }
    }
}

function clearOptions(HtmlId) {
    let selectElement;
    if (typeof(HtmlId) === 'object') {
        selectElement = HtmlId;
    } else {
        selectElement = document.getElementById(HtmlId);
    }

    // If the Select element exists, then remove all options.
    if (selectElement !== null) {
        if (selectElement.options.length === 0)
            return;

        for (var i=selectElement.options.length-1; i>=0; i--) {
            selectElement.remove(i);
        }
    }
}

//HtmlId : the id of the DOM select element
//textOrIndeces : 'text' to add the option element values.  Anything else to add option indeces.
//type : 'json' to return the selected options in JSON.  Anything else to return the selected options in an array.
function getSelectedOptionsAsJSON(HtmlId, textOrIndeces='text') {
    let results = [];
    let select = document.getElementById(HtmlId);
    for (var i=0; i<select.options.length; i++) {
        if (select.options[i].selected) {
            if (textOrIndeces === 'text') {
                let obj = {};
                obj['data'] = select.options[i].text;
                results.push(obj);
            } else {
                let obj = {};
                obj['data'] = i;
                results.push(obj);
            }
        }
    }
    return results;
}

function getSelectedOptionsAsArray(HtmlId, textOrIndeces='text') {
    let results = [];
    let select = document.getElementById(HtmlId);
    for (var i=0; i<select.options.length; i++) {
        if (select.options[i].selected) {
            (textOrIndeces === 'text') ? results.push(select.options[i].text) : results.push(i);
        }
    }
    return results;
}

function getParentTable() {
    let parentTables = $('#table option:selected').map(function(){
        return this.value;
    }).get();

    let targetJoinTables = [];

    let targetJoinTablesEls = document.getElementsByClassName('join-row');
    for (var i=0; i<targetJoinTablesEls.length; i++) {
        for (var j=0; j<targetJoinTablesEls[i].children.length; j++) {
            let element = targetJoinTablesEls[i].children[j];
            var rex = /joins\d\.targetTable/;
            if (rex.test(element.id)) {
                targetJoinTables.push(element.value);
            }
        }
    }

    if (parentTables.length - targetJoinTables.length !== 1) {
        throw `You have ${parentTables.length} table(s) chosen, but ${targetJoinTables.length} unique table(s) in your target joins.  
        You should have one less target join tables than the number of tables you choose.`
    }

    for (var i=0; i<parentTables.length; i++) {
        if (! targetJoinTables.includes(parentTables[i])) {
            return parentTables[i];
        }
    }
    // parentTables.forEach(parentTable => {
    //     if (! targetJoinTables.includes(parentTable)) {
    //         return parentTable;
    //     }
    // });

}

function buildRequestData() {
    // Serialize form's select and input elements except for table select element, which is added manually below.
    let requestData = $('#queryBuilder select, #queryBuilder input').not('#table').serialize()

    // Add selected columns manually to request data.
    let selectedColumns = document.getElementById('columns').options;
    for (var i=0; i<selectedColumns.length; i++) {
        requestData += '&columns=' + selectedColumns[i].value;
    }

    // Add parent table as 'table' request attribute.
    try {
        let parentTable = getParentTable();
        requestData += '&table=' + parentTable;
        return requestData;
    } catch(ex) {
        alert(ex);
        return null; // todo:  handle null return value in calling code.
    }
}

//===========================================================================
//                     START OF SCRIPT
//===========================================================================

renderHTML();

if (scriptVariables['landingDivs'] !== null) {
    hideAllDivsExcept(scriptVariables['landingDivs']);
}

$(document).ready(function() {

    setTimeout(function() {
        if (scriptVariables['getQueryTemplateEndpoint'] !== null) {
            getQueryTemplates();
        }

        if (scriptVariables['getSchemaEndpoint'] !== null) {
            getSchemas();
        } else if (scriptVariables('getTablesEndpoint') !== null) {
            getTables();
        }
    }, scriptVariables['phaseOutMilliseconds'] + 200);
});
