package app.daos;

import app.Main;
import app.entities.Collection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
import java.util.Set;

public class CollectionDAO implements IDAO<Collection> {
    EntityManagerFactory emf;
    public CollectionDAO(EntityManagerFactory _emf){
        this.emf = _emf;
    }
    @Override
    public void create(Collection collection) {
        // Implementation for creating a new collection in the database
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(collection);
            em.getTransaction().commit();
        }
    }

    @Override
    public Set<Collection> getAll() {
        // Implementation for retrieving all collections from the database
        try(EntityManager em = emf.createEntityManager()) {
            return new HashSet<>(em.createQuery("SELECT c FROM Collection c", Collection.class).getResultList());
        }
    }

    @Override
    public Collection getByID(int id) {
        // Implementation for retrieving a collection by its ID from the database
        try(EntityManager em = emf.createEntityManager()) {
            return em.find(Collection.class, id);
        }
    }

    @Override
    public Collection update(Collection collection) {
        // Implementation for updating an existing collection in the database
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(collection);
            em.getTransaction().commit();
            return collection;
        }
    }

    @Override
    public int delete(Collection collection) {
        // Implementation for deleting a collection from the database
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            if (collection != null) {
                em.remove(collection);
            }
            em.getTransaction().commit();
            return collection.getId();
        }
    }

    public Collection getByName(String name) {
        try(EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT c FROM Collection c WHERE c.name = :name", Collection.class)
                    .setParameter("name", name)
                    .getSingleResult();
        }
    }
}
