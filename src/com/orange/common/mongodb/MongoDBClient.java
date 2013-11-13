package com.orange.common.mongodb;

import com.mongodb.*;
import com.orange.common.utils.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.net.UnknownHostException;
import java.util.*;

public class MongoDBClient {

    public static final Logger log = Logger.getLogger(MongoDBClient.class
            .getName());
    public static final int SORT_ASCENDING = 1;
    public static final int SORT_DESCENDING = -1;
    public static String ID = "_id";
    Mongo mongo;
    DB db;

    @Deprecated
    public MongoDBClient(String serverAddress, String dbName, String userName,
                         String password) {

        try {
            this.mongo = new Mongo(serverAddress, 27017);
        } catch (UnknownHostException e) {
            e.printStackTrace(); // TODO
        } catch (MongoException e) {
            e.printStackTrace(); // TODO
        }
        this.db = mongo.getDB(dbName);
        boolean auth = db.authenticate(userName, password.toCharArray());
        return;
    }

    public MongoDBClient(String dbName) {

        String address = System.getProperty("mongodb.address");
        String portStr = System.getProperty("mongodb.port");
        String connectionPerHostStr = System
                .getProperty("mongodb.connectionPerHost");

        int port = 27017;
        int connectionPerHost = 200;

        if (address == null) {
            address = "127.0.0.1";
        }
        if (portStr != null) {
            port = Integer.parseInt(portStr);
        }
        if (connectionPerHostStr != null) {
            connectionPerHost = Integer.parseInt(connectionPerHostStr);
        }

        try {
            ServerAddress serverAddress = new ServerAddress(address, port);

            MongoOptions mongoOptions = new MongoOptions();
            mongoOptions.connectionsPerHost = connectionPerHost;
            mongoOptions.threadsAllowedToBlockForConnectionMultiplier = 5;
            mongoOptions.maxWaitTime = 30 * 1000;        // 30 seconds for thread block wait
//			mongoOptions.connectTimeout = 30*1000;
//			mongoOptions.socketTimeout = 60*1000;

            this.mongo = new Mongo(serverAddress, mongoOptions);
            log
                    .info("<MongoDBClient> mongo option="
                            + this.mongo.getMongoOptions().toString()
                            + ", connectionPerHost="
                            + this.mongo.getMongoOptions().connectionsPerHost
                            + ", threadsAllowedToBlockForConnectionMultiplier="
                            + this.mongo.getMongoOptions().threadsAllowedToBlockForConnectionMultiplier);

        } catch (UnknownHostException e) {
            log.error(
                    "<MongoDBClient> connect to DB server but catch UnknownHostException="
                            + e.toString(), e);
        } catch (MongoException e) {
            log.error(
                    "<MongoDBClient> connect to DB server but catch MongoException="
                            + e.toString(), e);
        }

        this.db = mongo.getDB(dbName);

        String plainAuth = System.getProperty("mongodb.plain_auth");
        String commandLineUserName = System.getProperty("mongodb.user");
        String commandLinePasswd = System.getProperty("mongodb.password");

        if (commandLineUserName == null && commandLinePasswd == null) {
            log.info("<MongoDBClient> access without user name and password");
            return;
        }

        boolean authResult = false;
        if (plainAuth != null && plainAuth.equals("1")) {
            authResult = db.authenticate(commandLineUserName, commandLinePasswd.toCharArray());
        } else {
            String user = StringUtil.deIntersetTwoStrings(new String(Base64.decodeBase64(commandLineUserName)), "Alpha");
            String passwd = StringUtil.deIntersetTwoStrings(new String(Base64.decodeBase64(commandLinePasswd)), "Gamma");
            authResult = db.authenticate(user, passwd.toCharArray());
        }

        if (authResult) {
            log.info("<MongoDBClient> authentication successfully");
        } else {
            log.error("<MongoDBClient> authentication failure, please check user name and password settings");
        }

		/*
        if (commandLineUserName == null && commandLinePasswd == null) {
			log.info("<MongoDBClient> access without user name and password");
			return;
		}

		boolean authResult = false;
		if (plainAuth != null && plainAuth.equals("1")) {
			authResult = db.authenticate(commandLineUserName, commandLinePasswd
					.toCharArray());
		} else {
			String user = StringUtil.deIntersetTwoStrings(new String(Base64
					.decodeBase64(commandLineUserName)), "Alpha");
			String passwd = StringUtil.deIntersetTwoStrings(new String(Base64
					.decodeBase64(commandLinePasswd)), "Gamma");
			authResult = db.authenticate(user, passwd.toCharArray());
		}

		if (authResult) {
			log.info("<MongoDBClient> authentication successfully");
		} else {
			log
					.error("<MongoDBClient> authentication failure, please check user name and password settings");
		}
		*/
    }

    public Mongo getMongo() {
        return mongo;
    }

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    public boolean insert(String tableName, DBObject docObject) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return false;
        collection.insert(docObject);
        return true;
    }

    public Object insertAndReturnField(String tableName, DBObject docObject, String fieldName) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;
        WriteResult wr = collection.insert(docObject);
        return wr.getField(fieldName);
    }

    // upsert = false
    public DBObject findAndModify(String tableName, String fieldName,
                                  int findValue, int modifyValue) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject query = new BasicDBObject();
        query.put(fieldName, findValue);

        DBObject update = new BasicDBObject();
        DBObject updateValue = new BasicDBObject();
        updateValue.put(fieldName, modifyValue);
        update.put("$set", updateValue);

        return collection.findAndModify(query, null, null, false, update, true,
                false);
    }

    public DBObject findAndModifyUpsert(String tableName, String fieldName,
                                        int findValue, int modifyValue) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject query = new BasicDBObject();
        DBObject queryOr = new BasicDBObject();
        query.put(fieldName, findValue);
        queryOr.put(fieldName, null);

        DBObject queryCondition = new BasicDBObject();
        BasicDBList values = new BasicDBList();
        values.add(query);
        values.add(queryOr);
        queryCondition.put("$or", values);

        DBObject update = new BasicDBObject();
        DBObject updateValue = new BasicDBObject();
        updateValue.put(fieldName, modifyValue);
        update.put("$set", updateValue);
        return collection.findAndModify(queryCondition, null, null, false,
                update, true, false);

    }

    // returnNew = true
    public DBObject findAndModify(String tableName, String fieldName,
                                  String findValue, String modifyValue) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject query = new BasicDBObject();
        query.put(fieldName, findValue);

        DBObject update = new BasicDBObject();
        DBObject updateValue = new BasicDBObject();
        updateValue.put(fieldName, modifyValue);
        update.put("$set", updateValue);

        return collection.findAndModify(query, null, null, false, update, true,
                false);
    }

    // returnNew = true
    public DBObject findAndModify(String tableName, DBObject query,
                                  DBObject update) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        // System.out.println("update db, query = " + query.toString() +
        // ", update = " + update.toString());
        return collection.findAndModify(query, null, null, false, update, true,
                false);
    }

    // returnNew = true
    public DBObject findAndModify(String tableName, DBObject query,
                                  DBObject update, DBObject fields) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        return collection.findAndModify(query, fields, null, false, update, true,
                false);
    }

    // returnNew = true, upsert = true
    public DBObject findAndModifyUpsert(String tableName, DBObject query,
                                        DBObject update) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        // System.out.println("update db, query = " + query.toString() +
        // ", update = " + update.toString());
        return collection.findAndModify(query, null, null, false, update, true,
                true);
    }

    public void updateOne(String tableName, DBObject query, DBObject update) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return;

        log.info("<updateOne> " + tableName + "query = " + query.toString() + ", update = "
                + update.toString());
        collection.update(query, update, false, false);
    }

    public void updateAll(String tableName, DBObject query, DBObject update) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return;

        log.info("<updateAll> " + tableName + " query = " + query.toString() + ", update = "
                + update.toString());
        collection.update(query, update, false, true);
    }

    public void updateAllByDBObject(String tableName, ObjectId objectId, DBObject updateValue) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null || objectId == null || updateValue == null || updateValue.keySet().size() == 0) {
            log.warn("<updateAll> incorrect data! table=" + tableName + ", objectId = " + objectId + ", updateValue = " + updateValue);
            return;
        }

        BasicDBObject query = new BasicDBObject("_id", objectId);
        BasicDBObject update = new BasicDBObject("$set", updateValue);

        log.info("<updateAll> " + tableName + " query = " + query.toString() + ", update = " + update.toString());
        collection.update(query, update, false, true);
    }

    public void updateAllByDBObject(String tableName, String stringObjectId, DBObject updateValue) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null || stringObjectId == null || updateValue == null || updateValue.keySet().size() == 0 || !ObjectId.isValid(stringObjectId)) {
            log.warn("<updateAll> incorrect data! table=" + tableName + ", objectId = " + stringObjectId + ", updateValue = " + updateValue);
            return;
        }

        updateAllByDBObject(tableName, new ObjectId(stringObjectId), updateValue);
    }

    public void upsertAll(String tableName, DBObject query, DBObject update) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return;

        log.info("<upsertAll> " + tableName + " query = " + query.toString() + ", update = "
                + update.toString());
        collection.update(query, update, true, true);
    }

    // returnNew = false
    public DBObject findAndModifySet(String tableName,
                                     Map<String, Object> equalCondition, Map<String, Object> updateMap) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject query = new BasicDBObject();
        // query.put(fieldName, findValue);
        query.putAll(equalCondition);

        DBObject update = new BasicDBObject();
        DBObject updateValue = new BasicDBObject();
        // updateValue.put(fieldName, modifyValue);
        updateValue.putAll(updateMap);
        update.put("$set", updateValue);

        log.info("<findAndModify> " + tableName + "query = " + query.toString() + ", update = "
                + update.toString());
        return collection.findAndModify(query, update);
    }

    public void save(String tableName, DBObject docObject) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return;

        collection.save(docObject);
        return;
    }

    public DBObject findOne(String tableName, String fieldName,
                            String fieldValue) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject query = new BasicDBObject();
        query.put(fieldName, fieldValue);
        return collection.findOne(query);
    }

    public DBCursor find(String tableName, String fieldName, String fieldValue,
                         int limit) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject query = new BasicDBObject();
        query.put(fieldName, fieldValue);
        return collection.find(query).limit(limit);
    }

    public DBObject findOne(String tableName, DBObject query) {
        if (query == null)
            return null;

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        return collection.findOne(query);
    }

    public DBObject findOne(String tableName, DBObject query,
                            DBObject returnFields) {
        if (query == null)
            return null;

        log.info("<findOne> " + tableName + " query = " + query + ", returnFields = "
                + returnFields);

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;
        return collection.findOne(query, returnFields);
    }

    public DBObject findOneWithArrayLimit(String tableName, DBObject query,
                                          String arrayField, int offset, int limit,
                                          HashSet<String> returnFields) {
        if (query == null)
            return null;

        if (limit <= 0) {
            log.warn(String.format("<findOneWithArrayLimit> invalid limit(%d), return null", limit));
            return null;
        }

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject slice = new BasicDBObject();
        BasicDBList sliceList = new BasicDBList();
        sliceList.add(Integer.valueOf(offset));
        sliceList.add(Integer.valueOf(limit));
        slice.put("$slice", sliceList);
        DBObject field = new BasicDBObject();
        field.put(arrayField, slice);
        if (returnFields != null && !returnFields.isEmpty()) {
            for (String fld : returnFields) {
                if (!StringUtil.isEmpty(fld)) {
                    field.put(fld, 1);
                }
            }
        }
        log.info("<findInArray> query=" + query.toString() + ", returnFields=" + field.toString());
        return collection.findOne(query, field);
    }

    public void pullArrayKey(String tableName, DBObject query,
                             String ArrayName, String key, String keyValue) {
        if (query == null) {
            return;
        }
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return;

        BasicDBObject pull = new BasicDBObject();
        BasicDBObject pullValue = new BasicDBObject();
        pullValue.put(key, keyValue);

        pull.put(ArrayName, pullValue);

        BasicDBObject update = new BasicDBObject();
        update.put("$pull", pull);

        log.info("<pullArray> " + tableName + " query=" + query + " update=" + update);
        updateAll(tableName, query, update);
    }

    public boolean removeOne(String tableName, DBObject query) {
        if (query == null)
            return false;

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return false;

        collection.findAndRemove(query);
        return true;
    }

    public boolean remove(String tableName, DBObject query) {
        if (query == null)
            return false;

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return false;

        collection.remove(query);
        return true;
    }

    public boolean removeByObjectId(String tableName, String objectId) {
        BasicDBObject object = new BasicDBObject();
        object.put("_id", new ObjectId(objectId));
        return removeOne(tableName, object);
    }

    public DBObject findOne(String tableName, Map<String, String> fieldValues) {
        if (fieldValues == null)
            return null;

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject query = new BasicDBObject();
        query.putAll(fieldValues);
        return collection.findOne(query);
    }

    public DBCursor find(String tableName, DBObject query, DBObject orderBy,
                         int offset, int limit) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBCursor cursor = null;
        if (orderBy == null) {
            cursor = collection.find(query).skip(offset).limit(limit);
        } else {
            cursor = collection.find(query).sort(orderBy).skip(offset).limit(
                    limit);
        }

        return cursor;
    }

    public DBCursor find(String tableName, DBObject query,
                         DBObject returnFields, DBObject orderBy, int offset, int limit) {

        log.info("<find> query = " + query + ", returnFields = " + returnFields + ", order = " + orderBy + ", offset = " + offset + ", limit = " + limit);

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;
        if (returnFields == null) {
            returnFields = new BasicDBObject();
        }
        DBCursor cursor = null;
        if (orderBy == null) {
            cursor = collection.find(query, returnFields).skip(offset).limit(
                    limit);
        } else {
            cursor = collection.find(query, returnFields).sort(orderBy).skip(
                    offset).limit(limit);
        }
        log.info("<find> cursor size = " + cursor.size());
        return cursor;
    }

    public DBCursor findByIds(String tableName, String fieldName,
                              List<ObjectId> valueList) {
        if (valueList == null || valueList.size() == 0)
            return null;
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;
        DBObject in = new BasicDBObject();
        DBObject query = new BasicDBObject();
        in.put("$in", valueList);
        query.put(fieldName, in);
        DBCursor result = collection.find(query);
        ;

        return result;
    }

    public DBCursor findByFieldInValues(String tableName, String fieldName,
                                        List<String> valueList, String sortFieldName,
                                        boolean sortAscending, int offset, int limit) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject orderBy = null;
        if (sortFieldName != null) {
            orderBy = new BasicDBObject();
            if (sortAscending) {
                orderBy.put(sortFieldName, 1);
            } else {
                orderBy.put(sortFieldName, -1);
            }
        }

        DBObject in = new BasicDBObject();
        DBObject query = new BasicDBObject();
        if (fieldName != null && fieldName.trim().length() > 0
                && valueList != null && valueList.size() > 0) {
            in.put("$in", valueList);
            query.put(fieldName, in);
        }
        DBCursor result;
        if (orderBy != null) {
            result = collection.find(query).sort(orderBy).skip(offset).limit(
                    limit);
        } else {
            result = collection.find(query).skip(offset).limit(limit);
        }
        return result;
    }

    public DBCursor findNearby(String tableName, String gpsFieldName,
                               double latitude, double longitude, int offset, int count) {

        if (gpsFieldName == null || gpsFieldName.trim().length() == 0) {
            return null;
        }

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        List<Double> gpsList = new ArrayList<Double>();
        gpsList.add(latitude);
        gpsList.add(longitude);

        DBObject near = new BasicDBObject();
        near.put("$near", gpsList);
        DBObject query = new BasicDBObject();
        query.put(gpsFieldName, near);

        log.info("<findNearby>" + query.toString());

        DBCursor result = collection.find(query).skip(offset).limit(count);
        return result;
    }

    public DBCursor findByFieldInValues(String tableName, String fieldName,
                                        List<Object> valueList, int offset, int count) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;
        DBObject in = new BasicDBObject();
        in.put("$in", valueList);
        DBObject query = new BasicDBObject();
        query.put(fieldName, in);
        // log.info("map search = " + query);
        return collection.find(query).skip(offset).limit(count);
    }

    public DBCursor findByFieldInValues(String tableName, String fieldName,
                                        List<Object> valueList, DBObject returnFields, int offset, int count) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;
        DBObject in = new BasicDBObject();
        in.put("$in", valueList);
        DBObject query = new BasicDBObject();
        query.put(fieldName, in);
        // log.info("map search = " + query);
        return collection.find(query, returnFields).skip(offset).limit(count);
    }

    public DBCursor findByFieldInValues(String tableName, String fieldName,
                                        Collection<?> valueList, DBObject returnFields) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;
        DBObject in = new BasicDBObject();
        in.put("$in", valueList);
        DBObject query = new BasicDBObject();
        query.put(fieldName, in);
        // log.info("map search = " + query);
        return collection.find(query, returnFields);
    }

    public DBCursor findByFieldsInValues(String tableName,
                                         Map<String, List<Object>> fieldValueMap, int offset, int limit) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null) {
            return null;
        }

        DBObject query = null;
        if (fieldValueMap != null && fieldValueMap.size() > 0) {
            query = new BasicDBObject();
            for (String field : fieldValueMap.keySet()) {
                DBObject in = new BasicDBObject();
                in.put("$in", fieldValueMap.get(field));
                query.put(field, in);
            }
        }
        if (query == null) {
            return collection.find().skip(offset).limit(limit);
        }
        return collection.find(query).skip(offset).limit(limit);
    }

    public boolean inc(String tableName, String keyFieldName,
                       Object keyFieldValue, String counterName, int counterValue) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null) {
            return false;
        }

        if (keyFieldName == null || counterName == null)
            return false;

        BasicDBObject query = new BasicDBObject();
        query.put(keyFieldName, keyFieldValue);

        BasicDBObject inc = new BasicDBObject();
        BasicDBObject incValue = new BasicDBObject();
        incValue.put(counterName, counterValue);
        inc.put("$inc", incValue);

        collection.update(query, inc);
        return true;
    }

    public void updateOrInsert(String tableName, DBObject query, DBObject update) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return;

        collection.update(query, update, true, false);
    }

    public DBObject findOne(String tableName, String fieldName, ObjectId value) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject query = new BasicDBObject();
        query.put(fieldName, value);
        return collection.findOne(query);
    }

    public DBObject findOne(String tableName, String fieldName, Object value,
                            DBObject returnFields) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject query = new BasicDBObject();
        query.put(fieldName, value);
        return collection.findOne(query, returnFields);
    }

    public DBObject findOneByObjectId(String tableName, String value) {
        return this.findOne(tableName, "_id", new ObjectId(value));
    }

    public DBObject findAndModifyInsert(String tableName, BasicDBObject query,
                                        BasicDBObject update) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        return collection.findAndModify(query, null, null, false, update, true,
                true);
    }

    public DBCursor findAll(String tableName) {
        return findAll(tableName, null);
    }

    public DBCursor findAll(String tableName, DBObject order) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;
        if (order != null) {
            return collection.find().sort(order);
        }
        return collection.find();
    }

    public DBCursor find(String tableName, String fieldName, Object fieldValue) {

        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        DBObject query = new BasicDBObject();
        query.put(fieldName, fieldValue);
        return collection.find(query);
    }

    public Long count(String tableName, DBObject query) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;

        return collection.count(query);
    }

    public DBObject findOneByObjectId(String tableName, String objectId,
                                      DBObject fields) {
        return findOne(tableName, "_id", new ObjectId(objectId), fields);
    }

    public DBCursor findAll(String tableName, DBObject query,
                            DBObject returnFields) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null)
            return null;
        log.info("<findAll> table=" + tableName + ",query=" + query + "fields=" + returnFields);
        return collection.find(query, returnFields);
    }

    public void createIndexIfNotExist(String tableName, String indexField, boolean unqiue) {
        DBCollection collection = db.getCollection(tableName);
        if (collection == null || indexField == null || indexField.length() == 0)
            return;

        List<DBObject> indexList = collection.getIndexInfo();
        if (indexList != null) {
            for (DBObject index : indexList) {
                DBObject keys = (DBObject) index.get("key");
                if (keys != null) {
                    if (keys.containsField(indexField)) {
                        log.info("<createIndex> " + tableName + ", but index[" + indexField + "] exist, skip");
                        return;
                    }
                }
            }
        }

        BasicDBObject index = new BasicDBObject();
        index.put(indexField, 1);

        DBObject indexOptions = new BasicDBObject();
        indexOptions.put("unique", true);

        if (unqiue) {
            log.info("<createIndex> " + tableName + ", index=" + index.toString() + ", options=" + indexOptions);
            collection.ensureIndex(index, indexOptions);
        } else {
            log.info("<createIndex> " + tableName + ", index=" + index.toString());
            collection.ensureIndex(index);
        }
    }

    public List<DBObject> fullTextSearch(String tableName, String keyWord, BasicDBObject filter, int limit) {
        DBObject searchCmd = new BasicDBObject();
        searchCmd.put("text", tableName); // the name of the collection (string)
        searchCmd.put("search", keyWord); // the term to search for (string)
        searchCmd.put("limit", limit);

        if (filter != null) {
            searchCmd.put("filter", filter);
        }
        CommandResult commandResult = db.command(searchCmd);

        try {
            List<DBObject> retList = new ArrayList<DBObject>();
            BasicDBList results = (BasicDBList) commandResult.get("results");
            int count = results.size();
            for (int i = 0; i < count; ++i) {
                DBObject result = (DBObject) results.get(i);
                DBObject obj = (DBObject) result.get("obj");
                if (obj != null) {
                    retList.add(obj);
                }
            }
            return retList;

        } catch (Exception e) {
            log.info("<fullTextSearch> catch an exception = " + e);
            return Collections.emptyList();
        }
    }

    public boolean inc(String tableName, ObjectId key, String counterName, int inc) {
        if (key == null || counterName == null){
            return false;
        }
        BasicDBObject incValue = new BasicDBObject(counterName, inc);
        BasicDBObject update = new BasicDBObject("$inc", incValue);
        DBObject query = new BasicDBObject("_id", key);
        updateOne(tableName, query, update);
        return true;
    }


    public void addToSet(String tableName, String keyObjectId, String field, Object value) {
        DBObject query = new BasicDBObject("_id", new ObjectId(keyObjectId));
        BasicDBObject add = new BasicDBObject(field, value);
        DBObject update = new BasicDBObject("$addToSet", add);
        this.updateOne(tableName, query, update);
    }

    public boolean isListEmpty(String tableName, String keyObjectId, String field) {
        DBObject obj = findOneByObjectId(tableName, keyObjectId, new BasicDBObject(field, 1));
        if (obj == null) {
            return true;
        }
        BasicDBList list  = (BasicDBList) obj.get(field);
        if (list != null && list instanceof BasicDBList){
            return list.isEmpty();
        }
        return true;
    }

    public void pullValueFromSet(String tableName, String keyObjectId, String field, Object value) {
        DBObject query = new BasicDBObject("_id", new ObjectId(keyObjectId));
        BasicDBObject pull = new BasicDBObject(field, value);
        DBObject update = new BasicDBObject("$pull", pull);
        updateOne(tableName, query, update);
    }
}
