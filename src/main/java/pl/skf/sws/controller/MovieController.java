package pl.skf.sws.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import pl.skf.sws.exception.InvalidSortTypeException;
import pl.skf.sws.model.*;
import pl.skf.sws.service.MovieService;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/movies")
public class MovieController {

    private static final Set<String> ALLOWED_SORTS = Set.of("ranking", "size");

    final private MovieService movieService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> addMovie(
            @RequestPart("movie") @Valid MovieDto movieDto,
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

    @GetMapping("/{id}/ranking")
    public ResponseEntity<RankingDto> getRanking(@PathVariable Long id) {
        RankingDto rankingDto = movieService.getMovieRanking(id);
        return ResponseEntity.ok(rankingDto);
    }

    @GetMapping
    public ResponseEntity<List<RankingDto>> getAllMovies(
            @RequestParam(name = "sort", defaultValue = "ranking") String sortBy,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        if (!ALLOWED_SORTS.contains(sortBy)) {
            throw new InvalidSortTypeException("Invalid sort type: " + sortBy);
        }
        return ResponseEntity.ok(movieService.getAllMoviesRankingSorted(sortBy, page, size));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMovie(@PathVariable Long id) throws FileNotFoundException {

        MovieFileResource fileResource = movieService.loadFileAsResource(id);
        String contentDisposition = "attachment; filename=\"" + fileResource.filename() + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResource.resource());
    }

}
