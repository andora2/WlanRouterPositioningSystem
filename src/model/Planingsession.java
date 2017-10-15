package model;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the PLANINGSESSION database table.
 * 
 */
@Entity
@NamedQuery(name="Planingsession.findAll", query="SELECT p FROM Planingsession p")
public class Planingsession implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	private String description;

	private Timestamp starttime;

	//bi-directional many-to-one association to Pingspeed
	@OneToMany(mappedBy="planingsession")
	private List<Pingspeed> pingspeeds;

	//bi-directional many-to-one association to Groundplanimage
	@ManyToOne
	@JoinColumn(name="GROUNDPLANID", referencedColumnName="ID")
	private Groundplanimage groundplanimage;

	//bi-directional many-to-one association to Rssi
	@OneToMany(mappedBy="planingsession")
	private List<Rssi> rssis;

	//bi-directional many-to-one association to Sensor
	@OneToMany(mappedBy="planingsession")
	private List<Sensor> sensors;

	public Planingsession() {
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getStarttime() {
		return this.starttime;
	}

	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}

	public List<Pingspeed> getPingspeeds() {
		return this.pingspeeds;
	}

	public void setPingspeeds(List<Pingspeed> pingspeeds) {
		this.pingspeeds = pingspeeds;
	}

	public Pingspeed addPingspeed(Pingspeed pingspeed) {
		getPingspeeds().add(pingspeed);
		pingspeed.setPlaningsession(this);

		return pingspeed;
	}

	public Pingspeed removePingspeed(Pingspeed pingspeed) {
		getPingspeeds().remove(pingspeed);
		pingspeed.setPlaningsession(null);

		return pingspeed;
	}

	public Groundplanimage getGroundplanimage() {
		return this.groundplanimage;
	}

	public void setGroundplanimage(Groundplanimage groundplanimage) {
		this.groundplanimage = groundplanimage;
	}

	public List<Rssi> getRssis() {
		return this.rssis;
	}

	public void setRssis(List<Rssi> rssis) {
		this.rssis = rssis;
	}

	public Rssi addRssi(Rssi rssi) {
		getRssis().add(rssi);
		rssi.setPlaningsession(this);

		return rssi;
	}

	public Rssi removeRssi(Rssi rssi) {
		getRssis().remove(rssi);
		rssi.setPlaningsession(null);

		return rssi;
	}

	public List<Sensor> getSensors() {
		return this.sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}

	public Sensor addSensor(Sensor sensor) {
		getSensors().add(sensor);
		sensor.setPlaningsession(this);

		return sensor;
	}

	public Sensor removeSensor(Sensor sensor) {
		getSensors().remove(sensor);
		sensor.setPlaningsession(null);

		return sensor;
	}

}