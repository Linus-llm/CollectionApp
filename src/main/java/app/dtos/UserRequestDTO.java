package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserRequestDTO {
    private String firstName;
    private String email;
}
