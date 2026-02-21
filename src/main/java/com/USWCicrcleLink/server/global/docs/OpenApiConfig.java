package com.USWCicrcleLink.server.global.docs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("USW Circle Link Server API").version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addServersItem(new Server().url("https://api.donggurami.net"))
                .addServersItem(new Server().url("http://localhost:8080"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME));
    }

    /**
     * ApiResponse<Void> 반환 메서드의 응답 스키마를 { "message": "string" } 로 고정.
     * SpringDoc이 Void를 string으로 해석하는 문제를 근본적으로 해결합니다.
     */
    @Bean
    public OperationCustomizer apiResponseVoidOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (isApiResponseVoid(handlerMethod)) {
                Schema<?> messageOnlySchema = new ObjectSchema()
                        .addProperty("message", new StringSchema());

                operation.getResponses().values().forEach(response -> {
                    if (response.getContent() != null) {
                        response.getContent().values().forEach(mediaType -> mediaType.setSchema(messageOnlySchema));
                    }
                });
            }
            return operation;
        };
    }

    /**
     * Components에 남아있는 ApiResponseVoid 스키마에서도 data 필드 제거 (보조)
     */
    @Bean
    public OpenApiCustomizer apiResponseVoidSchemaCustomizer() {
        return openApi -> {
            @SuppressWarnings("rawtypes")
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            if (schemas != null) {
                schemas.forEach((name, schema) -> {
                    if (name.startsWith("ApiResponse") && name.toLowerCase().contains("void")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Schema<?>> properties = schema.getProperties();
                        if (properties != null) {
                            properties.remove("data");
                        }
                    }
                });
            }
        };
    }

    /**
     * 컨트롤러 메서드의 반환 타입이 ResponseEntity<ApiResponse<Void>>인지 확인
     */
    private boolean isApiResponseVoid(org.springframework.web.method.HandlerMethod handlerMethod) {
        Type returnType = handlerMethod.getMethod().getGenericReturnType();

        // ResponseEntity<ApiResponse<Void>>
        if (returnType instanceof ParameterizedType pt) {
            Type[] outerArgs = pt.getActualTypeArguments();
            if (outerArgs.length == 1 && outerArgs[0] instanceof ParameterizedType innerPt) {
                String rawName = innerPt.getRawType().getTypeName();
                if (rawName.endsWith("ApiResponse")) {
                    Type[] innerArgs = innerPt.getActualTypeArguments();
                    return innerArgs.length == 1 && innerArgs[0] == Void.class;
                }
            }
        }

        return false;
    }
}
