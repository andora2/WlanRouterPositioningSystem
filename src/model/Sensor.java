package model;

import java.io.Serializable;
import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.util.List;


/**
 * The persistent class for the SENSOR database table.
 * 
 */
@Entity
@NamedQueries({
	@NamedQuery(name="Sensor.findAll", query="SELECT s FROM Sensor s"),
	@NamedQuery(name="Sensor.getForSession", query="SELECT s FROM Sensor s WHERE s.planingsession.id = :sessionId"),	
	@NamedQuery(name="Sensor.get", query="SELECT s FROM Sensor s WHERE s.id = :id"),	
	@NamedQuery(name="Sensor.getByNameInSession", query="SELECT s FROM Sensor s WHERE s.planingsession.id = :sessionId AND "
																			+ "	 s.name = :name"),	
	@NamedQuery(name="Sensor.getByIpAddressInSession", query="SELECT s FROM Sensor s WHERE s.planingsession.id = :sessionId AND "
																					+ "    s.ipaddress = :ipAddress"),	
	
})
public class Sensor implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	private String name;

	private String ipaddress;

	private String hostname;
	
	private double geolat;

	private double geolon;

	private int mapposx;

	private int mapposy;

	private int mapposz;


	//bi-directional many-to-one association to Planingsession
	@ManyToOne
	@JoinColumn(name="PLANINGSESSIONID", referencedColumnName="ID")
	@JsonIgnore
	private Planingsession planingsession;

	//bi-directional many-to-one association to Rssi
	@OneToMany(mappedBy="sensor")
	private List<Rssi> rssis;

	//bi-directional many-to-one association to Pingspeed
	@OneToMany(mappedBy="sensor")
	private List<Pingspeed> pingspeeds;

	public Sensor() {
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

	public String getIpaddress() {
		return this.ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public int getId() {
		return this.id;
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

	/*public Planingsession getPlaningsession() { 
		return this.planingsession;
	}*/

	@JsonIgnore
	public void setPlaningsession(Planingsession planingsession) {
		this.planingsession = planingsession;
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

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
}