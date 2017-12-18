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

	public void findNearestStation(String countryOrState) {

		String sql = "select * from master_station where name like ? ";

		try {
			MySqlConnection mySqlDB = new MySqlConnection();
			Connection conn = mySqlDB.getConnection();

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, "%" + countryOrState + "%");

			ResultSet rs = ps.executeQuery();

			int rowcount = 0;
			if (rs.last()) {
				rowcount = rs.getRow();
				rs.beforeFirst();
			}

			System.out.println(rowcount);

			while (rs.next()) {

				String stationId = rs.getString(1);
				double longitude = rs.getDouble(3);
				double latitude = rs.getDouble(4);

				String longLat = String.valueOf(longitude) + "," + String.valueOf(latitude);

				System.out.println(longLat);

				calculateNearestDistance(stationId, longLat);

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

		System.out.println(min.getKey()); // 0.1

		String nearestStation = min.getKey();
		updateCsvNearestStation(nearestStation);

	}

	private void calculateStationGeocode(String csvLong, String csvLat, double stationLong, double stationLat) {

		// some logic to find nearest station

	}

	public void reverseGeocode(String geocode) {
		System.out.println("starting to reverse");

		stationMaps = new HashMap<>();

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

		String stateOrCountryCode = getCountryCode(sb.toString());
		findNearestStation(stateOrCountryCode);

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

	public void calculateNearestDistance(String stationId, String destLongLat) {

		System.out.println("Start calculating...");

		String distanceMatrixApiKey = "AIzaSyBEn4emhP2aMKM-4ZAJXL-6FD19H8L09S4";
		String origin = "38.0114393,-89.2361935";
		String destination = "41.4255,-91.0094";
		StringBuilder sb = new StringBuilder();

		String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + origin + "&destinations="
				+ destLongLat + "&key=" + distanceMatrixApiKey;

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
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}

		} catch (ClientProtocolException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();
		}

		int distanceValue = parseJsonDistace(sb.toString());

		System.out.println(stationId + " = " + distanceValue);

		stationMaps.put(stationId, distanceValue);

	}

	private int parseJsonDistace(String jsonToParse) {
		JSONObject root = new JSONObject(jsonToParse);
		JSONArray rows = root.getJSONArray("rows");
		JSONObject element = rows.getJSONObject(0);
		JSONArray elements = new JSONArray();

		Iterator x = element.keys();

		while (x.hasNext()) {
			String key = (String) x.next();
			elements.put(element.get(key));
		}

		String status = elements.getJSONArray(0).getJSONObject(0).getString("status");
		int distanceValue = 99999999;

		if (status.equalsIgnoreCase("ok")) {
			distanceValue = elements.getJSONArray(0).getJSONObject(0).getJSONObject("distance").getInt("value");
		}

		return distanceValue;
	}

	private void updateCsvNearestStation(String station) {
		String longitude = "38.0114393";
		String latitude = "-89.2361935";
		String sql = "update station_info set stationID = ? where csv_long = ? and csv_lat = ?";

		try {
			MySqlConnection mySqlDB = new MySqlConnection();
			Connection conn = mySqlDB.getConnection();

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, station);
			ps.setString(2, longitude);
			ps.setString(3, latitude);

			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
