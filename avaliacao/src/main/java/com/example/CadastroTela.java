package com.example;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CadastroTela extends JFrame {
    private JTextField usuarioField;
    private JPasswordField senhaField;
    private JTextField funcaoField;
    private JTextField setorField;
    private JButton cadastrarButton;
    
    public CadastroTela() {
        super("Cadastro de Usuário");
        
        // Configurações da tela
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(500, 100, 400, 300);
        this.setLayout(new GridLayout(5, 2));
        
        // Campos de entrada
        usuarioField = new JTextField();
        senhaField = new JPasswordField();
        funcaoField = new JTextField();
        setorField = new JTextField();
        cadastrarButton = new JButton("Cadastrar");
        
        // Adiciona os componentes
        this.add(new JLabel("Usuário:"));
        this.add(usuarioField);
        
        this.add(new JLabel("Senha:"));
        this.add(senhaField);
        
        this.add(new JLabel("Função:"));
        this.add(funcaoField);
        
        this.add(new JLabel("Setor:"));
        this.add(setorField);
        
        this.add(cadastrarButton);
        
        // Ação do botão de cadastrar
        cadastrarButton.addActionListener(e -> cadastrarUsuario());
        
        this.setVisible(true);
    }
    
    private void cadastrarUsuario() {
        String usuario = usuarioField.getText().trim();
        String senha = new String(senhaField.getPassword()).trim();
        String funcao = funcaoField.getText().trim();
        String setor = setorField.getText().trim();
        
        // Valida os campos
        if (usuario.isEmpty() || senha.isEmpty() || funcao.isEmpty() || setor.isEmpty()) {
            showUserError("Preencha todos os campos!", "Erro de Cadastro");
            return;
        }

        // Insere no banco de dados
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO usuarios (usuario, senha, funcao, setor) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, usuario);
                stmt.setString(2, senha);
                stmt.setString(3, funcao);
                stmt.setString(4, setor);
                stmt.executeUpdate();
                showUserInfo("Cadastro realizado com sucesso!", "Cadastro");
                this.dispose();  // Fecha a tela de cadastro
                new LoginTela();  // Abre a tela de login após cadastro
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showUserError("Erro ao cadastrar usuário.", "Erro de Banco de Dados");
        }
    }

    private void showUserError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showUserInfo(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
