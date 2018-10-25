package com.tjh.couchbaseaccess;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MsgSender 
{
	private final String USER_AGENT = "Mozilla/5.0";

	public MsgSender() 
	{
		
	}
	
	public String getMsg() 
	{
		try 
		{
			String url = "http://datapoint.metoffice.gov.uk/public/data/val/wxfcs/all/json/3830?res=3hourly&key=0b1faa31-cdb3-48fd-9eae-df050b852c48";
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	
			con.setRequestMethod("GET");
	
			con.setRequestProperty("User-Agent", USER_AGENT);
	
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
	
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) 
			{
				response.append(inputLine);
			}
			in.close();
	
			System.out.println(response.toString());
			
			return response.toString();
		}
		catch  (Exception e) 
		{
			return "";
		}
	}
}
	
