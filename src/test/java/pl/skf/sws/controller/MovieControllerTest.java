package pl.skf.sws.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.skf.sws.model.MovieFileResource;
import pl.skf.sws.model.MoviePatchDto;
import pl.skf.sws.model.RankingDto;
import pl.skf.sws.service.MovieService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @Test
    void addMovie_shouldReturnCreatedId() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "movie.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy content".getBytes());

        MockMultipartFile movieDto = new MockMultipartFile("movie", "", "application/json",
                """
                {
                    "title": "Test Movie",
                    "releaseYear": 2023,
                    "fileSize": 123456789,
                    "director": "John Smith"
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
        doNothing().when(movieService).updateMovie(anyLong(), any(MoviePatchDto.class));

        String patchJson = """
                {
                    "title": "Updated Title"
                }
                """;

        mockMvc.perform(patch("/movies/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void getRanking_shouldReturnRankingDto() throws Exception {
        RankingDto rankingDto = new RankingDto();
        rankingDto.setTitle("Test Movie");
        rankingDto.setRanking(123);
        rankingDto.setReleaseYear(2023);

        when(movieService.getMovieRanking(5L)).thenReturn(rankingDto);

        mockMvc.perform(get("/movies/{id}/ranking", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Movie"))
                .andExpect(jsonPath("$.ranking").value(123))
                .andExpect(jsonPath("$.releaseYear").value(2023));
    }

    @Test
    void getAllMovies_withValidSort_shouldReturnList() throws Exception {
        RankingDto dto1 = new RankingDto();
        dto1.setTitle("Movie1");
        dto1.setRanking(100);

        RankingDto dto2 = new RankingDto();
        dto2.setTitle("Movie2");
        dto2.setRanking(90);

        when(movieService.getAllMoviesRankingSorted("ranking", 0, 5))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/movies")
                        .param("sort", "ranking")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Movie1"))
                .andExpect(jsonPath("$[1].ranking").value(90));
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
        byte[] data = "file content".getBytes(StandardCharsets.UTF_8);
        Resource resource = new ByteArrayResource(data);

        MovieFileResource movieFileResource = new MovieFileResource(resource, "movie.mp4");

        when(movieService.loadFileAsResource(7L)).thenReturn(movieFileResource);

        mockMvc.perform(get("/movies/{id}/download", 7L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"movie.mp4\""))
                .andExpect(content().bytes(data));
    }

}