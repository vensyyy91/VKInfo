package ru.vensy.vkinfo.auth.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vensy.vkinfo.api.dto.UserInfo;
import ru.vensy.vkinfo.auth.entity.User;
import ru.vensy.vkinfo.auth.repository.UserRepository;
import ru.vensy.vkinfo.auth.service.UserService;
import ru.vensy.vkinfo.exception.NotFoundException;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfo getUserInfoByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Пользователь " + username + " не найден."));

        log.info("Found user: {}", user);

        return new UserInfo(user.getId(), user.getUsername());
    }
}