package pl.skf.sws.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.skf.sws.exception.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<String> handleEmptyFileException() {
        log.error("The file has not been added");
        return ResponseEntity
                .badRequest()
                .body("The file has not been added");
    }

    @ExceptionHandler(FileToHeavyException.class)
    public ResponseEntity<String> handleFileToHeavyException() {
        log.error("File  to heavy, maximum size is 1GB");
        return ResponseEntity
                .status(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)
                .body("File  to heavy, maximum size is 1GB");
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<String> handleFileStorageException() {
        log.error("Failed to save the file");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to save the file");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException() {
        log.error("User Not found");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("User Not found");
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<String> handleMovieNotFoundException() {
        log.error("Movie not found");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Movie not found");
    }

}
