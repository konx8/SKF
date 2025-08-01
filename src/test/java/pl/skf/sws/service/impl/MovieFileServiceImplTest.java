package pl.skf.sws.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import pl.skf.sws.exception.EmptyFileException;
import pl.skf.sws.exception.FileStorageException;
import pl.skf.sws.exception.FileToHeavyException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class MovieFileServiceImplTest {

    MovieFileServiceImpl service;

    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        service = new MovieFileServiceImpl();
        service.maxSizeOfFile = 1_000_000L;

        tempDir = Files.createTempDirectory("upload_test");
        service.uploadDir = tempDir.toString();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        }
    }

    @Test
    void loadFileAsResource_shouldReturnResource_whenFileExistsAndReadable() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "test content");

        Resource resource = service.loadFileAsResource(testFile.toString());

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals(testFile.toAbsolutePath().toString(), resource.getFile().getAbsolutePath());
    }

    @Test
    void loadFileAsResource_shouldThrowFileNotFoundException_whenFileNotExists() {
        String fakePath = tempDir.resolve("nonexistent.txt").toString();

        FileNotFoundException ex = assertThrows(FileNotFoundException.class,
                () -> service.loadFileAsResource(fakePath));

        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void loadFileAsResource_shouldThrowRuntimeException_whenMalformedUrl() {
        String badPath = "http://invalid-url:://file";

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.loadFileAsResource(badPath));

        assertTrue(ex.getMessage().contains("Error loading file"));
    }

    @Test
    void validateFile_shouldThrowEmptyFileException_whenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        EmptyFileException ex = assertThrows(EmptyFileException.class,
                () -> service.validateFile(emptyFile));

        assertEquals("The file has not been added.", ex.getMessage());
    }

    @Test
    void validateFile_shouldThrowFileToHeavyException_whenFileTooBig() {
        byte[] bigContent = new byte[service.maxSizeOfFile.intValue() + 1];
        MockMultipartFile bigFile = new MockMultipartFile("file", bigContent);

        FileToHeavyException ex = assertThrows(FileToHeavyException.class,
                () -> service.validateFile(bigFile));

        assertTrue(ex.getMessage().contains("maximum size"));
    }

    @Test
    void validateFile_shouldNotThrowException_whenFileIsValid() {
        MockMultipartFile validFile = new MockMultipartFile("file", "data".getBytes());

        assertDoesNotThrow(() -> service.validateFile(validFile));
    }

    @Test
    void storeFile_shouldSaveFileAndReturnPath() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "content".getBytes());

        String storedFilePath = service.storeFile(file);

        Path storedPath = Paths.get(storedFilePath);

        assertTrue(Files.exists(storedPath));
        assertTrue(storedFilePath.startsWith(tempDir.toString()));
        assertTrue(storedFilePath.endsWith(file.getOriginalFilename()));
        assertEquals("content", Files.readString(storedPath));
    }

    @Test
    void storeFile_shouldThrowFileStorageException_whenIOException() {
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "content".getBytes());

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenCallRealMethod();
            filesMock.when(() -> Files.write(any(Path.class), any(byte[].class))).thenThrow(new IOException("Disk error"));

            FileStorageException ex = assertThrows(FileStorageException.class, () -> service.storeFile(file));
            assertTrue(ex.getMessage().contains("Failed to save the file"));
        }
    }


    @Test
    void deleteFileQuietly_shouldDeleteExistingFileWithoutException() throws IOException {
        Path fileToDelete = tempDir.resolve("to_delete.txt");
        Files.writeString(fileToDelete, "delete me");

        assertTrue(Files.exists(fileToDelete));

        assertDoesNotThrow(() -> service.deleteFileQuietly(fileToDelete.toString()));

        assertFalse(Files.exists(fileToDelete));
    }

    @Test
    void deleteFileQuietly_shouldNotThrowException_whenFileDoesNotExist() {
        String nonExistentPath = tempDir.resolve("does_not_exist.txt").toString();

        assertDoesNotThrow(() -> service.deleteFileQuietly(nonExistentPath));
    }

}