package com.iisc.pods.movieticketbooking.wallet;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<WalletResponse> getWalletDetails(@PathVariable("user_id") Integer userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        if (wallet != null) {
            return ResponseEntity.ok(new ResponseEntity<Wallet>(userId, wallet.getBalance()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{user_id}")
    public ResponseEntity<WalletResponse> updateWalletBalance(
            @PathVariable("user_id") Integer userId,
            @RequestBody WalletUpdateRequest request) {
        try {
            Wallet updatedWallet = walletService.updateWalletBalance(userId, request.getAction(), request.getAmount());
            return ResponseEntity.ok(new WalletResponse(userId, updatedWallet.getBalance()));
        } catch (InsufficientBalanceException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{user_id}")
    public ResponseEntity<Void> deleteWallet(@PathVariable("user_id") Integer userId) {
        boolean deleted = walletService.deleteWallet(userId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllWallets() {
        walletService.deleteAllWallets();
        return ResponseEntity.ok().build();
    }
}
