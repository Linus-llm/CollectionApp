package app.entities;

import app.security.ISecurityUser;
import app.security.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User implements ISecurityUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    private String password;
    @Column(name = "username", nullable = false)
    private String username;
    @ManyToMany(fetch = FetchType.EAGER)
    Set<UserRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Collection> collections = new HashSet<>();

    public User(String username, String password, String email ){
        String salt = BCrypt.gensalt(12);
        String hashedPassword = BCrypt.hashpw(password, salt);
        this.username = username;
        this.password = hashedPassword;
        this.email = email;
    }

    @Override
    public Set<String> getRolesAsStrings() {
        return this.roles.stream().map((role)->role.getRoleName()).collect(Collectors.toSet());
    }

    @Override
    public boolean verifyPassword(String pw) {
        return BCrypt.checkpw(pw, this.password);
    }

    @Override
    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    @Override
    public void removeRole(String role) {

    }
}
