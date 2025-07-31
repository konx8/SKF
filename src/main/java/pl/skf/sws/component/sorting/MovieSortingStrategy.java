package pl.skf.sws.component.sorting;

import pl.skf.sws.model.RankingDto;

import java.util.List;

public interface MovieSortingStrategy {

    List<RankingDto> sort(List<RankingDto> movies);

}
