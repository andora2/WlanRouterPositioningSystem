package repository;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Dependent
public class EntityManagerProducer {

    @PersistenceContext    
    private EntityManager em;

    @Produces
    @RequestScoped
    public EntityManager getEntityManager() {
        return em;
    }
}