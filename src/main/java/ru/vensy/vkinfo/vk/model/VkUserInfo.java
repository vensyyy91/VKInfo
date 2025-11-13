package ru.vensy.vkinfo.vk.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VkUserInfo(
        Long id,
        String firstName,
        String lastName,
        String nickname,
        Boolean canAccessClosed,
        Boolean isClosed
) {
}