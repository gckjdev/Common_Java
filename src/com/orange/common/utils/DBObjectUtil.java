package com.orange.common.utils;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-11-13
 * Time: 上午10:07
 * To change this template use File | Settings | File Templates.
 */
public class DBObjectUtil {

    public static int getInt(DBObject dbObject, String key) {
        Object obj = dbObject.get(key);
        if (obj == null) {
            return 0;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        } else if (obj instanceof Double) {
            return ((Double) obj).intValue();
        } else if (obj instanceof Long) {
            long longValue = ((Long) obj).longValue();
            return (int)longValue;
        } else {
            return 0;
        }
    }

    public static double getDouble(DBObject dbObject, String key) {
        Object value = dbObject.get(key);
        if (value == null)
            return 0.0f;

        if (value instanceof Double) {
            return ((Double) value).doubleValue();
        }

        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }

        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }

        return 0.0f;
    }

    public static boolean getBoolean(DBObject dbObject, String key) {
        Object obj = dbObject.get(key);
        if (obj != null && obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        }
        return false;
    }

    public static float getFloat(DBObject dbObject, String key) {
        Double value = (Double) dbObject.get(key);
        if (value == null)
            return 0.0f;

        return value.floatValue();
    }

    public static Date getDate(DBObject dbObject, String key) {
        Date value = (Date) dbObject.get(key);
        return value;
    }

    public static int getIntDate(DBObject dbObject, String key) {
        Date value = (Date) dbObject.get(key);
        if (value != null)
            return (int)(value.getTime()/1000);
        else
            return 0;
    }


    public static <T> List<T> getList(DBObject dbObject, String key, Class<T> clazz){
        BasicDBList list = (BasicDBList) dbObject.get(key);
        if (list != null && !list.isEmpty()){
            int count = list.size();
            List<T> retlist = new ArrayList<T>(count);
            for (int i = 0; i < count; ++ i){
                T obj = (T) list.get(i);
                retlist.add(obj);
            }
            return retlist;
        }
        return Collections.emptyList();

    }

    public static BasicDBList getList(DBObject dbObject, String key) {
        return (BasicDBList) dbObject.get(key);
    }



    @SuppressWarnings("unchecked")
    public static List<String> getStringList(DBObject dbObject, String key) {
        List<String> list = (List<String>) dbObject.get(key);
        if (list == null)
            return Collections.emptyList();
        else
            return list;
    }

    public static Object getObject(DBObject dbObject, String key) {
        return dbObject.get(key);
    }

    public static String getStringObjectId(DBObject dbObject) {
        return dbObject.get("_id").toString();
    }

    public static ObjectId getObjectId(DBObject dbObject) {
        Object obj = dbObject.get("_id");
        if (obj instanceof ObjectId)
            return (ObjectId) obj;
        else
            return null;
    }

    public static ObjectId getObjectId(DBObject dbObject, String key){
        Object obj = dbObject.get(key);
        if (obj instanceof ObjectId)
            return (ObjectId) obj;
        else
            return null;
    }
}
