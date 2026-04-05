package app.security;

import app.config.HibernateConfig;
import app.entities.User;
import app.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class SecurityDao implements ISecurityDAO{
    EntityManagerFactory emf;
    public SecurityDao(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException {
        try(EntityManager em = emf.createEntityManager()){
            User user = em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst().orElseThrow(() -> new ValidationException("User not found"));;
            if(user.verifyPassword(password)){
                return user;
            } else {
                throw new ValidationException("User could not be validated");
            }
        }
    }

    @Override
    public User createUser(String username, String password, String email) {
        try(EntityManager em = emf.createEntityManager()){
            User user = new User(username, password, email);
            UserRole userRole = em.find(UserRole.class, "user");
            em.getTransaction().begin();
            if(userRole == null){
                userRole = new UserRole("user");
                em.persist(userRole);
            }
            user.addRole(userRole);
            em.persist(user);

            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public UserRole createRole(String role) {
        return null;
    }

    @Override
    public User addUserRole(String username, String role) {
        return null;
    }

    public static void main(String[] args) {
        ISecurityDAO dao = new SecurityDao(HibernateConfig.getEntityManagerFactory());
        User user = dao.createUser("newUser3", "newPassword", "email@email");
        try {
            User foundUser = dao.getVerifiedUser("newUser3", "newPassword");
            System.out.println(foundUser.getUsername());
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
