package com.iisc.pods.movieticketbooking.wallet;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

/**
 * Resource representing a wallet of the user in the system.
 */
@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    private Integer user_id;
    private Integer balance;
}
