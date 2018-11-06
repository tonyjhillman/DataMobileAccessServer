package com.tjh.couchbaseaccess;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

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
	
	public Boolean furtherResponsesRequired = true;
	
	public String questionParameterValue = "";
	
	public String contingencyMode = "";
       
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
		
		// Hack to stop further responses being sent to client after the first.
		furtherResponsesRequired = true;
		
    	Cluster cluster = CouchbaseCluster.create("localhost");
    	cluster.authenticate("Administrator", "password");
    	
    	// Get the latest data from the met, and put into the document in the metOffice 
    	// bucket. This is switched off right now, because I'm using a document that I'm 
    	// manually updating, so that I can test query results.
    	//
		//MsgSender ms = new MsgSender();
		//String returnedJsonDocument = ms.getMsg();
        // = returnedJsonDocument.replace("\\",""); 
		
    	Bucket bucket = cluster.openBucket("metOffice");  

        //bucket.upsert(RawJsonDocument.create("metOffice0001", returnedJsonDocument));
        bucket.bucketManager().createN1qlPrimaryIndex(true, false);

        // Get the question submitted by the user.
        //
        questionParameterValue = request.getParameter("question");
        System.out.println("CouchbaseAccess:doPost: questionParameter value is: " + questionParameterValue);

        // The weatherAttributes bucket contains documents that specify time-periods and 
        // known weather attributes (such as 'rain', 'umbrella', etc). If we find these in 
        // the question, we can then search the met document to determine the associated 
        // weather conditions.
        //
		Bucket weatherAttributesBucket = cluster.openBucket("weatherAttributes");
		
		// We return, in the end, this string to the user. It contains our weather-prediction, based 
		// on the results of our search.
		//
		String latestPrediction = "";
		
		// Find a weather-condition match some element in the user-specified question. For 'rain', the returned value 
		// will be: '["rain",{"mode":"","role":"cause","attribute":"Pp","valency":"n"}]'.
		//
		WeatherMatcher myWeatherMatcher = new WeatherMatcher();		
		JsonArray retrievedWeatherTokenArray = myWeatherMatcher.MatchWeatherCondition(questionParameterValue, cluster, weatherAttributesBucket);
		System.out.println("CouchbaseAccess:doPost: array received from WeatherMatcher is: " + retrievedWeatherTokenArray.toString());
		
		// Pull the first element in the returned array, to verify which element we got a match on (eg, 'rain').
		//
		String weatherMatchResult = retrievedWeatherTokenArray.getString(0);
		System.out.println("CouchbaseAccess:doPost: weatherMatchResult from retrievedWeatherTokenArray is: " + weatherMatchResult);
		
		// Pull from the array the inner document that has the characteristics of the attribute we have found. Eg:
		// {"mode":"","role":"cause","attribute":"Pp","valency":"n"}.
		//
		JsonObject weatherAttributeTokenCharacteristics = retrievedWeatherTokenArray.getObject(1);
		
		// If no match was found on 'plague of locusts', the inner object took the form '["No match found",null]'.
		//
		if (weatherMatchResult.equals("No match found")) {	
        	response.getWriter().write("Sorry, I didn't understand. Are you asking about the weather?");
        	
        	// End the interaction with the user. FIX: Need to bale properly at this point.
        	//
        	furtherResponsesRequired = false;
        	
		} else {
			foundWeatherMatch = true;
			furtherResponsesRequired = true;
			
			// If the question involves a contingency item, such as a raincoat or an umbrella, 
			// prepare the right syntax for the answer that will be given in 'latestPrediction'.
			//
			if (weatherAttributeTokenCharacteristics.getString("role").equals("contingency")){
				if (weatherAttributeTokenCharacteristics.getString("mode").equals("take")){
					contingencyMode = "take";
				} else {
					if (weatherAttributeTokenCharacteristics.getString("mode").equals("wear")){
						contingencyMode = "wear";
					}
				}
			}
			
			// Now we have found a weather-attribute, we determine what time-period is being asked about.
			//
			System.out.println("CouchbaseAccess:doPost: Passing questionParameterValue to TimeMatcher class, MatchTime method.");
			TimeMatcher myTimeMatcher = new TimeMatcher();
			JsonArray myJsonArray = myTimeMatcher.MatchTime(questionParameterValue, cluster, weatherAttributesBucket);
			System.out.println("CouchbaseAccess:doPost: retrieved array from TimeMatch.MatchTime is: " + myJsonArray.toString());
			
			String foundTimeToken = myJsonArray.getString(0);
			
			// Utility class that provides methods for interpreting elements in the 
			// submitted question. For example, is "Tuesday" today, tomorrow, or the 
			// day after tomorrow.
			//
			PhraseInterpreter myPhraseInterpreter = new PhraseInterpreter();
			
			// If we asked about a day more than 4 days ahead, the returned inner object took the form:
			// '["No match found",null]'. 
			//
			if (foundTimeToken.equals("No match found")) {
				response.getWriter().write("I didn't understand the time you gave me. I can see up to 4 days ahead.");
				
				// End the interaction with the user. FIX: Need to bale properly at this point.
				//
				furtherResponsesRequired = false;
				
			} else {				
				foundTimeToken = foundTimeToken.replace("_"," ");
				
				JsonObject timeAttributesObject = myJsonArray.getObject(1);
				
				if (timeAttributesObject != null) {
					foundTimeMatch = true;
				}
				
				// Derive a specific date-string, from the wording located in the question.
				//
				String userSpecifiedDate = myPhraseInterpreter.deriveDateFromWording(foundTimeToken);
				
				// Class and method to derive a Pp prediction in instances where 'rain' or some associated attribute 
				// (like 'umbrella') has been matched on. This is obviously very clumsy right now. But I wanted at least 
				// to separate the routine out, since at this point, we'd probably want a conditional with various 
				// options (wind, sun, snow, etc).
				//
				DerivePpPrediction myDerivePpPrediction = new DerivePpPrediction();
				latestPrediction = myDerivePpPrediction.returnPpPrediction(weatherAttributeTokenCharacteristics, timeAttributesObject, 
						userSpecifiedDate, bucket, foundTimeToken, weatherMatchResult, 
						contingencyMode);
			}

			// Send the answer to the client. FIX: These conditionals shouldn't be necessary at this point.
			//
	        if (foundTimeMatch && foundWeatherMatch){
	        	if (furtherResponsesRequired){
	        		response.getWriter().write(latestPrediction);
	        		furtherResponsesRequired = false;
	        	}
	        } else {
	        	if (furtherResponsesRequired){
	        		response.getWriter().write("Sorry, I didn't understand that.");
	        		furtherResponsesRequired = false;
	        	}
	        }	
		}
	}
}
