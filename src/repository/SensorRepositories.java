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
		return entityManager.createNamedQuery("Sensor.findAll", Sensor.class).getResultList();
	}

	public List<Sensor> getForSession(int i_nSessionid){
		return entityManager.createNamedQuery("Sensor.getForSession", Sensor.class)
				.setParameter("sessionId", i_nSessionid)
				.getResultList();
	}

	public void persist(Sensor sensor ){
		//entityManager.getTransaction().begin();
		entityManager.persist(sensor);
		entityManager.flush();
		//entityManager.getTransaction().commit();
	}

	public void detach(Sensor sensor) {
		entityManager.detach(sensor);
	}
	
	public void delete(Sensor sensor ){
		//entityManager.getTransaction().begin();
		entityManager.remove(sensor);
		entityManager.flush();
		//entityManager.getTransaction().commit();
	}

	public Sensor getSensor(int i_nSensorId) {
		return entityManager.createNamedQuery("Sensor.get",Sensor.class)
				.setParameter("id", i_nSensorId)
				.getSingleResult();
	}

	public void update(Sensor i_sensor) {
		entityManager.merge(i_sensor);
		entityManager.persist(i_sensor);
		entityManager.flush();
	}
}
