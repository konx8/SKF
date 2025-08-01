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
    private int threadPoolSize;

    private final DigiKatClient digiKatClient;
    private final DigiKatRankingAdapter digiKatRankingAdapter;
    private ExecutorService threadPool;

    public MovieRankingService(DigiKatClient digiKatClient, DigiKatRankingAdapter digiKatRankingAdapter) {
        this.digiKatClient = digiKatClient;
        this.digiKatRankingAdapter = digiKatRankingAdapter;
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
        Map<String, CompletableFuture<RankingDto>> cache = new ConcurrentHashMap<>();

        List<CompletableFuture<RankingDto>> futureList = movies.stream()
                .map(movie -> cache.computeIfAbsent(movie.getTitle(), title ->
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                DigiKatResponse response = digiKatClient.getRanking(title);
                                return digiKatRankingAdapter.adapt(movie, response);
                            } catch (Exception e) {
                                log.error("Error retrieving ranking for {}: {}", title, e.getMessage(), e);
                                return null;
                            }

                        }, threadPool)
                ))
                .toList();

        return futureList.stream()
                .map(future -> {
                    try {
                        return future.join();
                    } catch (Exception e) {
                        log.error("Error combining future: {}", e.getMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

}
