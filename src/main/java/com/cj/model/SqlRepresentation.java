package com.cj.model;

public interface SqlRepresentation {

    String toSql(char beginningDelimiter, char endingDelimiter);

}
