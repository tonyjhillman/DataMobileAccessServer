package com.tjh.couchbaseaccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;

/**
 * Servlet implementation class CouchbaseAccess
 * 
 */
@WebServlet("/CouchbaseAccess")
public class CouchbaseAccess extends HttpServlet 
{
	public Boolean foundWeatherMatch = false;
	public Boolean foundTimeMatch = false;
	
	public String predictionRider = "";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CouchbaseAccess() 
    {
        super();
    }

	@SuppressWarnings("deprecation")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Request received.....handling a POST now.");
		
    	Cluster cluster = CouchbaseCluster.create("localhost");
    	cluster.authenticate("Administrator", "password");
    	
		MsgSender ms = new MsgSender();
		String returnedJsonDocument = ms.getMsg();
		
    	Bucket bucket = cluster.openBucket("metOffice");  
        returnedJsonDocument = returnedJsonDocument.replace("\\",""); 
        
        //bucket.upsert(RawJsonDocument.create("metOffice0001", returnedJsonDocument));
        bucket.bucketManager().createN1qlPrimaryIndex(true, false);

        String questionParameterValue = request.getParameter("question");

		Bucket weatherAttributesBucket = cluster.openBucket("weatherAttributes");
		
		String latestPrediction = "";
		
		WeatherMatcher myWeatherMatcher = new WeatherMatcher();
		//String weatherMatchResult = myWeatherMatcher.MatchWeatherCondition(questionParameterValue, cluster, weatherAttributesBucket);
		
		JsonArray retrievedWeatherTokenArray = myWeatherMatcher.MatchWeatherCondition(questionParameterValue, cluster, weatherAttributesBucket);
		
		String weatherMatchResult = retrievedWeatherTokenArray.getString(0);
		JsonObject weatherAttributeTokenCharacteristics = retrievedWeatherTokenArray.getObject(1);
		
		if (weatherMatchResult.equals("No match found")) {	
        	//response.getWriter().write("No weather match found.");
			
			System.out.println("No weather match found.");
			
			// QUESTION: SHALL WE DEFAULT TO 'GOOD' AND CARRY ON?
        	
		} else {
			foundWeatherMatch = true;
			
			TimeMatcher myTimeMatcher = new TimeMatcher();
			JsonArray myJsonArray = myTimeMatcher.MatchTime(questionParameterValue, cluster, weatherAttributesBucket);
			
			String foundTimeToken = myJsonArray.getString(0);
			
			if (foundTimeToken.equals("No match found")) {
				//response.getWriter().write("I didn't understand the time you gave me.");
				
				// QUESTION: SHALL WE DEFAULT TO 'TODAY' AND CARRY ON?
				
			} else {				
				foundTimeToken = foundTimeToken.replace("_"," ");
				
				JsonObject timeAttributesObject = myJsonArray.getObject(1);
				
				if (timeAttributesObject != null) {
					foundTimeMatch = true;
				}
				
				String userSpecifiedDate = deriveDateFromWording(foundTimeToken);
				
				if (weatherAttributeTokenCharacteristics.getString("attribute").equals("Pp")){
					System.out.println("yes, it is Pp");
				}
				
				int highestPpValueFound = deriveHighestPp(timeAttributesObject, userSpecifiedDate, bucket);
				
				Verbalizer myVerbalizer = new Verbalizer();
		        latestPrediction = myVerbalizer.verbalizeLikelihoodOfRain(highestPpValueFound); 
		        
		        predictionRider = "";
		        
		        
		        if (weatherAttributeTokenCharacteristics.getString("role").equals("contingency")){

		        	String myContingencyAdvice = myVerbalizer.verbalizeDegreeOfNeedForContingencyItem(highestPpValueFound);
		        	predictionRider = "";
		        	predictionRider = predictionRider + myContingencyAdvice + weatherMatchResult; 	
				}
		        
		        if (weatherAttributeTokenCharacteristics.getString("role").equals("liability")){

		        	String myLiabilityAdvice = myVerbalizer.verbalizeDegreeOfAvoidanceForLiabilityItem(highestPpValueFound);
		        	
		        	predictionRider = predictionRider + myLiabilityAdvice + weatherMatchResult; 	
				}
		        
		        String myEffectAdvice = "";
		        
		        if (weatherAttributeTokenCharacteristics.getString("role").equals("effect") && 
		        		weatherAttributeTokenCharacteristics.getString("valency").equals("n")){

		        	myEffectAdvice = myVerbalizer.verbalizePossibilityOfNegativeEffectItem(highestPpValueFound) + weatherMatchResult + ". ";	
				} 
		        
		        if (weatherAttributeTokenCharacteristics.getString("role").equals("effect") && 
		        		weatherAttributeTokenCharacteristics.getString("valency").equals("p")){
		        	
		        	myEffectAdvice = "";

		        	myEffectAdvice = myVerbalizer.verbalizePossibilityOfPositiveEffectItem(highestPpValueFound) + weatherMatchResult + ". ";	
				} 
		        
		        latestPrediction = myEffectAdvice  + latestPrediction + " " + foundTimeToken + "." + predictionRider; 
		        
		        
			}
		}
        
        if (foundTimeMatch && foundWeatherMatch){
        	response.getWriter().write(latestPrediction);
        } else {
        	response.getWriter().write("Sorry, I didn't understand that.");
        }	
	}
	
	public String deriveDateFromWording(String aFoundTimeToken){
		
		DateCalculator myDateCalculator = new DateCalculator();
		String userSpecifiedDate = "";
		
		if (aFoundTimeToken.contains("today") || 
				aFoundTimeToken.contains("this afternoon") || 
				aFoundTimeToken.contains("this evening") || 
				aFoundTimeToken.contains("tonight")){
			
			userSpecifiedDate = myDateCalculator.todaysDatePlusOffset(0);
			
		} else {
		
	        if (aFoundTimeToken.contains("tomorrow")){
	        	
	        	if (aFoundTimeToken.contains("day after")){
	        		
	        		userSpecifiedDate = myDateCalculator.todaysDatePlusOffset(2);
	        		
	        	} else {
	        		
	        		userSpecifiedDate = myDateCalculator.todaysDatePlusOffset(1);
	        	}
	        	
	        	System.out.println("The user specified date is " + userSpecifiedDate);
	        }
		}
		
		return userSpecifiedDate;
	}
	
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
