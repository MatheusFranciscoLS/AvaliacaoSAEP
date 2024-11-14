package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TodoList extends JFrame {
    private JPanel mainPanel;
    private JTextField taskInputField;
    private JButton addButton;
    private JList<String> taskList;
    private DefaultListModel<String> listModel;
    private JButton deleteButton;
    private JButton markDoneButton;
    private JComboBox<String> filterComboBox;
    private JButton clearCompletedButton;
    private JButton clearAll;
    private List<Task> tasks;

    public TodoList() {
        super("Minhas Tarefas");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(500, 100, 600, 500);

        try {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());

            tasks = new ArrayList<>();
            listModel = new DefaultListModel<>();
            taskList = new JList<>(listModel);

            taskInputField = new JTextField();
            addButton = new JButton("Adicionar");
            deleteButton = new JButton("Excluir");
            markDoneButton = new JButton("Concluir");
            filterComboBox = new JComboBox<>(new String[] { "Todas", "Ativas", "Concluídas" });
            clearCompletedButton = new JButton("Limpar Concluídas");
            clearAll = new JButton("Limpar Tarefas");

            JPanel inputPanel = new JPanel(new BorderLayout());
            inputPanel.add(taskInputField, BorderLayout.CENTER);
            inputPanel.add(addButton, BorderLayout.EAST);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(deleteButton);
            buttonPanel.add(markDoneButton);
            buttonPanel.add(filterComboBox);
            buttonPanel.add(clearCompletedButton);
            buttonPanel.add(clearAll);

            mainPanel.add(inputPanel, BorderLayout.NORTH);
            mainPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            this.add(mainPanel);

            addButton.addActionListener(e -> addTask());
            deleteButton.addActionListener(e -> deleteTask());
            markDoneButton.addActionListener(e -> markTaskDone());
            filterComboBox.addActionListener(e -> filterTasks());
            clearCompletedButton.addActionListener(e -> clearCompletedTasks());
            clearAll.addActionListener(e -> clearAll());

            taskInputField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        addTask();
                    }
                }
            });

            // Carrega as tarefas do banco de dados e atualiza a lista imediatamente após a criação
            loadTasksFromDatabase();
            updateTaskList(); // Atualiza a lista logo após carregar as tarefas

        } catch (Exception e) {
            e.printStackTrace();
            showUserError("Erro inesperado", "Erro");
        }
    }

    private void addTask() {
        String taskDescription = taskInputField.getText().trim();
        if (!taskDescription.isEmpty()) {
            addTaskToDatabase(taskDescription);
            loadTasksFromDatabase(); // Carrega as tarefas após adicionar
            updateTaskList(); // Atualiza a interface com a lista de tarefas
            taskInputField.setText("");
        } else {
            showUserError("Digite uma Tarefa para adicionar", "Nenhuma Tarefa Digitada");
        }
    }

    private void addTaskToDatabase(String description) {
        String sql = "INSERT INTO tasks (description, done) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, description);
            stmt.setBoolean(2, false); // Tarefa não concluída
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showUserError("Erro ao adicionar tarefa.", "Erro ao Adicionar");
        }
    }

    private void loadTasksFromDatabase() {
        String sql = "SELECT id, description, done FROM tasks";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            tasks.clear(); // Limpa a lista de tarefas
            while (rs.next()) {
                int id = rs.getInt("id");
                String description = rs.getString("description");
                boolean done = rs.getBoolean("done");
                tasks.add(new Task(id, description, done));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showUserError("Erro ao carregar tarefas.", "Erro ao Carregar");
        }
    }

    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < tasks.size()) {
            Task task = tasks.get(selectedIndex);
            deleteTaskFromDatabase(task.getId());
            tasks.remove(selectedIndex);
            updateTaskList(); // Atualiza a lista após excluir
        }
    }

    private void deleteTaskFromDatabase(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showUserError("Erro ao excluir tarefa.", "Erro ao Excluir");
        }
    }

    private void markTaskDone() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < tasks.size()) {
            Task task = tasks.get(selectedIndex);
            task.setDone(true);
            updateTaskStatusInDatabase(task.getId(), true);
            updateTaskList(); // Atualiza a lista com a tarefa concluída
        }
    }

    private void updateTaskStatusInDatabase(int taskId, boolean done) {
        String sql = "UPDATE tasks SET done = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, done);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showUserError("Erro ao atualizar status da tarefa.", "Erro ao Atualizar");
        }
    }

    private void filterTasks() {
        String filter = (String) filterComboBox.getSelectedItem();
        listModel.clear();
        for (Task task : tasks) {
            if (filter.equals("Todas") || (filter.equals("Ativas") && !task.isDone())
                    || (filter.equals("Concluídas") && task.isDone())) {
                listModel.addElement(task.getDescription() + (task.isDone() ? " (Concluída)" : ""));
            }
        }
    }

    private void clearCompletedTasks() {
        try {
            List<Task> completedTasks = new ArrayList<>();
            for (Task task : tasks) {
                if (task.isDone()) {
                    completedTasks.add(task);
                }
            }
            if (completedTasks.isEmpty()) {
                showUserError("Nenhuma Tarefa Concluída.", "Erro Inesperado");
            } else {
                int escolha = JOptionPane.showConfirmDialog(this, "Excluir todas as tarefas concluídas?", "Confirmação", JOptionPane.YES_NO_OPTION);
                if (escolha == JOptionPane.YES_OPTION) {
                    for (Task task : completedTasks) {
                        deleteTaskFromDatabase(task.getId());
                    }
                    tasks.removeAll(completedTasks);
                    updateTaskList();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showUserError("Erro ao limpar tarefas concluídas", "Erro");
        }
    }

    private void clearAll() {
        try {
            if (tasks.isEmpty()) {
                showUserError("Nenhuma Tarefa Adicionada.", "Erro Inesperado");
            } else {
                int confirmacao = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir TODAS as tarefas?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
                if (confirmacao == JOptionPane.YES_OPTION) {
                    for (Task task : tasks) {
                        deleteTaskFromDatabase(task.getId());
                    }
                    tasks.clear();
                    updateTaskList();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showUserError("Erro ao limpar todas as tarefas", "Erro");
        }
    }

    private void updateTaskList() {
        listModel.clear();
        for (Task task : tasks) {
            listModel.addElement(task.getDescription() + (task.isDone() ? " (Concluída)" : ""));
        }
    }

    private void showUserError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}