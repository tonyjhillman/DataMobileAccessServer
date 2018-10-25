package com.tjh.couchbaseaccess;

import java.util.Iterator;
import java.util.Set;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;

/**
 * Checks a user-specified question-string for the presence of a time-expression, used to
 * support querying on weather-data.
 * 
 * @author tonyhillman
 *
 */
public class TimeMatcher {
	
	/*
	 * A time, expressed in words; such as "tomorrow", or "tomorrow afternoon".
	 */
	public String foundTimeToken = "";
	
	/*
	 * The object that is the value of the key whose symbol is the foundTimeToken.
	 */
	public JsonObject foundTimeSpanObject = null;
	
	/*
	 * The key that corresponds to the time-period specified by the user, plus 
	 * its associated value from the document, as a two-member array.
	 */
	public JsonArray foundTimeKeyAndValue = null;
	
	public Boolean foundMatch = false;

	public TimeMatcher() {
	}
	
	/*
	 * Open a given bucket within a given cluster, and check the time-specific keys against the
	 * content of the user-specified string. If a match is found, return the object that is the
	 * value of the successfully matched key.
	 */
	public JsonArray MatchTime(String question, Cluster cluster, Bucket weatherAttributesBucket) {
        N1qlQueryResult timeArrayLengthCalculationResult = weatherAttributesBucket.query(
        		  	
        	N1qlQuery.simple("SELECT ARRAY_COUNT(time_tokens) AS total_time_members FROM weatherAttributes")
        );
        
        Integer totalNumberOfArrayMembers = 0;

        for (N1qlQueryRow timeArrayLengthRows : timeArrayLengthCalculationResult) 
        {	  
            JsonObject jsonObject = timeArrayLengthRows.value();
            if (jsonObject.getInt("total_time_members") != null)
            {
            	totalNumberOfArrayMembers = jsonObject.getInt("total_time_members");
            }
        }
    
        for (int i = 0; i < totalNumberOfArrayMembers; i++) {
	        N1qlQueryResult timeArrayValueResult = weatherAttributesBucket.query(	  	
	        	N1qlQuery.simple("SELECT time_tokens[" + i + "] FROM weatherAttributes")
	        );
	        
	        JsonObject innerJsonObject = null;
	        
	        for (N1qlQueryRow timeArrayValue : timeArrayValueResult) 
	        {	
	            JsonObject outerJsonObject = timeArrayValue.value();
	
			    if (outerJsonObject.containsKey("$1"))
			    {
			    	innerJsonObject = outerJsonObject.getObject("$1");  	
					Set<String> NameSet = innerJsonObject.getNames();					
					Iterator<String> iterator = NameSet.iterator();
					
				    while(iterator.hasNext()) 
				    {
				        String setKey = iterator.next();

				        if (question.contains(setKey))
				        {      	
				        	foundMatch = true;
				        	
				        	if (setKey.length() > foundTimeToken.length())
				        	{				        	
				        		foundTimeToken = setKey;				        		
						        foundTimeSpanObject = innerJsonObject.getObject(setKey); 						        
						        foundTimeKeyAndValue = JsonArray.from(foundTimeToken, foundTimeSpanObject);
				        	}
				        }
			        }			  			        
			    }
	        }  
        }	
        if (!foundMatch){
        	foundTimeKeyAndValue = JsonArray.from("No match found", null);
	    }
        
        return foundTimeKeyAndValue;
	}
}
