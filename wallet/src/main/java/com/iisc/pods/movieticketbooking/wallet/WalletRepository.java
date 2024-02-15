package com.iisc.pods.movieticketbooking.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for wallet related operations.
 */
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
}
