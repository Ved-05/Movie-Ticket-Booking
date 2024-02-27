package com.iisc.pods.movieticketbooking.booking_service.show;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for Show
 */
public interface ShowRepository extends JpaRepository<Show, Integer> {

    /**
     * Find all shows by theatre id
     *
     * @param theatreId Id of the theatre
     * @return List of all shows
     */
    @Query("SELECT s FROM Show s WHERE s.theatre.theatre_id = :theatreId")
    List<Show> findAllByTheatreId(Integer theatreId);
}
