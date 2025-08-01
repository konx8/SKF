package pl.skf.sws.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.skf.sws.exception.*;
import pl.skf.sws.model.RankingDto;
import pl.skf.sws.service.MovieService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MovieControllerExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @Test
    void addMovie_shouldReturnCreatedId() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "movie.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy content".getBytes());

        MockMultipartFile movieDto = new MockMultipartFile("movie", "",
                "application/json", """
                {
                    "title": "Test Movie",
                    "releaseYear": 2023,
                    "fileSize": 123456789,
                    "director": "John Doe"
                }
                """.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile userId = new MockMultipartFile("userId", "",
                "application/json", "1".getBytes(StandardCharsets.UTF_8));

        when(movieService.saveMovie(any(), any(), anyLong())).thenReturn(10L);

        mockMvc.perform(multipart("/movies")
                        .file(file)
                        .file(movieDto)
                        .file(userId))
                .andExpect(status().isCreated())
                .andExpect(content().string("10"));
    }

    @Test
    void patchMovie_shouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/movies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getRanking_shouldReturnRankingDto() throws Exception {
        RankingDto rankingDto = new RankingDto();
        rankingDto.setTitle("Test Movie");
        rankingDto.setRanking(99);

        when(movieService.getMovieRanking(anyLong())).thenReturn(rankingDto);

        mockMvc.perform(get("/movies/1/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Movie"))
                .andExpect(jsonPath("$.ranking").value(99));
    }

    @Test
    void getAllMovies_withValidSort_shouldReturnList() throws Exception {
        RankingDto dto1 = new RankingDto();
        dto1.setTitle("Movie A");
        dto1.setRanking(80);

        RankingDto dto2 = new RankingDto();
        dto2.setTitle("Movie B");
        dto2.setRanking(90);

        when(movieService.getAllMoviesRankingSorted(eq("ranking"), anyInt(), anyInt()))
                .thenReturn(List.of(dto2, dto1));

        mockMvc.perform(get("/movies")
                        .param("sort", "ranking")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Movie B"))
                .andExpect(jsonPath("$[1].title").value("Movie A"));
    }

    @Test
    void getAllMovies_withInvalidSort_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/movies")
                        .param("sort", "invalidSort"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid sort type"));
    }

    @Test
    void downloadMovie_shouldReturnFileResource() throws Exception {
        var resource = new org.springframework.core.io.ByteArrayResource("file content".getBytes());
        var fileResource = new pl.skf.sws.model.MovieFileResource(resource, "movie.mp4");

        when(movieService.loadFileAsResource(anyLong())).thenReturn(fileResource);

        mockMvc.perform(get("/movies/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"movie.mp4\""))
                .andExpect(content().bytes("file content".getBytes()));
    }

    @Test
    void whenUserNotFoundException_thenReturns404() throws Exception {
        when(movieService.getMovieRanking(anyLong()))
                .thenThrow(new UserNotFoundException("User Not found"));

        mockMvc.perform(get("/movies/1/ranking"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User Not found"));
    }

    @Test
    void whenInvalidSortTypeException_thenReturns400() throws Exception {
        mockMvc.perform(get("/movies")
                        .param("sort", "notValidSort"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid sort type"));
    }

    @Test
    void whenEmptyFileException_thenReturnsBadRequest() throws Exception {
        when(movieService.saveMovie(any(), any(), anyLong()))
                .thenThrow(new EmptyFileException("The file has not been added"));

        MockMultipartFile file = new MockMultipartFile("file", "movie.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy content".getBytes());

        MockMultipartFile movieDto = new MockMultipartFile("movie", "",
                "application/json", """
            {
                "title": "Test Movie",
                "releaseYear": 2023,
                "fileSize": 123456789,
                "director": "John Doe"
            }
            """.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile userId = new MockMultipartFile("userId", "",
                "application/json", "1".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/movies")
                        .file(file)
                        .file(movieDto)
                        .file(userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The file has not been added"));
    }

    @Test
    void whenFileToHeavyException_thenReturnsRequestHeaderFieldsTooLarge() throws Exception {
        when(movieService.saveMovie(any(), any(), anyLong()))
                .thenThrow(new FileToHeavyException("File too heavy"));

        MockMultipartFile file = new MockMultipartFile("file", "movie.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy content".getBytes());

        MockMultipartFile movieDto = new MockMultipartFile("movie", "",
                "application/json", """
            {
                "title": "Test Movie",
                "releaseYear": 2023,
                "fileSize": 123456789,
                "director": "John Doe"
            }
            """.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile userId = new MockMultipartFile("userId", "",
                "application/json", "1".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/movies")
                        .file(file)
                        .file(movieDto)
                        .file(userId))
                .andExpect(status().isRequestHeaderFieldsTooLarge())
                .andExpect(content().string("File  to heavy, maximum size is 1GB"));
    }

    @Test
    void whenFileStorageException_thenReturnsInternalServerError() throws Exception {
        when(movieService.saveMovie(any(), any(), anyLong()))
                .thenThrow(new FileStorageException("Failed to save file"));

        MockMultipartFile file = new MockMultipartFile("file", "movie.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy content".getBytes());

        MockMultipartFile movieDto = new MockMultipartFile("movie", "",
                "application/json", """
            {
                "title": "Test Movie",
                "releaseYear": 2023,
                "fileSize": 123456789,
                "director": "John Doe"
            }
            """.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile userId = new MockMultipartFile("userId", "",
                "application/json", "1".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/movies")
                        .file(file)
                        .file(movieDto)
                        .file(userId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to save the file"));
    }

    @Test
    void whenMovieNotFoundException_thenReturnsNotFound() throws Exception {
        when(movieService.getMovieRanking(anyLong()))
                .thenThrow(new MovieNotFoundException("Movie not found"));

        mockMvc.perform(get("/movies/999/ranking"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Movie not found"));
    }

}
