package org.hw1.data;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(name="is_staff", nullable = false)
    private boolean staff = false;
}
