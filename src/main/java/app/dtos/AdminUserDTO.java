package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserDTO {
    private Integer id;
    private String username;
    private String email;
    private Set<String> roles;
}
