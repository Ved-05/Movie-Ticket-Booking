package com.iisc.pods.movieticketbooking.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

/**
 * Resource representing a user in the system.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    private @Id @GeneratedValue Integer id;
    private String name;
    private @Column(unique = true) String email;

}
