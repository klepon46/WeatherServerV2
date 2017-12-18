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

import com.weather.dto.StationHistory;
import com.weather.sql.MySqlConnection;

public class StationHistoryFetcher {

	private List<StationHistory> histories;

	public void getStationHistory(String stationID) {

		try {

//			String url = "https://www.ncdc.noaa.gov/cdo-web/api/v2/data?datasetid=PRECIP_15" + "&stationid=" + stationID
//					+ "&units=metric" + "&startdate=1997-01-01" + "&enddate=2017-12-31";
			
			String url = 
					"https://www.ncdc.noaa.gov/cdo-web/api/v2/data?datasetid=PRECIP_15&stationid=COOP:010063&units=metric&startdate=2010-05-01&enddate=2010-05-31";

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

	private void parseJson(String jsonToParse) {
		histories = new ArrayList<>();
		
		JSONObject root = new JSONObject(jsonToParse);
		JSONArray results = root.getJSONArray("results");
		
		for (int i = 0; i < results.length(); i++) {
			JSONObject obj = results.getJSONObject(i);
			
			StationHistory hist = new StationHistory();
			hist.setDate(obj.getString("date"));
			hist.setDataType(obj.getString("datatype"));
			hist.setStation(obj.getString("station"));
			hist.setAttribute(obj.getString("attributes"));
			hist.setValue(obj.getDouble("value"));
			histories.add(hist);
			
		}
		
		insertIntoWeatherHistory();
		
	}

	private void insertIntoWeatherHistory() {
		try {
			MySqlConnection mySqlDB = new MySqlConnection();
			Connection conn = mySqlDB.getConnection();

			for (StationHistory item : histories) {
				final PreparedStatement ps = conn
						.prepareStatement("INSERT INTO station_history (station, date, datatype, attributes, value ) "
								 + "VALUES (?,?,?,?,?);");
				ps.setString(1, item.getStation());
				ps.setString(2, item.getDate());
				ps.setString(3, item.getDataType());
				ps.setString(4, item.getAttribute());
				ps.setDouble(5, item.getValue());

				ps.execute();
				// System.out.println("inserted to database");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}