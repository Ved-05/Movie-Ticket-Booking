package com.iisc.pods.movieticketbooking.user.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service for wallet service related operations.
 */
@Slf4j
@Service
public class RestService {

    private final RestTemplate restTemplate;

    @Value("${WALLET_SERVICE_URL}")
    private String WALLET_SERVICE_URL;

    @Value("${BOOKING_SERVICE_URL}")
    private String BOOKING_SERVICE_URL;

    RestService() {
        restTemplate = new RestTemplate();
    }

    /**
     * Delete all bookings for the user
     *
     * @param userId Id of the user
     * @return true if bookings are deleted, else false
     */
    public boolean deleteBookings(Integer userId) {
        String url = BOOKING_SERVICE_URL + "/users/{user_id}";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class,
                userId);
        return response.getStatusCode().is2xxSuccessful();
    }

    /**
     * Delete all booking records for the user
     *
     * @return true if wallet is deleted, else false
     */
    public boolean deleteBookings() {
        log.info("Deleting all bookings at " + BOOKING_SERVICE_URL);
        ResponseEntity<String> response = restTemplate.exchange(BOOKING_SERVICE_URL, HttpMethod.DELETE,
                null, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    /**
     * Delete wallet records for the user
     *
     * @param userId ID of the user
     * @return true if wallet is deleted, else false
     */
    public boolean deleteWallet(Integer userId) {
        ResponseEntity<String> response = restTemplate.exchange(WALLET_SERVICE_URL + "/{user_id}", HttpMethod.DELETE,
                null, String.class, userId);
        return response.getStatusCode().is2xxSuccessful();
    }

    /**
     * Delete all wallet records for the user
     *
     * @return true if wallet is deleted, else false
     */
    public boolean deleteWallet() {
        log.info("Deleting all wallets at " + WALLET_SERVICE_URL);
        ResponseEntity<String> response = restTemplate.exchange(WALLET_SERVICE_URL, HttpMethod.DELETE,
                null, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }
}
