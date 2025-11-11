package ru.vensy.vkinfo.auth.service;

import ru.vensy.vkinfo.api.dto.UserInfo;

public interface UserService {
    UserInfo getUserInfoByUsername(String username);
}