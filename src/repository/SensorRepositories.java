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
public class SensorRepositories {
	@Inject
    private EntityManager entityManager;

	public List<Sensor> getAllSensor(){
		return entityManager.createNamedQuery("Sensor.findAll").getResultList();
	}

	public void persist(Sensor sensor ){
		//entityManager.getTransaction().begin();
		entityManager.persist(sensor);
		entityManager.flush();
		//entityManager.getTransaction().commit();
	}
	
}
