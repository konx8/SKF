package pl.skf.sws.service.impl;

import org.springframework.web.multipart.MultipartFile;
import pl.skf.sws.model.Movie;
import pl.skf.sws.model.MovieDto;

import java.util.List;

public interface MovieService {

    List<Movie> allMovie();

    Long saveMovie(MovieDto movieDto, MultipartFile file, Long userId);


    }
