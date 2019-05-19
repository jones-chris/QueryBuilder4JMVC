const scriptVariables = {
    // The element to append the queryBuilder form to.  Set to the 1) element id to append to element or 2) null ot append
    // to end of document.
    renderHtmlAnchorElement : 'queryBuilderAnchor',
    // The base URL where the join png images are held.  Set the value to 'https://s3.amazonaws.com/qb4j-ui/images/' for official qb4j images.
    htmlFragmentUrl: 'https://s3.amazonaws.com/qb4j-ui/v1/',
    // Set contents of array to any of the following: 'queryTemplatesDiv', 'schemasDiv', 'tablesDiv', 'joinsDiv', 'columnsDiv',
    // 'criteriaDiv', or 'otherOptionsDiv' in order to show these divs when the page is rendered.
    landingDivs : ['schemasDiv', 'tablesDiv'],
    // set to the number of milliseconds that should elapse when hiding/showing the various divs.
    phaseOutMilliseconds : 200,
    // set to your query template endpoint
    getQueryTemplateEndpoint : '/queryTemplates',
    // set to your schemas endpoint
    getSchemaEndpoint : "/schemas",
    // set to your tables endpoint
    getTablesEndpoint : "/tablesAndViews/",
    // set to your table columns endpoint
    getColumnsEndpoint : "/columns",
    // set to your column members endpoint
    columnMembersEndpoint : '/columns-members/',
    // set to your query endpoint
    formSubmissionEndpoint : "/query",
    // set to your save query template endpoint
    saveAsQueryTemplateEndpoint : "/saveQueryTemplate",
    // set to the HTTP method that your query endpoint above accepts
    formMethod : "POST",
    // set to [] to render or null to not render.
    queryTemplates : [],
    // set to [] to render
    schemas : [],
    // set to [] to render
    createTables : true,
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
    // set to true to render
    saveAsQueryTemplate : true,
    //set to true to render
    suppressNulls : true,
    // set to [] to render
    limitChoices : ['', 5, 10, 50, 500],
    // set to [] to render
    offsetChoices : ['', 5, 10, 50, 500],
    queryTemplatesSize : 5,
    schemasSize : 5,
    tablesSize : 5,
    availableColumnsSize : 30,
    selectedColumnsSize : 30,
    orderByColumnsSize : 10,
    groupByColumnsSize : 10,
    // assign a function here to handle form submission.
    formSubmissionFunction : function () {
        let requestData;
        try {
            requestData = buildRequestData();
        } catch (ex) {
            alert(ex);
            return;
        }

        $.ajax({
            type: 'POST',
            url: scriptVariables['formSubmissionEndpoint'],
            data: requestData,
            contentType: 'application/json',
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
    }
};

// Do not change - INTERNAL USE ONLY.
const internalUseVariables = {
    joinImagesFilePaths:  [
        {'name': 'LEFT_EXCLUDING',         'file_path': scriptVariables.htmlFragmentUrl + 'left_join_excluding.png'},
        {'name': 'LEFT',                   'file_path': scriptVariables.htmlFragmentUrl + 'left_join.png'},
        {'name': 'INNER',                  'file_path': scriptVariables.htmlFragmentUrl + 'inner_join.png'},
        {'name': 'RIGHT',                  'file_path': scriptVariables.htmlFragmentUrl + 'right_join.png'},
        {'name': 'RIGHT_EXCLUDING',        'file_path': scriptVariables.htmlFragmentUrl + 'right_join_excluding.png'},
        {'name': 'FULL_OUTER',             'file_path': scriptVariables.htmlFragmentUrl + 'full_outer_join.png'},
        {'name': 'FULL_OUTER_EXCLUDING',   'file_path': scriptVariables.htmlFragmentUrl + 'full_outer_join_excluding.png'}
    ]
};

//===============================================================================================
// Column Members HTML fragment and JavaScript functions
//===============================================================================================

// Sets all column members modal variables and elements back to their default state.
function closeColumnMembers(modalId, parentId) {
    $('#' + modalId).remove();
    $('#' + parentId).show();
}

// modalHtmlId:  the HTML id of the modal.
function getModalCurrentOffset(modalHtmlId) {
    return document.getElementById(modalHtmlId + '-currentOffset');
}

// modalHtmlId:  the HTML id of the modal.
function setModalCurrentOffset(modalHtmlId, newValue) {
    return document.getElementById(modalHtmlId + '-currentOffset').setAttribute('value', newValue);
}

// modalHtmlId:  the HTML id of the modal.
function getModalAscending(modalHtmlId) {
    return document.getElementById(`${modalHtmlId}-ascending`).value;
}

// modalHtmlId:  the HTML id of the modal.
function getModalSearch(modalHtmlId) {
    return document.getElementById(`${modalHtmlId}-search`).value;
}

// modalHtmlId:  the HTML id of the modal.
function getModalPriorPageButton(modalHtmlId) {
    return document.getElementById(`${modalHtmlId}-priorPage`);
}

// modalHtmlId:  the HTML id of the modal.
function getModalNextPageButton(modalHtmlId) {
    return document.getElementById(`${modalHtmlId}-nextPage`);
}

// priorOrNext parameter should be either true (get prior page) or false (get next page).
function getPageMembers(modalId, isPrior) {
    // Create endpoint
    let schema = null;
    let parentElementId = document.getElementById(`${modalId}-parentId`).value;
    let parentElementColumn = document.getElementById(parentElementId.split('.')[0] + '.column').value;
    let table = parentElementColumn.split('.')[0];
    let column = parentElementColumn.split('.')[1];
    let endpoint = scriptVariables.columnMembersEndpoint + `${schema}/${table}/${column}/`;

    // Create query string
    let limit = parseInt(document.getElementById(`${modalId}-limit`).value);
    if (isPrior === true) {
        adjustColumnMembersCurrentOffset(-limit);
    }
    let offset = getModalCurrentOffset(modalId).getAttribute('value');
    let ascending = getModalAscending(modalId);
    let search = getModalSearch(modalId);
    let queryString = `limit=${limit}&offset=${offset}&ascending=${ascending}`;
    if (search !== '') {
        queryString += `&search=${search}`;
    }

    // Call endpoint
    sendAjaxRequest(endpoint,
        queryString,
        'GET',
        function (data) {
            // If data's length less than the limit, we have reached the end of the column members.  We do NOT want to update the select options.
            // Else if data's length is less than the limit, then we have reached the end of the column members.  We do want to update the select options.
            // Else there is likely still more data to get.  We do want to update the select options and current offset if we retrieved the next page.
            if (data.length === 0) {
                getModalNextPageButton(modalId).disabled = true;
                getModalPriorPageButton(modalId).disabled = false;
                alert('You have reached the last page.  There are no additional column members to retrieve.');
            } else if (data.length < limit) {
                getModalNextPageButton(modalId).disabled = true;
                getModalPriorPageButton(modalId).disabled = false;
                syncSelectOptionsWithDataModel(`${modalId}-availableMembers`, data);
                alert('You have reached the last page.  There are no additional column members to retrieve.');
            } else {
                getModalNextPageButton(modalId).disabled = false;
                syncSelectOptionsWithDataModel(`${modalId}-availableMembers`, data);
                if (! isPrior){
                    adjustColumnMembersCurrentOffset(limit, modalId);
                }
            }

            // Disable or enable prior page button based on whether current offset is 0 or not.
            if (getModalCurrentOffset(modalId).getAttribute('value') === 0) {
                document.getElementById(`${modalId}-priorPage`).disabled = true;
            } else {
                document.getElementById(`${modalId}-priorPage`).disabled = false;
            }
        }
    );
}

function adjustColumnMembersCurrentOffset(adjustment, modalHtmlId) {
    let currentOffsetElement = getModalCurrentOffset(modalHtmlId);
    let currentOffsetValue = parseInt(currentOffsetElement.value);
    let newOffsetValue = currentOffsetValue += adjustment;
    if (newOffsetValue < 0) {
        newOffsetValue = 0;
    }
    setModalCurrentOffset(modalHtmlId, newOffsetValue);
}

// selectedMembersModalId:  The HTML id of the modal's selectedMembers.
// modalParentId:  The HTML id of the modal's parent reference.
function setCriteriaFilterWithColumnMembers(selectedMembersModalId, modalParentId) {
    // Get modal's selectedMembers and stringify.
    let options = document.getElementById(selectedMembersModalId).options;
    let stringifiedMembers = "";
    for (let i=0; i<options.length; i++) {
        stringifiedMembers += options[i].value + ',';
    }

    // Remove trailing ','
    stringifiedMembers = stringifiedMembers.substring(0, stringifiedMembers.length - 1);

    // Set criterion's filter to stringifiedMembers.
    // document.getElementById(`criteria${internalUseVariables.activeCriteriaIdForColumnMembers}.filter`).value = stringifiedMembers;
    document.getElementById(modalParentId).value = stringifiedMembers;
}

// htmlId:  The HTML id of the element to add the data to.
// data:  The variable in the "scriptVariables" object that contains the data to be added as "option" elements.
function addSelectOptions(htmlId, data) {
    let select = document.getElementById(htmlId);
    for (let i in data) {
        let option = document.createElement('option');
        option.value = data[i];
        option.innerHTML = data[i];

        select.add(option);
    }
}

//===============================================================================================
// General JavaScript functions
//===============================================================================================

function sendAjaxRequest(endpoint, data, method, successCallbackFunction, doneCallbackFunction=function(){},
                         contentType='application/x-www-form-urlencoded;charset=UTF-8') {
    $.ajax({
        method: method,
        url: endpoint,
        data: data,
        contentType: contentType,
        success: function(responseText) {
            successCallbackFunction(responseText);
        },
        error: function(jqXHR, textStatus) {
            alert(jqXHR.responseText);
            console.log(textStatus);
        },
        dataType: 'json'
    }).done(function() {
        doneCallbackFunction();
    });
}

function getQueryTemplates() {
    sendAjaxRequest(scriptVariables['getQueryTemplateEndpoint'],
        null,
        "GET",
        function(queryTemplatesData) {
            syncSelectOptionsWithDataModel('queryTemplates', queryTemplatesData);
        }
    );
}

function getQueryTemplateById(id, successCallbackFunction) {
    sendAjaxRequest(scriptVariables['getQueryTemplateEndpoint'] + '/' + id,
        null,
        "GET",
        successCallbackFunction
    );
}

function getSchemas() {
    sendAjaxRequest(scriptVariables['getSchemaEndpoint'],
        null,
        "GET",
        function(schemasData) {
            syncSelectOptionsWithDataModel('schemas', schemasData);
        });
}

function getTables(schema) {
    sendAjaxRequest(scriptVariables['getTablesEndpoint'] + schema,
        null,
        "GET",
        function (tablesData) {
            syncSelectOptionsWithDataModel('table', tablesData);
        });
}

function getAvailableColumns(schema, tablesArray, doneCallbackFunction=function(){}) {
    let tableParamString = tablesArray.join('&');
    sendAjaxRequest(scriptVariables['getColumnsEndpoint'] + '/' + schema + "/" + tableParamString,
        null,
        "GET",
        function(columnsData) {
            syncSelectOptionsWithDataModel('availableColumns', columnsData);
        },
        doneCallbackFunction
    );
}

// members:  (JSON) JSON of members to add to data model.
// dataModel:  (string) Name of data model array to add members to.
// HtmlId:  (boolean) Id of HTML select element to sync with dataModel.
function addSelectedColumns(members, dataModel, HtmlId) {
    fillArrayProperty(dataModel, members, false);
    syncSelectOptionsWithDataModel(HtmlId, scriptVariables[dataModel]);
}

// members:  Array of indeces to remove from dataModel.
// dataModel:  Name of data model array to remove members from.
// HtmlId:  Id of HTML select element to sync with dataModel.
function removeSelectedColumn(memberIndeces, dataModel, HtmlId) {
    deleteArrayPropertyMembers(dataModel, memberIndeces);
    syncSelectOptionsWithDataModel(HtmlId, scriptVariables[dataModel]);
}

// htmlId:  The Id of the HTML select element that contains the option element to move up.
// dataModel:  The name of the data model to update and sync with the htmlId element with.
// isUp:  true if moving column up (increasing index) and false if moving column down (decreasing index).
function moveSelectedColumn(htmlId, dataModel, isUp) {
    // Get the index of the array item to move up.  It's assumed that there is only one element selected.
    // todo:  add functionality for multiple items to be selected.
    let index = 0;
    let options = document.getElementById(htmlId).options;
    for (let i=0; i<options.length; i++) {
        if (options[i].selected === true) {
            index = i;
            break;
        }
    }

    if (isUp) {
        if (index === 0) { return null; }

        // get destination item
        let itemToDelete = options[index - 1];

        // set destination item to current item
        let itemToMove = options[index];

        // Switch the items order.
        document.getElementById(htmlId).replaceChild(itemToMove, itemToDelete);
        document.getElementById(htmlId).insertBefore(itemToDelete, itemToMove.nextSibling);
    } else {
        if (index === options.length - 1) { return null; }

        // get destination item
        let itemToDelete = options[index + 1];
        // var itemToDelete = scriptVariables[dataModel][index + 1];

        // set destination item to current item
        let itemToMove = options[index];

        // insert destination item at current item's index
        document.getElementById(htmlId).replaceChild(itemToMove, itemToDelete);
        document.getElementById(htmlId).insertBefore(itemToDelete, itemToMove);
    }
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

        //add criteria
        criteria[i].children[8].id = 'addCriteria-' + newId; //id
        // criteria[i].children[7].name = 'criteria[' + newId + '].endParenthesis'; //name

        //remove criteria
        criteria[i].children[9].id = 'removeCriteria-' + newId; //id

        //show column members
        criteria[i].children[10].id = 'columnMembers-' + newId; //id
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
// criterion:  The object that encapsulates all criterion data that will be used to create a new Criteria HTML fragment and add it to the DOM.
async function addCriteria(parentNode, criterion=null) {
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

    // Now that we have determined the id and parentId variables, get the criterion html fragments and run eval to interpolate them.
    let htmlTemplate = document.createElement('template');
    let criterionBody = await loadHtmlFragment(scriptVariables.htmlFragmentUrl + 'criterion-body.html');
    criterionBody = eval(criterionBody);
    htmlTemplate.innerHTML = criterionBody;

    syncSelectOptionsWithDataModel(htmlTemplate.content.getElementById(`criteria${id}.column`), getOptionsAsArray('availableColumns'));

    htmlTemplate.content.getElementById(`addCriteria-${id}`).onclick = function() {
        let thisCriteriaId = this.id.slice(-1);
        let thisCriteriaDiv = document.getElementById('row.' + thisCriteriaId);
        addCriteria(thisCriteriaDiv);
    };

    htmlTemplate.content.getElementById(`removeCriteria-${id}`).onclick = function() {
        let criteriaId = this.parentNode.id.slice(-1);
        document.getElementById(`row.${criteriaId}`).remove();
        renumberCriteria(criteriaId, 'remove');
        reindentCriteria();
    };

    htmlTemplate.content.getElementById(`columnMembers-${id}`).onclick = function() {
        let criteriaId = parseInt(this.parentElement.id.slice(-1));
        let tableAndColumn = document.getElementById(`criteria${criteriaId}.column`).value;
        if (tableAndColumn === null) {
            alert('Please choose a column before choosing Column Members');
        }

        addColumnMembersHTML(`criteria${id}.filter`);
    };

    // If a criteria object was passed to this method, then set HTML element values now that div is finished,
    // but not attached to DOM yet.
    if (criterion !== null) {
        htmlTemplate.content.getElementById(`criteria${id}.conjunction`).value = criterion.conjunction;
        htmlTemplate.content.getElementById(`criteria${id}.column`).value = criterion.column;
        htmlTemplate.content.getElementById(`criteria${id}.operator`).value = criterion.operator;
        htmlTemplate.content.getElementById(`criteria${id}.filter`).value = criterion.filter;
    }

    // Insert template into the DOM
    if (parentNode === null) {
        document.getElementById('criteriaAnchor').prepend(htmlTemplate.content);
    } else {
        parentNode.insertAdjacentElement('afterend', htmlTemplate.content.firstChild.nextElementSibling)
    }

    reindentCriteria();
}

function renderHTML(beforeNode, queryTemplate=null) {
    // If queryTemplate is not null, then we are wiping the current queryBuilder form so that it can be rendered again
    // with the queryTemplate data.
    if (queryTemplate !== null) {
        $('#queryBuilder').remove();
    }

    var form = document.createElement('form');
    form.setAttribute('id', 'queryBuilder');
    form.setAttribute('name', 'queryBuilder');
    form.setAttribute('action', scriptVariables['formSubmissionEndpoint']);
    form.setAttribute('method', scriptVariables['formMethod']);

    var el = null;

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

    if (beforeNode === undefined || beforeNode === null) {
        document.body.appendChild(form);
    } else {
        document.getElementById(beforeNode).appendChild(form);
    }

    // Now that the new form has been rendered, hide all divs except the landing divs listed in scriptVariables.
    if (scriptVariables['landingDivs'] !== null) {
        hideAllDivsExcept(scriptVariables['landingDivs']);
    }

    // Now that the new form has been rendered, populate the form with the queryTemplate data if the queryTemplate is not
    // null.
    if (queryTemplate !== null) {
        Array.from(document.getElementById('queryTemplates').children).forEach((child) => {
            child.selected = (child.value === queryTemplate.name);
        });

        // todo add schema into the SelectStatement class so that it can be added here based on the queryTemplate data.
        document.getElementById('schemas').value = 'null';

        // get table and all unique join target tables...add them to the tables array.
        let tables = [];
        tables.push(queryTemplate.table);
        queryTemplate.joins.forEach((join) => {
            if (! tables.includes(join.targetTable)) {
                tables.push(join.targetTable);
            }
        });
        syncSelectOptionsWithDataModel('table', tables);
        Array.from(document.getElementById('table').children).forEach((child) => {
            child.selected = true;
        });

        function populateFormWithQueryTemplateData() {
            getQueryTemplates();
            syncSelectOptionsWithDataModel('schemas', ['null']);
            // table columns should update automatically after tables array is updated.  Update selected columns.
            // scriptVariables.selectedColumns = queryTemplate.columns;
            // syncSelectOptionsWithDataModel('columns', scriptVariables.selectedColumns);
            syncSelectOptionsWithDataModel('columns', queryTemplate.columns);

            // update criteria...call reindent criteria method.
            queryTemplate.criteria.forEach((criterion) => {
                let parentCriteriaId = criterion.parentId;
                let parentNode = null;
                if (parentCriteriaId !== undefined && parentCriteriaId !== null) {
                    parentNode = document.getElementById(`row.${parentCriteriaId}`);
                }
                addCriteria(parentNode, criterion);
            });

            // Get joins and add to joins array
            queryTemplate.joins.forEach((join) => {
                addJoin(join);
            });

            // Update other options.
            document.getElementById('distinct').checked = queryTemplate.distinct;
            document.getElementById('suppressNulls').checked = queryTemplate.suppressNulls;
            document.getElementById('limit').value = queryTemplate.limit;
            document.getElementById('offset').value = queryTemplate.offset;
        }

        // Now that we have the tables, get the available columns.
        getAvailableColumns('null', tables, populateFormWithQueryTemplateData);
    }
}

async function loadHtmlFragment(filePath) {
    let result = await fetch(filePath);
    if (result.ok) {
        return await result.text();
    }
}

// parentId:  the id of the parent element to be used in eval() interpolation.  This is the element that will receive the
//            user choices from the child modal.
// parentModal:  the id of the parent modal that contains the parentId to be used in eval() interpolation.  This is only
//               interpolated so that the parentModal can be shown when the child modal is closed.
async function addColumnMembersHTML(parentId=null) {
    // Get id to use in eval() interpolation.
    let id = 0;
    $('#modals div').each(function() {
        if (this.id.includes('cm-modal-')) {
            let idLength = $(this)[0].id.length;
            let thisId = parseInt($(this)[0].id[idLength - 1]);
            if (thisId >= id) {
                id = thisId + 1;
            }
        }
    });

    // let element = document.getElementById(htmlId);
    let element = document.getElementById('modals');
    let columnMembersHTML = await loadHtmlFragment(scriptVariables.htmlFragmentUrl + 'column-members.html');
    columnMembersHTML = eval(columnMembersHTML);
    element.insertAdjacentHTML('afterend', columnMembersHTML);
    $(`#cm-modal-${id}`).show();
}

// parentId:  The id of the parent HTML element to be used in eval() interpolation.
// htmlElToHide:  The id of the HTML element to hide.  Defaults to null, which will not hide an element.
async function addSubQueryHTML(parentId, parentModalId=null) {
    // Get id to use in eval() interpolation.
    let id = 0;
    $('#modals div').each(function() {
        if (this.id.includes('cm-modal-') || this.id.includes('sq-modal-')) {
            let idLength = $(this)[0].id.length;
            let thisId = parseInt($(this)[0].id[idLength - 1]);
            if (thisId >= id) {
                id = thisId + 1;
            }
        }
    });

    // Get subQuery Container div.
    let element = document.getElementById('modals');
    let subQueryHTML = await loadHtmlFragment(scriptVariables.htmlFragmentUrl + 'subquery-container.html');
    subQueryHTML = eval(subQueryHTML);
    element.insertAdjacentHTML('afterend', subQueryHTML);

    // Add query template options to select element.
    addSelectOptions(`sq-modal-${id}-query-templates`, getOptionsAsArray('queryTemplates'));

    if (parentModalId != null) {
        $('#' + parentModalId).hide();
    }

    $(`#sq-modal-${id}`).show();
}

// htmlId:  The HTML id of the subQuery modal to be used in the id of the new subQuery parameters.
// parentHtmlId:  The HTML id of the subQuery modal that the subQuery parameters should be added to.
// subQueryId:  The id of the query template that will be used to retrieve the query template and it's parameters.
function renderSubQueryParameters(htmlId, parentHtmlId, subQueryId) {
    getQueryTemplateById(subQueryId, async function(queryTemplate) {
        let criteriaParameterKeys = Object.keys(queryTemplate.criteriaParameters);
        let criteriaParameterValues = Object.values(queryTemplate.criteriaParameters);
        if (criteriaParameterKeys.length > 0) {
            let parameterHtml = await loadHtmlFragment(scriptVariables.htmlFragmentUrl + 'subquery-parameter.html');
            let parentHtmlElement = document.getElementById(parentHtmlId);

            for (let i=0; i<criteriaParameterKeys.length; i++) {
                // Assigned htmlId to id just so that interpolation works when eval() is called.
                let id = htmlId;
                let parameterId = i;
                let parameterName = criteriaParameterValues[i].name;
                let parameterDescription = criteriaParameterValues[i].description;
                let parameterColumn = criteriaParameterValues[i].column;
                let parameterHtmlEvaled = eval(parameterHtml);

                parentHtmlElement.insertAdjacentHTML('beforeend', parameterHtmlEvaled);
            }
        } else {
            alert("This query template does not contain any criteria parameters.")
        }
    });
}

// subQueryHtmlId:  The HTML id of the subQuery modal to create the subQuery call string.
// targetHtmlId:  The HTML id of the element who's value should be set to the subQuery call string.
// targetHtmlParentId:  The HTML id of the element that contains the targetHtmlId.  If non null, this parameter is used to show the container element.
function setTargetElementValueToSubQuery(subQueryHtmlId, targetHtmlId, targetHtmlParentId=null) {
    // get name of subQuery
    let subQueryName = document.getElementById(subQueryHtmlId + '-query-templates').value;

    // get all parameters (name and value)
    // let criteriaNames = $(`label[id^="${subQueryHtmlId}-parameter-"]`);
    let parameterNames = $(`[id^="${subQueryHtmlId}-parameter-name"]`);
    let parameterValues = $(`[id^="${subQueryHtmlId}-parameter-value"]`);
    let subQueryCall = `$${subQueryName}(`;

    for (var i=0; i<parameterNames.length; i++) {
        let paramName = parameterNames[i].innerHTML;
        let paramValue = parameterValues[i].value;
        subQueryCall += `${paramName}=${paramValue};`;
    }
    // Remove trialing ',' (only if parameters were added) and add ')'
    if (parameterNames.length > 0) {
        subQueryCall = subQueryCall.slice(0, subQueryCall.length - 1);
    }
    subQueryCall += ')';

    // Set targetHtmlId's value.
    let target = document.getElementById(targetHtmlId);
    if (target.nodeName.toUpperCase() === 'INPUT') {
        target.value = subQueryCall;
    } else if (target.nodeName.toUpperCase() === 'SELECT') {
        let newOptionEl = document.createElement('option');
        newOptionEl.value = subQueryCall;
        newOptionEl.innerHTML = subQueryCall;
        target.append(newOptionEl);
    } else {
        console.error('Unrecognized target element node name of ' + target.nodeName);
    }

    $('#' + subQueryHtmlId).remove();

    if (targetHtmlParentId != null) {
        $('#' + targetHtmlParentId).show();
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

function createNewElement(type, attributesMap, dataProperty=null, innerHtml=null) {
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
    let divs = [
        'queryTemplatesDiv',
        'tableColumns',
        'schemasDiv',
        'tablesDiv',
        'joinsDiv',
        'criteria',
        'otherOptionsDiv'
    ];

    // Todo:  condense these two for loops into one for loop.
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
    let saveAsQueryTemplateButton = null;

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

    if (scriptVariables['schemas'] !== null || scriptVariables.createTables) {
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

    if (scriptVariables.saveAsQueryTemplate) {
        let attributesMap = {
            'id': 'saveAsQueryTemplate',
            'name': 'saveAsQueryTemplate',
            'type': 'button',
            'class': 'save-as-query-template-button'
        };
        saveAsQueryTemplateButton = createNewElement('button', attributesMap, null);
        saveAsQueryTemplateButton.innerHTML = 'Save As Query Template';
        saveAsQueryTemplateButton.onclick = function () {
            let requestData;
            try {
                requestData = buildRequestData();
            } catch (ex) {
                alert(ex);
                return;
            }

            sendAjaxRequest(scriptVariables.saveAsQueryTemplateEndpoint,
                requestData,
                "POST",
                function() {
                    alert('Query Saved Successfully!');
                },
                null,
                'application/json');
        };
    }

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
    if (saveAsQueryTemplateButton !== null) div.appendChild(saveAsQueryTemplateButton);
    div.appendChild(runQueryButton);

    return div;
}

function renderQueryTemplatesHTML() {
    if (scriptVariables['queryTemplates'] !== null) {
        let attributesMap = {
            'id': 'queryTemplates',
            'name': 'queryTemplates'
        };
        let select = createNewElement('select', attributesMap, scriptVariables['queryTemplates']);
        select.onchange = function() {
            let queryTemplateId = document.getElementById('queryTemplates').value;
            getQueryTemplateById(queryTemplateId, function(queryTemplate) {
                renderHTML(scriptVariables.renderHtmlAnchorElement, queryTemplate);
            });
        };

        let div = createNewElement('div', {'id': 'queryTemplatesDiv', 'class': 'query-templates-div'});
        div.appendChild(select);

        return div;
    }
}

function renderSchemaHTML() {
    if (scriptVariables['schemas'] !== null) {
        let attributesMap = {
            'id': 'schemas',
            'name': 'schemas',
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
    if (scriptVariables.createTables) {
        let attributesMap = {
            'id': 'table', //TODO change this back to 'tables' once qb4j can handle multiple tables.
            'name': 'table',
            'multiple': 'true',
            'size': scriptVariables['tablesSize']
        };
        let select = createNewElement('select', attributesMap, getTables('null'));
        select.onchange = function() {
            let schema = document.getElementById('schemas').value;
            let tables = getOptionsAsArray('table', 'text', true);
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
        // Create parent div for all joins.
        let joinsDiv = createNewElement('div', {
            'id': 'joinsDiv',
            'class': 'joins-div'
        });

        let addJoinButton = createNewElement('button', {
            'id': 'addJoin',
            'name': 'addJoin',
            'type': 'button'
        });
        addJoinButton.innerHTML = 'Add Join';
        addJoinButton.onclick = function() {
            addJoin();
        };

        joinsDiv.appendChild(addJoinButton);

        return joinsDiv;
    }
}

function getJoinsDivMaxId() {
    let maxId = 0;
    $('#joinsDiv div').each(function() {
        let idLength = $(this)[0].id.length;
        let id = parseInt($(this)[0].id[idLength - 1]);
        if (id >= maxId) {
            maxId = id + 1;
        }
    });

    return maxId;
}

function getJoinImageFilePath(joinName) {
    let images = internalUseVariables.joinImagesFilePaths;
    for (let i in images) {
        if (images[i].name === joinName) {
            return images[i].file_path;
        }
    }
    return null;
}

function getNextJoinImageFilePath(joinName) {
    let returnObj = {};
    let images = internalUseVariables.joinImagesFilePaths;
    for (let i=0; i<images.length; i++) {
        if (images[i].name === joinName) {
            // If index is less than last index, then get file_path of index + 1.
            // Else (if index is last index), then get first file_path in array.
            if (i < images.length-1) {
                returnObj.name = images[i+1].name;
                returnObj.file_path = images[i+1].file_path;
                return returnObj;
            } else {
                returnObj.name = images[0].name;
                returnObj.file_path = images[0].file_path;
                return returnObj;
            }
        }
    }
    return null;
}

function addJoin(join=null) {
    let maxId = getJoinsDivMaxId();

    let newJoinDiv = createNewElement('div', {
        'id': `join-row${maxId}`,
        'name': `join-row${maxId}`,
        'class': 'join-row'
    });
    let newJoinType = createNewElement('input', {
        'id': `joins${maxId}.joinType`,
        'name': `joins[${maxId}].joinType`,
        'hidden': 'true',
        'value': (join === null) ? 'LEFT_EXCLUDING' : join.joinType // Note: The name must match the case of the Java enum class.
    });
    let newJoinImage = createNewElement('img', {
        'id': `joins${maxId}.image`,
        'name': `joins[${maxId}].image`,
        'src': (join === null) ? scriptVariables.htmlFragmentUrl + 'left_join_excluding.png' : getJoinImageFilePath(join.joinType),
        'width': '100',
        'height': '80'
    });
    newJoinImage.onclick = function() {
        // Set newJoinType and newJoinImage to next join name and file_path in array.
        let joinType = document.getElementById(`joins${maxId}.joinType`);
        let currentJoinName = joinType.value;
        let joinImage = document.getElementById(`joins${maxId}.image`);
        let imageAndFilePathObj = getNextJoinImageFilePath(currentJoinName);
        joinType.value = imageAndFilePathObj.name;
        joinImage.src = imageAndFilePathObj.file_path;
    };

    let parentTableColumns = (join === null) ? null : getTableColumns(join.parentTable);
    let newJoinParentColumn = createNewElement('select', {
        'id': `joins${maxId}.parentJoinColumns`,
        'name': `joins[${maxId}].parentJoinColumns`
    }, parentTableColumns);
    if (join !== null) {
        // Set the mandatory parent column element's value to the join's first parent column.
        newJoinParentColumn.value = join.parentJoinColumns[0];
    }

    let targetTableColumns = (join === null) ? null : getTableColumns(join.targetTable);
    let newJoinTargetColumn = createNewElement('select', {
        'id': `joins${maxId}.targetJoinColumns`,
        'name': `joins[${maxId}].targetJoinColumns`
    }, targetTableColumns);
    if (join !== null) {
        // Set the mandatory target column element's value to the join's first target column.
        newJoinTargetColumn.value = join.targetJoinColumns[0];
    }

    // slice() is to copy the 'tables' array.
    // let parentAndTargetJoinTables = scriptVariables['tables'].slice(0);
    let parentAndTargetJoinTables = getOptionsAsArray('table', 'text', true).slice(0);
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

    let newJoinAddParentAndTargetColumnButton = createNewElement('button', {
        'id': `joins${maxId}.addParentAndTargetColumn`,
        'name': `joins[${maxId}].addParentAndTargetColumn`,
        'class': 'add-parent-and-target-column',
        'type': 'button'
    });
    newJoinAddParentAndTargetColumnButton.innerHTML = '+';

    newJoinAddParentAndTargetColumnButton.onclick = function() {
        addParentAndColumnJoinColumns(maxId);
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
    firstParentAndTargetColumnDiv.appendChild(newJoinAddParentAndTargetColumnButton);
    newJoinDiv.appendChild(firstParentAndTargetColumnDiv);

    // Set elements' values if a join object was passed into the function.
    if (join !== null) {
        newJoinType.value = join.joinType;
        newJoinParentTable.value = join.parentTable;
        newJoinTargetTable.value = join.targetTable;
    }

    document.getElementById('joinsDiv').appendChild(newJoinDiv);

    // If a join object was passed into the function, then add a new parent and target join for each parent column (parent
    // and target column sizes should be the same and is checked on the server side).  Else, just add one new pair of parent
    // and target columns.  This can only be done after the newJoinDiv has been appended to the DOM, because the addParentAndColumnJoinColumns()
    // method will append it's elements to the newJoinDiv element.  The for loop starts with an index of 1 because the 0th
    // element was added already when the newJoinParentColumn and newJoinTargetColumn elements were created in this method.
    if (join !== null) {
        for (let i=1; i<join.parentJoinColumns.length; i++) {
            addParentAndColumnJoinColumns(maxId, join.parentJoinColumns[i], join.targetJoinColumns[i]);
        }
    }
}

function addParentAndColumnJoinColumns(maxId, parentJoinColumn=null, targetJoinColumn=null) {
    let parentTableColumns;
    let targetTableColumns;
    if (parentJoinColumn === null && targetJoinColumn === null) {
        parentTableColumns = Array.from(document.getElementById(`joins${maxId}.parentJoinColumns`).options).map(option => option.value);
        targetTableColumns = Array.from(document.getElementById(`joins${maxId}.targetJoinColumns`).options).map(option => option.value);
    } else {
        parentTableColumns = getTableColumns(parentJoinColumn.split('.')[0]);
        targetTableColumns = getTableColumns(targetJoinColumn.split('.')[0]);
    }

    let anotherParentColumn = createNewElement('select', {
        'id': `joins${maxId}.parentJoinColumns`,
        'name': `joins[${maxId}].parentJoinColumns`
    }, parentTableColumns);
    if (parentJoinColumn !== null) {
        anotherParentColumn.value = parentJoinColumn;
    }

    let anotherTargetColumn = createNewElement('select', {
        'id': `joins${maxId}.targetJoinColumns`,
        'name': `joins[${maxId}].targetJoinColumns`
    }, targetTableColumns);
    if (targetJoinColumn !== null) {
        anotherTargetColumn.value = targetJoinColumn;
    }

    let equalSign = createNewElement('b');
    equalSign.innerHTML = ' = ';

    let joinColumnsDeleteButton = createNewElement('button', {
        'id': `joins${maxId}.deleteJoinColumnsButton`,
        'name': `joins[${maxId}].deleteJoinColumnsButton`,
        'class': 'delete-join-columns-button',
        'type': 'button'
    });
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
    return scriptVariables.availableColumns.filter(column => column.split('.')[0] === table);
}

function renderAvailableColumnsHTML() {
    if (scriptVariables['availableColumns'] !== null) {
        // Create Available Columns Div, Label, and Select elements
        let attributesMapAvailableColumns = {
            'id': 'availableColumns',
            'name': 'availableColumns',
            'multiple': 'true',
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
            addSelectedColumns(selectedColumns, 'selectedColumns', 'columns');
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
            removeSelectedColumn(selectedColumns, 'selectedColumns', 'columns');
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

    let modalsDiv = createNewElement('div', {'id': 'modals',
                                             'name': 'modals',
                                             'class': 'modals-div'}, null);

    let criteriaDiv = createNewElement('div', attributesMap, null);
    criteriaDiv.appendChild(pEl);
    criteriaDiv.appendChild(addRootCriteriaButton);
    criteriaDiv.appendChild(pCriteriaAnchorEl);
    criteriaDiv.appendChild(modalsDiv);

    return criteriaDiv;
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
        let labelDistinct = createNewElement('label', {'for': 'distinct'}, null);
        labelDistinct.innerHTML = 'Distinct';
        parentDiv.appendChild(labelDistinct);
        parentDiv.appendChild(distinctEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    if (scriptVariables['orderByColumns'] !== null) {
        let attributesMap = {
            'id': 'orderBy',
            'name': 'orderBy'
        };
        orderByEl = createNewElement('select', attributesMap,  scriptVariables['orderByColumns']);
        let labelOrderBy = createNewElement('label', {'for': 'orderBy'}, null);
        labelOrderBy.innerHTML = 'Order By';
        parentDiv.appendChild(labelOrderBy);
        parentDiv.appendChild(orderByEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    if (scriptVariables['groupByColumns'] !== null) {
        let attributesMap = {
            'id': 'groupBy',
            'name': 'groupBy'
        };
        groupByEl = createNewElement('select', attributesMap,  scriptVariables['groupByColumns']);
        let labelGroupBy = createNewElement('label', {'for': 'groupBy'}, null);
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
        let labelSuppressNulls = createNewElement('label', {'for': 'suppressNulls'}, null);
        labelSuppressNulls.innerHTML = 'Suppress Null Records';
        parentDiv.appendChild(labelSuppressNulls);
        parentDiv.appendChild(suppressNullsEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    if (scriptVariables['limitChoices'] !== null) {
        let attributesMap = {
            'id': 'limit',
            'name': 'limit'
        };
        limitEl = createNewElement('select', attributesMap, scriptVariables['limitChoices']);
        let labelLimit = createNewElement('label', {'for': 'limit'}, null);
        labelLimit.innerHTML = 'Limit  ';
        parentDiv.appendChild(labelLimit);
        parentDiv.appendChild(limitEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    if (scriptVariables['offsetChoices'] !== null) {
        let attributesMap = {
            'id': 'offset',
            'name': 'offset'
        };
        offsetEl = createNewElement('select', attributesMap, scriptVariables['offsetChoices']);
        let labelOffset = createNewElement('label', {'for': 'offset'}, null);
        labelOffset.innerHTML = 'Offset  ';
        parentDiv.appendChild(labelOffset);
        parentDiv.appendChild(offsetEl);
        parentDiv.appendChild(createNewElement('br', {}, null));
    }

    return parentDiv;
}

function syncSelectOptionsWithDataModel(HtmlId, dataProperty) {
    clearOptions(HtmlId);
    addOptionsToSelectElement(HtmlId, dataProperty);
}

//HtmlId : the 1) id of the select element to clear or the 2) select element to clear.
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

// htmlId:  either 1) the HTML id of the select element to add options to or 2) the select element object.
// data:  either 1) a flat array or 2) an array of flat objects (no nesting).
function addOptionsToSelectElement(htmlId, data) {
    if (data !== null) {
        let selectElement = (typeof(htmlId) === 'object') ? htmlId : document.getElementById(htmlId);
        for (let i=0; i<data.length; i++) {
            let option = document.createElement("option");
            if (typeof(data[i]) === 'object') {
                // Assumes flat json (no nesting).
                let keyValuePair = data[i];
                for (let key in keyValuePair) {
                    option.text = data[i][key];
                    option.value = data[i][key];
                }
            } else {
                option.text = data[i];
                option.value = data[i];
            }

            if (typeof(htmlId) === 'object') {
                selectElement.add(option);
            } else {
                document.getElementById(htmlId).add(option);
            }
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

function getOptionsAsArray(HtmlId, textOrIndeces='text', selectedOptionsOnly=false) {
    let results = [];
    let select = document.getElementById(HtmlId);
    for (var i=0; i<select.options.length; i++) {
        if (selectedOptionsOnly) {
            if (select.options[i].selected) {
                (textOrIndeces === 'text') ? results.push(select.options[i].text) : results.push(i);
            }
        } else {
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

    if (parentTables.length === 0) {
        throw 'You must select at least one table.'
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
}

// returnJson:  true (the default) to return the form data as JSON.  False to return the form data as x-www-form-urlencoded.
// Throws exception if getParentTable throws exception.
function buildRequestData(returnJson=true) {
    if (returnJson) {
        let json = {};
        json.table = getParentTable();
        json.columns = getOptionsAsArray('columns', 'text', false);

        // Criteria
        let criteriaDivs = document.getElementsByClassName('criteria-row');
        let criteriaArray = [];
        if (criteriaDivs !== undefined) {
            for (let i=0; i<criteriaDivs.length; i++) {
                let divId = criteriaDivs[i].id.slice(-1);
                let criteriaJson = {};
                criteriaJson.id = document.getElementById(`criteria${divId}.id`).value;
                criteriaJson.parentId = document.getElementById(`criteria${divId}.parentId`).value;
                criteriaJson.conjunction = document.getElementById(`criteria${divId}.conjunction`).value;
                criteriaJson.column = document.getElementById(`criteria${divId}.column`).value;
                criteriaJson.operator = document.getElementById(`criteria${divId}.operator`).value;
                criteriaJson.filter = document.getElementById(`criteria${divId}.filter`).value;
                criteriaArray.push(criteriaJson);
            }
        }
        json.criteria = criteriaArray;

        // Joins
        let joinsDivs = document.getElementsByClassName('join-row');
        let joinsArray = [];
        if (joinsDivs !== undefined) {
            for (let i=0; i<joinsDivs.length; i++) {
                let joinId = joinsDivs[i].id.slice(-1);
                let joinJson = {};
                joinJson.joinType = document.getElementById(`joins${joinId}.joinType`).value;
                joinJson.parentTable = document.getElementById(`joins${joinId}.parentTable`).value;
                joinJson.targetTable = document.getElementById(`joins${joinId}.targetTable`).value;

                let parentJoinColumns = [];
                let parentJoinColumnsEls = document.getElementsByName(`joins[${joinId}].parentJoinColumns`);
                parentJoinColumnsEls.forEach((element) => {
                    parentJoinColumns.push(element.value);
                });
                joinJson.parentJoinColumns = parentJoinColumns;

                let targetJoinColumns = [];
                let targetJoinColumnsEls = document.getElementsByName(`joins[${joinId}].targetJoinColumns`);
                targetJoinColumnsEls.forEach((element) => {
                    targetJoinColumns.push(element.value);
                });
                joinJson.targetJoinColumns = targetJoinColumns;

                joinsArray.push(joinJson);
            }

            json.joins = joinsArray;
        }

        // Other Options
        json.distinct = document.getElementById('distinct').checked;
        json.suppressNulls = document.getElementById('suppressNulls').checked;
        json.limit = document.getElementById('limit').value;
        json.offset = document.getElementById('offset').value;

        return JSON.stringify(json);
    } else {
        // Serialize form's select and input elements except for table select element, which is added manually below.
        let requestData = $('#queryBuilder select, #queryBuilder input').not('#table').serialize();

        // Add selected columns manually to request data.
        let selectedColumns = document.getElementById('columns').options;
        for (var i=0; i<selectedColumns.length; i++) {
            requestData += '&columns=' + selectedColumns[i].value;
        }

        // Add parent table as 'table' request attribute.
        let parentTable = getParentTable();
        requestData += '&table=' + parentTable;
        return requestData;
    }
}

//===========================================================================
//                     START OF SCRIPT
//===========================================================================

renderHTML(scriptVariables.renderHtmlAnchorElement);

$(document).ready(function() {
    setTimeout(function() {
        if (scriptVariables['getQueryTemplateEndpoint'] !== null) {
            getQueryTemplates();
        }

        if (scriptVariables['getSchemaEndpoint'] !== null) {
            getSchemas();
        } else if (scriptVariables['getTablesEndpoint'] !== null) {
            getTables();
        }
    }, scriptVariables['phaseOutMilliseconds'] + 200);
});