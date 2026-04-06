package unitTest;

import app.Controller.CollectionController;
import app.Controller.ItemController;
import app.Controller.UserController;
import app.config.HibernateConfig;
import app.daos.BookDAO;
import app.daos.CollectionDAO;
import app.daos.ItemDAO;
import app.daos.UserDAO;
import app.entities.Collection;
import app.entities.User;
import app.security.SecurityController;
import app.security.SecurityDao;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static io.restassured.RestAssured.given;

public class ControllerTest {
    static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    static ItemDAO itemDAO = new ItemDAO(emf);
    static UserDAO userDAO = new UserDAO(emf);
    static CollectionDAO collectionDAO = new CollectionDAO(emf);
    static BookDAO bookDAO = new BookDAO(emf);
    static ItemController itemController = new ItemController(itemDAO, bookDAO, collectionDAO);
    static CollectionController collectionController = new CollectionController(userDAO, collectionDAO);
    static UserController userController = new UserController(userDAO);
    static SecurityController securityController = new SecurityController();
    static SecurityDao securityDAO = new SecurityDao(emf);
    static Javalin app;

    @BeforeAll
    public static void setup() {
        app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(7070);
        app.get("/", ctx -> ctx.result("Server running"));
        //USER
        app.get("/api/user", userController::handleGetUsers);
        app.post("/api/auth/login", securityController::login);
        app.post("/api/auth/register", securityController::register);
        app.get("/api/user/{id}", userController::handleGetUserById);
        app.put("/api/user/{id}", userController::handleUpdateUser);
        app.delete("/api/user/{id}", userController::handleDeleteUser);

        //COLLECTION
        app.get("/api/collection/{userId}", collectionController::handleGetCollectionsForUser);
        app.get("/api/collection/{collectionId}", collectionController::handleGetCollectionById);
        app.post("/api/collection/{userId}", collectionController::handleCreateCollection);
        app.put("/api/collection/{collectionId}", collectionController::handleUpdateCollection);
        app.delete("/api/collection/{collectionId}", collectionController::handleDeleteCollection);

        //ITEM
        app.get("/api/item/{itemId}", itemController::handleGetItemById);
        app.get("/api/item/collection/{collectionId}", itemController::handleGetItemsForCollection);
        app.put("/api/item/{itemId}", itemController::handleUpdateItem);
        app.delete("/api/item/{itemId}", itemController::handleDeleteItem);

        //BOOKS (item)
        app.post("/api/item/{collectionId}", itemController::handleCreateBookInCollection);
    }

    @AfterAll
    public static void shutDown() {
        app.stop();
    }

    @Test
    public void createBookTest() {
        RestAssured.baseURI = "http://localhost:7070/api";

        securityDAO.createUser("lars", "password123", "test@test.dk");
        collectionDAO.create(new Collection(userDAO.getByID(1), "Lars' collection", "A collection of items", LocalDateTime.now()));
        Set<Collection> collections = new HashSet<>();
        User user = userDAO.getByID(1);
        collections.add(collectionDAO.getByName("Lars' collection", user));

        user.setCollections(collections);
        userDAO.update(user);

        given()
                .contentType("application/json")
                .body("""

                        {
          "title": "Sunrise paints the sky",
          "description": "Sunrise paints the sky, Gentle waves kiss sandy shores, Day awakens slow.",
          "authors": ["D. Tyrell", "R. Monke"],
                  "releaseYear": 2023,
                  "status": "OWNED",
                  "condition": "GOOD"
        }
        """)
                .when()
                .post("/item/1")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo("Sunrise paints the sky"));
    }
    @Test
    public void createUserTest( ){
        RestAssured.baseURI = "http://localhost:7070/api";

        given()
                .contentType("application/json")
                .body("""
        {
          "username": "John",
          "email": "test@dk",
          "password": "password"
         
  
    }"""
                ).when().post("/auth/register").then().log().all()
                .statusCode(201)
                .body("msg", equalTo("User registered"));

}

    @Test
    public void loginTest(){

        RestAssured.baseURI = "http://localhost:7070/api";

        given()
                .contentType("application/json")
                .body("""
        {
          "username": "John",
          "email": "test@dk",
          "password": "password"
         
  
    }"""
                ).when().post("/auth/register").then().log().all()
                .statusCode(201)
                .body("msg", equalTo("User registered"));

        //------------------------------------------

        given().contentType("application/json").body("""
        {
          "username": "John",
          "password": "password"
         
  
    }"""
        ).when().post("/auth/login").then().log().all().statusCode(200).body("username", equalTo("John"));
    }
}
