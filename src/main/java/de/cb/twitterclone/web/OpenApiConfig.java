package de.cb.twitterclone.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME = "keycloak";

    @Bean
    OpenAPI openApi(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {
        var authorizationCode = new OAuthFlow()
                .authorizationUrl(issuer + "/protocol/openid-connect/auth")
                .tokenUrl(issuer + "/protocol/openid-connect/token");
        var keycloak = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().authorizationCode(authorizationCode));
        return new OpenAPI()
                .info(new Info().title("Twitter Clone API").version("v1"))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME, keycloak));
    }
}
