package com.iisc.pods.movieticketbooking.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Resource representing a user in the system.
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "MY_USERS")
public class User {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private @Column(unique = true) String email;

    /**
     * Constructor for User.
     *
     * @param name  Name of the user.
     * @param email Email of the user.
     */
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
