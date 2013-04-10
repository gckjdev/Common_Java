package com.orange.common.elasticsearch;

import java.io.IOException;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.orange.common.log.ServerLog;

public class ESIndexBuilder {

	private ESIndexBuilder() {}
	
	private final static Client client = new TransportClient()
	   .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
	public final static String INDEX_NAME = "mongoindex";
	
	/**
	 *  这个利用mongodb-river插件来索引我们的数据库
	 * @param dbName　    要索引的数据库名
	 * @param collection  要索引的表名
	 * @param indexName　 索引名字
	 * @param indexType　 索引类型，通常用索引的表名作为类型
	 * @return
	 */
	public static IndexResponse indexByMongodbRiver(String dbName, String collection, String indexName, String indexType) {

		if (dbName == null || collection == null || indexName == null) 
			return null;
		
		Client client = new TransportClient()
		   .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		
		String type;
		if ( indexType == null)
			type = collection;
		else 
			type = indexType;
		
		XContentBuilder mongodbRiver = null;
		try {
			mongodbRiver = XContentFactory.jsonBuilder()
					.startObject()
						.field("type","mongodb")
						.startObject("mongodb")
//								.field("host","127.0.0.1")
//								.field("port",27017)
								.field("db",dbName)
								.field("collection", collection)
								.field("gridfs", "true")
					   .endObject()
					   .startObject("index")
					    		.field("name",indexName)
					    		.field("type",type)
					   .endObject()
					.endObject();
		} catch (IOException e) {
			mongodbRiver = null;
//			client.close();
			ServerLog.warn(0, "Creating elasticsearch index for mongodb fails");
		} 
			
		IndexResponse response = null;
		if (mongodbRiver != null) {
			 response = client.prepareIndex(
				                  "_river",  // 索引库名
		                          "mongodb", // 索引类型，区分同一索引库下不同类型数据
	                              "_meta")   // id字段，可选
		                     .setSource(mongodbRiver)
		                     .execute().actionGet(); 
		}
		
		return response;
	}
	
	/**
	 * 不使用mongodb-river插件索引，而是手工索引，  
	 * @param  jsonDoc   　要索引的json文档
	 * @param  indexName 　索引库名
	 * @param  indexType　　索引类型
	 * @return 
	 */
	public static boolean indexByRawAPI(String jsonDoc, String indexName, String indexType) {
	
		if (jsonDoc == null || indexName == null || indexType == null){
			ServerLog.warn(0, "Imcomplete arguments , fails to index!");
			return false;
		}
		
		IndexResponse response = client.prepareIndex(indexName, indexType)
									   .setSource(jsonDoc)
		                               .execute()
		                               .actionGet(); 
		
		if ( ! response.getIndex().equals(indexName)){
			ServerLog.warn(0, "response.getIndex = " + response.getIndex() +", indexName = " + indexName);
			return false;
		}
		return true;
	
	}
	
	public static void main(String[] args) {
		 IndexResponse indexResponse = ESIndexBuilder.indexByMongodbRiver("game","user", "mongoindex", null);
		 if ( indexResponse == null ) {
			 ServerLog.info(0, "Index mongodb fails");
			 return;
		 }
		 ServerLog.info(0, "indexResponse.name = " + indexResponse.index() + ", type = " + indexResponse.type());
	}
}
