package com.orange.common.elasticsearch;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

import com.orange.common.log.ServerLog;

public class ESQueryBuilder {

	private final static Client client = new TransportClient()
	   .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
	
	
	/**
	 *  Search by a specific field 
	 *  
	 *  @param filed : the field to search
	 *  @param value : the value to match
	 *  */
	public static QueryBuilder findByField(String field, String value) {
		
		if ( field == null || value == null ) 
			return null;
		
		return QueryBuilders.termQuery(field, value);
		
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
	public static QueryBuilder findByWildcard(String field, String wildcardString) {
		
		if ( field == null || wildcardString == null )
			return null;
		
		return QueryBuilders.wildcardQuery(field, wildcardString);
	}
		
	
	
	public static void main(String[] args) {
//		 IndexResponse indexResponse = ESIndexBuilder.indexMongoDB("game","user","mongoindex");
//		 if ( indexResponse == null ) {
//			 ServerLog.info(0, "Index mongodb fails");
//			 return;
//		 }
//		 
		 SearchResponse searchResponse = client.prepareSearch("mongoindex")   
	    		  	.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	    		  	.setQuery(ESQueryBuilder.findByField("is_robot","1")) 
	    	      .setFrom(0).setSize(60).setExplain(true)   
	    	      .execute()   
	    	      .actionGet();  	      
	      
		 SearchHits hits = searchResponse.hits();
		 long totalHits = hits.getTotalHits();
		 if ( totalHits != 0 ) {
			 for ( int i = 0; i < totalHits; i++ ) {
				 System.out.println(hits.getAt(i).getSource().get("nick_name"));  
			 }
		 }
	}
}
