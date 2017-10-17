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
	private int id;

	private String description;

	private Timestamp starttime;

	//bi-directional many-to-one association to Groundplanimage
	@ManyToOne
	@JoinColumn(name="GROUNDPLANID", referencedColumnName="ID")
	private Groundplanimage groundplanimage;

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

	public Groundplanimage getGroundplanimage() {
		return this.groundplanimage;
	}

	public void setGroundplanimage(Groundplanimage groundplanimage) {
		this.groundplanimage = groundplanimage;
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