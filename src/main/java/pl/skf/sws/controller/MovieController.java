package pl.skf.sws.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.skf.sws.model.DigiKatResponse;
import pl.skf.sws.model.Movie;
import pl.skf.sws.service.DigiKatService;
import pl.skf.sws.service.MovieService;

import java.util.List;

@RestController
@AllArgsConstructor
public class MovieController {

    private MovieService movieService;
    private DigiKatService digiKatService;

    @GetMapping("/movies")
    public List<Movie> getAllMovies(){
        return movieService.allMovie();
    }

    @GetMapping("/digiKat")
    public DigiKatResponse getDigiKat(){
        return digiKatService.getRankingByTitle("Avengers");
    }

}
