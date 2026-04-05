package unitTest;

import app.config.HibernateConfig;
import app.daos.CollectionDAO;
import app.daos.IDAO;
import app.daos.ItemDAO;
import app.daos.UserDAO;
import app.entities.*;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class IDAOTest {
    private static EntityManagerFactory emf;
    private static IDAO<User> userDAO;
    private static IDAO<Collection> collectionDAO;
    private static IDAO<Item> itemDAO;
    @BeforeAll
    static void setUp() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        userDAO = new UserDAO(emf);
        collectionDAO = new CollectionDAO(emf);
        itemDAO = new ItemDAO(emf);

        User u1 = new User("John", "Doe", "john@example.dk", "test", "+45302123042");
        User u2 = new User("Alice", "Johnson", "alice@example.dk", "test", "+45432123042");
        User u3 = new User("Bob", "Nielsen", "bob@example.dk", "test", "+45111111111");

        userDAO.create(u1);
        userDAO.create(u2);
        userDAO.create(u3);

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
    void create() {
        // This test relies on the setup method to create entities, so we just check if they exist
        User testu1 = new User("Test","Test", "test@example.dk", "test ", "+4500000000");
        userDAO.create(testu1);
        User retrieved = userDAO.getByID(testu1.getId());
        assert retrieved != null;
        assert retrieved.getEmail().equals("test@example.dk");
    }



    @AfterAll
    static void tearDown() {
        emf.close();
    }
}
