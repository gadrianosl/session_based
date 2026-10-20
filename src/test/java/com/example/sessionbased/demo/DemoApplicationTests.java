package com.example.sessionbased.demo;

import com.example.sessionbased.session_based.DemoApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
class DemoApplicationTests {
    @Autowired WebApplicationContext context;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void publicLoginAndProtectedHome() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
        mvc.perform(get("/login")).andExpect(status().isOk())
            .andExpect(content().string(containsString("name=\"_csrf\"")))
            .andExpect(content().string(containsString("Entrar no sistema")));
        mvc.perform(get("/css/auth.css")).andExpect(status().isOk());
        mvc.perform(get("/login?error")).andExpect(content().string(containsString("Usuário ou senha inválidos")));
        mvc.perform(get("/login?logout")).andExpect(content().string(containsString("Você saiu do sistema com sucesso")));
    }

    @Test
    void authenticationAndLogoutInvalidateSharedSession() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", "errada"))
            .andExpect(redirectedUrl("/login?error"));
        var result = mvc.perform(post("/login").with(csrf())
                .param("username", "admin").param("password", "123456"))
            .andExpect(redirectedUrl("/")).andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        mvc.perform(get("/").session(session)).andExpect(status().isOk())
            .andExpect(content().string(containsString("admin")));
        mvc.perform(get("/logout").session(session)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Confirmar saída")))
            .andExpect(content().string(containsString("name=\"_csrf\"")));
        assertFalse(session.isInvalid());
        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());
        assertFalse(session.isInvalid());
        mvc.perform(post("/logout").session(session).with(csrf()))
            .andExpect(redirectedUrl("/login?logout"))
            .andExpect(cookie().maxAge("JSESSIONID", 0));
        assertTrue(session.isInvalid());
        mvc.perform(get("/")).andExpect(status().is3xxRedirection());
    }
}
