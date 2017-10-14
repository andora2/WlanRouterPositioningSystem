package model;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the PINGSPEED database table.
 * 
 */
@Entity
@NamedQuery(name="Pingspeed.findAll", query="SELECT p FROM Pingspeed p")
public class Pingspeed implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String sensormacid;

	private int responsemstime;

	private Timestamp savedtime;

	private int usedpacketbytesize;

	//bi-directional many-to-one association to Sensor
	@ManyToOne
	@JoinColumn(name="SENSORMACID")
	private Sensor sensor;

	public Pingspeed() {
	}

	public String getSensormacid() {
		return this.sensormacid;
	}

	public void setSensormacid(String sensormacid) {
		this.sensormacid = sensormacid;
	}

	public int getResponsemstime() {
		return this.responsemstime;
	}

	public void setResponsemstime(int responsemstime) {
		this.responsemstime = responsemstime;
	}

	public Timestamp getSavedtime() {
		return this.savedtime;
	}

	public void setSavedtime(Timestamp savedtime) {
		this.savedtime = savedtime;
	}

	public int getUsedpacketbytesize() {
		return this.usedpacketbytesize;
	}

	public void setUsedpacketbytesize(int usedpacketbytesize) {
		this.usedpacketbytesize = usedpacketbytesize;
	}

	public Sensor getSensor() {
		return this.sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

}