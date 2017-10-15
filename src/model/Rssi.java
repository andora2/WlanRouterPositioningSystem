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
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	private int rssi;

	private Timestamp savedtime;

	//bi-directional many-to-one association to Planingsession
	@ManyToOne
	@JoinColumn(name="PLANINGSESSIONID", referencedColumnName="ID")
	private Planingsession planingsession;

	//bi-directional many-to-one association to Sensor
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name="SENSORMACID", referencedColumnName="MACID"),
		@JoinColumn(name="PLANINGSESSIONID", referencedColumnName="PLANINGSESSIONID")
		})
	private Sensor sensor;

	public Rssi() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Planingsession getPlaningsession() {
		return this.planingsession;
	}

	public void setPlaningsession(Planingsession planingsession) {
		this.planingsession = planingsession;
	}

	public Sensor getSensor() {
		return this.sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

}