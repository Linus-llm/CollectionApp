package app.daos;

import app.Main;
import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
import java.util.Set;

public class UserDAO implements IDAO<User> {
    EntityManagerFactory emf;
    public UserDAO(EntityManagerFactory _emf){
        this.emf = _emf;
    }
    @Override
    public void create(User user) {
        // Implementation for creating a user in the database
        try(EntityManager em = Main.emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        }
    }

    @Override
    public Set<User> getAll() {
        // Implementation for retrieving all users from the database
        try(EntityManager em = Main.emf.createEntityManager()) {
            return new HashSet<>(em.createQuery("SELECT u FROM User u", User.class).getResultList());
        }
    }

    @Override
    public User getByID(int id) {
        // Implementation for retrieving a user by ID from the database
        try(EntityManager em = Main.emf.createEntityManager()) {
            return em.find(User.class, id);
        }
    }

    @Override
    public User update(User user) {
        // Implementation for updating a user in the database
        try(EntityManager em = Main.emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public int delete(User user) {
        // Implementation for deleting a user from the database
        try(EntityManager em = Main.emf.createEntityManager()) {
            em.getTransaction().begin();
            if (user != null) {
                em.remove(user);
            }
            em.getTransaction().commit();
            return user.getId();
        }
    }
}
