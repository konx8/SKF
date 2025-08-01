package pl.skf.sws.service.impl;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.skf.sws.adapter.DigiKatRankingAdapter;

import pl.skf.sws.component.sorting.MovieSortingFactory;
import pl.skf.sws.exception.MovieNotFoundException;
import pl.skf.sws.feign.DigiKatClient;
import pl.skf.sws.model.*;
import pl.skf.sws.repo.MovieRepo;
import pl.skf.sws.service.MovieFileService;
import pl.skf.sws.service.MovieRankingService;
import pl.skf.sws.service.MovieService;
import pl.skf.sws.service.UserService;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MovieServiceImpl implements MovieService {

    @Value("${page.max-size}")
    private int pageMaxSize;
    @Value("${page.default-size}")
    private int pageDefaultSize;

    private final MovieRepo movieRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final DigiKatClient digiKatClient;
    private final DigiKatRankingAdapter digiKatRankingAdapter;
    private final MovieFileService movieFileService;
    private final MovieSortingFactory sortingFactory;
    private final MovieRankingService movieRankingService;

    public MovieServiceImpl(MovieRepo movieRepo, UserService userService,
                            ModelMapper modelMapper, DigiKatClient digiKatClient,
                            DigiKatRankingAdapter digiKatRankingAdapter, MovieFileService movieFileService,
                            MovieSortingFactory sortingFactory, MovieRankingService movieRankingService) {
        this.movieRepo = movieRepo;
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.digiKatClient = digiKatClient;
        this.digiKatRankingAdapter = digiKatRankingAdapter;
        this.movieFileService = movieFileService;
        this.sortingFactory = sortingFactory;
        this.movieRankingService = movieRankingService;
    }

    @Override
    public MovieFileResource loadFileAsResource(Long id) throws FileNotFoundException {
        Movie movie = getMovie(id);
        Resource fileAsResource = movieFileService.loadFileAsResource(movie.getFilePath());
        String filename = Paths.get(movie.getFilePath()).getFileName().toString();
        return new MovieFileResource(fileAsResource, filename);
    }

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
    public List<RankingDto> getAllMoviesRankingSorted(String sortBy, int page, int size) {
        size = validPageSize(size);
        page = Math.max(page, 0);
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviePage = movieRepo.findAll(pageable);
        List<Movie> movies = moviePage.getContent();

        List<RankingDto> rankingsForMovies = movieRankingService.getRankingsForMovies(movies);
        return sortingFactory.getStrategy(sortBy).sort(rankingsForMovies);
    }

    @Override
    public RankingDto getMovieRanking(long id) {
        Movie movie = getMovie(id);
        DigiKatResponse digiKatResponse;
        try {
            digiKatResponse = digiKatClient.getRanking(movie.getTitle());
        } catch (FeignException.NotFound ex) {
            log.warn("DigiKat returned 404 for movie: {}", movie.getTitle());
            throw new MovieNotFoundException("Movie not found with id: " + id);
        } catch (FeignException ex) {
            log.error("Error calling DigiKat for movie '{}': {}", movie.getTitle(), ex.getMessage());
            throw new RuntimeException("External service error", ex);
        }
        return digiKatRankingAdapter.adapt(movie, digiKatResponse);
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

    private Movie getMovie(Long id) {
        return movieRepo.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + id));
    }

    private int validPageSize(int pageSize) {
        return (pageSize < 1 || pageSize > pageMaxSize) ? pageMaxSize : pageSize;
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
        log.info("Saved movie '{}' with ID {}", movie.getTitle(), movie.getId());
        return movie.getId();
    }

}
