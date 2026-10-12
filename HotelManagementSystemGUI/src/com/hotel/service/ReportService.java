package com.hotel.service;

import com.hotel.dao.BillDAO;
import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Bill;
import com.hotel.model.Booking;
import com.hotel.model.Room;

import java.time.LocalDate;
import java.util.List;

/**
 * ReportService — aggregates data from multiple DAOs to produce management reports.
 * All methods return formatted strings ready to be printed to the console.
 */
public class ReportService {

    private final BookingDAO bookingDAO;
    private final RoomDAO    roomDAO;
    private final BillDAO    billDAO;

    public ReportService() {
        this.bookingDAO = new BookingDAO();
        this.roomDAO    = new RoomDAO();
        this.billDAO    = new BillDAO();
    }

    // ── DAILY BOOKING REPORT ──────────────────────────────────────────────────

    public String dailyBookingReport(LocalDate date) {
        List<Booking> bookings = bookingDAO.findByDate(date);
        StringBuilder sb = new StringBuilder();
        String line = "─".repeat(80);

        sb.append("\n").append(line).append("\n");
        sb.append(String.format("  DAILY BOOKING REPORT — %s%n", date));
        sb.append(line).append("\n");
        sb.append(String.format("  %-4s  %-20s  %-6s  %-10s  %-10s  %-6s  %-12s%n",
                "#", "Customer", "Room", "Check-In", "Check-Out", "Nights", "Status"));
        sb.append(line).append("\n");

        if (bookings.isEmpty()) {
            sb.append("  No bookings found for this date.\n");
        } else {
            for (Booking b : bookings) {
                sb.append(String.format("  %-4d  %-20s  %-6s  %-10s  %-10s  %-6d  %-12s%n",
                        b.getBookingId(),
                        truncate(b.getCustomerName(), 20),
                        b.getRoomNumber(),
                        b.getCheckIn(),
                        b.getCheckOut(),
                        b.getNights(),
                        b.getStatus()));
            }
        }
        sb.append(line).append("\n");
        sb.append(String.format("  Total bookings: %d%n", bookings.size()));
        sb.append(line).append("\n");
        return sb.toString();
    }

    // ── AVAILABLE ROOMS REPORT ────────────────────────────────────────────────

    public String availableRoomsReport() {
        List<Room> rooms = roomDAO.findAvailable();
        StringBuilder sb = new StringBuilder();
        String line = "─".repeat(75);

        sb.append("\n").append(line).append("\n");
        sb.append("  AVAILABLE ROOMS REPORT\n");
        sb.append(line).append("\n");
        sb.append(String.format("  %-6s  %-8s  %-8s  %-10s  %s%n",
                "Room", "Type", "Floor", "₹/Night", "Description"));
        sb.append(line).append("\n");

        if (rooms.isEmpty()) {
            sb.append("  No rooms currently available.\n");
        } else {
            for (Room r : rooms) {
                sb.append(String.format("  %-6s  %-8s  %-8d  %-10.2f  %s%n",
                        r.getRoomNumber(),
                        r.getRoomType(),
                        r.getFloorNumber(),
                        r.getPricePerNight(),
                        truncate(r.getDescription(), 35)));
            }
        }
        sb.append(line).append("\n");
        sb.append(String.format("  Total available: %d / %d rooms%n",
                rooms.size(), roomDAO.findAll().size()));
        sb.append(line).append("\n");
        return sb.toString();
    }

    // ── REVENUE REPORT ────────────────────────────────────────────────────────

    public String revenueReport(LocalDate from, LocalDate to) {
        List<Bill> bills = billDAO.findPaidBetween(from, to);
        double totalRevenue = bills.stream().mapToDouble(Bill::getNetAmount).sum();
        double totalTax     = bills.stream().mapToDouble(Bill::getTaxAmount).sum();

        StringBuilder sb = new StringBuilder();
        String line = "─".repeat(70);

        sb.append("\n").append(line).append("\n");
        sb.append(String.format("  REVENUE REPORT  [%s  →  %s]%n", from, to));
        sb.append(line).append("\n");
        sb.append(String.format("  %-5s  %-20s  %-8s  %-10s  %-10s  %-8s%n",
                "Bill", "Customer", "Room", "Net Amt", "Tax", "Method"));
        sb.append(line).append("\n");

        if (bills.isEmpty()) {
            sb.append("  No paid bills in this period.\n");
        } else {
            for (Bill b : bills) {
                sb.append(String.format("  %-5d  %-20s  %-8s  %-10.2f  %-10.2f  %-8s%n",
                        b.getBillId(),
                        truncate(b.getCustomerName(), 20),
                        b.getRoomNumber(),
                        b.getNetAmount(),
                        b.getTaxAmount(),
                        b.getPaymentMethod() != null ? b.getPaymentMethod() : "-"));
            }
        }

        sb.append(line).append("\n");
        sb.append(String.format("  Total Bills     : %d%n",    bills.size()));
        sb.append(String.format("  Total Tax       : ₹%.2f%n", totalTax));
        sb.append(String.format("  TOTAL REVENUE   : ₹%.2f%n", totalRevenue));
        sb.append(line).append("\n");
        return sb.toString();
    }

    // ── TODAY'S SUMMARY ───────────────────────────────────────────────────────

    public String todaySummary() {
        LocalDate today  = LocalDate.now();
        List<Booking> ci = bookingDAO.findTodaysCheckIns();
        List<Booking> co = bookingDAO.findTodaysCheckOuts();
        List<Room>    av = roomDAO.findAvailable();
        double todayRev  = billDAO.dailyRevenue(today);

        String line = "─".repeat(50);
        return "\n" + line + "\n"
             + "  TODAY'S SUMMARY — " + today + "\n"
             + line + "\n"
             + String.format("  Expected Check-Ins  : %d%n",   ci.size())
             + String.format("  Expected Check-Outs : %d%n",   co.size())
             + String.format("  Available Rooms     : %d%n",   av.size())
             + String.format("  Today's Revenue     : ₹%.2f%n", todayRev)
             + line + "\n";
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }
}
