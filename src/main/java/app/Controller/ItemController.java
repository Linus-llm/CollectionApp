package app.Controller;

import app.daos.BookDAO;
import app.daos.CollectionDAO;
import app.daos.ItemDAO;
import app.daos.UserDAO;
import app.dtos.ApiResponseDTO;
import app.dtos.BookRequestDTO;
import app.dtos.ItemRequestDTO;
import app.entities.*;
import app.exceptions.ApiException;
import app.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemController {
    static EntityManagerFactory emf = app.Main.emf;
    private final ItemDAO itemDAO;
    private final BookDAO bookDAO;
    private final CollectionDAO collectionDAO;
    private final UserDAO userDAO;


    public ItemController(ItemDAO itemDAO, BookDAO bookDAO, CollectionDAO collectionDAO, UserDAO userDAO) {
        this.itemDAO = itemDAO;
        this.bookDAO = bookDAO;
        this.collectionDAO = collectionDAO;
        this.userDAO = userDAO;
    }


    public void handleGetItemsForCollection(Context ctx) {
        int collectionId = Integer.parseInt(ctx.pathParam("collectionId"));
        Collection collection = collectionDAO.getByID(collectionId);



        if (collection == null) {
            throw new ApiException(404, "Collection not found");

        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = collection.getUser();

        checkOwnership(tokenUser, existingUser.getUsername());

        Set<ItemRequestDTO> dtoSet = collection.getItems().stream()
                .map(item -> new ItemRequestDTO(
                        item.getId(),
                        item.getName(),
                        item.getDescription(),
                        item.getCreatedAt(),
                        item.getReleaseYear(),
                        item.getType(),
                        item.getStatus(),
                        item.getCondition(),
                        item.getCollection().getId()
                ))
                .collect(Collectors.toSet());

        ctx.json(dtoSet);
    }


    public void handleGetItemById(Context ctx) {
        int itemId = Integer.parseInt(ctx.pathParam("itemId"));
        Item item = itemDAO.getByID(itemId);

        if (item == null) {
            throw new ApiException(404, "Item not found");
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = item.getCollection().getUser();

        checkOwnership(tokenUser, existingUser.getUsername());


        ctx.json(new ItemRequestDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getCreatedAt(),
                item.getReleaseYear(),
                item.getType(),
                item.getStatus(),
                item.getCondition(),
                item.getCollection().getId()
        ));
    }

    public void handleUpdateItem(Context ctx) {
        int itemId = Integer.parseInt(ctx.pathParam("itemId"));
        Item existingItem = itemDAO.getByID(itemId);

        if (existingItem == null) {
            throw new ApiException(404, "Item not found");
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = existingItem.getCollection().getUser();

        checkOwnership(tokenUser, existingUser.getUsername());

        ItemRequestDTO received = ctx.bodyAsClass(ItemRequestDTO.class);

        if (received.getName() != null && !received.getName().isBlank()) {
            existingItem.setName(received.getName());
        }
        if (received.getDescription() != null) {
            existingItem.setDescription(received.getDescription());
        }
        if (received.getReleaseYear() != 0 && received.getReleaseYear() < LocalDateTime.now().getYear()) {
            existingItem.setReleaseYear(received.getReleaseYear());
        }
        if (received.getStatus() != null) {
            existingItem.setStatus(received.getStatus());
        }
        if (received.getCondition() != null) {
            existingItem.setCondition(received.getCondition());
        }

        itemDAO.update(existingItem);

        ctx.json(new ItemRequestDTO(
                existingItem.getId(),
                existingItem.getName(),
                existingItem.getDescription(),
                existingItem.getCreatedAt(),
                existingItem.getReleaseYear(),
                existingItem.getType(),
                existingItem.getStatus(),
                existingItem.getCondition(),
                existingItem.getCollection().getId()
        ));
    }

    public void handleDeleteItem(Context ctx) {
        int itemId = Integer.parseInt(ctx.pathParam("itemId"));
        Item itemToDelete = itemDAO.getByID(itemId);

        if (itemToDelete == null) {
            throw new ApiException(404, "Item not found");
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = itemToDelete.getCollection().getUser();

        checkOwnership(tokenUser, existingUser.getUsername());

        itemDAO.delete(itemToDelete);
        ctx.status(204).json(new ApiResponseDTO(204, "Item with id " + itemId + " deleted"));
    }


    public void handleCreateItemInCollection(Context ctx){
        int collectionId = Integer.parseInt(ctx.pathParam("collectionId"));
        Collection collection = collectionDAO.getByID(collectionId);
        if (collection == null) {
            throw new ApiException(404, "Collection not found");
        }
        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = collection.getUser();

        checkOwnership(tokenUser, existingUser.getUsername());

        ItemRequestDTO received = ctx.bodyAsClass(ItemRequestDTO.class);

        if (received.getName() == null || received.getName().isBlank()) {
            throw new ValidationException("Item name is required");
        }

        if (received.getType() == null) {
            throw new ValidationException("Item type is required");
        }

        if (received.getStatus() == null || received.getCondition() == null) {
            throw new ValidationException("Status and condition are required");
        }

        Item newItem = new Item();
        newItem.setName(received.getName());
        newItem.setDescription(received.getDescription());
        newItem.setCollection(collection);
        newItem.setCreatedAt(LocalDateTime.now());
        newItem.setReleaseYear(received.getReleaseYear());
        newItem.setType(received.getType());
        newItem.setStatus(received.getStatus());
        newItem.setCondition(received.getCondition());

        itemDAO.create(newItem);

        ctx.status(201).json(new ItemRequestDTO(
                newItem.getId(),
                newItem.getName(),
                newItem.getDescription(),
                newItem.getCreatedAt(),
                newItem.getReleaseYear(),
                newItem.getType(),
                newItem.getStatus(),
                newItem.getCondition(),
                newItem.getCollection().getId()
        ));

    }
    //--------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------
    //------------------------------BOOK-SPECIFIC ENDPOINTS---------------------------------
    //--------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------


    public void handleCreateBookInCollection(Context ctx) {
        int collectionId = Integer.parseInt(ctx.pathParam("collectionId"));
        Collection collection = collectionDAO.getByID(collectionId);

        if (collection == null) {
            throw new ApiException(404, "Collection not found");
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = collection.getUser();

        checkOwnership(tokenUser, existingUser.getUsername());

        BookRequestDTO received = ctx.bodyAsClass(BookRequestDTO.class);

        if (received.getTitle() == null || received.getTitle().isBlank()) {
            throw new ValidationException("Book title is required");
        }

        if (received.getAuthors() == null || received.getAuthors().isEmpty()) {
            throw new ValidationException("At least one author is required");
        }

        if (received.getStatus() == null || received.getCondition() == null) {
            throw new ValidationException("Status and condition are required");
        }

        Book newBook = new Book();
        newBook.setName(received.getTitle());
        newBook.setDescription(received.getDescription());
        newBook.setCreatedAt(LocalDateTime.now());
        newBook.setReleaseYear(received.getReleaseYear());
        newBook.setType(ItemType.BOOK);
        newBook.setStatus(received.getStatus());
        newBook.setCondition(received.getCondition());
        newBook.setAuthor(received.getAuthors());
        newBook.setCollection(collection);

        bookDAO.create(newBook);

        ctx.status(201).json(new ItemRequestDTO(
                newBook.getId(),
                newBook.getName(),
                newBook.getDescription(),
                newBook.getCreatedAt(),
                newBook.getReleaseYear(),
                newBook.getType(),
                newBook.getStatus(),
                newBook.getCondition(),
                newBook.getCollection().getId()
        ));
    }

    private void checkOwnership(UserDTO tokenUser, String ownerUsername) {
        if (!tokenUser.getUsername().equals(ownerUsername)) {
            throw new ApiException(403, "Forbidden: not your resource");
        }
    }

}
