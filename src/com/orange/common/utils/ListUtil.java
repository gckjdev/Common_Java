package com.orange.common.utils;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtil {

	public static List<String> stringsToList(String...strings){
		if (strings == null)
			return null;
		
		List<String> list = new ArrayList<String>();
		for (int i=0; i<strings.length; i++){
			list.add(strings[i]);
		}
		
		return list;
	}

	public static List<Integer> stringsToIntList(String...strings){
		if (strings == null)
			return null;
		
		List<Integer> list = new ArrayList<Integer>();
		for (int i=0; i<strings.length; i++){			
			list.add(Integer.parseInt(strings[i]));
		}
		
		return list;
	}

    public static List<ObjectId> stringListToObjectIdList(List<String> stringList){
        if (stringList == null || stringList.size() == 0)
            return Collections.emptyList();

        List<ObjectId> retList = new ArrayList<ObjectId>();
        for (String id : stringList){
            if (id != null && ObjectId.isValid(id)){
                retList.add(new ObjectId(id));
            }
        }

        return retList;
    }

}
