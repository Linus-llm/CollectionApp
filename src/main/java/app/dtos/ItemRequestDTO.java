package app.dtos;

import app.entities.ItemCondition;
import app.entities.ItemStatus;
import app.entities.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ItemRequestDTO {
    private int id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private int releaseYear;
    private ItemType type;
    private ItemStatus status;
    private ItemCondition condition;
    private int collectionId;
}
