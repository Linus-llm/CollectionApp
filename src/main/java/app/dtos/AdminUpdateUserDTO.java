package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserDTO {
    private String username;
    private String email;
    private Set<String> roles;
}
