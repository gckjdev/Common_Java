package com.orange.common.utils;

import com.google.gson.Gson;

/**
 * Created by chaoso on 14-6-23.
 */
public class GsonUtils {

    public static String toJSON(Object obj){
        Gson gson = new Gson();
        String str = gson.toJson(obj);
        return str;
    }
}
