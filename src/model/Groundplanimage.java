package model;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the GROUNDPLANIMAGE database table.
 * 
 */
@Entity
@NamedQuery(name="Groundplanimage.findAll", query="SELECT g FROM Groundplanimage g")
public class Groundplanimage implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String filename;

	private String description;

	private Timestamp savedtime;

	//bi-directional many-to-one association to Planingsession
	@OneToMany(mappedBy="groundplanimage")
	private List<Planingsession> planingsessions;

	public Groundplanimage() {
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getSavedtime() {
		return this.savedtime;
	}

	public void setSavedtime(Timestamp savedtime) {
		this.savedtime = savedtime;
	}

	public List<Planingsession> getPlaningsessions() {
		return this.planingsessions;
	}

	public void setPlaningsessions(List<Planingsession> planingsessions) {
		this.planingsessions = planingsessions;
	}

	public Planingsession addPlaningsession(Planingsession planingsession) {
		getPlaningsessions().add(planingsession);
		planingsession.setGroundplanimage(this);

		return planingsession;
	}

	public Planingsession removePlaningsession(Planingsession planingsession) {
		getPlaningsessions().remove(planingsession);
		planingsession.setGroundplanimage(null);

		return planingsession;
	}

}