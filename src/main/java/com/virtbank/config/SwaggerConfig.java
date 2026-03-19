package com.virtbank.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * OpenAPI 3 / Swagger configuration.
 * Accessible at /swagger-ui.html (dev profile only in production).
 */
@Configuration
@Profile("!prod")          // disable Swagger in production profile
public class SwaggerConfig {

    @Bean
    public OpenAPI virtbankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VIRTBANK API")
                        .version("1.0.0")
                        .description("VIRTBANK is a full-stack Banking Management System with three panels — "
                                + "Admin, Customer, and Business. It provides user management, account operations, "
                                + "transaction processing, loan lifecycle management, payroll processing, invoicing, "
                                + "KYC compliance, support tickets, real-time notifications via SSE, and analytics — "
                                + "all secured with JWT-based authentication and role-based access control.")
                        .contact(new Contact().name("VIRTBANK Team").email("admin@virtbank.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token. Obtain it via POST /api/auth/login.")))
                .tags(List.of(
                        new Tag().name("Auth").description("Authentication and registration endpoints"),
                        new Tag().name("Admin").description("Admin panel endpoints — user management, accounts, loans, audit"),
                        new Tag().name("Customer").description("Customer panel endpoints — transactions, loans, profile, support"),
                        new Tag().name("Business").description("Business panel endpoints — payroll, invoices, analytics, team")
                ));
    }
}
