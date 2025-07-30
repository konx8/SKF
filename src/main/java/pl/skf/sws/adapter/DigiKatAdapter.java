package pl.skf.sws.adapter;

import org.springframework.stereotype.Component;
import pl.skf.sws.model.DigiKatResponse;
import pl.skf.sws.model.Movie;
import pl.skf.sws.model.MovieAvailability;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DigiKatAdapter {

    public Movie convertToMovie(DigiKatResponse digiKatResponse) {
        Movie movie = new Movie();

        movie.setTitle(digiKatResponse.getTitle());
        movie.setProduction(digiKatResponse.getProduction());
        movie.setAvailabilities(mapMovieAvailability(digiKatResponse.getAvailability(), movie));
        movie.setTitle(digiKatResponse.getTitle());

        movie.setUserRating(digiKatResponse.getUserRating());
        movie.setLastUpdate(LocalDateTime.parse(digiKatResponse.getLastUpdate()));


        return movie;
    }

    private List<MovieAvailability> mapMovieAvailability(List<String> availability, Movie movie){
        if(availability == null || availability.isEmpty()){
            return new ArrayList<>();
        } else {
            return availability.stream().map(platform -> {
                MovieAvailability ma = new MovieAvailability();
                ma.setPlatform(platform);
                ma.setMovie(movie);
                return ma;
            }).toList();
        }
    }

}
