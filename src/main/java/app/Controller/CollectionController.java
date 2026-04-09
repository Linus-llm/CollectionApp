package app.Controller;

import app.daos.CollectionDAO;
import app.daos.UserDAO;
import app.dtos.ApiResponseDTO;
import app.dtos.CollectionRequestDTO;
import app.entities.Collection;
import app.entities.User;
import app.exceptions.ApiException;
import app.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
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
            throw new ApiException(404, "User not found");
        }

        UserDTO tokenUser = ctx.attribute("user");

        checkOwnership(tokenUser, user.getUsername());

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
            throw new ApiException(404, "Collection not found");
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = collection.getUser();

        checkOwnership(tokenUser, existingUser.getUsername());

        ctx.json(new CollectionRequestDTO(
                collection.getId(),
                collection.getName(),
                collection.getDescription()));
    }


    public void handleCreateCollection(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        User user = userDAO.getByID(userId);

        if (user == null) {
            throw new ApiException(404, "User not found");
        }

        UserDTO tokenUser = ctx.attribute("user");

        checkOwnership(tokenUser, user.getUsername());

        CollectionRequestDTO received = ctx.bodyAsClass(CollectionRequestDTO.class);

        if (received.getName() == null || received.getName().isBlank()) {
            throw new ValidationException("Collection name is required");
        }
        if (received.getDescription() == null || received.getDescription().isBlank()) {
            throw new ValidationException("Collection description is required");
        }

        Collection newCollection = new Collection();
        newCollection.setName(received.getName());
        newCollection.setDescription(received.getDescription());
        newCollection.setCreatedAt(LocalDateTime.now());
        newCollection.setUser(user);

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
            throw new ApiException(404, "Collection not found");
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = existingCollection.getUser();

        checkOwnership(tokenUser, existingUser.getUsername());

        CollectionRequestDTO received = ctx.bodyAsClass(CollectionRequestDTO.class);

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
            throw new ApiException(404, "Collection not found");
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = collectionToDelete.getUser();

        checkOwnership(tokenUser, existingUser.getUsername());

        collectionDAO.delete(collectionToDelete);
        ctx.status(204).json(new ApiResponseDTO(204, "Collection with id " + collectionId + " deleted"));
    }

    private void checkOwnership(UserDTO tokenUser, String ownerUsername) {
        if (!tokenUser.getUsername().equals(ownerUsername)) {
            throw new ApiException(403, "Forbidden: not your resource");
        }
    }

}
