package pl.skf.sws.component.sorting.impl;

import org.springframework.stereotype.Component;
import pl.skf.sws.component.sorting.MovieSortingStrategy;
import pl.skf.sws.model.RankingDto;

import java.util.Comparator;
import java.util.List;

@Component("ranking")
public class RankingSortingStrategy implements MovieSortingStrategy {

    @Override
    public List<RankingDto> sort(List<RankingDto> movies) {
        return movies.stream()
                .sorted(Comparator.comparing(RankingDto::getRanking).reversed())
                .toList();
    }

}
