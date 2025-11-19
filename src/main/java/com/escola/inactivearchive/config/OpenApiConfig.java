package com.escola.inactivearchive.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestão de Arquivos - Escola Quilombola")
                        .description("Sistema para gestão de alunos inativos, digitalização de pastas e relatórios.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jefferson Medeiros da Silva")
                                .url("https://linkedin.com/in/jefferson-morpheus")
                                .email("jeffersonmedeirosdasilva@gmail.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
