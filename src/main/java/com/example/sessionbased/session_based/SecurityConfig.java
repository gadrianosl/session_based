package com.example.sessionbased.session_based;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Configura as regras de acesso (Quem pode ver o quê)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated() // Bloqueia tudo, exigindo login para qualquer tela
            )
            .formLogin(Customizer.withDefaults()) // Ativa a tela de login padrão
            .logout(Customizer.withDefaults());   // Ativa a rota de logout padrão
            
        return http.build();
    }

    // 2. Define o decodificador de senhas (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 3. Conecta o Spring Security ao nosso banco de dados real
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository repository) {
        return username -> {
            // Busca o usuário no banco pelo username digitado na tela de login
            Usuario usuario = repository.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

            // Retorna um objeto de usuário que o Spring Security entende
            return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getSenha()) // Aqui já vai estar criptografada com BCrypt
                .roles("USER")
                .build();
        };
    }
}
