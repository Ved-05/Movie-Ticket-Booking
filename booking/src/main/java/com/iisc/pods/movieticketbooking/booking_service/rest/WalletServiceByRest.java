package com.iisc.pods.movieticketbooking.booking_service.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Service for wallet service related operations.
 */
@Slf4j
@Service
public class WalletServiceByRest {

    private final RestTemplate restTemplate;
    private static final String WALLET_SERVICE_URL = "http://localhost:8082/wallets/";

    WalletServiceByRest() {
        restTemplate = new RestTemplate();
    }

    /**
     * Deduct amount from the wallet
     *
     * @param userId Id of the user
     * @param amount Amount to be deducted
     * @return true if amount is deducted, else false
     */
    public boolean deductAmountFromWallet(Integer userId, Integer amount) {
        String url = WALLET_SERVICE_URL + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = "{\"action\": \"debit\", \"amount\": " + amount + "}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        log.info("Deduct request: " + requestEntity);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    /**
     * Refund amount to the wallet
     *
     * @param userId Id of the user
     * @param amount Amount to be refunded
     * @return true if amount is refunded, else false
     */
    public boolean refundAmount(Integer userId, Integer amount) {
        String url = WALLET_SERVICE_URL + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = "{\"action\": \"credit\", \"amount\": " + amount + "}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        log.info("Refund request: " + requestEntity);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

}
