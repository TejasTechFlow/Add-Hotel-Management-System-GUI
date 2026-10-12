package com.hotel.ui.panels;

import com.hotel.model.Customer;
import com.hotel.model.User;
import com.hotel.service.CustomerService;
import com.hotel.ui.components.*;
import com.hotel.ui.dialogs.CustomerDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * CustomerPanel — full customer management panel.
 */
public class CustomerPanel extends JPanel {

    private final JFrame           owner;
    private final User             currentUser;
    private final CustomerService  customerService = new CustomerService();

    private JTable            table;
    private DefaultTableModel model;
    private JTextField        searchField;

    public CustomerPanel(JFrame owner, User currentUser) {
        this.owner       = owner;
        this.currentUser = currentUser;
        setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
        loadData("");
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.add(UIUtils.title("Customer Management"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        searchField = UIUtils.textField("Search name / phone / email...");
        searchField.setPreferredSize(new Dimension(220, 34));
        JButton searchBtn = UIUtils.outlineBtn("Search");
        searchBtn.addActionListener(e -> loadData(searchField.getText().trim()));
        searchField.addActionListener(e -> loadData(searchField.getText().trim()));

        JButton addBtn = UIUtils.primaryBtn("+ Add Customer");
        addBtn.addActionListener(e -> openAddDialog());

        actions.add(searchField);
        actions.add(searchBtn);
        actions.add(addBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Mobile", "Email", "Address", "ID Proof", "Actions"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        table = new JTable(model);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);

        table.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionEditor());

        JPanel card = UIUtils.card();
        card.setLayout(new BorderLayout());
        card.add(UIUtils.scrollPane(table), BorderLayout.CENTER);

        JPanel w = new JPanel(new BorderLayout()); w.setOpaque(false);
        w.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        w.add(card);
        add(w, BorderLayout.CENTER);
    }

    private void loadData(String keyword) {
        model.setRowCount(0);
        List<Customer> list = keyword.isEmpty()
                ? customerService.getAllCustomers()
                : customerService.search(keyword);
        for (Customer c : list) {
            model.addRow(new Object[]{
                c.getCustomerId(), c.getName(), c.getContact(),
                c.getEmail(), c.getAddress(),
                c.getIdProof() + " " + c.getIdNumber(), c.getCustomerId()
            });
        }
    }

    private void openAddDialog() {
        CustomerDialog dlg = new CustomerDialog(owner, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadData(""); UIUtils.toast(owner, "Customer added!", true); }
    }

    private void editCustomer(int id) {
        Customer c = customerService.getById(id);
        CustomerDialog dlg = new CustomerDialog(owner, c);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadData(""); UIUtils.toast(owner, "Customer updated.", true); }
    }

    private void deleteCustomer(int id) {
        if (!currentUser.isAdmin()) { UIUtils.toast(owner, "Admin access required.", false); return; }
        int r = JOptionPane.showConfirmDialog(owner, "Delete Customer #" + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        try { customerService.deleteCustomer(id); loadData(""); UIUtils.toast(owner, "Deleted.", true); }
        catch (Exception ex) { UIUtils.toast(owner, ex.getMessage(), false); }
    }

    // ── Action cells ──────────────────────────────────────────────────────────

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

    private JPanel buildActions(int id, boolean active) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        p.setBackground(table.getBackground());
        JButton edit = UIUtils.outlineBtn("Edit");
        JButton del  = UIUtils.dangerBtn("Delete");
        edit.setPreferredSize(new Dimension(66, 26)); edit.setFont(Theme.FONT_SMALL);
        del.setPreferredSize(new Dimension(66, 26));  del.setFont(Theme.FONT_SMALL);
        if (active) {
            edit.addActionListener(e -> { fireStop(); editCustomer(id); });
            del.addActionListener(e  -> { fireStop(); deleteCustomer(id); });
        }
        p.add(edit); p.add(del);
        return p;
    }
    private void fireStop() { if (table.getCellEditor() != null) table.getCellEditor().stopCellEditing(); }
}
