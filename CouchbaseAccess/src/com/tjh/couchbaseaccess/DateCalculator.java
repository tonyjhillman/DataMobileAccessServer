package com.tjh.couchbaseaccess;

import java.util.Calendar;
import java.util.Date;

public class DateCalculator {
	
	
	public String[] DaysOfTheWeekArray = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

	public DateCalculator() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Method to get value of current day and time. The offset represents the
	 * number of days from today. So, tomorrow has an offset specified by '1',
	 * the day after tomorrow '2', and so on.
	 * 
	 */
	public String todaysDatePlusOffset(int offset) {
		String fullDate = "";
		
		Calendar calendar = Calendar.getInstance();       
        Date date = new Date();
        calendar.setTime(date); 
        calendar.add(Calendar.DATE, offset);
        date = calendar.getTime();
        
        int myYear = calendar.get(Calendar.YEAR);
        int myMonth = calendar.get(Calendar.MONTH);
        myMonth++;
        int myDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        

        String formattedMonth = "";
        String formattedDay = "";

        if (myMonth < 10) {
            formattedMonth = "0" + Integer.toString(myMonth);
        }
        else {
            formattedMonth = Integer.toString(myMonth);
        }

        if (myDayOfMonth < 10) {
            formattedDay = "0" + Integer.toString(myDayOfMonth);
        } else {
            formattedDay = Integer.toString(myDayOfMonth);
        }

        fullDate = fullDate + Integer.toString(myYear) +
                "-" + formattedMonth + "-" + formattedDay + "Z";
		
		return fullDate;
	}
	
	public int offsetForFutureDayOfTheWeek(String todaysDayName, String futureDaysName){
		
		
		
		System.out.println("In offsetForFutureDayOfTheWeek");
		System.out.println("todaysDayName is " + todaysDayName);
		System.out.println("futureDaysName is " + futureDaysName);
        
        int todaysOffset = getArrayPositionOfDayName(todaysDayName);
        int futureDaysOffset = getArrayPositionOfDayName(futureDaysName);
        
        System.out.println("todaysOffset is: " + todaysOffset);
        System.out.println("futureDaysOffset is: " + futureDaysOffset);
        
        int stepsToFutureDay = 0;
        
        if (todaysOffset < futureDaysOffset){
        	stepsToFutureDay = futureDaysOffset - todaysOffset;	
        } else {
        	if (todaysOffset > futureDaysOffset){
        		stepsToFutureDay = (7 - todaysOffset) + futureDaysOffset;
        	} else {
        		if (todaysOffset == futureDaysOffset){
        			stepsToFutureDay = 0;
        		}
        	}
        }
        
        System.out.println("DateCalculator:offsetForFutureDayOfTheWeek: stepsToFutureDay is: " + stepsToFutureDay);

		return stepsToFutureDay;
	}
	
	private int getArrayPositionOfDayName(String dayName){
		
		System.out.println("In getArrayPositionOfDayName.");
		System.out.println("Submitted dayName is: " + dayName);
		
		int daysOffset = 0;
		
		for (int index = 0; index <= DaysOfTheWeekArray.length -1; index++){
        	
        	if (dayName.equals(DaysOfTheWeekArray[index]))
    		{
    			daysOffset = index + 1;
    			
    			break;
    		}
        }

		return daysOffset;
	}
	
	
	public String futureDayOfTheWeekAsString(int offset) {
		Calendar calendar = Calendar.getInstance();       
        Date date = new Date();
        calendar.setTime(date); 
        calendar.add(Calendar.DATE, offset); // So we can access the array,
        										 // which counts from zero
        int myDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        String myDayOfWeekAsString = DaysOfTheWeekArray[myDayOfWeek];
		
		return myDayOfWeekAsString;
	}
}
