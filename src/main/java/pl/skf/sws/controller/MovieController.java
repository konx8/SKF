package pl.skf.sws.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.skf.sws.model.DigiKatResponse;
import pl.skf.sws.model.Movie;
import pl.skf.sws.model.MovieDto;
import pl.skf.sws.model.MoviePatchDto;
import pl.skf.sws.service.impl.DigiKatService;
import pl.skf.sws.service.impl.MovieService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/movies")
public class MovieController {

    final private MovieService movieService;
    final private DigiKatService digiKatService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> addMovie(
            @RequestPart("movie") MovieDto movieDto,
            @RequestPart("file") MultipartFile file,
            @RequestPart("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.saveMovie(movieDto, file, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> patchMovie(
            @PathVariable("id") Long movieId,
            @RequestBody MoviePatchDto moviePatchDto) {
        movieService.updateMovie(movieId, moviePatchDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Movie> getAllMovies() {
        return movieService.allMovie();
    }

    @GetMapping("/digiKat")
    public DigiKatResponse getDigiKat() {
        return digiKatService.getRankingByTitle("Avengers");
    }

}
