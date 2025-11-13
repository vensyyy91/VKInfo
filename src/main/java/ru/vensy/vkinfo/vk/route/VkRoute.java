package ru.vensy.vkinfo.vk.route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;
import ru.vensy.vkinfo.api.dto.VkResponse;
import ru.vensy.vkinfo.exception.NotFoundException;
import ru.vensy.vkinfo.exception.VkApiException;
import ru.vensy.vkinfo.vk.model.VkApiResponse;
import ru.vensy.vkinfo.vk.model.VkUserInfo;

import java.util.List;

@Component
public class VkRoute extends RouteBuilder {
    private static final String VK_API_URL = "https://api.vk.ru/method/";
    private static final String VK_API_VERSION = "5.199";

    private final ObjectMapper mapper;

    public VkRoute(ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }

    @Override
    public void configure() {

        from("direct:getVkUserInfo")
                .routeId("vk-info-route")
                .log("Received VK request: userId=${body.userId}, groupId=${body.groupId}")

                .setProperty("userId", simple("${body.userId}"))
                .setProperty("groupId", simple("${body.groupId}"))
                .setProperty("vkServiceToken", simple("${header.vk_service_token}"))
                .removeHeader("vk_service_token")

                .setHeader("CamelHttpMethod", constant("GET"))

                .toD(VK_API_URL + "users.get" +
                        "?user_ids=${exchangeProperty.userId}" +
                        "&access_token=${exchangeProperty.vkServiceToken}" +
                        "&fields=nickname" +
                        "&v=" + VK_API_VERSION)
                .unmarshal().json(JsonLibrary.Jackson, VkApiResponse.class)

                .process(exchange -> {
                    VkApiResponse response = exchange.getIn().getBody(VkApiResponse.class);

                    if (response.error() != null) {
                        throw new VkApiException(response.error().errorCode(), response.error().errorMsg());
                    }

                    List<VkUserInfo> users = mapper.convertValue(
                            response.response(),
                            new TypeReference<>() {}
                    );

                    if (users.isEmpty()) {
                        throw new NotFoundException(
                                "Пользователь VK с id=" + exchange.getProperty("userId") + " не найден."
                        );
                    }
                    VkUserInfo userInfo = users.getFirst();
                    exchange.setProperty("firstName", userInfo.firstName());
                    exchange.setProperty("lastName", userInfo.lastName());
                    exchange.setProperty("middleName", userInfo.nickname());
                })

                .toD(VK_API_URL + "groups.isMember" +
                        "?group_id=${exchangeProperty.groupId}" +
                        "&user_id=${exchangeProperty.userId}" +
                        "&access_token=${exchangeProperty.vkServiceToken}" +
                        "&v=" + VK_API_VERSION)
                .unmarshal().json(JsonLibrary.Jackson, VkApiResponse.class)

                .process(exchange -> {
                    VkApiResponse response = exchange.getIn().getBody(VkApiResponse.class);

                    if (response.error() != null) {
                        throw new VkApiException(response.error().errorCode(), response.error().errorMsg());
                    }

                    Integer memberResponse = mapper.convertValue(response.response(), Integer.class);
                    boolean isMember = memberResponse == 1;

                    VkResponse result = new VkResponse(
                            (String) exchange.getProperty("firstName"),
                            (String) exchange.getProperty("lastName"),
                            (String) exchange.getProperty("middleName"),
                            isMember
                    );

                    exchange.getIn().setBody(result);
                })
                .log("VK response ready: ${body}");
    }
}