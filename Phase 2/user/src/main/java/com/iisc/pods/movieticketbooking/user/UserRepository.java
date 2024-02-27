package com.iisc.pods.movieticketbooking.user;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for user related operations.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
}