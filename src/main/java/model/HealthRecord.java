package model;

import java.sql.Date;

public class HealthRecord {
	private int id;
	private Date date;
	private int weight;
	private int temperature;
	private BloodPressure bloodPressure;
	private String notes;
	
	public HealthRecord() {
		
	}
	
	public HealthRecord(int id,Date date,int weight,int temperature,BloodPressure bloodPressure,String notes) {
		this.id = id;
		this.date = date;
		this.weight = temperature;
		this.bloodPressure = bloodPressure;
		this.notes = notes;
		
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public int getTemperature() {
		return temperature;
	}
	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}
	public BloodPressure getBloodPressure() {
		return bloodPressure;
	}
	public void setBloodPressure(BloodPressure bloodPressure) {
		this.bloodPressure = bloodPressure;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return String.valueOf(this.getId()+this.getTemperature());
	}
	
}
