package com.company.wallet.repository;

import com.company.wallet.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    // Custom methods (if needed) can be added here
    // For example:
    // Wallet findByUserId(Integer userId);
    // void deleteByUserId(Integer userId);
}

// The WalletRepository interface extends JpaRepository<Wallet, Integer>.
// It inherits basic CRUD (Create, Read, Update, Delete) operations from
// JpaRepository.
// You can add custom methods (queries) specific to your wallet-related data
// access needs.
// Annotate it with @Repository to indicate that it’s a Spring Data repository.