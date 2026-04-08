package app.dtos;

import lombok.Getter;

@Getter
public class ApiResponseDTO {
    private int status;
    private String message;

    public ApiResponseDTO(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
