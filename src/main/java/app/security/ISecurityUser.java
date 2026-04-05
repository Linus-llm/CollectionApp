package app.security;

import java.util.Set;

public interface ISecurityUser {
    Set<String> getRolesAsStrings();

    boolean verifyPassword(String pw);

    void addRole(UserRole role);

    void removeRole(String role);
}
