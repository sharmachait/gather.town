package com.sharmachait.PrimaryBackend.models.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private int width;
    private int height;
    private String thumbnail;

    @OneToMany(
            mappedBy = "space",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    @JsonManagedReference
    private Set<SpaceElement> spaceElements;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_id", referencedColumnName = "id")
    @JsonBackReference
    private GameMap gameMap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    @JsonBackReference
    private User owner;
}
