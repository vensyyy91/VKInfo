package ru.vensy.vkinfo.vk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VkApiResponse(Object response, VkApiError error) {
}