package app.dtos;

import app.entities.ItemCondition;
import app.entities.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestDTO {
    private String title;
    private String description;
    private List<String> authors;
    private int releaseYear;
    private ItemStatus status;
    private ItemCondition condition;
}
