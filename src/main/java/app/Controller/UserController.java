package app.Controller;

import app.daos.BookDAO;
import app.daos.CollectionDAO;
import app.daos.ItemDAO;
import app.daos.UserDAO;
import app.dtos.ApiResponseDTO;
import app.dtos.UserRequestDTO;
import app.entities.User;
import app.exceptions.ApiException;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class UserController {
    static EntityManagerFactory emf = app.Main.emf;

    private final UserDAO userDAO;

    public UserController( UserDAO userDAO) {

        this.userDAO = userDAO;
    }


    public void handleGetUsers(Context ctx){

        Set<User> setOfUsers = userDAO.getAll();
        //convert user to userDTO to only return first name and email
        Set<UserRequestDTO> dtoSet = setOfUsers.stream()
                .map(u -> new UserRequestDTO(u.getEmail()))
                .collect(Collectors.toSet());

        ctx.json(dtoSet);
    }
    public void handleGetUserById(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        User user = userDAO.getByID(id);
        if (user == null){
            throw new ApiException(404, "User not found");
        }

        ctx.json(new UserRequestDTO(user.getEmail()));
    }

    public void handleUpdateUser(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = userDAO.getByID(id);

        if (existingUser == null){
            throw new ApiException(404, "User not found");
        }

        checkOwnership(tokenUser, existingUser.getUsername());


        UserRequestDTO received = ctx.bodyAsClass(UserRequestDTO.class);

        if (received.getUsername() != null) {
            existingUser.setUsername(received.getUsername());
        }

        if (received.getEmail() != null) {
            existingUser.setEmail(received.getEmail());
        }

        if (received.getPassword() != null) {
            existingUser.setPassword(received.getPassword());
        }

        User updatedUser = userDAO.update(existingUser);

        ctx.json(new UserRequestDTO(updatedUser.getUsername(), updatedUser.getEmail()));
    }
    public void handleDeleteUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        User userToDelete = userDAO.getByID(id);
        UserDTO tokenUser = ctx.attribute("user");

        if (userToDelete == null) {
            throw new ApiException(404, "User not found");
        }

        checkOwnership(tokenUser, userToDelete.getUsername());


        userDAO.delete(userToDelete);
        ctx.status(204).json(new ApiResponseDTO(204, "User with id " + id + " deleted"));
    }

    private void checkOwnership(UserDTO tokenUser, String ownerUsername) {
        if (!tokenUser.getUsername().equals(ownerUsername)) {
            throw new ApiException(403, "Forbidden: not your resource");
        }
    }
}
