package model;

import java.io.Serializable;
import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonTypeIdResolver;

import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the GROUNDPLANIMAGE database table.
 * 
 */
@Entity
@NamedQueries(value = { 	
		@NamedQuery(name="Groundplanimage.findAll", query="SELECT g FROM Groundplanimage g ORDER BY g.id DESC"),
		@NamedQuery(name="Groundplanimage.get", query="SELECT g FROM Groundplanimage g WHERE g.id = :id")
})
public class Groundplanimage implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "ID", nullable = false, updatable = false, insertable = false)
	private int id;
	
	@Id
	private String filename;

	private String name;
	
	private String description;

	private Timestamp savedtime;

	//bi-directional many-to-one association to Planingsession
	@OneToMany(mappedBy="groundplanimage", fetch=FetchType.LAZY)
	private List<Planingsession> planingsessions;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
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

	@JsonIgnore
	/*public List<Planingsession> getPlaningsessions() {
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
	}*/

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}