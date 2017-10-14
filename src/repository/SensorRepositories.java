package repository;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import model.Sensor;

public class SensorRepositories {
	@Inject
    private EntityManager entityManager;

	public List<Sensor> getAllSensor(){
		return entityManager.createNamedQuery("Sensor.findAll").getResultList();
	}
	
}