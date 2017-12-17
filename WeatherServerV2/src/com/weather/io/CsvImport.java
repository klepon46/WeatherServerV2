package com.weather.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.weather.sql.MySqlConnection;

public class CsvImport {

	private static final String TRACK_CSV = "csv/track.csv";
	private Set<String> geoCodes;

	public void prepareCsv() {

		geoCodes = new HashSet<>();

		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(TRACK_CSV));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] geoCode = line.split(csvSplitBy);
				if (geoCode[1] == null || geoCode[1].isEmpty())
					continue;

				geoCodes.add(geoCode[10]);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void insertGeocodesIntoTable() {

		prepareCsv();
		
		try {
			MySqlConnection mySqlDB = new MySqlConnection();
			Connection conn = mySqlDB.getConnection();

			String longitude = "";
			String latitude = "";

			for (String item : geoCodes) {
				String[] longLat = item.split("\\s+");

				if (longLat.length > 1) {
					longitude = longLat[0];
					latitude = longLat[1];
				}

				final PreparedStatement ps = conn.prepareStatement(
						"INSERT INTO station_info (station_long, station_lat) VALUES (?,?);");
				ps.setString(1, longitude);
				ps.setString(2, latitude);

				ps.execute();
				System.out.println("inserted to database");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
