package pl.skf.sws.adapter;

import org.springframework.stereotype.Component;
import pl.skf.sws.component.impl.RankingCalculator;
import pl.skf.sws.model.*;

import java.util.Collections;
import java.util.Optional;

@Component
public class DigiKatRankingAdapter {

    private final RankingCalculator rankingCalculator;

    public DigiKatRankingAdapter(RankingCalculator rankingCalculator) {
        this.rankingCalculator = rankingCalculator;
    }

    public RankingDto adapt(Movie movie, DigiKatResponse digiKatResponse) {
        RankingDto rankingDto = new RankingDto();

        rankingDto.setTitle(movie.getTitle());
        rankingDto.setReleaseYear(movie.getReleaseYear());

        rankingDto.setRanking(rankingCalculator.calculateRanking(movie, digiKatResponse));

        rankingDto.setProduction(Optional.ofNullable(digiKatResponse.getProductionType())
                .map(ProductionType::getCode)
                .orElse(ProductionType.UNKNOWN.getCode()));

        rankingDto.setAvailability(Optional.ofNullable(digiKatResponse.getAvailabilityPlatforms())
                .orElse(Collections.emptyList())
                .stream()
                .map(Enum::name)
                .toList());

        rankingDto.setUserRating(Optional.ofNullable(digiKatResponse.getUserRatingEnum())
                .map(Enum::name)
                .orElse(UserRating.UNKNOWN.name()));

        return rankingDto;
    }

}
