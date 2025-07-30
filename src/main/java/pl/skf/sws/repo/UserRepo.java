package pl.skf.sws.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.skf.sws.model.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
}
