package com.escola.inactivearchive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        // 1. Liberar arquivos estáticos (CSS, Imagens, JS) para a tela de login não ficar feia
                        .requestMatchers("/css/**", "/img/**", "/js/**").permitAll()
                        // 2. Qualquer outra requisição precisa de autenticação
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        // 3. Página de login personalizada (vamos criar agora)
                        .loginPage("/login")
                        // 4. Se o login der certo, vai para a lista
                        .defaultSuccessUrl("/alunos", true)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // 5. Definindo o usuário ADMIN em memória
        // AVISO: Em produção real, isso viria do Banco de Dados, mas para uso interno isso resolve.
        UserDetails admin = User.withDefaultPasswordEncoder() // "withDefault" é apenas para testes/estudos
                .username("admin")
                .password("123456") // <--- A SENHA É ESSA AQUI
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
