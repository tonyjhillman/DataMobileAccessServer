package com.tjh.couchbaseaccess;

import java.util.Iterator;
import java.util.Set;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;

public class GetPpValues {
	
	public Integer highestPpValueFound = 0;
	
	public Boolean foundDate = false;

	public GetPpValues() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Method to get a Pp value for the specified span-period.
	 * 
	 */
	public int getThePpValues(String theDateSpecifiedByTheUser, String startOfSpanPeriod, Bucket bucket) {
		// Iterate over all five days' worth of data.
        for (int y = 0; y <= 4; y++)
        {
    		N1qlQueryResult resultq = bucket.query(	  	
    	       N1qlQuery.simple("SELECT SiteRep.DV.Location.Period[" + y  + "] FROM `metOffice`")
    				);
    		
	        for (N1qlQueryRow row2 : resultq) 
	        {	
	            JsonObject jsonObject = row2.value();
	            
	            if (jsonObject.containsKey("$1"))
	            {
	            	JsonObject newJsonObject = jsonObject.getObject("$1");

	        		Set<String> NameSet = newJsonObject.getNames();
	        		
	        		Iterator<String> iterator = NameSet.iterator();

	        	    while(iterator.hasNext()) 
	        	    {
	        	        String setKey = iterator.next();

	        	        if (setKey.equals("value") )
	        	        {
	        	        	String myString = newJsonObject.getString(setKey);

	        	        	if (myString.equals(theDateSpecifiedByTheUser))
	        	        	{
	        	        		foundDate = true;
	        	        		
	        	        		if (newJsonObject.containsKey("Rep"))
	        	        		{
	        	        			JsonArray innerJsonArray = newJsonObject.getArray("Rep");

	        	        			for (int q = 0; q <= innerJsonArray.size() -1; q++)
	        	        			{
	        	        				JsonObject inmostJsonObject = (JsonObject) innerJsonArray.get(q);
	        	        				String spanValue = inmostJsonObject.getString("$");

	        	        				if (spanValue.equals(startOfSpanPeriod))
	        	        				{
	        	        					int newlyFoundPpValue = Integer.parseInt(inmostJsonObject.getString("Pp"));
	        	        					
	        	        					System.out.println("newlyFoundPpValue in GetPpValues recorded as: " + newlyFoundPpValue);
	        	        					
	        	        					if (newlyFoundPpValue > highestPpValueFound)
	        	        					{
	        	        						highestPpValueFound = newlyFoundPpValue;
	        	        						System.out.println("highestPpValueFound in GetPpValues recorded as: " + highestPpValueFound);
	        	        					}
	        	        				}
	        	        			}
	        	        		}	        	        		
	        	        	}
	        	        }
	        	    }
	            }
	        }
        } 
        
        if (!foundDate) {
        	highestPpValueFound = 500;
        }
        
        return highestPpValueFound;
	}
}
