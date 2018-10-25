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
 * Checks a user-specified question-string for the presence of a weather-expression, used to
 * support querying on weather-data.
 * 
 * @author tonyhillman
 *
 */
public class WeatherMatcher {
	
	public JsonArray foundWeatherKeyAndValue = null;
	public Boolean foundMatch = false;

	public WeatherMatcher() {
	}
	
	public JsonArray MatchWeatherCondition(String question, Cluster cluster, Bucket weatherAttributesBucket) {		 
        N1qlQueryResult arrayLengthCalculationResult = weatherAttributesBucket.query(       		 
        	N1qlQuery.simple("SELECT ARRAY_COUNT(attribute_tokens) AS total_members FROM weatherAttributes")
        );
        
        System.out.println("Hello");
        
        Integer arrayLength = 0;
        String returnValue = "No match found";
        
        for (N1qlQueryRow arrayLengthRows : arrayLengthCalculationResult) {	 
            JsonObject totalJsonObject = arrayLengthRows.value();
            arrayLength = totalJsonObject.getInt("total_members");
        }

        for (int i = 0; i < arrayLength; i++) {
        	N1qlQueryResult arrayValueResult = weatherAttributesBucket.query(	    		  	
	        	N1qlQuery.simple("SELECT attribute_tokens[" + i + "] FROM weatherAttributes")
	        );
	        
	        JsonObject innerJsonObject = null;
	        
	        for (N1qlQueryRow arrayValue : arrayValueResult) {	
	            JsonObject outerJsonObject = arrayValue.value();
	
			    if (outerJsonObject.containsKey("$1")) {
			    	innerJsonObject = outerJsonObject.getObject("$1");

					Set<String> NameSet = innerJsonObject.getNames();

					Iterator<String> iterator = NameSet.iterator();
					
				    while (iterator.hasNext()) {
				        String setKey = iterator.next();
				        
				        if (question.contains(setKey)) {
				        	returnValue = setKey;
				        	foundMatch = true;
				        	
				        	JsonObject weatherAttributeTokenCharacteristics = innerJsonObject.getObject(setKey);
				        	Set<String> NewNameSet = weatherAttributeTokenCharacteristics.getNames();
				        	System.out.println("...........name set is: " + NewNameSet.toString());
				        	//JsonObject testObject = weatherAttributeTokenCharacteristics.getObject(setKey);
				        	System.out.println("...........testObject is: " + weatherAttributeTokenCharacteristics.toString());
				        	
				        	foundWeatherKeyAndValue = JsonArray.from(setKey, weatherAttributeTokenCharacteristics);
				        	
				        	break;
				        } 
			        }
				    
				    if (!foundMatch){
				    	foundWeatherKeyAndValue = JsonArray.from("No match found", null);
				    }
			    }
	        }  
        }
        //return returnValue;
        return foundWeatherKeyAndValue;
	}
}
