package app.Controller;

import app.daos.BookDAO;
import app.daos.CollectionDAO;
import app.daos.ItemDAO;
import app.daos.UserDAO;
import app.dtos.UserRequestDTO;
import app.entities.User;
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
                .map(u -> new UserRequestDTO(u.getFirstName(), u.getEmail()))
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

        ctx.json(new UserRequestDTO(user.getFirstName(), user.getEmail()));
    }
    public void handleCreateUser(Context ctx){
        User received = ctx.bodyAsClass(User.class);
        if (received.getFirstName() == null || received.getPassword() == null || received.getLastName() == null || received.getEmail() == null || received.getPhoneNumber() == null){
            ctx.status(400).result("Invalid user data");
            return;
        }

        User newUser = new User(received.getFirstName(), received.getLastName(), received.getEmail(), received.getPassword(), received.getPhoneNumber());
        userDAO.create(newUser);
        ctx.status(201).json(new UserRequestDTO(newUser.getFirstName(), newUser.getEmail()));
    }
    public void handleUpdateUser(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        User existingUser = userDAO.getByID(id);
        if (existingUser == null){
            ctx.status(404).result("User not found");
            return;
        }
        User received = ctx.bodyAsClass(User.class);
        if (received.getFirstName() == null) {
            received.setFirstName(existingUser.getFirstName());
            existingUser.setFirstName(received.getFirstName());
        } else {
            existingUser.setFirstName(received.getFirstName());
        }
        if (received.getLastName() == null) {
            received.setLastName(existingUser.getLastName());
            existingUser.setLastName(received.getLastName());
        } else {
            existingUser.setLastName(received.getLastName());
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
        if (received.getPhoneNumber() == null) {
            received.setPhoneNumber(existingUser.getPhoneNumber());
            existingUser.setPhoneNumber(received.getPhoneNumber());
        } else {
            existingUser.setPhoneNumber(received.getPhoneNumber());
        }
        userDAO.update(existingUser);
        ctx.json(new UserRequestDTO(existingUser.getFirstName(), existingUser.getEmail()));
    }
    public void handleDeleteUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        User userToDelete = userDAO.getByID(id);
        if (userToDelete == null) {
            ctx.status(404).result("User not found");
            return;
        }
        userDAO.delete(userToDelete);
        ctx.status(200).result("User with id " + id + " deleted");
    }
}
