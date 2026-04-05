package app.Controller;

import app.daos.BookDAO;
import app.daos.CollectionDAO;
import app.daos.ItemDAO;
import app.daos.UserDAO;
import app.dtos.CollectionRequestDTO;
import app.entities.Collection;
import app.entities.User;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectionController {
    static EntityManagerFactory emf = app.Main.emf;
    private final UserDAO userDAO;
    private final CollectionDAO collectionDAO;

    public CollectionController(UserDAO userDAO, CollectionDAO collectionDAO) {
        this.userDAO = userDAO;
        this.collectionDAO = collectionDAO;
    }


    public void handleGetCollectionsForUser(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        User user = userDAO.getByID(userId);

        if (user == null) {
            ctx.status(404).result("User not found");
            return;
        }

        Set<Collection> collections = user.getCollections();

        Set<CollectionRequestDTO> dtoSet = collections.stream()
                .map(c -> new CollectionRequestDTO(c.getId(), c.getName(), c.getDescription()))
                .collect(Collectors.toSet());

        ctx.json(dtoSet);
    }

    public void handleGetCollectionById(Context ctx) {
        int collectionId = Integer.parseInt(ctx.pathParam("collectionId"));
        Collection collection = collectionDAO.getByID(collectionId);

        if (collection == null) {
            ctx.status(404).result("Collection not found");
            return;
        }

        ctx.json(new CollectionRequestDTO(
                collection.getId(),
                collection.getName(),
                collection.getDescription()));
    }


    public void handleCreateCollection(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        User user = userDAO.getByID(userId);

        if (user == null) {
            ctx.status(404).result("User not found");
            return;
        }

        Collection received = ctx.bodyAsClass(Collection.class);

        if (received.getName() == null || received.getName().isBlank()) {
            ctx.status(400).result("Collection name is required");
            return;
        }

        Collection newCollection = new Collection();
        newCollection.setName(received.getName());
        newCollection.setDescription(received.getDescription());
        newCollection.setCreatedAt(LocalDateTime.now());
        newCollection.setUser(user);

        user.getCollections().add(newCollection);

        collectionDAO.create(newCollection);

        ctx.status(201).json(new CollectionRequestDTO(
                newCollection.getId(),
                newCollection.getName(),
                newCollection.getDescription()
        ));
    }


    public void handleUpdateCollection(Context ctx) {
        int collectionId = Integer.parseInt(ctx.pathParam("collectionId"));
        Collection existingCollection = collectionDAO.getByID(collectionId);

        if (existingCollection == null) {
            ctx.status(404).result("Collection not found");
            return;
        }

        Collection received = ctx.bodyAsClass(Collection.class);

        if (received.getName() != null) {
            existingCollection.setName(received.getName());
        }
        if (received.getDescription() != null) {
            existingCollection.setDescription(received.getDescription());
        }

        collectionDAO.update(existingCollection);

        ctx.json(new CollectionRequestDTO(
                existingCollection.getId(),
                existingCollection.getName(),
                existingCollection.getDescription()
        ));
    }

    public void handleDeleteCollection(Context ctx) {
        int collectionId = Integer.parseInt(ctx.pathParam("collectionId"));
        Collection collectionToDelete = collectionDAO.getByID(collectionId);

        if (collectionToDelete == null) {
            ctx.status(404).result("Collection not found");
            return;
        }

        collectionDAO.delete(collectionToDelete);
        ctx.status(200).result("Collection with id " + collectionId + " deleted");
    }

}
