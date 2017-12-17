package com.weather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.spi.DirStateFactory.Result;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.weather.sql.MySqlConnection;

public class StationFinder {

	public void findNearestStation(String csvLong, String csvLat) {

		String sql = "select * from master_station where latitude like ? and longitude like ? ";

		try {
			MySqlConnection mySqlDB = new MySqlConnection();
			Connection conn = mySqlDB.getConnection();

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, "2%");
			ps.setString(2, "7%");

			ResultSet rs = ps.executeQuery();

			int rowcount = 0;
			if (rs.last()) {
				rowcount = rs.getRow();
				rs.beforeFirst();
			}

			
			System.out.println(rowcount);
			
			while (rs.next()) {
				double stationLong = rs.getDouble(3);
				double stationLat = rs.getDouble(4);
				
				calculateStationGeocode(csvLong, csvLat, stationLong, stationLat);
				
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	
	private void calculateStationGeocode(String csvLong, String csvLat ,
			double stationLong, double stationLat) {
		
		//some logic to find nearest station
		
		
	}

	public void reverseGeocode(String geocode) {
		String longitude = "38.0114393";
		String latitude = "-89.2361935";
		String longLat = longitude + "," + latitude;
		String apiKey = "AIzaSyAfuSTx2PnytL7QMk-6jMekHuFFfdRE6nA";
		StringBuilder sb = new StringBuilder();

		try {

			String url = "https://maps.googleapis.com/maps/api/geocode/json?" + "latlng=" + longLat
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
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}

		} catch (ClientProtocolException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();
		}

		getCountryCode(sb.toString());

	}

	public void getCountryCode(String jsonToParse) {

		JSONObject root = new JSONObject(jsonToParse);
		JSONArray results = root.getJSONArray("results");

		JSONObject objInner = results.getJSONObject(1);
		JSONArray addressComponent = objInner.getJSONArray("address_components");

		JSONObject jCountry = addressComponent.getJSONObject(3);
		String countryCode = jCountry.getString("short_name");

	}
}
