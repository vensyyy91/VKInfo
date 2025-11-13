package ru.vensy.vkinfo.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.vensy.vkinfo.api.dto.VkRequest;
import ru.vensy.vkinfo.api.dto.VkResponse;
import ru.vensy.vkinfo.vk.service.VkService;

@RestController
@RequestMapping("/api/vk")
@Validated
public class VkController {
    private final VkService vkService;

    private final Logger log =  LoggerFactory.getLogger(VkController.class);

    public VkController(VkService vkService) {
        this.vkService = vkService;
    }

    @PostMapping("/info")
    public ResponseEntity<VkResponse> getVkUserInfo(@RequestHeader("vk_service_token") @NotBlank String vkServiceToken,
                                                    @RequestBody @Valid VkRequest request) {
        log.info("POST /api/vk/info ; body: {}", request);
        VkResponse response = vkService.getVkUserInfo(vkServiceToken, request);

        return ResponseEntity.ok(response);
    }
}