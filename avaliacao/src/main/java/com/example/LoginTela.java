package com.example;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginTela extends JFrame {
    private JTextField usuarioField;
    private JPasswordField senhaField;
    private JButton loginButton;
    private JButton cadastroButton;

    public LoginTela() {
        super("Login");

        // Configurações da tela
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(500, 100, 400, 200);
        this.setLayout(new GridLayout(3, 2));

        // Campos de entrada
        usuarioField = new JTextField();
        senhaField = new JPasswordField();
        loginButton = new JButton("Login");
        cadastroButton = new JButton("Cadastrar");

        // Adiciona os componentes
        this.add(new JLabel("Usuário:"));
        this.add(usuarioField);

        this.add(new JLabel("Senha:"));
        this.add(senhaField);

        this.add(loginButton);
        this.add(cadastroButton);

        // Ação do botão de login
        loginButton.addActionListener(e -> fazerLogin());
        cadastroButton.addActionListener(e -> abrirCadastroTela());

        this.setVisible(true);
    }

    private void fazerLogin() {
        String usuario = usuarioField.getText().trim();
        String senha = new String(senhaField.getPassword()).trim();

        // Valida os campos
        if (usuario.isEmpty() || senha.isEmpty()) {
            showUserError("Digite seu usuário e senha", "Erro de Login");
            return;
        }

        // Verifica se as credenciais são válidas
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM usuarios WHERE usuario = ? AND senha = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, usuario);
                stmt.setString(2, senha);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Login bem-sucedido
                        showUserInfo("Login bem-sucedido", "Login");

                        // Abre a tela de tarefas
                        openTodoListScreen();  // Abre a tela de tarefas
                    } else {
                        showUserError("Usuário ou senha inválidos", "Erro de Login");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showUserError("Erro ao verificar login", "Erro de Banco de Dados");
        }
    }

    private void abrirCadastroTela() {
        // Aqui você pode implementar a lógica para abrir a tela de cadastro
        this.setVisible(false);  // Fecha a tela de login
        new CadastroTela();      // Abre a tela de cadastro
    }

    private void openTodoListScreen() {
        // Esconde a tela de login
        this.setVisible(false);  // Em vez de dispose(), usamos setVisible(false)

        // Abre a tela de tarefas
        SwingUtilities.invokeLater(() -> {
            TodoList todoList = new TodoList(); // Cria a tela de tarefas
            todoList.setVisible(true); // Torna a tela de tarefas visível
        });
    }

    private void showUserError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showUserInfo(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
