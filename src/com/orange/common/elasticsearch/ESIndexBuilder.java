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

	private final static Client client = new TransportClient()
	   .addTransportAddress(new InetSocketTransportAddress("localhost", 9200));
	
	private ESIndexBuilder() {}
	
	public final static String INDEX_NAME = "mongoindex";
	
	public static IndexResponse indexMongoDB(String dbName, String collection, String indexName) {

		if (dbName == null || collection == null || indexName == null) 
			return null;
		
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
					    		.field("type",collection)
					   .endObject()
					.endObject();
		} catch (IOException e) {
			mongodbRiver = null;
			client.close();
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
		client.close();
		
		return response;
	}
	
	public static void main(String[] args) {
		 IndexResponse indexResponse = ESIndexBuilder.indexMongoDB("game","user", "mongoindex");
		 if ( indexResponse == null ) {
			 ServerLog.info(0, "Index mongodb fails");
			 return;
		 }
		 ServerLog.info(0, "indexResponse.name = " + indexResponse.index() + ", type = " + indexResponse.type());
	}
}
