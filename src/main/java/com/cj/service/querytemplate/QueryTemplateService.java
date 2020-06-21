package com.cj.service.querytemplate;

import com.querybuilder4j.statements.SelectStatement;

import java.util.List;

public interface QueryTemplateService {

    boolean save(String primaryKey, String json);
    SelectStatement findByName(String name);
    List<String> getNames(Integer limit, Integer offset, boolean ascending) throws Exception;

}
