package app.Controller;

import app.daos.BookDAO;
import app.daos.CollectionDAO;
import app.daos.ItemDAO;
import app.daos.UserDAO;
import app.dtos.ApiResponseDTO;
import app.dtos.BookRequestDTO;
import app.dtos.ItemRequestDTO;
import app.entities.*;
import app.utils.BookService;
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
            ctx.status(404).json(new ApiResponseDTO(404, "Collection not found"));
            return;
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = collection.getUser();

        if(!checkOwnership(ctx, tokenUser, existingUser.getUsername())) return;

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
            ctx.status(404).json(new ApiResponseDTO(404, "Item not found"));
            return;
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = item.getCollection().getUser();

        if(!checkOwnership(ctx, tokenUser, existingUser.getUsername())) return;


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
            ctx.status(404).json(new ApiResponseDTO(404, "Item not found"));
            return;
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = existingItem.getCollection().getUser();

        if(!checkOwnership(ctx, tokenUser, existingUser.getUsername())) return;

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
            ctx.status(404).json(new ApiResponseDTO(404, "Item not found"));
            return;
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = itemToDelete.getCollection().getUser();

        if(!checkOwnership(ctx, tokenUser, existingUser.getUsername())) return;

        itemDAO.delete(itemToDelete);
        ctx.status(204).json(new ApiResponseDTO(204, "Item with id " + itemId + " deleted"));
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
            ctx.status(404).json(new ApiResponseDTO(404, "Collection not found"));
            return;
        }

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = collection.getUser();

        if(!checkOwnership(ctx, tokenUser, existingUser.getUsername())) return;

        BookRequestDTO received = ctx.bodyAsClass(BookRequestDTO.class);

        if (received.getTitle() == null || received.getTitle().isBlank()) {
            ctx.status(400).json(new ApiResponseDTO(400, "Book title is required"));
            return;
        }

        if (received.getAuthors() == null || received.getAuthors().isEmpty()) {
            ctx.status(400).json(new ApiResponseDTO(400, "At least one author is required"));
            return;
        }

        if (received.getStatus() == null || received.getCondition() == null) {
            ctx.status(400).json(new ApiResponseDTO(400, "Status and condition are required"));
            return;
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

    private boolean checkOwnership(Context ctx, UserDTO tokenUser, String ownerUsername) {
        if (!tokenUser.getUsername().equals(ownerUsername)) {
            ctx.status(403).json(new ApiResponseDTO(403, "Forbidden not your own information"));
            return false;
        }
        return true;
    }

}
