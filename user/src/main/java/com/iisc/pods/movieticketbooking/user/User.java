package com.iisc.pods.movieticketbooking.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resource representing a user in the system.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    private @Id @GeneratedValue Integer id;
    private String name;
    private @Column(unique = true) String email;

    /**
     * Constructor for User.
     * @param name Name of the user.
     * @param email Email of the user.
     */
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
