package pl.skf.sws.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.skf.sws.adapter.DigiKatRankingAdapter;
import pl.skf.sws.feign.DigiKatClient;
import pl.skf.sws.model.DigiKatResponse;
import pl.skf.sws.model.Movie;
import pl.skf.sws.model.RankingDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieRankingServiceTest {

    @Mock
    private DigiKatClient digiKatClient;

    @Mock
    private DigiKatRankingAdapter digiKatRankingAdapter;

    private MovieRankingService service;

    @BeforeEach
    void setup() {
        service = new MovieRankingService(digiKatClient, digiKatRankingAdapter, 2);
        service.initThreadPool();
    }
    @AfterEach
    void teardown() {
        service.shutdown();
    }

    @Test
    void getRankingsForMovies_shouldReturnAdaptedRankings() {
        Movie movie1 = new Movie();
        movie1.setTitle("Movie1");

        Movie movie2 = new Movie();
        movie2.setTitle("Movie2");

        DigiKatResponse response1 = new DigiKatResponse();
        DigiKatResponse response2 = new DigiKatResponse();

        RankingDto ranking1 = new RankingDto();
        RankingDto ranking2 = new RankingDto();

        when(digiKatClient.getRanking("Movie1")).thenReturn(response1);
        when(digiKatClient.getRanking("Movie2")).thenReturn(response2);

        when(digiKatRankingAdapter.adapt(movie1, response1)).thenReturn(ranking1);
        when(digiKatRankingAdapter.adapt(movie2, response2)).thenReturn(ranking2);

        List<RankingDto> rankings = service.getRankingsForMovies(List.of(movie1, movie2));

        assertNotNull(rankings);
        assertEquals(2, rankings.size());
        assertTrue(rankings.contains(ranking1));
        assertTrue(rankings.contains(ranking2));

        verify(digiKatClient, times(1)).getRanking("Movie1");
        verify(digiKatClient, times(1)).getRanking("Movie2");
        verify(digiKatRankingAdapter, times(1)).adapt(movie1, response1);
        verify(digiKatRankingAdapter, times(1)).adapt(movie2, response2);
    }

    @Test
    void getRankingsForMovies_shouldSkipNullRankingsOnExceptionFromFeignClient() {
        Movie movie1 = new Movie();
        movie1.setTitle("Movie1");

        Movie movie2 = new Movie();
        movie2.setTitle("Movie2");

        DigiKatResponse response2 = new DigiKatResponse();
        RankingDto ranking2 = new RankingDto();

        when(digiKatClient.getRanking("Movie1")).thenThrow(new RuntimeException("Feign error"));
        when(digiKatClient.getRanking("Movie2")).thenReturn(response2);
        when(digiKatRankingAdapter.adapt(movie2, response2)).thenReturn(ranking2);

        List<RankingDto> rankings = service.getRankingsForMovies(List.of(movie1, movie2));

        assertNotNull(rankings);
        assertEquals(1, rankings.size());
        assertTrue(rankings.contains(ranking2));

        verify(digiKatClient, times(1)).getRanking("Movie1");
        verify(digiKatClient, times(1)).getRanking("Movie2");
        verify(digiKatRankingAdapter, times(1)).adapt(movie2, response2);
    }

    @Test
    void getRankingsForMovies_shouldSkipNullRankingsOnExceptionFromAdapter() {
        Movie movie1 = new Movie();
        movie1.setTitle("Movie1");

        DigiKatResponse response1 = new DigiKatResponse();

        when(digiKatClient.getRanking("Movie1")).thenReturn(response1);
        when(digiKatRankingAdapter.adapt(movie1, response1)).thenThrow(new RuntimeException("Adapter error"));

        List<RankingDto> rankings = service.getRankingsForMovies(List.of(movie1));

        assertNotNull(rankings);
        assertTrue(rankings.isEmpty());

        verify(digiKatClient, times(1)).getRanking("Movie1");
        verify(digiKatRankingAdapter, times(1)).adapt(movie1, response1);
    }


}