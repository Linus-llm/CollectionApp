package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "items")
@Inheritance(strategy = InheritanceType.JOINED)
public class Item {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    public int id;
    public String name;
    public String description;
    public LocalDateTime createdAt;
    public int releaseYear;

    @Enumerated(EnumType.STRING)
    public ItemType type;

    @Enumerated(EnumType.STRING)
    public ItemStatus status;

    @Enumerated(EnumType.STRING)
    public ItemCondition condition;

    @ManyToOne
    @JoinColumn(name = "collection_id", nullable = false)
    public Collection collection;

    public Item(String name, String description, LocalDateTime createdAt, ItemType type, int releaseYear, ItemStatus status, ItemCondition condition) {
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.releaseYear = releaseYear;
        this.type = type;
        this.status = status;
        this.condition = condition;
    }
}
