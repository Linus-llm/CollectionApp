package unitTest;

import app.config.HibernateConfig;
import app.daos.CollectionDAO;
import app.daos.IDAO;
import app.daos.ItemDAO;
import app.daos.UserDAO;
import app.entities.*;
import app.security.ISecurityDAO;
import app.security.SecurityDao;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;

public class IDAOTest {
    private static EntityManagerFactory emf;
    private static IDAO<User> userDAO;
    private static IDAO<Collection> collectionDAO;
    private static IDAO<Item> itemDAO;
    private static SecurityDao securityDAO;
    @BeforeAll
    static void setUp() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        userDAO = new UserDAO(emf);
        collectionDAO = new CollectionDAO(emf);
        itemDAO = new ItemDAO(emf);
        securityDAO = new SecurityDao(emf);

        User u1 = securityDAO.createUser("Testusername", "1234556", "Test@email");
        User u2 = securityDAO.createUser("Testusername2", "1234552", "Test@email2");
        User u3 = securityDAO.createUser("Testusername3", "1234556", "Test@email3");

        // Collections (each belongs to exactly one user, per your current mapping)
        Collection c1 = new Collection(u1, "Games", "My video game library", LocalDateTime.now().minusDays(10));
        Collection c2 = new Collection(u1, "Consoles", "Hardware shelf", LocalDateTime.now().minusDays(8));
        Collection c3 = new Collection(u2, "Books", "Reading list", LocalDateTime.now().minusDays(5));

        collectionDAO.create(c1);
        collectionDAO.create(c2);
        collectionDAO.create(c3);

        // Items (each belongs to one Collection)
        Item i1 = new Item("The Witcher 3", "Complete Edition", LocalDateTime.now().minusDays(9),
                ItemType.VIDEO_GAME, 2015, ItemStatus.OWNED, ItemCondition.GOOD);
        i1.setCollection(c1);

        Item i2 = new Item("Nintendo Switch", "OLED model", LocalDateTime.now().minusDays(7),
                ItemType.CONSOLE, 2021, ItemStatus.OWNED, ItemCondition.GOOD);
        i2.setCollection(c2);

        Item i3 = new Item("Dune", "Frank Herbert", LocalDateTime.now().minusDays(4),
                ItemType.BOOK, 1965, ItemStatus.WISHLIST, ItemCondition.NEW);
        i3.setCollection(c3);

        Item i4 = new Item("Zelda: TOTK", "Physical copy", LocalDateTime.now().minusDays(3),
                ItemType.VIDEO_GAME, 2023, ItemStatus.BORROWED, ItemCondition.GOOD);
        i4.setCollection(c1);

        itemDAO.create(i1);
        itemDAO.create(i2);
        itemDAO.create(i3);
        itemDAO.create(i4);

    }

    @Test
    void createUser() {
        // This test relies on the setup method to create entities, so we just check if they exist

        User createdUser = securityDAO.createUser("testu1", "password", "testemail@dk.dk");
        assertNotNull(createdUser);
        assertEquals("testemail@dk.dk", createdUser.getEmail());
    }
    @Test
    void getAllUsers() {

        Set<User> testUsers = userDAO.getAll();
        assertNotNull(testUsers);
        assertTrue(testUsers.size() >= 2); // We created 3 users in the setup
    }



    @AfterAll
    static void tearDown() {
        emf.close();
    }
}
