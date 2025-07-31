package pl.skf.sws.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.skf.sws.adapter.DigiKatRankingAdapter;

import pl.skf.sws.component.sorting.MovieSortingFactory;
import pl.skf.sws.exception.MovieNotFoundException;
import pl.skf.sws.feign.DigiKatClient;
import pl.skf.sws.model.*;
import pl.skf.sws.repo.MovieRepo;
import pl.skf.sws.service.MovieFileService;
import pl.skf.sws.service.MovieService;
import pl.skf.sws.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepo movieRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final DigiKatClient digiKatClient;
    private final DigiKatRankingAdapter digiKatRankingAdapter;
    private final MovieFileService movieFileService;
    private final MovieSortingFactory sortingFactory;


    @Transactional
    @Override
    public Long saveMovie(MovieDto movieDto, MultipartFile file, Long userId) {
        movieFileService.validateFile(file);
        String savedFilePath = movieFileService.storeFile(file);
        User user = userService.getUserById(userId);
        Movie movie = mapToMovieEntity(movieDto, savedFilePath, file, user);
        movie.setId(null);
        return persistMovieOrRollbackFile(movie, savedFilePath);
    }

    @Override
    public List<RankingDto> getAllMoviesSorted(String sortBy) {
        List<Movie> movies = movieRepo.findAll();
        List<RankingDto> rankingDtoList = movies.stream()
                .map(movie -> {
                    DigiKatResponse response = digiKatClient.getRanking((movie.getTitle()));
                    return digiKatRankingAdapter.adapt(movie,response);
                }).toList();

        return sortingFactory.getStrategy(sortBy).sort(rankingDtoList);
    }

    @Transactional
    @Override
    public void updateMovie(Long movieId, MoviePatchDto moviePatchDto) {
        log.info("Updating movie with id: {}", movieId);
        Movie movie = getMovie(movieId);

        modelMapper.map(moviePatchDto, movie);
        movie.setUpdatedAt(LocalDateTime.now());
        movieRepo.save(movie);

    }

    @Override
    public RankingDto getMovieRanking(long id) {
        Movie movie = getMovie(id);
        DigiKatResponse digiKatResponse;
        try {
            digiKatResponse = digiKatClient.getRanking(movie.getTitle());
        } catch (Exception ex) {
            throw new MovieNotFoundException("Movie not found with id: " + id);
        }
        return digiKatRankingAdapter.adapt(movie, digiKatResponse);
    }

    private Movie getMovie(Long id) {
        return movieRepo.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + id));
    }

    private Movie mapToMovieEntity(MovieDto movieDto, String savedFilePath, MultipartFile file, User user) {
        Movie movie = modelMapper.map(movieDto, Movie.class);
        movie.setFileSize(file.getSize());
        movie.setFilePath(savedFilePath);
        movie.setUser(user);
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedAt(LocalDateTime.now());
        return movie;
    }

    private Long persistMovieOrRollbackFile(Movie movie, String filePath) {
        try {
            movie = movieRepo.save(movie);
        } catch (Exception e) {
            movieFileService.deleteFileQuietly(filePath);
            throw e;
        }
        log.info("Saved on DB Successfully");
        return movie.getId();
    }

}
