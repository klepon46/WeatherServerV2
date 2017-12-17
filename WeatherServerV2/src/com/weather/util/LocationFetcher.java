package com.weather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.weather.dto.MasterLocation;
import com.weather.sql.MySqlConnection;

public class LocationFetcher {

	
	private List<MasterLocation> locations = new ArrayList<>();

	private int offset;
	private int limit;

	public void downloadLocation(int offset, int limit) {
		
		try {

			String url = "https://www.ncdc.noaa.gov/cdo-web/api/v2/locations?limit=" + limit + "&offset=" + offset;

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(url);
			getRequest.addHeader("accept", "application/json");
			getRequest.addHeader("token", "swnIBYDzkGYKVzhEsLfPrWCoiSCtoBUn");

			HttpResponse response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			StringBuilder sb = new StringBuilder();
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}

			parseJson(sb.toString());

		} catch (ClientProtocolException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void downloadAllLocationFromWS() {
		
		limit = 1000;
		offset = 1;
		
		for (int cycle = 0; cycle < 39; cycle++) {
			
			
			
			if (cycle >= 1) {
				offset = cycle * limit + 1;
			}
			
			System.out.println(offset + " dan " + limit);

			downloadLocation(offset, limit);
		}

	}

	public void parseJson(String jsonToParse) {

		locations = new ArrayList<>();

		JSONObject root = new JSONObject(jsonToParse);
		JSONArray results = root.getJSONArray("results");

		for (int i = 0; i < results.length(); i++) {
			JSONObject obj = results.getJSONObject(i);

			MasterLocation loc = new MasterLocation();
			loc.setId(obj.getString("id"));
			loc.setName(obj.getString("name"));
			loc.setMinDate(obj.getString("mindate"));
			loc.setMaxDate(obj.getString("maxdate"));
			loc.setDataCoverage(obj.getDouble("datacoverage"));

			locations.add(loc);
		}
		
		
		insertIntoTableMasterLocation();
	}

	private void insertIntoTableMasterLocation() {

		try {
			MySqlConnection mySqlDB = new MySqlConnection();
			Connection conn = mySqlDB.getConnection();

			for (MasterLocation item : locations) {
				final PreparedStatement ps = conn.prepareStatement("INSERT INTO master_location "
						+ "(id, name, mindate, maxdate, datacoverage) VALUES (?,?,?,?,?);");
				ps.setString(1, item.getId());
				ps.setString(2, item.getName());
				ps.setString(3, item.getMinDate());
				ps.setString(4, item.getMaxDate());
				ps.setDouble(5, item.getDataCoverage());
				ps.execute();
				//System.out.println("inserted to database");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
