package pl.skf.sws.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.skf.sws.component.ranking.RankingCalculator;
import pl.skf.sws.model.*;
import pl.skf.sws.model.enums.ProductionType;
import pl.skf.sws.model.enums.UserRating;

import java.util.List;

class DigiKatRankingAdapterTest {

    private RankingCalculator rankingCalculator;
    private DigiKatRankingAdapter adapter;

    @BeforeEach
    void setUp() {
        rankingCalculator = mock(RankingCalculator.class);
        adapter = new DigiKatRankingAdapter(rankingCalculator);
    }

    @Test
    void adapt_shouldMapAllFieldsCorrectly_whenAllValuesPresent() {
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setReleaseYear(2020);
        movie.setFileSize(12345L);

        DigiKatResponse response = new DigiKatResponse();
        response.setProduction(ProductionType.POLISH.getCode());
        response.setAvailability(List.of("NETFLIX", "HBO"));
        response.setUserRating("WYBITNY");

        when(rankingCalculator.calculateRanking(movie, response)).thenReturn(10);

        RankingDto dto = adapter.adapt(movie, response);

        assertEquals(movie.getTitle(), dto.getTitle());
        assertEquals(movie.getReleaseYear(), dto.getReleaseYear());
        assertEquals(10, dto.getRanking());
        assertEquals(ProductionType.POLISH.getCode(), dto.getProduction());
        assertEquals(List.of("NETFLIX", "HBO"), dto.getAvailability());
        assertEquals("WYBITNY", dto.getUserRating());
        assertEquals(movie.getFileSize(), dto.getFileSize());
    }

    @Test
    void adapt_shouldUseDefaults_whenResponseFieldsAreNull() {
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setReleaseYear(2020);
        movie.setFileSize(12345L);

        DigiKatResponse response = new DigiKatResponse();
        response.setProduction(null);
        response.setAvailability(null);
        response.setUserRating(null);

        when(rankingCalculator.calculateRanking(movie, response)).thenReturn(50);

        RankingDto dto = adapter.adapt(movie, response);

        assertEquals(ProductionType.UNKNOWN.getCode(), dto.getProduction());
        assertEquals(List.of(), dto.getAvailability());
        assertEquals(UserRating.UNKNOWN.name(), dto.getUserRating());
    }

}