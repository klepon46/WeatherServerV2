package com.weather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.weather.sql.MySqlConnection;

public class WeatherFetcher {

	private List<String> values;

	public void getStationWeatherData() {

		String sql = "select * from station_info where stationID is not null ";

		MySqlConnection mySqlDB = new MySqlConnection();
		Connection conn = null;
		try {
			conn = mySqlDB.getConnection();

			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String station[] = rs.getString(1).split(":");
				String stationId = station[1];
				
				getWeatherValueByStation(stationId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void getWeatherValueByStation(String stationId) {
		values = new ArrayList<>();

		String alamat = "https://www.ncdc.noaa.gov/access-data-service/api/v1/data?dataset=Daily-Summaries&dataTypes=LATITUDE,LONGITUDE,ELEVATION,NAME,"
				+ "PRCP,SNOW,SNWD,TMAX,TMIN,ACMC,ACMH,ACSC,ACSH,AWDR,AWND,DAEV,DAPR,DASF,DATN,DATX,DAWM,DWPR,EVAP,FMTM,FRGB,FRGT,FRTH,GAHT,MDEV,MDPR,"
				+ "MDSF,MDTN,MDTX,MDWM,MNPN,MXPN,PGTM,PSUN,TAVG,THIC,TOBS,TSUN&stations="+ stationId+ "&startDate=2010-01-01&endDate=2016-01-02";

		try {

			URL url = new URL(alamat);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("token", "swnIBYDzkGYKVzhEsLfPrWCoiSCtoBUn");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				values.add(output);
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

		for (String item : values) {

			try {
				CSVReader reader = new CSVReader(new StringReader(item));
				List<String[]> lists = reader.readAll();
				for (String[] kuda : lists) {
					csvReader(kuda);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public void csvReader(String[] weather) {

		String query = "insert into weather_data (STATION,DATE,ACMC,ACMH,ACSC,ACSH,AWDR,AWND,DAEV,DAPR,DASF,"
				+ "DATN,DATX,DAWM,DWPR,ELEVATION,EVAP,FMTM,FRGB,FRGT,FRTH,GAHT,LATITUDE,LONGITUDE,MDEV,MDPR,"
				+ "MDSF,MDTN,MDTX,MDWM,MNPN,MXPN,NAME,PGTM,PRCP,PSUN,SNOW,SNWD,TAVG,THIC,TMAX,TMIN,TOBS,TSUN) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

		MySqlConnection mySqlDB = new MySqlConnection();
		Connection conn = null;

		try {
			conn = mySqlDB.getConnection();
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, weather[0]);
			ps.setString(2, weather[1]);
			ps.setString(3, weather[2]);
			ps.setString(4, weather[3]);
			ps.setString(5, weather[4]);
			ps.setString(6, weather[5]);
			ps.setString(7, weather[6]);
			ps.setString(8, weather[7]);
			ps.setString(9, weather[8]);
			ps.setString(10, weather[9]);
			ps.setString(11, weather[10]);
			ps.setString(12, weather[11]);
			ps.setString(13, weather[12]);
			ps.setString(14, weather[13]);
			ps.setString(15, weather[14]);
			ps.setString(16, weather[15]);
			ps.setString(17, weather[16]);
			ps.setString(18, weather[17]);
			ps.setString(19, weather[18]);
			ps.setString(20, weather[19]);
			ps.setString(21, weather[20]);
			ps.setString(22, weather[21]);
			ps.setString(23, weather[22]);
			ps.setString(24, weather[23]);
			ps.setString(25, weather[24]);
			ps.setString(26, weather[25]);
			ps.setString(27, weather[26]);
			ps.setString(28, weather[27]);
			ps.setString(29, weather[28]);
			ps.setString(30, weather[29]);
			ps.setString(31, weather[30]);
			ps.setString(32, weather[31]);
			ps.setString(33, weather[32]);
			ps.setString(34, weather[33]);
			ps.setString(35, weather[34]);
			ps.setString(36, weather[35]);
			ps.setString(37, weather[36]);
			ps.setString(38, weather[37]);
			ps.setString(39, weather[38]);
			ps.setString(40, weather[39]);
			ps.setString(41, weather[40]);
			ps.setString(42, weather[41]);
			ps.setString(43, weather[42]);
			ps.setString(44, weather[43]);

			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
