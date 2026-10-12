package com.hotel.ui.dialogs;

import com.hotel.model.Customer;
import com.hotel.service.CustomerService;
import com.hotel.ui.components.*;

import javax.swing.*;
import java.awt.*;

/**
 * CustomerDialog — modal form for adding or editing a customer.
 */
public class CustomerDialog extends JDialog {

    private final Customer        existing;
    private final CustomerService customerService = new CustomerService();
    private boolean saved = false;

    private JTextField nameField, contactField, emailField, addressField, idProofField, idNumField;

    public CustomerDialog(JFrame owner, Customer customer) {
        super(owner, customer == null ? "Add Customer" : "Edit Customer", true);
        this.existing = customer;
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        root.add(UIUtils.heading(existing == null ? "Add New Customer" : "Edit Customer"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 12));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        nameField    = UIUtils.textField("Full name");
        contactField = UIUtils.textField("10-digit mobile");
        emailField   = UIUtils.textField("email@example.com");
        addressField = UIUtils.textField("City, State");
        idProofField = UIUtils.textField("Aadhaar / Passport / DL");
        idNumField   = UIUtils.textField("Document number");

        if (existing != null) {
            nameField.setText(existing.getName());
            contactField.setText(existing.getContact());
            emailField.setText(existing.getEmail());
            addressField.setText(existing.getAddress());
            idProofField.setText(existing.getIdProof());
            idNumField.setText(existing.getIdNumber());
        }

        form.add(UIUtils.formRow("Full Name *",   nameField));
        form.add(UIUtils.formRow("Mobile *",      contactField));
        form.add(UIUtils.formRow("Email",         emailField));
        form.add(UIUtils.formRow("Address",       addressField));
        form.add(UIUtils.formRow("ID Proof Type", idProofField));
        form.add(UIUtils.formRow("ID Number",     idNumField));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton cancelBtn = UIUtils.outlineBtn("Cancel");
        JButton saveBtn   = UIUtils.primaryBtn(existing == null ? "Add Customer" : "Save");
        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> save());

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        root.add(form, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void save() {
        try {
            String name    = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String email   = emailField.getText().trim();
            String address = addressField.getText().trim();
            String idProof = idProofField.getText().trim();
            String idNum   = idNumField.getText().trim();

            if (existing == null) {
                customerService.addCustomer(name, contact, email, address, idProof, idNum);
            } else {
                existing.setName(name);
                existing.setContact(contact);
                existing.setEmail(email);
                existing.setAddress(address);
                existing.setIdProof(idProof);
                existing.setIdNumber(idNum);
                customerService.updateCustomer(existing);
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
