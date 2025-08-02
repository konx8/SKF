package pl.skf.sws.adapter;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import pl.skf.sws.component.ranking.RankingCalculator;
import pl.skf.sws.model.*;
import pl.skf.sws.model.enums.ProductionType;
import pl.skf.sws.model.enums.UserRating;

import java.util.Collections;
import java.util.Optional;

@Component
@AllArgsConstructor
public class DigiKatRankingAdapter {

    private final RankingCalculator rankingCalculator;

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

        rankingDto.setFileSize(movie.getFileSize());
        return rankingDto;
    }

}
