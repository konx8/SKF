package pl.skf.sws.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import pl.skf.sws.model.Movie;

@Repository
public interface MovieRepo extends JpaRepository <Movie, Long> {

    @NonNull
    Page<Movie> findAll(@NonNull Pageable pageable);

}
