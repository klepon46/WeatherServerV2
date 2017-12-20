package com.weather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.spi.DirStateFactory.Result;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.weather.sql.MySqlConnection;

public class StationFinder {

	private Map<String, Integer> stationMaps;
	
	public void findByTraversing(){
		
		
		
	}

	public void findNearestStationByContryCode(String countryOrState, String csvGeocode) {

		stationMaps = new HashMap<>();
		String sql = "select * from master_station where name like ? ";

		try {
			MySqlConnection mySqlDB = new MySqlConnection();
			Connection conn = mySqlDB.getConnection();

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, "%" + countryOrState + "%");

			ResultSet rs = ps.executeQuery();

			// int rowcount = 0;
			// if (rs.last()) {
			// rowcount = rs.getRow();
			// rs.beforeFirst();
			// }
			//
			// System.out.println(rowcount);

			while (rs.next()) {

				String stationId = rs.getString(1);
				double longitude = rs.getDouble(3);
				double latitude = rs.getDouble(4);

				String destGeocode = String.valueOf(longitude) + "," + String.valueOf(latitude);
				calculateNearestDistance(stationId, csvGeocode, destGeocode);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		Entry<String, Integer> min = null;
		for (Entry<String, Integer> entry : stationMaps.entrySet()) {
			if (min == null || min.getValue() > entry.getValue()) {
				min = entry;
			}
		}

		
		if(min != null){
			String nearestStation = min.getKey();
			updateCsvNearestStation(nearestStation, csvGeocode);
		}

	}

	private void calculateStationGeocode(String csvLong, String csvLat, double stationLong, double stationLat) {

		// some logic to find nearest station

	}

	public void reverseGeocode(String csvGeocode) {
		System.out.println("starting to reverse this : " + csvGeocode);

		String longitude = "38.0114393";
		String latitude = "-89.2361935";
		String longLat = longitude + "," + latitude;
		String apiKey = "AIzaSyAfuSTx2PnytL7QMk-6jMekHuFFfdRE6nA";
		StringBuilder sb = new StringBuilder();

		try {

			String url = "https://maps.googleapis.com/maps/api/geocode/json?" + "latlng=" + csvGeocode
					+ "&sensor=false&key=" + apiKey;

			DefaultHttpClient httpClient = new DefaultHttpClient();

			HttpGet getRequest = new HttpGet(url);
			getRequest.addHeader("accept", "application/json");

			HttpResponse response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}

		} catch (ClientProtocolException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();
		}

		String stateOrCountryCode = getCountryCode(sb.toString());
		System.out.println("reverse result = " + stateOrCountryCode);

		findNearestStationByContryCode(stateOrCountryCode, csvGeocode);

	}

	public String getCountryCode(String jsonToParse) {

		JSONObject root = new JSONObject(jsonToParse);
		JSONArray results = root.getJSONArray("results");

		JSONObject objInner = results.getJSONObject(1);
		JSONArray addressComponent = objInner.getJSONArray("address_components");

		JSONObject jCountry = addressComponent.getJSONObject(3);
		String countryCode = jCountry.getString("short_name");

		if (countryCode.equalsIgnoreCase("US")) {
			JSONObject jState = addressComponent.getJSONObject(2);
			System.out.println(jState.get("long_name"));

			return jState.getString("long_name");
		} else {
			System.out.println(countryCode);

			return countryCode;
		}

	}

	public void calculateNearestDistance(String stationId, String csvGeocode, String destGeocode) {

		System.out.println("starting to calculating distance : " + csvGeocode + " to " + destGeocode);

		String distanceMatrixApiKey = "AIzaSyBEn4emhP2aMKM-4ZAJXL-6FD19H8L09S4";
		String origin = "38.0114393,-89.2361935";
		String destination = "41.4255,-91.0094";
		StringBuilder sb = new StringBuilder();

		String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + csvGeocode + "&destinations="
				+ destGeocode + "&key=" + distanceMatrixApiKey;

		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(url);
			getRequest.addHeader("accept", "application/json");

			HttpResponse response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}

		} catch (ClientProtocolException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();
		}

		int distanceValue = parseJsonDistace(sb.toString());

		System.out.println("the distance is : " + distanceValue);

		stationMaps.put(stationId, distanceValue);

	}

	private int parseJsonDistace(String jsonToParse) {
		int distanceValue = 99999999;

		JSONObject root = new JSONObject(jsonToParse);
		JSONArray rows = root.getJSONArray("rows");

		if (rows.length() == 0 || rows == null) {
			return distanceValue;
		}

		JSONObject element = rows.getJSONObject(0);
		JSONArray elements = new JSONArray();

		Iterator x = element.keys();

		while (x.hasNext()) {
			String key = (String) x.next();
			elements.put(element.get(key));
		}

		String status = elements.getJSONArray(0).getJSONObject(0).getString("status");

		if (status.equalsIgnoreCase("ok")) {
			distanceValue = elements.getJSONArray(0).getJSONObject(0).getJSONObject("distance").getInt("value");
		}

		return distanceValue;
	}

	private void updateCsvNearestStation(String station, String csvGeocode) {
		// String longitude = "38.0114393";
		// String latitude = "-89.2361935";

		String[] split = csvGeocode.split(",");
		String longitude = split[0];
		String latitude = split[1];

		String sql = "update station_info set stationID = ? where csv_long = ? and csv_lat = ?";

		try {
			MySqlConnection mySqlDB = new MySqlConnection();
			Connection conn = mySqlDB.getConnection();

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, station);
			ps.setString(2, longitude);
			ps.setString(3, latitude);

			ps.execute();
			
			System.out.println("done get stationID for = " + csvGeocode);

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void getAllNullStationID() {

		String sql = "select * from station_info where stationID is null ";

		try {
			MySqlConnection mySqlDB = new MySqlConnection();
			Connection conn = mySqlDB.getConnection();

			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String csvLong = rs.getString(5);
				String csvLat = rs.getString(6);

				String longLat = csvLong + "," + csvLat;
				
				if(longLat.equalsIgnoreCase("0,0")){
					continue;
				}
				
				reverseGeocode(longLat);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
