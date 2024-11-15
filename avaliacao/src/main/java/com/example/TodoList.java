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
    private JComboBox<String> responsavelComboBox; // JComboBox para selecionar o responsável
    private List<Task> tasks;
    private List<User> users; // Lista de usuários para os responsáveis

    public TodoList() {
        super("Minhas Tarefas");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(500, 100, 600, 500);

        try {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());

            tasks = new ArrayList<>();
            users = new ArrayList<>(); // Inicializa a lista de usuários
            listModel = new DefaultListModel<>();
            taskList = new JList<>(listModel);

            taskInputField = new JTextField();
            addButton = new JButton("Adicionar");
            deleteButton = new JButton("Excluir");
            markDoneButton = new JButton("Concluir");
            filterComboBox = new JComboBox<>(new String[] { "Todas", "Ativas", "Concluídas" });
            clearCompletedButton = new JButton("Limpar Concluídas");
            clearAll = new JButton("Limpar Tarefas");

            responsavelComboBox = new JComboBox<>(); // Inicializa o JComboBox para responsáveis

            JPanel inputPanel = new JPanel(new BorderLayout());
            inputPanel.add(taskInputField, BorderLayout.CENTER);
            inputPanel.add(addButton, BorderLayout.EAST);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(deleteButton);
            buttonPanel.add(markDoneButton);
            buttonPanel.add(filterComboBox);
            buttonPanel.add(clearCompletedButton);
            buttonPanel.add(clearAll);
            buttonPanel.add(new JLabel("Responsável:"));
            buttonPanel.add(responsavelComboBox); // Adiciona o JComboBox para o responsável

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

            // Carrega as tarefas e os usuários do banco de dados
            loadTasksFromDatabase();
            loadUsersFromDatabase(); // Carrega os responsáveis (usuários)
            updateTaskList(); // Atualiza a lista de tarefas

        } catch (Exception e) {
            e.printStackTrace();
            showUserError("Erro inesperado", "Erro");
        }
    }

    // Método para carregar os usuários do banco de dados
   // Função que carrega os usuários (responsáveis) do banco
private void loadUsersFromDatabase() {
    String sql = "SELECT id, usuario FROM usuarios"; // Consulta para buscar os responsáveis
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            int userId = rs.getInt("id");
            String username = rs.getString("usuario");
            users.add(new User(userId, username)); // Adiciona o usuário à lista
            responsavelComboBox.addItem(username); // Adiciona o nome no ComboBox
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showUserError("Erro ao carregar responsáveis.", "Erro ao Carregar Responsáveis");
    }
}

// Função para adicionar tarefa ao banco com o responsável selecionado
private void addTask() {
    String taskDescription = taskInputField.getText().trim();
    if (!taskDescription.isEmpty()) {
        // Pega o responsável selecionado
        String responsavelName = (String) responsavelComboBox.getSelectedItem();
        User responsavel = getUserByName(responsavelName); // Encontra o responsável pelo nome
        addTaskToDatabase(taskDescription, responsavel); // Passa o responsável ao método
        loadTasksFromDatabase(); // Carrega as tarefas após adicionar
        updateTaskList(); // Atualiza a interface com a lista de tarefas
        taskInputField.setText("");
    } else {
        showUserError("Digite uma Tarefa para adicionar", "Nenhuma Tarefa Digitada");
    }
}


    // Método para adicionar a tarefa ao banco de dados com responsável
    private void addTaskToDatabase(String description, User responsavel) {
        String sql = "INSERT INTO tasks (description, done, responsavel_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, description);
            stmt.setBoolean(2, false); // Tarefa não concluída
            if (responsavel != null) {
                stmt.setInt(3, responsavel.getId()); // Responsável selecionado
            } else {
                stmt.setNull(3, Types.INTEGER); // Se nenhum responsável selecionado
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showUserError("Erro ao adicionar tarefa.", "Erro ao Adicionar");
        }
    }

    // Método para encontrar o usuário pelo nome
// Método para encontrar o usuário pelo nome
private User getUserByName(String username) {
    for (User user : users) { // Procura o responsável pela lista de usuários
        if (user.getUsername().equals(username)) {
            return user;
        }
    }
    return null;
}

    // Método para carregar as tarefas do banco de dados
    private void loadTasksFromDatabase() {
        String sql = "SELECT id, description, done, responsavel_id FROM tasks";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            tasks.clear(); // Limpa a lista de tarefas
            while (rs.next()) {
                int id = rs.getInt("id");
                String description = rs.getString("description");
                boolean done = rs.getBoolean("done");
                int responsavelId = rs.getInt("responsavel_id");
                User responsavel = getUserById(responsavelId); // Obtém o responsável pela ID
                tasks.add(new Task(id, description, done, responsavel)); // Cria a tarefa com o responsável
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showUserError("Erro ao carregar tarefas.", "Erro ao Carregar");
        }
    }

    // Método para encontrar o usuário pelo ID
    private User getUserById(int userId) {
        for (User user : users) {
            if (user.getId() == userId) {
                return user;
            }
        }
        return null;
    }

    // Método para atualizar a lista de tarefas na interface
    private void updateTaskList() {
        listModel.clear();
        for (Task task : tasks) {
            String taskText = task.getDescription() + (task.isDone() ? " (Concluída)" : "");
            if (task.getResponsavel() != null) {
                taskText += " - " + task.getResponsavel().getUsername(); // Exibe o responsável
            }
            listModel.addElement(taskText);
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

    private void showUserError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
