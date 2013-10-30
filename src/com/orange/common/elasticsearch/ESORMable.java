package com.orange.common.elasticsearch;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-10-28
 * Time: 下午4:43
 * To change this template use File | Settings | File Templates.
 */
public interface ESORMable {
    public Map<String, Object> getESORM();
    public String getESIndexType();
    public String getESIndexName();
    public String getID();
    public boolean hasFieldForSearch();
    public boolean canBeIndexed();
}
