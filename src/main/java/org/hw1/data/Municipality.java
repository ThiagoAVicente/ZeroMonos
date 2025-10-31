package org.hw1.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "municipalities")
@Setter
@Getter
public class Municipality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    public Municipality(String name) {
        this.name = name;
    }
    public Municipality() {
    }
}
