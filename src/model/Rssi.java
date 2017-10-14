package model;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the RSSI database table.
 * 
 */
@Entity
@NamedQuery(name="Rssi.findAll", query="SELECT r FROM Rssi r")
public class Rssi implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String sensormacid;

	private int rssi;

	private Timestamp savedtime;

	//bi-directional many-to-one association to Sensor
	@ManyToOne
	@JoinColumn(name="SENSORMACID")
	private Sensor sensor;

	public Rssi() {
	}

	public String getSensormacid() {
		return this.sensormacid;
	}

	public void setSensormacid(String sensormacid) {
		this.sensormacid = sensormacid;
	}

	public int getRssi() {
		return this.rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public Timestamp getSavedtime() {
		return this.savedtime;
	}

	public void setSavedtime(Timestamp savedtime) {
		this.savedtime = savedtime;
	}

	public Sensor getSensor() {
		return this.sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

}