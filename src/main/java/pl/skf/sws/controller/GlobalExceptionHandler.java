package pl.skf.sws.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.skf.sws.exception.EmptyFileException;
import pl.skf.sws.exception.FileStorageException;
import pl.skf.sws.exception.FileToHeavyException;
import pl.skf.sws.exception.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<String> handleEmptyFileException() {
        return ResponseEntity
                .badRequest()
                .body("The file has not been added");
    }

    @ExceptionHandler(FileToHeavyException.class)
    public ResponseEntity<String> handleFileToHeavyException() {
        return ResponseEntity
                .status(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)
                .body("File  to heavy, maximum size is 1GB");
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<String> handleFileStorageException() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to save the file");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("User Not Founded");
    }

}
