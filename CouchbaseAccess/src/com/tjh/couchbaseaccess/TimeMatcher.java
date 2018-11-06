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

		// First, determine whether the question contains day-names. Change these to the relative 
		// values stored in cdb.
		
		System.out.println("In TimeMatcher:MatchTime: passing to applyRelativeTimeNamesToQuestionString, the question: " + question);
		question = applyRelativeTimeNamesToQuestionString(question);
		System.out.println("In TimeMatcher:MatchTime: returning fro applyRelativeTimeNamesToQuestionString, question becomes: " + question);
		
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
				        System.out.println("In time iterator: question is " + question);
				        System.out.println("In time iterator: setKey is " + setKey);
				        if (question.contains(setKey))
				        {      	
				        	foundMatch = true;
				        	
				        	if (setKey.length() > foundTimeToken.length())
				        	{				        	
				        		foundTimeToken = setKey;				        		
						        foundTimeSpanObject = innerJsonObject.getObject(setKey); 						        
						        foundTimeKeyAndValue = JsonArray.from(foundTimeToken, foundTimeSpanObject);
						        System.out.println("foundTimeKeyAndValue are: " + foundTimeKeyAndValue.toString());
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
	
	// If the user-specified question contains day-names, such as 'Friday' or 
	// 'Tuesday', convert them to relative names (such as 'today', or 'in 2 days 
	// time", so that a match on them can be sought. Append the new value to the 
	// existing question.
	//
	public String applyRelativeTimeNamesToQuestionString(String theQuestion){
		
		System.out.println("In TimeMatcher:applyRelativeNames: theQuestion received is: " + theQuestion);
		
		String userSpecifiedDayName = "";
		
		String[] myDaysOfTheWeekArray = {"Saturday", 
				"Sunday", "Monday", "Tuesday", "Wednesday", 
				"Thursday", "Friday"};
		
		// Find whether there is a day-name employed in the question.
		//
		for (int index = 0; index <= myDaysOfTheWeekArray.length -1; index++){
			
			if (theQuestion.contains(myDaysOfTheWeekArray[index])){
				userSpecifiedDayName = myDaysOfTheWeekArray[index];
				break;
			}
		}
		
		// If we have indeed found a day-name in the user-specified question, use the DateCalculator 
		// to determine its relation to today: that is, how many days in advance it is.
		//
		if(!userSpecifiedDayName.equals("")){
			
			DateCalculator myDateCalculator = new DateCalculator();
			System.out.println("In TimeMatcher:applyRelativeTimeNamesToQuestionString: calculating today's day name via MyDateCalculator:futureDayOfTheWeekAsString");
			String todaysDayName = myDateCalculator.futureDayOfTheWeekAsString(0);
			
			System.out.println("In TimeMatcher:applyRelativeTimeNamesToQuestionString: todaysDayName is: " + todaysDayName);
			System.out.println("In TimeMatcher:applyRelativeTimeNamesToQuestionString: userSpecifiedDayName is: " + userSpecifiedDayName);
			
			int offsetOfFutureDay = 0;
			
			// If the specified day-name is simply today's day-name, we know the offset is zero. Otherwise,
			// use the DateCalculator to determine the offset.
			//
			if (todaysDayName.equals(userSpecifiedDayName) || userSpecifiedDayName.equals("")){	
				offsetOfFutureDay = 0;
			} else {
				offsetOfFutureDay = myDateCalculator.offsetForFutureDayOfTheWeek(todaysDayName, userSpecifiedDayName);	
			}

			System.out.println("offsetOfFutureDay in TimeMatcher is set to: " + offsetOfFutureDay);
			
			// Using the offset, find the appropriate relative terminology for the user-specified day-name.
			//
			switch(offsetOfFutureDay){
				case(0):
					theQuestion = theQuestion + "_" + "today";
					break;
				case(1):
					theQuestion = theQuestion + "_" + "tomorrow";
					break;
				case(2):
					theQuestion = theQuestion + "_" + "in_2_days_time";
					break;
				case(3):
					theQuestion = theQuestion + "_" + "in_3_days_time";
					break;
				case(4):
					theQuestion = theQuestion + "_" + "in_4_days_time";
					break;
				default:
					// Don't change the question.			
			}

		}

		System.out.println("applyRelativeTimeNamesToQuestionString: theQuestion is: " + theQuestion);
		
		return theQuestion;	
	}	
}
