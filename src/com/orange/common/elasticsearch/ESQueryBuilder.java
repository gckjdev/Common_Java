package com.orange.common.elasticsearch;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

public class ESQueryBuilder {

	private final static Client client = new TransportClient()
	   .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
	
	
	/**
	 *  Search by a specific field 
	 *  
	 *  @param filed : the field to search
	 *  @param value : the value to match
	 *  */
	public static SearchResponse findByField(String indexName, String field, String value) {
		
		if ( indexName == null || field == null || value == null ) 
			return null;
		
		SearchResponse searchResponse = client.prepareSearch(indexName)   
    		  							.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    		  							.setQuery(QueryBuilders.matchQuery(field, value)) 
    		  							.setFrom(0).setSize(60).setExplain(true)   
    		  							.execute()   
    		  							.actionGet();  	      
		
		return searchResponse;
	}
	
	/**
	 *  Search by wildcard String, * for arbitrary-length string,
	 *   ? for a single character.
	 *  
	 *  @param filed : the field to search
	 *  @param value : the wildcard string value to match
	 *  
	 *   Note : Don't put * or ? at the first character of the wildcard
	 *          string.
	 *  */
	public static SearchResponse findByWildcard(String indexName, String field, String wildcardString) {
		
		if ( indexName == null || field == null || wildcardString == null )
			return null;
		
		SearchResponse searchResponse = client.prepareSearch(indexName)   
    		  							.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    		  							.setQuery(QueryBuilders.wildcardQuery(field, wildcardString)) 
    		  							.setFrom(0).setSize(60).setExplain(true)   
    		  							.execute()   
    		  							.actionGet();  	      
		
		return searchResponse;
	}
		
	
	public static MultiSearchResponse multiMatchSearch(String indexName, String value) {
		
		if ( indexName == null || value == null )
			return null;
		
		SearchRequestBuilder srb1 = client.prepareSearch()
				 .setQuery(QueryBuilders.matchQuery("nick_name", value))
				 .setSize(1);
		SearchRequestBuilder srb2 = client.prepareSearch()
	             .setQuery(QueryBuilders.matchQuery("sina_nick", value))
	             .setSize(1);
		SearchRequestBuilder srb3 = client.prepareSearch()
	             .setQuery(QueryBuilders.matchQuery("qq_nick", value))
	             .setSize(1);
		SearchRequestBuilder srb4 = client.prepareSearch()
	             .setQuery(QueryBuilders.matchQuery("sina_id", value))
	             .setSize(1);
		SearchRequestBuilder srb5 = client.prepareSearch()
	             .setQuery(QueryBuilders.matchQuery("qq_id", value))
	             .setSize(1);
		SearchRequestBuilder srb6 = client.prepareSearch()
	             .setQuery(QueryBuilders.matchQuery("facebook_id", value))
	             .setSize(1);
		SearchRequestBuilder srb7 = client.prepareSearch()
	             .setQuery(QueryBuilders.matchQuery("email", value))
	             .setSize(1);
		SearchRequestBuilder srb8 = client.prepareSearch()
	             .setQuery(QueryBuilders.matchQuery("signature", value))
	             .setSize(1);
		
	    MultiSearchResponse msr = client.prepareMultiSearch()
			        .add(srb1)
			        .add(srb2)
			        .add(srb3)
			        .add(srb4)
			        .add(srb5)
			        .add(srb6)
			        .add(srb7)
			        .add(srb8)
			        .execute().actionGet();

		return msr;
	}
	
	public static void main(String[] args) {
//		 IndexResponse indexResponse = ESIndexBuilder.indexMongoDB("game","user","mongoindex");
//		 if ( indexResponse == null ) {
//			 ServerLog.info(0, "Index mongodb fails");
//			 return;
//		 }

		 SearchResponse searchResponse = findByField("mongoindex", "is_robot", "1");
		 SearchHits hits = searchResponse.hits();
		 long totalHits = hits.getTotalHits();
		 if ( totalHits != 0 ) {
			 for ( int i = 0; i < totalHits; i++ ) {
				 System.out.println(hits.getAt(i).getSource().get("nick_name"));  
			 }
		 }
	}

	
}
