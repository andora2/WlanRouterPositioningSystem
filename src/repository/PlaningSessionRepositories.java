package repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import model.Groundplanimage;
import model.Planingsession;
import model.Sensor;

@Dependent
public class PlaningSessionRepositories {
	@Inject
    private EntityManager entityManager;

	public List<Planingsession> getAll(){
		return entityManager.createNamedQuery("Planingsession.findAll").getResultList();
	}

	public void persist(Planingsession planingsession ){
		//entityManager.getTransaction().begin();
		entityManager.persist(planingsession);
		entityManager.flush();
		//entityManager.getTransaction().commit();
	}

	public Planingsession get(int id) {
		return entityManager.createNamedQuery("Planingsession.get", Planingsession.class)
				.setParameter("id", id)
				.getSingleResult();
	}

	public Planingsession getLatest() {
		List<Planingsession> res = entityManager.createNamedQuery("Planingsession.findAll", Planingsession.class)
				.getResultList();
		return res.isEmpty()? null: res.get(0);
	}
	
}
