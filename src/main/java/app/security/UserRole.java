package app.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "role")
public class UserRole {
    @Id
    @Column(name = "role_name", nullable = false)
    private String roleName;
    public UserRole(String roleName){
        this.roleName = roleName;
    }

}
