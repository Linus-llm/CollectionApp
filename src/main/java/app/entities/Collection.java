package app.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "collections")
public class Collection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;
    private String description;
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Item> items = new HashSet<>();



    public Collection(User user, String name, String description, LocalDateTime createdAt) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

}
