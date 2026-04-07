package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserRequestDTO {
    private String username;
    private String email;

    public UserRequestDTO(String email) {
        this.email = email;
    }
}
