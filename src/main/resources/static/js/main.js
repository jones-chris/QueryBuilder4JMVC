//TODO:  try this for drawing tree lines in criteria div:  http://jsfiddle.net/vLYX5/1 and also https://stackoverflow.com/questions/21864989/draw-lines-between-2-elements-in-html-page

let columns = ['fiscal_year_period', 'fiscal_year', 'service', 'department', 'program', 'amount'];
let tables = ['county_spending_detail'];
let criteria = [];


document.getElementById('addRootCriteria').onclick = function() {
    addCriteria(null);
}

// parentNode:  The criteria node to insert this child node after
function addCriteria(parentNode) {
    // These default assignments for parentId and id assume we are adding a new root criteria.
    let parentId = null;
    let id = 0;

    // If the parentNode parameter is not null, then that means we are adding a child criteria and will reassign the
    //   parentId and id variables.
    if (parentNode !== null) {
        parentId = parentNode.id.slice(-1);
        id = parseInt(parentId) + 1;
    }
    //let parentId = parentNode.id.slice(-1);
    //let id = parseInt(parentId) + 1;
    renumberCriteriaAdding(id);

    //inserts new row after row where 'Add Criteria' button was clicked.
    let newDiv = document.createElement('div');
    newDiv.id = 'row.' + id;
    newDiv.classList.add('criteria-row');
    //this.parentNode.insertAdjacentElement('afterend', newDiv);

    // create id input element
    let idInput = document.createElement('input');
    idInput.type = 'hidden';
    idInput.id = 'criteria' + id + '.id';
    idInput.name = 'criteria[' + id + '].id';
    idInput.value = id;
    newDiv.appendChild(idInput);

    // create parentId input element
    let parentIdInput = document.createElement('input');
    parentIdInput.type = 'hidden';
    parentIdInput.id = 'criteria' + id + '.parentId';
    parentIdInput.name = 'criteria[' + id + '].parentId';
    parentIdInput.value = parentId;
    newDiv.appendChild(parentIdInput);

    // create conjunction select element
    let conjunctionEl = createNewConjunctionSelectEl(id);
    newDiv.appendChild(conjunctionEl);

    // create front parenthesis input element
    let frontParenInput = document.createElement('input');
    frontParenInput.type = 'hidden';
    frontParenInput.id = 'criteria' + id + '.frontParenthesis';
    frontParenInput.name = 'criteria[' + id + '].frontParenthesis';
    newDiv.appendChild(frontParenInput);

    // create column select element
    let columnEl = createNewColumnSelectEl(id, columns);
    newDiv.appendChild(columnEl);

    // create operator select element
    let operatorEl = createNewOperatorSelectEl(id);
    newDiv.appendChild(operatorEl);

    // create filter input element
    let filterInput = document.createElement('input');
    filterInput.id = 'criteria' + id + '.filter';
    filterInput.name = 'criteria[' + id + '].filter';
    newDiv.appendChild(filterInput);

    // create end parenthesis input element
    let endParenInput = document.createElement('input');
    endParenInput.type = 'hidden';
    endParenInput.id = 'criteria' + id + '.endParenthesis';
    endParenInput.name = 'criteria[' + id + '].endParenthesis';
    newDiv.appendChild(endParenInput);

    // create 'Add Criteria' button
    let addCriteriaButton = document.createElement('input');
    addCriteriaButton.type = 'button';
    addCriteriaButton.value = 'Add Criteria';
    addCriteriaButton.onclick = function () {
        addCriteria(newDiv);
    }

    newDiv.appendChild(addCriteriaButton);

    // create 'Remove Criteria' button
    let removeCriteriaButton = document.createElement('input');
    removeCriteriaButton.type = 'button';
    removeCriteriaButton.value = 'Remove Criteria';
    removeCriteriaButton.onclick = function () {
      //let id = parseInt(id);
      //this.parentNode.remove();
      newDiv.remove();
      renumberCriteriaRemoving(newDiv.id.slice(-1));
      reindentCriteria();
    }
    newDiv.appendChild(removeCriteriaButton);

    if (parentNode === null) {
        document.getElementById('criteriaAnchor').prepend(newDiv);
    } else {
        parentNode.insertAdjacentElement('afterend', newDiv);
    }

    reindentCriteria();
}

function removeCriteria(rowIndex) {
    document.getElementById('criteriaTable').deleteRow(rowIndex);
}

function renumberCriteriaAdding(idThatWasAdded) { //TODO:  add parameter to determine if criteria need to be increased or decreased when being renumbered (add when adding criteria, subtract when subtracting criteria)
    // get table
    let criteria = document.getElementsByClassName('criteria-row');

    // for each row, slice id and see if it's greater than parameter
    for (var i=0; i<criteria.length; i++) {
        let currentId = parseInt(criteria[i].id.slice(-1));
        if (currentId >= idThatWasAdded) {

            //get new id
            let newId = currentId + 1;

            //get new parent id
            let currentParentId = criteria[i].children[1].value;
            let newParentId = null;
            if (currentParentId !== "") {
                newParentId = parseInt(currentParentId) + 1;
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
            if (currentParentId >= idThatWasAdded) {
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

}

function renumberCriteriaRemoving(idThatWasRemoved) { //TODO:  add parameter to determine if criteria need to be increased or decreased when being renumbered (add when adding criteria, subtract when subtracting criteria)
    // get table
    let criteria = document.getElementsByClassName('criteria-row');

    // for each row, slice id and see if it's greater than parameter
    for (var i=0; i<criteria.length; i++) {
        let currentId = parseInt(criteria[i].id.slice(-1));
        if (currentId > idThatWasRemoved) {

            //get new id
            let newId = currentId - 1;

            //get new parent id
            let currentParentId = criteria[i].children[1].value;
            let newParentId = null;
            if (currentParentId !== "") {
                if (parseInt(currentParentId) === 0 && parseInt(idThatWasRemoved) !== 0) {
                    newParentId = currentParentId;
                }
                if (parseInt(currentParentId) === 0 && parseInt(idThatWasRemoved) === 0) {
                    newParentId = null;
                }
                if (parseInt(currentParentId) > 0) {
                    newParentId = parseInt(currentParentId) - 1;
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
            criteria[i].children[1].value = newParentId; //value

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

function createNewConjunctionSelectEl(id) {
    let select = document.createElement('select');
    select.id = `criteria${id}.conjunction`;
    select.name = `criteria[${id}].conjunction`;

    let optionAnd = document.createElement('option');
    optionAnd.text = 'And';
    optionAnd.value = 'And';
    select.appendChild(optionAnd);

    let optionOr = document.createElement('option');
    optionOr.text = 'Or';
    optionOr.value = 'Or';
    select.appendChild(optionOr);

    return select;
}

function createNewColumnSelectEl(id, columns) {
    let select = document.createElement('select');
    select.id = `criteria${id}.column`;
    select.name = `criteria[${id}].column`;

    for (var i=0; i<columns.length; i++) {
        let option = document.createElement('option');
        option.text = columns[i];
        option.value = columns[i];
        select.appendChild(option);
    }

    return select;
}

function createNewOperatorSelectEl(id) {
    let select = document.createElement('select');
    select.id = `criteria${id}.operator`;
    select.name = `criteria[${id}].operator`;

    let optionEqual = document.createElement('option');
    optionEqual.text = '=';
    optionEqual.value = 'equalTo';
    select.appendChild(optionEqual);

    let optionNotEqualTo = document.createElement('option');
    optionNotEqualTo.text = '<>';
    optionNotEqualTo.value = 'notEqualTo';
    select.appendChild(optionNotEqualTo);

    let optionGreaterThanOrEquals = document.createElement('option');
    optionGreaterThanOrEquals.text = '>=';
    optionGreaterThanOrEquals.value = 'greaterThanOrEquals';
    select.appendChild(optionGreaterThanOrEquals);

    let operatorLessThanOrequals = document.createElement('option');
    operatorLessThanOrequals.text = '<=';
    operatorLessThanOrequals.value = 'lessThanOrEquals';
    select.appendChild(operatorLessThanOrequals);

    let optionGreaterThan = document.createElement('option');
    optionGreaterThan.text = '>';
    optionGreaterThan.value = 'greaterThan';
    select.appendChild(optionGreaterThan);

    let optionLessThan = document.createElement('option');
    optionLessThan.text = '<';
    optionLessThan.value = 'lessThan';
    select.appendChild(optionLessThan);

    let optionLike = document.createElement('option');
    optionLike.text = 'like';
    optionLike.value = 'like';
    select.appendChild(optionLike);

    let optionNotLike = document.createElement('option');
    optionNotLike.text = 'not like';
    optionNotLike.value = 'notLike';
    select.appendChild(optionNotLike);

    let optionIn = document.createElement('option');
    optionIn.text = 'in';
    optionIn.value = 'in';
    select.appendChild(optionIn);

    let optionNotIn = document.createElement('option');
    optionNotIn.text = 'not in';
    optionNotIn.value = 'notIn';
    select.appendChild(optionNotIn);

    let optionIsNull = document.createElement('option');
    optionIsNull.text = 'is null';
    optionIsNull.value = 'isNull';
    select.appendChild(optionIsNull);

    let optionIsNotNull = document.createElement('option');
    optionIsNotNull.text = 'is not null';
    optionIsNotNull.value = 'isNotNull';
    select.appendChild(optionIsNotNull);

    return select;
}