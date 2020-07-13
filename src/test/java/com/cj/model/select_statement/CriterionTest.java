package com.cj.model.select_statement;

import com.cj.model.Column;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class CriterionTest {

    @Test
    public void hasSearchOperator_trueForLikeOperator() {
        Column column = createMockColumn("test", false);
        Criterion criterion = new Criterion(null, null, column, Operator.like, "hello%", null);

        assertTrue(criterion.hasSearchOperator());
    }

    @Test
    public void hasSearchOperator_trueForNotLikeOperator() {
        Column column = createMockColumn("test", false);
        Criterion criterion = new Criterion(null, null, column, Operator.notLike, "hello%", null);

        assertTrue(criterion.hasSearchOperator());
    }

    @Test
    public void hasSearchOperator_falseForEqualToOperator() {
        Column column = createMockColumn("test",false);
        Criterion criterion = new Criterion(null, null, column, Operator.equalTo, "hello%", null);

        assertFalse(criterion.hasSearchOperator());
    }

    @Test
    public void isValid_trueForNotNullOperatorWithEmptyStringFilter() {
        Column column = createMockColumn("test",true);
        Criterion criterion = new Criterion(null, null, column, Operator.isNotNull, "", null);

        assertTrue(criterion.isValid());
    }

    @Test
    public void isValid_falseForEqualToOperatorWithEmptyStringFilter() {
        Column column = createMockColumn("test",true);
        Criterion criterion = new Criterion(null, null, column, Operator.equalTo, "", null);

        assertFalse(criterion.isValid());
    }

    @Test
    public void isValid_trueForEqualToOperatorWithNonEmptyStringFilter() {
        Column column = createMockColumn("test",true);
        Criterion criterion = new Criterion(null, null, column, Operator.equalTo, "test", null);

        assertTrue(criterion.isValid());
    }

    @Test
    public void toSql_nullSchema() {
        Column column = createMockColumn(null, true);
        Criterion criterion = new Criterion(null, Conjunction.And, column, Operator.equalTo, "test", null);
        String expectedSql = " AND `test`.`test` = test ";

        String actualSql = criterion.toSql('`', '`');

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void toSql_nullStringSchema() {
        Column column = createMockColumn("null", true);
        Criterion criterion = new Criterion(null, Conjunction.And, column, Operator.equalTo, "test", null);
        String expectedSql = " AND `test`.`test` = test ";

        String actualSql = criterion.toSql('`', '`');

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void toSql_nonNullSchema() {
        Column column = createMockColumn("my_schema", true);
        Criterion criterion = new Criterion(null, Conjunction.And, column, Operator.equalTo, "test", null);
        String expectedSql = " AND `my_schema`.`test`.`test` = test ";

        String actualSql = criterion.toSql('`', '`');

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void toSqlDeep_oneBranchWithOneNodeDeep() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(null, column);
        Criterion childCriterion = createMockCriterion(rootCriterion, column);
        rootCriterion.getChildCriteria().add(childCriterion);
        String expectedSql = "  `test`.`test` = test   AND `test`.`test` = test ";

        String actualSql = rootCriterion.toSqlDeep('`', '`', new CriteriaSqlStringHolder());

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void toSqlDeep_twoBranchesWithOneNodeDeep() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(null, column);
        Criterion childCriterion1 = createMockCriterion(rootCriterion, column);
        Criterion childCriterion2 = createMockCriterion(rootCriterion, column);
        rootCriterion.setChildCriteria(Arrays.asList(childCriterion1, childCriterion2));
        String expectedSql = "  `test`.`test` = test   AND `test`.`test` = test   AND `test`.`test` = test ";

        String actualSql = rootCriterion.toSqlDeep('`', '`', new CriteriaSqlStringHolder());

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void toSqlDeep_oneBranchWithTwoNodesDeep() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(null, column);
        Criterion childCriterion1 = createMockCriterion(rootCriterion, column);
        Criterion childCriterion1_1 = createMockCriterion(childCriterion1, column);
        childCriterion1.setChildCriteria(Collections.singletonList(childCriterion1_1));
        rootCriterion.setChildCriteria(Collections.singletonList(childCriterion1));
        String expectedSql = "  `test`.`test` = test   AND (`test`.`test` = test   AND `test`.`test` = test) ";

        String actualSql = rootCriterion.toSqlDeep('`', '`', new CriteriaSqlStringHolder());

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void toSqlDeep_oneBranchWithTwoNestedBranches() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(null, column);
        Criterion childCriterion1 = createMockCriterion(rootCriterion, column);
        Criterion childCriterion1_1 = createMockCriterion(childCriterion1, column);
        childCriterion1.setChildCriteria(Collections.singletonList(childCriterion1_1));
        Criterion childCriterion2 = createMockCriterion(rootCriterion, column);
        Criterion childCriterion2_1 = createMockCriterion(childCriterion2, column);
        childCriterion2.setChildCriteria(Collections.singletonList(childCriterion2_1));
        rootCriterion.setChildCriteria(Arrays.asList(childCriterion1, childCriterion2));
        String expectedSql = "  `test`.`test` = test   AND (`test`.`test` = test   AND `test`.`test` = test)   AND (`test`.`test` = test   AND `test`.`test` = test) ";

        String actualSql = rootCriterion.toSqlDeep('`', '`', new CriteriaSqlStringHolder());

        assertEquals(expectedSql, actualSql);
    }

    private Column createMockColumn(String schema, boolean hasSingleQuotedColumn) {
        int dataType = (hasSingleQuotedColumn) ? Types.VARCHAR : Types.INTEGER;
        return new Column("test", schema, "test", "test", dataType, null);
    }

    private Criterion createMockCriterion(Criterion parentCriterion, Column column) {
        // If no parentCriterion parameter, then return a root criterion.
        // Else, return a child criterion.
        if (parentCriterion == null) {
            return new Criterion(null, null, column, Operator.equalTo, "test", null);
        } else {
            return new Criterion(parentCriterion, Conjunction.And, column, Operator.equalTo, "test", null);
        }
    }

}
