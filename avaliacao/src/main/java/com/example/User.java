package com.example;

public class User {
    private int id;
    private String username;

    // Construtor
    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    // Getter para o 'username'
    public String getUsername() {
        return username;
    }

    // Getter para o 'id'
    public int getId() {
        return id;
    }

    // Setter para o 'username' (caso você precise atualizar o nome de usuário)
    public void setUsername(String username) {
        this.username = username;
    }

    // Setter para o 'id'
    public void setId(int id) {
        this.id = id;
    }
}
