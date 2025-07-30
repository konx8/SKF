package pl.skf.sws.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.skf.sws.model.Movie;
import pl.skf.sws.repo.MovieRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class MovieService {

    private MovieRepo movieRepo;

    public List<Movie> allMovie(){
        return movieRepo.findAll();
    }

}
