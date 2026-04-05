package app.daos;

import app.Main;
import app.entities.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
import java.util.Set;

public class ItemDAO implements IDAO<Item> {
    EntityManagerFactory emf;
    public ItemDAO(EntityManagerFactory _emf){
        this.emf = _emf;
    }

    @Override
    public void create(Item item) {
        // Code to insert item into database
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(item);
            em.getTransaction().commit();
        }
    }

    @Override
    public Set<Item> getAll() {
        // Code to retrieve all items from database
        try(EntityManager em = emf.createEntityManager()) {
            return new HashSet<>(em.createQuery("SELECT i FROM Item i", Item.class).getResultList());
        }
    }
    @Override
    public Item getByID(int id) {
        // Code to find item by id from database
        try(EntityManager em = emf.createEntityManager()) {
            return em.find(Item.class, id);
        }
    }
    @Override
    public Item update(Item item) {
        // Code to update item in database
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(item);
            em.getTransaction().commit();
            return item;
        }
    }
    @Override
    public int delete(Item item) {
        // Code to delete item from database
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            if (item != null) {
                em.remove(item);
            }
            em.getTransaction().commit();
            return item.getId();
        }
    }
}
