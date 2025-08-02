package pl.skf.sws.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

public interface MovieFileService {

    String storeFile(MultipartFile file);

    void validateFile(MultipartFile file);

    void deleteFileQuietly(String filePath);

    Resource loadFileAsResource(String filePath) throws FileNotFoundException;

}
