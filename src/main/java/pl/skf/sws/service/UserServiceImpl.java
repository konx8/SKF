package pl.skf.sws.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.skf.sws.exception.UserNotFoundException;
import pl.skf.sws.model.User;
import pl.skf.sws.repo.UserRepo;
import pl.skf.sws.service.impl.UserService;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepo userRepo;

    @Override
    public User getUserById(Long userId){
        return userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for id: " + userId));
    }
}
