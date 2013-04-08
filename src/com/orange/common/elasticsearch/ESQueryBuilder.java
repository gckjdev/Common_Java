package com.orange.common.elasticsearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.Source;

import org.apache.cassandra.cli.CliParser.newColumnFamily_return;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.percolator.PercolatorExecutor.SourceRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
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
	 * Like searchByField, but it tries to match multiplek fields.
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
//		String fieldToSearch = "nick_name";
//		String matchText = "皮　彭";
//		int start1 = 0;
//		int offset1 = 100;
//		SearchResponse searchResponse = searchByField(indexName, fieldToSearch, matchText, start1, offset1);
//		SearchHits hits = searchResponse.hits();
//		long totalHits1 = hits.getTotalHits();
//		if ( hits.getTotalHits() != 0 ) {
//			 long count = start1+offset1 < totalHits1 ? start1+offset1 : totalHits1;
//			 for ( int i = start1; i < count; i++ ) {
//				 ServerLog.info(0, i + " : " + hits.getAt(i).getSource().get("nick_name").toString());  
//			 }
//		}
	
		
		// Test for searchByMultiMatch
		String textVal = "皮 凌哲";
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
//		candidateFields.add("user_id");
		int start2 = 0;
		int offset2 = 10;
		// 降序比较器，按搜索得分从高到低排列
		Map<Float, Map<String, Object>> scoreResult = new TreeMap<Float, Map<String, Object>>(
				new Comparator<Float>() {
					@Override
					public int compare(Float f1, Float f2) {
						// Don't do like this :   return (int)(f2-f1),
						// because cast into int will loss precision. That said, 0.xxx will end up being 0.
						// This makes the map thinks they are the same key, and thus overwrite the privious 
						// key-value pair stored.
						if ( f2 - f1 < 0.0) 
							return -1;
						else 
							return 1;
					}
				});
		MultiSearchResponse msr = searchByMultiMatch(indexName, candidateFields, textVal, start2, offset2);
		// 把每一个字段的查找结果，按“得分”和“user ID“键值对放入TreeMap中（使其按得分从高到低排列）
		for (MultiSearchResponse.Item item : msr.responses()) {
		    SearchResponse response = item.response();
		    long totalHits2 = response.hits().totalHits();
		    if ( totalHits2 != 0) {
		    	long count = start2 + offset2 < totalHits2 ? start2 + offset2 : totalHits2;
		    	for ( int i = start2; i < count; i++ ) {
		    		SearchHit searchHit = response.hits().getAt(i);
		    		float score = searchHit.getScore();
		    		Map<String, Object> source = searchHit.getSource();
			    	scoreResult.put(score, source);
		    	}
		    }
		}
		List<Map<String, Object>> sourceList = new ArrayList<Map<String,Object>>();
		for (Map.Entry<Float, Map<String, Object>> entry: scoreResult.entrySet()) {
			sourceList.add(entry.getValue());
		}
		// 截取需要的范围返回
		int first = start2 >= sourceList.size() ? sourceList.size()-1 : start2;
		int last = start2+offset2 <= sourceList.size()-1 ? start2+offset2 : sourceList.size()-1;
		List<Map<String, Object>>  result = sourceList.subList(first, last);

		ServerLog.info(0, "Total : " + result.size());
		for (Map<String, Object> e: result) {
			ServerLog.info(0, e.toString());
		}
	}

	
}
