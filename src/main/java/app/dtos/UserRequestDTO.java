package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UserRequestDTO {
    private String username;
    private String email;
    private String password;

    public UserRequestDTO(String email) {
        this.email = email;
    }

    public UserRequestDTO(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
