package ru.vensy.vkinfo.api.dto;

public record UserCookies(UserInfo user, String accessCookie, String refreshCookie) {
}