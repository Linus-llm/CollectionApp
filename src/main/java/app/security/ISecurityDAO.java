package app.security;

import app.entities.User;
import app.exceptions.ValidationException;

public interface ISecurityDAO {
    User getVerifiedUser(String username, String password) throws ValidationException; // used for login
    User createUser(String username, String password); // used for register
    UserRole createRole(String UserRole);
    User addUserRole(String username, String role);
}
