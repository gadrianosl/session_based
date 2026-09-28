// ... Ajuste sua classe DemoApplication para ficar assim:
package com.example.sessionbased.session_based;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UsuarioRepository repository) {
		return args -> {
			// Cria um usuário mestre inicial salvo diretamente no banco com senha criptografada
			if(repository.count() == 0) {
				Usuario admin = new Usuario();
				admin.setNome("Administrador");
				admin.setEmail("admin@gmail.com");
				admin.setUsername("admin");
				admin.setSenha(new BCryptPasswordEncoder().encode("123456"));
				repository.save(admin);
			}
		};
	}
}
