package com.cj.dao.querytemplate;


import com.cj.model.select_statement.SelectStatement;

import java.util.List;

public interface QueryTemplateDao {

    SelectStatement findByName(String name);
    boolean save(String primaryKey, String json);
    List<String> listNames(Integer limit, Integer offset, boolean ascending) throws Exception;

}
