package com.procredito.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ProCrédito API",
                version = "1.0",
                description = "Sistema de Gestión de Microcréditos para Microempresas",
                contact = @Contact(name = "ProEmpresa", email = "soporte@proempresa.com")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingrese el token JWT (sin la palabra 'Bearer'). El token se obtiene del endpoint /api/auth/login"
)
public class OpenApiConfig {
    // Esta clase solo contiene anotaciones, no necesita métodos.
}