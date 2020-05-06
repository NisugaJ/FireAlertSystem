package models;

public class Alert {
	private String alertId;
	private String sensorId;
	private String time;
	private int co2Level;
	private int smokeLevel;
	private String status;
	 
	public Alert() {}
	
	public Alert(String alertId, String sensorId, String time, int co2Level, int smokeLevel, String status) {
		super();
		this.alertId = alertId;
		this.sensorId = sensorId;
		this.time = time;
		this.co2Level = co2Level;
		this.smokeLevel = smokeLevel;
		this.status = status;
	}

	public String getAlertId() {
		return alertId;
	}

	public void setAlertId(String alertId) {
		this.alertId = alertId;
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getCo2Level() {
		return co2Level;
	}

	public void setCo2Level(int co2Level) {
		this.co2Level = co2Level;
	}

	public int getSmokeLevel() {
		return smokeLevel;
	}

	public void setSmokeLevel(int smokeLevel) {
		this.smokeLevel = smokeLevel;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
	
	
}