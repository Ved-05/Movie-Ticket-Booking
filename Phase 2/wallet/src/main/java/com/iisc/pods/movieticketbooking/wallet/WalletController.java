package com.iisc.pods.movieticketbooking.wallet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /**
     * Create a new wallet for the user
     *
     * @param userId ID of the user (Primary key)
     * @return Created wallet with status code 200 if created, else status code 400 for invalid request
     */
    @GetMapping("/{user_id}")
    public ResponseEntity<Wallet> getWalletDetails(@PathVariable("user_id") Integer userId) {
        ResponseEntity<Wallet> responseEntity;
        try {
            Wallet wallet = walletService.getWalletById(userId);
            responseEntity = new ResponseEntity<>(wallet, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting wallet details for user id: " + userId, e);
            responseEntity = ResponseEntity.notFound().build();
        }
        return responseEntity;
    }

    /**
     * Update wallet balance for the user by user id.
     *
     * @param user_id     ID of the user (Primary key)
     * @param requestBody Request body with action and amount
     * @return Updated wallet with status code 200 if updated, else status code 400 for invalid request
     */
    @PutMapping("/{user_id}")
    public ResponseEntity<Wallet> updateWalletBalance(@PathVariable Integer user_id,
                                                      @RequestBody Map<String, Object> requestBody) {
        ResponseEntity<Wallet> responseEntity;
        try {
            log.info("Updating wallet balance for user id: " + user_id);
            String action = (String) requestBody.get("action");
            int amount = (int) requestBody.get("amount");
            Wallet updatedWallet = walletService.updateWalletBalance(user_id, action, amount);
            responseEntity = new ResponseEntity<>(updatedWallet, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating wallet balance for user id: " + user_id, e);
            responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    /**
     * Delete wallet for the user by user id.
     *
     * @param userId ID of the user (Primary key)
     * @return status code 200 if deleted, else status code 404 for not found
     */
    @DeleteMapping("/{user_id}")
    public ResponseEntity<Void> deleteWallet(@PathVariable("user_id") Integer userId) {
        ResponseEntity<Void> responseEntity;
        try {
            walletService.deleteWallet(userId);
            responseEntity = ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting wallet for user id: " + userId, e);
            responseEntity = ResponseEntity.notFound().build();
        }
        return responseEntity;
    }

    /**
     * Delete all wallets.
     *
     * @return status code 200
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllWallets() {
        log.info("Deleting all wallets");
        walletService.deleteAllWallets();
        return ResponseEntity.ok().build();
    }
}
