package pl.skf.sws.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.skf.sws.exception.UserNotFoundException;
import pl.skf.sws.model.User;
import pl.skf.sws.repo.UserRepo;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepo userRepo;

    public User getUserById(Long userId){
        return userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found for id: " + userId));
    }
}
