package com.orange.common.elasticsearch.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import static org.elasticsearch.index.query.FilterBuilders.*;   
import static org.elasticsearch.index.query.QueryBuilders.*; 
import static org.elasticsearch.common.xcontent.XContentFactory.*;

import org.junit.Test;

public class QuickTest {

//	@Test
	public void test() {
		Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));   
   
		
		XContentBuilder doc;
		try {
			doc = jsonBuilder()  
				      .startObject()       
				          .field("title", "this is a title 2!")  
				          .field("description", "descript what 2?")   
				          .field("price", 200)  
				          .field("onSale", false)  
				          .field("type", 2)  
				          .field("createDate", new Date())                            
				     .endObject();
			IndexResponse indexResponse =  client.prepareIndex("product_index","product_type").setSource(doc).execute().actionGet();
			System.out.println("return index="+indexResponse.getId());
			
			GetResponse response = client.prepareGet("product_index", "product_type", "1")
					.setOperationThreaded(false)
			        .execute()
			        .actionGet();
			System.out.println("return get="+response.getFields().toString());
			
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		
		/*
		// delete by id
		DeleteResponse response = client.prepareDelete("productIndex", "productType", "1")   
		        .execute()   
		        .actionGet();		
		System.out.println("return DeleteResponse="+response.toString());
		
		// delete by query
		QueryBuilder query = QueryBuilders.fieldQuery("title", "query");  
	    DeleteByQueryResponse deleteByQueryResponse = client.prepareDeleteByQuery("productIndex").setQuery(query).execute().actionGet();		
		System.out.println("return deleteByQueryResponse="+deleteByQueryResponse.toString());


	      // delete by sync
      DeleteResponse syncDeleteResponse = client.prepareDelete("twitter", "tweet", "1")   
    	        .setOperationThreaded(false)   
    	        .execute()   
    	        .actionGet(); 	      
		System.out.println("return syncDeleteResponse="+syncDeleteResponse.toString());
	    */
	     
      // query
      QueryBuilder qb1 = termQuery("name", "kimchy");   
      
      QueryBuilder qb2 = boolQuery()   
                          .must(termQuery("price", 200));   
//                          .mustNot(termQuery("content", "test2"))   
//                          .should(termQuery("content", "test3"));   
         
      QueryBuilder qb3 = filteredQuery(   
                  termQuery("name.first", "shay"),    
                  rangeFilter("age")   
                      .from(23)   
                      .to(54)   
                      .includeLower(true)   
                      .includeUpper(false)   
                  );  
      
		client.close();		
	}

//	@Test
	public void testQuery1() {
		Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));   
		
		SearchResponse response1 = client.prepareSearch().execute().actionGet();
		System.out.println("return response="+response1.getHits().getTotalHits());	      
		
	      QueryBuilder qb2 = boolQuery()   
                  .must(termQuery("price", 200));   

	      SearchResponse response = client.prepareSearch("product_index")   
	    		  	.setTypes("product_type")
	    		  	.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	    		  	.setQuery(QueryBuilders.termQuery("title", "title"))
	    	        .setFrom(0).setSize(60).setExplain(true)   
	    	        .execute()   
	    	        .actionGet();  	      
	      
			System.out.println("return response="+response.toString());	      
	      
	      client.close();
	}
}
