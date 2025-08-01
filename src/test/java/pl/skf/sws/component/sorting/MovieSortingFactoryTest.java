package pl.skf.sws.component.sorting;

import org.junit.jupiter.api.Test;
import pl.skf.sws.component.sorting.impl.RankingSortingStrategy;
import pl.skf.sws.component.sorting.impl.SizeSortingStrategy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovieSortingFactoryTest {

    @Test
    void getStrategy_shouldReturnCorrectStrategyByKey() {
        RankingSortingStrategy rankingStrategy = new RankingSortingStrategy();
        SizeSortingStrategy sizeStrategy = new SizeSortingStrategy();

        MovieSortingFactory factory = new MovieSortingFactory(List.of(rankingStrategy, sizeStrategy));

        MovieSortingStrategy strat1 = factory.getStrategy("ranking");
        MovieSortingStrategy strat2 = factory.getStrategy("size");

        assertInstanceOf(RankingSortingStrategy.class, strat1);
        assertInstanceOf(SizeSortingStrategy.class, strat2);
    }

    @Test
    void getStrategy_shouldReturnDefaultRankingStrategyForUnknownKey() {
        RankingSortingStrategy rankingStrategy = new RankingSortingStrategy();
        SizeSortingStrategy sizeStrategy = new SizeSortingStrategy();

        MovieSortingFactory factory = new MovieSortingFactory(List.of(rankingStrategy, sizeStrategy));

        MovieSortingStrategy strat = factory.getStrategy("unknown-key");

        assertInstanceOf(RankingSortingStrategy.class, strat);
    }

}