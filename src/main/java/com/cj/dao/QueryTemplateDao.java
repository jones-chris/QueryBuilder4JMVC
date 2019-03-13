package com.cj.dao;

import java.util.List;

public interface QueryTemplateDao {
    boolean save(String primaryKey, String json);
    String findByName(String name);
    List<String> getNames(Integer limit, Integer offset, boolean ascending) throws Exception;
}
