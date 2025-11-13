package ru.vensy.vkinfo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vensy.vkinfo.api.dto.UserInfo;
import ru.vensy.vkinfo.auth.entity.User;
import ru.vensy.vkinfo.auth.repository.UserRepository;
import ru.vensy.vkinfo.auth.service.impl.UserServiceImpl;
import ru.vensy.vkinfo.exception.NotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("qwerty");
    }

    @Test
    void getUserInfoByUsername_shouldReturnUserInfo_whenUserExists() {
        UserInfo expected = new UserInfo(user.getId(), user.getUsername());

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        UserInfo actual = userService.getUserInfoByUsername(user.getUsername());

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(userRepository, times(1)).findByUsername(user.getUsername());
    }

    @Test
    void getUserInfoByUsername_shouldThrowNotFound_whenUserNotExists() {
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        NotFoundException nfe = assertThrows(
                NotFoundException.class,
                () -> userService.getUserInfoByUsername("unknown")
        );

        assertEquals("Пользователь unknown не найден.", nfe.getMessage());
        verify(userRepository, times(1)).findByUsername("unknown");
    }
}