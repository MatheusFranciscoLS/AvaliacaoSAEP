package com.example;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Inicia a aplicação com a tela de login
        SwingUtilities.invokeLater(() -> new LoginTela());  // Abre a tela de login
    }
}
