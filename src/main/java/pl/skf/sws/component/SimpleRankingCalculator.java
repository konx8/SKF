package pl.skf.sws.component;

import org.springframework.stereotype.Component;
import pl.skf.sws.component.impl.RankingCalculator;
import pl.skf.sws.model.*;

@Component
public class SimpleRankingCalculator implements RankingCalculator {

    private static final long SMALL_MOVIE_SIZE_BYTES = 200 * 1024 * 1024;

    @Override
    public int calculateRanking(Movie movie, DigiKatResponse digiKatResponse) {
        if (isSmallMovie(movie)) {
            return 100;
        }

        int ranking = 0;
        ranking += polishProductionBonus(digiKatResponse);
        ranking += netflixPenalty(digiKatResponse);
        ranking += userRatingBonus(digiKatResponse);

        return ranking;
    }

    private boolean isSmallMovie(Movie movie) {
        return movie.getFileSize() != null && movie.getFileSize() < SMALL_MOVIE_SIZE_BYTES;
    }

    private int polishProductionBonus(DigiKatResponse response) {
        ProductionType type = response.getProductionType();
        if (type == ProductionType.PISF_POLISH || type == ProductionType.POLISH) {
            return 200;
        }
        return 0;
    }

    private int netflixPenalty(DigiKatResponse response) {
        if (response.getAvailabilityPlatforms().contains(AvailabilityPlatform.NETFLIX)) {
            return -50;
        }
        return 0;
    }

    private int userRatingBonus(DigiKatResponse response) {
        if (response.getUserRatingEnum() == UserRating.WYBITNY) {
            return 100;
        }
        return 0;
    }
}

