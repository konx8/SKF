package pl.skf.sws.service.impl;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;
import pl.skf.sws.adapter.DigiKatRankingAdapter;
import pl.skf.sws.component.sorting.MovieSortingFactory;
import pl.skf.sws.exception.MovieNotFoundException;
import pl.skf.sws.feign.DigiKatClient;
import pl.skf.sws.model.*;
import pl.skf.sws.repo.MovieRepo;
import pl.skf.sws.service.MovieFileService;
import pl.skf.sws.service.MovieRankingService;
import pl.skf.sws.service.UserService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceImplTest {

    @Mock
    private MovieRepo movieRepo;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private DigiKatClient digiKatClient;

    @Mock
    private DigiKatRankingAdapter digiKatRankingAdapter;

    @Mock
    private MovieFileService movieFileService;

    @Mock
    private MovieSortingFactory sortingFactory;

    @Mock
    private MovieRankingService movieRankingService;

    @InjectMocks
    private MovieServiceImpl movieService;

    private final int PAGE_MAX_SIZE = 50;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        movieService.pageMaxSize = PAGE_MAX_SIZE;
        movieService.pageDefaultSize = 10;
    }

    @Test
    void loadFileAsResource_shouldReturnMovieFileResource_whenMovieFound() throws Exception {
        Long movieId = 1L;
        String filePath = "/path/to/movie.mp4";
        Movie movie = new Movie();
        movie.setFilePath(filePath);
        Resource resourceMock = mock(Resource.class);

        when(movieRepo.findById(movieId)).thenReturn(Optional.of(movie));
        when(movieFileService.loadFileAsResource(filePath)).thenReturn(resourceMock);

        MovieFileResource result = movieService.loadFileAsResource(movieId);

        assertNotNull(result);
        assertEquals(resourceMock, result.resource());
        assertEquals(Paths.get(filePath).getFileName().toString(), result.filename());

        verify(movieRepo).findById(movieId);
        verify(movieFileService).loadFileAsResource(filePath);
    }

    @Test
    void loadFileAsResource_shouldThrowMovieNotFoundException_whenMovieNotFound() {
        Long movieId = 1L;
        when(movieRepo.findById(movieId)).thenReturn(Optional.empty());

        assertThrows(MovieNotFoundException.class, () -> movieService.loadFileAsResource(movieId));

        verify(movieRepo).findById(movieId);
        verifyNoInteractions(movieFileService);
    }

    @Test
    void saveMovie_shouldValidateAndSaveMovie_thenReturnId() {
        MovieDto movieDto = new MovieDto();
        MultipartFile file = mock(MultipartFile.class);
        Long userId = 1L;
        String savedFilePath = "/saved/path/movie.mp4";
        User user = new User();
        Movie movieMapped = new Movie();
        movieMapped.setId(null);
        movieMapped.setTitle("Test movie");

        when(file.getSize()).thenReturn(123L);
        when(movieFileService.storeFile(file)).thenReturn(savedFilePath);
        when(userService.getUserById(userId)).thenReturn(user);
        when(modelMapper.map(movieDto, Movie.class)).thenReturn(movieMapped);
        doNothing().when(movieFileService).validateFile(file);
        when(movieRepo.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie m = invocation.getArgument(0);
            m.setId(10L);
            return m;
        });

        Long savedId = movieService.saveMovie(movieDto, file, userId);

        assertEquals(10L, savedId);
        verify(movieFileService).validateFile(file);
        verify(movieFileService).storeFile(file);
        verify(userService).getUserById(userId);
        verify(movieRepo).save(any(Movie.class));
    }

    @Test
    void saveMovie_shouldRollbackFile_whenSaveThrowsException() {
        MovieDto movieDto = new MovieDto();
        MultipartFile file = mock(MultipartFile.class);
        Long userId = 1L;
        String savedFilePath = "/saved/path/movie.mp4";
        User user = new User();
        Movie movieMapped = new Movie();

        when(movieFileService.storeFile(file)).thenReturn(savedFilePath);
        when(userService.getUserById(userId)).thenReturn(user);
        when(modelMapper.map(movieDto, Movie.class)).thenReturn(movieMapped);
        doNothing().when(movieFileService).validateFile(file);

        when(movieRepo.save(any(Movie.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> movieService.saveMovie(movieDto, file, userId));

        verify(movieFileService).deleteFileQuietly(savedFilePath);
    }

    @Test
    void getAllMoviesRankingSorted_shouldReturnSortedRankings() {
        String sortBy = "ranking";
        int page = 1;
        int size = 10;

        Movie movie1 = new Movie();
        Movie movie2 = new Movie();
        List<Movie> movieList = List.of(movie1, movie2);

        RankingDto ranking1 = new RankingDto();
        RankingDto ranking2 = new RankingDto();
        List<RankingDto> rankings = List.of(ranking1, ranking2);

        Page<Movie> moviePage = new PageImpl<>(movieList);

        when(movieRepo.findAll(any(Pageable.class))).thenReturn(moviePage);
        when(movieRankingService.getRankingsForMovies(movieList)).thenReturn(rankings);
        when(sortingFactory.getStrategy(sortBy)).thenReturn(r -> r);

        List<RankingDto> result = movieService.getAllMoviesRankingSorted(sortBy, page, size);

        assertEquals(rankings, result);
        verify(movieRepo).findAll(any(Pageable.class));
        verify(movieRankingService).getRankingsForMovies(movieList);
        verify(sortingFactory).getStrategy(sortBy);
    }

    @Test
    void getAllMoviesRankingSorted_shouldCapPageSizeAtMax() {
        String sortBy = "ranking";
        int page = 0;
        int size = PAGE_MAX_SIZE + 100;

        when(movieRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());
        when(movieRankingService.getRankingsForMovies(any())).thenReturn(Collections.emptyList());
        when(sortingFactory.getStrategy(sortBy)).thenReturn(r -> r);

        movieService.getAllMoviesRankingSorted(sortBy, page, size);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(movieRepo).findAll(captor.capture());

        Pageable pageableUsed = captor.getValue();
        assertEquals(PAGE_MAX_SIZE, pageableUsed.getPageSize());
    }

    @Test
    void getAllMoviesRankingSorted_shouldCorrectNegativePageToZero() {
        String sortBy = "ranking";
        int page = -5;
        int size = 10;

        when(movieRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());
        when(movieRankingService.getRankingsForMovies(any())).thenReturn(Collections.emptyList());
        when(sortingFactory.getStrategy(sortBy)).thenReturn(r -> r);

        movieService.getAllMoviesRankingSorted(sortBy, page, size);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(movieRepo).findAll(captor.capture());

        Pageable pageableUsed = captor.getValue();
        assertEquals(0, pageableUsed.getPageNumber());
    }

    @Test
    void getMovieRanking_shouldReturnRanking_whenDigiKatClientSucceeds() {
        long movieId = 1L;
        Movie movie = new Movie();
        movie.setTitle("Test movie");
        DigiKatResponse response = new DigiKatResponse();
        RankingDto rankingDto = new RankingDto();

        when(movieRepo.findById(movieId)).thenReturn(Optional.of(movie));
        when(digiKatClient.getRanking(movie.getTitle())).thenReturn(response);
        when(digiKatRankingAdapter.adapt(movie, response)).thenReturn(rankingDto);

        RankingDto result = movieService.getMovieRanking(movieId);

        assertEquals(rankingDto, result);
        verify(digiKatClient).getRanking(movie.getTitle());
        verify(digiKatRankingAdapter).adapt(movie, response);
    }

    @Test
    void getMovieRanking_shouldThrowMovieNotFoundException_whenFeign404() {
        long movieId = 1L;
        Movie movie = new Movie();
        movie.setTitle("Test movie");

        when(movieRepo.findById(movieId)).thenReturn(Optional.of(movie));
        when(digiKatClient.getRanking(movie.getTitle()))
                .thenThrow(mockFeignNotFound());

        assertThrows(MovieNotFoundException.class, () -> movieService.getMovieRanking(movieId));
    }

    @Test
    void getMovieRanking_shouldThrowRuntimeException_whenFeignOtherError() {
        long movieId = 1L;
        Movie movie = new Movie();
        movie.setTitle("Test movie");

        when(movieRepo.findById(movieId)).thenReturn(Optional.of(movie));
        when(digiKatClient.getRanking(movie.getTitle()))
                .thenThrow(mockFeignGeneric());

        assertThrows(RuntimeException.class, () -> movieService.getMovieRanking(movieId));
    }

    private FeignException.NotFound mockFeignNotFound() {
        return (FeignException.NotFound) FeignException.errorStatus("Not Found",
                Response.builder()
                        .status(404)
                        .request(Request.create(
                                Request.HttpMethod.GET,
                                "/test",
                                new HashMap<>(),
                                new byte[0],
                                StandardCharsets.UTF_8))
                        .build());
    }

    private FeignException.InternalServerError mockFeignGeneric() {
        return (FeignException.InternalServerError) FeignException.errorStatus("Error",
                Response.builder()
                        .status(500)
                        .request(Request.create(
                                Request.HttpMethod.GET,
                                "/test",
                                new HashMap<>(),
                                new byte[0],
                                StandardCharsets.UTF_8))
                        .build());
    }

    @Test
    void updateMovie_shouldMapAndSaveMovie() {
        Long movieId = 1L;
        MoviePatchDto patchDto = new MoviePatchDto();
        Movie movie = new Movie();

        when(movieRepo.findById(movieId)).thenReturn(Optional.of(movie));
        doNothing().when(modelMapper).map(patchDto, movie);
        when(movieRepo.save(movie)).thenReturn(movie);

        movieService.updateMovie(movieId, patchDto);

        verify(modelMapper).map(patchDto, movie);
        assertNotNull(movie.getUpdatedAt());
        verify(movieRepo).save(movie);
    }

    @Test
    void updateMovie_shouldThrowMovieNotFoundException_whenMovieNotFound() {
        Long movieId = 1L;
        MoviePatchDto patchDto = new MoviePatchDto();

        when(movieRepo.findById(movieId)).thenReturn(Optional.empty());

        assertThrows(MovieNotFoundException.class, () -> movieService.updateMovie(movieId, patchDto));
    }
}
