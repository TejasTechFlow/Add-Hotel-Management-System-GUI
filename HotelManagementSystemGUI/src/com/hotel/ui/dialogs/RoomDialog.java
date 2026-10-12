package com.hotel.ui.dialogs;

import com.hotel.model.Room;
import com.hotel.model.User;
import com.hotel.service.RoomService;
import com.hotel.ui.components.*;

import javax.swing.*;
import java.awt.*;

/**
 * RoomDialog — modal form for adding or editing a room.
 */
public class RoomDialog extends JDialog {

    private final Room        existing;
    private final User        currentUser;
    private final RoomService roomService = new RoomService();
    private boolean saved = false;

    private JTextField   roomNumberField, priceField, floorField, descField;
    private JComboBox<String> typeCombo;

    public RoomDialog(JFrame owner, Room room, User user) {
        super(owner, room == null ? "Add New Room" : "Edit Room #" + room.getRoomId(), true);
        this.existing    = room;
        this.currentUser = user;
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Title
        root.add(UIUtils.heading(existing == null ? "Add New Room" : "Edit Room"), BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 12));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        roomNumberField = UIUtils.textField("e.g. 205");
        typeCombo       = UIUtils.comboBox("SINGLE", "DOUBLE", "SUITE", "DELUXE");
        priceField      = UIUtils.textField("e.g. 2500.00");
        floorField      = UIUtils.textField("e.g. 2");
        descField       = UIUtils.textField("Brief description");

        if (existing != null) {
            roomNumberField.setText(existing.getRoomNumber());
            roomNumberField.setEnabled(false);
            typeCombo.setSelectedItem(existing.getRoomType().name());
            priceField.setText(String.valueOf(existing.getPricePerNight()));
            floorField.setText(String.valueOf(existing.getFloorNumber()));
            descField.setText(existing.getDescription());
        }

        form.add(UIUtils.formRow("Room Number *", roomNumberField));
        form.add(UIUtils.formRow("Room Type *",   typeCombo));
        form.add(UIUtils.formRow("Price/Night *", priceField));
        form.add(UIUtils.formRow("Floor *",       floorField));
        form.add(UIUtils.formRow("Description",   descField));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton cancelBtn = UIUtils.outlineBtn("Cancel");
        JButton saveBtn   = UIUtils.primaryBtn(existing == null ? "Add Room" : "Save Changes");
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
            String roomNum = roomNumberField.getText().trim();
            Room.RoomType type = Room.RoomType.valueOf((String) typeCombo.getSelectedItem());
            double price = Double.parseDouble(priceField.getText().trim());
            int    floor = Integer.parseInt(floorField.getText().trim());
            String desc  = descField.getText().trim();

            if (existing == null) {
                roomService.addRoom(roomNum, type, price, floor, desc);
            } else {
                existing.setRoomType(type);
                existing.setPricePerNight(price);
                existing.setFloorNumber(floor);
                existing.setDescription(desc);
                roomService.updateRoom(existing);
            }
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price and floor must be valid numbers.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
