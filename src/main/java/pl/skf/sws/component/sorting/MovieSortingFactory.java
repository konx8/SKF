package pl.skf.sws.component.sorting;

import org.springframework.stereotype.Component;
import pl.skf.sws.component.sorting.impl.RankingSortingStrategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MovieSortingFactory {

    private final Map<String, MovieSortingStrategy> strategyMap;

    public MovieSortingFactory(List<MovieSortingStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(s ->
                        s.getClass().getAnnotation(Component.class).value(), Function.identity()));
    }

    public MovieSortingStrategy getStrategy(String key) {
        return strategyMap.getOrDefault(key, new RankingSortingStrategy());
    }

}
