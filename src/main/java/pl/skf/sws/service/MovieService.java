package pl.skf.sws.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.skf.sws.exception.EmptyFileException;
import pl.skf.sws.exception.FileStorageException;
import pl.skf.sws.exception.FileToHeavyException;
import pl.skf.sws.model.Movie;
import pl.skf.sws.model.MovieDto;
import pl.skf.sws.model.User;
import pl.skf.sws.repo.MovieRepo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MovieService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private MovieRepo movieRepo;
    private UserService userService;

    public MovieService(MovieRepo movieRepo, UserService userService) {
        this.movieRepo = movieRepo;
        this.userService = userService;
    }

    public List<Movie> allMovie() {
        return movieRepo.findAll();
    }

    @Transactional
    public Long saveMovie(MovieDto movieDto, MultipartFile file, Long userId) {
        validateFile(file);
        String savedFilePath = storeFile(file);
        User user = userService.getUserById(userId);
        Movie movie = assigningDataToMovie(movieDto, savedFilePath, file, user);
        return save(movie, savedFilePath);
    }

    private Movie assigningDataToMovie(MovieDto movieDto, String savedFilePath, MultipartFile file, User user) {
        Movie movie = new Movie();
        movie.setTitle(movieDto.getTitle());
        movie.setDirector(movieDto.getDirector());
        movie.setReleaseYear(movieDto.getYear());
        movie.setFileSize(file.getSize());
        movie.setFilePath(savedFilePath);
        movie.setUser(user);
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
        return movie.getId();
    }


    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException("The file has not been added.");
        }
        if (file.getSize() > 1024L * 1024 * 1024) {
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
