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

import static org.hamcrest.CoreMatchers.hasItems;
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
        }).start(7070).beforeMatched(securityController::authenticate)
                .beforeMatched(securityController::authorize);;
        app.get("/", ctx -> ctx.result("Server running"));
        //USER
        app.get("/api/user", userController::handleGetUsers);
        app.post("/api/auth/login", securityController::login);
        app.post("/api/auth/register", securityController::register);
        app.get("/api/user/{id}", userController::handleGetUserById);
        app.put("/api/user/{id}", userController::handleUpdateUser);
        app.delete("/api/user/{id}", userController::handleDeleteUser);

        //COLLECTION
        app.get("/api/user/{userId}/collection", collectionController::handleGetCollectionsForUser);
        app.post("/api/user/{userId}/collection", collectionController::handleCreateCollection);
        app.get("/api/collection/{collectionId}", collectionController::handleGetCollectionById);
        app.put("/api/collection/{collectionId}", collectionController::handleUpdateCollection);
        app.delete("/api/collection/{collectionId}", collectionController::handleDeleteCollection);

        //ITEM
        app.get("/api/item/{itemId}", itemController::handleGetItemById);
        app.get("/api/collection/{collectionId}/item", itemController::handleGetItemsForCollection);
        app.put("/api/item/{itemId}", itemController::handleUpdateItem);
        app.delete("/api/item/{itemId}", itemController::handleDeleteItem);

        //BOOKS (item)
        app.post("/api/collection/{collectionId}/item", itemController::handleCreateBookInCollection);
    }

    @AfterAll
    public static void shutDown() {
        app.stop();
    }


    //------------------------------------------
    // ItemController tests
    //------------------------------------------

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
                .post("/collection/1/item")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo("Sunrise paints the sky"));
    }

    @Test
    public void getItemsForCollectionTest(){
        RestAssured.baseURI = "http://localhost:7070/api";

        //1. Register user John and extract userId
        int userId =
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
                .body("msg", equalTo("User registered")).extract().path("id");

        //2. Login and extract token

        String token = given().contentType("application/json").body("""
        {
          "username": "John",
          "password": "password"
         
  
    }"""
        ).when().post("/auth/login").then().log().all().statusCode(200).body("username", equalTo("John")).extract().path("token");

        //3. Create collection for user John with token and extract collectionId
        int collectionId = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
            {
              "name": "Books",
              "description": "My book collection"
            }
        """)
                .when()
                .post("/user/"+userId+"/collection")
                .then()
                .statusCode(201).extract().path("id");

        // 4. Create first book in collection 1
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
            {
              "title": "Clean Code",
              "description": "Book about clean code",
              "authors": ["Robert C. Martin"],
              "releaseYear": 2008,
              "status": "OWNED",
              "condition": "GOOD"
            }
        """)
                .when()
                .post("/collection/"+collectionId+"/item")
                .then()
                .statusCode(201);

        // 5. Create second book in collection 1
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
            {
              "title": "Harry Potter",
              "description": "Wizard book",
              "authors": ["J. K. Rowling"],
              "releaseYear": 1997,
              "status": "OWNED",
              "condition": "GOOD"
            }
        """)
                .when()
                .post("/collection/"+collectionId+"/item")
                .then()
                .statusCode(201);

        //6.  test if items are returned when getting items for collection 1
        given()
                .when()
                .get("/collection/"+collectionId+"/item")
                .then()
                .statusCode(200)
                .body("name", hasItems("Clean Code", "Harry Potter"));
    }
    @Test
    public void getItemsForNonExistentCollectionTest() {
        RestAssured.baseURI = "http://localhost:7070/api";

        // register
        given()
                .contentType("application/json")
                .body("""
            {
              "username": "John",
              "email": "test@dk",
              "password": "password"
            }
            """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201);

        // login
        String token = given()
                .contentType("application/json")
                .body("""
            {
              "username": "John",
              "password": "password"
            }
            """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        // non existent collection
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/collection/999")
                .then()
                .statusCode(404)
                .body(equalTo("Collection not found"));
    }

    //------------------------------------------
    // SecurityController tests
    //------------------------------------------


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

    //------------------------------------------
    // CollectionController tests
    //------------------------------------------

    @Test
    public void createCollectionTest() {
        RestAssured.baseURI = "http://localhost:7070/api";

        // 1. register user John and extract userId

        int userId = given()
                .contentType("application/json")
                .body("""

                        {
              "username": "John",
              "email": "test@dk",
              "password": "password"
            }
            """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201).extract().path("id");

        // 2. login and extract token
        String token = given()
                .contentType("application/json")
                .body("""
            {
              "username": "John",
              "password": "password"
            }
            """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");


        // 3. create collection for user John with token

      given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("""
            {
              "name": "Books",
              "description": "My book collection"
            }
        """)
            .when()
            .post("/user/"+userId+"/collection")
            .then().log().all()
            .statusCode(201).body("name", equalTo("Books"));
    }

    @Test
    public void getAllCollectionsFromUserTest() {
        RestAssured.baseURI = "http://localhost:7070/api";

        // 1. register user John and extract userId

        int userId = given()
                .contentType("application/json")
                .body("""

                        {
              "username": "John",
              "email": "test@dk",
              "password": "password"
            }
            """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201).extract().path("id");

        // 2. login and extract token
        String token = given()
                .contentType("application/json")
                .body("""
            {
              "username": "John",
              "password": "password"
            }
            """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");


        // 3. create collections for user John with token

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
            {
              "name": "Books",
              "description": "My book collection"
            }
        """)
                .when()
                .post("/user/"+userId+"/collection")
                .then().log().all()
                .statusCode(201);

    given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
            {
              "name": "Games",
              "description": "My game collection"
            }
        """)
                .when()
                .post("/user/"+userId+"/collection")
                .then().log().all()
                .statusCode(201);

    given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .when()
            .get("/user/"+userId+"/collection")
            .then().log().all()
            .statusCode(200)
            .body("name", hasItems("Books","Games"));

}

    @Test
    public void getCollectionByIdWithNonExistentCollection(){
        RestAssured.baseURI = "http://localhost:7070/api";

        given()
                .contentType("application/json")
                .body("""
            {
              "username": "John",
              "email": "test@dk",
              "password": "password"
            }
            """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201);

        String token = given()
                .contentType("application/json")
                .body("""
            {
              "username": "John",
              "password": "password"
            }
            """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .when()
                .get("/collection/999")
                .then()
                .statusCode(404)
                .body(equalTo("Collection not found"));
    }




}
