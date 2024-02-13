package com.iisc.pods.movieticketbooking.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer user_id;
    private Integer balance;

    public Wallet() {
        // Default constructor for JPA
    }

    public Wallet(Integer user_id, Integer balance) {
        this.user_id = user_id;
        this.balance = balance;
    }

    public Integer getId() {
        return id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }
}

// The Wallet class represents the wallet entity.
// It is annotated with @Entity to indicate that it is a JPA entity.
// The @Id annotation specifies the primary key field.
// The @GeneratedValue annotation indicates that the ID is automatically
// generated.
// Fields include user_id and balance.
// Getters and setters are provided for accessing and modifying the fields

// WalletController.java:
// This class will handle the REST endpoints (GET, PUT, DELETE) for
// wallet-related operations.
// Annotate it with @RestController and define methods to handle each endpoint.
// WalletService.java:
// This class will contain the business logic for wallet management.
// Implement methods for creating wallets, debiting/crediting balances, fetching
// wallet details, and deleting wallets.
// WalletRepository.java (Optional, if using a database):
// If you’re using a database, create this interface to perform CRUD operations
// on wallet data.
// Extend JpaRepository<Wallet, Integer> or a similar interface.
// Annotate it with @Repository.
// Wallet.java (Entity/Model class):
// Define the Wallet entity class with fields like user_id and balance.
// Annotate it with @Entity.
// Define appropriate relationships (e.g., @OneToOne with User).
// WalletApplication.java (Spring Boot Application class):
// Create this main application class in the root package
// (com.iisc.pods.movieticketbooking).
// Annotate it with @SpringBootApplication.
// This class serves as the entry point for your Wallet service.
// application.properties or application.yml:
// Create this configuration file in the src/main/resources directory.
// Configure any necessary properties related to your application, such as
// database connection details, server port, etc.