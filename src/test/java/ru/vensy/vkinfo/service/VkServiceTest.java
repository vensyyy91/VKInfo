package ru.vensy.vkinfo.service;


import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vensy.vkinfo.api.dto.VkRequest;
import ru.vensy.vkinfo.api.dto.VkResponse;
import ru.vensy.vkinfo.exception.NotFoundException;
import ru.vensy.vkinfo.exception.VkApiException;
import ru.vensy.vkinfo.vk.service.impl.VkServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VkServiceTest {
    @Mock
    private ProducerTemplate producerTemplate;
    @InjectMocks
    private VkServiceImpl vkService;
    private VkRequest request;

    @BeforeEach
    void setUp() {
        request = new VkRequest(123L, 12345L);
    }

    @Test
    void getVkUserInfo_shouldReturnValidResponse_whenCamelReturnsOk() {
        VkResponse expected = new VkResponse(
                "Solovyev",
                "Vasily",
                "Dmitrievich",
                true
        );

        when(producerTemplate.requestBodyAndHeader(
                eq("direct:getVkUserInfo"),
                eq(request),
                eq("vk_service_token"),
                eq("valid_token"),
                eq(VkResponse.class)
        )).thenReturn(expected);

        VkResponse actual = vkService.getVkUserInfo("valid_token", request);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(producerTemplate, times(1))
                .requestBodyAndHeader(anyString(), any(), anyString(), any(), eq(VkResponse.class));
    }

    @Test
    void getVkUserInfo_shouldThrowNotFoundException_whenCamelThrowsNotFoundWrapped() {
        NotFoundException nfe = new NotFoundException("Пользователь VK с id=" + request.userId() + " не найден.");
        CamelExecutionException cee = new CamelExecutionException("wrapped", null, nfe);

        when(producerTemplate.requestBodyAndHeader(
                eq("direct:getVkUserInfo"),
                any(),
                eq("vk_service_token"),
                eq("valid_token"),
                eq(VkResponse.class)
        )).thenThrow(cee);

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> vkService.getVkUserInfo("valid_token", request)
        );

        assertEquals(nfe.getMessage(), thrown.getMessage());
    }

    @Test
    void getVkUserInfo_shouldThrowVkApiException_whenCamelThrowsVkApiWrapped() {
        VkApiException vae = new VkApiException(5, "User authorization failed: invalid access_token (4).");
        CamelExecutionException cee = new CamelExecutionException("wrapped", null, vae);

        when(producerTemplate.requestBodyAndHeader(
                eq("direct:getVkUserInfo"),
                any(),
                eq("vk_service_token"),
                eq("invalid_token"),
                eq(VkResponse.class)
        )).thenThrow(cee);

        VkApiException thrown = assertThrows(
                VkApiException.class,
                () -> vkService.getVkUserInfo("invalid_token", request)
        );

        assertEquals(5, thrown.getErrorCode());
        assertEquals(vae.getMessage(), thrown.getMessage());
    }

    @Test
    void getVkUserInfo_shouldWrapIntoRuntimeException_whenCamelThrowsUnknownException() {
        RuntimeException inner = new RuntimeException("Что-то пошло не так.");
        CamelExecutionException cee = new CamelExecutionException("wrapped", null, inner);

        when(producerTemplate.requestBodyAndHeader(
                eq("direct:getVkUserInfo"),
                any(),
                eq("vk_service_token"),
                eq("valid_token"),
                eq(VkResponse.class)
        )).thenThrow(cee);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> vkService.getVkUserInfo("valid_token", request)
        );

        assertTrue(thrown.getMessage().contains("Что-то пошло не так."));
    }
}