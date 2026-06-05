package app;
import app.Controller.CollectionController;
import app.Controller.ItemController;
import app.Controller.UserController;
import app.config.HibernateConfig;
import app.daos.BookDAO;
import app.daos.CollectionDAO;
import app.daos.ItemDAO;
import app.daos.UserDAO;
import app.dtos.ApiResponseDTO;
import app.entities.Item;
import app.entities.User;
import app.exceptions.ApiException;
import app.exceptions.ValidationException;
import app.security.Role;
import app.security.SecurityController;
import app.services.BookService;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;

public class Main {
    public static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static SecurityController securityController = new SecurityController();
    public static void main(String[] args) {

        emf.createEntityManager();
        Javalin app = Javalin.create(config -> {
                    config.showJavalinBanner = false;

                    config.bundledPlugins.enableCors(cors -> {
                        cors.addRule(it -> {
                            it.allowHost("http://localhost:5173");
                            it.allowHost("http://localhost:5175");
                            it.allowHost("https://collector.viskode.dk");
                        });
                    });
                })
                .start(7070)
                .beforeMatched(securityController::authenticate)
                .beforeMatched(securityController::authorize);


        app.exception(ApiException.class, (e, ctx) -> {
            ctx.status(e.getCode()).json(new ApiResponseDTO(e.getCode(), e.getMessage()));
        });
        app.exception(ValidationException.class, (e, ctx) -> {
            ctx.status(400).json(new ApiResponseDTO(400, e.getMessage()));
        });
        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).json(new ApiResponseDTO(500, e.getClass().getSimpleName() + ": " + e.getMessage()));
        });



        ItemDAO itemDAO = new ItemDAO(emf);
        BookDAO bookDAO = new BookDAO(emf);
        CollectionDAO collectionDAO = new CollectionDAO(emf);
        UserDAO userDAO = new UserDAO(emf);

        ItemController itemController = new ItemController(itemDAO, bookDAO, collectionDAO, userDAO);
        CollectionController collectionController = new CollectionController(userDAO, collectionDAO);
        UserController userController = new UserController(userDAO);

        app.get("/", ctx -> ctx.result("Server running"));

        //AUTH
        app.post("/api/auth/login", securityController::login);
        app.post("/api/auth/register", securityController::register);
        app.post("/api/auth/register/admin", securityController::adminRegister, Role.ADMIN);
        //USER
        app.get("/api/user", userController::handleGetUsers, Role.USER, Role.ADMIN);
        app.get("/api/user/{id}", userController::handleGetUserById, Role.USER, Role.ADMIN);
        app.put("/api/user/{id}", userController::handleUpdateUser, Role.USER, Role.ADMIN);
        app.delete("/api/user/{id}", userController::handleDeleteUser, Role.USER, Role.ADMIN);

        //ADMIN
        app.get("/api/admin/user", userController::handleGetUsers, Role.ADMIN);
        app.get("/api/admin/user/search", userController::handleGetUserByUsername, Role.ADMIN);
        app.get("/api/admin/user/{id}", userController::handleGetUserByIdAdmin, Role.ADMIN);
        app.put("/api/admin/user/{id}", userController::handleAdminUpdateUser, Role.ADMIN);
        app.delete("/api/admin/user/{id}", userController::handleAdminDeleteUser, Role.ADMIN);

        //COLLECTION
        app.get("/api/user/{userId}/collection", collectionController::handleGetCollectionsForUser, Role.USER,Role.ADMIN);
        app.post("/api/user/{userId}/collection", collectionController::handleCreateCollection, Role.USER,Role.ADMIN);
        app.get("/api/collection/{collectionId}", collectionController::handleGetCollectionById, Role.USER,Role.ADMIN);
        app.put("/api/collection/{collectionId}", collectionController::handleUpdateCollection, Role.USER,Role.ADMIN);
        app.delete("/api/collection/{collectionId}", collectionController::handleDeleteCollection, Role.USER,Role.ADMIN);

        //ITEM
        app.get("/api/item/{itemId}", itemController::handleGetItemById,Role.USER,Role.ADMIN);
        app.get("/api/collection/{collectionId}/item", itemController::handleGetItemsForCollection,Role.USER,Role.ADMIN);
        app.put("/api/item/{itemId}", itemController::handleUpdateItem,Role.USER,Role.ADMIN);
        app.delete("/api/item/{itemId}", itemController::handleDeleteItem,Role.USER,Role.ADMIN);
        app.post("/api/collection/{collectionId}/item", itemController::handleCreateItemInCollection,Role.USER,Role.ADMIN);

        //BOOKS (item)
        app.post("/api/collection/{collectionId}/book", itemController::handleCreateBookInCollection,Role.USER,Role.ADMIN);
        app.get("/api/books/search", itemController::handleSearchBooks, Role.USER, Role.ADMIN);

/*
        userDAO.create(new User("test@test.dk", "test", "testName"));
        BookService.saveBookChoiceToDatabase("Harry Potter", 1);
        for(Item i: itemDAO.getAll()){
            System.out.println(i.name);
        }*/


    }
}