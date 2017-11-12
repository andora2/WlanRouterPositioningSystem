package model;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;


/**
 * The persistent class for the PINGSPEED database table.
 * 
 */
@Entity
@NamedQuery(name="Pingspeed.findAll", query="SELECT p FROM Pingspeed p")
public class Pingspeed implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	private int responsemstime;

	private Timestamp savedtime;

	private int usedpacketbytesize;

	//bi-directional many-to-one association to Sensor
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name="PLANINGSESSIONID", referencedColumnName="PLANINGSESSIONID"),
		@JoinColumn(name="SENSORID", referencedColumnName="ID")
		})
	private Sensor sensor;

	public Pingspeed() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getResponsemstime() {
		return this.responsemstime;
	}

	public void setResponsemstime(int responsemstime) {
		this.responsemstime = responsemstime;
	}

	public Date getSavedtime() {
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