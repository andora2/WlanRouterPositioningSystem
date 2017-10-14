package model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the SENSOR database table.
 * 
 */
@Entity
@NamedQuery(name="Sensor.findAll", query="SELECT s FROM Sensor s")
public class Sensor implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String macid;

	private double geolat;

	private double geolon;

	private String lastipaddress;

	private String locationname;

	private int mapposx;

	private int mapposy;

	private int mapposz;

	private String name;

	//bi-directional many-to-one association to Rssi
	@OneToMany(mappedBy="sensor")
	private List<Rssi> rssis;

	//bi-directional many-to-one association to Pingspeed
	@OneToMany(mappedBy="sensor")
	private List<Pingspeed> pingspeeds;

	public Sensor() {
	}

	public String getMacid() {
		return this.macid;
	}

	public void setMacid(String macid) {
		this.macid = macid;
	}

	public double getGeolat() {
		return this.geolat;
	}

	public void setGeolat(double geolat) {
		this.geolat = geolat;
	}

	public double getGeolon() {
		return this.geolon;
	}

	public void setGeolon(double geolon) {
		this.geolon = geolon;
	}

	public String getLastipaddress() {
		return this.lastipaddress;
	}

	public void setLastipaddress(String lastipaddress) {
		this.lastipaddress = lastipaddress;
	}

	public String getLocationname() {
		return this.locationname;
	}

	public void setLocationname(String locationname) {
		this.locationname = locationname;
	}

	public int getMapposx() {
		return this.mapposx;
	}

	public void setMapposx(int mapposx) {
		this.mapposx = mapposx;
	}

	public int getMapposy() {
		return this.mapposy;
	}

	public void setMapposy(int mapposy) {
		this.mapposy = mapposy;
	}

	public int getMapposz() {
		return this.mapposz;
	}

	public void setMapposz(int mapposz) {
		this.mapposz = mapposz;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Rssi> getRssis() {
		return this.rssis;
	}

	public void setRssis(List<Rssi> rssis) {
		this.rssis = rssis;
	}

	public Rssi addRssi(Rssi rssi) {
		getRssis().add(rssi);
		rssi.setSensor(this);

		return rssi;
	}

	public Rssi removeRssi(Rssi rssi) {
		getRssis().remove(rssi);
		rssi.setSensor(null);

		return rssi;
	}

	public List<Pingspeed> getPingspeeds() {
		return this.pingspeeds;
	}

	public void setPingspeeds(List<Pingspeed> pingspeeds) {
		this.pingspeeds = pingspeeds;
	}

	public Pingspeed addPingspeed(Pingspeed pingspeed) {
		getPingspeeds().add(pingspeed);
		pingspeed.setSensor(this);

		return pingspeed;
	}

	public Pingspeed removePingspeed(Pingspeed pingspeed) {
		getPingspeeds().remove(pingspeed);
		pingspeed.setSensor(null);

		return pingspeed;
	}

}