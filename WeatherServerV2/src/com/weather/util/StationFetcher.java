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

import com.weather.dto.StationData;
import com.weather.sql.MySqlConnection;

public class StationFetcher {

	private int offset;
	private int limit;

	private List<StationData> stations;

	
	public void downloadAllStation() {
		limit = 1000;
		offset = 1;
		
		for (int cycle = 0; cycle < 83; cycle++) {
			if (cycle >= 1) {
				offset = cycle * limit + 1;
			}
			
			System.out.println(offset + " dan " + limit);

			downloadStation(offset, limit);
		}
	}
	
	public void downloadStation(int offset, int limit) {

		try {

			String url = 
					"https://www.ncdc.noaa.gov/cdo-web/api/v2/stations?"
							+ "startdate=1997-01-01&offset=" +offset+ "&limit="+limit;

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

	public void parseJson(String jsonToParse) {

		stations = new ArrayList<>();

		JSONObject root = new JSONObject(jsonToParse);
		JSONArray results = root.getJSONArray("results");

		for (int i = 0; i < results.length(); i++) {
			JSONObject obj = results.getJSONObject(i);

			StationData station = new StationData();
			station.setId(obj.getString("id"));
			station.setName(obj.getString("name"));
			station.setLatitude(obj.getDouble("latitude"));
			station.setLongitude(obj.getDouble("longitude"));
			station.setMinDate(obj.getString("mindate"));
			station.setMaxDate(obj.getString("maxdate"));
			station.setElevationUnit("-");
			station.setElevation(0d);
			station.setDataCoverage(obj.getDouble("datacoverage"));

			stations.add(station);
		}


		insertIntoTableMasterStation();
	}

	public void insertIntoTableMasterStation() {
		try {
			MySqlConnection mySqlDB = new MySqlConnection();
			Connection conn = mySqlDB.getConnection();

			for (StationData item : stations) {
				final PreparedStatement ps = conn.prepareStatement("INSERT INTO master_station "
						+ "(id, name, latitude, longitude, mindate, "
						+ "maxdate, elevationUnit, datacoverage, elevation) "
						+ "VALUES (?,?,?,?,?,?,?,?,?);");
				ps.setString(1, item.getId());
				ps.setString(2, item.getName());
				ps.setDouble(3, item.getLatitude());
				ps.setDouble(4, item.getLongitude());
				ps.setString(5, item.getMinDate());
				ps.setString(6, item.getMaxDate());
				ps.setString(7, item.getElevationUnit());
				ps.setDouble(8, item.getDataCoverage());
				ps.setDouble(9, item.getElevation());
				ps.execute();
				//System.out.println("inserted to database");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	

}
