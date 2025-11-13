package ru.vensy.vkinfo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.vensy.vkinfo.config.TestSecurityConfig;
import ru.vensy.vkinfo.api.controller.VkController;
import ru.vensy.vkinfo.api.dto.VkRequest;
import ru.vensy.vkinfo.api.dto.VkResponse;
import ru.vensy.vkinfo.auth.security.AuthTokenFilter;
import ru.vensy.vkinfo.exception.NotFoundException;
import ru.vensy.vkinfo.vk.service.VkService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = VkController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = ru.vensy.vkinfo.auth.security.SecurityConfig.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = AuthTokenFilter.class
                )
        }
)
@Import(TestSecurityConfig.class)
public class VkControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private VkService vkService;
    private VkRequest request;

    @BeforeEach
    void setUp() {
        request = new VkRequest(123L, 12345L);
    }

    @Test
    void getVkUserInfo_shouldReturnUserInfo_whenRequestIsValid() throws Exception {
        VkResponse expected = new VkResponse(
                "Solovyev",
                "Vasily",
                "Dmitrievich",
                true
        );

        when(vkService.getVkUserInfo("valid_token", request))
                .thenReturn(expected);

        mockMvc.perform(post("/api/vk/info")
                        .header("vk_service_token", "valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.last_name").value("Solovyev"))
                .andExpect(jsonPath("$.first_name").value("Vasily"))
                .andExpect(jsonPath("$.middle_name").value("Dmitrievich"))
                .andExpect(jsonPath("$.member").value(true));
    }

    @Test
    void getVkUserInfo_shouldReturnBadRequest_whenTokenMissing() throws Exception {
        mockMvc.perform(post("/api/vk/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getVkUserInfo_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        when(vkService.getVkUserInfo("valid_token", request))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(post("/api/vk/info")
                        .header("vk_service_token", "valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}