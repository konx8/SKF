package pl.skf.sws.component.ranking.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.skf.sws.model.DigiKatResponse;
import pl.skf.sws.model.Movie;
import pl.skf.sws.model.enums.AvailabilityPlatform;
import pl.skf.sws.model.enums.ProductionType;
import pl.skf.sws.model.enums.UserRating;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class SimpleRankingCalculatorTest {

    private SimpleRankingCalculator calculator;

    @BeforeEach
    void setup() {
        calculator = new SimpleRankingCalculator();
    }

    private Movie createMovie(Long fileSize) {
        Movie movie = new Movie();
        movie.setFileSize(fileSize);
        return movie;
    }

    private DigiKatResponse createResponse(ProductionType production, List<AvailabilityPlatform> platforms, UserRating userRating) {
        DigiKatResponse response = new DigiKatResponse();
        response.setProduction(production.getCode());
        response.setAvailability(platforms.stream().map(Enum::name).toList());
        response.setUserRating(userRating.name());
        return response;
    }

    @Test
    void shouldReturn100ForSmallMovie() {
        Movie smallMovie = createMovie(100 * 1024 * 1024L);
        DigiKatResponse response = createResponse(ProductionType.UNKNOWN, List.of(), UserRating.UNKNOWN);

        int ranking = calculator.calculateRanking(smallMovie, response);

        assertEquals(100, ranking);
    }

    @Test
    void shouldCalculateRankingWithBonusesAndPenalties() {
        Movie bigMovie = createMovie(500 * 1024 * 1024L);

        DigiKatResponse response = createResponse(ProductionType.POLISH, List.of(AvailabilityPlatform.NETFLIX), UserRating.WYBITNY);

        int ranking = calculator.calculateRanking(bigMovie, response);

        assertEquals(250, ranking);
    }

    @Test
    void shouldReturnZeroIfNoBonusesOrPenalties() {
        Movie bigMovie = createMovie(500 * 1024 * 1024L);

        DigiKatResponse response = createResponse(ProductionType.UNKNOWN, List.of(), UserRating.UNKNOWN);

        int ranking = calculator.calculateRanking(bigMovie, response);

        assertEquals(0, ranking);
    }

    @Test
    void netflixPenaltyShouldApplyOnlyIfNetflixInPlatforms() {
        Movie bigMovie = createMovie(300 * 1024 * 1024L);

        DigiKatResponse responseWithNetflix = createResponse(ProductionType.UNKNOWN, List.of(AvailabilityPlatform.NETFLIX), UserRating.UNKNOWN);
        DigiKatResponse responseWithoutNetflix = createResponse(ProductionType.UNKNOWN, List.of(AvailabilityPlatform.HBO), UserRating.UNKNOWN);

        assertEquals(-50, calculator.calculateRanking(bigMovie, responseWithNetflix));
        assertEquals(0, calculator.calculateRanking(bigMovie, responseWithoutNetflix));
    }

    @Test
    void polishProductionBonusShouldApplyForPolishProductions() {
        Movie bigMovie = createMovie(400 * 1024 * 1024L);

        DigiKatResponse pisfResponse = createResponse(ProductionType.PISF_POLISH, List.of(), UserRating.UNKNOWN);
        DigiKatResponse polishResponse = createResponse(ProductionType.POLISH, List.of(), UserRating.UNKNOWN);
        DigiKatResponse unknownResponse = createResponse(ProductionType.UNKNOWN, List.of(), UserRating.UNKNOWN);

        assertEquals(200, calculator.calculateRanking(bigMovie, pisfResponse));
        assertEquals(200, calculator.calculateRanking(bigMovie, polishResponse));
        assertEquals(0, calculator.calculateRanking(bigMovie, unknownResponse));
    }

    @Test
    void userRatingBonusShouldApplyForWybitny() {
        Movie bigMovie = createMovie(400 * 1024 * 1024L);

        DigiKatResponse wybitnyResponse = createResponse(ProductionType.UNKNOWN, List.of(), UserRating.WYBITNY);
        DigiKatResponse unknownResponse = createResponse(ProductionType.UNKNOWN, List.of(), UserRating.UNKNOWN);

        assertEquals(100, calculator.calculateRanking(bigMovie, wybitnyResponse));
        assertEquals(0, calculator.calculateRanking(bigMovie, unknownResponse));
    }

}