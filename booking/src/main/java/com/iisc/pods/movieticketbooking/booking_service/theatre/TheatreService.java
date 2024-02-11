package com.iisc.pods.movieticketbooking.booking_service.theatre;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Slf4j
@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;

    /**
     * Constructor for TheatreService
     *
     * @param theatreRepository Repository for theatre related operations
     */
    @Autowired
    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

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
            ClassPathResource resource = new ClassPathResource("static/theatres.csv");
            BufferedReader br = new BufferedReader(new FileReader(resource.getFile()));
            br.readLine(); // skip header
            String line = br.readLine();
            while (line != null) {
                log.info("Loading theatre: " + line);
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
