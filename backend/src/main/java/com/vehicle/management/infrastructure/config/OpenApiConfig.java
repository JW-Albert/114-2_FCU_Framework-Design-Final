package com.vehicle.management.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI 設定。
 *
 * <p>啟動後可於 {@code /swagger-ui.html} 瀏覽互動式 API 文件，
 * 並透過右上角 Authorize 輸入 JWT（Bearer Token）測試受保護端點。</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI vehicleManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("公司車輛管理系統 API")
                        .description("企業內部車輛借用管理平台 REST API 文件。"
                                + "受保護端點需於 Authorize 輸入 JWT。")
                        .version("v2"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
