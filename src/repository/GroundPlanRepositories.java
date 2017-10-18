package repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import model.Groundplanimage;
import model.Sensor;

@Dependent
public class GroundPlanRepositories {
	@Inject
    private EntityManager entityManager;

	public List<Groundplanimage> getAllGroundPlans(){
		return entityManager.createNamedQuery("Groundplanimage.findAll").getResultList();
	}
	
	public void persist(Groundplanimage newGroundPlan ){
		//entityManager.getTransaction().begin();
		newGroundPlan.setSavedtime(new Timestamp((new Date()).getTime()) );
		entityManager.persist(newGroundPlan);
		entityManager.flush();
		//entityManager.getTransaction().commit();
	}
	
	
}
