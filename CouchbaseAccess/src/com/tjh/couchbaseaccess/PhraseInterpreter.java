package com.tjh.couchbaseaccess;

public class PhraseInterpreter {

	public PhraseInterpreter() {
		
	}
	
	// Changes the foundTimeToken to a day-name, in cases where it 
	// was user-specified as a number of days in the future. Sounds better 
	// when read back.
	//
	public String deriveDayFromWording(String aFoundTimeToken){
		
		System.out.println("In deriveDayFromWording - aFoundTimeToken is: " + aFoundTimeToken);
		
		DateCalculator myDateCalculator = new DateCalculator();
		
		String originalFoundTimeToken = aFoundTimeToken;
		
		if (aFoundTimeToken.contains("in 2 days time") || 
				aFoundTimeToken.contains("2 days from now")){

			aFoundTimeToken = myDateCalculator.futureDayOfTheWeekAsString(2);
			
			aFoundTimeToken = augmentDayWithSegment(aFoundTimeToken, originalFoundTimeToken);

			System.out.println("In CA, aFoundTimeToken for 2 days from now is: " + aFoundTimeToken);
			
	    } else {
	    	
	    	if (aFoundTimeToken.contains("in 3 days time") || 
	    			aFoundTimeToken.contains("3 days from now")){
	    		
	    		aFoundTimeToken = myDateCalculator.futureDayOfTheWeekAsString(3);
	    		
	    		aFoundTimeToken = augmentDayWithSegment(aFoundTimeToken, originalFoundTimeToken);
	    		
				System.out.println("In CA, aFoundTimeToken for 3 days from now is: " + aFoundTimeToken);
	    		
	    	} else {
	    		
	    		if (aFoundTimeToken.contains("in 4 days time") || 
	    				aFoundTimeToken.contains("4 days from now")){
	    			
	    			aFoundTimeToken = myDateCalculator.futureDayOfTheWeekAsString(4);
	    			
	    			aFoundTimeToken = augmentDayWithSegment(aFoundTimeToken, originalFoundTimeToken);
	    			
	    			System.out.println("In CA, aFoundTimeToken for 4 days from now is: " + aFoundTimeToken);
	    			
	    		} 
	    	}
	    }
		
		return aFoundTimeToken;
	}
	
	// Figure out a specific date-string, inferred from relative terms employed
	// by the user.
	//
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
	        	
	        	System.out.println("Reached tomorrow passage.");
	        	
	        	if (aFoundTimeToken.contains("day after")){
	        		
	        		userSpecifiedDate = myDateCalculator.todaysDatePlusOffset(2);
	        		
	        	} else {
	        		
	        		userSpecifiedDate = myDateCalculator.todaysDatePlusOffset(1);
	        		System.out.println("In pure tomorrow passage, the user specified date is " + userSpecifiedDate);
	        	}
	        	
	        	System.out.println("The user specified date is " + userSpecifiedDate);
	        
	        } else {
	        	
	        	if (aFoundTimeToken.contains("2 days from now") || 
	        			aFoundTimeToken.contains("in 2 days time")){
	        		
	        		userSpecifiedDate = myDateCalculator.todaysDatePlusOffset(2);
	        		
	        	} else {
	        		
	        		if (aFoundTimeToken.contains("3 days from now") || 
	        				aFoundTimeToken.contains("in 3 days time")){
	        			
	        			userSpecifiedDate = myDateCalculator.todaysDatePlusOffset(3);
	        			
	        		} else {
	        			
	        			if (aFoundTimeToken.contains("4 days from now") || 
	        					aFoundTimeToken.contains("in 4 days time")){
	        				
	        				userSpecifiedDate = myDateCalculator.todaysDatePlusOffset(4);        				
	        			} 
	        		}
	        	}
	        } 
		}
		
		return userSpecifiedDate;
	}
	
	// When analyzing the user's question, determine whether elements such as 'morning', 'afternoon', and
	// 'evening' are used, so qualifying the initially matched element (such as 'today' or 'tomorrow'), and 
	// modify the inference appropriately.
	//
	public String augmentDayWithSegment(String theModifiedTimeToken, String theUnmodifiedTimeToken){
		
		if (theUnmodifiedTimeToken.contains("morning")){
			
			theModifiedTimeToken = theModifiedTimeToken + " " + "morning";
			
		} else { 
			if (theUnmodifiedTimeToken.contains("afternoon")){
				
				theModifiedTimeToken = theModifiedTimeToken + " " + "afternoon";	
				
			} else {
				
				if (theUnmodifiedTimeToken.contains("evening")){
					
					theModifiedTimeToken = theModifiedTimeToken + " " + "evening";
					
				} else {
					
					if (theUnmodifiedTimeToken.contains("night")){
						
						theModifiedTimeToken = theModifiedTimeToken + " " + "night";
						
					}
				}
			}
		}
		
		return theModifiedTimeToken;
	}

}
