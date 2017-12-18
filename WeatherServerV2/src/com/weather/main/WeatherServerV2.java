package com.weather.main;

import com.weather.util.StationFinder;

public class WeatherServerV2 {

	public static void main(String[] args) {
		
		
		//uncomment to import CSV file into table
		// CsvImport csv = new CsvImport();
		// csv.insertGeocodesIntoTable();
		
		//uncomment to fetch all station from NOAA webService
		// StationFetcher sf = new StationFetcher();
		// sf.downloadAllStation();
		
		//find nearest station
		StationFinder finder = new StationFinder();
		finder.reverseGeocode("");
		
		System.out.println("done");

	}

}
