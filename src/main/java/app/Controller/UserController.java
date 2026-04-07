package app.Controller;

import app.daos.BookDAO;
import app.daos.CollectionDAO;
import app.daos.ItemDAO;
import app.daos.UserDAO;
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
            ctx.status(404).result("User not found");
            return;
        }

        ctx.json(new UserRequestDTO(user.getEmail()));
    }

    public void handleUpdateUser(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));

        UserDTO tokenUser = ctx.attribute("user");

        User existingUser = userDAO.getByID(id);

        if(!checkOwnership(ctx, tokenUser, existingUser.getUsername())) return;

        if (existingUser == null){
            ctx.status(404).result("User not found");
            return;
        }
        User received = ctx.bodyAsClass(User.class);
        if (received.getUsername() == null) {
            received.setUsername(existingUser.getUsername());
            existingUser.setUsername(received.getUsername());
        } else {
            existingUser.setUsername(received.getUsername());
        }
        if (received.getEmail() == null) {
            received.setEmail(existingUser.getEmail());
            existingUser.setEmail(received.getEmail());
        } else {
            existingUser.setEmail(received.getEmail());
        }
        if (received.getPassword() == null) {
            received.setPassword(existingUser.getPassword());
            existingUser.setPassword(received.getPassword());
        } else {
            existingUser.setPassword(received.getPassword());
        }
        userDAO.update(existingUser);
        ctx.json(new UserRequestDTO(existingUser.getUsername(), existingUser.getEmail()));
    }
    public void handleDeleteUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        User userToDelete = userDAO.getByID(id);
        UserDTO tokenUser = ctx.attribute("user");

        if(!checkOwnership(ctx, tokenUser, userToDelete.getUsername())) return;

        if (userToDelete == null) {
            ctx.status(404).result("User not found");
            return;
        }
        userDAO.delete(userToDelete);
        ctx.status(200).result("User with id " + id + " deleted");
    }

    private boolean checkOwnership(Context ctx, UserDTO tokenUser, String ownerUsername) {
        if (!tokenUser.getUsername().equals(ownerUsername)) {
            ctx.status(403).json("Forbidden not your own information");
            return false;
        }
        return true;
    }
}
