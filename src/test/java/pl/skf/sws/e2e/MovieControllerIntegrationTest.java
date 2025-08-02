package pl.skf.sws.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.skf.sws.feign.DigiKatClient;
import pl.skf.sws.model.*;
import pl.skf.sws.repo.MovieRepo;
import pl.skf.sws.repo.UserRepo;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MovieControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private DigiKatClient digiKatClient;

    private Long savedUserId;

    @BeforeAll
    void setupUser() {
        movieRepo.deleteAll();
        if (userRepo.count() == 0) {
            User user = new User();
            user.setLogin("testuser");
            user.setEmail("test@email.com");
            User savedUser = userRepo.save(user);
            savedUserId = savedUser.getId();
        }
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

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.multipart("/movies")
                        .part(movieDtoPart)
                        .part(userIdPart)
                        .file(filePart))
                .andExpect(status().isCreated());
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        assertEquals("Test Movie", movieRepo.findById(Long.valueOf(contentAsString)).get().getTitle());
        assertEquals("John Doe", movieRepo.findById(Long.valueOf(contentAsString)).get().getDirector());
    }

    @Test
    void patchMovie_shouldUpdateMovieDetails() throws Exception {
        Movie savedMovie = initTestMovie("Title");

        String patchJson = """
                {
                    "title": "Updated Title",
                    "director": "Updated Director",
                    "releaseYear": 2024
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.patch("/movies/{id}", savedMovie.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isNoContent());

        Movie updatedMovie = movieRepo.findById(savedMovie.getId()).orElseThrow();
        assertEquals("Updated Title", updatedMovie.getTitle());
        assertEquals("Updated Director", updatedMovie.getDirector());
        assertEquals(2024, updatedMovie.getReleaseYear());
    }

    @Test
    void shouldReturnRankingWithMockedDigiKatClient() throws Exception {
        initTestMovie("Title");
        DigiKatResponse fakeResponse = new DigiKatResponse();
        fakeResponse.setTitle("Title");
        fakeResponse.setProduction(1);
        fakeResponse.setAvailability(List.of("HBO"));
        fakeResponse.setUserRating("dobry");
        fakeResponse.setLastUpdate("2025-08-02T15:30:00");

        when(digiKatClient.getRanking(anyString())).thenReturn(fakeResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/movies/1/ranking")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranking").value(100));
    }

    @Test
    void getAllMovies_shouldReturnPagedMovies() throws Exception {
        initTestMovie("Inception");
        initTestMovie("Interstellar");

        when(digiKatClient.getRanking("Inception")).thenReturn(
                createDigiKatResponse("Inception", 1, List.of("Netflix"), "mierny", "2024-08-01T10:00:00")
        );
        when(digiKatClient.getRanking("Interstellar")).thenReturn(
                createDigiKatResponse("Interstellar", 1, List.of("HBO"), "wybitny", "2024-08-01T11:00:00")
        );

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/movies")
                        .param("sort", "ranking")
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        List<RankingDto> rankingList = objectMapper.readValue(jsonResponse,
                new TypeReference<List<RankingDto>>() {});

        assertEquals(2, rankingList.size());
        assertEquals("Inception", rankingList.get(0).getTitle());
        assertEquals("Interstellar", rankingList.get(1).getTitle());
    }

    private DigiKatResponse createDigiKatResponse(String title, Integer production,
                                                  List<String> availability, String userRating,
                                                  String lastUpdate) {
        DigiKatResponse response = new DigiKatResponse();
        response.setTitle(title);
        response.setProduction(production);
        response.setAvailability(availability);
        response.setUserRating(userRating);
        response.setLastUpdate(lastUpdate);
        return response;
    }


    @Test
    void downloadMovie_shouldReturnFileAsAttachment() throws Exception {
        String filename = "movie.mp4";
        byte[] fileContent = "dummy file content".getBytes(StandardCharsets.UTF_8);

        Path filePath = Paths.get("tmp/testfiles/" + filename);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, fileContent);

        Movie movie = initTestMovie("Joker");
        movie.setFilePath(filePath.toString());
        movie.setFileSize((long) fileContent.length);
        Movie savedMovie = movieRepo.save(movie);

        mockMvc.perform(MockMvcRequestBuilders.get("/movies/{id}/download", savedMovie.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(fileContent));

        Files.deleteIfExists(filePath);
    }

    private Movie initTestMovie(String title) {
        Movie movieToSave = new Movie();
        movieToSave.setTitle(title);
        movieToSave.setDirector("Director");
        movieToSave.setReleaseYear(2000);
        movieToSave.setFilePath("/test/123test.mp4");
        movieToSave.setFileSize(10000L);
        return movieRepo.save(movieToSave);
    }


}
