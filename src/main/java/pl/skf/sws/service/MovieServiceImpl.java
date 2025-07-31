package pl.skf.sws.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.skf.sws.adapter.DigiKatRankingAdapter;
import pl.skf.sws.exception.EmptyFileException;
import pl.skf.sws.exception.FileStorageException;
import pl.skf.sws.exception.FileToHeavyException;
import pl.skf.sws.exception.MovieNotFoundException;
import pl.skf.sws.feign.DigiKatClient;
import pl.skf.sws.model.*;
import pl.skf.sws.repo.MovieRepo;
import pl.skf.sws.service.impl.MovieService;
import pl.skf.sws.service.impl.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MovieServiceImpl implements MovieService {

    private final static int FILE_MAX_SIZE = 1073741824; // 1Gb

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final MovieRepo movieRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final DigiKatClient digiKatClient;
    private final DigiKatRankingAdapter digiKatRankingAdapter;


    public MovieServiceImpl(MovieRepo movieRepo, UserService userService,
                            ModelMapper modelMapper, DigiKatClient digiKatClient,
                            DigiKatRankingAdapter digiKatRankingAdapter) {
        this.movieRepo = movieRepo;
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.digiKatClient = digiKatClient;
        this.digiKatRankingAdapter = digiKatRankingAdapter;
    }

    @Transactional
    @Override
    public Long saveMovie(MovieDto movieDto, MultipartFile file, Long userId) {
        validateFile(file);
        String savedFilePath = storeFile(file);
        User user = userService.getUserById(userId);
        Movie movie = assigningDataToMovie(movieDto, savedFilePath, file, user);
        movie.setId(null);
        return save(movie, savedFilePath);
    }

    @Override
    public List<Movie> allMovie() {
        return movieRepo.findAll();
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
        try{
            digiKatResponse = digiKatClient.getRanking(movie.getTitle());
        } catch (Exception ex){
            throw new MovieNotFoundException("Movie not found with id: " + id);
        }

        return digiKatRankingAdapter.adapt(movie, digiKatResponse);
    }

    private Movie getMovie(Long id) {
        return movieRepo.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + id));
    }

    private Movie assigningDataToMovie(MovieDto movieDto, String savedFilePath, MultipartFile file, User user) {
        Movie movie  = modelMapper.map(movieDto, Movie.class);

        movie.setFileSize(file.getSize());
        movie.setFilePath(savedFilePath);
        movie.setUser(user);
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedAt(LocalDateTime.now());
        return movie;
    }

    private Long save(Movie movie, String filePath) {
        try {
            movie = movieRepo.save(movie);
        } catch (Exception e) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException ex) {
                log.error("Failed to delete the file after an error writing to the database", ex);
            }
            throw e;
        }
        log.info("Saved on DB Successfully ");
        return movie.getId();
    }


    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException("The file has not been added.");
        }
        if (file.getSize() > FILE_MAX_SIZE) {
            throw new FileToHeavyException("File  to heavy, maximum size is 1GB");
        }
        log.info("File correct");
    }

    private String storeFile(MultipartFile file) {
        try {
            String dir = uploadDir;
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path filePath = Paths.get(dir, filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            log.info("File successfully saved");
            return filePath.toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to save the file", e);
        }
    }

}
