package app.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "books")
public class Book extends Item{

    private List<String> author;

    public Book(String name, String description, List<String> author, int releaseYear, ItemStatus status, ItemCondition condition, Collection collection) {
        super(name, description, LocalDateTime.now(), ItemType.BOOK, releaseYear, status, condition, collection);
        this.author = author;
    }


}
