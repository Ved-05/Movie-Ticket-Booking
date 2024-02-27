package com.iisc.pods.movieticketbooking.booking_service.theatre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class TheatreService {

    @Autowired
    private TheatreRepository theatreRepository;

    /**
     * Get all theatres from the repository
     *
     * @return List of all theatres
     */
    public List<Theatre> getAllTheatres() {
        return theatreRepository.findAll();
    }

    /**
     * Get theatre by id
     *
     * @param theatreId Id of the theatre
     * @return Theatre object with the given id
     * @throws TheatreNotFoundException If theatre with id does not exist.
     */
    public Theatre getTheatreById(Integer theatreId) throws TheatreNotFoundException {
        return theatreRepository.findById(theatreId).orElseThrow(
                () -> new TheatreNotFoundException(theatreId)
        );
    }

    /**
     * Initialize the theatre repository with a theatres from csv file.
     */
    public void init() {
        log.info("Loading theatres from CSV file");
        try {
            BufferedReader br = new BufferedReader(new FileReader("/Users/suvedghanmode/IISc/Courses/PoDS/Movie-TIcket-Booking/Phase 2/booking/data/theatres.csv"));
            br.readLine(); // skip header
            String line = br.readLine();
            while (line != null) {
                String[] values = line.split(",");
                Integer theatreId = Integer.parseInt(values[0]);
                String name = values[1];
                String location = values[2];
                Theatre theatre = new Theatre(name, location);
                theatreRepository.save(theatre);
                assert theatre.getTheatre_id().equals(theatreId);
                line = br.readLine();
            }
        } catch (IOException e) {
            log.error("Error loading theatres from CSV file. Message: " + e.getMessage());
        }
    }
}
