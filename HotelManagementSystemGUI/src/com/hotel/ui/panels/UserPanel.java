package com.hotel.ui.panels;

import com.hotel.dao.UserDAO;
import com.hotel.model.User;
import com.hotel.ui.components.*;
import com.hotel.ui.dialogs.UserDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * UserPanel — admin-only user management.
 */
public class UserPanel extends JPanel {

    private final JFrame   owner;
    private final User     currentUser;
    private final UserDAO  userDAO = new UserDAO();

    private JTable            table;
    private DefaultTableModel model;

    public UserPanel(JFrame owner, User currentUser) {
        this.owner       = owner;
        this.currentUser = currentUser;
        setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        if (!currentUser.isAdmin()) {
            add(new JLabel("  Access denied — Admin only", SwingConstants.CENTER), BorderLayout.CENTER);
            return;
        }
        build();
        loadData();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.add(UIUtils.title("User Management"), BorderLayout.WEST);

        JButton addBtn = UIUtils.primaryBtn("+ Add User");
        addBtn.addActionListener(e -> openAddDialog());
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(addBtn);
        header.add(acts, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Role", "Full Name", "Email", "Phone", "Active", "Actions"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 7; }
        };
        table = new JTable(model);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(60);
        table.getColumnModel().getColumn(7).setPreferredWidth(140);

        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionEditor());

        JPanel card = UIUtils.card();
        card.setLayout(new BorderLayout());
        card.add(UIUtils.scrollPane(table), BorderLayout.CENTER);

        JPanel w = new JPanel(new BorderLayout()); w.setOpaque(false);
        w.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        w.add(card);
        add(w, BorderLayout.CENTER);
    }

    private void loadData() {
        model.setRowCount(0);
        List<User> users = userDAO.findAll();
        for (User u : users) {
            model.addRow(new Object[]{
                u.getUserId(), u.getUsername(), u.getRole().name(),
                u.getFullName(), u.getEmail(), u.getPhone(),
                u.isActive() ? "Yes" : "No", u.getUserId()
            });
        }
    }

    private void openAddDialog() {
        UserDialog dlg = new UserDialog(owner, null, currentUser);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadData(); UIUtils.toast(owner, "User created.", true); }
    }

    private void editUser(int id) {
        userDAO.findAll().stream().filter(u -> u.getUserId() == id).findFirst().ifPresent(user -> {
            UserDialog dlg = new UserDialog(owner, user, currentUser);
            dlg.setVisible(true);
            if (dlg.isSaved()) { loadData(); UIUtils.toast(owner, "User updated.", true); }
        });
    }

    private void deactivateUser(int id) {
        if (id == currentUser.getUserId()) { UIUtils.toast(owner, "Cannot deactivate yourself.", false); return; }
        int r = JOptionPane.showConfirmDialog(owner, "Deactivate User #" + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;
        userDAO.delete(id);
        loadData();
        UIUtils.toast(owner, "User deactivated.", true);
    }

    class ActionRenderer implements javax.swing.table.TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            return buildActions((int) model.getValueAt(row, 0), false);
        }
    }

    class ActionEditor extends DefaultCellEditor {
        private int id;
        ActionEditor() { super(new JCheckBox()); setClickCountToStart(1); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            id = (int) model.getValueAt(row, 0);
            return buildActions(id, true);
        }
        @Override public Object getCellEditorValue() { return id; }
    }

    private JPanel buildActions(int id, boolean interactive) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        p.setBackground(table.getBackground());
        JButton edit  = UIUtils.outlineBtn("Edit");
        JButton deact = UIUtils.dangerBtn("Deactivate");
        edit.setPreferredSize(new Dimension(62, 26));   edit.setFont(Theme.FONT_SMALL);
        deact.setPreferredSize(new Dimension(88, 26));  deact.setFont(Theme.FONT_SMALL);
        if (interactive) {
            edit.addActionListener(e  -> { fireStop(); editUser(id); });
            deact.addActionListener(e -> { fireStop(); deactivateUser(id); });
        }
        p.add(edit); p.add(deact);
        return p;
    }
    private void fireStop() { if (table.getCellEditor() != null) table.getCellEditor().stopCellEditing(); }
}
