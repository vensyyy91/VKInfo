package ru.vensy.vkinfo.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VkRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long groupId
) {
}