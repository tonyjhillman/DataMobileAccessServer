package com.tjh.couchbaseaccess;

import java.util.Calendar;
import java.util.Date;

public class DateCalculator {

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
	
	public int todaysDayOfTheWeek() {
		Calendar calendar = Calendar.getInstance();
		int myDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		
		return myDayOfWeek;
	}
}
