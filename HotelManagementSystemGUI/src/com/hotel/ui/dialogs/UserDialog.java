package com.hotel.ui.dialogs;

import com.hotel.dao.UserDAO;
import com.hotel.model.User;
import com.hotel.ui.components.*;
import com.hotel.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;

/**
 * UserDialog — modal form for adding or editing a system user.
 */
public class UserDialog extends JDialog {

    private final User    existing;
    private final User    currentUser;
    private final UserDAO userDAO = new UserDAO();
    private boolean saved = false;

    private JTextField     usernameField, fullNameField, emailField, phoneField;
    private JPasswordField passwordField, confirmField;
    private JComboBox<String> roleCombo;

    public UserDialog(JFrame owner, User user, User currentUser) {
        super(owner, user == null ? "Add New User" : "Edit User", true);
        this.existing    = user;
        this.currentUser = currentUser;
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        root.add(UIUtils.heading(existing == null ? "Add New User" : "Edit User"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 12));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        usernameField = UIUtils.textField("Login username");
        fullNameField = UIUtils.textField("Full name");
        emailField    = UIUtils.textField("email@hotel.com");
        phoneField    = UIUtils.textField("10-digit mobile");
        passwordField = UIUtils.passwordField();
        confirmField  = UIUtils.passwordField();
        roleCombo     = UIUtils.comboBox("STAFF", "ADMIN");

        if (existing != null) {
            usernameField.setText(existing.getUsername());
            usernameField.setEnabled(false);
            fullNameField.setText(existing.getFullName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
            roleCombo.setSelectedItem(existing.getRole().name());
        }

        form.add(UIUtils.formRow("Username *",   usernameField));
        form.add(UIUtils.formRow("Full Name *",  fullNameField));
        form.add(UIUtils.formRow("Role *",       roleCombo));
        form.add(UIUtils.formRow("Email",        emailField));
        form.add(UIUtils.formRow("Phone",        phoneField));

        if (existing == null) {
            form.add(UIUtils.formRow("Password *",  passwordField));
            form.add(UIUtils.formRow("Confirm Pwd *", confirmField));
        } else {
            JLabel note = UIUtils.muted("Leave password fields blank to keep existing password.");
            note.setFont(Theme.FONT_SMALL);
            form.add(note);
            form.add(UIUtils.formRow("New Password", passwordField));
            form.add(UIUtils.formRow("Confirm",      confirmField));
        }

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton cancelBtn = UIUtils.outlineBtn("Cancel");
        JButton saveBtn   = UIUtils.primaryBtn(existing == null ? "Create User" : "Save Changes");
        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> save());
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        root.add(form,   BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void save() {
        try {
            String username  = usernameField.getText().trim();
            String fullName  = fullNameField.getText().trim();
            String email     = emailField.getText().trim();
            String phone     = phoneField.getText().trim();
            User.Role role   = User.Role.valueOf((String) roleCombo.getSelectedItem());
            String password  = new String(passwordField.getPassword());
            String confirm   = new String(confirmField.getPassword());

            if (username.isBlank()) throw new Exception("Username is required.");
            if (fullName.isBlank()) throw new Exception("Full name is required.");

            if (existing == null) {
                // Creating new user
                if (password.isBlank()) throw new Exception("Password is required.");
                if (!password.equals(confirm)) throw new Exception("Passwords do not match.");
                User newUser = new User(username, PasswordUtil.hash(password), role, fullName, email, phone);
                userDAO.create(newUser);
            } else {
                // Updating existing user
                existing.setFullName(fullName);
                existing.setEmail(email);
                existing.setPhone(phone);
                existing.setRole(role);
                userDAO.update(existing);

                // Only update password if provided
                if (!password.isBlank()) {
                    if (!password.equals(confirm)) throw new Exception("Passwords do not match.");
                    userDAO.updatePassword(existing.getUserId(), PasswordUtil.hash(password));
                }
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
