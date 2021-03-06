package com.weather.sql;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySqlConnection {

	private static final String serverName = "localhost";
	private static final String portNumber = "3306";
	private static final String dataBase = "weather";
	private static final String dbms = "mysql";
	// private static final String localPropertiesFile = "conf/db.properties";
	private static String password;
	private static String userName;

	public MySqlConnection() {
		if (MySqlConnection.password == null) {
			Properties properties = new Properties();
			BufferedInputStream stream;
			// try {
			// stream = new BufferedInputStream(new
			// FileInputStream(localPropertiesFile));
			// try {
			// properties.load(stream);
			// stream.close();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			// } catch (FileNotFoundException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			// String user = properties.getProperty("db.user");
			// String password = properties.getProperty("db.password");

			String user = "root";
			//String password = "RDikGbg1njEtokc";
			 String password = "ahmids";

			MySqlConnection.userName = user;
			MySqlConnection.password = password;

		}
	}

	public Connection getConnection() throws SQLException {

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", MySqlConnection.userName);
		connectionProps.put("password", MySqlConnection.password);

		conn = DriverManager.getConnection("jdbc:" + MySqlConnection.dbms + "://" + MySqlConnection.serverName + ":"
				+ MySqlConnection.portNumber + "/" + MySqlConnection.dataBase, connectionProps);

		System.out.println("Connected to database");
		return conn;
	}
}
