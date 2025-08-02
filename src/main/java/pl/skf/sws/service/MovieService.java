package pl.skf.sws.service;

import org.springframework.web.multipart.MultipartFile;
import pl.skf.sws.model.MovieDto;
import pl.skf.sws.model.MovieFileResource;
import pl.skf.sws.model.MoviePatchDto;
import pl.skf.sws.model.RankingDto;

import java.io.FileNotFoundException;
import java.util.List;

public interface MovieService {

    List<RankingDto> getAllMoviesRankingSorted(String sortBy, int page, int size);

    Long saveMovie(MovieDto movieDto, MultipartFile file, Long userId);

    void updateMovie(Long movieId, MoviePatchDto moviePatchDto);

    RankingDto getMovieRanking(long id);

    MovieFileResource loadFileAsResource(Long id) throws FileNotFoundException;

}
