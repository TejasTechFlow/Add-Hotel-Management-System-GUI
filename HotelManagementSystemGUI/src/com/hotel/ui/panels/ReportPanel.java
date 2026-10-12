package com.hotel.ui.panels;

import com.hotel.model.User;
import com.hotel.service.BillingService;
import com.hotel.service.ReportService;
import com.hotel.ui.components.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ReportPanel — tabbed panel for daily bookings, available rooms and revenue reports.
 */
public class ReportPanel extends JPanel {

    private final User          currentUser;
    private final ReportService reportService  = new ReportService();
    private final BillingService billingService = new BillingService();

    public ReportPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        add(UIUtils.title("Reports"), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_SUBHEAD);
        tabs.setBackground(Theme.BG_CARD);
        tabs.addTab("📅  Daily Bookings",   buildDailyTab());
        tabs.addTab("🏠  Available Rooms",  buildRoomsTab());
        tabs.addTab("💰  Revenue",           buildRevenueTab());
        tabs.addTab("📋  Today's Summary",  buildSummaryTab());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        wrapper.add(tabs);
        add(wrapper, BorderLayout.CENTER);
    }

    // ── Daily bookings ────────────────────────────────────────────────────────

    private JPanel buildDailyTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        controls.add(UIUtils.subhead("Date:"));
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setPreferredSize(new Dimension(130, 34));
        dateSpinner.setValue(java.util.Date.from(LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        controls.add(dateSpinner);

        JTextArea output = new JTextArea();
        output.setFont(Theme.FONT_MONO);
        output.setEditable(false);
        output.setBackground(new Color(248, 250, 252));

        JButton runBtn = UIUtils.primaryBtn("Generate");
        runBtn.addActionListener(e -> {
            java.util.Date d = (java.util.Date) dateSpinner.getValue();
            LocalDate date   = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            output.setText(reportService.dailyBookingReport(date));
        });
        controls.add(runBtn);

        p.add(controls,           BorderLayout.NORTH);
        p.add(new JScrollPane(output), BorderLayout.CENTER);
        return p;
    }

    // ── Available rooms ───────────────────────────────────────────────────────

    private JPanel buildRoomsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextArea output = new JTextArea();
        output.setFont(Theme.FONT_MONO);
        output.setEditable(false);
        output.setBackground(new Color(248, 250, 252));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton runBtn = UIUtils.primaryBtn("Refresh Report");
        runBtn.addActionListener(e -> output.setText(reportService.availableRoomsReport()));
        top.add(runBtn);

        // Auto-load
        output.setText(reportService.availableRoomsReport());

        p.add(top,                     BorderLayout.NORTH);
        p.add(new JScrollPane(output), BorderLayout.CENTER);
        return p;
    }

    // ── Revenue report ────────────────────────────────────────────────────────

    private JPanel buildRevenueTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        if (!currentUser.isAdmin()) {
            p.add(UIUtils.muted("Revenue report is restricted to Admin users."), BorderLayout.CENTER);
            return p;
        }

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);

        controls.add(UIUtils.subhead("From:"));
        JSpinner fromSpinner = dateSpinner(LocalDate.now().minusDays(30));
        controls.add(fromSpinner);

        controls.add(UIUtils.subhead("To:"));
        JSpinner toSpinner = dateSpinner(LocalDate.now());
        controls.add(toSpinner);

        JTextArea output = new JTextArea();
        output.setFont(Theme.FONT_MONO);
        output.setEditable(false);
        output.setBackground(new Color(248, 250, 252));

        JButton runBtn = UIUtils.primaryBtn("Generate");
        runBtn.addActionListener(e -> {
            LocalDate from = toLocalDate(fromSpinner);
            LocalDate to   = toLocalDate(toSpinner);
            output.setText(reportService.revenueReport(from, to));
        });
        controls.add(runBtn);

        p.add(controls,           BorderLayout.NORTH);
        p.add(new JScrollPane(output), BorderLayout.CENTER);
        return p;
    }

    // ── Today's summary ───────────────────────────────────────────────────────

    private JPanel buildSummaryTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextArea output = new JTextArea();
        output.setFont(Theme.FONT_MONO);
        output.setEditable(false);
        output.setBackground(new Color(248, 250, 252));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton runBtn = UIUtils.primaryBtn("Refresh");
        runBtn.addActionListener(e -> output.setText(reportService.todaySummary()));
        top.add(runBtn);

        output.setText(reportService.todaySummary());

        p.add(top,                     BorderLayout.NORTH);
        p.add(new JScrollPane(output), BorderLayout.CENTER);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JSpinner dateSpinner(LocalDate initial) {
        JSpinner sp = new JSpinner(new SpinnerDateModel());
        sp.setEditor(new JSpinner.DateEditor(sp, "yyyy-MM-dd"));
        sp.setPreferredSize(new Dimension(130, 34));
        sp.setValue(java.util.Date.from(initial.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        return sp;
    }

    private LocalDate toLocalDate(JSpinner sp) {
        java.util.Date d = (java.util.Date) sp.getValue();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
}
