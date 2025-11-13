package ru.vensy.vkinfo.vk.service.impl;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.vensy.vkinfo.api.dto.VkRequest;
import ru.vensy.vkinfo.api.dto.VkResponse;
import ru.vensy.vkinfo.exception.NotFoundException;
import ru.vensy.vkinfo.exception.VkApiException;
import ru.vensy.vkinfo.vk.service.VkService;

@Service
public class VkServiceImpl implements VkService {
    private final ProducerTemplate producerTemplate;

    public VkServiceImpl(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @Override
    @Cacheable(value = "vkUserInfo", key = "#request.userId() + ':' + #request.groupId()")
    public VkResponse getVkUserInfo(String token, VkRequest request) {
        try {
            return producerTemplate.requestBodyAndHeader(
                    "direct:getVkUserInfo",
                    request,
                    "vk_service_token", token,
                    VkResponse.class
            );
        } catch (CamelExecutionException e) {
            throw unwrapCamelException(e);
        }
    }

    private RuntimeException unwrapCamelException(CamelExecutionException e) {
        Throwable cause = e.getCause();

        if (cause instanceof NotFoundException nfe) {
            return nfe;
        }
        if (cause instanceof VkApiException vae) {
            return vae;
        }
        return new RuntimeException(cause);
    }
}