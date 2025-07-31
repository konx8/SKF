package pl.skf.sws.service;

import org.springframework.web.multipart.MultipartFile;

public interface MovieFileService {

    String storeFile(MultipartFile file);

    void validateFile(MultipartFile file);

    void deleteFileQuietly(String filePath);

}
