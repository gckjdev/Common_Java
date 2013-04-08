package com.orange.common.elasticsearch;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

import com.orange.common.log.ServerLog;

public class ESQueryBuilder {

	private final static Client client = new TransportClient()
	   .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
	
	
	/**
	 *  Search by a text, return all responses which have tokens within the text. 
	 *  
	 *  @param filed : the field to search
	 *  @param textVal : the text to match
	 *  @param start : the start index of response we are interested ins
	 *  @param offset: how many result to be returned.
	 *  */
	public static SearchResponse searchByField(String indexName, String field, String textVal, int start, int offset) {
		
		if ( indexName == null || field == null || textVal == null ) {
			ServerLog.info(0, "Please input proper arguments to search.");
			return null;
		}
		
		if ( start < 0 || offset < 0 ) {
			ServerLog.info(0, "Please input a positive start and offset.");
			return null;
		}
		
		
		SearchResponse searchResponse = client.prepareSearch(indexName)   
    		  							.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    		  							.setQuery(QueryBuilders.matchQuery(field, textVal)) 
    		  							.setFrom(start).setSize(offset).setExplain(true)   
    		  							.execute()   
    		  							.actionGet();  	      
		
		return searchResponse;
	}
	
	/**
	 *  Search by wildcard String, * for arbitrary-length string,
	 *   ? for a single character.
	 *  
	 *  @param indexName: the index name
	 *  @param filed    : the field to search
	 *  @param value    : the wildcard string value to match
	 *  @param start    : the start index of response we are interested in
	 *  @param offset   : how many result to be returned.
	 *  
	 *   Note : Don't put * or ? at the first character of the wildcard
	 *          string.
	 **/
	public static SearchResponse searchByWildcard(String indexName, String field, String wildcardString,
			                                   int start, int offset) {
		
		if ( indexName == null || field == null || wildcardString == null ) {
			ServerLog.info(0, "Please input proper arguments to search");
			return null;
		}
		
		if ( start < 0 || offset < 0 ) {
			ServerLog.info(0, "Please input a positive start and offset.");
			return null;
		}
		
		SearchResponse searchResponse = client.prepareSearch(indexName)   
    		  							.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    		  							.setQuery(QueryBuilders.wildcardQuery(field, wildcardString)) 
    		  							.setFrom(start).setSize(offset).setExplain(true)   
    		  							.execute()   
    		  							.actionGet();  	      
		
		return searchResponse;
	}
		
	/**
	 * Like searchByField, but it tries to match multiple fields.
	 *  
	 *   
	 * @param indexName     : the index name 
	 * @param cadidateFields: a list of fields to consider 
	 * @param textVal       : the text value to search
	 * @param start         : the start index of response we are interested in
	 * @param offset        : how many result to be returned. 
	 */
	public static MultiSearchResponse searchByMultiMatch(String indexName, List<String> candidateFields,
					String textVal, int start, int offset) {
		
		if ( indexName == null || candidateFields.isEmpty() || textVal == null ) {
			ServerLog.info(0, "Please input proper arguments to search");
			return null;
		}
		
		List<SearchRequestBuilder> srbList = new ArrayList<SearchRequestBuilder>();
		for (int i = 0; i < candidateFields.size(); i++) {
			srbList.add(client.prepareSearch(indexName)
					          .setQuery(QueryBuilders.matchQuery(candidateFields.get(i), textVal))
					          .setFrom(start)
					          .setSize(offset));
		}
		
	    MultiSearchRequestBuilder msRequestBuilder = client.prepareMultiSearch();
	    for ( SearchRequestBuilder srb: srbList) {
	    	msRequestBuilder.add(srb);
	    }
	    
	    if (msRequestBuilder != null) {
	    	MultiSearchResponse msResponse = msRequestBuilder.execute().actionGet();
	    	return msResponse;
	    }

	    return null;
	}
	
	public static void main(String[] args) {

		String indexName = "mongoindex";
		
		// Test for searchByFiled
		String fieldToSearch = "nick_name";
		String matchText = "皮　彭";
		int start1 = 0;
		int offset1 = 100;
		SearchResponse searchResponse = searchByField(indexName, fieldToSearch, matchText, start1, offset1);
		SearchHits hits = searchResponse.hits();
		long totalHits1 = hits.getTotalHits();
		if ( hits.getTotalHits() != 0 ) {
			 long count = start1+offset1 < totalHits1 ? start1+offset1 : totalHits1;
			 for ( int i = start1; i < count; i++ ) {
				 ServerLog.info(0, i + " : " + hits.getAt(i).getSource().get("nick_name").toString());  
			 }
		}
	
		
		// Test for searchByMultiMatch
		String textVal = "皮皮彭";
		List<String> candidateFields = new ArrayList<String>();
		// nick_name, email ,sina_nick,qq_nick, sina_id, qq_id, facebook_id, signature, user_id;
		candidateFields.add("nick_name");
		candidateFields.add("email");
		candidateFields.add("sina_nick");
		candidateFields.add("qq_nick");
		candidateFields.add("sina_id");
		candidateFields.add("qq_id");
		candidateFields.add("facebook_id");
		candidateFields.add("signature");
		candidateFields.add("user_id");
		int start2 = 0;
		int offset2 = 10;
		MultiSearchResponse msr = searchByMultiMatch(indexName, candidateFields, textVal, start2, offset2);
		for (MultiSearchResponse.Item item : msr.responses()) {
		    SearchResponse response = item.response();
		    long totalHits2 = response.hits().totalHits();
		    if ( totalHits2 != 0) {
		    	long count = start2 + offset2 < totalHits2 ? start2 + offset2 : totalHits2;
		    	ServerLog.info(0, " ========== ");
		    	for ( int i = start2; i < count; i++ ) {
		    		ServerLog.info(0, response.hits().getAt(i).getSource().toString());
		    	}
		    }
		}
	}

	
}
