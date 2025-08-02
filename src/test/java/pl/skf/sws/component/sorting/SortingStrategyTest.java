package pl.skf.sws.component.sorting;

import org.junit.jupiter.api.Test;
import pl.skf.sws.component.sorting.impl.RankingSortingStrategy;
import pl.skf.sws.component.sorting.impl.SizeSortingStrategy;
import pl.skf.sws.model.RankingDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SortingStrategyTest {

    private final RankingSortingStrategy rankingStrategy = new RankingSortingStrategy();
    private final SizeSortingStrategy sizeStrategy = new SizeSortingStrategy();

    @Test
    void rankingStrategy_shouldSortByRankingDescending() {
        RankingDto dto1 = new RankingDto();
        dto1.setRanking(70);
        dto1.setFileSize(500L);

        RankingDto dto2 = new RankingDto();
        dto2.setRanking(90);
        dto2.setFileSize(300L);

        RankingDto dto3 = new RankingDto();
        dto3.setRanking(50);
        dto3.setFileSize(1000L);

        List<RankingDto> sorted = rankingStrategy.sort(List.of(dto1, dto2, dto3));

        assertEquals(List.of(dto2, dto1, dto3), sorted);
    }

    @Test
    void sizeStrategy_shouldSortByFileSizeDescending() {
        RankingDto dto1 = new RankingDto();
        dto1.setRanking(70);
        dto1.setFileSize(500L);

        RankingDto dto2 = new RankingDto();
        dto2.setRanking(90);
        dto2.setFileSize(300L);

        RankingDto dto3 = new RankingDto();
        dto3.setRanking(50);
        dto3.setFileSize(1000L);

        List<RankingDto> sorted = sizeStrategy.sort(List.of(dto1, dto2, dto3));

        assertEquals(List.of(dto3, dto1, dto2), sorted);
    }

}
