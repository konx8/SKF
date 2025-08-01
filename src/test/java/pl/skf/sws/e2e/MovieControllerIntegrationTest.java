package pl.skf.sws.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.skf.sws.model.User;
import pl.skf.sws.repo.MovieRepo;
import pl.skf.sws.repo.UserRepo;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MovieControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    void setup() {
        userRepo.deleteAll();

        User user = new User();
        user.setLogin("testuser");
        user.setEmail("test@email.com");

        userRepo.save(user);
    }

    @Test
    void addMovie_shouldSaveMovieAndReturnId() throws Exception {
        String movieDtoJson = """
        {
          "title": "Test Movie",
          "director": "John Doe",
          "releaseYear": 2024
        }
        """;

        MockPart movieDtoPart = new MockPart("movie", movieDtoJson.getBytes(StandardCharsets.UTF_8));
        movieDtoPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        MockPart userIdPart = new MockPart("userId", "1".getBytes(StandardCharsets.UTF_8));
        userIdPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "movie.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "dummy video content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/movies")
                        .part(movieDtoPart)
                        .part(userIdPart)
                        .file(filePart))
                .andExpect(status().isCreated());
        assertEquals(1, movieRepo.findAll().size());
    }




}
