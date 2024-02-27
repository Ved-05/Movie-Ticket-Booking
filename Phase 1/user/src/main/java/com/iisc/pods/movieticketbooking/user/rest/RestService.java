package com.iisc.pods.movieticketbooking.user.rest;

import lombok.extern.slf4j.Slf4j;
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

    private static final String BASE_URL = "http://localhost";
    private static final String WALLET_SERVICE_URL = BASE_URL + ":8082/wallets/";
    private static final String BOOKING_SERVICE_URL = BASE_URL + ":8081/bookings/";

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
        String url = BOOKING_SERVICE_URL + "{user_id}";
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
        ResponseEntity<String> response = restTemplate.exchange(BOOKING_SERVICE_URL, HttpMethod.DELETE,
                null, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    /**
     * Delete wallet records for the user
     *
     * @param userId Id of the user
     * @return true if wallet is deleted, else false
     */
    public boolean deleteWallet(Integer userId) {
        ResponseEntity<String> response = restTemplate.exchange(WALLET_SERVICE_URL + "{user_id}", HttpMethod.DELETE,
                null, String.class, userId);
        return response.getStatusCode().is2xxSuccessful();
    }

    /**
     * Delete all wallet records for the user
     *
     * @return true if wallet is deleted, else false
     */
    public boolean deleteWallet() {
        ResponseEntity<String> response = restTemplate.exchange(WALLET_SERVICE_URL, HttpMethod.DELETE,
                null, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }
}
