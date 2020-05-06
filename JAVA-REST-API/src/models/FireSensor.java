package models;

public class FireSensor {
	private String fireSensorId;
	private String description;
	private Boolean active;
	private int currentSmokeLevel;
	private int currentCO2Level;
	private Boolean deleteReqeust;
	private int floorId;
	private int roomId;

	public FireSensor() {
		this.currentSmokeLevel = 0;
		this.currentCO2Level = 0;
		this.active = true;
		this.deleteReqeust = false;
	}

	public FireSensor(String fireSensorId, String description, int currentSmokeLevel, int currentCO2Level, int floorId,
			int roomId) {
		super();
		this.fireSensorId = fireSensorId;
		this.description = description;
		this.currentSmokeLevel = currentSmokeLevel;
		this.currentCO2Level = currentCO2Level;
		this.active = true;
		this.deleteReqeust = false;
		this.floorId = floorId;
		this.roomId = roomId;
	}

	public String getFireSensorId() {
		return fireSensorId;
	}

	public void setFireSensorId(String fireSensorId) {
		this.fireSensorId = fireSensorId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getCurrentSmokeLevel() {
		return currentSmokeLevel;
	}

	public void setCurrentSmokeLevel(int currentSmokeLevel) {
		this.currentSmokeLevel = currentSmokeLevel;
	}

	public int getCurrentCO2Level() {
		return currentCO2Level;
	}

	public void setCurrentCO2Level(int currentCO2Level) {
		this.currentCO2Level = currentCO2Level;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getDeleteReqeust() {
		return deleteReqeust;
	}

	public void setDeleteReqeust(Boolean deleteReqeust) {
		this.deleteReqeust = deleteReqeust;
	}

	public int getFloorId() {
		return floorId;
	}

	public void setFloorId(int floorId) {
		this.floorId = floorId;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

}