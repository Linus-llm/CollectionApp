package app.daos;

import app.daos.*;
import app.entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;

public class RetrieveDAO {
    private EntityManagerFactory emf;
    private IDAO<User> userDAO;
    private IDAO<Item> itemDAO;
    private IDAO<Collection> collectionDAO;

    public RetrieveDAO(EntityManagerFactory emf) {
        this.userDAO = new UserDAO(emf);
        this.itemDAO = new ItemDAO(emf);
        this.collectionDAO = new CollectionDAO(emf);
        this.emf = emf;
    }

    public Set<Item> getAllItemsInCollection(int collectionId) {
        try(EntityManager em = emf.createEntityManager()) {
            TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.collection.id = :collectionId", Item.class);
            query.setParameter("collectionId", collectionId);
            return new HashSet(query.getResultList());
        }
    }

    public Set<Collection> getAllCollectionsForUser(int userId) {
        try(EntityManager em = emf.createEntityManager()) {
            TypedQuery<Collection> query = em.createQuery("SELECT c FROM Collection c WHERE c.user.id = :userId", Collection.class);
            query.setParameter("userId", userId);
            return new HashSet(query.getResultList());
        }
    }

    public Set<User> getAllUsersWithItem(int itemId) {
        try(EntityManager em = emf.createEntityManager()) {
            TypedQuery<User> query = em.createQuery("SELECT DISTINCT c.user FROM Collection c JOIN c.items i WHERE i.id = :itemId", User.class);
            query.setParameter("itemId", itemId);
            return new HashSet(query.getResultList());
        }
    }

    public Set<User> getAllUsers() {
        return userDAO.getAll();
    }

}
