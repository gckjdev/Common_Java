package com.orange.common.utils;

import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: gckj
 * Date: 13-11-23
 * Time: 上午11:26
 * To change this template use File | Settings | File Templates.
 */
public class MapUtil {

    public static <K,V> Map<K,V> makeMap(Collection<? extends MakeMapable<K,V>> collection){
        if (collection != null && !collection.isEmpty()){
            Map<K,V> map = new HashMap<K, V>();
            for (MakeMapable<K,V> mapable : collection){
                if (mapable.getKey() != null){
                    map.put(mapable.getKey(), mapable.getValue());
                }
            }
            return map;
        }
        return Collections.emptyMap();
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static interface MakeMapable<K, V>{
        public K getKey();
        public V getValue();
    }

}
