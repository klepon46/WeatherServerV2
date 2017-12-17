package com.weather.dto;

public class StationData {

	private String id;
	private String name;
	private String minDate;
	private String maxDate;
	private double latitude;
	private double longitude;
	private String elevationUnit;
	private double elevation;
	private double dataCoverage;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMinDate() {
		return minDate;
	}
	public void setMinDate(String minDate) {
		this.minDate = minDate;
	}
	public String getMaxDate() {
		return maxDate;
	}
	public void setMaxDate(String maxDate) {
		this.maxDate = maxDate;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getElevationUnit() {
		return elevationUnit;
	}
	public void setElevationUnit(String elevationUnit) {
		this.elevationUnit = elevationUnit;
	}
	public double getElevation() {
		return elevation;
	}
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}
	public double getDataCoverage() {
		return dataCoverage;
	}
	public void setDataCoverage(double dataCoverage) {
		this.dataCoverage = dataCoverage;
	}
	
	
	
	
}
