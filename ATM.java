/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;

// User class representing ATM users
class User implements Serializable {
    private String username;
    private String password;
    private double balance;

    public User(String username, String password, double balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

// UserManager class for handling user-related operations
class UserManager {
    private HashMap<String, User> users;
    private final String dataFilePath = "users.dat";

    public UserManager() {
        users = readUserDataFromFile();
    }

    public boolean authenticateUser(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }

    public void createAccount(String username, String password, double balance) {
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }
        users.put(username, new User(username, password, balance));
        saveUserDataToFile();
    }

    public User getUser(String username) {
        return users.get(username);
    }

    private HashMap<String, User> readUserDataFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFilePath))) {
            return (HashMap<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();
        }
    }

    public void saveUserDataToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFilePath))) {
            oos.writeObject(users);
        } catch (IOException e) {
            throw new RuntimeException("Error saving user data to file", e);
        }
    }
}


// ATMGUI class for handling the graphical user interface
class ATMGUI {
    private ATM atm;
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public ATMGUI(ATM atm) {
        this.atm = atm;
        setupGUI();
    }

    private void setupGUI() {
        frame = new JFrame("ATM");
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel mainMenuPanel = createMainMenuPanel();
        mainPanel.add(mainMenuPanel, "MainMenu");

        frame.add(mainPanel);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        JButton createAccountButton = new JButton("Create Account");
        JButton loginButton = new JButton("Login");
        JButton exitButton = new JButton("Exit");

        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCreateAccountPanel();
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLoginPanel();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        panel.add(createAccountButton);
        panel.add(loginButton);
        panel.add(exitButton);

        return panel;
    }

    private void showCreateAccountPanel() {
        JPanel createAccountPanel = new JPanel(new GridLayout(5, 2));
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JLabel balanceLabel = new JLabel("Initial Balance:");
        JTextField balanceField = new JTextField();
        JLabel accountTypeLabel = new JLabel("Account Type:");
        JComboBox<String> accountTypeComboBox = new JComboBox<>(new String[]{"Savings Account", "Current Account"});
        JButton createButton = new JButton("Create");
        JButton backButton = new JButton("Back");

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());
                    double balance = Double.parseDouble(balanceField.getText());
                    atm.getUserManager().createAccount(username, password, balance);
                    JOptionPane.showMessageDialog(frame, "Account created successfully.");
                    cardLayout.show(mainPanel, "MainMenu");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error creating account: " + ex.getMessage());
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "MainMenu");
            }
        });

        createAccountPanel.add(usernameLabel);
        createAccountPanel.add(usernameField);
        createAccountPanel.add(passwordLabel);
        createAccountPanel.add(passwordField);
        createAccountPanel.add(balanceLabel);
        createAccountPanel.add(balanceField);
        createAccountPanel.add(accountTypeLabel);
        createAccountPanel.add(accountTypeComboBox);
        createAccountPanel.add(createButton);
        createAccountPanel.add(backButton);

        mainPanel.add(createAccountPanel, "CreateAccount");
        cardLayout.show(mainPanel, "CreateAccount");
    }

    private void showLoginPanel() {
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton backButton = new JButton("Back");

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());
                    if (atm.getUserManager().authenticateUser(username, password)) {
                        showATMMenuPanel(username);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid username or password.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error logging in: " + ex.getMessage());
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "MainMenu");
            }
        });

        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);
        loginPanel.add(backButton);

        mainPanel.add(loginPanel, "Login");
        cardLayout.show(mainPanel, "Login");
    }

    private void showATMMenuPanel(String username) {
        JPanel atmMenuPanel = new JPanel(new GridLayout(5, 1));
        JButton withdrawButton = new JButton("Withdraw");
        JButton depositButton = new JButton("Deposit");
        JButton balanceButton = new JButton("Balance Inquiry");
        JButton transferButton = new JButton("Transfer Funds");
        JButton logoutButton = new JButton("Logout");

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atm.performWithdrawal(username);
            }
        });

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atm.performDeposit(username);
            }
        });

        balanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atm.checkBalance(username);
            }
        });

        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTransferFundsPanel(username);
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "MainMenu");
            }
        });

        atmMenuPanel.add(withdrawButton);
        atmMenuPanel.add(depositButton);
        atmMenuPanel.add(balanceButton);
        atmMenuPanel.add(transferButton);
        atmMenuPanel.add(logoutButton);

        mainPanel.add(atmMenuPanel, "ATMMenu");
        cardLayout.show(mainPanel, "ATMMenu");
    }

    private void showTransferFundsPanel(String username) {
        JPanel transferPanel = new JPanel(new GridLayout(4, 2));
        JLabel recipientLabel = new JLabel("Recipient Username:");
        JTextField recipientField = new JTextField();
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();
        JButton transferButton = new JButton("Transfer");
        JButton backButton = new JButton("Back");

        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String recipientUsername = recipientField.getText();
                    double amount = Double.parseDouble(amountField.getText());
                    atm.performTransfer(username, recipientUsername, amount);
                    cardLayout.show(mainPanel, "ATMMenu");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error transferring funds: " + ex.getMessage());
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "ATMMenu");
            }
        });

        transferPanel.add(recipientLabel);
        transferPanel.add(recipientField);
        transferPanel.add(amountLabel);
        transferPanel.add(amountField);
        transferPanel.add(transferButton);
        transferPanel.add(backButton);

        mainPanel.add(transferPanel, "TransferFunds");
        cardLayout.show(mainPanel, "TransferFunds");
    }
}

// ATM class for handling ATM operations
class ATM {
    private UserManager userManager;

    public ATM() {
        userManager = new UserManager();
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void performWithdrawal(String username) {
        try {
            String amountStr = JOptionPane.showInputDialog(null, "Enter withdrawal amount:");
            if (amountStr == null) return;
            double amount = Double.parseDouble(amountStr);
            User user = userManager.getUser(username);
            if (amount <= 0 || amount > user.getBalance()) {
                JOptionPane.showMessageDialog(null, "Invalid withdrawal amount.");
                return;
            }
            user.setBalance(user.getBalance() - amount);
            userManager.saveUserDataToFile();
            JOptionPane.showMessageDialog(null, "Withdrawal successful. Remaining balance: " + user.getBalance());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error performing withdrawal: " + e.getMessage());
        }
    }

    public void performDeposit(String username) {
        try {
            String amountStr = JOptionPane.showInputDialog(null, "Enter deposit amount:");
            if (amountStr == null) return;
            double amount = Double.parseDouble(amountStr);
            User user = userManager.getUser(username);
            user.setBalance(user.getBalance() + amount);
            userManager.saveUserDataToFile();
            JOptionPane.showMessageDialog(null, "Deposit successful. New balance: " + user.getBalance());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error performing deposit: " + e.getMessage());
        }
    }

    public void checkBalance(String username) {
        User user = userManager.getUser(username);
        JOptionPane.showMessageDialog(null, "Current balance: " + user.getBalance());
    }

    public void performTransfer(String senderUsername, String recipientUsername, double amount) {
        try {
            User sender = userManager.getUser(senderUsername);
            User recipient = userManager.getUser(recipientUsername);

            if (sender == null || recipient == null) {
                JOptionPane.showMessageDialog(null, "Invalid recipient username.");
                return;
            }

            if (amount <= 0 || amount > sender.getBalance()) {
                JOptionPane.showMessageDialog(null, "Invalid transfer amount.");
                return;
            }

            sender.setBalance(sender.getBalance() - amount);
            recipient.setBalance(recipient.getBalance() + amount);
            userManager.saveUserDataToFile();
            JOptionPane.showMessageDialog(null, "Transfer successful. Your new balance: " + sender.getBalance());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error performing transfer: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ATM atm = new ATM();
        ATMGUI gui = new ATMGUI(atm);
    }
}


