package com.cj.service.querytemplate;

public interface QueryTemplateService {

    boolean save(String primaryKey, String json);
    String findByName(String name);
    String getNames(Integer limit, Integer offset, boolean ascending) throws Exception;

}
