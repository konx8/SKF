package pl.skf.sws.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.skf.sws.model.Movie;

@Repository
public interface MovieRepo extends JpaRepository <Movie, Long> {
}
