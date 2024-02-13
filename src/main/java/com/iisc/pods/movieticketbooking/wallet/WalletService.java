package com.iisc.pods.movieticketbooking.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    @Autowired
    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet getWalletByUserId(Integer userId) {
        return walletRepository.findByUserId(userId);
    }

    public Wallet updateWalletBalance(Integer userId, String action, Integer amount) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet == null) {
            // Create a new wallet if not found
            wallet = new Wallet(userId, 0);
        }

        if ("debit".equalsIgnoreCase(action)) {
            if (wallet.getBalance() < amount) {
                throw new InsufficientBalanceException("Insufficient balance for debit");
            }
            wallet.setBalance(wallet.getBalance() - amount);
        } else if ("credit".equalsIgnoreCase(action)) {
            wallet.setBalance(wallet.getBalance() + amount);
        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }

        return walletRepository.save(wallet);
    }

    public boolean deleteWallet(Integer userId) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet != null) {
            walletRepository.delete(wallet);
            return true;
        }
        return false;
    }

    public void deleteAllWallets() {
        walletRepository.deleteAll();
    }
}

// The WalletService class contains the business logic for wallet management.
// It interacts with the WalletRepository to perform CRUD operations on wallet
// data.
// The updateWalletBalance method handles debiting/crediting balances and
// ensures sufficient balance.
// The InsufficientBalanceException is thrown when there’s insufficient balance
// for a debit operation.