package pl.skf.sws.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.skf.sws.model.User;
import pl.skf.sws.repo.UserRepo;

import pl.skf.sws.exception.UserNotFoundException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setLogin("janek123");
        mockUser.setEmail("janek@example.com");

        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("janek123", result.getLogin());
        assertEquals("janek@example.com", result.getEmail());

        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void getUserById_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        Long userId = 99L;

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(userId));

        assertTrue(exception.getMessage().contains("User not found for id: " + userId));

        verify(userRepo, times(1)).findById(userId);
    }

}