package ru.vensy.vkinfo.vk.service;

import ru.vensy.vkinfo.api.dto.VkRequest;
import ru.vensy.vkinfo.api.dto.VkResponse;

public interface VkService {
    VkResponse getVkUserInfo(String token, VkRequest request);
}