package repository;

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
		entityManager.persist(newGroundPlan);
		//entityManager.getTransaction().commit();
	}
	
	
}
