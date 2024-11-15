package com.example;

public class Task {
    private int id;
    private String description;
    private boolean done;
    private User responsavel; // Responsável pela tarefa

    // Construtores, getters e setters

    public Task(int id, String description, boolean done, User responsavel) {
        this.id = id;
        this.description = description;
        this.done = done;
        this.responsavel = responsavel;
    }

    // Getters e Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDone() {
        return done;
    }

    // Adicionando o setDone
    public void setDone(boolean done) {
        this.done = done;
    }

    public User getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(User responsavel) {
        this.responsavel = responsavel;
    }

    
}
