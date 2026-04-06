package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CollectionRequestDTO {
    private int id;
    private String name;
    private String description;
}
