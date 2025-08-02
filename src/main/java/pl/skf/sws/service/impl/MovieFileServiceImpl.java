package pl.skf.sws.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.skf.sws.exception.EmptyFileException;
import pl.skf.sws.exception.FileStorageException;
import pl.skf.sws.exception.FileToHeavyException;
import pl.skf.sws.service.MovieFileService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class MovieFileServiceImpl implements MovieFileService {

    @Value("${file.upload-dir}")
    String uploadDir;

    @Value("${file.max-size}")
    Long maxSizeOfFile;

    public Resource loadFileAsResource(String filePath) throws FileNotFoundException {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found or not readable: " + filePath);
            }
        } catch (MalformedURLException | InvalidPathException e) {
            throw new RuntimeException("Error loading file: " + filePath, e);
        }
    }


    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException("The file has not been added.");
        }
        if (file.getSize() > maxSizeOfFile) {
            throw new FileToHeavyException("File  to heavy, maximum size is 1GB");
        }
        log.info("File correct");
    }

    public String storeFile(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            log.info("File successfully saved");
            return filePath.toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to save the file", e);
        }
    }

    public void deleteFileQuietly(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException ex) {
            log.error("Failed to delete file: {}", filePath, ex);
        }
    }

}
