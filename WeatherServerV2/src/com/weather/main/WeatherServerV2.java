package com.weather.main;

import java.time.LocalDate;

import com.weather.io.CsvImport;
import com.weather.util.LocationFetcher;
import com.weather.util.StationFetcher;
import com.weather.util.StationFinder;

public class WeatherServerV2 {

	public static void main(String[] args) {
		// CsvImport csv = new CsvImport();
		// csv.insertGeocodesIntoTable();

		// StationFetcher sf = new StationFetcher();
		// sf.downloadAllStation();
		
		StationFinder finder = new StationFinder();
		finder.findNearestStation("", "");

	}

}
