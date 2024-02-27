package com.iisc.pods.movieticketbooking.wallet;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    /**
     * Get wallet by id.
     *
     * @param userId Id of the user (Primary key)
     * @return Wallet object for the user.
     * @throws IllegalArgumentException If wallet not found for the user id.
     */
    public Wallet getWalletById(Integer userId) {
        return walletRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("Wallet not found for user id: " + userId)
        );
    }

    /**
     * Update wallet balance for the user by user id.
     *
     * @param userId Id of the user (Primary key)
     * @param action Action to be performed (debit/credit)
     * @param amount Amount to be debited/credited
     * @return Updated wallet object.
     */
    public Wallet updateWalletBalance(Integer userId, String action, Integer amount) throws BadRequestException {
        Wallet wallet = walletRepository.findById(userId).orElse(new Wallet(userId, 0));
        Integer balance = wallet.getBalance();
        if ("debit".equalsIgnoreCase(action)) {
            if (balance < amount) throw new BadRequestException("Insufficient balance for debit");
            wallet.setBalance(balance - amount);
        } else if ("credit".equalsIgnoreCase(action)) {
            wallet.setBalance(balance + amount);
        } else {
            throw new BadRequestException("Invalid action: " + action);
        }

        return walletRepository.save(wallet);
    }

    /**
     * Create a new wallet for the user
     *
     * @param userId Id of the user (Primary key)
     * @throws IllegalArgumentException If wallet does not exists for the user id.
     */
    public void deleteWallet(Integer userId) throws IllegalArgumentException {
        Wallet wallet = walletRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("Wallet not found for user id: " + userId)
        );
        walletRepository.delete(wallet);
    }

    /**
     * Delete all wallets.
     */
    public void deleteAllWallets() {
        walletRepository.deleteAll();
    }
}