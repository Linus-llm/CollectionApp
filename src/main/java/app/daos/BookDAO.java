package app.daos;

import app.Main;
import app.entities.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Set;

public class BookDAO implements IDAO<Book> {

    EntityManagerFactory emf;
    public BookDAO(EntityManagerFactory _emf){
        this.emf = _emf;
    }
    @Override
    public void create(Book book) {
        // Implementation for creating a new book record in the database
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(book);
            em.getTransaction().commit();
        }
    }

    @Override
    public Set<Book> getAll() {
        // Implementation for retrieving all book records from the database
        return null;
    }

    @Override
    public Book getByID(int id) {
        // Implementation for retrieving a book record by its ID from the database
        return null;
    }

    @Override
    public Book update(Book book) {
        // Implementation for updating an existing book record in the database
        return null;
    }

    @Override
    public int delete(Book book) {
        // Implementation for deleting a book record from the database
        return 0;
    }
}
