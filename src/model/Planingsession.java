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
@NamedQueries({
	@NamedQuery(name="Planingsession.findAll", query="SELECT p FROM Planingsession p"),
	@NamedQuery(name="Planingsession.get", query="SELECT p FROM Planingsession p WHERE p.id = :id")
})
public class Planingsession implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", nullable = false, updatable = false, insertable = false)	
	private int id;

	private String name;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}