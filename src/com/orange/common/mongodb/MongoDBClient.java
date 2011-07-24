package com.orange.common.mongodb;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.management.Query;

import org.apache.cassandra.cli.CliParser.getCondition_return;
import org.apache.cassandra.cli.CliParser.newColumnFamily_return;

import org.apache.cassandra.thrift.Cassandra.system_add_column_family_args;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBConnector;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;
import com.sun.jndi.url.dns.dnsURLContext;

public class MongoDBClient {

	public static String ID = "_id";

	Mongo mongo;
	DB db;

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

	public boolean insert(String tableName, DBObject docObject) {
		DBCollection collection = db.getCollection(tableName);
		if (collection == null)
			return false;
		collection.insert(docObject);
		return true;
	}

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

	public DBObject findAndModify(String tableName,
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
		// collection.findan
		System.out.println("query = " + query.toString());
		System.out.println("update = " + updateValue.toString());
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
		if (fieldValue == null)
			return null;

		DBCollection collection = db.getCollection(tableName);
		if (collection == null)
			return null;

		DBObject query = new BasicDBObject();
		query.put(fieldName, fieldValue);
		return collection.findOne(query);
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

	public static final int SORT_ASCENDING = 1;
	public static final int SORT_DESCENDING = -1;
	
	public DBCursor findByFieldInValues(String tableName,
			String fieldName, List<String> valueList, String sortFieldName, boolean sortAscending, int offset,
			int limit) {
						
		DBCollection collection = db.getCollection(tableName);
		if (collection == null)
			return null;

		DBObject orderBy = new BasicDBObject();
		if (sortFieldName != null){
			if (sortAscending) {
				orderBy.put(sortFieldName, 1);
			} else {
				orderBy.put(sortFieldName, -1);
			}
		}
		
		DBObject in = new BasicDBObject();
		DBObject query = new BasicDBObject();
		if (fieldName != null && fieldName.trim().length() > 0 && valueList != null && valueList.size() > 0) {
			in.put("$in", valueList);
			query.put(fieldName, in);
		}

		DBCursor result = collection.find(query).sort(orderBy).skip(offset)
				.limit(limit);
				
		return result;
	}

	public DBCursor findNearby(String tableName, String gpsFieldName,
			double latitude, double longitude, int offset, int count) {

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

		System.out.println(query.toString());

		DBCursor result = collection.find(query).skip(offset).limit(count);
		return result;
	}

	public  DBCursor findAll(String tableName, String fieldName,
			List<Object> orList, int offset, int count) {

		DBCollection collection = db.getCollection(tableName);
		if (collection == null)
			return null;
		DBObject in = new BasicDBObject();
		in.put("$in", orList);
		DBObject query = new BasicDBObject();
		query.put(fieldName, in);
		return collection.find(query).skip(offset).limit(count);
	}

}
