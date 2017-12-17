package com.weather.dto;

public class MasterLocation {

	private String id;
	private String minDate;
	private String maxDate;
	private String name;
	private double dataCoverage;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getDataCoverage() {
		return dataCoverage;
	}
	public void setDataCoverage(double dataCoverage) {
		this.dataCoverage = dataCoverage;
	}
	
}
