package com.example.sessionbased.session_based;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/")
    public String paginaInicial() {
        return "home"; 
    }

    // Alinhado para listar todos os usuários na mesma tela
    @GetMapping("/usuarios/novo")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("usuarioForm", new Usuario()); // Objeto vazio para o formulário
        model.addAttribute("listaUsuarios", usuarioRepository.findAll()); // Lista preenchida do banco
        return "cadastro";
    }

    // Recebe os dados e decide se Cria um novo ou Edita um existente
    @PostMapping("/usuarios/cadastrar")
    public String cadastrarUsuario(Usuario usuario) {
        if (usuario.getId() != null) {
            // Cenário de EDICAO: Busca o registro atual do banco
            Optional<Usuario> usuarioAntigoOpt = usuarioRepository.findById(usuario.getId());
            if (usuarioAntigoOpt.isPresent()) {
                Usuario usuarioAntigo = usuarioAntigoOpt.get();
                // Se o adm não digitou uma nova senha, mantém a senha antiga do banco
                if (usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
                    usuario.setSenha(usuarioAntigo.getSenha());
                } else {
                    // Se digitou uma nova senha, criptografa a nova senha
                    usuario.setSenha(encoder.encode(usuario.getSenha()));
                }
            }
        } else {
            // Cenário de NOVO CADASTRO: Criptografia obrigatória
            usuario.setSenha(encoder.encode(usuario.getSenha()));
        }

        usuarioRepository.save(usuario);
        return "redirect:/usuarios/novo?sucesso";
    }

    // Rota acionada ao clicar em "Editar" na tabela
    @GetMapping("/usuarios/editar/{id}")
    public String carregarDadosParaEdicao(@PathVariable("id") Long id, Model model) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuarioForm", usuario.get()); // Preenche o formulário com os dados do usuário encontrado
            model.addAttribute("listaUsuarios", usuarioRepository.findAll()); // Mantém a lista atualizada abaixo
            return "cadastro";
        }
        return "redirect:/usuarios/novo?erro";
    }

    // Rota acionada ao clicar em "Excluir" na tabela
    @GetMapping("/usuarios/excluir/{id}")
    public String excluirUsuario(@PathVariable("id") Long id) {
        // Evita que o administrador delete a si próprio por acidente (opcional, baseado no id 1 do admin)
        if (id != 1) {
            usuarioRepository.deleteById(id);
        }
        return "redirect:/usuarios/novo?excluido";
    }
}
