package pl.skf.sws.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.skf.sws.adapter.DigiKatRankingAdapter;
import pl.skf.sws.feign.DigiKatClient;
import pl.skf.sws.model.DigiKatResponse;
import pl.skf.sws.model.Movie;
import pl.skf.sws.model.RankingDto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class MovieRankingService {

    @Value("${thread-pool.size}")
    private final int threadPoolSize;

    private final DigiKatClient digiKatClient;
    private final DigiKatRankingAdapter digiKatRankingAdapter;
    private ExecutorService threadPool;

    private final Map<String, CompletableFuture<RankingDto>> cache = new ConcurrentHashMap<>();

    public MovieRankingService(DigiKatClient digiKatClient, DigiKatRankingAdapter digiKatRankingAdapter,
                               @Value("${thread-pool.size}") int threadPoolSize) {
        this.digiKatClient = digiKatClient;
        this.digiKatRankingAdapter = digiKatRankingAdapter;
        this.threadPoolSize = threadPoolSize;
    }

    @PostConstruct
    public void initThreadPool() {
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        log.info("Thread pool initialized with size: {}", threadPoolSize);
    }

    @PreDestroy
    public void shutdown() {
        threadPool.shutdown();
    }

    public List<RankingDto> getRankingsForMovies(List<Movie> movies) {
        log.info("Getting Rankings for Movies Started");

        List<CompletableFuture<RankingDto>> futures = movies.stream()
                .map(this::getRankingForMovie)
                .toList();

        return futures.stream()
                .map(future -> {
                    try {
                        return future.join();
                    } catch (Exception e) {
                        log.error("Error joining future: {}", e.getMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private CompletableFuture<RankingDto> getRankingForMovie(Movie movie) {
        return cache.computeIfAbsent(movie.getTitle(), title ->
                CompletableFuture.supplyAsync(() -> fetchRanking(movie), threadPool));
    }

    private RankingDto fetchRanking(Movie movie) {
        try {
            DigiKatResponse response = digiKatClient.getRanking(movie.getTitle());
            return digiKatRankingAdapter.adapt(movie, response);
        } catch (Exception e) {
            log.error("Error retrieving ranking for {}: {}", movie.getTitle(), e.getMessage(), e);
            return null;
        }
    }
}
