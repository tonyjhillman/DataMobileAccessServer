package com.tjh.couchbaseaccess;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import java.util.Iterator;
import java.util.Set;

import com.couchbase.client.java.Bucket;

public class DerivePpPrediction {
	
	public String latestPrediction = "";
	
	public String predictionRider = "";
	
	public PhraseInterpreter newPhraseInterpreter = null;

	public DerivePpPrediction() {
		newPhraseInterpreter = new PhraseInterpreter();
	}
	
	public String returnPpPrediction(JsonObject weatherAttributeTokenCharacteristics, JsonObject timeAttributesObject, 
			String userSpecifiedDate, Bucket bucket, String foundTimeToken, String weatherMatchResult, String contingencyMode){
		
		if (weatherAttributeTokenCharacteristics.getString("attribute").equals("Pp")){
			System.out.println("yes, it is Pp");
			
			int highestPpValueFound = deriveHighestPp(timeAttributesObject, userSpecifiedDate, bucket);
			
			if (highestPpValueFound == 500){
				//response.getWriter().write("No precipitation value found. Sorry.");
				//furtherResponsesRequired = false;
			}
			
			// Figure out the basic, verbalized prediction, based on the weather-attribute and 
			// time-period specified by the user.
			//
			Verbalizer myVerbalizer = new Verbalizer();
	        latestPrediction = myVerbalizer.verbalizeLikelihoodOfRain(highestPpValueFound); 
	        
	        // Now add wording to the basic prediction.
	        
	        predictionRider = "";
	        
	        // In the answer, reference the day specified by the user. We may not want to repeat the exact 
	        // formula. 
	        //
	        System.out.println("In CA: deriveDayFromWording, foundTimeToken value going in is: " + foundTimeToken);
	        foundTimeToken = newPhraseInterpreter.deriveDayFromWording(foundTimeToken);
	        System.out.println("In CA: deriveDayFromWording, foundTimeToken value coming out is: " + foundTimeToken);
	        
	        // Possibly add a phrase like 'so you may want to take your umbrella'.
	        //
	        if (weatherAttributeTokenCharacteristics.getString("role").equals("contingency")){

	        	String myContingencyAdvice = myVerbalizer.verbalizeDegreeOfNeedForContingencyItem(highestPpValueFound, contingencyMode);
	        	predictionRider = "";
	        	predictionRider = predictionRider + myContingencyAdvice + weatherMatchResult; 	
			}
	        
	        // Possibly add a phrase like 'so it may not be a good idea to...(walk)'
	        //
	        if (weatherAttributeTokenCharacteristics.getString("role").equals("liability")){

	        	String myLiabilityAdvice = myVerbalizer.verbalizeDegreeOfAvoidanceForLiabilityItem(highestPpValueFound);
	        	
	        	predictionRider = predictionRider + myLiabilityAdvice + weatherMatchResult; 	
			}
	        
	        String myEffectAdvice = "";
	        
	        // Add phrase like 'not likely to be' or 'may be' or 'should be', or whatever.
	        //
	        if (weatherAttributeTokenCharacteristics.getString("role").equals("effect") && 
	        		weatherAttributeTokenCharacteristics.getString("valency").equals("n")){

	        	myEffectAdvice = myVerbalizer.verbalizePossibilityOfNegativeEffectItem(highestPpValueFound) + weatherMatchResult + ". ";	
			} 
	        
	        if (weatherAttributeTokenCharacteristics.getString("role").equals("effect") && 
	        		weatherAttributeTokenCharacteristics.getString("valency").equals("p")){
	        	
	        	myEffectAdvice = "";

	        	myEffectAdvice = myVerbalizer.verbalizePossibilityOfPositiveEffectItem(highestPpValueFound) + weatherMatchResult + ". ";	
			} 
	        
	        // Concatenate the phrases into the full answer.
	        //
	        latestPrediction = myEffectAdvice  + latestPrediction + " " + foundTimeToken + "." + predictionRider; 

		} else {
			System.out.println("This wasn't about Pp");
		}
		
		return latestPrediction;
	}
	
	
	// Determine the highest precipitation percentage within the time-spans specified 
	// by the user.
	//
	public int deriveHighestPp(JsonObject theTimeAttributesObject, String theUserSpecifiedDate, Bucket theBucket){
        
        GetPpValues myGetPpValues = new GetPpValues();
	    int highestPpValueFound = 0;
	    int newHighestPpValueFound = 0;

	    // The span-periods that the user is interested in (as indicated by, say, the word 
	    // 'tomorrow'. These will be searched for 
		JsonArray yArray = theTimeAttributesObject.getArray("span-periods");

		for (int i = 0; i < yArray.size(); ++i){
    	    JsonObject temp = yArray.getObject(i);

    	    Set<String> NameSet = temp.getNames();
    	    
    	    Iterator<String> iterator = NameSet.iterator();
    	    
    	    while(iterator.hasNext()) {
    	        String setKey = iterator.next();	
    	        
    	        // Searches for a new span-period each time.
    			newHighestPpValueFound = myGetPpValues.getThePpValues(theUserSpecifiedDate, setKey, theBucket);
    			
    			// FIX
    			if (newHighestPpValueFound > highestPpValueFound){
    				highestPpValueFound = newHighestPpValueFound;
    			}
    			
    			System.out.println("highestPpValueFound in CouchbaseAccess recorded as: " + highestPpValueFound);
    			
    			if (highestPpValueFound == 500){
    				System.out.println("Error: No Pp value was found.");
    			}
    	    }  
    	}
		
		return highestPpValueFound;
	}
}
 	    
	


