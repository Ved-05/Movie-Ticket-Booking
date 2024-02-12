package com.iisc.pods.movieticketbooking.booking_service.show;

import com.iisc.pods.movieticketbooking.booking_service.theatre.Theatre;
import com.iisc.pods.movieticketbooking.booking_service.theatre.TheatreNotFoundException;
import com.iisc.pods.movieticketbooking.booking_service.theatre.TheatreService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final TheatreService theatreService;

    /**
     * Constructor for ShowService
     *
     * @param showRepository Repository for Show
     */
    @Autowired
    public ShowService(ShowRepository showRepository, TheatreService theatreService) {
        this.showRepository = showRepository;
        this.theatreService = theatreService;
    }

    /**
     * Get all shows from the repository
     *
     * @param theatreId Id of the theatre
     * @return List of all shows
     */
    public List<Show> getAllShowsByTheatreId(Integer theatreId) throws TheatreNotFoundException {
        if (theatreService.getTheatreById(theatreId) != null) {
            return showRepository.findAllByTheatreId(theatreId);
        } else {
            throw new TheatreNotFoundException(theatreId);
        }
    }

    /**
     * Get all shows from the repository
     *
     * @return List of all shows
     */
    public Show getShowById(Integer showId) throws ShowNotFoundException {
        return showRepository.findById(showId).orElseThrow(
                () -> new ShowNotFoundException(showId)
        );
    }

    /**
     * Initialize the show repository with a shows from csv file.
     */
    @PostConstruct
    public void init() {
        theatreService.init();
        log.info("Loading shows from CSV file");
        try {
            BufferedReader br = new BufferedReader(new FileReader("data/shows.csv"));
            br.readLine(); // skip header
            String line = br.readLine();
            while (line != null) {
                String[] values = line.split(",");
                Integer theatreId = Integer.parseInt(values[1]);
                String movieName = values[2];
                Integer price = Integer.parseInt(values[3]);
                Integer seatsAvailable = Integer.parseInt(values[4]);
                Theatre theatre = theatreService.getTheatreById(theatreId);
                Show show = new Show(theatre, movieName, price, seatsAvailable);
                showRepository.save(show);
                assert show.getShow_id().equals(Integer.parseInt(values[0]));
                line = br.readLine();
            }
        } catch (IOException e) {
            log.error("Error loading shows from CSV file Message: " + e.getMessage());
        }
    }

    public void resetAllShows() {
        init();
    }
}
